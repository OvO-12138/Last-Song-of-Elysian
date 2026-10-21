package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(
        modid = lastsongofelysian.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class PacketFixerNotice {
    private static final String PACKET_FIXER_MOD_ID = "packetfixer";

    private PacketFixerNotice() {
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (ModList.get().isLoaded(PACKET_FIXER_MOD_ID)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.translatable("message.lastsongofelysian.packet_fixer_recommendation"),
                        false
                );
            }
        });
    }
}
