package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WeaponEnergyAccess {
    private WeaponEnergyAccess() {
    }

    public static ItemStack findHeldWeapon(Player player) {
        ItemStack main = player.getMainHandItem();
        if (isEnergyWeapon(main)) return main;
        ItemStack off = player.getOffhandItem();
        return isEnergyWeapon(off) ? off : ItemStack.EMPTY;
    }

    public static boolean isEnergyWeapon(ItemStack stack) {
        return stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())
                || stack.is(ModItems.ABYSS_FLOWER.get())
                || stack.is(ModItems.EDEN_STAR.get())
                || stack.is(ModItems.SKYFIRE_GREATSWORD.get())
                || stack.is(ModItems.FENGHUANG_DOWN.get());
    }

    public static int get(ItemStack stack) {
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) {
            return FlawlessWeaponEnergy.getEnergy(stack);
        }
        if (stack.is(ModItems.ABYSS_FLOWER.get())) return AbyssFlowerEnergy.get(stack);
        if (stack.is(ModItems.EDEN_STAR.get())) return EdenStarEnergy.get(stack);
        if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) return SkyfireGreatswordEnergy.get(stack);
        if (stack.is(ModItems.FENGHUANG_DOWN.get())) return FenghuangDownEnergy.getEnergy(stack);
        return 0;
    }

    public static void add(Player player, int amount) {
        if (player == null || amount <= 0) return;
        ItemStack stack = findHeldWeapon(player);
        if (stack.isEmpty()) return;
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) {
            FlawlessWeaponEnergy.addEnergy(player, stack, amount);
        } else if (stack.is(ModItems.ABYSS_FLOWER.get())) {
            AbyssFlowerEnergy.add(player, stack, amount);
        } else if (stack.is(ModItems.EDEN_STAR.get())) {
            EdenStarEnergy.add(player, stack, amount);
        } else if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) {
            SkyfireGreatswordEnergy.add(player, stack, amount);
        } else if (stack.is(ModItems.FENGHUANG_DOWN.get())) {
            FenghuangDownEnergy.addEnergy(player, stack, amount);
        }
    }
}
