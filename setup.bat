@echo off
setlocal
set "SDK=C:\Android"
set "SM=%SDK%\cmdline-tools\latest\bin\sdkmanager.bat"
echo === Step 1/3: JDK 17 ===
java -version 2>&1 | findstr /C:"17." >nul
if errorlevel 1 (
  echo Installing JDK 17 with winget...
  winget install -e --id EclipseAdoptium.Temurin.17.JDK --accept-package-agreements --accept-source-agreements
  if errorlevel 1 (echo [ERROR] winget failed. Install JDK 17 manually, then re-run setup.bat. & exit /b 1)
  echo.
  echo JDK installed. CLOSE this window, open a NEW CMD, and run setup.bat again.
  exit /b 0
) else (echo JDK 17 found.)
echo.
echo === Step 2/3: Android command-line tools ===
if not exist "%SM%" (
  if exist "%SDK%\cmdline-tools" rmdir /s /q "%SDK%\cmdline-tools"
  if exist "%SDK%\tmp" rmdir /s /q "%SDK%\tmp"
  mkdir "%SDK%" 2>nul
  powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://dl.google.com/android/repository/commandlinetools-win-15859902_latest.zip' -OutFile '%SDK%\cmdtools.zip'"
  if not exist "%SDK%\cmdtools.zip" (echo [ERROR] Download failed. Check internet/VPN. & exit /b 1)
  powershell -NoProfile -Command "Expand-Archive -Force '%SDK%\cmdtools.zip' '%SDK%\tmp'"
  mkdir "%SDK%\cmdline-tools"
  move "%SDK%\tmp\cmdline-tools" "%SDK%\cmdline-tools\latest" >nul
  rmdir /s /q "%SDK%\tmp" 2>nul
  del "%SDK%\cmdtools.zip" 2>nul
)
if not exist "%SM%" (
  echo [ERROR] sdkmanager not found at: %SM%
  echo Contents of %SDK%\cmdline-tools:
  dir /s /b "%SDK%\cmdline-tools" 2>nul
  exit /b 1
)
echo sdkmanager found.
echo.
echo === Step 3/3: SDK packages and licenses ===
del "%TEMP%\yes.txt" 2>nul
for /l %%i in (1,1,30) do @echo y>>"%TEMP%\yes.txt"
call "%SM%" --sdk_root=%SDK% --licenses < "%TEMP%\yes.txt" >nul
call "%SM%" --sdk_root=%SDK% "platform-tools" "platforms;android-34" "build-tools;34.0.0"
if errorlevel 1 (echo [ERROR] sdkmanager failed. Check internet/VPN. & exit /b 1)
setx ANDROID_HOME "%SDK%" >nul
echo.
echo Setup complete. Now run: build.bat
