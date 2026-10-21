package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ElysianHeroExplosionProtection {
    private static final ThreadLocal<Integer> ACTIVE_DEPTH = ThreadLocal.withInitial(() -> 0);

    private ElysianHeroExplosionProtection() {
    }

    public static void run(Runnable explosion) {
        ACTIVE_DEPTH.set(ACTIVE_DEPTH.get() + 1);
        try {
            explosion.run();
        } finally {
            int remaining = ACTIVE_DEPTH.get() - 1;
            if (remaining == 0) ACTIVE_DEPTH.remove();
            else ACTIVE_DEPTH.set(remaining);
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (ACTIVE_DEPTH.get() > 0) {
            event.getAffectedEntities().removeIf(PlayerControlledAllies::isElysianHero);
        }
    }
}
