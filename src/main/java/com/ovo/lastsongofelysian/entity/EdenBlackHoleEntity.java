package com.ovo.lastsongofelysian.entity;

import com.ovo.lastsongofelysian.event.EdenStarSystem;
import com.ovo.lastsongofelysian.event.SignetEffects;
import com.ovo.lastsongofelysian.dimension.ElysianRealmBlockMutationGuard;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public final class EdenBlackHoleEntity extends Entity {
    public static final float INITIAL_CORE_RADIUS = 9.0F;
    public static final float INITIAL_HORIZON_RADIUS = 25.0F;
    public static final float ACCRETION_DISK_SCALE = 1.90F;
    public static final double DISK_GROUND_OFFSET = 0.04D;
    private static final float MAX_GROWTH = 9.0F;
    private static final EntityDataAccessor<Float> HORIZON_RADIUS = SynchedEntityData.defineId(
            EdenBlackHoleEntity.class,
            EntityDataSerializers.FLOAT
    );
    private static final String NBT_CASTER = "Caster";
    private static final String NBT_LIFETIME = "Lifetime";
    private static final String NBT_AGE = "Age";
    private static final String NBT_CONSUMED_BLOCKS = "ConsumedBlocks";
    private static final String NBT_GROUND_Y = "GroundY";
    private UUID casterId;
    private int lifetime = 600;
    private int age;
    private int consumedBlocks;
    private double groundY = Double.NaN;

    public EdenBlackHoleEntity(EntityType<? extends EdenBlackHoleEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void initialize(UUID casterId, int lifetime, double groundY) {
        this.casterId = casterId;
        this.lifetime = Math.max(1, lifetime);
        this.age = 0;
        this.consumedBlocks = 0;
        this.groundY = groundY;
        this.entityData.set(HORIZON_RADIUS, INITIAL_HORIZON_RADIUS);
    }

    public float getEventHorizonRadius() {
        return this.entityData.get(HORIZON_RADIUS);
    }

    public float getAccretionDiskRadius() {
        return getEventHorizonRadius() * ACCRETION_DISK_SCALE;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(HORIZON_RADIUS, INITIAL_HORIZON_RADIUS);
    }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level().isClientSide()) return;
        if (!(this.level() instanceof ServerLevel level)) {
            this.discard();
            return;
        }
        if (++this.age >= this.lifetime) {
            EdenStarSystem.finishBlackHole(
                    level,
                    this.casterId,
                    this.position(),
                    this.getAccretionDiskRadius()
            );
            this.discard();
            return;
        }

        float timeGrowth = MAX_GROWTH * 0.35F * this.age / this.lifetime;
        float consumedGrowth = this.consumedBlocks / 500.0F;
        float growth = Math.min(MAX_GROWTH, timeGrowth + consumedGrowth);
        float horizonRadius = INITIAL_HORIZON_RADIUS + growth;
        float coreRadius = INITIAL_CORE_RADIUS + growth;
        if (!Double.isFinite(this.groundY)) {
            this.groundY = this.getY() - DISK_GROUND_OFFSET;
        }
        this.setPos(
                this.getX(),
                this.groundY + DISK_GROUND_OFFSET,
                this.getZ()
        );
        if (Math.abs(this.getEventHorizonRadius() - horizonRadius) >= 0.02F) {
            this.entityData.set(HORIZON_RADIUS, horizonRadius);
        }

        ServerPlayer caster = this.casterId == null
                ? null
                : level.getServer().getPlayerList().getPlayer(this.casterId);
        applyEventHorizon(level, caster, horizonRadius, coreRadius);
        consumeBlocks(level, coreRadius);
    }

    private void applyEventHorizon(
            ServerLevel level,
            ServerPlayer caster,
            double horizonRadius,
            double coreRadius
    ) {
        double accretionDiskRadius = horizonRadius * ACCRETION_DISK_SCALE;
        AABB area = new AABB(this.position(), this.position()).inflate(accretionDiskRadius);
        DamageSource damage = caster == null
                ? new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD))
                : new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD), caster);

        for (Entity entity : level.getEntities(this, area, candidate ->
                candidate.isAlive()
                        && candidate != this
                        && !PlayerControlledAllies.isElysianHero(candidate)
                        && (this.casterId == null || !candidate.getUUID().equals(this.casterId))
                        && (caster == null || !PlayerControlledAllies.isProtectedEffectTarget(caster, candidate)))) {
            Vec3 offset = this.position().subtract(entity.getBoundingBox().getCenter());
            double distance = offset.length();
            if (distance > accretionDiskRadius) continue;

            double depth = Mth.clamp(1.0D - distance / accretionDiskRadius, 0.0D, 1.0D);
            double timeScale = Mth.lerp(depth, 0.52D, 0.04D);
            Vec3 slowedMotion = entity.getDeltaMovement().scale(timeScale * 0.35D);
            Vec3 pull = distance < 0.2D
                    ? Vec3.ZERO
                    : offset.normalize().scale(0.16D + 0.72D * depth);
            entity.setDeltaMovement(slowedMotion.add(pull));
            entity.hasImpulse = true;
            entity.fallDistance = 0.0F;
            if (entity instanceof ServerPlayer player) player.hurtMarked = true;
            if (entity instanceof Mob mob) mob.getNavigation().stop();

            if (entity instanceof LivingEntity living) {
                int amplifier = Mth.clamp((int) Math.floor(depth * 5.0D), 0, 4);
                living.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 6, amplifier, true, false, false
                ));
                living.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SLOWDOWN, 6, amplifier, true, false, false
                ));
                living.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 6, Math.max(0, amplifier - 1), true, false, false
                ));
                if (distance <= coreRadius) {
                    living.invulnerableTime = 0;
                    EdenStarSystem.hurtFromBlackHole(living, damage, 50.0F);
                    damageArmor(living);
                    if (living instanceof Player player) {
                        SignetEffects.addCorruptionByPercent(player, 0.001F);
                    }
                    if ((this.age + living.getId()) % 10 == 0) {
                        EdenStarSystem.explodeBlackHoleVictim(level, this.casterId, living);
                    }
                }
            }
        }
    }

    private void consumeBlocks(ServerLevel level, double radius) {
        if (ElysianRealmBlockMutationGuard.isProtectedLevel(level)) return;

        for (int i = 0; i < 20; i++) {
            double theta = level.random.nextDouble() * Math.PI * 2.0D;
            double phi = Math.acos(level.random.nextDouble() * 2.0D - 1.0D);
            double distance = 1.5D + level.random.nextDouble() * radius;
            BlockPos pos = BlockPos.containing(
                    this.getX() + Math.sin(phi) * Math.cos(theta) * distance,
                    this.getY() + Math.cos(phi) * distance,
                    this.getZ() + Math.sin(phi) * Math.sin(theta) * distance
            );
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F || level.getBlockEntity(pos) != null) {
                continue;
            }
            if (level.destroyBlock(pos, false)) this.consumedBlocks++;
        }
    }

    private static void damageArmor(LivingEntity living) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        }) {
            ItemStack armor = living.getItemBySlot(slot);
            if (!armor.isEmpty()) {
                armor.hurtAndBreak(5, living, entity -> entity.broadcastBreakEvent(slot));
            }
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(this.getAccretionDiskRadius() * 1.1D);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double range = Math.max(256.0D, this.getAccretionDiskRadius() * 4.0D);
        return distance < range * range;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(NBT_CASTER)) this.casterId = tag.getUUID(NBT_CASTER);
        this.lifetime = Math.max(1, tag.getInt(NBT_LIFETIME));
        this.age = Math.max(0, tag.getInt(NBT_AGE));
        this.consumedBlocks = Math.max(0, tag.getInt(NBT_CONSUMED_BLOCKS));
        this.groundY = tag.contains(NBT_GROUND_Y, Tag.TAG_DOUBLE)
                ? tag.getDouble(NBT_GROUND_Y)
                : Double.NaN;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.casterId != null) tag.putUUID(NBT_CASTER, this.casterId);
        tag.putInt(NBT_LIFETIME, this.lifetime);
        tag.putInt(NBT_AGE, this.age);
        tag.putInt(NBT_CONSUMED_BLOCKS, this.consumedBlocks);
        if (Double.isFinite(this.groundY)) tag.putDouble(NBT_GROUND_Y, this.groundY);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
