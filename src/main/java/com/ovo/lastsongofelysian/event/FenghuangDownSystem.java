package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.network.FenghuangDownVisualPacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.FenghuangDownEnergy;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FenghuangDownSystem {
    public static final int DOMAIN_DURATION = 100;
    public static final double DOMAIN_RADIUS = 32.0D;

    private static final int MAX_FEATHERS = 10;
    private static final int ZEROTH_POWER_COOLDOWN = 6000;
    private static final int FIRST_POWER_COOLDOWN = 4800;
    private static final int SECOND_POWER_COOLDOWN = 2400;
    private static final int FIRST_POWER_DURATION = 100;
    private static final int FIRST_POWER_SWORD_COUNT = 300;
    private static final int FIRST_POWER_SWORDS_PER_TICK = 3;
    private static final float FIRST_POWER_SWORD_DAMAGE = 50.0F;
    private static final double FIRST_POWER_RANGE = 36.0D;
    private static final int COCOON_SEVER_DURATION = 1200;
    private static final int IMMOBILIZE_DURATION = 300;
    private static final String NBT_FEATHERS = "FenghuangDownFeathers";
    private static final String NBT_ZEROTH_COOLDOWN = "FenghuangDownZerothCooldown";
    private static final String NBT_FIRST_COOLDOWN = "FenghuangDownFirstCooldown";
    private static final String NBT_SECOND_COOLDOWN = "FenghuangDownSecondCooldown";
    private static final String NBT_COCOON_SEVERED_UNTIL = "FenghuangDownCocoonSeveredUntil";
    private static final String NBT_IMMOBILIZED_UNTIL = "FenghuangDownImmobilizedUntil";
    private static final String NBT_IMMOBILIZED_X = "FenghuangDownImmobilizedX";
    private static final String NBT_IMMOBILIZED_Y = "FenghuangDownImmobilizedY";
    private static final String NBT_IMMOBILIZED_Z = "FenghuangDownImmobilizedZ";
    private static final Map<ResourceKey<Level>, List<TimeStopDomain>> TIME_STOP_DOMAINS =
            new HashMap<>();
    private static final Map<ResourceKey<Level>, List<FirstPowerBarrage>> FIRST_POWER_BARRAGES =
            new HashMap<>();

    private FenghuangDownSystem() {
    }

    public static boolean isHoldingFenghuangDown(Player player) {
        return player.getMainHandItem().is(ModItems.FENGHUANG_DOWN.get())
                || player.getOffhandItem().is(ModItems.FENGHUANG_DOWN.get());
    }

    private static boolean hasFenghuangDown(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(ModItems.FENGHUANG_DOWN.get())) {
                return true;
            }
        }
        return false;
    }

    public static void useZerothRatedPower(ServerPlayer player) {
        HitResult result = player.pick(96.0D, 1.0F, false);
        if (!(result instanceof BlockHitResult blockHitResult)
                || result.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos targetPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
        Vec3 target = findSafeTarget(player, targetPos);
        CompoundTag data = getData(player);
        long now = player.level().getGameTime();
        if (showCooldown(player, data, NBT_ZEROTH_COOLDOWN, now, "第零额定功率")) {
            return;
        }
        ItemStack fenghuangDown = FenghuangDownEnergy.findHeld(player);
        if (!FenghuangDownEnergy.consumeEnergy(player, fenghuangDown, 100)) {
            return;
        }
        data.putLong(NBT_ZEROTH_COOLDOWN, now + ZEROTH_POWER_COOLDOWN);
        ListTag feathers = data.getList(NBT_FEATHERS, Tag.TAG_COMPOUND);
        while (feathers.size() >= MAX_FEATHERS) {
            feathers.remove(0);
        }

        CompoundTag feather = new CompoundTag();
        feather.putString("Dimension", player.level().dimension().location().toString());
        feather.putDouble("X", target.x);
        feather.putDouble("Y", target.y);
        feather.putDouble("Z", target.z);
        feather.putFloat("Yaw", player.getYRot());
        feather.putFloat("Pitch", player.getXRot());
        feathers.add(feather);
        data.put(NBT_FEATHERS, feathers);

        sendFeatherVisual(player, target, 40);

        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.END_ROD, target.x, target.y + 0.9D, target.z,
                28, 0.22D, 0.45D, 0.22D, 0.018D);
        level.sendParticles(ParticleTypes.FLAME, target.x, target.y + 0.7D, target.z,
                10, 0.12D, 0.25D, 0.12D, 0.006D);
        level.playSound(null, target.x, target.y, target.z,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    public static void useFirstRatedPower(ServerPlayer player) {
        CompoundTag data = getData(player);
        long now = player.level().getGameTime();
        if (showCooldown(player, data, NBT_FIRST_COOLDOWN, now, "第一额定功率")) {
            return;
        }
        data.putLong(NBT_FIRST_COOLDOWN, now + FIRST_POWER_COOLDOWN);
        triggerWeaponSkill(player);

        Inventory inventory = player.getInventory();
        int burnedItems = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || stack.is(ModItems.FENGHUANG_DOWN.get())) {
                continue;
            }
            burnedItems += stack.getCount();
            inventory.setItem(slot, ItemStack.EMPTY);
        }
        inventory.setChanged();

        float damage = 2000.0F + burnedItems * 10.0F;
        FenghuangDownEnergy.addEnergy(
                player,
                FenghuangDownEnergy.find(player),
                30
        );

        data.putLong(
                NBT_COCOON_SEVERED_UNTIL,
                now + COCOON_SEVER_DURATION
        );
        ServerLevel level = player.serverLevel();
        releaseFlameWave(level, player, damage);
        FIRST_POWER_BARRAGES.computeIfAbsent(level.dimension(), key -> new ArrayList<>())
                .add(new FirstPowerBarrage(
                        player.getUUID(), now + FIRST_POWER_DURATION + 1L,
                        FIRST_POWER_SWORD_COUNT
                ));
        ModNetwork.CHANNEL.send(
                PacketDistributor.DIMENSION.with(level::dimension),
                new FenghuangDownVisualPacket(
                        FenghuangDownVisualPacket.Effect.FIRST_POWER_BARRAGE,
                        player.getId(), player.getX(), player.getY(), player.getZ(),
                        FIRST_POWER_DURATION
                )
        );
        level.playSound(null, player.blockPosition(), SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.PLAYERS, 2.5F, 0.55F);
    }

    public static void useSecondRatedPower(ServerPlayer player) {
        CompoundTag data = getData(player);
        long now = player.level().getGameTime();
        if (showCooldown(player, data, NBT_SECOND_COOLDOWN, now, "第二额定功率")) {
            return;
        }
        data.putLong(NBT_SECOND_COOLDOWN, now + SECOND_POWER_COOLDOWN);
        triggerWeaponSkill(player);

        Vec3 center = player.position();
        TimeStopDomain domain = new TimeStopDomain(
                player.getUUID(), center, now + DOMAIN_DURATION, player.level().getDayTime()
        );
        TIME_STOP_DOMAINS.computeIfAbsent(player.level().dimension(), key -> new ArrayList<>())
                .add(domain);

        ModNetwork.CHANNEL.send(
                PacketDistributor.DIMENSION.with(player.level()::dimension),
                new FenghuangDownVisualPacket(
                        FenghuangDownVisualPacket.Effect.MIND_DOMAIN,
                        player.getId(), center.x, center.y, center.z, DOMAIN_DURATION
                )
        );
        player.serverLevel().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.7F);
    }

    public static boolean isCocoonConnectionSevered(Player player) {
        return player != null
                && player.level().getGameTime() < getData(player).getLong(NBT_COCOON_SEVERED_UNTIL);
    }

    public static boolean shouldFreeze(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        for (TimeStopDomain domain : activeDomains(level)) {
            if (!domain.caster.equals(entity.getUUID())
                    && domain.center.distanceToSqr(entity.position()) <= DOMAIN_RADIUS * DOMAIN_RADIUS) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldFreeze(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        Vec3 center = Vec3.atCenterOf(pos);
        for (TimeStopDomain domain : activeDomains(serverLevel)) {
            if (domain.center.distanceToSqr(center) <= DOMAIN_RADIUS * DOMAIN_RADIUS) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldFreezeChunk(ServerLevel level, LevelChunk chunk) {
        double x = chunk.getPos().getMiddleBlockX();
        double z = chunk.getPos().getMiddleBlockZ();
        double reach = DOMAIN_RADIUS + 8.0D;
        for (TimeStopDomain domain : activeDomains(level)) {
            double dx = domain.center.x - x;
            double dz = domain.center.z - z;
            if (dx * dx + dz * dz <= reach * reach) {
                return true;
            }
        }
        return false;
    }

    private static Vec3 findSafeTarget(ServerPlayer player, BlockPos initial) {
        AABB original = player.getBoundingBox();
        for (int offset = 0; offset <= 3; offset++) {
            BlockPos pos = initial.above(offset);
            double x = pos.getX() + 0.5D;
            double y = pos.getY();
            double z = pos.getZ() + 0.5D;
            AABB moved = original.move(x - player.getX(), y - player.getY(), z - player.getZ());
            if (player.level().noCollision(player, moved)) {
                return new Vec3(x, y, z);
            }
        }
        return new Vec3(initial.getX() + 0.5D, initial.getY() + 1.0D, initial.getZ() + 0.5D);
    }

    private static void immobilize(LivingEntity entity, long until) {
        CompoundTag data = entity.getPersistentData();
        data.putLong(NBT_IMMOBILIZED_UNTIL, until);
        data.putDouble(NBT_IMMOBILIZED_X, entity.getX());
        data.putDouble(NBT_IMMOBILIZED_Y, entity.getY());
        data.putDouble(NBT_IMMOBILIZED_Z, entity.getZ());
        entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, IMMOBILIZE_DURATION, 255, false, false, false
        ));
        entity.setDeltaMovement(Vec3.ZERO);
    }

    private static boolean isImmobilized(LivingEntity entity) {
        return entity.level().getGameTime()
                < entity.getPersistentData().getLong(NBT_IMMOBILIZED_UNTIL);
    }

    private static List<TimeStopDomain> activeDomains(ServerLevel level) {
        List<TimeStopDomain> domains = TIME_STOP_DOMAINS.get(level.dimension());
        if (domains == null) {
            return List.of();
        }
        domains.removeIf(domain -> domain.endTick <= level.getGameTime());
        if (domains.isEmpty()) {
            TIME_STOP_DOMAINS.remove(level.dimension());
            return List.of();
        }
        return domains;
    }

    private static CompoundTag getData(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static boolean showCooldown(
            ServerPlayer player,
            CompoundTag data,
            String key,
            long now,
            String powerName
    ) {
        long cooldownUntil = data.getLong(key);
        if (now >= cooldownUntil) {
            return false;
        }
        long seconds = Math.max(1L, (cooldownUntil - now + 19L) / 20L);
        player.displayClientMessage(
                ModText.literal(powerName + "冷却中：" + seconds + "秒")
                        .withStyle(ChatFormatting.GRAY),
                true
        );
        return true;
    }

    private static void sendFeatherVisual(ServerPlayer player, Vec3 position, int duration) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new FenghuangDownVisualPacket(
                        FenghuangDownVisualPacket.Effect.FEATHER,
                        player.getId(),
                        position.x,
                        position.y,
                        position.z,
                        duration
                )
        );
    }

    private static void triggerWeaponSkill(Player player) {
        SignetEffects.onWeaponSkillActivated(player);
    }

    private static boolean isDomainCaster(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }
        for (TimeStopDomain domain : activeDomains(level)) {
            if (domain.caster.equals(player.getUUID())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInsideOwnedDomain(Player player, Entity target) {
        if (!(player.level() instanceof ServerLevel level) || target.level() != level) {
            return false;
        }
        for (TimeStopDomain domain : activeDomains(level)) {
            if (domain.caster.equals(player.getUUID())
                    && domain.center.distanceToSqr(target.position())
                    <= DOMAIN_RADIUS * DOMAIN_RADIUS) {
                return true;
            }
        }
        return false;
    }

    private static void releaseFlameWave(ServerLevel level, ServerPlayer player, float damage) {
        Vec3 origin = player.getEyePosition();
        Vec3 forward = player.getLookAngle().normalize();
        DamageSource flameDamage = new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.ON_FIRE),
                player
        );
        long immobilizedUntil = level.getGameTime() + IMMOBILIZE_DURATION;
        level.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(32.0D),
                        target -> PlayerControlledAllies.isValidEnemy(player, target)
                )
                .forEach(target -> {
                    Vec3 offset = target.getBoundingBox().getCenter().subtract(origin);
                    double forwardDistance = offset.dot(forward);
                    if (forwardDistance <= 0.0D || forwardDistance > 30.0D) {
                        return;
                    }
                    double lateralSquared = Math.max(
                            0.0D,
                            offset.lengthSqr() - forwardDistance * forwardDistance
                    );
                    double radius = 2.0D + forwardDistance * 0.45D;
                    if (lateralSquared > radius * radius) {
                        return;
                    }
                    target.setSecondsOnFire(15);
                    PlayerControlledAllies.hurt(player, target, flameDamage, damage);
                    if (target.isAlive()) {
                        immobilize(target, immobilizedUntil);
                    }
                });

        Vec3 ignition = origin.add(forward.scale(2.0D));
        level.sendParticles(
                ParticleTypes.FLAME,
                ignition.x, ignition.y, ignition.z,
                70, 1.2D, 0.85D, 1.2D, 0.13D
        );
        level.sendParticles(
                ParticleTypes.LAVA,
                ignition.x, ignition.y, ignition.z,
                24, 0.8D, 0.55D, 0.8D, 0.045D
        );
        level.sendParticles(
                ParticleTypes.END_ROD,
                ignition.x, ignition.y, ignition.z,
                18, 0.45D, 0.35D, 0.45D, 0.075D
        );
    }

    private static void tickFirstPowerBarrages(ServerLevel level) {
        List<FirstPowerBarrage> barrages = FIRST_POWER_BARRAGES.get(level.dimension());
        if (barrages == null) {
            return;
        }
        long now = level.getGameTime();
        barrages.removeIf(barrage -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(barrage.caster);
            if (player == null || player.level() != level
                    || now >= barrage.endTick || barrage.remainingSwords <= 0) {
                return true;
            }
            int count = Math.min(FIRST_POWER_SWORDS_PER_TICK, barrage.remainingSwords);
            for (int i = 0; i < count; i++) {
                launchXuanyuanSword(level, player);
            }
            barrage.remainingSwords -= count;
            return false;
        });
        if (barrages.isEmpty()) {
            FIRST_POWER_BARRAGES.remove(level.dimension());
        }
    }

    private static void launchXuanyuanSword(ServerLevel level, ServerPlayer player) {
        Vec3 forward = player.getLookAngle().normalize();
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-5D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(forward).normalize();
        Vec3 start = player.position()
                .add(0.0D, 1.4D, 0.0D)
                .subtract(forward.scale(3.0D))
                .add(right.scale((level.random.nextDouble() - 0.5D) * 7.0D))
                .add(up.scale(level.random.nextDouble() * 4.5D - 0.5D));
        Vec3 end = start.add(forward.scale(FIRST_POWER_RANGE));
        LivingEntity target = findSwordTarget(level, player, start, end);
        if (target != null) {
            PlayerControlledAllies.hurt(player, target,
                    player.damageSources().playerAttack(player), FIRST_POWER_SWORD_DAMAGE);
        }
    }

    private static LivingEntity findSwordTarget(
            ServerLevel level,
            ServerPlayer player,
            Vec3 start,
            Vec3 end
    ) {
        LivingEntity closest = null;
        double closestDistance = start.distanceToSqr(end);
        AABB searchArea = new AABB(start, end).inflate(1.5D);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                target -> PlayerControlledAllies.isValidEnemy(player, target)
        )) {
            Vec3 hit = target.getBoundingBox().inflate(0.6D).clip(start, end).orElse(null);
            if (hit == null) {
                continue;
            }
            double distance = start.distanceToSqr(hit);
            if (distance < closestDistance) {
                closest = target;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !hasFenghuangDown(player)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        CompoundTag data = getData(player);
        ListTag feathers = data.getList(NBT_FEATHERS, Tag.TAG_COMPOUND);
        if (feathers.isEmpty()) {
            return;
        }

        CompoundTag feather = feathers.getCompound(0);
        ResourceLocation dimensionId = ResourceLocation.tryParse(feather.getString("Dimension"));
        if (dimensionId == null) {
            feathers.remove(0);
            data.put(NBT_FEATHERS, feathers);
            return;
        }
        ServerLevel targetLevel = player.server.getLevel(
                ResourceKey.create(Registries.DIMENSION, dimensionId)
        );
        if (targetLevel == null) {
            return;
        }

        feathers.remove(0);
        data.put(NBT_FEATHERS, feathers);
        event.setCanceled(true);
        player.deathTime = 0;
        player.setHealth(player.getMaxHealth());
        player.setAbsorptionAmount(0.0F);
        player.clearFire();
        player.setAirSupply(player.getMaxAirSupply());
        player.invulnerableTime = 60;
        player.teleportTo(
                targetLevel,
                feather.getDouble("X"),
                feather.getDouble("Y"),
                feather.getDouble("Z"),
                Set.of(),
                feather.getFloat("Yaw"),
                feather.getFloat("Pitch")
        );
        targetLevel.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                80, 0.6D, 1.0D, 0.6D, 0.035D);
        targetLevel.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS, 1.0F, 1.15F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDomainKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || !isDomainCaster(player)) {
            return;
        }
        ItemStack fenghuangDown = FenghuangDownEnergy.find(player);
        if (!fenghuangDown.isEmpty()) {
            FenghuangDownEnergy.addEnergy(player, fenghuangDown, 5);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDomainDamage(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getEntity() == player
                || PlayerControlledAllies.isElysianHero(event.getEntity())
                || !isDomainCaster(player)) {
            return;
        }
        event.setAmount(event.getAmount() * 2.0F);
        player.serverLevel().sendParticles(
                ParticleTypes.SWEEP_ATTACK,
                event.getEntity().getX(),
                event.getEntity().getY() + event.getEntity().getBbHeight() * 0.5D,
                event.getEntity().getZ(),
                5, 0.35D, 0.45D, 0.35D, 0.0D
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.side != LogicalSide.SERVER
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 10 != 0) {
            return;
        }
        ListTag feathers = getData(player).getList(NBT_FEATHERS, Tag.TAG_COMPOUND);
        String dimension = player.level().dimension().location().toString();
        for (int i = 0; i < feathers.size(); i++) {
            CompoundTag feather = feathers.getCompound(i);
            if (!dimension.equals(feather.getString("Dimension"))) {
                continue;
            }
            Vec3 position = new Vec3(
                    feather.getDouble("X"),
                    feather.getDouble("Y"),
                    feather.getDouble("Z")
            );
            sendFeatherVisual(player, position, 30);
            player.serverLevel().sendParticles(
                    ParticleTypes.END_ROD,
                    feather.getDouble("X"),
                    feather.getDouble("Y") + 0.85D,
                    feather.getDouble("Z"),
                    1, 0.08D, 0.2D, 0.08D, 0.005D
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        CompoundTag data = entity.getPersistentData();
        if (!isImmobilized(entity)) {
            if (data.contains(NBT_IMMOBILIZED_UNTIL)) {
                data.remove(NBT_IMMOBILIZED_UNTIL);
                data.remove(NBT_IMMOBILIZED_X);
                data.remove(NBT_IMMOBILIZED_Y);
                data.remove(NBT_IMMOBILIZED_Z);
            }
            return;
        }
        entity.teleportTo(
                data.getDouble(NBT_IMMOBILIZED_X),
                data.getDouble(NBT_IMMOBILIZED_Y),
                data.getDouble(NBT_IMMOBILIZED_Z)
        );
        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0F;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player
                && event.getEntity() != player
                && isInsideOwnedDomain(player, event.getEntity())) {
            event.getEntity().invulnerableTime = 0;
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && isImmobilized(attacker)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        tickFirstPowerBarrages(level);
        List<TimeStopDomain> domains = activeDomains(level);
        if (!domains.isEmpty()) {
            level.setDayTime(domains.get(0).frozenDayTime);
        }
    }

    private record TimeStopDomain(UUID caster, Vec3 center, long endTick, long frozenDayTime) {
    }

    private static final class FirstPowerBarrage {
        private final UUID caster;
        private final long endTick;
        private int remainingSwords;

        private FirstPowerBarrage(UUID caster, long endTick, int remainingSwords) {
            this.caster = caster;
            this.endTick = endTick;
            this.remainingSwords = remainingSwords;
        }
    }
}
