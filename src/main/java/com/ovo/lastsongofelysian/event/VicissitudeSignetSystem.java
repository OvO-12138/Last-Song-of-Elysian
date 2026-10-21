package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEffects;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.WeaponEnergyAccess;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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
public final class VicissitudeSignetSystem {
    private static final UUID MOVE_MOD = UUID.fromString("92e4090b-63e9-42e8-9f7b-6276f61aaf1e");
    private static final UUID ATTACK_SPEED_MOD = UUID.fromString("9499891b-9f3e-4a4d-ae14-0bb078bb0afd");
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();

    private VicissitudeSignetSystem() {
    }

    private static boolean hasVicissitude(Player player) {
        return player != null && CuriosApi.getCuriosHelper().findFirstCurio(
                player, stack -> stack.is(ModItems.SIGNET_OF_VICISSITUDE.get())).isPresent();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()
                || !hasVicissitude(player) || event.getAmount() <= 0.0F) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.VICISSITUDE);
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        SignetUpgradeData.ExtensionCore core = SignetUpgradeData.getExtensionCore(
                player, SignetUpgradeData.Signet.VICISSITUDE);
        if (core == SignetUpgradeData.ExtensionCore.VICISSITUDE_MEMORY && state.memory > 0
                && event.getSource().getEntity() instanceof LivingEntity attacker
                && PlayerControlledAllies.isValidEnemy(player, attacker)) {
            state.memory--;
            state.memoryBlocks++;
            event.setAmount(0.0F);
            int invulnerability = 10;
            if (SignetUpgradeData.hasExtensionCoreSignet(
                    player, SignetUpgradeData.Skill.VICISSITUDE_FEATHER)) {
                invulnerability += state.memoryBlocks * 6;
                WeaponEnergyAccess.add(player, 5);
            }
            player.invulnerableTime = Math.max(player.invulnerableTime, invulnerability);
            if (state.memory == 0 && player.level().getGameTime() >= state.nextAwakenTick
                    && SignetUpgradeData.hasExtensionCoreSignet(
                    player, SignetUpgradeData.Skill.VICISSITUDE_AWAKEN)) {
                state.nextAwakenTick = player.level().getGameTime() + 200L;
                state.maxPenalty = Math.min(60, state.maxPenalty + 10);
                state.memory = 1;
            }
            return;
        }
        if (core == SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE && state.resolve > 0) {
            state.resolve--;
            event.setAmount(0.0F);
            float damage = Math.max(1.0F,
                    (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE))
                    * state.resolve * 6.50F;
            damageArea(player, damage);
            return;
        }
        if (state.stacks > 0) {
            event.setAmount(event.getAmount()
                    * (1.0F - Math.min(0.95F, state.stacks * 0.008F)));
            state.stacks = Math.max(0, state.stacks - 10);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onOutgoingDamage(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || player.level().isClientSide() || !hasVicissitude(player)
                || !PlayerControlledAllies.isValidEnemy(player, event.getEntity())) return;
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        float multiplier = 1.0F + state.stacks * 0.008F;
        if (state.memory > 0 && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.VICISSITUDE_REED)) {
            multiplier += state.stacks * 0.008F * 0.20F;
        }
        event.setAmount(event.getAmount() * multiplier);
        if (SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.VICISSITUDE_DREAM)) {
            event.setAmount(SignetEffects.applyArmorPenetration(
                    event.getEntity(), event.getSource(), event.getAmount(),
                    Math.min(0.95F, state.stacks * 0.006F)));
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || !hasVicissitude(player)
                || !SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.VICISSITUDE_RED_DUST)) return;
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        state.resolve = Math.min(6, state.resolve + 1);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        if (!hasVicissitude(player)) {
            clearModifiers(player);
            SignetStatusEffectSync.update(player,
                    ModEffects.VICISSITUDE_WARRIORS_RESOLVE.get(), 0);
            STATES.remove(player.getUUID());
            return;
        }
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.VICISSITUDE);
        long now = player.level().getGameTime();
        SignetUpgradeData.ExtensionCore core = SignetUpgradeData.getExtensionCore(
                player, SignetUpgradeData.Signet.VICISSITUDE);
        if (core != state.activeCore) {
            state.activeCore = core;
            state.resolve = 0;
            state.resolveTicks = 0;
            state.memory = 0;
            state.memoryTicks = 0;
            state.memoryBlocks = 0;
            state.maxPenalty = 0;
            state.nextAwakenTick = 0L;
        }
        if (core == SignetUpgradeData.ExtensionCore.VICISSITUDE_MEMORY) {
            if (++state.memoryTicks >= 200) {
                state.memoryTicks = 0;
                state.memory = Math.min(6, state.memory + 1);
            }
        }
        if (now % 20L == 0L) {
            int maximum = maximumStacks(player, state);
            if (core == SignetUpgradeData.ExtensionCore.VICISSITUDE_MEMORY && state.memory > 0) {
                state.stacks = maximum;
            } else {
                int gain = 1;
                if (core == SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE
                        && SignetUpgradeData.hasExtensionCoreSignet(
                        player, SignetUpgradeData.Skill.VICISSITUDE_PROSPERITY)) {
                    gain += state.resolve;
                }
                state.stacks = Math.min(maximum, state.stacks + gain);
            }
        }
        tickWarriorResolve(player, state, core);
        SignetStatusEffectSync.update(player,
                ModEffects.VICISSITUDE_WARRIORS_RESOLVE.get(),
                hasWarriorResolve(player) ? state.resolve : 0);
        updateModifiers(player, state.stacks);
    }

    private static void tickWarriorResolve(Player player, State state,
                                           SignetUpgradeData.ExtensionCore core) {
        if (core != SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE) return;
        int interval = core == SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE
                && state.stacks > 30 && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.VICISSITUDE_FAMILIAR) ? 160 : 200;
        if (++state.resolveTicks >= interval) {
            state.resolveTicks = 0;
            state.resolve = Math.min(6, state.resolve + 1);
        }
    }

    public static float corruptionCapacityMultiplier(Player player) {
        if (!hasVicissitude(player) || !SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.VICISSITUDE_WORDS)) return 1.0F;
        State state = STATES.get(player.getUUID());
        int stacks = state == null ? 0 : state.stacks;
        return 1.0F + stacks * 0.003F;
    }

    public static boolean hasWarriorResolve(Player player) {
        if (!hasVicissitude(player)) return false;
        SignetUpgradeData.ExtensionCore core = SignetUpgradeData.getExtensionCore(
                player, SignetUpgradeData.Signet.VICISSITUDE);
        return core == SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE;
    }

    public static int getWarriorResolve(Player player) {
        State state = player == null ? null : STATES.get(player.getUUID());
        return state == null ? 0 : state.resolve;
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        State state = STATES.get(player.getUUID());
        if (state != null) {
            state.resolve = 0;
            state.memory = 0;
            state.memoryBlocks = 0;
            state.maxPenalty = 0;
            state.memoryTicks = 0;
            state.nextAwakenTick = 0L;
            state.activeCore = null;
            state.resolveTicks = 0;
        }
        SignetStatusEffectSync.update(player,
                ModEffects.VICISSITUDE_WARRIORS_RESOLVE.get(), 0);
    }

    private static int maximumStacks(Player player, State state) {
        int maximum = 40;
        SignetUpgradeData.ExtensionCore core = SignetUpgradeData.getExtensionCore(
                player, SignetUpgradeData.Signet.VICISSITUDE);
        if (core == SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE
                && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.VICISSITUDE_FAMILIAR)) maximum = 60;
        if (core == SignetUpgradeData.ExtensionCore.VICISSITUDE_MEMORY && state.memory > 0
                && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.VICISSITUDE_AWAKEN)) maximum += 20;
        return Math.max(0, maximum - state.maxPenalty);
    }

    private static void updateModifiers(Player player, int stacks) {
        AttributeInstance movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (movement != null) {
            movement.removeModifier(MOVE_MOD);
            if (SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                    player, SignetUpgradeData.Skill.VICISSITUDE_TIME) && stacks > 0) {
                movement.addTransientModifier(new AttributeModifier(MOVE_MOD,
                        "vicissitude_time", stacks * 0.002D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MOD);
            if (SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                    player, SignetUpgradeData.Skill.VICISSITUDE_ROBE) && stacks > 0) {
                attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED_MOD,
                        "vicissitude_robe", stacks * 0.005D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }

    private static void clearModifiers(Player player) {
        AttributeInstance movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (movement != null) movement.removeModifier(MOVE_MOD);
        if (attackSpeed != null) attackSpeed.removeModifier(ATTACK_SPEED_MOD);
    }

    private static void damageArea(Player player, float damage) {
        UUID uuid = player.getUUID();
        if (!INTERNAL_DAMAGE.add(uuid)) return;
        try {
            for (LivingEntity target : player.level().getEntitiesOfClass(
                    LivingEntity.class, player.getBoundingBox().inflate(12.0D),
                    entity -> PlayerControlledAllies.isValidEnemy(player, entity))) {
                PlayerControlledAllies.hurt(player, target,
                        player.damageSources().playerAttack(player), damage);
            }
        } finally {
            INTERNAL_DAMAGE.remove(uuid);
        }
    }

    private static final class State {
        private int stacks;
        private int resolve;
        private int memory;
        private int memoryBlocks;
        private int maxPenalty;
        private int memoryTicks;
        private long nextAwakenTick;
        private SignetUpgradeData.ExtensionCore activeCore;
        private int resolveTicks;
    }
}
