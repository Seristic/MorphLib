package com.seristic.morphlib.neoforge;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.MorphManager;
import com.seristic.morphlib.MorphStack;
import com.seristic.morphlib.neoforge.network.S2CMorphClearPacket;
import com.seristic.morphlib.neoforge.network.S2CMorphStackSyncPacket;
import com.seristic.morphlib.neoforge.network.S2CMorphSyncPacket;
import com.seristic.morphlib.logging.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Handles networking for MorphLib on NeoForge platform.
 * Manages morph synchronization between server and clients.
 */
public class MorphLibNetworking {

    /**
     * Register networking packets
     */
    public static void register(PayloadRegistrar registrar) {
        ModLogger.info("MorphLibNetworking", "🔧 Starting packet registration...");
        ModLogger.info("MorphLibNetworking", "🔍 Registrar info: " + registrar.toString());

        try {
            registrar.playToClient(
                    S2CMorphSyncPacket.TYPE,
                    S2CMorphSyncPacket.CODEC,
                    MorphLibNetworking::handleMorphSync);
            ModLogger.info("MorphLibNetworking",
                    "✅ Registered S2CMorphSyncPacket with TYPE: " + S2CMorphSyncPacket.TYPE);

            registrar.playToClient(
                    S2CMorphClearPacket.TYPE,
                    S2CMorphClearPacket.CODEC,
                    MorphLibNetworking::handleMorphClear);
            ModLogger.info("MorphLibNetworking", "✅ Registered S2CMorphClearPacket");

            registrar.playToClient(
                    S2CMorphStackSyncPacket.TYPE,
                    S2CMorphStackSyncPacket.CODEC,
                    MorphLibNetworking::handleMorphStackSync);
            ModLogger.info("MorphLibNetworking", "✅ Registered S2CMorphStackSyncPacket");

            ModLogger.info("MorphLibNetworking", "🎉 All networking packets registered successfully!");
        } catch (Exception e) {
            ModLogger.error("MorphLibNetworking", "💥 Exception during packet registration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle morph sync packet on client side
     */
    private static void handleMorphSync(S2CMorphSyncPacket payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        ModLogger.info("MorphLibNetworking", "� PACKET HANDLER CALLED! handleMorphSync invoked!");
        ModLogger.info("MorphLibNetworking", "�📦 Received morph sync packet for entity ID: " + payload.entityId() +
                " with morph: " + payload.morphData().getEntityType().toShortString());

        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(payload.entityId());
                if (entity != null) {
                    MorphManager.applyMorph(entity, payload.morphData());
                    ModLogger.info("MorphLibNetworking",
                            "✅ Successfully applied morph " + payload.morphData().getEntityType().toShortString() +
                                    " to entity: " + entity.getName().getString() + " (ID: " + payload.entityId()
                                    + ")");
                } else {
                    ModLogger.warn("MorphLibNetworking",
                            "❌ Could not find entity with ID: " + payload.entityId() + " in client world");
                }
            } else {
                ModLogger.warn("MorphLibNetworking", "❌ Client world is null, cannot apply morph sync");
            }
        });
    }

    /**
     * Handle morph clear packet on client side
     */
    private static void handleMorphClear(S2CMorphClearPacket payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(payload.entityId());
                if (entity != null) {
                    MorphManager.removeMorph(entity);
                    ModLogger.debug("MorphLibNetworking",
                            "Removed morph from entity: " + entity.getName().getString());
                }
            }
        });
    }

    /**
     * Handle morph stack sync packet on client side
     */
    private static void handleMorphStackSync(S2CMorphStackSyncPacket payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(payload.entityId());
                if (entity != null) {
                    // Apply the entire morph stack to the entity
                    // Clear existing stack first, then apply new one
                    MorphManager.clearAllMorphs(entity);
                    for (var layer : payload.morphStack().getLayers()) {
                        MorphManager.addMorphLayer(entity, layer.getLayerId(),
                                layer.getPriority(), layer.getMorphData());
                    }
                    ModLogger.debug("MorphLibNetworking",
                            "Applied morph stack to entity: " + entity.getName().getString() +
                                    " (" + payload.morphStack().getLayers().size() + " layers)");
                }
            }
        });
    }

    /**
     * Send morph data to a specific player
     */
    public static void sendToPlayer(ServerPlayer player, Entity entity, MorphData morphData) {
        ModLogger.info("MorphLibNetworking",
                "🎯 ENTERING sendToPlayer for player: " + player.getName().getString() +
                        ", entity ID: " + entity.getId() + " with morph: " + morphData.getEntityType().toShortString());

        S2CMorphSyncPacket payload = new S2CMorphSyncPacket(entity.getId(), morphData);

        try {
            PacketDistributor.sendToPlayer(player, payload);
            ModLogger.info("MorphLibNetworking",
                    "🚀 Successfully sent morph sync packet to player " + player.getName().getString());
        } catch (Exception e) {
            ModLogger.error("MorphLibNetworking",
                    "💥 Exception while sending packet to player: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send morph data to all players tracking this entity
     */
    public static void sendToAllTracking(Entity entity, MorphData morphData) {
        ModLogger.info("MorphLibNetworking",
                "🎯 ENTERING sendToAllTracking for entity ID: " + entity.getId() +
                        " with morph: " + morphData.getEntityType().toShortString());

        S2CMorphSyncPacket payload = new S2CMorphSyncPacket(entity.getId(), morphData);

        ModLogger.info("MorphLibNetworking",
                "📦 Created packet payload, about to send to tracking players...");

        try {
            PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
            ModLogger.info("MorphLibNetworking",
                    "🚀 Successfully sent morph sync packet to ALL TRACKING for entity ID: " + entity.getId());
        } catch (Exception e) {
            ModLogger.error("MorphLibNetworking",
                    "💥 Exception while sending packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send morph removal to all players tracking this entity
     */
    public static void sendRemovalToAllTracking(Entity entity) {
        S2CMorphClearPacket payload = new S2CMorphClearPacket(entity.getId());
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
        ModLogger.info("MorphLibNetworking",
                "🚀 Sending morph REMOVAL packet to ALL TRACKING for entity ID: " + entity.getId());
    }

    /**
     * Send morph removal to a specific player
     */
    public static void sendRemovalToPlayer(ServerPlayer player, Entity entity) {
        S2CMorphClearPacket payload = new S2CMorphClearPacket(entity.getId());
        PacketDistributor.sendToPlayer(player, payload);
        ModLogger.info("MorphLibNetworking",
                "🚀 Sending morph REMOVAL packet to player " + player.getName().getString() + " for entity ID: " + entity.getId());
    }

    /**
     * Send morph stack data to a specific player
     */
    public static void sendStackToPlayer(ServerPlayer player, Entity entity, MorphStack morphStack) {
        S2CMorphStackSyncPacket payload = new S2CMorphStackSyncPacket(entity.getId(), morphStack);
        PacketDistributor.sendToPlayer(player, payload);
    }

    /**
     * Send morph stack data to all players tracking this entity
     */
    public static void sendStackToAllTracking(Entity entity, MorphStack morphStack) {
        S2CMorphStackSyncPacket payload = new S2CMorphStackSyncPacket(entity.getId(), morphStack);
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }
}
