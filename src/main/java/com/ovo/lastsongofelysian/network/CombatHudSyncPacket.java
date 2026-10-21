package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.client.ClientHudHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CombatHudSyncPacket(boolean bloodActive, int bloodCharge,
                                  boolean vipActive, int vip,
                                  boolean resolveActive, int resolve,
                                  boolean evasionActive, int evasionCooldown,
                                  int evasionCooldownMaximum, int evasionUses,
                                  int evasionMaximumUses) {
    public static void encode(CombatHudSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.bloodActive);
        buffer.writeVarInt(packet.bloodCharge);
        buffer.writeBoolean(packet.vipActive);
        buffer.writeVarInt(packet.vip);
        buffer.writeBoolean(packet.resolveActive);
        buffer.writeVarInt(packet.resolve);
        buffer.writeBoolean(packet.evasionActive);
        buffer.writeVarInt(packet.evasionCooldown);
        buffer.writeVarInt(packet.evasionCooldownMaximum);
        buffer.writeVarInt(packet.evasionUses);
        buffer.writeVarInt(packet.evasionMaximumUses);
    }

    public static CombatHudSyncPacket decode(FriendlyByteBuf buffer) {
        return new CombatHudSyncPacket(
                buffer.readBoolean(), buffer.readVarInt(),
                buffer.readBoolean(), buffer.readVarInt(),
                buffer.readBoolean(), buffer.readVarInt(),
                buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(CombatHudSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> ClientHudHooks.handleCombatHud(packet)));
        context.get().setPacketHandled(true);
    }
}
