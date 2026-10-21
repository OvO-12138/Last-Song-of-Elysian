package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEffects;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.WeaponEnergyAccess;
import com.ovo.lastsongofelysian.util.EgoSignetAmplification;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarsSignetSystem {
    private static final int RED = 1;
    private static final int BLUE = 2;
    private static final int YELLOW = 4;
    private static final long BASE_INK_DURATION = 160L;
    private static final long EXTENDED_INK_DURATION = 260L;
    private static final double COLOR_RADIUS = 16.0D;
    private static final double FIELD_RADIUS = 64.0D;

    private static final Map<UUID, InkState> INKS = new HashMap<>();
    private static final Map<UUID, BuffState> BUFFS = new HashMap<>();
    private static final Map<UUID, Long> WHITE_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Long> LEAF_SHINING_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Long> HARMONY_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Long> WILDFLOWER_DIM_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Long> LEAF_DIM_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Long> FLAME_DIM_COOLDOWN = new HashMap<>();
    private static final Map<UUID, FractureState> FRACTURES = new HashMap<>();
    private static final List<PendingSpread> PENDING_SPREADS = new ArrayList<>();
    private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();

    private StarsSignetSystem() {
    }

    public static boolean hasStars(Player player) {
        return player != null && CuriosApi.getCuriosHelper().findFirstCurio(
                player, stack -> stack.is(ModItems.SIGNET_OF_STARS.get())).isPresent();
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        UUID id = player.getUUID();
        LEAF_SHINING_COOLDOWN.remove(id);
        HARMONY_COOLDOWN.remove(id);
        WILDFLOWER_DIM_COOLDOWN.remove(id);
        LEAF_DIM_COOLDOWN.remove(id);
        FLAME_DIM_COOLDOWN.remove(id);
        FRACTURES.remove(id);
        PENDING_SPREADS.removeIf(spread -> id.equals(spread.owner()));
        BuffState buff = BUFFS.get(id);
        if (buff != null) {
            buff.purpleUntil = 0L;
            buff.greenUntil = 0L;
            buff.orangeUntil = 0L;
        }
        player.removeEffect(ModEffects.STARS_PURPLE_INK.get());
        player.removeEffect(ModEffects.STARS_GREEN_INK.get());
        player.removeEffect(ModEffects.STARS_ORANGE_INK.get());
        for (InkState ink : INKS.values()) {
            if (id.equals(ink.defenseReductionOwner)) {
                ink.defenseReduction = 0.0F;
                ink.defenseReductionUntil = 0L;
                ink.defenseReductionOwner = null;
            }
        }
    }

    public static void onWeaponSkill(Player player) {
        if (hasStars(player)) colorNearby(player, RED);
    }

    public static void onUltimate(Player player) {
        if (hasStars(player) && SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.STARS_YELLOW)) {
            colorNearby(player, YELLOW);
        }
    }

    public static void onEvasion(Player player) {
        if (hasStars(player)) colorNearby(player, BLUE);
    }

    private static void colorNearby(Player player, int color) {
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.STARS);
        long now = player.level().getGameTime();
        long duration = inkDuration(player);
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(COLOR_RADIUS),
                entity -> PlayerControlledAllies.isValidEnemy(player, entity))) {
            applyInk(player, target, color, now, duration);
        }
    }

    private static long inkDuration(Player player) {
        return SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.STARS_GRAY)
                ? EXTENDED_INK_DURATION : BASE_INK_DURATION;
    }

    private static void applyInk(Player player, LivingEntity target, int color,
                                 long now, long duration) {
        if (!PlayerControlledAllies.isValidEnemy(player, target)) return;
        InkState state = INKS.computeIfAbsent(target.getUUID(), ignored -> new InkState());
        int oldMask = state.activeMask(now);
        state.owner = player.getUUID();
        state.set(color, now + duration);
        showInk(target, effectFor(color), state.until(color) - now, 0);

        BuffState buff = BUFFS.computeIfAbsent(player.getUUID(), ignored -> new BuffState());
        if (SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.STARS_BLACK)) {
            if (buff.blackUntil < now) {
                buff.blackStacks = 0;
                player.removeEffect(ModEffects.STARS_BLACK_INK.get());
            }
            buff.blackStacks = Math.min(4, buff.blackStacks + 1);
            buff.blackUntil = now + 200L;
            showInk(player, ModEffects.STARS_BLACK_INK, buff.blackUntil - now,
                    buff.blackStacks - 1);
        }
        if (SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.STARS_WHITE)
                && now >= WHITE_COOLDOWN.getOrDefault(player.getUUID(), 0L)) {
            WHITE_COOLDOWN.put(player.getUUID(), now + 100L);
            WeaponEnergyAccess.add(player, 5);
        }
        if (oldMask != 0 && (oldMask & color) == 0
                && SignetUpgradeData.getExtensionCore(player, SignetUpgradeData.Signet.STARS)
                == SignetUpgradeData.ExtensionCore.STARS_DIM) {
            triggerHarmony(player, target, oldMask | color, now, buff);
        }
    }

    private static void triggerHarmony(Player player, LivingEntity center, int colors,
                                       long now, BuffState buff) {
        UUID playerId = player.getUUID();
        if (now < HARMONY_COOLDOWN.getOrDefault(playerId, 0L)) return;
        HARMONY_COOLDOWN.put(playerId, now + 20L);

        float attack = baseAttack(player);
        damageArea(player, center, 15.0D, attack * 0.30F);

        if ((colors & (RED | BLUE)) == (RED | BLUE)
                && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.STARS_WILDFLOWER_DIM)
                && now >= WILDFLOWER_DIM_COOLDOWN.getOrDefault(playerId, 0L)) {
            WILDFLOWER_DIM_COOLDOWN.put(playerId, now + 60L);
            guardedHurt(player, center, attack * 1.20F);
            damageAreaExcept(player, center, 8.0D, attack * 0.50F, center.getUUID());
            buff.purpleUntil = now + 300L;
            showInk(player, ModEffects.STARS_PURPLE_INK, buff.purpleUntil - now, 0);
        }
        if ((colors & (YELLOW | BLUE)) == (YELLOW | BLUE)
                && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.STARS_LEAF_DIM)
                && now >= LEAF_DIM_COOLDOWN.getOrDefault(playerId, 0L)) {
            LEAF_DIM_COOLDOWN.put(playerId, now + 60L);
            damageArea(player, center, 8.0D, attack * 0.80F);
            buff.greenUntil = now + 300L;
            showInk(player, ModEffects.STARS_GREEN_INK, buff.greenUntil - now, 0);
        }
        if ((colors & (RED | YELLOW)) == (RED | YELLOW)
                && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.STARS_FLAME_DIM)
                && now >= FLAME_DIM_COOLDOWN.getOrDefault(playerId, 0L)) {
            FLAME_DIM_COOLDOWN.put(playerId, now + 140L);
            buff.orangeUntil = now + 300L;
            showInk(player, ModEffects.STARS_ORANGE_INK, buff.orangeUntil - now, 0);
            triggerTimeFracture(player, now);
            reduceAllEnemyDefense(player, now);
        }
    }

    private static void triggerTimeFracture(Player player, long now) {
        if (!(player.level() instanceof ServerLevel level)) return;
        FractureState fracture = new FractureState(level.dimension(), now + 50L);
        captureFractureTargets(player, level, fracture);
        FRACTURES.put(player.getUUID(), fracture);
    }

    private static void captureFractureTargets(Player player, ServerLevel level,
                                               FractureState fracture) {
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity living)
                    || !PlayerControlledAllies.isValidEnemy(player, living)
                    || PlayerControlledAllies.isSignetTimeStopExempt(entity, level.players())) continue;
            fracture.positions.putIfAbsent(entity.getUUID(), entity.position());
        }
    }

    private static void reduceAllEnemyDefense(Player player, long now) {
        if (!(player.level() instanceof ServerLevel level)) return;
        float reduction = Math.min(0.40F,
                0.20F + 0.05F * Math.min(4, countExtensionAugmentFamilies(player)));
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity living)
                    || !PlayerControlledAllies.isValidEnemy(player, living)) continue;
            InkState state = stateFor(living);
            state.defenseReduction = Math.max(state.defenseReduction, reduction);
            state.defenseReductionUntil = now + 200L;
            state.defenseReductionOwner = player.getUUID();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        long now = event.getEntity().level().getGameTime();
        ServerLevel level = event.getEntity().level() instanceof ServerLevel serverLevel
                ? serverLevel : null;
        InkState targetInk = INKS.get(event.getEntity().getUUID());
        if (targetInk != null) {
            int mask = targetInk.activeMask(now);
            float primaryBoost = inkPrimaryBoost(level, targetInk, now);
            if ((mask & RED) != 0) {
                event.setAmount(event.getAmount() * (1.10F + primaryBoost));
            }
            if ((mask & YELLOW) != 0) {
                event.setAmount(event.getAmount() * (1.15F + primaryBoost));
            }
            if (targetInk.defenseReductionUntil >= now && targetInk.defenseReduction > 0.0F) {
                event.setAmount(SignetEffects.applyArmorPenetration(
                        event.getEntity(), event.getSource(), event.getAmount(),
                        targetInk.defenseReduction));
            }
        }
        if (event.getSource().getEntity() instanceof LivingEntity source) {
            InkState sourceInk = INKS.get(source.getUUID());
            if (sourceInk != null && (sourceInk.activeMask(now) & BLUE) != 0) {
                float reduction = Math.min(0.95F,
                        0.10F + inkPrimaryBoost(level, sourceInk, now));
                event.setAmount(event.getAmount() * (1.0F - reduction));
            }
        }
        if (!(event.getSource().getEntity() instanceof Player player) || !hasStars(player)
                || !PlayerControlledAllies.isValidEnemy(player, event.getEntity())) return;
        UUID playerId = player.getUUID();
        if (INTERNAL_DAMAGE.contains(playerId)) return;

        BuffState buff = BUFFS.computeIfAbsent(playerId, ignored -> new BuffState());
        if (EgoSignetAmplification.isEnabledFor(
                player, ModItems.SIGNET_OF_STARS.get())) {
            event.getEntity().addEffect(new MobEffectInstance(
                    ModEffects.PINK_INK.get(), Integer.MAX_VALUE, 0,
                    false, false, true));
        }
        if (buff.blackUntil >= now) {
            event.setAmount(event.getAmount() * (1.0F + 0.10F * buff.blackStacks));
        }
        int selfColors = (buff.purpleUntil >= now ? 1 : 0)
                + (buff.greenUntil >= now ? 1 : 0)
                + (buff.orangeUntil >= now ? 1 : 0);
        if (selfColors > 0) {
            float perColor = 0.15F;
            if (buff.greenUntil >= now) {
                perColor += Math.min(0.20F,
                        countExtensionAugmentFamilies(player) * 0.05F);
            }
            event.setAmount(event.getAmount() * (1.0F + selfColors * perColor));
        }

        if (SignetUpgradeData.getExtensionCore(player, SignetUpgradeData.Signet.STARS)
                != SignetUpgradeData.ExtensionCore.STARS_SHINING) return;

        int fieldColors = fieldColorCount(player, now);
        if (fieldColors > 0) {
            event.setAmount(event.getAmount() * (1.0F + fieldColors * 0.10F));
        }
        float penetration = Math.min(0.40F, countSelectedCores(player) * 0.05F);
        event.setAmount(SignetEffects.applyArmorPenetration(
                event.getEntity(), event.getSource(), event.getAmount(), penetration));

        if (targetInk == null || !playerId.equals(targetInk.owner)) return;
        int mask = targetInk.activeMask(now);
        if ((mask & (RED | BLUE)) == (RED | BLUE)
                && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.STARS_WILDFLOWER_SHINING)) {
            event.setAmount(event.getAmount() * 1.50F);
        }
        if ((mask & (RED | YELLOW)) == (RED | YELLOW)
                && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.STARS_FLAME_SHINING)) {
            event.setAmount(SignetEffects.applyArmorPenetration(
                    event.getEntity(), event.getSource(), event.getAmount(), 0.10F));
        }
        if ((mask & (BLUE | YELLOW)) == (BLUE | YELLOW)
                && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.STARS_LEAF_SHINING)
                && now >= LEAF_SHINING_COOLDOWN.getOrDefault(playerId, 0L)) {
            LEAF_SHINING_COOLDOWN.put(playerId, now + 160L);
            damageArea(player, event.getEntity(), 6.0D, baseAttack(player) * 3.0F);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        InkState dead = INKS.remove(event.getEntity().getUUID());
        if (dead == null || dead.owner == null
                || !(event.getEntity().level() instanceof ServerLevel level)) return;
        Player player = level.getPlayerByUUID(dead.owner);
        if (player == null || !SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.STARS_WILDFLOWER_SHINING)) return;
        int mask = dead.activeMask(level.getGameTime());
        if (mask == 0) return;
        PENDING_SPREADS.add(new PendingSpread(
                player.getUUID(), level.dimension(), event.getEntity().position(),
                mask, level.getGameTime() + 20L));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        long now = player.level().getGameTime();
        tickFracture(player, now);
        tickPendingSpreads(player, now);
        if (now % 20L == 0L) cleanupPlayerState(player, now);
    }

    private static void tickFracture(Player player, long now) {
        FractureState fracture = FRACTURES.get(player.getUUID());
        if (fracture == null) return;
        if (!(player.level() instanceof ServerLevel level)
                || !level.dimension().equals(fracture.dimension)
                || now > fracture.until) {
            FRACTURES.remove(player.getUUID());
            return;
        }
        captureFractureTargets(player, level, fracture);
        Iterator<Map.Entry<UUID, Vec3>> iterator = fracture.positions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Vec3> entry = iterator.next();
            Entity entity = level.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity living)
                    || !PlayerControlledAllies.isValidEnemy(player, living)
                    || PlayerControlledAllies.isSignetTimeStopExempt(entity, level.players())) {
                iterator.remove();
                continue;
            }
            Vec3 locked = entry.getValue();
            entity.setDeltaMovement(Vec3.ZERO);
            entity.fallDistance = 0.0F;
            entity.teleportTo(locked.x, locked.y, locked.z);
        }
    }

    private static void tickPendingSpreads(Player player, long now) {
        if (player.getServer() == null) return;
        Iterator<PendingSpread> iterator = PENDING_SPREADS.iterator();
        while (iterator.hasNext()) {
            PendingSpread spread = iterator.next();
            if (!spread.owner.equals(player.getUUID()) || now < spread.executeAt) continue;
            iterator.remove();
            ServerLevel level = player.getServer().getLevel(spread.dimension);
            if (level == null || !hasStars(player)) continue;
            long duration = inkDuration(player);
            AABB area = new AABB(spread.position, spread.position).inflate(8.0D);
            for (LivingEntity target : level.getEntitiesOfClass(
                    LivingEntity.class, area,
                    entity -> PlayerControlledAllies.isValidEnemy(player, entity))) {
                if ((spread.colors & RED) != 0) applyInk(player, target, RED, now, duration);
                if ((spread.colors & BLUE) != 0) applyInk(player, target, BLUE, now, duration);
                if ((spread.colors & YELLOW) != 0) applyInk(player, target, YELLOW, now, duration);
            }
        }
    }

    private static void cleanupPlayerState(Player player, long now) {
        UUID playerId = player.getUUID();
        INKS.entrySet().removeIf(entry -> entry.getValue().activeMask(now) == 0
                && entry.getValue().defenseReductionUntil < now);
        BuffState buff = BUFFS.get(playerId);
        if (buff != null && buff.expired(now)) BUFFS.remove(playerId);
    }

    private static RegistryObject<MobEffect> effectFor(int color) {
        if (color == RED) return ModEffects.STARS_RED_INK;
        if (color == BLUE) return ModEffects.STARS_BLUE_INK;
        return ModEffects.STARS_YELLOW_INK;
    }

    private static void showInk(LivingEntity entity, RegistryObject<MobEffect> effect,
                                long remaining, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect.get(),
                (int) Math.max(1L, Math.min(Integer.MAX_VALUE, remaining)),
                amplifier, false, false, true));
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()
                || !(event.getEntity() instanceof LivingEntity entity)) return;
        long now = entity.level().getGameTime();
        InkState ink = INKS.get(entity.getUUID());
        removeStaleInk(entity, ModEffects.STARS_RED_INK, ink == null ? 0L : ink.redUntil, now);
        removeStaleInk(entity, ModEffects.STARS_BLUE_INK, ink == null ? 0L : ink.blueUntil, now);
        removeStaleInk(entity, ModEffects.STARS_YELLOW_INK, ink == null ? 0L : ink.yellowUntil, now);
        if (entity instanceof Player) {
            BuffState buff = BUFFS.get(entity.getUUID());
            removeStaleInk(entity, ModEffects.STARS_BLACK_INK,
                    buff == null ? 0L : buff.blackUntil, now);
            removeStaleInk(entity, ModEffects.STARS_PURPLE_INK,
                    buff == null ? 0L : buff.purpleUntil, now);
            removeStaleInk(entity, ModEffects.STARS_GREEN_INK,
                    buff == null ? 0L : buff.greenUntil, now);
            removeStaleInk(entity, ModEffects.STARS_ORANGE_INK,
                    buff == null ? 0L : buff.orangeUntil, now);
        }
    }

    private static void removeStaleInk(LivingEntity entity, RegistryObject<MobEffect> effect,
                                       long until, long now) {
        if (until == 0L || until < now) entity.removeEffect(effect.get());
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        long now = entity.level().getGameTime();
        if (!(entity instanceof Player)) {
            MobEffectInstance pink = entity.getEffect(ModEffects.PINK_INK.get());
            if (pink != null && pink.getDuration() < 1_000_000) {
                entity.addEffect(new MobEffectInstance(
                        ModEffects.PINK_INK.get(), Integer.MAX_VALUE,
                        pink.getAmplifier(), false, false, true));
            }
        }
        InkState ink = INKS.get(entity.getUUID());
        if (ink != null) {
            syncInk(entity, ModEffects.STARS_RED_INK, ink.redUntil, now, 0);
            syncInk(entity, ModEffects.STARS_BLUE_INK, ink.blueUntil, now, 0);
            syncInk(entity, ModEffects.STARS_YELLOW_INK, ink.yellowUntil, now, 0);
        }
        if (entity instanceof Player) {
            BuffState buff = BUFFS.get(entity.getUUID());
            if (buff != null) {
                syncInk(entity, ModEffects.STARS_BLACK_INK, buff.blackUntil, now,
                        Math.max(0, buff.blackStacks - 1));
                syncInk(entity, ModEffects.STARS_PURPLE_INK, buff.purpleUntil, now, 0);
                syncInk(entity, ModEffects.STARS_GREEN_INK, buff.greenUntil, now, 0);
                syncInk(entity, ModEffects.STARS_ORANGE_INK, buff.orangeUntil, now, 0);
            }
        }
    }

    private static void syncInk(LivingEntity entity, RegistryObject<MobEffect> effect,
                                long until, long now, int amplifier) {
        MobEffectInstance shown = entity.getEffect(effect.get());
        if (until == 0L || until < now) {
            if (shown != null) entity.removeEffect(effect.get());
        } else if (shown == null || shown.getAmplifier() != amplifier) {
            showInk(entity, effect, until - now, amplifier);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();
        player.removeEffect(ModEffects.STARS_BLACK_INK.get());
        player.removeEffect(ModEffects.STARS_PURPLE_INK.get());
        player.removeEffect(ModEffects.STARS_GREEN_INK.get());
        player.removeEffect(ModEffects.STARS_ORANGE_INK.get());
        if (player.getServer() != null) {
            for (Map.Entry<UUID, InkState> entry : INKS.entrySet()) {
                if (!playerId.equals(entry.getValue().owner)) continue;
                for (ServerLevel level : player.getServer().getAllLevels()) {
                    Entity entity = level.getEntity(entry.getKey());
                    if (entity instanceof LivingEntity living) {
                        living.removeEffect(ModEffects.STARS_RED_INK.get());
                        living.removeEffect(ModEffects.STARS_BLUE_INK.get());
                        living.removeEffect(ModEffects.STARS_YELLOW_INK.get());
                    }
                }
            }
        }
        INKS.entrySet().removeIf(entry -> playerId.equals(entry.getValue().owner));
        BUFFS.remove(playerId);
        WHITE_COOLDOWN.remove(playerId);
        LEAF_SHINING_COOLDOWN.remove(playerId);
        HARMONY_COOLDOWN.remove(playerId);
        WILDFLOWER_DIM_COOLDOWN.remove(playerId);
        LEAF_DIM_COOLDOWN.remove(playerId);
        FLAME_DIM_COOLDOWN.remove(playerId);
        FRACTURES.remove(playerId);
        PENDING_SPREADS.removeIf(spread -> spread.owner.equals(playerId));
        INTERNAL_DAMAGE.remove(playerId);
    }

    private static float inkPrimaryBoost(ServerLevel level, InkState ink, long now) {
        if (level == null || ink.owner == null) return 0.0F;
        Player owner = level.getPlayerByUUID(ink.owner);
        BuffState buff = BUFFS.get(ink.owner);
        if (owner == null || buff == null || buff.purpleUntil < now) return 0.0F;
        return Math.min(0.40F, countExtensionAugmentFamilies(owner) * 0.05F);
    }

    private static int fieldColorCount(Player player, long now) {
        if (!(player.level() instanceof ServerLevel level)) return 0;
        int colors = 0;
        for (Map.Entry<UUID, InkState> entry : INKS.entrySet()) {
            InkState ink = entry.getValue();
            if (!player.getUUID().equals(ink.owner)) continue;
            Entity entity = level.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity living)
                    || !PlayerControlledAllies.isValidEnemy(player, living)
                    || living.distanceToSqr(player) > FIELD_RADIUS * FIELD_RADIUS) continue;
            colors |= ink.activeMask(now);
            if ((colors & 7) == 7) return 3;
        }
        return Integer.bitCount(colors & 7);
    }

    private static InkState stateFor(LivingEntity entity) {
        return INKS.computeIfAbsent(entity.getUUID(), ignored -> new InkState());
    }

    private static float baseAttack(Player player) {
        return Math.max(1.0F, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    private static void damageArea(Player player, LivingEntity center, double radius,
                                   float damage) {
        damageAreaExcept(player, center, radius, damage, null);
    }

    private static void damageAreaExcept(Player player, LivingEntity center, double radius,
                                         float damage, UUID excluded) {
        for (LivingEntity target : center.level().getEntitiesOfClass(
                LivingEntity.class, center.getBoundingBox().inflate(radius),
                entity -> (excluded == null || !excluded.equals(entity.getUUID()))
                        && PlayerControlledAllies.isValidEnemy(player, entity))) {
            guardedHurt(player, target, damage);
        }
    }

    private static void guardedHurt(Player player, LivingEntity target, float damage) {
        UUID uuid = player.getUUID();
        if (!INTERNAL_DAMAGE.add(uuid)) return;
        try {
            PlayerControlledAllies.hurt(player, target,
                    player.damageSources().playerAttack(player), damage);
        } finally {
            INTERNAL_DAMAGE.remove(uuid);
        }
    }

    private static int countSelectedCores(Player player) {
        int count = 0;
        if (SignetUpgradeData.getDeliveranceCore(player) != null) count++;
        if (SignetUpgradeData.getDisciplineCore(player) != null) count++;
        if (SignetUpgradeData.getGoldCore(player) != null) count++;
        if (SignetUpgradeData.getHelixCore(player) != null) count++;
        if (SignetUpgradeData.getDecimationCore(player) != null) count++;
        if (SignetUpgradeData.getBodhiCore(player) != null) count++;
        if (SignetUpgradeData.getSetsuraCore(player) != null) count++;
        if (SignetUpgradeData.getDaybreakCore(player) != null) count++;
        for (SignetUpgradeData.Signet signet : new SignetUpgradeData.Signet[]{
                SignetUpgradeData.Signet.REVERIE, SignetUpgradeData.Signet.VICISSITUDE,
                SignetUpgradeData.Signet.STARS, SignetUpgradeData.Signet.INFINITY}) {
            if (SignetUpgradeData.getExtensionCore(player, signet) != null) count++;
        }
        return count;
    }

    private static int countExtensionAugmentFamilies(Player player) {
        int count = 0;
        for (SignetUpgradeData.Signet signet : SignetUpgradeData.Signet.values()) {
            boolean found = false;
            for (SignetUpgradeData.Skill skill : SignetUpgradeData.Skill.values()) {
                if (skill.signet() == signet && (
                        skill.isExtensionCoreSignet()
                                && SignetUpgradeData.hasExtensionCoreSignet(player, skill)
                        || skill.isDaybreakCoreSignet()
                                && SignetUpgradeData.hasDaybreakCoreSignet(player, skill)
                        || skill.isSetsuraCoreSignet()
                                && SignetUpgradeData.hasSetsuraCoreSignet(player, skill)
                        || skill.isBodhiCoreSignet()
                                && SignetUpgradeData.hasBodhiCoreSignet(player, skill)
                        || skill.isDecimationCoreSignet()
                                && SignetUpgradeData.hasDecimationCoreSignet(player, skill)
                        || skill.isHelixCoreSignet()
                                && SignetUpgradeData.hasHelixCoreSignet(player, skill)
                        || skill.isGoldCoreSignet()
                                && SignetUpgradeData.hasGoldCoreSignet(player, skill)
                        || skill.isDisciplineCoreSignet()
                                && SignetUpgradeData.hasDisciplineCoreSignet(player, skill)
                        || skill.isDeliveranceCoreSignet()
                                && SignetUpgradeData.hasDeliveranceCoreSignet(player, skill))) {
                    found = true;
                    break;
                }
            }
            if (found) count++;
        }
        return count;
    }

    private static final class InkState {
        private UUID owner;
        private UUID defenseReductionOwner;
        private long redUntil;
        private long blueUntil;
        private long yellowUntil;
        private float defenseReduction;
        private long defenseReductionUntil;

        private int activeMask(long now) {
            return (redUntil >= now ? RED : 0)
                    | (blueUntil >= now ? BLUE : 0)
                    | (yellowUntil >= now ? YELLOW : 0);
        }

        private void set(int color, long until) {
            if (color == RED) redUntil = Math.max(redUntil, until);
            if (color == BLUE) blueUntil = Math.max(blueUntil, until);
            if (color == YELLOW) yellowUntil = Math.max(yellowUntil, until);
        }

        private long until(int color) {
            if (color == RED) return redUntil;
            if (color == BLUE) return blueUntil;
            return yellowUntil;
        }
    }

    private static final class BuffState {
        private int blackStacks;
        private long blackUntil;
        private long purpleUntil;
        private long greenUntil;
        private long orangeUntil;

        private boolean expired(long now) {
            return blackUntil < now && purpleUntil < now
                    && greenUntil < now && orangeUntil < now;
        }
    }

    private static final class FractureState {
        private final ResourceKey<Level> dimension;
        private final long until;
        private final Map<UUID, Vec3> positions = new HashMap<>();

        private FractureState(ResourceKey<Level> dimension, long until) {
            this.dimension = dimension;
            this.until = until;
        }
    }

    private record PendingSpread(UUID owner, ResourceKey<Level> dimension,
                                 Vec3 position, int colors, long executeAt) {
    }
}
