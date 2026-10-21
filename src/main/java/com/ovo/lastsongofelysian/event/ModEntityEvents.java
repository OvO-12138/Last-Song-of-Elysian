package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.entity.CorrosionMirrorEntity;
import com.ovo.lastsongofelysian.entity.ElysianHeroNpcEntity;
import com.ovo.lastsongofelysian.entity.PardofelisMerchantEntity;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = lastsongofelysian.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ModEntityEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(
                ModEntities.CORROSION_MIRROR.get(),
                CorrosionMirrorEntity.createAttributes().build()
        );
        event.put(
                ModEntities.PARDOFELIS_MERCHANT.get(),
                PardofelisMerchantEntity.createAttributes().build()
        );
        event.put(
                ModEntities.APONIA_NPC.get(),
                ElysianHeroNpcEntity.createAttributes().build()
        );
        event.put(
                ModEntities.ELYSIA_NPC.get(),
                ElysianHeroNpcEntity.createAttributes().build()
        );
        event.put(
                ModEntities.GRISEO_NPC.get(),
                ElysianHeroNpcEntity.createAttributes().build()
        );
        event.put(
                ModEntities.VILL_V_NPC.get(),
                ElysianHeroNpcEntity.createAttributes().build()
        );
        event.put(
                ModEntities.EDEN_NPC.get(),
                ElysianHeroNpcEntity.createAttributes().build()
        );
    }

}
