package com.seristic.morphlib.neoforge.network;

import com.seristic.morphlib.MorphStack;
import com.seristic.morphlib.Morphlib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet sent from server to client to sync morph stack data for an entity.
 * This packet contains the entity ID and the morph stack to apply.
 */
public record S2CMorphStackSyncPacket(int entityId, MorphStack morphStack) implements CustomPacketPayload {

    public static final Type<S2CMorphStackSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Morphlib.MOD_ID, "morph_stack_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMorphStackSyncPacket> CODEC = StreamCodec
            .of(S2CMorphStackSyncPacket::write, S2CMorphStackSyncPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, S2CMorphStackSyncPacket packet) {
        buf.writeInt(packet.entityId);
        packet.morphStack.write(buf);
    }

    private static S2CMorphStackSyncPacket read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        MorphStack morphStack = MorphStack.read(buf);
        return new S2CMorphStackSyncPacket(entityId, morphStack);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}