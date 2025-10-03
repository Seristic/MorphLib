package com.seristic.morphlib;

import com.seristic.morphlib.logging.ModLogger;
import net.minecraft.world.entity.Entity;

/**
 * Manages morph states for all entities.
 * On NeoForge, morphs are stored using data attachments.
 * On Fabric, morphs are stored using components (to be implemented).
 * 
 * This class provides a platform-agnostic API that delegates to
 * platform-specific implementations.
 * 
 * Supports both simple morphs (MorphData) and complex layered morphs
 * (MorphStack).
 */
public class MorphManager {

    private static MorphAccessor accessor;
    private static MorphStackAccessor stackAccessor;

    /**
     * Interface for platform-specific morph storage.
     */
    public interface MorphAccessor {
        MorphData getMorph(Entity entity);

        void setMorph(Entity entity, MorphData data);

        void removeMorph(Entity entity);
    }

    /**
     * Interface for platform-specific morph stack storage.
     */
    public interface MorphStackAccessor {
        MorphStack getMorphStack(Entity entity);

        void setMorphStack(Entity entity, MorphStack stack);

        void removeMorphStack(Entity entity);
    }

    /**
     * Initialize the MorphManager with a platform-specific accessor.
     * Called during mod initialization.
     */
    public static void setAccessor(MorphAccessor accessor) {
        MorphManager.accessor = accessor;
        ModLogger.info("MorphManager", "Morph accessor initialized");
    }

    /**
     * Initialize the MorphManager with a platform-specific stack accessor.
     * Called during mod initialization.
     */
    public static void setStackAccessor(MorphStackAccessor stackAccessor) {
        MorphManager.stackAccessor = stackAccessor;
        ModLogger.info("MorphManager", "Morph stack accessor initialized");
    }

    // ===== Simple Morph API (existing) =====

    /**
     * Apply a morph to an entity.
     * This sets the morph data and should trigger sync to clients.
     */
    public static void applyMorph(Entity entity, MorphData data) {
        if (accessor == null) {
            ModLogger.error("MorphManager", "MorphAccessor not initialized!");
            return;
        }
        ModLogger.info("MorphManager", "Applying morph to entity: " + entity.getStringUUID() + " with data: " + data);
        accessor.setMorph(entity, data);
    }

    /**
     * Get current morph data for an entity.
     * Returns null if no morph is applied.
     */
    public static MorphData getMorph(Entity entity) {
        if (accessor == null) {
            ModLogger.error("MorphManager", "MorphAccessor not initialized!");
            return null;
        }
        return accessor.getMorph(entity);
    }

    /**
     * Check if an entity has a morph applied.
     */
    public static boolean hasMorph(Entity entity) {
        MorphData simpleMorph = getMorph(entity);
        if (simpleMorph != null) {
            return true;
        }

        // Also check for stacked morphs
        MorphStack stack = getMorphStack(entity);
        return stack != null && stack.hasMorph();
    }

    /**
     * Remove morph data for an entity.
     */
    public static void removeMorph(Entity entity) {
        if (accessor == null) {
            ModLogger.error("MorphManager", "MorphAccessor not initialized!");
            return;
        }
        MorphData removed = accessor.getMorph(entity);
        if (removed != null) {
            ModLogger.info("MorphManager", "Removed morph from entity: " + entity.getStringUUID());
            accessor.removeMorph(entity);
        }
    }

    // ===== Morph Stack API (new) =====

    /**
     * Get the morph stack for an entity.
     * Returns null if no stack exists.
     */
    public static MorphStack getMorphStack(Entity entity) {
        if (stackAccessor == null) {
            return null; // Stack system not available
        }
        return stackAccessor.getMorphStack(entity);
    }

    /**
     * Set the entire morph stack for an entity.
     */
    public static void setMorphStack(Entity entity, MorphStack stack) {
        if (stackAccessor == null) {
            ModLogger.warn("MorphManager", "MorphStackAccessor not initialized!");
            return;
        }
        ModLogger.info("MorphManager", "Setting morph stack for entity: " + entity.getStringUUID() + " with "
                + stack.getLayers().size() + " layers");
        stackAccessor.setMorphStack(entity, stack);
    }

    /**
     * Add a morph layer to an entity's stack.
     * Creates a new stack if none exists.
     */
    public static void addMorphLayer(Entity entity, String layerId, int priority, MorphData morphData) {
        MorphStack stack = getMorphStack(entity);
        if (stack == null) {
            stack = new MorphStack();
        }

        stack.addLayer(layerId, priority, morphData);
        setMorphStack(entity, stack);

        ModLogger.info("MorphManager",
                "Added morph layer '" + layerId + "' (priority " + priority + ") to entity: " + entity.getStringUUID());
    }

    /**
     * Remove a specific morph layer from an entity's stack.
     */
    public static void removeMorphLayer(Entity entity, String layerId) {
        MorphStack stack = getMorphStack(entity);
        if (stack == null) {
            return; // No stack to remove from
        }

        stack.removeLayer(layerId);

        if (stack.isEmpty()) {
            removeMorphStack(entity);
        } else {
            setMorphStack(entity, stack);
        }

        ModLogger.info("MorphManager", "Removed morph layer '" + layerId + "' from entity: " + entity.getStringUUID());
    }

    /**
     * Remove the entire morph stack from an entity.
     */
    public static void removeMorphStack(Entity entity) {
        if (stackAccessor == null) {
            return;
        }

        MorphStack removed = stackAccessor.getMorphStack(entity);
        if (removed != null) {
            ModLogger.info("MorphManager", "Removed morph stack from entity: " + entity.getStringUUID());
            stackAccessor.removeMorphStack(entity);
        }
    }

    /**
     * Get the effective morph data for an entity, combining simple and stacked
     * morphs.
     * If both exist, the stacked morph takes precedence.
     */
    public static MorphData getEffectiveMorph(Entity entity) {
        // Check for stacked morph first (higher priority)
        MorphStack stack = getMorphStack(entity);
        if (stack != null && stack.hasMorph()) {
            return stack.combine();
        }

        // Fall back to simple morph
        return getMorph(entity);
    }

    /**
     * Clear all morphs (simple and stacked) from an entity.
     */
    public static void clearAllMorphs(Entity entity) {
        removeMorph(entity);
        removeMorphStack(entity);
        ModLogger.info("MorphManager", "Cleared all morphs from entity: " + entity.getStringUUID());
    }
}
