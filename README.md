CalorIQ Testing Instructions
1. Environment Requirements
Android Studio (latest version recommended), Android SDK 33 or higher, Kotlin support, Physical device(Android device) or emulator (camera permission required for scan feature)
2. Clone the Project
git clone https://github.com/yun201/CalorIQ.git
cd CalorIQ
3. Running the Project
Open the project root directory in Android Studio.
Wait for Gradle to finish building and syncing dependencies.
Connect your device or start an emulator.
Click the “Run” button to launch the app.
4. Permissions
The first time you enter the “Scan Food” page, the app will request camera permission. Please grant it, or the scan feature will not work.
5. Test Account
A test account is built in. On the login page, you’ll see available test credentials at the bottom. For example:
Username: 123
Password: 123
6. Feature Testing Guide
Login/Register: Use the test account to log in, or click “Create Account” to register a new account (registration is local only).
Main Screen: View today’s total intake and recent scan history.
Scan Food: Tap the “Scan Food” button at the bottom. Grant camera permission if prompted. Tap “Capture” to simulate a scan and view the food report.
History Details: Tap a history item to view its detailed nutrition report.
Profile: Tap the avatar in the top right to access your profile. You can change your email and password or log out to return to the login screen.
