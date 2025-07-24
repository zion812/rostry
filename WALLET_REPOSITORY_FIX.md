# WalletRepository Fix

The issue is that DAO operations (which are suspend functions) are being called inside Firestore transactions. 

Lines 92-93 and 151-152 need to be moved outside the transaction block.

The fix is to:
1. Complete the Firestore transaction first
2. Then update the local database after the transaction succeeds

This ensures proper separation of concerns and avoids suspension function issues.