package com.example.nextstation.ui.screens

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nextstation.ui.components.GlassCard
import com.example.nextstation.ui.components.PulseAnimation
import com.example.nextstation.ui.main.MainViewModel
import com.example.nextstation.util.EmulatorDetector
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val isAlarmRunning by viewModel.isAlarmRunning.collectAsStateWithLifecycle()
    val destinationName by viewModel.activeAlarmDestination.collectAsStateWithLifecycle()
    val busNumber by viewModel.activeAlarmBusNumber.collectAsStateWithLifecycle()
    val remainingMinutes by viewModel.activeAlarmRemainingMinutes.collectAsStateWithLifecycle()
    val destGpsX by viewModel.activeAlarmGpsX.collectAsStateWithLifecycle()
    val destGpsY by viewModel.activeAlarmGpsY.collectAsStateWithLifecycle()
    val leadMinutes by viewModel.activeAlarmLeadMinutes.collectAsStateWithLifecycle()

    // Real-time location tracking
    var currentLocation by remember { mutableStateOf<Location?>(null) }

    val displayLocation = remember(currentLocation) {
        val loc = currentLocation
        if (loc != null) {
            // Check if in Seoul: Latitude 37.42 to 37.70, Longitude 126.75 to 127.20
            val inSeoul = loc.latitude in 37.42..37.70 && loc.longitude in 126.75..127.20
            if (inSeoul) {
                loc to false
            } else {
                // Return dummy Seoul location (Gangnam Station)
                val dummyLoc = Location("").apply {
                    latitude = 37.4979
                    longitude = 127.0276
                }
                dummyLoc to true
            }
        } else {
            // Default to dummy Seoul location before GPS fix
            val dummyLoc = Location("").apply {
                latitude = 37.4979
                longitude = 127.0276
            }
            dummyLoc to true
        }
    }
    val effectiveLocation = displayLocation.first
    val isDummyLocationUsed = displayLocation.second

    val distanceMeters = remember(effectiveLocation, destGpsY, destGpsX) {
        val startLoc = effectiveLocation
        if (destGpsY != 0.0 && destGpsX != 0.0) {
            val destLoc = Location("").apply {
                latitude = destGpsY
                longitude = destGpsX
            }
            startLoc.distanceTo(destLoc)
        } else {
            null
        }
    }

    val fallbackRemainingMinutes = remember(distanceMeters) {
        if (distanceMeters != null) {
            val mins = (distanceMeters / 1000.0 * 2.4).toInt() + 3
            if (mins < 1) 1 else mins
        } else {
            15
        }
    }

    val routePath by viewModel.routePath.collectAsStateWithLifecycle()

    LaunchedEffect(effectiveLocation, destGpsY, destGpsX) {
        if (destGpsY != 0.0 && destGpsX != 0.0) {
            viewModel.fetchRoutePath(
                startX = effectiveLocation.longitude,
                startY = effectiveLocation.latitude,
                endX = destGpsX,
                endY = destGpsY
            )
        }
    }

    DisposableEffect(context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            currentLocation = lastGps ?: lastNetwork

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                3000L, // 3 seconds
                2f,    // 2 meters
                listener
            )
        } catch (e: SecurityException) {
            // Permission or emulator issues
        }

        onDispose {
            try {
                locationManager.removeUpdates(listener)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("실시간 하차 안내", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (!isAlarmRunning) {
                // No active alarm state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            null,
                            modifier = Modifier.size(72.dp),
                            tint = colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "현재 진행 중인 하차 알림이 없습니다.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "새로운 알림을 설정해 보세요!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.outline
                        )
                    }
                }
            } else {
                // Active Alarm Info Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colorScheme.primaryContainer.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PulseAnimation(
                                modifier = Modifier.fillMaxSize(),
                                color = colorScheme.primary
                            )
                            Surface(
                                shape = CircleShape,
                                color = colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    null,
                                    tint = colorScheme.onPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${busNumber}번 버스 탑승 중",
                                style = MaterialTheme.typography.labelLarge,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = destinationName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${leadMinutes}분 전 알림 예약",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "남은 예상 시간",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                            val displayMinutes = remainingMinutes ?: fallbackRemainingMinutes
                            val remainingText = displayMinutes?.let { "약 ${displayMinutes}분" } ?: "계산 중..."
                            Text(
                                text = remainingText,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.primary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colorScheme.secondaryContainer,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "실시간 위치 추적 중",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Traffic Analysis Card (신뢰성 강화)
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "실시간 운행 분석",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("연동 채널", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (remainingMinutes != null) "공공 API 연동" else "GPS 예측 엔진",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingMinutes != null) colorScheme.primary else colorScheme.secondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("남은 예상 거리", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                val distText = distanceMeters?.let {
                                    if (it > 1000) String.format("%.2f km", it / 1000.0)
                                    else "${it.toInt()} m"
                                } ?: "측정 중..."
                                Text(
                                    text = distText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("남은 정류장 (예상)", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                val stopsText = distanceMeters?.let {
                                    val stops = (it / 400.0).toInt()
                                    if (stops < 1) "곧 도착" else "약 ${stops}개 정류장"
                                } ?: "분석 중..."
                                Text(
                                    text = stopsText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("분석 기준 속도", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                Text(
                                    text = "평균 25 km/h",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dummy GPS Warning Banner
                if (isDummyLocationUsed) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = colorScheme.errorContainer.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "서울 외 지역이므로 더미 GPS(강남역)로 시뮬레이션 중입니다.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Progress Route Bar
                Text(
                    text = "운행 경로 및 현재 위치",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                RouteProgressBar(
                    currentLocation = effectiveLocation,
                    destLat = destGpsY,
                    destLng = destGpsX,
                    destName = destinationName
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Map Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (EmulatorDetector.isEmulator()) {
                            MockDetailMapView(
                                modifier = Modifier.fillMaxSize(),
                                destinationName = destinationName
                            )
                        } else {
                            DetailKakaoMapView(
                                modifier = Modifier.fillMaxSize(),
                                currentLocation = effectiveLocation,
                                destLat = destGpsY,
                                destLng = destGpsX,
                                routePath = routePath
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Control Button
                Button(
                    onClick = {
                        viewModel.stopAlarm()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) {
                    Icon(Icons.Default.NotificationsOff, null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "하차 알림 안내 종료",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun RouteProgressBar(
    currentLocation: Location?,
    destLat: Double,
    destLng: Double,
    destName: String
) {
    val colorScheme = MaterialTheme.colorScheme

    val distanceMeters = currentLocation?.let { curr ->
        if (destLat != 0.0 && destLng != 0.0) {
            val destLoc = Location("").apply {
                latitude = destLat
                longitude = destLng
            }
            curr.distanceTo(destLoc)
        } else null
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Current Loc Node
                Surface(
                    shape = CircleShape,
                    color = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(8.dp)) {}
                    }
                }
                Text("내 위치", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                val latLngText = currentLocation?.let {
                    String.format("%.4f, %.4f", it.latitude, it.longitude)
                } ?: "GPS 탐색 중..."
                Text(
                    text = latLngText,
                    fontSize = 9.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            // Progress Line
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "bus_move")
                val busProgress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "bus_pos"
                )

                Canvas(modifier = Modifier.fillMaxWidth()) {
                    val width = size.width
                    val height = size.height

                    // Background line
                    drawLine(
                        color = colorScheme.outlineVariant,
                        start = Offset(0f, height / 2),
                        end = Offset(width, height / 2),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Active animated progress line
                    drawLine(
                        color = colorScheme.primary,
                        start = Offset(0f, height / 2),
                        end = Offset(width * busProgress, height / 2),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Bus Icon moving
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = 120.dp * busProgress) // Animated offset
                            .size(18.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Destination Node
                Surface(
                    shape = CircleShape,
                    color = colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Place,
                        null,
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Text(
                    text = destName.take(6) + if (destName.length > 6) ".." else "",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                val distText = distanceMeters?.let {
                    if (it > 1000) String.format("%.1f km 남음", it / 1000)
                    else "${it.toInt()}m 남음"
                } ?: "위치 계산 중..."
                Text(
                    text = distText,
                    fontSize = 10.sp,
                    color = colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MockDetailMapView(
    modifier: Modifier = Modifier,
    destinationName: String
) {
    val colorScheme = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_alarm")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.background(colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Grid lines for map details
            val spacing = 60.dp.toPx()
            val gridColor = colorScheme.onSurfaceVariant.copy(alpha = 0.06f)
            var x = 0f
            while (x < width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, height), 1f)
                x += spacing
            }
            var y = 0f
            while (y < height) {
                drawLine(gridColor, Offset(0f, y), Offset(width, y), 1f)
                y += spacing
            }

            // Draw a road layout
            val roadColor = colorScheme.primary.copy(alpha = 0.12f)
            val roadWidth = 32.dp.toPx()
            
            // Loop route path
            val routePath = Path().apply {
                moveTo(width * 0.2f, height * 0.8f)
                lineTo(width * 0.5f, height * 0.6f)
                lineTo(width * 0.5f, height * 0.3f)
                lineTo(width * 0.8f, height * 0.2f)
            }
            
            drawPath(
                path = routePath,
                color = roadColor,
                style = Stroke(width = roadWidth, cap = StrokeCap.Round)
            )

            // Draw driving route indicator line
            drawPath(
                path = routePath,
                color = colorScheme.primary.copy(alpha = 0.7f),
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f),
                    cap = StrokeCap.Round
                )
            )

            // Current location (user)
            val userPos = Offset(width * 0.35f, height * 0.7f) // Moving position mock
            drawCircle(
                color = colorScheme.primary,
                radius = 14.dp.toPx() * pulseScale,
                center = userPos,
                alpha = pulseAlpha
            )
            drawCircle(
                color = colorScheme.primary,
                radius = 7.dp.toPx(),
                center = userPos
            )

            // Destination Location (bus stop)
            val destPos = Offset(width * 0.8f, height * 0.2f)
            drawCircle(
                color = colorScheme.secondary,
                radius = 9.dp.toPx(),
                center = destPos
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = destPos
            )
        }

        // Info Overlay
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, modifier = Modifier.size(12.dp), tint = colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("에뮬레이터: 시뮬레이션 맵 모드 활성화", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetailKakaoMapView(
    modifier: Modifier = Modifier,
    currentLocation: Location?,
    destLat: Double,
    destLng: Double,
    routePath: List<Pair<Double, Double>>
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // Update map camera when location changes
    LaunchedEffect(currentLocation) {
        if (currentLocation != null) {
            // Since we can't easily retrieve the MapInstance directly from Compose without callback,
            // we will let the KakaoMapView callback handle the initial centering.
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                start(object : MapLifeCycleCallback() {
                    override fun onMapDestroy() {
                        android.util.Log.d("NextStation_Map", "DetailKakaoMap Destroyed")
                    }

                    override fun onMapError(error: Exception) {
                        android.util.Log.e("NextStation_Map", "DetailKakaoMap Error", error)
                    }
                }, object : KakaoMapReadyCallback() {
                    override fun onMapReady(kakaoMap: KakaoMap) {
                        // Center on user's current location or destination
                        val startLat = currentLocation?.latitude ?: 37.5665
                        val startLng = currentLocation?.longitude ?: 126.9780
                        
                        val startLatLng = LatLng.from(startLat, startLng)
                        
                        if (destLat != 0.0 && destLng != 0.0) {
                            val destLatLng = LatLng.from(destLat, destLng)
                            
                            // Fit map bounds to show both user and destination
                            val cameraUpdate = com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(
                                arrayOf(startLatLng, destLatLng), 80
                            )
                            kakaoMap.moveCamera(cameraUpdate)

                            // 1. Draw Polyline connecting user location and destination
                            val shapeManager = kakaoMap.shapeManager
                            val shapeLayer = shapeManager?.layer
                            
                            val polylinePoints = if (routePath.size > 2) {
                                routePath.map { LatLng.from(it.first, it.second) }
                            } else {
                                // Draw a curved line using the closest bridge if crossing the Han River
                                val points = mutableListOf<LatLng>()
                                points.add(startLatLng)
                                
                                val isStartNorth = startLatLng.latitude > 37.53
                                val isEndNorth = destLatLng.latitude > 37.53
                                val isStartSouth = startLatLng.latitude < 37.51
                                val isEndSouth = destLatLng.latitude < 37.51
                                
                                if ((isStartNorth && isEndSouth) || (isStartSouth && isEndNorth)) {
                                    // Crosses Han River! Find the closest bridge to route through
                                    val bridges = listOf(
                                        LatLng.from(37.533, 126.936), // Mapo Bridge
                                        LatLng.from(37.518, 126.996), // Banpo Bridge
                                        LatLng.from(37.525, 127.012), // Hannam Bridge
                                        LatLng.from(37.538, 127.052), // Yeongdong Bridge
                                        LatLng.from(37.535, 127.068)  // Jamsil Bridge
                                    )
                                    val bestBridge = bridges.minByOrNull { bridge ->
                                        val distStart = (startLatLng.latitude - bridge.latitude) * (startLatLng.latitude - bridge.latitude) + (startLatLng.longitude - bridge.longitude) * (startLatLng.longitude - bridge.longitude)
                                        val distEnd = (bridge.latitude - destLatLng.latitude) * (bridge.latitude - destLatLng.latitude) + (bridge.longitude - destLatLng.longitude) * (bridge.longitude - destLatLng.longitude)
                                        distStart + distEnd
                                    }
                                    if (bestBridge != null) {
                                        points.add(bestBridge)
                                    }
                                }
                                points.add(destLatLng)
                                points
                            }

                            val mapPoints = com.kakao.vectormap.shape.MapPoints.fromLatLng(polylinePoints)
                            val polylineStyle = com.kakao.vectormap.shape.PolylineStyle.from(
                                10f, // Line thickness
                                android.graphics.Color.parseColor("#3B82F6") // Blue matching theme
                            )
                            
                            val polylineOptions = com.kakao.vectormap.shape.PolylineOptions.from(
                                mapPoints, polylineStyle
                            )
                            
                            shapeLayer?.addPolyline(polylineOptions)

                            // 2. Add Markers for User and Destination
                            val labelManager = kakaoMap.labelManager
                            val labelLayer = labelManager?.layer

                            // User marker
                            val userStyle = com.kakao.vectormap.label.LabelStyle.from(android.R.drawable.ic_menu_mylocation)
                            val userLabelOptions = com.kakao.vectormap.label.LabelOptions.from("user_loc", startLatLng)
                                .setStyles(com.kakao.vectormap.label.LabelStyles.from(userStyle))
                            labelLayer?.addLabel(userLabelOptions)

                            // Destination marker
                            val destStyle = com.kakao.vectormap.label.LabelStyle.from(android.R.drawable.ic_dialog_map)
                            val destLabelOptions = com.kakao.vectormap.label.LabelOptions.from("dest_loc", destLatLng)
                                .setStyles(com.kakao.vectormap.label.LabelStyles.from(destStyle))
                            labelLayer?.addLabel(destLabelOptions)
                        } else {
                            // Only center on user
                            val cameraUpdate = com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(startLatLng, 15)
                            kakaoMap.moveCamera(cameraUpdate)
                        }
                    }
                })
            }
        },
        update = {
            // Updates to the map view if needed
        }
    )
}
