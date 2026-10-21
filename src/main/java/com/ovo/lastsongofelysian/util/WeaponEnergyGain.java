package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.event.SignetEffects;
import com.ovo.lastsongofelysian.event.GoldSignetSystem;
import com.ovo.lastsongofelysian.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public final class WeaponEnergyGain {
    private WeaponEnergyGain() {
    }

    public static int apply(Player player, int amount) {
        if (amount <= 0 || CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(ModItems.SIGNET_OF_DISCIPLINE.get()))
                .isEmpty()) {
            return amount;
        }
        float bonus = SignetEffects.getScaledSignetValue(
                player,
                SignetUpgradeData.Skill.DISCIPLINE_THREE,
                SignetEffects.getPreceptCount(player) * 0.003F
        );
        return Math.max(amount, Math.round(amount * (1.0F + bonus)));
    }

    public static int applyAttack(Player player, int amount) {
        int adjusted = apply(player, amount);
        return Math.max(adjusted,
                Math.round(adjusted * GoldSignetSystem.attackRecoveryMultiplier(player)));
    }
}
