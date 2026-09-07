package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.DataUsageRepository
import com.example.model.SpeedTestPhase
import com.example.service.SpeedMeterService
import com.example.ui.SpeedMeterViewModel
import com.example.ui.components.HistoryView
import com.example.ui.components.MetricCardsGrid
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SpeedGauge
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusGreen
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: SpeedMeterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                SpeedMeterApp(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkBatteryOptimization()
    }
}

@Composable
fun SpeedMeterApp(viewModel: SpeedMeterViewModel) {
    val context = LocalContext.current
    val liveSpeed by viewModel.liveSpeed.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val speedTestResult by viewModel.speedTestResult.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val isStartOnBoot by viewModel.isStartOnBoot.collectAsState()
    val speedUnit by viewModel.speedUnit.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val isTesting by viewModel.isTesting.collectAsState()
    val isBatteryOptimizationIgnored by viewModel.isBatteryOptimizationIgnored.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            SpeedMeterService.start(context)
        }
    }

    // Auto-start live status bar meter on launch
    LaunchedEffect(Unit) {
        val repo = DataUsageRepository(context)
        if (repo.isServiceEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (hasNotificationPermission) {
                    SpeedMeterService.start(context)
                } else {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                SpeedMeterService.start(context)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                SpeedMeterBottomNav(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.setSelectedTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
        ) {
            // Header: starts cleanly at safe area top
            SpeedMeterHeader(
                networkName = liveSpeed.networkName,
                isConnected = liveSpeed.isConnected,
                onSettingsClick = { viewModel.setShowSettingsDialog(true) }
            )

            // Content body based on tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> {
                        SpeedMainScreen(
                            isTesting = isTesting,
                            speedTestResult = speedTestResult,
                            liveSpeed = liveSpeed,
                            speedUnit = speedUnit,
                            onRunSpeedTest = {
                                viewModel.startSpeedTest()
                            }
                        )
                    }
                    1 -> {
                        HistoryView(historyList = historyList)
                    }
                    2 -> {
                        NetworkInfoScreen(
                            liveSpeed = liveSpeed,
                            isServiceRunning = isServiceRunning
                        )
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            isServiceRunning = isServiceRunning,
            isStartOnBoot = isStartOnBoot,
            isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
            currentUnit = speedUnit,
            currentThemeMode = themeMode,
            onToggleService = {
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.toggleService()
                }
            },
            onToggleStartOnBoot = { viewModel.toggleStartOnBoot(it) },
            onRequestIgnoreBatteryOptimization = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                }
            },
            onSelectUnit = { viewModel.setSpeedUnit(it) },
            onSelectThemeMode = { viewModel.setThemeMode(it) },
            onDismiss = { viewModel.setShowSettingsDialog(false) }
        )
    }
}

@Composable
fun SpeedMeterHeader(
    networkName: String,
    isConnected: Boolean,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = "CURRENT PROVIDER",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = networkName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isConnected) StatusGreen else Color.Gray,
                            CircleShape
                        )
                )
            }
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .testTag("settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SpeedMainScreen(
    isTesting: Boolean,
    speedTestResult: com.example.model.SpeedTestResult,
    liveSpeed: com.example.model.LiveSpeedData,
    speedUnit: com.example.model.SpeedUnit,
    onRunSpeedTest: () -> Unit
) {
    // Gauge speed calculation
    val (displaySpeed, displayUnit, progress) = remember(isTesting, speedTestResult, liveSpeed, speedUnit) {
        if (isTesting || speedTestResult.testFinished) {
            val speed = speedTestResult.currentSpeedMbps
            val formatted = String.format(Locale.US, "%.1f", speed)
            val unit = when (speedTestResult.phase) {
                SpeedTestPhase.PING -> "Ping Test"
                SpeedTestPhase.DOWNLOAD -> "Mbps Download"
                SpeedTestPhase.UPLOAD -> "Mbps Upload"
                SpeedTestPhase.COMPLETED -> "Mbps Download"
                else -> "Mbps"
            }
            val prog = (speed / 200.0).toFloat().coerceIn(0.05f, 1f)
            Triple(formatted, unit, prog)
        } else {
            val (valStr, unitStr) = DataUsageRepository.formatSpeed(liveSpeed.downloadSpeedBytes, speedUnit)
            val prog = (liveSpeed.downloadSpeedBytes / (5.0 * 1024 * 1024)).toFloat().coerceIn(0.04f, 1f)
            Triple(valStr, "$unitStr Live", prog)
        }
    }

    // Metric cards calculations
    val (uploadVal, uploadUnit) = remember(isTesting, speedTestResult, liveSpeed, speedUnit) {
        if (isTesting || speedTestResult.testFinished) {
            val up = speedTestResult.uploadSpeedMbps
            Pair(String.format(Locale.US, "%.1f", up), "Mbps")
        } else {
            DataUsageRepository.formatSpeed(liveSpeed.uploadSpeedBytes, speedUnit)
        }
    }

    val pingStr = if (speedTestResult.pingMs > 0) speedTestResult.pingMs.toString() else "18"
    val jitterStr = if (speedTestResult.jitterMs > 0) speedTestResult.jitterMs.toString() else "4"
    val lossStr = String.format(Locale.US, "%.1f", speedTestResult.packetLossPercent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Central Speedometer Gauge (fits cleanly with generous spacing)
        SpeedGauge(
            speedValue = displaySpeed,
            speedUnitLabel = displayUnit,
            progressFraction = progress
        )

        // 2x2 Metric Cards Grid
        MetricCardsGrid(
            uploadSpeed = uploadVal,
            uploadUnit = uploadUnit,
            pingMs = pingStr,
            jitterMs = jitterStr,
            lossPercent = lossStr
        )

        // Primary Test Button
        Button(
            onClick = onRunSpeedTest,
            enabled = !isTesting,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("test_speed_button"),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "TESTING NETWORK...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (speedTestResult.testFinished) "TEST AGAIN" else "START SPEED TEST",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
fun NetworkInfoScreen(
    liveSpeed: com.example.model.LiveSpeedData,
    isServiceRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "NETWORK DIAGNOSTICS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoRow(label = "Network Status", value = if (liveSpeed.isConnected) "Online" else "Offline", isHighlight = true)
                InfoRow(label = "Interface Type", value = if (liveSpeed.isWifi) "Wi-Fi (High Speed)" else if (liveSpeed.isMobile) "Cellular 5G/LTE" else "Local Adapter")
                InfoRow(label = "Access Point / Carrier", value = liveSpeed.networkName)
                InfoRow(label = "Status Bar Live Service", value = if (isServiceRunning) "Running in Foreground" else "Stopped")
                InfoRow(label = "Today's Total Traffic", value = DataUsageRepository.formatBytes(liveSpeed.todayTotalBytes))
                InfoRow(label = "Wi-Fi Traffic Today", value = DataUsageRepository.formatBytes(liveSpeed.todayWifiBytes))
                InfoRow(label = "Mobile Traffic Today", value = DataUsageRepository.formatBytes(liveSpeed.todayMobileBytes))
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isHighlight) StatusGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SpeedMeterBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabItem(
                icon = Icons.Default.Speed,
                label = "Speed",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            NavTabItem(
                icon = Icons.Default.BarChart,
                label = "History",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            NavTabItem(
                icon = Icons.Default.Info,
                label = "Diagnostics",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
fun NavTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
