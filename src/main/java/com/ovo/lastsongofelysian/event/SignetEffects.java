package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.network.ComboSyncPacket;
import com.ovo.lastsongofelysian.network.CorruptionSyncPacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import com.ovo.lastsongofelysian.network.PreceptSyncPacket;
import com.ovo.lastsongofelysian.network.SakuraDodgeEffectPacket;
import com.ovo.lastsongofelysian.registry.ModEffects;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.EgoSignetAmplification;
import com.ovo.lastsongofelysian.util.FenghuangDownEnergy;
import com.ovo.lastsongofelysian.util.AbyssFlowerEnergy;
import com.ovo.lastsongofelysian.util.FlawlessWeaponEnergy;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.SkyfireGreatswordEnergy;
import com.ovo.lastsongofelysian.util.EdenStarEnergy;
import com.ovo.lastsongofelysian.util.ElysianHeroExplosionProtection;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.util.SignetUpgradeData.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import com.ovo.lastsongofelysian.event.CocoonCoreProgress;

import java.util.*;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SignetEffects {

    private static final Random RAND = new Random();


    private static final ThreadLocal<Deque<PenetrationContext>> PENETRATION_CONTEXTS =
            ThreadLocal.withInitial(ArrayDeque::new);

    private static final class PenetrationContext {
        private final LivingEntity target;
        private final DamageSource source;
        private float penetration;

        private PenetrationContext(LivingEntity target, DamageSource source) {
            this.target = target;
            this.source = source;
        }

        private boolean matches(LivingEntity target, DamageSource source) {
            return this.target == target && this.source == source;
        }
    }

    @SubscribeEvent
    public static void onMobKill(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof Enemy)) {
            return;
        }

        int amount = rollWithLooting(event.getLootingLevel());

        if (amount <= 0) {
            return;
        }

        if (hasItem(player, ModItems.SIGNET_OF_REVERIE.get())) {
            int bonus = Math.max(1, (int) Math.ceil(amount
                    * signetBoost(player, Skill.REVERIE_SILVER, 0.10F)
                    * ReverieSignetSystem.ordinaryBonusMultiplier(player)));
            amount = Math.min(10, amount + bonus);
        }

        ItemStack drop = new ItemStack(
                ModItems.SHINY_SILVER.get(),
                amount
        );

        ItemEntity entity = new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY() + 0.5D,
                event.getEntity().getZ(),
                drop
        );

        entity.setDefaultPickUpDelay();
        event.getDrops().add(entity);
    }

    private static int rollWithLooting(int looting) {

        int best = RAND.nextInt(11);

        for (int i = 0; i < looting; i++) {
            int roll = RAND.nextInt(11);

            if (roll > best) {
                best = roll;
            }
        }

        return best;
    }
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        if (!name.getNamespace().equals("minecraft")) return;
        if (!name.getPath().startsWith("chests/")) return;
        LootPool pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.SHINY_SILVER.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 10f)))
                        .when(LootItemRandomChanceCondition.randomChance(0.40f)))
                .build();
        event.getTable().addPool(pool);
    }

    @SubscribeEvent
    public static void onInkDamage(LivingDamageEvent event) {
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        LivingEntity target = event.getEntity();
        if (PlayerControlledAllies.isElysianHero(target)) return;
        MobEffectInstance redInk = target.getEffect(ModEffects.RED_INK.get());
        if (redInk != null) {
            float bonus = 0.10F * SignetUpgradeData.factorForLevel(redInk.getAmplifier());
            event.setAmount(event.getAmount() * (1.0F + bonus));
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            MobEffectInstance blueInk = attacker.getEffect(ModEffects.BLUE_INK.get());
            if (blueInk != null) {
                float reduction = Math.min(0.95F,
                        0.10F * SignetUpgradeData.factorForLevel(blueInk.getAmplifier()));
                event.setAmount(applyIndependentReduction(event.getAmount(), reduction));
            }
        }
    }

    private static PenetrationContext findPenetrationContext(
            LivingEntity target,
            DamageSource source
    ) {
        for (PenetrationContext context : PENETRATION_CONTEXTS.get()) {
            if (context.matches(target, source)) return context;
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void beginPenetrationContext(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        PENETRATION_CONTEXTS.get().push(
                new PenetrationContext(event.getEntity(), event.getSource())
        );
    }

    private static void discardPenetrationContext(LivingEntity target, DamageSource source) {
        Deque<PenetrationContext> contexts = PENETRATION_CONTEXTS.get();
        Iterator<PenetrationContext> iterator = contexts.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().matches(target, source)) {
                iterator.remove();
                break;
            }
        }
        if (contexts.isEmpty()) PENETRATION_CONTEXTS.remove();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void discardCanceledPenetrationContext(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F) {
            discardPenetrationContext(event.getEntity(), event.getSource());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void finishPenetrationContext(LivingDamageEvent event) {
        discardPenetrationContext(event.getEntity(), event.getSource());
    }

    public static boolean hasActivePenetration(LivingEntity target, DamageSource source) {
        PenetrationContext context = findPenetrationContext(target, source);
        return context != null && context.penetration > 0.0F;
    }

    public static float activePenetration(LivingEntity target, DamageSource source) {
        PenetrationContext context = findPenetrationContext(target, source);
        return context == null ? 0.0F : context.penetration;
    }

    @SubscribeEvent
    public static void onReverieExperience(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || event.getAmount() <= 0
                || !hasItem(player, ModItems.SIGNET_OF_REVERIE.get())) return;
        int bonus = Math.max(1, Math.round(event.getAmount()
                * signetBoost(player, Skill.REVERIE_EXPERIENCE, 1.0F)
                * ReverieSignetSystem.ordinaryBonusMultiplier(player)));
        event.setAmount(event.getAmount() + bonus);
    }

    private static final int MAX_STACKS = 3;
    private static final Map<UUID, Integer> strengthNoHitTicks = new HashMap<>();
    private static final Map<UUID, Integer> resistanceNoHitTicks = new HashMap<>();
    private static final Map<UUID, Integer> strengthStacks = new HashMap<>();
    private static final Map<UUID, Integer> resistanceStacks = new HashMap<>();
    private static final UUID VICISSITUDE_STRENGTH_UUID =
            UUID.fromString("45da0689-c933-47e8-92bd-cf9153c7c788");

    private static final UUID TUOLIN_MOD_UUID =
            UUID.fromString("a8e56c7d-c4f2-4d3e-8b1a-ef0123456789");

    private static final String NBT_TUOLIN_STACKS = "TuoLinStacks";
    private static final String NBT_TUOLIN_TICKS = "TuoLinTicks";
    private static final String NBT_TUOLIN_BASE_MAX = "TuoLinBaseMax";

    private static final int TUOLIN_DURATION = 3600;
    private static final double TUOLIN_FINAL_DEATH_THRESHOLD_RATIO = 0.01;

    private static final ResourceLocation TOUHOU_MAID_ENTITY_ID =
            new ResourceLocation("touhou_little_maid", "maid");

    private static final int LACERATION_DURATION = 100, LACERATION_MAX_STACKS = 10;
    private static final String NBT_LACERATION_POWER = "LSELacerationPower";

    private static final Map<UUID, Long> EVADE_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Long> EVADE_BUFF_END = new HashMap<>();
    private static final int EVADE_COOLDOWN_TICKS = 400, EVADE_BUFF_DURATION = 160;
    private static final UUID SETSUNA_ATK_SPEED_UUID = UUID.fromString("b8f4a2d1-c3e5-4a7f-9d1c-2b6a8e0f7d3a");
    private static final UUID SETSUNA_MOV_SPEED_UUID = UUID.fromString("c7d3e2f1-a5b6-4c8d-9e0f-1a2b3c4d5e6f");

    private static final UUID DECIMATION_HEALTH_UUID = UUID.fromString("e4a1f2b3-c5d6-4e7f-8a9b-0c1d2e3f4a5b");
    private static final double DECIMATION_DEF_CAP = 0.20;
    private static final double DECIMATION_DEF_MAX = 0.30;

    private static final Map<UUID, Integer> comboCount = new HashMap<>(), comboResetTimer = new HashMap<>();
    private static final Map<UUID, Integer> bodhiDmgTicks = new HashMap<>(), bodhiDefTicks = new HashMap<>();

    private static final Map<UUID, Integer> helixMagicStacks = new HashMap<>();
    private static final Map<UUID, Long> helixMagicTimer = new HashMap<>();
    private static final Map<UUID, Long> helixPendulumTimer = new HashMap<>();
    private static final Map<UUID, Float> helixPendulumPower = new HashMap<>();
    private static final int HELIX_MAGIC_DURATION_TICKS = 200, HELIX_MAGIC_MAX_STACKS = 4;

    public static final double GOLD_DAMAGE_PER_ENERGY = 0.005, GOLD_MAX_DAMAGE_BONUS = 0.65;

    private static final Map<UUID, Integer> preceptCount = new HashMap<>();
    private static final int PRECEPT_MAX = 100;
    private static final double PRECEPT_DAMAGE_BONUS = 0.003, PRECEPT_DEFENSE_BONUS = 0.0015;
    private static final float PRECEPT_DEFENSE_CAP = 0.30F;
    private static final Map<UUID, Long> disciplineUltimateEnd = new HashMap<>();
    private static final Map<UUID, Long> disciplineUltimatePenetrationEnd = new HashMap<>();
    private static final Map<UUID, Long> disciplineUnyieldingEnd = new HashMap<>();
    private static final Map<UUID, Long> disciplineMountainNext = new HashMap<>();
    private static final Map<UUID, Long> disciplineMountainDamageEnd = new HashMap<>();
    private static final Map<UUID, Float> disciplineMountainDamageBonus = new HashMap<>();
    private static final Map<UUID, Map<UUID, Integer>> disciplineMountainHits = new HashMap<>();
    private static final Map<UUID, Long> disciplineVirtuePenetrationEnd = new HashMap<>();
    private static final Map<UUID, Float> disciplineVirtuePenetration = new HashMap<>();
    private static final Map<UUID, Long> disciplinePunishmentEnd = new HashMap<>();
    private static final Map<UUID, Long> disciplineRetaliationNext = new HashMap<>();
    private static final Set<UUID> disciplineRetaliationGuard = new HashSet<>();
    private static final Map<UUID, Long> disciplineFalseSpeechEnd = new HashMap<>();
    private static final Map<UUID, Float> disciplineFalseSpeechPower = new HashMap<>();

    public static int getPreceptCount(Player player) {
        return player == null ? 0 : preceptCount.getOrDefault(player.getUUID(), 0);
    }

    private static final Map<UUID, Long> deliveranceBuffEnd = new HashMap<>();
    private static final UUID DELIVERANCE_ARMOR_UUID = UUID.fromString("d1a2b3c4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final int DELIVERANCE_SECONDARY_DURATION_TICKS = 140;
    private static final Map<UUID, Long> deliveranceHuntersMaskEnd = new HashMap<>();
    private static final Map<UUID, Long> deliveranceHolyShieldEnd = new HashMap<>();
    private static final Map<UUID, Float> deliveranceHolyShieldAmount = new HashMap<>();
    private static final Map<UUID, Long> deliveranceSeekersRobeEnd = new HashMap<>();
    private static final Map<UUID, Long> deliveranceSeekersRobeNextPulse = new HashMap<>();
    private static final Set<UUID> deliveranceFenghuangEnergyTargets = new HashSet<>();
    private static final Set<UUID> deliveranceAbyssEnergyTargets = new HashSet<>();
    private static final Set<UUID> deliveranceSkyfireEnergyTargets = new HashSet<>();
    private static final Set<UUID> deliveranceEdenEnergyTargets = new HashSet<>();
    private static final UUID DELIVERANCE_ANTI_STAGGER_UUID =
            UUID.fromString("861f501d-9f9a-4b7d-bb8c-0eb34b74029a");
    private static final UUID DELIVERANCE_ULTIMATE_BUFF_UUID =
            UUID.fromString("f9a1b2c3-d4e5-6f7a-8b9c-0d1e2f3a4b5c");
    private static final Map<UUID, Long> deliveranceFinalBattleEnd = new HashMap<>();
    private static final Map<UUID, Map<UUID, Float>> deliveranceFinalBattleDamage = new HashMap<>();
    private static final Map<UUID, Long> deliveranceLoneHitWindowEnd = new HashMap<>();
    private static final Map<UUID, Integer> deliveranceLoneHitCount = new HashMap<>();
    private static final Map<UUID, Long> deliveranceSaviorBattleEnd = new HashMap<>();
    private static final Map<UUID, Float> deliveranceDecisionDamageBonus = new HashMap<>();
    private static final Map<UUID, Long> deliveranceTriumphIgnoreDefenseEnd = new HashMap<>();
    private static final Set<UUID> deliveranceTriumphTriggered = new HashSet<>();
    private static final Map<UUID, Map<UUID, DeliveranceTriumphVulnerability>>
            deliveranceTriumphVulnerabilities = new HashMap<>();
    private static final Set<UUID> deliveranceTriumphOwners = new HashSet<>();
    private static final Set<UUID> deliveranceFinalBurstGuard = new HashSet<>();

    private record DeliveranceTriumphVulnerability(float amount, long end) {
    }

    private static final UUID EGO_SPEED_UUID = UUID.fromString("e1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6d");
    private static final UUID EGO_ATTACK_UUID = UUID.fromString("f1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6e");
    private static final UUID EGO_HEALTH_UUID = UUID.fromString("a1b2c3d4-e5f6-7f8a-9b0c-1d2e3f4a5c6f");
    private static final UUID EGO_VICISSITUDE_KNOCKBACK_UUID =
            UUID.fromString("5ccdf6a4-92a7-4f9f-a6f5-67e76e409f44");
    private static final Map<UUID, Integer> egoFlightTicks = new HashMap<>();
    private static final int EGO_FLIGHT_MAX_TICKS = 40 * 20;

    private static final String NBT_EGO_GOLD_GRANTED_FLIGHT =
            "LSE_EgoGoldGrantedFlight";

    private static final Map<UUID, Float> corruptionEnergy = new HashMap<>();
    private static final float CORRUPTION_MAX = 10000.0f;
    private static final float CORRUPTION_PER_TICK = 1.0f / 20.0f;
    private static final String NBT_CORRUPTION = "CorruptionEnergy";
    private static final String NBT_CORRUPTION_RESISTANCE_TESTED = "CorruptionResistanceTested";
    private static final String NBT_CORRUPTION_RESISTANCE_RANK = "CorruptionResistanceRank";
    private static final String NBT_INNATE_CORRUPTION_CAPACITY = "InnateCorruptionCapacity";
    private static final float COCOON_CORRUPTION_CAPACITY_MULTIPLIER = 0.40f;
    private static final String NBT_HIDDEN_ATTRIBUTE_SLOT = "CocoonHiddenAttributeSlot";
    private static final int HIDDEN_ATTRIBUTE_SLOT_COUNT = 6;
    private static final Set<UUID> effectDurationRescaleGuard = new HashSet<>();
    private static final String NBT_PLAYER_PERSISTED = "PlayerPersisted";
    private static CompoundTag getPersistedData(Player player) {
        CompoundTag data = player.getPersistentData();

        if (!data.contains(NBT_PLAYER_PERSISTED)) {
            data.put(NBT_PLAYER_PERSISTED, new CompoundTag());
        }

        return data.getCompound(NBT_PLAYER_PERSISTED);
    }

    private static float loadCorruptionEnergy(Player player) {
        return getPersistedData(player).getFloat(NBT_CORRUPTION);
    }

    private static void saveCorruptionEnergy(Player player, float energy) {
        getPersistedData(player).putFloat(NBT_CORRUPTION, energy);
    }
    public static void initCorruptionResistanceTest(Player player) {
        if (player.level().isClientSide()) return;

        CompoundTag data = getPersistedData(player);

        if (data.getBoolean(NBT_CORRUPTION_RESISTANCE_TESTED)) {
            return;
        }

        data.putBoolean(NBT_CORRUPTION_RESISTANCE_TESTED, true);

        int roll = player.getRandom().nextInt(100000);

        String rank;
        float capacity;

        if (roll == 0) {

            rank = "E";
            capacity = 5000.0F;
        } else if (roll <= 10) {

            rank = "S";
            capacity = 100000.0F;
        } else if (roll <= 5010) {

            rank = "A";
            capacity = 12500.0F;
        } else if (roll <= 65000) {

            rank = "B";
            capacity = 10000.0F;
        } else if (roll <= 97000) {

            rank = "C";
            capacity = 7500.0F;
        } else {

            rank = "D";
            capacity = 5000.0F;
        }

        data.putString(NBT_CORRUPTION_RESISTANCE_RANK, rank);
        data.putFloat(NBT_INNATE_CORRUPTION_CAPACITY, capacity);

        ChatFormatting color = switch (rank) {
            case "E" -> ChatFormatting.DARK_RED;
            case "S" -> ChatFormatting.LIGHT_PURPLE;
            case "A" -> ChatFormatting.GOLD;
            case "B" -> ChatFormatting.AQUA;
            case "C" -> ChatFormatting.GREEN;
            case "D" -> ChatFormatting.GRAY;
            default -> ChatFormatting.WHITE;
        };

        player.displayClientMessage(
                ModText.literal("崩坏能抗性测试完成：评级 " + rank + "，崩坏能容量 " + Math.round(capacity))
                        .withStyle(color),
                false
        );

        if ("E".equals(rank)) {
            player.displayClientMessage(
                    ModText.literal("检测结果异常：崩坏能适应性极低，反应失控。")
                            .withStyle(ChatFormatting.DARK_RED),
                    false
            );

            ElysianHeroExplosionProtection.run(() -> player.level().explode(
                    player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    4.0F,
                    Level.ExplosionInteraction.MOB
            ));
        }
    }

    public static float getInnateCorruptionCapacity(Player player) {
        CompoundTag data = getPersistedData(player);

        if (!data.getBoolean(NBT_CORRUPTION_RESISTANCE_TESTED)) {
            return CORRUPTION_MAX;
        }

        float capacity = data.getFloat(NBT_INNATE_CORRUPTION_CAPACITY);

        if (capacity <= 0.0F) {
            return CORRUPTION_MAX;
        }

        return capacity;
    }

    private static final UUID COCOON_SPEED_UUID = UUID.fromString("c1a2b3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID COCOON_ATTACK_SPEED_UUID = UUID.fromString("d1a2b3c4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID COCOON_HEALTH_UUID = UUID.fromString("e1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5c6d");
    private static final UUID COCOON_ICE_SPEED_UUID = UUID.fromString("f1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5c6d");
    private static final UUID COCOON_DOMINANCE_ATTACK_UUID = UUID.fromString("12c64ad8-dc22-4e54-8343-b91d88fa22a1");
    private static final UUID COCOON_DOMINANCE_ARMOR_UUID = UUID.fromString("03e7b871-84ab-49d6-a535-3e25578a02fb");
    private static final String NBT_NEXT_COCOON_HALLUCINATION = "CocoonNextHallucination";

    private static final String NBT_GOT_SPAWN_INVINCIBILITY = "GotSpawnInvincibility", NBT_GOT_COCOON = "GotCocoon";
    private static final int SPAWN_INVINCIBILITY_DURATION = 2400;

    private static boolean isWearingCocoon(Player player) {
        return !FenghuangDownSystem.isCocoonConnectionSevered(player)
                && hasItem(player, ModItems.COCOON_OF_FINALITY.get());
    }

    private static boolean hasCocoon(Player player) {
        return isWearingCocoon(player) && !CocoonCoreProgress.isReversed(player);
    }

    private static boolean hasCorrosionCocoonCurse(Player player) {
        return isWearingCocoon(player)
                && !isCorrosionCleared(player);
    }

    private static boolean isCorrosionCleared(Player player) {
        return CocoonTrialProgress.isComplete(player, CocoonTrialProgress.CORROSION)
                || CocoonCoreProgress.hasAcquiredCore(player, ModItems.CORE_OF_CORROSION.get());
    }

    private static boolean hasItem(Player player, Item item) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(item))
                .isPresent();
    }

    private static boolean hasEgoAnd(Player player, Item item) {
        return EgoSignetAmplification.isEnabledFor(player, item);
    }

    private static boolean hasEgo(Player player) {
        return hasItem(player, ModItems.SIGNET_OF_EGO.get());
    }

    private static boolean hasFlawlessBenediction(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) {
                return true;
            }
        }
        return false;
    }

    private static float egoBoost(Player player, float baseValue) {
        if (hasFlawlessBenediction(player)) {
            return baseValue * 14.14F;
        }
        return hasEgo(player) ? baseValue * SignetUpgradeData.egoMultiplier(player) : baseValue;
    }

    private static double egoBoost(Player player, double baseValue) {
        if (hasFlawlessBenediction(player)) {
            return baseValue * 14.14D;
        }
        return hasEgo(player) ? baseValue * SignetUpgradeData.egoMultiplier(player) : baseValue;
    }

    private static float signetBoost(Player player, Skill skill, float baseValue) {
        float value = SignetUpgradeData.scaleBonus(player, skill, baseValue);
        if (skill.isDeliveranceOrdinary()) {
            value *= deliveranceOrdinaryMultiplier(player);
        }
        value = egoBoost(player, value);
        return isCombatSignet(skill)
                ? value * CorrosionCurseEvents.getCombatSignetMultiplier(player)
                : value;
    }

    public static float getScaledSignetValue(Player player, Skill skill, float baseValue) {
        return signetBoost(player, skill, baseValue);
    }

    private static double signetBoost(Player player, Skill skill, double baseValue) {
        double value = SignetUpgradeData.scaleBonus(player, skill, baseValue);
        if (skill.isDeliveranceOrdinary()) {
            value *= deliveranceOrdinaryMultiplier(player);
        }
        value = egoBoost(player, value);
        return isCombatSignet(skill)
                ? value * CorrosionCurseEvents.getCombatSignetMultiplier(player)
                : value;
    }

    private static boolean isCombatSignet(Skill skill) {
        return switch (skill) {
            case REVERIE_EXPERIENCE,
                    REVERIE_DISCOUNT,
                    REVERIE_SILVER,
                    HELIX_PARADOX,
                    GOLD_STREAM -> false;
            default -> true;
        };
    }

    private static float deliveranceOrdinaryMultiplier(Player player) {
        long gameTime = player.level().getGameTime();
        UUID uuid = player.getUUID();
        if (deliveranceFinalBattleEnd.getOrDefault(uuid, 0L) >= gameTime
                && SignetUpgradeData.getDeliveranceCore(player)
                == SignetUpgradeData.DeliveranceCore.KINGS_SWORD) {
            return 1.50F;
        }
        if (deliveranceSaviorBattleEnd.getOrDefault(uuid, 0L) >= gameTime
                && SignetUpgradeData.hasDeliveranceCoreSignet(
                player,
                Skill.DELIVERANCE_LONE_RESIDUAL_DREAM
        )) {
            return 1.0F + SignetUpgradeData.scaleBonus(
                    player,
                    Skill.DELIVERANCE_LONE_RESIDUAL_DREAM,
                    0.60F
            );
        }
        return 1.0F;
    }

    private static float signetBonusMultiplier(Player player, Skill skill, float baseBonus) {
        return 1.0F + signetBoost(player, skill, baseBonus);
    }

    private static float signetReductionMultiplier(Player player, Skill skill, float baseReduction) {
        return 1.0F - Math.min(0.95F, signetBoost(player, skill, baseReduction));
    }

    private static float applyIndependentReduction(float damage, double reduction) {
        return damage * (1.0F - (float) Math.min(0.95D, Math.max(0.0D, reduction)));
    }

    private static float signetChance(Player player, Skill skill, float baseChance) {
        return Math.min(1.0F, signetBoost(player, skill, baseChance));
    }

    private static int egoBoostCeil(Player player, int baseValue) {
        return Math.max(0, (int) Math.ceil(egoBoost(player, (float) baseValue)));
    }

    private static float egoChance(Player player, float baseChance) {
        return Math.min(1.0F, egoBoost(player, baseChance));
    }

    private static float egoBonusMultiplier(Player player, float baseBonus) {
        return 1.0F + egoBoost(player, baseBonus);
    }

    private static float egoReductionMultiplier(Player player, float baseReduction) {
        return 1.0F - Math.min(0.95F, egoBoost(player, baseReduction));
    }

    private static float compensateArmorForStarsIgnoreDefense(LivingEntity target, DamageSource source, float desiredDamage) {
        return applyArmorPenetration(target, source, desiredDamage, 1.0F);
    }

    private static float getDamageAfterArmorAbsorb(float damage, float armor, float toughness) {
        float toughnessFactor = 2.0F + toughness / 4.0F;
        float armorReduction = armor - damage / toughnessFactor;
        float minReduction = armor * 0.2F;
        armorReduction = Math.max(minReduction, armorReduction);
        armorReduction = Math.min(20.0F, armorReduction);
        return damage * (1.0F - armorReduction / 25.0F);
    }

    public static float getDamageAfterArmorWithPenetration(
            LivingEntity target,
            DamageSource source,
            float damage
    ) {
        if (!hasActivePenetration(target, source)
                || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return damage;
        }
        float penetration = activePenetration(target, source);
        float armor = target.getArmorValue() * (1.0F - penetration);
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        return getDamageAfterArmorAbsorb(damage, Math.max(0.0F, armor), toughness);
    }

    public static float getDamageAfterMagicWithPenetration(
            LivingEntity target,
            DamageSource source,
            float damage
    ) {
        if (!hasActivePenetration(target, source)
                || source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return damage;
        }

        float penetration = activePenetration(target, source);
        MobEffectInstance resistance = target.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (resistance != null && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            float reduction = Mth.clamp(
                    (resistance.getAmplifier() + 1) * 5.0F / 25.0F,
                    0.0F,
                    1.0F
            );
            damage *= 1.0F - reduction * (1.0F - penetration);
        }

        if (damage <= 0.0F || source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return Math.max(0.0F, damage);
        }
        int protection = EnchantmentHelper.getDamageProtection(
                target.getArmorSlots(), source
        );
        return protection > 0
                ? CombatRules.getDamageAfterMagicAbsorb(damage, protection)
                : damage;
    }

    private static CompoundTag getPermanentPlayerData(Player player) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(NBT_PLAYER_PERSISTED);
        root.put(NBT_PLAYER_PERSISTED, persisted);
        return persisted;
    }

    public static float getCorruptionEnergy(Player player) {
        if (isCorrosionCleared(player)) return 0.0F;
        return corruptionEnergy.computeIfAbsent(
                player.getUUID(),
                ignored -> loadCorruptionEnergy(player)
        );
    }

    public static float getCorruptionRatio(Player player) {
        float capacity = getCorruptionCapacity(player);
        if (capacity <= 0.0F) return 0.0F;
        return Math.max(0.0F, Math.min(1.0F, getCorruptionEnergy(player) / capacity));
    }

    public static float getCorruptionCapacity(Player player) {
        float capacity = getInnateCorruptionCapacity(player)
                * VicissitudeSignetSystem.corruptionCapacityMultiplier(player);

        if (!hasCorrosionCocoonCurse(player)) {
            return capacity;
        }

        float reduction = Math.min(
                0.90F,
                CocoonCoreProgress.scalePercent(player, 0.20F)
        );

        return Math.max(1.0F, capacity * (1.0F - reduction));
    }

    public static float getCorruptionUseMultiplier(Player player) {
        if (!hasCorrosionCocoonCurse(player)) {
            return 1.0F;
        }

        float reduction = Math.min(
                0.90F,
                CocoonCoreProgress.scalePercent(player, 0.20F)
        );

        return 1.0F - reduction;
    }

    public static float applyCorruptionGainMultiplier(
            Player player,
            float amount
    ) {
        return amount
                * getCorruptionGrowthMultiplier(
                player
        );
    }

    public static float getCorruptionGrowthMultiplier(
            Player player
    ) {
        if (!hasCorrosionCocoonCurse(player)) {
            return 1.0F;
        }

        return CocoonCoreProgress
                .getMultiplier(player);
    }

    public static boolean addCorruptionByPercent(
            Player player,
            float basePercent
    ) {
        if (
                player.level().isClientSide() ||
                basePercent <= 0.0F ||
                isCorrosionCleared(player)
        ) {
            return false;
        }

        UUID uuid = player.getUUID();

        float maxCorruption =
                getCorruptionCapacity(player);

        float current =
                corruptionEnergy.computeIfAbsent(
                        uuid,
                        ignored ->
                                loadCorruptionEnergy(
                                        player
                                )
                );

        float scaledPercent =
                basePercent
                        * getCorruptionGrowthMultiplier(
                        player
                );

        float gain =
                maxCorruption
                        * scaledPercent;

        float next =
                Math.min(
                        maxCorruption,
                        current + gain
                );

        if (next <= current) {
            return false;
        }

        corruptionEnergy.put(
                uuid,
                next
        );

        saveCorruptionEnergy(
                player,
                next
        );

        syncCorruptionToClient(
                player,
                next,
                maxCorruption
        );

        return true;
    }

    public static boolean reduceCorruptionByPercent(Player player, float percentOfMax) {
        if (player.level().isClientSide()
                || isCorrosionCleared(player)) return false;

        UUID uuid = player.getUUID();
        float maxCorruption = getCorruptionCapacity(player);
        float current = corruptionEnergy.computeIfAbsent(uuid, id -> loadCorruptionEnergy(player));

        if (current <= 0.0f) return false;

        float reduceAmount = maxCorruption * percentOfMax;
        float newEnergy = Math.max(0.0f, current - reduceAmount);
        corruptionEnergy.put(uuid, newEnergy);
        saveCorruptionEnergy(player, newEnergy);

        syncCorruptionToClient(
                player,
                newEnergy,
                maxCorruption
        );

        return true;
    }

    public static void clearCorruption(Player player) {
        if (player.level().isClientSide()) return;
        corruptionEnergy.put(player.getUUID(), 0.0F);
        saveCorruptionEnergy(player, 0.0F);
        syncCorruptionToClient(player, 0.0F, Math.max(1.0F, getInnateCorruptionCapacity(player)));
    }

    private static void syncCorruptionToClient(
            Player player,
            float energy,
            float maxCorruption
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int displayEnergy =
                (int) (
                        energy
                                / maxCorruption
                                * CORRUPTION_MAX
                );

        displayEnergy =
                Math.min(
                        (int) CORRUPTION_MAX,
                        Math.max(
                                0,
                                displayEnergy
                        )
                );

        int finalDisplayEnergy =
                displayEnergy;

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> serverPlayer
                ),
                new CorruptionSyncPacket(
                        finalDisplayEnergy
                )
        );
    }

    public static int getHiddenAttributeSlot(Player player) {
        return -1;
    }

    public static boolean hasGoldFurnaceBoost(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) return false;
        return !level.getEntitiesOfClass(Player.class, new net.minecraft.world.phys.AABB(pos).inflate(8.0),
                player -> player.isAlive() && hasItem(player, ModItems.SIGNET_OF_GOLD.get())).isEmpty();
    }

    @SubscribeEvent
    public static void onPlayerFirstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        corruptionEnergy.put(player.getUUID(), loadCorruptionEnergy(player));
        initCorruptionResistanceTest(player);

        CompoundTag root = player.getPersistentData();
        CompoundTag data = getPermanentPlayerData(player);
        if (root.getBoolean(NBT_GOT_SPAWN_INVINCIBILITY)) data.putBoolean(NBT_GOT_SPAWN_INVINCIBILITY, true);
        if (root.getBoolean(NBT_GOT_COCOON)) data.putBoolean(NBT_GOT_COCOON, true);

        if (!data.getBoolean(NBT_GOT_SPAWN_INVINCIBILITY)) {
            data.putBoolean(NBT_GOT_SPAWN_INVINCIBILITY, true);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, SPAWN_INVINCIBILITY_DURATION, 4, false, false));
        }
        if (!data.getBoolean(NBT_GOT_COCOON)) {
            data.putBoolean(NBT_GOT_COCOON, true);
            ItemStack cocoonStack = new ItemStack(ModItems.COCOON_OF_FINALITY.get());
            if (!player.addItem(cocoonStack)) player.drop(cocoonStack, false);
        }
    }

    private static void applyCocoonCurses(Player player) {

        double speedReduction = CocoonCoreProgress.scalePercent(player, 0.20F);
        double attackSpeedReduction = CocoonCoreProgress.scalePercent(player, 0.30F);
        double maxHealthReduction = CocoonCoreProgress.scalePercent(player, 0.25F);
        double attackDamageReduction = CocoonCoreProgress.scalePercent(player, 0.10F);
        double armorReduction = CocoonCoreProgress.scalePercent(player, 0.10F);

        setCocoonModifier(
                player.getAttribute(Attributes.MOVEMENT_SPEED),
                COCOON_SPEED_UUID,
                "cocoon_speed",
                -speedReduction
        );
        setCocoonModifier(
                player.getAttribute(Attributes.ATTACK_SPEED),
                COCOON_ATTACK_SPEED_UUID,
                "cocoon_attack_speed",
                -attackSpeedReduction
        );

        setCocoonModifier(
                player.getAttribute(Attributes.MAX_HEALTH),
                COCOON_HEALTH_UUID,
                "cocoon_health",
                -maxHealthReduction
        );
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));

        setCocoonModifier(
                player.getAttribute(Attributes.ATTACK_DAMAGE),
                COCOON_DOMINANCE_ATTACK_UUID,
                "cocoon_dominance_attack",
                -attackDamageReduction
        );
        setCocoonModifier(
                player.getAttribute(Attributes.ARMOR),
                COCOON_DOMINANCE_ARMOR_UUID,
                "cocoon_dominance_armor",
                -armorReduction
        );

        AttributeInstance iceSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (isColdBiome(player)) {
            setCocoonModifier(
                    iceSpeed,
                    COCOON_ICE_SPEED_UUID,
                    "cocoon_ice_speed",
                    -CocoonCoreProgress.scalePercent(player, 0.10F)
            );
        } else if (iceSpeed != null) {
            iceSpeed.removeModifier(COCOON_ICE_SPEED_UUID);
        }
    }

    private static void setCocoonModifier(
            AttributeInstance attribute,
            UUID uuid,
            String name,
            double amount
    ) {
        if (attribute == null) {
            return;
        }

        attribute.removeModifier(uuid);
        attribute.addTransientModifier(
                new AttributeModifier(
                        uuid,
                        name,
                        amount,
                        AttributeModifier.Operation.MULTIPLY_BASE
                )
        );
    }

    private static boolean isColdBiome(Player player) {
        return player.level()
                .getBiome(player.blockPosition())
                .value()
                .getBaseTemperature() <= 0.15F;
    }

    private static void removeCocoonModifiers(Player player) {
        AttributeInstance movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null) {
            movement.removeModifier(COCOON_SPEED_UUID);
            movement.removeModifier(COCOON_ICE_SPEED_UUID);
        }

        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(COCOON_ATTACK_SPEED_UUID);
        }

        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(COCOON_HEALTH_UUID);
        }

        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.removeModifier(COCOON_DOMINANCE_ATTACK_UUID);
        }

        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(COCOON_DOMINANCE_ARMOR_UUID);
        }

        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    private static void applyCocoonTickEffects(Player player) {
        float multiplier = CocoonCoreProgress.getMultiplier(player);

        if (player.tickCount % 20 == 0) {
            player.causeFoodExhaustion(0.15F * multiplier);
        }

        if (player.isInWater()) {
            int interval = Math.max(1, Math.round(4.0F / multiplier));
            if (player.tickCount % interval == 0) {
                player.setAirSupply(Math.max(-20, player.getAirSupply() - 1));
            }

            if (CocoonCoreProgress.unlocked(player, CocoonCoreProgress.ICE_STAGE)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        40,
                        0,
                        false,
                        false,
                        true
                ));
            }
        }

        if (CocoonCoreProgress.unlocked(player, CocoonCoreProgress.ICE_STAGE)
                && isColdBiome(player)
                && player.tickCount % 100 == 0) {
            player.hurt(
                    player.damageSources().freeze(),
                    CocoonCoreProgress.scaleValue(player, 1.0F)
            );
        }

        if (CocoonCoreProgress.unlocked(player, CocoonCoreProgress.THUNDER_STAGE)
                && player.tickCount % 100 == 0
                && player.level().canSeeSky(player.blockPosition())) {
            float baseChance = player.level().isThundering()
                    ? 0.20F
                    : player.level().isRaining() ? 0.10F : 0.0F;

            if (baseChance > 0.0F
                    && RAND.nextFloat() < CocoonCoreProgress.scaleChance(player, baseChance)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.level());
                if (bolt != null) {
                    bolt.moveTo(player.getX(), player.getY(), player.getZ());
                    player.level().addFreshEntity(bolt);
                }
            }
        }

        if (CocoonCoreProgress.unlocked(player, CocoonCoreProgress.FIRE_STAGE)
                && player.tickCount % 20 == 0) {
            if (player.getBlockStateOn().is(Blocks.MAGMA_BLOCK)) {
                player.hurt(
                        player.damageSources().hotFloor(),
                        CocoonCoreProgress.scaleValue(player, 1.0F)
                );
            }

            if (player.isOnFire()) {
                player.hurt(
                        player.damageSources().onFire(),
                        CocoonCoreProgress.scaleValue(player, 1.0F)
                );
            }
        }

        if (CocoonCoreProgress.unlocked(player, CocoonCoreProgress.SENTIENCE_STAGE)) {
            CompoundTag data = player.getPersistentData();
            long now = player.level().getGameTime();
            long next = data.getLong(NBT_NEXT_COCOON_HALLUCINATION);

            if (next <= 0L) {
                data.putLong(
                        NBT_NEXT_COCOON_HALLUCINATION,
                        now + 200L + RAND.nextInt(21)
                );
            } else if (now >= next) {
                data.putLong(
                        NBT_NEXT_COCOON_HALLUCINATION,
                        now + 200L + RAND.nextInt(21)
                );

                spawnHallucination(player);
                int hallucinationDuration = CocoonCoreProgress.scaleDuration(player, 50);
                int selected = RAND.nextInt(3);
                MobEffectInstance hallucination = switch (selected) {
                    case 0 -> new MobEffectInstance(MobEffects.CONFUSION, hallucinationDuration, 0);
                    case 1 -> new MobEffectInstance(MobEffects.BLINDNESS, hallucinationDuration, 0);
                    default -> new MobEffectInstance(MobEffects.DARKNESS, hallucinationDuration, 0);
                };
                player.addEffect(hallucination);
                player.level().playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.ELDER_GUARDIAN_CURSE,
                        SoundSource.PLAYERS,
                        0.8F,
                        0.8F + RAND.nextFloat() * 0.4F
                );
            }
        }

        if (CocoonCoreProgress.unlocked(player, CocoonCoreProgress.DOMINANCE_STAGE)
                && player.tickCount % 400 == 0
                && RAND.nextFloat() < CocoonCoreProgress.scaleChance(player, 0.10F)) {
            int controlDuration = CocoonCoreProgress.scaleDuration(player, 30);
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    controlDuration,
                    255,
                    false,
                    false,
                    true
            ));
            player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    controlDuration,
                    4,
                    false,
                    false,
                    true
            ));
        }

        if (!CocoonCoreProgress.unlocked(player, CocoonCoreProgress.BINDING_STAGE)) {
            if (player.tickCount % 600 == 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SLOWDOWN,
                        CocoonCoreProgress.scaleDuration(player, 100),
                        0,
                        false,
                        false,
                        true
                ));
            }
        } else {

            player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    40,
                    0,
                    false,
                    false,
                    true
            ));
        }

    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            onPlayerTickInternal(event);
        } catch (Throwable throwable) {

            System.err.println(
                    "[LastSongOfElysian] SignetEffects player tick failed:"
            );
            throwable.printStackTrace(System.err);
        }
    }

    private static void onPlayerTickInternal(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) return;
        Player player = event.player;
        UUID uuid = player.getUUID();

        boolean hasDecimation = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DECIMATION.get()))
                .isPresent();
        AttributeInstance maxHp = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHp != null) {
            maxHp.removeModifier(DECIMATION_HEALTH_UUID);
            if (hasDecimation) {
                maxHp.addTransientModifier(new AttributeModifier(
                        DECIMATION_HEALTH_UUID, "decimation_hp",
                        signetBoost(player, Skill.DECIMATION_HEALTH, 0.15F),
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        boolean hasVicissitude = false;

        boolean hasEgoVicissitude = hasVicissitude
                && EgoSignetAmplification.isEnabled(player);
        AttributeInstance knockbackResistance =
                player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);

        if (knockbackResistance != null) {
            knockbackResistance.removeModifier(
                    EGO_VICISSITUDE_KNOCKBACK_UUID
            );

            if (hasEgoVicissitude) {
                knockbackResistance.addTransientModifier(
                        new AttributeModifier(
                                EGO_VICISSITUDE_KNOCKBACK_UUID,
                                "ego_vicissitude_knockback_immunity",
                                1.0D,
                                AttributeModifier.Operation.ADDITION
                        )
                );
            }
        }
        if (!hasVicissitude) {
            if (strengthNoHitTicks.containsKey(uuid) || resistanceNoHitTicks.containsKey(uuid)) {
                strengthNoHitTicks.remove(uuid);
                resistanceNoHitTicks.remove(uuid);
                strengthStacks.remove(uuid);
                resistanceStacks.remove(uuid);
            }
        } else {
            int strengthTick = strengthNoHitTicks.merge(uuid, 1, Integer::sum);
            int strengthStack = strengthStacks.getOrDefault(uuid, 0);
            if (strengthStack < MAX_STACKS && strengthTick >= SignetUpgradeData.vicissitudeChargeTicks(
                    player, Skill.VICISSITUDE_STRENGTH)) {
                strengthStacks.put(uuid, ++strengthStack);
                strengthNoHitTicks.put(uuid, 0);
            }

            int resistanceTick = resistanceNoHitTicks.merge(uuid, 1, Integer::sum);
            int resistanceStack = resistanceStacks.getOrDefault(uuid, 0);
            if (resistanceStack < MAX_STACKS && resistanceTick >= SignetUpgradeData.vicissitudeChargeTicks(
                    player, Skill.VICISSITUDE_RESISTANCE)) {
                resistanceStacks.put(uuid, ++resistanceStack);
                resistanceNoHitTicks.put(uuid, 0);
            }

        }

        AttributeInstance vicissitudeAttack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (vicissitudeAttack != null) {
            vicissitudeAttack.removeModifier(VICISSITUDE_STRENGTH_UUID);
            int strengthStack = strengthStacks.getOrDefault(uuid, 0);
            if (hasVicissitude && strengthStack > 0) {
                vicissitudeAttack.addTransientModifier(new AttributeModifier(
                        VICISSITUDE_STRENGTH_UUID,
                        "vicissitude_strength",
                        3.0D * strengthStack,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }

        int dTick = bodhiDmgTicks.getOrDefault(uuid, 0);
        if (dTick > 0) bodhiDmgTicks.put(uuid, dTick - 1);
        int fTick = bodhiDefTicks.getOrDefault(uuid, 0);
        if (fTick > 0) bodhiDefTicks.put(uuid, fTick - 1);
        int cTimer = comboResetTimer.getOrDefault(uuid, 0);
        if (cTimer > 0) {
            if (cTimer == 1) {
                comboResetTimer.remove(uuid);
                comboCount.remove(uuid);
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new ComboSyncPacket(0));
            } else {
                comboResetTimer.put(uuid, cTimer - 1);
            }
        }

        handleTuoLinTick(player);

        handleSetsunaBuffs(player);

        handleHelixMagic(player);

        boolean hasDiscipline = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DISCIPLINE.get())).isPresent();
        if (hasDiscipline) {
            boolean hasEgoSignet = EgoSignetAmplification.isEnabled(player);
            int currentMax = PRECEPT_MAX + (hasEgoSignet ? 30 : 0);
            if (SignetUpgradeData.hasDisciplineCoreSignet(player, Skill.DISCIPLINE_ASCETIC_LIFE)) {
                currentMax += Math.round(SignetUpgradeData.scaleBonus(
                        player, Skill.DISCIPLINE_ASCETIC_LIFE, 20.0F));
            }
            int preceptGain = SignetUpgradeData.getDisciplineCore(player)
                    == SignetUpgradeData.DisciplineCore.BLESSING ? 10 : 5;
            if (player.tickCount % 20 == 0) {
                int current = preceptCount.getOrDefault(uuid, 0);
                if (current < currentMax) preceptCount.put(uuid, Math.min(current + preceptGain, currentMax));
            }
            int current = preceptCount.getOrDefault(uuid, 0);
            if (SignetUpgradeData.hasDisciplineCoreSignet(player, Skill.DISCIPLINE_ASCETIC_LIFE)
                    && current < 60) {
                preceptCount.put(uuid, 60);
                current = 60;
            }
            handleDisciplineCoreTick(player, current);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new PreceptSyncPacket(preceptCount.getOrDefault(uuid, 0)));
        } else {
            Integer removed = preceptCount.remove(uuid);
            if (removed != null) {
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new PreceptSyncPacket(0));
            }
            updateDisciplineCoreEffects(player, false, false);
            player.removeEffect(ModEffects.DISCIPLINE_PUNISHMENT.get());
            clearDisciplineRuntime(uuid);
        }

        boolean hasDeliverance = hasItem(player, ModItems.SIGNET_OF_DELIVERANCE.get());
        handleDeliveranceCoreTick(player, hasDeliverance);
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(DELIVERANCE_ARMOR_UUID);
            if (hasDeliverance && deliveranceBuffEnd.getOrDefault(uuid, 0L) >= player.level().getGameTime()) {
                armorAttr.addTransientModifier(new AttributeModifier(
                        DELIVERANCE_ARMOR_UUID,
                        "deliverance_armor",
                        signetBoost(player, Skill.DELIVERANCE_ARMOR, 0.15F),
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        handleDeliveranceSecondaryTick(player);

        AttributeInstance egoSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (egoSpeedAttr != null) {
            egoSpeedAttr.removeModifier(EGO_SPEED_UUID);
            if (hasEgoAnd(player, ModItems.SIGNET_OF_REVERIE.get())) {
                egoSpeedAttr.addTransientModifier(new AttributeModifier(
                        EGO_SPEED_UUID,
                        "ego_reverie_speed",
                        0.39F,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        AttributeInstance egoHpAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (egoHpAttr != null) {
            egoHpAttr.removeModifier(EGO_HEALTH_UUID);
            if (hasEgoAnd(player, ModItems.SIGNET_OF_DECIMATION.get())) {
                egoHpAttr.addTransientModifier(new AttributeModifier(
                        EGO_HEALTH_UUID,
                        "ego_decimation_hp",
                        0.60F,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
        }

        AttributeInstance egoAtkAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (egoAtkAttr != null) {
            egoAtkAttr.removeModifier(EGO_ATTACK_UUID);
            if (hasEgoAnd(player, ModItems.SIGNET_OF_DELIVERANCE.get())) {
                egoAtkAttr.addTransientModifier(new AttributeModifier(
                        EGO_ATTACK_UUID,
                        "ego_deliverance_attack",
                        1.0F,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        if (hasEgoAnd(player, ModItems.SIGNET_OF_BODHI.get())) {
            MobEffectInstance currentNightVision =
                    player.getEffect(MobEffects.NIGHT_VISION);

            if (currentNightVision == null
                    || currentNightVision.getDuration() < 220) {

                player.addEffect(
                        new MobEffectInstance(
                                MobEffects.NIGHT_VISION,
                                400,
                                0,
                                false,
                                false,
                                false
                        )
                );
            }
        }

        if (hasEgoAnd(player, ModItems.SIGNET_OF_GOLD.get())) {
            if (player.isCreative() || player.isSpectator()) {

                egoFlightTicks.put(
                        uuid,
                        EGO_FLIGHT_MAX_TICKS
                );
            } else {
                int flightLeft =
                        egoFlightTicks.getOrDefault(
                                uuid,
                                EGO_FLIGHT_MAX_TICKS
                        );

                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.getPersistentData().putBoolean(
                            NBT_EGO_GOLD_GRANTED_FLIGHT,
                            true
                    );
                    player.onUpdateAbilities();
                }

                if (player.getAbilities().flying) {
                    flightLeft--;

                    if (flightLeft <= 0) {
                        boolean grantedByThisMod =
                                player.getPersistentData()
                                        .getBoolean(
                                                NBT_EGO_GOLD_GRANTED_FLIGHT
                                        );

                        if (grantedByThisMod) {
                            player.getAbilities().mayfly = false;
                            player.getAbilities().flying = false;
                            player.getPersistentData().remove(
                                    NBT_EGO_GOLD_GRANTED_FLIGHT
                            );
                            player.onUpdateAbilities();
                        }

                        flightLeft =
                                EGO_FLIGHT_MAX_TICKS;
                    }
                } else {
                    flightLeft =
                            EGO_FLIGHT_MAX_TICKS;
                }

                egoFlightTicks.put(
                        uuid,
                        flightLeft
                );
            }
        } else {
            boolean grantedByThisMod =
                    player.getPersistentData()
                            .getBoolean(
                                    NBT_EGO_GOLD_GRANTED_FLIGHT
                            );

            if (
                    grantedByThisMod &&
                    !player.isCreative() &&
                    !player.isSpectator()
            ) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getPersistentData().remove(
                        NBT_EGO_GOLD_GRANTED_FLIGHT
                );
                player.onUpdateAbilities();
            }

            egoFlightTicks.remove(uuid);
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCorruptionTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.side != LogicalSide.SERVER) {
            return;
        }

        handleCorruptionSystem(event.player);
    }

    private static void handleCorruptionSystem(Player player) {
        UUID uuid = player.getUUID();

        if (isCorrosionCleared(player)) {
            if (corruptionEnergy.getOrDefault(uuid, loadCorruptionEnergy(player)) > 0.0F) {
                clearCorruption(player);
            }
            return;
        }

        float maxCorruption = getCorruptionCapacity(player);

        float energy = corruptionEnergy.computeIfAbsent(uuid, id -> loadCorruptionEnergy(player));

        float naturalCorruptionGain =
                CORRUPTION_PER_TICK
                        * getCorruptionGrowthMultiplier(
                        player
                )
                        * CorrosionCurseEvents.getCurseStrengthMultiplier(player);
        energy = Math.min(energy + naturalCorruptionGain, maxCorruption);

        corruptionEnergy.put(uuid, energy);
        saveCorruptionEnergy(player, energy);

        syncCorruptionToClient(
                player,
                energy,
                maxCorruption
        );

        if (energy >= maxCorruption) {
            if (CorrosionCurseEvents.handleFullCorruption(player)) {
                return;
            }

            if (player.tickCount % 20 == 0) {
                float overflowDamage = hasCorrosionCocoonCurse(player)
                        ? CocoonCoreProgress.scaleValue(player, 5.0F)
                        : 5.0F;
                player.hurt(player.damageSources().magic(), overflowDamage);
            }

            int overflowEffectDuration = hasCorrosionCocoonCurse(player)
                    ? CocoonCoreProgress.scaleDuration(player, 60)
                    : 60;
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, overflowEffectDuration, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, overflowEffectDuration, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, overflowEffectDuration, 0, false, false));

            if (player.getHealth() <= 1.0f) {
                ElysianHeroExplosionProtection.run(() -> player.level().explode(
                        player,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        4.0f,
                        Level.ExplosionInteraction.MOB
                ));

                corruptionEnergy.put(uuid, 0.0f);
                saveCorruptionEnergy(player, 0.0f);

                player.kill();
            }
        }
    }

    private static void handleTuoLinTick(Player player) {
        CompoundTag data = player.getPersistentData();
        int ticks = data.getInt(NBT_TUOLIN_TICKS);
        if (ticks <= 0) return;

        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null && attr.getModifier(TUOLIN_MOD_UUID) == null) {
            int stacks = data.getInt(NBT_TUOLIN_STACKS);
            double healthLoss = SignetUpgradeData.infinityHealthLoss(player);
            if (stacks > 0) attr.addTransientModifier(new AttributeModifier(
                    TUOLIN_MOD_UUID, "tuolin", -healthLoss * stacks, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        ticks--;
        data.putInt(NBT_TUOLIN_TICKS, ticks);

        if (ticks <= 0) {
            data.putInt(NBT_TUOLIN_STACKS, 0);
            data.remove(NBT_TUOLIN_BASE_MAX);
            if (attr != null) attr.removeModifier(TUOLIN_MOD_UUID);
            player.removeEffect(ModEffects.TUOLIN.get());
            player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInfinityFatalDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        boolean hasSignet = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_INFINITY.get()))
                .isPresent();
        if (!hasSignet) return;

        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (event.getAmount() < player.getHealth()) return;

        double currentMax = player.getMaxHealth();
        CompoundTag data = player.getPersistentData();
        if (!data.contains(NBT_TUOLIN_BASE_MAX)) data.putDouble(NBT_TUOLIN_BASE_MAX, currentMax);
        double baseMax = data.getDouble(NBT_TUOLIN_BASE_MAX);

        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        double finalDeathThreshold = Math.max(1.0, baseMax * TUOLIN_FINAL_DEATH_THRESHOLD_RATIO);
        if (currentMax <= finalDeathThreshold) {

            if (attr != null) attr.removeModifier(TUOLIN_MOD_UUID);
            data.putInt(NBT_TUOLIN_STACKS, 0);
            data.putInt(NBT_TUOLIN_TICKS, 0);
            data.remove(NBT_TUOLIN_BASE_MAX);
            player.removeEffect(ModEffects.TUOLIN.get());
            player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
            return;
        }

        event.setAmount(0);

        int stacks = data.getInt(NBT_TUOLIN_STACKS) + 1;
        data.putInt(NBT_TUOLIN_STACKS, stacks);
        data.putInt(NBT_TUOLIN_TICKS, TUOLIN_DURATION);

        double healthLoss = SignetUpgradeData.infinityHealthLoss(player);
        double newMax = baseMax * (1.0 - healthLoss * stacks);
        if (newMax < 1.0) newMax = 1.0;

        if (attr != null) {
            attr.removeModifier(TUOLIN_MOD_UUID);
            attr.addTransientModifier(new AttributeModifier(
                    TUOLIN_MOD_UUID, "tuolin", -healthLoss * stacks, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        player.addEffect(new MobEffectInstance(
                ModEffects.TUOLIN.get(), TUOLIN_DURATION, stacks - 1, false, true));

        player.setHealth((float) newMax);
    }

    @SubscribeEvent
    public static void onDaybreakDamage(LivingDamageEvent event) {
        if (DaybreakSignetSystem.isInternalDamage()) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide() || event.getAmount() <= 0) return;

        boolean hasDaybreak = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DAYBREAK.get()))
                .isPresent();
        if (!hasDaybreak) return;

        LivingEntity target = event.getEntity();
        if (PlayerControlledAllies.isProtectedEffectTarget(player, target)) return;
        MobEffectInstance existing = target.getEffect(ModEffects.LACERATION.get());
        int preStacks = existing != null ? existing.getAmplifier() + 1 : 0;

        float amount = event.getAmount();
        if (preStacks > 0) {
            boolean hasMemento = CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, stack -> stack.is(ModItems.OUT_OF_REACH.get()))
                    .isPresent();
            if (hasMemento) {
                amount *= signetBonusMultiplier(
                        player,
                        Skill.DAYBREAK_CLAW,
                        0.15F
                );
            }
            damageArmorForDaybreak(target, preStacks, player);
            amount *= signetBonusMultiplier(
                    player,
                    Skill.DAYBREAK_EYE,
                    preStacks * 0.02F
            );
        }

        event.setAmount(amount);

        DaybreakSignetSystem.addLaceration(player, target, 1);
    }

    @SubscribeEvent
    public static void onLacerationTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (PlayerControlledAllies.isElysianHero(entity)) {
            entity.removeEffect(ModEffects.LACERATION.get());
            return;
        }
        if (entity.tickCount % 20 != 0) return;

        MobEffectInstance instance = entity.getEffect(ModEffects.LACERATION.get());
        if (instance == null) return;

        int stacks = instance.getAmplifier() + 1;
        float power = entity.getPersistentData().contains(NBT_LACERATION_POWER)
                ? entity.getPersistentData().getFloat(NBT_LACERATION_POWER)
                : 0.5F;
        float damage = power * stacks;
        if (!DaybreakSignetSystem.applySelfLacerationTick(entity, damage)) {
            entity.hurt(entity.damageSources().magic(), damage);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSetsunaEvade(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        boolean hasSetsuna = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_SETSURA.get()))
                .isPresent();
        if (!hasSetsuna) return;

        if (!(event.getSource().getEntity() instanceof LivingEntity)) return;
        if (isInstakill(event.getSource())) return;

        long gameTime = player.level().getGameTime();
        Long cooldownEnd = EVADE_COOLDOWNS.get(player.getUUID());
        boolean usingExtraCharge = cooldownEnd != null && gameTime < cooldownEnd;
        if (usingExtraCharge && !SetsunaSignetSystem.hasExtraEvasionCharge(player)) return;

        if (RAND.nextFloat() < signetChance(player, Skill.SETSURA_DODGE, 0.2F)) {
            if (usingExtraCharge && !SetsunaSignetSystem.consumeExtraEvasionCharge(player)) return;
            event.setAmount(0);
            applyEvadeBuffs(player, gameTime);

            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new SakuraDodgeEffectPacket());

            if (!usingExtraCharge) {
                int cooldown = Math.max(
                        20,
                        Math.round(
                                EVADE_COOLDOWN_TICKS
                                        * SignetUpgradeData.setsuraCooldownMultiplier(player)
                                        * SetsunaSignetSystem.cooldownMultiplier(player)
                        )
                );
                if (hasEgoAnd(player, ModItems.SIGNET_OF_SETSURA.get())) cooldown = Math.max(20, Math.round(cooldown * 0.68F));
                EVADE_COOLDOWNS.put(player.getUUID(), gameTime + cooldown);
                SetsunaSignetSystem.resetExtraEvasionCharge(player);
            }
            SetsunaSignetSystem.onSuccessfulEvasion(player);
            DaybreakSignetSystem.onSuccessfulEvasion(player);
            StarsSignetSystem.onEvasion(player);
            ReverieSignetSystem.onEvasion(player);
        }

        if (event.getAmount() == 0) {
            if (CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DISCIPLINE.get()))
                    .isPresent()) {
                reducePrecept(player);
            }
        }
    }

    public static boolean hasSetsunaEvasion(Player player) {
        return player != null && CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_SETSURA.get()))
                .isPresent();
    }

    public static int getSetsunaEvasionCooldown(Player player) {
        if (player == null) return 0;
        long remaining = EVADE_COOLDOWNS.getOrDefault(player.getUUID(), 0L)
                - player.level().getGameTime();
        return (int) Mth.clamp(remaining, 0L, Integer.MAX_VALUE);
    }

    public static int getSetsunaEvasionCooldownMaximum(Player player) {
        if (player == null) return EVADE_COOLDOWN_TICKS;
        int cooldown = Math.max(20, Math.round(
                EVADE_COOLDOWN_TICKS
                        * SignetUpgradeData.setsuraCooldownMultiplier(player)
                        * SetsunaSignetSystem.cooldownMultiplier(player)));
        if (hasEgoAnd(player, ModItems.SIGNET_OF_SETSURA.get())) {
            cooldown = Math.max(20, Math.round(cooldown * 0.68F));
        }
        return cooldown;
    }

    public static int getSetsunaEvasionMaximumUses(Player player) {
        return SignetUpgradeData.isSetsuraSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.SETSURA_WISTERIA) ? 2 : 1;
    }

    public static int getSetsunaEvasionUses(Player player) {
        if (player == null) return 0;
        int uses = getSetsunaEvasionCooldown(player) <= 0 ? 1 : 0;
        if (SetsunaSignetSystem.hasExtraEvasionCharge(player)) uses++;
        return Math.min(getSetsunaEvasionMaximumUses(player), uses);
    }

    private static boolean isInstakill(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    public static void reduceSetsunaEvadeCooldown(Player player, float fraction) {
        if (player == null) return;
        long now = player.level().getGameTime();
        Long until = EVADE_COOLDOWNS.get(player.getUUID());
        if (until == null || until <= now) return;
        float reduction = Mth.clamp(fraction, 0.0F, 0.95F);
        EVADE_COOLDOWNS.put(
                player.getUUID(),
                now + Math.max(1L, Math.round((until - now) * (1.0F - reduction)))
        );
    }

    private static void applyEvadeBuffs(Player player, long gameTime) {
        AttributeInstance atkAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance movAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);

        float speedBonus = signetBoost(player, Skill.SETSURA_SPEED, 0.1F);
        AttributeModifier atkMod = new AttributeModifier(SETSUNA_ATK_SPEED_UUID, "setsuna_atk", speedBonus, AttributeModifier.Operation.MULTIPLY_TOTAL);
        AttributeModifier movMod = new AttributeModifier(SETSUNA_MOV_SPEED_UUID, "setsuna_mov", speedBonus, AttributeModifier.Operation.MULTIPLY_TOTAL);

        if (atkAttr != null && !atkAttr.hasModifier(atkMod)) atkAttr.addTransientModifier(atkMod);
        if (movAttr != null && !movAttr.hasModifier(movMod)) movAttr.addTransientModifier(movMod);
        EVADE_BUFF_END.put(player.getUUID(), gameTime + EVADE_BUFF_DURATION);
    }

    private static void handleSetsunaBuffs(Player player) {
        Long endTime = EVADE_BUFF_END.get(player.getUUID());
        if (endTime == null) return;
        if (player.level().getGameTime() >= endTime) {
            AttributeInstance atk = player.getAttribute(Attributes.ATTACK_SPEED);
            AttributeInstance mov = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (atk != null) atk.removeModifier(SETSUNA_ATK_SPEED_UUID);
            if (mov != null) mov.removeModifier(SETSUNA_MOV_SPEED_UUID);
            EVADE_BUFF_END.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onDecimationEffect(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;

        if (event.getEntity() instanceof Player player) {
            boolean has = CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DECIMATION.get()))
                    .isPresent();
            if (has) {
                if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                    event.setAmount(applyIndependentReduction(
                            event.getAmount(),
                            Math.min(0.60F, egoBoost(player, 0.40F))
                    ));
                }
                float lostHealth = player.getMaxHealth() - player.getHealth();
                double reduction = Math.min(
                        signetBoost(player, Skill.DECIMATION_LOW_HEALTH, lostHealth * 0.003),
                        signetBoost(player, Skill.DECIMATION_LOW_HEALTH, DECIMATION_DEF_CAP)
                );
                event.setAmount(applyIndependentReduction(
                        event.getAmount(),
                        Math.min(DECIMATION_DEF_MAX, reduction)
                ));
            }
        }

        if (event.getSource().getEntity() instanceof Player player) {
            boolean has = CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DECIMATION.get()))
                    .isPresent();
            if (has && event.getSource().is(DamageTypeTags.IS_FIRE)) {
                event.setAmount(event.getAmount() * signetBonusMultiplier(
                        player,
                        Skill.DECIMATION_FIRE,
                        0.5F
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onVicissitudeResistance(LivingHurtEvent event) {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBodhiDamageBonus(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;

        if (event.getSource().getEntity() instanceof Player attacker) {
            if (hasItem(attacker, ModItems.SIGNET_OF_BODHI.get())
                    && bodhiDmgTicks.getOrDefault(attacker.getUUID(), 0) > 0) {
                event.setAmount(event.getAmount() * signetBonusMultiplier(
                        attacker,
                        Skill.BODHI_DAMAGE,
                        0.25F
                ));
            }
        }

        if (event.getEntity() instanceof Player defender) {
            if (hasItem(defender, ModItems.SIGNET_OF_BODHI.get())
                    && bodhiDefTicks.getOrDefault(defender.getUUID(), 0) > 0) {
                event.setAmount(applyIndependentReduction(
                        event.getAmount(),
                        signetBoost(defender, Skill.BODHI_DEFENSE, 0.12F)
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onBodhiCombo(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        if (EdenStarSystem.isBlackHoleComboDamage(event.getSource())) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (BodhiSignetSystem.isInternalDamage(player)) return;

        addCombo(player, 1);
    }

    public static int getComboCount(Player player) {
        return player == null ? 0 : comboCount.getOrDefault(player.getUUID(), 0);
    }

    public static int addCombo(Player player, int amount) {
        if (player == null || player.level().isClientSide() || amount <= 0) {
            return getComboCount(player);
        }

        boolean hasBodhi = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_BODHI.get()))
                .isPresent();
        boolean hasDiscipline = hasItem(player, ModItems.SIGNET_OF_DISCIPLINE.get());
        if (!hasBodhi && !hasDiscipline) return 0;

        UUID uuid = player.getUUID();
        int previous = comboCount.getOrDefault(uuid, 0);
        int combo = Math.min(15, previous + amount);
        comboCount.put(uuid, combo);
        comboResetTimer.put(uuid, 60);

        if (hasBodhi) {
            BodhiSignetSystem.onComboAdvanced(player, previous, combo);
        }

        if (combo >= 15) {
            comboCount.put(uuid, 0);
            if (hasBodhi) {
                bodhiDmgTicks.put(uuid, 100);
                bodhiDefTicks.put(uuid, 100);
                BodhiSignetSystem.onComboFifteen(player);
            }
            combo = 0;

            if (hasDiscipline) {
                reducePrecept(player);
            }
        }

        final int displayCombo = combo;
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new ComboSyncPacket(displayCombo));
        return combo;
    }

    private static void reducePrecept(Player player) {
        UUID uuid = player.getUUID();
        long now = player.level().getGameTime();
        if (disciplineUnyieldingEnd.getOrDefault(uuid, 0L) >= now) return;
        int current = preceptCount.getOrDefault(uuid, 0);
        if (current <= 0) return;
        float removeRatio = EgoSignetAmplification.isEnabled(player) ? 0.25F : 0.50F;
        int minimum = SignetUpgradeData.hasDisciplineCoreSignet(
                player, Skill.DISCIPLINE_ASCETIC_LIFE) ? 60 : 0;
        int updated = Math.max(minimum, current - Math.max(1, (int) (current * removeRatio)));
        if (updated >= current) return;
        preceptCount.put(uuid, updated);
        if (SignetUpgradeData.isDisciplineSecondarySkillUnlocked(
                player, Skill.DISCIPLINE_FIVE)) {
            triggerFalseSpeech(player);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new PreceptSyncPacket(updated));
        }
    }

    private static void triggerFalseSpeech(Player player) {
        float power = SignetUpgradeData.scaleBonus(player, Skill.DISCIPLINE_FIVE, 0.20F);
        long end = player.level().getGameTime() + 100L;
        player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(64.0D),
                entity -> PlayerControlledAllies.isValidEnemy(player, entity)
        ).forEach(entity -> {
            UUID targetId = entity.getUUID();
            disciplineFalseSpeechEnd.put(targetId, end);
            disciplineFalseSpeechPower.put(
                    targetId,
                    Math.max(power, disciplineFalseSpeechPower.getOrDefault(targetId, 0.0F))
            );
        });
    }

    public static void onWeaponUltimateActivated(Player player, int durationTicks) {
        if (player == null || player.level().isClientSide()) return;
        DaybreakSignetSystem.onWeaponUltimateActivated(player);
        StarsSignetSystem.onUltimate(player);
        ReverieSignetSystem.onUltimate(player);
        if (!hasItem(player, ModItems.SIGNET_OF_DISCIPLINE.get())) return;
        disciplineUltimateEnd.put(
                player.getUUID(),
                player.level().getGameTime() + Math.max(1, durationTicks)
        );
    }

    public static float getDisciplineUltimateDamageMultiplier(Player player) {
        if (player == null || !hasItem(player, ModItems.SIGNET_OF_DISCIPLINE.get())) return 1.0F;
        long now = player.level().getGameTime();
        if (disciplineUltimateEnd.getOrDefault(player.getUUID(), 0L) < now) return 1.0F;
        int count = preceptCount.getOrDefault(player.getUUID(), 0);
        float multiplier = SignetUpgradeData.isDisciplineSecondarySkillUnlocked(
                player, Skill.DISCIPLINE_FOUR)
                ? 1.0F + count * SignetUpgradeData.scaleBonus(
                player, Skill.DISCIPLINE_FOUR, 0.003F)
                : 1.0F;
        if (SignetUpgradeData.isDisciplineSecondarySkillUnlocked(
                player, Skill.DISCIPLINE_SIX)
                && disciplineUnyieldingEnd.getOrDefault(player.getUUID(), 0L) >= now) {
            multiplier *= 1.0F + SignetUpgradeData.scaleBonus(
                    player, Skill.DISCIPLINE_SIX, 0.40F);
        }
        if (SignetUpgradeData.getDisciplineCore(player)
                == SignetUpgradeData.DisciplineCore.ASCETICISM && count >= 90) {
            int signetCount = getDisciplineSignetCount(player);
            multiplier *= 1.25F + signetCount * 0.05F;
            if (SignetUpgradeData.hasDisciplineCoreSignet(
                    player, Skill.DISCIPLINE_ASCETIC_PROOF)) {
                multiplier *= 1.0F + SignetUpgradeData.scaleBonus(
                        player, Skill.DISCIPLINE_ASCETIC_PROOF, 0.20F);
            }
        }
        multiplier *= 1.0F + disciplineMountainDamageBonus.getOrDefault(
                player.getUUID(), 0.0F);
        if (disciplinePunishmentEnd.getOrDefault(player.getUUID(), 0L) >= now
                && SignetUpgradeData.hasDisciplineCoreSignet(
                player, Skill.DISCIPLINE_BLESSING_RETURN)) {
            multiplier *= 1.0F + disciplineAugmentValue(
                    player, Skill.DISCIPLINE_BLESSING_RETURN, 0.26F);
        }
        return multiplier;
    }

    public static void onWeaponSkillActivated(Player player) {
        if (player == null || player.level().isClientSide()) return;
        DaybreakSignetSystem.onWeaponSkillActivated(player);
        StarsSignetSystem.onWeaponSkill(player);
        UUID uuid = player.getUUID();
        if (hasItem(player, ModItems.SIGNET_OF_HELIX.get())) {
            int stacks = helixMagicStacks.getOrDefault(uuid, 0);
            if (stacks < HELIX_MAGIC_MAX_STACKS) {
                stacks++;
                helixMagicStacks.put(uuid, stacks);
            }
            helixMagicTimer.put(uuid, player.level().getGameTime() + HELIX_MAGIC_DURATION_TICKS);
            onWeaponSkillActivatedPendulum(player);
        }
        if (hasItem(player, ModItems.SIGNET_OF_DISCIPLINE.get())
                && SignetUpgradeData.isDisciplineSecondarySkillUnlocked(
                player, Skill.DISCIPLINE_FOUR)) {
            reducePrecept(player);
        }
        HelixSignetSystem.onWeaponSkillActivated(player);
    }

    private static void handleHelixMagic(Player player) {
        UUID uuid = player.getUUID();
        Long expire = helixMagicTimer.get(uuid);
        if (expire == null) return;
        if (player.level().getGameTime() > expire) {
            helixMagicStacks.remove(uuid);
            helixMagicTimer.remove(uuid);
        }
    }

    public static void onWeaponSkillActivatedPendulum(Player player) {
        long expire = player.level().getGameTime() + 90L;
        float power = signetBoost(player, Skill.HELIX_PENDULUM, 0.20F);
        player.level().getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(16.0),
                        e -> e instanceof Enemy
                                && PlayerControlledAllies.isValidEnemy(player, e))
                .forEach(target -> {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 90, 0, false, false));
                    helixPendulumTimer.put(target.getUUID(), expire);
                    helixPendulumPower.put(target.getUUID(), power);
                });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHelixDamage(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;

        if (event.getSource().getEntity() instanceof Player player
                && hasItem(player, ModItems.SIGNET_OF_HELIX.get())) {
            int stacks = helixMagicStacks.getOrDefault(player.getUUID(), 0);
            if (stacks > 0) {
                event.setAmount(event.getAmount() * signetBonusMultiplier(
                        player,
                        Skill.HELIX_MAGIC,
                        0.10F * stacks
                ));
            }
        }

        UUID targetId = event.getEntity().getUUID();
        long expire = helixPendulumTimer.getOrDefault(targetId, 0L);
        if (expire >= event.getEntity().level().getGameTime()) {
            event.setAmount(event.getAmount() * (1.0F + helixPendulumPower.getOrDefault(targetId, 0.20F)));
        } else {
            helixPendulumTimer.remove(targetId);
            helixPendulumPower.remove(targetId);
        }
    }

    @SubscribeEvent
    public static void onGoldDamage(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            boolean hasGold = CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_GOLD.get()))
                    .isPresent();
            if (!hasGold) return;

        }
    }

    private static void handleDisciplineCoreTick(Player player, int precept) {
        UUID uuid = player.getUUID();
        long now = player.level().getGameTime();
        SignetUpgradeData.DisciplineCore core = SignetUpgradeData.getDisciplineCore(player);
        boolean asceticism = core == SignetUpgradeData.DisciplineCore.ASCETICISM
                && precept >= 90;
        boolean blessing = core == SignetUpgradeData.DisciplineCore.BLESSING
                && precept > 80;
        updateDisciplineCoreEffects(player, asceticism, blessing);
        if (blessing) {
            long next = disciplineMountainNext.getOrDefault(uuid, 0L);
            if (now >= next) {
                disciplineMountainNext.put(uuid, now + 100L);
                triggerDisciplineMountain(player, precept);
            }
        } else {
            disciplineMountainNext.remove(uuid);
        }
        if (disciplineMountainDamageEnd.getOrDefault(uuid, 0L) < now) {
            disciplineMountainDamageBonus.remove(uuid);
        }
        if (disciplineVirtuePenetrationEnd.getOrDefault(uuid, 0L) < now) {
            disciplineVirtuePenetration.remove(uuid);
        }
    }

    private static void updateDisciplineCoreEffects(
            Player player,
            boolean asceticism,
            boolean blessing
    ) {
        if (asceticism) {
            if (!player.hasEffect(ModEffects.DISCIPLINE_ASCETICISM.get())) {
                player.addEffect(new MobEffectInstance(
                        ModEffects.DISCIPLINE_ASCETICISM.get(), -1, 0, false, false, true));
            }
        } else {
            player.removeEffect(ModEffects.DISCIPLINE_ASCETICISM.get());
        }
        if (blessing) {
            if (!player.hasEffect(ModEffects.DISCIPLINE_BLESSING.get())) {
                player.addEffect(new MobEffectInstance(
                        ModEffects.DISCIPLINE_BLESSING.get(), -1, 0, false, false, true));
            }
        } else {
            player.removeEffect(ModEffects.DISCIPLINE_BLESSING.get());
        }
    }

    private static int getDisciplineSignetCount(Player player) {
        int ordinary = 3 + Integer.bitCount(
                SignetUpgradeData.getDisciplineSecondaryUnlockMask(player) & 0b111);
        int core = SignetUpgradeData.getDisciplineCore(player) == null ? 0 : 1;
        int augments = Integer.bitCount(
                SignetUpgradeData.getDisciplineCoreSignetMask(player) & 0b111111);
        return ordinary + core + augments;
    }

    private static void triggerDisciplineMountain(Player player, int precept) {
        boolean virtue = SignetUpgradeData.hasDisciplineCoreSignet(
                player, Skill.DISCIPLINE_BLESSING_VIRTUE);
        boolean punishment = SignetUpgradeData.hasDisciplineCoreSignet(
                player, Skill.DISCIPLINE_BLESSING_PUNISHMENT);
        boolean returnSignet = SignetUpgradeData.hasDisciplineCoreSignet(
                player, Skill.DISCIPLINE_BLESSING_RETURN);
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 10.0F;
        if (virtue) {
            damage *= 1.0F + disciplineAugmentValue(
                    player, Skill.DISCIPLINE_BLESSING_VIRTUE, 0.30F);
            disciplineVirtuePenetration.put(
                    player.getUUID(),
                    Math.min(0.95F, precept * disciplineAugmentValue(
                            player, Skill.DISCIPLINE_BLESSING_VIRTUE, 0.0013F))
            );
            disciplineVirtuePenetrationEnd.put(
                    player.getUUID(), player.level().getGameTime() + 120L);
        }
        if (returnSignet) {
            disciplinePunishmentEnd.put(player.getUUID(), player.level().getGameTime() + 160L);
            player.addEffect(new MobEffectInstance(
                    ModEffects.DISCIPLINE_PUNISHMENT.get(),
                    160,
                    0,
                    false,
                    false,
                    true
            ));
        }
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(10.0D),
                entity -> PlayerControlledAllies.isValidEnemy(player, entity)
        );
        Map<UUID, Integer> hitCounts = disciplineMountainHits.computeIfAbsent(
                player.getUUID(), ignored -> new HashMap<>());
        int hitEnemies = 0;
        for (LivingEntity target : targets) {
            if (PlayerControlledAllies.hurt(player, target,
                    player.damageSources().playerAttack(player), damage)) {
                hitCounts.merge(target.getUUID(), 1, Integer::sum);
                hitEnemies++;
                if (punishment) {
                    addHeldWeaponEnergy(player, Math.max(1, Math.round(
                            disciplineAugmentValue(
                                    player, Skill.DISCIPLINE_BLESSING_PUNISHMENT, 5.0F))));
                }
            }
        }
        if (hitEnemies > 0) {
            float current = disciplineMountainDamageBonus.getOrDefault(player.getUUID(), 0.0F);
            disciplineMountainDamageBonus.put(
                    player.getUUID(), Math.min(0.40F, current + hitEnemies * 0.10F));
            disciplineMountainDamageEnd.put(
                    player.getUUID(), player.level().getGameTime() + 200L);
        }
    }

    private static float disciplineAugmentValue(Player player, Skill skill, float base) {
        float value = SignetUpgradeData.scaleBonus(player, skill, base);
        int mask = SignetUpgradeData.getDisciplineCoreSignetMask(player);
        int relevantMask = skill.disciplineCore() == SignetUpgradeData.DisciplineCore.ASCETICISM
                ? 0b000111 : 0b111000;
        return Integer.bitCount(mask & relevantMask) == 1 ? value * 1.5F : value;
    }

    private static void addHeldWeaponEnergy(Player player, int amount) {
        ItemStack weapon = player.getMainHandItem();
        if (weapon.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) {
            FlawlessWeaponEnergy.addEnergy(player, weapon, amount);
        } else if (weapon.is(ModItems.FENGHUANG_DOWN.get())) {
            FenghuangDownEnergy.addEnergy(player, weapon, amount);
        } else if (weapon.is(ModItems.ABYSS_FLOWER.get())) {
            AbyssFlowerEnergy.add(player, weapon, amount);
        } else if (weapon.is(ModItems.SKYFIRE_GREATSWORD.get())) {
            SkyfireGreatswordEnergy.add(player, weapon, amount);
        } else if (weapon.is(ModItems.EDEN_STAR.get())) {
            EdenStarEnergy.add(player, weapon, amount);
        }
    }

    @SubscribeEvent
    public static void onDisciplineUnitDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        long end = event.getEntity().level().getGameTime() + 200L;
        for (Player player : event.getEntity().level().players()) {
            if (player.distanceToSqr(event.getEntity()) > 4096.0D
                    || !hasItem(player, ModItems.SIGNET_OF_DISCIPLINE.get())
                    || !SignetUpgradeData.isDisciplineSecondarySkillUnlocked(
                    player, Skill.DISCIPLINE_SIX)) continue;
            disciplineUnyieldingEnd.put(player.getUUID(), end);
        }
    }

    private static void clearDisciplineRuntime(UUID uuid) {
        disciplineUltimateEnd.remove(uuid);
        disciplineUltimatePenetrationEnd.remove(uuid);
        disciplineUnyieldingEnd.remove(uuid);
        disciplineMountainNext.remove(uuid);
        disciplineMountainDamageEnd.remove(uuid);
        disciplineMountainDamageBonus.remove(uuid);
        disciplineMountainHits.remove(uuid);
        disciplineVirtuePenetrationEnd.remove(uuid);
        disciplineVirtuePenetration.remove(uuid);
        disciplinePunishmentEnd.remove(uuid);
        disciplineRetaliationNext.remove(uuid);
    }

    public static void resetDisciplineCoreRuntime(Player player) {
        if (player == null) return;
        UUID id = player.getUUID();
        disciplineUltimatePenetrationEnd.remove(id);
        disciplineMountainNext.remove(id);
        disciplineMountainDamageEnd.remove(id);
        disciplineMountainDamageBonus.remove(id);
        disciplineMountainHits.remove(id);
        disciplineVirtuePenetrationEnd.remove(id);
        disciplineVirtuePenetration.remove(id);
        disciplinePunishmentEnd.remove(id);
        disciplineRetaliationNext.remove(id);
        player.removeEffect(ModEffects.DISCIPLINE_ASCETICISM.get());
        player.removeEffect(ModEffects.DISCIPLINE_BLESSING.get());
        player.removeEffect(ModEffects.DISCIPLINE_PUNISHMENT.get());
    }

    @SubscribeEvent
    public static void onDisciplineEffect(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        LivingEntity target = event.getEntity();
        if (PlayerControlledAllies.isElysianHero(target)) return;
        long now = target.level().getGameTime();
        UUID targetId = target.getUUID();
        if (disciplineFalseSpeechEnd.getOrDefault(targetId, 0L) >= now) {
            event.setAmount(event.getAmount() * (1.0F
                    + disciplineFalseSpeechPower.getOrDefault(targetId, 0.0F)));
        }

        if (event.getSource().getEntity() instanceof Player player) {
            boolean has = CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DISCIPLINE.get()))
                    .isPresent();
            if (has) {
                int count = preceptCount.getOrDefault(player.getUUID(), 0);
                if (count > 0) {
                    float bonus = (float) (count * signetBoost(
                            player,
                            Skill.DISCIPLINE_ONE,
                            PRECEPT_DAMAGE_BONUS
                    ));
                    event.setAmount(event.getAmount() * (1.0f + bonus));
                }
                UUID uuid = player.getUUID();
                boolean ultimateActive = disciplineUltimateEnd.getOrDefault(uuid, 0L) >= now;
                if (ultimateActive && count > 0
                        && SignetUpgradeData.isDisciplineSecondarySkillUnlocked(
                        player, Skill.DISCIPLINE_FOUR)) {
                    event.setAmount(event.getAmount() * (1.0F + count
                            * SignetUpgradeData.scaleBonus(
                            player, Skill.DISCIPLINE_FOUR, 0.003F)));
                }
                if (SignetUpgradeData.isDisciplineSecondarySkillUnlocked(
                        player, Skill.DISCIPLINE_SIX)
                        && disciplineUnyieldingEnd.getOrDefault(uuid, 0L) >= now) {
                    event.setAmount(event.getAmount() * (1.0F
                            + SignetUpgradeData.scaleBonus(
                            player, Skill.DISCIPLINE_SIX, 0.40F)));
                }
                boolean asceticism = SignetUpgradeData.getDisciplineCore(player)
                        == SignetUpgradeData.DisciplineCore.ASCETICISM && count >= 90;
                if (asceticism) {
                    int signetCount = getDisciplineSignetCount(player);
                    event.setAmount(event.getAmount() * (1.25F + signetCount * 0.05F));
                    if (ultimateActive) {
                        disciplineUltimatePenetrationEnd.put(uuid, now + 400L);
                        if (SignetUpgradeData.hasDisciplineCoreSignet(
                                player, Skill.DISCIPLINE_ASCETIC_PROOF)) {
                            event.setAmount(event.getAmount() * (1.0F
                                    + SignetUpgradeData.scaleBonus(
                                    player, Skill.DISCIPLINE_ASCETIC_PROOF, 0.20F)));
                        }
                    }
                }
                event.setAmount(event.getAmount() * (1.0F
                        + disciplineMountainDamageBonus.getOrDefault(uuid, 0.0F)));
                if (disciplinePunishmentEnd.getOrDefault(uuid, 0L) >= now
                        && SignetUpgradeData.hasDisciplineCoreSignet(
                        player, Skill.DISCIPLINE_BLESSING_RETURN)) {
                    event.setAmount(event.getAmount() * (1.0F + disciplineAugmentValue(
                            player, Skill.DISCIPLINE_BLESSING_RETURN, 0.26F)));
                }
                Map<UUID, Integer> mountainHits = disciplineMountainHits.get(uuid);
                if (mountainHits != null
                        && mountainHits.getOrDefault(targetId, 0) >= 2
                        && SignetUpgradeData.hasDisciplineCoreSignet(
                        player, Skill.DISCIPLINE_BLESSING_PUNISHMENT)) {
                    event.setAmount(event.getAmount() * (1.0F + disciplineAugmentValue(
                            player, Skill.DISCIPLINE_BLESSING_PUNISHMENT, 0.26F)));
                }
                float penetration = 0.0F;
                if (disciplineUltimatePenetrationEnd.getOrDefault(uuid, 0L) >= now) {
                    penetration = combinePenetration(penetration, 0.40F);
                }
                if (asceticism && !ultimateActive
                        && event.getSource().getDirectEntity() == player
                        && SignetUpgradeData.hasDisciplineCoreSignet(
                        player, Skill.DISCIPLINE_ASCETIC_BODY)) {
                    penetration = combinePenetration(
                            penetration,
                            SignetUpgradeData.scaleBonus(
                                    player, Skill.DISCIPLINE_ASCETIC_BODY, 0.15F));
                }
                if (disciplineVirtuePenetrationEnd.getOrDefault(uuid, 0L) >= now) {
                    penetration = combinePenetration(
                            penetration,
                            disciplineVirtuePenetration.getOrDefault(uuid, 0.0F));
                }
                if (penetration > 0.0F) {
                    event.setAmount(applyArmorPenetration(
                            target, event.getSource(), event.getAmount(), penetration));
                }
            }
        }

        if (event.getEntity() instanceof Player player) {
            boolean has = CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DISCIPLINE.get()))
                    .isPresent();
            if (has) {
                int count = preceptCount.getOrDefault(player.getUUID(), 0);
                if (count > 0) {
                    float reduction = Math.min(PRECEPT_DEFENSE_CAP, (float) (count * signetBoost(
                            player,
                            Skill.DISCIPLINE_TWO,
                            PRECEPT_DEFENSE_BONUS
                    )));
                    event.setAmount(applyIndependentReduction(event.getAmount(), reduction));
                }
                boolean asceticism = SignetUpgradeData.getDisciplineCore(player)
                        == SignetUpgradeData.DisciplineCore.ASCETICISM && count >= 90;
                if (asceticism && SignetUpgradeData.hasDisciplineCoreSignet(
                        player, Skill.DISCIPLINE_ASCETIC_BODY)) {
                    event.setAmount(event.getAmount() * (1.0F + SignetUpgradeData.scaleBonus(
                            player, Skill.DISCIPLINE_ASCETIC_BODY, 0.15F)));
                }
                if (asceticism && SignetUpgradeData.hasDisciplineCoreSignet(
                        player, Skill.DISCIPLINE_ASCETIC_PROOF)) {
                    event.setAmount(event.getAmount() * (1.0F + SignetUpgradeData.scaleBonus(
                            player, Skill.DISCIPLINE_ASCETIC_PROOF, 0.15F)));
                }
                UUID uuid = player.getUUID();
                if (disciplinePunishmentEnd.getOrDefault(uuid, 0L) >= now
                        && SignetUpgradeData.hasDisciplineCoreSignet(
                        player, Skill.DISCIPLINE_BLESSING_RETURN)) {
                    event.setAmount(applyIndependentReduction(
                            event.getAmount(),
                            disciplineAugmentValue(
                                    player, Skill.DISCIPLINE_BLESSING_RETURN, 0.20F)));
                    Entity source = event.getSource().getEntity();
                    if (source instanceof LivingEntity living
                            && living != player
                            && !PlayerControlledAllies.isProtectedEffectTarget(player, living)
                            && disciplineRetaliationNext.getOrDefault(uuid, 0L) <= now
                            && disciplineRetaliationGuard.add(uuid)) {
                        try {
                            float retaliation = (float) player.getAttributeValue(
                                    Attributes.ATTACK_DAMAGE) * disciplineAugmentValue(
                                    player, Skill.DISCIPLINE_BLESSING_RETURN, 7.0F);
                            PlayerControlledAllies.hurt(player, living,
                                    player.damageSources().playerAttack(player), retaliation);
                            disciplineRetaliationNext.put(uuid, now + 100L);
                        } finally {
                            disciplineRetaliationGuard.remove(uuid);
                        }
                    }
                }
            }
        }
    }

    private static float combinePenetration(float current, float added) {
        return 1.0F - (1.0F - Mth.clamp(current, 0.0F, 1.0F))
                * (1.0F - Mth.clamp(added, 0.0F, 1.0F));
    }

    public static float applyArmorPenetration(
            LivingEntity target,
            DamageSource source,
            float damage,
            float penetration
    ) {
        if (target == null || source == null || damage <= 0.0F) return damage;
        PenetrationContext context = findPenetrationContext(target, source);
        if (context == null) return damage;
        context.penetration = combinePenetration(context.penetration, penetration);
        return damage;
    }

    public static void activateDeliveranceUltimateBuff(
            Player player
    ) {
        deliveranceFenghuangEnergyTargets.remove(player.getUUID());
        deliveranceAbyssEnergyTargets.remove(player.getUUID());
        deliveranceSkyfireEnergyTargets.remove(player.getUUID());
        deliveranceEdenEnergyTargets.remove(player.getUUID());
        activateDeliveranceUltimateBuffInternal(player);
    }

    public static void activateDeliveranceUltimateBuffFromFenghuangDown(Player player) {
        deliveranceAbyssEnergyTargets.remove(player.getUUID());
        deliveranceSkyfireEnergyTargets.remove(player.getUUID());
        deliveranceEdenEnergyTargets.remove(player.getUUID());
        deliveranceFenghuangEnergyTargets.add(player.getUUID());
        activateDeliveranceUltimateBuffInternal(player);
    }

    public static void activateDeliveranceUltimateBuffFromAbyssFlower(Player player) {
        deliveranceFenghuangEnergyTargets.remove(player.getUUID());
        deliveranceSkyfireEnergyTargets.remove(player.getUUID());
        deliveranceEdenEnergyTargets.remove(player.getUUID());
        deliveranceAbyssEnergyTargets.add(player.getUUID());
        activateDeliveranceUltimateBuffInternal(player);
    }

    public static void activateDeliveranceUltimateBuffFromSkyfire(Player player) {
        deliveranceFenghuangEnergyTargets.remove(player.getUUID());
        deliveranceAbyssEnergyTargets.remove(player.getUUID());
        deliveranceEdenEnergyTargets.remove(player.getUUID());
        deliveranceSkyfireEnergyTargets.add(player.getUUID());
        activateDeliveranceUltimateBuffInternal(player);
    }

    public static void activateDeliveranceUltimateBuffFromEdenStar(Player player) {
        deliveranceFenghuangEnergyTargets.remove(player.getUUID());
        deliveranceAbyssEnergyTargets.remove(player.getUUID());
        deliveranceSkyfireEnergyTargets.remove(player.getUUID());
        deliveranceEdenEnergyTargets.add(player.getUUID());
        activateDeliveranceUltimateBuffInternal(player);
    }

    private static void activateDeliveranceUltimateBuffInternal(Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        int durationTicks = activateDeliveranceCore(player);
        activateDeliveranceSecondaryEffects(player, durationTicks);
        deliveranceBuffEnd.put(
                player.getUUID(),
                player.level().getGameTime() + durationTicks
        );
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(DELIVERANCE_ULTIMATE_BUFF_UUID);
        }
    }

    private static int activateDeliveranceCore(Player player) {
        long gameTime = player.level().getGameTime();
        UUID uuid = player.getUUID();
        SignetUpgradeData.DeliveranceCore core =
                SignetUpgradeData.getDeliveranceCore(player);
        if (core == SignetUpgradeData.DeliveranceCore.KINGS_SWORD) {
            int durationSeconds = 7;
            if (SignetUpgradeData.hasDeliveranceCoreSignet(
                    player,
                    Skill.DELIVERANCE_KINGS_ECHO
            )) {
                durationSeconds = 9 + SignetUpgradeData.getPlayerLevel(
                        player,
                        Skill.DELIVERANCE_KINGS_ECHO
                );
            }
            int durationTicks = durationSeconds * 20;
            deliveranceFinalBattleEnd.put(uuid, gameTime + durationTicks);
            deliveranceFinalBattleDamage.put(uuid, new HashMap<>());
            player.addEffect(
                    new MobEffectInstance(
                            ModEffects.DELIVERANCE_FINAL_BATTLE.get(),
                            durationTicks,
                            0,
                            false,
                            false,
                            true
                    )
            );
            return durationTicks;
        }
        if (core == SignetUpgradeData.DeliveranceCore.LONE_SHADOW) {
            deliveranceLoneHitWindowEnd.put(uuid, gameTime + 100L);
            deliveranceLoneHitCount.put(uuid, 0);
        }
        return DELIVERANCE_SECONDARY_DURATION_TICKS;
    }

    private static void activateDeliveranceSecondaryEffects(
            Player player,
            int durationTicks
    ) {
        long gameTime = player.level().getGameTime();
        UUID uuid = player.getUUID();

        if (SignetUpgradeData.isSecondarySkillUnlocked(
                player,
                Skill.DELIVERANCE_HUNTERS_MASK
        )) {
            deliveranceHuntersMaskEnd.put(
                    uuid,
                    gameTime + durationTicks
            );
        }

        if (SignetUpgradeData.isSecondarySkillUnlocked(
                player,
                Skill.DELIVERANCE_RESTRAINERS_RELIC
        )) {
            float shield = player.getMaxHealth() * deliveranceShieldRatio(player);
            deliveranceHolyShieldAmount.put(uuid, shield);
            deliveranceHolyShieldEnd.put(
                    uuid,
                    gameTime + durationTicks
            );
        }

        if (SignetUpgradeData.isSecondarySkillUnlocked(
                player,
                Skill.DELIVERANCE_SEEKERS_ROBE
        )) {
            deliveranceSeekersRobeEnd.put(
                    uuid,
                    gameTime + durationTicks
            );
            deliveranceSeekersRobeNextPulse.put(uuid, gameTime + 20L);
        }
    }

    private static void handleDeliveranceSecondaryTick(Player player) {
        UUID uuid = player.getUUID();
        long gameTime = player.level().getGameTime();

        AttributeInstance knockbackResistance =
                player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackResistance != null) {
            knockbackResistance.removeModifier(DELIVERANCE_ANTI_STAGGER_UUID);
            Long shieldEnd = deliveranceHolyShieldEnd.get(uuid);
            if (shieldEnd != null && gameTime <= shieldEnd
                    && deliveranceHolyShieldAmount.getOrDefault(uuid, 0.0F) > 0.0F) {
                knockbackResistance.addTransientModifier(
                        new AttributeModifier(
                                DELIVERANCE_ANTI_STAGGER_UUID,
                                "deliverance_anti_stagger",
                                1.0D,
                                AttributeModifier.Operation.ADDITION
                        )
                );
            }
        }

        Long shieldEnd = deliveranceHolyShieldEnd.get(uuid);
        if (shieldEnd != null && gameTime > shieldEnd) {
            deliveranceHolyShieldEnd.remove(uuid);
            deliveranceHolyShieldAmount.remove(uuid);
        }

        Long maskEnd = deliveranceHuntersMaskEnd.get(uuid);
        if (maskEnd != null && gameTime > maskEnd) {
            deliveranceHuntersMaskEnd.remove(uuid);
        }

        Long robeEnd = deliveranceSeekersRobeEnd.get(uuid);
        if (robeEnd == null) return;
        if (gameTime > robeEnd) {
            deliveranceSeekersRobeEnd.remove(uuid);
            deliveranceSeekersRobeNextPulse.remove(uuid);
            return;
        }

        long nextPulse = deliveranceSeekersRobeNextPulse.getOrDefault(uuid, gameTime + 20L);
        if (gameTime < nextPulse) return;
        if (deliveranceAbyssEnergyTargets.contains(uuid)) {
            ItemStack weapon = AbyssFlowerEnergy.find(player);
            if (!weapon.isEmpty()) AbyssFlowerEnergy.add(player, weapon, deliveranceEnergyPerSecond(player));
        } else if (deliveranceFenghuangEnergyTargets.contains(uuid)) {
            ItemStack weapon = FenghuangDownEnergy.find(player);
            if (!weapon.isEmpty()) {
                FenghuangDownEnergy.addEnergy(
                        player,
                        weapon,
                        deliveranceEnergyPerSecond(player)
                );
            }
        } else if (deliveranceSkyfireEnergyTargets.contains(uuid)) {
            ItemStack weapon = SkyfireGreatswordEnergy.find(player);
            if (!weapon.isEmpty()) {
                SkyfireGreatswordEnergy.add(player, weapon, deliveranceEnergyPerSecond(player));
            }
        } else if (deliveranceEdenEnergyTargets.contains(uuid)) {
            ItemStack weapon = EdenStarEnergy.find(player);
            if (!weapon.isEmpty()) {
                EdenStarEnergy.add(player, weapon, deliveranceEnergyPerSecond(player));
            }
        } else {
            ItemStack weapon = findFlawlessWeapon(player);
            if (!weapon.isEmpty()) {
                FlawlessWeaponEnergy.addEnergy(
                        player,
                        weapon,
                        deliveranceEnergyPerSecond(player)
                );
            }
        }
        deliveranceSeekersRobeNextPulse.put(uuid, nextPulse + 20L);
    }

    private static void handleDeliveranceCoreTick(Player player, boolean hasDeliverance) {
        UUID uuid = player.getUUID();
        long gameTime = player.level().getGameTime();

        if (!hasDeliverance) {
            resetDeliveranceCoreRuntime(player);
            AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.removeModifier(DELIVERANCE_ULTIMATE_BUFF_UUID);
            }
            deliveranceBuffEnd.remove(uuid);
            deliveranceFenghuangEnergyTargets.remove(uuid);
            deliveranceAbyssEnergyTargets.remove(uuid);
            deliveranceSkyfireEnergyTargets.remove(uuid);
            deliveranceEdenEnergyTargets.remove(uuid);
            return;
        }

        Long finalBattleEnd = deliveranceFinalBattleEnd.get(uuid);
        if (finalBattleEnd != null && gameTime > finalBattleEnd) {
            finishDeliveranceFinalBattle(player);
        }

        Long hitWindowEnd = deliveranceLoneHitWindowEnd.get(uuid);
        if (hitWindowEnd != null && gameTime > hitWindowEnd) {
            deliveranceLoneHitWindowEnd.remove(uuid);
            deliveranceLoneHitCount.remove(uuid);
        }

        Long saviorBattleEnd = deliveranceSaviorBattleEnd.get(uuid);
        if (saviorBattleEnd != null && gameTime > saviorBattleEnd) {
            deliveranceSaviorBattleEnd.remove(uuid);
            deliveranceDecisionDamageBonus.remove(uuid);
            deliveranceTriumphTriggered.remove(uuid);
            player.displayClientMessage(
                    ModText.literal("救世之战结束").withStyle(ChatFormatting.GRAY),
                    true
            );
        }

        Long triumphEnd = deliveranceTriumphIgnoreDefenseEnd.get(uuid);
        if (triumphEnd != null && gameTime > triumphEnd) {
            deliveranceTriumphIgnoreDefenseEnd.remove(uuid);
            applyDeliveranceTriumphVulnerability(player);
        }

        Long buffEnd = deliveranceBuffEnd.get(uuid);
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(DELIVERANCE_ULTIMATE_BUFF_UUID);
        }
        if (buffEnd != null && gameTime > buffEnd) {
            deliveranceBuffEnd.remove(uuid);
            deliveranceFenghuangEnergyTargets.remove(uuid);
            deliveranceAbyssEnergyTargets.remove(uuid);
            deliveranceSkyfireEnergyTargets.remove(uuid);
            deliveranceEdenEnergyTargets.remove(uuid);
        }

        if (player.tickCount % 200 == 0) {
            deliveranceTriumphVulnerabilities.entrySet().removeIf(entry -> {
                entry.getValue().values().removeIf(vulnerability -> vulnerability.end() < gameTime);
                return entry.getValue().isEmpty();
            });
        }
    }

    private static void startDeliveranceSaviorBattle(Player player) {
        UUID uuid = player.getUUID();
        long gameTime = player.level().getGameTime();
        int durationTicks = 200;
        long end = gameTime + durationTicks;
        deliveranceLoneHitWindowEnd.remove(uuid);
        deliveranceLoneHitCount.remove(uuid);
        deliveranceSaviorBattleEnd.put(uuid, end);
        deliveranceDecisionDamageBonus.put(uuid, 0.0F);
        deliveranceTriumphTriggered.remove(uuid);
        deliveranceTriumphIgnoreDefenseEnd.remove(uuid);
        deliveranceBuffEnd.put(uuid, end);
        activateDeliveranceSecondaryEffects(player, durationTicks);
        player.displayClientMessage(
                ModText.literal("救世之战").withStyle(ChatFormatting.DARK_BLUE),
                true
        );
    }

    private static void finishDeliveranceFinalBattle(Player player) {
        UUID uuid = player.getUUID();
        deliveranceFinalBattleEnd.remove(uuid);
        player.removeEffect(
                ModEffects.DELIVERANCE_FINAL_BATTLE.get()
        );
        Map<UUID, Float> accumulated = deliveranceFinalBattleDamage.remove(uuid);
        if (accumulated == null
                || accumulated.isEmpty()
                || !SignetUpgradeData.hasDeliveranceCoreSignet(
                player,
                Skill.DELIVERANCE_KINGS_MUSTER
        )
                || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float ratio = SignetUpgradeData.scaleBonus(
                player,
                Skill.DELIVERANCE_KINGS_MUSTER,
                0.50F
        );
        deliveranceFinalBurstGuard.add(uuid);
        try {
            for (Map.Entry<UUID, Float> entry : accumulated.entrySet()) {
                Entity target = serverLevel.getEntity(entry.getKey());
                if (target instanceof LivingEntity living
                        && PlayerControlledAllies.isValidEnemy(player, living)) {
                    PlayerControlledAllies.hurt(player, living,
                            player.damageSources().playerAttack(player),
                            Math.max(0.0F, entry.getValue()) * ratio);
                }
            }
        } finally {
            deliveranceFinalBurstGuard.remove(uuid);
        }
    }

    private static void applyDeliveranceTriumphVulnerability(Player player) {
        if (!SignetUpgradeData.hasDeliveranceCoreSignet(
                player,
                Skill.DELIVERANCE_LONE_TRIUMPH
        )) {
            return;
        }
        float vulnerability = switch (SignetUpgradeData.getPlayerLevel(
                player,
                Skill.DELIVERANCE_LONE_TRIUMPH
        )) {
            case 1 -> 0.23F;
            case 2 -> 0.26F;
            case 3 -> 0.30F;
            default -> 0.20F;
        };
        long now = player.level().getGameTime();
        long end = now + 200L;
        deliveranceTriumphOwners.add(player.getUUID());
        player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(64.0D),
                entity -> PlayerControlledAllies.isValidEnemy(player, entity)
        ).forEach(entity -> {
            UUID targetId = entity.getUUID();
            deliveranceTriumphVulnerabilities
                    .computeIfAbsent(targetId, ignored -> new HashMap<>())
                    .merge(player.getUUID(),
                            new DeliveranceTriumphVulnerability(vulnerability, end),
                            (oldValue, newValue) -> oldValue.end() < now
                                    ? newValue : new DeliveranceTriumphVulnerability(
                                            Math.max(oldValue.amount(), newValue.amount()),
                                            Math.max(oldValue.end(), newValue.end())));
        });
    }

    public static void resetDeliveranceCoreRuntime(Player player) {
        if (player == null) return;
        UUID uuid = player.getUUID();
        deliveranceFinalBattleEnd.remove(uuid);
        deliveranceFinalBattleDamage.remove(uuid);
        player.removeEffect(
                ModEffects.DELIVERANCE_FINAL_BATTLE.get()
        );
        deliveranceLoneHitWindowEnd.remove(uuid);
        deliveranceLoneHitCount.remove(uuid);
        deliveranceSaviorBattleEnd.remove(uuid);
        deliveranceDecisionDamageBonus.remove(uuid);
        deliveranceTriumphIgnoreDefenseEnd.remove(uuid);
        deliveranceTriumphTriggered.remove(uuid);
        deliveranceFinalBurstGuard.remove(uuid);
        deliveranceFenghuangEnergyTargets.remove(uuid);
        deliveranceAbyssEnergyTargets.remove(uuid);
        deliveranceSkyfireEnergyTargets.remove(uuid);
        deliveranceEdenEnergyTargets.remove(uuid);
        if (deliveranceTriumphOwners.remove(uuid)) {
            deliveranceTriumphVulnerabilities.entrySet().removeIf(entry -> {
                entry.getValue().remove(uuid);
                return entry.getValue().isEmpty();
            });
        }
    }

    private static ItemStack findFlawlessWeapon(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) return mainHand;
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) return offhand;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static float deliverancePenetration(Player player) {
        float value = switch (SignetUpgradeData.getPlayerLevel(player, Skill.DELIVERANCE_HUNTERS_MASK)) {
            case 1 -> 0.20F;
            case 2 -> 0.25F;
            case 3 -> 0.30F;
            default -> 0.15F;
        };
        return value * deliveranceOrdinaryMultiplier(player);
    }

    private static float deliveranceShieldRatio(Player player) {
        float value = switch (SignetUpgradeData.getPlayerLevel(player, Skill.DELIVERANCE_RESTRAINERS_RELIC)) {
            case 1 -> 0.25F;
            case 2 -> 0.30F;
            case 3 -> 0.35F;
            default -> 0.20F;
        };
        return value * deliveranceOrdinaryMultiplier(player);
    }

    private static int deliveranceEnergyPerSecond(Player player) {
        int value = switch (SignetUpgradeData.getPlayerLevel(player, Skill.DELIVERANCE_SEEKERS_ROBE)) {
            case 1 -> 4;
            case 2 -> 5;
            case 3 -> 6;
            default -> 3;
        };
        return Math.max(1, Math.round(value * deliveranceOrdinaryMultiplier(player)));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeliveranceHuntersMaskDamage(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (PlayerControlledAllies.isElysianHero(target)) return;
        long gameTime = target.level().getGameTime();
        UUID targetId = target.getUUID();
        Map<UUID, DeliveranceTriumphVulnerability> vulnerabilities =
                deliveranceTriumphVulnerabilities.get(targetId);
        if (vulnerabilities != null) {
            float strongest = 0.0F;
            for (DeliveranceTriumphVulnerability vulnerability : vulnerabilities.values()) {
                if (gameTime <= vulnerability.end()) {
                    strongest = Math.max(strongest, vulnerability.amount());
                }
            }
            if (strongest > 0.0F) {
                event.setAmount(event.getAmount() * (1.0F + strongest));
            }
        }

        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (event.getAmount() <= 0.0F) return;
        UUID uuid = player.getUUID();

        Long buffEnd = deliveranceBuffEnd.get(uuid);
        if (buffEnd != null && gameTime <= buffEnd) {
            event.setAmount(event.getAmount() * (1.0F + signetBoost(
                    player,
                    Skill.DELIVERANCE_ULTIMATE,
                    0.25F
            )));
        }

        Long maskEnd = deliveranceHuntersMaskEnd.get(uuid);
        if (maskEnd != null && gameTime <= maskEnd) {
            event.setAmount(event.getAmount() * (1.0F + deliverancePenetration(player)));
        }

        Long finalBattleEnd = deliveranceFinalBattleEnd.get(uuid);
        if (finalBattleEnd != null
                && gameTime <= finalBattleEnd
                && SignetUpgradeData.hasDeliveranceCoreSignet(
                player,
                Skill.DELIVERANCE_KINGS_EXPEDITION
        )) {
            event.setAmount(event.getAmount() * (1.0F + SignetUpgradeData.scaleBonus(
                    player,
                    Skill.DELIVERANCE_KINGS_EXPEDITION,
                    0.20F
            )));
        }

        Long saviorBattleEnd = deliveranceSaviorBattleEnd.get(uuid);
        if (saviorBattleEnd != null && gameTime <= saviorBattleEnd) {
            if (SignetUpgradeData.hasDeliveranceCoreSignet(
                    player,
                    Skill.DELIVERANCE_LONE_DECISION
            )) {
                float current = deliveranceDecisionDamageBonus.getOrDefault(uuid, 0.0F);
                event.setAmount(event.getAmount() * (1.0F + current));
                float step = SignetUpgradeData.scaleBonus(
                        player,
                        Skill.DELIVERANCE_LONE_DECISION,
                        0.02F
                );
                deliveranceDecisionDamageBonus.put(uuid, Math.min(0.50F, current + step));
            }

            if (SignetUpgradeData.hasDeliveranceCoreSignet(
                    player,
                    Skill.DELIVERANCE_LONE_TRIUMPH
            )) {
                if (deliveranceTriumphTriggered.add(uuid)) {
                    int seconds = 4 + SignetUpgradeData.getPlayerLevel(
                            player,
                            Skill.DELIVERANCE_LONE_TRIUMPH
                    );
                    deliveranceTriumphIgnoreDefenseEnd.put(uuid, gameTime + seconds * 20L);
                } else if (gameTime <= deliveranceTriumphIgnoreDefenseEnd.getOrDefault(uuid, 0L)
                        && !event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
                    event.setAmount(compensateArmorForStarsIgnoreDefense(
                            target,
                            event.getSource(),
                            event.getAmount()
                    ));
                }
            }
        }

        Long hitWindowEnd = deliveranceLoneHitWindowEnd.get(uuid);
        if (hitWindowEnd != null
                && gameTime <= hitWindowEnd
                && SignetUpgradeData.getDeliveranceCore(player)
                == SignetUpgradeData.DeliveranceCore.LONE_SHADOW) {
            int hits = deliveranceLoneHitCount.merge(uuid, 1, Integer::sum);
            if (hits >= 20) {
                startDeliveranceSaviorBattle(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeliveranceFinalBattleDamage(LivingDamageEvent event) {
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        if (!(event.getSource().getEntity() instanceof Player player)
                || event.getAmount() <= 0.0F) {
            return;
        }
        UUID uuid = player.getUUID();
        long gameTime = player.level().getGameTime();
        if (deliveranceFinalBurstGuard.contains(uuid)
                || deliveranceFinalBattleEnd.getOrDefault(uuid, 0L) < gameTime) {
            return;
        }
        deliveranceFinalBattleDamage
                .computeIfAbsent(uuid, ignored -> new HashMap<>())
                .merge(event.getEntity().getUUID(), event.getAmount(), Float::sum);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeliveranceHolyShieldDamage(LivingDamageEvent event) {
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        UUID uuid = player.getUUID();
        Long end = deliveranceHolyShieldEnd.get(uuid);
        if (end == null || player.level().getGameTime() > end) return;
        float shield = deliveranceHolyShieldAmount.getOrDefault(uuid, 0.0F);
        if (shield <= 0.0F || event.getAmount() <= 0.0F) return;
        float absorbed = Math.min(shield, event.getAmount());
        float remaining = shield - absorbed;
        event.setAmount(event.getAmount() - absorbed);
        if (remaining <= 0.0F) {
            deliveranceHolyShieldAmount.remove(uuid);
            deliveranceHolyShieldEnd.remove(uuid);
        } else {
            deliveranceHolyShieldAmount.put(uuid, remaining);
        }
    }

    @Deprecated
    public static void activateDeliveranceBuff(
            Player player
    ) {
        activateDeliveranceUltimateBuff(player);
    }

    @SubscribeEvent
    public static void onInfinityDamageBoost(LivingDamageEvent event) {
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        boolean hasSignet = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_INFINITY.get()))
                .isPresent();
        if (!hasSignet) return;

        int allyCount = (int) player.level()
                .getEntitiesOfClass(TamableAnimal.class,
                        player.getBoundingBox().inflate(32.0),
                        e -> e.isTame()
                                && player.getUUID().equals(e.getOwnerUUID())
                                && e.isAlive())
                .stream().count();
        allyCount = Math.min(allyCount, 3);
        if (allyCount > 0) {
            event.setAmount(event.getAmount() * signetBonusMultiplier(
                    player,
                    Skill.INFINITY_ALLY,
                    0.05F * allyCount
            ));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEgoGoldWaterBreathing(
            TickEvent.PlayerTickEvent event
    ) {
        if (
                event.phase != TickEvent.Phase.END ||
                event.side != LogicalSide.SERVER
        ) {
            return;
        }

        Player player = event.player;

        if (!hasEgoAnd(
                player,
                ModItems.SIGNET_OF_GOLD.get()
        )) {
            return;
        }

        MobEffectInstance currentWaterBreathing =
                player.getEffect(MobEffects.WATER_BREATHING);

        if (
                currentWaterBreathing == null ||
                currentWaterBreathing.getDuration() < 220
        ) {
            player.addEffect(
                    new MobEffectInstance(
                            MobEffects.WATER_BREATHING,
                            400,
                            0,
                            false,
                            false,
                            false
                    )
            );
        }

        if (player.isUnderWater()) {
            player.setAirSupply(player.getMaxAirSupply());
        }
    }

    @SubscribeEvent(
            priority = EventPriority.LOWEST,
            receiveCanceled = true
    )
    public static void onEgoKnockback(
            LivingKnockBackEvent event
    ) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!hasEgoAnd(
                player,
                ModItems.SIGNET_OF_VICISSITUDE.get()
        )) {
            return;
        }

        event.setStrength(0.0F);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEgoFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasEgoAnd(player, ModItems.SIGNET_OF_REVERIE.get())) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEgoHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;

        if (event.getEntity() instanceof Player player) {
            if (hasEgoAnd(player, ModItems.SIGNET_OF_VICISSITUDE.get())) {
                event.setAmount(applyIndependentReduction(
                        event.getAmount(),
                        0.48F
                ));
            }
        }

        if (event.getSource().getEntity() instanceof TamableAnimal pet && pet.isTame()) {
            if (pet.getOwner() instanceof Player owner) {
                if (hasEgoAnd(owner, ModItems.SIGNET_OF_INFINITY.get())) {
                    event.setAmount(event.getAmount() * 1.31415926F);
                }
            }
        }

        if (event.getSource().getEntity() instanceof Player player) {
            if (hasEgoAnd(player, ModItems.SIGNET_OF_STARS.get())) {
                LivingEntity target = event.getEntity();
                float boostedDamage = event.getAmount() * 1.5F;
                event.setAmount(applyArmorPenetration(
                        target, event.getSource(), boostedDamage, 0.50F));
            }
        }
    }

    @SubscribeEvent(
            priority = EventPriority.LOWEST,
            receiveCanceled = true
    )
    public static void onLivingAttack(
            LivingAttackEvent event
    ) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        UUID uuid = player.getUUID();
        boolean hasVicissitude = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_VICISSITUDE.get()))
                .isPresent();
        if (hasVicissitude) {
            strengthNoHitTicks.put(uuid, 0);
            resistanceNoHitTicks.put(uuid, 0);
            strengthStacks.put(uuid, 0);
            resistanceStacks.put(uuid, 0);
            AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attack != null) attack.removeModifier(VICISSITUDE_STRENGTH_UUID);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUUID();

        float energy = corruptionEnergy.getOrDefault(uuid, loadCorruptionEnergy(player));
        saveCorruptionEnergy(player, energy);
        corruptionEnergy.remove(uuid);

        strengthNoHitTicks.remove(uuid);
        resistanceNoHitTicks.remove(uuid);
        strengthStacks.remove(uuid);
        resistanceStacks.remove(uuid);
        EVADE_COOLDOWNS.remove(uuid);
        EVADE_BUFF_END.remove(uuid);
        egoFlightTicks.remove(uuid);
        effectDurationRescaleGuard.remove(uuid);
        AttributeInstance maxHp = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHp != null) maxHp.removeModifier(DECIMATION_HEALTH_UUID);
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.removeModifier(VICISSITUDE_STRENGTH_UUID);
    }

    private static void randomTeleportAndSpawnMob(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = player.blockPosition().offset(
                RAND.nextInt(3) - 1,
                RAND.nextInt(3) - 1,
                RAND.nextInt(3) - 1
        );

        player.randomTeleport(
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                true
        );

        Zombie zombie = EntityType.ZOMBIE.create(serverLevel);
        if (zombie != null) {
            zombie.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            zombie.setTarget(player);
            serverLevel.addFreshEntity(zombie);
        }
    }

    private static void spawnHallucination(Player player) {
        Phantom phantom = EntityType.PHANTOM.create(player.level());
        if (phantom != null) {
            phantom.moveTo(player.getX()+RAND.nextInt(5)-2, player.getY()+2, player.getZ()+RAND.nextInt(5)-2);
            phantom.setTarget(player);
            player.level().addFreshEntity(phantom);
        }
    }

    private static void spawnCatalystMobs(Player player) {
        for (int i=0; i<2; i++) {
            Zombie zombie = EntityType.ZOMBIE.create(player.level());
            if (zombie != null) {
                zombie.moveTo(player.getX()+RAND.nextInt(5)-2, player.getY(), player.getZ()+RAND.nextInt(5)-2);
                zombie.setTarget(player);
                player.level().addFreshEntity(zombie);
            }
        }
    }

    private static boolean hasCocoonNearby(Level level, BlockPos pos, double range) {
        return findNearestCocoonPlayer(level, pos, range) != null;
    }

    private static Player findNearestCocoonPlayer(
            Level level,
            BlockPos pos,
            double range
    ) {
        List<Player> players = level.getEntitiesOfClass(
                Player.class,
                new net.minecraft.world.phys.AABB(pos).inflate(range),
                player -> player.isAlive() && hasCocoon(player)
        );

        if (players.isEmpty()) {
            return null;
        }

        return players.stream()
                .min(Comparator.comparingDouble(
                        player -> player.distanceToSqr(
                                pos.getX() + 0.5D,
                                pos.getY() + 0.5D,
                                pos.getZ() + 0.5D
                        )
                ))
                .orElse(null);
    }

    private static void damageArmorForDaybreak(LivingEntity target, int stacks, Player attacker) {
        if (stacks <= 0) return;
        int durabilityDamage = Math.max(1, (int) Math.ceil(
                signetBoost(
                        attacker,
                        Skill.DAYBREAK_EYE,
                        stacks * 0.05F * 4.0F
                )
        ));
        damageArmorSet(target, durabilityDamage);
    }

    private static void damageArmorSet(LivingEntity target, int amount) {
        if (amount <= 0) return;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                stack.hurtAndBreak(amount, target, entity -> entity.broadcastBreakEvent(slot));
            }
        }
    }

    private static void damageRandomArmorSeverely(Player player) {
        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        List<EquipmentSlot> validSlots = new ArrayList<>();
        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                validSlots.add(slot);
            }
        }

        if (validSlots.isEmpty()) {
            return;
        }

        EquipmentSlot slot = validSlots.get(RAND.nextInt(validSlots.size()));
        ItemStack stack = player.getItemBySlot(slot);
        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();

        float durabilityLossPercent = Math.min(
                0.50F,
                CocoonCoreProgress.scalePercent(player, 0.20F)
        );

        int damage = Math.max(
                1,
                Math.round(remainingDurability * durabilityLossPercent)
        );

        stack.hurtAndBreak(
                damage,
                player,
                entity -> entity.broadcastBreakEvent(slot)
        );

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.ITEM_BREAK,
                SoundSource.PLAYERS,
                0.8F,
                0.9F
        );
    }

    private static void damageHeldItem(Player player, int amount) {
        if (amount <= 0) return;
        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.isDamageableItem()) {
            stack.hurtAndBreak(amount, player, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
    }

    private static void sendIntoVoid(Player player) {
        double y = player.level().getMinBuildHeight() - 8.0;
        player.teleportTo(player.getX(), y, player.getZ());
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false));
        player.displayClientMessage(ModText.literal("空之诅咒将你拖入了虚空……").withStyle(ChatFormatting.DARK_PURPLE), true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }

        UUID uuid = player.getUUID();
        if (effectDurationRescaleGuard.remove(uuid)) {
            return;
        }

        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || instance.getDuration() <= 1) {
            return;
        }

        float durationMultiplier = 1.0F;
        MobEffectCategory category = instance.getEffect().getCategory();

        if (category == MobEffectCategory.BENEFICIAL
                && hasItem(player, ModItems.SIGNET_OF_GOLD.get())) {
            durationMultiplier *= signetBonusMultiplier(
                    player,
                    Skill.GOLD_STREAM,
                    0.30F
            );
        }

        if (category == MobEffectCategory.HARMFUL
                && hasItem(player, ModItems.SIGNET_OF_DELIVERANCE.get())) {
            durationMultiplier *= signetReductionMultiplier(
                    player,
                    Skill.DELIVERANCE_EFFECT,
                    0.30F
            );
        }

        if (isWearingCocoon(player)) {
            if (CocoonTrialProgress.isComplete(player, CocoonTrialProgress.SENTIENCE)) {
                durationMultiplier *= category == MobEffectCategory.BENEFICIAL
                        ? 1.20F
                        : category == MobEffectCategory.HARMFUL ? 0.80F : 1.0F;
            } else if (category == MobEffectCategory.BENEFICIAL
                    && !CocoonTrialProgress.isComplete(player, CocoonTrialProgress.SENTIENCE)) {

                float reduction = Math.min(
                        0.90F,
                        CocoonCoreProgress.scalePercent(player, 0.15F)
                );
                durationMultiplier *= 1.0F - reduction;
            } else if (category == MobEffectCategory.HARMFUL) {

                durationMultiplier *= 1.0F
                        + CocoonCoreProgress.scalePercent(player, 0.15F);
            }
        }

        if (Math.abs(durationMultiplier - 1.0F) < 0.001F) {
            return;
        }

        int newDuration = Math.max(
                1,
                Math.round(instance.getDuration() * durationMultiplier)
        );

        MobEffectInstance adjusted = new MobEffectInstance(
                instance.getEffect(),
                newDuration,
                instance.getAmplifier(),
                instance.isAmbient(),
                instance.isVisible(),
                instance.showIcon()
        );

        effectDurationRescaleGuard.add(uuid);
        player.addEffect(adjusted, event.getEffectSource());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        boolean hasGold = CuriosApi.getCuriosHelper()
                .findFirstCurio(
                        player,
                        stack -> stack.is(ModItems.SIGNET_OF_GOLD.get())
                )
                .isPresent();

        if (!hasGold) {
            return;
        }

        BlockState state = event.getState();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId == null || !blockId.getPath().endsWith("_ore")) {
            return;
        }

        event.setCanceled(true);
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        List<ItemStack> drops = Block.getDrops(
                state,
                (ServerLevel) level,
                pos,
                level.getBlockEntity(pos),
                player,
                player.getMainHandItem()
        );

        int extraCopies = Math.max(
                1,
                Math.round(signetBoost(player, Skill.GOLD_STREAM, 1.0F))
        );

        for (ItemStack drop : drops) {
            Block.popResource(level, pos, drop);
            for (int i = 0; i < extraCopies; i++) {
                Block.popResource(level, pos, drop.copy());
            }
        }

        level.removeBlock(pos, false);
        state.getBlock().playerDestroy(
                level,
                player,
                pos,
                state,
                null,
                player.getMainHandItem()
        );
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        CompoundTag originalRootData = event.getOriginal().getPersistentData();
        CompoundTag originalPermanentData = getPermanentPlayerData(event.getOriginal());
        CompoundTag newPermanentData = getPermanentPlayerData(event.getEntity());
        if (originalRootData.getBoolean(NBT_GOT_SPAWN_INVINCIBILITY) || originalPermanentData.getBoolean(NBT_GOT_SPAWN_INVINCIBILITY)) {
            newPermanentData.putBoolean(NBT_GOT_SPAWN_INVINCIBILITY, true);
        }
        if (originalRootData.getBoolean(NBT_GOT_COCOON) || originalPermanentData.getBoolean(NBT_GOT_COCOON)) {
            newPermanentData.putBoolean(NBT_GOT_COCOON, true);
        }

        CompoundTag data = event.getEntity().getPersistentData();
        data.putInt(NBT_TUOLIN_STACKS, 0);
        data.putInt(NBT_TUOLIN_TICKS, 0);
        data.remove(NBT_TUOLIN_BASE_MAX);

        AttributeInstance hpAttr = event.getEntity().getAttribute(Attributes.MAX_HEALTH);
        if (hpAttr != null) {
            hpAttr.removeModifier(COCOON_HEALTH_UUID);
        }

        float oldEnergy = corruptionEnergy.getOrDefault(
                event.getOriginal().getUUID(),
                loadCorruptionEnergy(event.getOriginal())
        );
        float newEnergy = oldEnergy / 2.0f;
        corruptionEnergy.put(event.getEntity().getUUID(), newEnergy);
        saveCorruptionEnergy(event.getEntity(), newEnergy);
    }
}
