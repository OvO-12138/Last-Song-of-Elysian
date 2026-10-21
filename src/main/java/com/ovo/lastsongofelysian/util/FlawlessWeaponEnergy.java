package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.event.SignetEffects;
import com.ovo.lastsongofelysian.event.GoldSignetSystem;
import com.ovo.lastsongofelysian.item.FlawlessBenedictionLegacyItem;
import com.ovo.lastsongofelysian.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(
        modid = lastsongofelysian.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class FlawlessWeaponEnergy {

    public static final int MAX_ENERGY = 300;
    public static final String NBT_ENERGY = "WeaponEnergy";

    private static final float DAMAGE_PER_ENERGY = 0.005F;
    private static final float MAX_DAMAGE_BONUS = 0.65F;

    private FlawlessWeaponEnergy() {
    }

    public static int getEnergy(ItemStack weapon) {
        if (
                weapon.isEmpty() ||
                !weapon.is(
                        ModItems.FLAWLESS_BENEDICTION_LEGACY.get()
                )
        ) {
            return 0;
        }

        return Mth.clamp(
                weapon.getOrCreateTag()
                        .getInt(NBT_ENERGY),
                0,
                4000
        );
    }

    public static void setEnergy(
            Player player,
            ItemStack weapon,
            int energy
    ) {
        if (
                weapon.isEmpty() ||
                !weapon.is(
                        ModItems.FLAWLESS_BENEDICTION_LEGACY.get()
                )
        ) {
            return;
        }

        int previous = getEnergy(weapon);
        int maximum = GoldSignetSystem.getMaximumEnergy(player);
        int next = Mth.clamp(energy, 0, maximum);
        weapon.getOrCreateTag().putInt(
                NBT_ENERGY,
                next
        );

        GoldSignetSystem.onEnergyChanged(player, previous, next);

        sync(player);
    }

    public static int addEnergy(
            Player player,
            ItemStack weapon,
            int amount
    ) {
        if (amount <= 0) {
            return getEnergy(weapon);
        }

        int adjustedAmount = WeaponEnergyGain.apply(player, amount);

        int next = Math.min(
                GoldSignetSystem.getMaximumEnergy(player),
                getEnergy(weapon) + adjustedAmount
        );

        setEnergy(player, weapon, next);

        return next;
    }

    public static int addEnergyFromAttack(Player player, ItemStack weapon, int amount) {
        if (amount <= 0) return getEnergy(weapon);
        int next = Math.min(GoldSignetSystem.getMaximumEnergy(player),
                getEnergy(weapon) + WeaponEnergyGain.applyAttack(player, amount));
        setEnergy(player, weapon, next);
        return next;
    }

    public static boolean consumeEnergy(
            Player player,
            ItemStack weapon,
            int amount
    ) {

        int current = getEnergy(weapon);

        if (current < amount) {
            player.displayClientMessage(
                    ModText.literal(
                            "武器能量不足："
                                    + current
                                    + "/"
                                    + amount
                    ).withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        setEnergy(
                player,
                weapon,
                current - amount
        );

        return true;
    }

    public static boolean hasGold(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(
                        player,
                        stack -> stack.is(
                                ModItems.SIGNET_OF_GOLD.get()
                        )
                )
                .isPresent();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGoldEnergyDamage(
            LivingHurtEvent event
    ) {
        if (
                event.getEntity().level().isClientSide() ||
                !(event.getSource().getEntity()
                        instanceof Player player) ||
                !hasGold(player)
        ) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        int energy;
        if (weapon.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) {
            energy = getEnergy(weapon);
        } else if (!AbyssFlowerEnergy.findHeld(player).isEmpty()) {
            energy = AbyssFlowerEnergy.get(AbyssFlowerEnergy.findHeld(player));
        } else if (!EdenStarEnergy.findHeld(player).isEmpty()) {
            energy = EdenStarEnergy.get(EdenStarEnergy.findHeld(player));
        } else if (!SkyfireGreatswordEnergy.findHeld(player).isEmpty()) {
            energy = SkyfireGreatswordEnergy.get(SkyfireGreatswordEnergy.findHeld(player));
        } else {
            ItemStack fenghuangDown = FenghuangDownEnergy.findHeld(player);
            if (fenghuangDown.isEmpty()) {
                return;
            }
            energy = FenghuangDownEnergy.getEnergy(fenghuangDown);
        }

        float bonus = Math.min(
                SignetEffects.getScaledSignetValue(
                        player,
                        SignetUpgradeData.Skill.GOLD_PARADISE,
                        MAX_DAMAGE_BONUS
                ),
                SignetEffects.getScaledSignetValue(
                        player,
                        SignetUpgradeData.Skill.GOLD_PARADISE,
                        energy * DAMAGE_PER_ENERGY
                )
        );

        if (bonus > 0.0F) {
            event.setAmount(
                    event.getAmount()
                            * (1.0F + bonus)
            );
        }
    }

    private static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getInventory().setChanged();
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }
}
