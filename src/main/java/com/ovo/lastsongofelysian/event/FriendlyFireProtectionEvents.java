package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FriendlyFireProtectionEvents {
    private FriendlyFireProtectionEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDamage(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker == null) attacker = event.getSource().getDirectEntity();
        Player player = PlayerControlledAllies.findController(attacker);
        if (player == null
                || !PlayerControlledAllies.isProtectedAlly(player, event.getEntity())) return;
        event.setAmount(0.0F);
        event.setCanceled(true);
    }
}
