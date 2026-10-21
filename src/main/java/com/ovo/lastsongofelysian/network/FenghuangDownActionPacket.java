package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.event.FenghuangDownSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class FenghuangDownActionPacket {
    public enum Action {
        ZEROTH,
        FIRST,
        SECOND
    }

    private final Action action;

    public FenghuangDownActionPacket(Action action) {
        this.action = action;
    }

    public static void encode(FenghuangDownActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    public static FenghuangDownActionPacket decode(FriendlyByteBuf buffer) {
        return new FenghuangDownActionPacket(buffer.readEnum(Action.class));
    }

    public static void handle(
            FenghuangDownActionPacket packet,
            Supplier<NetworkEvent.Context> supplier
    ) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !FenghuangDownSystem.isHoldingFenghuangDown(player)) {
                return;
            }
            switch (packet.action) {
                case ZEROTH -> FenghuangDownSystem.useZerothRatedPower(player);
                case FIRST -> FenghuangDownSystem.useFirstRatedPower(player);
                case SECOND -> FenghuangDownSystem.useSecondRatedPower(player);
            }
        });
        context.setPacketHandled(true);
    }
}
