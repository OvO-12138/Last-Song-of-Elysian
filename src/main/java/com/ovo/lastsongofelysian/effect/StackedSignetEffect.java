package com.ovo.lastsongofelysian.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

public final class StackedSignetEffect extends MobEffect {
    public StackedSignetEffect(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new IClientMobEffectExtensions() {
            @Override
            public boolean renderGuiIcon(MobEffectInstance instance, Gui gui,
                                         GuiGraphics graphics, int x, int y,
                                         float z, float alpha) {
                Minecraft minecraft = Minecraft.getInstance();
                TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(instance.getEffect());
                graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                graphics.blit(x + 3, y + 3, 0, 18, 18, sprite);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

                String stacks = Integer.toString(instance.getAmplifier() + 1);
                graphics.fill(x + 14, y + 13, x + 23, y + 23, 0xC0000000);
                graphics.drawString(minecraft.font, stacks,
                        x + 22 - minecraft.font.width(stacks), y + 14,
                        0xFFFFFFFF, true);
                return true;
            }
        });
    }
}
