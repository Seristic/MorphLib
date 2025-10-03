package com.seristic.morphlib.events;

import com.seristic.morphlib.MorphData;
import net.minecraft.world.entity.player.Player;

/**
 * Event fired before a player morphs into a new form.
 * Can be cancelled to prevent the morph from occurring.
 */
public class PreMorphEvent {
    private final Player player;
    private final MorphData fromMorph;
    private final MorphData toMorph;
    private boolean cancelled = false;
    private String cancelReason = null;

    public PreMorphEvent(Player player, MorphData fromMorph, MorphData toMorph) {
        this.player = player;
        this.fromMorph = fromMorph;
        this.toMorph = toMorph;
    }

    public Player getPlayer() {
        return player;
    }

    public MorphData getFromMorph() {
        return fromMorph;
    }

    public MorphData getToMorph() {
        return toMorph;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setCancelled(boolean cancelled, String reason) {
        this.cancelled = cancelled;
        this.cancelReason = reason;
    }

    public String getCancelReason() {
        return cancelReason;
    }
}