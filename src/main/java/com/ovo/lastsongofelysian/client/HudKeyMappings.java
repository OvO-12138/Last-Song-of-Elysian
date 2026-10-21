package com.ovo.lastsongofelysian.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ovo.lastsongofelysian.client.screen.HudEditorScreen;
import com.ovo.lastsongofelysian.network.BloodReleaseTogglePacket;
import com.ovo.lastsongofelysian.network.BodhiPassiveDamageTogglePacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public final class HudKeyMappings {
    public static final KeyMapping TOGGLE_BLOOD_RELEASE = new KeyMapping(
            "key.lastsongofelysian.toggle_blood_release",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.lastsongofelysian");
    public static final KeyMapping TOGGLE_BODHI_PASSIVE_DAMAGE = new KeyMapping(
            "key.lastsongofelysian.toggle_bodhi_passive_damage",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.lastsongofelysian");
    public static final KeyMapping OPEN_HUD_EDITOR = new KeyMapping(
            "key.lastsongofelysian.open_hud_editor",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.lastsongofelysian");

    private HudKeyMappings() {
    }

    public static final class ForgeEvents {
        private ForgeEvents() {
        }

        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft minecraft = Minecraft.getInstance();
            while (TOGGLE_BLOOD_RELEASE.consumeClick()) {
                if (minecraft.player != null) {
                    ModNetwork.CHANNEL.sendToServer(new BloodReleaseTogglePacket());
                }
            }
            while (TOGGLE_BODHI_PASSIVE_DAMAGE.consumeClick()) {
                if (minecraft.player != null) {
                    ModNetwork.CHANNEL.sendToServer(new BodhiPassiveDamageTogglePacket());
                }
            }
            while (OPEN_HUD_EDITOR.consumeClick()) {
                if (minecraft.screen == null && minecraft.player != null) {
                    minecraft.setScreen(new HudEditorScreen());
                }
            }
        }
    }
}
