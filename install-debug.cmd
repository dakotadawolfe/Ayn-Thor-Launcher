@echo off
cd /d %~dp0
call gradlew installDebug
pause
