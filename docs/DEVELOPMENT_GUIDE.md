# ROSTRY Development Guide

> **Version**: 1.0.0  
> **Last Updated**: 2025-01-08  
> **Target Audience**: Developers, Contributors  

## 🚀 Getting Started

### Prerequisites

#### Required Software
- **Android Studio**: Hedgehog | 2023.1.1 or later
- **JDK**: 11 or higher (OpenJDK recommended)
- **Git**: Latest version
- **Android SDK**: API 24+ (Android 7.0)

#### Recommended Tools
- **Gradle**: 8.11.1+ (included with Android Studio)
- **Kotlin**: 2.0.21+ (included with Android Studio)
- **Firebase CLI**: For Firebase operations (optional)

### Environment Setup

#### 1. Clone Repository
```bash
git clone https://github.com/company/rostry.git
cd rostry
```

#### 2. Android Studio Configuration
1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the cloned `rostry` directory
4. Wait for Gradle sync to complete

#### 3. Firebase Configuration
1. **Download Configuration File**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Select your ROSTRY project
   - Download `google-services.json`
   - Place it in `app/` directory

2. **Verify Firebase Services**
   - Authentication: Email/Password enabled
   - Firestore: Database created with proper security rules
   - Storage: Bucket configured for image uploads
   - Crashlytics: Enabled for crash reporting

#### 4. Local Properties Setup
Create `local.properties` file in root directory:
```properties
# Android SDK location
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk

# Optional: Firebase project configuration
firebase.project.id=your-project-id
```

### Build Configuration

#### Gradle Configuration
The project uses Gradle Version Catalog for dependency management:

```kotlin
// gradle/libs.versions.toml
[versions]
kotlin = "2.0.21"
compose-bom = "2024.09.00"
hilt = "2.52"
room = "2.6.1"
# ... other versions
```

#### Build Variants
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

#### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

## 🏗️ Project Architecture

### Package Structure
```
com.rio.rostry/
├── MainActivity.kt                 # App entry point
├── RostryApplication.kt           # Application class
├── data/                          # Data layer
│   ├── local/                     # Room database
│   │   ├── dao/                   # Data Access Objects
│   │   ├── RostryDatabase.kt      # Database configuration
│   │   └── Converters.kt          # Type converters
│   ├── model/                     # Data entities
│   └── repository/                # Repository implementations
├── di/                            # Dependency injection
│   ├── DatabaseModule.kt          # Database dependencies
│   ├── FirebaseModule.kt          # Firebase dependencies
│   └── RepositoryModule.kt        # Repository dependencies
├── domain/                        # Business logic
├── ui/                            # Presentation layer
│   ├── auth/                      # Authentication screens
│   ├── fowls/                     # Fowl management
│   ├── marketplace/               # Trading features
│   ├── home/                      # Social feed
│   ├── chat/                      # Messaging
│   ├── dashboard/                 # Analytics
│   ├── profile/                   # User management
│   ├── wallet/                    # Monetization
│   ├── verification/              # KYC system
│   ├── navigation/                # Navigation setup
│   └── theme/                     # UI theming
├── util/                          # Utility classes
└── viewmodel/                     # Shared ViewModels
```

### Architecture Patterns
- **MVVM**: Model-View-ViewModel pattern
- **Repository Pattern**: Data access abstraction
- **Clean Architecture**: Separation of concerns
- **Dependency Injection**: Hilt for DI

## 🛠️ Development Workflow

### Git Workflow
```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and commit
git add .
git commit -m "feat: add your feature description"

# Push to remote
git push origin feature/your-feature-name

# Create Pull Request on GitHub
```

### Commit Message Convention
```
type(scope): description

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation changes
- style: Code style changes
- refactor: Code refactoring
- test: Test additions/modifications
- chore: Build process or auxiliary tool changes

Examples:
feat(fowls): add fowl breeding lineage tracking
fix(auth): resolve login validation issue
docs(api): update repository documentation
```

### Code Style Guidelines

#### Kotlin Style
```kotlin
// Use descriptive names
class FowlRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fowlDao: FowlDao
) {
    
    // Use suspend functions for async operations
    suspend fun addFowl(fowl: Fowl): Result<String> {
        return try {
            // Implementation
            Result.success(fowlId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Use Flow for reactive data
    fun getFowlsByOwnerFlow(ownerId: String): Flow<List<Fowl>> {
        return fowlDao.getFowlsByOwnerFlow(ownerId)
    }
}
```

#### Compose UI Style
```kotlin
@Composable
fun FowlCard(
    fowl: Fowl,
    onFowlClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onFowlClick(fowl.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Card content
    }
}
```

### Testing Strategy

#### Unit Tests
```kotlin
@Test
fun `addFowl should return success when fowl is valid`() = runTest {
    // Given
    val fowl = Fowl(name = "Test Fowl", breed = "Test Breed")
    
    // When
    val result = fowlRepository.addFowl(fowl)
    
    // Then
    assertTrue(result.isSuccess)
}
```

#### UI Tests
```kotlin
@Test
fun testFowlListDisplayed() {
    composeTestRule.setContent {
        RostryTheme {
            MyFowlsScreen(
                onNavigateToAddFowl = {},
                onNavigateToFowlDetail = {}
            )
        }
    }
    
    composeTestRule.onNodeWithText("My Fowls").assertIsDisplayed()
}
```

### Debugging

#### Common Issues & Solutions

1. **Build Failures**
   ```bash
   # Clean and rebuild
   ./gradlew clean
   ./gradlew build
   
   # Check for dependency conflicts
   ./gradlew dependencies
   ```

2. **Firebase Connection Issues**
   - Verify `google-services.json` is in correct location
   - Check Firebase project configuration
   - Ensure internet connectivity

3. **Database Migration Issues**
   ```kotlin
   // Add fallback migration
   .fallbackToDestructiveMigration()
   ```

#### Logging
```kotlin
// Use structured logging
Log.d("FowlRepository", "Adding fowl: ${fowl.name}")
Log.e("FowlRepository", "Failed to add fowl", exception)

// Use Timber for production logging (if implemented)
Timber.d("Adding fowl: %s", fowl.name)
```

## 🧪 Testing

### Test Structure
```
src/
├── test/                          # Unit tests
│   └── java/com/rio/rostry/
│       ├── repository/            # Repository tests
│       ├── viewmodel/             # ViewModel tests
│       └── util/                  # Utility tests
└── androidTest/                   # Instrumented tests
    └── java/com/rio/rostry/
        ├── database/              # Database tests
        ├── ui/                    # UI tests
        └── integration/           # Integration tests
```

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Specific test class
./gradlew test --tests "FowlRepositoryTest"

# Test with coverage
./gradlew testDebugUnitTestCoverage
```

### Test Configuration
```kotlin
// Test dependencies in build.gradle.kts
testImplementation(libs.junit)
testImplementation(libs.mockito.core)
testImplementation(libs.coroutines.test)
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)
androidTestImplementation(libs.compose.ui.test.junit4)
```

## 🚀 Deployment

### Debug Deployment
```bash
# Quick deployment script
./quick_start.bat

# Manual deployment
./gradlew installDebug
adb shell am start -n com.rio.rostry/.MainActivity
```

### Release Deployment
```bash
# Build release APK
./gradlew assembleRelease

# Sign APK (if keystore configured)
./gradlew bundleRelease
```

### Development Scripts
- `quick_start.bat` - Build and install debug APK
- `run_app.bat` - Launch application on device
- `run_all_tests.bat` - Execute complete test suite
- `verify_monetization.bat` - Test monetization features

## 📚 Resources

### Documentation
- [Project Blueprint](PROJECT_BLUEPRINT.md)
- [API Documentation](API_DOCUMENTATION.md)
- [Database Schema](DATABASE_SCHEMA.md)
- [Architecture Overview](ARCHITECTURE_SNAPSHOT.md)

### External Resources
- [Android Developer Guide](https://developer.android.com)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs)

### Community
- [GitHub Issues](https://github.com/company/rostry/issues)
- [GitHub Discussions](https://github.com/company/rostry/discussions)
- [Development Team Slack](https://rostry-team.slack.com)

## 🤝 Contributing

### Pull Request Process
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add/update tests
5. Update documentation
6. Submit pull request

### Code Review Guidelines
- Code follows project style guidelines
- All tests pass
- Documentation is updated
- No breaking changes without discussion
- Performance impact considered

### Issue Reporting
When reporting issues, include:
- Android version and device model
- Steps to reproduce
- Expected vs actual behavior
- Relevant logs or screenshots
- App version and build variant

---

**This development guide provides the foundation for contributing to ROSTRY. For specific questions, please refer to the documentation or reach out to the development team.**
