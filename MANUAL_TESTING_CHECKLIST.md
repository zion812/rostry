# Rostry App - Comprehensive Manual Testing Checklist

## Authentication Screens

### Login Screen
- [ ] Email input field accepts valid email formats
- [ ] Password input field masks characters
- [ ] "Login" button is clickable and functional
- [ ] "Don't have an account? Register" link navigates to Register screen
- [ ] "Forgot Password?" link navigates to Forgot Password screen
- [ ] Form validation works (empty fields, invalid email)
- [ ] Loading state displays during authentication
- [ ] Error messages display for invalid credentials
- [ ] Successful login navigates to Home screen

### Register Screen
- [ ] All input fields (Name, Email, Password, Confirm Password) are functional
- [ ] Password confirmation validation works
- [ ] "Register" button is clickable and functional
- [ ] "Already have an account? Login" link navigates to Login screen
- [ ] Form validation works for all fields
- [ ] Loading state displays during registration
- [ ] Error messages display for registration failures
- [ ] Successful registration navigates to Home screen

### Forgot Password Screen
- [ ] Email input field accepts valid email formats
- [ ] "Reset Password" button is clickable and functional
- [ ] Back navigation button works
- [ ] Form validation works for email field
- [ ] Success message displays after password reset request
- [ ] Error messages display for failures

## Main App Navigation

### Bottom Navigation Bar
- [ ] Home tab is clickable and navigates to Home screen
- [ ] Marketplace tab is clickable and navigates to Marketplace screen
- [ ] My Fowls tab is clickable and navigates to My Fowls screen
- [ ] Chat tab is clickable and navigates to Chat screen
- [ ] Profile tab is clickable and navigates to Profile screen
- [ ] Active tab is visually highlighted
- [ ] Navigation preserves state when switching tabs
- [ ] Bottom bar is hidden on detail screens

## Home Screen
- [ ] Screen displays properly with all content
- [ ] "Browse Marketplace" button navigates to Marketplace
- [ ] "My Fowls" button navigates to My Fowls screen
- [ ] "Create Post" button navigates to Create Post screen
- [ ] Recent posts are displayed (if any)
- [ ] Pull-to-refresh functionality works
- [ ] Loading states display properly

## Marketplace Screen
- [ ] Screen displays properly with fowl listings
- [ ] Search bar is functional and filters results
- [ ] Filter buttons work (All, Chickens, Ducks, Geese, Turkeys)
- [ ] Cart icon in top bar is clickable and shows cart count
- [ ] Cart icon navigates to Cart screen
- [ ] Fowl items are clickable and navigate to Fowl Detail screen
- [ ] "Add to Cart" buttons on fowl items work
- [ ] Loading states display when fetching data
- [ ] Empty state displays when no fowls available
- [ ] Pull-to-refresh functionality works

## My Fowls Screen
- [ ] Screen displays user's fowls properly
- [ ] "Add New Fowl" button navigates to Add Fowl screen
- [ ] Fowl items display correctly with images and details
- [ ] Edit buttons on fowl items navigate to Edit Fowl screen
- [ ] Delete buttons work and show confirmation dialog
- [ ] Fowl items are clickable and navigate to Fowl Detail screen
- [ ] Empty state displays when no fowls owned
- [ ] Pull-to-refresh functionality works

## Add Fowl Screen
- [ ] All form fields are functional (Name, Type, Breed, Age, Price, Description)
- [ ] Type dropdown/picker works properly
- [ ] "Select Image" button opens image picker
- [ ] Selected image displays in preview
- [ ] "Save Fowl" button validates form and saves fowl
- [ ] Back navigation button works
- [ ] Form validation displays appropriate error messages
- [ ] Loading state displays during save operation
- [ ] Success navigation back to My Fowls screen

## Edit Fowl Screen
- [ ] Form pre-populates with existing fowl data
- [ ] All form fields are editable
- [ ] Image can be changed
- [ ] "Update Fowl" button saves changes
- [ ] "Delete Fowl" button shows confirmation and deletes
- [ ] Back navigation button works
- [ ] Form validation works
- [ ] Loading states display properly
- [ ] Success navigation back to My Fowls screen

## Fowl Detail Screen
- [ ] Fowl information displays correctly
- [ ] Images display properly with swipe/scroll functionality
- [ ] "Add to Cart" button works (for marketplace fowls)
- [ ] "Edit" button appears for owned fowls and navigates to Edit screen
- [ ] Back navigation button works
- [ ] Contact seller functionality works
- [ ] Price and details are clearly displayed

## Cart Screen
- [ ] Cart items display correctly
- [ ] Quantity controls (+ and -) work
- [ ] Remove item buttons work
- [ ] Total price calculates correctly
- [ ] "Checkout" button is functional
- [ ] Empty cart state displays when no items
- [ ] Back navigation button works

## Chat Screens

### Chat List Screen
- [ ] Chat conversations display correctly
- [ ] Chat items are clickable and navigate to Chat Detail
- [ ] Unread message indicators work
- [ ] Last message preview displays
- [ ] Timestamps display correctly
- [ ] Empty state displays when no chats

### Chat Detail Screen
- [ ] Messages display in correct order
- [ ] Message input field is functional
- [ ] Send button works
- [ ] Messages send and receive in real-time
- [ ] Back navigation button works
- [ ] Keyboard behavior is proper
- [ ] Message timestamps display

## Profile Screen
- [ ] User information displays correctly
- [ ] Profile picture displays
- [ ] "Edit Profile" button works (if implemented)
- [ ] "Logout" button works and returns to login
- [ ] Settings options are functional
- [ ] Statistics display correctly (fowls owned, posts, etc.)

## Create Post Screen
- [ ] Title input field is functional
- [ ] Content text area is functional
- [ ] "Add Image" button opens image picker
- [ ] Selected images display in preview
- [ ] "Post" button validates and creates post
- [ ] Back navigation button works
- [ ] Form validation works
- [ ] Loading state displays during post creation
- [ ] Success navigation back to Home screen

## General UI/UX Testing

### Navigation
- [ ] All back buttons work correctly
- [ ] Deep linking works (if implemented)
- [ ] Navigation state is preserved correctly
- [ ] No navigation loops or dead ends

### Buttons and Interactions
- [ ] All buttons have proper touch feedback
- [ ] Buttons are appropriately sized for touch
- [ ] Loading states prevent multiple taps
- [ ] Disabled states are visually clear
- [ ] Long press actions work where implemented

### Forms and Input
- [ ] All text inputs accept appropriate input
- [ ] Input validation provides clear feedback
- [ ] Keyboard types are appropriate for input fields
- [ ] Form submission prevents empty required fields
- [ ] Error states are clearly communicated

### Visual Design
- [ ] All screens follow consistent design patterns
- [ ] Text is readable and appropriately sized
- [ ] Images load and display correctly
- [ ] Loading indicators are visible and appropriate
- [ ] Error states have clear visual feedback

### Performance
- [ ] Screen transitions are smooth
- [ ] Images load efficiently
- [ ] No noticeable lag in interactions
- [ ] App doesn't crash during normal usage
- [ ] Memory usage is reasonable

### Edge Cases
- [ ] App handles network connectivity issues
- [ ] App handles empty states gracefully
- [ ] App handles error states appropriately
- [ ] App works with different screen sizes
- [ ] App handles orientation changes (if supported)

## Device-Specific Testing

### Android Specific
- [ ] Back button behavior is correct
- [ ] App handles system notifications
- [ ] App handles incoming calls/interruptions
- [ ] App works with different Android versions
- [ ] App handles permission requests properly

### Accessibility
- [ ] Content descriptions are provided for images/buttons
- [ ] Text is readable with system font scaling
- [ ] App works with TalkBack/screen readers
- [ ] Color contrast meets accessibility standards
- [ ] Touch targets meet minimum size requirements

## Data and State Management
- [ ] Data persists between app sessions
- [ ] User authentication state is maintained
- [ ] Cart state is preserved
- [ ] Form data is preserved during navigation
- [ ] Offline functionality works (if implemented)

## Security Testing
- [ ] User data is properly secured
- [ ] Authentication tokens are handled securely
- [ ] Sensitive information is not logged
- [ ] API calls are made over HTTPS
- [ ] User input is properly sanitized

## Testing Notes
- Test on multiple devices with different screen sizes
- Test with different network conditions (WiFi, mobile data, offline)
- Test with different user accounts and data states
- Test edge cases like very long text, special characters
- Test interruption scenarios (phone calls, notifications)
- Document any bugs or issues found during testing