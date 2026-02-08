# KDrain

Desktop application built with Compose Multiplatform that demonstrates how to interact with [KBridge](https://github.com/ArtLoz/Kbridge) API. It provides a plugin system that lets you write bot automation scripts in Kotlin, package them as JARs, and load them at runtime.

## Project Structure

```
KDrain/
├── composeApp/          Main desktop application (Compose Multiplatform, JVM)
├── KDrainPluginApi/     Plugin interface that all plugins must implement
├── kutils/              Shared utilities: extension functions, GPS navigation, locations
├── scrydePathEoPlug/    Example plugin with a full automation script
└── app/
    └── plugins/         Directory where compiled plugin JARs are placed
```

### composeApp

The main application module. Provides the UI for bot management and plugin control.

**Stack:** Compose Multiplatform 1.10, Kotlin 2.3, Koin 4.1 (DI), Coroutines 1.10, KBridge API 1.0.4

**Key components:**
- **MainViewModel** — MVVM state management: bot selection, connection, plugin lifecycle
- **PluginManager** — discovers and loads plugin JARs at runtime, runs/stops the active plugin
- **LogController** — collects and displays bot events (actions, packets, errors) in real time

### KDrainPluginApi

Minimal interface that every plugin must implement:

```kotlin
interface KDrainPlugin {
    suspend fun onEnable(bot: L2Bot)
}
```

The `bot` instance is provided by the application when the plugin is launched. Through it the plugin has full access to the KBridge API.

### kutils

Shared utility library available to all plugins via `compileOnly` dependency.

| File | Description |
|------|-------------|
| `Utils.kt` | Extension functions for `L2Bot`, `L2User`, inventory, buffs, dialogs, navigation |
| `ResourceHelper.kt` | Extracts embedded resources (DB, zone/config files) from JARs to disk |
| `location/` | Game world data: `TownLocation`, `NpcInfo`, `LocationPoint` for known villages and NPCs |
| `test/GPSH.kt` | GPS navigation helper using embedded SQLite database |

### scrydePathEoPlug

A complete example plugin that implements a multi-stage quest automation script. Demonstrates:
- Implementing `KDrainPlugin`
- Using `kutils` extension functions for bot control
- Bundling zone/config files as JAR resources
- Extracting resources at runtime via `ResourceHelper.extractPluginResource`

## Getting Started

### Prerequisites

- JDK 17+
- Gradle 8+ (wrapper included)

### Build & Run

```bash
# Build the entire project
./gradlew build

# Run the desktop application
./gradlew :composeApp:run

# Build the example plugin (JAR is copied to app/plugins/ automatically)
./gradlew :scrydePathEoPlug:jar
```

## Creating a Plugin

### 1. Create a new Gradle module

```kotlin
// my-plugin/build.gradle.kts
plugins {
    id("java-library")
    kotlin("jvm") version "2.3.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(project(":KDrainPluginApi"))
    compileOnly(project(":kutils"))
}

// Auto-copy JAR to plugins directory after build
tasks.jar {
    doLast {
        copy {
            from(archiveFile)
            into("${rootProject.projectDir}/app/plugins/")
        }
    }
}
```

Add the module to `settings.gradle.kts`:

```kotlin
include(":my-plugin")
```

### 2. Implement the plugin interface

```kotlin
class MyPlugin : KDrainPlugin {
    override suspend fun onEnable(bot: L2Bot) {
        // Your script logic here
        val user = bot.user()
        println("Hello from plugin! Character: ${user.name}, Level: ${user.level}")
    }
}
```

### 3. Use kutils helpers

```kotlin
override suspend fun onEnable(bot: L2Bot) {
    // Navigate to NPC via GPS
    bot.moveByKGpsToNpc(KamaelVillage.npcGatekeeper)

    // Target and interact
    bot.targetAndConfirm(KamaelVillage.npcGatekeeper.id)
    bot.openDialogAndConfirm()
    bot.selectedDialogByIndex(1)

    // Check character state
    if (bot.user().isBuffEnding(4328)) {
        // rebuff logic
    }

    // Use items
    bot.useItemAndDelay(10650, 35_000)
}
```

### 4. Bundle resource files

Place zone maps (`.zmap`) and configs (`.xml`) in `src/main/resources/`:

```
my-plugin/src/main/resources/
  files/
    MY_ZONE.zmap
    MY_CONFIG.xml
```

Extract them at runtime:

```kotlin
private val loader = MyPlugin::class.java.classLoader

override suspend fun onEnable(bot: L2Bot) {
    val zone = ResourceHelper.extractPluginResource(loader, "files/MY_ZONE.zmap", "my-plugin")
    val config = ResourceHelper.extractPluginResource(loader, "files/MY_CONFIG.xml", "my-plugin")

    bot.loadZone(zone)
    bot.loadConfig(config)
    bot.setFaceControl(0, true)
}
```

Extracted files are cached in `app/plugins_data/{pluginName}/`.

### 5. Build and use

```bash
./gradlew :my-plugin:jar
```

The JAR is automatically copied to `app/plugins/`. Restart the app or click the refresh button in the Plugins panel to pick it up.

## Plugin Runtime

- Only **one plugin** can run at a time
- Plugins are discovered by scanning `app/plugins/` for JAR files
- Classes implementing `KDrainPlugin` are found automatically (no configuration needed)
- The plugin receives the currently selected and connected `L2Bot` instance
- Stopping a plugin cancels its coroutine

## Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 2.3.0 |
| Compose Multiplatform | 1.10.0 |
| Koin | 4.1.1 |
| Kotlinx Coroutines | 1.10.2 |
| KBridge API | 1.0.4 |
| JVM Target | 17 |

## License

This project is provided as a reference implementation for working with the KBridge API.
