package com.seristic.morphlib.network;

import java.util.UUID;

import com.seristic.morphlib.Morphlib;
import com.seristic.morphlib.morph.MorphState;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet sent from server to client to sync morph state changes.
 * This packet contains the entity ID and the new morph state to apply.
 */
public record S2CMorphStatePacket(UUID entityId, MorphState morphState) implements CustomPacketPayload {

    public static final Type<S2CMorphStatePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Morphlib.MOD_ID, "morph_state_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMorphStatePacket> CODEC = StreamCodec
            .of(S2CMorphStatePacket::write, S2CMorphStatePacket::read);

    private static void write(RegistryFriendlyByteBuf buf, S2CMorphStatePacket packet) {
        buf.writeUUID(packet.entityId);
        packet.morphState.write(buf);
    }

    private static S2CMorphStatePacket read(RegistryFriendlyByteBuf buf) {
        UUID entityId = buf.readUUID();
        MorphState morphState = MorphState.read(buf);
        return new S2CMorphStatePacket(entityId, morphState);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}