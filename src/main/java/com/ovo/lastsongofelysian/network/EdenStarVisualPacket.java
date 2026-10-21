package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.client.EdenStarClientEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class EdenStarVisualPacket {
    public enum Effect {
        BLACK_HOLE,
        GRAVITY_WAVE
    }

    private final Effect effect;
    private final int targetId;
    private final double x;
    private final double y;
    private final double z;
    private final int duration;

    public EdenStarVisualPacket(Effect effect, int targetId, double x, double y, double z, int duration) {
        this.effect = effect;
        this.targetId = targetId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.duration = duration;
    }

    public static void encode(EdenStarVisualPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.effect);
        buffer.writeVarInt(packet.targetId);
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeVarInt(packet.duration);
    }

    public static EdenStarVisualPacket decode(FriendlyByteBuf buffer) {
        return new EdenStarVisualPacket(
                buffer.readEnum(Effect.class),
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readVarInt()
        );
    }

    public static void handle(EdenStarVisualPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {
                    if (packet.effect == Effect.GRAVITY_WAVE) {
                        EdenStarClientEffects.startGravityWave(packet.targetId, packet.duration);
                    }
                }
        ));
        context.setPacketHandled(true);
    }
}
