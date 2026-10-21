package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.Optional;

public class ModNetwork {
    private static final String VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(lastsongofelysian.MODID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }

        CHANNEL.registerMessage(0,
                SilverPouchActionPacket.class,
                SilverPouchActionPacket::encode,
                SilverPouchActionPacket::decode,
                SilverPouchActionPacket::handle);
        CHANNEL.registerMessage(1,
                ComboSyncPacket.class,
                ComboSyncPacket::encode,
                ComboSyncPacket::decode,
                ComboSyncPacket::handle);
        CHANNEL.registerMessage(2,
                PreceptSyncPacket.class,
                PreceptSyncPacket::encode,
                PreceptSyncPacket::decode,
                PreceptSyncPacket::handle);
        CHANNEL.registerMessage(3,
                CorruptionSyncPacket.class,
                CorruptionSyncPacket::encode,
                CorruptionSyncPacket::decode,
                CorruptionSyncPacket::handle);
        CHANNEL.registerMessage(4,
                FlawlessVisualPacket.class,
                FlawlessVisualPacket::encode,
                FlawlessVisualPacket::decode,
                FlawlessVisualPacket::handle);
        CHANNEL.registerMessage(5,
                SakuraDodgeEffectPacket.class,
                SakuraDodgeEffectPacket::encode,
                SakuraDodgeEffectPacket::decode,
                SakuraDodgeEffectPacket::handle);
        CHANNEL.registerMessage(6,
                FlawlessWeaponActionPacket.class,
                FlawlessWeaponActionPacket::encode,
                FlawlessWeaponActionPacket::decode,
                FlawlessWeaponActionPacket::handle);
        CHANNEL.registerMessage(7,
                BloomStackSyncPacket.class,
                BloomStackSyncPacket::encode,
                BloomStackSyncPacket::decode,
                BloomStackSyncPacket::handle);
        CHANNEL.registerMessage(8,
                FenghuangDownActionPacket.class,
                FenghuangDownActionPacket::encode,
                FenghuangDownActionPacket::decode,
                FenghuangDownActionPacket::handle);
        CHANNEL.registerMessage(9,
                FenghuangDownVisualPacket.class,
                FenghuangDownVisualPacket::encode,
                FenghuangDownVisualPacket::decode,
                FenghuangDownVisualPacket::handle);

        CHANNEL.registerMessage(10, AbyssFlowerActionPacket.class, AbyssFlowerActionPacket::encode,
                AbyssFlowerActionPacket::decode, AbyssFlowerActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(11, AbyssFlowerStatePacket.class, AbyssFlowerStatePacket::encode,
                AbyssFlowerStatePacket::decode, AbyssFlowerStatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(12, SkyfireGreatswordActionPacket.class,
                SkyfireGreatswordActionPacket::encode,
                SkyfireGreatswordActionPacket::decode,
                SkyfireGreatswordActionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(13, EdenStarActionPacket.class,
                EdenStarActionPacket::encode,
                EdenStarActionPacket::decode,
                EdenStarActionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(14, EdenStarVisualPacket.class,
                EdenStarVisualPacket::encode,
                EdenStarVisualPacket::decode,
                EdenStarVisualPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(15, CombatHudSyncPacket.class,
                CombatHudSyncPacket::encode,
                CombatHudSyncPacket::decode,
                CombatHudSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(16, BloodReleaseTogglePacket.class,
                BloodReleaseTogglePacket::encode,
                BloodReleaseTogglePacket::decode,
                BloodReleaseTogglePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(17, BodhiPassiveDamageTogglePacket.class,
                BodhiPassiveDamageTogglePacket::encode,
                BodhiPassiveDamageTogglePacket::decode,
                BodhiPassiveDamageTogglePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(18, MissingShiJiBookCrashPacket.class,
                MissingShiJiBookCrashPacket::encode,
                MissingShiJiBookCrashPacket::decode,
                MissingShiJiBookCrashPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registered = true;
    }
}
