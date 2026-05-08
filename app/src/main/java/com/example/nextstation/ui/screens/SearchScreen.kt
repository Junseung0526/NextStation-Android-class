package com.example.nextstation.ui.screens

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nextstation.domain.model.RouteInfo
import com.example.nextstation.ui.components.GlassTextField
import com.example.nextstation.ui.main.MainViewModel

@Composable
fun SearchScreen(viewModel: MainViewModel) {
    val routeResults by viewModel.routeResults.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var destinationQuery by remember { mutableStateOf("") }
    var selectedRoute by remember { mutableStateOf<RouteInfo?>(null) }
    var alarmMinutes by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Destination Search Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = destinationQuery,
                onValueChange = { destinationQuery = it },
                placeholder = { Text("어디로 가시나요?") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Place, null, tint = colorScheme.primary) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = {
                        if (destinationQuery.isNotBlank() && !isLoading) {
                            selectedRoute = null
                            viewModel.searchRoutes(destinationQuery)
                        }
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Button(
                onClick = { 
                    selectedRoute = null
                    viewModel.searchRoutes(destinationQuery)
                },
                enabled = destinationQuery.isNotBlank() && !isLoading,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("길 찾기")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route Results Section
        Column(modifier = Modifier.weight(1f)) {
            if (routeResults.isNotEmpty() && selectedRoute == null) {
                Text(
                    text = "추천 버스 노선",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(routeResults) { route ->
                        RouteItem(route) {
                            selectedRoute = route
                            alarmMinutes = (route.firstArrivalTimeSeconds / 60).toString()
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedRoute != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedRoute?.let { route ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "선택된 노선: ${route.busNumber}번", 
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            )
                            TextButton(onClick = { selectedRoute = null }) {
                                Text("노선 변경")
                            }
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DirectionsBus, null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("${route.stNm} 정류장 도착 정보", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = route.firstArrivalMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Alarm Setup
        AnimatedVisibility(
            visible = selectedRoute != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "하차 알림 예약",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTextField(
                            value = alarmMinutes,
                            onValueChange = { alarmMinutes = it },
                            label = "분 전 알림",
                            modifier = Modifier.weight(1f),
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                            leadingIcon = Icons.Default.Timer
                        )
                        GlassTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = "전화번호",
                            modifier = Modifier.weight(1.5f),
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                            leadingIcon = Icons.Default.Phone
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (selectedRoute != null && alarmMinutes.isNotBlank()) {
                                viewModel.setAlarm(destinationQuery, alarmMinutes.toInt(), phoneNumber, "곧 도착 예정입니다.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("예약하기", fontWeight = FontWeight.Bold)
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
