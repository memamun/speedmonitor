package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataUsageRepository
import com.example.model.DayUsageItem
import com.example.model.LiveSpeedData
import com.example.model.SpeedTestPhase
import com.example.model.SpeedTestResult
import com.example.model.SpeedUnit
import com.example.model.ThemeMode
import com.example.network.SpeedTestEngine
import com.example.service.SpeedMeterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpeedMeterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DataUsageRepository(application)
    private val speedTestEngine = SpeedTestEngine()

    val liveSpeed: StateFlow<LiveSpeedData> = SpeedMeterService.liveSpeedData
    val isServiceRunning: StateFlow<Boolean> = SpeedMeterService.isServiceRunning
    val speedTestResult: StateFlow<SpeedTestResult> = speedTestEngine.testResult

    private val _historyList = MutableStateFlow<List<DayUsageItem>>(emptyList())
    val historyList: StateFlow<List<DayUsageItem>> = _historyList.asStateFlow()

    private val _isStartOnBoot = MutableStateFlow(repository.isStartOnBoot())
    val isStartOnBoot: StateFlow<Boolean> = _isStartOnBoot.asStateFlow()

    private val _speedUnit = MutableStateFlow(repository.getSpeedUnit())
    val speedUnit: StateFlow<SpeedUnit> = _speedUnit.asStateFlow()

    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    init {
        refreshHistory()
        checkBatteryOptimization()
    }

    fun checkBatteryOptimization() {
        val app = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = app.getSystemService(Context.POWER_SERVICE) as? PowerManager
            _isBatteryOptimizationIgnored.value = pm?.isIgnoringBatteryOptimizations(app.packageName) == true
        } else {
            _isBatteryOptimizationIgnored.value = true
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
        if (tab == 1) {
            refreshHistory()
        }
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun refreshHistory() {
        _historyList.value = repository.getUsageHistory()
    }

    fun toggleService() {
        val app = getApplication<Application>()
        if (isServiceRunning.value) {
            SpeedMeterService.stop(app)
            repository.setServiceEnabled(false)
        } else {
            repository.setServiceEnabled(true)
            SpeedMeterService.start(app)
        }
    }

    fun toggleStartOnBoot(enabled: Boolean) {
        repository.setStartOnBoot(enabled)
        _isStartOnBoot.value = enabled
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        repository.setSpeedUnit(unit)
        _speedUnit.value = unit
    }

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
        _themeMode.value = mode
    }

    fun startSpeedTest() {
        if (_isTesting.value) return
        _isTesting.value = true
        viewModelScope.launch {
            try {
                speedTestEngine.runSpeedTest()
            } finally {
                _isTesting.value = false
                refreshHistory()
            }
        }
    }
}
