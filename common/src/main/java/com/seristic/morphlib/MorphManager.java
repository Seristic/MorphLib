package com.seristic.morphlib;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;

import com.seristic.logging.ModLogger;

/**
 * Manages morph states for all entities.
 * Uses WeakHashMap to prevent memory leaks when entities are removed.
 */
public class MorphManager {
    private static final Map<Entity, MorphData> morphMap = new WeakHashMap<>();

    /**
     * Apply a morph to an entity.
     * This only sets the morph data locally - networking is handled by the
     * platform-specific code.
     */
    public static void applyMorph(Entity entity, MorphData data) {
        ModLogger.info("MorphManager", "Applying morph to entity: " + entity.getStringUUID() + " with data: " + data);
        morphMap.put(entity, data);
    }

    /**
     * Get current morph data for an entity.
     * Returns null if no morph is applied.
     */
    public static MorphData getMorph(Entity entity) {
        return morphMap.get(entity);
    }

    /**
     * Check if an entity has a morph applied.
     */
    public static boolean hasMorph(Entity entity) {
        return morphMap.containsKey(entity);
    }

    /**
     * Remove morph data for an entity.
     */
    public static void removeMorph(Entity entity) {
        MorphData removed = morphMap.remove(entity);
        if (removed != null) {
            ModLogger.info("MorphManager", "Removed morph from entity: " + entity.getStringUUID());
        }
    }

    /**
     * Clear all morph data. Used for cleanup.
     */
    public static void clear() {
        morphMap.clear();
        ModLogger.info("MorphManager", "Cleared all morph data");
    }

    /**
     * Get the number of entities with morphs applied.
     */
    public static int getMorphCount() {
        return morphMap.size();
    }
}
