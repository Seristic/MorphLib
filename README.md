# MorphLib

[![License: Solace](https://img.shields.io/badge/License-Solace-blue.svg)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-orange)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.4.0--rc1-red)](https://neoforge.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.107.0%2B1.21.4-blue)](https://fabricmc.net/)

**MorphLib** is a library mod for Minecraft that provides a robust data structure and API for managing body proportions, entity morphs, and character customization. It **does not** provide visual rendering or player model changes on its own—it's designed to be used by other mods that implement their own rendering systems.

## ⚠️ Important: This is a Library

MorphLib **only provides data storage and management**. It does not:

- ❌ Change player appearance visually
- ❌ Include player model modifications
- ❌ Provide `/morph` commands
- ❌ Work standalone without other mods

MorphLib is meant to be used as a dependency by other mods that handle the visual/rendering aspects.

## ✨ Features

### Core Data Management

- **MorphState System**: Comprehensive data structure for body proportions (chest, hips, shoulders, limbs, height, etc.)
- **MorphData Storage**: Entity type, gender, body type, and custom skin tracking
- **Client-Server Synchronization**: Automatic network packet handling for multiplayer
- **Persistent Storage**: Save and load morph data with proper serialization

### Developer-Friendly API

- **Platform Agnostic**: Works with both NeoForge and Fabric
- **Clean Architecture**: Separation between data management and rendering (rendering not included)
- **Extensible Design**: Easy to integrate with custom rendering systems
- **Comprehensive Logging**: Detailed debug information for troubleshooting

### What MorphLib Provides

- ✅ **Data Structures**: `MorphState`, `MorphData`, `Gender`, `BodyType` enums
- ✅ **Cache Management**: Thread-safe caching and interpolation of morph states
- ✅ **Networking**: Automatic synchronization between client and server
- ✅ **API Access**: Simple methods to create, modify, and query morph data

### What MorphLib Does NOT Provide

- ❌ **Visual Rendering**: No player model changes or visual transformations
- ❌ **Player Commands**: No built-in `/morph` command (implement in your mod)
- ❌ **Custom Models**: No model replacement or texture modification
- ❌ **Standalone Functionality**: Must be used by other mods to have visible effects

## 🚀 Quick Start

### For Players

MorphLib is a **library mod** and does nothing on its own. You need to install mods that depend on MorphLib to see any functionality. Common examples might include character customization mods, transformation mods, or appearance modification mods.

1. Install MorphLib from [GitHub Releases](https://github.com/Seristic/MorphLib/releases)
2. Install a mod that uses MorphLib (check the mod's documentation)
3. Follow that mod's instructions for using its features

### For Developers

MorphLib provides the data layer for managing body proportions and morph states. You implement the rendering and visual changes in your own mod.

#### Basic Usage

```java
// Create and apply morph data
MorphData morphData = MorphData.create(
    EntityType.COW,
    Gender.MALE,
    BodyType.NORMAL,
    null // custom skin (optional)
);
MorphManager.applyMorph(player, morphData);

// The data is now synchronized across client/server
// YOU must implement rendering changes in your mod
```

#### Working with MorphState

```java
// Create custom body proportions
MorphState customState = new MorphState();
customState.setChestScale(1.3f);    // 30% larger chest
customState.setHipWidth(1.2f);      // 20% wider hips
customState.setHeight(1.1f);        // 10% taller
customState.setShoulderWidth(1.1f); // 10% wider shoulders

// Update the cache
MorphCache.getInstance().updateMorphState(player.getUUID(), customState);

// In your rendering code, query the data:
MorphState state = MorphCache.getInstance()
    .getInterpolatedState(player.getUUID(), partialTick);
// Use state.getChestScale(), state.getHeight(), etc. to modify your rendering
```

#### Integration Pattern for Optional Dependency

```java
// Optional integration - your mod works with or without MorphLib
public class YourModIntegration {
    public static void setBodyProportions(Player player, float chestScale, float height) {
        if (!ModList.get().isLoaded("morphlib")) {
            // Handle without MorphLib
            return;
        }

        // MorphLib is present - use it for data storage
        MorphState state = MorphCache.getInstance()
            .getInterpolatedState(player.getUUID(), 0.0f);
        state.setChestScale(chestScale);
        state.setHeight(height);
        MorphCache.getInstance().updateMorphState(player.getUUID(), state);

        // YOUR rendering code uses this data to modify the player model
    }
}
```

## 📦 Installation

### Prerequisites

- **Minecraft**: 1.21.4
- **NeoForge**: 21.4.0-rc1+ (recommended)
- **Fabric**: 0.107.0+1.21.4 (alternative)

### Player Installation

1. Download MorphLib from [GitHub Releases](https://github.com/Seristic/MorphLib/releases)
2. Place the JAR file in your `.minecraft/mods` folder
3. Install a mod that uses MorphLib (MorphLib alone does nothing visible)
4. Launch Minecraft with your chosen mod loader

### Developer Dependency

Add MorphLib as a dependency in your build.gradle:

```gradle
repositories {
    maven {
        name = "MorphLib"
        url = "https://maven.example.com/morphlib" // Replace with actual maven
    }
}

dependencies {
    // For NeoForge
    implementation "com.seristic:morphlib-neoforge:${morphlib_version}"

    // For Fabric
    modImplementation "com.seristic:morphlib-fabric:${morphlib_version}"
}
```

### Development Setup

```bash
# Clone the repository
git clone https://github.com/Seristic/MorphLib.git
cd MorphLib

# Build for both platforms
./gradlew build

# Run test environment with NeoForge
./gradlew :neoforge:runClient

# Run test environment with Fabric
./gradlew :fabric:runClient
```

## 🛠️ API Reference

### Available Data Fields

MorphLib tracks the following body proportion data:

- **Basic Scaling**: `height`, `width`, `depth`
- **Body Parts**: `chestScale`, `chestSpacing`, `hipWidth`, `shoulderWidth`, `waistWidth`
- **Limbs**: `armLength`, `armWidth`, `legLength`, `legWidth`
- **Head**: `headScale`, `neckLength`, `neckWidth`
- **Other**: `bustSize`, `bodyFat`

### MorphData

```java
// Create morph data with entity type
MorphData data = MorphData.create(
    EntityType.COW,      // Entity type
    Gender.FEMALE,       // Gender enum
    BodyType.ATHLETIC,   // BodyType enum
    skinLocation         // ResourceLocation (optional)
);

// Access fields
EntityType<?> type = data.getEntityType();
Gender gender = data.getGender();
BodyType bodyType = data.getBodyType();
```

### MorphState

```java
// Create and modify morph state
MorphState state = new MorphState();

// Set proportions
state.setHeight(1.2f);
state.setChestScale(1.3f);
state.setHipWidth(1.15f);
state.setShoulderWidth(1.1f);

// Get proportions
float height = state.getHeight();
float chest = state.getChestScale();

// Update cache
MorphCache.getInstance().updateMorphState(playerUUID, state);
```

### MorphManager

```java
// Apply morph to player
MorphManager.applyMorph(player, morphData);

// Clear morph
MorphManager.clearMorph(player);

// Check if player has morph
boolean hasMorph = MorphManager.hasMorph(player);
```

### MorphCache

```java
// Get current state (no interpolation)
MorphState state = MorphCache.getInstance()
    .getInterpolatedState(playerUUID, 0.0f);

// Get interpolated state (for smooth rendering)
MorphState smoothState = MorphCache.getInstance()
    .getInterpolatedState(playerUUID, partialTick);

// Direct update
MorphCache.getInstance().updateMorphState(playerUUID, newState);
```

## 🏗️ Architecture

### Core Components

- **MorphManager**: Central API for applying and managing morph data
- **MorphData**: Entity type, gender, body type, and skin data
- **MorphState**: Detailed body proportions (chest, hips, shoulders, limbs, height, etc.)
- **MorphCache**: Thread-safe caching and interpolation of morph states
- **Networking**: Automatic client-server synchronization packets

### Platform Support

- **Common**: Shared code between platforms (data structures, API, cache)
- **NeoForge**: NeoForge-specific networking and events
- **Fabric**: Fabric-specific networking and events

### Data Flow

1. **Mod calls MorphLib API**: `MorphManager.applyMorph(player, morphData)`
2. **MorphLib stores data**: Updates `MorphCache` with new `MorphState`
3. **Network sync**: Automatically sends packets to client/server
4. **Mod queries data**: Calls `MorphCache.getInstance().getInterpolatedState(uuid, partialTick)`
5. **Mod implements rendering**: Uses the data to modify player model/rendering

> **Note**: MorphLib stops at step 4. Step 5 (visual changes) must be implemented by your mod.

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details on:

- Code style and conventions
- Development setup
- Testing procedures
- Pull request process

## 📄 License

**Copyright (c) 2025 Seristic**

MorphLib is licensed under the **SOLACE License** (Software Of Liberty And Community Equity). This license promotes:

- ✨ **Open and Accessible Software**: Free to use, modify, and distribute
- 🏳️‍🌈 **Inclusive Values**: Celebrates diversity, equity, and inclusion, particularly supporting the LGBT community
- 🚫 **Non-Commercial Distribution**: Software itself cannot be sold, but services around it can be charged
- 🤝 **Community Equity**: Preserving inclusive content and maintaining ethical software practices

### Key License Terms

- **Grant of License**: Worldwide, royalty-free, non-exclusive, perpetual license to use, reproduce, distribute, and create derivative works
- **Preservation of Inclusive Content**: Any LGBT-supporting features, references, or content must be maintained in their original intent and visibility
- **Non-Commercial Clause**: The software cannot be sold directly, but you may charge for services (support, hosting, etc.)
- **Attribution Required**: Copyright notice and license must be included in all copies

**Please read the full [LICENSE](LICENSE) before using or contributing to this project.**

## 🔗 Links

- [GitHub Repository](https://github.com/Seristic/MorphLib)
- [Issue Tracker](https://github.com/Seristic/MorphLib/issues)
- [Releases](https://github.com/Seristic/MorphLib/releases)
- [Contributing Guidelines](CONTRIBUTING.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)

## 💡 Example Use Cases

MorphLib is designed to support mods that need to track body proportions, such as:

- **Character customization mods**: Store player's custom body proportions
- **Transformation mods**: Track entity morph data and proportions
- **RPG mods**: Manage character stats that affect appearance
- **Cosmetic mods**: Store visual customization preferences

Remember: MorphLib provides the **data layer only**. Your mod must handle the **rendering layer** to make changes visible in-game.

## 🙏 Acknowledgments

- Built with [Architectury](https://github.com/architectury/architectury-api) for multi-platform support
- Compatible with [NeoForge](https://neoforge.net/) and [Fabric](https://fabricmc.net/)
- Inspired by the need for standardized body proportion data in Minecraft modding
