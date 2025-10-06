# MorphLib Universal Morphing - Quick Reference

## Installation

Add to your `build.gradle`:

```gradle
repositories {
    // Add MorphLib repository here
}

dependencies {
    implementation "com.seristic:morphlib:0.1"
}
```

## Basic Usage

### 1. Simple Morph (Using Existing MorphData)

```java
import com.seristic.morphlib.*;
import com.seristic.morphlib.morph.MorphState;

// Create morph state with custom proportions
MorphState state = new MorphState();
state.setHeight(1.2f);        // 20% taller
state.setChestScale(1.3f);    // Female chest
state.setHipWidth(1.15f);     // Wider hips

// Create morph data
MorphData morphData = MorphData.create(
    EntityType.PLAYER,               // Any entity type
    MorphData.Gender.FEMALE,         // MALE or FEMALE
    MorphData.BodyType.NORMAL,       // NORMAL or SLIM
    null,                            // Custom skin (optional)
    state                            // Morph state
);

// Apply to entity
MorphManager.applyMorph(entity, morphData);
```

### 2. Gender-Specific Morphs

```java
// Female (with chest geometry)
MorphState femaleState = new MorphState();
femaleState.setChestScale(1.2f);       // Visible breasts
femaleState.setChestSpacing(0.15f);    // Natural spacing
femaleState.setHipWidth(1.15f);        // Wider hips
femaleState.setShoulderWidth(0.95f);   // Narrower shoulders

// Male (flat chest)
MorphState maleState = new MorphState();
maleState.setChestScale(0.0f);         // No visible chest
maleState.setHipWidth(1.0f);           // Normal hips
maleState.setShoulderWidth(1.1f);      // Wider shoulders
```

### 3. Body Type Presets

```java
public MorphState createBodyType(BodyType type, boolean isFemale) {
    MorphState state = new MorphState();

    switch (type) {
        case CURVY:
            state.setChestScale(isFemale ? 1.4f : 0.0f);
            state.setHipWidth(1.25f);
            state.setBodyWidth(1.15f);
            state.setShoulderWidth(0.95f);
            break;

        case ATHLETIC:
            state.setChestScale(isFemale ? 1.1f : 0.0f);
            state.setHipWidth(1.05f);
            state.setShoulderWidth(1.15f);
            state.setArmLength(1.05f);
            state.setLegLength(1.05f);
            break;

        case SLIM:
            state.setChestScale(isFemale ? 1.0f : 0.0f);
            state.setBodyWidth(0.9f);
            state.setHipWidth(0.95f);
            state.setLegLength(1.05f);
            break;
    }

    return state;
}
```

### 4. Height Variations

```java
// Short colonist
state.setHeight(0.85f);      // 15% shorter
state.setLegLength(0.9f);    // Shorter legs

// Tall colonist
state.setHeight(1.15f);      // 15% taller
state.setLegLength(1.1f);    // Longer legs
```

### 5. Checking Morph Status

```java
// Check if entity has morph
if (MorphManager.hasMorph(entity)) {
    MorphData data = MorphManager.getEffectiveMorph(entity);
    MorphState state = data.getMorphState();

    float height = state.getHeight();
    float chestScale = state.getChestScale();

    // Use the data...
}
```

### 6. Removing Morphs

```java
// Remove morph from entity
MorphManager.removeMorph(entity);

// Or clear all morphs (including stacks)
MorphManager.clearAllMorphs(entity);
```

## MorphState Parameters Reference

| Parameter       | Range   | Description      | Use Case                      |
| --------------- | ------- | ---------------- | ----------------------------- |
| `height`        | 0.5-2.0 | Overall height   | Tall/short characters         |
| `bodyWidth`     | 0.8-1.5 | Body width       | Thin/heavy builds             |
| `chestScale`    | 0.0-2.0 | Chest size       | Female breasts (0.0 for male) |
| `chestSpacing`  | 0.0-1.0 | Chest separation | Natural vs. wide              |
| `hipWidth`      | 0.8-1.5 | Hip width        | Female vs. male hips          |
| `shoulderWidth` | 0.8-1.5 | Shoulder width   | Broad vs. narrow              |
| `armLength`     | 0.8-1.2 | Arm length       | Long/short arms               |
| `legLength`     | 0.8-1.2 | Leg length       | Proportions                   |
| `headSize`      | 0.8-1.2 | Head size        | Stylized looks                |
| `neckLength`    | 0.8-1.2 | Neck length      | Giraffe neck?                 |
| `idleBounce`    | 0.0-1.0 | Idle animation   | Chest bounce                  |
| `walkSway`      | 0.0-1.0 | Walk animation   | Movement sway                 |

## Entity Type Compatibility

| Entity Type                   | Supported | Chest Geometry | Notes        |
| ----------------------------- | --------- | -------------- | ------------ |
| Player                        | ✅        | ✅             | Full support |
| Villager                      | ✅        | ✅             | Full support |
| Zombie                        | ✅        | ✅             | Full support |
| Skeleton                      | ✅        | ✅             | Full support |
| Custom (extends LivingEntity) | ✅        | ✅             | Full support |
| Animals                       | ⚠️        | ❌             | Scaling only |

## Network Synchronization

Morphs automatically sync in multiplayer:

```java
// On server
MorphManager.applyMorph(entity, morphData);
// → Automatically syncs to all tracking clients

// On client (automatic)
// → Receives morph data
// → Applies transformations
// → Renders correctly
```

## Performance Tips

1. **Reuse MorphState objects** when possible
2. **Cache body type presets** instead of recreating
3. **Avoid extreme values** (keep scales 0.5-2.0)
4. **Use morphState.getContentHash()** to detect changes

## Common Patterns

### Pattern 1: Random Colonist Generation

```java
public void generateRandomColonist(ColonistEntity entity) {
    Random rand = new Random();

    MorphState state = new MorphState();
    Gender gender = rand.nextBoolean() ? Gender.FEMALE : Gender.MALE;

    // Random height
    state.setHeight(0.9f + rand.nextFloat() * 0.3f); // 0.9-1.2

    // Gender-specific
    if (gender == Gender.FEMALE) {
        state.setChestScale(1.0f + rand.nextFloat() * 0.5f);
        state.setHipWidth(1.1f + rand.nextFloat() * 0.2f);
    } else {
        state.setChestScale(0.0f);
        state.setShoulderWidth(1.05f + rand.nextFloat() * 0.2f);
    }

    MorphData morphData = MorphData.create(
        EntityType.VILLAGER,
        gender,
        MorphData.BodyType.NORMAL,
        null,
        state
    );

    MorphManager.applyMorph(entity, morphData);
}
```

### Pattern 2: Morph Interpolation

```java
public void morphOver Time(Entity entity, MorphState targetState, int durationTicks) {
    MorphData current = MorphManager.getEffectiveMorph(entity);
    MorphState startState = current != null ? current.getMorphState() : new MorphState();

    // Animate over time
    new AnimationTask() {
        int tick = 0;

        @Override
        public void run() {
            float progress = (float) tick / durationTicks;
            MorphState interpolated = MorphState.lerp(startState, targetState, progress);

            current.setMorphState(interpolated);
            MorphManager.applyMorph(entity, current);

            if (++tick >= durationTicks) {
                cancel();
            }
        }
    }.runTaskTimer(plugin, 0, 1);
}
```

### Pattern 3: Body Type Selector

```java
public enum ColonistBodyType {
    SLIM(0.9f, 1.0f, 0.95f),
    NORMAL(1.0f, 1.15f, 1.0f),
    ATHLETIC(1.05f, 1.1f, 1.15f),
    CURVY(1.1f, 1.25f, 1.0f);

    private final float bodyWidth;
    private final float hipWidth;
    private final float shoulderWidth;

    ColonistBodyType(float bodyWidth, float hipWidth, float shoulderWidth) {
        this.bodyWidth = bodyWidth;
        this.hipWidth = hipWidth;
        this.shoulderWidth = shoulderWidth;
    }

    public MorphState createState(boolean isFemale) {
        MorphState state = new MorphState();
        state.setBodyWidth(bodyWidth);
        state.setHipWidth(hipWidth);
        state.setShoulderWidth(shoulderWidth);
        state.setChestScale(isFemale ? 1.2f : 0.0f);
        return state;
    }
}
```

## Debug Utilities

```java
// Log all model parts (debugging)
Map<String, ModelPart> parts = ModelPartMapper.getAllParts(model);
parts.forEach((name, part) -> {
    System.out.println("Part: " + name + " at " + part.x + "," + part.y + "," + part.z);
});

// Check if morph is rendering
if (UniversalMorphRenderHandler.hasActiveMorph(entity)) {
    Optional<MorphState> state = UniversalMorphRenderHandler.getMorphState(entity);
    state.ifPresent(s -> {
        System.out.println("Active morph: " + s);
    });
}
```

## Troubleshooting

| Problem                    | Solution                                         |
| -------------------------- | ------------------------------------------------ |
| Chest not visible          | Check: `gender == FEMALE` && `chestScale > 0.0`  |
| Model not scaling          | Verify entity is `LivingEntity`                  |
| Not syncing in multiplayer | Call `MorphManager.applyMorph()` on server       |
| Visual glitches            | Keep scale values in 0.5-2.0 range               |
| Performance issues         | Cache MorphState objects, avoid per-tick updates |

## License

MorphLib is licensed under the SOLACE License (Software Of Liberty And Community Equity).
See LICENSE file for details.

---

**Happy Morphing!** 🎨
