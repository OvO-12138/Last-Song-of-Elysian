package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEffects;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.AbyssFlowerEnergy;
import com.ovo.lastsongofelysian.util.EdenStarEnergy;
import com.ovo.lastsongofelysian.util.FenghuangDownEnergy;
import com.ovo.lastsongofelysian.util.FlawlessWeaponEnergy;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.SkyfireGreatswordEnergy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GoldSignetSystem {
    private static final int BASE_MAX_ENERGY = 300;
    private static final Map<UUID, Float> RECOVERED_ENERGY = new HashMap<>();
    private static final Map<UUID, Long> COMBAT_UNTIL = new HashMap<>();
    private static final Map<UUID, Float> WINE_REMAINDER = new HashMap<>();
    private static final Map<UUID, Float> WITHERED_REMAINDER = new HashMap<>();
    private static final Map<UUID, Long> ELEMENTAL_PENETRATION_UNTIL = new HashMap<>();
    private static final Map<UUID, EchoState> ECHO_STATES = new HashMap<>();
    private static final Map<UUID, SilentDebuff> SILENT_DEBUFFS = new HashMap<>();
    private static final Map<UUID, UUID> INTOXICATED_OWNERS = new HashMap<>();
    private static final Set<UUID> DAMAGE_GUARD = new HashSet<>();
    private static final Set<UUID> INTERNAL_ENERGY_CHANGE = new HashSet<>();

    private GoldSignetSystem() {
    }

    public static boolean hasGold(Player player) {
        return player != null && CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_GOLD.get()))
                .isPresent();
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        UUID id = player.getUUID();
        RECOVERED_ENERGY.remove(id);
        WITHERED_REMAINDER.remove(id);
        ELEMENTAL_PENETRATION_UNTIL.remove(id);
        ECHO_STATES.remove(id);
        player.removeEffect(ModEffects.GOLD_FINAL_ECHO.get());
        SILENT_DEBUFFS.entrySet().removeIf(entry -> id.equals(entry.getValue().owner()));
        INTOXICATED_OWNERS.entrySet().removeIf(entry -> {
            if (!id.equals(entry.getValue())) return false;
            if (player.getServer() != null) {
                for (ServerLevel level : player.getServer().getAllLevels()) {
                    Entity target = level.getEntity(entry.getKey());
                    if (target instanceof LivingEntity living) {
                        living.removeEffect(ModEffects.GOLD_INTOXICATED.get());
                    }
                }
            }
            return true;
        });
        clampHeldEnergy(player);
    }

    public static int getMaximumEnergy(Player player) {
        int maximum = BASE_MAX_ENERGY + ReverieSignetSystem.energyCapacityBonus(player);
        if (!hasGold(player)) return Mth.clamp(maximum, BASE_MAX_ENERGY, 4000);
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.GOLD);
        if (SignetUpgradeData.isGoldSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.GOLD_JADE)) {
            maximum += Math.round(SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.GOLD_JADE, 50.0F));
        }
        MobEffectInstance echo = player.getEffect(ModEffects.GOLD_FINAL_ECHO.get());
        if (echo != null && echo.getAmplifier() > 0
                && SignetUpgradeData.hasGoldCoreSignet(
                player, SignetUpgradeData.Skill.GOLD_ECHO_WITHERED)) {
            maximum = Math.round(maximum * (1.0F + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.GOLD_ECHO_WITHERED, 0.50F)));
        }
        return Mth.clamp(maximum, BASE_MAX_ENERGY, 4000);
    }

    public static float attackRecoveryMultiplier(Player player) {
        if (!hasGold(player) || !SignetUpgradeData.isGoldSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.GOLD_ORCHARD)) return 1.0F;
        int maximum = getMaximumEnergy(player);
        if (maximum <= 0) return 1.0F;
        float missingRatio = 1.0F - Mth.clamp(getHeldEnergy(player) / (float) maximum, 0.0F, 1.0F);
        float maximumBonus = SignetEffects.getScaledSignetValue(
                player, SignetUpgradeData.Skill.GOLD_ORCHARD, 0.50F);
        return 1.0F + missingRatio * maximumBonus;
    }

    public static void onEnergyChanged(Player player, int previous, int current) {
        if (player == null || player.level().isClientSide() || previous == current
                || !hasGold(player) || INTERNAL_ENERGY_CHANGE.contains(player.getUUID())) return;
        UUID uuid = player.getUUID();
        int change = current - previous;
        if (change > 0) {
            RECOVERED_ENERGY.merge(uuid, (float) change, Float::sum);
        } else if (SignetUpgradeData.getGoldCore(player) == SignetUpgradeData.GoldCore.ECHO) {
            EchoState state = ECHO_STATES.get(uuid);
            long now = player.level().getGameTime();
            if (state == null) {
                state = new EchoState(now, 0.0F, -1L);
                ECHO_STATES.put(uuid, state);
            }
            state.charge += -change;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        long now = event.getEntity().level().getGameTime();
        if (event.getEntity() instanceof Player defender) {
            COMBAT_UNTIL.put(defender.getUUID(), now + 200L);
            if (hasGold(defender) && SignetUpgradeData.isGoldSecondarySkillUnlocked(
                    defender, SignetUpgradeData.Skill.GOLD_BIRD)) {
                int energy = getHeldEnergy(defender);
                float perTen = SignetEffects.getScaledSignetValue(
                        defender, SignetUpgradeData.Skill.GOLD_BIRD, 0.05F);
                float cap = SignetEffects.getScaledSignetValue(
                        defender, SignetUpgradeData.Skill.GOLD_BIRD, 0.65F);
                event.setAmount(event.getAmount()
                        * (1.0F - Math.min(0.95F, Math.min(cap, energy / 10 * perTen))));
            }
        }

        LivingEntity sourceLiving = event.getSource().getEntity() instanceof LivingEntity living
                ? living : null;
        if (sourceLiving != null) {
            SilentDebuff sourceDebuff = SILENT_DEBUFFS.get(sourceLiving.getUUID());
            if (sourceDebuff != null && sourceDebuff.expires >= now) {
                event.setAmount(event.getAmount() * (1.0F - sourceDebuff.amount));
            }
        }
        SilentDebuff targetDebuff = SILENT_DEBUFFS.get(event.getEntity().getUUID());
        if (targetDebuff != null && targetDebuff.expires >= now) {
            event.setAmount(event.getAmount() * (1.0F + targetDebuff.amount));
        }

        if (!(event.getSource().getEntity() instanceof Player attacker) || !hasGold(attacker)) return;
        COMBAT_UNTIL.put(attacker.getUUID(), now + 200L);
        UUID attackerId = attacker.getUUID();
        boolean internal = DAMAGE_GUARD.contains(attackerId);

        MobEffectInstance intoxicated = event.getEntity().getEffect(ModEffects.GOLD_INTOXICATED.get());
        if (intoxicated != null) {
            float vulnerability = 0.15F;
            if (SignetUpgradeData.hasGoldCoreSignet(
                    attacker, SignetUpgradeData.Skill.GOLD_ARIA_FERTILE)) {
                vulnerability += (intoxicated.getAmplifier() + 1)
                        * SignetEffects.getScaledSignetValue(
                        attacker, SignetUpgradeData.Skill.GOLD_ARIA_FERTILE, 0.15F);
            }
            event.setAmount(event.getAmount() * (1.0F + vulnerability));
            if (!internal && SignetUpgradeData.hasGoldCoreSignet(
                    attacker, SignetUpgradeData.Skill.GOLD_ARIA_LIFE)) {
                addHeldEnergy(attacker, Math.max(1, Math.round(SignetEffects.getScaledSignetValue(
                        attacker, SignetUpgradeData.Skill.GOLD_ARIA_LIFE, 1.0F))));
            }
        }

        EchoState echo = ECHO_STATES.get(attackerId);
        if (echo != null && SignetUpgradeData.getGoldCore(attacker) == SignetUpgradeData.GoldCore.ECHO) {
            int elapsedSeconds = (int) Math.max(0L, (now - echo.startTick) / 20L);
            float bonus = Math.min(0.80F,
                    getMaximumEnergy(attacker) / 10 * 0.04F * elapsedSeconds);
            event.setAmount(event.getAmount() * (1.0F + bonus));
        }

        if (ELEMENTAL_PENETRATION_UNTIL.getOrDefault(attackerId, 0L) >= now
                && (event.getSource().is(DamageTypes.MAGIC)
                || event.getSource().is(DamageTypes.ON_FIRE)
                || event.getSource().is(DamageTypes.IN_FIRE)
                || event.getSource().is(DamageTypes.EXPLOSION)
                || event.getSource().is(DamageTypes.PLAYER_EXPLOSION))) {
            event.setAmount(SignetEffects.applyArmorPenetration(
                    event.getEntity(), event.getSource(), event.getAmount(), 0.20F));
        }

        if (!internal && SignetUpgradeData.getGoldCore(attacker) == SignetUpgradeData.GoldCore.ARIA
                && RECOVERED_ENERGY.getOrDefault(attackerId, 0.0F) >= 100.0F) {
            RECOVERED_ENERGY.put(attackerId,
                    RECOVERED_ENERGY.getOrDefault(attackerId, 0.0F) - 100.0F);
            ELEMENTAL_PENETRATION_UNTIL.put(attackerId, now + 200L);
            releaseAriaThunder(attacker, event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        UUID uuid = player.getUUID();
        long now = player.level().getGameTime();
        if (!hasGold(player)) {
            clearPlayerState(player);
            return;
        }
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.GOLD);

        if (now % 20L == 0L && COMBAT_UNTIL.getOrDefault(uuid, 0L) >= now
                && SignetUpgradeData.isGoldSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.GOLD_WINE)) {
            float amount = WINE_REMAINDER.getOrDefault(uuid, 0.0F)
                    + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.GOLD_WINE, 1.0F);
            int whole = Mth.floor(amount);
            WINE_REMAINDER.put(uuid, amount - whole);
            if (whole > 0) addHeldEnergy(player, whole);
        }

        EchoState echo = ECHO_STATES.get(uuid);
        if (echo != null) {
            if (SignetUpgradeData.getGoldCore(player) != SignetUpgradeData.GoldCore.ECHO) {
                endEcho(player);
            } else {
                int elapsed = (int) Math.max(0L, (now - echo.startTick) / 20L);
                boolean empowered = elapsed > 3;
                player.addEffect(new MobEffectInstance(
                        ModEffects.GOLD_FINAL_ECHO.get(), 40, empowered ? 1 : 0,
                        false, false, false));
                if (elapsed > 0 && echo.lastPaidTick != elapsed) {
                    echo.lastPaidTick = elapsed;
                    if (echo.charge < elapsed) {
                        endEcho(player);
                    } else {
                        echo.charge -= elapsed;
                        if (empowered) applyEchoAugments(player, now);
                    }
                }
            }
        }
        SILENT_DEBUFFS.entrySet().removeIf(entry -> entry.getValue().expires < now);
    }

    private static void applyEchoAugments(Player player, long now) {
        if (SignetUpgradeData.hasGoldCoreSignet(
                player, SignetUpgradeData.Skill.GOLD_ECHO_WITHERED)) {
            UUID uuid = player.getUUID();
            float amount = WITHERED_REMAINDER.getOrDefault(uuid, 0.0F)
                    + getMaximumEnergy(player) * SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.GOLD_ECHO_WITHERED, 0.01F);
            int whole = Mth.floor(amount);
            WITHERED_REMAINDER.put(uuid, amount - whole);
            if (whole > 0) addHeldEnergy(player, whole);
        }
        List<LivingEntity> nearby = player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(16.0D),
                target -> PlayerControlledAllies.isValidEnemy(player, target));
        if (SignetUpgradeData.hasGoldCoreSignet(
                player, SignetUpgradeData.Skill.GOLD_ECHO_COLD_WIND)) {
            float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                    * SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.GOLD_ECHO_COLD_WIND, 30.0F);
            guardedDamage(player, nearby, damage);
        }
        if (SignetUpgradeData.hasGoldCoreSignet(
                player, SignetUpgradeData.Skill.GOLD_ECHO_SILENT_NIGHT)) {
            float step = SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.GOLD_ECHO_SILENT_NIGHT, 0.02F);
            float cap = SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.GOLD_ECHO_SILENT_NIGHT, 0.40F);
            for (LivingEntity target : nearby) {
                SilentDebuff current = SILENT_DEBUFFS.get(target.getUUID());
                float next = Math.min(cap, (current == null ? 0.0F : current.amount) + step);
                SILENT_DEBUFFS.put(target.getUUID(),
                        new SilentDebuff(player.getUUID(), next, now + 30L));
            }
        }
    }

    private static void releaseAriaThunder(Player player, LivingEntity originalTarget) {
        List<LivingEntity> candidates = new ArrayList<>(player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(20.0D),
                target -> PlayerControlledAllies.isValidEnemy(player, target)
                        && target instanceof Enemy));
        if (candidates.isEmpty() && PlayerControlledAllies.isValidEnemy(player, originalTarget)) {
            candidates.add(originalTarget);
        }
        if (candidates.isEmpty()) return;
        Map<UUID, Integer> hits = new HashMap<>();
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 4.50F;
        DAMAGE_GUARD.add(player.getUUID());
        try {
            for (int i = 0; i < 3; i++) {
                LivingEntity target = candidates.get(player.getRandom().nextInt(candidates.size()));
                PlayerControlledAllies.hurt(player, target,
                        player.damageSources().playerAttack(player), damage);
                applyIntoxicated(player, target);
                hits.merge(target.getUUID(), 1, Integer::sum);
                if (player.level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                            28, 0.35D, 0.6D, 0.35D, 0.08D);
                }
            }
        } finally {
            DAMAGE_GUARD.remove(player.getUUID());
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS, 0.8F, 1.25F);
        if (SignetUpgradeData.hasGoldCoreSignet(
                player, SignetUpgradeData.Skill.GOLD_ARIA_DAWN)) {
            for (LivingEntity target : candidates) {
                if (hits.getOrDefault(target.getUUID(), 0) == 3) {
                    triggerThunderSymphony(player, target);
                    break;
                }
            }
        }
    }

    private static void applyIntoxicated(Player player, LivingEntity target) {
        if (!PlayerControlledAllies.isValidEnemy(player, target)) return;
        int amplifier = 0;
        MobEffectInstance current = target.getEffect(ModEffects.GOLD_INTOXICATED.get());
        if (current != null && SignetUpgradeData.hasGoldCoreSignet(
                player, SignetUpgradeData.Skill.GOLD_ARIA_FERTILE)) {
            amplifier = Math.min(2, current.getAmplifier() + 1);
        }
        target.addEffect(new MobEffectInstance(
                ModEffects.GOLD_INTOXICATED.get(), 120, amplifier, false, false, false));
        INTOXICATED_OWNERS.put(target.getUUID(), player.getUUID());
    }

    private static void triggerThunderSymphony(Player player, LivingEntity center) {
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                * SignetEffects.getScaledSignetValue(
                player, SignetUpgradeData.Skill.GOLD_ARIA_DAWN, 12.0F);
        List<LivingEntity> targets = center.level().getEntitiesOfClass(
                LivingEntity.class, center.getBoundingBox().inflate(6.0D),
                target -> PlayerControlledAllies.isValidEnemy(player, target));
        guardedDamage(player, targets, damage);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    60, 255, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,
                    60, 255, false, false, false));
        }
    }

    private static void guardedDamage(Player player, List<LivingEntity> targets, float damage) {
        if (!DAMAGE_GUARD.add(player.getUUID())) return;
        try {
            for (LivingEntity target : targets) {
                PlayerControlledAllies.hurt(player, target,
                        player.damageSources().playerAttack(player), damage);
            }
        } finally {
            DAMAGE_GUARD.remove(player.getUUID());
        }
    }

    private static int getHeldEnergy(Player player) {
        ItemStack stack = player.getMainHandItem();
        int energy = energyOf(stack);
        return energy >= 0 ? energy : Math.max(0, energyOf(player.getOffhandItem()));
    }

    private static int energyOf(ItemStack stack) {
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) return FlawlessWeaponEnergy.getEnergy(stack);
        if (stack.is(ModItems.ABYSS_FLOWER.get())) return AbyssFlowerEnergy.get(stack);
        if (stack.is(ModItems.EDEN_STAR.get())) return EdenStarEnergy.get(stack);
        if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) return SkyfireGreatswordEnergy.get(stack);
        if (stack.is(ModItems.FENGHUANG_DOWN.get())) return FenghuangDownEnergy.getEnergy(stack);
        return -1;
    }

    private static void addHeldEnergy(Player player, int amount) {
        ItemStack stack = player.getMainHandItem();
        if (energyOf(stack) < 0) stack = player.getOffhandItem();
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) FlawlessWeaponEnergy.addEnergy(player, stack, amount);
        else if (stack.is(ModItems.ABYSS_FLOWER.get())) AbyssFlowerEnergy.add(player, stack, amount);
        else if (stack.is(ModItems.EDEN_STAR.get())) EdenStarEnergy.add(player, stack, amount);
        else if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) SkyfireGreatswordEnergy.add(player, stack, amount);
        else if (stack.is(ModItems.FENGHUANG_DOWN.get())) FenghuangDownEnergy.addEnergy(player, stack, amount);
    }

    private static void endEcho(Player player) {
        ECHO_STATES.remove(player.getUUID());
        WITHERED_REMAINDER.remove(player.getUUID());
        player.removeEffect(ModEffects.GOLD_FINAL_ECHO.get());
        clampHeldEnergy(player);
    }

    private static void clampHeldEnergy(Player player) {
        int maximum = getMaximumEnergy(player);
        ItemStack stack = player.getMainHandItem();
        if (energyOf(stack) < 0) stack = player.getOffhandItem();
        int current = energyOf(stack);
        if (current <= maximum) return;
        INTERNAL_ENERGY_CHANGE.add(player.getUUID());
        try {
            if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) FlawlessWeaponEnergy.setEnergy(player, stack, maximum);
            else if (stack.is(ModItems.ABYSS_FLOWER.get())) AbyssFlowerEnergy.set(player, stack, maximum);
            else if (stack.is(ModItems.EDEN_STAR.get())) EdenStarEnergy.set(player, stack, maximum);
            else if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) SkyfireGreatswordEnergy.set(player, stack, maximum);
            else if (stack.is(ModItems.FENGHUANG_DOWN.get())) FenghuangDownEnergy.setEnergy(player, stack, maximum);
        } finally {
            INTERNAL_ENERGY_CHANGE.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayerState(event.getEntity());
    }

    private static void clearPlayerState(Player player) {
        UUID uuid = player.getUUID();
        resetCoreRuntime(player);
        COMBAT_UNTIL.remove(uuid);
        WINE_REMAINDER.remove(uuid);
    }

    private static final class EchoState {
        private final long startTick;
        private float charge;
        private long lastPaidTick;

        private EchoState(long startTick, float charge, long lastPaidTick) {
            this.startTick = startTick;
            this.charge = charge;
            this.lastPaidTick = lastPaidTick;
        }
    }

    private record SilentDebuff(UUID owner, float amount, long expires) {
    }
}
