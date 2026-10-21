package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.event.FenghuangDownSystem;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(
        modid = lastsongofelysian.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class FenghuangDownClientEffects {
    private static final DustParticleOptions FLAME_CORE = new DustParticleOptions(
            new Vector3f(1.0F, 0.82F, 0.24F), 1.65F
    );
    private static final DustParticleOptions FLAME_GOLD = new DustParticleOptions(
            new Vector3f(1.0F, 0.34F, 0.02F), 1.4F
    );
    private static final DustParticleOptions FLAME_CRIMSON = new DustParticleOptions(
            new Vector3f(0.82F, 0.015F, 0.01F), 1.2F
    );
    private static final ResourceLocation XUANYUAN_SWORD_TEXTURE = new ResourceLocation(
            lastsongofelysian.MODID,
            "textures/effect/xuanyuan_sword.png"
    );
    private static final ResourceLocation FEATHER_TEXTURE = new ResourceLocation(
            lastsongofelysian.MODID,
            "textures/effect/fenghuang_down_feather.png"
    );
    private static final List<DomainVisual> DOMAINS = new ArrayList<>();
    private static final List<SwordBarrageVisual> SWORD_BARRAGES = new ArrayList<>();
    private static final List<FlameWaveVisual> FLAME_WAVES = new ArrayList<>();
    private static final List<FeatherVisual> FEATHERS = new ArrayList<>();
    private static ClientLevel activeLevel;

    private FenghuangDownClientEffects() {
    }

    public static void startDomain(int casterId, double x, double y, double z, int duration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        if (activeLevel != minecraft.level) {
            DOMAINS.clear();
            SWORD_BARRAGES.clear();
            FLAME_WAVES.clear();
            FEATHERS.clear();
            activeLevel = minecraft.level;
        }
        DOMAINS.removeIf(domain -> domain.casterId == casterId);
        DOMAINS.add(new DomainVisual(
                casterId,
                new Vec3(x, y, z),
                Util.getMillis() + Math.max(1, duration) * 50L
        ));
    }

    public static void startFirstPowerBarrage(int casterId, int duration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        if (activeLevel != minecraft.level) {
            DOMAINS.clear();
            SWORD_BARRAGES.clear();
            FLAME_WAVES.clear();
            FEATHERS.clear();
            activeLevel = minecraft.level;
        }
        long start = Util.getMillis();
        SWORD_BARRAGES.removeIf(barrage -> barrage.casterId == casterId);
        SWORD_BARRAGES.add(new SwordBarrageVisual(
                casterId,
                start,
                start + Math.max(1, duration) * 50L
        ));
        FLAME_WAVES.removeIf(wave -> wave.casterId == casterId);
        FLAME_WAVES.add(new FlameWaveVisual(casterId, start, start + 1400L));
    }

    public static void startFeather(
            int casterId,
            double x,
            double y,
            double z,
            int duration
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        if (activeLevel != minecraft.level) {
            DOMAINS.clear();
            SWORD_BARRAGES.clear();
            FLAME_WAVES.clear();
            FEATHERS.clear();
            activeLevel = minecraft.level;
        }
        Vec3 position = new Vec3(x, y, z);
        FEATHERS.removeIf(feather -> feather.casterId == casterId
                && feather.position.distanceToSqr(position) < 0.01D);
        FEATHERS.add(new FeatherVisual(
                casterId,
                position,
                Util.getMillis() + Math.max(1, duration) * 50L
        ));
    }

    public static boolean shouldFreeze(Entity entity) {
        cleanup();
        for (DomainVisual domain : DOMAINS) {
            if (entity.getId() != domain.casterId
                    && entity.position().distanceToSqr(domain.center)
                    <= FenghuangDownSystem.DOMAIN_RADIUS * FenghuangDownSystem.DOMAIN_RADIUS) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            cleanup();
            tickFlameWaves();
        }
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!isInsideDomain(event.getCamera().getPosition())) {
            return;
        }
        event.setRed(1.0F);
        event.setGreen(1.0F);
        event.setBlue(1.0F);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!isInsideDomain(event.getCamera().getPosition())) {
            return;
        }
        event.setNearPlaneDistance(0.0F);
        event.setFarPlaneDistance(8.0F);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        cleanup();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        boolean rendered = false;

        for (DomainVisual domain : DOMAINS) {
            if (camera.distanceToSqr(domain.center)
                    > FenghuangDownSystem.DOMAIN_RADIUS * FenghuangDownSystem.DOMAIN_RADIUS) {
                continue;
            }
            Entity entity = minecraft.level.getEntity(domain.casterId);
            if (!(entity instanceof Player caster)) {
                continue;
            }
            for (int i = 0; i < 5; i++) {
                double angle = Math.PI * 2.0D * i / 5.0D - Math.PI / 2.0D;
                double x = domain.center.x + Math.cos(angle) * 2.8D;
                double z = domain.center.z + Math.sin(angle) * 2.8D;
                dispatcher.render(
                        caster,
                        x - camera.x,
                        domain.center.y - camera.y,
                        z - camera.z,
                        caster.getYRot(),
                        event.getPartialTick(),
                        poseStack,
                        buffers,
                        LightTexture.FULL_BRIGHT
                );
            }
            rendered = true;
        }
        long now = Util.getMillis();
        for (SwordBarrageVisual barrage : SWORD_BARRAGES) {
            Entity entity = minecraft.level.getEntity(barrage.casterId);
            if (!(entity instanceof Player caster)) {
                continue;
            }
            Vec3 forward = caster.getLookAngle().normalize();
            Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (right.lengthSqr() < 1.0E-5D) {
                right = new Vec3(1.0D, 0.0D, 0.0D);
            } else {
                right = right.normalize();
            }
            Vec3 up = right.cross(forward).normalize();
            long duration = Math.max(1L, barrage.endMillis - barrage.startMillis);
            for (int index = 0; index < 300; index++) {
                long launchTime = barrage.startMillis + duration * index / 300L;
                long age = now - launchTime;
                if (age < 0L || age > 850L) {
                    continue;
                }
                float progress = age / 850.0F;
                Vec3 origin = caster.position()
                        .add(0.0D, 1.4D, 0.0D)
                        .subtract(forward.scale(3.0D))
                        .add(right.scale(noise(index, 11) * 3.5D))
                        .add(up.scale(noise(index, 23) * 2.2D + 1.6D));
                Vec3 position = origin.add(forward.scale(progress * 36.0D));
                int alpha = Math.max(0, Math.min(255, Math.round((1.0F - progress) * 255.0F)));

                poseStack.pushPose();
                poseStack.translate(
                        position.x - camera.x,
                        position.y - camera.y,
                        position.z - camera.z
                );
                poseStack.mulPose(event.getCamera().rotation());
                PoseStack.Pose pose = poseStack.last();
                Matrix4f matrix = pose.pose();
                Matrix3f normal = pose.normal();
                VertexConsumer consumer = buffers.getBuffer(
                        RenderType.entityTranslucentEmissive(XUANYUAN_SWORD_TEXTURE)
                );
                float halfWidth = 2.2F;
                float halfHeight = 1.25F;
                consumer.vertex(matrix, -halfWidth, -halfHeight, 0.0F)
                        .color(255, 255, 255, alpha).uv(0.0F, 1.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, halfWidth, -halfHeight, 0.0F)
                        .color(255, 255, 255, alpha).uv(1.0F, 1.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, halfWidth, halfHeight, 0.0F)
                        .color(255, 255, 255, alpha).uv(1.0F, 0.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, -halfWidth, halfHeight, 0.0F)
                        .color(255, 255, 255, alpha).uv(0.0F, 0.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
                poseStack.popPose();
                rendered = true;
            }
        }
        for (FeatherVisual feather : FEATHERS) {
            poseStack.pushPose();
            poseStack.translate(
                    feather.position.x - camera.x,
                    feather.position.y + 0.35D - camera.y,
                    feather.position.z - camera.z
            );
            poseStack.mulPose(event.getCamera().rotation());
            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            Matrix3f normal = pose.normal();
            VertexConsumer consumer = buffers.getBuffer(
                    RenderType.entityTranslucentEmissive(FEATHER_TEXTURE)
            );
            float halfSize = 0.6F;
            float size = 1.2F;
            consumer.vertex(matrix, -halfSize, 0.0F, 0.0F)
                    .color(255, 255, 255, 255).uv(0.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                    .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, halfSize, 0.0F, 0.0F)
                    .color(255, 255, 255, 255).uv(1.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                    .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, halfSize, size, 0.0F)
                    .color(255, 255, 255, 255).uv(1.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                    .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, -halfSize, size, 0.0F)
                    .color(255, 255, 255, 255).uv(0.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                    .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
            poseStack.popPose();
            rendered = true;
        }
        if (rendered) {
            buffers.endBatch();
        }
    }

    private static boolean isInsideDomain(Vec3 position) {
        cleanup();
        for (DomainVisual domain : DOMAINS) {
            if (position.distanceToSqr(domain.center)
                    <= FenghuangDownSystem.DOMAIN_RADIUS * FenghuangDownSystem.DOMAIN_RADIUS) {
                return true;
            }
        }
        return false;
    }

    private static double noise(int index, int salt) {
        int value = index * 73428767 ^ salt * 912931;
        value ^= value >>> 13;
        value *= 1274126177;
        value ^= value >>> 16;
        return ((value & 0x7FFFFFFF) / (double) Integer.MAX_VALUE) * 2.0D - 1.0D;
    }

    private static void tickFlameWaves() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        long gameTime = minecraft.level.getGameTime();
        long now = Util.getMillis();
        for (FlameWaveVisual wave : FLAME_WAVES) {
            if (wave.lastParticleTick == gameTime) {
                continue;
            }
            wave.lastParticleTick = gameTime;
            Entity entity = minecraft.level.getEntity(wave.casterId);
            if (!(entity instanceof Player caster)) {
                continue;
            }
            double duration = Math.max(1.0D, wave.endMillis - wave.startMillis);
            double progress = Math.min(1.0D, Math.max(0.0D, (now - wave.startMillis) / duration));
            spawnFlameWaveParticles(minecraft.level, caster, progress, gameTime);
        }
    }

    private static void spawnFlameWaveParticles(
            ClientLevel level,
            Player caster,
            double progress,
            long gameTime
    ) {
        Vec3 forward = caster.getLookAngle().normalize();
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-5D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(forward).normalize();
        Vec3 origin = caster.getEyePosition().add(forward.scale(1.2D));
        double frontDistance = 2.0D + progress * 28.0D;
        double ringRadius = 0.7D + progress * 7.8D;
        Vec3 front = origin.add(forward.scale(frontDistance));
        double rotation = progress * Math.PI * 8.0D;

        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2.0D * i / 24.0D + rotation;
            Vec3 radial = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vec3 position = front.add(radial.scale(ringRadius));
            Vec3 velocity = forward.scale(0.22D).add(radial.scale(0.065D));
            level.addParticle(
                    i % 3 == 0 ? FLAME_CORE : i % 2 == 0 ? FLAME_GOLD : ParticleTypes.FLAME,
                    position.x, position.y, position.z,
                    velocity.x, velocity.y, velocity.z
            );
        }

        for (int i = 0; i < 12; i++) {
            double fraction = (i + 0.5D) / 12.0D;
            double distance = frontDistance * fraction;
            double radius = (0.25D + fraction * ringRadius * 0.62D)
                    * Math.sqrt(Math.max(0.05D, progress));
            double angle = rotation * 1.35D + distance * 0.72D;
            for (int side = 0; side < 2; side++) {
                double spiralAngle = angle + side * Math.PI;
                Vec3 radial = right.scale(Math.cos(spiralAngle))
                        .add(up.scale(Math.sin(spiralAngle)));
                Vec3 position = origin.add(forward.scale(distance)).add(radial.scale(radius));
                level.addParticle(
                        side == 0 ? ParticleTypes.FLAME : FLAME_CRIMSON,
                        position.x, position.y, position.z,
                        forward.x * 0.16D,
                        forward.y * 0.16D,
                        forward.z * 0.16D
                );
            }
        }

        for (int i = 0; i < 12; i++) {
            int seed = (int) (gameTime * 31L + i * 17L);
            double distance = frontDistance * (0.15D + 0.85D * (noise(seed, 71) + 1.0D) * 0.5D);
            double radius = (0.25D + distance * 0.18D) * (noise(seed, 89) + 1.0D) * 0.5D;
            double angle = Math.PI * (noise(seed, 107) + 1.0D);
            Vec3 radial = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vec3 position = origin.add(forward.scale(distance)).add(radial.scale(radius));
            level.addParticle(
                    i % 4 == 0 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                    position.x, position.y, position.z,
                    forward.x * 0.28D,
                    forward.y * 0.28D + 0.018D,
                    forward.z * 0.28D
            );
        }

        for (int i = 0; i < 4; i++) {
            double angle = rotation + Math.PI * 0.5D * i;
            Vec3 radial = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vec3 position = front.add(radial.scale(ringRadius * 0.45D));
            level.addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    position.x, position.y, position.z,
                    forward.x * 0.35D + radial.x * 0.08D,
                    forward.y * 0.35D + radial.y * 0.08D,
                    forward.z * 0.35D + radial.z * 0.08D
            );
        }
    }

    private static void cleanup() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || activeLevel != minecraft.level) {
            DOMAINS.clear();
            SWORD_BARRAGES.clear();
            FLAME_WAVES.clear();
            FEATHERS.clear();
            activeLevel = minecraft.level;
            return;
        }
        long now = Util.getMillis();
        Iterator<DomainVisual> iterator = DOMAINS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().endMillis <= now) {
                iterator.remove();
            }
        }
        SWORD_BARRAGES.removeIf(barrage -> barrage.endMillis + 850L <= now);
        FLAME_WAVES.removeIf(wave -> wave.endMillis <= now);
        FEATHERS.removeIf(feather -> feather.endMillis <= now);
    }

    private record DomainVisual(int casterId, Vec3 center, long endMillis) {
    }

    private record SwordBarrageVisual(int casterId, long startMillis, long endMillis) {
    }

    private static final class FlameWaveVisual {
        private final int casterId;
        private final long startMillis;
        private final long endMillis;
        private long lastParticleTick = Long.MIN_VALUE;

        private FlameWaveVisual(int casterId, long startMillis, long endMillis) {
            this.casterId = casterId;
            this.startMillis = startMillis;
            this.endMillis = endMillis;
        }
    }

    private record FeatherVisual(int casterId, Vec3 position, long endMillis) {
    }
}
