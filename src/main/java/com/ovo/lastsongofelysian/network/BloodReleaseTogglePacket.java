package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.event.DecimationSignetSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class BloodReleaseTogglePacket {
    public static void encode(BloodReleaseTogglePacket packet, FriendlyByteBuf buffer) {
    }

    public static BloodReleaseTogglePacket decode(FriendlyByteBuf buffer) {
        return new BloodReleaseTogglePacket();
    }

    public static void handle(BloodReleaseTogglePacket packet,
                              Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            Boolean enabled = DecimationSignetSystem.toggleBloodRelease(player);
            String key = enabled == null
                    ? "message.lastsongofelysian.blood_release_unavailable"
                    : enabled
                    ? "message.lastsongofelysian.blood_release_on"
                    : "message.lastsongofelysian.blood_release_off";
            player.displayClientMessage(Component.translatable(key), true);
        });
        context.setPacketHandled(true);
    }
}
