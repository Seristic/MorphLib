package com.seristic.morphlib.neoforge.network;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.Morphlib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet sent from server to client to sync morph data for an entity.
 * This packet contains the entity ID and the morph data to apply.
 */
public record S2CMorphSyncPacket(int entityId, MorphData morphData) implements CustomPacketPayload {

    public static final Type<S2CMorphSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Morphlib.MOD_ID, "morph_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMorphSyncPacket> CODEC = StreamCodec
            .of(S2CMorphSyncPacket::write, S2CMorphSyncPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, S2CMorphSyncPacket packet) {
        buf.writeInt(packet.entityId);
        packet.morphData.write(buf);
    }

    private static S2CMorphSyncPacket read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        MorphData morphData = MorphData.read(buf);
        return new S2CMorphSyncPacket(entityId, morphData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
