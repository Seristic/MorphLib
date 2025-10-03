package com.seristic.morphlib.restrictions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.logging.ModLogger;

import net.minecraft.world.entity.player.Player;

/**
 * Manages morph restrictions including cooldowns, mana costs, and other
 * limitations.
 */
public class MorphRestrictionManager {
    private static final MorphRestrictionManager INSTANCE = new MorphRestrictionManager();

    // Player UUID -> Last morph time
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();

    // Default cooldown in milliseconds (30 seconds)
    private static final long DEFAULT_COOLDOWN = 30000;

    private MorphRestrictionManager() {
    }

    public static MorphRestrictionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Check if a player can morph into the specified form.
     * 
     * @param player    The player attempting to morph
     * @param morphData The morph they want to transform into
     * @return A result indicating success or the reason for failure
     */
    public MorphRestrictionResult canMorph(Player player, MorphData morphData) {
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();

        // Check cooldown
        if (playerCooldowns.containsKey(playerId)) {
            long lastMorphTime = playerCooldowns.get(playerId);
            long timeSinceLastMorph = currentTime - lastMorphTime;

            if (timeSinceLastMorph < DEFAULT_COOLDOWN) {
                long remainingCooldown = DEFAULT_COOLDOWN - timeSinceLastMorph;
                return MorphRestrictionResult
                        .failure("Morph on cooldown for " + (remainingCooldown / 1000) + " seconds");
            }
        }

        // Check mana/energy (placeholder for future implementation)
        if (!hasEnoughMana(player, morphData)) {
            return MorphRestrictionResult.failure("Not enough mana to morph");
        }

        // Check dimension restrictions (placeholder)
        if (!isDimensionAllowed(player, morphData)) {
            return MorphRestrictionResult.failure("Cannot morph in this dimension");
        }

        return MorphRestrictionResult.success();
    }

    /**
     * Record that a player has successfully morphed (for cooldown tracking).
     */
    public void recordMorph(Player player) {
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        playerCooldowns.put(playerId, currentTime);
        ModLogger.debug("Recorded morph cooldown for player: " + player.getName().getString());
    }

    /**
     * Clear cooldown for a player (admin command or special circumstances).
     */
    public void clearCooldown(Player player) {
        UUID playerId = player.getUUID();
        playerCooldowns.remove(playerId);
        ModLogger.info("Cleared morph cooldown for player: " + player.getName().getString());
    }

    /**
     * Get remaining cooldown time in milliseconds.
     */
    public long getRemainingCooldown(Player player) {
        UUID playerId = player.getUUID();
        if (!playerCooldowns.containsKey(playerId)) {
            return 0;
        }

        long lastMorphTime = playerCooldowns.get(playerId);
        long timeSinceLastMorph = System.currentTimeMillis() - lastMorphTime;
        long remaining = DEFAULT_COOLDOWN - timeSinceLastMorph;

        return Math.max(0, remaining);
    }

    // Placeholder methods for future features
    private boolean hasEnoughMana(Player player, MorphData morphData) {
        // TODO: Implement mana system integration
        return true;
    }

    private boolean isDimensionAllowed(Player player, MorphData morphData) {
        // TODO: Implement dimension-specific morph restrictions
        return true;
    }

    /**
     * Result class for morph restriction checks.
     */
    public static class MorphRestrictionResult {
        private final boolean allowed;
        private final String reason;

        private MorphRestrictionResult(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        public static MorphRestrictionResult success() {
            return new MorphRestrictionResult(true, null);
        }

        public static MorphRestrictionResult failure(String reason) {
            return new MorphRestrictionResult(false, reason);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }
    }
}