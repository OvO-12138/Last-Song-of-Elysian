package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.CocoonTrialPalette;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CocoonTrialProgress {

    public static final int REASON = 0;
    public static final int VOID = 1;
    public static final int THUNDER = 2;
    public static final int WIND = 3;
    public static final int ICE = 4;
    public static final int DEATH = 5;
    public static final int FIRE = 6;
    public static final int SENTIENCE = 7;
    public static final int EARTH = 8;
    public static final int DOMINANCE = 9;
    public static final int BINDING = 10;
    public static final int CORROSION = 11;
    public static final int TRIAL_COUNT = 12;
    public static final int ALL_TRIALS_MASK = (1 << TRIAL_COUNT) - 1;

    private static final String NBT_PLAYER_PERSISTED = "PlayerPersisted";
    private static final String NBT_UNLOCKED = "CocoonTrialsUnlocked";
    private static final String NBT_COMPLETED = "CocoonCompletedTrialMask";
    private static final String NBT_REASON_CRAFTS = "CocoonTrialReasonCrafts";
    private static final String NBT_REASON_ENCHANTS = "CocoonTrialReasonEnchants";
    private static final String NBT_VOID_WINDOW = "CocoonTrialVoidWindow";
    private static final String NBT_VOID_KILLS = "CocoonTrialVoidKills";
    private static final String NBT_THUNDER_STRIKES = "CocoonTrialThunderStrikes";
    private static final String NBT_THUNDER_KILLS = "CocoonTrialThunderKills";
    private static final String NBT_THUNDER_PENDING = "CocoonTrialThunderPending";
    private static final String NBT_THUNDER_PENDING_TICK = "CocoonTrialThunderPendingTick";
    private static final String NBT_WIND_HEAL = "CocoonTrialWindHeal";
    private static final String NBT_WIND_EXTERNAL_HEAL = "CocoonTrialWindExternalHeal";
    private static final String NBT_ICE_TICKS = "CocoonTrialIceTicks";
    private static final String NBT_ICE_KILLS = "CocoonTrialIceKills";
    private static final String NBT_DEATH_TICKS = "CocoonTrialDeathTicks";
    private static final String NBT_DEATH_KILLS = "CocoonTrialDeathKills";
    private static final String NBT_FIRE_TICKS = "CocoonTrialFireTicks";
    private static final String NBT_FIRE_KILLS = "CocoonTrialFireKills";
    private static final String NBT_SENTIENCE_KILLS = "CocoonTrialSentienceKills";
    private static final String NBT_EARTH_EXPLOSIONS = "CocoonTrialEarthExplosions";
    private static final String NBT_EARTH_LAST_TICK = "CocoonTrialEarthLastTick";
    private static final String NBT_DOMINANCE_STARTED = "CocoonTrialDominanceStarted";
    private static final String NBT_DOMINANCE_LEVEL = "CocoonTrialDominanceLevel";
    private static final String NBT_DOMINANCE_INITIALIZED = "CocoonTrialDominanceInitialized";
    private static final String NBT_DOMINANCE_LAST_LEVEL = "CocoonTrialDominanceLastLevel";
    private static final String NBT_DOMINANCE_LEVELS_GAINED = "CocoonTrialDominanceLevelsGained";
    private static final String NBT_DOMINANCE_KILLS = "CocoonTrialDominanceKills";
    private static final String NBT_BINDING_BLOCKS = "CocoonTrialBindingBlocks";
    private static final String NBT_CORROSION_KILLS = "CocoonTrialCorrosionKills";

    private static final String[] NAMES = {
            "失序的真理", "虚空的漠视", "天罚的见证", "无风的嘶哑",
            "冻结的深渊", "死生的边界", "永燃的薪炎", "幻觉的彼岸",
            "坚韧的壁垒", "断裂的人偶", "束缚的行路", "认知的崩解"
    };

    private CocoonTrialProgress() {
    }

    private static CompoundTag getData(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(NBT_PLAYER_PERSISTED, Tag.TAG_COMPOUND)) {
            root.put(NBT_PLAYER_PERSISTED, new CompoundTag());
        }
        return root.getCompound(NBT_PLAYER_PERSISTED);
    }

    public static boolean isUnlocked(Player player) {
        return getData(player).getBoolean(NBT_UNLOCKED);
    }

    public static void unlock(Player player) {
        getData(player).putBoolean(NBT_UNLOCKED, true);
    }

    public static int getCompletedMask(Player player) {
        return getData(player).getInt(NBT_COMPLETED) & ALL_TRIALS_MASK;
    }

    public static boolean isComplete(Player player, int trial) {
        return trial >= 0 && trial < TRIAL_COUNT
                && (getCompletedMask(player) & (1 << trial)) != 0;
    }

    public static boolean hasAnyIncompleteTrial(Player player) {
        return getCompletedMask(player) != ALL_TRIALS_MASK;
    }

    public static int completedCount(Player player) {
        return Integer.bitCount(getCompletedMask(player));
    }

    public static int getThunderStrikes(Player player) {
        return getData(player).getInt(NBT_THUNDER_STRIKES);
    }

    public static int getThunderKills(Player player) {
        return getData(player).getInt(NBT_THUNDER_KILLS);
    }

    public static int getEarthExplosions(Player player) {
        return getData(player).getInt(NBT_EARTH_EXPLOSIONS);
    }

    public static void markThunderLightningPending(Player player) {
        if (!canProgress(player, THUNDER)) return;
        CompoundTag data = getData(player);
        data.putBoolean(NBT_THUNDER_PENDING, true);
        data.putLong(NBT_THUNDER_PENDING_TICK, player.level().getGameTime());
    }

    public static void copy(Player original, Player target) {
        CompoundTag oldData = getData(original);
        CompoundTag newData = getData(target);
        String[] keys = {
                NBT_UNLOCKED, NBT_COMPLETED, NBT_REASON_CRAFTS, NBT_REASON_ENCHANTS,
                NBT_VOID_WINDOW, NBT_VOID_KILLS, NBT_THUNDER_STRIKES, NBT_THUNDER_KILLS,
                NBT_THUNDER_PENDING, NBT_THUNDER_PENDING_TICK,
                NBT_WIND_HEAL, NBT_WIND_EXTERNAL_HEAL, NBT_ICE_TICKS, NBT_ICE_KILLS, NBT_DEATH_TICKS,
                NBT_DEATH_KILLS, NBT_FIRE_TICKS, NBT_FIRE_KILLS, NBT_SENTIENCE_KILLS,
                NBT_EARTH_EXPLOSIONS, NBT_EARTH_LAST_TICK, NBT_DOMINANCE_STARTED,
                NBT_DOMINANCE_LEVEL, NBT_DOMINANCE_INITIALIZED, NBT_DOMINANCE_LAST_LEVEL,
                NBT_DOMINANCE_LEVELS_GAINED, NBT_DOMINANCE_KILLS, NBT_BINDING_BLOCKS,
                NBT_CORROSION_KILLS
        };
        for (String key : keys) {
            if (oldData.contains(key)) newData.put(key, oldData.get(key).copy());
        }
    }

    private static boolean canProgress(Player player, int trial) {
        return player != null && !player.level().isClientSide() && player.isAlive()
                && !player.isCreative() && !player.isSpectator()
                && isUnlocked(player) && !isComplete(player, trial)
                && (trial != CORROSION || CocoonCoreProgress.hasAcquiredCore(
                player, ModItems.CORE_OF_CORROSION.get()))
                && !FenghuangDownSystem.isCocoonConnectionSevered(player)
                && CuriosApi.getCuriosHelper().findFirstCurio(
                player, stack -> stack.is(ModItems.COCOON_OF_FINALITY.get())).isPresent();
    }

    private static void complete(Player player, int trial) {
        if (!canProgress(player, trial)) return;
        CompoundTag data = getData(player);
        int mask = getCompletedMask(player) | (1 << trial);
        data.putInt(NBT_COMPLETED, mask);
        player.displayClientMessage(
                ModText.literal("试炼完成：" + NAMES[trial])
                        .withStyle(CocoonTrialPalette.color(trial), ChatFormatting.BOLD), false);

        if (trial == CORROSION) {
            SignetEffects.clearCorruption(player);
            CorrosionCurseEvents.clearCurseAfterTrial(player);
        }

        if (mask == ALL_TRIALS_MASK) {
            CocoonCoreProgress.setReversed(player, true);
            SignetEffects.clearCorruption(player);
            player.displayClientMessage(
                    ModText.literal("以人类的意志，拥抱明天。")
                            .withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.displayClientMessage(
                    ModText.literal("终焉之茧诅咒均已反转")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        }
        CocoonCoreProgress.syncToClient(player);
    }

    public static void completeAll(Player player) {
        CompoundTag data = getData(player);
        data.putBoolean(NBT_UNLOCKED, true);
        data.putInt(NBT_COMPLETED, ALL_TRIALS_MASK);
        data.putInt(NBT_DOMINANCE_LEVELS_GAINED, 30);
        data.putBoolean(NBT_DOMINANCE_LEVEL, true);
        CocoonCoreProgress.setReversed(player, true);
        SignetEffects.clearCorruption(player);
        CorrosionCurseEvents.clearCurseAfterTrial(player);
        CocoonCoreProgress.syncToClient(player);
    }

    public static void recordSuccessfulCraft(Player player) {
        if (!canProgress(player, REASON)) return;
        CompoundTag data = getData(player);
        data.putInt(NBT_REASON_CRAFTS, data.getInt(NBT_REASON_CRAFTS) + 1);
        checkReason(player);
    }

    public static void recordHighestEnchant(Player player) {
        if (!canProgress(player, REASON)) return;
        CompoundTag data = getData(player);
        data.putInt(NBT_REASON_ENCHANTS, data.getInt(NBT_REASON_ENCHANTS) + 1);
        checkReason(player);
    }

    private static void checkReason(Player player) {
        CompoundTag data = getData(player);
        if (data.getInt(NBT_REASON_CRAFTS) >= 50 && data.getInt(NBT_REASON_ENCHANTS) >= 5) {
            complete(player, REASON);
        }
    }

    public static void recordVoidTeleport(Player player) {
        if (canProgress(player, VOID)) {
            getData(player).putLong(NBT_VOID_WINDOW, player.level().getGameTime() + 600L);
        }
    }

    public static void recordVoidSummonKill(Player player) {
        if (!canProgress(player, VOID)
                || player.level().getGameTime() > getData(player).getLong(NBT_VOID_WINDOW)) return;
        CompoundTag data = getData(player);
        int kills = data.getInt(NBT_VOID_KILLS) + 1;
        data.putInt(NBT_VOID_KILLS, kills);
        data.remove(NBT_VOID_WINDOW);
        if (kills >= 10) complete(player, VOID);
    }

    public static void recordWindHealing(Player player, float amount) {
        if (!canProgress(player, WIND) || amount <= 0.0F) return;
        CompoundTag data = getData(player);
        float healed = data.getFloat(NBT_WIND_HEAL) + amount;
        data.putFloat(NBT_WIND_HEAL, healed);
        if (healed >= 200.0F) complete(player, WIND);
    }

    public static boolean allowsWindExternalHealing(Player player) {
        return player.hasEffect(MobEffects.REGENERATION)
                || player.level().getGameTime() <= getData(player).getLong(NBT_WIND_EXTERNAL_HEAL);
    }

    @SubscribeEvent
    public static void onUseHealingItem(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player) || !canProgress(player, WIND)) return;
        ItemStack stack = event.getItem();
        if (stack.getItem() instanceof PotionItem || stack.is(Items.GOLDEN_APPLE)
                || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            getData(player).putLong(
                    NBT_WIND_EXTERNAL_HEAL,
                    player.level().getGameTime() + Math.max(40, event.getDuration() + 20L)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) return;
        Player player = event.player;
        CompoundTag data = getData(player);
        if (data.getBoolean(NBT_THUNDER_PENDING)
                && player.level().getGameTime() - data.getLong(NBT_THUNDER_PENDING_TICK) > 3L) {
            data.putBoolean(NBT_THUNDER_PENDING, false);
        }
        if (!isUnlocked(player) || player.isCreative() || player.isSpectator()) return;

        if (canProgress(player, ICE) && player.isUnderWater()
                && !player.hasEffect(MobEffects.WATER_BREATHING)
                && player.level().getBiome(player.blockPosition()).value().getBaseTemperature() <= 0.15F) {
            data.putInt(NBT_ICE_TICKS, data.getInt(NBT_ICE_TICKS) + 1);
            if (data.getInt(NBT_ICE_TICKS) >= 2400 && data.getInt(NBT_ICE_KILLS) >= 20) {
                complete(player, ICE);
            }
        }

        if (canProgress(player, DEATH) && player.getHealth() <= player.getMaxHealth() * 0.20F) {
            data.putInt(NBT_DEATH_TICKS, data.getInt(NBT_DEATH_TICKS) + 1);
            if (data.getInt(NBT_DEATH_TICKS) >= 7200 && data.getInt(NBT_DEATH_KILLS) >= 20) {
                complete(player, DEATH);
            }
        }

        if (canProgress(player, FIRE) && player.isOnFire()) {
            data.putInt(NBT_FIRE_TICKS, data.getInt(NBT_FIRE_TICKS) + 1);
            if (data.getInt(NBT_FIRE_TICKS) >= 1800 && data.getInt(NBT_FIRE_KILLS) >= 20) {
                complete(player, FIRE);
            }
        }

        if (canProgress(player, EARTH) && data.getInt(NBT_EARTH_EXPLOSIONS) > 0 && !hasFullArmor(player)) {
            data.putInt(NBT_EARTH_EXPLOSIONS, 0);
            CocoonCoreProgress.syncToClient(player);
        }

        if (canProgress(player, DOMINANCE)) {
            if (!data.getBoolean(NBT_DOMINANCE_INITIALIZED)) {
                data.putBoolean(NBT_DOMINANCE_INITIALIZED, true);
                data.putInt(NBT_DOMINANCE_LAST_LEVEL, player.experienceLevel);
                if (data.getBoolean(NBT_DOMINANCE_LEVEL)) {
                    data.putInt(NBT_DOMINANCE_LEVELS_GAINED, 30);
                }
            } else {
                int lastLevel = data.getInt(NBT_DOMINANCE_LAST_LEVEL);
                int gained = Math.max(0, player.experienceLevel - lastLevel);
                if (gained > 0) {
                    data.putInt(
                            NBT_DOMINANCE_LEVELS_GAINED,
                            data.getInt(NBT_DOMINANCE_LEVELS_GAINED) + gained
                    );
                }
                data.putInt(NBT_DOMINANCE_LAST_LEVEL, player.experienceLevel);
            }
            if (data.getInt(NBT_DOMINANCE_LEVELS_GAINED) >= 30) {
                data.putBoolean(NBT_DOMINANCE_LEVEL, true);
            }
            if (data.getBoolean(NBT_DOMINANCE_LEVEL) && data.getInt(NBT_DOMINANCE_KILLS) >= 100) {
                complete(player, DOMINANCE);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getAmount() <= 0.0F) return;
        CompoundTag data = getData(player);

        if (canProgress(player, THUNDER) && isOwnedCurseLightning(event.getSource(), player, data)) {
            data.putBoolean(NBT_THUNDER_PENDING, false);
            if (player.getHealth() + player.getAbsorptionAmount() > event.getAmount()) {
                int strikes = data.getInt(NBT_THUNDER_STRIKES) + 1;
                data.putInt(NBT_THUNDER_STRIKES, strikes);
                if (strikes >= 5 && data.getInt(NBT_THUNDER_KILLS) >= 20) {
                    complete(player, THUNDER);
                } else {
                    CocoonCoreProgress.syncToClient(player);
                }
            }
        }

        if (canProgress(player, EARTH) && event.getSource().is(DamageTypeTags.IS_EXPLOSION)
                && hasFullArmor(player) && data.getLong(NBT_EARTH_LAST_TICK) != player.level().getGameTime()) {
            data.putLong(NBT_EARTH_LAST_TICK, player.level().getGameTime());
            int explosions = data.getInt(NBT_EARTH_EXPLOSIONS) + 1;
            data.putInt(NBT_EARTH_EXPLOSIONS, explosions);
            if (explosions >= 10) {
                complete(player, EARTH);
            } else {
                CocoonCoreProgress.syncToClient(player);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || !(event.getEntity() instanceof Enemy)) return;

        if (CocoonCurseEvents.isOwnVoidSummon(event.getEntity(), player)) {
            recordVoidSummonKill(player);
        }

        CompoundTag data = getData(player);
        if (canProgress(player, THUNDER) && player.level().isThundering()) {
            int kills = data.getInt(NBT_THUNDER_KILLS) + 1;
            data.putInt(NBT_THUNDER_KILLS, kills);
            if (data.getInt(NBT_THUNDER_STRIKES) >= 5 && data.getInt(NBT_THUNDER_KILLS) >= 20) {
                complete(player, THUNDER);
            } else {
                CocoonCoreProgress.syncToClient(player);
            }
        }
        if (canProgress(player, ICE) && event.getEntity() instanceof Drowned) {
            data.putInt(NBT_ICE_KILLS, data.getInt(NBT_ICE_KILLS) + 1);
        }
        if (canProgress(player, DEATH) && player.getHealth() <= player.getMaxHealth() * 0.20F) {
            data.putInt(NBT_DEATH_KILLS, data.getInt(NBT_DEATH_KILLS) + 1);
        }
        if (canProgress(player, FIRE) && event.getEntity().isOnFire()) {
            data.putInt(NBT_FIRE_KILLS, data.getInt(NBT_FIRE_KILLS) + 1);
        }
        if (canProgress(player, SENTIENCE) && (player.hasEffect(MobEffects.CONFUSION)
                || player.hasEffect(MobEffects.BLINDNESS) || player.hasEffect(MobEffects.DARKNESS))) {
            int kills = data.getInt(NBT_SENTIENCE_KILLS) + 1;
            data.putInt(NBT_SENTIENCE_KILLS, kills);
            if (kills >= 25) complete(player, SENTIENCE);
        }
        if (canProgress(player, DOMINANCE)) {
            data.putInt(NBT_DOMINANCE_KILLS, data.getInt(NBT_DOMINANCE_KILLS) + 1);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCorrosionTrialDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof Player deadPlayer) {
            if (!isComplete(deadPlayer, CORROSION)) {
                getData(deadPlayer).putInt(NBT_CORROSION_KILLS, 0);
            }
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player)
                || !(event.getEntity() instanceof Enemy)
                || PlayerControlledAllies.isProtectedEffectTarget(player, event.getEntity())
                || !canProgress(player, CORROSION)) return;

        CompoundTag data = getData(player);
        int kills = data.getInt(NBT_CORROSION_KILLS) + 1;
        data.putInt(NBT_CORROSION_KILLS, kills);
        if (kills >= 30) complete(player, CORROSION);
    }

    @SubscribeEvent
    public static void onTotem(LivingUseTotemEvent event) {
        if (event.getEntity() instanceof Player player && canProgress(player, DEATH)) {
            getData(player).putInt(NBT_DEATH_TICKS, 0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!canProgress(player, BINDING) || event.isCanceled()
                || !player.hasEffect(MobEffects.DIG_SLOWDOWN)
                || player.hasEffect(MobEffects.DIG_SPEED)
                || !event.getState().is(BlockTags.MINEABLE_WITH_PICKAXE)) return;
        CompoundTag data = getData(player);
        int blocks = data.getInt(NBT_BINDING_BLOCKS) + 1;
        data.putInt(NBT_BINDING_BLOCKS, blocks);
        if (blocks >= 128) complete(player, BINDING);
    }

    private static boolean hasFullArmor(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)
                    || armor.getEquipmentSlot() != slot) return false;
        }
        return true;
    }

    private static boolean isOwnedCurseLightning(DamageSource source, Player player, CompoundTag data) {
        if (isOwnedCurseLightningEntity(source.getDirectEntity(), player)
                || isOwnedCurseLightningEntity(source.getEntity(), player)) {
            return true;
        }
        if (!source.is(DamageTypes.LIGHTNING_BOLT) || !data.getBoolean(NBT_THUNDER_PENDING)) {
            return false;
        }
        long pendingTick = data.getLong(NBT_THUNDER_PENDING_TICK);
        long age = player.level().getGameTime() - pendingTick;
        return age >= 0L && age <= 3L;
    }

    private static boolean isOwnedCurseLightningEntity(net.minecraft.world.entity.Entity entity, Player player) {
        if (entity == null) return false;
        CompoundTag entityData = entity.getPersistentData();
        return entityData.getBoolean(CocoonCurseEvents.NBT_CURSE_LIGHTNING)
                && entityData.hasUUID(CocoonCurseEvents.NBT_CURSE_LIGHTNING_OWNER)
                && entityData.getUUID(CocoonCurseEvents.NBT_CURSE_LIGHTNING_OWNER).equals(player.getUUID());
    }
}
