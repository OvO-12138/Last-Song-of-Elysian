package com.ovo.lastsongofelysian.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ovo.lastsongofelysian.entity.EdenBlackHoleEntity;
import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class EdenBlackHoleRenderer extends EntityRenderer<EdenBlackHoleEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            lastsongofelysian.MODID,
            "textures/effect/eden_star_black_hole.png"
    );

    public EdenBlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            EdenBlackHoleEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        float appearance = Mth.clamp((entity.tickCount + partialTick) / 16.0F, 0.02F, 1.0F);
        float horizonRadius = entity.getEventHorizonRadius() * appearance;
        VertexConsumer consumer = buffer.getBuffer(RenderType.debugQuads());

        poseStack.pushPose();
        Matrix4f spherePose = poseStack.last().pose();
        EdenStarRenderGeometry.drawSphere(
                spherePose, consumer, horizonRadius, 20, 32, 0, 0, 0, 255
        );

        float diskRadius = entity.getAccretionDiskRadius() * appearance;
        Matrix4f diskPose = poseStack.last().pose();
        EdenStarRenderGeometry.drawAnnulus(
                diskPose, consumer, horizonRadius * 0.96F, diskRadius,
                96, 0, 0, 0, 255
        );
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EdenBlackHoleEntity entity) {
        return TEXTURE;
    }
}
