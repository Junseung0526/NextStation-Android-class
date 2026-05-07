package com.example.nextstation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nextstation.ui.main.MainViewModel
import com.example.nextstation.ui.theme.NextStationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NextStationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    var destination by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("곧 도착 예정입니다.") }

    val history by viewModel.history.collectAsState()

    val permissionsToRequest = mutableListOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.VIBRATE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle results if needed
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissionsToRequest.toTypedArray())
    }

    androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(androidx.compose.ui.res.stringResource(id = R.string.app_name)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text(androidx.compose.ui.res.stringResource(id = R.string.destination_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = minutes,
                onValueChange = { minutes = it },
                label = { Text(androidx.compose.ui.res.stringResource(id = R.string.minutes_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(androidx.compose.ui.res.stringResource(id = R.string.phone_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(androidx.compose.ui.res.stringResource(id = R.string.message_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val mins = minutes.toIntOrNull() ?: 0
                    if (destination.isNotBlank() && mins > 0) {
                        viewModel.setAlarm(destination, mins, phoneNumber, message)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(androidx.compose.ui.res.stringResource(id = R.string.reserve_button))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(androidx.compose.ui.res.stringResource(id = R.string.recent_history), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(history) { info ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = info.destinationName, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "연락처: ${info.phoneNumber}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}