package io.github.mattpvaughn.chronicle.features.login

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.github.mattpvaughn.chronicle.application.ChronicleApplication
import io.github.mattpvaughn.chronicle.data.model.ServerModel
import io.github.mattpvaughn.chronicle.databinding.OnboardingPlexChooseServerBinding
import io.github.mattpvaughn.chronicle.ui.theme.AppTheme
import javax.inject.Inject

class ChooseServerFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = ChooseServerFragment()

        const val TAG = "Choose server fragment"
    }

    @Inject
    lateinit var viewModelFactory: ChooseServerViewModel.Factory
    private lateinit var viewModel: ChooseServerViewModel

    override fun onAttach(context: Context) {
        ((activity as Activity).application as ChronicleApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ServerListItem(server: ServerModel) {
        ListItem(modifier = Modifier.clickable { viewModel.chooseServer(server) }, headlineText = {
            Text(
                text = server.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
        }, trailingContent = {
            if (server.owned) Icon(
                Icons.Outlined.Star,
                contentDescription = "owned",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }, leadingContent = {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(color = MaterialTheme.colorScheme.primaryContainer),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = server.name[0].toString(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        })
    }

    @Composable
    fun ChooseServerScreen(viewModel: ChooseServerViewModel) {
        val servers by viewModel.servers.observeAsState()

        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = "Choose Server",
                    modifier = Modifier.padding(top = 56.dp, bottom = 28.dp),
                    style = MaterialTheme.typography.headlineMedium
                )

                servers?.let { servers ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                    ) {
                        items(servers.size) { index ->
                            val server = servers[index]
                            ServerListItem(server = server)
                            if (index != servers.size - 1) Divider()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            viewModelStore, viewModelFactory
        )[ChooseServerViewModel::class.java]

        val binding = OnboardingPlexChooseServerBinding.inflate(inflater, container, false).apply {
            composeView.setContent {
                AppTheme {
                    ChooseServerScreen(viewModel)
                }
            }
        }

        return binding.root
    }
}
