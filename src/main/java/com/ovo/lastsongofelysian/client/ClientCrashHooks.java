package com.ovo.lastsongofelysian.client;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientCrashHooks {
    private ClientCrashHooks() {
    }

    public static void crashWithoutExplanation() {
        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(new RuntimeException(), ""));
    }
}
