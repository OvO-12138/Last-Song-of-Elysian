package com.ovo.lastsongofelysian.registry;

import com.ovo.lastsongofelysian.item.*;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.ForgeSpawnEggItem;
import com.ovo.lastsongofelysian.item.WhisperOfThePastItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, lastsongofelysian.MODID);
    public static final RegistryObject<Item> SIGNET_OF_REVERIE = ITEMS.register("signet_of_reverie",
            () -> new SignetOfReverieItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> REVERIE_CAT_VERSE = registerExtensionAugment("reverie_cat_verse", SignetUpgradeData.Skill.REVERIE_CAT_VERSE);
    public static final RegistryObject<Item> REVERIE_BLADE_TAIL = registerExtensionAugment("reverie_blade_tail", SignetUpgradeData.Skill.REVERIE_BLADE_TAIL);
    public static final RegistryObject<Item> REVERIE_STREET_JOY = registerExtensionAugment("reverie_street_joy", SignetUpgradeData.Skill.REVERIE_STREET_JOY);
    public static final RegistryObject<Item> REVERIE_PC = registerExtensionAugment("reverie_pc", SignetUpgradeData.Skill.REVERIE_PC);
    public static final RegistryObject<Item> REVERIE_JOY = registerExtensionCore("reverie_joy", SignetUpgradeData.ExtensionCore.REVERIE_JOY);
    public static final RegistryObject<Item> REVERIE_BOSS = registerExtensionAugment("reverie_boss", SignetUpgradeData.Skill.REVERIE_BOSS);
    public static final RegistryObject<Item> REVERIE_CAT_PUNCH = registerExtensionAugment("reverie_cat_punch", SignetUpgradeData.Skill.REVERIE_CAT_PUNCH);
    public static final RegistryObject<Item> REVERIE_LUCKY = registerExtensionAugment("reverie_lucky", SignetUpgradeData.Skill.REVERIE_LUCKY);
    public static final RegistryObject<Item> REVERIE_DREAM = registerExtensionCore("reverie_dream", SignetUpgradeData.ExtensionCore.REVERIE_DREAM);
    public static final RegistryObject<Item> REVERIE_ZERO = registerExtensionAugment("reverie_zero", SignetUpgradeData.Skill.REVERIE_ZERO);
    public static final RegistryObject<Item> REVERIE_SNOW = registerExtensionAugment("reverie_snow", SignetUpgradeData.Skill.REVERIE_SNOW);
    public static final RegistryObject<Item> REVERIE_DREAM_TRUE = registerExtensionAugment("reverie_dream_true", SignetUpgradeData.Skill.REVERIE_DREAM_TRUE);
    public static final RegistryObject<Item> SHINY_SILVER = ITEMS.register("shiny_silver",
            () -> new ShinySilverItem(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> SIGNET_OF_VICISSITUDE = ITEMS.register("signet_of_vicissitude",
            () -> new SignetOfVicissitudeItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> VICISSITUDE_TIME = registerExtensionAugment("vicissitude_time", SignetUpgradeData.Skill.VICISSITUDE_TIME);
    public static final RegistryObject<Item> VICISSITUDE_ROBE = registerExtensionAugment("vicissitude_robe", SignetUpgradeData.Skill.VICISSITUDE_ROBE);
    public static final RegistryObject<Item> VICISSITUDE_DREAM = registerExtensionAugment("vicissitude_dream", SignetUpgradeData.Skill.VICISSITUDE_DREAM);
    public static final RegistryObject<Item> VICISSITUDE_WORDS = registerExtensionAugment("vicissitude_words", SignetUpgradeData.Skill.VICISSITUDE_WORDS);
    public static final RegistryObject<Item> VICISSITUDE_RESOLVE = registerExtensionCore("vicissitude_resolve", SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE);
    public static final RegistryObject<Item> VICISSITUDE_RED_DUST = registerExtensionAugment("vicissitude_red_dust", SignetUpgradeData.Skill.VICISSITUDE_RED_DUST);
    public static final RegistryObject<Item> VICISSITUDE_PROSPERITY = registerExtensionAugment("vicissitude_prosperity", SignetUpgradeData.Skill.VICISSITUDE_PROSPERITY);
    public static final RegistryObject<Item> VICISSITUDE_FAMILIAR = registerExtensionAugment("vicissitude_familiar", SignetUpgradeData.Skill.VICISSITUDE_FAMILIAR);
    public static final RegistryObject<Item> VICISSITUDE_MEMORY = registerExtensionCore("vicissitude_memory", SignetUpgradeData.ExtensionCore.VICISSITUDE_MEMORY);
    public static final RegistryObject<Item> VICISSITUDE_REED = registerExtensionAugment("vicissitude_reed", SignetUpgradeData.Skill.VICISSITUDE_REED);
    public static final RegistryObject<Item> VICISSITUDE_FEATHER = registerExtensionAugment("vicissitude_feather", SignetUpgradeData.Skill.VICISSITUDE_FEATHER);
    public static final RegistryObject<Item> VICISSITUDE_AWAKEN = registerExtensionAugment("vicissitude_awaken", SignetUpgradeData.Skill.VICISSITUDE_AWAKEN);
    public static final RegistryObject<Item> SILVER_POUCH = ITEMS.register("silver_pouch",
            () -> new SilverPouchItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SIGNET_OF_STARS = ITEMS.register("signet_of_stars",
            () -> new SignetOfStarsItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STARS_YELLOW = registerExtensionAugment("stars_yellow", SignetUpgradeData.Skill.STARS_YELLOW);
    public static final RegistryObject<Item> STARS_BLACK = registerExtensionAugment("stars_black", SignetUpgradeData.Skill.STARS_BLACK);
    public static final RegistryObject<Item> STARS_WHITE = registerExtensionAugment("stars_white", SignetUpgradeData.Skill.STARS_WHITE);
    public static final RegistryObject<Item> STARS_GRAY = registerExtensionAugment("stars_gray", SignetUpgradeData.Skill.STARS_GRAY);
    public static final RegistryObject<Item> STARS_SHINING = registerExtensionCore("stars_shining", SignetUpgradeData.ExtensionCore.STARS_SHINING);
    public static final RegistryObject<Item> STARS_WILDFLOWER_SHINING = registerExtensionAugment("stars_wildflower_shining", SignetUpgradeData.Skill.STARS_WILDFLOWER_SHINING);
    public static final RegistryObject<Item> STARS_FLAME_SHINING = registerExtensionAugment("stars_flame_shining", SignetUpgradeData.Skill.STARS_FLAME_SHINING);
    public static final RegistryObject<Item> STARS_LEAF_SHINING = registerExtensionAugment("stars_leaf_shining", SignetUpgradeData.Skill.STARS_LEAF_SHINING);
    public static final RegistryObject<Item> STARS_DIM = registerExtensionCore("stars_dim", SignetUpgradeData.ExtensionCore.STARS_DIM);
    public static final RegistryObject<Item> STARS_WILDFLOWER_DIM = registerExtensionAugment("stars_wildflower_dim", SignetUpgradeData.Skill.STARS_WILDFLOWER_DIM);
    public static final RegistryObject<Item> STARS_LEAF_DIM = registerExtensionAugment("stars_leaf_dim", SignetUpgradeData.Skill.STARS_LEAF_DIM);
    public static final RegistryObject<Item> STARS_FLAME_DIM = registerExtensionAugment("stars_flame_dim", SignetUpgradeData.Skill.STARS_FLAME_DIM);
    public static final RegistryObject<Item> PAINTBRUSH = ITEMS.register("paintbrush",
            () -> new PaintbrushItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SIGNET_OF_INFINITY = ITEMS.register("signet_of_infinity",
            () -> new SignetOfInfinityItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> INFINITY_P = registerExtensionAugment("infinity_p", SignetUpgradeData.Skill.INFINITY_P);
    public static final RegistryObject<Item> INFINITY_B = registerExtensionAugment("infinity_b", SignetUpgradeData.Skill.INFINITY_B);
    public static final RegistryObject<Item> INFINITY_E = registerExtensionAugment("infinity_e", SignetUpgradeData.Skill.INFINITY_E);
    public static final RegistryObject<Item> INFINITY_X = registerExtensionCore("infinity_x", SignetUpgradeData.ExtensionCore.INFINITY_X);
    public static final RegistryObject<Item> INFINITY_DEATH_X = registerExtensionAugment("infinity_death_x", SignetUpgradeData.Skill.INFINITY_DEATH_X);
    public static final RegistryObject<Item> INFINITY_UNKNOWN_X = registerExtensionAugment("infinity_unknown_x", SignetUpgradeData.Skill.INFINITY_UNKNOWN_X);
    public static final RegistryObject<Item> INFINITY_NEWBORN_X = registerExtensionAugment("infinity_newborn_x", SignetUpgradeData.Skill.INFINITY_NEWBORN_X);
    public static final RegistryObject<Item> INFINITY_M = registerExtensionCore("infinity_m", SignetUpgradeData.ExtensionCore.INFINITY_M);
    public static final RegistryObject<Item> INFINITY_SACRIFICE_M = registerExtensionAugment("infinity_sacrifice_m", SignetUpgradeData.Skill.INFINITY_SACRIFICE_M);
    public static final RegistryObject<Item> INFINITY_IMMORTAL_M = registerExtensionAugment("infinity_immortal_m", SignetUpgradeData.Skill.INFINITY_IMMORTAL_M);
    public static final RegistryObject<Item> INFINITY_COLLAPSE_M = registerExtensionAugment("infinity_collapse_m", SignetUpgradeData.Skill.INFINITY_COLLAPSE_M);
    public static final RegistryObject<Item> SIGNET_OF_DAYBREAK = ITEMS.register("signet_of_daybreak",
            () -> new SignetOfDaybreakItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DAYBREAK_WING = registerDaybreakAugment(
            "daybreak_wing", SignetUpgradeData.Skill.DAYBREAK_WING);
    public static final RegistryObject<Item> DAYBREAK_HORN = registerDaybreakAugment(
            "daybreak_horn", SignetUpgradeData.Skill.DAYBREAK_HORN);
    public static final RegistryObject<Item> DAYBREAK_BREATH = registerDaybreakAugment(
            "daybreak_breath", SignetUpgradeData.Skill.DAYBREAK_BREATH);
    public static final RegistryObject<Item> DAYBREAK_HEART = registerDaybreakAugment(
            "daybreak_heart", SignetUpgradeData.Skill.DAYBREAK_HEART);
    public static final RegistryObject<Item> DAYBREAK_UNFADING_LIGHT = registerDaybreakCore(
            "daybreak_unfading_light", SignetUpgradeData.DaybreakCore.UNFADING_LIGHT);
    public static final RegistryObject<Item> DAYBREAK_LIGHT_HERO = registerDaybreakAugment(
            "daybreak_light_hero", SignetUpgradeData.Skill.DAYBREAK_LIGHT_HERO);
    public static final RegistryObject<Item> DAYBREAK_LIGHT_GOOD_EVIL = registerDaybreakAugment(
            "daybreak_light_good_evil", SignetUpgradeData.Skill.DAYBREAK_LIGHT_GOOD_EVIL);
    public static final RegistryObject<Item> DAYBREAK_LIGHT_VOW = registerDaybreakAugment(
            "daybreak_light_vow", SignetUpgradeData.Skill.DAYBREAK_LIGHT_VOW);
    public static final RegistryObject<Item> DAYBREAK_LONELY_NIGHT = registerDaybreakCore(
            "daybreak_lonely_night", SignetUpgradeData.DaybreakCore.LONELY_NIGHT);
    public static final RegistryObject<Item> DAYBREAK_NIGHT_DAWN = registerDaybreakAugment(
            "daybreak_night_dawn", SignetUpgradeData.Skill.DAYBREAK_NIGHT_DAWN);
    public static final RegistryObject<Item> DAYBREAK_NIGHT_CHOICE = registerDaybreakAugment(
            "daybreak_night_choice", SignetUpgradeData.Skill.DAYBREAK_NIGHT_CHOICE);
    public static final RegistryObject<Item> DAYBREAK_NIGHT_INTENT = registerDaybreakAugment(
            "daybreak_night_intent", SignetUpgradeData.Skill.DAYBREAK_NIGHT_INTENT);
    public static final RegistryObject<Item> SIGNET_OF_SETSURA = ITEMS.register("signet_of_setsura",
            () -> new SignetOfSetsuraItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SETSURA_RED_LEAVES = registerSetsuraAugment(
            "setsura_red_leaves", SignetUpgradeData.Skill.SETSURA_RED_LEAVES);
    public static final RegistryObject<Item> SETSURA_PEONY = registerSetsuraAugment(
            "setsura_peony", SignetUpgradeData.Skill.SETSURA_PEONY);
    public static final RegistryObject<Item> SETSURA_WISTERIA = registerSetsuraAugment(
            "setsura_wisteria", SignetUpgradeData.Skill.SETSURA_WISTERIA);
    public static final RegistryObject<Item> SETSURA_IRIS = registerSetsuraAugment(
            "setsura_iris", SignetUpgradeData.Skill.SETSURA_IRIS);
    public static final RegistryObject<Item> SETSURA_SAKURA_CURTAIN = registerSetsuraCore(
            "setsura_sakura_curtain", SignetUpgradeData.SetsuraCore.SAKURA_CURTAIN);
    public static final RegistryObject<Item> SETSURA_CURTAIN_RAIN_FOUR_LIGHTS = registerSetsuraAugment(
            "setsura_curtain_rain_four_lights", SignetUpgradeData.Skill.SETSURA_CURTAIN_RAIN_FOUR_LIGHTS);
    public static final RegistryObject<Item> SETSURA_CURTAIN_MOON_SAKE = registerSetsuraAugment(
            "setsura_curtain_moon_sake", SignetUpgradeData.Skill.SETSURA_CURTAIN_MOON_SAKE);
    public static final RegistryObject<Item> SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY = registerSetsuraAugment(
            "setsura_curtain_boar_deer_butterfly", SignetUpgradeData.Skill.SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY);
    public static final RegistryObject<Item> SETSURA_NIGHT_YAKSHA = registerSetsuraCore(
            "setsura_night_yaksha", SignetUpgradeData.SetsuraCore.NIGHT_YAKSHA);
    public static final RegistryObject<Item> SETSURA_YAKSHA_MOMENTARY_MYRIAD = registerSetsuraAugment(
            "setsura_yaksha_momentary_myriad", SignetUpgradeData.Skill.SETSURA_YAKSHA_MOMENTARY_MYRIAD);
    public static final RegistryObject<Item> SETSURA_YAKSHA_ENDLESS_EVIL = registerSetsuraAugment(
            "setsura_yaksha_endless_evil", SignetUpgradeData.Skill.SETSURA_YAKSHA_ENDLESS_EVIL);
    public static final RegistryObject<Item> SETSURA_YAKSHA_ALL_FORMS = registerSetsuraAugment(
            "setsura_yaksha_all_forms", SignetUpgradeData.Skill.SETSURA_YAKSHA_ALL_FORMS);
    public static final RegistryObject<Item> SIGNET_OF_BODHI = ITEMS.register("signet_of_bodhi",
            () -> new SignetOfBodhiItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BODHI_HEAVENLY_EYE = registerBodhiAugment(
            "bodhi_heavenly_eye", SignetUpgradeData.Skill.BODHI_HEAVENLY_EYE);
    public static final RegistryObject<Item> BODHI_MIND_READING = registerBodhiAugment(
            "bodhi_mind_reading", SignetUpgradeData.Skill.BODHI_MIND_READING);
    public static final RegistryObject<Item> BODHI_DIVINE_FOOT = registerBodhiAugment(
            "bodhi_divine_foot", SignetUpgradeData.Skill.BODHI_DIVINE_FOOT);
    public static final RegistryObject<Item> BODHI_EXHAUSTION = registerBodhiAugment(
            "bodhi_exhaustion", SignetUpgradeData.Skill.BODHI_EXHAUSTION);
    public static final RegistryObject<Item> BODHI_TRUE_WORD = registerBodhiCore(
            "bodhi_true_word", SignetUpgradeData.BodhiCore.TRUE_WORD);
    public static final RegistryObject<Item> BODHI_TRUE_SELFLESS = registerBodhiAugment(
            "bodhi_true_selfless", SignetUpgradeData.Skill.BODHI_TRUE_SELFLESS);
    public static final RegistryObject<Item> BODHI_TRUE_IMPERMANENCE = registerBodhiAugment(
            "bodhi_true_impermanence", SignetUpgradeData.Skill.BODHI_TRUE_IMPERMANENCE);
    public static final RegistryObject<Item> BODHI_TRUE_SILENCE = registerBodhiAugment(
            "bodhi_true_silence", SignetUpgradeData.Skill.BODHI_TRUE_SILENCE);
    public static final RegistryObject<Item> BODHI_SECRET_WORD = registerBodhiCore(
            "bodhi_secret_word", SignetUpgradeData.BodhiCore.SECRET_WORD);
    public static final RegistryObject<Item> BODHI_SECRET_IMPERMANENCE = registerBodhiAugment(
            "bodhi_secret_impermanence", SignetUpgradeData.Skill.BODHI_SECRET_IMPERMANENCE);
    public static final RegistryObject<Item> BODHI_SECRET_SELFLESS = registerBodhiAugment(
            "bodhi_secret_selfless", SignetUpgradeData.Skill.BODHI_SECRET_SELFLESS);
    public static final RegistryObject<Item> BODHI_SECRET_SILENCE = registerBodhiAugment(
            "bodhi_secret_silence", SignetUpgradeData.Skill.BODHI_SECRET_SILENCE);
    public static final RegistryObject<Item> SIGNET_OF_DECIMATION = ITEMS.register("signet_of_decimation",
            () -> new SignetOfDecimationItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DECIMATION_BLADE = registerDecimationAugment(
            "decimation_blade", SignetUpgradeData.Skill.DECIMATION_BLADE);
    public static final RegistryObject<Item> DECIMATION_WUWANG = registerDecimationAugment(
            "decimation_wuwang", SignetUpgradeData.Skill.DECIMATION_WUWANG);
    public static final RegistryObject<Item> DECIMATION_WEISHEN = registerDecimationAugment(
            "decimation_weishen", SignetUpgradeData.Skill.DECIMATION_WEISHEN);
    public static final RegistryObject<Item> DECIMATION_BLOOD_RELEASE = registerDecimationCore(
            "decimation_blood_release", SignetUpgradeData.DecimationCore.BLOOD_RELEASE);
    public static final RegistryObject<Item> DECIMATION_RELEASE_NONHUMAN = registerDecimationAugment(
            "decimation_release_nonhuman", SignetUpgradeData.Skill.DECIMATION_RELEASE_NONHUMAN);
    public static final RegistryObject<Item> DECIMATION_RELEASE_ONE_PATH = registerDecimationAugment(
            "decimation_release_one_path", SignetUpgradeData.Skill.DECIMATION_RELEASE_ONE_PATH);
    public static final RegistryObject<Item> DECIMATION_RELEASE_KALPAS = registerDecimationAugment(
            "decimation_release_kalpas", SignetUpgradeData.Skill.DECIMATION_RELEASE_KALPAS);
    public static final RegistryObject<Item> DECIMATION_BLOOD_RAIMENT = registerDecimationCore(
            "decimation_blood_raiment", SignetUpgradeData.DecimationCore.BLOOD_RAIMENT);
    public static final RegistryObject<Item> DECIMATION_RAIMENT_UNDYING = registerDecimationAugment(
            "decimation_raiment_undying", SignetUpgradeData.Skill.DECIMATION_RAIMENT_UNDYING);
    public static final RegistryObject<Item> DECIMATION_RAIMENT_BURNING_SOUL = registerDecimationAugment(
            "decimation_raiment_burning_soul", SignetUpgradeData.Skill.DECIMATION_RAIMENT_BURNING_SOUL);
    public static final RegistryObject<Item> DECIMATION_RAIMENT_HOMELAND = registerDecimationAugment(
            "decimation_raiment_homeland", SignetUpgradeData.Skill.DECIMATION_RAIMENT_HOMELAND);
    public static final RegistryObject<Item> SIGNET_OF_HELIX = ITEMS.register("signet_of_helix",
            () -> new SignetOfHelixItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HELIX_SPOON = registerHelixAugment(
            "helix_spoon", SignetUpgradeData.Skill.HELIX_SPOON);
    public static final RegistryObject<Item> HELIX_COBWEB = registerHelixAugment(
            "helix_cobweb", SignetUpgradeData.Skill.HELIX_COBWEB);
    public static final RegistryObject<Item> HELIX_SNEER = registerHelixAugment(
            "helix_sneer", SignetUpgradeData.Skill.HELIX_SNEER);
    public static final RegistryObject<Item> HELIX_REVERSED_SPIRAL = registerHelixCore(
            "helix_reversed_spiral", SignetUpgradeData.HelixCore.REVERSED_SPIRAL);
    public static final RegistryObject<Item> HELIX_REVERSED_DOOR = registerHelixAugment(
            "helix_reversed_door", SignetUpgradeData.Skill.HELIX_REVERSED_DOOR);
    public static final RegistryObject<Item> HELIX_REVERSED_HALL = registerHelixAugment(
            "helix_reversed_hall", SignetUpgradeData.Skill.HELIX_REVERSED_HALL);
    public static final RegistryObject<Item> HELIX_REVERSED_TRUTH = registerHelixAugment(
            "helix_reversed_truth", SignetUpgradeData.Skill.HELIX_REVERSED_TRUTH);
    public static final RegistryObject<Item> HELIX_CONCLUSION_SPIRAL = registerHelixCore(
            "helix_conclusion_spiral", SignetUpgradeData.HelixCore.CONCLUSION_SPIRAL);
    public static final RegistryObject<Item> HELIX_CONCLUSION_PRISON = registerHelixAugment(
            "helix_conclusion_prison", SignetUpgradeData.Skill.HELIX_CONCLUSION_PRISON);
    public static final RegistryObject<Item> HELIX_CONCLUSION_ORIGIN = registerHelixAugment(
            "helix_conclusion_origin", SignetUpgradeData.Skill.HELIX_CONCLUSION_ORIGIN);
    public static final RegistryObject<Item> HELIX_CONCLUSION_POINTER = registerHelixAugment(
            "helix_conclusion_pointer", SignetUpgradeData.Skill.HELIX_CONCLUSION_POINTER);
    public static final RegistryObject<Item> SIGNET_OF_GOLD = ITEMS.register("signet_of_gold",
            () -> new SignetOfGoldItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SIGNET_OF_DISCIPLINE = ITEMS.register("signet_of_discipline",
            () -> new SignetOfDisciplineItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SIGNET_OF_DELIVERANCE = ITEMS.register("signet_of_deliverance",
            () -> new SignetOfDeliveranceItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DELIVERANCE_HUNTERS_MASK = ITEMS.register(
            "deliverance_hunters_mask",
            () -> new DeliveranceSecondarySignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.Skill.DELIVERANCE_HUNTERS_MASK
            ));
    public static final RegistryObject<Item> DELIVERANCE_RESTRAINERS_RELIC = ITEMS.register(
            "deliverance_restrainers_relic",
            () -> new DeliveranceSecondarySignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.Skill.DELIVERANCE_RESTRAINERS_RELIC
            ));
    public static final RegistryObject<Item> DELIVERANCE_SEEKERS_ROBE = ITEMS.register(
            "deliverance_seekers_robe",
            () -> new DeliveranceSecondarySignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.Skill.DELIVERANCE_SEEKERS_ROBE
            ));
    public static final RegistryObject<Item> DELIVERANCE_KINGS_SWORD = ITEMS.register(
            "deliverance_kings_sword",
            () -> new DeliveranceCoreSignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.DeliveranceCore.KINGS_SWORD
            ));
    public static final RegistryObject<Item> DELIVERANCE_LONE_SHADOW = ITEMS.register(
            "deliverance_lone_shadow",
            () -> new DeliveranceCoreSignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.DeliveranceCore.LONE_SHADOW
            ));
    public static final RegistryObject<Item> DELIVERANCE_KINGS_MUSTER = ITEMS.register(
            "deliverance_kings_muster",
            () -> new DeliveranceSecondarySignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.Skill.DELIVERANCE_KINGS_MUSTER
            ));
    public static final RegistryObject<Item> DELIVERANCE_KINGS_EXPEDITION = ITEMS.register(
            "deliverance_kings_expedition",
            () -> new DeliveranceSecondarySignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.Skill.DELIVERANCE_KINGS_EXPEDITION
            ));
    public static final RegistryObject<Item> DELIVERANCE_KINGS_ECHO = ITEMS.register(
            "deliverance_kings_echo",
            () -> new DeliveranceSecondarySignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.Skill.DELIVERANCE_KINGS_ECHO
            ));
    public static final RegistryObject<Item> DELIVERANCE_LONE_RESIDUAL_DREAM = ITEMS.register(
            "deliverance_lone_residual_dream",
            () -> new DeliveranceSecondarySignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.Skill.DELIVERANCE_LONE_RESIDUAL_DREAM
            ));
    public static final RegistryObject<Item> DELIVERANCE_LONE_DECISION = ITEMS.register(
            "deliverance_lone_decision",
            () -> new DeliveranceSecondarySignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.Skill.DELIVERANCE_LONE_DECISION
            ));
    public static final RegistryObject<Item> DELIVERANCE_LONE_TRIUMPH = ITEMS.register(
            "deliverance_lone_triumph",
            () -> new DeliveranceSecondarySignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.Skill.DELIVERANCE_LONE_TRIUMPH
            ));
    public static final RegistryObject<Item> DISCIPLINE_ASCETICISM = ITEMS.register(
            "discipline_asceticism",
            () -> new DisciplineCoreSignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.DisciplineCore.ASCETICISM
            ));
    public static final RegistryObject<Item> DISCIPLINE_BLESSING = ITEMS.register(
            "discipline_blessing",
            () -> new DisciplineCoreSignetItem(
                    new Item.Properties().stacksTo(1),
                    SignetUpgradeData.DisciplineCore.BLESSING
            ));
    public static final RegistryObject<Item> DISCIPLINE_FOUR = registerDisciplineAugment(
            "discipline_four", SignetUpgradeData.Skill.DISCIPLINE_FOUR);
    public static final RegistryObject<Item> DISCIPLINE_FIVE = registerDisciplineAugment(
            "discipline_five", SignetUpgradeData.Skill.DISCIPLINE_FIVE);
    public static final RegistryObject<Item> DISCIPLINE_SIX = registerDisciplineAugment(
            "discipline_six", SignetUpgradeData.Skill.DISCIPLINE_SIX);
    public static final RegistryObject<Item> DISCIPLINE_ASCETIC_BODY = registerDisciplineAugment(
            "discipline_ascetic_body", SignetUpgradeData.Skill.DISCIPLINE_ASCETIC_BODY);
    public static final RegistryObject<Item> DISCIPLINE_ASCETIC_LIFE = registerDisciplineAugment(
            "discipline_ascetic_life", SignetUpgradeData.Skill.DISCIPLINE_ASCETIC_LIFE);
    public static final RegistryObject<Item> DISCIPLINE_ASCETIC_PROOF = registerDisciplineAugment(
            "discipline_ascetic_proof", SignetUpgradeData.Skill.DISCIPLINE_ASCETIC_PROOF);
    public static final RegistryObject<Item> DISCIPLINE_BLESSING_PUNISHMENT = registerDisciplineAugment(
            "discipline_blessing_punishment", SignetUpgradeData.Skill.DISCIPLINE_BLESSING_PUNISHMENT);
    public static final RegistryObject<Item> DISCIPLINE_BLESSING_VIRTUE = registerDisciplineAugment(
            "discipline_blessing_virtue", SignetUpgradeData.Skill.DISCIPLINE_BLESSING_VIRTUE);
    public static final RegistryObject<Item> DISCIPLINE_BLESSING_RETURN = registerDisciplineAugment(
            "discipline_blessing_return", SignetUpgradeData.Skill.DISCIPLINE_BLESSING_RETURN);
    public static final RegistryObject<Item> GOLD_BIRD = registerGoldAugment(
            "gold_bird", SignetUpgradeData.Skill.GOLD_BIRD);
    public static final RegistryObject<Item> GOLD_ORCHARD = registerGoldAugment(
            "gold_orchard", SignetUpgradeData.Skill.GOLD_ORCHARD);
    public static final RegistryObject<Item> GOLD_WINE = registerGoldAugment(
            "gold_wine", SignetUpgradeData.Skill.GOLD_WINE);
    public static final RegistryObject<Item> GOLD_JADE = registerGoldAugment(
            "gold_jade", SignetUpgradeData.Skill.GOLD_JADE);
    public static final RegistryObject<Item> GOLD_ARIA = registerGoldCore(
            "gold_aria", SignetUpgradeData.GoldCore.ARIA);
    public static final RegistryObject<Item> GOLD_ECHO = registerGoldCore(
            "gold_echo", SignetUpgradeData.GoldCore.ECHO);
    public static final RegistryObject<Item> GOLD_ARIA_FERTILE = registerGoldAugment(
            "gold_aria_fertile", SignetUpgradeData.Skill.GOLD_ARIA_FERTILE);
    public static final RegistryObject<Item> GOLD_ARIA_LIFE = registerGoldAugment(
            "gold_aria_life", SignetUpgradeData.Skill.GOLD_ARIA_LIFE);
    public static final RegistryObject<Item> GOLD_ARIA_DAWN = registerGoldAugment(
            "gold_aria_dawn", SignetUpgradeData.Skill.GOLD_ARIA_DAWN);
    public static final RegistryObject<Item> GOLD_ECHO_WITHERED = registerGoldAugment(
            "gold_echo_withered", SignetUpgradeData.Skill.GOLD_ECHO_WITHERED);
    public static final RegistryObject<Item> GOLD_ECHO_COLD_WIND = registerGoldAugment(
            "gold_echo_cold_wind", SignetUpgradeData.Skill.GOLD_ECHO_COLD_WIND);
    public static final RegistryObject<Item> GOLD_ECHO_SILENT_NIGHT = registerGoldAugment(
            "gold_echo_silent_night", SignetUpgradeData.Skill.GOLD_ECHO_SILENT_NIGHT);
    public static final RegistryObject<Item> REVERIE_SIGNET_UPGRADE = registerSignetUpgrade(
            "reverie_signet_upgrade", SignetUpgradeData.Signet.REVERIE);
    public static final RegistryObject<Item> VICISSITUDE_SIGNET_UPGRADE = registerSignetUpgrade(
            "vicissitude_signet_upgrade", SignetUpgradeData.Signet.VICISSITUDE);
    public static final RegistryObject<Item> STARS_SIGNET_UPGRADE = registerSignetUpgrade(
            "stars_signet_upgrade", SignetUpgradeData.Signet.STARS);
    public static final RegistryObject<Item> INFINITY_SIGNET_UPGRADE = registerSignetUpgrade(
            "infinity_signet_upgrade", SignetUpgradeData.Signet.INFINITY);
    public static final RegistryObject<Item> DAYBREAK_SIGNET_UPGRADE = registerSignetUpgrade(
            "daybreak_signet_upgrade", SignetUpgradeData.Signet.DAYBREAK);
    public static final RegistryObject<Item> SETSURA_SIGNET_UPGRADE = registerSignetUpgrade(
            "setsura_signet_upgrade", SignetUpgradeData.Signet.SETSURA);
    public static final RegistryObject<Item> BODHI_SIGNET_UPGRADE = registerSignetUpgrade(
            "bodhi_signet_upgrade", SignetUpgradeData.Signet.BODHI);
    public static final RegistryObject<Item> DECIMATION_SIGNET_UPGRADE = registerSignetUpgrade(
            "decimation_signet_upgrade", SignetUpgradeData.Signet.DECIMATION);
    public static final RegistryObject<Item> HELIX_SIGNET_UPGRADE = registerSignetUpgrade(
            "helix_signet_upgrade", SignetUpgradeData.Signet.HELIX);
    public static final RegistryObject<Item> GOLD_SIGNET_UPGRADE = registerSignetUpgrade(
            "gold_signet_upgrade", SignetUpgradeData.Signet.GOLD);
    public static final RegistryObject<Item> DISCIPLINE_SIGNET_UPGRADE = registerSignetUpgrade(
            "discipline_signet_upgrade", SignetUpgradeData.Signet.DISCIPLINE);
    public static final RegistryObject<Item> DELIVERANCE_SIGNET_UPGRADE = registerSignetUpgrade(
            "deliverance_signet_upgrade", SignetUpgradeData.Signet.DELIVERANCE);
    public static final RegistryObject<Item> SIGNET_OF_EGO = ITEMS.register("signet_of_ego",
            () -> new SignetOfEgoItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AWAKENING =
            registerRemembranceSigil("awakening", 4);
    public static final RegistryObject<Item> NINE_LIVES =
            registerRemembranceSigil("nine_lives", 3);

    public static final RegistryObject<Item> FEAST_OF_EMPTINESS =
            registerRemembranceSigil("feast_of_emptiness", 2);

    public static final RegistryObject<Item> HOMETOWN =
            registerRemembranceSigil("hometown", 4);

    public static final RegistryObject<Item> RESOLVE =
            registerRemembranceSigil("resolve", 3);

    public static final RegistryObject<Item> MEMORY =
            registerRemembranceSigil("memory", 2);

    public static final RegistryObject<Item> HOME_LOST =
            registerRemembranceSigil("home_lost", 1);

    public static final RegistryObject<Item> THE_LONELY_MOON =
            registerRemembranceSigil("the_lonely_moon", 4);

    public static final RegistryObject<Item> GREY_SCALE_RAINBOW =
            registerRemembranceSigil("grey_scale_rainbow", 3);

    public static final RegistryObject<Item> RAINBOW_OF_ABSENCE =
            registerRemembranceSigil("rainbow_of_absence", 2);

    public static final RegistryObject<Item> BOUNDLESS_LOGOS =
            registerRemembranceSigil("boundless_logos", 4);

    public static final RegistryObject<Item> THE_FIRST_SCALE =
            registerRemembranceSigil("the_first_scale", 3);

    public static final RegistryObject<Item> FORBIDDEN_SEED =
            registerRemembranceSigil("forbidden_seed", 2);

    public static final RegistryObject<Item> FALSE_HOPE =
            registerRemembranceSigil("false_hope", 1);

    public static final RegistryObject<Item> OUT_OF_REACH =
            registerRemembranceSigil("out_of_reach", 4);

    public static final RegistryObject<Item> RAVENOUS_GULLY =
            registerRemembranceSigil("ravenous_gully", 3);

    public static final RegistryObject<Item> FRAGILE_FRIEND =
            registerRemembranceSigil("fragile_friend", 2);

    public static final RegistryObject<Item> TSUKIMI_HIMIKO =
            registerRemembranceSigil("tsukimi_himiko", 4);

    public static final RegistryObject<Item> STAINED_SAKURA =
            registerRemembranceSigil("stained_sakura", 3);

    public static final RegistryObject<Item> FORGET_ME_NOT =
            registerRemembranceSigil("forget_me_not", 2);

    public static final RegistryObject<Item> EMPTY_LIKE_SHALA =
            registerRemembranceSigil("empty_like_shala", 4);

    public static final RegistryObject<Item> HEAVY_AS_A_MILLION_LIVES =
            registerRemembranceSigil("heavy_as_a_million_lives", 3);

    public static final RegistryObject<Item> LIGHT_AS_A_BODHI_LEAF =
            registerRemembranceSigil("light_as_a_bodhi_leaf", 2);

    public static final RegistryObject<Item> AN_OLD_PALS_LEGACY =
            registerRemembranceSigil("an_old_pals_legacy", 4);

    public static final RegistryObject<Item> SHATTERED_SHACKLES =
            registerRemembranceSigil("shattered_shackles", 3);

    public static final RegistryObject<Item> MAD_KINGS_MASK =
            registerRemembranceSigil("mad_kings_mask", 2);

    public static final RegistryObject<Item> FALLING_IN_PAST_LIGHT =
            registerRemembranceSigil("falling_in_past_light", 4);

    public static final RegistryObject<Item> FARAWAY_SHIP =
            registerRemembranceSigil("faraway_ship", 3);

    public static final RegistryObject<Item> PSEUDO_MIRACLE =
            registerRemembranceSigil("pseudo_miracle", 2);

    public static final RegistryObject<Item> RUINED_LEGACY =
            registerRemembranceSigil("ruined_legacy", 1);

    public static final RegistryObject<Item> DREAMFUL_GOLD =
            registerRemembranceSigil("dreamful_gold", 4);

    public static final RegistryObject<Item> GOOD_OLD_DAYS =
            registerRemembranceSigil("good_old_days", 3);

    public static final RegistryObject<Item> GOLD_GOBLET =
            registerRemembranceSigil("gold_goblet", 2);

    public static final RegistryObject<Item> TIN_FLASK =
            registerRemembranceSigil("tin_flask", 1);

    public static final RegistryObject<Item> BOUNDLESS_FEELING =
            registerRemembranceSigil("boundless_feeling", 4);

    public static final RegistryObject<Item> PROOF_OF_GOOD_AND_EVIL =
            registerRemembranceSigil("proof_of_good_and_evil", 3);

    public static final RegistryObject<Item> VEIL_OF_TEARS =
            registerRemembranceSigil("veil_of_tears", 2);

    public static final RegistryObject<Item> KEY_TO_THE_DEEP =
            registerRemembranceSigil("key_to_the_deep", 1);

    public static final RegistryObject<Item> IT_WILL_BE_WRITTEN =
            registerRemembranceSigil("it_will_be_written", 4);

    public static final RegistryObject<Item> ABANDONED =
            registerRemembranceSigil("abandoned", 3);

    public static final RegistryObject<Item> BURDEN =
            registerRemembranceSigil("burden", 2);

    public static final RegistryObject<Item> THE_MOTH_INSIGNIA =
            registerRemembranceSigil("the_moth_insignia", 1);

    public static final RegistryObject<Item> THORNY_CROWN =
            registerRemembranceSigil("thorny_crown", 5);

    public static final RegistryObject<Item> BECAUSE_OF_YOU =
            registerRemembranceSigil("because_of_you", 4);

    public static final RegistryObject<Item> CRYSTAL_ROSE =
            registerRemembranceSigil("crystal_rose", 2);

    public static final RegistryObject<Item> FLAWLESS_KEY =
            ITEMS.register("flawless_key",
                    () -> new ElysianMaterialItem(
                            new Item.Properties().stacksTo(64),
                            "一柄仿若艺术品般的水晶钥匙。美丽，纯净，永恒。",
                            "但钥匙并不会只作为艺术品而存在，它注定要去打开某处的一道枷锁。",
                            "而这把无瑕的水晶之钥，它所存在的意义，也同样如此。"
                    ));

    public static final RegistryObject<Item> STONE_OF_CHOICE =
            ITEMS.register("stone_of_choice",
                    () -> new ElysianMaterialItem(
                            new Item.Properties().stacksTo(64),
                            "没有什么重量，仿佛并不真实存在一般的石块。",
                            "石块的色彩并不唯一，它们无时无刻不在流动、变化、融合。",
                            "但据说在某些时候，这种石块中的色彩又会完全固定下来。",
                            "而在那时，这石块也将变得异常沉重，远非常人所能持握。"
                    ));

    public static final RegistryObject<Item> DYING_KINDLING =
            ITEMS.register("dying_kindling",
                    () -> new ElysianMaterialItem(
                            new Item.Properties().stacksTo(64),
                            "保存在形似追忆之皿的容器中，即将熄灭的火种。其本质乃是英桀们记忆的碎片。",
                            "将其注入追忆之皿的话，英桀们或许会愿意和来访者讲述更多关于旧世代的记忆。",
                            "那个世代虽已逝去，但它所留下的火种却并未消亡。",
                            "逐火者们仍将追逐着这流火前行，并最终，抵达前人所未能至的未来。"
                    ));
    public static final RegistryObject<Item> COCOON_OF_FINALITY = ITEMS.register("cocoon_of_finality",
            () -> new CocoonOfFinalityItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HYPERION_LOG =
            ITEMS.register("hyperion_log",
                    () -> new HyperionLogItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CYBER_CHARACTER_BIOGRAPHY =
            ITEMS.register("cyber_character_biography",
                    () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> IVTV_BOOK = ITEMS.register("ivtv_book",
            () -> new IVTVBookItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHI_JI =
            ITEMS.register("shi_ji",
                    () -> new ShiJiBookItem(new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Item> FENGHUANG_DOWN =
            ITEMS.register("fenghuang_down",
                    () -> new FenghuangDownItem(new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Item> ABYSS_FLOWER =
            ITEMS.register("abyss_flower",
                    () -> new AbyssFlowerItem(new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Item> SKYFIRE_GREATSWORD =
            ITEMS.register("skyfire_greatsword",
                    () -> new SkyfireGreatswordItem(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> EDEN_STAR =
            ITEMS.register("eden_star",
                    () -> new EdenStarItem(new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Item> SOUL_STEEL_INGOT =
            ITEMS.register("soul_steel_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> SOUL_STEEL_NUGGET =
            ITEMS.register("soul_steel_nugget",
                    () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> SOUL_STEEL_BLOCK =
            ITEMS.register("soul_steel_block",
                    () -> new BlockItem(ModBlock.SOUL_STEEL_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> CORE_OF_REASON =
            ITEMS.register("core_of_reason",
                    () -> new CoreOfReasonItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_VOID =
            ITEMS.register("core_of_void",
                    () -> new CoreOfVoidItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_THUNDER     = ITEMS.register("core_of_thunder",
            () -> new CoreOfThunderItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_WIND        = ITEMS.register("core_of_wind",
            () -> new CoreOfWindItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_ICE         = ITEMS.register("core_of_ice",
            () -> new CoreOfIceItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_DEATH       = ITEMS.register("core_of_death",
            () -> new CoreOfDeathItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_FIRE        = ITEMS.register("core_of_fire",
            () -> new CoreOfFlameItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_SENTIENCE   = ITEMS.register("core_of_sentience",
            () -> new CoreOfSentienceItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_EARTH       = ITEMS.register("core_of_earth",
            () -> new CoreOfRockItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_DOMINANCE   = ITEMS.register("core_of_dominance",
            () -> new CoreOfDominationItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_BINDING     = ITEMS.register("core_of_binding",
            () -> new CoreOfBindingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_CORROSION   = ITEMS.register("core_of_corrosion",
            () -> new CoreOfCorruptionItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CORE_OF_ORIGIN = ITEMS.register("core_of_origin",
            () -> new CoreOfOriginItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BIOLOGICAL_RESIDUE =
            ITEMS.register("biological_residue",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> COMPRESSED_BIOLOGICAL_RESIDUE =
            ITEMS.register("compressed_biological_residue",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> DOUBLE_COMPRESSED_BIOLOGICAL_RESIDUE =
            ITEMS.register("double_compressed_biological_residue",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> LOW_HONKAI_ENERGY_INHIBITOR =
            ITEMS.register("low_honkai_energy_inhibitor",
                    () -> new HonkaiEnergyInhibitorItem(
                            new Item.Properties().stacksTo(16),
                            "低等",
                            5.0F,
                            0.05F
                    ));

    public static final RegistryObject<Item> HONKAI_ENERGY_INHIBITOR =
            ITEMS.register("honkai_energy_inhibitor",
                    () -> new HonkaiEnergyInhibitorItem(
                            new Item.Properties().stacksTo(16),
                            "中等",
                            2.0F,
                            0.10F
                    ));

    public static final RegistryObject<Item> HIGH_HONKAI_ENERGY_INHIBITOR =
            ITEMS.register("high_honkai_energy_inhibitor",
                    () -> new HonkaiEnergyInhibitorItem(
                            new Item.Properties().stacksTo(16),
                            "高等",
                            0.0F,
                            0.20F
                    ));
    public static final RegistryObject<Item> PARDOFELIS_MERCHANT_SPAWN_EGG =
            ITEMS.register("pardofelis_merchant_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.PARDOFELIS_MERCHANT,
                            0xD9B17D,
                            0x6B4A2E,
                            new Item.Properties()
                    ));
    public static final RegistryObject<Item> APONIA_NPC_SPAWN_EGG =
            ITEMS.register(
                    "aponia_npc_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.APONIA_NPC,
                            0xF4F1E8,
                            0x6E4D7B,
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> ELYSIA_NPC_SPAWN_EGG =
            ITEMS.register(
                    "elysia_npc_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.ELYSIA_NPC,
                            0xFFB6D9,
                            0xFF5FA2,
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> GRISEO_NPC_SPAWN_EGG =
            ITEMS.register(
                    "griseo_npc_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.GRISEO_NPC,
                            0xDDF7FF,
                            0x63B8FF,
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> VILL_V_NPC_SPAWN_EGG =
            ITEMS.register(
                    "vill_v_npc_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.VILL_V_NPC,
                            0x231A1A,
                            0xD6A56D,
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> EDEN_NPC_SPAWN_EGG =
            ITEMS.register(
                    "eden_npc_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.EDEN_NPC,
                            0xF3C46F,
                            0x8B3159,
                            new Item.Properties()
                    )
            );
    public static final RegistryObject<Item> ELYSIAN_REALM_ENTRANCE =
            ITEMS.register(
                    "elysian_realm_entrance",
                    () -> new BlockItem(
                            ModBlock.ELYSIAN_REALM_ENTRANCE.get(),
                            new Item.Properties()
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );
    public static final RegistryObject<Item> WHISPER_OF_THE_PAST =
            ITEMS.register("whisper_of_the_past",
                    () -> new WhisperOfThePastItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );
    public static final RegistryObject<Item> FLAWLESS_BENEDICTION_LEGACY =
            ITEMS.register(
                    "flawless_benediction_legacy",
                    () -> new FlawlessBenedictionLegacyItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );

    public static final RegistryObject<Item> TAB_ICON = ITEMS.register("tab_icon",
            () -> new Item(new Item.Properties()));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, lastsongofelysian.MODID);

    private static final java.util.List<RegistryObject<Item>> ADDITIONAL_SIGNETS = java.util.List.of(
            REVERIE_CAT_VERSE, REVERIE_BLADE_TAIL, REVERIE_STREET_JOY, REVERIE_PC,
            REVERIE_JOY, REVERIE_BOSS, REVERIE_CAT_PUNCH, REVERIE_LUCKY,
            REVERIE_DREAM, REVERIE_ZERO, REVERIE_SNOW, REVERIE_DREAM_TRUE,
            VICISSITUDE_TIME, VICISSITUDE_ROBE, VICISSITUDE_DREAM, VICISSITUDE_WORDS,
            VICISSITUDE_RESOLVE, VICISSITUDE_RED_DUST, VICISSITUDE_PROSPERITY,
            VICISSITUDE_FAMILIAR, VICISSITUDE_MEMORY, VICISSITUDE_REED,
            VICISSITUDE_FEATHER, VICISSITUDE_AWAKEN,
            STARS_YELLOW, STARS_BLACK, STARS_WHITE, STARS_GRAY, STARS_SHINING,
            STARS_WILDFLOWER_SHINING, STARS_FLAME_SHINING, STARS_LEAF_SHINING,
            STARS_DIM, STARS_WILDFLOWER_DIM, STARS_LEAF_DIM, STARS_FLAME_DIM,
            INFINITY_P, INFINITY_B, INFINITY_E, INFINITY_X, INFINITY_DEATH_X,
            INFINITY_UNKNOWN_X, INFINITY_NEWBORN_X, INFINITY_M,
            INFINITY_SACRIFICE_M, INFINITY_IMMORTAL_M, INFINITY_COLLAPSE_M,
            DAYBREAK_WING, DAYBREAK_HORN, DAYBREAK_BREATH, DAYBREAK_HEART,
            DAYBREAK_UNFADING_LIGHT, DAYBREAK_LIGHT_HERO, DAYBREAK_LIGHT_GOOD_EVIL,
            DAYBREAK_LIGHT_VOW, DAYBREAK_LONELY_NIGHT, DAYBREAK_NIGHT_DAWN,
            DAYBREAK_NIGHT_CHOICE, DAYBREAK_NIGHT_INTENT,
            SETSURA_RED_LEAVES, SETSURA_PEONY, SETSURA_WISTERIA, SETSURA_IRIS,
            SETSURA_SAKURA_CURTAIN, SETSURA_CURTAIN_RAIN_FOUR_LIGHTS,
            SETSURA_CURTAIN_MOON_SAKE, SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY,
            SETSURA_NIGHT_YAKSHA, SETSURA_YAKSHA_MOMENTARY_MYRIAD,
            SETSURA_YAKSHA_ENDLESS_EVIL, SETSURA_YAKSHA_ALL_FORMS,
            BODHI_HEAVENLY_EYE, BODHI_MIND_READING, BODHI_DIVINE_FOOT,
            BODHI_EXHAUSTION, BODHI_TRUE_WORD, BODHI_TRUE_SELFLESS,
            BODHI_TRUE_IMPERMANENCE, BODHI_TRUE_SILENCE, BODHI_SECRET_WORD,
            BODHI_SECRET_IMPERMANENCE, BODHI_SECRET_SELFLESS, BODHI_SECRET_SILENCE,
            DECIMATION_BLADE, DECIMATION_WUWANG, DECIMATION_WEISHEN,
            DECIMATION_BLOOD_RELEASE, DECIMATION_RELEASE_NONHUMAN,
            DECIMATION_RELEASE_ONE_PATH, DECIMATION_RELEASE_KALPAS,
            DECIMATION_BLOOD_RAIMENT, DECIMATION_RAIMENT_UNDYING,
            DECIMATION_RAIMENT_BURNING_SOUL, DECIMATION_RAIMENT_HOMELAND,
            HELIX_SPOON, HELIX_COBWEB, HELIX_SNEER, HELIX_REVERSED_SPIRAL,
            HELIX_REVERSED_DOOR, HELIX_REVERSED_HALL, HELIX_REVERSED_TRUTH,
            HELIX_CONCLUSION_SPIRAL, HELIX_CONCLUSION_PRISON,
            HELIX_CONCLUSION_ORIGIN, HELIX_CONCLUSION_POINTER,
            GOLD_BIRD, GOLD_ORCHARD, GOLD_WINE, GOLD_JADE, GOLD_ARIA,
            GOLD_ARIA_FERTILE, GOLD_ARIA_LIFE, GOLD_ARIA_DAWN, GOLD_ECHO,
            GOLD_ECHO_WITHERED, GOLD_ECHO_COLD_WIND, GOLD_ECHO_SILENT_NIGHT,
            DISCIPLINE_FOUR, DISCIPLINE_FIVE, DISCIPLINE_SIX,
            DISCIPLINE_ASCETICISM, DISCIPLINE_ASCETIC_BODY,
            DISCIPLINE_ASCETIC_LIFE, DISCIPLINE_ASCETIC_PROOF,
            DISCIPLINE_BLESSING, DISCIPLINE_BLESSING_PUNISHMENT,
            DISCIPLINE_BLESSING_VIRTUE, DISCIPLINE_BLESSING_RETURN,
            DELIVERANCE_HUNTERS_MASK, DELIVERANCE_RESTRAINERS_RELIC,
            DELIVERANCE_SEEKERS_ROBE, DELIVERANCE_KINGS_SWORD,
            DELIVERANCE_KINGS_MUSTER, DELIVERANCE_KINGS_EXPEDITION,
            DELIVERANCE_KINGS_ECHO, DELIVERANCE_LONE_SHADOW,
            DELIVERANCE_LONE_RESIDUAL_DREAM, DELIVERANCE_LONE_DECISION,
            DELIVERANCE_LONE_TRIUMPH
    );

    public static final RegistryObject<CreativeModeTab> LSE_TAB = CREATIVE_MODE_TABS.register("lse_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(TAB_ICON.get()))
                    .title(Component.translatable("itemGroup.lastsongofelysian"))
                    .displayItems((params, output) -> {
                        output.accept(COCOON_OF_FINALITY.get());
                        output.accept(SIGNET_OF_EGO.get());
                        output.accept(SIGNET_OF_REVERIE.get());
                        output.accept(SIGNET_OF_VICISSITUDE.get());
                        output.accept(SIGNET_OF_STARS.get());
                        output.accept(SIGNET_OF_INFINITY.get());
                        output.accept(SIGNET_OF_DAYBREAK.get());
                        output.accept(SIGNET_OF_SETSURA.get());
                        output.accept(SIGNET_OF_BODHI.get());
                        output.accept(SIGNET_OF_DECIMATION.get());
                        output.accept(SIGNET_OF_HELIX.get());
                        output.accept(SIGNET_OF_GOLD.get());
                        output.accept(SIGNET_OF_DISCIPLINE.get());
                        output.accept(SIGNET_OF_DELIVERANCE.get());
                        output.accept(REVERIE_SIGNET_UPGRADE.get());
                        output.accept(VICISSITUDE_SIGNET_UPGRADE.get());
                        output.accept(STARS_SIGNET_UPGRADE.get());
                        output.accept(INFINITY_SIGNET_UPGRADE.get());
                        output.accept(DAYBREAK_SIGNET_UPGRADE.get());
                        output.accept(SETSURA_SIGNET_UPGRADE.get());
                        output.accept(BODHI_SIGNET_UPGRADE.get());
                        output.accept(DECIMATION_SIGNET_UPGRADE.get());
                        output.accept(HELIX_SIGNET_UPGRADE.get());
                        output.accept(GOLD_SIGNET_UPGRADE.get());
                        output.accept(DISCIPLINE_SIGNET_UPGRADE.get());
                        output.accept(DELIVERANCE_SIGNET_UPGRADE.get());
                        output.accept(AWAKENING.get());
                        output.accept(NINE_LIVES.get());
                        output.accept(FEAST_OF_EMPTINESS.get());

                        output.accept(HOMETOWN.get());
                        output.accept(RESOLVE.get());
                        output.accept(MEMORY.get());
                        output.accept(HOME_LOST.get());

                        output.accept(THE_LONELY_MOON.get());
                        output.accept(GREY_SCALE_RAINBOW.get());
                        output.accept(RAINBOW_OF_ABSENCE.get());

                        output.accept(BOUNDLESS_LOGOS.get());
                        output.accept(THE_FIRST_SCALE.get());
                        output.accept(FORBIDDEN_SEED.get());
                        output.accept(FALSE_HOPE.get());

                        output.accept(OUT_OF_REACH.get());
                        output.accept(RAVENOUS_GULLY.get());
                        output.accept(FRAGILE_FRIEND.get());

                        output.accept(TSUKIMI_HIMIKO.get());
                        output.accept(STAINED_SAKURA.get());
                        output.accept(FORGET_ME_NOT.get());

                        output.accept(EMPTY_LIKE_SHALA.get());
                        output.accept(HEAVY_AS_A_MILLION_LIVES.get());
                        output.accept(LIGHT_AS_A_BODHI_LEAF.get());

                        output.accept(AN_OLD_PALS_LEGACY.get());
                        output.accept(SHATTERED_SHACKLES.get());
                        output.accept(MAD_KINGS_MASK.get());

                        output.accept(FALLING_IN_PAST_LIGHT.get());
                        output.accept(FARAWAY_SHIP.get());
                        output.accept(PSEUDO_MIRACLE.get());
                        output.accept(RUINED_LEGACY.get());

                        output.accept(DREAMFUL_GOLD.get());
                        output.accept(GOOD_OLD_DAYS.get());
                        output.accept(GOLD_GOBLET.get());
                        output.accept(TIN_FLASK.get());

                        output.accept(BOUNDLESS_FEELING.get());
                        output.accept(PROOF_OF_GOOD_AND_EVIL.get());
                        output.accept(VEIL_OF_TEARS.get());
                        output.accept(KEY_TO_THE_DEEP.get());

                        output.accept(IT_WILL_BE_WRITTEN.get());
                        output.accept(ABANDONED.get());
                        output.accept(BURDEN.get());
                        output.accept(THE_MOTH_INSIGNIA.get());

                        output.accept(THORNY_CROWN.get());
                        output.accept(BECAUSE_OF_YOU.get());
                        output.accept(CRYSTAL_ROSE.get());
                        output.accept(FLAWLESS_KEY.get());
                        output.accept(STONE_OF_CHOICE.get());
                        output.accept(DYING_KINDLING.get());
                        output.accept(CORE_OF_REASON.get());
                        output.accept(CORE_OF_VOID.get());
                        output.accept(CORE_OF_THUNDER.get());
                        output.accept(CORE_OF_WIND.get());
                        output.accept(CORE_OF_ICE.get());
                        output.accept(CORE_OF_DEATH.get());
                        output.accept(CORE_OF_FIRE.get());
                        output.accept(CORE_OF_SENTIENCE.get());
                        output.accept(CORE_OF_EARTH.get());
                        output.accept(CORE_OF_DOMINANCE.get());
                        output.accept(CORE_OF_BINDING.get());
                        output.accept(CORE_OF_CORROSION.get());
                        output.accept(CORE_OF_ORIGIN.get());
                        output.accept(WHISPER_OF_THE_PAST.get());
                        output.accept(FLAWLESS_BENEDICTION_LEGACY.get());
                        output.accept(FENGHUANG_DOWN.get());
                        output.accept(ABYSS_FLOWER.get());
                        output.accept(SKYFIRE_GREATSWORD.get());
                        output.accept(EDEN_STAR.get());
                        output.accept(SHINY_SILVER.get());
                        output.accept(SILVER_POUCH.get());
                        output.accept(BIOLOGICAL_RESIDUE.get());
                        output.accept(COMPRESSED_BIOLOGICAL_RESIDUE.get());
                        output.accept(DOUBLE_COMPRESSED_BIOLOGICAL_RESIDUE.get());
                        output.accept(LOW_HONKAI_ENERGY_INHIBITOR.get());
                        output.accept(HONKAI_ENERGY_INHIBITOR.get());
                        output.accept(HIGH_HONKAI_ENERGY_INHIBITOR.get());
                        output.accept(HYPERION_LOG.get());
                        output.accept(CYBER_CHARACTER_BIOGRAPHY.get());
                        output.accept(IVTV_BOOK.get());
                        output.accept(SHI_JI.get());
                        output.accept(SOUL_STEEL_NUGGET.get());
                        output.accept(SOUL_STEEL_INGOT.get());
                        output.accept(SOUL_STEEL_BLOCK.get());
                        output.accept(PARDOFELIS_MERCHANT_SPAWN_EGG.get());
                        output.accept(APONIA_NPC_SPAWN_EGG.get());
                        output.accept(ELYSIA_NPC_SPAWN_EGG.get());
                        output.accept(GRISEO_NPC_SPAWN_EGG.get());
                        output.accept(VILL_V_NPC_SPAWN_EGG.get());
                        output.accept(EDEN_NPC_SPAWN_EGG.get());
                        output.accept(ELYSIAN_REALM_ENTRANCE.get());
                    })
                    .build()
    );

    public static final RegistryObject<CreativeModeTab> ADDITIONAL_SIGNETS_TAB =
            CREATIVE_MODE_TABS.register("additional_signets_tab",
                    () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(SIGNET_OF_EGO.get()))
                            .title(Component.translatable(
                                    "itemGroup.lastsongofelysian.additional_signets"))
                            .displayItems((params, output) -> ADDITIONAL_SIGNETS.forEach(
                                    item -> output.accept(item.get())))
                            .build());

    private static RegistryObject<Item> registerSignetUpgrade(
            String id,
            SignetUpgradeData.Signet signet
    ) {
        return ITEMS.register(
                id,
                () -> new SignetUpgradeItem(
                        new Item.Properties().stacksTo(64),
                        signet
                )
        );
    }

    private static RegistryObject<Item> registerDisciplineAugment(
            String id,
            SignetUpgradeData.Skill skill
    ) {
        return ITEMS.register(
                id,
                () -> new DisciplineSecondarySignetItem(
                        new Item.Properties().stacksTo(1),
                        skill
                )
        );
    }

    private static RegistryObject<Item> registerGoldCore(
            String id,
            SignetUpgradeData.GoldCore core
    ) {
        return ITEMS.register(id, () -> new GoldFusionSignetItem(
                new Item.Properties().stacksTo(1), core));
    }

    private static RegistryObject<Item> registerHelixCore(
            String id,
            SignetUpgradeData.HelixCore core
    ) {
        return ITEMS.register(id, () -> new HelixFusionSignetItem(
                new Item.Properties().stacksTo(1), core));
    }

    private static RegistryObject<Item> registerDecimationCore(
            String id, SignetUpgradeData.DecimationCore core
    ) {
        return ITEMS.register(id, () -> new DecimationFusionSignetItem(
                new Item.Properties().stacksTo(1), core));
    }

    private static RegistryObject<Item> registerBodhiCore(
            String id, SignetUpgradeData.BodhiCore core
    ) {
        return ITEMS.register(id, () -> new BodhiFusionSignetItem(
                new Item.Properties().stacksTo(1), core));
    }

    private static RegistryObject<Item> registerSetsuraCore(
            String id, SignetUpgradeData.SetsuraCore core
    ) {
        return ITEMS.register(id, () -> new SetsuraFusionSignetItem(
                new Item.Properties().stacksTo(1), core));
    }

    private static RegistryObject<Item> registerDaybreakCore(
            String id, SignetUpgradeData.DaybreakCore core
    ) {
        return ITEMS.register(id, () -> new DaybreakFusionSignetItem(
                new Item.Properties().stacksTo(1), core));
    }

    private static RegistryObject<Item> registerDaybreakAugment(
            String id, SignetUpgradeData.Skill skill
    ) {
        return ITEMS.register(id, () -> new DaybreakFusionSignetItem(
                new Item.Properties().stacksTo(1), skill));
    }

    private static RegistryObject<Item> registerExtensionCore(
            String id, SignetUpgradeData.ExtensionCore core
    ) {
        return ITEMS.register(id, () -> new ExtensionFusionSignetItem(
                new Item.Properties().stacksTo(1), core));
    }

    private static RegistryObject<Item> registerExtensionAugment(
            String id, SignetUpgradeData.Skill skill
    ) {
        return ITEMS.register(id, () -> new ExtensionFusionSignetItem(
                new Item.Properties().stacksTo(1), skill));
    }

    private static RegistryObject<Item> registerSetsuraAugment(
            String id, SignetUpgradeData.Skill skill
    ) {
        return ITEMS.register(id, () -> new SetsuraFusionSignetItem(
                new Item.Properties().stacksTo(1), skill));
    }

    private static RegistryObject<Item> registerBodhiAugment(
            String id, SignetUpgradeData.Skill skill
    ) {
        return ITEMS.register(id, () -> new BodhiFusionSignetItem(
                new Item.Properties().stacksTo(1), skill));
    }

    private static RegistryObject<Item> registerDecimationAugment(
            String id, SignetUpgradeData.Skill skill
    ) {
        return ITEMS.register(id, () -> new DecimationFusionSignetItem(
                new Item.Properties().stacksTo(1), skill));
    }

    private static RegistryObject<Item> registerHelixAugment(
            String id,
            SignetUpgradeData.Skill skill
    ) {
        return ITEMS.register(id, () -> new HelixFusionSignetItem(
                new Item.Properties().stacksTo(1), skill));
    }

    private static RegistryObject<Item> registerGoldAugment(
            String id,
            SignetUpgradeData.Skill skill
    ) {
        return ITEMS.register(id, () -> new GoldFusionSignetItem(
                new Item.Properties().stacksTo(1), skill));
    }

    private static RegistryObject<Item> registerRemembranceSigil(
            String id,
            int stars
    ) {
        return ITEMS.register(
                id,
                () -> new RemembranceSigilItem(
                        new Item.Properties().stacksTo(1),
                        stars
                )
        );
    }
}
