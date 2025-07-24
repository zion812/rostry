# Quick Test Execution Guide

## Prerequisites
1. **Android Device/Emulator**: Ensure you have a connected Android device or running emulator
2. **Developer Options**: Enable USB debugging on your device
3. **ADB**: Verify ADB can detect your device with `adb devices`

## Quick Test Commands

### 1. Build and Install App
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### 2. Run Specific Test Classes
```bash
# Navigation Tests
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.rio.rostry.NavigationTest

# Button Interaction Tests
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.rio.rostry.ButtonInteractionTest

# Screen-Specific Tests
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.rio.rostry.ScreenSpecificTest

# Comprehensive UI Tests
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.rio.rostry.ComprehensiveUITest
```

### 3. Run All Tests
```bash
./gradlew connectedDebugAndroidTest
```

### 4. Using the Batch Script
```bash
# Windows
run_all_tests.bat

# Or manually execute the commands in the script
```

## Test Coverage Summary

### ✅ Screens Tested (15 screens)
1. **Login Screen** - Authentication, validation, navigation
2. **Register Screen** - Form validation, account creation
3. **Forgot Password Screen** - Password reset functionality
4. **Home Screen** - Main dashboard, quick actions
5. **Marketplace Screen** - Browse fowls, search, filters
6. **My Fowls Screen** - User's fowl management
7. **Add Fowl Screen** - Create new fowl listings
8. **Edit Fowl Screen** - Modify existing fowls
9. **Fowl Detail Screen** - View fowl information
10. **Cart Screen** - Shopping cart management
11. **Chat List Screen** - View conversations
12. **Chat Detail Screen** - Individual chat interface
13. **Profile Screen** - User profile and settings
14. **Create Post Screen** - Social posting functionality
15. **Transfer/Verification Screens** - Ownership transfers

### ✅ Navigation Flows Tested (20+ flows)
- **Bottom Navigation**: Home ↔ Marketplace ↔ My Fowls ↔ Chat ↔ Profile
- **Authentication Flow**: Login → Register → Forgot Password
- **Deep Navigation**: Screen → Detail → Edit → Back
- **Cross-Screen Navigation**: Home → Marketplace → Cart
- **State Preservation**: Tab switching with data retention

### ✅ Button Interactions Tested (50+ buttons)
- **Primary Actions**: Login, Register, Save, Update, Delete
- **Navigation**: Back buttons, tab buttons, link buttons
- **Form Controls**: Submit, Cancel, Reset buttons
- **Interactive Elements**: Add to cart, quantity controls
- **Media Actions**: Image selection, camera access

## Manual Testing Checklist

Use `MANUAL_TESTING_CHECKLIST.md` for comprehensive manual validation:

### Critical Test Areas
- [ ] **Authentication Flow** - Login/Register/Password Reset
- [ ] **Core Navigation** - All screen transitions work
- [ ] **Data Operations** - CRUD operations for fowls/posts
- [ ] **Shopping Flow** - Browse → Add to Cart → Checkout
- [ ] **Communication** - Chat functionality
- [ ] **User Management** - Profile updates, logout

### Edge Cases to Test
- [ ] **Network Issues** - Offline/poor connectivity
- [ ] **Empty States** - No data scenarios
- [ ] **Error Handling** - Invalid inputs, server errors
- [ ] **Performance** - Large datasets, slow devices
- [ ] **Accessibility** - Screen readers, font scaling

## Test Results

### Automated Test Reports
- **Location**: `app/build/reports/androidTests/connected/index.html`
- **Coverage**: All test classes and methods
- **Metrics**: Pass/fail rates, execution times

### Manual Test Documentation
- **Checklist**: Track manual test completion
- **Bug Reports**: Document any issues found
- **Performance Notes**: Record any performance concerns

## Troubleshooting

### Common Issues
1. **Build Failures**: Check dependencies and SDK versions
2. **Test Failures**: Verify device/emulator state
3. **Connection Issues**: Ensure ADB is working
4. **Timeout Errors**: Increase test timeouts if needed

### Debug Commands
```bash
# Check connected devices
adb devices

# Clear app data
adb shell pm clear com.rio.rostry

# View logs during testing
adb logcat | grep "TestRunner"
```

## Next Steps

1. **Execute Tests**: Run the automated test suite
2. **Review Results**: Check test reports for failures
3. **Manual Validation**: Complete manual testing checklist
4. **Document Issues**: Record any bugs or concerns
5. **Iterate**: Fix issues and re-test

## Test Maintenance

- **Update Tests**: When UI changes are made
- **Add Tests**: For new features or bug fixes
- **Review Coverage**: Ensure all critical paths are tested
- **Performance**: Monitor test execution times