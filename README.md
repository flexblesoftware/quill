# Quill 🪶
**Mobile AI Workspace for Android (Images, Chat, and Code)**

Quill is a production-grade Android application running on your smartphone, powered by the **Google Antigravity CLI (`agy`)** executing natively inside a Linux PRoot container.

---

## 🚀 Key Features

1. **AI Image Studio (Primary Feature):**
   - High-fidelity prompt-based generation with aspect ratio selection (`1:1`, `16:9`, `9:16`, `4:3`, `3:4`).
   - Live generation status directly streamed from real agent events.
   - Lightbox inspection and one-tap download to Android Gallery (`Pictures/Quill`) using MediaStore.

2. **Chat Assistant:**
   - ChatGPT-style conversational assistant with streaming token responses.
   - Live activity logs, tool call status, and raw NDJSON event telemetry inspection.
   - Local conversation persistence and management in Room.

3. **Code Studio & Terminal:**
   - Project generator that creates full project structures inside app private storage.
   - Interactive in-app Linux PTY terminal with a custom keyboard toolbar (`Ctrl`, `Esc`, `Tab`, arrows).
   - Built-in file explorer and syntax viewer/editor.

4. **Telemetry & Controls:**
   - **Models:** Real model engine discovery and capability levels.
   - **Usage:** Live quota telemetry (weekly and 5-hour windows), reset countdown, and token tracking.
   - **Permissions Manager:** Granular Allow/Ask/Deny rules and execution audit logging.
   - **Settings:** OAuth login/logout, data export, cache controls, and GitHub update engine.

---

## 📱 Manual Steps for User (MD Shahid)

1. **Google Account Authentication:**
   - Navigate to **Settings** > tap **Log In via Terminal Session**.
   - An interactive terminal session will start `agy auth login`.
   - Open the generated URL in your browser, approve with your Google account, copy the authorization code, and paste it into the terminal toolbar.

2. **Setting up GitHub Secrets for Releases:**
   In your repository (`flexblesoftware/quill`), navigate to **Settings > Secrets and variables > Actions** and add:
   - `KEYSTORE_BASE64`: Base64 encoded `.keystore` or `.jks` file (`base64 -w 0 your-keystore.jks`).
   - `KEYSTORE_PASSWORD`: Keystore master password.
   - `KEY_ALIAS`: Keystore key alias.
   - `KEY_PASSWORD`: Key password.

3. **Installing the App APK:**
   - Download the release APK from the [GitHub Releases](https://github.com/flexblesoftware/quill/releases) page.
   - When prompted, grant "Install unknown apps" permission for your browser or file manager.
   - Tap Install to run Quill.
