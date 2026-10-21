package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.client.model.ElysianHeroModel;
import com.ovo.lastsongofelysian.client.model.PardofelisMerchantModel;
import com.ovo.lastsongofelysian.client.renderer.CorrosionMirrorRenderer;
import com.ovo.lastsongofelysian.client.renderer.EdenBlackHoleRenderer;
import com.ovo.lastsongofelysian.client.renderer.ElysianHeroRenderer;
import com.ovo.lastsongofelysian.client.renderer.PardofelisMerchantRenderer;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = lastsongofelysian.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                ElysianHeroModel.LAYER_LOCATION,
                ElysianHeroModel::createBodyLayer
        );
        event.registerLayerDefinition(
                PardofelisMerchantModel.LAYER_LOCATION,
                PardofelisMerchantModel::createBodyLayer
        );
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.CORROSION_MIRROR.get(),
                CorrosionMirrorRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.EDEN_BLACK_HOLE.get(),
                EdenBlackHoleRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.APONIA_NPC.get(),
                ElysianHeroRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.ELYSIA_NPC.get(),
                ElysianHeroRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.GRISEO_NPC.get(),
                ElysianHeroRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.VILL_V_NPC.get(),
                ElysianHeroRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.EDEN_NPC.get(),
                ElysianHeroRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.PARDOFELIS_MERCHANT.get(),
                PardofelisMerchantRenderer::new
        );
    }
}
