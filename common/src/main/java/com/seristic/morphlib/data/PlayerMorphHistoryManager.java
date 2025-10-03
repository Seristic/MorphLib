package com.seristic.morphlib.data;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player morph histories for all players.
 * Provides centralized access and persistence handling.
 */
public class PlayerMorphHistoryManager {
    private static final PlayerMorphHistoryManager INSTANCE = new PlayerMorphHistoryManager();

    private final Map<UUID, PlayerMorphHistory> playerHistories = new ConcurrentHashMap<>();

    private PlayerMorphHistoryManager() {
    }

    public static PlayerMorphHistoryManager getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the morph history for a player, creating a new one if it doesn't exist.
     */
    public PlayerMorphHistory getHistory(UUID playerId) {
        return playerHistories.computeIfAbsent(playerId, id -> new PlayerMorphHistory());
    }

    /**
     * Sets the morph history for a player (used during loading).
     */
    public void setHistory(UUID playerId, PlayerMorphHistory history) {
        playerHistories.put(playerId, history);
    }

    /**
     * Removes a player's history (cleanup when player leaves).
     */
    public void removeHistory(UUID playerId) {
        playerHistories.remove(playerId);
    }

    /**
     * Saves a player's history to NBT.
     */
    public CompoundTag saveHistory(UUID playerId) {
        PlayerMorphHistory history = playerHistories.get(playerId);
        if (history != null) {
            return history.toNbt();
        }
        return new CompoundTag();
    }

    /**
     * Loads a player's history from NBT.
     */
    public void loadHistory(UUID playerId, CompoundTag tag) {
        PlayerMorphHistory history = PlayerMorphHistory.fromNbt(tag);
        setHistory(playerId, history);
    }

    /**
     * Clears all player histories (for server shutdown/cleanup).
     */
    public void clearAll() {
        playerHistories.clear();
    }
}
