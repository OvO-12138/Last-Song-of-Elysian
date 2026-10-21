package com.ovo.lastsongofelysian.registry;

import com.ovo.lastsongofelysian.entity.CorrosionMirrorEntity;
import com.ovo.lastsongofelysian.entity.EdenBlackHoleEntity;
import com.ovo.lastsongofelysian.entity.ElysianHeroNpcEntity;
import com.ovo.lastsongofelysian.entity.PardofelisMerchantEntity;
import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, lastsongofelysian.MODID);

    public static final RegistryObject<EntityType<CorrosionMirrorEntity>> CORROSION_MIRROR =
            ENTITY_TYPES.register("corrosion_mirror",
                    () -> EntityType.Builder.of(CorrosionMirrorEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build("corrosion_mirror"));

    public static final RegistryObject<EntityType<EdenBlackHoleEntity>> EDEN_BLACK_HOLE =
            ENTITY_TYPES.register("eden_black_hole",
                    () -> EntityType.Builder.<EdenBlackHoleEntity>of(
                                    EdenBlackHoleEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .fireImmune()
                            .build("eden_black_hole"));

    public static final RegistryObject<EntityType<PardofelisMerchantEntity>> PARDOFELIS_MERCHANT =
            ENTITY_TYPES.register("pardofelis_merchant",
                    () -> EntityType.Builder.of(PardofelisMerchantEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .build("pardofelis_merchant"));

    public static final RegistryObject<EntityType<ElysianHeroNpcEntity>> APONIA_NPC =
            registerHero("aponia_npc", ElysianHeroNpcEntity.HeroVariant.APONIA);

    public static final RegistryObject<EntityType<ElysianHeroNpcEntity>> ELYSIA_NPC =
            registerHero("elysia_npc", ElysianHeroNpcEntity.HeroVariant.ELYSIA);

    public static final RegistryObject<EntityType<ElysianHeroNpcEntity>> GRISEO_NPC =
            registerHero("griseo_npc", ElysianHeroNpcEntity.HeroVariant.GRISEO);

    public static final RegistryObject<EntityType<ElysianHeroNpcEntity>> VILL_V_NPC =
            registerHero("vill_v_npc", ElysianHeroNpcEntity.HeroVariant.VILL_V);

    public static final RegistryObject<EntityType<ElysianHeroNpcEntity>> EDEN_NPC =
            registerHero("eden_npc", ElysianHeroNpcEntity.HeroVariant.EDEN);




    private static RegistryObject<EntityType<ElysianHeroNpcEntity>> registerHero(
            String id,
            ElysianHeroNpcEntity.HeroVariant variant
    ) {
        return ENTITY_TYPES.register(
                id,
                () -> EntityType.Builder.<ElysianHeroNpcEntity>of(
                                (type, level) ->
                                        new ElysianHeroNpcEntity(
                                                type,
                                                level,
                                                variant
                                        ),
                                MobCategory.CREATURE
                        )
                        .sized(0.6F, 1.8F)
                        .clientTrackingRange(10)
                        .updateInterval(2)
                        .build(id)
        );
    }

}
