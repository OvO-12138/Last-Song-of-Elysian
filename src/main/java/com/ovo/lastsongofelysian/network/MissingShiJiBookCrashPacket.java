package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.client.ClientCrashHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MissingShiJiBookCrashPacket() {
    public static void encode(MissingShiJiBookCrashPacket packet, FriendlyByteBuf buffer) {
    }

    public static MissingShiJiBookCrashPacket decode(FriendlyByteBuf buffer) {
        return new MissingShiJiBookCrashPacket();
    }

    public static void handle(MissingShiJiBookCrashPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> ClientCrashHooks::crashWithoutExplanation));
        context.get().setPacketHandled(true);
    }
}
