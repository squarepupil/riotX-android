/*
 * Copyright (c) 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.riotx.features.terms

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.airbnb.mvrx.viewModel
import im.vector.matrix.android.api.session.terms.TermsService
import im.vector.riotx.R
import im.vector.riotx.core.di.ScreenComponent
import im.vector.riotx.core.error.ErrorFormatter
import im.vector.riotx.core.extensions.exhaustive
import im.vector.riotx.core.extensions.replaceFragment
import im.vector.riotx.core.platform.SimpleFragmentActivity
import javax.inject.Inject

class ReviewTermsActivity : SimpleFragmentActivity() {

    @Inject lateinit var errorFormatter: ErrorFormatter
    @Inject lateinit var viewModelFactory: ReviewTermsViewModel.Factory

    private val reviewTermsViewModel: ReviewTermsViewModel by viewModel()

    override fun injectWith(injector: ScreenComponent) {
        super.injectWith(injector)
        injector.inject(this)
    }

    override fun initUiAndData() {
        super.initUiAndData()

        if (isFirstCreation()) {
            replaceFragment(R.id.container, ReviewTermsFragment::class.java)
        }

        reviewTermsViewModel.termsArgs = intent.getParcelableExtra(EXTRA_INFO) ?: error("Missing parameter")

        reviewTermsViewModel.observeViewEvents {
            when (it) {
                is ReviewTermsViewEvents.Loading -> Unit
                is ReviewTermsViewEvents.Failure -> {
                    AlertDialog.Builder(this)
                            .setMessage(errorFormatter.toHumanReadable(it.throwable))
                            .setPositiveButton(R.string.ok) { _, _ ->
                                if (it.finish) {
                                    finish()
                                }
                            }
                            .show()
                    Unit
                }
                ReviewTermsViewEvents.Success    -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }.exhaustive
        }
    }

    companion object {
        const val TERMS_REQUEST_CODE = 15000
        private const val EXTRA_INFO = "EXTRA_INFO"

        fun intent(context: Context, serviceType: TermsService.ServiceType, baseUrl: String, token: String?): Intent {
            return Intent(context, ReviewTermsActivity::class.java).also {
                it.putExtra(EXTRA_INFO, ServiceTermsArgs(serviceType, baseUrl, token))
            }
        }
    }
}
