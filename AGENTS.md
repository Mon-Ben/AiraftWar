# AGENTS.md - AircraftWar Development Guide

This file provides guidance for agentic coding agents working on this codebase.

## Project Overview

- **Language**: Java 11/17
- **Build System**: IntelliJ IDEA (no Maven/Gradle)
- **UI Framework**: Swing
- **Testing**: JUnit 5
- **External Dependencies**: commons-lang3-3.8.1.jar (in `lib/`)

## Project Structure

```
src/edu/hitsz/
  aircraft/           # Aircraft classes (HeroAircraft, enemies)
  application/        # Main app classes (Game, Main, ImageManager)
  basic/              # Base classes (AbstractFlyingObject)
  bullet/             # Bullet classes
  dao/                # Data access (FileScoreDao, ScoreItem)
  difficulty/         # Difficulty templates
  factory/            # Factory pattern implementations
  observer/           # Observer pattern
  prop/               # Prop items
  strategy/           # Strategy pattern (fire strategies)
  ui/                 # Swing UI components
test/                 # JUnit test files (mirrors src/ structure)
```

---

## Build, Run & Test Commands

### Running the Application

```bash
# Open in IntelliJ IDEA and run Main class
# Or compile and run from command line:
javac -d out -sourcepath src -cp "lib/*" src/edu/hitsz/application/Main.java
java -cp "out:lib/*" edu.hitsz.application.Main
```

### Running Tests

```bash
# Run all tests: Right-click test/ folder > Run All Tests (IntelliJ)
# Run single test class: Right-click on test class > Run
# Run single test method: Right-click on test method > Run
```

### Test Framework Details

- **Framework**: JUnit 5.8.1
- **Annotations**: `@Test`, `@BeforeEach`, `@ParameterizedTest`, `@CsvSource`, `@DisplayName`
- **Assertions**: `org.junit.jupiter.api.Assertions`

### Compiling

```bash
# Compile all sources
javac -d out -sourcepath src -cp "lib/*" $(find src -name "*.java")

# Compile with test sources
javac -d out -sourcepath src:test -cp "lib/*:out" $(find src test -name "*.java")
```

---

## Code Style Guidelines

### Naming Conventions

- **Classes**: PascalCase (e.g., `HeroAircraft`, `AbstractAircraft`)
- **Methods**: camelCase (e.g., `forward()`, `shoot()`, `decreaseHp()`)
- **Variables**: camelCase (e.g., `heroAircraft`, `enemyBullets`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `WINDOW_WIDTH`, `WINDOW_HEIGHT`)
- **Packages**: lowercase (e.g., `edu.hitsz.aircraft`)
- **Test classes**: `<ClassName>Test` (e.g., `HeroAircraftTest`)

### Import Order

1. External Java packages (`java.util.*`, `javax.swing.*`)
2. External third-party libraries (`org.apache.commons.lang3.*`)
3. Internal project packages (`edu.hitsz.*`)

### Formatting

- **Indentation**: 4 spaces
- **Line length**: Under 120 characters preferred
- **Curly braces**: Same-line opening brace

### JavaDoc and Comments

- Use Chinese comments throughout (matching existing style)
- Use Javadoc for public API methods
- Include `@author hitsz` in class-level Javadoc

### Type Usage

- Use primitives (`int`, `double`, `boolean`) over wrappers where possible
- Use interfaces for variable types (`List<>`, `Map<>`), concrete for instantiation

### Error Handling

- Use try-catch blocks with simple `e.printStackTrace()` pattern
- Use `assert` for invariants in development

### Design Patterns Used

1. **Singleton**: `HeroAircraft.getInstance()`
2. **Factory**: `EnemyFactory`, `PropFactory`
3. **Strategy**: `FireStrategy` implementations
4. **Observer**: `BombClearObserver`
5. **Template Method**: `GameDifficultyTemplate`

### Testing Guidelines

- Place tests in `test/` mirroring `src/` package structure
- Test class naming: `<ClassName>Test`
- Use `@BeforeEach` to reset state between tests
- For singleton classes, reset static instance in `@BeforeEach`:
```java
@BeforeEach
void setUp() {
    HeroAircraft.instance = null;
    hero = HeroAircraft.getInstance(256, 700, 0, 0, 100);
}
```

### Accessibility Modifiers

- **Fields**: `private` or `protected` by default
- **Methods**: `public` for API, `private` for internal
- Use `protected` for methods intended to be overridden

### Code Smells to Avoid

- Avoid hardcoding magic numbers; use named constants
- Avoid `System.out.println()` in production
- Avoid catching generic `Exception` or `Throwable`
- Avoid mutable static state where possible

---

## Common Development Tasks

### Adding a New Aircraft Type

1. Create class in `src/edu/hitsz/aircraft/` extending `AbstractAircraft`
2. Create factory in `src/edu/hitsz/factory/` implementing `EnemyFactory`
3. Add factory to `Game.java`

### Adding a New Prop

1. Create class in `src/edu/hitsz/prop/` extending `AbstractProp`
2. Create factory in `src/edu/hitsz/factory/`
3. Add to prop factories array in `Game.java`

---

## Notes

- Swing-based game application
- Uses game loop with `ScheduledExecutorService`
- Score persistence via `FileScoreDao` to `ranking/` directory
