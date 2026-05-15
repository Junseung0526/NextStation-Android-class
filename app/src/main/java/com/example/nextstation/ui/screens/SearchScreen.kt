package com.example.nextstation.ui.screens

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nextstation.domain.model.RouteInfo
import com.example.nextstation.ui.components.GlassCard
import com.example.nextstation.ui.main.MainViewModel
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@Composable
fun SearchScreen(viewModel: MainViewModel) {
    val routeResults by viewModel.routeResults.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val estimatedTime by viewModel.estimatedTime.collectAsStateWithLifecycle()

    var destinationQuery by remember { mutableStateOf("") }
    var selectedRoute by remember { mutableStateOf<RouteInfo?>(null) }
    var alarmMinutes by remember { mutableStateOf("5") }
    val phoneNumber by viewModel.defaultPhoneNumber.collectAsStateWithLifecycle()

    val colorScheme = MaterialTheme.colorScheme
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    
    // MapView instance management
    val mapView = remember { 
        MapView(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full Screen Map Section
        AndroidView(
            factory = {
                mapView.apply {
                    start(object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            android.util.Log.d("NextStation_Map", "KakaoMap Destroyed")
                        }

                        override fun onMapError(error: Exception) {
                            android.util.Log.e("NextStation_Map", "KakaoMap Error", error)
                        }
                    }, object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            android.util.Log.d("NextStation_Map", "KakaoMap is ready")
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Floating Top Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = { /* Back or Clear */ }) {
                    Icon(Icons.Default.Search, null, tint = colorScheme.primary)
                }
                TextField(
                    value = destinationQuery,
                    onValueChange = { destinationQuery = it },
                    placeholder = { Text("어디로 가시나요?") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { viewModel.searchRoutes(destinationQuery) }) {
                        Icon(Icons.Default.DirectionsBus, null, tint = colorScheme.primary)
                    }
                }
            }
        }

        // Bottom Content (Results or Selection)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            AnimatedContent(
                targetState = selectedRoute,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                },
                label = "search_content"
            ) { targetRoute ->
                if (targetRoute == null) {
                    if (routeResults.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "검색 결과", 
                                    style = MaterialTheme.typography.titleSmall, 
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                                )
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(routeResults) { route ->
                                        RouteItem(route) {
                                            selectedRoute = route
                                            viewModel.estimateTravelTime(126.9780, 37.5665, 127.0000, 37.6000)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${targetRoute.busNumber}번",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${targetRoute.stNm} 승차",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    val timeText = estimatedTime?.let { "${it / 60}분 소요 예상" } ?: "소요 시간 계산 중..."
                                    Text(
                                        text = timeText,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("알림 설정: ", style = MaterialTheme.typography.labelLarge)
                                Text("${alarmMinutes}분 전", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = alarmMinutes.toFloatOrNull() ?: 5f,
                                onValueChange = { alarmMinutes = it.toInt().toString() },
                                valueRange = 1f..15f,
                                steps = 14
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { selectedRoute = null },
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("취소")
                                }
                                Button(
                                    onClick = {
                                        viewModel.setAlarm(destinationQuery, alarmMinutes.toInt(), phoneNumber, "곧 도착합니다!")
                                    },
                                    modifier = Modifier.weight(2f).height(52.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("알림 예약", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteItem(route: RouteInfo, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = route.busNumber, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 20.sp,
                        color = colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colorScheme.primaryContainer,
                    ) {
                        val busTypeStr = when(route.busType) {
                            "1" -> "저상"
                            "2" -> "굴절"
                            else -> "일반"
                        }
                        Text(
                            busTypeStr, 
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "${route.stNm} 승차", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = route.firstArrivalMessage, 
                    color = colorScheme.primary, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                val congestionText = when (route.firstCongestion) {
                    3 -> "여유"
                    4 -> "보통"
                    5 -> "혼잡"
                    else -> ""
                }
                if (congestionText.isNotEmpty()) {
                    Text(
                        text = congestionText,
                        color = when(route.firstCongestion) {
                            3 -> Color(0xFF10B981)
                            4 -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
