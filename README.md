# ROSTRY - Fowl Management & Marketplace

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/company/rostry)
[![Version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/company/rostry/releases)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![API Level](https://img.shields.io/badge/API-24%2B-orange)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)

ROSTRY is a comprehensive Android application for fowl management and marketplace operations. Built with modern Android development practices, it serves as a complete ecosystem for poultry farmers, breeders, and enthusiasts to manage their flocks, trade fowls, and connect with the community.

## 🚀 Quick Start

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 11 or higher
- Android SDK API 24+ (Android 7.0)
- Git

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/company/rostry.git
   cd rostry
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Configure Firebase** (Required)
   - Add your `google-services.json` file to `app/` directory
   - Ensure Firebase Authentication, Firestore, and Storage are enabled

4. **Build and Run**
   ```bash
   # Using Gradle wrapper
   ./gradlew installDebug
   
   # Or use the provided batch script
   quick_start.bat
   ```

## 📱 Features

### Core Functionality
- **Fowl Management**: Complete lifecycle tracking for chickens, ducks, turkeys, geese, and guinea fowl
- **Marketplace**: Buy/sell platform with integrated shopping cart and secure checkout
- **Social Community**: Post sharing, messaging, and community interaction
- **Digital Wallet**: Coin-based economy with premium features
- **Verification System**: KYC verification for sellers and breeders
- **Dashboard Analytics**: Flock management and performance tracking

### Advanced Features
- **Farm Management System**: Complete farm operations with multi-user collaboration
- **Access Control**: Role-based permissions with 25+ granular permissions
- **Flock Management**: Health monitoring, production metrics, and analytics
- **Ownership Transfer**: Secure fowl ownership transfer with verification
- **Health Records**: Comprehensive health and breeding record management
- **Breeding Lineage**: Track parent-offspring relationships with recommendations
- **Real-time Chat**: Messaging system for buyers and sellers
- **Farm Analytics**: Interactive dashboards with performance insights
- **Offline Support**: Local data caching with cloud synchronization

## 🏗️ Architecture

ROSTRY follows **Clean Architecture** principles with **MVVM** pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
├─────────────────────────────────────────────────────────────┤
│  UI (Jetpack Compose) │ ViewModels │ Navigation │ Theme     │
├─────────────────────────────────────────────────────────────┤
│                     DOMAIN LAYER                            │
├─────────────────────────────────────────────────────────────┤
│  Use Cases │ Business Logic │ Domain Models │ Interfaces   │
├─────────────────────────────────────────────────────────────┤
│                      DATA LAYER                             │
├─────────────────────────────────────────────────────────────┤
│  Repositories │ Data Sources │ Local DB │ Remote API       │
├─────────────────────────────────────────────────────────────┤
│                 INFRASTRUCTURE LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  Firebase │ Room Database │ Dependency Injection │ Utils   │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt/Dagger
- **Database**: Room (local) + Firebase Firestore (cloud)
- **Authentication**: Firebase Auth
- **Storage**: Firebase Storage
- **Navigation**: Navigation Compose
- **Image Loading**: Coil
- **Async**: Kotlin Coroutines

## 📁 Project Structure

```
app/src/main/java/com/rio/rostry/
├── MainActivity.kt                    # Entry point
├── RostryApplication.kt              # Application class with Hilt
├── data/                             # Data layer
│   ├── local/                        # Room database
│   │   ├── dao/                      # Data Access Objects (20+ DAOs)
│   │   ├── RostryDatabase.kt         # Database configuration
│   │   └── Converters.kt             # Type converters
│   ├── model/                        # Data entities (25+ entities)
│   └── repository/                   # Repository implementations (15+ repos)
├── di/                               # Dependency injection modules
├── domain/                           # Business logic layer
├── ui/                               # Presentation layer
│   ├── auth/                         # Authentication screens
│   ├── fowls/                        # Fowl management
│   ├── marketplace/                  # Trading platform
│   ├── home/                         # Social feed
│   ├── chat/                         # Messaging
│   ├── dashboard/                    # Analytics
│   ├── profile/                      # User management
│   ├── wallet/                       # Monetization
│   ├── verification/                 # KYC system
│   ├── navigation/                   # Navigation setup
│   └── theme/                        # UI theming
├── util/                             # Utility classes
└── viewmodel/                        # Shared ViewModels
```

## 🗄️ Database Schema

### Room Database (Local Storage)
- **25+ Entities**:
  - **Core**: User, Fowl, Post, Chat, Message, CartItem, FowlRecord, TransferLog, MarketplaceListing, Order, Wallet, CoinTransaction, VerificationRequest, ShowcaseSlot, FlockSummary
  - **Farm Management**: Farm, Flock, FowlLifecycle, FowlLineage
  - **Access Control**: FarmAccess, FarmInvitation, InvitationTemplate, BulkInvitation, AccessAuditLog, PermissionRequest, InvitationAnalytics
- **20+ DAOs**: Comprehensive data access objects with farm management
- **Version**: 7 (with farm management migration support)

### Firebase Collections (Cloud Storage)
- `users` - User profiles and authentication data
- `fowls` - Fowl entities and metadata with lifecycle/lineage
- `farms` - Farm entities with facilities and certifications
- `farm_access` - Access control and permissions
- `farm_invitations` - Invitation system for collaboration
- `posts` - Social media posts
- `chats` - Chat conversations
- `orders` - Purchase orders and transactions
- `transfers` - Ownership transfer records

## 🛠️ Development

### Build Configuration
```kotlin
compileSdk = 36
minSdk = 24
targetSdk = 36
kotlinVersion = "2.0.21"
jvmTarget = "11"
```

### Key Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose | 2024.09.00 | Modern UI framework |
| Hilt | 2.52 | Dependency injection |
| Room | 2.6.1 | Local database |
| Firebase | Latest | Backend services |
| Navigation Compose | 2.8.4 | Screen navigation |
| Coil | 2.7.0 | Image loading |
| Coroutines | 1.9.0 | Async programming |

### Development Scripts
- `quick_start.bat` - Build and install debug APK
- `run_app.bat` - Launch application
- `run_all_tests.bat` - Execute test suite
- `verify_monetization.bat` - Test monetization features

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run all tests
./gradlew check
```

## 📚 Documentation

- [Architecture Overview](docs/ARCHITECTURE_SNAPSHOT.md) - Detailed system architecture
- [API Documentation](docs/api/) - Generated API docs (coming soon)
- [Database Schema](docs/database/) - Database design (coming soon)
- [Development Guide](docs/development/) - Setup and contribution guide (coming soon)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/company/rostry/issues)
- **Discussions**: [GitHub Discussions](https://github.com/company/rostry/discussions)
- **Email**: support@rostry.com

---

**Built with ❤️ by the ROSTRY Team**
