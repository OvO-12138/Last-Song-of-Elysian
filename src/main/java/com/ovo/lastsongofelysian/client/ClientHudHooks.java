package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.network.CombatHudSyncPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientHudHooks {
    private ClientHudHooks() {
    }

    public static void handleCombatHud(CombatHudSyncPacket packet) {
        CombatHudData.update(
                packet.bloodActive(), packet.bloodCharge(),
                packet.vipActive(), packet.vip(),
                packet.resolveActive(), packet.resolve(),
                packet.evasionActive(), packet.evasionCooldown(),
                packet.evasionCooldownMaximum(), packet.evasionUses(),
                packet.evasionMaximumUses());
    }
}
