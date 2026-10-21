package com.ovo.lastsongofelysian.event;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

final class SignetStatusEffectSync {
    private SignetStatusEffectSync() {
    }

    static void update(Player player, MobEffect effect, int stacks) {
        MobEffectInstance current = player.getEffect(effect);
        if (stacks <= 0) {
            if (current != null) player.removeEffect(effect);
            return;
        }

        int amplifier = stacks - 1;
        if (current != null && current.getAmplifier() == amplifier
                && current.getDuration() == -1 && !current.isVisible()
                && current.showIcon()) return;
        if (current != null) player.removeEffect(effect);
        player.addEffect(new MobEffectInstance(effect, -1, amplifier, false, false, true));
    }
}
