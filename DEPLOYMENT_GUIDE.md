# Android App Deployment Guide

## Prerequisites

1. **Android device connected via USB** with USB debugging enabled
2. **ADB (Android Debug Bridge)** installed (comes with Android SDK)
3. **Java JDK** installed (required for Gradle)

## Quick Start

### Windows Users
```bash
# Double-click or run:
deploy-to-device.bat
```

### Linux/Mac Users
```bash
# Make executable and run:
chmod +x deploy-to-device.sh
./deploy-to-device.sh
```

## Manual Commands

### 1. Check Device Connection
```bash
adb devices
```

Expected output:
```
List of devices attached
ABC123XYZ    device
```

### 2. Quick Install (Fastest)
```bash
./gradlew installDebug
```
or on Windows:
```bash
gradlew.bat installDebug
```

### 3. Clean Build and Install
```bash
./gradlew clean assembleDebug installDebug
```

### 4. Build APK Only
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

### 5. Install and Launch
```bash
./gradlew installDebug
adb shell am start -n com.example.learncompose303/.MainActivity
```

## Troubleshooting

### Device Not Detected

**Problem:** `adb devices` shows no devices

**Solutions:**
1. Enable USB Debugging:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times to enable Developer Options
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

2. Accept USB debugging prompt on device

3. Try different USB cable (some cables are charge-only)

4. Restart ADB:
   ```bash
   adb kill-server
   adb start-server
   adb devices
   ```

### Build Fails

**Problem:** Gradle build errors

**Solutions:**
1. Clean and rebuild:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

2. Check Java version:
   ```bash
   java -version
   ```
   (Should be JDK 11 or higher)

3. Sync Gradle files in Android Studio

### Installation Fails

**Problem:** App won't install

**Solutions:**
1. Uninstall existing app first:
   ```bash
   adb uninstall com.example.learncompose303
   ./gradlew installDebug
   ```

2. Use force reinstall:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### App Crashes on Launch

**Problem:** App crashes immediately

**Solutions:**
1. Check logs:
   ```bash
   adb logcat | grep "learncompose303"
   ```

2. Clear app data:
   ```bash
   adb shell pm clear com.example.learncompose303
   ```

3. Rebuild with clean:
   ```bash
   ./gradlew clean installDebug
   ```

## Testing the Sync Implementation

After deploying, test the new automatic sync:

### Test 1: Create Invoice
1. Open app and create a new invoice
2. Check logs: `adb logcat | grep "SyncManager"`
3. Should see: "Auto-sync scheduled for new invoice"

### Test 2: Update Product
1. Update a product quantity
2. Check logs for: "Auto-sync scheduled for updated product"

### Test 3: Swipe Refresh
1. Swipe down on main screen
2. Should see sync indicator
3. Should NOT navigate to login screen
4. Check logs for: "SwipeRefresh triggered - starting sync operations"

### Test 4: Offline Sync
1. Turn off device network
2. Create invoice/product
3. Turn on network
4. Check if items sync automatically

## Viewing Logs

### All app logs:
```bash
adb logcat | grep "learncompose303"
```

### Sync-specific logs:
```bash
adb logcat | grep -E "SyncManager|SyncInvoiceWorker|SyncProductWorker|InvoiceRepositoryImpl|ProductRepositoryImpl"
```

### Clear logs and start fresh:
```bash
adb logcat -c
adb logcat | grep "SyncManager"
```

## Build Variants

### Debug Build (Development)
```bash
./gradlew installDebug
```
- Includes debugging symbols
- Larger APK size
- Faster build time

### Release Build (Production)
```bash
./gradlew assembleRelease
```
- Optimized and minified
- Requires signing configuration
- Smaller APK size

## Performance Tips

### Faster Builds
1. Enable Gradle daemon (usually enabled by default)
2. Increase Gradle memory in `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Xmx2048m
   ```

### Incremental Builds
```bash
# Only rebuild changed files
./gradlew installDebug
```

### Parallel Builds
```bash
./gradlew installDebug --parallel
```

## Common Commands Reference

| Command | Description |
|---------|-------------|
| `adb devices` | List connected devices |
| `adb install -r app.apk` | Install/reinstall APK |
| `adb uninstall com.example.learncompose303` | Uninstall app |
| `adb shell am start -n com.example.learncompose303/.MainActivity` | Launch app |
| `adb shell pm clear com.example.learncompose303` | Clear app data |
| `adb logcat` | View device logs |
| `adb logcat -c` | Clear logs |
| `./gradlew clean` | Clean build artifacts |
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew installDebug` | Build and install |

## Next Steps

After successful deployment:
1. ✅ Test automatic sync functionality
2. ✅ Verify swipe refresh works without navigation
3. ✅ Check offline sync behavior
4. ✅ Monitor logs for any errors
5. ✅ Test with real data

## Support

If you encounter issues:
1. Check this guide's troubleshooting section
2. Review logs with `adb logcat`
3. Verify device connection with `adb devices`
4. Try clean build: `./gradlew clean installDebug`
