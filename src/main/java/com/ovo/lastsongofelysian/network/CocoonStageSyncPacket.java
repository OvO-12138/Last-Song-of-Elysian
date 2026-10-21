package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.client.ClientCocoonPacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class CocoonStageSyncPacket {

    private static final int PACKET_ID = 100;
    private static boolean registered = false;

    private final int stage;
    private final int acquiredCoreMask;
    private final boolean reversed;
    private final boolean trialsUnlocked;
    private final int completedTrialMask;
    private final int thunderStrikes;
    private final int thunderKills;
    private final int earthExplosions;

    public CocoonStageSyncPacket(
            int stage,
            int acquiredCoreMask,
            boolean reversed,
            boolean trialsUnlocked,
            int completedTrialMask,
            int thunderStrikes,
            int thunderKills,
            int earthExplosions
    ) {
        this.stage = Math.max(0, Math.min(12, stage));
        this.acquiredCoreMask = acquiredCoreMask & 0x1FFF;
        this.reversed = reversed;
        this.trialsUnlocked = trialsUnlocked;
        this.completedTrialMask = completedTrialMask & 0xFFF;
        this.thunderStrikes = Math.max(0, Math.min(5, thunderStrikes));
        this.thunderKills = Math.max(0, Math.min(20, thunderKills));
        this.earthExplosions = Math.max(0, Math.min(10, earthExplosions));
    }

    public int getStage() {
        return stage;
    }

    public int getAcquiredCoreMask() {
        return acquiredCoreMask;
    }

    public boolean isReversed() {
        return reversed;
    }

    public boolean areTrialsUnlocked() {
        return trialsUnlocked;
    }

    public int getCompletedTrialMask() {
        return completedTrialMask;
    }

    public int getThunderStrikes() {
        return thunderStrikes;
    }

    public int getThunderKills() {
        return thunderKills;
    }

    public int getEarthExplosions() {
        return earthExplosions;
    }

    public static void encode(
            CocoonStageSyncPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeVarInt(message.stage);
        buffer.writeVarInt(message.acquiredCoreMask);
        buffer.writeBoolean(message.reversed);
        buffer.writeBoolean(message.trialsUnlocked);
        buffer.writeVarInt(message.completedTrialMask);
        buffer.writeVarInt(message.thunderStrikes);
        buffer.writeVarInt(message.thunderKills);
        buffer.writeVarInt(message.earthExplosions);
    }

    public static CocoonStageSyncPacket decode(FriendlyByteBuf buffer) {
        return new CocoonStageSyncPacket(
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    public static void handle(
            CocoonStageSyncPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(
                () -> DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () -> ClientCocoonPacketHandler.handle(message)
                )
        );

        context.setPacketHandled(true);
    }

    public static void register() {
        if (registered) {
            return;
        }

        ModNetwork.CHANNEL.registerMessage(
                PACKET_ID,
                CocoonStageSyncPacket.class,
                CocoonStageSyncPacket::encode,
                CocoonStageSyncPacket::decode,
                CocoonStageSyncPacket::handle
        );

        registered = true;
    }
}
