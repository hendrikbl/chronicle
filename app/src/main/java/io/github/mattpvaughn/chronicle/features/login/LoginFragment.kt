package io.github.mattpvaughn.chronicle.features.login

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.github.mattpvaughn.chronicle.R
import io.github.mattpvaughn.chronicle.application.ChronicleApplication
import io.github.mattpvaughn.chronicle.data.local.PrefsRepo
import io.github.mattpvaughn.chronicle.databinding.OnboardingLoginBinding
import io.github.mattpvaughn.chronicle.ui.theme.AppTheme
import timber.log.Timber
import javax.inject.Inject

class LoginFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()

        const val TAG: String = "Login tag"
    }

    @Inject
    lateinit var prefsRepo: PrefsRepo

    @Inject
    lateinit var viewModelFactory: LoginViewModel.Factory

    private lateinit var loginViewModel: LoginViewModel

    override fun onAttach(context: Context) {
        ((activity as Activity).application as ChronicleApplication).appComponent.inject(this)
        super.onAttach(context)
    }


    @Composable
    fun LoginScreen(loginViewModel: LoginViewModel) {

        val isLoading by loginViewModel.isLoading.observeAsState()

        LoginScreen(
            isLoading = (isLoading == true),
            onClickLogin = { loginViewModel.loginWithOAuth() })
    }

    @Composable
    fun LoginScreen(isLoading: Boolean, onClickLogin: () -> Unit) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) CircularProgressIndicator()
                else Button(onClick = { onClickLogin() }) {
                    Text(
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.plex_button_padding_horizontal),
                        ), text = "Login with Plex"
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun LoginScreenPreview() {
        LoginScreen(isLoading = true, onClickLogin = { })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loginViewModel = ViewModelProvider(
            this, viewModelFactory
        )[LoginViewModel::class.java]

        loginViewModel.authEvent.observe(viewLifecycleOwner, Observer { authRequestEvent ->
            val oAuthPin = authRequestEvent.getContentIfNotHandled()
            if (oAuthPin != null) {
                val backButton = resources.getDrawable(
                    R.drawable.ic_arrow_back_white, requireActivity().theme
                ).apply { setTint(Color.BLACK) }
                val backButtonBitmap: Bitmap? =
                    if (backButton is BitmapDrawable) backButton.bitmap else null

                val customTabsIntentBuilder = CustomTabsIntent.Builder().setShowTitle(true)

                if (backButtonBitmap != null) {
                    customTabsIntentBuilder.setCloseButtonIcon(backButtonBitmap)
                }

                val customTabsIntent = customTabsIntentBuilder.build()

                // make login url
                val url = loginViewModel.makeOAuthLoginUrl(oAuthPin.clientIdentifier, oAuthPin.code)

                loginViewModel.setLaunched(true)
                customTabsIntent.launchUrl(requireContext(), url)
            }
        })

        val binding = OnboardingLoginBinding.inflate(inflater, container, false).apply {
            composeView.setContent {
                AppTheme {
                    LoginScreen(loginViewModel)
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        Timber.i("RESUMING LoginFragment")
        loginViewModel.checkForAccess()
        super.onResume()
    }
}
