# Chrono ⏱️

A modern, minimalist productivity app for Android built with Jetpack Compose.

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)

## Features

### 📋 Tasks
- Single tasks and task groups with breaks
- Persistent notifications for active tasks
- Quick complete/delete actions

### ⏰ Focus Mode
- Pomodoro-style timer with customizable durations
- **Normal Mode**: Background timer with notifications
- **Strict Mode**: App blocking + DND activation
- Persistent countdown notification

### 📅 Calendar & Events
- Monthly calendar view with event indicators
- Add, edit, delete events
- Shared data between Calendar and Events screens

### 🔔 Reminders
- Time-based reminders with full-screen alerts
- Custom notification sounds

### 📝 Notes & Timetable
- Quick note-taking
- Weekly timetable management

### ⚙️ Settings
- Water break reminders
- Focus timer duration configuration
- Permission management

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: Single-activity with Compose Navigation
- **Data Persistence**: DataStore with Gson serialization
- **Background Work**: WorkManager, Foreground Services
- **Notifications**: NotificationCompat with channels

## Design

Minimalist black and white theme with glassmorphism effects. Designed for distraction-free productivity.

## Development

### Built With AI Assistance

This project was developed using an innovative **human-AI collaboration** workflow:

| Role | Contributor |
|------|-------------|
| **Project Lead & Prompt Engineer** | Developer |
| **AI Development Assistant** | Claude (Anthropic) |

The development process involved:
- Iterative feature design through natural language prompts
- AI-assisted code generation and debugging
- Collaborative problem-solving for complex features
- Code review and optimization suggestions

### Development Phases

1. **Foundation** - Project setup, navigation, theme
2. **Core Features** - Tasks, Events, Timetable, Notes
3. **Notifications** - Reminders, water breaks, task notifications
4. **Focus Mode** - Timer, app blocking, DND integration
5. **Polish** - UI refinements, bug fixes, data persistence

## Installation

1. Clone the repository
2. Open in Android Studio
3. Build and run on device/emulator (API 26+)

## Version

**Current**: v1.0.0

See [CHANGELOG.md](CHANGELOG.md) for version history.

## License

MIT License - see [LICENSE](LICENSE) for details.

---

*Built with ❤️ and AI*
