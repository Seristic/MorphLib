package com.seristic.morphlib.services;

import java.util.List;
import java.util.UUID;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.events.PostMorphEvent;
import com.seristic.morphlib.events.PreMorphEvent;
import com.seristic.morphlib.logging.ModLogger;
import com.seristic.morphlib.registry.MorphRegistery;
import com.seristic.morphlib.restrictions.MorphRestrictionManager;

import net.minecraft.world.entity.player.Player;

/**
 * Central service for managing morph operations, integrating restrictions,
 * events, and data management.
 */
public class MorphService {
    private static final MorphService INSTANCE = new MorphService();

    private final MorphRegistery registry = MorphRegistery.getInstance();
    private final MorphRestrictionManager restrictions = MorphRestrictionManager.getInstance();

    private MorphService() {
    }

    public static MorphService getInstance() {
        return INSTANCE;
    }

    /**
     * Attempt to morph a player into the specified form.
     * 
     * @param player        The player to morph
     * @param targetMorphId The UUID of the morph to transform into
     * @return true if the morph was successful, false otherwise
     */
    public boolean morphPlayer(Player player, UUID targetMorphId) {
        // Get current and target morph data
        MorphData currentMorph = registry.getActiveMorph(player.getUUID());
        MorphData targetMorph = registry.getMorphById(targetMorphId);

        if (targetMorph == null) {
            ModLogger.warn("Attempted to morph player " + player.getName().getString() + " into non-existent morph: "
                    + targetMorphId);
            return false;
        }

        // Check restrictions
        var restrictionResult = restrictions.canMorph(player, targetMorph);
        if (!restrictionResult.isAllowed()) {
            ModLogger.info(
                    "Morph blocked for player " + player.getName().getString() + ": " + restrictionResult.getReason());
            // TODO: Send message to player about restriction
            return false;
        }

        // Fire pre-morph event
        PreMorphEvent preEvent = new PreMorphEvent(player, currentMorph, targetMorph);
        // TODO: Fire event through platform-specific event bus

        if (preEvent.isCancelled()) {
            ModLogger.info("Morph cancelled by event for player " + player.getName().getString() +
                    (preEvent.getCancelReason() != null ? ": " + preEvent.getCancelReason() : ""));
            return false;
        }

        // Perform the morph
        registry.setActiveMorph(player.getUUID(), targetMorphId);
        restrictions.recordMorph(player);

        // Fire post-morph event
        PostMorphEvent postEvent = new PostMorphEvent(player, currentMorph, targetMorph);
        // TODO: Fire event through platform-specific event bus

        ModLogger.info("Player " + player.getName().getString() + " morphed from " +
                (currentMorph != null ? currentMorph.getEntityType() : "human") +
                " to " + targetMorph.getEntityType());

        return true;
    }

    /**
     * Revert a player back to their human form.
     */
    public boolean revertPlayer(Player player) {
        MorphData currentMorph = registry.getActiveMorph(player.getUUID());

        if (currentMorph == null) {
            ModLogger.debug("Player " + player.getName().getString() + " is already in human form");
            return true; // Already human
        }

        // Create human morph data (null represents human form)
        PreMorphEvent preEvent = new PreMorphEvent(player, currentMorph, null);
        // TODO: Fire event through platform-specific event bus

        if (preEvent.isCancelled()) {
            ModLogger.info("Morph reversion cancelled by event for player " + player.getName().getString());
            return false;
        }

        // Revert to human
        registry.clearActiveMorph(player.getUUID());

        PostMorphEvent postEvent = new PostMorphEvent(player, currentMorph, null);
        // TODO: Fire event through platform-specific event bus

        ModLogger.info("Player " + player.getName().getString() + " reverted from " + currentMorph.getEntityType()
                + " to human");

        return true;
    }

    /**
     * Get all available morphs for a player (considering unlocks, restrictions,
     * etc.).
     */
    public List<MorphData> getAvailableMorphs(Player player) {
        // TODO: Filter based on player's unlocked morphs, current restrictions, etc.
        return registry.getAvailableMorphs().stream().toList();
    }

    /**
     * Check if a player can currently morph (not considering specific target
     * morph).
     */
    public boolean canPlayerMorph(Player player) {
        long remainingCooldown = restrictions.getRemainingCooldown(player);
        return remainingCooldown <= 0;
    }

    /**
     * Get remaining cooldown time for a player in seconds.
     */
    public long getPlayerCooldownSeconds(Player player) {
        return restrictions.getRemainingCooldown(player) / 1000;
    }
}