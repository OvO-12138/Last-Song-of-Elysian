package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.item.SilverPouchItem;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEffects;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.WeaponEnergyAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
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
public final class ReverieSignetSystem {
    private static final UUID HEALTH_MOD = UUID.fromString("542f16a5-c1a8-454b-81f4-4e17b7220ba0");
    private static final String ROOT = "PlayerPersisted";
    private static final String NBT_SPENT = "LSEReverieSilverSpent";
    private static final String NBT_VIP = "LSEReverieVip";
    private static final String NBT_REROLLS = "LSEReverieRerolls";
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final Map<UUID, FadingVulnerability> DREAM_VULNERABILITY = new HashMap<>();
    private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();

    private ReverieSignetSystem() {
    }

    public static boolean hasReverie(Player player) {
        return player != null && CuriosApi.getCuriosHelper().findFirstCurio(
                player, stack -> stack.is(ModItems.SIGNET_OF_REVERIE.get())).isPresent();
    }

    public static void resetCoreRuntime(Player player) {
        if (player == null) return;
        CompoundTag persisted = data(player);
        persisted.remove(NBT_SPENT);
        persisted.remove(NBT_VIP);
        persisted.remove(NBT_REROLLS);
        State state = STATES.get(player.getUUID());
        if (state != null) {
            state.nextLucky = 0L;
            state.secretCooldown = 0L;
            state.nextDreamAttack = 0L;
            state.luckyCharges = 0;
        }
        DREAM_VULNERABILITY.entrySet().removeIf(
                entry -> player.getUUID().equals(entry.getValue().owner()));
        SignetStatusEffectSync.update(player, ModEffects.REVERIE_VIP_TREATMENT.get(), 0);
    }

    public static long pouchSilver(Player player) {
        long total = 0L;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.SILVER_POUCH.get())) total += SilverPouchItem.getBalance(stack);
        }
        return total;
    }

    private static int hundreds(Player player) {
        return (int) Math.min(100_000L, pouchSilver(player) / 100L);
    }

    public static float ordinaryBonusMultiplier(Player player) {
        if (SignetUpgradeData.getExtensionCore(player, SignetUpgradeData.Signet.REVERIE)
                != SignetUpgradeData.ExtensionCore.REVERIE_JOY) return 1.0F;
        long silver = pouchSilver(player);
        return silver >= 2000L ? 1.50F : silver >= 1000L ? 1.25F : 1.0F;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncoming(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()
                || !hasReverie(player) || event.getAmount() <= 0.0F) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        long now = player.level().getGameTime();
        state.regenBlockedUntil = now + 100L;
        state.combatUntil = now + 200L;
        if (state.luckyCharges > 0 && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.REVERIE_LUCKY)) {
            state.luckyCharges--;
            event.setAmount(0.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onOutgoing(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (PlayerControlledAllies.isElysianHero(event.getEntity())) return;
        FadingVulnerability vulnerability = DREAM_VULNERABILITY.get(event.getEntity().getUUID());
        if (vulnerability != null) {
            long now = event.getEntity().level().getGameTime();
            if (now >= vulnerability.endTick) {
                DREAM_VULNERABILITY.remove(event.getEntity().getUUID());
            } else {
                float remaining = (vulnerability.endTick - now)
                        / (float) (vulnerability.endTick - vulnerability.startTick);
                event.setAmount(event.getAmount() * (1.0F + 0.10F * remaining));
            }
        }
        if (!(event.getSource().getEntity() instanceof Player player)
                || !hasReverie(player)
                || !PlayerControlledAllies.isValidEnemy(player, event.getEntity())) return;
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        state.combatUntil = player.level().getGameTime() + 200L;
        int hundreds = hundreds(player);
        float ordinary = ordinaryBonusMultiplier(player);
        float damage = SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.REVERIE_CAT_VERSE)
                ? Math.min(1.0F, hundreds * 0.01F) * ordinary : 0.0F;
        int vip = getVip(player);
        float vipPerLayer = SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.REVERIE_ZERO) ? 0.30F : 0.20F;
        event.setAmount(event.getAmount() * (1.0F + damage) * (1.0F + vip * vipPerLayer));
        float penetration = SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.REVERIE_BLADE_TAIL)
                ? Math.min(0.80F, hundreds * 0.008F) * ordinary : 0.0F;
        if (state.luckyCharges > 0 && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.REVERIE_LUCKY)) {
            penetration += Math.min(0.80F, hundreds * 0.008F);
        }
        event.setAmount(SignetEffects.applyArmorPenetration(
                event.getEntity(), event.getSource(), event.getAmount(),
                Math.min(0.95F, penetration)));
    }

    public static void onEvasion(Player player) {
        if (hasReverie(player) && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.REVERIE_BOSS)) triggerSecret(player);
    }

    public static void onUltimate(Player player) {
        if (hasReverie(player) && SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.REVERIE_CAT_PUNCH)) triggerSecret(player);
    }

    private static void triggerSecret(Player player) {
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        long now = player.level().getGameTime();
        if (now < state.secretCooldown) return;
        state.secretCooldown = now + 300L;
        float multiplier = 0.50F + hundreds(player) * 0.10F;
        damageArea(player, 10.0D,
                Math.max(1.0F, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE))
                        * multiplier);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        if (!hasReverie(player)) {
            clearHealth(player);
            SignetStatusEffectSync.update(player, ModEffects.REVERIE_VIP_TREATMENT.get(), 0);
            STATES.remove(player.getUUID());
            return;
        }
        SignetUpgradeData.synchronizeOwnedLevels(player, SignetUpgradeData.Signet.REVERIE);
        SignetStatusEffectSync.update(player, ModEffects.REVERIE_VIP_TREATMENT.get(),
                hasVipTreatment(player) ? getVip(player) : 0);
        long now = player.level().getGameTime();
        int hundreds = hundreds(player);
        float ordinary = ordinaryBonusMultiplier(player);
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH_MOD);
            float healthBonus = SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                    player, SignetUpgradeData.Skill.REVERIE_STREET_JOY)
                    ? hundreds * ordinary : 0.0F;
            if (healthBonus > 0.0F) health.addTransientModifier(new AttributeModifier(
                    HEALTH_MOD, "reverie_street_joy", healthBonus,
                    AttributeModifier.Operation.ADDITION));
        }
        if (now % 20L == 0L && SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.REVERIE_PC)
                && now >= state.regenBlockedUntil && hundreds > 0) {
            player.heal(hundreds * ordinary);
            state.energyRemainder += hundreds * 0.1F * ordinary;
            int whole = (int) state.energyRemainder;
            if (whole > 0) {
                state.energyRemainder -= whole;
                WeaponEnergyAccess.add(player, whole);
            }
        }
        if (SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.REVERIE_LUCKY)
                && now >= state.nextLucky) {
            state.nextLucky = now + 200L;
            state.luckyCharges = 2 + (int) Math.min(1000L, pouchSilver(player) / 1000L);
        }
        if (SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.REVERIE_DREAM_TRUE)
                && getVip(player) > 0 && state.combatUntil >= now
                && now >= state.nextDreamAttack) {
            int vip = getVip(player);
            int interval = vip >= 3 ? 100 : vip == 2 ? 200 : 300;
            state.nextDreamAttack = now + interval;
            damageArea(player, 12.0D,
                    Math.max(1.0F, (float) player.getAttributeValue(
                            Attributes.ATTACK_DAMAGE)) * 0.80F, true);
        }
    }

    public static int energyCapacityBonus(Player player) {
        if (!hasReverie(player) || !SignetUpgradeData.isExtensionSecondarySkillUnlocked(
                player, SignetUpgradeData.Skill.REVERIE_STREET_JOY)) return 0;
        return Math.round(hundreds(player) * 0.1F * ordinaryBonusMultiplier(player));
    }

    public static boolean isNextRerollFree(Player player) {
        if (!hasDreamCore(player)) return false;
        return Math.floorMod(data(player).getInt(NBT_REROLLS), 10) < 2;
    }

    public static void recordSuccessfulReroll(Player player) {
        if (!hasDreamCore(player)) return;
        CompoundTag data = data(player);
        int index = Math.floorMod(data.getInt(NBT_REROLLS), 10);
        data.putInt(NBT_REROLLS, (index + 1) % 10);
    }

    private static boolean hasDreamCore(Player player) {
        return hasReverie(player)
                && SignetUpgradeData.getExtensionCore(player, SignetUpgradeData.Signet.REVERIE)
                == SignetUpgradeData.ExtensionCore.REVERIE_DREAM;
    }

    public static void onSilverSpent(Player player, int amount) {
        if (!hasReverie(player) || amount <= 0
                || SignetUpgradeData.getExtensionCore(player, SignetUpgradeData.Signet.REVERIE)
                != SignetUpgradeData.ExtensionCore.REVERIE_DREAM) return;
        CompoundTag data = data(player);
        int spent = data.getInt(NBT_SPENT) + amount;
        int maxVip = SignetUpgradeData.hasExtensionCoreSignet(
                player, SignetUpgradeData.Skill.REVERIE_SNOW) ? 5 : 4;
        int vip = Math.min(maxVip, spent / 2000);
        data.putInt(NBT_SPENT, spent);
        data.putInt(NBT_VIP, Math.max(data.getInt(NBT_VIP), vip));
    }

    public static int getVip(Player player) {
        return data(player).getInt(NBT_VIP);
    }

    public static boolean hasVipTreatment(Player player) {
        return hasReverie(player) && SignetUpgradeData.getExtensionCore(
                player, SignetUpgradeData.Signet.REVERIE)
                == SignetUpgradeData.ExtensionCore.REVERIE_DREAM;
    }

    private static CompoundTag data(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(ROOT)) root.put(ROOT, new CompoundTag());
        return root.getCompound(ROOT);
    }

    private static void damageArea(Player player, double radius, float damage) {
        damageArea(player, radius, damage, false);
    }

    private static void damageArea(Player player, double radius, float damage,
                                   boolean applyDreamVulnerability) {
        UUID uuid = player.getUUID();
        if (!INTERNAL_DAMAGE.add(uuid)) return;
        try {
            for (LivingEntity target : player.level().getEntitiesOfClass(
                    LivingEntity.class, player.getBoundingBox().inflate(radius),
                    entity -> PlayerControlledAllies.isValidEnemy(player, entity))) {
                PlayerControlledAllies.hurt(player, target,
                        player.damageSources().playerAttack(player), damage);
                if (applyDreamVulnerability) {
                    long now = player.level().getGameTime();
                    DREAM_VULNERABILITY.put(target.getUUID(),
                            new FadingVulnerability(player.getUUID(), now, now + 100L));
                }
            }
        } finally {
            INTERNAL_DAMAGE.remove(uuid);
        }
    }

    private static void clearHealth(Player player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.removeModifier(HEALTH_MOD);
    }

    private static final class State {
        private long regenBlockedUntil;
        private long combatUntil;
        private long nextLucky;
        private long secretCooldown;
        private long nextDreamAttack;
        private int luckyCharges;
        private float energyRemainder;
    }

    private record FadingVulnerability(UUID owner, long startTick, long endTick) {
    }
}
