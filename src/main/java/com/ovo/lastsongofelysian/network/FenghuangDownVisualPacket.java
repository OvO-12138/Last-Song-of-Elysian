package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.client.FenghuangDownClientEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class FenghuangDownVisualPacket {
    public enum Effect {
        MIND_DOMAIN,
        FIRST_POWER_BARRAGE,
        FEATHER
    }

    private final Effect effect;
    private final int casterId;
    private final double x;
    private final double y;
    private final double z;
    private final int duration;

    public FenghuangDownVisualPacket(
            Effect effect,
            int casterId,
            double x,
            double y,
            double z,
            int duration
    ) {
        this.effect = effect;
        this.casterId = casterId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.duration = duration;
    }

    public static void encode(FenghuangDownVisualPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.effect);
        buffer.writeVarInt(packet.casterId);
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeVarInt(packet.duration);
    }

    public static FenghuangDownVisualPacket decode(FriendlyByteBuf buffer) {
        return new FenghuangDownVisualPacket(
                buffer.readEnum(Effect.class),
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readVarInt()
        );
    }

    public static void handle(
            FenghuangDownVisualPacket packet,
            Supplier<NetworkEvent.Context> supplier
    ) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {
                    switch (packet.effect) {
                        case MIND_DOMAIN -> FenghuangDownClientEffects.startDomain(
                                packet.casterId, packet.x, packet.y, packet.z, packet.duration
                        );
                        case FIRST_POWER_BARRAGE -> FenghuangDownClientEffects.startFirstPowerBarrage(
                                packet.casterId, packet.duration
                        );
                        case FEATHER -> FenghuangDownClientEffects.startFeather(
                                packet.casterId, packet.x, packet.y, packet.z, packet.duration
                        );
                    }
                }
        ));
        context.setPacketHandled(true);
    }
}
