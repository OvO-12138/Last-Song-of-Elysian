package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

public final class EgoSignetAmplification {
    private EgoSignetAmplification() {
    }

    public static boolean isEnabled(Player player) {
        if (player == null) return false;
        return player.getMainHandItem().is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())
                || isEquipped(player, ModItems.SIGNET_OF_EGO.get());
    }

    public static boolean isEnabledFor(Player player, Item signet) {
        return isEnabled(player) && isEquipped(player, signet);
    }

    private static boolean isEquipped(Player player, Item item) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(item))
                .isPresent();
    }
}
