package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.AbyssFlowerEnergy;
import com.ovo.lastsongofelysian.util.EdenStarEnergy;
import com.ovo.lastsongofelysian.util.FenghuangDownEnergy;
import com.ovo.lastsongofelysian.util.FlawlessWeaponEnergy;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.SkyfireGreatswordEnergy;
import com.ovo.lastsongofelysian.util.WeaponEnergyGain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SetsunaSignetSystem {
    private static final long BUFF_DURATION = 160L;
    private static final long PENETRATION_DURATION = 100L;
    private static final long CURTAIN_DURATION = 60L;
    private static final int CURTAIN_MAX_PAUSE = 60;
    private static final long MOON_SAKE_COOLDOWN = 20L;
    private static final long YAKSHA_READY_INTERVAL = 200L;
    private static final int YAKSHA_HITS = 7;

    private static final Map<UUID, Integer> EXTRA_EVASION_CHARGES = new HashMap<>();
    private static final Map<UUID, TimedPower> PEONY_BUFF = new HashMap<>();
    private static final Map<UUID, TimedPower> RED_LEAVES_VULNERABILITY = new HashMap<>();
    private static final Map<UUID, Long> PENETRATION_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> MOON_SAKE_NEXT = new HashMap<>();
    private static final Map<UUID, Integer> CURTAIN_EVASIONS = new HashMap<>();
    private static final Map<UUID, CurtainState> CURTAINS = new HashMap<>();
    private static final Map<UUID, FrozenTarget> FROZEN_TARGETS = new HashMap<>();
    private static final Map<UUID, Long> YAKSHA_READY_AT = new HashMap<>();
    private static final Map<UUID, Integer> MYRIAD_EVASIONS = new HashMap<>();
    private static final Map<UUID, Integer> YAKSHA_STACKS = new HashMap<>();
    private static final ThreadLocal<Boolean> INTERNAL_DAMAGE = ThreadLocal.withInitial(() -> false);

    private SetsunaSignetSystem() {
    }

    public static float cooldownMultiplier(Player player) {
        if (!SignetUpgradeData.isSetsuraSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.SETSURA_WISTERIA)) return 1.0F;
        float reduction = SignetEffects.getScaledSignetValue(
                player, SignetUpgradeData.Skill.SETSURA_WISTERIA, 0.10F);
        return Math.max(0.05F, 1.0F - reduction);
    }

    public static boolean hasExtraEvasionCharge(Player player) {
        return SignetUpgradeData.isSetsuraSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.SETSURA_WISTERIA)
                && EXTRA_EVASION_CHARGES.getOrDefault(player.getUUID(), 0) > 0;
    }

    public static boolean consumeExtraEvasionCharge(Player player) {
        if (!hasExtraEvasionCharge(player)) return false;
        EXTRA_EVASION_CHARGES.put(
                player.getUUID(), EXTRA_EVASION_CHARGES.get(player.getUUID()) - 1);
        return true;
    }

    public static void resetExtraEvasionCharge(Player player) {
        if (SignetUpgradeData.isSetsuraSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.SETSURA_WISTERIA)) {
            EXTRA_EVASION_CHARGES.put(player.getUUID(), 1);
        } else {
            EXTRA_EVASION_CHARGES.remove(player.getUUID());
        }
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        UUID id = player.getUUID();
        PENETRATION_UNTIL.remove(id);
        MOON_SAKE_NEXT.remove(id);
        CURTAIN_EVASIONS.remove(id);
        CURTAINS.remove(id);
        FROZEN_TARGETS.entrySet().removeIf(entry -> id.equals(entry.getValue().owner()));
        YAKSHA_READY_AT.remove(id);
        MYRIAD_EVASIONS.remove(id);
        YAKSHA_STACKS.remove(id);
    }

    public static void onSuccessfulEvasion(Player player) {
        if (player.level().isClientSide() || !(player.level() instanceof ServerLevel level)) return;
        long now = level.getGameTime();

        if (SignetUpgradeData.isSetsuraSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.SETSURA_RED_LEAVES)) {
            float vulnerability = SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.SETSURA_RED_LEAVES, 0.10F);
            forEachEnemy(player, target -> RED_LEAVES_VULNERABILITY.merge(
                    target.getUUID(), new TimedPower(now + BUFF_DURATION, vulnerability),
                    (oldValue, newValue) -> new TimedPower(
                            Math.max(oldValue.end(), newValue.end()),
                            Math.max(oldValue.power(), newValue.power()))));
        }

        if (SignetUpgradeData.isSetsuraSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.SETSURA_PEONY)) {
            PEONY_BUFF.put(player.getUUID(), new TimedPower(
                    now + BUFF_DURATION,
                    SignetEffects.getScaledSignetValue(
                            player, SignetUpgradeData.Skill.SETSURA_PEONY, 0.20F)));
        }

        if (SignetUpgradeData.isSetsuraSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.SETSURA_IRIS)) {
            int energy = Math.max(1, Math.round(SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.SETSURA_IRIS, 8.0F)));
            addHeldEnergy(player, WeaponEnergyGain.apply(player, energy));
        }

        if (SignetUpgradeData.getSetsuraCore(player)
                == SignetUpgradeData.SetsuraCore.SAKURA_CURTAIN) {
            int evades = CURTAIN_EVASIONS.merge(player.getUUID(), 1, Integer::sum);
            if (evades >= 3 && !CURTAINS.containsKey(player.getUUID())) {
                CURTAIN_EVASIONS.put(player.getUUID(), 0);
                openCurtain(player, now);
            }
        }

        if (SignetUpgradeData.getSetsuraCore(player)
                == SignetUpgradeData.SetsuraCore.NIGHT_YAKSHA) {
            Long readyAt = YAKSHA_READY_AT.get(player.getUUID());
            if (readyAt == null) {
                YAKSHA_READY_AT.put(player.getUUID(), now + YAKSHA_READY_INTERVAL);
            } else if (now >= readyAt) {
                releaseKarmaHunt(player, false);
                YAKSHA_READY_AT.put(player.getUUID(), now + YAKSHA_READY_INTERVAL);
            }
            if (SignetUpgradeData.hasSetsuraCoreSignet(
                    player, SignetUpgradeData.Skill.SETSURA_YAKSHA_MOMENTARY_MYRIAD)) {
                int evades = MYRIAD_EVASIONS.merge(player.getUUID(), 1, Integer::sum);
                if (evades >= 3) {
                    MYRIAD_EVASIONS.put(player.getUUID(), 0);
                    releaseKarmaHunt(player, true);
                }
            }
        }
    }

    private static void openCurtain(Player player, long now) {
        CurtainState state = new CurtainState(
                now + CURTAIN_DURATION,
                CURTAIN_MAX_PAUSE,
                player.getX(), player.getZ());
        CURTAINS.put(player.getUUID(), state);
        forEachEnemy(player, target -> {
            if (!PlayerControlledAllies.isSignetTimeStopExempt(target, player.level().players())) {
                FROZEN_TARGETS.put(target.getUUID(),
                        new FrozenTarget(player.getUUID(), target.position()));
            }
        });
        if (SignetUpgradeData.hasSetsuraCoreSignet(
                player, SignetUpgradeData.Skill.SETSURA_CURTAIN_RAIN_FOUR_LIGHTS)) {
            resetRatedPowerCooldowns(player);
        }
    }

    private static void finishCurtain(Player player) {
        CURTAINS.remove(player.getUUID());
        FROZEN_TARGETS.entrySet().removeIf(entry -> entry.getValue().owner().equals(player.getUUID()));
        forEachEnemy(player, target -> removeLife(player, target, target.getMaxHealth() * 0.10F));
        PENETRATION_UNTIL.put(player.getUUID(), player.level().getGameTime() + PENETRATION_DURATION);
    }

    private static void releaseKarmaHunt(Player player, boolean myriadTrigger) {
        float attack = Math.max(1.0F, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        float hitDamage = attack * 2.0F;
        if (myriadTrigger) {
            hitDamage += attack * SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.SETSURA_YAKSHA_MOMENTARY_MYRIAD, 2.0F);
        }
        if (SignetUpgradeData.hasSetsuraCoreSignet(
                player, SignetUpgradeData.Skill.SETSURA_YAKSHA_ALL_FORMS)) {
            int stacks = YAKSHA_STACKS.getOrDefault(player.getUUID(), 0);
            hitDamage *= 1.0F + stacks * SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.SETSURA_YAKSHA_ALL_FORMS, 0.25F);
        }

        final float damagePerHit = hitDamage;
        boolean previous = INTERNAL_DAMAGE.get();
        INTERNAL_DAMAGE.set(true);
        try {
            forEachEnemy(player, target -> {
                DamageSource source = player.damageSources().playerAttack(player);
                for (int i = 0; i < YAKSHA_HITS && target.isAlive(); i++) {
                    target.invulnerableTime = 0;
                    PlayerControlledAllies.hurt(player, target, source, damagePerHit);
                }
            });
        } finally {
            INTERNAL_DAMAGE.set(previous);
        }

        PENETRATION_UNTIL.put(player.getUUID(), player.level().getGameTime() + PENETRATION_DURATION);
        if (SignetUpgradeData.hasSetsuraCoreSignet(
                player, SignetUpgradeData.Skill.SETSURA_YAKSHA_ENDLESS_EVIL)) {
            SignetEffects.reduceSetsunaEvadeCooldown(
                    player,
                    SignetEffects.getScaledSignetValue(
                            player, SignetUpgradeData.Skill.SETSURA_YAKSHA_ENDLESS_EVIL, 0.25F));
        }
        if (SignetUpgradeData.hasSetsuraCoreSignet(
                player, SignetUpgradeData.Skill.SETSURA_YAKSHA_ALL_FORMS)) {
            YAKSHA_STACKS.merge(player.getUUID(), 1, (oldValue, one) -> Math.min(8, oldValue + 1));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        long now = event.getEntity().level().getGameTime();

        TimedPower vulnerability = RED_LEAVES_VULNERABILITY.get(event.getEntity().getUUID());
        if (vulnerability != null && vulnerability.end() >= now) {
            event.setAmount(event.getAmount() * (1.0F + vulnerability.power()));
        }

        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        TimedPower peony = PEONY_BUFF.get(attacker.getUUID());
        if (peony != null && peony.end() >= now) {
            event.setAmount(event.getAmount() * (1.0F + peony.power()));
        }
        if (PENETRATION_UNTIL.getOrDefault(attacker.getUUID(), 0L) >= now) {
            event.setAmount(SignetEffects.applyArmorPenetration(
                    event.getEntity(), event.getSource(), event.getAmount(), 0.20F));
        }
        CurtainState curtain = CURTAINS.get(attacker.getUUID());
        if (!INTERNAL_DAMAGE.get() && curtain != null && curtain.endTick >= now
                && SignetUpgradeData.hasSetsuraCoreSignet(
                attacker, SignetUpgradeData.Skill.SETSURA_CURTAIN_MOON_SAKE)
                && now >= MOON_SAKE_NEXT.getOrDefault(attacker.getUUID(), 0L)) {
            MOON_SAKE_NEXT.put(attacker.getUUID(), now + MOON_SAKE_COOLDOWN);
            event.setAmount(event.getAmount() * (1.0F + SignetEffects.getScaledSignetValue(
                    attacker, SignetUpgradeData.Skill.SETSURA_CURTAIN_MOON_SAKE, 0.20F)));
        }
    }

    @SubscribeEvent
    public static void onFrozenAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && FROZEN_TARGETS.containsKey(attacker.getUUID())) {
            if (attacker.level() instanceof ServerLevel level
                    && PlayerControlledAllies.isSignetTimeStopExempt(attacker, level.players())) {
                FROZEN_TARGETS.remove(attacker.getUUID());
            } else {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onFrozenTick(LivingEvent.LivingTickEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;
        FrozenTarget frozen = FROZEN_TARGETS.get(target.getUUID());
        if (frozen == null) return;
        if (target.level() instanceof ServerLevel level
                && PlayerControlledAllies.isSignetTimeStopExempt(target, level.players())) {
            FROZEN_TARGETS.remove(target.getUUID());
            return;
        }
        CurtainState state = CURTAINS.get(frozen.owner());
        if (state == null || target.level().getGameTime() >= state.endTick) {
            FROZEN_TARGETS.remove(target.getUUID());
            return;
        }
        if (target instanceof Mob mob) mob.getNavigation().stop();
        target.setDeltaMovement(Vec3.ZERO);
        target.fallDistance = 0.0F;
        Vec3 position = frozen.position();
        target.moveTo(position.x, position.y, position.z, target.getYRot(), target.getXRot());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        long now = player.level().getGameTime();
        if (hasSetsuna(player) && SignetUpgradeData.getSetsuraCore(player)
                == SignetUpgradeData.SetsuraCore.NIGHT_YAKSHA) {
            YAKSHA_READY_AT.putIfAbsent(player.getUUID(), now + YAKSHA_READY_INTERVAL);
        } else {
            YAKSHA_READY_AT.remove(player.getUUID());
        }
        CurtainState curtain = CURTAINS.get(player.getUUID());
        if (curtain != null) {
            boolean moving = player.position().distanceToSqr(
                    new Vec3(curtain.lastX, player.getY(), curtain.lastZ)) > 0.0004D;
            if (moving && curtain.pauseRemaining > 0
                    && SignetUpgradeData.hasSetsuraCoreSignet(
                    player, SignetUpgradeData.Skill.SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY)) {
                curtain.endTick++;
                curtain.pauseRemaining--;
            }
            curtain.lastX = player.getX();
            curtain.lastZ = player.getZ();
            if (now >= curtain.endTick) finishCurtain(player);
        }
        cleanup(now);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        EXTRA_EVASION_CHARGES.remove(uuid);
        PEONY_BUFF.remove(uuid);
        PENETRATION_UNTIL.remove(uuid);
        MOON_SAKE_NEXT.remove(uuid);
        CURTAIN_EVASIONS.remove(uuid);
        CURTAINS.remove(uuid);
        FROZEN_TARGETS.entrySet().removeIf(entry -> entry.getValue().owner().equals(uuid));
        YAKSHA_READY_AT.remove(uuid);
        MYRIAD_EVASIONS.remove(uuid);
        YAKSHA_STACKS.remove(uuid);
    }

    private static void removeLife(Player player, LivingEntity target, float amount) {
        if (amount <= 0.0F || !PlayerControlledAllies.isValidEnemy(player, target)) return;
        if (target.getHealth() > amount) {
            target.setHealth(target.getHealth() - amount);
            return;
        }
        target.setHealth(Math.min(1.0F, target.getMaxHealth()));
        target.invulnerableTime = 0;
        PlayerControlledAllies.hurt(player, target,
                player.damageSources().playerAttack(player), Float.MAX_VALUE / 1024.0F);
    }

    private static void forEachEnemy(Player player, java.util.function.Consumer<LivingEntity> action) {
        if (!(player.level() instanceof ServerLevel level)) return;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity living && validTarget(player, living)) {
                action.accept(living);
            }
        }
    }

    private static boolean validTarget(Player player, LivingEntity target) {
        return PlayerControlledAllies.isValidEnemy(player, target);
    }

    private static boolean hasSetsuna(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_SETSURA.get()))
                .isPresent();
    }

    private static void resetRatedPowerCooldowns(Player player) {
        String[] persistedKeys = {
                "AbyssFlowerCooldownZEROTH", "AbyssFlowerCooldownFIRST",
                "AbyssFlowerCooldownSECOND", "AbyssFlowerMistCooldown",
                "FenghuangDownZerothCooldown", "FenghuangDownFirstCooldown",
                "FenghuangDownSecondCooldown", "SkyfireGreatswordZerothCooldown",
                "EdenStarZerothCooldown", "EdenStarFirstCooldown", "EdenStarThirdCooldown"
        };
        CompoundTag root = player.getPersistentData();
        for (String key : persistedKeys) root.remove(key);
        root.remove("FlawlessUltimateCooldownUntil");
        if (root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
            for (String key : persistedKeys) persisted.remove(key);
            persisted.remove("FlawlessUltimateCooldownUntil");
        }
    }

    private static void addHeldEnergy(Player player, int amount) {
        if (amount <= 0) return;
        ItemStack stack = player.getMainHandItem();
        if (!isRatedPowerWeapon(stack)) stack = player.getOffhandItem();
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) {
            FlawlessWeaponEnergy.addEnergy(player, stack, amount);
        } else if (stack.is(ModItems.FENGHUANG_DOWN.get())) {
            FenghuangDownEnergy.addEnergy(player, stack, amount);
        } else if (stack.is(ModItems.ABYSS_FLOWER.get())) {
            AbyssFlowerEnergy.add(player, stack, amount);
        } else if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) {
            SkyfireGreatswordEnergy.add(player, stack, amount);
        } else if (stack.is(ModItems.EDEN_STAR.get())) {
            EdenStarEnergy.add(player, stack, amount);
        }
    }

    private static boolean isRatedPowerWeapon(ItemStack stack) {
        return stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())
                || stack.is(ModItems.FENGHUANG_DOWN.get())
                || stack.is(ModItems.ABYSS_FLOWER.get())
                || stack.is(ModItems.SKYFIRE_GREATSWORD.get())
                || stack.is(ModItems.EDEN_STAR.get());
    }

    private static void cleanup(long now) {
        PEONY_BUFF.entrySet().removeIf(entry -> entry.getValue().end() < now);
        RED_LEAVES_VULNERABILITY.entrySet().removeIf(entry -> entry.getValue().end() < now);
        PENETRATION_UNTIL.entrySet().removeIf(entry -> entry.getValue() < now);
        MOON_SAKE_NEXT.entrySet().removeIf(entry -> entry.getValue() + 200L < now);
    }

    private record TimedPower(long end, float power) {
    }

    private record FrozenTarget(UUID owner, Vec3 position) {
    }

    private static final class CurtainState {
        private long endTick;
        private int pauseRemaining;
        private double lastX;
        private double lastZ;

        private CurtainState(long endTick, int pauseRemaining, double lastX, double lastZ) {
            this.endTick = endTick;
            this.pauseRemaining = pauseRemaining;
            this.lastX = lastX;
            this.lastZ = lastZ;
        }
    }
}
