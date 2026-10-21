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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BodhiSignetSystem {
    private static final String NBT_PASSIVE_DAMAGE_ENABLED = "LSEBodhiPassiveDamageEnabled";
    private static final Map<UUID, Long> DIVINE_FOOT_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Long> EXHAUSTION_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Float> ENERGY_REMAINDER = new HashMap<>();
    private static final Map<UUID, TimedPower> HEAVENLY_EYE_VULNERABILITY = new HashMap<>();
    private static final Map<UUID, TimedPower> CAUSAL_VULNERABILITY = new HashMap<>();
    private static final Map<UUID, UUID> CAUSAL_OWNER = new HashMap<>();
    private static final Map<UUID, TimedPower> TRUE_SELFLESS_VULNERABILITY = new HashMap<>();
    private static final Map<UUID, UUID> TRUE_SELFLESS_VULNERABILITY_OWNER = new HashMap<>();
    private static final Map<UUID, TimedPower> TRUE_SELFLESS_ATTACK_REDUCTION = new HashMap<>();
    private static final Map<UUID, UUID> TRUE_SELFLESS_ATTACK_OWNER = new HashMap<>();
    private static final Map<UUID, Long> TRUE_PENETRATION_END = new HashMap<>();
    private static final Map<UUID, Float> TRUE_PENETRATION_POWER = new HashMap<>();
    private static final Map<UUID, Long> DAZE_END = new HashMap<>();
    private static final Map<UUID, UUID> DAZE_OWNER = new HashMap<>();
    private static final Map<UUID, SecretVulnerability> SECRET_VULNERABILITY = new HashMap<>();
    private static final Map<UUID, UUID> SECRET_VULNERABILITY_OWNER = new HashMap<>();
    private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();
    private static final List<MobEffect> NEGATIVE_EFFECTS = List.of(
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.WEAKNESS,
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.BLINDNESS,
            MobEffects.DARKNESS,
            MobEffects.CONFUSION,
            MobEffects.HUNGER
    );

    private BodhiSignetSystem() {
    }

    public static boolean isInternalDamage(Player player) {
        return player != null && INTERNAL_DAMAGE.contains(player.getUUID());
    }

    public static boolean isPassiveDamageEnabled(Player player) {
        if (player == null) return false;
        CompoundTag data = persisted(player);
        return !data.contains(NBT_PASSIVE_DAMAGE_ENABLED)
                || data.getBoolean(NBT_PASSIVE_DAMAGE_ENABLED);
    }

    public static Boolean togglePassiveDamage(Player player) {
        if (player == null || !hasBodhi(player)
                || SignetUpgradeData.getBodhiCore(player)
                != SignetUpgradeData.BodhiCore.SECRET_WORD) return null;
        boolean enabled = !isPassiveDamageEnabled(player);
        persisted(player).putBoolean(NBT_PASSIVE_DAMAGE_ENABLED, enabled);
        if (!enabled) {
            UUID id = player.getUUID();
            clearOwnedCoreState(DAZE_END, DAZE_OWNER, id);
            clearOwnedCoreState(SECRET_VULNERABILITY, SECRET_VULNERABILITY_OWNER, id);
        }
        return enabled;
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        UUID id = player.getUUID();
        TRUE_PENETRATION_END.remove(id);
        TRUE_PENETRATION_POWER.remove(id);
        clearOwnedCoreState(CAUSAL_VULNERABILITY, CAUSAL_OWNER, id);
        clearOwnedCoreState(TRUE_SELFLESS_VULNERABILITY,
                TRUE_SELFLESS_VULNERABILITY_OWNER, id);
        clearOwnedCoreState(TRUE_SELFLESS_ATTACK_REDUCTION,
                TRUE_SELFLESS_ATTACK_OWNER, id);
        clearOwnedCoreState(DAZE_END, DAZE_OWNER, id);
        clearOwnedCoreState(SECRET_VULNERABILITY, SECRET_VULNERABILITY_OWNER, id);
    }

    public static void onComboAdvanced(Player player, int previous, int current) {
        if (player == null || current <= previous || !hasBodhi(player)) return;
        SignetUpgradeData.BodhiCore core = SignetUpgradeData.getBodhiCore(player);
        if (core == SignetUpgradeData.BodhiCore.TRUE_WORD) {
            if (crossed(previous, current, 5)) releaseCausalWheel(player, 0.50F);
            if (crossed(previous, current, 10)) releaseCausalWheel(player, 1.00F);
            if (crossed(previous, current, 15)) releaseCausalWheel(player, 1.50F);
        } else if (core == SignetUpgradeData.BodhiCore.SECRET_WORD) {
            if (crossed(previous, current, 6)) openHeavenlyEye(player, 8.00F);
            if (crossed(previous, current, 10)) openHeavenlyEye(player, 9.00F);
            if (crossed(previous, current, 15)) openHeavenlyEye(player, 10.00F);
        }
    }

    public static void onComboFifteen(Player player) {
        if (player == null || !hasBodhi(player)) return;
        long now = player.level().getGameTime();
        if (SignetUpgradeData.isBodhiSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.BODHI_HEAVENLY_EYE)
                && player.level() instanceof ServerLevel level) {
            float power = SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.BODHI_HEAVENLY_EYE, 0.15F);
            long end = now + 100L;
            for (var entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity living)
                        || !PlayerControlledAllies.isValidEnemy(player, living)) continue;
                putTimedPower(HEAVENLY_EYE_VULNERABILITY, living.getUUID(), end, power);
            }
        }
        if (SignetUpgradeData.isBodhiSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.BODHI_MIND_READING)) {
            float amount = ENERGY_REMAINDER.getOrDefault(player.getUUID(), 0.0F)
                    + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.BODHI_MIND_READING, 10.0F);
            int whole = Mth.floor(amount);
            ENERGY_REMAINDER.put(player.getUUID(), amount - whole);
            addHeldEnergy(player, whole);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackAddsCombo(LivingHurtEvent event) {
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F
                || !(event.getSource().getEntity() instanceof Player player)
                || !hasBodhi(player) || isInternalDamage(player)) return;
        long now = player.level().getGameTime();
        UUID playerId = player.getUUID();
        if (SignetUpgradeData.isBodhiSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.BODHI_EXHAUSTION)
                && EXHAUSTION_COOLDOWN.getOrDefault(playerId, 0L) <= now) {
            SignetEffects.addCombo(player, 3);
            float factor = SignetUpgradeData.factorForLevel(
                    SignetUpgradeData.getEquippedLevel(
                            player, SignetUpgradeData.Skill.BODHI_EXHAUSTION));
            EXHAUSTION_COOLDOWN.put(playerId, now + Math.max(1L, Math.round(100.0F / factor)));
        }
        if (DAZE_END.getOrDefault(event.getEntity().getUUID(), 0L) >= now) {
            SignetEffects.addCombo(player, 1);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamageModifiers(LivingHurtEvent event) {
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        long now = event.getEntity().level().getGameTime();
        LivingEntity target = event.getEntity();
        float multiplier = 1.0F;
        multiplier *= activeMultiplier(HEAVENLY_EYE_VULNERABILITY.get(target.getUUID()), now);
        multiplier *= activeMultiplier(CAUSAL_VULNERABILITY.get(target.getUUID()), now);
        multiplier *= activeMultiplier(TRUE_SELFLESS_VULNERABILITY.get(target.getUUID()), now);
        SecretVulnerability secret = SECRET_VULNERABILITY.get(target.getUUID());
        if (secret != null && secret.end() >= now) {
            int negatives = Math.min(5, countNegativeEffects(target));
            multiplier *= 1.0F + secret.base() + secret.perEffect() * negatives;
        }
        event.setAmount(event.getAmount() * multiplier);

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            TimedPower reduction = TRUE_SELFLESS_ATTACK_REDUCTION.remove(attacker.getUUID());
            TRUE_SELFLESS_ATTACK_OWNER.remove(attacker.getUUID());
            if (reduction != null && reduction.end() >= now) {
                event.setAmount(event.getAmount()
                        * (1.0F - Mth.clamp(reduction.power(), 0.0F, 1.0F)));
            }
        }

        if (event.getSource().getEntity() instanceof Player player && hasBodhi(player)
                && TRUE_PENETRATION_END.getOrDefault(player.getUUID(), 0L) >= now) {
            event.setAmount(SignetEffects.applyArmorPenetration(
                    target, event.getSource(), event.getAmount(),
                    TRUE_PENETRATION_POWER.getOrDefault(player.getUUID(), 0.0F)));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        long now = player.level().getGameTime();
        if (hasBodhi(player)) {
            SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.BODHI);
            if (SignetUpgradeData.isBodhiSecondarySkillUnlocked(
                    player, SignetUpgradeData.Skill.BODHI_DIVINE_FOOT)
                    && SignetEffects.getComboCount(player) < 8
                    && DIVINE_FOOT_COOLDOWN.getOrDefault(player.getUUID(), 0L) <= now) {
                int current = SignetEffects.getComboCount(player);
                SignetEffects.addCombo(player, 12 - current);
                float factor = SignetUpgradeData.factorForLevel(
                        SignetUpgradeData.getEquippedLevel(
                                player, SignetUpgradeData.Skill.BODHI_DIVINE_FOOT));
                DIVINE_FOOT_COOLDOWN.put(
                        player.getUUID(), now + Math.max(1L, Math.round(200.0F / factor)));
            }
        }
        if (now % 20L == 0L) cleanup(now);
    }

    private static void releaseCausalWheel(Player player, float attackScale) {
        long now = player.level().getGameTime();
        float damage = baseAttack(player) * attackScale;
        AABB area = player.getBoundingBox().inflate(8.0D);
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, area,
                target -> PlayerControlledAllies.isValidEnemy(player, target))) {
            double dx = target.getX() - player.getX();
            double dz = target.getZ() - player.getZ();
            double length = Math.max(0.25D, Math.sqrt(dx * dx + dz * dz));
            target.push(dx / length * 1.2D, 0.35D, dz / length * 1.2D);
            guardedDamage(player, target, damage);
            putTimedPower(CAUSAL_VULNERABILITY, target.getUUID(), now + 100L, 0.30F);
            CAUSAL_OWNER.put(target.getUUID(), player.getUUID());

            if (SignetUpgradeData.hasBodhiCoreSignet(
                    player, SignetUpgradeData.Skill.BODHI_TRUE_SELFLESS)) {
                float reduction = SignetEffects.getScaledSignetValue(
                        player, SignetUpgradeData.Skill.BODHI_TRUE_SELFLESS, 0.50F);
                float vulnerability = SignetEffects.getScaledSignetValue(
                        player, SignetUpgradeData.Skill.BODHI_TRUE_SELFLESS, 0.10F);
                putTimedPower(TRUE_SELFLESS_ATTACK_REDUCTION,
                        target.getUUID(), now + 160L, reduction);
                TRUE_SELFLESS_ATTACK_OWNER.put(target.getUUID(), player.getUUID());
                putTimedPower(TRUE_SELFLESS_VULNERABILITY,
                        target.getUUID(), now + 160L, vulnerability);
                TRUE_SELFLESS_VULNERABILITY_OWNER.put(target.getUUID(), player.getUUID());
            }
            if (target.getArmorValue() <= 0) {
                if (SignetUpgradeData.hasBodhiCoreSignet(
                        player, SignetUpgradeData.Skill.BODHI_TRUE_IMPERMANENCE)) {
                    TRUE_PENETRATION_END.put(player.getUUID(), now + 200L);
                    TRUE_PENETRATION_POWER.put(player.getUUID(),
                            SignetEffects.getScaledSignetValue(
                                    player, SignetUpgradeData.Skill.BODHI_TRUE_IMPERMANENCE, 0.15F));
                }
                if (SignetUpgradeData.hasBodhiCoreSignet(
                        player, SignetUpgradeData.Skill.BODHI_TRUE_SILENCE)) {
                    guardedDamage(player, target, baseAttack(player)
                            * SignetEffects.getScaledSignetValue(
                            player, SignetUpgradeData.Skill.BODHI_TRUE_SILENCE, 5.0F));
                }
            } else if (SignetUpgradeData.hasBodhiCoreSignet(
                    player, SignetUpgradeData.Skill.BODHI_TRUE_SILENCE)) {
                damageArmor(target, damage, SignetEffects.getScaledSignetValue(
                        player, SignetUpgradeData.Skill.BODHI_TRUE_SILENCE, 0.20F));
            }
        }
    }

    private static void openHeavenlyEye(Player player, float attackScale) {
        if (!isPassiveDamageEnabled(player)) return;
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(24.0D),
                target -> PlayerControlledAllies.isValidEnemy(player, target));
        if (targets.isEmpty()) return;
        LivingEntity target = targets.get(player.getRandom().nextInt(targets.size()));
        int negativeDuration = 60;
        if (SignetUpgradeData.hasBodhiCoreSignet(
                player, SignetUpgradeData.Skill.BODHI_SECRET_SELFLESS)) {
            negativeDuration += 60;
        }
        if (SignetUpgradeData.hasBodhiCoreSignet(
                player, SignetUpgradeData.Skill.BODHI_SECRET_SILENCE)) {
            applyRandomNegativeEffects(player, target);
        }
        int negatives = countNegativeEffects(target);
        guardedDamage(player, target, baseAttack(player) * attackScale);
        if (SignetUpgradeData.hasBodhiCoreSignet(
                player, SignetUpgradeData.Skill.BODHI_SECRET_IMPERMANENCE)) {
            float extra = baseAttack(player) * SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.BODHI_SECRET_IMPERMANENCE, 1.50F);
            for (int i = 0; i < negatives; i++) guardedDamage(player, target, extra);
        }
        long now = player.level().getGameTime();
        DAZE_END.put(target.getUUID(), now + negativeDuration);
        DAZE_OWNER.put(target.getUUID(), player.getUUID());
        if (SignetUpgradeData.hasBodhiCoreSignet(
                player, SignetUpgradeData.Skill.BODHI_SECRET_SELFLESS)) {
            SECRET_VULNERABILITY.put(target.getUUID(), new SecretVulnerability(
                    now + negativeDuration,
                    SignetEffects.getScaledSignetValue(
                            player, SignetUpgradeData.Skill.BODHI_SECRET_SELFLESS, 0.10F),
                    SignetEffects.getScaledSignetValue(
                            player, SignetUpgradeData.Skill.BODHI_SECRET_SELFLESS, 0.025F)));
            SECRET_VULNERABILITY_OWNER.put(target.getUUID(), player.getUUID());
        }
    }

    private static void applyRandomNegativeEffects(Player player, LivingEntity target) {
        List<MobEffect> effects = new ArrayList<>(NEGATIVE_EFFECTS);
        Collections.shuffle(effects);
        int duration = Math.max(1, Math.round(60.0F * SignetUpgradeData.factorForLevel(
                SignetUpgradeData.getEquippedLevel(
                        player, SignetUpgradeData.Skill.BODHI_SECRET_SILENCE))));
        if (SignetUpgradeData.hasBodhiCoreSignet(
                player, SignetUpgradeData.Skill.BODHI_SECRET_SELFLESS)) {
            duration += 60;
        }
        for (int i = 0; i < 3; i++) {
            target.addEffect(new MobEffectInstance(effects.get(i), duration, 0));
        }
    }

    private static int countNegativeEffects(LivingEntity target) {
        int count = 0;
        for (MobEffectInstance effect : target.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) count++;
        }
        return count;
    }

    private static void damageArmor(LivingEntity target, float damage, float extraMultiplier) {
        int durability = Math.max(1, Mth.ceil(damage * Math.max(0.0F, extraMultiplier)));
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = target.getItemBySlot(slot);
            if (!armor.isEmpty() && armor.isDamageableItem()) {
                armor.hurtAndBreak(durability, target, entity -> entity.broadcastBreakEvent(slot));
            }
        }
    }

    private static void guardedDamage(Player player, LivingEntity target, float damage) {
        UUID id = player.getUUID();
        boolean added = INTERNAL_DAMAGE.add(id);
        try {
            PlayerControlledAllies.hurt(player, target,
                    player.damageSources().playerAttack(player), Math.max(0.0F, damage));
        } finally {
            if (added) INTERNAL_DAMAGE.remove(id);
        }
    }

    private static void putTimedPower(Map<UUID, TimedPower> map, UUID id, long end, float power) {
        TimedPower current = map.get(id);
        if (current == null || current.end() < end || current.power() < power) {
            map.put(id, new TimedPower(
                    Math.max(end, current == null ? 0L : current.end()),
                    Math.max(power, current == null ? 0.0F : current.power())));
        }
    }

    private static float activeMultiplier(TimedPower power, long now) {
        return power != null && power.end() >= now ? 1.0F + power.power() : 1.0F;
    }

    private static boolean crossed(int previous, int current, int threshold) {
        return previous < threshold && current >= threshold;
    }

    private static float baseAttack(Player player) {
        return Math.max(1.0F, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    private static boolean hasBodhi(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_BODHI.get()))
                .isPresent();
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static ItemStack heldWeapon(Player player) {
        ItemStack main = player.getMainHandItem();
        if (isRatedPowerWeapon(main)) return main;
        ItemStack off = player.getOffhandItem();
        return isRatedPowerWeapon(off) ? off : ItemStack.EMPTY;
    }

    private static boolean isRatedPowerWeapon(ItemStack stack) {
        return stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())
                || stack.is(ModItems.ABYSS_FLOWER.get())
                || stack.is(ModItems.EDEN_STAR.get())
                || stack.is(ModItems.SKYFIRE_GREATSWORD.get())
                || stack.is(ModItems.FENGHUANG_DOWN.get());
    }

    private static void addHeldEnergy(Player player, int amount) {
        ItemStack stack = heldWeapon(player);
        if (stack.isEmpty() || amount <= 0) return;
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) {
            FlawlessWeaponEnergy.addEnergy(player, stack, amount);
        } else if (stack.is(ModItems.ABYSS_FLOWER.get())) {
            AbyssFlowerEnergy.add(player, stack, amount);
        } else if (stack.is(ModItems.EDEN_STAR.get())) {
            EdenStarEnergy.add(player, stack, amount);
        } else if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) {
            SkyfireGreatswordEnergy.add(player, stack, amount);
        } else if (stack.is(ModItems.FENGHUANG_DOWN.get())) {
            FenghuangDownEnergy.addEnergy(player, stack, amount);
        }
    }

    private static void cleanup(long now) {
        HEAVENLY_EYE_VULNERABILITY.entrySet().removeIf(entry -> entry.getValue().end() < now);
        CAUSAL_VULNERABILITY.entrySet().removeIf(entry -> entry.getValue().end() < now);
        TRUE_SELFLESS_VULNERABILITY.entrySet().removeIf(entry -> entry.getValue().end() < now);
        TRUE_SELFLESS_ATTACK_REDUCTION.entrySet().removeIf(entry -> entry.getValue().end() < now);
        TRUE_PENETRATION_END.entrySet().removeIf(entry -> entry.getValue() < now);
        DAZE_END.entrySet().removeIf(entry -> entry.getValue() < now);
        SECRET_VULNERABILITY.entrySet().removeIf(entry -> entry.getValue().end() < now);
        CAUSAL_OWNER.keySet().removeIf(id -> !CAUSAL_VULNERABILITY.containsKey(id));
        TRUE_SELFLESS_VULNERABILITY_OWNER.keySet().removeIf(
                id -> !TRUE_SELFLESS_VULNERABILITY.containsKey(id));
        TRUE_SELFLESS_ATTACK_OWNER.keySet().removeIf(
                id -> !TRUE_SELFLESS_ATTACK_REDUCTION.containsKey(id));
        DAZE_OWNER.keySet().removeIf(id -> !DAZE_END.containsKey(id));
        SECRET_VULNERABILITY_OWNER.keySet().removeIf(
                id -> !SECRET_VULNERABILITY.containsKey(id));
    }

    private static <T> void clearOwnedCoreState(Map<UUID, T> values,
                                                 Map<UUID, UUID> owners,
                                                 UUID playerId) {
        owners.entrySet().removeIf(entry -> {
            if (!playerId.equals(entry.getValue())) return false;
            values.remove(entry.getKey());
            return true;
        });
    }

    private record TimedPower(long end, float power) {
    }

    private record SecretVulnerability(long end, float base, float perEffect) {
    }
}
