package com.example.nextstation.ui.screens

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import com.example.nextstation.util.EmulatorDetector
import com.example.nextstation.util.NetworkUtils
import android.widget.Toast

@Composable
fun MockMapView(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw map grid lines
            val gridSpacing = 80.dp.toPx()
            val gridColor = colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
            
            // Vertical grid lines
            var x = 0f
            while (x < width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx()
                )
                x += gridSpacing
            }

            // Horizontal grid lines
            var y = 0f
            while (y < height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += gridSpacing
            }

            // Draw a mock road/path line
            val pathColor = colorScheme.primary.copy(alpha = 0.15f)
            val roadWidth = 24.dp.toPx()
            
            // Draw a main diagonal road
            drawLine(
                color = pathColor,
                start = Offset(width * 0.1f, height * 0.3f),
                end = Offset(width * 0.9f, height * 0.7f),
                strokeWidth = roadWidth,
                cap = StrokeCap.Round
            )

            // Draw an intersection road
            drawLine(
                color = pathColor,
                start = Offset(width * 0.5f, 0f),
                end = Offset(width * 0.5f, height),
                strokeWidth = roadWidth,
                cap = StrokeCap.Round
            )
            
            // Draw a mock route line in primary color (dotted/dashed)
            val routeColor = colorScheme.primary.copy(alpha = 0.6f)
            val routePath = Path().apply {
                moveTo(width * 0.5f, height * 0.1f)
                lineTo(width * 0.5f, height * 0.5f)
                lineTo(width * 0.8f, height * 0.65f)
            }
            
            drawPath(
                path = routePath,
                color = routeColor,
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(20f, 10f),
                        0f
                    )
                )
            )

            // Draw current location dot with pulse
            val locationCenter = Offset(width * 0.5f, height * 0.5f)
            
            // Dotted pulse
            drawCircle(
                color = colorScheme.primary,
                radius = 16.dp.toPx() * pulseScale,
                center = locationCenter,
                alpha = pulseAlpha
            )

            // Central solid dot
            drawCircle(
                color = colorScheme.primary,
                radius = 8.dp.toPx(),
                center = locationCenter
            )

            // Draw a mock bus stop marker
            val stopCenter = Offset(width * 0.5f, height * 0.25f)
            drawCircle(
                color = colorScheme.secondary,
                radius = 6.dp.toPx(),
                center = stopCenter
            )
        }

        // Tag indicating Emulator/Mock mode
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 80.dp), // Clear bottom nav bar
            shape = RoundedCornerShape(8.dp),
            color = colorScheme.surfaceVariant.copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Mock Map (Emulator Mode)",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun KakaoMapView(
    modifier: Modifier = Modifier,
    onMapReady: (KakaoMap) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Remember the MapView instance across recompositions
    val mapView = remember { MapView(context) }

    // 2. Manage Lifecycle events strictly
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> { /* No specific start for MapView v2 */ }
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                Lifecycle.Event.ON_STOP -> { /* No specific stop for MapView v2 */ }
                Lifecycle.Event.ON_DESTROY -> {
                    // MapView cleanup is handled by the SDK usually, 
                    // but we ensure it stops rendering.
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 3. Use AndroidView with a stable factory and update block
    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                // Initialize the map only once in the factory
                start(object : MapLifeCycleCallback() {
                    override fun onMapDestroy() {
                        android.util.Log.d("NextStation_Map", "KakaoMap Destroyed")
                    }

                    override fun onMapError(error: Exception) {
                        android.util.Log.e("NextStation_Map", "KakaoMap Error", error)
                    }
                }, object : KakaoMapReadyCallback() {
                    override fun onMapReady(kakaoMap: KakaoMap) {
                        onMapReady(kakaoMap)
                        android.util.Log.d("NextStation_Map", "KakaoMap is ready")
                    }
                })
            }
        },
        update = {
            // Logic to update the map when state changes can go here
        }
    )
}

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit
) {
    val routeResults by viewModel.routeResults.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val estimatedTime by viewModel.estimatedTime.collectAsStateWithLifecycle()
    val searchQueryState by viewModel.searchQuery.collectAsStateWithLifecycle()

    var destinationQuery by remember { mutableStateOf("") }
    var selectedRoute by remember { mutableStateOf<RouteInfo?>(null) }
    var alarmMinutes by remember { mutableStateOf("5") }
    val phoneNumber by viewModel.defaultPhoneNumber.collectAsStateWithLifecycle()

    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    // Sync viewModel.searchQuery to local destinationQuery when it changes
    LaunchedEffect(searchQueryState) {
        if (searchQueryState.isNotEmpty()) {
            destinationQuery = searchQueryState
            viewModel.searchRoutes(searchQueryState) // Auto trigger search
            viewModel.updateSearchQuery("") // Clear the flow
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Render MockMapView on emulator to prevent native lib loading and lag, else use real KakaoMapView
        if (EmulatorDetector.isEmulator()) {
            MockMapView(modifier = Modifier.fillMaxSize())
        } else {
            // Optimized KakaoMapView
            KakaoMapView(
                modifier = Modifier.fillMaxSize(),
                onMapReady = { kakaoMap ->
                    // Center camera on user's current location to resolve the Pangyo Station issue
                    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                    try {
                        val loc = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                            ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                        // Use actual GPS if inside Seoul bounds, else use dummy Seoul location (Gangnam Station)
                        val targetLatLng = if (loc != null && loc.latitude in 37.42..37.70 && loc.longitude in 126.75..127.20) {
                            com.kakao.vectormap.LatLng.from(loc.latitude, loc.longitude)
                        } else {
                            com.kakao.vectormap.LatLng.from(37.4979, 127.0276) // Dummy Gangnam Station
                        }
                        val cameraUpdate = com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(targetLatLng, 15)
                        kakaoMap.moveCamera(cameraUpdate)
                    } catch (e: SecurityException) {
                        val targetLatLng = com.kakao.vectormap.LatLng.from(37.4979, 127.0276)
                        val cameraUpdate = com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(targetLatLng, 15)
                        kakaoMap.moveCamera(cameraUpdate)
                    }
                }
            )
        }
        
        // Floating Top Search Bar (rest of the UI remains the same)
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
                    IconButton(onClick = {
                        if (NetworkUtils.isNetworkAvailable(context)) {
                            viewModel.searchRoutes(destinationQuery)
                        } else {
                            Toast.makeText(context, "네트워크 연결이 끊겼습니다. 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
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
                                            
                                            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                                            val (startX, startY) = try {
                                                val loc = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                                                    ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                                                // Use actual GPS if inside Seoul bounds, else use dummy Seoul location (Gangnam Station)
                                                if (loc != null && loc.latitude in 37.42..37.70 && loc.longitude in 126.75..127.20) {
                                                    loc.longitude to loc.latitude
                                                } else {
                                                    127.0276 to 37.4979 // Dummy Gangnam Station
                                                }
                                            } catch (e: SecurityException) {
                                                127.0276 to 37.4979
                                            }
                                            
                                            if (NetworkUtils.isNetworkAvailable(context)) {
                                                viewModel.estimateTravelTime(
                                                    startX = startX,
                                                    startY = startY,
                                                    endX = route.gpsX ?: 127.0000,
                                                    endY = route.gpsY ?: 37.6000
                                                )
                                            } else {
                                                Toast.makeText(context, "네트워크 연결이 끊겼습니다.", Toast.LENGTH_SHORT).show()
                                            }
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
                                        viewModel.setAlarm(
                                            destination = targetRoute.stNm,
                                            leadMinutes = alarmMinutes.toIntOrNull() ?: 5,
                                            phoneNumber = phoneNumber,
                                            message = "버스(${targetRoute.busNumber})가 곧 도착 예정입니다.",
                                            arsId = targetRoute.arsId,
                                            busNumber = targetRoute.busNumber,
                                            gpsX = targetRoute.gpsX,
                                            gpsY = targetRoute.gpsY
                                        )
                                        // Reset search state
                                        destinationQuery = ""
                                        selectedRoute = null
                                        viewModel.clearSearchResults()
                                        // Navigate to Home
                                        onNavigateToHome()
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
