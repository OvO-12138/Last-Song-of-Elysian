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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DecimationSignetSystem {
    private static final DustParticleOptions BLOOD_DARK = new DustParticleOptions(
            new Vector3f(0.28F, 0.005F, 0.012F), 1.65F);
    private static final DustParticleOptions BLOOD_CRIMSON = new DustParticleOptions(
            new Vector3f(0.92F, 0.015F, 0.025F), 1.35F);
    private static final DustParticleOptions BLOOD_EMBER = new DustParticleOptions(
            new Vector3f(1.0F, 0.16F, 0.015F), 1.05F);
    private static final String NBT_BLOOD_RELEASE_CHARGE = "LSEBloodReleaseCharge";
    private static final String NBT_BLOOD_RELEASE_ENABLED = "LSEBloodReleaseEnabled";
    private static final String NBT_BLOOD_RAIMENT_CHARGE = "LSEBloodRaimentCharge";
    private static final UUID RAIMENT_HEALTH_UUID =
            UUID.fromString("94e48f20-830c-42a1-9d37-e588ed56df40");
    private static final Map<UUID, Float> LAST_HEALTH = new HashMap<>();
    private static final Map<UUID, Float> ENERGY_REMAINDER = new HashMap<>();
    private static final Map<UUID, Long> WUWANG_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> COMBAT_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> NONHUMAN_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Long> RELEASE_DAMAGE_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> BURNING_SOUL_COOLDOWN = new HashMap<>();
    private static final Set<UUID> BURNING_SOUL_ARMED = new HashSet<>();
    private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();

    private DecimationSignetSystem() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        long now = event.getEntity().level().getGameTime();
        if (event.getEntity() instanceof Player defender && hasDecimation(defender)) {
            COMBAT_UNTIL.put(defender.getUUID(), now + 200L);
            if (SignetUpgradeData.getDecimationCore(defender)
                    == SignetUpgradeData.DecimationCore.BLOOD_RAIMENT
                    && getRaimentCharge(defender) > 1500.0F
                    && SignetUpgradeData.hasDecimationCoreSignet(
                    defender, SignetUpgradeData.Skill.DECIMATION_RAIMENT_UNDYING)) {
                float reduction = getRaimentCharge(defender)
                        * SignetEffects.getScaledSignetValue(
                        defender, SignetUpgradeData.Skill.DECIMATION_RAIMENT_UNDYING, 0.001F);
                event.setAmount(event.getAmount() * (1.0F - Mth.clamp(reduction, 0.0F, 0.95F)));
            }
        }

        applyWuwangVulnerability(event, now);
        if (!(event.getSource().getEntity() instanceof Player attacker)
                || !hasDecimation(attacker)) return;
        UUID attackerId = attacker.getUUID();
        COMBAT_UNTIL.put(attackerId, now + 200L);
        if (INTERNAL_DAMAGE.contains(attackerId)) return;
        SignetUpgradeData.synchronizeOwnedLevels(attacker, SignetUpgradeData.Signet.DECIMATION);

        if (SignetUpgradeData.isDecimationSecondarySkillUnlocked(
                attacker, SignetUpgradeData.Skill.DECIMATION_BLADE)) {
            float missing = Math.max(0.0F, attacker.getMaxHealth() - attacker.getHealth());
            float perPoint = SignetEffects.getScaledSignetValue(
                    attacker, SignetUpgradeData.Skill.DECIMATION_BLADE, 0.003F);
            float cap = SignetEffects.getScaledSignetValue(
                    attacker, SignetUpgradeData.Skill.DECIMATION_BLADE, 0.20F);
            event.setAmount(event.getAmount() * (1.0F + Math.min(cap, missing * perPoint)));
        }

        SignetUpgradeData.DecimationCore core = SignetUpgradeData.getDecimationCore(attacker);
        if (core == SignetUpgradeData.DecimationCore.BLOOD_RELEASE
                && isBloodReleaseEnabled(attacker)) {
            if (event.getEntity().isOnFire()) {
                event.setAmount(event.getAmount() * 1.20F);
            }
            if (SignetUpgradeData.hasDecimationCoreSignet(
                    attacker, SignetUpgradeData.Skill.DECIMATION_RELEASE_ONE_PATH)
                    && healthRatio(event.getEntity()) > healthRatio(attacker)) {
                event.setAmount(event.getAmount() * (1.0F + SignetEffects.getScaledSignetValue(
                        attacker, SignetUpgradeData.Skill.DECIMATION_RELEASE_ONE_PATH, 0.10F)));
            }
            if (RELEASE_DAMAGE_UNTIL.getOrDefault(attackerId, 0L) >= now) {
                event.setAmount(event.getAmount() * (1.0F + SignetEffects.getScaledSignetValue(
                        attacker, SignetUpgradeData.Skill.DECIMATION_RELEASE_KALPAS, 0.10F)));
            }
            if (getReleaseCharge(attacker) >= 1000.0F) {
                setReleaseCharge(attacker, 0.0F);
                releaseBlood(attacker, event.getEntity(), now);
            }
        } else if (core == SignetUpgradeData.DecimationCore.BLOOD_RAIMENT) {
            float charge = getRaimentCharge(attacker);
            if (charge > 1500.0F && SignetUpgradeData.hasDecimationCoreSignet(
                    attacker, SignetUpgradeData.Skill.DECIMATION_RAIMENT_HOMELAND)) {
                event.setAmount(SignetEffects.applyArmorPenetration(
                        event.getEntity(), event.getSource(), event.getAmount(),
                        SignetEffects.getScaledSignetValue(
                                attacker, SignetUpgradeData.Skill.DECIMATION_RAIMENT_HOMELAND, 0.10F)));
            }
            if (BURNING_SOUL_ARMED.remove(attackerId)
                    && SignetUpgradeData.hasDecimationCoreSignet(
                    attacker, SignetUpgradeData.Skill.DECIMATION_RAIMENT_BURNING_SOUL)) {
                float damage = baseAttack(attacker) * SignetEffects.getScaledSignetValue(
                        attacker, SignetUpgradeData.Skill.DECIMATION_RAIMENT_BURNING_SOUL, 8.0F);
                guardedDamage(attacker, event.getEntity(), damage);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()
                || event.getAmount() <= 0.0F || !hasDecimation(player)
                || SignetUpgradeData.getDecimationCore(player)
                != SignetUpgradeData.DecimationCore.BLOOD_RAIMENT) return;
        long now = player.level().getGameTime();
        if (COMBAT_UNTIL.getOrDefault(player.getUUID(), 0L) < now) return;
        float heal = event.getAmount();
        float factor = heal > 40.0F ? 1.0F : heal > 20.0F ? 0.80F : 0.10F;
        addRaimentCharge(player, Math.min(900.0F, heal * factor));
        if (getRaimentCharge(player) > 1500.0F
                && SignetUpgradeData.hasDecimationCoreSignet(
                player, SignetUpgradeData.Skill.DECIMATION_RAIMENT_BURNING_SOUL)
                && BURNING_SOUL_COOLDOWN.getOrDefault(player.getUUID(), 0L) <= now) {
            BURNING_SOUL_ARMED.add(player.getUUID());
            BURNING_SOUL_COOLDOWN.put(player.getUUID(), now + 100L);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        UUID uuid = player.getUUID();
        long now = player.level().getGameTime();
        if (!hasDecimation(player)) {
            clearPlayer(player);
            return;
        }
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.DECIMATION);
        float previous = LAST_HEALTH.getOrDefault(uuid, player.getHealth());
        float lost = Math.max(0.0F, previous - player.getHealth());
        if (lost > 0.0F) onHealthLost(player, lost, now);

        SignetUpgradeData.DecimationCore core = SignetUpgradeData.getDecimationCore(player);
        if (core == SignetUpgradeData.DecimationCore.BLOOD_RELEASE
                && isBloodReleaseEnabled(player) && now % 20L == 0L) {
            float missing = Math.max(0.0F, player.getMaxHealth() - player.getHealth());
            addReleaseCharge(player, 10.0F + Mth.floor(missing / 5.0F) * 50.0F);
        }
        if (core == SignetUpgradeData.DecimationCore.BLOOD_RAIMENT) {
            updateRaimentHealth(player);
            float maximumAllowed = player.getMaxHealth() * 0.40F;
            if (player.getHealth() > maximumAllowed) player.setHealth(maximumAllowed);
        } else {
            removeRaimentHealth(player);
        }
        LAST_HEALTH.put(uuid, player.getHealth());
        WUWANG_UNTIL.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    private static void onHealthLost(Player player, float lost, long now) {
        UUID uuid = player.getUUID();
        if (SignetUpgradeData.isDecimationSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.DECIMATION_WUWANG)) {
            WUWANG_UNTIL.put(uuid, now + 160L);
        }
        if (SignetUpgradeData.isDecimationSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.DECIMATION_WEISHEN)) {
            float amount = ENERGY_REMAINDER.getOrDefault(uuid, 0.0F)
                    + lost * SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DECIMATION_WEISHEN, 0.30F);
            int whole = Mth.floor(amount);
            ENERGY_REMAINDER.put(uuid, amount - whole);
            if (whole > 0) addHeldEnergy(player, whole);
        }
        if (SignetUpgradeData.getDecimationCore(player)
                == SignetUpgradeData.DecimationCore.BLOOD_RELEASE
                && isBloodReleaseEnabled(player)
                && SignetUpgradeData.hasDecimationCoreSignet(
                player, SignetUpgradeData.Skill.DECIMATION_RELEASE_NONHUMAN)
                && NONHUMAN_COOLDOWN.getOrDefault(uuid, 0L) <= now) {
            addReleaseCharge(player, SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DECIMATION_RELEASE_NONHUMAN, 100.0F));
            NONHUMAN_COOLDOWN.put(uuid, now + 20L);
        }
    }

    private static void applyWuwangVulnerability(LivingHurtEvent event, long now) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        for (Map.Entry<UUID, Long> entry : WUWANG_UNTIL.entrySet()) {
            if (entry.getValue() < now) continue;
            Player owner = level.getPlayerByUUID(entry.getKey());
            if (owner == null || !hasDecimation(owner)
                    || !PlayerControlledAllies.isValidEnemy(owner, event.getEntity())) continue;
            event.setAmount(event.getAmount() * (1.0F + SignetEffects.getScaledSignetValue(
                    owner, SignetUpgradeData.Skill.DECIMATION_WUWANG, 0.15F)));
        }
    }

    private static void releaseBlood(Player player, LivingEntity center, long now) {
        float extra = 0.0F;
        if (SignetUpgradeData.hasDecimationCoreSignet(
                player, SignetUpgradeData.Skill.DECIMATION_RELEASE_KALPAS)) {
            float cost = player.getHealth() * 0.20F;
            float actual = Math.min(cost, Math.max(0.0F, player.getHealth() - 1.0F));
            player.setHealth(player.getHealth() - actual);
            extra = Mth.floor(actual / 10.0F) * baseAttack(player)
                    * SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DECIMATION_RELEASE_KALPAS, 1.50F);
            RELEASE_DAMAGE_UNTIL.put(player.getUUID(), now + 100L);
        }
        float damage = baseAttack(player) * 15.0F + extra;
        if (SignetUpgradeData.hasDecimationCoreSignet(
                player, SignetUpgradeData.Skill.DECIMATION_RELEASE_ONE_PATH)) {
            damage *= 1.0F + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DECIMATION_RELEASE_ONE_PATH, 0.30F);
        }
        playBloodReleaseEffects(player, center);
        AABB area = center.getBoundingBox().inflate(8.0D);
        for (LivingEntity target : center.level().getEntitiesOfClass(
                LivingEntity.class, area,
                target -> PlayerControlledAllies.isValidEnemy(player, target))) {
            guardedDamage(player, target, damage);
        }
    }

    private static void playBloodReleaseEffects(Player player, LivingEntity center) {
        if (!(center.level() instanceof ServerLevel level)) return;
        double centerX = center.getX();
        double centerY = center.getY() + 0.18D;
        double centerZ = center.getZ();

        level.sendParticles(ParticleTypes.FLASH,
                centerX, centerY + 1.0D, centerZ,
                2, 0.25D, 0.35D, 0.25D, 0.0D);
        level.sendParticles(BLOOD_DARK,
                centerX, centerY + 0.9D, centerZ,
                70, 1.7D, 1.45D, 1.7D, 0.06D);
        level.sendParticles(BLOOD_CRIMSON,
                centerX, centerY + 1.0D, centerZ,
                55, 1.2D, 1.8D, 1.2D, 0.10D);
        level.sendParticles(ParticleTypes.FLAME,
                centerX, centerY + 0.7D, centerZ,
                46, 2.25D, 0.9D, 2.25D, 0.11D);
        level.sendParticles(ParticleTypes.LAVA,
                centerX, centerY + 0.65D, centerZ,
                18, 1.85D, 0.75D, 1.85D, 0.08D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                centerX, centerY + 0.8D, centerZ,
                22, 2.4D, 0.8D, 2.4D, 0.07D);

        for (int ring = 0; ring < 3; ring++) {
            double radius = 2.4D + ring * 2.7D;
            int points = 22 + ring * 8;
            double phase = player.getRandom().nextDouble() * Math.PI * 2.0D;
            for (int point = 0; point < points; point++) {
                double angle = phase + Math.PI * 2.0D * point / points
                        + (player.getRandom().nextDouble() - 0.5D) * 0.16D;
                double irregularRadius = radius
                        * (0.84D + player.getRandom().nextDouble() * 0.32D);
                double x = centerX + Math.cos(angle) * irregularRadius;
                double z = centerZ + Math.sin(angle) * irregularRadius;
                double y = centerY + player.getRandom().nextDouble() * 0.28D;
                DustParticleOptions color = (point + ring) % 3 == 0
                        ? BLOOD_EMBER
                        : (point & 1) == 0 ? BLOOD_CRIMSON : BLOOD_DARK;
                level.sendParticles(color, x, y, z,
                        1, 0.045D, 0.035D, 0.045D, 0.012D);
            }
        }

        for (int ray = 0; ray < 34; ray++) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = 0.45D + player.getRandom().nextDouble() * 3.2D;
            double directionX = Math.cos(angle);
            double directionZ = Math.sin(angle);
            level.sendParticles(
                    (ray & 1) == 0 ? BLOOD_CRIMSON : BLOOD_EMBER,
                    centerX + directionX * distance,
                    centerY + 0.3D + player.getRandom().nextDouble() * 1.8D,
                    centerZ + directionZ * distance,
                    0,
                    directionX * (0.14D + player.getRandom().nextDouble() * 0.18D),
                    0.08D + player.getRandom().nextDouble() * 0.16D,
                    directionZ * (0.14D + player.getRandom().nextDouble() * 0.18D),
                    1.0D);
        }

        BlockPos soundPosition = center.blockPosition();
        level.playSound(null, soundPosition, SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 2.8F, 0.68F);
        level.playSound(null, soundPosition, SoundEvents.WITHER_SHOOT,
                SoundSource.PLAYERS, 1.6F, 0.82F);
    }

    private static void guardedDamage(Player player, LivingEntity target, float damage) {
        UUID uuid = player.getUUID();
        boolean added = INTERNAL_DAMAGE.add(uuid);
        try {
            DamageSource source = player.damageSources().playerAttack(player);
            PlayerControlledAllies.hurt(player, target, source, Math.max(0.0F, damage));
        } finally {
            if (added) INTERNAL_DAMAGE.remove(uuid);
        }
    }

    private static float healthRatio(LivingEntity entity) {
        return entity.getMaxHealth() <= 0.0F ? 0.0F : entity.getHealth() / entity.getMaxHealth();
    }

    private static float baseAttack(Player player) {
        return Math.max(1.0F, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    public static boolean hasBloodRelease(Player player) {
        return player != null && hasDecimation(player)
                && SignetUpgradeData.getDecimationCore(player)
                == SignetUpgradeData.DecimationCore.BLOOD_RELEASE;
    }

    public static boolean isBloodReleaseEnabled(Player player) {
        if (player == null) return false;
        CompoundTag data = persisted(player);
        return !data.contains(NBT_BLOOD_RELEASE_ENABLED)
                || data.getBoolean(NBT_BLOOD_RELEASE_ENABLED);
    }

    public static boolean isBloodReleaseActive(Player player) {
        return hasBloodRelease(player) && isBloodReleaseEnabled(player);
    }

    public static Boolean toggleBloodRelease(Player player) {
        if (!hasBloodRelease(player)) return null;
        boolean enabled = !isBloodReleaseEnabled(player);
        persisted(player).putBoolean(NBT_BLOOD_RELEASE_ENABLED, enabled);
        return enabled;
    }

    public static int getBloodReleaseCharge(Player player) {
        return Mth.floor(getReleaseCharge(player));
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        UUID id = player.getUUID();
        CompoundTag data = persisted(player);
        data.remove(NBT_BLOOD_RELEASE_CHARGE);
        data.remove(NBT_BLOOD_RELEASE_ENABLED);
        data.remove(NBT_BLOOD_RAIMENT_CHARGE);
        NONHUMAN_COOLDOWN.remove(id);
        RELEASE_DAMAGE_UNTIL.remove(id);
        BURNING_SOUL_COOLDOWN.remove(id);
        BURNING_SOUL_ARMED.remove(id);
        removeRaimentHealth(player);
    }

    private static float getReleaseCharge(Player player) {
        return persisted(player).getFloat(NBT_BLOOD_RELEASE_CHARGE);
    }

    private static void setReleaseCharge(Player player, float amount) {
        persisted(player).putFloat(NBT_BLOOD_RELEASE_CHARGE, Mth.clamp(amount, 0.0F, 1000.0F));
    }

    private static void addReleaseCharge(Player player, float amount) {
        setReleaseCharge(player, getReleaseCharge(player) + amount);
    }

    private static float getRaimentCharge(Player player) {
        return persisted(player).getFloat(NBT_BLOOD_RAIMENT_CHARGE);
    }

    private static void addRaimentCharge(Player player, float amount) {
        int cap = 1800;
        if (SignetUpgradeData.hasDecimationCoreSignet(
                player, SignetUpgradeData.Skill.DECIMATION_RAIMENT_UNDYING)) {
            cap += Math.round(SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DECIMATION_RAIMENT_UNDYING, 500.0F));
        }
        if (SignetUpgradeData.hasDecimationCoreSignet(
                player, SignetUpgradeData.Skill.DECIMATION_RAIMENT_BURNING_SOUL)) {
            cap += Math.round(SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DECIMATION_RAIMENT_BURNING_SOUL, 500.0F));
        }
        if (SignetUpgradeData.hasDecimationCoreSignet(
                player, SignetUpgradeData.Skill.DECIMATION_RAIMENT_HOMELAND)) {
            cap += Math.round(SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.DECIMATION_RAIMENT_HOMELAND, 500.0F));
        }
        persisted(player).putFloat(NBT_BLOOD_RAIMENT_CHARGE,
                Mth.clamp(getRaimentCharge(player) + amount, 0.0F, cap));
    }

    private static void updateRaimentHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        attribute.removeModifier(RAIMENT_HEALTH_UUID);
        double bonus = getRaimentCharge(player) * 0.1D;
        if (bonus > 0.0D) {
            attribute.addTransientModifier(new AttributeModifier(
                    RAIMENT_HEALTH_UUID, "blood_raiment_health", bonus,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeRaimentHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) attribute.removeModifier(RAIMENT_HEALTH_UUID);
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static boolean hasDecimation(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DECIMATION.get()))
                .isPresent();
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

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayer(event.getEntity());
    }

    private static void clearPlayer(Player player) {
        UUID uuid = player.getUUID();
        LAST_HEALTH.remove(uuid);
        ENERGY_REMAINDER.remove(uuid);
        WUWANG_UNTIL.remove(uuid);
        COMBAT_UNTIL.remove(uuid);
        NONHUMAN_COOLDOWN.remove(uuid);
        RELEASE_DAMAGE_UNTIL.remove(uuid);
        BURNING_SOUL_COOLDOWN.remove(uuid);
        BURNING_SOUL_ARMED.remove(uuid);
        INTERNAL_DAMAGE.remove(uuid);
        removeRaimentHealth(player);
    }
}
