package com.seristic.morphlib.neoforge;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.MorphManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Handles networking for MorphLib on NeoForge platform.
 * Manages morph synchronization between server and clients.
 */
public class MorphLibNetworking {

    /**
     * Packet for syncing morph data to clients
     */
    public static class MorphSyncPayload implements CustomPacketPayload {
        public static final Type<MorphSyncPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("morphlib", "morph_sync"));

        public static final StreamCodec<RegistryFriendlyByteBuf, MorphSyncPayload> CODEC = StreamCodec
                .of(MorphSyncPayload::write, MorphSyncPayload::read);

        private final MorphData morphData;
        private final int entityId;

        public MorphSyncPayload(MorphData morphData, int entityId) {
            this.morphData = morphData;
            this.entityId = entityId;
        }

        private static void write(RegistryFriendlyByteBuf buf, MorphSyncPayload payload) {
            buf.writeInt(payload.entityId);
            buf.writeBoolean(payload.morphData != null);
            if (payload.morphData != null) {
                payload.morphData.write(buf);
            }
        }

        private static MorphSyncPayload read(RegistryFriendlyByteBuf buf) {
            int entityId = buf.readInt();
            MorphData morphData = null;
            if (buf.readBoolean()) {
                morphData = MorphData.read(buf);
            }
            return new MorphSyncPayload(morphData, entityId);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public MorphData getMorphData() {
            return morphData;
        }

        public int getEntityId() {
            return entityId;
        }
    }

    /**
     * Register networking packets
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(MorphSyncPayload.TYPE, MorphSyncPayload.CODEC, MorphLibNetworking::handleMorphSync);
        com.seristic.morphlib.Morphlib.LOGGER.info("Registered networking packets");
    }

    /**
     * Handle morph sync packet on client side
     */
    private static void handleMorphSync(MorphSyncPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(payload.getEntityId());
                if (entity != null) {
                    if (payload.getMorphData() != null) {
                        MorphManager.applyMorph(entity, payload.getMorphData());
                        com.seristic.morphlib.Morphlib.LOGGER.debug("Applied morph to entity: {}",
                                entity.getName().getString());
                    } else {
                        MorphManager.removeMorph(entity);
                        com.seristic.morphlib.Morphlib.LOGGER.debug("Removed morph from entity: {}",
                                entity.getName().getString());
                    }
                }
            }
        });
    }

    /**
     * Send morph data to a specific player
     */
    public static void sendToPlayer(ServerPlayer player, Entity entity, MorphData morphData) {
        MorphSyncPayload payload = new MorphSyncPayload(morphData, entity.getId());
        PacketDistributor.sendToPlayer(player, payload);
    }

    /**
     * Send morph data to all players tracking this entity
     */
    public static void sendToAllTracking(Entity entity, MorphData morphData) {
        MorphSyncPayload payload = new MorphSyncPayload(morphData, entity.getId());
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    /**
     * Send morph removal to all players tracking this entity
     */
    public static void sendRemovalToAllTracking(Entity entity) {
        MorphSyncPayload payload = new MorphSyncPayload(null, entity.getId());
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }
}
