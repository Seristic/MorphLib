package com.seristic.morphlib.events;

import com.seristic.morphlib.MorphData;
import net.minecraft.world.entity.player.Player;

/**
 * Event fired after a player successfully morphs into a new form.
 */
public class PostMorphEvent {
    private final Player player;
    private final MorphData fromMorph;
    private final MorphData toMorph;

    public PostMorphEvent(Player player, MorphData fromMorph, MorphData toMorph) {
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
}