package com.example.nextstation.ui.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextstation.domain.model.ArrivalInfo
import com.example.nextstation.domain.model.RealTimeArrival
import com.example.nextstation.domain.model.StationInfo
import com.example.nextstation.domain.repository.ArrivalRepository
import com.example.nextstation.data.repository.UserPreferencesRepository
import com.example.nextstation.service.ArrivalService
import com.example.nextstation.util.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ArrivalRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val history = repository.getArrivalHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultPhoneNumber = userPreferencesRepository.defaultPhoneNumber
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _realTimeResults = MutableStateFlow<List<RealTimeArrival>>(emptyList())
    val realTimeResults = _realTimeResults.asStateFlow()

    private val _routeResults = MutableStateFlow<List<com.example.nextstation.domain.model.RouteInfo>>(emptyList())
    val routeResults = _routeResults.asStateFlow()

    private val _stationSearchResults = MutableStateFlow<List<StationInfo>>(emptyList())
    val stationSearchResults = _stationSearchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun searchRoutes(destination: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _routeResults.value = repository.searchRoutesToDestination(destination)
            _isLoading.value = false
        }
    }

    fun searchStations(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _stationSearchResults.value = repository.searchStations(name)
            _isLoading.value = false
        }
    }

    fun searchBusArrival(arsId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val results = withContext(Dispatchers.IO) {
                repository.getRealTimeArrival(arsId)
            }
            _realTimeResults.value = results
            _isLoading.value = false
        }
    }

    fun clearSearchResults() {
        _stationSearchResults.value = emptyList()
        _realTimeResults.value = emptyList()
    }

    fun deleteArrivalInfo(info: ArrivalInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteArrivalInfo(info)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateDefaultPhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultPhoneNumber(phoneNumber)
        }
    }

    private val _estimatedTime = MutableStateFlow<Int?>(null)
    val estimatedTime = _estimatedTime.asStateFlow()

    fun estimateTravelTime(startX: Double, startY: Double, endX: Double, endY: Double) {
        viewModelScope.launch {
            _estimatedTime.value = repository.getEstimatedTravelTime(startX, startY, endX, endY)
        }
    }

    fun setAlarm(destination: String, leadMinutes: Int, phoneNumber: String, message: String) {
        val travelSeconds = _estimatedTime.value ?: (30 * 60) // Default 30 mins if estimation fails
        val arrivalTime = System.currentTimeMillis() + (travelSeconds * 1000L)
        val alarmTime = arrivalTime - (leadMinutes * 60 * 1000L)
        
        val info = ArrivalInfo(
            destinationName = destination,
            arrivalTime = arrivalTime, // Actual estimated arrival
            phoneNumber = phoneNumber,
            message = message
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertArrivalInfo(info)
        }

        val serviceIntent = Intent(context, ArrivalService::class.java).apply {
            action = ArrivalService.ACTION_START
            putExtra("destination", destination)
            putExtra("arrivalTime", arrivalTime)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("destination", destination)
            putExtra("phoneNumber", phoneNumber)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
            } else {
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }
    }
}
