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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HelixSignetSystem {
    private static final Map<UUID, Long> SPOON_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> COBWEB_UNTIL = new HashMap<>();
    private static final Map<UUID, EnergyRestore> ENERGY_RESTORES = new HashMap<>();
    private static final Map<UUID, Long> MAGIC_COOLDOWN_UNTIL = new HashMap<>();
    private static final Map<UUID, Float> MAGIC_DAMAGE_BONUS = new HashMap<>();
    private static final Map<UUID, Long> MAGIC_PENETRATION_UNTIL = new HashMap<>();
    private static final Map<UUID, Vulnerability> MAGIC_VULNERABILITY = new HashMap<>();
    private static final Map<UUID, TypeFourField> TYPE_FOUR_FIELDS = new HashMap<>();
    private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();

    private HelixSignetSystem() {
    }

    public static void onWeaponSkillActivated(Player player) {
        if (player == null || player.level().isClientSide() || !hasHelix(player)) return;
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.HELIX);
        long now = player.level().getGameTime();
        UUID uuid = player.getUUID();

        if (SignetUpgradeData.isHelixSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.HELIX_SPOON)) {
            SPOON_UNTIL.put(uuid, now + 90L);
        }
        if (SignetUpgradeData.isHelixSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.HELIX_COBWEB)) {
            COBWEB_UNTIL.put(uuid, now + 90L);
        }
        if (SignetUpgradeData.isHelixSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.HELIX_SNEER)) {
            int total = Math.round(SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.HELIX_SNEER, 12.0F));
            ENERGY_RESTORES.put(uuid, new EnergyRestore(now, now + 100L, total, 0));
            reduceWeaponSkillCooldowns(player, SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.HELIX_SNEER, 0.15F));
        }

        SignetUpgradeData.HelixCore core = SignetUpgradeData.getHelixCore(player);
        if (core == SignetUpgradeData.HelixCore.REVERSED_SPIRAL
                && MAGIC_COOLDOWN_UNTIL.getOrDefault(uuid, 0L) <= now) {
            MAGIC_COOLDOWN_UNTIL.put(uuid, now + 100L);
            triggerSurpriseMagic(player, now);
        } else if (core == SignetUpgradeData.HelixCore.CONCLUSION_SPIRAL) {
            placeTypeFour(player, now);
        }
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        UUID id = player.getUUID();
        MAGIC_COOLDOWN_UNTIL.remove(id);
        MAGIC_DAMAGE_BONUS.remove(id);
        MAGIC_PENETRATION_UNTIL.remove(id);
        TYPE_FOUR_FIELDS.remove(id);
        MAGIC_VULNERABILITY.entrySet().removeIf(
                entry -> id.equals(entry.getValue().owner()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        long now = event.getEntity().level().getGameTime();
        Vulnerability vulnerability = MAGIC_VULNERABILITY.get(event.getEntity().getUUID());
        if (vulnerability != null) {
            if (vulnerability.until >= now) {
                event.setAmount(event.getAmount() * (1.0F + vulnerability.amount));
            } else {
                MAGIC_VULNERABILITY.remove(event.getEntity().getUUID());
            }
        }
        applyTypeFourVulnerability(event, now);
        if (!(event.getSource().getEntity() instanceof Player player) || !hasHelix(player)) return;
        UUID uuid = player.getUUID();

        if (COBWEB_UNTIL.getOrDefault(uuid, 0L) >= now
                && SignetUpgradeData.isHelixSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.HELIX_COBWEB)) {
            event.setAmount(event.getAmount() * (1.0F + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.HELIX_COBWEB, 0.10F)));
        }
        if (SPOON_UNTIL.getOrDefault(uuid, 0L) >= now
                && event.getSource().getDirectEntity() instanceof AbstractArrow
                && SignetUpgradeData.isHelixSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.HELIX_SPOON)) {
            event.setAmount(event.getAmount() * (1.0F + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.HELIX_SPOON, 0.20F)));
        }
        if (SignetUpgradeData.hasHelixCoreSignet(
                player, SignetUpgradeData.Skill.HELIX_REVERSED_DOOR)) {
            event.setAmount(event.getAmount() * (1.0F + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.HELIX_REVERSED_DOOR, 0.30F)));
        }
        if (MAGIC_PENETRATION_UNTIL.getOrDefault(uuid, 0L) >= now
                && SignetUpgradeData.hasHelixCoreSignet(
                player, SignetUpgradeData.Skill.HELIX_REVERSED_HALL)) {
            event.setAmount(SignetEffects.applyArmorPenetration(
                    event.getEntity(), event.getSource(), event.getAmount(),
                    SignetEffects.getScaledSignetValue(
                            player, SignetUpgradeData.Skill.HELIX_REVERSED_HALL, 0.10F)));
        }

        TypeFourField field = TYPE_FOUR_FIELDS.get(uuid);
        if (field != null && field.fractureEnd >= now
                && SignetUpgradeData.hasHelixCoreSignet(
                player, SignetUpgradeData.Skill.HELIX_CONCLUSION_PRISON)) {
            event.setAmount(SignetEffects.applyArmorPenetration(
                    event.getEntity(), event.getSource(), event.getAmount(),
                    SignetEffects.getScaledSignetValue(
                            player, SignetUpgradeData.Skill.HELIX_CONCLUSION_PRISON, 0.10F)));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        UUID uuid = player.getUUID();
        long now = player.level().getGameTime();
        if (!hasHelix(player)) {
            clearPlayer(uuid);
            return;
        }
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.HELIX);
        tickEnergyRestore(player, now);
        tickTypeFour(player, now);
        MAGIC_VULNERABILITY.entrySet().removeIf(entry -> entry.getValue().until < now);
    }

    private static void triggerSurpriseMagic(Player player, long now) {
        Vec3 look = player.getLookAngle().normalize();
        AABB area = player.getBoundingBox().inflate(12.0D);
        float attack = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float slashMultiplier = 2.0F;
        if (SignetUpgradeData.hasHelixCoreSignet(
                player, SignetUpgradeData.Skill.HELIX_REVERSED_HALL)) {
            consumeHeldEnergy(player, 5);
            slashMultiplier *= 1.0F + SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.HELIX_REVERSED_HALL, 0.35F);
            MAGIC_PENETRATION_UNTIL.put(player.getUUID(), now + 120L);
        }
        float cumulative = MAGIC_DAMAGE_BONUS.getOrDefault(player.getUUID(), 0.0F);
        if (SignetUpgradeData.hasHelixCoreSignet(
                player, SignetUpgradeData.Skill.HELIX_REVERSED_TRUTH)) {
            float step = SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.HELIX_REVERSED_TRUTH, 0.06F);
            float cap = SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.HELIX_REVERSED_TRUTH, 0.30F);
            cumulative = Math.min(cap, cumulative + step);
            MAGIC_DAMAGE_BONUS.put(player.getUUID(), cumulative);
        }
        float damage = attack * slashMultiplier * (1.0F + cumulative);
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, area, target -> validTarget(player, target))) {
            Vec3 direction = target.position().subtract(player.position()).normalize();
            if (look.dot(direction) < 0.25D) continue;
            guardedDamage(player, target, damage, 3);
            MAGIC_VULNERABILITY.put(target.getUUID(),
                    new Vulnerability(player.getUUID(), 0.25F, now + 160L));
            if (SignetUpgradeData.hasHelixCoreSignet(
                    player, SignetUpgradeData.Skill.HELIX_REVERSED_DOOR)) {
                float bladeDamage = attack * SignetEffects.getScaledSignetValue(
                        player, SignetUpgradeData.Skill.HELIX_REVERSED_DOOR, 0.20F);
                guardedDamage(player, target, bladeDamage, 2);
            }
            if (SignetUpgradeData.hasHelixCoreSignet(
                    player, SignetUpgradeData.Skill.HELIX_REVERSED_TRUTH)) {
                damageArmor(target, damage, SignetEffects.getScaledSignetValue(
                        player, SignetUpgradeData.Skill.HELIX_REVERSED_TRUTH, 1.0F));
            }
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                        8, 0.5D, 0.6D, 0.5D, 0.0D);
            }
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.2F, 0.75F);
    }

    private static void placeTypeFour(Player player, long now) {
        if (!(player.level() instanceof ServerLevel level)) return;
        Vec3 look = player.getLookAngle();
        Vec3 target = player.position().add(look.x * 7.0D, 0.0D, look.z * 7.0D);
        int x = Mth.floor(target.x);
        int z = Mth.floor(target.z);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        Vec3 center = new Vec3(x + 0.5D, y + 0.15D, z + 0.5D);
        TYPE_FOUR_FIELDS.put(player.getUUID(), new TypeFourField(
                level.dimension(), center, now + 20L));
    }

    private static void tickTypeFour(Player player, long now) {
        TypeFourField field = TYPE_FOUR_FIELDS.get(player.getUUID());
        if (field == null) return;
        if (!(player.level() instanceof ServerLevel level)
                || !level.dimension().equals(field.dimension)) {
            TYPE_FOUR_FIELDS.remove(player.getUUID());
            return;
        }
        if (!field.exploded) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    field.center.x, field.center.y + 0.5D, field.center.z,
                    5, 0.35D, 0.5D, 0.35D, 0.02D);
            if (now >= field.explodeTick) explodeTypeFour(player, field, now);
            return;
        }
        if (now > field.fieldEnd) {
            TYPE_FOUR_FIELDS.remove(player.getUUID());
            return;
        }
        if (now <= field.fractureEnd) freezeLoadedEntities(player, field);
        double radius = SignetUpgradeData.hasHelixCoreSignet(
                player, SignetUpgradeData.Skill.HELIX_CONCLUSION_POINTER) ? 12.0D : 8.0D;
        double force = radius > 8.0D ? 0.34D : 0.20D;
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(field.center, field.center).inflate(radius),
                target -> validTarget(player, target))) {
            Vec3 pull = field.center.subtract(target.position());
            if (pull.lengthSqr() > 0.01D) {
                target.setDeltaMovement(target.getDeltaMovement().scale(0.35D)
                        .add(pull.normalize().scale(force)));
                target.hurtMarked = true;
            }
        }
        if (now % 20L == 0L) damageGravityField(player, field, radius);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                field.center.x, field.center.y + 0.35D, field.center.z,
                22, radius * 0.35D, 0.35D, radius * 0.35D, 0.03D);
    }

    private static void explodeTypeFour(Player player, TypeFourField field, long now) {
        ServerLevel level = (ServerLevel) player.level();
        field.exploded = true;
        int fracture = 30;
        if (SignetUpgradeData.hasHelixCoreSignet(
                player, SignetUpgradeData.Skill.HELIX_CONCLUSION_PRISON)) {
            fracture += Math.round(SignetEffects.getScaledSignetValue(
                    player, SignetUpgradeData.Skill.HELIX_CONCLUSION_PRISON, 40.0F));
        }
        field.fractureEnd = now + fracture;
        field.fieldEnd = now + 120L;
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(field.center, field.center).inflate(5.0D),
                target -> validTarget(player, target))) {
            guardedDamage(player, target, 100.0F, 1);
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                field.center.x, field.center.y + 0.5D, field.center.z,
                2, 0.4D, 0.4D, 0.4D, 0.0D);
        level.playSound(null, BlockPos.containing(field.center), SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 1.7F, 0.75F);
    }

    private static void freezeLoadedEntities(Player player, TypeFourField field) {
        ServerLevel level = (ServerLevel) player.level();
        for (Entity entity : level.getAllEntities()) {
            if (entity.isRemoved()
                    || PlayerControlledAllies.isSignetTimeStopExempt(entity, level.players())) {
                field.frozenPositions.remove(entity.getUUID());
                continue;
            }
            Vec3 locked = field.frozenPositions.computeIfAbsent(entity.getUUID(), ignored -> entity.position());
            entity.setDeltaMovement(Vec3.ZERO);
            entity.fallDistance = 0.0F;
            entity.teleportTo(locked.x, locked.y, locked.z);
        }
    }

    private static void damageGravityField(Player player, TypeFourField field, double radius) {
        ServerLevel level = (ServerLevel) player.level();
        float attack = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(field.center, field.center).inflate(radius),
                target -> validTarget(player, target))) {
            guardedDamage(player, target, attack * 2.0F, 1);
            if (SignetUpgradeData.hasHelixCoreSignet(
                    player, SignetUpgradeData.Skill.HELIX_CONCLUSION_POINTER)) {
                guardedDamage(player, target, attack * SignetEffects.getScaledSignetValue(
                        player, SignetUpgradeData.Skill.HELIX_CONCLUSION_POINTER, 2.0F), 1);
            }
        }
    }

    private static void applyTypeFourVulnerability(LivingHurtEvent event, long now) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        for (Map.Entry<UUID, TypeFourField> entry : TYPE_FOUR_FIELDS.entrySet()) {
            TypeFourField field = entry.getValue();
            if (!field.exploded || field.fieldEnd < now
                    || !field.dimension.equals(level.dimension())) continue;
            Player owner = level.getPlayerByUUID(entry.getKey());
            if (owner == null || !validTarget(owner, event.getEntity())
                    || !SignetUpgradeData.hasHelixCoreSignet(
                    owner, SignetUpgradeData.Skill.HELIX_CONCLUSION_ORIGIN)) continue;
            double radius = SignetUpgradeData.hasHelixCoreSignet(
                    owner, SignetUpgradeData.Skill.HELIX_CONCLUSION_POINTER) ? 12.0D : 8.0D;
            if (event.getEntity().position().distanceToSqr(field.center) > radius * radius) continue;
            float bonus = SignetEffects.getScaledSignetValue(
                    owner, SignetUpgradeData.Skill.HELIX_CONCLUSION_ORIGIN, 0.15F);
            if (field.fractureEnd >= now) {
                bonus += SignetEffects.getScaledSignetValue(
                        owner, SignetUpgradeData.Skill.HELIX_CONCLUSION_ORIGIN, 0.05F);
            }
            event.setAmount(event.getAmount() * (1.0F + bonus));
        }
    }

    private static void tickEnergyRestore(Player player, long now) {
        EnergyRestore restore = ENERGY_RESTORES.get(player.getUUID());
        if (restore == null) return;
        long elapsed = Math.max(0L, Math.min(100L, now - restore.startTick));
        int expected = Math.round(restore.total * elapsed / 100.0F);
        if (expected > restore.delivered) {
            addHeldEnergy(player, expected - restore.delivered);
            restore.delivered = expected;
        }
        if (now >= restore.endTick) ENERGY_RESTORES.remove(player.getUUID());
    }

    private static void reduceWeaponSkillCooldowns(Player player, float fraction) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) return;
        CompoundTag data = root.getCompound(Player.PERSISTED_NBT_TAG);
        long now = player.level().getGameTime();
        String[] keys = {
                "AbyssFlowerCooldownFIRST", "AbyssFlowerCooldownSECOND",
                "AbyssFlowerMistCooldown", "FenghuangDownFirstCooldown",
                "FenghuangDownSecondCooldown", "EdenStarFirstCooldown",
                "EdenStarThirdCooldown"
        };
        float reduction = Mth.clamp(fraction, 0.0F, 0.90F);
        for (String key : keys) {
            long until = data.getLong(key);
            if (until > now) {
                data.putLong(key, now + Math.max(1L, Math.round((until - now) * (1.0F - reduction))));
            }
        }
    }

    private static void guardedDamage(Player player, LivingEntity target, float damage, int hits) {
        UUID uuid = player.getUUID();
        boolean added = INTERNAL_DAMAGE.add(uuid);
        try {
            DamageSource source = player.damageSources().playerAttack(player);
            for (int i = 0; i < hits && target.isAlive(); i++) {
                PlayerControlledAllies.hurt(player, target, source, damage);
            }
        } finally {
            if (added) INTERNAL_DAMAGE.remove(uuid);
        }
    }

    private static void damageArmor(LivingEntity target, float damage, float extraMultiplier) {
        int durability = Math.max(1, Mth.ceil(damage / 4.0F * extraMultiplier));
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = target.getItemBySlot(slot);
            if (!armor.isEmpty() && armor.isDamageableItem()) {
                armor.hurtAndBreak(durability, target, ignored -> { });
            }
        }
    }

    private static boolean validTarget(Player player, LivingEntity target) {
        return PlayerControlledAllies.isValidEnemy(player, target);
    }

    private static boolean hasHelix(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_HELIX.get()))
                .isPresent();
    }

    private static int heldEnergy(Player player) {
        ItemStack stack = heldWeapon(player);
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) return FlawlessWeaponEnergy.getEnergy(stack);
        if (stack.is(ModItems.ABYSS_FLOWER.get())) return AbyssFlowerEnergy.get(stack);
        if (stack.is(ModItems.EDEN_STAR.get())) return EdenStarEnergy.get(stack);
        if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) return SkyfireGreatswordEnergy.get(stack);
        if (stack.is(ModItems.FENGHUANG_DOWN.get())) return FenghuangDownEnergy.getEnergy(stack);
        return 0;
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
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) FlawlessWeaponEnergy.addEnergy(player, stack, amount);
        else if (stack.is(ModItems.ABYSS_FLOWER.get())) AbyssFlowerEnergy.add(player, stack, amount);
        else if (stack.is(ModItems.EDEN_STAR.get())) EdenStarEnergy.add(player, stack, amount);
        else if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) SkyfireGreatswordEnergy.add(player, stack, amount);
        else if (stack.is(ModItems.FENGHUANG_DOWN.get())) FenghuangDownEnergy.addEnergy(player, stack, amount);
    }

    private static void consumeHeldEnergy(Player player, int amount) {
        ItemStack stack = heldWeapon(player);
        int next = Math.max(0, heldEnergy(player) - amount);
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) FlawlessWeaponEnergy.setEnergy(player, stack, next);
        else if (stack.is(ModItems.ABYSS_FLOWER.get())) AbyssFlowerEnergy.set(player, stack, next);
        else if (stack.is(ModItems.EDEN_STAR.get())) EdenStarEnergy.set(player, stack, next);
        else if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) SkyfireGreatswordEnergy.set(player, stack, next);
        else if (stack.is(ModItems.FENGHUANG_DOWN.get())) FenghuangDownEnergy.setEnergy(player, stack, next);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayer(event.getEntity().getUUID());
    }

    private static void clearPlayer(UUID uuid) {
        SPOON_UNTIL.remove(uuid);
        COBWEB_UNTIL.remove(uuid);
        ENERGY_RESTORES.remove(uuid);
        MAGIC_COOLDOWN_UNTIL.remove(uuid);
        MAGIC_DAMAGE_BONUS.remove(uuid);
        MAGIC_PENETRATION_UNTIL.remove(uuid);
        TYPE_FOUR_FIELDS.remove(uuid);
        MAGIC_VULNERABILITY.entrySet().removeIf(
                entry -> uuid.equals(entry.getValue().owner()));
    }

    private static final class EnergyRestore {
        private final long startTick;
        private final long endTick;
        private final int total;
        private int delivered;

        private EnergyRestore(long startTick, long endTick, int total, int delivered) {
            this.startTick = startTick;
            this.endTick = endTick;
            this.total = total;
            this.delivered = delivered;
        }
    }

    private static final class TypeFourField {
        private final net.minecraft.resources.ResourceKey<Level> dimension;
        private final Vec3 center;
        private final long explodeTick;
        private final Map<UUID, Vec3> frozenPositions = new HashMap<>();
        private boolean exploded;
        private long fractureEnd;
        private long fieldEnd;

        private TypeFourField(net.minecraft.resources.ResourceKey<Level> dimension,
                              Vec3 center, long explodeTick) {
            this.dimension = dimension;
            this.center = center;
            this.explodeTick = explodeTick;
        }
    }

    private record Vulnerability(UUID owner, float amount, long until) {
    }
}
