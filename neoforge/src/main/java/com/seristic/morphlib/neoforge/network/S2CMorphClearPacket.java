package com.seristic.morphlib.neoforge.network;

import com.seristic.morphlib.Morphlib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet sent from server to client to clear/remove morph data from an entity.
 * This packet only contains the entity ID.
 */
public record S2CMorphClearPacket(int entityId) implements CustomPacketPayload {

    public static final Type<S2CMorphClearPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Morphlib.MOD_ID, "morph_clear"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMorphClearPacket> CODEC = StreamCodec
            .of(S2CMorphClearPacket::write, S2CMorphClearPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, S2CMorphClearPacket packet) {
        buf.writeInt(packet.entityId);
    }

    private static S2CMorphClearPacket read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        return new S2CMorphClearPacket(entityId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
