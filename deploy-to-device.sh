#!/bin/bash

# Android App Deployment Script
# This script builds and deploys the iPOS app to a connected Android device

echo "=========================================="
echo "iPOS Android App Deployment"
echo "=========================================="
echo ""

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ Error: adb not found. Please install Android SDK Platform Tools."
    exit 1
fi

# Check for connected devices
echo "📱 Checking for connected devices..."
DEVICES=$(adb devices | grep -v "List" | grep "device$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No devices connected. Please connect your Android device via USB."
    echo ""
    echo "Troubleshooting:"
    echo "1. Enable USB Debugging on your device"
    echo "2. Connect device via USB"
    echo "3. Accept USB debugging prompt on device"
    echo "4. Run 'adb devices' to verify connection"
    exit 1
fi

echo "✅ Found $DEVICES connected device(s)"
adb devices
echo ""

# Ask user what to do
echo "Select deployment option:"
echo "1) Quick install (installDebug)"
echo "2) Clean build and install"
echo "3) Build APK only"
echo "4) Install and launch app"
read -p "Enter choice [1-4]: " choice

case $choice in
    1)
        echo ""
        echo "🔨 Building and installing app..."
        ./gradlew installDebug
        if [ $? -eq 0 ]; then
            echo ""
            echo "✅ App installed successfully!"
            echo "📱 Open the app on your device to test."
        else
            echo ""
            echo "❌ Build/install failed. Check errors above."
            exit 1
        fi
        ;;
    2)
        echo ""
        echo "🧹 Cleaning project..."
        ./gradlew clean
        echo ""
        echo "🔨 Building and installing app..."
        ./gradlew assembleDebug installDebug
        if [ $? -eq 0 ]; then
            echo ""
            echo "✅ App installed successfully!"
            echo "📱 Open the app on your device to test."
        else
            echo ""
            echo "❌ Build/install failed. Check errors above."
            exit 1
        fi
        ;;
    3)
        echo ""
        echo "🔨 Building APK..."
        ./gradlew assembleDebug
        if [ $? -eq 0 ]; then
            echo ""
            echo "✅ APK built successfully!"
            echo "📦 Location: app/build/outputs/apk/debug/app-debug.apk"
            echo ""
            echo "To install manually, run:"
            echo "adb install -r app/build/outputs/apk/debug/app-debug.apk"
        else
            echo ""
            echo "❌ Build failed. Check errors above."
            exit 1
        fi
        ;;
    4)
        echo ""
        echo "🔨 Building and installing app..."
        ./gradlew installDebug
        if [ $? -eq 0 ]; then
            echo ""
            echo "✅ App installed successfully!"
            echo "🚀 Launching app..."
            adb shell am start -n com.example.learncompose303/.MainActivity
            echo ""
            echo "✅ App launched on device!"
        else
            echo ""
            echo "❌ Build/install failed. Check errors above."
            exit 1
        fi
        ;;
    *)
        echo "❌ Invalid choice. Exiting."
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "Deployment Complete!"
echo "=========================================="
