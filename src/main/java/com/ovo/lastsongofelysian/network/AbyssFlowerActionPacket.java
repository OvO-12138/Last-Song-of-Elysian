package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.event.AbyssFlowerSystem;
import com.ovo.lastsongofelysian.util.AbyssFlowerRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AbyssFlowerActionPacket(Action action) {
    public enum Action { ZEROTH, FIRST, SECOND, CANCEL }

    public static void encode(AbyssFlowerActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    public static AbyssFlowerActionPacket decode(FriendlyByteBuf buffer) {
        return new AbyssFlowerActionPacket(buffer.readEnum(Action.class));
    }

    public static void handle(AbyssFlowerActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.isAlive()) return;
            if (packet.action == Action.CANCEL) AbyssFlowerSystem.cancelChant(player);
            else AbyssFlowerSystem.beginChant(player, AbyssFlowerRules.Power.valueOf(packet.action.name()));
        });
        context.setPacketHandled(true);
    }
}
