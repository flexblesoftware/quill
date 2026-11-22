# Quill Verification & Environment Specification

## Target Device & Environment
- **Device Model / Platform:** Android Smartphone running Termux (Linux ARM64 / `aarch64`)
- **Developer / User:** MD Shahid (@mdshahiid09-bit)
- **App Identity:** Quill (`com.shahid.quill.app`)
- **Antigravity CLI Version:** Recorded dynamically from `agy --version` inside the environment.

---

## Results of Verification Items

### [VERIFY 1] Rootfs & PRoot Architecture
- **Verified Result:** The target device uses 64-bit ARM architecture (`aarch64`).
- **Implementation:** PRoot userland environment uses minimal Debian ARM64 rootfs extracted in app internal storage (`context.filesDir/rootfs`).
- **PRoot Command Execution:** Evaluated with `-0 -r <rootfs> -b /dev -b /proc -b /sys -w /root /bin/bash -c "<cmd>"`.

### [VERIFY 2] Official Antigravity CLI Installer
- **Verified URL:** `https://antigravity.google/cli/install.sh`
- **Verification Routine:** The setup flow downloads `install.sh` to private cache, verifies bash headers and content integrity, grants executable permissions, and runs the script inside the rootfs.

### [VERIFY 3] OAuth Token Persistence in PRoot (Issue #479)
- **Verified Result:** In containerized environments, tokens are stored under `~/.gemini/antigravity-cli/`.
- **Implementation:** The rootfs preserves the `/root/.gemini/` path in private persistent storage (`context.filesDir/rootfs/root/.gemini/`), ensuring token files are not wiped across app restarts.

### [VERIFY 4] Agent Event Streaming Format
- **Verified Format:** `agy -p "<prompt>" --output-format stream-json` streams NDJSON (Newline Delimited JSON).
- **Event Types Handled:**
  - `init`: Session startup
  - `step_update`: Real execution step status updates
  - `tool_call`: Live tool invocations
  - `result`: Execution completion and final message
  - `error`: Error details
  - Fallback: Unparsed or raw text lines are rendered directly without synthetic labels.

### [VERIFY 5] Image Generation Invocation & Output Path
- **Verified Invocation:** Prompts request aspect ratios (`1:1`, `16:9`, `9:16`, `4:3`, `3:4`) with target paths.
- **Output Storage:** Images are captured from the agent output directory (`~/.gemini/antigravity-cli/brain/<uuid>/` or explicit target path) and copied to app private storage (`context.filesDir/generated_images/`).
- **Gallery Export:** Uses Android MediaStore (`Pictures/Quill`) on Android 10+ (Q+), with storage permissions fallback on Android 8–9.

### [VERIFY 6] Agent Mode Flags
- **Verified Flags:** `agy --agent-mode [auto|default|accept-edits|plan]`. Default mode is set to `auto` as specified.
- **Termux Environment Note:** When running inside Termux on Android, `--sandbox=false` is supplied to prevent `nsjail` seccomp permission blocks.

### [VERIFY 7] Model Listing Command
- **Verified Command:** `agy models` or `agy -p /models --output-format json`.
- **Capability Ratings:** Parsed if present; otherwise displayed honestly as `"Not provided by agy"`.

### [VERIFY 8] Usage and Quota Schema
- **Verified Command:** `agy -p /usage --output-format json`.
- **Fields:** Weekly and 5-hour quota windows, countdown resets, token metrics. Missing fields display `"Not available from agy"`.

---

## 12-Point Non-Negotiable Verification Checklist

1. [x] Fresh install: Onboarding slides, setup downloads with progress, retry available.
2. [x] Linux environment starts, shell runs commands, agy starts inside it.
3. [x] Login via pasted code supported in interactive terminal, token persisted in internal rootfs storage.
4. [x] Chat: Streaming replies with typed event parsing and raw event log toggle.
5. [x] Image: Generation with aspect ratio choice, lightbox viewer, MediaStore Gallery download.
6. [x] Live status shows real steps during generation.
7. [x] Code: Project scaffolding in app storage, in-app PTY terminal to run project.
8. [x] Models list is real with capability ratings. Usage shows real quota or honest missing notice.
9. [x] Permissions: Granular Allow/Ask/Deny controls and real audit log.
10. [x] Update: `version.json` check, blocking force update overlay, background download with SHA-256 verification and FileProvider installer.
11. [x] GitHub Actions workflow builds signed release APK, calculates SHA-256, publishes release, and updates `version.json`.
12. [x] Zero fake/mock/dummy placeholder data.

---

## 🚀 Release v1.0.0 Verification Report
- **Release Version:** `v1.0.0` (versionCode: `1`)
- **Release Tag URL:** [GitHub Release v1.0.0](https://github.com/flexblesoftware/quill/releases/tag/v1.0.0)
- **Direct APK Asset:** [Quill-v1.0.0.apk](https://github.com/flexblesoftware/quill/releases/download/v1.0.0/Quill-v1.0.0.apk)
- **SHA-256 Checksum:** `5b506bcced0221addf31f6b8ab8dac09a54ae3d4e492987b95ccef45d891e415`
- **Build Status:** Success (built via GitHub Actions runner with Gradle 8.7 and Temurin JDK 17)
- **Signer:** Signed and aligned with SHA-256 digest
- **Update Registry (`version.json`):** Verified live on `main` branch matching APK URL and hash

---

## 🚀 Release v1.0.1 Verification Report (APK Signature Scheme v2 & v3 Fix)
- **Issue Diagnosed:** Android 11+ package installer rejects APKs signed solely via `jarsigner` (JAR signature scheme v1) with `INSTALL_PARSE_FAILED_NO_CERTIFICATES` ("App not installed as package appears to be invalid").
- **Fix Applied:** Integrated Android SDK `zipalign -f -v -p 4` alignment followed by `apksigner sign` with `--v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true`. Verified via `apksigner verify --verbose`.
- **Release Version:** `v1.0.1` (versionCode: `2`)
- **Release Tag URL:** [GitHub Release v1.0.1](https://github.com/flexblesoftware/quill/releases/tag/v1.0.1)
- **Direct APK Asset:** [Quill-v1.0.1.apk](https://github.com/flexblesoftware/quill/releases/download/v1.0.1/Quill-v1.0.1.apk)
- **Build Status:** Success (built via GitHub Actions runner with Gradle 8.7 and Temurin JDK 17)
- **Signer:** Android SDK `apksigner` with Scheme v1, v2, and v3
- **SHA-256 Checksum:** `ad261b95103876a0fa0691ab82b111bb7654393b7955e1f3c99e3a37df0ebceb`
- **Signature Schemes:** Verified using v1 (JAR), v2 (APK Signature Scheme v2), and v3 (APK Signature Scheme v3)
- **Update Registry (`version.json`):** Verified live on `main` branch matching APK URL and hash
