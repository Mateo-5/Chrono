# Changelog

All notable changes to Chrono are documented here.

## [1.0.0] - 2024-12-06

### Added
- **Tasks System**: Single tasks and task groups with break support
- **Focus Mode**: Pomodoro timer with Normal and Strict modes
- **App Blocking**: Restrict app usage during Strict Mode focus sessions
- **Calendar**: Monthly view with event management
- **Events**: Full event CRUD with persistence
- **Reminders**: Time-based reminders with full-screen alerts
- **Notes**: Quick note-taking functionality
- **Timetable**: Weekly schedule management
- **Exams**: Exam tracking and countdown
- **Water Break Reminders**: Hydration notifications
- **Persistent Notifications**: Active task and focus timer notifications
- **DND Integration**: Automatic Do Not Disturb in Strict Mode
- **Minimalist Theme**: Black and white glassmorphism design

### Technical
- Jetpack Compose UI with Material 3
- DataStore for data persistence
- Foreground services for reliable background operation
- WorkManager for scheduled notifications

---

## Development Timeline

### Phase 1: Foundation
- Project initialization with Compose
- Navigation setup (NavHost)
- Base theme and color system

### Phase 2: Core Screens
- Home screen with calendar widget
- Tasks screen with timer UI
- Events and Calendar screens
- Timetable with period management
- Notes quick-access

### Phase 3: Notifications
- Notification channels setup
- Reminder scheduling with AlarmManager
- Full-screen reminder activity
- Water break WorkManager integration

### Phase 4: Focus Mode
- Pomodoro timer implementation
- Strict Mode with app blocking
- FocusService for background operation
- DND toggle integration

### Phase 5: Polish & Fixes
- Minimalist black/white theme redesign
- Data persistence bug fixes
- Backward compatibility for data models
- Code cleanup and deprecation fixes
