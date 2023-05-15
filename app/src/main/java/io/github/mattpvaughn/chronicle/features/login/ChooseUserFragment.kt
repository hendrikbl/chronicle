package io.github.mattpvaughn.chronicle.features.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import io.github.mattpvaughn.chronicle.application.ChronicleApplication
import io.github.mattpvaughn.chronicle.data.sources.plex.IPlexLoginRepo
import io.github.mattpvaughn.chronicle.data.sources.plex.PlexConfig
import io.github.mattpvaughn.chronicle.data.sources.plex.model.PlexUser
import io.github.mattpvaughn.chronicle.databinding.OnboardingPlexChooseUserBinding
import io.github.mattpvaughn.chronicle.ui.theme.AppTheme
import javax.inject.Inject

/** Handles the picking of user profiles. */
class ChooseUserFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = ChooseUserFragment()

        const val TAG = "Choose user fragment"
    }

    @Inject
    lateinit var viewModelFactory: ChooseUserViewModel.Factory
    private lateinit var viewModel: ChooseUserViewModel

    @Inject
    lateinit var plexLoginRepo: IPlexLoginRepo

    @Inject
    lateinit var plexConfig: PlexConfig

    @Composable
    fun ProfileGridItem(user: PlexUser) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(model = user.thumb,
                contentDescription = null,
                modifier = Modifier
                    .width(128.dp)
                    .height(128.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable { viewModel.pickUser(user) })
            Text(text = user.title, style = MaterialTheme.typography.labelLarge)
        }
    }

    @Composable
    fun ChooseUserScreen(viewModel: ChooseUserViewModel) {
        val users by viewModel.users.observeAsState()

        users?.let { users ->
            Surface {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var otpValue by remember {
                        mutableStateOf("")
                    }
                    val showPin by viewModel.showPin.observeAsState()
                    var pinError by remember {
                        mutableStateOf<String?>(null)
                    }

                    fun onPinChange(value: String, complete: Boolean) {
                        otpValue = value
                        if (complete) {
                            viewModel.setPinData(value)
                            viewModel.submitPin()
                        }
                    }

                    viewModel.userMessage.observe(viewLifecycleOwner) {
                        if (!it.hasBeenHandled) {
                            pinError = it.getContentIfNotHandled()
                            otpValue = ""
                        }
                    }

                    if (showPin != null && showPin == true) {
                        Text(
                            text = "PLEX Pin required",
                            modifier = Modifier.padding(top = 56.dp, bottom = 28.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        OtpTextField(
                            otpText = otpValue, onOtpTextChange = { value, otpInputFilled ->
                                onPinChange(value, otpInputFilled)
                            }, otpCount = 4
                        )
                        pinError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Who is listening?",
                            modifier = Modifier.padding(top = 56.dp, bottom = 28.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.padding(horizontal = 56.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(users.size) { index ->
                                ProfileGridItem(user = users[index])
                            }
                        }
                    }
                }
            }
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        ((activity as Activity).application as ChronicleApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            viewModelStore, viewModelFactory
        )[ChooseUserViewModel::class.java]

        val binding = OnboardingPlexChooseUserBinding.inflate(inflater, container, false).apply {
            composeView.setContent {
                AppTheme {
                    ChooseUserScreen(viewModel)
                }
            }
        }

        return binding.root
    }

    fun isPinEntryScreenVisible(): Boolean {
        return viewModel.showPin.value == true
    }

    fun hidePinEntryScreen() {
        viewModel.hidePinScreen()
    }
}