@echo off
REM Android App Deployment Script for Windows
REM This script builds and deploys the iPOS app to a connected Android device

echo ==========================================
echo iPOS Android App Deployment
echo ==========================================
echo.

REM Check for connected devices
echo Checking for connected devices...
adb devices | findstr "device$" >nul
if errorlevel 1 (
    echo No devices connected. Please connect your Android device via USB.
    echo.
    echo Troubleshooting:
    echo 1. Enable USB Debugging on your device
    echo 2. Connect device via USB
    echo 3. Accept USB debugging prompt on device
    echo 4. Run 'adb devices' to verify connection
    pause
    exit /b 1
)

echo Connected devices:
adb devices
echo.

echo Select deployment option:
echo 1. Quick install
echo 2. Clean build and install
echo 3. Build APK only
echo 4. Install and launch app
set /p choice="Enter choice [1-4]: "

if "%choice%"=="1" goto quick
if "%choice%"=="2" goto clean
if "%choice%"=="3" goto build
if "%choice%"=="4" goto launch
goto invalid

:quick
echo.
echo Building and installing app...
call gradlew.bat installDebug
if errorlevel 1 goto error
echo.
echo App installed successfully!
goto end

:clean
echo.
echo Cleaning project...
call gradlew.bat clean
echo.
echo Building and installing app...
call gradlew.bat assembleDebug installDebug
if errorlevel 1 goto error
echo.
echo App installed successfully!
goto end

:build
echo.
echo Building APK...
call gradlew.bat assembleDebug
if errorlevel 1 goto error
echo.
echo APK built: app\build\outputs\apk\debug\app-debug.apk
goto end

:launch
echo.
echo Building and installing app...
call gradlew.bat installDebug
if errorlevel 1 goto error
echo.
echo Launching app...
adb shell am start -n com.example.learncompose303/.MainActivity
echo App launched!
goto end

:invalid
echo Invalid choice.
goto end

:error
echo Build/install failed!
pause
exit /b 1

:end
echo.
echo ==========================================
echo Deployment Complete!
echo ==========================================
pause
