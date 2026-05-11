# AircraftWar

AircraftWar 是一个基于 Android Java 的飞机大战项目，包含单人游戏、在线排行榜和双人联机对战功能。项目使用 Gradle 构建，主应用为 Android App，同时提供两个可独立运行的 Java 服务端模块。

## 功能简介

- 单人飞机大战
  - 支持简单、普通、困难三种难度。
  - 包含敌机、Boss、子弹、道具、碰撞检测、音效和本地排行榜。

- 全球排行榜
  - App 通过 HTTP 将单人模式分数上传到排行榜服务器。
  - 排行榜服务器提供分数上传、分数查询和健康检查接口。

- 双人联机对战
  - App 通过 TCP Socket 连接联机服务器。
  - 支持创建房间、加入房间和双方进入对战。
  - 服务器负责房间管理和玩家状态转发。

- UML 设计图
  - `uml/` 目录保留项目主要设计模式和模块结构图。

## 项目结构

```text
AircraftWar/
├── app/                    # Android App 主模块
├── battleServer/           # 双人联机 TCP Socket 服务器
├── leaderboardServer/      # 全球排行榜 HTTP 服务器
├── gradle/                 # Gradle Wrapper 和版本配置
├── uml/                    # UML 设计图
├── build.gradle            # 根项目 Gradle 配置
├── settings.gradle         # Gradle 模块配置
├── gradle.properties       # Gradle 属性配置
├── gradlew                 # Windows/Linux/macOS Gradle 启动脚本
├── gradlew.bat             # Windows Gradle 启动脚本
└── README.md               # 项目说明
```

## 环境要求

- Android Studio
- JDK 11 或更高版本
- Android SDK
- Android 模拟器或真机

## 构建 Android App

在项目根目录执行：

```powershell
.\gradlew :app:assembleDebug
```

Debug APK 输出路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 运行全球排行榜服务器

新开一个终端，在项目根目录执行：

```powershell
.\gradlew :leaderboardServer:run
```

默认端口：

```text
8080
```

App 中的全球排行榜功能需要该服务器保持运行。

## 运行双人联机服务器

再新开一个终端，在项目根目录执行：

```powershell
.\gradlew :battleServer:run
```

默认端口：

```text
9999
```

App 中的双人联机对战功能需要该服务器保持运行。

## 模拟器网络配置

Android 模拟器访问宿主机电脑时，默认使用：

```text
10.0.2.2
```

如果你使用 `adb reverse`，需要给每个模拟器转发排行榜和联机端口：

```powershell
adb -s <device-id> reverse tcp:8080 tcp:8080
adb -s <device-id> reverse tcp:9999 tcp:9999
```

然后将 `app/src/main/java/edu/hitsz/network/NetworkConfig.java` 中的服务器地址改为：

```java
SERVER_HOST = "127.0.0.1";
```

如果不使用 `adb reverse`，通常保持：

```java
SERVER_HOST = "10.0.2.2";
```

## 使用说明

### 单人模式

1. 启动 App。
2. 在主菜单选择简单、普通或困难难度。
3. 游戏结束后输入昵称。
4. 分数会保存到本地排行榜，并尝试上传到全球排行榜服务器。

### 全球排行榜

1. 先启动 `leaderboardServer`。
2. 确认模拟器能访问 `8080` 端口。
3. 在 App 主菜单点击“全球排行榜”。

### 双人联机对战

1. 先启动 `battleServer`。
2. 两个模拟器都安装并打开 App。
3. 玩家一点击“双人联机对战”并创建房间。
4. 玩家二输入玩家一的房间号并加入。
5. 双方就绪后进入对战。

> 注意：联机测试时，建议先把 App 安装到两个模拟器，再开始创建/加入房间。不要在玩家一已经创建房间后再点击 Android Studio 的 Run，否则可能导致 App 被重启、Socket 断开。

## 常用命令

构建全部核心模块：

```powershell
.\gradlew :app:assembleDebug :leaderboardServer:build :battleServer:build
```

运行单元测试：

```powershell
.\gradlew :app:testDebugUnitTest
```

查看已连接设备：

```powershell
adb devices
```

## Git 上传说明

仓库只保留最小可运行项目文件、UML 设计图和本 README。以下内容不会上传：

- Android Studio 本地配置：`.idea/`、`*.iml`
- Gradle/Android 构建产物：`.gradle/`、`**/build/`
- 本地环境配置：`local.properties`、`.env`
- 运行时数据：`ranking/`、`leaderboardServer/leaderboard_scores.json`
- 旧桌面版源码和输出：`src/`、`test/`、`out/`
- 除 `README.md` 以外的 Markdown 说明文档
