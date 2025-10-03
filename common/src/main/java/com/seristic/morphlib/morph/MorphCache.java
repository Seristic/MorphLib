package com.seristic.morphlib.morph;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.seristic.morphlib.logging.ModLogger;

/**
 * Client-side cache for morph states with interpolation support.
 * Manages entity-specific morph data and smooth transitions.
 */
public class MorphCache {
    private static final MorphCache INSTANCE = new MorphCache();

    // Entity ID -> Current cached morph state
    private final Map<UUID, CachedMorphState> cachedStates = new ConcurrentHashMap<>();

    // Transition duration in ticks (1 second = 20 ticks)
    private static final int TRANSITION_DURATION = 20;

    private MorphCache() {
    }

    public static MorphCache getInstance() {
        return INSTANCE;
    }

    /**
     * Update the target morph state for an entity, triggering interpolation.
     */
    public void updateMorphState(UUID entityId, MorphState newState) {
        CachedMorphState cached = cachedStates.computeIfAbsent(entityId,
                k -> new CachedMorphState(new MorphState(), new MorphState()));

        // Only start transition if the state actually changed
        if (cached.targetState.hasChanged(newState)) {
            cached.startTransition(newState);
            ModLogger.debug("Started morph transition for entity: " + entityId);
        }
    }

    /**
     * Get the interpolated morph state for rendering.
     * 
     * @param entityId     The entity to get morph state for
     * @param partialTicks Partial tick progress for smooth interpolation
     * @return Interpolated morph state
     */
    public MorphState getInterpolatedState(UUID entityId, float partialTicks) {
        CachedMorphState cached = cachedStates.get(entityId);
        if (cached == null) {
            return new MorphState(); // Default morph state
        }

        return cached.getInterpolated(partialTicks);
    }

    /**
     * Update interpolation progress (called each client tick).
     */
    public void tick() {
        cachedStates.values().forEach(CachedMorphState::tick);
    }

    /**
     * Remove cached state for an entity (when entity is unloaded).
     */
    public void removeEntity(UUID entityId) {
        cachedStates.remove(entityId);
        ModLogger.debug("Removed morph cache for entity: " + entityId);
    }

    /**
     * Clear all cached states (world unload, etc.).
     */
    public void clearAll() {
        cachedStates.clear();
        ModLogger.info("Cleared all morph cache data");
    }

    /**
     * Inner class to manage individual entity morph state transitions.
     */
    private static class CachedMorphState {
        private MorphState currentState;
        private MorphState targetState;
        private MorphState previousState;

        private boolean transitioning = false;
        private int transitionTicks = 0;

        public CachedMorphState(MorphState initial, MorphState target) {
            this.currentState = new MorphState(initial);
            this.targetState = new MorphState(target);
            this.previousState = new MorphState(initial);
        }

        public void startTransition(MorphState newTarget) {
            // Save current interpolated state as starting point
            this.previousState = new MorphState(currentState);
            this.targetState = new MorphState(newTarget);
            this.transitioning = true;
            this.transitionTicks = 0;
        }

        public void tick() {
            if (transitioning) {
                transitionTicks++;

                if (transitionTicks >= TRANSITION_DURATION) {
                    // Transition complete
                    currentState = new MorphState(targetState);
                    transitioning = false;
                    transitionTicks = 0;
                }
            }
        }

        public MorphState getInterpolated(float partialTicks) {
            if (!transitioning) {
                return currentState;
            }

            // Calculate interpolation progress
            float totalTicks = transitionTicks + partialTicks;
            float progress = Math.min(totalTicks / TRANSITION_DURATION, 1.0f);

            // Apply easing function for smoother transitions
            progress = easeInOutCubic(progress);

            // Interpolate between previous and target states
            return MorphState.lerp(previousState, targetState, progress);
        }

        /**
         * Smooth easing function for natural-feeling transitions.
         */
        private float easeInOutCubic(float t) {
            return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
        }
    }
}