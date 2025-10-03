package com.seristic.morphlib.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * **NEW**
 * Stores player-specific morph history, including unlocks and favorites.
 * Persistent across sessions.
 */
public class PlayerMorphHistory {
    private final Set<UUID> unlockedMorphs = new HashSet<>();
    private final Set<UUID> favoriteMorphs = new HashSet<>();
    private final List<UUID> recentMorphs = new ArrayList<>();
    private static final int MAX_RECENT_MORPHS = 50;

    /**
     * Unlocks a morph for this player.
     */
    public void unlockMorph(UUID morphId) {
        unlockedMorphs.add(morphId);
    }

    /**
     * Locks a morph (removes unlock).
     */
    public void lockMorph(UUID morphId) {
        unlockedMorphs.remove(morphId);
        favoriteMorphs.remove(morphId);
    }

    /**
     * Checks if a morph is unlocked for this player.
     */
    public boolean isMorphUnlocked(UUID morphId) {
        return unlockedMorphs.contains(morphId);
    }

    /**
     * Gets all unlocked morph IDs.
     */
    public Set<UUID> getUnlockedMorphs() {
        return new HashSet<>(unlockedMorphs);
    }

    /**
     * Adds a morph to favorites (must be unlocked).
     */
    public void addToFavorites(UUID morphId) {
        if (unlockedMorphs.contains(morphId)) {
            favoriteMorphs.add(morphId);
        }
    }

    /**
     * Removes a morph from favorites.
     */
    public void removeFromFavorites(UUID morphId) {
        favoriteMorphs.remove(morphId);
    }

    /**
     * Checks if a morph is favorited.
     */
    public boolean isMorphFavorite(UUID morphId) {
        return favoriteMorphs.contains(morphId);
    }

    /**
     * Gets all favorite morph IDs.
     */
    public Set<UUID> getFavoriteMorphs() {
        return new HashSet<>(favoriteMorphs);
    }

    /**
     * Adds a morph to recent history (removes duplicates, adds to front).
     */
    public void addToRecentHistory(UUID morphId) {
        recentMorphs.remove(morphId);
        recentMorphs.add(0, morphId);

        if (recentMorphs.size() > MAX_RECENT_MORPHS) {
            recentMorphs.remove(recentMorphs.size() - 1);
        }
    }

    /**
     * Gets recent morph history (ordered, most recent first).
     */
    public List<UUID> getRecentMorphs() {
        return new ArrayList<>(recentMorphs);
    }

    /**
     * Clears all recent history.
     */
    public void clearRecentHistory() {
        recentMorphs.clear();
    }

    /**
     * Serialization for persistence (save to player data).
     */
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        ListTag unlockedList = new ListTag();
        for (UUID id : unlockedMorphs) {
            CompoundTag idTag = new CompoundTag();
            idTag.putUUID("id", id);
            unlockedList.add(idTag);
        }
        tag.put("unlockedMorphs", unlockedList);

        ListTag favoriteList = new ListTag();
        for (UUID id : favoriteMorphs) {
            CompoundTag idTag = new CompoundTag();
            idTag.putUUID("id", id);
            favoriteList.add(idTag);
        }
        tag.put("favoriteMorphs", favoriteList);

        ListTag recentList = new ListTag();
        for (UUID id : recentMorphs) {
            CompoundTag idTag = new CompoundTag();
            idTag.putUUID("id", id);
            recentList.add(idTag);
        }
        tag.put("recentMorphs", recentList);

        return tag;
    }

    /**
     * Deserialization from NBT.
     */
    public static PlayerMorphHistory fromNbt(CompoundTag tag) {
        PlayerMorphHistory history = new PlayerMorphHistory();

        if (tag.contains("unlockedMorphs", Tag.TAG_LIST)) {
            ListTag unlockedList = tag.getList("unlockedMorphs", Tag.TAG_COMPOUND);
            for (int i = 0; i < unlockedList.size(); i++) {
                CompoundTag idTag = unlockedList.getCompound(i);
                history.unlockMorph(idTag.getUUID("id"));
            }
        }

        if (tag.contains("favoriteMorphs", Tag.TAG_LIST)) {
            ListTag favoriteList = tag.getList("favoriteMorphs", Tag.TAG_COMPOUND);
            for (int i = 0; i < favoriteList.size(); i++) {
                CompoundTag idTag = favoriteList.getCompound(i);
                UUID id = idTag.getUUID("id");
                if (history.unlockedMorphs.contains(id)) {
                    history.favoriteMorphs.add(id);
                }
            }
        }

        if (tag.contains("recentMorphs", Tag.TAG_LIST)) {
            ListTag recentList = tag.getList("recentMorphs", Tag.TAG_COMPOUND);
            for (int i = 0; i < recentList.size(); i++) {
                CompoundTag idTag = recentList.getCompound(i);
                history.recentMorphs.add(idTag.getUUID("id"));
            }
        }

        return history;
    }

    /**
     * Creates a copy of this history.
     */
    public PlayerMorphHistory copy() {
        PlayerMorphHistory copy = new PlayerMorphHistory();
        copy.unlockedMorphs.addAll(this.unlockedMorphs);
        copy.favoriteMorphs.addAll(this.favoriteMorphs);
        copy.recentMorphs.addAll(this.recentMorphs);
        return copy;
    }
}
