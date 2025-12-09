package com.chrono.data

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChronoBackup(
    val version: String = "1.1.0",
    val exportDate: Long = System.currentTimeMillis(),
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
    
    // Folder in Downloads/Chrono
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
            // Collect all data
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
            
            // Generate filename with timestamp
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
            val fileName = "chrono_backup_${dateFormat.format(Date())}.json"
            
            val file = File(backupFolder, fileName)
            file.writeText(gson.toJson(backup))
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    fun getBackupFiles(): List<File> {
        return backupFolder.listFiles { file ->
            file.isFile && file.name.startsWith("chrono_backup") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    fun getLatestBackup(): File? {
        return getBackupFiles().firstOrNull()
    }
    
    suspend fun importData(file: File? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backupFile = file ?: getLatestBackup()
                ?: return@withContext Result.failure(Exception("No backup file found"))
            
            val json = backupFile.readText()
            val backup = gson.fromJson(json, ChronoBackup::class.java)
                ?: return@withContext Result.failure(Exception("Invalid backup file"))
            
            // Restore all data
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
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            Result.success("Restored from backup: ${dateFormat.format(Date(backup.exportDate))}")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun importFromJson(json: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backup = gson.fromJson(json, ChronoBackup::class.java)
                ?: return@withContext Result.failure(Exception("Invalid backup file"))
            
            // Restore all data
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
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            Result.success("Restored from backup: ${dateFormat.format(Date(backup.exportDate))}")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
