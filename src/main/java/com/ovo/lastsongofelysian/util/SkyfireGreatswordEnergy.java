package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.event.GoldSignetSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SkyfireGreatswordEnergy {
    public static final int MAX_ENERGY = 300;
    private static final String NBT_ENERGY = "SkyfireGreatswordEnergy";
    private static final String NBT_HEALTH_LOSS_REMAINDER = "SkyfireHealthLossRemainder";

    private SkyfireGreatswordEnergy() {
    }

    public static int get(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ModItems.SKYFIRE_GREATSWORD.get())) return 0;
        return Mth.clamp(stack.getOrCreateTag().getInt(NBT_ENERGY), 0, 4000);
    }

    public static void set(Player player, ItemStack stack, int energy) {
        if (stack.isEmpty() || !stack.is(ModItems.SKYFIRE_GREATSWORD.get())) return;
        int previous = get(stack);
        int next = Mth.clamp(energy, 0, GoldSignetSystem.getMaximumEnergy(player));
        stack.getOrCreateTag().putInt(NBT_ENERGY, next);
        GoldSignetSystem.onEnergyChanged(player, previous, next);
        sync(player);
    }

    public static int add(Player player, ItemStack stack, int amount) {
        if (amount <= 0) return get(stack);
        int adjusted = WeaponEnergyGain.apply(player, amount);
        int next = Math.min(GoldSignetSystem.getMaximumEnergy(player), get(stack) + adjusted);
        set(player, stack, next);
        return next;
    }

    public static int addFromAttack(Player player, ItemStack stack, int amount) {
        if (amount <= 0) return get(stack);
        int next = Math.min(GoldSignetSystem.getMaximumEnergy(player),
                get(stack) + WeaponEnergyGain.applyAttack(player, amount));
        set(player, stack, next);
        return next;
    }

    public static void addFromHealthLoss(Player player, ItemStack stack, float healthLost) {
        if (healthLost <= 0.0F || stack.isEmpty()) return;
        float accumulated = stack.getOrCreateTag().getFloat(NBT_HEALTH_LOSS_REMAINDER) + healthLost;
        int wholeHealth = Mth.floor(accumulated);
        stack.getOrCreateTag().putFloat(NBT_HEALTH_LOSS_REMAINDER, accumulated - wholeHealth);
        if (wholeHealth > 0) {
            add(player, stack, (int) Math.min(MAX_ENERGY, wholeHealth * 3L));
        }
    }

    public static boolean consume(Player player, ItemStack stack, int amount) {
        int current = get(stack);
        if (current < amount) {
            player.displayClientMessage(
                    Component.translatable("message.lastsongofelysian.skyfire_greatsword.energy", current, amount)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }
        set(player, stack, current - amount);
        return true;
    }

    public static ItemStack find(Player player) {
        ItemStack held = findHeld(player);
        if (!held.isEmpty()) return held;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack findHeld(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(ModItems.SKYFIRE_GREATSWORD.get())) return mainHand;
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.SKYFIRE_GREATSWORD.get())) return offhand;
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
