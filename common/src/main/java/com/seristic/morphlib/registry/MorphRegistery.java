package com.seristic.morphlib.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.seristic.morphlib.logging.ModLogger;
import com.seristic.morphlib.MorphData;

import net.minecraft.world.entity.EntityType;

/**
 * Registry for managing available morphs and active morph instances.
 * Tracks morphs by their unique IDs and provides lookup by entity type.
 */
public class MorphRegistery {
    private static final MorphRegistery INSTANCE = new MorphRegistery();

    private final Map<UUID, MorphData> availableMorphs = new HashMap<>();

    private final Map<EntityType<?>, List<MorphData>> morphsByEntityType = new ConcurrentHashMap<>();

    private final Map<UUID, UUID> activeMorphs = new ConcurrentHashMap<>();

    private MorphRegistery() {
    }

    public static MorphRegistery getInstance() {
        return INSTANCE;
    }

    /**
     * Register a new morph, making it available for use.
     *
     * @param morphData The morph data to register.
     */
    public void registerMorph(MorphData morphData) {
        UUID morphId = morphData.getMorphId();
        availableMorphs.put(morphId, morphData);
        morphsByEntityType.computeIfAbsent(morphData.getEntityType(), k -> new ArrayList<>()).add(morphData);
        ModLogger.info("Registered new morph: " + morphId + " for entity type: " + morphData.getEntityType());
    }

    /**
     * Unregister a morph, removing it from availability.
     * 
     * @param morphId The unique ID of the morph to unregister.
     */
    public void unregisterMorph(UUID morphId) {
        MorphData morphData = availableMorphs.remove(morphId);
        if (morphData != null) {
            List<MorphData> morphs = morphsByEntityType.get(morphData.getEntityType());
            if (morphs != null) {
                morphs.removeIf(m -> m.getMorphId().equals(morphId));
            }
            ModLogger.info("Unregistered morph: " + morphId);
        }
    }

    /**
     * Gets all available morphs
     * 
     * @return A collection of all registered MorphData instances.
     */
    public Collection<MorphData> getAvailableMorphs() {
        return new ArrayList<>(availableMorphs.values());
    }

    /**
     * Gets a morph by its unique ID.
     * 
     * @param morphId The unique ID of the morph.
     * @return The MorphData if found, null otherwise.
     */
    public MorphData getMorphById(UUID morphId) {
        return availableMorphs.get(morphId);
    }

    /**
     * Gets all morphs for a specific entity type.
     * 
     * @param entityType The entity type to look up.
     * @return A list of MorphData instances for the given entity type.
     */
    public List<MorphData> getMorphsForEntityType(EntityType<?> entityType) {
        return new ArrayList<>(morphsByEntityType.getOrDefault(entityType, Collections.emptyList()));
    }

    /**
     * Sets the active morph for an entity.
     * 
     * @param entityId The unique ID of the entity.
     * @param morphId  The unique ID of the morph to set as active.
     */
    public void setActiveMorph(UUID playerId, UUID morphId) {
        if (availableMorphs.containsKey(morphId)) {
            activeMorphs.put(playerId, morphId);
            ModLogger.debug("Set active morph: " + morphId + " for player: " + playerId, "MorphRegistery");
        } else {
            ModLogger.warn("Attempted to set unknown morph as active: " + morphId, "MorphRegistery");
        }
    }

    /**
     * Gets the active morph ID for a player.
     * 
     * @param playerId The unique ID of the player.
     * @return The active morph ID, or null if none is set.
     */
    public UUID getActiveMorphId(UUID playerId) {
        return activeMorphs.get(playerId);
    }

    /**
     * Gets the active morph data for a player.
     * 
     * @param playerId The unique ID of the player.
     * @return The active MorphData, or null if none is set.
     */
    public MorphData getActiveMorph(UUID playerId) {
        UUID morphId = activeMorphs.get(playerId);
        return morphId != null ? availableMorphs.get(morphId) : null;
    }

    /**
     * Clears the active morph for a player.
     * 
     * @param playerId The unique ID of the player.
     * @return The previous active morph ID, or null if none was set.
     */
    public void clearActiveMorph(UUID playerId) {
        UUID removed = activeMorphs.remove(playerId);
        if (removed != null) {
            ModLogger.debug("Cleared active morph: " + removed + " for player: " + playerId, "MorphRegistery");
        }
    }

    /**
     * Checks if a morph is available.
     * 
     * @param morphId The unique ID of the morph.
     * @return True if the morph is available, false otherwise.
     */
    public boolean isMorphAvailable(UUID morphId) {
        return availableMorphs.containsKey(morphId);
    }

    /**
     * Gets all entity types that have registered morphs.
     * 
     * @return A set of entity types with registered morphs.
     */
    public Set<EntityType<?>> getRegisteredEntityTypes() {
        return new HashSet<>(morphsByEntityType.keySet());
    }

    /**
     * Clears all registered morphs (for cleanup/reload)
     * 
     * @deprecated Use with caution - primarily for testing or reloading scenarios.
     */
    @Deprecated
    public void clearAll() {
        availableMorphs.clear();
        morphsByEntityType.clear();
        activeMorphs.clear();
        ModLogger.info("Cleared all registered morphs and active morphs", "MorphRegistery");
    }
}
