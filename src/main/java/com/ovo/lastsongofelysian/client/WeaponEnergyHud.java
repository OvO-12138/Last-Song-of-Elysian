package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.AbyssFlowerEnergy;
import com.ovo.lastsongofelysian.util.FenghuangDownEnergy;
import com.ovo.lastsongofelysian.util.FlawlessWeaponEnergy;
import com.ovo.lastsongofelysian.util.SkyfireGreatswordEnergy;
import com.ovo.lastsongofelysian.util.EdenStarEnergy;
import com.ovo.lastsongofelysian.event.GoldSignetSystem;
import com.ovo.lastsongofelysian.client.hud.HudLayout;
import com.ovo.lastsongofelysian.config.ClientHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WeaponEnergyHud {
    private static final int FRAME_WIDTH = 126;
    private static final int FRAME_HEIGHT = 14;
    private static final int TRACK_X = 16;
    private static final int TRACK_Y = 3;
    private static final int TRACK_WIDTH = 73;
    private static final int TRACK_HEIGHT = 8;

    private WeaponEnergyHud() {
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.options.renderDebug
                || minecraft.screen != null) return;
        EnergyView view = view(minecraft.player);
        if (view == null) return;

        GuiGraphics graphics = event.getGuiGraphics();
        if (!HudLayout.begin(graphics, graphics.guiWidth(), graphics.guiHeight(),
                FRAME_WIDTH + 2, FRAME_HEIGHT + 2, ClientHudConfig.WEAPON_ENERGY)) return;
        int x = 0;
        int y = 0;
        int fill = Mth.floor(TRACK_WIDTH * view.energy / (float) view.maximum);

        graphics.fill(x + 2, y + 2, x + FRAME_WIDTH + 2, y + FRAME_HEIGHT + 2, 0x99000000);
        graphics.fillGradient(x, y, x + FRAME_WIDTH, y + FRAME_HEIGHT, 0xE51B1525, 0xE50B0911);
        graphics.fill(x, y, x + 3, y + FRAME_HEIGHT, view.accent);
        graphics.fill(x + 5, y + 1, x + FRAME_WIDTH - 2, y + 2, 0x805D5368);
        graphics.fill(x + TRACK_X, y + TRACK_Y, x + TRACK_X + TRACK_WIDTH,
                y + TRACK_Y + TRACK_HEIGHT, 0xFF17111E);
        if (fill > 0) {
            graphics.fillGradient(x + TRACK_X, y + TRACK_Y,
                    x + TRACK_X + fill, y + TRACK_Y + TRACK_HEIGHT,
                    view.highlight, view.accent);
        }
        for (int i = 1; i < 10; i++) {
            int marker = x + TRACK_X + TRACK_WIDTH * i / 10;
            graphics.fill(marker, y + TRACK_Y, marker + 1, y + TRACK_Y + TRACK_HEIGHT, 0x663F3448);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(x + 1.0F, y + 1.0F, 0.0F);
        graphics.pose().scale(0.75F, 0.75F, 1.0F);
        graphics.renderItem(view.stack, 0, 0);
        graphics.pose().popPose();
        String amount = view.energy + " / " + view.maximum;
        graphics.drawString(minecraft.font, amount,
                x + FRAME_WIDTH - minecraft.font.width(amount) - 5, y + 3, view.highlight, true);
        HudLayout.end(graphics);
    }

    private static EnergyView view(Player player) {
        EnergyView main = view(player, player.getMainHandItem());
        return main != null ? main : view(player, player.getOffhandItem());
    }

    private static EnergyView view(Player player, ItemStack stack) {
        int maximum = GoldSignetSystem.getMaximumEnergy(player);
        if (stack.is(ModItems.FENGHUANG_DOWN.get())) {
            return new EnergyView(stack, FenghuangDownEnergy.getEnergy(stack), maximum,
                    0xFFE14D45, 0xFFFFB45E);
        }
        if (stack.is(ModItems.FLAWLESS_BENEDICTION_LEGACY.get())) {
            return new EnergyView(stack, FlawlessWeaponEnergy.getEnergy(stack), maximum,
                    0xFFE55AA5, 0xFFFFB4DB);
        }
        if (stack.is(ModItems.ABYSS_FLOWER.get())) {
            return new EnergyView(stack, AbyssFlowerEnergy.get(stack), maximum,
                    0xFF7650B7, 0xFFD8BDFF);
        }
        if (stack.is(ModItems.SKYFIRE_GREATSWORD.get())) {
            return new EnergyView(stack, SkyfireGreatswordEnergy.get(stack), maximum,
                    0xFFFF4A16, 0xFFFFC45C);
        }
        if (stack.is(ModItems.EDEN_STAR.get())) {
            return new EnergyView(stack, EdenStarEnergy.get(stack), maximum,
                    0xFF7650B7, 0xFFE5C15C);
        }
        return null;
    }

    private record EnergyView(ItemStack stack, int energy, int maximum, int accent, int highlight) {
    }
}
