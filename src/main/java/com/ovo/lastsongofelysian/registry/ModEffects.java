package com.ovo.lastsongofelysian.registry;

import com.ovo.lastsongofelysian.effect.BlueInkEffect;
import com.ovo.lastsongofelysian.effect.AbyssFlowerWitherEffect;
import com.ovo.lastsongofelysian.effect.FlawlessBloomEffect;
import com.ovo.lastsongofelysian.effect.PinkInkEffect;
import com.ovo.lastsongofelysian.effect.RedInkEffect;
import com.ovo.lastsongofelysian.effect.StackedSignetEffect;
import com.ovo.lastsongofelysian.effect.TuolinEffect;
import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(
                    ForgeRegistries.MOB_EFFECTS,
                    lastsongofelysian.MODID
            );

    public static final RegistryObject<MobEffect> RED_INK =
            EFFECTS.register(
                    "red_ink",
                    RedInkEffect::new
            );

    public static final RegistryObject<MobEffect> BLUE_INK =
            EFFECTS.register(
                    "blue_ink",
                    BlueInkEffect::new
            );

    public static final RegistryObject<MobEffect> STARS_RED_INK =
            EFFECTS.register("stars_red_ink", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFF3333) {});
    public static final RegistryObject<MobEffect> STARS_BLUE_INK =
            EFFECTS.register("stars_blue_ink", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0x3366FF) {});
    public static final RegistryObject<MobEffect> STARS_YELLOW_INK =
            EFFECTS.register("stars_yellow_ink", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFFD84A) {});
    public static final RegistryObject<MobEffect> STARS_BLACK_INK =
            EFFECTS.register("stars_black_ink", () -> new StackedSignetEffect(0x292833));
    public static final RegistryObject<MobEffect> STARS_PURPLE_INK =
            EFFECTS.register("stars_purple_ink", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xA565D2) {});
    public static final RegistryObject<MobEffect> STARS_GREEN_INK =
            EFFECTS.register("stars_green_ink", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0x5BD374) {});
    public static final RegistryObject<MobEffect> STARS_ORANGE_INK =
            EFFECTS.register("stars_orange_ink", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xF69B48) {});

    public static final RegistryObject<MobEffect> LACERATION =
            EFFECTS.register(
                    "laceration",
                    () -> new MobEffect(
                            MobEffectCategory.HARMFUL,
                            0x4CAF50
                    ) {
                    }
            );

    public static final RegistryObject<MobEffect> TUOLIN =
            EFFECTS.register(
                    "tuolin",
                    TuolinEffect::new
            );

    public static final RegistryObject<MobEffect> PINK_INK =
            EFFECTS.register(
                    "pink_ink",
                    PinkInkEffect::new
            );

    public static final RegistryObject<MobEffect> FLAWLESS_BLOOM =
            EFFECTS.register(
                    "flawless_bloom",
                    FlawlessBloomEffect::new
            );
    public static final RegistryObject<MobEffect> BLACK_ABYSS_WITHER =
            EFFECTS.register("black_abyss_wither", AbyssFlowerWitherEffect::new);

    public static final RegistryObject<MobEffect> DELIVERANCE_FINAL_BATTLE =
            EFFECTS.register(
                    "deliverance_final_battle",
                    () -> new MobEffect(
                            MobEffectCategory.BENEFICIAL,
                            0x173A7A
                    ) {
                    }
            );

    public static final RegistryObject<MobEffect> DISCIPLINE_ASCETICISM =
            EFFECTS.register(
                    "discipline_asceticism",
                    () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xD4A72C) {
                    }
            );

    public static final RegistryObject<MobEffect> DISCIPLINE_BLESSING =
            EFFECTS.register(
                    "discipline_blessing",
                    () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFE88A) {
                    }
            );

    public static final RegistryObject<MobEffect> DISCIPLINE_PUNISHMENT =
            EFFECTS.register(
                    "discipline_punishment",
                    () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xB88516) {
                    }
            );

    public static final RegistryObject<MobEffect> GOLD_INTOXICATED =
            EFFECTS.register(
                    "gold_intoxicated",
                    () -> new MobEffect(MobEffectCategory.HARMFUL, 0xE4B642) {
                    }
            );

    public static final RegistryObject<MobEffect> GOLD_FINAL_ECHO =
            EFFECTS.register(
                    "gold_final_echo",
                    () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFD75A) {
                    }
            );

    public static final RegistryObject<MobEffect> REVERIE_VIP_TREATMENT =
            EFFECTS.register(
                    "reverie_vip_treatment",
                    () -> new StackedSignetEffect(0xFFD266)
            );

    public static final RegistryObject<MobEffect> VICISSITUDE_WARRIORS_RESOLVE =
            EFFECTS.register(
                    "vicissitude_warriors_resolve",
                    () -> new StackedSignetEffect(0xFFD8AE)
            );
}
