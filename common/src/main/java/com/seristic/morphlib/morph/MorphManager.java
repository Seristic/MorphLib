package com.seristic.morphlib.morph;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.seristic.morphlib.logging.ModLogger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/**
 * Server-side manager for morph states and synchronization.
 * Handles persistence, change detection, and network sync.
 */
public class MorphManager {
    private static final MorphManager INSTANCE = new MorphManager();

    // Entity ID -> Current morph state
    private final Map<UUID, MorphState> entityMorphStates = new ConcurrentHashMap<>();

    // Entity ID -> Last synced hash (for change detection)
    private final Map<UUID, Integer> lastSyncedHashes = new ConcurrentHashMap<>();

    private MorphManager() {
    }

    public static MorphManager getInstance() {
        return INSTANCE;
    }

    /**
     * Set the morph state for an entity.
     * Automatically handles change detection and network sync.
     * 
     * @param entity     The entity to set morph state for
     * @param morphState The new morph state
     */
    public void setMorphState(Entity entity, MorphState morphState) {
        UUID entityId = entity.getUUID();
        MorphState oldState = entityMorphStates.get(entityId);

        // Check if state actually changed
        if (oldState != null && !morphState.hasChanged(oldState)) {
            return; // No change, skip update
        }

        // Update stored state
        entityMorphStates.put(entityId, new MorphState(morphState));

        // Sync to clients if this is a player or visible entity
        syncToClients(entity, morphState);

        ModLogger.debug("Updated morph state for entity: " + entityId);
    }

    /**
     * Get the current morph state for an entity.
     * 
     * @param entityId The entity ID
     * @return The morph state, or default state if none set
     */
    public MorphState getMorphState(UUID entityId) {
        return entityMorphStates.getOrDefault(entityId, new MorphState());
    }

    /**
     * Remove morph state for an entity (when entity is removed).
     * 
     * @param entityId The entity ID to remove
     */
    public void removeEntity(UUID entityId) {
        entityMorphStates.remove(entityId);
        lastSyncedHashes.remove(entityId);
        ModLogger.debug("Removed morph state for entity: " + entityId);
    }

    /**
     * Update a specific morph parameter for an entity.
     * 
     * @param entityId  The entity ID
     * @param parameter The parameter name
     * @param value     The new value
     */
    public void updateParameter(UUID entityId, String parameter, float value) {
        MorphState currentState = getMorphState(entityId);
        currentState.setParameter(parameter, value);

        // Find the entity and update
        // TODO: This would need platform-specific entity lookup
        ModLogger.debug("Updated parameter " + parameter + " to " + value + " for entity " + entityId);
    }

    /**
     * Save morph state to NBT for entity persistence.
     * 
     * @param entity The entity
     * @return NBT compound with morph data
     */
    public CompoundTag saveMorphData(Entity entity) {
        MorphState morphState = entityMorphStates.get(entity.getUUID());
        if (morphState != null) {
            CompoundTag morphData = new CompoundTag();
            morphData.put("morphState", morphState.writeNBT());
            return morphData;
        }
        return new CompoundTag();
    }

    /**
     * Load morph state from NBT for entity persistence.
     * 
     * @param entity The entity
     * @param nbt    The NBT data
     */
    public void loadMorphData(Entity entity, CompoundTag nbt) {
        if (nbt.contains("morphState")) {
            MorphState morphState = MorphState.readNBT(nbt.getCompound("morphState"));
            entityMorphStates.put(entity.getUUID(), morphState);
            ModLogger.debug("Loaded morph state for entity: " + entity.getUUID());
        }
    }

    /**
     * Sync morph state to all relevant clients.
     * 
     * @param entity     The entity whose morph state changed
     * @param morphState The new morph state
     */
    private void syncToClients(Entity entity, MorphState morphState) {
        UUID entityId = entity.getUUID();
        int newHash = morphState.getContentHash();
        Integer lastHash = lastSyncedHashes.get(entityId);

        // Only sync if hash actually changed
        if (lastHash != null && lastHash == newHash) {
            return;
        }

        lastSyncedHashes.put(entityId, newHash);

        // TODO: Send S2CMorphStatePacket to relevant clients
        // This would be implemented in platform-specific networking code
        ModLogger.debug("Syncing morph state to clients for entity: " + entityId);
    }

    /**
     * Tick method for server-side processing.
     * Called each server tick to handle gradual morph changes, aging, etc.
     */
    public void tick() {
        // TODO: Handle gradual morphing, aging effects, etc.
        // This is where you could implement slow morph changes over time

        // Example implementation for gradual aging:
        // for (Map.Entry<UUID, MorphState> entry : entityMorphStates.entrySet()) {
        // UUID entityId = entry.getKey();
        // MorphState morphState = entry.getValue();
        //
        // if (shouldAge(entityId)) {
        // applyAgingEffect(morphState);
        // syncToClients(findEntity(entityId), morphState);
        // }
        // }
    }

    /**
     * Generate a random morph state for procedural generation.
     * 
     * @param seed Random seed for consistent generation
     * @return Randomly generated morph state
     */
    public MorphState generateRandomMorph(long seed) {
        // Use seed for consistent random generation
        java.util.Random random = new java.util.Random(seed);

        MorphState morphState = new MorphState();

        // Generate random but realistic proportions
        morphState.setHeight(0.85f + random.nextFloat() * 0.3f); // 0.85 to 1.15
        morphState.setBodyWidth(0.8f + random.nextFloat() * 0.4f); // 0.8 to 1.2
        morphState.setArmLength(0.9f + random.nextFloat() * 0.2f); // 0.9 to 1.1
        morphState.setLegLength(0.9f + random.nextFloat() * 0.2f); // 0.9 to 1.1

        // More varied chest proportions
        morphState.setChestScale(0.5f + random.nextFloat() * 1.5f); // 0.5 to 2.0
        morphState.setChestSpacing(-0.2f + random.nextFloat() * 0.4f); // -0.2 to 0.2

        // Hip and shoulder variation
        morphState.setHipWidth(0.8f + random.nextFloat() * 0.4f); // 0.8 to 1.2
        morphState.setShoulderWidth(0.9f + random.nextFloat() * 0.2f); // 0.9 to 1.1

        // Head and neck variation
        morphState.setHeadSize(0.9f + random.nextFloat() * 0.2f); // 0.9 to 1.1
        morphState.setNeckLength(0.9f + random.nextFloat() * 0.3f); // 0.9 to 1.2

        // Subtle animation preferences
        morphState.setIdleBounce(random.nextFloat() * 0.05f); // 0 to 0.05
        morphState.setWalkSway(0.8f + random.nextFloat() * 0.4f); // 0.8 to 1.2

        return morphState;
    }

    /**
     * Apply genetic inheritance for child entity morphs.
     * 
     * @param parent1State First parent's morph state
     * @param parent2State Second parent's morph state
     * @param variation    Amount of random variation (0.0 to 1.0)
     * @return Child morph state inheriting from both parents
     */
    public MorphState inheritMorph(MorphState parent1State, MorphState parent2State, float variation) {
        java.util.Random random = new java.util.Random();

        MorphState childState = new MorphState();

        // Average parent traits with some random variation
        childState.setHeight(average(parent1State.getHeight(), parent2State.getHeight(), variation, random));
        childState.setBodyWidth(average(parent1State.getBodyWidth(), parent2State.getBodyWidth(), variation, random));
        childState.setArmLength(average(parent1State.getArmLength(), parent2State.getArmLength(), variation, random));
        childState.setLegLength(average(parent1State.getLegLength(), parent2State.getLegLength(), variation, random));
        childState
                .setChestScale(average(parent1State.getChestScale(), parent2State.getChestScale(), variation, random));
        childState.setChestSpacing(
                average(parent1State.getChestSpacing(), parent2State.getChestSpacing(), variation, random));
        childState.setHipWidth(average(parent1State.getHipWidth(), parent2State.getHipWidth(), variation, random));
        childState.setShoulderWidth(
                average(parent1State.getShoulderWidth(), parent2State.getShoulderWidth(), variation, random));
        childState.setHeadSize(average(parent1State.getHeadSize(), parent2State.getHeadSize(), variation, random));
        childState
                .setNeckLength(average(parent1State.getNeckLength(), parent2State.getNeckLength(), variation, random));

        return childState;
    }

    /**
     * Helper method for genetic averaging with variation.
     */
    private float average(float val1, float val2, float variation, java.util.Random random) {
        float average = (val1 + val2) / 2.0f;
        float variationAmount = variation * 0.2f * (random.nextFloat() - 0.5f); // ±10% variation
        return Math.max(0.1f, average + variationAmount); // Ensure positive values
    }
}