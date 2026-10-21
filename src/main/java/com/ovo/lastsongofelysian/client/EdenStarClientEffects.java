package com.ovo.lastsongofelysian.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ovo.lastsongofelysian.client.renderer.EdenStarRenderGeometry;
import com.ovo.lastsongofelysian.entity.EdenBlackHoleEntity;
import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EdenStarClientEffects {
    private static final List<GravityWaveVisual> GRAVITY_WAVES = new ArrayList<>();
    private static ClientLevel activeLevel;

    private EdenStarClientEffects() {
    }

    public static void startGravityWave(int targetId, int duration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        resetForLevel(minecraft.level);
        long now = Util.getMillis();
        GRAVITY_WAVES.removeIf(wave -> wave.targetId == targetId);
        GRAVITY_WAVES.add(new GravityWaveVisual(targetId, now, now + Math.max(1, duration) * 50L));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!isInsideEventHorizon(event.getCamera().getPosition())) return;
        event.setRed(0.0F);
        event.setGreen(0.0F);
        event.setBlue(0.0F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!isInsideEventHorizon(event.getCamera().getPosition())) return;
        event.setNearPlaneDistance(0.0F);
        event.setFarPlaneDistance(0.01F);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInsideEventHorizon(minecraft.gameRenderer.getMainCamera().getPosition())) return;
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0xFF000000);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        resetForLevel(minecraft.level);
        cleanup();
        if (GRAVITY_WAVES.isEmpty()) return;

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.debugQuads());
        long now = Util.getMillis();

        for (GravityWaveVisual wave : GRAVITY_WAVES) {
            Entity target = minecraft.level.getEntity(wave.targetId);
            if (target == null) continue;
            float partialTick = event.getPartialTick();
            double x = Mth.lerp(partialTick, target.xo, target.getX());
            double y = Mth.lerp(partialTick, target.yo, target.getY()) + 0.03D;
            double z = Mth.lerp(partialTick, target.zo, target.getZ());
            double duration = Math.max(1.0D, wave.endMillis - wave.startMillis);
            double progress = Mth.clamp((now - wave.startMillis) / duration, 0.0D, 1.0D);
            poseStack.pushPose();
            poseStack.translate(x - camera.x, y - camera.y, z - camera.z);
            float minimumRadius = Math.max(0.4F, target.getBbWidth() * 0.65F);
            for (int ring = 0; ring < 5; ring++) {
                double phase = (progress * 8.0D + ring * 0.2D) % 1.0D;
                float radius = minimumRadius + (float) ((1.0D - phase) * 5.4D);
                float width = 0.09F + (float) phase * 0.08F;
                poseStack.pushPose();
                poseStack.translate(0.0D, ring * 0.002D, 0.0D);
                Matrix4f pose = poseStack.last().pose();
                EdenStarRenderGeometry.drawAnnulus(
                        pose, consumer,
                        Math.max(0.05F, radius - width), radius + width,
                        56, 0, 0, 0, 230
                );
                poseStack.popPose();
            }
            poseStack.popPose();
        }

        buffers.endBatch(RenderType.debugQuads());
    }

    private static void resetForLevel(ClientLevel level) {
        if (activeLevel == level) return;
        GRAVITY_WAVES.clear();
        activeLevel = level;
    }

    private static boolean isInsideEventHorizon(Vec3 position) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return false;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof EdenBlackHoleEntity blackHole)) continue;
            double radius = blackHole.getEventHorizonRadius();
            if (position.distanceToSqr(blackHole.position()) <= radius * radius) return true;
        }
        return false;
    }

    private static void cleanup() {
        long now = Util.getMillis();
        GRAVITY_WAVES.removeIf(wave -> wave.endMillis <= now);
    }

    private static final class GravityWaveVisual {
        private final int targetId;
        private final long startMillis;
        private final long endMillis;

        private GravityWaveVisual(int targetId, long startMillis, long endMillis) {
            this.targetId = targetId;
            this.startMillis = startMillis;
            this.endMillis = endMillis;
        }
    }
}
