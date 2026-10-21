package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.event.EdenStarSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class EdenStarActionPacket {
    public enum Action {
        ZEROTH,
        FIRST,
        THIRD
    }

    private final Action action;

    public EdenStarActionPacket(Action action) {
        this.action = action;
    }

    public static void encode(EdenStarActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    public static EdenStarActionPacket decode(FriendlyByteBuf buffer) {
        return new EdenStarActionPacket(buffer.readEnum(Action.class));
    }

    public static void handle(EdenStarActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !EdenStarSystem.isHolding(player)) return;
            switch (packet.action) {
                case ZEROTH -> EdenStarSystem.useZerothRatedPower(player);
                case FIRST -> EdenStarSystem.useFirstRatedPower(player);
                case THIRD -> EdenStarSystem.useThirdRatedPower(player);
            }
        });
        context.setPacketHandled(true);
    }
}
