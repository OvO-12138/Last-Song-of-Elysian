package com.ovo.lastsongofelysian.client.overlay;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarsTargetInkOverlay {
    private static final int COLUMNS = 6;
    private static final int SPACING = 26;
    private static final int TOP = 56;
    private static final List<InkType> INKS = List.of(
            new InkType(ModEffects.STARS_RED_INK, 0xFFFF3333),
            new InkType(ModEffects.STARS_BLUE_INK, 0xFF3366FF),
            new InkType(ModEffects.STARS_YELLOW_INK, 0xFFFFD84A),
            new InkType(ModEffects.STARS_BLACK_INK, 0xFF292833),
            new InkType(ModEffects.STARS_PURPLE_INK, 0xFFA565D2),
            new InkType(ModEffects.STARS_GREEN_INK, 0xFF5BD374),
            new InkType(ModEffects.STARS_ORANGE_INK, 0xFFF69B48),
            new InkType(ModEffects.RED_INK, 0xFFFF3333),
            new InkType(ModEffects.BLUE_INK, 0xFF3366FF),
            new InkType(ModEffects.PINK_INK, 0xFFFF69B4));

    private StarsTargetInkOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui
                || minecraft.options.renderDebug || minecraft.screen != null
                || !(minecraft.hitResult instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity target)
                || target == minecraft.player) return;

        List<ShownInk> active = new ArrayList<>();
        for (InkType ink : INKS) {
            MobEffectInstance instance = target.getEffect(ink.effect.get());
            if (instance != null && instance.getDuration() > 0) active.add(new ShownInk(ink, instance));
        }
        if (active.isEmpty()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int count = Math.min(COLUMNS, active.size());
        int rowLeft = width - 4 - count * SPACING;
        String caption = net.minecraft.network.chat.Component.translatable(
                "hud.lastsongofelysian.target_inks").getString();
        graphics.drawString(minecraft.font, caption,
                width - 6 - minecraft.font.width(caption), TOP - 11, 0xFFE9DDF9, true);

        for (int i = 0; i < active.size(); i++) {
            ShownInk shown = active.get(i);
            int x = rowLeft + (i % COLUMNS) * SPACING;
            int y = TOP + (i / COLUMNS) * SPACING;
            graphics.fill(x, y, x + 24, y + 24, 0xC0101019);
            graphics.fill(x, y + 21, x + 24, y + 24, shown.type.color);
            graphics.blit(x + 3, y + 2, 0, 18, 18,
                    minecraft.getMobEffectTextures().get(shown.instance.getEffect()));
        }
    }

    private record InkType(RegistryObject<MobEffect> effect, int color) {
    }

    private record ShownInk(InkType type, MobEffectInstance instance) {
    }
}
