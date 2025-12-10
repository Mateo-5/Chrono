package com.chrono.security

import android.content.Context
import android.os.Build
import android.os.Debug
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Security manager for detecting compromised device states.
 * Provides runtime security checks for rooted devices, debuggers, and emulators.
 */
object SecurityManager {
    
    /**
     * Performs all security checks and returns true if device appears compromised.
     */
    fun isDeviceCompromised(context: Context): Boolean {
        return isRooted() || isDebugged() || isEmulator()
    }
    
    /**
     * Checks for common indicators of a rooted device.
     */
    fun isRooted(): Boolean {
        return checkRootBinaries() || checkSuExists() || checkRootManagementApps()
    }
    
    private fun checkRootBinaries(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup",
            "/system/xbin/mu",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        
        return paths.any { File(it).exists() }
    }
    
    private fun checkSuExists(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            result != null
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkRootManagementApps(): Boolean {
        val rootApps = arrayOf(
            "com.topjohnwu.magisk",          // Magisk
            "eu.chainfire.supersu",           // SuperSU
            "com.koushikdutta.superuser",     // Koushik Superuser
            "com.noshufou.android.su",        // Superuser
            "com.thirdparty.superuser",       // Third-party Superuser
            "com.yellowes.su",                // YellowES Superuser
            "com.zachspong.temprootremovejb", // Root Remover
            "com.ramdroid.appquarantine"      // App Quarantine
        )
        
        val pm = try {
            Runtime.getRuntime().exec("pm list packages")
        } catch (e: Exception) {
            return false
        }
        
        val reader = BufferedReader(InputStreamReader(pm.inputStream))
        val installedPackages = reader.readLines()
        reader.close()
        
        return rootApps.any { app ->
            installedPackages.any { it.contains(app) }
        }
    }
    
    /**
     * Checks if a debugger is attached to the process.
     */
    fun isDebugged(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }
    
    /**
     * Checks if running on an emulator.
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT == "google_sdk"
                || Build.PRODUCT == "sdk"
                || Build.PRODUCT == "sdk_x86"
                || Build.PRODUCT == "vbox86p"
                || Build.PRODUCT == "emulator"
                || Build.PRODUCT == "simulator"
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }
    
    /**
     * Gets a human-readable security status message.
     */
    fun getSecurityStatus(context: Context): SecurityStatus {
        val issues = mutableListOf<String>()
        
        if (isRooted()) issues.add("Device appears to be rooted")
        if (isDebugged()) issues.add("Debugger is attached")
        if (isEmulator()) issues.add("Running on emulator")
        
        return SecurityStatus(
            isCompromised = issues.isNotEmpty(),
            issues = issues
        )
    }
    
    data class SecurityStatus(
        val isCompromised: Boolean,
        val issues: List<String>
    )
}
