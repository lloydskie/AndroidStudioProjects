# ToDo App 📝

A modern and feature-rich Android ToDo application built with Jetpack Compose and Kotlin.

## Features ✨

### Core Functionality
- ✅ **Task Management**: Create, edit, delete, and mark tasks as complete
- 📝 **Rich Task Details**: Add notes, due dates, categories, and priority levels
- 🔄 **Recurring Tasks**: Support for daily, weekly, monthly, and custom recurring tasks
- 📊 **Progress Tracking**: Visual progress indicators and completion percentages
- 💬 **Task Comments**: Add comments and notes to individual tasks
- 📎 **Attachments**: Attach files, images, and links to tasks

### Organization & Filtering
- 🏷️ **Categories**: Organize tasks by Personal, Work, or custom categories  
- ⚡ **Priority Levels**: High, Medium, Low priority classification
- 🔍 **Smart Search**: Search tasks by title and content
- 🎯 **Advanced Filtering**: Filter by category, priority, and completion status
- 📅 **Multiple Views**: List view and calendar view for different perspectives

### Smart Features
- 🔔 **Notifications**: Deadline reminders and notifications
- 🌙 **Dark/Light Theme**: Automatic and manual theme switching
- 📈 **Activity Log**: Track task history and changes
- 🔥 **Streak Tracking**: Monitor consistency with recurring tasks
- 📊 **Analytics**: Task completion statistics and insights

### Technical Features
- 🎨 **Material Design 3**: Modern UI with Material You design principles
- 📱 **Edge-to-Edge Display**: Immersive full-screen experience
- 🔄 **Real-time Updates**: Instant UI updates and synchronization
- 🔒 **Permission Management**: Smart notification permission handling

## Screenshots 📸

*Add your app screenshots here*

## Tech Stack 🛠️

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM pattern
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **Build System**: Gradle with Kotlin DSL

### Key Dependencies
- **Jetpack Compose**: Modern UI toolkit
- **Material 3**: Material Design components
- **Firebase**: Crashlytics for error reporting
- **WorkManager**: Background task scheduling
- **Compose Navigation**: In-app navigation
- **Material Icons Extended**: Comprehensive icon set

## Installation 🚀

### Prerequisites
- Android Studio Flamingo or later
- JDK 11 or higher
- Android SDK with API level 36

### Setup Steps
1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/todo-app.git
   cd todo-app
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it

3. **Configure Firebase (Optional)**
   - Add your `google-services.json` file to the `app/` directory
   - Enable Crashlytics in your Firebase console

4. **Build and Run**
   - Sync the project with Gradle files
   - Run on an emulator or physical device

## Usage Guide 📖

### Creating Tasks
1. Tap the **+** floating action button
2. Fill in task details:
   - Title and description
   - Due date and time
   - Category and priority
   - Set as recurring if needed

### Managing Tasks
- **Complete**: Tap the checkbox to mark as done
- **Edit**: Long press or tap the task for options
- **Delete**: Use the task options menu
- **Add Comments**: Open task details to add comments

### Filtering and Search
- Use the search bar to find specific tasks
- Apply filters by category, priority, or completion status
- Toggle between list and calendar views

### Notifications
- Grant notification permissions when prompted
- Set due dates to receive deadline reminders
- Customize notification preferences in settings

## Project Structure 📁

```
app/
├── src/main/java/com/example/todo/
│   ├── MainActivity.kt          # Main activity and UI components
│   ├── TodoApp.kt              # Application class and notification setup
│   ├── ReminderScheduler.kt    # Notification scheduling logic
│   ├── ReminderReceiver.kt     # Notification broadcast receiver
│   └── ui/theme/               # Theme and styling components
├── src/main/res/               # Resources (layouts, strings, etc.)
└── build.gradle.kts            # App-level build configuration
```

## Contributing 🤝

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Use Jetpack Compose best practices
- Add appropriate comments for complex logic
- Test on multiple device sizes and Android versions

## Roadmap 🗺️

- [ ] **Data Persistence**: Local database integration
- [ ] **Cloud Sync**: Firebase/Google Drive synchronization
- [ ] **Team Collaboration**: Shared tasks and projects
- [ ] **Advanced Analytics**: Detailed productivity insights
- [ ] **Widget Support**: Home screen widgets
- [ ] **Export/Import**: Backup and restore functionality
- [ ] **Voice Input**: Voice-to-text task creation
- [ ] **Subtasks**: Hierarchical task organization

## Known Issues 🐛

- Notification permissions may require manual enabling on some devices
- Calendar view performance optimization in progress
- Theme switching animation improvements planned

## License 📄

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support 💬

If you encounter any issues or have questions:
- Create an issue on GitHub
- Check existing issues for solutions
- Contact the development team

## Acknowledgments 🙏

- Material Design team for design guidelines
- Jetpack Compose team for the amazing UI toolkit
- Android development community for inspiration and support

---

**Made with ❤️ using Jetpack Compose**

*Last updated: September 2025*
