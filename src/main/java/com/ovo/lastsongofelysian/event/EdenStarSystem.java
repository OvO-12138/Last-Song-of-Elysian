package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.entity.EdenBlackHoleEntity;
import com.ovo.lastsongofelysian.dimension.ElysianRealmBlockMutationGuard;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEntities;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.EdenStarEnergy;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.network.EdenStarVisualPacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import top.theillusivec4.curios.api.CuriosApi;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EdenStarSystem {
    private static final int ZEROTH_ENERGY_COST = 150;
    private static final int ZEROTH_COOLDOWN = 6000;
    private static final int BLACK_HOLE_DURATION = 600;
    private static final int FIRST_COOLDOWN = 300;
    private static final int FIRST_DURATION = 100;
    private static final int THIRD_ENERGY_COST = 300;
    private static final int THIRD_COOLDOWN = 12000;
    private static final int REWIND_DELAY = 100;
    private static final int REWIND_RADIUS = 6;
    private static final int BLACK_HOLE_FINAL_RING_BURSTS = 16;
    private static final String NBT_ZEROTH_COOLDOWN = "EdenStarZerothCooldown";
    private static final String NBT_FIRST_COOLDOWN = "EdenStarFirstCooldown";
    private static final String NBT_THIRD_COOLDOWN = "EdenStarThirdCooldown";
    private static final Map<ResourceKey<Level>, List<SpaceRewind>> REWINDS = new HashMap<>();
    private static final Map<UUID, GravityPrison> GRAVITY_PRISONS = new HashMap<>();
    private static final Set<UUID> PROTECTED_CASTERS = new HashSet<>();
    private static final Set<UUID> COMBO_IGNORED_DAMAGE_SOURCES = new HashSet<>();

    private EdenStarSystem() {
    }

    public static boolean isHolding(Player player) {
        return !EdenStarEnergy.findHeld(player).isEmpty();
    }

    public static void useZerothRatedPower(ServerPlayer player) {
        ItemStack weapon = EdenStarEnergy.findHeld(player);
        if (weapon.isEmpty()) return;
        long now = player.level().getGameTime();
        if (showCooldown(player, NBT_ZEROTH_COOLDOWN, now, "message.lastsongofelysian.eden_star.zeroth_cooldown")) {
            return;
        }
        if (!EdenStarEnergy.consume(player, weapon, ZEROTH_ENERGY_COST)) return;

        Vec3 ground = groundBlackHoleTarget(player.serverLevel(), targetPoint(player, 64.0D));
        data(player).putLong(NBT_ZEROTH_COOLDOWN, now + ZEROTH_COOLDOWN);
        triggerUltimate(player);
        EdenBlackHoleEntity blackHole = new EdenBlackHoleEntity(
                ModEntities.EDEN_BLACK_HOLE.get(),
                player.serverLevel()
        );
        blackHole.moveTo(
                ground.x,
                ground.y + EdenBlackHoleEntity.DISK_GROUND_OFFSET,
                ground.z,
                player.getYRot(),
                0.0F
        );
        blackHole.initialize(player.getUUID(), BLACK_HOLE_DURATION, ground.y);
        player.serverLevel().addFreshEntity(blackHole);
        player.serverLevel().playSound(null, BlockPos.containing(ground), SoundEvents.END_PORTAL_SPAWN,
                SoundSource.PLAYERS, 4.0F, 0.45F);
    }

    public static void useFirstRatedPower(ServerPlayer player) {
        long now = player.level().getGameTime();
        if (showCooldown(player, NBT_FIRST_COOLDOWN, now, "message.lastsongofelysian.eden_star.first_cooldown")) {
            return;
        }
        LivingEntity target = targetEntity(player, 64.0D);
        if (target == null) {
            player.displayClientMessage(Component.translatable(
                    "message.lastsongofelysian.eden_star.no_target"
            ).withStyle(ChatFormatting.GRAY), true);
            return;
        }
        data(player).putLong(NBT_FIRST_COOLDOWN, now + FIRST_COOLDOWN);
        GRAVITY_PRISONS.put(target.getUUID(), new GravityPrison(
                player.level().dimension(), target.position(), now + FIRST_DURATION
        ));
        EdenStarEnergy.add(player, EdenStarEnergy.findHeld(player), 30);
        ModNetwork.CHANNEL.send(
                PacketDistributor.DIMENSION.with(player.level()::dimension),
                new EdenStarVisualPacket(
                        EdenStarVisualPacket.Effect.GRAVITY_WAVE,
                        target.getId(), target.getX(), target.getY(), target.getZ(), FIRST_DURATION
                )
        );
        player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 1.6F, 0.55F);
    }

    public static void useThirdRatedPower(ServerPlayer player) {
        ItemStack weapon = EdenStarEnergy.findHeld(player);
        if (weapon.isEmpty()) return;
        long now = player.level().getGameTime();
        if (showCooldown(player, NBT_THIRD_COOLDOWN, now, "message.lastsongofelysian.eden_star.third_cooldown")) {
            return;
        }
        if (!EdenStarEnergy.consume(player, weapon, THIRD_ENERGY_COST)) return;

        Vec3 center = targetPoint(player, 48.0D);
        SpaceRewind rewind = captureRewind(player.serverLevel(), player.getUUID(), center, now + REWIND_DELAY);
        REWINDS.computeIfAbsent(player.level().dimension(), key -> new ArrayList<>()).add(rewind);
        data(player).putLong(NBT_THIRD_COOLDOWN, now + THIRD_COOLDOWN);
        player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                center.x, center.y, center.z, 180, REWIND_RADIUS, REWIND_RADIUS, REWIND_RADIUS, 0.02D);
        player.serverLevel().playSound(null, BlockPos.containing(center), SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.PLAYERS, 2.0F, 0.6F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        tickGravityPrisons(level);
        tickRewinds(level);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && isSuppressed(attacker)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getLevel().isClientSide() && event.isCancelable() && isSuppressed(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isSuppressed(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        event.getAffectedEntities().removeIf(entity -> {
            if (!PROTECTED_CASTERS.isEmpty() && PlayerControlledAllies.isElysianHero(entity)) {
                return true;
            }
            for (UUID casterId : PROTECTED_CASTERS) {
                if (level.getEntity(casterId) instanceof Player caster
                        && (entity == caster || PlayerControlledAllies.isProtectedEffectTarget(caster, entity))) {
                    return true;
                }
            }
            return false;
        });
    }

    private static void tickGravityPrisons(ServerLevel level) {
        long now = level.getGameTime();
        Iterator<Map.Entry<UUID, GravityPrison>> iterator = GRAVITY_PRISONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, GravityPrison> entry = iterator.next();
            GravityPrison prison = entry.getValue();
            if (!prison.dimension.equals(level.dimension())) continue;
            Entity entity = level.getEntity(entry.getKey());
            if (now >= prison.endTick || !(entity instanceof LivingEntity living) || !living.isAlive()) {
                iterator.remove();
                continue;
            }
            living.teleportTo(prison.position.x, prison.position.y, prison.position.z);
            living.setDeltaMovement(Vec3.ZERO);
            living.fallDistance = 0.0F;
            if (living instanceof Mob mob) mob.getNavigation().stop();
        }
    }

    public static void finishBlackHole(
            ServerLevel level,
            UUID casterId,
            Vec3 center,
            double accretionDiskRadius
    ) {
        ServerPlayer caster = casterId == null
                ? null
                : level.getServer().getPlayerList().getPlayer(casterId);
        double explosionRadius = Math.max(1.0D, accretionDiskRadius);
        int burstCount = Mth.clamp(Mth.ceil(explosionRadius), 24, 96);
        int portalCount = Mth.clamp(Mth.ceil(explosionRadius * 25.0D), 1200, 2400);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                center.x, center.y, center.z, burstCount,
                explosionRadius * 0.90D, explosionRadius * 0.30D,
                explosionRadius * 0.90D, 0.0D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                center.x, center.y, center.z, portalCount,
                explosionRadius * 0.90D, explosionRadius * 0.40D,
                explosionRadius * 0.90D, 1.0D);
        if (casterId != null) PROTECTED_CASTERS.add(casterId);
        if (casterId != null) COMBO_IGNORED_DAMAGE_SOURCES.add(casterId);
        Level.ExplosionInteraction interaction = ElysianRealmBlockMutationGuard.isProtectedLevel(level)
                ? Level.ExplosionInteraction.NONE
                : Level.ExplosionInteraction.BLOCK;
        try {
            level.explode(caster, center.x, center.y, center.z,
                    (float) explosionRadius, false, interaction);
            double ringRadius = explosionRadius * 0.70D;
            float ringPower = (float) Math.max(6.0D, explosionRadius * 0.32D);
            double phase = level.getRandom().nextDouble() * Mth.TWO_PI;
            for (int i = 0; i < BLACK_HOLE_FINAL_RING_BURSTS; i++) {
                double angle = phase + Mth.TWO_PI * i / BLACK_HOLE_FINAL_RING_BURSTS;
                double x = center.x + Math.cos(angle) * ringRadius;
                double y = center.y + Math.sin(angle * 2.0D) * explosionRadius * 0.04D;
                double z = center.z + Math.sin(angle) * ringRadius;
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        x, y, z, 2,
                        ringPower * 0.20D, ringPower * 0.12D,
                        ringPower * 0.20D, 0.0D);
                level.explode(caster, x, y, z,
                        ringPower, false, interaction);
            }
        } finally {
            if (casterId != null) COMBO_IGNORED_DAMAGE_SOURCES.remove(casterId);
            if (casterId != null) PROTECTED_CASTERS.remove(casterId);
        }
    }

    public static void explodeBlackHoleVictim(ServerLevel level, UUID casterId, LivingEntity victim) {
        if (casterId != null) PROTECTED_CASTERS.add(casterId);
        COMBO_IGNORED_DAMAGE_SOURCES.add(victim.getUUID());
        try {
            level.explode(victim,
                    victim.getX(), victim.getY() + victim.getBbHeight() * 0.5D, victim.getZ(),
                    2.5F, false, Level.ExplosionInteraction.NONE);
        } finally {
            COMBO_IGNORED_DAMAGE_SOURCES.remove(victim.getUUID());
            if (casterId != null) PROTECTED_CASTERS.remove(casterId);
        }
    }

    public static boolean hurtFromBlackHole(LivingEntity target, DamageSource source, float amount) {
        if (PlayerControlledAllies.isElysianHero(target)) return false;
        Entity attacker = source.getEntity();
        if (attacker == null) return target.hurt(source, amount);
        if (attacker instanceof Player player
                && PlayerControlledAllies.isProtectedEffectTarget(player, target)) return false;
        COMBO_IGNORED_DAMAGE_SOURCES.add(attacker.getUUID());
        try {
            return target.hurt(source, amount);
        } finally {
            COMBO_IGNORED_DAMAGE_SOURCES.remove(attacker.getUUID());
        }
    }

    public static boolean isBlackHoleComboDamage(DamageSource source) {
        Entity attacker = source.getEntity();
        return attacker != null && COMBO_IGNORED_DAMAGE_SOURCES.contains(attacker.getUUID());
    }

    private static void tickRewinds(ServerLevel level) {
        List<SpaceRewind> rewinds = REWINDS.get(level.dimension());
        if (rewinds == null) return;
        long now = level.getGameTime();
        Iterator<SpaceRewind> iterator = rewinds.iterator();
        while (iterator.hasNext()) {
            SpaceRewind rewind = iterator.next();
            if (now < rewind.restoreTick) continue;
            restoreRewind(level, rewind);
            iterator.remove();
        }
        if (rewinds.isEmpty()) REWINDS.remove(level.dimension());
    }

    private static SpaceRewind captureRewind(ServerLevel level, UUID caster, Vec3 center, long restoreTick) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        BlockPos origin = BlockPos.containing(center);
        for (BlockPos mutable : BlockPos.betweenClosed(
                origin.offset(-REWIND_RADIUS, -REWIND_RADIUS, -REWIND_RADIUS),
                origin.offset(REWIND_RADIUS, REWIND_RADIUS, REWIND_RADIUS))) {
            if (mutable.distSqr(origin) > REWIND_RADIUS * REWIND_RADIUS || level.getBlockEntity(mutable) != null) {
                continue;
            }
            blocks.put(mutable.immutable(), level.getBlockState(mutable));
        }
        Map<UUID, EntityState> entities = new HashMap<>();
        AABB area = new AABB(center, center).inflate(REWIND_RADIUS);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && !PlayerControlledAllies.isElysianHero(entity))) {
            if (!living.getUUID().equals(caster)) {
                entities.put(living.getUUID(), new EntityState(living.position(), living.getHealth()));
            }
        }
        return new SpaceRewind(center, restoreTick, blocks, entities);
    }

    private static void restoreRewind(ServerLevel level, SpaceRewind rewind) {
        rewind.blocks.forEach((pos, state) -> {
            if (level.getBlockEntity(pos) == null) level.setBlock(pos, state, 3);
        });
        rewind.entities.forEach((uuid, state) -> {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof LivingEntity living && living.isAlive()
                    && !PlayerControlledAllies.isElysianHero(living)) {
                living.teleportTo(state.position.x, state.position.y, state.position.z);
                living.setDeltaMovement(Vec3.ZERO);
                living.setHealth(Mth.clamp(state.health, 1.0F, living.getMaxHealth()));
            }
        });
        level.sendParticles(ParticleTypes.FLASH,
                rewind.center.x, rewind.center.y, rewind.center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD,
                rewind.center.x, rewind.center.y, rewind.center.z, 300,
                REWIND_RADIUS, REWIND_RADIUS, REWIND_RADIUS, 0.08D);
        level.playSound(null, BlockPos.containing(rewind.center), SoundEvents.END_PORTAL_FRAME_FILL,
                SoundSource.PLAYERS, 2.0F, 0.8F);
    }

    private static boolean isSuppressed(LivingEntity entity) {
        GravityPrison prison = GRAVITY_PRISONS.get(entity.getUUID());
        return prison != null
                && prison.dimension == entity.level().dimension()
                && prison.endTick > entity.level().getGameTime();
    }

    private static LivingEntity targetEntity(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(range));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, start, end, player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0D),
                entity -> entity instanceof LivingEntity living
                        && PlayerControlledAllies.isValidEnemy(player, living),
                range * range
        );
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    private static Vec3 targetPoint(ServerPlayer player, double range) {
        HitResult hit = player.pick(range, 1.0F, false);
        return hit.getType() == HitResult.Type.MISS
                ? player.getEyePosition().add(player.getLookAngle().scale(range * 0.5D))
                : hit.getLocation();
    }

    private static Vec3 groundBlackHoleTarget(ServerLevel level, Vec3 target) {
        int x = Mth.floor(target.x);
        int z = Mth.floor(target.z);
        int minimumY = level.getMinBuildHeight();
        int maximumY = level.getMaxBuildHeight() - 1;
        int startY = Mth.clamp(Mth.floor(target.y) + 2, minimumY, maximumY);
        int endY = Math.max(minimumY, startY - 128);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, startY, z);
        for (int y = startY; y >= endY; y--) {
            cursor.setY(y);
            BlockState state = level.getBlockState(cursor);
            VoxelShape collision = state.getCollisionShape(level, cursor);
            if (!collision.isEmpty()) {
                return new Vec3(
                        target.x,
                        y + collision.max(Direction.Axis.Y),
                        target.z
                );
            }
        }
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new Vec3(target.x, surfaceY, target.z);
    }

    private static CompoundTag data(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static boolean showCooldown(ServerPlayer player, String key, long now, String messageKey) {
        long until = data(player).getLong(key);
        if (until <= now) return false;
        int seconds = Mth.ceil((until - now) / 20.0D);
        player.displayClientMessage(Component.translatable(messageKey, seconds)
                .withStyle(ChatFormatting.GRAY), true);
        return true;
    }

    private static void triggerUltimate(Player player) {
        CorrosionCurseEvents.onWeaponUltimate(player);
        SignetEffects.onWeaponUltimateActivated(player, 600);
        if (hasSignet(player, ModItems.SIGNET_OF_DELIVERANCE.get())) {
            SignetEffects.activateDeliveranceUltimateBuffFromEdenStar(player);
        }
    }

    private static boolean hasSignet(Player player, net.minecraft.world.item.Item item) {
        return CuriosApi.getCuriosHelper().findFirstCurio(player, stack -> stack.is(item)).isPresent();
    }

    private record GravityPrison(ResourceKey<Level> dimension, Vec3 position, long endTick) {
    }

    private record SpaceRewind(
            Vec3 center,
            long restoreTick,
            Map<BlockPos, BlockState> blocks,
            Map<UUID, EntityState> entities
    ) {
    }

    private record EntityState(Vec3 position, float health) {
    }
}
