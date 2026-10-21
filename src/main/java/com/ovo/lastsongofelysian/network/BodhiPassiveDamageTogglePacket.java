package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.event.BodhiSignetSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class BodhiPassiveDamageTogglePacket {
    public static void encode(BodhiPassiveDamageTogglePacket packet, FriendlyByteBuf buffer) {
    }

    public static BodhiPassiveDamageTogglePacket decode(FriendlyByteBuf buffer) {
        return new BodhiPassiveDamageTogglePacket();
    }

    public static void handle(BodhiPassiveDamageTogglePacket packet,
                              Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            Boolean enabled = BodhiSignetSystem.togglePassiveDamage(player);
            String key = enabled == null
                    ? "message.lastsongofelysian.bodhi_passive_damage_unavailable"
                    : enabled
                    ? "message.lastsongofelysian.bodhi_passive_damage_on"
                    : "message.lastsongofelysian.bodhi_passive_damage_off";
            player.displayClientMessage(Component.translatable(key), true);
        });
        context.setPacketHandled(true);
    }
}
