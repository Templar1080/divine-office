@echo off
setlocal
where java >nul 2>nul || (echo [ERROR] Java 17 not found. Install JDK 17 and add it to PATH. & exit /b 1)
if "%ANDROID_HOME%"=="" if not "%ANDROID_SDK_ROOT%"=="" set ANDROID_HOME=%ANDROID_SDK_ROOT%
if "%ANDROID_HOME%"=="" if exist C:\Android set ANDROID_HOME=C:\Android
if "%ANDROID_HOME%"=="" (echo [ERROR] Set ANDROID_HOME to your Android SDK folder. & exit /b 1)
set "SDKP=%ANDROID_HOME:\=/%"
echo sdk.dir=%SDKP%> local.properties
call gradlew.bat assembleDebug || (echo [ERROR] Build failed. & exit /b 1)
copy /Y app\build\outputs\apk\debug\app-debug.apk DivineOffice.apk >nul
echo.
echo Done: %CD%\DivineOffice.apk
echo To install on a USB-connected phone: adb install -r DivineOffice.apk
