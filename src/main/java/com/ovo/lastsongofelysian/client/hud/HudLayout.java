package com.ovo.lastsongofelysian.client.hud;

import com.ovo.lastsongofelysian.config.ClientHudConfig;
import net.minecraft.client.gui.GuiGraphics;

public final class HudLayout {
    private HudLayout() {
    }

    public static boolean begin(GuiGraphics graphics, int screenWidth, int screenHeight,
                                int elementWidth, int elementHeight,
                                ClientHudConfig.HudElement settings) {
        if (!settings.enabled().get()) return false;
        double scale = settings.scale().get();
        int scaledWidth = (int) Math.ceil(elementWidth * scale);
        int scaledHeight = (int) Math.ceil(elementHeight * scale);
        int configuredX = settings.x().get();
        int configuredY = settings.y().get();
        int x = configuredX >= 0
                ? configuredX
                : screenWidth - scaledWidth + configuredX;
        int y = configuredY >= 0
                ? configuredY
                : screenHeight - scaledHeight + configuredY;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0D);
        graphics.pose().scale((float) scale, (float) scale, 1.0F);
        return true;
    }

    public static void end(GuiGraphics graphics) {
        graphics.pose().popPose();
    }
}
