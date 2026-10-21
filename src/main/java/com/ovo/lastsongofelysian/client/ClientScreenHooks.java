package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.screen.IVTVBookScreen;
import com.ovo.lastsongofelysian.screen.SignetOfEgoScreen;
import com.ovo.lastsongofelysian.screen.SilverPouchScreen;
import com.ovo.lastsongofelysian.screen.ShiJiBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientScreenHooks {

    public static void openShiJiScreen() {
        Minecraft.getInstance().setScreen(new ShiJiBookScreen());
    }

    public static void openSignetOfEgoScreen() {
        Minecraft.getInstance().setScreen(new SignetOfEgoScreen());
    }

    public static void openSilverPouchScreen(long balance) {
        Minecraft.getInstance().setScreen(new SilverPouchScreen(balance));
    }

    public static void openIVTVBookScreen() {
        Minecraft.getInstance().setScreen(new IVTVBookScreen());
    }
}
