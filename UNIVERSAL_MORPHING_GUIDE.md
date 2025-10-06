# Universal Entity Morphing System - Testing Guide

## Overview

MorphLib now supports morphing **ALL LivingEntity types**, not just Players. This includes villagers, zombies, custom entities, and more.

## Quick Start Examples

### Example 1: Morph a Player with Custom Proportions

```java
// Create a female character with custom body
MorphState state = new MorphState();
state.setHeight(1.1f);           // 10% taller
state.setChestScale(1.3f);       // 30% larger chest
state.setChestSpacing(0.2f);     // Slightly wider spacing
state.setHipWidth(1.2f);         // 20% wider hips
state.setShoulderWidth(0.9f);    // 10% narrower shoulders

MorphData morphData = MorphData.create(
    EntityType.PLAYER,
    MorphData.Gender.FEMALE,
    MorphData.BodyType.CURVY,
    null, // no custom skin
    state
);

MorphManager.applyMorph(player, morphData);
```

### Example 2: Morph a Villager (Custom Colonist)

```java
// Create a male colonist with athletic build
MorphState state = new MorphState();
state.setHeight(1.0f);           // Normal height
state.setChestScale(0.0f);       // Flat chest (male)
state.setShoulderWidth(1.2f);    // 20% wider shoulders
state.setBodyWidth(1.1f);        // 10% wider body

MorphData morphData = MorphData.create(
    EntityType.VILLAGER,
    MorphData.Gender.MALE,
    MorphData.BodyType.ATHLETIC,
    null,
    state
);

MorphManager.applyMorph(villagerEntity, morphData);
```

### Example 3: Homestead Integration

```java
// In your ColonistEntity class
public void setAppearance(Gender gender, BodyType bodyType) {
    MorphState state = new MorphState();

    // Configure based on gender
    if (gender == Gender.FEMALE) {
        state.setChestScale(1.2f);
        state.setHipWidth(1.15f);
        state.setShoulderWidth(0.95f);
    } else {
        state.setChestScale(0.0f);
        state.setHipWidth(1.0f);
        state.setShoulderWidth(1.1f);
    }

    // Configure based on body type
    switch (bodyType) {
        case CURVY:
            state.setChestScale(state.getChestScale() * 1.2f);
            state.setHipWidth(state.getHipWidth() * 1.15f);
            state.setBodyWidth(1.1f);
            break;
        case ATHLETIC:
            state.setShoulderWidth(state.getShoulderWidth() * 1.1f);
            state.setBodyWidth(1.05f);
            state.setArmLength(1.05f);
            break;
        case SLIM:
            state.setBodyWidth(0.9f);
            state.setArmLength(0.95f);
            state.setLegLength(1.05f);
            break;
    }

    MorphData morphData = MorphData.create(
        EntityType.VILLAGER,
        gender == Gender.FEMALE ? MorphData.Gender.FEMALE : MorphData.Gender.MALE,
        bodyType == BodyType.CURVY ? MorphData.BodyType.NORMAL : MorphData.BodyType.SLIM,
        null,
        state
    );

    MorphManager.applyMorph(this, morphData);
}
```

## Testing Checklist

### ✅ Test 1: Player Morphing

1. Start game in creative mode
2. Run command: `/morph minecraft:player`
3. Verify player renders normally
4. Apply custom MorphState with chest geometry
5. **Expected**: Female players show visible breast geometry, males show flat chest

### ✅ Test 2: Villager Morphing

1. Spawn a villager: `/summon minecraft:villager ~ ~ ~`
2. Apply morph with custom proportions
3. **Expected**: Villager model scales correctly (height, width, limbs)
4. **Expected**: Chest geometry is added if female gender

### ✅ Test 3: Zombie Morphing

1. Spawn a zombie: `/summon minecraft:zombie ~ ~ ~`
2. Apply morph with various body types
3. **Expected**: Zombie model transforms based on MorphState
4. **Expected**: Animations still work (walking, attacking)

### ✅ Test 4: Custom Entity (ColonistEntity)

1. Spawn a colonist from Homestead mod
2. Set appearance via colonist's API
3. **Expected**: Colonist shows custom body proportions
4. **Expected**: Female colonists have visible breasts
5. **Expected**: Different body types look visually distinct

### ✅ Test 5: Network Synchronization

1. Start a multiplayer server
2. Apply morph to entity on server
3. Join as client
4. **Expected**: Client sees morphed entity correctly
5. **Expected**: Geometry and scaling syncs properly

### ✅ Test 6: Animation Compatibility

1. Apply morph to walking villager
2. **Expected**: Walk animation still plays
3. **Expected**: Chest geometry animates with idle bounce/sway
4. **Expected**: No visual glitches or Z-fighting

## Debug Commands

```java
// Check if entity has morph
boolean hasMorph = MorphManager.hasMorph(entity);

// Get current morph data
MorphData morphData = MorphManager.getEffectiveMorph(entity);

// Get morph state
MorphState state = morphData.getMorphState();

// Check specific values
float chestScale = state.getChestScale();
float height = state.getHeight();
```

## Common Issues & Solutions

### Issue: Chest geometry not visible

- **Check**: Is gender set to FEMALE?
- **Check**: Is chestScale > 0.0?
- **Solution**: Ensure `morphData.getGender() == Gender.FEMALE` and `morphState.getChestScale() > 0.0`

### Issue: Model parts not scaling

- **Check**: Is entity a LivingEntity?
- **Check**: Does model have the expected parts?
- **Solution**: Use `ModelPartMapper.getAllParts(model)` to debug available parts

### Issue: Morph not syncing to clients

- **Check**: Is morph applied on server side?
- **Check**: Are network packets registered?
- **Solution**: Ensure `MorphManager.applyMorph()` is called on server, packets will auto-sync

### Issue: Z-fighting or visual artifacts

- **Check**: Are multiple morphs applied?
- **Check**: Are scale values extreme (>2.0 or <0.5)?
- **Solution**: Keep scales reasonable (0.8-1.5 range for best results)

## Performance Considerations

- **Model Part Discovery**: Cached after first lookup per model type
- **Geometry Injection**: Only happens once per entity, then updated
- **Network Sync**: Only sends when morph changes, not every tick
- **Rendering**: Minimal overhead, only transforms existing model parts

## API Reference

### MorphManager

- `applyMorph(Entity, MorphData)` - Apply morph to any entity
- `removeMorph(Entity)` - Remove morph and reset geometry
- `getEffectiveMorph(Entity)` - Get current morph data
- `hasMorph(Entity)` - Check if entity has morph

### MorphData

- `create(EntityType, Gender, BodyType, ResourceLocation, MorphState)` - Create morph data
- `getMorphState()` - Get transformation parameters
- `setMorphState(MorphState)` - Update transformation parameters

### MorphState

- `setHeight(float)` - Overall height (0.5-2.0 recommended)
- `setBodyWidth(float)` - Body width (0.8-1.5 recommended)
- `setChestScale(float)` - Chest size (0.0-2.0, 0.0 for male)
- `setChestSpacing(float)` - Chest separation (0.0-1.0)
- `setHipWidth(float)` - Hip width (0.8-1.5 recommended)
- `setShoulderWidth(float)` - Shoulder width (0.8-1.5 recommended)
- `setArmLength(float)` - Arm length (0.8-1.2 recommended)
- `setLegLength(float)` - Leg length (0.8-1.2 recommended)
- `setHeadSize(float)` - Head size (0.8-1.2 recommended)

## Success Metrics

Your implementation is successful when:

✅ Female colonists have visible breast geometry  
✅ Male colonists have flat chests  
✅ Different body types (CURVY, ATHLETIC, SLIM) are visually distinct  
✅ Hip and shoulder width variations are noticeable  
✅ Height scaling works correctly  
✅ System works on vanilla mobs (zombies, villagers)  
✅ Morphs sync correctly in multiplayer  
✅ Animations continue to work normally  
✅ No performance degradation  
✅ No visual artifacts or Z-fighting

## Next Steps

1. **Build the mod**: `./gradlew build`
2. **Test with vanilla entities** first (player, villager, zombie)
3. **Integrate with Homestead** colonist system
4. **Fine-tune** chest geometry dimensions and positioning
5. **Optimize** performance if needed
6. **Report issues** with specific entity types or models
