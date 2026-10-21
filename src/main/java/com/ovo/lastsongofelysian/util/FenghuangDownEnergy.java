package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.event.GoldSignetSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class FenghuangDownEnergy {
    public static final int MAX_ENERGY = 300;
    public static final String NBT_ENERGY = "FenghuangDownEnergy";

    private FenghuangDownEnergy() {
    }

    public static int getEnergy(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ModItems.FENGHUANG_DOWN.get())) {
            return 0;
        }
        return Mth.clamp(stack.getOrCreateTag().getInt(NBT_ENERGY), 0, 4000);
    }

    public static void setEnergy(Player player, ItemStack stack, int energy) {
        if (stack.isEmpty() || !stack.is(ModItems.FENGHUANG_DOWN.get())) {
            return;
        }
        int previous = getEnergy(stack);
        int next = Mth.clamp(energy, 0, GoldSignetSystem.getMaximumEnergy(player));
        stack.getOrCreateTag().putInt(NBT_ENERGY, next);
        GoldSignetSystem.onEnergyChanged(player, previous, next);
        sync(player);
    }

    public static int addEnergy(Player player, ItemStack stack, int amount) {
        if (amount <= 0) {
            return getEnergy(stack);
        }
        int adjustedAmount = WeaponEnergyGain.apply(player, amount);
        int next = Math.min(GoldSignetSystem.getMaximumEnergy(player), getEnergy(stack) + adjustedAmount);
        setEnergy(player, stack, next);
        return next;
    }

    public static boolean consumeEnergy(Player player, ItemStack stack, int amount) {
        int current = getEnergy(stack);
        if (current < amount) {
            player.displayClientMessage(
                    ModText.literal("羽渡尘能量不足：" + current + "/" + amount)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }
        setEnergy(player, stack, current - amount);
        return true;
    }

    public static ItemStack find(Player player) {
        ItemStack held = findHeld(player);
        if (!held.isEmpty()) {
            return held;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.FENGHUANG_DOWN.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack findHeld(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(ModItems.FENGHUANG_DOWN.get())) {
            return mainHand;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.FENGHUANG_DOWN.get())) {
            return offhand;
        }
        return ItemStack.EMPTY;
    }

    private static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getInventory().setChanged();
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }
}
