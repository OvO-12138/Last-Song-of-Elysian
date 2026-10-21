package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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
public final class InfinitySignetSystem {
    private static final UUID UNKNOWN_SPEED = UUID.fromString("ef5c09b3-cbef-4404-b861-67b8eed38343");
    private static final Map<UUID, Long> ASSIMILATION_END = new HashMap<>();
    private static final Map<UUID, Long> ASSIMILATION_START = new HashMap<>();
    private static final Map<UUID, Long> UNKNOWN_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Set<UUID>> UNKNOWN_AFFECTED = new HashMap<>();
    private static final Map<UUID, Integer> SYNC_PROGRESS = new HashMap<>();
    private static final Map<UUID, Long> SYNC_END = new HashMap<>();
    private static final Map<UUID, Long> LAST_SHADOW = new HashMap<>();
    private static final Map<UUID, Map<UUID, Long>> ALLY_FIRST_SEEN = new HashMap<>();
    private static final Map<UUID, Long> COLLAPSE_LAST = new HashMap<>();
    private static final Map<UUID, Float> COLLAPSE_VULNERABILITY = new HashMap<>();
    private static final Map<UUID, Map<UUID, Float>> COLLAPSE_BY_OWNER = new HashMap<>();
    private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();

    private InfinitySignetSystem() {
    }

    public static boolean hasInfinity(Player player) {
        return player != null && CuriosApi.getCuriosHelper().findFirstCurio(
                player, stack -> stack.is(ModItems.SIGNET_OF_INFINITY.get())).isPresent();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        float existingVulnerability = COLLAPSE_VULNERABILITY.getOrDefault(
                event.getEntity().getUUID(), 0.0F);
        if (existingVulnerability > 0.0F) {
            event.setAmount(event.getAmount() * (1.0F + existingVulnerability));
        }
        LivingEntity source = event.getSource().getEntity() instanceof LivingEntity living
                ? living : null;
        if (event.getEntity() instanceof Player defender && hasInfinity(defender)
                && SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                defender, SignetUpgradeData.Skill.INFINITY_B)) {
            int allies = effectiveAllyCount(defender, defender.level().getGameTime());
            event.setAmount(event.getAmount() * (1.0F - Math.min(0.95F, allies * 0.08F)));
        }
        if (source == null) return;

        Player controller = PlayerControlledAllies.findController(source);
        if (controller == null || !hasInfinity(controller)
                || !PlayerControlledAllies.isValidEnemy(controller, event.getEntity())) return;
        UUID controllerId = controller.getUUID();
        long now = controller.level().getGameTime();
        boolean internal = INTERNAL_DAMAGE.contains(controllerId);
        int allies = effectiveAllyCount(controller, now);

        if (source == controller) {
            if (SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                    controller, SignetUpgradeData.Skill.INFINITY_P)) {
                event.setAmount(event.getAmount() * (1.0F + allies * 0.08F));
            }
            if (ASSIMILATION_END.getOrDefault(controllerId, 0L) >= now) {
                if (SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                        controller, SignetUpgradeData.Skill.INFINITY_E)) {
                    event.setAmount(event.getAmount() * 1.60F);
                }
                event.setAmount(event.getAmount() * 1.10F);
                event.setAmount(event.getAmount() * (1.0F + deathXBonus(controller, now)));
                if (SignetUpgradeData.hasExtensionCoreSignet(
                        controller, SignetUpgradeData.Skill.INFINITY_NEWBORN_X)) {
                    event.setAmount(SignetEffects.applyArmorPenetration(
                            event.getEntity(), event.getSource(), event.getAmount(), 0.15F));
                }
            }
            if (SYNC_END.getOrDefault(controllerId, 0L) >= now && allies > 0) {
                event.setAmount(event.getAmount()
                        * (1.0F + playerInheritedSummonBonus(controller)));
                event.setAmount(SignetEffects.applyArmorPenetration(
                        event.getEntity(), event.getSource(), event.getAmount(), 0.05F));
                applyCollapse(controller, event.getEntity(), now);
            }
        } else {
            if (SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                    controller, SignetUpgradeData.Skill.INFINITY_E)) {
                event.setAmount(event.getAmount() * 1.60F);
            }
            long seen = markAllySeen(controller, source, now);
            if (SignetUpgradeData.getExtensionCore(controller, SignetUpgradeData.Signet.INFINITY)
                    == SignetUpgradeData.ExtensionCore.INFINITY_M
                    && SignetUpgradeData.hasExtensionCoreSignet(
                    controller, SignetUpgradeData.Skill.INFINITY_IMMORTAL_M)
                    && now - seen >= 160L) {
                float ramp = Math.min(0.50F, (now - seen - 160L) / 20.0F * 0.05F);
                event.setAmount(event.getAmount() * (1.0F + ramp));
            }
            enterAssimilation(controller, source, now);
            if (ASSIMILATION_END.getOrDefault(controllerId, 0L) >= now) {
                event.setAmount(event.getAmount() * 1.10F);
                event.setAmount(event.getAmount() * (1.0F + deathXBonus(controller, now)));
            }
            if (!internal && SYNC_END.getOrDefault(controllerId, 0L) >= now) {
                applyCollapse(controller, event.getEntity(), now);
                if (now - LAST_SHADOW.getOrDefault(controllerId, Long.MIN_VALUE) >= 16L) {
                    LAST_SHADOW.put(controllerId, now);
                    float damage = Math.max(1.0F,
                            (float) controller.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.50F);
                    guardedHurt(controller, event.getEntity(), damage);
                }
            }
        }

    }

    private static void enterAssimilation(Player player, LivingEntity ally, long now) {
        if (SignetUpgradeData.getExtensionCore(player, SignetUpgradeData.Signet.INFINITY)
                != SignetUpgradeData.ExtensionCore.INFINITY_X) return;
        UUID uuid = player.getUUID();
        long currentEnd = ASSIMILATION_END.getOrDefault(uuid, 0L);
        if (currentEnd < now) {
            ASSIMILATION_END.put(uuid, now + 160L);
            ASSIMILATION_START.put(uuid, now);
        } else {
            ASSIMILATION_END.put(uuid, currentEnd + 80L);
            if (SignetUpgradeData.hasExtensionCoreSignet(
                    player, SignetUpgradeData.Skill.INFINITY_UNKNOWN_X)
                    && now >= UNKNOWN_COOLDOWN.getOrDefault(uuid, 0L)) {
                UNKNOWN_COOLDOWN.put(uuid, now + 80L);
                for (LivingEntity controlled : allies(player)) {
                    AttributeInstance speed = controlled.getAttribute(Attributes.ATTACK_SPEED);
                    if (speed == null) continue;
                    speed.removeModifier(UNKNOWN_SPEED);
                    speed.addTransientModifier(new AttributeModifier(
                            UNKNOWN_SPEED, "infinity_unknown_x", 0.30D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                    UNKNOWN_AFFECTED.computeIfAbsent(uuid, ignored -> new HashSet<>())
                            .add(controlled.getUUID());
                }
            }
        }
    }

    private static void applyCollapse(Player player, LivingEntity target, long now) {
        if (!SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.INFINITY_COLLAPSE_M)) return;
        UUID targetId = target.getUUID();
        if (now - COLLAPSE_LAST.getOrDefault(targetId, Long.MIN_VALUE) < 20L) return;
        COLLAPSE_LAST.put(targetId, now);
        Map<UUID, Float> ownerValues = COLLAPSE_BY_OWNER.computeIfAbsent(
                player.getUUID(), ignored -> new HashMap<>());
        ownerValues.put(targetId,
                Math.min(0.30F, ownerValues.getOrDefault(targetId, 0.0F) + 0.01F));
        refreshCollapseVulnerability(targetId);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        UUID uuid = player.getUUID();
        long now = player.level().getGameTime();
        if (!hasInfinity(player)) {
            clearPlayerState(player);
            return;
        }
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.INFINITY);
        if (ASSIMILATION_END.getOrDefault(uuid, 0L) < now) {
            ASSIMILATION_START.remove(uuid);
            removeUnknownSpeed(player);
        }
        if (now % 20L != 0L) return;
        int allies = countAllies(player);
        observeAllies(player, now);
        if (SignetUpgradeData.getExtensionCore(player, SignetUpgradeData.Signet.INFINITY)
                == SignetUpgradeData.ExtensionCore.INFINITY_M) {
            int progress = SYNC_PROGRESS.getOrDefault(uuid, 0) + 10;
            if (allies > 0 && SignetUpgradeData.hasExtensionCoreSignet(
                    player, SignetUpgradeData.Skill.INFINITY_SACRIFICE_M)) progress += 5;
            if (progress >= 100) {
                progress = 0;
                SYNC_END.put(uuid, now + 120L);
            } else if (allies > 0 && SYNC_END.getOrDefault(uuid, 0L) >= now
                    && SignetUpgradeData.hasExtensionCoreSignet(
                    player, SignetUpgradeData.Skill.INFINITY_SACRIFICE_M)) {
                SYNC_END.merge(uuid, 20L, Long::sum);
            }
            SYNC_PROGRESS.put(uuid, progress);
        }
    }

    public static float playerInheritedSummonBonus(Player player) {
        if (!hasInfinity(player)
                || SignetUpgradeData.getExtensionCore(player, SignetUpgradeData.Signet.INFINITY)
                != SignetUpgradeData.ExtensionCore.INFINITY_M
                || !SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.INFINITY_IMMORTAL_M)
                || SYNC_END.getOrDefault(player.getUUID(), 0L) < player.level().getGameTime()) return 0.0F;
        long now = player.level().getGameTime();
        float strongest = 0.0F;
        Map<UUID, Long> seen = ALLY_FIRST_SEEN.getOrDefault(player.getUUID(), Map.of());
        for (LivingEntity ally : allies(player)) {
            long first = seen.getOrDefault(ally.getUUID(), now);
            if (now - first < 160L) continue;
            strongest = Math.max(strongest,
                    Math.min(0.50F, (now - first - 160L) / 20.0F * 0.05F));
        }
        return Math.min(0.30F, strongest);
    }

    private static int countAllies(Player player) {
        return Math.min(3, allies(player).size());
    }

    private static int effectiveAllyCount(Player player, long now) {
        int count = countAllies(player);
        if (ASSIMILATION_END.getOrDefault(player.getUUID(), 0L) >= now) count++;
        return Math.min(3, count);
    }

    private static java.util.List<LivingEntity> allies(Player player) {
        return player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(64.0D),
                entity -> entity != player && entity.isAlive()
                        && PlayerControlledAllies.isProtectedAlly(player, entity));
    }

    private static long markAllySeen(Player player, LivingEntity ally, long now) {
        return ALLY_FIRST_SEEN.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>())
                .computeIfAbsent(ally.getUUID(), ignored -> now);
    }

    private static void observeAllies(Player player, long now) {
        Map<UUID, Long> seen = ALLY_FIRST_SEEN.computeIfAbsent(
                player.getUUID(), ignored -> new HashMap<>());
        Set<UUID> present = new HashSet<>();
        for (LivingEntity ally : allies(player)) {
            present.add(ally.getUUID());
            seen.putIfAbsent(ally.getUUID(), now);
        }
        seen.keySet().removeIf(id -> !present.contains(id));
    }

    private static float deathXBonus(Player player, long now) {
        if (!SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.INFINITY_DEATH_X)) return 0.0F;
        long start = ASSIMILATION_START.getOrDefault(player.getUUID(), now);
        return Math.min(0.50F, Math.max(0L, now - start) / 20.0F * 0.10F);
    }

    private static void removeUnknownSpeed(Player player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        Set<UUID> affected = UNKNOWN_AFFECTED.remove(player.getUUID());
        if (affected == null) return;
        for (UUID entityId : affected) {
            Entity entity = level.getEntity(entityId);
            if (!(entity instanceof LivingEntity living)) continue;
            AttributeInstance speed = living.getAttribute(Attributes.ATTACK_SPEED);
            if (speed != null) speed.removeModifier(UNKNOWN_SPEED);
        }
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        UUID uuid = player.getUUID();
        removeUnknownSpeed(player);
        ASSIMILATION_END.remove(uuid);
        ASSIMILATION_START.remove(uuid);
        UNKNOWN_COOLDOWN.remove(uuid);
        SYNC_PROGRESS.remove(uuid);
        SYNC_END.remove(uuid);
        LAST_SHADOW.remove(uuid);
        ALLY_FIRST_SEEN.remove(uuid);
        Map<UUID, Float> affected = COLLAPSE_BY_OWNER.remove(uuid);
        if (affected != null) {
            for (UUID targetId : affected.keySet()) {
                refreshCollapseVulnerability(targetId);
                if (!COLLAPSE_VULNERABILITY.containsKey(targetId)) {
                    COLLAPSE_LAST.remove(targetId);
                }
            }
        }
    }

    private static void refreshCollapseVulnerability(UUID targetId) {
        float total = 0.0F;
        for (Map<UUID, Float> ownerValues : COLLAPSE_BY_OWNER.values()) {
            total += ownerValues.getOrDefault(targetId, 0.0F);
        }
        if (total <= 0.0F) COLLAPSE_VULNERABILITY.remove(targetId);
        else COLLAPSE_VULNERABILITY.put(targetId, Math.min(0.30F, total));
    }

    private static void clearPlayerState(Player player) {
        resetCoreRuntime(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayerState(event.getEntity());
    }

    private static void guardedHurt(Player player, LivingEntity target, float damage) {
        if (!PlayerControlledAllies.isValidEnemy(player, target)) return;
        UUID uuid = player.getUUID();
        if (!INTERNAL_DAMAGE.add(uuid)) return;
        try {
            PlayerControlledAllies.hurt(player, target,
                    player.damageSources().playerAttack(player), damage);
        } finally {
            INTERNAL_DAMAGE.remove(uuid);
        }
    }
}
