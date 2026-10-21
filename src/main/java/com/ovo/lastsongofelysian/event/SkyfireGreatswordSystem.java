package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.dimension.ElysianRealmBlockMutationGuard;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.util.SkyfireGreatswordEnergy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = com.ovo.lastsongofelysian.lastsongofelysian.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SkyfireGreatswordSystem {
    private static final int ENERGY_COST = 50;
    private static final int COOLDOWN_TICKS = 600;
    private static final float HEALTH_COST_RATIO = 0.95F;
    private static final float DAMAGE_PER_BURNED_HEALTH = 3000.0F;
    private static final double EXPLOSION_SCALE = 5.0D;
    private static final double DAMAGE_RADIUS = 32.0D * EXPLOSION_SCALE;
    private static final float EXPLOSION_POWER = (float) (24.0D * EXPLOSION_SCALE);
    private static final int IRREGULAR_BLAST_COUNT = 7;
    private static final String NBT_COOLDOWN = "SkyfireGreatswordZerothCooldown";
    private static final Set<UUID> PROTECTED_CASTERS = new HashSet<>();

    private SkyfireGreatswordSystem() {
    }

    public static boolean isHolding(Player player) {
        return !SkyfireGreatswordEnergy.findHeld(player).isEmpty();
    }

    public static void useZerothRatedPower(ServerPlayer player) {
        ItemStack weapon = SkyfireGreatswordEnergy.findHeld(player);
        if (weapon.isEmpty() || !(player.level() instanceof ServerLevel level)) return;
        long now = level.getGameTime();
        long cooldownUntil = player.getPersistentData().getLong(NBT_COOLDOWN);
        if (cooldownUntil > now) {
            int seconds = Mth.ceil((cooldownUntil - now) / 20.0D);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.lastsongofelysian.skyfire_greatsword.cooldown", seconds
                    ).withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }
        if (!SkyfireGreatswordEnergy.consume(player, weapon, ENERGY_COST)) return;

        player.getPersistentData().putLong(NBT_COOLDOWN, now + COOLDOWN_TICKS);
        CorrosionCurseEvents.onWeaponUltimate(player);
        SignetEffects.onWeaponUltimateActivated(player, 20);
        if (hasSignet(player, ModItems.SIGNET_OF_DELIVERANCE.get())) {
            SignetEffects.activateDeliveranceUltimateBuffFromSkyfire(player);
        }

        Vec3 center = player.position();
        float healthCost = player.getMaxHealth() * HEALTH_COST_RATIO;
        float healthBefore = player.getHealth();
        float healthBurned = Math.min(healthBefore, healthCost);
        boolean lethal = healthBefore <= healthCost;
        if (!lethal) player.setHealth(healthBefore - healthCost);
        SkyfireGreatswordEnergy.addFromHealthLoss(player, weapon, healthBurned);

        DamageSource fireDamage = new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.ON_FIRE),
                player
        );
        float damage = DAMAGE_PER_BURNED_HEALTH * healthBurned;
        AABB area = new AABB(center, center).inflate(DAMAGE_RADIUS);
        level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                target -> PlayerControlledAllies.isValidEnemy(player, target)
        ).forEach(target -> {
            target.setSecondsOnFire(20);
            PlayerControlledAllies.hurt(player, target, fireDamage, damage);
        });

        spawnInferno(level, center);
        PROTECTED_CASTERS.add(player.getUUID());
        try {
            detonateIrregularly(level, player, center);
        } finally {
            PROTECTED_CASTERS.remove(player.getUUID());
        }
        if (lethal && player.isAlive()) player.kill();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBasicAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || event.getAmount() <= 0.0F) return;
        ItemStack weapon = player.getMainHandItem();
        if (!weapon.is(ModItems.SKYFIRE_GREATSWORD.get())) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        event.getEntity().setSecondsOnFire(5);
        if (hasSignet(player, ModItems.SIGNET_OF_DECIMATION.get())) {
            float bonus = SignetEffects.getScaledSignetValue(
                    player,
                    SignetUpgradeData.Skill.DECIMATION_FIRE,
                    0.50F
            );
            event.setAmount(event.getAmount() * (1.0F + bonus));
        }
        SkyfireGreatswordEnergy.addFromAttack(player, weapon, 5);
    }

    @SubscribeEvent
    public static void onHealthLost(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0.0F) return;
        ItemStack weapon = SkyfireGreatswordEnergy.findHeld(player);
        if (!weapon.isEmpty()) {
            SkyfireGreatswordEnergy.addFromHealthLoss(player, weapon, event.getAmount());
        }
    }

    @SubscribeEvent
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

    private static void spawnInferno(ServerLevel level, Vec3 center) {
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                center.x, center.y + 1.0D, center.z, 40,
                8.0D * EXPLOSION_SCALE, 5.0D * EXPLOSION_SCALE,
                8.0D * EXPLOSION_SCALE, 0.0D);
        level.sendParticles(ParticleTypes.FLAME,
                center.x, center.y + 1.0D, center.z, 1200,
                18.0D * EXPLOSION_SCALE, 10.0D * EXPLOSION_SCALE,
                18.0D * EXPLOSION_SCALE, 0.45D);
        level.sendParticles(ParticleTypes.LAVA,
                center.x, center.y + 1.0D, center.z, 240,
                14.0D * EXPLOSION_SCALE, 8.0D * EXPLOSION_SCALE,
                14.0D * EXPLOSION_SCALE, 0.8D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                center.x, center.y + 2.0D, center.z, 400,
                16.0D * EXPLOSION_SCALE, 9.0D * EXPLOSION_SCALE,
                16.0D * EXPLOSION_SCALE, 0.25D);
    }

    private static void detonateIrregularly(ServerLevel level, ServerPlayer player, Vec3 center) {
        Level.ExplosionInteraction interaction = ElysianRealmBlockMutationGuard.isProtectedLevel(level)
                ? Level.ExplosionInteraction.NONE
                : Level.ExplosionInteraction.BLOCK;
        level.explode(
                player,
                center.x,
                center.y + 0.5D,
                center.z,
                EXPLOSION_POWER * 0.70F,
                true,
                interaction
        );
        for (int i = 0; i < IRREGULAR_BLAST_COUNT; i++) {
            double angle = Math.PI * 2.0D * i / IRREGULAR_BLAST_COUNT
                    + (level.random.nextDouble() - 0.5D) * 1.3D;
            double distance = EXPLOSION_POWER * (0.12D + level.random.nextDouble() * 0.30D);
            double heightOffset = EXPLOSION_POWER * (-0.16D + level.random.nextDouble() * 0.23D);
            float power = EXPLOSION_POWER * (0.32F + level.random.nextFloat() * 0.22F);
            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            level.explode(
                    player,
                    x,
                    center.y + 0.5D + heightOffset,
                    z,
                    power,
                    true,
                    interaction
            );
        }
    }

    private static boolean hasSignet(Player player, Item signet) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(signet))
                .isPresent();
    }
}
