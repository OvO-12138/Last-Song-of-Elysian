package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.client.AbyssFlowerClient;
import com.ovo.lastsongofelysian.util.AbyssFlowerRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AbyssFlowerStatePacket(int casterId, AbyssFlowerRules.Power power, boolean active,
                                    double x, double y, double z, int duration) {
    public static void encode(AbyssFlowerStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.casterId);
        buffer.writeEnum(packet.power);
        buffer.writeBoolean(packet.active);
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeVarInt(packet.duration);
    }

    public static AbyssFlowerStatePacket decode(FriendlyByteBuf buffer) {
        return new AbyssFlowerStatePacket(buffer.readVarInt(), buffer.readEnum(AbyssFlowerRules.Power.class),
                buffer.readBoolean(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readVarInt());
    }

    public static void handle(AbyssFlowerStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AbyssFlowerClient.accept(packet)));
        context.setPacketHandled(true);
    }
}
