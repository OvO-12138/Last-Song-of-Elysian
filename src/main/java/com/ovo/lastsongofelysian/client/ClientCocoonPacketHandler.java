package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.network.CocoonStageSyncPacket;

public final class ClientCocoonPacketHandler {

    private ClientCocoonPacketHandler() {
    }

    public static void handle(CocoonStageSyncPacket message) {
        ClientCocoonData.setCoreStage(message.getStage());
        ClientCocoonData.setAcquiredCoreMask(message.getAcquiredCoreMask());
        ClientCocoonData.setReversed(message.isReversed());
        ClientCocoonData.setTrialsUnlocked(message.areTrialsUnlocked());
        ClientCocoonData.setCompletedTrialMask(message.getCompletedTrialMask());
        ClientCocoonData.setThunderStrikes(message.getThunderStrikes());
        ClientCocoonData.setThunderKills(message.getThunderKills());
        ClientCocoonData.setEarthExplosions(message.getEarthExplosions());
    }
}
