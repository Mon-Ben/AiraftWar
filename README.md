# AircraftWar

Android Java aircraft shooting game with local gameplay, online leaderboard, and two-player socket battle.

## Requirements

- Android Studio
- JDK 11+
- Android SDK

## Build App

```powershell
.\gradlew :app:assembleDebug
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Run Servers

Open separate terminals from the project root.

### Online Leaderboard

```powershell
.\gradlew :leaderboardServer:run
```

Default port: `8080`.

### Two-player Battle

```powershell
.\gradlew :battleServer:run
```

Default port: `9999`.

## Emulator Network

By default Android emulator accesses the host computer with:

```text
10.0.2.2
```

If using `adb reverse`, forward both ports for each emulator:

```powershell
adb -s <device-id> reverse tcp:8080 tcp:8080
adb -s <device-id> reverse tcp:9999 tcp:9999
```

Then set the app server host to `127.0.0.1` in `NetworkConfig.java`.

## Useful Commands

```powershell
.\gradlew :app:assembleDebug :leaderboardServer:build :battleServer:build
.\gradlew :app:testDebugUnitTest
```
