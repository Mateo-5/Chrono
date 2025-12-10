# Changelog

All notable changes to Chrono will be documented in this file.

## [2.0.0] - 2025-12-10

### 🔐 Security Hardening

#### Encrypted Storage
- **All data now encrypted at rest** using AES-256 via EncryptedSharedPreferences
- Data stored in Android Keystore-backed encrypted storage
- Automatic migration of existing data on first launch

#### Anti-Extraction
- **ADB backup disabled** - Data cannot be extracted via `adb backup`
- **Cloud backup blocked** - Data excluded from Google Drive backup
- **Device transfer blocked** - Data excluded during device migration

#### Build Protection
- **ProGuard/R8 enabled** for release builds
- Code obfuscation makes reverse engineering harder
- Resource shrinking for smaller APK size

#### Runtime Security
- **Root detection** - Warns if device is rooted
- **Debugger detection** - Detects attached debuggers
- **Emulator detection** - Identifies emulator environments

#### Encrypted Backups
- Backup exports now use `.chrono` format with AES-128 encryption
- Legacy `.json` backups still supported for import (backward compatible)

### 📦 New Components
- `EncryptedPreferencesManager` - Secure storage wrapper
- `SecurityManager` - Runtime security checks
- `DataMigrationManager` - Automatic data migration

### 🔧 Technical Changes
- All 9 DataStores migrated to encrypted SharedPreferences
- Added `data_extraction_rules.xml` for Android 12+ backup control
- Updated ProGuard rules for Gson serialization

---

## [1.1.1] - 2025-12-10

### 🐛 Bug Fixes

#### Reminder Timing
- **Fixed**: Reminders now fire at exact scheduled time
- **Fixed**: Added wake lock to ensure device wakes up for alarms
- **Fixed**: Repeating alarms now use exact scheduling instead of inexact `setRepeating()`
- **Fixed**: Repeating alarms properly reschedule for next day after firing

#### Notes Input
- **Fixed**: Word duplication bug when typing in notes
- **Fixed**: Removed blocking keyboardActions that interfered with IME composition

### ✨ Improvements

#### Multilingual Support
- All text fields now properly support multilingual input (Malayalam, Hindi, etc.)
- Improved IME composition handling for complex scripts

### 🔐 Permissions
- Added `USE_EXACT_ALARM` for Android 13+ exact alarm scheduling
- Added `RECEIVE_BOOT_COMPLETED` for alarm persistence

---

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
