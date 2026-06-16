@echo off
cd /d %~dp0
call gradlew assembleDebug
echo.
echo APK should be at:
echo app\build\outputs\apk\debug\app-debug.apk
pause