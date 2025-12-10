package com.chrono.data

import android.content.Context
import android.os.Environment
import android.util.Base64
import com.chrono.security.EncryptedPreferencesManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class ChronoBackup(
    val version: String = "2.0.0",
    val exportDate: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true,
    val tasks: TasksData? = null,
    val reminders: RemindersData? = null,
    val events: EventsData? = null,
    val exams: ExamsData? = null,
    val notes: NotesData? = null,
    val timetable: TimetableData? = null,
    val focusSettings: FocusSettings? = null
)

class BackupManager(private val context: Context) {
    
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    companion object {
        // Fixed encryption key derived from app signature (not user password)
        private const val BACKUP_KEY = "ChronoSecureBack" // 16 bytes for AES-128
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }
    
    private val backupFolder: File
        get() {
            val folder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Chrono"
            )
            if (!folder.exists()) {
                folder.mkdirs()
            }
            return folder
        }
    
    suspend fun exportData(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tasksDataStore = TasksDataStore(context)
            val remindersDataStore = RemindersDataStore(context)
            val eventsDataStore = EventsDataStore(context)
            val examsDataStore = ExamsDataStore(context)
            val notesDataStore = NotesDataStore(context)
            val timetableDataStore = TimetableDataStore(context)
            val focusSettingsDataStore = FocusSettingsDataStore(context)
            
            val backup = ChronoBackup(
                tasks = tasksDataStore.tasksData.first(),
                reminders = remindersDataStore.remindersData.first(),
                events = eventsDataStore.eventsData.first(),
                exams = examsDataStore.examsData.first(),
                notes = notesDataStore.notesData.first(),
                timetable = timetableDataStore.timetableData.first(),
                focusSettings = focusSettingsDataStore.focusSettings.first()
            )
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
            val fileName = "chrono_backup_${dateFormat.format(Date())}.chrono"
            
            val jsonData = gson.toJson(backup)
            val encryptedData = encryptData(jsonData)
            
            val file = File(backupFolder, fileName)
            file.writeText(encryptedData)
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    fun getBackupFiles(): List<File> {
        return backupFolder.listFiles { file ->
            file.isFile && file.name.startsWith("chrono_backup") && 
            (file.name.endsWith(".chrono") || file.name.endsWith(".json"))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    fun getLatestBackup(): File? {
        return getBackupFiles().firstOrNull()
    }
    
    suspend fun importData(file: File? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backupFile = file ?: getLatestBackup()
                ?: return@withContext Result.failure(Exception("No backup file found"))
            
            val fileContent = backupFile.readText()
            
            // Try to decrypt if it's an encrypted backup, otherwise treat as legacy JSON
            val jsonContent = if (backupFile.name.endsWith(".chrono")) {
                decryptData(fileContent)
            } else {
                // Legacy plain JSON backup - still supported for import
                fileContent
            }
            
            val backup = gson.fromJson(jsonContent, ChronoBackup::class.java)
                ?: return@withContext Result.failure(Exception("Invalid backup file"))
            
            restoreFromBackup(backup)
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            Result.success("Restored from backup: ${dateFormat.format(Date(backup.exportDate))}")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun importFromJson(json: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Try decryption first, fall back to plain JSON
            val jsonContent = try {
                decryptData(json)
            } catch (e: Exception) {
                json // Might be plain JSON from old backup
            }
            
            val backup = gson.fromJson(jsonContent, ChronoBackup::class.java)
                ?: return@withContext Result.failure(Exception("Invalid backup file"))
            
            restoreFromBackup(backup)
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            Result.success("Restored from backup: ${dateFormat.format(Date(backup.exportDate))}")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    private suspend fun restoreFromBackup(backup: ChronoBackup) {
        val tasksDataStore = TasksDataStore(context)
        val remindersDataStore = RemindersDataStore(context)
        val eventsDataStore = EventsDataStore(context)
        val examsDataStore = ExamsDataStore(context)
        val notesDataStore = NotesDataStore(context)
        val timetableDataStore = TimetableDataStore(context)
        val focusSettingsDataStore = FocusSettingsDataStore(context)
        
        backup.tasks?.let { tasksDataStore.restoreData(it) }
        backup.reminders?.let { remindersDataStore.restoreData(it) }
        backup.events?.let { eventsDataStore.restoreData(it) }
        backup.exams?.let { examsDataStore.restoreData(it) }
        backup.notes?.let { notesDataStore.restoreData(it) }
        backup.timetable?.let { timetableDataStore.restoreData(it) }
        backup.focusSettings?.let { focusSettingsDataStore.restoreData(it) }
    }
    
    private fun encryptData(plainText: String): String {
        val key = SecretKeySpec(BACKUP_KEY.toByteArray(Charsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        // Combine IV + encrypted data
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    
    private fun decryptData(encryptedBase64: String): String {
        val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
        
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        
        val key = SecretKeySpec(BACKUP_KEY.toByteArray(Charsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
