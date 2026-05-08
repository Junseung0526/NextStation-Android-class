package com.example.nextstation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nextstation.domain.model.ArrivalInfo
import com.example.nextstation.domain.model.RealTimeArrival
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
    val history by viewModel.history.collectAsState()
    val realTimeResults by viewModel.realTimeResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isDark = isSystemInDarkTheme()
    
    // Dynamic Gradient Background
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFE0E0), Color(0xFFE0E0FF)))
    }

    val permissionsToRequest = remember {
        mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        launcher.launch(permissionsToRequest)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = if (isDark) Color.White else Color(0xFF333333)
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                
                InputSection(
                    onSetAlarm = viewModel::setAlarm,
                    onSearchBus = { viewModel.searchBusArrival(it) },
                    realTimeResults = realTimeResults,
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.recent_history),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                HistoryList(history = history)
            }
        }
    }
}

@Composable
fun InputSection(
    onSetAlarm: (String, Int, String, String) -> Unit,
    onSearchBus: (String) -> Unit,
    realTimeResults: List<RealTimeArrival>,
    isLoading: Boolean
) {
    var destination by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("곧 도착 예정입니다.") }
    var stationId by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()
    val glassColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.6f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.3f)

    val isInputValid by remember {
        derivedStateOf {
            destination.isNotBlank() && (minutes.toIntOrNull() ?: 0) > 0
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        color = glassColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Smart Setup",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color(0xFFBB86FC) else Color(0xFF6200EE)
            )

            // Bus Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassTextField(
                    value = stationId,
                    onValueChange = { stationId = it },
                    label = "Station ID",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                Button(
                    onClick = { onSearchBus(stationId) },
                    enabled = stationId.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFFBB86FC) else Color(0xFF6200EE)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Search, null)
                    }
                }
            }

            // Real-time Results
            if (realTimeResults.isNotEmpty()) {
                LazyColumn(modifier = Modifier.heightIn(max = 140.dp)) {
                    items(realTimeResults) { result ->
                        val arrivalMinutes = result.arrivalMessage.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                        Surface(
                            onClick = {
                                destination = result.stationName
                                minutes = arrivalMinutes.toString()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${result.busNumber}번", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                Text(result.arrivalMessage, color = if (isDark) Color.White.copy(0.7f) else Color.Black.copy(0.7f))
                            }
                        }
                    }
                }
            }
            
            GlassTextField(
                value = destination,
                onValueChange = { destination = it },
                label = stringResource(id = R.string.destination_label),
                leadingIcon = Icons.Default.LocationOn
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassTextField(
                    value = minutes,
                    onValueChange = { minutes = it },
                    label = "Min",
                    modifier = Modifier.weight(0.8f),
                    keyboardType = KeyboardType.Number,
                    leadingIcon = Icons.Default.Info
                )
                
                GlassTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Phone",
                    modifier = Modifier.weight(1.2f),
                    keyboardType = KeyboardType.Phone,
                    leadingIcon = Icons.Default.Phone
                )
            }
            
            Button(
                onClick = {
                    if (isInputValid) {
                        onSetAlarm(destination, minutes.toInt(), phoneNumber, message)
                        destination = ""
                        minutes = ""
                    }
                },
                enabled = isInputValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF03DAC6) else Color(0xFFE91E63),
                    disabledContainerColor = if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.1f)
                )
            ) {
                Text("Reserve Now", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val isDark = isSystemInDarkTheme()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f)) },
        leadingIcon = leadingIcon?.let { { Icon(it, null, tint = if (isDark) Color.White.copy(0.7f) else Color.Black.copy(0.7f)) } },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = if (isDark) Color.White.copy(0.2f) else Color.Black.copy(0.1f),
            focusedBorderColor = if (isDark) Color(0xFFBB86FC) else Color(0xFF6200EE),
            cursorColor = if (isDark) Color.White else Color.Black
        )
    )
}

@Composable
fun HistoryList(history: List<ArrivalInfo>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(
            items = history,
            key = { it.id }
        ) { info ->
            HistoryItem(info = info)
        }
    }
}

@Composable
fun HistoryItem(info: ArrivalInfo) {
    val isDark = isSystemInDarkTheme()
    val glassColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.4f)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = glassColor,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = if (isDark) Color(0xFFBB86FC) else Color(0xFF6200EE)
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = info.destinationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    text = "ID: ${info.id} | Contact: ${info.phoneNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f)
                )
            }
        }
    }
}
