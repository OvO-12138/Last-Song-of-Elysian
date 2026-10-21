package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.event.SkyfireGreatswordSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SkyfireGreatswordActionPacket {
    public enum Action {
        ZEROTH
    }

    private final Action action;

    public SkyfireGreatswordActionPacket(Action action) {
        this.action = action;
    }

    public static void encode(SkyfireGreatswordActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    public static SkyfireGreatswordActionPacket decode(FriendlyByteBuf buffer) {
        return new SkyfireGreatswordActionPacket(buffer.readEnum(Action.class));
    }

    public static void handle(SkyfireGreatswordActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && SkyfireGreatswordSystem.isHolding(player)
                    && packet.action == Action.ZEROTH) {
                SkyfireGreatswordSystem.useZerothRatedPower(player);
            }
        });
        context.setPacketHandled(true);
    }
}
