package com.chrono.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chrono.MainActivity
import com.chrono.R
import com.chrono.data.FocusSettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FocusService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null
    private var monitorJob: Job? = null
    
    private lateinit var focusSettingsDataStore: FocusSettingsDataStore
    
    // State
    private val _timeRemaining = MutableStateFlow(25 * 60)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _isBreak = MutableStateFlow(false)
    val isBreak: StateFlow<Boolean> = _isBreak.asStateFlow()
    
    private val _sessionsCompleted = MutableStateFlow(0)
    val sessionsCompleted: StateFlow<Int> = _sessionsCompleted.asStateFlow()
    
    // Configuration
    private var isStrictMode = false

    inner class LocalBinder : Binder() {
        fun getService(): FocusService = this@FocusService
    }

    override fun onCreate() {
        super.onCreate()
        focusSettingsDataStore = FocusSettingsDataStore(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopFocus()
            stopSelf()
            return START_NOT_STICKY
        }
        
        isStrictMode = intent?.getBooleanExtra(EXTRA_STRICT_MODE, false) ?: false
        
        startForeground(NOTIFICATION_ID, createNotification())
        startTimer()
        
        if (isStrictMode) {
            startMonitoring()
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        monitorJob?.cancel()
        setDnd(false)
    }
    
    fun toggleTimer() {
        if (_isRunning.value) {
            pauseTimer()
        } else {
            resumeTimer()
        }
    }
    
    fun resetTimer() {
        pauseTimer()
        _timeRemaining.value = if (_isBreak.value) 5 * 60 else 25 * 60
        updateNotification()
    }
    
    fun skipSession() {
        pauseTimer()
        if (!_isBreak.value) {
            _sessionsCompleted.value++
            _isBreak.value = true
            _timeRemaining.value = 5 * 60
        } else {
            _isBreak.value = false
            _timeRemaining.value = 25 * 60
        }
        updateNotification()
    }

    private fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true
        
        if (isStrictMode && !_isBreak.value) {
            setDnd(true)
        }
        
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_isRunning.value && _timeRemaining.value > 0) {
                delay(1000)
                _timeRemaining.value--
                updateNotification()
                
                if (_timeRemaining.value == 0) {
                    withContext(Dispatchers.Main) {
                        onTimerFinished()
                    }
                }
            }
        }
    }
    
    private fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        updateNotification()
        if (isStrictMode) {
            setDnd(false) // Disable DND when paused? Or keep it? Usually keep it if session active.
            // Let's keep DND on if paused, but maybe user wants to check phone.
            // For now, let's disable DND on pause to be safe.
            setDnd(false)
        }
    }
    
    private fun resumeTimer() {
        startTimer()
    }
    
    private fun stopFocus() {
        _isRunning.value = false
        timerJob?.cancel()
        monitorJob?.cancel()
        setDnd(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun onTimerFinished() {
        _isRunning.value = false
        // Vibrate or sound
        
        if (!_isBreak.value) {
            _sessionsCompleted.value++
            _isBreak.value = true
            _timeRemaining.value = 5 * 60
            // Break started, disable DND
            setDnd(false)
        } else {
            _isBreak.value = false
            _timeRemaining.value = 25 * 60
            // Focus started
        }
        updateNotification()
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val settings = focusSettingsDataStore.focusSettings.first()
            val blockedPackages = settings.blockedPackageNames

            if (blockedPackages.isEmpty()) return@launch

            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            while (true) {
                if (_isRunning.value && !_isBreak.value) {
                    val time = System.currentTimeMillis()
                    val stats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        time - 1000 * 10,
                        time
                    )

                    if (stats != null) {
                        val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
                        if (sortedStats.isNotEmpty()) {
                            val currentPackage = sortedStats[0].packageName
                            if (blockedPackages.contains(currentPackage)) {
                                withContext(Dispatchers.Main) {
                                    showBlockScreen()
                                }
                            }
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    private fun showBlockScreen() {
        val intent = Intent(this, BlockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }
    
    private fun setDnd(enable: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            if (enable) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Focus Mode",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val minutes = _timeRemaining.value / 60
        val seconds = _timeRemaining.value % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        val status = if (_isBreak.value) "Break Time" else "Focus Time"
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, FocusService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(status)
            .setContentText(timeString)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    companion object {
        private const val CHANNEL_ID = "FocusServiceChannel"
        private const val NOTIFICATION_ID = 2002
        const val ACTION_STOP = "com.chrono.focus.ACTION_STOP"
        const val EXTRA_STRICT_MODE = "extra_strict_mode"
    }
}
