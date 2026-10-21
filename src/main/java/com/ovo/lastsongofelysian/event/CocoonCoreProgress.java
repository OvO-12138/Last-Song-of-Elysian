package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.util.ModText;
import com.ovo.lastsongofelysian.util.ModAdvancements;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.network.CocoonStageSyncPacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import com.ovo.lastsongofelysian.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Locale;

@Mod.EventBusSubscriber(
        modid = lastsongofelysian.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class CocoonCoreProgress {

    private CocoonCoreProgress() {
    }

    private static final String NBT_PLAYER_PERSISTED = "PlayerPersisted";
    private static final String NBT_CORE_STAGE = "CocoonHerrscherCoreStage";
    private static final String NBT_ACQUIRED_CORE_MASK = "CocoonAcquiredCoreMask";
    private static final String NBT_UNORDERED_CORE_MIGRATED = "CocoonUnorderedCoreMigrated";
    private static final String NBT_ACQUIRED_SIGNET_MASK = "CocoonAcquiredSignetMask";
    private static final String NBT_CURSES_REVERSED = "CocoonCursesReversed";
    private static final String NBT_TRIAL_SYSTEM_MIGRATED = "CocoonTrialSystemMigrated";
    private static final String NBT_CORROSION_CURSE_CLEARED = "CocoonCorrosionCurseCleared";
    private static final ResourceLocation[] ALL_CORE_IDS = {
            coreId("core_of_reason"), coreId("core_of_void"),
            coreId("core_of_thunder"), coreId("core_of_wind"),
            coreId("core_of_ice"), coreId("core_of_death"),
            coreId("core_of_fire"), coreId("core_of_sentience"),
            coreId("core_of_earth"), coreId("core_of_dominance"),
            coreId("core_of_binding"), coreId("core_of_corrosion"),
            coreId("core_of_origin")
    };
    private static final int ALL_CORE_MASK = (1 << ALL_CORE_IDS.length) - 1;
    private static final int NON_ORIGIN_CORE_MASK = (1 << 12) - 1;
    private static final ResourceLocation[] ALL_SIGNET_IDS = {
            itemId("signet_of_reverie"), itemId("signet_of_vicissitude"),
            itemId("signet_of_stars"), itemId("signet_of_infinity"),
            itemId("signet_of_daybreak"), itemId("signet_of_setsura"),
            itemId("signet_of_bodhi"), itemId("signet_of_decimation"),
            itemId("signet_of_helix"), itemId("signet_of_gold"),
            itemId("signet_of_discipline"), itemId("signet_of_deliverance"),
            itemId("signet_of_ego")
    };
    private static final int ALL_SIGNET_MASK = (1 << ALL_SIGNET_IDS.length) - 1;
    private static final int EGO_SIGNET_BIT = 1 << (ALL_SIGNET_IDS.length - 1);
    private static final int NON_EGO_SIGNET_MASK = EGO_SIGNET_BIT - 1;

    public static final int REASON_STAGE = 1;
    public static final int VOID_STAGE = 2;
    public static final int THUNDER_STAGE = 3;
    public static final int WIND_STAGE = 4;
    public static final int ICE_STAGE = 5;
    public static final int DEATH_STAGE = 6;
    public static final int FIRE_STAGE = 7;
    public static final int SENTIENCE_STAGE = 8;
    public static final int EARTH_STAGE = 9;
    public static final int DOMINANCE_STAGE = 10;
    public static final int BINDING_STAGE = 11;
    public static final int CORROSION_STAGE = 12;
    public static final int MAX_STAGE = 12;

    private enum CoreEntry {
        REASON(REASON_STAGE, "理之核心", "core_of_reason", ChatFormatting.BLUE),
        VOID(VOID_STAGE, "空之核心", "core_of_void", ChatFormatting.LIGHT_PURPLE),
        THUNDER(THUNDER_STAGE, "雷之核心", "core_of_thunder", ChatFormatting.LIGHT_PURPLE),
        WIND(WIND_STAGE, "风之核心", "core_of_wind", ChatFormatting.DARK_GREEN),
        ICE(ICE_STAGE, "冰之核心", "core_of_ice", ChatFormatting.AQUA),
        DEATH(DEATH_STAGE, "死之核心", "core_of_death", ChatFormatting.DARK_GREEN),
        FIRE(FIRE_STAGE, "炎之核心", "core_of_fire", ChatFormatting.RED),
        SENTIENCE(SENTIENCE_STAGE, "识之核心", "core_of_sentience", ChatFormatting.YELLOW),
        EARTH(EARTH_STAGE, "岩之核心", "core_of_earth", ChatFormatting.GOLD),
        DOMINANCE(DOMINANCE_STAGE, "支配之核心", "core_of_dominance", ChatFormatting.YELLOW),
        BINDING(BINDING_STAGE, "约束之核心", "core_of_binding", ChatFormatting.GOLD),
        CORROSION(CORROSION_STAGE, "侵蚀之核心", "core_of_corrosion", ChatFormatting.LIGHT_PURPLE);

        private final int stage;
        private final String displayName;
        private final ResourceLocation itemId;
        private final ChatFormatting color;

        CoreEntry(
                int stage,
                String displayName,
                String registryName,
                ChatFormatting color
        ) {
            this.stage = stage;
            this.displayName = displayName;
            this.itemId = new ResourceLocation(lastsongofelysian.MODID, registryName);
            this.color = color;
        }

        private static CoreEntry byStage(int stage) {
            for (CoreEntry entry : values()) {
                if (entry.stage == stage) {
                    return entry;
                }
            }
            return null;
        }
    }

    private static CompoundTag getData(Player player) {
        CompoundTag root = player.getPersistentData();

        if (!root.contains(NBT_PLAYER_PERSISTED, Tag.TAG_COMPOUND)) {
            root.put(NBT_PLAYER_PERSISTED, new CompoundTag());
        }

        return root.getCompound(NBT_PLAYER_PERSISTED);
    }

    private static int getAcquiredCoreMask(Player player) {
        CompoundTag data = getData(player);
        int mask = data.getInt(NBT_ACQUIRED_CORE_MASK) & ALL_CORE_MASK;

        if (!data.getBoolean(NBT_UNORDERED_CORE_MIGRATED)) {
            int legacyStage = Mth.clamp(data.getInt(NBT_CORE_STAGE), 0, MAX_STAGE);
            if (legacyStage > 0) {
                mask |= (1 << legacyStage) - 1;
            }
            data.putBoolean(NBT_UNORDERED_CORE_MIGRATED, true);
            data.putInt(NBT_ACQUIRED_CORE_MASK, mask);
        }

        return mask;
    }

    public static int getStage(Player player) {
        return Integer.bitCount(getAcquiredCoreMask(player) & NON_ORIGIN_CORE_MASK);
    }

    public static void setStage(Player player, int stage) {
        int normalized = Mth.clamp(stage, 0, MAX_STAGE);
        CompoundTag data = getData(player);
        int origin = getAcquiredCoreMask(player) & ~NON_ORIGIN_CORE_MASK;
        int coreMask = normalized == 0 ? 0 : (1 << normalized) - 1;
        data.putInt(NBT_CORE_STAGE, normalized);
        data.putInt(NBT_ACQUIRED_CORE_MASK, origin | coreMask);
    }

    public static boolean unlocked(Player player, int requiredStage) {
        return requiredStage >= 1
                && requiredStage <= MAX_STAGE
                && (getAcquiredCoreMask(player) & (1 << (requiredStage - 1))) != 0;
    }

    public static boolean isReversed(Player player) {
        return getData(player).getBoolean(NBT_CURSES_REVERSED);
    }

    public static void setReversed(Player player, boolean reversed) {
        getData(player).putBoolean(NBT_CURSES_REVERSED, reversed);
    }

    public static boolean hasAllCores(Player player) {
        return (getAcquiredCoreMask(player) & ALL_CORE_MASK) == ALL_CORE_MASK;
    }

    public static float getMultiplier(Player player) {
        return 1.0F + getStage(player) * 0.10F;
    }

    public static float scaleValue(Player player, float value) {
        return value * getMultiplier(player);
    }

    public static double scaleValue(Player player, double value) {
        return value * getMultiplier(player);
    }

    public static float scaleChance(Player player, float chance) {
        return Mth.clamp(
                chance * getMultiplier(player),
                0.0F,
                0.95F
        );
    }

    public static float scalePercent(Player player, float percent) {
        return Mth.clamp(
                percent * getMultiplier(player),
                0.0F,
                0.90F
        );
    }

    public static int scaleDuration(Player player, int ticks) {
        return Math.max(
                1,
                Math.round(ticks * Math.min(3.0F, getMultiplier(player)))
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide() || player.tickCount % 20 != 0) {
            return;
        }

        try {
            updateAcquiredCores(player);
        } catch (Throwable throwable) {
            System.err.println(
                    "[LastSongOfElysian] CocoonCoreProgress player tick failed:"
            );
            throwable.printStackTrace(System.err);
        }
    }

    private static void updateAcquiredCores(Player player) {
        CompoundTag data = getData(player);
        int previousMask = getAcquiredCoreMask(player);
        int mask = previousMask;
        for (int index = 0; index < ALL_CORE_IDS.length; index++) {
            if (containsItemId(player, ALL_CORE_IDS[index])) mask |= 1 << index;
        }
        data.putInt(NBT_ACQUIRED_CORE_MASK, mask);
        data.putInt(NBT_CORE_STAGE, Integer.bitCount(mask & NON_ORIGIN_CORE_MASK));
        clearCorrosionCurseIfNeeded(player, data, mask);

        int newlyAcquired = (mask & NON_ORIGIN_CORE_MASK) & ~previousMask;
        for (int index = 0; index < 12; index++) {
            if ((newlyAcquired & (1 << index)) != 0) {
                announceCoreAcquired(player, CoreEntry.byStage(index + 1));
            }
        }

        if (newlyAcquired != 0) {
            syncToClient(player);
        }

        int signetMask = data.getInt(NBT_ACQUIRED_SIGNET_MASK);

        for (int index = 0; index < ALL_SIGNET_IDS.length; index++) {
            if (containsItemId(player, ALL_SIGNET_IDS[index])) {
                signetMask |= 1 << index;
            }
        }

        if ((signetMask & NON_EGO_SIGNET_MASK) == NON_EGO_SIGNET_MASK
                && (signetMask & EGO_SIGNET_BIT) == 0) {
            ItemStack ego = new ItemStack(ModItems.SIGNET_OF_EGO.get());
            if (!player.addItem(ego)) {
                player.drop(ego, false);
            }
            signetMask |= EGO_SIGNET_BIT;
        }

        if ((signetMask & NON_EGO_SIGNET_MASK) == NON_EGO_SIGNET_MASK) {
            ModAdvancements.award(player, "signet_ego");
        }

        data.putInt(NBT_ACQUIRED_SIGNET_MASK, signetMask);

        if (
                (mask & NON_ORIGIN_CORE_MASK) == NON_ORIGIN_CORE_MASK &&
                (signetMask & ALL_SIGNET_MASK) == ALL_SIGNET_MASK &&
                (mask & (1 << 12)) == 0
        ) {
            ItemStack origin = new ItemStack(
                    ModItems.CORE_OF_ORIGIN.get()
            );

            if (!origin.isEmpty()) {
                if (!player.addItem(origin)) {
                    player.drop(origin, false);
                }

                mask |= 1 << 12;
                data.putInt(NBT_ACQUIRED_CORE_MASK, mask);
            }
        }

        if ((mask & NON_ORIGIN_CORE_MASK) == NON_ORIGIN_CORE_MASK
                && (signetMask & EGO_SIGNET_BIT) != 0) {
            ModAdvancements.award(player, "core_origin");
        }

        if (mask == ALL_CORE_MASK && !data.getBoolean(NBT_TRIAL_SYSTEM_MIGRATED)) {
            data.putBoolean(NBT_TRIAL_SYSTEM_MIGRATED, true);
            data.putBoolean(NBT_CURSES_REVERSED, false);
            CocoonTrialProgress.unlock(player);
            syncToClient(player);
            player.displayClientMessage(
                    ModText.literal("终焉试炼已开启")
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    false
            );
        }
    }

    private static ResourceLocation coreId(String path) {
        return new ResourceLocation(lastsongofelysian.MODID, path);
    }

    private static ResourceLocation itemId(String path) {
        return new ResourceLocation(lastsongofelysian.MODID, path);
    }

    public static boolean hasAcquiredCore(
            Player player,
            Item core
    ) {
        ResourceLocation coreId = ForgeRegistries.ITEMS.getKey(core);

        if (coreId == null) {
            return false;
        }

        for (int index = 0; index < 12; index++) {
            if (ALL_CORE_IDS[index].equals(coreId)) {
                return (getAcquiredCoreMask(player)
                        & (1 << index)) != 0
                        || containsItemId(player, coreId);
            }
        }

        return false;
    }

    public static void recordAcquiredCore(
            Player player,
            Item core
    ) {
        ResourceLocation coreId = ForgeRegistries.ITEMS.getKey(core);

        if (coreId == null) {
            return;
        }

        for (int index = 0; index < 12; index++) {
            if (!ALL_CORE_IDS[index].equals(coreId)) {
                continue;
            }

            CompoundTag data = getData(player);
            int previousMask = getAcquiredCoreMask(player);
            int mask = previousMask | (1 << index);
            data.putInt(
                    NBT_ACQUIRED_CORE_MASK,
                    mask
            );
            data.putInt(NBT_CORE_STAGE, Integer.bitCount(mask & NON_ORIGIN_CORE_MASK));
            clearCorrosionCurseIfNeeded(player, data, mask);
            if (mask != previousMask) {
                announceCoreAcquired(player, CoreEntry.byStage(index + 1));
                syncToClient(player);
            }
            return;
        }
    }

    private static void announceCoreAcquired(Player player, CoreEntry core) {
        if (core == null) {
            return;
        }

        player.displayClientMessage(
                ModText.literal("已获得 ")
                        .append(ModText.literal(core.displayName)
                                .withStyle(core.color, ChatFormatting.BOLD))
                        .append(ModText.literal(
                                "：终焉强度提高至 ×"
                                        + String.format(Locale.ROOT, "%.1f", getMultiplier(player))
                        ).withStyle(ChatFormatting.RED)),
                false
        );

        if (core == CoreEntry.CORROSION) {
            CorrosionCurseEvents.syncToClient(player);
        } else {
            player.displayClientMessage(
                    ModText.literal(core.displayName + "对应的诅咒已开启。")
                            .withStyle(ChatFormatting.DARK_RED),
                    false
            );
        }
    }

    private static void clearCorrosionCurseIfNeeded(
            Player player,
            CompoundTag data,
            int coreMask
    ) {
        int corrosionBit = 1 << (CORROSION_STAGE - 1);
        if ((coreMask & corrosionBit) == 0
                || data.getBoolean(NBT_CORROSION_CURSE_CLEARED)) {
            return;
        }
        data.putBoolean(NBT_CORROSION_CURSE_CLEARED, true);
        SignetEffects.clearCorruption(player);
        CorrosionCurseEvents.clearCurseAfterTrial(player);
    }

    private static boolean containsItemId(
            Player player,
            ResourceLocation requiredId
    ) {
        for (
                int slot = 0;
                slot < player.getInventory().getContainerSize();
                slot++
        ) {
            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            ResourceLocation actualId =
                    ForgeRegistries.ITEMS.getKey(stack.getItem());

            if (requiredId.equals(actualId)) {
                return true;
            }
        }

        return CuriosApi.getCuriosHelper()
                .findFirstCurio(
                        player,
                        stack -> requiredId.equals(
                                ForgeRegistries.ITEMS.getKey(
                                        stack.getItem()
                                )
                        )
                )
                .isPresent();
    }

    public static void syncToClient(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> serverPlayer),
                new CocoonStageSyncPacket(
                        getStage(player),
                        getAcquiredCoreMask(player),
                        isReversed(player),
                        CocoonTrialProgress.isUnlocked(player),
                        CocoonTrialProgress.getCompletedMask(player),
                        CocoonTrialProgress.getThunderStrikes(player),
                        CocoonTrialProgress.getThunderKills(player),
                        CocoonTrialProgress.getEarthExplosions(player)
                )
        );
    }

    @SubscribeEvent
    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {
        syncToClient(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(
            PlayerEvent.PlayerRespawnEvent event
    ) {
        syncToClient(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        try {
            CompoundTag oldData = getData(event.getOriginal());
            CompoundTag newData = getData(event.getEntity());
            int oldMask = getAcquiredCoreMask(event.getOriginal());
            newData.putInt(NBT_ACQUIRED_CORE_MASK, oldMask);
            newData.putInt(NBT_CORE_STAGE, Integer.bitCount(oldMask & NON_ORIGIN_CORE_MASK));
            newData.putBoolean(NBT_UNORDERED_CORE_MIGRATED, true);
            newData.putInt(NBT_ACQUIRED_SIGNET_MASK, oldData.getInt(NBT_ACQUIRED_SIGNET_MASK));
            newData.putBoolean(NBT_CURSES_REVERSED, oldData.getBoolean(NBT_CURSES_REVERSED));
            newData.putBoolean(NBT_TRIAL_SYSTEM_MIGRATED, oldData.getBoolean(NBT_TRIAL_SYSTEM_MIGRATED));
            newData.putBoolean(
                    NBT_CORROSION_CURSE_CLEARED,
                    oldData.getBoolean(NBT_CORROSION_CURSE_CLEARED)
            );
            CocoonTrialProgress.copy(event.getOriginal(), event.getEntity());
        } catch (Throwable throwable) {
            System.err.println(
                    "[LastSongOfElysian] Failed to copy cocoon core progress:"
            );
            throwable.printStackTrace(System.err);
        }
    }
}
