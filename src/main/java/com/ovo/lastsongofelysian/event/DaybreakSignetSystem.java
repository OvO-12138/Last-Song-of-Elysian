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
import com.ovo.lastsongofelysian.util.WeaponEnergyGain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DaybreakSignetSystem {
    private static final int BASE_DURATION = 100;
    private static final int BASE_MAX_STACKS = 10;
    private static final int EGO_EXTRA_STACKS = 18;
    private static final long ENERGY_COOLDOWN = 100L;
    private static final long CORE_ATTACK_COOLDOWN = 100L;
    private static final long ASHES_COOLDOWN = 200L;
    private static final UUID MAX_HEALTH_UUID = UUID.fromString("351087f5-b6f1-43c9-8c31-d363f65aeb27");
    private static final String LACERATION_POWER = "LSELacerationPower";
    private static final String SELF_LACERATION = "LSESelfLaceration";

    private static final Map<UUID, Long> ENERGY_NEXT = new HashMap<>();
    private static final Map<UUID, Long> CORE_ATTACK_NEXT = new HashMap<>();
    private static final Map<UUID, Long> SELF_STACK_NEXT = new HashMap<>();
    private static final Map<UUID, Long> ASHES_NEXT = new HashMap<>();
    private static final Map<UUID, Long> GOOD_EVIL_UNTIL = new HashMap<>();
    private static final Map<UUID, Float> LAST_HEALTH = new HashMap<>();
    private static final Map<UUID, Integer> HEAL_STACKS = new HashMap<>();
    private static final Map<UUID, Integer> LOSS_STACKS = new HashMap<>();
    private static final ThreadLocal<Boolean> INTERNAL_DAMAGE = ThreadLocal.withInitial(() -> false);

    private DaybreakSignetSystem() {
    }

    public static boolean isInternalDamage() {
        return INTERNAL_DAMAGE.get();
    }

    public static int getLacerationStacks(LivingEntity entity) {
        MobEffectInstance effect = entity.getEffect(ModEffects.LACERATION.get());
        return effect == null ? 0 : effect.getAmplifier() + 1;
    }

    public static void addLaceration(Player player, LivingEntity target, int stacks) {
        addLaceration(player, target, stacks, 0);
    }

    public static void addLaceration(Player player, LivingEntity target,
                                     int stacks, int minimumCap) {
        if (player == null || target == null || stacks <= 0 || target.level().isClientSide()) return;
        if (target != player && PlayerControlledAllies.isProtectedEffectTarget(player, target)) return;
        int before = getLacerationStacks(target);
        int max = BASE_MAX_STACKS + (hasEgo(player) ? EGO_EXTRA_STACKS : 0);
        max = Math.max(max, Math.max(before, minimumCap));
        int after = Math.min(max, before + stacks);
        int duration = BASE_DURATION;
        if (SignetUpgradeData.isDaybreakSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.DAYBREAK_HEART)) {
            duration += Math.round(SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DAYBREAK_HEART, 5.0F) * 20.0F);
        }
        target.getPersistentData().putFloat(
                LACERATION_POWER,
                target == player ? 1.0F : SignetEffects.getScaledSignetValue(
                        player, SignetUpgradeData.Skill.DAYBREAK_CLAW, 0.5F)
        );
        if (target == player) target.getPersistentData().putBoolean(SELF_LACERATION, true);
        else target.getPersistentData().remove(SELF_LACERATION);
        MobEffectInstance refreshed = new MobEffectInstance(
                ModEffects.LACERATION.get(), duration, after - 1, false, true);
        if (before > 0) target.removeEffect(ModEffects.LACERATION.get());
        target.addEffect(refreshed);

        if (before > 0 && SignetUpgradeData.isDaybreakSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.DAYBREAK_BREATH)) {
            long now = player.level().getGameTime();
            if (now >= ENERGY_NEXT.getOrDefault(player.getUUID(), 0L)) {
                ENERGY_NEXT.put(player.getUUID(), now + ENERGY_COOLDOWN);
                int amount = Math.max(1, Math.round(SignetEffects.getScaledSignetValue(
                        player, SignetUpgradeData.Skill.DAYBREAK_BREATH, 5.0F)));
                addHeldEnergy(player, WeaponEnergyGain.apply(player, amount));
            }
        }
    }

    public static void onSuccessfulEvasion(Player player) {
        if (!hasDaybreak(player) || !SignetUpgradeData.isDaybreakSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.DAYBREAK_WING)) return;
        int stacks = Math.max(1, Math.round(SignetEffects.getScaledSignetValue(
                player, SignetUpgradeData.Skill.DAYBREAK_WING, 5.0F)));
        forEachEnemy(player, target -> addLaceration(player, target, stacks));
    }

    public static void onWeaponUltimateActivated(Player player) {
        if (!hasDaybreak(player) || !SignetUpgradeData.isDaybreakSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.DAYBREAK_HORN)) return;
        int stacks = Math.max(1, Math.round(SignetEffects.getScaledSignetValue(
                player, SignetUpgradeData.Skill.DAYBREAK_HORN, 15.0F)));
        forEachEnemy(player, target -> addLaceration(player, target, stacks, stacks));
    }

    public static void onWeaponSkillActivated(Player player) {
        if (!hasDaybreak(player)
                || SignetUpgradeData.getDaybreakCore(player)
                != SignetUpgradeData.DaybreakCore.LONELY_NIGHT
                || !SignetUpgradeData.hasDaybreakCoreSignet(
                player, SignetUpgradeData.Skill.DAYBREAK_NIGHT_CHOICE)) return;
        long now = player.level().getGameTime();
        if (now < ASHES_NEXT.getOrDefault(player.getUUID(), 0L)) return;
        ASHES_NEXT.put(player.getUUID(), now + ASHES_COOLDOWN);
        float attack = Math.max(1.0F, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        float damage = attack * SignetEffects.getScaledSignetValue(
                player, SignetUpgradeData.Skill.DAYBREAK_NIGHT_CHOICE, 3.0F);
        withInternalDamage(() -> forEachEnemy(player, target -> {
            if (target.distanceToSqr(player) <= 64.0D) {
                PlayerControlledAllies.hurt(
                        player, target, player.damageSources().playerAttack(player), damage);
            }
        }));
        int selfStacks = getLacerationStacks(player);
        if (selfStacks > 0) player.heal(selfStacks);
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        UUID id = player.getUUID();
        CORE_ATTACK_NEXT.remove(id);
        SELF_STACK_NEXT.remove(id);
        ASHES_NEXT.remove(id);
        GOOD_EVIL_UNTIL.remove(id);
        HEAL_STACKS.remove(id);
        LOSS_STACKS.remove(id);
        if (player.getPersistentData().getBoolean(SELF_LACERATION)) {
            player.removeEffect(ModEffects.LACERATION.get());
            player.getPersistentData().remove(SELF_LACERATION);
            player.getPersistentData().remove(LACERATION_POWER);
        }
        updateMaxHealth(player, 0);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (event.getEntity() instanceof Player victim && hasDaybreak(victim)
                && SignetUpgradeData.getDaybreakCore(victim)
                == SignetUpgradeData.DaybreakCore.LONELY_NIGHT
                && SignetUpgradeData.hasDaybreakCoreSignet(
                victim, SignetUpgradeData.Skill.DAYBREAK_NIGHT_DAWN)
                && getLacerationStacks(victim) > 0) {
            float reduction = Math.min(
                    SignetEffects.getScaledSignetValue(
                            victim, SignetUpgradeData.Skill.DAYBREAK_NIGHT_DAWN, 0.30F),
                    HEAL_STACKS.getOrDefault(victim.getUUID(), 0)
                            * SignetEffects.getScaledSignetValue(
                            victim, SignetUpgradeData.Skill.DAYBREAK_NIGHT_DAWN, 0.01F));
            event.setAmount(event.getAmount() * (1.0F - Mth.clamp(reduction, 0.0F, 0.95F)));
        }

        if (!(event.getSource().getEntity() instanceof Player player)
                || !hasDaybreak(player) || INTERNAL_DAMAGE.get()) return;
        LivingEntity target = event.getEntity();
        if (PlayerControlledAllies.isProtectedAlly(player, target)) {
            event.setAmount(0.0F);
            return;
        }
        if (PlayerControlledAllies.isElysianHero(target)) return;
        int targetStacks = getLacerationStacks(target);
        float amount = event.getAmount();
        if (targetStacks > 0 && SignetUpgradeData.isDaybreakSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.DAYBREAK_HORN)) {
            amount *= 1.0F + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DAYBREAK_HORN, 0.20F);
        }

        int fieldStacks = totalFieldStacks(player);
        if (SignetUpgradeData.getDaybreakCore(player)
                == SignetUpgradeData.DaybreakCore.UNFADING_LIGHT) {
            if (SignetUpgradeData.hasDaybreakCoreSignet(
                    player, SignetUpgradeData.Skill.DAYBREAK_LIGHT_HERO)) {
                float penetration = Math.min(
                        SignetEffects.getScaledSignetValue(
                                player, SignetUpgradeData.Skill.DAYBREAK_LIGHT_HERO, 0.28F),
                        fieldStacks * SignetEffects.getScaledSignetValue(
                                player, SignetUpgradeData.Skill.DAYBREAK_LIGHT_HERO, 0.01F));
                amount = SignetEffects.applyArmorPenetration(
                        target, event.getSource(), amount, penetration);
            }
            if (SignetUpgradeData.hasDaybreakCoreSignet(
                    player, SignetUpgradeData.Skill.DAYBREAK_LIGHT_VOW)) {
                float bonus = Math.min(
                        SignetEffects.getScaledSignetValue(
                                player, SignetUpgradeData.Skill.DAYBREAK_LIGHT_VOW, 0.40F),
                        fieldStacks * SignetEffects.getScaledSignetValue(
                                player, SignetUpgradeData.Skill.DAYBREAK_LIGHT_VOW, 0.016F));
                amount *= 1.0F + bonus;
            }
            if (targetStacks >= 10) triggerCoreAttack(player, target);
        } else if (SignetUpgradeData.getDaybreakCore(player)
                == SignetUpgradeData.DaybreakCore.LONELY_NIGHT) {
            int selfStacks = getLacerationStacks(player);
            amount *= 1.0F + selfStacks * 0.01F;
            if (SignetUpgradeData.hasDaybreakCoreSignet(
                    player, SignetUpgradeData.Skill.DAYBREAK_NIGHT_INTENT)) {
                amount *= 1.0F + selfStacks * SignetEffects.getScaledSignetValue(
                        player, SignetUpgradeData.Skill.DAYBREAK_NIGHT_INTENT, 0.015F);
            }
            if (SignetUpgradeData.hasDaybreakCoreSignet(
                    player, SignetUpgradeData.Skill.DAYBREAK_NIGHT_DAWN)) {
                float penetration = Math.min(
                        SignetEffects.getScaledSignetValue(
                                player, SignetUpgradeData.Skill.DAYBREAK_NIGHT_DAWN, 0.30F),
                        LOSS_STACKS.getOrDefault(player.getUUID(), 0)
                                * SignetEffects.getScaledSignetValue(
                                player, SignetUpgradeData.Skill.DAYBREAK_NIGHT_DAWN, 0.04F));
                amount = SignetEffects.applyArmorPenetration(
                        target, event.getSource(), amount, penetration);
            }
            long now = player.level().getGameTime();
            if (now >= SELF_STACK_NEXT.getOrDefault(player.getUUID(), 0L)) {
                SELF_STACK_NEXT.put(player.getUUID(), now + 20L);
                addLaceration(player, player, 1);
            }
        }
        if (GOOD_EVIL_UNTIL.getOrDefault(player.getUUID(), 0L)
                >= player.level().getGameTime()) {
            amount *= 1.0F + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DAYBREAK_LIGHT_GOOD_EVIL, 0.10F);
        }
        event.setAmount(amount);
    }

    private static void triggerCoreAttack(Player player, LivingEntity target) {
        long now = player.level().getGameTime();
        if (now < CORE_ATTACK_NEXT.getOrDefault(player.getUUID(), 0L)) return;
        CORE_ATTACK_NEXT.put(player.getUUID(), now + CORE_ATTACK_COOLDOWN);
        float attack = Math.max(1.0F, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        float damage = attack * 0.15F;
        boolean armored = target.getArmorValue() > 0;
        if (armored) damage += attack * 0.50F;
        DamageSource source = player.damageSources().playerAttack(player);
        damage = SignetEffects.applyArmorPenetration(target, source, damage, 0.05F);
        final float dealt = damage;
        withInternalDamage(() -> PlayerControlledAllies.hurt(player, target, source, dealt));

        if (!armored && SignetUpgradeData.hasDaybreakCoreSignet(
                player, SignetUpgradeData.Skill.DAYBREAK_LIGHT_GOOD_EVIL)) {
            addHeldEnergy(player, WeaponEnergyGain.apply(player, Math.max(1, Math.round(
                    SignetEffects.getScaledSignetValue(
                            player, SignetUpgradeData.Skill.DAYBREAK_LIGHT_GOOD_EVIL, 5.0F)))));
            GOOD_EVIL_UNTIL.put(player.getUUID(), now + 200L);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        UUID id = player.getUUID();
        boolean lonelyNight = hasDaybreak(player)
                && SignetUpgradeData.getDaybreakCore(player)
                == SignetUpgradeData.DaybreakCore.LONELY_NIGHT;
        if (!lonelyNight && player.getPersistentData().getBoolean(SELF_LACERATION)) {
            player.removeEffect(ModEffects.LACERATION.get());
        }
        if (!player.hasEffect(ModEffects.LACERATION.get())) {
            player.getPersistentData().remove(SELF_LACERATION);
            player.getPersistentData().remove(LACERATION_POWER);
        }
        int stacks = lonelyNight ? getLacerationStacks(player) : 0;
        updateMaxHealth(player, stacks);
        float current = player.getHealth();
        Float previous = LAST_HEALTH.put(id, current);
        if (!lonelyNight || stacks <= 0) {
            HEAL_STACKS.remove(id);
            LOSS_STACKS.remove(id);
        } else if (previous != null) {
            if (current > previous + 0.001F) HEAL_STACKS.merge(id, 1, Integer::sum);
            else if (current < previous - 0.001F) LOSS_STACKS.merge(id, 1, Integer::sum);
        }
    }

    public static boolean applySelfLacerationTick(LivingEntity entity, float damage) {
        if (!(entity instanceof Player player) || !hasDaybreak(player)
                || SignetUpgradeData.getDaybreakCore(player)
                != SignetUpgradeData.DaybreakCore.LONELY_NIGHT) return false;
        player.setHealth(Math.max(1.0F, player.getHealth() - Math.max(0.0F, damage)));
        return true;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        ENERGY_NEXT.remove(id);
        CORE_ATTACK_NEXT.remove(id);
        SELF_STACK_NEXT.remove(id);
        ASHES_NEXT.remove(id);
        GOOD_EVIL_UNTIL.remove(id);
        LAST_HEALTH.remove(id);
        HEAL_STACKS.remove(id);
        LOSS_STACKS.remove(id);
        updateMaxHealth(event.getEntity(), 0);
    }

    private static void updateMaxHealth(Player player, int stacks) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        attribute.removeModifier(MAX_HEALTH_UUID);
        if (stacks > 0) {
            attribute.addTransientModifier(new AttributeModifier(
                    MAX_HEALTH_UUID, "daybreak_lonely_night_health",
                    stacks * 0.05D, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static int totalFieldStacks(Player player) {
        if (!(player.level() instanceof ServerLevel level)) return 0;
        int total = 0;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity living && living.isAlive()) {
                total += getLacerationStacks(living);
            }
        }
        return total;
    }

    private static void forEachEnemy(Player player, java.util.function.Consumer<LivingEntity> action) {
        if (!(player.level() instanceof ServerLevel level)) return;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity living
                    && PlayerControlledAllies.isValidEnemy(player, living)) action.accept(living);
        }
    }

    private static boolean hasDaybreak(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DAYBREAK.get()))
                .isPresent();
    }

    private static boolean hasEgo(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_EGO.get()))
                .isPresent();
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

    private static void withInternalDamage(Runnable action) {
        boolean previous = INTERNAL_DAMAGE.get();
        INTERNAL_DAMAGE.set(true);
        try {
            action.run();
        } finally {
            INTERNAL_DAMAGE.set(previous);
        }
    }
}
