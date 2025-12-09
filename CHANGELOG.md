# Changelog

All notable changes to Chrono will be documented in this file.

## [1.1.0] - 2025-12-09

### ✨ New Features

#### Backup & Restore
- **Export Data**: Save all app data to `Downloads/Chrono` as a JSON file
- **Import Data**: Restore from backup using system file picker
- **Complete Data Coverage**: Backs up Tasks, Reminders, Events, Exams, Notes, Timetable, and Focus Settings

#### Visibility Assist
- **Text Scale Slider**: Adjust app-wide text size (0.8x to 1.4x) in Settings
- **Persistent Setting**: Scale preference saved and applied across app restarts

#### Reminder Notifications
- **Full-Screen Alarm**: Reminders now display full-screen notification with alarm sound
- **Tap to Reopen**: Tapping notification reopens full-screen reminder
- **Bundled Alarm Sound**: Uses built-in `remainder_sound.mp3` for reliability
- **Completion on Dismiss**: Reminders automatically move to "Completed" when dismissed

### 🐛 Bug Fixes

#### Reminder Completion
- **Fixed**: Reminders now correctly move to "Completed" dropdown after dismissing full-screen notification
- **Fixed**: Reminder ID properly passed through alarm scheduling flow
- **Fixed**: Expired single reminders stay visible until user dismisses them

#### Notification System
- **Fixed**: Notification tap correctly opens full-screen activity
- **Fixed**: Alarm sound plays reliably using bundled resource

### 🔧 Improvements

#### Code Cleanup
- Removed unused `ReminderActionReceiver.kt`
- Removed unused `Typography.kt`
- Removed unused `ReminderTone` enum and sound selection settings
- Removed unused `LocalTextScale` CompositionLocal
- Cleaned up `SettingsDataStore.kt` (~35% reduction)
- Cleaned up `Theme.kt` (removed unused extensions)

#### Backup System Optimization
- Added `restoreData()` methods to all DataStores
- Added `importFromJson()` method for file picker compatibility
- Works on all Android versions including Android 10+ (scoped storage)

### 📦 Dependencies
- Added `com.google.code.gson:gson` for JSON serialization

### 🔐 Permissions
- Added `READ_EXTERNAL_STORAGE` (maxSdkVersion 32)
- Added `WRITE_EXTERNAL_STORAGE` (maxSdkVersion 29)

---

## [1.0.0] - Initial Release

### Features
- Tasks with groups and priorities
- Reminders (single and repeating)
- Events calendar
- Exams schedule
- Notes
- Timetable
- Focus mode with app blocking
- Water break reminders
- Notification history
- Dark minimalist theme
