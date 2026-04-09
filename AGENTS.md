# AGENTS.md - AircraftWar Project Guidelines

## Project Overview
This is an Android Java game project (Aircraft War). Build system uses Gradle with Android Gradle Plugin 9.1.0, targeting Java 11.

---

## Build Commands

### Build
```bash
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build
```

### Run Tests
```bash
./gradlew test             # Run unit tests
./gradlew testDebugUnitTest   # Run debug unit tests
./gradlew connectedAndroidTest  # Run instrumented tests (requires emulator/device)
```

### Run Single Test
```bash
./gradlew test --tests="edu.hitsz.ExampleUnitTest.addition_isCorrect"
./gradlew testDebugUnitTest --tests="edu.hitsz.ExampleUnitTest"
```

### Lint
```bash
./gradlew lint             # Run lint analysis
./gradlew lintDebug        # Run debug lint
```

### Clean & Rebuild
```bash
./gradlew clean
./gradlew build
```

### Debug APK Location
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Code Style Guidelines

### Package Structure
```
edu.hitsz/
├── aircraft/      # Aircraft classes (AbstractAircraft, HeroAircraft, enemies)
├── application/   # Android activities, views, settings
├── audio/         # Audio management (AudioManager)
├── basic/         # Base classes (AbstractFlyingObject)
├── bullet/        # Bullet classes
├── dao/           # Data access objects (score storage)
├── difficulty/    # Difficulty templates
├── factory/       # Factory classes
├── observer/      # Observer pattern implementations
├── prop/          # Prop/power-up classes
└── strategy/      # Fire strategies
```

### Naming Conventions
- **Classes**: PascalCase (e.g., `GameView`, `AbstractAircraft`)
- **Methods**: camelCase (e.g., `shootAction()`, `crashCheckAction()`)
- **Variables**: camelCase (e.g., `heroAircraft`, `enemyBullets`)
- **Constants**: `final` with camelCase (e.g., `bossThreshold`, `timeInterval`) - matches existing codebase
- **Abstract classes**: Prefix with `Abstract` (e.g., `AbstractAircraft`, `AbstractFlyingObject`)
- **Interfaces**: Descriptive names (e.g., `FireStrategy`, `EnemyFactory`, `PropFactory`)

### Imports
- Prefer explicit imports; wildcard imports (`import edu.hitsz.aircraft.*`) are acceptable in GameView for brevity
- Group imports: standard library first, then Android, then project-specific
- Sort alphabetically within groups

### Formatting
- Indent: 4 spaces (no tabs)
- Line length: Prefer under 120 chars
- Curly braces: Same-line opening brace (K&R style)
- Use blank lines between methods and logical sections
- Javadoc comments on public/protected methods (Chinese acceptable per existing codebase)

### Types
- Use interface types for parameters (e.g., `List<BaseBullet>` rather than `LinkedList<BaseBullet>`)
- Primitive types (`int`, `double`) for performance-critical numeric operations (coordinates, speeds, HP)

### Error Handling
- Use try-catch for recoverable errors (e.g., `surfaceCreated`, `surfaceDestroyed`, game loop sleep)
- Catch specific exceptions (e.g., `InterruptedException`), not broad `Exception`
- Log errors with `e.printStackTrace()` for debugging

### Design Patterns Used
- **Factory Pattern**: `EnemyFactory`, `PropFactory` and concrete implementations
- **Observer Pattern**: `GameObserver`, `BombClearObserver`
- **Strategy Pattern**: `FireStrategy` (DirectFireStrategy, RingFireStrategy, ScatterFireStrategy)
- **Singleton**: `HeroAircraft.getInstance()`, `AudioManager.getInstance(context)`
- **Template Method**: `GameDifficultyTemplate` (EasyDifficulty, MediumDifficulty, HardDifficulty)

### Java Version
- Target Java 11 (source and target compatibility)
- Min SDK: 24, Target SDK: 36, Compile SDK: 36

---

## Testing Guidelines

### Unit Tests
- Location: `app/src/test/java/edu/hitsz/`
- Framework: JUnit 4
- Run on development machine (JVM)

### Instrumented Tests
- Location: `app/src/androidTest/java/edu/hitsz/`
- Framework: AndroidJUnit4 + Espresso
- Run on Android device/emulator

---

## Key Files

### Game Logic
- `GameView.java` - Main game loop, rendering, collision detection
- `AbstractAircraft.java` - Base aircraft class with HP management
- `AbstractFlyingObject.java` - Base class for all flying objects

### Configuration
- `app/build.gradle` - Module build configuration
- `gradle/libs.versions.toml` - Version catalog (AGP 9.1.0, JUnit 4.13.2)
- `AppSettings.java` - Global app settings (screen size)

### Data
- `FileScoreDao.java` - Score persistence
- `GameConfig.java` - Game configuration storage

---

## Common Issues

### Null Checks
Always check for null when using `ImageManager.get()`:
```java
Bitmap img = ImageManager.get(obj.getClass().getName());
if (img != null && !obj.notValid()) {
    // Draw
}
```

### Canvas Locking
Always unlock canvas in finally block:
```java
canvas = holder.lockCanvas();
if (canvas == null) return;
try {
    // Draw
} finally {
    holder.unlockCanvasAndPost(canvas);
}
```

### Thread Safety
Game thread handles rendering; ensure proper synchronization if adding multi-threading
