1# 🐔 Rostry - Fowl Management & Marketplace

> A comprehensive Android application for fowl farming management, marketplace, and community interaction.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Kotlin](https://img.shields.io/badge/kotlin-100%25-blue.svg)]()
[![Android](https://img.shields.io/badge/platform-Android-green.svg)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)]()

## 📱 Features

### 🔐 Authentication & User Management
- **User Registration** with role selection (General, Farmer, Enthusiast)
- **Secure Login** with email/password
- **Password Reset** functionality
- **Google Sign-In** integration ready
- **Profile Management** with customizable user information

### 🐔 Fowl Management System
- **Add Fowls** with comprehensive details (breed, type, age, weight, etc.)
- **Edit & Update** fowl information
- **Health Records** tracking and management
- **Image Upload** for fowl documentation
- **Toggle Sale Status** to list fowls in marketplace
- **Detailed View** with complete fowl information

### 🛒 Marketplace & E-commerce
- **Browse Fowls** available for sale
- **Search & Filter** by breed, type, price, location
- **Shopping Cart** with quantity management
- **Checkout Process** for purchasing
- **Seller Communication** via integrated chat

### 💬 Real-time Communication
- **Direct Messaging** between users
- **Chat History** with message persistence
- **Read Status** indicators
- **Unread Message** notifications
- **Seller-Buyer** communication for transactions

### 📝 Community Features
- **Create Posts** with text and images
- **Post Categories** (General, Tips, Questions, Showcase)
- **Community Guidelines** integration
- **Location Tagging** for posts
- **Feed Display** with latest community content

### 👤 Profile & Settings
- **User Profiles** with role-based features
- **Settings Management**
- **Account Information** editing
- **Logout** functionality

## 🏗️ Technical Architecture

### Frontend
- **Framework**: Jetpack Compose
- **Design System**: Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Compose Navigation with Bottom Navigation
- **State Management**: StateFlow and Compose State

### Backend & Data
- **Local Database**: Room Database with SQLite
- **Remote Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **File Storage**: Firebase Storage
- **Repository Pattern**: Clean data layer separation

### Development Tools
- **Language**: Kotlin 100%
- **Build System**: Gradle with Kotlin DSL
- **Dependency Injection**: Hilt (Dagger-Hilt)
- **Image Loading**: Coil
- **Async Operations**: Kotlin Coroutines

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9+
- Gradle 8.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/rostry.git
   cd rostry
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Firebase Setup** (Optional for basic functionality)
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add Android app with package name `com.rio.rostry`
   - Download `google-services.json` and place in `app/` directory
   - Enable Authentication, Firestore, and Storage

4. **Build and Run**
   ```bash
   # Using Gradle
   ./gradlew installDebug
   
   # Or use the quick start script
   quick_start.bat
   ```

### Quick Start Script
Run the included `quick_start.bat` for automated build and installation:
```bash
quick_start.bat
```

## 📱 Usage Guide

### First Time Setup
1. **Launch the app** on your Android device
2. **Register** a new account (select "Farmer" role for full features)
3. **Complete profile** setup
4. **Explore features** using the bottom navigation

### Adding Your First Fowl
1. Navigate to **"My Fowls"** tab
2. Tap **"Add Fowl"** button
3. Fill in fowl details (name, breed, type, etc.)
4. Add photos (optional)
5. Save the fowl record

### Using the Marketplace
1. Navigate to **"Marketplace"** tab
2. Browse available fowls
3. Tap on fowls for detailed information
4. Add to cart and proceed to checkout
5. Contact sellers via integrated chat

### Community Interaction
1. Navigate to **"Home"** tab for community feed
2. Create posts to share experiences
3. Use **"Messages"** tab for direct communication
4. Engage with other community members

## ��� Configuration

### Environment Setup
The app supports different configurations for development and production:

- **Debug Build**: Uses local database with Firebase optional
- **Release Build**: Requires Firebase configuration for full functionality

### Firebase Configuration
For production deployment, configure Firebase services:

1. **Authentication**: Email/password and Google Sign-In
2. **Firestore**: Real-time database for app data
3. **Storage**: Image and file storage
4. **Analytics**: User behavior tracking (optional)

### Permissions
The app requires these Android permissions:
- `INTERNET` - For network communication
- `CAMERA` - For taking fowl photos
- `READ_EXTERNAL_STORAGE` - For selecting images

## 📊 Project Structure

```
app/src/main/java/com/rio/rostry/
├── data/
│   ├── local/
│   │   ├── dao/              # Room database DAOs
│   │   └── database/         # Database configuration
│   ├── model/                # Data models and entities
│   └── repository/           # Repository implementations
├── di/                       # Dependency injection modules
├── ui/
│   ├── auth/                # Authentication screens
│   ├── cart/                # Shopping cart functionality
│   ├── chat/                # Messaging system
│   ├── fowls/               # Fowl management screens
│   ├── home/                # Home feed and posts
│   ├── marketplace/         # Marketplace screens
│   ├── navigation/          # Navigation components
│   ├── posts/               # Post creation
│   ├── profile/             # User profile management
│   └── theme/               # UI theme and styling
└── MainActivity.kt          # Main application entry point
```

## 🧪 Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Test Coverage
- **Unit Tests**: Repository and ViewModel logic
- **Integration Tests**: Database operations
- **UI Tests**: Critical user flows

## 🚀 Deployment

### Debug Build
```bash
./gradlew assembleDebug
# APK location: app/build/outputs/apk/debug/
```

### Release Build
```bash
./gradlew assembleRelease
# APK location: app/build/outputs/apk/release/
```

### Play Store Deployment
1. Generate signed APK/AAB
2. Update version code and name
3. Test on multiple devices
4. Upload to Google Play Console

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Write unit tests for new features
- Update documentation for API changes
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Material Design** for UI/UX guidelines
- **Firebase** for backend services
- **Jetpack Compose** for modern Android UI
- **Open Source Community** for excellent libraries

## 📞 Support

For support and questions:
- **Issues**: [GitHub Issues](https://github.com/yourusername/rostry/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/rostry/discussions)
- **Email**: support@rostry.app

## ����️ Roadmap

### Version 2.0 (Planned)
- [ ] Advanced search and filtering
- [ ] Payment gateway integration
- [ ] Push notifications
- [ ] Offline mode improvements
- [ ] Admin dashboard

### Version 3.0 (Future)
- [ ] Multi-language support
- [ ] Advanced analytics
- [ ] AI-powered recommendations
- [ ] IoT device integration
- [ ] Web application

---

**Made with ❤️ for the fowl farming community**