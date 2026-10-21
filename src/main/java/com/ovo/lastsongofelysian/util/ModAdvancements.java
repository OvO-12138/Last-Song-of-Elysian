package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ModAdvancements {

    private ModAdvancements() {
    }

    public static void award(Player player, String path) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        Advancement advancement = server.getAdvancements().getAdvancement(
                new ResourceLocation(lastsongofelysian.MODID, path)
        );
        if (advancement == null) return;

        AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            serverPlayer.getAdvancements().award(advancement, criterion);
        }
    }
}
