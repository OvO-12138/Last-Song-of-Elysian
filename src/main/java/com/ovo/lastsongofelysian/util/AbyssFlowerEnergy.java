package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.event.GoldSignetSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AbyssFlowerEnergy {
    public static final String NBT_ENERGY = "AbyssFlowerEnergy";

    private AbyssFlowerEnergy() {
    }

    public static int get(ItemStack stack) {
        return stack.is(ModItems.ABYSS_FLOWER.get()) && stack.hasTag()
                ? Mth.clamp(stack.getTag().getInt(NBT_ENERGY), 0, 4000) : 0;
    }

    public static void set(Player player, ItemStack stack, int energy) {
        if (!stack.is(ModItems.ABYSS_FLOWER.get())) return;
        int previous = get(stack);
        int next = Mth.clamp(energy, 0, GoldSignetSystem.getMaximumEnergy(player));
        stack.getOrCreateTag().putInt(NBT_ENERGY, next);
        GoldSignetSystem.onEnergyChanged(player, previous, next);
        player.getInventory().setChanged();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }

    public static void add(Player player, ItemStack stack, int amount) {
        if (amount <= 0) return;
        set(player, stack, get(stack) + WeaponEnergyGain.apply(player, amount));
    }

    public static void addFromAttack(Player player, ItemStack stack, int amount) {
        if (amount <= 0) return;
        set(player, stack, get(stack) + WeaponEnergyGain.applyAttack(player, amount));
    }

    public static ItemStack findHeld(Player player) {
        if (player.getMainHandItem().is(ModItems.ABYSS_FLOWER.get())) return player.getMainHandItem();
        if (player.getOffhandItem().is(ModItems.ABYSS_FLOWER.get())) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    public static ItemStack find(Player player) {
        ItemStack held = findHeld(player);
        if (!held.isEmpty()) return held;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.ABYSS_FLOWER.get())) return stack;
        }
        return ItemStack.EMPTY;
    }
}
