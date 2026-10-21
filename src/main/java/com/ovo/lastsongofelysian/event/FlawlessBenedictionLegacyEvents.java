package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.item.FlawlessBenedictionLegacyItem;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEffects;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.EgoSignetAmplification;
import com.ovo.lastsongofelysian.util.FlawlessWeaponEnergy;
import com.ovo.lastsongofelysian.util.FlawlessWeaponSkill;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;
import com.ovo.lastsongofelysian.network.BloomStackSyncPacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = lastsongofelysian.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class FlawlessBenedictionLegacyEvents {

    public static final int MAX_BLOOM_STACKS = 3;

    private static final int DOMAIN_DURATION_TICKS = 20 * 20;

    private static final int DOMAIN_ARROW_INTERVAL_TICKS = 4;

    private static final int DOMAIN_PETAL_HIT_INTERVAL_TICKS = 2;

    private static final int DOMAIN_CLOSE_DURATION_TICKS = 80;

    private static final double DOMAIN_RADIUS = 10.0D;

    private static final double DOMAIN_RADIUS_SQR =
            DOMAIN_RADIUS * DOMAIN_RADIUS;

    private static final float DOMAIN_DAMAGE_PER_SECOND =
            5000.0F;

    private static final float DOMAIN_PETAL_DAMAGE_VALUE =
            5000.0F;

    private static final double DOMAIN_CLOSE_DAMAGE_PER_TICK =
            200000.0D;

    private static final String NBT_DOMAIN_CLOSE_UNTIL =
            "FlawlessDomainCloseUntil";

    private static final String NBT_DOMAIN_CLOSE_REMAINING =
            "FlawlessDomainCloseRemaining";

    private static final String NBT_DOMAIN_CLOSE_X =
            "FlawlessDomainCloseX";

    private static final String NBT_DOMAIN_CLOSE_Y =
            "FlawlessDomainCloseY";

    private static final String NBT_DOMAIN_CLOSE_Z =
            "FlawlessDomainCloseZ";

    private static final String NBT_BLOOM_STACKS =
            "FlawlessBloomStacks";

    private static final String NBT_DOMAIN_UNTIL =
            "FlawlessDomainUntilPlayer";

    private static final String NBT_PERMANENT_FLIGHT =
            "FlawlessPermanentFlight";

    private static final String NBT_DOMAIN_ARROW =
            "FlawlessDomainArrow";

    private static final String NBT_DOMAIN_ARROW_EXPIRES =
            "FlawlessDomainArrowExpires";

    private static final String NBT_FLAWLESS_NO_HEAL_UNTIL =
            "FlawlessNoHealUntil";

    private static final String NBT_DOMAIN_PETAL_DAMAGE =
            "FlawlessDomainPetalDamage";

    private static final String NBT_ARROW_IMPACT_VISUAL =
            "FlawlessArrowImpactVisual";

    private static final int DOMAIN_ARROW_LIFETIME_TICKS =
            60;

    private static final UUID DELIVERANCE_ULTIMATE_BUFF_UUID =
            FlawlessWeaponSkill.DELIVERANCE_ULTIMATE_BUFF_UUID;

    private static final UUID FLAWLESS_HEALTH_BOOST_UUID =
            UUID.fromString(
                    "88fd3589-5772-46e4-95ec-8e0e41fc4e38"
            );

    private static final UUID WATER_SPEED_UUID =
            UUID.fromString(
                    "a3ad1901-e59c-4056-88bd-6eafbbf15603"
            );

    private static final UUID LAVA_SPEED_UUID =
            UUID.fromString(
                    "9d985d87-5eef-4a06-9845-33cf7470d213"
            );

    private static final Random RAND = new Random();

    private static Method damageDisplayDummyDamageMethod;

    private static final DustParticleOptions DOMAIN_DEEP_PINK =
            new DustParticleOptions(
                    new Vector3f(
                            0.78F,
                            0.02F,
                            0.30F
                    ),
                    1.35F
            );

    private static final DustParticleOptions DOMAIN_LIGHT_PINK =
            new DustParticleOptions(
                    new Vector3f(
                            1.00F,
                            0.48F,
                            0.72F
                    ),
                    1.05F
            );

    private static final int BLOOM_EFFECT_DURATION =
            20 * 60 * 60;

    private static final Block[] FLOWERS = {
            Blocks.DANDELION,
            Blocks.POPPY,
            Blocks.BLUE_ORCHID,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER,
            Blocks.LILY_OF_THE_VALLEY
    };

    private FlawlessBenedictionLegacyEvents() {
    }

    public static void activateDomain(
            Player player,
            ItemStack weapon
    ) {
        if (player.level().isClientSide()) {
            return;
        }

        clearDomainClosingData(player);

        long until =
                player.level().getGameTime()
                        + DOMAIN_DURATION_TICKS;

        player.getPersistentData().putLong(
                NBT_DOMAIN_UNTIL,
                until
        );

        weapon.getOrCreateTag().putLong(
                FlawlessBenedictionLegacyItem
                        .NBT_DOMAIN_UNTIL,
                until
        );

        player.displayClientMessage(
                ModText.literal(
                        "必杀技——无暇乐土"
                ).withStyle(
                        net.minecraft.ChatFormatting.LIGHT_PURPLE,
                        net.minecraft.ChatFormatting.BOLD
                ),
                true
        );

        if (
                player instanceof
                        ServerPlayer
                                serverPlayer
        ) {
            serverPlayer.getInventory().setChanged();
            serverPlayer.containerMenu.broadcastChanges();
        }
    }

    public static boolean isDomainActive(Player player) {
        return player.getPersistentData()
                .getLong(NBT_DOMAIN_UNTIL)
                > player.level().getGameTime();
    }

    public static boolean isDomainClosing(Player player) {
        return player.getPersistentData()
                .getLong(NBT_DOMAIN_CLOSE_UNTIL)
                > player.level().getGameTime();
    }

    public static boolean closeDomainEarly(
            Player player,
            ItemStack weapon
    ) {
        if (
                player.level().isClientSide() ||
                !isDomainActive(player) ||
                isDomainClosing(player)
        ) {
            return false;
        }

        long now = player.level().getGameTime();
        long domainUntil =
                player.getPersistentData()
                        .getLong(NBT_DOMAIN_UNTIL);

        int remainingTicks =
                (int) Math.max(
                        1L,
                        domainUntil - now
                );

        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook =
                new Vec3(
                        look.x,
                        0.0D,
                        look.z
                );

        if (horizontalLook.lengthSqr() < 0.000001D) {
            horizontalLook =
                    new Vec3(
                            0.0D,
                            0.0D,
                            1.0D
                    );
        } else {
            horizontalLook = horizontalLook.normalize();
        }

        Vec3 domainCenter = player.position();
        Vec3 visualCenter =
                domainCenter
                        .subtract(
                                horizontalLook.scale(2.75D)
                        )
                        .add(
                                0.0D,
                                0.25D,
                                0.0D
                        );

        player.getPersistentData().remove(
                NBT_DOMAIN_UNTIL
        );

        player.getPersistentData().putLong(
                NBT_DOMAIN_CLOSE_UNTIL,
                now + DOMAIN_CLOSE_DURATION_TICKS
        );

        player.getPersistentData().putInt(
                NBT_DOMAIN_CLOSE_REMAINING,
                remainingTicks
        );

        player.getPersistentData().putDouble(
                NBT_DOMAIN_CLOSE_X,
                domainCenter.x
        );

        player.getPersistentData().putDouble(
                NBT_DOMAIN_CLOSE_Y,
                domainCenter.y
        );

        player.getPersistentData().putDouble(
                NBT_DOMAIN_CLOSE_Z,
                domainCenter.z
        );

        if (weapon.hasTag()) {
            weapon.getTag().remove(
                    FlawlessBenedictionLegacyItem
                            .NBT_DOMAIN_UNTIL
            );
        }

        AttributeInstance attackDamage =
                player.getAttribute(
                        Attributes.ATTACK_DAMAGE
                );

        if (attackDamage != null) {
            attackDamage.removeModifier(
                    DELIVERANCE_ULTIMATE_BUFF_UUID
            );
        }

        if (player.level() instanceof ServerLevel level) {
            applyDomainClosingDamage(
                    level,
                    player,
                    domainCenter,
                    remainingTicks
            );
        }

        FlawlessVisualEffects.spawnUltimateClosing(
                player,
                visualCenter,
                remainingTicks
        );

        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(
                    DOMAIN_LIGHT_PINK,
                    visualCenter.x,
                    visualCenter.y + 0.20D,
                    visualCenter.z,
                    64,
                    4.5D,
                    0.25D,
                    4.5D,
                    0.035D
            );
        }

        return true;
    }

    public static int getBloomStacks(
            LivingEntity entity
    ) {
        MobEffectInstance visibleEffect =
                entity.getEffect(
                        ModEffects.FLAWLESS_BLOOM.get()
                );

        if (visibleEffect != null) {
            return Math.max(
                    0,
                    Math.min(
                            MAX_BLOOM_STACKS,
                            visibleEffect.getAmplifier() + 1
                    )
            );
        }

        return Math.max(
                0,
                Math.min(
                        MAX_BLOOM_STACKS,
                        entity.getPersistentData()
                                .getInt(NBT_BLOOM_STACKS)
                )
        );
    }

    public static int addBloomStack(
            LivingEntity entity
    ) {
        return addBloomStack(entity, true);
    }

    private static int addBloomStack(
            LivingEntity entity,
            boolean showVisuals
    ) {
        if (PlayerControlledAllies.isElysianHero(entity)) {
            return 0;
        }
        int next = Math.min(
                MAX_BLOOM_STACKS,
                getBloomStacks(entity) + 1
        );

        entity.getPersistentData().putInt(
                NBT_BLOOM_STACKS,
                next
        );

        MobEffectInstance currentBloom =
                entity.getEffect(
                        ModEffects.FLAWLESS_BLOOM.get()
                );

        if (
                currentBloom != null &&
                currentBloom.getAmplifier() != next - 1
        ) {
            entity.removeEffect(
                    ModEffects.FLAWLESS_BLOOM.get()
            );
        }

        entity.addEffect(
                new MobEffectInstance(
                        ModEffects.FLAWLESS_BLOOM.get(),
                        BLOOM_EFFECT_DURATION,
                        next - 1,
                        false,
                        true,
                        true
                )
        );

        if (showVisuals) {
            spawnBloomStackParticles(
                    entity,
                    next
            );

            FlawlessVisualEffects.spawnBloomStackSigil(
                    entity,
                    next
            );
        }
        syncBloomStacks(
                entity,
                next
        );

        return next;
    }

    public static void clearBloomStacks(
            LivingEntity entity
    ) {
        entity.getPersistentData().remove(
                NBT_BLOOM_STACKS
        );

        entity.removeEffect(
                ModEffects.FLAWLESS_BLOOM.get()
        );

        syncBloomStacks(
                entity,
                0
        );
    }

    private static void syncBloomStacks(
            LivingEntity entity,
            int stacks
    ) {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }

        int safeStacks =
                Math.max(
                        0,
                        Math.min(
                                MAX_BLOOM_STACKS,
                                stacks
                        )
                );

        ModNetwork.CHANNEL.send(
                PacketDistributor
                        .TRACKING_ENTITY_AND_SELF
                        .with(
                                () -> entity
                        ),
                new BloomStackSyncPacket(
                        entity.getUUID(),
                        safeStacks
                )
        );
    }

    @SubscribeEvent
    public static void onStartTracking(
            PlayerEvent.StartTracking event
    ) {
        if (
                !(event.getEntity()
                        instanceof ServerPlayer player) ||
                        !(event.getTarget()
                                instanceof LivingEntity target)
        ) {
            return;
        }

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new BloomStackSyncPacket(
                        target.getUUID(),
                        getBloomStacks(target)
                )
        );
    }

    @SubscribeEvent(
            priority = EventPriority.LOWEST
    )
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (
                event.phase != TickEvent.Phase.END ||
                event.side != LogicalSide.SERVER
        ) {
            return;
        }

        Player player = event.player;
        ItemStack mainHand =
                player.getMainHandItem();

        boolean holding =
                mainHand.is(
                        ModItems.FLAWLESS_BENEDICTION_LEGACY.get()
                );

        boolean carrying = hasWeapon(player);

        if (holding) {
            applyMainHandBlessings(player);
        } else {
            removeMainHandBlessings(player);
        }

        if (isAggroProtected(player)) {
            preventHostileTargeting(player);
        }

        if (carrying) {
            preventNearbyPassiveEscape(player);

            if (player.tickCount % 100 == 0) {
                growFlowers(player);
            }

            if (player.tickCount % 20 == 0) {
                accelerateCrops(player);
            }
        }

        handleSurfaceCharge(player, mainHand);

        if (player.tickCount % 5 == 0) {
            cleanupDomainArrows(player);
        }

        tickDomainClosure(player);

        if (isDomainActive(player)) {
            tickDomain(player, mainHand);
        } else {

            AttributeInstance attackDamage =
                    player.getAttribute(
                            Attributes.ATTACK_DAMAGE
                    );

            if (attackDamage != null) {
                attackDamage.removeModifier(
                        DELIVERANCE_ULTIMATE_BUFF_UUID
                );
            }

            if (
                    mainHand.is(
                            ModItems.FLAWLESS_BENEDICTION_LEGACY.get()
                    ) &&
                    mainHand.hasTag()
            ) {
                mainHand.getTag().remove(
                        FlawlessBenedictionLegacyItem
                                .NBT_DOMAIN_UNTIL
                );
            }
        }
    }

    private static void tickDomainClosure(
            Player player
    ) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        long closeUntil =
                player.getPersistentData()
                        .getLong(NBT_DOMAIN_CLOSE_UNTIL);

        if (closeUntil <= 0L) {
            return;
        }

        long now = level.getGameTime();

        if (now < closeUntil) {
            return;
        }

        Vec3 center =
                new Vec3(
                        player.getPersistentData()
                                .getDouble(NBT_DOMAIN_CLOSE_X),
                        player.getPersistentData()
                                .getDouble(NBT_DOMAIN_CLOSE_Y),
                        player.getPersistentData()
                                .getDouble(NBT_DOMAIN_CLOSE_Z)
                );

        level.sendParticles(
                ParticleTypes.FIREWORK,
                center.x,
                center.y + 1.0D,
                center.z,
                180,
                DOMAIN_RADIUS,
                DOMAIN_RADIUS * 0.75D,
                DOMAIN_RADIUS,
                0.20D
        );

        level.sendParticles(
                DOMAIN_DEEP_PINK,
                center.x,
                center.y + 1.0D,
                center.z,
                240,
                DOMAIN_RADIUS,
                DOMAIN_RADIUS * 0.65D,
                DOMAIN_RADIUS,
                0.12D
        );

        level.sendParticles(
                ParticleTypes.CHERRY_LEAVES,
                center.x,
                center.y + DOMAIN_RADIUS,
                center.z,
                160,
                DOMAIN_RADIUS,
                DOMAIN_RADIUS,
                DOMAIN_RADIUS,
                0.18D
        );

        level.playSound(
                null,
                player.blockPosition(),
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                4.0F,
                1.35F
        );

        clearDomainClosingData(player);
    }

    private static void applyDomainClosingDamage(
            ServerLevel level,
            Player player,
            Vec3 center,
            int remainingTicks
    ) {
        float damage =
                (float) Math.min(
                        Float.MAX_VALUE,
                        DOMAIN_CLOSE_DAMAGE_PER_TICK
                                * remainingTicks
                );

        AABB area =
                new AABB(center, center)
                        .inflate(DOMAIN_RADIUS);

        List<LivingEntity> enemies =
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        area,
                        entity ->
                                isDomainEnemy(
                                        player,
                                        entity
                                ) && entity.position()
                                        .distanceToSqr(center)
                                        <= DOMAIN_RADIUS_SQR
                );

        DamageSource source =
                player.damageSources()
                        .playerAttack(player);

        for (LivingEntity enemy : enemies) {
            markNoHeal(enemy);
            applyFixedDomainDamage(
                    enemy,
                    source,
                    damage
            );

            if (enemy instanceof Mob mob) {
                clearHostileIntent(
                        mob,
                        player
                );
            }
        }
    }

    private static void clearDomainClosingData(
            Player player
    ) {
        player.getPersistentData().remove(
                NBT_DOMAIN_CLOSE_UNTIL
        );
        player.getPersistentData().remove(
                NBT_DOMAIN_CLOSE_REMAINING
        );
        player.getPersistentData().remove(
                NBT_DOMAIN_CLOSE_X
        );
        player.getPersistentData().remove(
                NBT_DOMAIN_CLOSE_Y
        );
        player.getPersistentData().remove(
                NBT_DOMAIN_CLOSE_Z
        );
    }

    private static void applyMainHandBlessings(
            Player player
    ) {

        if (player.tickCount % 20 == 0) {
            ensureEffect(
                    player,
                    MobEffects.MOVEMENT_SPEED,
                    4
            );
            ensureEffect(
                    player,
                    MobEffects.DAMAGE_BOOST,
                    4
            );
            ensureEffect(
                    player,
                    MobEffects.DAMAGE_RESISTANCE,
                    2
            );
            ensureEffect(
                    player,
                    MobEffects.FIRE_RESISTANCE,
                    4
            );
            ensureEffect(
                    player,
                    MobEffects.REGENERATION,
                    4
            );
            ensureEffect(
                    player,
                    MobEffects.JUMP,
                    2
            );
            ensureEffect(
                    player,
                    MobEffects.HERO_OF_THE_VILLAGE,
                    4
            );
            ensureEffect(
                    player,
                    MobEffects.LUCK,
                    4
            );

            player.addEffect(
                    new MobEffectInstance(
                            MobEffects.SATURATION,
                            1,
                            4,
                            false,
                            false,
                            true
                    )
            );
        }

        AttributeInstance maxHealth =
                player.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (
                maxHealth != null &&
                maxHealth.getModifier(
                        FLAWLESS_HEALTH_BOOST_UUID
                ) == null
        ) {
            maxHealth.addTransientModifier(
                    new AttributeModifier(
                            FLAWLESS_HEALTH_BOOST_UUID,
                            "flawless_health_boost_v",
                            20.0D,
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }

        if (EgoSignetAmplification.isEnabledFor(
                player, ModItems.SIGNET_OF_GOLD.get())) {
            player.getPersistentData()
                    .putBoolean(
                            NBT_PERMANENT_FLIGHT,
                            true
                    );
        }

        if (
                player.getPersistentData()
                        .getBoolean(
                                NBT_PERMANENT_FLIGHT
                        ) &&
                !player.getAbilities().mayfly
        ) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    private static void removeMainHandBlessings(
            Player player
    ) {
        AttributeInstance maxHealth =
                player.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (maxHealth != null) {
            maxHealth.removeModifier(
                    FLAWLESS_HEALTH_BOOST_UUID
            );
        }

        player.setHealth(
                Math.min(
                        player.getHealth(),
                        player.getMaxHealth()
                )
        );
    }

    private static void ensureEffect(
            Player player,
            net.minecraft.world.effect.MobEffect effect,
            int amplifier
    ) {
        MobEffectInstance current =
                player.getEffect(effect);

        if (
                current != null &&
                current.getAmplifier() == amplifier &&
                current.getDuration() > 60
        ) {
            return;
        }

        player.addEffect(
                new MobEffectInstance(
                        effect,
                        200,
                        amplifier,
                        false,
                        false,
                        true
                )
        );
    }

    private static void handleSurfaceCharge(
            Player player,
            ItemStack mainHand
    ) {
        AttributeInstance speed =
                player.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (speed == null) {
            return;
        }

        boolean chargingThisWeapon =
                mainHand.is(
                        ModItems.FLAWLESS_BENEDICTION_LEGACY.get()
                )
                        && player.isUsingItem()
                        && player.getUseItem() == mainHand;

        if (!chargingThisWeapon) {
            speed.removeModifier(WATER_SPEED_UUID);
            speed.removeModifier(LAVA_SPEED_UUID);
            return;
        }

        BlockPos fluidPos =
                findFluidSurfaceBelow(player);

        if (fluidPos == null) {
            speed.removeModifier(WATER_SPEED_UUID);
            speed.removeModifier(LAVA_SPEED_UUID);
            return;
        }

        FluidState fluidState =
                player.level().getFluidState(fluidPos);

        if (fluidState.is(FluidTags.WATER)) {
            speed.removeModifier(LAVA_SPEED_UUID);

            ensureSpeedModifier(
                    speed,
                    WATER_SPEED_UUID,
                    "flawless_water_charge_speed",
                    5.20D
            );
        } else if (fluidState.is(FluidTags.LAVA)) {
            speed.removeModifier(WATER_SPEED_UUID);

            ensureSpeedModifier(
                    speed,
                    LAVA_SPEED_UUID,
                    "flawless_lava_charge_speed",
                    1.314D
            );
        } else {
            speed.removeModifier(WATER_SPEED_UUID);
            speed.removeModifier(LAVA_SPEED_UUID);
            return;
        }

        holdOnFluidSurface(
                player,
                fluidPos,
                fluidState
        );
    }

    private static void ensureSpeedModifier(
            AttributeInstance speed,
            UUID uuid,
            String name,
            double amount
    ) {

        if (speed.getModifier(uuid) != null) {
            return;
        }

        speed.addTransientModifier(
                new AttributeModifier(
                        uuid,
                        name,
                        amount,
                        AttributeModifier.Operation
                                .MULTIPLY_TOTAL
                )
        );
    }

    private static BlockPos findFluidSurfaceBelow(
            Player player
    ) {
        BlockPos origin = BlockPos.containing(
                player.getX(),
                player.getY(),
                player.getZ()
        );

        for (int depth = 0; depth <= 2; depth++) {
            BlockPos candidate =
                    origin.below(depth);

            FluidState fluidState =
                    player.level()
                            .getFluidState(candidate);

            if (
                    !fluidState.is(FluidTags.WATER) &&
                    !fluidState.is(FluidTags.LAVA)
            ) {
                continue;
            }

            FluidState aboveFluid =
                    player.level()
                            .getFluidState(
                                    candidate.above()
                            );

            boolean fluidContinuesAbove =
                    (
                            fluidState.is(
                                    FluidTags.WATER
                            ) &&
                            aboveFluid.is(
                                    FluidTags.WATER
                            )
                    ) ||
                    (
                            fluidState.is(
                                    FluidTags.LAVA
                            ) &&
                            aboveFluid.is(
                                    FluidTags.LAVA
                            )
                    );

            if (fluidContinuesAbove) {
                continue;
            }

            double surfaceY =
                    candidate.getY()
                            + fluidState.getHeight(
                                    player.level(),
                                    candidate
                            );

            double distanceFromSurface =
                    player.getY() - surfaceY;

            if (
                    distanceFromSurface >= -0.15D &&
                    distanceFromSurface <= 0.65D
            ) {
                return candidate.immutable();
            }
        }

        return null;
    }

    private static void holdOnFluidSurface(
            Player player,
            BlockPos fluidPos,
            FluidState fluidState
    ) {
        Vec3 motion = player.getDeltaMovement();

        if (motion.y > 0.08D) {
            player.fallDistance = 0.0F;
            return;
        }

        double surfaceY =
                fluidPos.getY()
                        + fluidState.getHeight(
                                player.level(),
                                fluidPos
                        );

        double standingY = surfaceY + 0.02D;

        if (
                Math.abs(
                        player.getY() - standingY
                ) > 0.001D
        ) {
            player.setPos(
                    player.getX(),
                    standingY,
                    player.getZ()
            );
        }

        player.setDeltaMovement(
                motion.x,
                0.0D,
                motion.z
        );

        player.fallDistance = 0.0F;
        player.setOnGround(true);
    }

    private static void growFlowers(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos origin = player.blockPosition();

        for (int attempt = 0; attempt < 12; attempt++) {
            BlockPos pos = origin.offset(
                    RAND.nextInt(13) - 6,
                    RAND.nextInt(3) - 1,
                    RAND.nextInt(13) - 6
            );

            if (
                    level.getBlockState(pos.below())
                            .is(Blocks.GRASS_BLOCK) &&
                    level.getBlockState(pos).isAir()
            ) {
                Block flower =
                        FLOWERS[
                                RAND.nextInt(
                                        FLOWERS.length
                                )
                                ];

                level.setBlockAndUpdate(
                        pos,
                        flower.defaultBlockState()
                );

                break;
            }
        }
    }

    private static void accelerateCrops(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos origin = player.blockPosition();

        for (int attempt = 0; attempt < 13; attempt++) {
            BlockPos pos = origin.offset(
                    RAND.nextInt(17) - 8,
                    RAND.nextInt(7) - 3,
                    RAND.nextInt(17) - 8
            );

            BlockState state =
                    level.getBlockState(pos);

            Block block = state.getBlock();

            if (
                    block instanceof CropBlock ||
                    block instanceof StemBlock ||
                    block instanceof CocoaBlock ||
                    block instanceof SweetBerryBushBlock ||
                    block instanceof NetherWartBlock
            ) {
                state.randomTick(
                        level,
                        pos,
                        level.getRandom()
                );
            }
        }
    }

    private static void preventHostileTargeting(
            Player player
    ) {

        List<Mob> mobs =
                player.level().getEntitiesOfClass(
                        Mob.class,
                        player.getBoundingBox()
                                .inflate(64.0D),
                        Mob::isAlive
                );

        for (Mob mob : mobs) {

            if (mob instanceof Enemy) {
                MobEffectInstance regeneration =
                        mob.getEffect(
                                MobEffects.REGENERATION
                        );

                if (
                        regeneration != null &&
                        regeneration.getAmplifier() == 4 &&
                        regeneration.getDuration() <= 220
                ) {
                    mob.removeEffect(
                            MobEffects.REGENERATION
                    );
                }
            }

            if (mob.getTarget() != player) {
                continue;
            }

            clearHostileIntent(
                    mob,
                    player
            );
        }
    }

    private static void clearHostileIntent(
            Mob mob,
            Player player
    ) {
        if (mob.getTarget() == player) {
            mob.setTarget(null);
        }

        if (mob instanceof NeutralMob neutralMob) {
            neutralMob.stopBeingAngry();
        }

        mob.setAggressive(false);
        mob.getNavigation().stop();
    }

    private static void preventNearbyPassiveEscape(
            Player player
    ) {
        List<PathfinderMob> mobs =
                player.level().getEntitiesOfClass(
                        PathfinderMob.class,
                        player.getBoundingBox()
                                .inflate(8.0D),
                        mob ->
                                !(mob instanceof Enemy)
                                        && mob.isAlive()
                );

        for (PathfinderMob mob : mobs) {
            if (
                    mob.getNavigation().isInProgress() &&
                    mob.distanceToSqr(player) < 64.0D
            ) {
                mob.getNavigation().stop();
            }
        }
    }

    private static void tickDomain(
            Player player,
            ItemStack weapon
    ) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (
                weapon.is(
                        ModItems.FLAWLESS_BENEDICTION_LEGACY.get()
                )
        ) {
            weapon.getOrCreateTag().putLong(
                    FlawlessBenedictionLegacyItem
                            .NBT_DOMAIN_UNTIL,
                    player.getPersistentData()
                            .getLong(NBT_DOMAIN_UNTIL)
            );
        }

        AABB domain =
                player.getBoundingBox()
                        .inflate(DOMAIN_RADIUS);

        List<LivingEntity> enemies =
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        domain,
                        entity ->
                                isDomainEnemy(
                                        player,
                                        entity
                                ) && entity.distanceToSqr(player)
                                        <= DOMAIN_RADIUS_SQR
                );
        if (player.tickCount % 5 == 0) {
            spawnDomainParticles(level, player);
        }

        if (player.tickCount % 20 == 0) {
            for (LivingEntity enemy : enemies) {
                markNoHeal(enemy);

                applyFixedDomainDamage(
                        enemy,
                        level.damageSources().magic(),
                        DOMAIN_DAMAGE_PER_SECOND
                );

                if (enemy instanceof Mob mob) {
                    clearHostileIntent(
                            mob,
                            player
                    );
                }
            }
        }

        if (
                player.tickCount
                        % DOMAIN_ARROW_INTERVAL_TICKS
                        == 0
        ) {
            fireOneDomainArrow(
                    level,
                    player,
                    enemies
            );
        }

        if (
                player.tickCount
                        % DOMAIN_PETAL_HIT_INTERVAL_TICKS
                        == 0
        ) {
            strikeOneDomainPetal(
                    player,
                    enemies
            );
        }

        if (player.tickCount % 60 == 0) {
            for (LivingEntity enemy : enemies) {
                enemy.setTicksFrozen(
                        Math.max(
                                enemy.getTicksFrozen(),
                                300
                        )
                );

                enemy.addEffect(
                        new MobEffectInstance(
                                MobEffects.MOVEMENT_SLOWDOWN,
                                20,
                                255,
                                false,
                                false,
                                true
                        )
                );

                addBloomStack(enemy, false);
            }
        }
    }

    private static boolean isDomainEnemy(
            Player owner,
            LivingEntity entity
    ) {
        if (!PlayerControlledAllies.isValidEnemy(owner, entity)) {
            return false;
        }

        if (entity instanceof Player otherPlayer) {
            return !otherPlayer.isCreative() &&
                    !otherPlayer.isSpectator() &&
                    owner.canHarmPlayer(otherPlayer);
        }

        if (
                entity instanceof TamableAnimal tamable &&
                tamable.isOwnedBy(owner)
        ) {
            return false;
        }

        return true;
    }

    private static void spawnDomainParticles(
            ServerLevel level,
            Player player
    ) {
        level.sendParticles(
                ParticleTypes.CHERRY_LEAVES,
                player.getX(),
                player.getY() + 6.0D,
                player.getZ(),
                12,
                DOMAIN_RADIUS - 0.15D,
                1.2D,
                DOMAIN_RADIUS - 0.15D,
                0.045D
        );

        level.sendParticles(
                DOMAIN_DEEP_PINK,
                player.getX(),
                player.getY() + 2.0D,
                player.getZ(),
                14,
                DOMAIN_RADIUS - 0.15D,
                2.0D,
                DOMAIN_RADIUS - 0.15D,
                0.015D
        );

        level.sendParticles(
                DOMAIN_LIGHT_PINK,
                player.getX(),
                player.getY() + 2.0D,
                player.getZ(),
                14,
                DOMAIN_RADIUS - 0.15D,
                2.0D,
                DOMAIN_RADIUS - 0.15D,
                0.010D
        );

        level.sendParticles(
                DOMAIN_DEEP_PINK,
                player.getX(),
                player.getY() + 0.12D,
                player.getZ(),
                10,
                DOMAIN_RADIUS - 0.05D,
                0.08D,
                DOMAIN_RADIUS - 0.05D,
                0.004D
        );

        level.sendParticles(
                DOMAIN_LIGHT_PINK,
                player.getX(),
                player.getY() + 0.20D,
                player.getZ(),
                10,
                DOMAIN_RADIUS - 0.05D,
                0.12D,
                DOMAIN_RADIUS - 0.05D,
                0.004D
        );
    }

    private static void spawnBloomStackParticles(
            LivingEntity target,
            int stacks
    ) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        int count = 12 + stacks * 8;

        level.sendParticles(
                DOMAIN_DEEP_PINK,
                target.getX(),
                target.getY()
                        + target.getBbHeight() * 0.55D,
                target.getZ(),
                count,
                target.getBbWidth() * 0.65D,
                target.getBbHeight() * 0.45D,
                target.getBbWidth() * 0.65D,
                0.025D
        );

        level.sendParticles(
                DOMAIN_LIGHT_PINK,
                target.getX(),
                target.getY()
                        + target.getBbHeight() * 0.65D,
                target.getZ(),
                count,
                target.getBbWidth() * 0.55D,
                target.getBbHeight() * 0.40D,
                target.getBbWidth() * 0.55D,
                0.018D
        );
    }

    private static void spawnWeaponHitParticles(
            LivingEntity target,
            int stacks
    ) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        int count = 18 + Math.max(0, stacks) * 6;

        level.sendParticles(
                DOMAIN_DEEP_PINK,
                target.getX(),
                target.getY()
                        + target.getBbHeight() * 0.55D,
                target.getZ(),
                count,
                target.getBbWidth() * 0.55D,
                target.getBbHeight() * 0.40D,
                target.getBbWidth() * 0.55D,
                0.055D
        );

        level.sendParticles(
                DOMAIN_LIGHT_PINK,
                target.getX(),
                target.getY()
                        + target.getBbHeight() * 0.60D,
                target.getZ(),
                count,
                target.getBbWidth() * 0.50D,
                target.getBbHeight() * 0.35D,
                target.getBbWidth() * 0.50D,
                0.040D
        );
    }

    private static void strikeOneDomainPetal(
            Player owner,
            List<LivingEntity> enemies
    ) {
        if (enemies.isEmpty()) {
            return;
        }

        LivingEntity target =
                enemies.get(
                        RAND.nextInt(enemies.size())
                );

        markNoHeal(target);

        boolean damaged =
                applyFixedDomainDamage(
                        target,
                        owner.damageSources()
                                .playerAttack(owner),
                        DOMAIN_PETAL_DAMAGE_VALUE
                );

        if (!damaged) {
            return;
        }

        if (target.isAlive()) {
            addBloomStack(target, false);
        }

        if (target instanceof Mob mob) {
            clearHostileIntent(
                    mob,
                    owner
            );
        }
    }

    private static boolean applyFixedDomainDamage(
            LivingEntity target,
            DamageSource source,
            float amount
    ) {
        if (source.getEntity() instanceof Player player
                && PlayerControlledAllies.isProtectedEffectTarget(player, target)) return false;
        if (source.getEntity() instanceof Player player && source.getDirectEntity() == player) {
            amount *= SignetEffects.getDisciplineUltimateDamageMultiplier(player);
        }
        if (
                !target.isAlive() ||
                amount <= 0.0F
        ) {
            return false;
        }

        if (isDamageDisplayDummy(target)) {
            target.invulnerableTime = 0;
            target.hurtTime = 0;
            target.hurtDuration = 0;

            boolean wasMarked = target.getPersistentData()
                    .getBoolean(NBT_DOMAIN_PETAL_DAMAGE);
            target.getPersistentData().putBoolean(
                    NBT_DOMAIN_PETAL_DAMAGE,
                    true
            );

            try {
                target.hurt(source, 0.0F);
                Method method = damageDisplayDummyDamageMethod;
                if (method == null || method.getDeclaringClass() != target.getClass()) {
                    method = target.getClass().getDeclaredMethod(
                            "onActuallyDamagedOrTrueDamageDetected",
                            float.class,
                            DamageSource.class
                    );
                    method.setAccessible(true);
                    damageDisplayDummyDamageMethod = method;
                }
                method.invoke(target, amount, source);
                return true;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                damageDisplayDummyDamageMethod = null;
                return target.hurt(source, amount);
            } finally {
                if (!wasMarked) {
                    target.getPersistentData().remove(
                            NBT_DOMAIN_PETAL_DAMAGE
                    );
                }
            }
        }

        float healthBefore =
                target.getHealth();

        float requiredHealth =
                Math.max(
                        0.0F,
                        healthBefore - amount
                );

        target.invulnerableTime = 0;
        target.hurtTime = 0;
        target.hurtDuration = 0;
        target.setHealth(requiredHealth);

        if (
                requiredHealth <= 0.0F &&
                !target.isDeadOrDying()
        ) {
            target.die(source);
        }

        return target.getHealth() < healthBefore ||
                requiredHealth <= 0.0F;
    }

    private static boolean isDamageDisplayDummy(
            LivingEntity target
    ) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES
                .getKey(target.getType());
        return id != null &&
                id.getNamespace().equals("dummmmmmy") &&
                id.getPath().equals("target_dummy");
    }

    private static void fireOneDomainArrow(
            ServerLevel level,
            Player owner,
            List<LivingEntity> enemies
    ) {
        if (enemies.isEmpty()) {
            return;
        }

        LivingEntity target =
                enemies.get(
                        RAND.nextInt(enemies.size())
                );

        double angle =
                RAND.nextDouble()
                        * Math.PI * 2.0D;

        double startX =
                owner.getX()
                        + Math.cos(angle) * 6.0D;

        double startY =
                owner.getY()
                        + 1.5D
                        + RAND.nextDouble() * 3.0D;

        double startZ =
                owner.getZ()
                        + Math.sin(angle) * 6.0D;

        Arrow arrow = new Arrow(level, owner);

        arrow.setPos(
                startX,
                startY,
                startZ
        );

        arrow.setBaseDamage(8.0D);
        arrow.setNoGravity(true);
        arrow.pickup =
                AbstractArrow.Pickup.DISALLOWED;

        arrow.getPersistentData()
                .putBoolean(
                        NBT_DOMAIN_ARROW,
                        true
                );

        arrow.getPersistentData()
                .putLong(
                        NBT_DOMAIN_ARROW_EXPIRES,
                        level.getGameTime()
                                + DOMAIN_ARROW_LIFETIME_TICKS
                );

        Vec3 direction =
                target.getEyePosition()
                        .subtract(
                                startX,
                                startY,
                                startZ
                        );

        arrow.shoot(
                direction.x,
                direction.y,
                direction.z,
                2.5F,
                0.5F
        );

        level.addFreshEntity(arrow);
    }

    private static boolean isAggroProtected(
            Player player
    ) {
        return isProtectedByMainHand(player)
                || isDomainActive(player);
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST,
            receiveCanceled = true
    )
    public static void onLivingChangeTarget(
            LivingChangeTargetEvent event
    ) {
        if (
                event.getEntity()
                        .level()
                        .isClientSide() ||
                !(event.getNewTarget()
                        instanceof Player player) ||
                !isAggroProtected(player)
        ) {
            return;
        }

        event.setCanceled(true);

        if (event.getEntity() instanceof Mob mob) {
            clearHostileIntent(
                    mob,
                    player
            );
        }
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST,
            receiveCanceled = true
    )
    public static void onLivingAttack(
            LivingAttackEvent event
    ) {

        if (
                event.getEntity() instanceof Player &&
                event.getSource().getDirectEntity()
                        instanceof AbstractArrow arrow &&
                arrow.getPersistentData()
                        .getBoolean(NBT_DOMAIN_ARROW)
        ) {
            event.setCanceled(true);
            arrow.discard();
            return;
        }

        if (
                !(event.getEntity()
                        instanceof Player player) ||
                !isProtectedByMainHand(player) ||
                !isHostileSource(event.getSource())
        ) {
            return;
        }

        event.setCanceled(true);

        Mob hostileMob =
                getHostileMob(
                        event.getSource()
                );

        if (hostileMob != null) {
            clearHostileIntent(
                    hostileMob,
                    player
            );
        }
    }

    private static boolean isProtectedByMainHand(
            Player player
    ) {
        return player.getMainHandItem().is(
                ModItems.FLAWLESS_BENEDICTION_LEGACY.get()
        );
    }

    private static boolean isHostileSource(
            DamageSource source
    ) {
        return getHostileMob(source) != null;
    }

    private static Mob getHostileMob(
            DamageSource source
    ) {
        Entity sourceEntity = source.getEntity();

        if (
                sourceEntity instanceof Mob mob &&
                mob instanceof Enemy
        ) {
            return mob;
        }

        Entity directEntity =
                source.getDirectEntity();

        if (
                directEntity instanceof Mob mob &&
                mob instanceof Enemy
        ) {
            return mob;
        }

        if (
                directEntity instanceof Projectile projectile &&
                projectile.getOwner() instanceof Mob owner &&
                owner instanceof Enemy
        ) {
            return owner;
        }

        return null;
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST,
            receiveCanceled = true
    )
    public static void onLivingHurt(
            LivingHurtEvent event
    ) {
        LivingEntity target = event.getEntity();

        if (
                target instanceof Player player &&
                isProtectedByMainHand(player) &&
                isHostileSource(event.getSource())
        ) {
            event.setCanceled(true);

            Mob hostileMob =
                    getHostileMob(
                            event.getSource()
                    );

            if (hostileMob != null) {
                clearHostileIntent(
                        hostileMob,
                        player
                );
            }

            return;
        }

        if (
                event.getSource().getEntity()
                        instanceof LivingEntity attacker &&
                attacker instanceof Enemy &&
                isInsideAnyDomain(attacker)
        ) {
            event.setAmount(
                    event.getAmount() * 0.48F
            );
        }

        int stacks = getBloomStacks(target);

        boolean domainPetalDamage =
                target.getPersistentData()
                        .getBoolean(
                                NBT_DOMAIN_PETAL_DAMAGE
                        );

        if (
                stacks > 0 &&
                !domainPetalDamage
        ) {
            float vulnerability =
                    17.333F * stacks;

            event.setAmount(
                    event.getAmount()
                            * (1.0F + vulnerability)
            );
        }

        if (
                event.getSource().getDirectEntity()
                        instanceof AbstractArrow arrow
        ) {
            boolean normalFlawlessArrow =
                    arrow.getPersistentData()
                            .getBoolean(
                                    FlawlessBenedictionLegacyItem
                                            .NBT_ARROW_MARKER
                            );

            boolean domainArrow =
                    arrow.getPersistentData()
                            .getBoolean(
                                    NBT_DOMAIN_ARROW
                            );

            if (normalFlawlessArrow) {
                markNoHeal(target);

                spawnWeaponHitParticles(
                        target,
                        getBloomStacks(target)
                );

                if (
                        target instanceof Mob mob &&
                        arrow.getOwner()
                                instanceof Player owner &&
                        isAggroProtected(owner)
                ) {
                    clearHostileIntent(
                            mob,
                            owner
                    );
                }
            }

            if (domainArrow) {
                float fixedArrowDamage =
                        Math.max(
                                0.0F,
                                event.getAmount()
                        );

                event.setCanceled(true);

                applyFixedDomainDamage(
                        target,
                        event.getSource(),
                        fixedArrowDamage
                );

                arrow.discard();
                return;
            }

            if (
                    normalFlawlessArrow &&
                    arrow.getOwner()
                            instanceof Player player
            ) {
                addBloomStack(target);

                FlawlessWeaponEnergy.addEnergyFromAttack(
                        player,
                        player.getMainHandItem(),
                        5
                );
            }
        }
    }

    private static void markNoHeal(
            LivingEntity target
    ) {
        target.getPersistentData().putLong(
                NBT_FLAWLESS_NO_HEAL_UNTIL,
                target.level().getGameTime() + 40L
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHeal(
            LivingHealEvent event
    ) {
        LivingEntity entity =
                event.getEntity();

        if (
                !(entity instanceof Enemy) ||
                entity.getPersistentData()
                        .getLong(
                                NBT_FLAWLESS_NO_HEAL_UNTIL
                        )
                        <= entity.level()
                                .getGameTime()
        ) {
            return;
        }

        event.setAmount(0.0F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileImpact(
            ProjectileImpactEvent event
    ) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) {
            return;
        }

        boolean normalFlawlessArrow =
                arrow.getPersistentData()
                        .getBoolean(
                                FlawlessBenedictionLegacyItem
                                        .NBT_ARROW_MARKER
                        );

        boolean domainArrow =
                arrow.getPersistentData()
                        .getBoolean(
                                NBT_DOMAIN_ARROW
                        );

        if ((normalFlawlessArrow || domainArrow)
                && arrow.getOwner() instanceof Player owner
                && event.getRayTraceResult() instanceof EntityHitResult hit
                && PlayerControlledAllies.isProtectedEffectTarget(
                        owner,
                        hit.getEntity()
                )) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
            return;
        }

        if (
                normalFlawlessArrow &&
                !arrow.getPersistentData()
                        .getBoolean(
                                NBT_ARROW_IMPACT_VISUAL
                        )
        ) {
            arrow.getPersistentData().putBoolean(
                    NBT_ARROW_IMPACT_VISUAL,
                    true
            );

            FlawlessVisualEffects.spawnArrowImpact(
                    arrow,
                    false
            );
        }

        if (domainArrow) {
            arrow.discard();
        }
    }

    private static void cleanupDomainArrows(
            Player player
    ) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        long now = level.getGameTime();

        List<AbstractArrow> arrows =
                level.getEntitiesOfClass(
                        AbstractArrow.class,
                        player.getBoundingBox()
                                .inflate(128.0D),
                        arrow ->
                                arrow.getPersistentData()
                                        .getBoolean(
                                                NBT_DOMAIN_ARROW
                                        )
                );

        boolean domainActive =
                isDomainActive(player);

        for (AbstractArrow arrow : arrows) {
            long expires =
                    arrow.getPersistentData()
                            .getLong(
                                    NBT_DOMAIN_ARROW_EXPIRES
                            );

            if (
                    !domainActive ||
                    expires <= now
            ) {
                arrow.discard();
            }
        }
    }

    private static boolean isInsideAnyDomain(
            LivingEntity entity
    ) {
        List<Player> players =
                entity.level().getEntitiesOfClass(
                        Player.class,
                        entity.getBoundingBox()
                                .inflate(DOMAIN_RADIUS),
                        player ->
                                player.isAlive()
                                        && isDomainActive(player)
                                        && player.distanceToSqr(entity)
                                        <= DOMAIN_RADIUS_SQR
                );

        return !players.isEmpty();
    }

    private static boolean hasWeapon(Player player) {
        for (
                int slot = 0;
                slot < player.getInventory()
                        .getContainerSize();
                slot++
        ) {
            if (
                    player.getInventory()
                            .getItem(slot)
                            .is(
                                    ModItems
                                            .FLAWLESS_BENEDICTION_LEGACY
                                            .get()
                            )
            ) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasCurio(
            Player player,
            net.minecraft.world.item.Item item
    ) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(
                        player,
                        stack -> stack.is(item)
                )
                .isPresent();
    }
}
