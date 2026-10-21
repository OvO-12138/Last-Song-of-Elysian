package com.ovo.lastsongofelysian.client.overlay;

import com.ovo.lastsongofelysian.client.CombatHudData;
import com.ovo.lastsongofelysian.client.hud.HudLayout;
import com.ovo.lastsongofelysian.config.ClientHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class CombatStatusOverlay implements IGuiOverlay {
    private static final int BAR_WIDTH = 126;
    private static final int BAR_HEIGHT = 18;
    private static final int CARD_WIDTH = 126;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick,
                       int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui
                || minecraft.options.renderDebug || minecraft.screen != null) return;

        renderBloodRelease(graphics, minecraft, screenWidth, screenHeight);
        renderEvasion(graphics, minecraft, screenWidth, screenHeight);
    }

    private static void renderBloodRelease(GuiGraphics graphics, Minecraft minecraft,
                                           int screenWidth, int screenHeight) {
        if (!CombatHudData.bloodReleaseActive
                || !HudLayout.begin(graphics, screenWidth, screenHeight, BAR_WIDTH, BAR_HEIGHT,
                ClientHudConfig.BLOOD_RELEASE)) return;
        int charge = Mth.clamp(CombatHudData.bloodReleaseCharge, 0, 1000);
        int fill = Math.round(106.0F * charge / 1000.0F);
        graphics.fill(2, 2, BAR_WIDTH + 2, BAR_HEIGHT + 2, 0x99000000);
        graphics.fillGradient(0, 0, BAR_WIDTH, BAR_HEIGHT, 0xE532090B, 0xE50B0507);
        graphics.fill(0, 0, 3, BAR_HEIGHT, 0xFFFF342E);
        graphics.fill(10, 11, 116, 15, 0xFF26090C);
        if (fill > 0) {
            graphics.fillGradient(10, 11, 10 + fill, 15, 0xFFFF7A3D, 0xFFB30019);
            graphics.fill(10 + fill - 1, 10, 10 + fill, 16, 0xFFFFD0A1);
        }
        Component label = Component.translatable("hud.lastsongofelysian.blood_release");
        String value = charge + " / 1000";
        graphics.drawString(minecraft.font, label, 8, 2, 0xFFFFB0A5, false);
        graphics.drawString(minecraft.font, value,
                BAR_WIDTH - minecraft.font.width(value) - 7, 2, 0xFFFFE2D5, true);
        HudLayout.end(graphics);
    }

    private static void renderEvasion(GuiGraphics graphics, Minecraft minecraft,
                                      int screenWidth, int screenHeight) {
        if (!CombatHudData.evasionActive
                || !HudLayout.begin(graphics, screenWidth, screenHeight, CARD_WIDTH, 22,
                ClientHudConfig.EVASION)) return;
        int remaining = CombatHudData.evasionCooldown;
        int maximum = Math.max(1, CombatHudData.evasionCooldownMaximum);
        int readyFill = remaining <= 0 ? 104 : Math.round(104.0F * (maximum - remaining) / maximum);
        graphics.fill(-2, -2, CARD_WIDTH + 2, 24, 0x4A000000);
        graphics.fillGradient(0, 0, CARD_WIDTH, 22, 0xD00B2632, 0xD00B101C);
        graphics.fill(0, 0, 3, 22, 0xFF79E8FF);
        graphics.fill(10, 16, 114, 19, 0xFF102630);
        graphics.fill(10, 16, 10 + Mth.clamp(readyFill, 0, 104), 19, 0xFF72DCF4);
        Component label = Component.translatable("hud.lastsongofelysian.evasion");
        String uses = CombatHudData.evasionUses + " / " + CombatHudData.evasionMaximumUses;
        String cooldown = remaining <= 0 ? "" : String.format("%.1fs", remaining / 20.0F);
        graphics.drawString(minecraft.font, label, 9, 3, 0xFFE6FAFF, false);
        graphics.drawString(minecraft.font, uses,
                CARD_WIDTH - minecraft.font.width(uses) - 8, 3, 0xFF92EDFF, true);
        if (!cooldown.isEmpty()) {
            graphics.drawString(minecraft.font, cooldown,
                    (CARD_WIDTH - minecraft.font.width(cooldown)) / 2, 3, 0xFFFFFFFF, true);
        }
        HudLayout.end(graphics);
    }
}
