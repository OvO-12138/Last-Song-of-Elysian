package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.network.CombatHudSyncPacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CombatHudSyncEvents {
    private CombatHudSyncEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 5 != 0) return;

        boolean bloodActive = DecimationSignetSystem.isBloodReleaseActive(player);
        boolean vipActive = ReverieSignetSystem.hasVipTreatment(player);
        boolean resolveActive = VicissitudeSignetSystem.hasWarriorResolve(player);
        boolean evasionActive = SignetEffects.hasSetsunaEvasion(player);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CombatHudSyncPacket(
                        bloodActive, DecimationSignetSystem.getBloodReleaseCharge(player),
                        vipActive, ReverieSignetSystem.getVip(player),
                        resolveActive, VicissitudeSignetSystem.getWarriorResolve(player),
                        evasionActive, SignetEffects.getSetsunaEvasionCooldown(player),
                        SignetEffects.getSetsunaEvasionCooldownMaximum(player),
                        SignetEffects.getSetsunaEvasionUses(player),
                        SignetEffects.getSetsunaEvasionMaximumUses(player)));
    }
}
