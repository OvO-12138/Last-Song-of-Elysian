package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.item.SilverPouchItem;
import com.ovo.lastsongofelysian.item.ExtensionFusionSignetItem;
import com.ovo.lastsongofelysian.event.ReverieSignetSystem;
import com.ovo.lastsongofelysian.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public final class SignetUpgradeData {

    public static final String LEGACY_NBT_LEVEL = "LSESignetUpgradeLevel";
    public static final String NBT_SKILL_LEVELS = "LSESignetSkillLevels";
    public static final String NBT_PLAYER_SKILL_LEVELS = "LSEPersistentSignetSkillLevels";
    public static final String NBT_DELIVERANCE_SECONDARY_UNLOCKS = "LSEDeliveranceSecondaryUnlocks";
    public static final String NBT_DELIVERANCE_CORE = "LSEDeliveranceCore";
    public static final String NBT_DELIVERANCE_CORE_SIGNETS = "LSEDeliveranceCoreSignets";
    public static final String NBT_DELIVERANCE_CORE_PURCHASED = "LSEDeliveranceCorePurchased";
    public static final String NBT_DELIVERANCE_CORE_SIGNETS_PURCHASED = "LSEDeliveranceCoreSignetsPurchased";
    public static final String NBT_DELIVERANCE_CORE_SILVER_SPENT = "LSEDeliveranceCoreSilverSpent";
    public static final String NBT_DELIVERANCE_CORE_EXPERIENCE_SPENT = "LSEDeliveranceCoreExperienceSpent";
    public static final String NBT_DISCIPLINE_CORE = "LSEDisciplineCore";
    public static final String NBT_DISCIPLINE_CORE_SIGNETS = "LSEDisciplineCoreSignets";
    public static final String NBT_DISCIPLINE_CORE_PURCHASED = "LSEDisciplineCorePurchased";
    public static final String NBT_DISCIPLINE_CORE_SIGNETS_PURCHASED = "LSEDisciplineCoreSignetsPurchased";
    public static final String NBT_DISCIPLINE_SECONDARY_UNLOCKS = "LSEDisciplineSecondaryUnlocks";
    public static final String NBT_GOLD_CORE = "LSEGoldCore";
    public static final String NBT_GOLD_CORE_SIGNETS = "LSEGoldCoreSignets";
    public static final String NBT_GOLD_CORE_PURCHASED = "LSEGoldCorePurchased";
    public static final String NBT_GOLD_CORE_SIGNETS_PURCHASED = "LSEGoldCoreSignetsPurchased";
    public static final String NBT_GOLD_SECONDARY_UNLOCKS = "LSEGoldSecondaryUnlocks";
    public static final String NBT_HELIX_CORE = "LSEHelixCore";
    public static final String NBT_HELIX_CORE_SIGNETS = "LSEHelixCoreSignets";
    public static final String NBT_HELIX_CORE_PURCHASED = "LSEHelixCorePurchased";
    public static final String NBT_HELIX_CORE_SIGNETS_PURCHASED = "LSEHelixCoreSignetsPurchased";
    public static final String NBT_HELIX_SECONDARY_UNLOCKS = "LSEHelixSecondaryUnlocks";
    public static final String NBT_DECIMATION_CORE = "LSEDecimationCore";
    public static final String NBT_DECIMATION_CORE_SIGNETS = "LSEDecimationCoreSignets";
    public static final String NBT_DECIMATION_CORE_PURCHASED = "LSEDecimationCorePurchased";
    public static final String NBT_DECIMATION_CORE_SIGNETS_PURCHASED = "LSEDecimationCoreSignetsPurchased";
    public static final String NBT_DECIMATION_SECONDARY_UNLOCKS = "LSEDecimationSecondaryUnlocks";
    public static final String NBT_BODHI_CORE = "LSEBodhiCore";
    public static final String NBT_BODHI_CORE_SIGNETS = "LSEBodhiCoreSignets";
    public static final String NBT_BODHI_CORE_PURCHASED = "LSEBodhiCorePurchased";
    public static final String NBT_BODHI_CORE_SIGNETS_PURCHASED = "LSEBodhiCoreSignetsPurchased";
    public static final String NBT_BODHI_SECONDARY_UNLOCKS = "LSEBodhiSecondaryUnlocks";
    public static final String NBT_DAYBREAK_CORE = "LSEDaybreakCore";
    public static final String NBT_DAYBREAK_CORE_SIGNETS = "LSEDaybreakCoreSignets";
    public static final String NBT_DAYBREAK_CORE_PURCHASED = "LSEDaybreakCorePurchased";
    public static final String NBT_DAYBREAK_CORE_SIGNETS_PURCHASED = "LSEDaybreakCoreSignetsPurchased";
    public static final String NBT_DAYBREAK_SECONDARY_UNLOCKS = "LSEDaybreakSecondaryUnlocks";
    public static final String NBT_SETSURA_CORE = "LSESetsuraCore";
    public static final String NBT_SETSURA_CORE_SIGNETS = "LSESetsuraCoreSignets";
    public static final String NBT_SETSURA_CORE_PURCHASED = "LSESetsuraCorePurchased";
    public static final String NBT_SETSURA_CORE_SIGNETS_PURCHASED = "LSESetsuraCoreSignetsPurchased";
    public static final String NBT_SETSURA_SECONDARY_UNLOCKS = "LSESetsuraSecondaryUnlocks";
    public static final String NBT_SHOP_PAID_SILVER_PER_ITEM = "LSEShopPaidSilverPerItem";
    private static final String NBT_CORE_ITEM_OWNER = "LSECoreItemOwner";
    private static final String NBT_PLAYER_PERSISTED = "PlayerPersisted";
    public static final int MAX_LEVEL = 3;
    public static final int UPGRADE_ITEM_COST = 1;
    public static final int EXPERIENCE_LEVEL_COST = 10;
    private static final float[] FACTORS = {1.0F, 1.15F, 1.30F, 1.50F};
    private static final float EGO_MULTIPLIER = 2.314520F;

    public enum Signet {
        REVERIE("空梦", () -> ModItems.SIGNET_OF_REVERIE.get()),
        VICISSITUDE("浮生", () -> ModItems.SIGNET_OF_VICISSITUDE.get()),
        STARS("繁星", () -> ModItems.SIGNET_OF_STARS.get()),
        INFINITY("无限", () -> ModItems.SIGNET_OF_INFINITY.get()),
        DAYBREAK("旭光", () -> ModItems.SIGNET_OF_DAYBREAK.get()),
        SETSURA("刹那", () -> ModItems.SIGNET_OF_SETSURA.get()),
        BODHI("天慧", () -> ModItems.SIGNET_OF_BODHI.get()),
        DECIMATION("鏖灭", () -> ModItems.SIGNET_OF_DECIMATION.get()),
        HELIX("螺旋", () -> ModItems.SIGNET_OF_HELIX.get()),
        GOLD("黄金", () -> ModItems.SIGNET_OF_GOLD.get()),
        DISCIPLINE("戒律", () -> ModItems.SIGNET_OF_DISCIPLINE.get()),
        DELIVERANCE("救世", () -> ModItems.SIGNET_OF_DELIVERANCE.get());

        private final String displayName;
        private final Supplier<Item> item;

        Signet(String displayName, Supplier<Item> item) {
            this.displayName = displayName;
            this.item = item;
        }

        public String displayName() {
            return this.displayName;
        }

        public Item item() {
            return this.item.get();
        }
    }

    public enum DeliveranceCore {
        KINGS_SWORD(
                1,
                "救世者的王剑",
                "开启武器必杀技后，进入终末之战状态。\n期间所有「救世」普通刻印提供的加成效果提高50%，持续7秒。"
        ),
        LONE_SHADOW(
                2,
                "救世者的孤影",
                "释放武器必杀技后五秒内的攻击累计命中20次后，进入救世之战状态。\n救世之战状态下所有「救世」普通刻印提供的加成效果强制生效，持续10秒。"
        );

        private final int id;
        private final String displayName;
        private final String description;

        DeliveranceCore(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }

        public int id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public String description() {
            return this.description;
        }

        public static DeliveranceCore fromId(int id) {
            for (DeliveranceCore core : values()) {
                if (core.id == id) return core;
            }
            return null;
        }
    }

    public enum DisciplineCore {
        ASCETICISM(
                1,
                "汝，当受万般戒律",
                "规诫计数达到90时，进入「苦修」状态，获得25%全伤害加成，每拥有一个「戒律」刻印，造成的伤害提高5%。\n武器必杀技命中敌方单位时，伤害穿透提高40%，持续20秒，重复触发刷新持续时间。"
        ),
        BLESSING(
                2,
                "汝，当为戒律所佑",
                "「戒律」普通刻印每秒获得的「规诫」计数增加100%。\n「规诫」计数超过80点时，每5秒触发一次「汝，当知罪愆如山」，造成1000%角色攻击力的伤害；每命中一名敌方单位，全伤害提高10%，上限40%，持续10秒，重复触发刷新。"
        );

        private final int id;
        private final String displayName;
        private final String description;

        DisciplineCore(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }

        public int id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public String description() {
            return this.description;
        }

        public static DisciplineCore fromId(int id) {
            for (DisciplineCore core : values()) {
                if (core.id == id) return core;
            }
            return null;
        }
    }

    public enum GoldCore {
        ARIA(
                1,
                "黄金的咏叹",
                "累计回复100点能量后，攻击命中敌方单位时释放3道【咏叹的雷鸣】，每道对随机敌方单位造成450%角色基础攻击力的伤害，并施加6秒【沉醉】状态。\n处于【沉醉】状态下的敌方单位受到的全伤害提高15%，角色获得20%的元素穿透，持续10秒，重复触发刷新。"
        ),
        ECHO(
                2,
                "黄金的余音",
                "消耗能量时进入【终末余音】状态，并获得与消耗量相同的充能。\n【终末余音】状态下，每具有10点能量上限，每秒提高4%伤害，上限80%；每秒扣除与当前持续时间相同的充能，充能不足时重置并结束【终末余音】状态。"
        );

        private final int id;
        private final String displayName;
        private final String description;

        GoldCore(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }

        public int id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public String description() {
            return this.description;
        }

        public static GoldCore fromId(int id) {
            for (GoldCore core : values()) {
                if (core.id == id) return core;
            }
            return null;
        }
    }

    public enum HelixCore {
        REVERSED_SPIRAL(
                1,
                "幕间剧「逆转的螺旋」",
                "使用武器技时，对前方敌方单位发动「惊奇魔术」，造成3×200%角色基础攻击力的伤害，并使敌方单位在8秒内受到的伤害提高25%。\n触发间隔5秒，伤害视为武器技伤害。"
        ),
        CONCLUSION_SPIRAL(
                2,
                "幕间剧「颠末的螺旋」",
                "使用武器技时，在前方放置「特型武装 · 型号四」，1秒后爆炸造成100点伤害。\n爆炸后形成1.5秒全局时空断裂，并生成持续6秒的小型引力场；引力场每秒造成200%角色基础攻击力的伤害，同时最多存在一个。"
        );

        private final int id;
        private final String displayName;
        private final String description;

        HelixCore(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }

        public int id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public String description() {
            return this.description;
        }

        public static HelixCore fromId(int id) {
            for (HelixCore core : values()) {
                if (core.id == id) return core;
            }
            return null;
        }
    }

    public enum DecimationCore {
        BLOOD_RELEASE(
                1,
                "鏖斗 · 鏖战 · 鏖杀 · 鏖灭",
                "每秒为【燃血解放】增加10点充能，每损失5点生命，每秒额外增加50点充能。\n充能达到1000时，下次攻击清空充能并释放【燃血解放】，造成1500%角色基础攻击力的大范围自适应伤害。点燃状态下的敌方单位受到的伤害提高20%。"
        ),
        BLOOD_RAIMENT(
                2,
                "鏖兵 · 鏖剪 · 鏖馘 · 鏖灭",
                "当前生命无法超过最大生命的40%。战斗状态下，生命回复超过0/20/40点时，分别增加回复量10%/80%/100%的【燃血狱衣】充能，单次最多充能900。\n充能上限为1800，每点充能提高0.1点生命上限。"
        );

        private final int id;
        private final String displayName;
        private final String description;

        DecimationCore(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }

        public int id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public String description() {
            return this.description;
        }

        public static DecimationCore fromId(int id) {
            for (DecimationCore core : values()) {
                if (core.id == id) return core;
            }
            return null;
        }
    }

    public enum BodhiCore {
        TRUE_WORD(
                1,
                "天慧之真言",
                "连击数达到5/10/15时，以自身为中心释放【因果转轮】，击退周围生物并造成50%/100%/150%角色基础攻击力的伤害。\n被命中的单位受到的全伤害提高30%，持续5秒，重复触发刷新。"
        ),
        SECRET_WORD(
                2,
                "天慧之秘言",
                "连击数达到6/10/15时，对随机一个单位释放【天慧·开眼】，造成800%/900%/1000%角色基础攻击力的伤害。\n同时施加持续3秒的【恍惚】状态，使其受到攻击时额外增加1连击数。"
        );

        private final int id;
        private final String displayName;
        private final String description;

        BodhiCore(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }

        public int id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public String description() {
            return this.description;
        }

        public static BodhiCore fromId(int id) {
            for (BodhiCore core : values()) {
                if (core.id == id) return core;
            }
            return null;
        }
    }

    public enum SetsuraCore {
        SAKURA_CURTAIN(
                1,
                "刹那一刀「樱上幕」",
                "【樱幕结界】未开启且累计触发3次闪避后，开启【樱幕结界】，静止所有敌方单位3秒。\n结界结束后，使所有敌方单位流失10%最大生命，并使自身获得20%伤害穿透，持续5秒。"
        ),
        NIGHT_YAKSHA(
                2,
                "刹那一刀「苦夜叉」",
                "每10秒进入【瞬心】状态。处于【瞬心】状态时触发闪避，会对所有敌方单位释放【刹那一刀「业狩」】，造成7×200%角色基础攻击力的伤害。\n释放后获得20%伤害穿透，持续5秒。"
        );

        private final int id;
        private final String displayName;
        private final String description;

        SetsuraCore(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }

        public int id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public String description() {
            return this.description;
        }

        public static SetsuraCore fromId(int id) {
            for (SetsuraCore core : values()) {
                if (core.id == id) return core;
            }
            return null;
        }
    }

    public enum DaybreakCore {
        UNFADING_LIGHT(
                1,
                "旭光，长明不落",
                "攻击命中处于10层或以上【撕裂】状态的单位时，触发一次额外攻击，造成15%角色基础攻击力的伤害。\n若单位具有护甲，则额外造成50%角色基础攻击力的伤害；该攻击获得5%伤害穿透，冷却5秒。"
        ),
        LONELY_NIGHT(
                2,
                "旭光，长夜孤悬",
                "【撕裂】状态同时对角色自身生效。每层【撕裂】使角色造成的全伤害提高1%、生命值上限提高5%，并使角色每秒减少1点生命值，最低降至1点。\n角色攻击时获得1层【撕裂】，触发间隔1秒。"
        );

        private final int id;
        private final String displayName;
        private final String description;

        DaybreakCore(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }

        public int id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public String description() {
            return this.description;
        }

        public static DaybreakCore fromId(int id) {
            for (DaybreakCore core : values()) {
                if (core.id == id) return core;
            }
            return null;
        }
    }

    public enum ExtensionCore {
        INFINITY_X(1, Signet.INFINITY, "无限的「X」", "玩家召唤物或受玩家控制的友方单位攻击时进入【思维同化】状态，持续8秒。"),
        INFINITY_M(2, Signet.INFINITY, "无限的「M」", "每秒增加10%进度；达到100%时进入持续6秒的【意识同步】状态。"),
        STARS_SHINING(1, Signet.STARS, "像是繁星，闪耀着的",
                "场上有敌方单位染上1/2/3种不同颜色时，玩家对敌方单位造成的全伤害提高10%/20%/30%。\n每持有一种英桀核心刻印，使自身的伤害穿透提高5%，上限40%。"),
        STARS_DIM(2, Signet.STARS, "像是繁星，黯淡了的",
                "对敌方单位进行染色时，若其已被染上其他颜色，则触发一次【色相调和】，对其周围15格内的敌方单位造成30%角色基础攻击力的伤害，冷却1秒。\n根据增幅刻印为角色自身染上对应的紫色、绿色或橙色油墨；角色身上每有一种油墨，造成的伤害提高15%。"),
        VICISSITUDE_RESOLVE(1, Signet.VICISSITUDE, "浮生历历，百态无常", "每10秒获得1层【战士的决意】，最多6层；受伤时消耗1层抵消伤害。"),
        VICISSITUDE_MEMORY(2, Signet.VICISSITUDE, "浮生茫茫，百态皆苦", "每10秒获得1层【战士的回忆】，最多6层。拥有期间所有「浮生」的普通刻印提供的加成效果立刻达到最大叠加层数；被敌方单位攻击并受到伤害时，减少1层【战士的回忆】抵消本次伤害并无敌0.5秒。"),
        REVERIE_JOY(1, Signet.REVERIE, "空梦·空集·空我·空欢", "银币数量达到1000/2000时，所有【空梦】普通刻印效果提高25%/50%。"),
        REVERIE_DREAM(2, Signet.REVERIE, "空幻·空灵·空想·空梦", "每十次刷新商店的前两次免费；每消费2000银币获得1层【贵宾待遇】。" );

        private final int id;
        private final Signet signet;
        private final String displayName;
        private final String description;

        ExtensionCore(int id, Signet signet, String displayName, String description) {
            this.id = id;
            this.signet = signet;
            this.displayName = displayName;
            this.description = description;
        }

        public int id() { return this.id; }
        public Signet signet() { return this.signet; }
        public String displayName() { return this.displayName; }
        public String description() { return this.description; }

        public static ExtensionCore fromId(Signet signet, int id) {
            for (ExtensionCore core : values()) {
                if (core.signet == signet && core.id == id) return core;
            }
            return null;
        }
    }

    public enum Skill {
        REVERIE_EXPERIENCE(Signet.REVERIE, "reverie_experience", "人生，空梦一场",
                "经验获取 +100%", "经验获取 +115%", "经验获取 +130%", "经验获取 +150%"),
        REVERIE_DISCOUNT(Signet.REVERIE, "reverie_discount", "值钱的，闪闪的",
                "商店价格为原价的80%", "商店价格为原价的75%", "商店价格为原价的72%", "商店价格为原价的70%"),
        REVERIE_SILVER(Signet.REVERIE, "reverie_silver", "行商者的哲学",
                "银币获取 +10%", "银币获取 +11.5%", "银币获取 +13%", "银币获取 +15%"),
        REVERIE_CAT_VERSE(Signet.REVERIE, "reverie_cat_verse", "猫猫之箴言",
                "根据银币袋里的闪亮的银币数提高造成的全伤害，每100枚提高1%，上限100%"),
        REVERIE_BLADE_TAIL(Signet.REVERIE, "reverie_blade_tail", "钢刃逆卷之「尾」",
                "根据银币袋里的闪亮的银币数提高伤害穿透，每100枚提高0.8%，上限80%"),
        REVERIE_STREET_JOY(Signet.REVERIE, "reverie_street_joy", "街巷的童欢",
                "每100枚闪亮的银币提高1点生命上限和0.1点能量上限"),
        REVERIE_PC(Signet.REVERIE, "reverie_pc", "灵动的「P&C」",
                "每100枚闪亮的银币每秒回复1点生命和0.1点能量；受到伤害后暂停5秒"),
        REVERIE_BOSS(Signet.REVERIE, "reverie_boss", "即兴短剧「老板」",
                "触发闪避时发起【帕朵的秘技】，造成50%角色基础攻击力伤害，每100银币额外附加10%，冷却15秒"),
        REVERIE_CAT_PUNCH(Signet.REVERIE, "reverie_cat_punch", "刹那一爪「猫猫拳」",
                "释放必杀技时发起【帕朵的秘技】，造成50%角色基础攻击力伤害，每100银币额外附加10%，冷却15秒"),
        REVERIE_LUCKY(Signet.REVERIE, "reverie_lucky", "喵，此地命不该绝",
                "每10秒获得【好运气】护盾；每100银币提高0.8%伤害穿透，护盾基础可抵挡2次伤害，每1000银币增加1次"),
        REVERIE_ZERO(Signet.REVERIE, "reverie_zero", "即兴魔术「归零」",
                "商店价格额外减少5%，【贵宾待遇】每层全伤害加成提高10%"),
        REVERIE_SNOW(Signet.REVERIE, "reverie_snow", "刹那一爪「雪里藏」",
                "【贵宾待遇】上限增加1层"),
        REVERIE_DREAM_TRUE(Signet.REVERIE, "reverie_dream_true", "喵，早晚美梦成真",
                "拥有1/2/3层【贵宾待遇】且处于战斗时，每15/10/5秒释放【要发财啦】，造成80%基础攻击力并施加递减易伤"),

        VICISSITUDE_STRENGTH(Signet.VICISSITUDE, "vicissitude_strength", "行路漫漫",
                "每秒提高0.8%伤害，最多40层，受到伤害时减少10层"),
        VICISSITUDE_RESISTANCE(Signet.VICISSITUDE, "vicissitude_resistance", "纵使寻得",
                "每秒降低0.8%受到的伤害，最多40层，受到伤害时减少10层"),
        VICISSITUDE_TIME(Signet.VICISSITUDE, "vicissitude_time", "日月蹉跎", "每秒提高0.2%移动速度，最多40层，受到伤害时减少10层"),
        VICISSITUDE_ROBE(Signet.VICISSITUDE, "vicissitude_robe", "玄衣不再", "每秒提高0.5%攻击速度，最多40层，受到伤害时减少10层"),
        VICISSITUDE_DREAM(Signet.VICISSITUDE, "vicissitude_dream", "旧梦如昨", "每秒提高0.6%伤害穿透，最多40层，受到伤害时减少10层"),
        VICISSITUDE_WORDS(Signet.VICISSITUDE, "vicissitude_words", "与谁言说", "每秒提高0.3%崩坏能容量，最多40层，受到伤害时减少10层"),
        VICISSITUDE_RED_DUST(Signet.VICISSITUDE, "vicissitude_red_dust", "红尘碌碌，一梦黄粱", "每击杀一个单位获得1层【战士的决意】"),
        VICISSITUDE_PROSPERITY(Signet.VICISSITUDE, "vicissitude_prosperity", "无可奈何，繁华落尽", "每层【战士的决意】使浮生普通刻印每秒额外叠加1层"),
        VICISSITUDE_FAMILIAR(Signet.VICISSITUDE, "vicissitude_familiar", "似曾相识，却又相忘", "普通刻印最大层数提高至60；层数超过30时，每8秒获得1层【战士的决意】"),
        VICISSITUDE_REED(Signet.VICISSITUDE, "vicissitude_reed", "凡尘总总，一苇难渡", "拥有【战士的回忆】时，刻印减伤的20%额外转化为伤害加成"),
        VICISSITUDE_FEATHER(Signet.VICISSITUDE, "vicissitude_feather", "如羽随风，似浪逐流", "每次回忆抵伤使无敌时间额外延长0.3秒，并回复5点能量"),
        VICISSITUDE_AWAKEN(Signet.VICISSITUDE, "vicissitude_awaken", "怎堪梦醒，难觅归途", "拥有【战士的回忆】时，所有「浮生」普通刻印提供的加成效果的最大叠加层数增加20层。失去全部【战士的回忆】时，减少10层最大叠加层数并恢复1层【战士的回忆】，冷却10秒。"),

        STARS_RED(Signet.STARS, "stars_red", "红色的。热热的",
                "使用武器技时，使周围的敌方单位被染上红色油墨8秒。\n被红色油墨染色的敌方单位受到的伤害增加10%，重复触发刷新持续时间"),
        STARS_BLUE(Signet.STARS, "stars_blue", "蓝色的。冷冷的",
                "触发闪避时，使周围的敌方单位被染上蓝色油墨8秒。\n被蓝色油墨染色的敌方单位造成的伤害降低10%，重复触发刷新持续时间"),
        STARS_YELLOW(Signet.STARS, "stars_yellow", "黄色的，暖暖的",
                "使用必杀技时，使周围的敌方单位被染上黄色油墨8秒。\n被黄色油墨染色的敌方单位受到的伤害提高15%，重复触发刷新持续时间"),
        STARS_BLACK(Signet.STARS, "stars_black", "黑色的，暗暗的",
                "每次给敌方单位染色时，玩家造成的伤害提高10%，持续10秒，最多叠加4层"),
        STARS_WHITE(Signet.STARS, "stars_white", "白色的，亮亮的",
                "每次给敌方单位染色时，玩家当前武器获得5点能量，冷却5秒"),
        STARS_GRAY(Signet.STARS, "stars_gray", "灰色的，空空的",
                "给敌方单位染色的持续时间延长5秒"),
        STARS_WILDFLOWER_SHINING(Signet.STARS, "stars_wildflower_shining", "像是野花，绽放着的",
                "敌方单位身上存在蓝色油墨与红色油墨时，对敌方单位造成的全伤害提高50%。\n被染色敌方单位死亡时，延迟1秒后使周围的敌方单位染上与其颜色相同的油墨"),
        STARS_FLAME_SHINING(Signet.STARS, "stars_flame_shining", "像是火焰，燃烧着的",
                "敌方单位身上存在红色油墨与黄色油墨时，玩家对其造成伤害时额外提高10%的伤害穿透"),
        STARS_LEAF_SHINING(Signet.STARS, "stars_leaf_shining", "像是叶子，舒展着的",
                "敌方单位身上存在蓝色油墨与黄色油墨时，攻击敌方单位可以对6格内敌方单位造成300%角色基础攻击力伤害，冷却8秒"),
        STARS_WILDFLOWER_DIM(Signet.STARS, "stars_wildflower_dim", "像是野花，飞散了的",
                "以红色和蓝色触发【色相调和】时，对敌方单位及其周围8格分别造成120%/50%角色基础攻击力的伤害，冷却3秒。\n为自身染上持续15秒的紫色油墨；每持有一种不同英桀的增幅刻印，使红色、黄色和蓝色油墨的加成效果提高5%，上限40%，重复触发刷新"),
        STARS_LEAF_DIM(Signet.STARS, "stars_leaf_dim", "像是叶子，飘落了的",
                "以黄色和蓝色触发【色相调和】时，额外触发一次范围攻击，造成80%角色基础攻击力的伤害，冷却3秒。\n为自身染上持续15秒的绿色油墨；每持有一种不同英桀的增幅刻印，使【像是繁星，黯淡了的】中每种油墨的伤害提高效果额外提高5%，上限20%，重复触发刷新"),
        STARS_FLAME_DIM(Signet.STARS, "stars_flame_dim", "像是火焰，熄灭了的",
                "以红色和黄色触发【色相调和】时，为自身染上持续15秒的橙色油墨，并触发2.5秒的时空断裂。\n使所有敌方单位的防御降低20%；每持有一种英桀的增幅刻印，防御额外降低5%，上限20%，持续10秒，冷却7秒，重复触发刷新"),

        INFINITY_REBIRTH(Signet.INFINITY, "infinity_rebirth", "超越无限的命途",
                "受到致命伤害时，不会直接死亡，而是进入褪麟状态：减少20%的最大血量上限，最多减少到1点。\n生命上限小于或等于1%时，下一次受到致命伤害，会直接死亡并恢复全部血量上限。褪麟持续时间为3分钟", "受到致命伤害时，不会直接死亡，而是进入褪麟状态：减少18%的最大血量上限，最多减少到1点。\n生命上限小于或等于1%时，下一次受到致命伤害，会直接死亡并恢复全部血量上限。褪麟持续时间为3分钟", "受到致命伤害时，不会直接死亡，而是进入褪麟状态：减少16%的最大血量上限，最多减少到1点。\n生命上限小于或等于1%时，下一次受到致命伤害，会直接死亡并恢复全部血量上限。褪麟持续时间为3分钟", "受到致命伤害时，不会直接死亡，而是进入褪麟状态：减少14%的最大血量上限，最多减少到1点。\n生命上限小于或等于1%时，下一次受到致命伤害，会直接死亡并恢复全部血量上限。褪麟持续时间为3分钟"),
        INFINITY_ALLY(Signet.INFINITY, "infinity_ally", "利齿的「V」",
                "场上每有一名协同者或人偶，造成的伤害增加5%，最多叠加三次", "场上每有一名协同者或人偶，造成的伤害增加5.75%，最多叠加三次", "场上每有一名协同者或人偶，造成的伤害增加6.5%，最多叠加三次", "场上每有一名协同者或人偶，造成的伤害增加7.5%，最多叠加三次"),
        INFINITY_P(Signet.INFINITY, "infinity_p", "缠环的「P」", "场上每有一个玩家召唤物或受玩家控制的友方单位，敌方单位受到的全伤害提高8%，最多3个"),
        INFINITY_B(Signet.INFINITY, "infinity_b", "静默的「B」", "场上每有一个玩家召唤物或受玩家控制的友方单位，玩家受到的全伤害降低8%，最多3个"),
        INFINITY_E(Signet.INFINITY, "infinity_e", "吻毒的「E」", "玩家召唤物或受玩家控制的友方单位造成的全伤害提高60%"),
        INFINITY_DEATH_X(Signet.INFINITY, "infinity_death_x", "死亡的「X」", "【思维同化】期间每秒使召唤物伤害提高10%，最多5层，退出时重置"),
        INFINITY_UNKNOWN_X(Signet.INFINITY, "infinity_unknown_x", "未知的「X」", "思维同化期间再次进入时，使召唤物攻速提高30%，冷却4秒"),
        INFINITY_NEWBORN_X(Signet.INFINITY, "infinity_newborn_x", "新生的「X」", "【思维同化】期间玩家获得15%伤害穿透"),
        INFINITY_SACRIFICE_M(Signet.INFINITY, "infinity_sacrifice_m", "代祭的「M」", "召唤物在场超过1秒后，同步期间每秒延长1秒，否则每秒增加5%进度"),
        INFINITY_IMMORTAL_M(Signet.INFINITY, "infinity_immortal_m", "不灭的「M」", "召唤物在场8秒后伤害每秒提高5%，上限50%；同步期间玩家继承加成，上限30%"),
        INFINITY_COLLAPSE_M(Signet.INFINITY, "infinity_collapse_m", "崩落的「M」", "召唤物每次造成伤害使目标受到的全伤害提高1%；同步期间玩家攻击同样生效，冷却1秒，上限30%"),

        DAYBREAK_CLAW(Signet.DAYBREAK, "daybreak_claw", "亵渎不归之「爪」",
                "角色攻击时，对攻击目标施加一层撕裂，每层撕裂每秒造成0.5伤害，持续五秒，可叠加十层。\n佩戴追忆之证「长天，易望难及」时，对拥有撕裂状态下的生物造成的伤害提高15%", "角色攻击时，对攻击目标施加一层撕裂，每层撕裂每秒造成0.575伤害，持续五秒，可叠加十层。\n佩戴追忆之证「长天，易望难及」时，对拥有撕裂状态下的生物造成的伤害提高17.25%", "角色攻击时，对攻击目标施加一层撕裂，每层撕裂每秒造成0.65伤害，持续五秒，可叠加十层。\n佩戴追忆之证「长天，易望难及」时，对拥有撕裂状态下的生物造成的伤害提高19.5%", "角色攻击时，对攻击目标施加一层撕裂，每层撕裂每秒造成0.75伤害，持续五秒，可叠加十层。\n佩戴追忆之证「长天，易望难及」时，对拥有撕裂状态下的生物造成的伤害提高22.5%"),
        DAYBREAK_EYE(Signet.DAYBREAK, "daybreak_eye", "俯视邪渊之「眼」",
                "生物每持有一层撕裂，角色对其防具的伤害提升5%，受到的伤害提高2%", "生物每持有一层撕裂，角色对其防具的伤害提升5.75%，受到的伤害提高2.3%", "生物每持有一层撕裂，角色对其防具的伤害提升6.5%，受到的伤害提高2.6%", "生物每持有一层撕裂，角色对其防具的伤害提升7.5%，受到的伤害提高3%"),
        DAYBREAK_WING(Signet.DAYBREAK, "daybreak_wing", "掩蔽血月之「翼」",
                "触发闪避时，对场上所有单位附加5层【撕裂】状态", "触发闪避时，对场上所有单位附加6层【撕裂】状态", "触发闪避时，对场上所有单位附加7层【撕裂】状态", "触发闪避时，对场上所有单位附加8层【撕裂】状态"),
        DAYBREAK_HORN(Signet.DAYBREAK, "daybreak_horn", "撕裂暗空之「角」",
                "使用必杀技时，对场上所有单位附加15层【撕裂】状态；对处于【撕裂】状态的单位造成的伤害提高20%", "使用必杀技时，对场上所有单位附加17层【撕裂】状态；对处于【撕裂】状态的单位造成的伤害提高23%", "使用必杀技时，对场上所有单位附加20层【撕裂】状态；对处于【撕裂】状态的单位造成的伤害提高26%", "使用必杀技时，对场上所有单位附加23层【撕裂】状态；对处于【撕裂】状态的单位造成的伤害提高30%"),
        DAYBREAK_BREATH(Signet.DAYBREAK, "daybreak_breath", "毁谤硫磺之「息」",
                "对已处于【撕裂】状态的单位再次施加【撕裂】时，回复5点能量，冷却5秒", "对已处于【撕裂】状态的单位再次施加【撕裂】时，回复5.75点能量，冷却5秒", "对已处于【撕裂】状态的单位再次施加【撕裂】时，回复6.5点能量，冷却5秒", "对已处于【撕裂】状态的单位再次施加【撕裂】时，回复7.5点能量，冷却5秒"),
        DAYBREAK_HEART(Signet.DAYBREAK, "daybreak_heart", "逆结七罪之「心」",
                "【撕裂】状态持续时间延长5秒", "【撕裂】状态持续时间延长5.75秒", "【撕裂】状态持续时间延长6.5秒", "【撕裂】状态持续时间延长7.5秒"),

        DAYBREAK_LIGHT_HERO(Signet.DAYBREAK, "daybreak_light_hero", "英雄，独担兴衰",
                "场上的单位每拥有1层【撕裂】，玩家获得1%伤害穿透，最多提高28%", "场上的单位每拥有1层【撕裂】，玩家获得1.15%伤害穿透，最多提高32.2%", "场上的单位每拥有1层【撕裂】，玩家获得1.3%伤害穿透，最多提高36.4%", "场上的单位每拥有1层【撕裂】，玩家获得1.5%伤害穿透，最多提高42%"),
        DAYBREAK_LIGHT_GOOD_EVIL(Signet.DAYBREAK, "daybreak_light_good_evil", "善恶，两分难渡",
                "核心攻击命中没有护甲保护的单位时，回复5点能量，冷却5秒，并使角色造成的伤害提高10%，持续10秒", "核心攻击命中没有护甲保护的单位时，回复5.75点能量，冷却5秒，并使角色造成的伤害提高11.5%，持续10秒", "核心攻击命中没有护甲保护的单位时，回复6.5点能量，冷却5秒，并使角色造成的伤害提高13%，持续10秒", "核心攻击命中没有护甲保护的单位时，回复7.5点能量，冷却5秒，并使角色造成的伤害提高15%，持续10秒"),
        DAYBREAK_LIGHT_VOW(Signet.DAYBREAK, "daybreak_light_vow", "宿诺，百折未易",
                "场上的单位总计每拥有1层【撕裂】，角色伤害提高1.6%，最多提高40%", "场上的单位总计每拥有1层【撕裂】，角色伤害提高1.84%，最多提高46%", "场上的单位总计每拥有1层【撕裂】，角色伤害提高2.08%，最多提高52%", "场上的单位总计每拥有1层【撕裂】，角色伤害提高2.4%，最多提高60%"),
        DAYBREAK_NIGHT_DAWN(Signet.DAYBREAK, "daybreak_night_dawn", "黎明，独待皆尽",
                "处于【撕裂】状态时，每次生命值增加使受到的全伤害降低1%，上限30%；每次生命值降低使伤害穿透提高4%，上限30%", "处于【撕裂】状态时，每次生命值增加使受到的全伤害降低1.15%，上限34.5%；每次生命值降低使伤害穿透提高4.6%，上限34.5%", "处于【撕裂】状态时，每次生命值增加使受到的全伤害降低1.3%，上限39%；每次生命值降低使伤害穿透提高5.2%，上限39%", "处于【撕裂】状态时，每次生命值增加使受到的全伤害降低1.5%，上限45%；每次生命值降低使伤害穿透提高6%，上限45%"),
        DAYBREAK_NIGHT_CHOICE(Signet.DAYBREAK, "daybreak_night_choice", "抉择，两全追憾",
                "角色武器技命中单位时，释放一次【燃尽灰烬】，对周围单位造成300%角色基础攻击力的伤害，并根据自身【撕裂】层数回复生命值，每10秒最多触发一次", "角色武器技命中单位时，释放一次【燃尽灰烬】，对周围单位造成345%角色基础攻击力的伤害，并根据自身【撕裂】层数回复生命值，每10秒最多触发一次", "角色武器技命中单位时，释放一次【燃尽灰烬】，对周围单位造成390%角色基础攻击力的伤害，并根据自身【撕裂】层数回复生命值，每10秒最多触发一次", "角色武器技命中单位时，释放一次【燃尽灰烬】，对周围单位造成450%角色基础攻击力的伤害，并根据自身【撕裂】层数回复生命值，每10秒最多触发一次"),
        DAYBREAK_NIGHT_INTENT(Signet.DAYBREAK, "daybreak_night_intent", "本念，千摧难改",
                "角色自身每有1层【撕裂】，角色造成的伤害提高1.5%", "角色自身每有1层【撕裂】，角色造成的伤害提高1.725%", "角色自身每有1层【撕裂】，角色造成的伤害提高1.95%", "角色自身每有1层【撕裂】，角色造成的伤害提高2.25%"),

        SETSURA_DODGE(Signet.SETSURA, "setsura_dodge", "御神装·勿忘-樱之舞",
                "角色可以触发闪避，概率为20%，冷却20秒，不可免疫秒杀技", "角色可以触发闪避，概率为23%，冷却18.4秒，不可免疫秒杀技", "角色可以触发闪避，概率为26%，冷却16.8秒，不可免疫秒杀技", "角色可以触发闪避，概率为30%，冷却15秒，不可免疫秒杀技"),
        SETSURA_SPEED(Signet.SETSURA, "setsura_speed", "缭乱百花「梅」",
                "触发极限闪避技能后，攻击速度和移动速度提高10%，持续8秒", "触发极限闪避技能后，攻击速度和移动速度提高11.5%，持续8秒", "触发极限闪避技能后，攻击速度和移动速度提高13%，持续8秒", "触发极限闪避技能后，攻击速度和移动速度提高15%，持续8秒"),
        SETSURA_RED_LEAVES(Signet.SETSURA, "setsura_red_leaves", "缭乱百花「红叶」",
                "触发闪避后，所有单位受到的全伤害提高10%，持续8秒", "触发闪避后，所有单位受到的全伤害提高11.5%，持续8秒", "触发闪避后，所有单位受到的全伤害提高13%，持续8秒", "触发闪避后，所有单位受到的全伤害提高15%，持续8秒"),
        SETSURA_PEONY(Signet.SETSURA, "setsura_peony", "缭乱百花「牡丹」",
                "触发闪避后，造成的伤害提高20%，持续8秒", "触发闪避后，造成的伤害提高23%，持续8秒", "触发闪避后，造成的伤害提高26%，持续8秒", "触发闪避后，造成的伤害提高30%，持续8秒"),
        SETSURA_WISTERIA(Signet.SETSURA, "setsura_wisteria", "缭乱百花「藤」",
                "闪避可额外触发1次，冷却时间降低10%", "闪避可额外触发1次，冷却时间降低11.5%", "闪避可额外触发1次，冷却时间降低13%", "闪避可额外触发1次，冷却时间降低15%"),
        SETSURA_IRIS(Signet.SETSURA, "setsura_iris", "缭乱百花「菖蒲」",
                "触发闪避后，额外回复8点能量", "触发闪避后，额外回复9.2点能量", "触发闪避后，额外回复10.4点能量", "触发闪避后，额外回复12点能量"),

        SETSURA_CURTAIN_RAIN_FOUR_LIGHTS(Signet.SETSURA, "setsura_curtain_rain_four_lights", "刹那一刀「雨四光」",
                "【樱幕结界】开启时，重置必杀技和武器技的冷却\n该刻印无法升级", "【樱幕结界】开启时，重置必杀技和武器技的冷却\n该刻印无法升级", "【樱幕结界】开启时，重置必杀技和武器技的冷却\n该刻印无法升级", "【樱幕结界】开启时，重置必杀技和武器技的冷却\n该刻印无法升级"),
        SETSURA_CURTAIN_MOON_SAKE(Signet.SETSURA, "setsura_curtain_moon_sake", "刹那一刀「月见酒」",
                "【樱幕结界】开启期间，每次攻击额外附加20%原伤害，触发间隔1秒", "【樱幕结界】开启期间，每次攻击额外附加23%原伤害，触发间隔1秒", "【樱幕结界】开启期间，每次攻击额外附加26%原伤害，触发间隔1秒", "【樱幕结界】开启期间，每次攻击额外附加30%原伤害，触发间隔1秒"),
        SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY(Signet.SETSURA, "setsura_curtain_boar_deer_butterfly", "刹那一刀「猪鹿蝶」",
                "【樱幕结界】开启期间，移动状态下最多有3秒使【樱幕结界】的持续时间不会减少\n该刻印无法升级", "【樱幕结界】开启期间，移动状态下最多有3秒使【樱幕结界】的持续时间不会减少\n该刻印无法升级", "【樱幕结界】开启期间，移动状态下最多有3秒使【樱幕结界】的持续时间不会减少\n该刻印无法升级", "【樱幕结界】开启期间，移动状态下最多有3秒使【樱幕结界】的持续时间不会减少\n该刻印无法升级"),
        SETSURA_YAKSHA_MOMENTARY_MYRIAD(Signet.SETSURA, "setsura_yaksha_momentary_myriad", "刹那一刀「一瞬森罗」",
                "累计触发3次闪避后，也会对所有敌方单位释放【刹那一刀「业狩」】；每段额外造成200%角色基础攻击力的自适应伤害", "累计触发3次闪避后，也会对所有敌方单位释放【刹那一刀「业狩」】；每段额外造成230%角色基础攻击力的自适应伤害", "累计触发3次闪避后，也会对所有敌方单位释放【刹那一刀「业狩」】；每段额外造成260%角色基础攻击力的自适应伤害", "累计触发3次闪避后，也会对所有敌方单位释放【刹那一刀「业狩」】；每段额外造成300%角色基础攻击力的自适应伤害"),
        SETSURA_YAKSHA_ENDLESS_EVIL(Signet.SETSURA, "setsura_yaksha_endless_evil", "刹那一刀「无间恶刹」",
                "释放【刹那一刀「业狩」】后，闪避的剩余冷却时间减少25%", "释放【刹那一刀「业狩」】后，闪避的剩余冷却时间减少28.75%", "释放【刹那一刀「业狩」】后，闪避的剩余冷却时间减少32.5%", "释放【刹那一刀「业狩」】后，闪避的剩余冷却时间减少37.5%"),
        SETSURA_YAKSHA_ALL_FORMS(Signet.SETSURA, "setsura_yaksha_all_forms", "刹那一刀「万象无明」",
                "每释放一次【刹那一刀「业狩」】，接下来的【刹那一刀「业狩」】每段额外造成25%原伤害，最多叠加8次", "每释放一次【刹那一刀「业狩」】，接下来的【刹那一刀「业狩」】每段额外造成28.75%原伤害，最多叠加8次", "每释放一次【刹那一刀「业狩」】，接下来的【刹那一刀「业狩」】每段额外造成32.5%原伤害，最多叠加8次", "每释放一次【刹那一刀「业狩」】，接下来的【刹那一刀「业狩」】每段额外造成37.5%原伤害，最多叠加8次"),

        BODHI_DAMAGE(Signet.BODHI, "bodhi_damage", "宿命之箴言",
                "连击数达到15时清空全部连击数，全伤害提高25%，持续5秒", "连击数达到15时清空全部连击数，全伤害提高28.75%，持续5秒", "连击数达到15时清空全部连击数，全伤害提高32.5%，持续5秒", "连击数达到15时清空全部连击数，全伤害提高37.5%，持续5秒"),
        BODHI_DEFENSE(Signet.BODHI, "bodhi_defense", "天耳之箴言",
                "连击数达到15时清空全部连击数，受到的全伤害降低12%，持续5秒", "连击数达到15时清空全部连击数，受到的全伤害降低13.8%，持续5秒", "连击数达到15时清空全部连击数，受到的全伤害降低15.6%，持续5秒", "连击数达到15时清空全部连击数，受到的全伤害降低18%，持续5秒"),
        BODHI_HEAVENLY_EYE(Signet.BODHI, "bodhi_heavenly_eye", "天眼之箴言",
                "连击数达到15时清空全部连击数，使所有单位受到的全伤害提高15%，持续5秒", "连击数达到15时清空全部连击数，使所有单位受到的全伤害提高17.25%，持续5秒", "连击数达到15时清空全部连击数，使所有单位受到的全伤害提高19.5%，持续5秒", "连击数达到15时清空全部连击数，使所有单位受到的全伤害提高22.5%，持续5秒"),
        BODHI_MIND_READING(Signet.BODHI, "bodhi_mind_reading", "他心之箴言",
                "连击数达到15时清空全部连击数，回复10点能量", "连击数达到15时清空全部连击数，回复11.5点能量", "连击数达到15时清空全部连击数，回复13点能量", "连击数达到15时清空全部连击数，回复15点能量"),
        BODHI_DIVINE_FOOT(Signet.BODHI, "bodhi_divine_foot", "神足之箴言",
                "连击数低于8时，立刻提高至12，冷却10秒", "连击数低于8时，立刻提高至12，冷却8.7秒", "连击数低于8时，立刻提高至12，冷却7.7秒", "连击数低于8时，立刻提高至12，冷却6.7秒"),
        BODHI_EXHAUSTION(Signet.BODHI, "bodhi_exhaustion", "漏尽之箴言",
                "攻击命中时额外提高3连击数，冷却5秒", "攻击命中时额外提高3连击数，冷却4.35秒", "攻击命中时额外提高3连击数，冷却3.85秒", "攻击命中时额外提高3连击数，冷却3.35秒"),

        BODHI_TRUE_SELFLESS(Signet.BODHI, "bodhi_true_selfless", "无我之真言",
                "被【因果转轮】命中的单位下一次攻击全伤害降低50%，且受到的伤害提高10%，持续8秒", "被【因果转轮】命中的单位下一次攻击全伤害降低57.5%，且受到的伤害提高11.5%，持续8秒", "被【因果转轮】命中的单位下一次攻击全伤害降低65%，且受到的伤害提高13%，持续8秒", "被【因果转轮】命中的单位下一次攻击全伤害降低75%，且受到的伤害提高15%，持续8秒"),
        BODHI_TRUE_IMPERMANENCE(Signet.BODHI, "bodhi_true_impermanence", "无常之真言",
                "【因果转轮】命中没有护甲的单位时，自身获得15%伤害穿透，持续10秒", "【因果转轮】命中没有护甲的单位时，自身获得17.25%伤害穿透，持续10秒", "【因果转轮】命中没有护甲的单位时，自身获得19.5%伤害穿透，持续10秒", "【因果转轮】命中没有护甲的单位时，自身获得22.5%伤害穿透，持续10秒"),
        BODHI_TRUE_SILENCE(Signet.BODHI, "bodhi_true_silence", "寂静之真言",
                "【因果转轮】对护甲造成的伤害提高20%，对没有护甲保护的单位额外造成500%角色基础攻击力的伤害", "【因果转轮】对护甲造成的伤害提高23%，对没有护甲保护的单位额外造成575%角色基础攻击力的伤害", "【因果转轮】对护甲造成的伤害提高26%，对没有护甲保护的单位额外造成650%角色基础攻击力的伤害", "【因果转轮】对护甲造成的伤害提高30%，对没有护甲保护的单位额外造成750%角色基础攻击力的伤害"),
        BODHI_SECRET_IMPERMANENCE(Signet.BODHI, "bodhi_secret_impermanence", "无常之秘言",
                "【天慧·开眼】命中单位时，每处于一种负面状态，额外造成1次150%角色基础攻击力的伤害", "【天慧·开眼】命中单位时，每处于一种负面状态，额外造成1次172.5%角色基础攻击力的伤害", "【天慧·开眼】命中单位时，每处于一种负面状态，额外造成1次195%角色基础攻击力的伤害", "【天慧·开眼】命中单位时，每处于一种负面状态，额外造成1次225%角色基础攻击力的伤害"),
        BODHI_SECRET_SELFLESS(Signet.BODHI, "bodhi_secret_selfless", "无我之秘言",
                "【天慧·开眼】施加的负面状态延长3秒；单位受到的伤害提高10%，每处于一种负面状态额外提高2.5%，最多计算5种", "【天慧·开眼】施加的负面状态延长3秒；单位受到的伤害提高11.5%，每处于一种负面状态额外提高2.875%，最多计算5种", "【天慧·开眼】施加的负面状态延长3秒；单位受到的伤害提高13%，每处于一种负面状态额外提高3.25%，最多计算5种", "【天慧·开眼】施加的负面状态延长3秒；单位受到的伤害提高15%，每处于一种负面状态额外提高3.75%，最多计算5种"),
        BODHI_SECRET_SILENCE(Signet.BODHI, "bodhi_secret_silence", "寂静之秘言",
                "【天慧·开眼】命中单位时，施加3种随机负面状态，持续3秒", "【天慧·开眼】命中单位时，施加3种随机负面状态，持续3.45秒", "【天慧·开眼】命中单位时，施加3种随机负面状态，持续3.9秒", "【天慧·开眼】命中单位时，施加3种随机负面状态，持续4.5秒"),

        DECIMATION_HEALTH(Signet.DECIMATION, "decimation_health", "命路·命舛·命刻",
                "生命上限 +15%", "生命上限 +17.25%", "生命上限 +19.5%", "生命上限 +22.5%"),
        DECIMATION_LOW_HEALTH(Signet.DECIMATION, "decimation_low_health", "狂信·狂人·狂言",
                "每损失1生命减伤0.3%，上限20%", "每损失1生命减伤0.345%，上限23%", "每损失1生命减伤0.39%，上限26%", "每损失1生命减伤0.45%，上限30%"),
        DECIMATION_FIRE(Signet.DECIMATION, "decimation_fire", "赤骨·赤血·赤练",
                "火焰伤害 +50%，受到火伤 -40%", "火焰伤害 +57.5%，受到火伤 -40%", "火焰伤害 +65%，受到火伤 -40%", "火焰伤害 +75%，受到火伤 -40%"),
        DECIMATION_BLADE(Signet.DECIMATION, "decimation_blade", "剑锋 · 剑冢 · 剑痕",
                "每损失1生命值，全伤害提高0.3%，上限20%", "每损失1生命值，全伤害提高0.345%，上限23%", "每损失1生命值，全伤害提高0.39%，上限26%", "每损失1生命值，全伤害提高0.45%，上限30%"),
        DECIMATION_WUWANG(Signet.DECIMATION, "decimation_wuwang", "无妄 · 无心 · 无归",
                "生命降低时，使所有敌方单位受到的全伤害提高15%，持续8秒", "生命降低时，使所有敌方单位受到的全伤害提高17.25%，持续8秒", "生命降低时，使所有敌方单位受到的全伤害提高19.5%，持续8秒", "生命降低时，使所有敌方单位受到的全伤害提高22.5%，持续8秒"),
        DECIMATION_WEISHEN(Signet.DECIMATION, "decimation_weishen", "唯神 · 唯我 · 唯一",
                "生命降低时，回复降低值30%的能量", "生命降低时，回复降低值34.5%的能量", "生命降低时，回复降低值39%的能量", "生命降低时，回复降低值45%的能量"),

        DECIMATION_RELEASE_NONHUMAN(Signet.DECIMATION, "decimation_release_nonhuman", "非人 · 非鬼 · 非神 · 非天",
                "生命降低时，为【燃血解放】增加100点充能，冷却1秒", "生命降低时，为【燃血解放】增加115点充能，冷却1秒", "生命降低时，为【燃血解放】增加130点充能，冷却1秒", "生命降低时，为【燃血解放】增加150点充能，冷却1秒"),
        DECIMATION_RELEASE_ONE_PATH(Signet.DECIMATION, "decimation_release_one_path", "一人 · 一面 · 一契 · 一途",
                "对当前生命百分比高于自己的敌方单位造成的伤害提高10%，【燃血解放】伤害额外提高30%", "对当前生命百分比高于自己的敌方单位造成的伤害提高11.5%，【燃血解放】伤害额外提高34.5%", "对当前生命百分比高于自己的敌方单位造成的伤害提高13%，【燃血解放】伤害额外提高39%", "对当前生命百分比高于自己的敌方单位造成的伤害提高15%，【燃血解放】伤害额外提高45%"),
        DECIMATION_RELEASE_KALPAS(Signet.DECIMATION, "decimation_release_kalpas", "千钧 · 千转 · 千难 · 千劫",
                "释放【燃血解放】消耗20%当前生命；每消耗10生命额外造成150%角色基础攻击力的伤害，并使全伤害提高10%，持续5秒", "释放【燃血解放】消耗20%当前生命；每消耗10生命额外造成172.5%角色基础攻击力的伤害，并使全伤害提高11.5%，持续5秒", "释放【燃血解放】消耗20%当前生命；每消耗10生命额外造成195%角色基础攻击力的伤害，并使全伤害提高13%，持续5秒", "释放【燃血解放】消耗20%当前生命；每消耗10生命额外造成225%角色基础攻击力的伤害，并使全伤害提高15%，持续5秒"),
        DECIMATION_RAIMENT_UNDYING(Signet.DECIMATION, "decimation_raiment_undying", "无死 · 无生 · 无灭 · 无存",
                "【燃血狱衣】充能上限提高500；充能超过1500时，受到伤害可抵挡充能数×0.1%的伤害，最多抵挡95%", "【燃血狱衣】充能上限提高575；充能超过1500时，受到伤害可抵挡充能数×0.115%的伤害，最多抵挡95%", "【燃血狱衣】充能上限提高650；充能超过1500时，受到伤害可抵挡充能数×0.13%的伤害，最多抵挡95%", "【燃血狱衣】充能上限提高750；充能超过1500时，受到伤害可抵挡充能数×0.15%的伤害，最多抵挡95%"),
        DECIMATION_RAIMENT_BURNING_SOUL(Signet.DECIMATION, "decimation_raiment_burning_soul", "焚身 · 焚骨 · 焚心 · 焚魂",
                "【燃血狱衣】充能上限提高500；充能超过1500时，获得生命回复后，下一次攻击额外附带800%角色基础攻击力的伤害，触发间隔5秒", "【燃血狱衣】充能上限提高575；充能超过1500时，获得生命回复后，下一次攻击额外附带920%角色基础攻击力的伤害，触发间隔5秒", "【燃血狱衣】充能上限提高650；充能超过1500时，获得生命回复后，下一次攻击额外附带1040%角色基础攻击力的伤害，触发间隔5秒", "【燃血狱衣】充能上限提高750；充能超过1500时，获得生命回复后，下一次攻击额外附带1200%角色基础攻击力的伤害，触发间隔5秒"),
        DECIMATION_RAIMENT_HOMELAND(Signet.DECIMATION, "decimation_raiment_homeland", "故土 · 故国 · 故友 · 故人",
                "【燃血狱衣】充能上限提高500；充能超过1500时，伤害穿透提高10%", "【燃血狱衣】充能上限提高575；充能超过1500时，伤害穿透提高11.5%", "【燃血狱衣】充能上限提高650；充能超过1500时，伤害穿透提高13%", "【燃血狱衣】充能上限提高750；充能超过1500时，伤害穿透提高15%"),

        HELIX_MAGIC(Signet.HELIX, "helix_magic", "第一幕「魔术」",
                "武器技每层全伤害 +10%", "武器技每层全伤害 +11.5%", "武器技每层全伤害 +13%", "武器技每层全伤害 +15%"),
        HELIX_PENDULUM(Signet.HELIX, "helix_pendulum", "第二幕「钟摆」",
                "武器技使敌方单位易伤20%", "武器技使敌方单位易伤23%", "武器技使敌方单位易伤26%", "武器技使敌方单位易伤30%"),
        HELIX_PARADOX(Signet.HELIX, "helix_paradox", "第三幕「矛盾」",
                "铁砧消耗降为1", "铁砧额外修复25%耐久", "铁砧额外修复50%耐久", "铁砧额外修复75%耐久"),
        HELIX_SPOON(Signet.HELIX, "helix_spoon", "第四幕「汤匙」",
                "使用武器技后，蓄力攻击造成的伤害提高20%，持续4.5秒，重复触发时刷新时间",
                "使用武器技后，蓄力攻击造成的伤害提高23%，持续4.5秒，重复触发时刷新时间",
                "使用武器技后，蓄力攻击造成的伤害提高26%，持续4.5秒，重复触发时刷新时间",
                "使用武器技后，蓄力攻击造成的伤害提高30%，持续4.5秒，重复触发时刷新时间"),
        HELIX_COBWEB(Signet.HELIX, "helix_cobweb", "第五幕「蛛丝」",
                "使用武器技后，造成的伤害提高10%，持续4.5秒，重复触发时刷新时间",
                "使用武器技后，造成的伤害提高11.5%，持续4.5秒，重复触发时刷新时间",
                "使用武器技后，造成的伤害提高13%，持续4.5秒，重复触发时刷新时间",
                "使用武器技后，造成的伤害提高15%，持续4.5秒，重复触发时刷新时间"),
        HELIX_SNEER(Signet.HELIX, "helix_sneer", "第六幕「嗤笑」",
                "使用武器技后，在接下来的5秒内总计回复12点能量，并减少15%武器技冷却时间",
                "使用武器技后，在接下来的5秒内总计回复13.8点能量，并减少17.25%武器技冷却时间",
                "使用武器技后，在接下来的5秒内总计回复15.6点能量，并减少19.5%武器技冷却时间",
                "使用武器技后，在接下来的5秒内总计回复18点能量，并减少22.5%武器技冷却时间"),

        HELIX_REVERSED_DOOR(Signet.HELIX, "helix_reversed_door", "第七幕「虚掩的门扉」",
                "造成的伤害提高30%；「惊奇魔术」额外生成2个圆刃，造成2×20%攻击力的物理伤害",
                "造成的伤害提高34.5%；「惊奇魔术」额外生成2个圆刃，造成2×23%攻击力的物理伤害",
                "造成的伤害提高39%；「惊奇魔术」额外生成2个圆刃，造成2×26%攻击力的物理伤害",
                "造成的伤害提高45%；「惊奇魔术」额外生成2个圆刃，造成2×30%攻击力的物理伤害"),
        HELIX_REVERSED_HALL(Signet.HELIX, "helix_reversed_hall", "第八幕「伪饰的厅堂」",
                "发动「惊奇魔术」时消耗5点能量，斩击基础伤害提高35%；发动后6秒内伤害穿透提高10%",
                "发动「惊奇魔术」时消耗5点能量，斩击基础伤害提高40.25%；发动后6秒内伤害穿透提高11.5%",
                "发动「惊奇魔术」时消耗5点能量，斩击基础伤害提高45.5%；发动后6秒内伤害穿透提高13%",
                "发动「惊奇魔术」时消耗5点能量，斩击基础伤害提高52.5%；发动后6秒内伤害穿透提高15%"),
        HELIX_REVERSED_TRUTH(Signet.HELIX, "helix_reversed_truth", "最终幕「破碎的真相」",
                "「惊奇魔术」对护甲的伤害提高100%；每次发动后，其全伤害提高6%，上限30%",
                "「惊奇魔术」对护甲的伤害提高115%；每次发动后，其全伤害提高6.9%，上限34.5%",
                "「惊奇魔术」对护甲的伤害提高130%；每次发动后，其全伤害提高7.8%，上限39%",
                "「惊奇魔术」对护甲的伤害提高150%；每次发动后，其全伤害提高9%，上限45%"),
        HELIX_CONCLUSION_PRISON(Signet.HELIX, "helix_conclusion_prison", "木偶剧「爻错的监牢」",
                "「特型武装 · 型号四」引爆时的全局时空断裂延长2秒；期间角色获得10%伤害穿透",
                "「特型武装 · 型号四」引爆时的全局时空断裂延长2.3秒；期间角色获得11.5%伤害穿透",
                "「特型武装 · 型号四」引爆时的全局时空断裂延长2.6秒；期间角色获得13%伤害穿透",
                "「特型武装 · 型号四」引爆时的全局时空断裂延长3秒；期间角色获得15%伤害穿透"),
        HELIX_CONCLUSION_ORIGIN(Signet.HELIX, "helix_conclusion_origin", "严肃剧「沉眠的始源」",
                "引力场存在时，场上敌方单位受到的全伤害提高15%；全局时空断裂期间额外提高5%",
                "引力场存在时，场上敌方单位受到的全伤害提高17.25%；全局时空断裂期间额外提高5.75%",
                "引力场存在时，场上敌方单位受到的全伤害提高19.5%；全局时空断裂期间额外提高6.5%",
                "引力场存在时，场上敌方单位受到的全伤害提高22.5%；全局时空断裂期间额外提高7.5%"),
        HELIX_CONCLUSION_POINTER(Signet.HELIX, "helix_conclusion_pointer", "传奇剧「交叠的指针」",
                "引力场作用范围变大、吸引力增强；每次造成伤害时额外附加200%角色基础攻击力的自适应伤害",
                "引力场作用范围变大、吸引力增强；每次造成伤害时额外附加230%角色基础攻击力的自适应伤害",
                "引力场作用范围变大、吸引力增强；每次造成伤害时额外附加260%角色基础攻击力的自适应伤害",
                "引力场作用范围变大、吸引力增强；每次造成伤害时额外附加300%角色基础攻击力的自适应伤害"),

        GOLD_STREAM(Signet.GOLD, "gold_stream", "溪流的宣叙",
                "正面效果 +30%，矿物与熔炉 +200%", "正面效果 +34.5%，矿物与熔炉 +230%", "正面效果 +39%，矿物与熔炉 +260%", "正面效果 +45%，矿物与熔炉 +300%"),
        GOLD_PARADISE(Signet.GOLD, "gold_paradise", "乐园的宣叙",
                "每10能量伤害 +5%，上限65%", "每10能量伤害 +5.75%，上限74.75%", "每10能量伤害 +6.5%，上限84.5%", "每10能量伤害 +7.5%，上限97.5%"),
        GOLD_BIRD(Signet.GOLD, "gold_bird", "飞鸟的宣叙",
                "每10能量减伤5%，上限65%", "每10能量减伤5.75%，上限74.75%", "每10能量减伤6.5%，上限84.5%", "每10能量减伤7.5%，上限97.5%"),
        GOLD_ORCHARD(Signet.GOLD, "gold_orchard", "果林的宣叙",
                "攻击的能量回复效率最多提高50%", "攻击的能量回复效率最多提高57.5%", "攻击的能量回复效率最多提高65%", "攻击的能量回复效率最多提高75%"),
        GOLD_WINE(Signet.GOLD, "gold_wine", "美酒的宣叙",
                "获得仇恨时每秒回复1点能量", "获得仇恨时每秒回复1.15点能量", "获得仇恨时每秒回复1.3点能量", "获得仇恨时每秒回复1.5点能量"),
        GOLD_JADE(Signet.GOLD, "gold_jade", "宝玉的宣叙",
                "能量上限提高50点", "能量上限提高57.5点", "能量上限提高65点", "能量上限提高75点"),

        GOLD_ARIA_FERTILE(Signet.GOLD, "gold_aria_fertile", "丰壤的咏叹",
                "【沉醉】状态下的敌方单位受到的全伤害提高15%，最多叠加3层",
                "【沉醉】状态下的敌方单位受到的全伤害提高17.25%，最多叠加3层",
                "【沉醉】状态下的敌方单位受到的全伤害提高19.5%，最多叠加3层",
                "【沉醉】状态下的敌方单位受到的全伤害提高22.5%，最多叠加3层"),
        GOLD_ARIA_LIFE(Signet.GOLD, "gold_aria_life", "生命的咏叹",
                "处于【沉醉】状态下的敌方单位每次受到攻击都会使自己回复1点能量",
                "处于【沉醉】状态下的敌方单位每次受到攻击都会使自己回复1.15点能量",
                "处于【沉醉】状态下的敌方单位每次受到攻击都会使自己回复1.3点能量",
                "处于【沉醉】状态下的敌方单位每次受到攻击都会使自己回复1.5点能量"),
        GOLD_ARIA_DAWN(Signet.GOLD, "gold_aria_dawn", "黎明的咏叹",
                "3道【咏叹的雷鸣】命中同一敌方单位时，引发【雷鸣交响】，造成1200%角色基础攻击力的大范围自适应伤害并施加麻痹状态",
                "3道【咏叹的雷鸣】命中同一敌方单位时，引发【雷鸣交响】，造成1380%角色基础攻击力的大范围自适应伤害并施加麻痹状态",
                "3道【咏叹的雷鸣】命中同一敌方单位时，引发【雷鸣交响】，造成1560%角色基础攻击力的大范围自适应伤害并施加麻痹状态",
                "3道【咏叹的雷鸣】命中同一敌方单位时，引发【雷鸣交响】，造成1800%角色基础攻击力的大范围自适应伤害并施加麻痹状态"),
        GOLD_ECHO_WITHERED(Signet.GOLD, "gold_echo_withered", "枯壤的余音",
                "【终末余音】持续超过3秒后，能量上限提高50%，每秒回复1%能量，直至状态结束",
                "【终末余音】持续超过3秒后，能量上限提高57.5%，每秒回复1.15%能量，直至状态结束",
                "【终末余音】持续超过3秒后，能量上限提高65%，每秒回复1.3%能量，直至状态结束",
                "【终末余音】持续超过3秒后，能量上限提高75%，每秒回复1.5%能量，直至状态结束"),
        GOLD_ECHO_COLD_WIND(Signet.GOLD, "gold_echo_cold_wind", "凄风的余音",
                "【终末余音】持续超过3秒后，每秒对周围敌方单位造成3000%角色基础攻击力的伤害，直至状态结束",
                "【终末余音】持续超过3秒后，每秒对周围敌方单位造成3450%角色基础攻击力的伤害，直至状态结束",
                "【终末余音】持续超过3秒后，每秒对周围敌方单位造成3900%角色基础攻击力的伤害，直至状态结束",
                "【终末余音】持续超过3秒后，每秒对周围敌方单位造成4500%角色基础攻击力的伤害，直至状态结束"),
        GOLD_ECHO_SILENT_NIGHT(Signet.GOLD, "gold_echo_silent_night", "寂夜的余音",
                "【终末余音】持续超过3秒后，每秒降低周围敌方单位2%攻击力与防御，上限40%，直至状态结束",
                "【终末余音】持续超过3秒后，每秒降低周围敌方单位2.3%攻击力与防御，上限46%，直至状态结束",
                "【终末余音】持续超过3秒后，每秒降低周围敌方单位2.6%攻击力与防御，上限52%，直至状态结束",
                "【终末余音】持续超过3秒后，每秒降低周围敌方单位3%攻击力与防御，上限60%，直至状态结束"),

        DISCIPLINE_ONE(Signet.DISCIPLINE, "discipline_one", "其一，不可背叛",
                "每点规诫使普攻伤害 +0.3%", "每点规诫使普攻伤害 +0.345%", "每点规诫使普攻伤害 +0.39%", "每点规诫使普攻伤害 +0.45%"),
        DISCIPLINE_TWO(Signet.DISCIPLINE, "discipline_two", "其二，不可欺瞒",
                "每点规诫使伤害抵抗 +0.15%", "每点规诫使伤害抵抗 +0.1725%", "每点规诫使伤害抵抗 +0.195%", "每点规诫使伤害抵抗 +0.225%"),
        DISCIPLINE_THREE(Signet.DISCIPLINE, "discipline_three", "其三，不可暴戾",
                "每点规诫使能量回复 +0.3%", "每点规诫使能量回复 +0.345%", "每点规诫使能量回复 +0.39%", "每点规诫使能量回复 +0.45%"),
        DISCIPLINE_FOUR(Signet.DISCIPLINE, "discipline_four", "其四，不可妄行",
                "每点规诫使武器必杀技伤害 +0.3%", "每点规诫使武器必杀技伤害 +0.345%", "每点规诫使武器必杀技伤害 +0.39%", "每点规诫使武器必杀技伤害 +0.45%"),
        DISCIPLINE_FIVE(Signet.DISCIPLINE, "discipline_five", "其五，不可伪言",
                "降低规诫时使敌方单位受到的全伤害 +20%", "降低规诫时使敌方单位受到的全伤害 +23%", "降低规诫时使敌方单位受到的全伤害 +26%", "降低规诫时使敌方单位受到的全伤害 +30%"),
        DISCIPLINE_SIX(Signet.DISCIPLINE, "discipline_six", "其六，不可沉沦",
                "单位死亡后全伤害 +40%", "单位死亡后全伤害 +46%", "单位死亡后全伤害 +52%", "单位死亡后全伤害 +60%"),

        DISCIPLINE_ASCETIC_BODY(Signet.DISCIPLINE, "discipline_ascetic_body", "汝，当以此身为束",
                "「苦修」状态下普攻额外获得15%伤害穿透，自身受到的全伤害提高15%",
                "「苦修」状态下普攻额外获得17.25%伤害穿透，自身受到的全伤害提高17.25%",
                "「苦修」状态下普攻额外获得19.5%伤害穿透，自身受到的全伤害提高19.5%",
                "「苦修」状态下普攻额外获得22.5%伤害穿透，自身受到的全伤害提高22.5%"),
        DISCIPLINE_ASCETIC_LIFE(Signet.DISCIPLINE, "discipline_ascetic_life", "汝，当以此生为契",
                "规诫计数的基础值提高20，「规诫」计数始终不会低于60",
                "规诫计数的基础值提高23，「规诫」计数始终不会低于60",
                "规诫计数的基础值提高26，「规诫」计数始终不会低于60",
                "规诫计数的基础值提高30，「规诫」计数始终不会低于60"),
        DISCIPLINE_ASCETIC_PROOF(Signet.DISCIPLINE, "discipline_ascetic_proof", "汝，当以此命为证",
                "「苦修」状态下武器必杀技伤害提高20%，自身受到的全伤害提高15%",
                "「苦修」状态下武器必杀技伤害提高23%，自身受到的全伤害提高17.25%",
                "「苦修」状态下武器必杀技伤害提高26%，自身受到的全伤害提高19.5%",
                "「苦修」状态下武器必杀技伤害提高30%，自身受到的全伤害提高22.5%"),
        DISCIPLINE_BLESSING_PUNISHMENT(Signet.DISCIPLINE, "discipline_blessing_punishment", "汝，当见诸恶得惩",
                "被「汝，当知罪愆如山」命中2次或以上的单位受到的全伤害提高26%，每次命中使当前手持武器回复5点能量。若无其他戒律增幅刻印，数值效果提高50%",
                "被「汝，当知罪愆如山」命中2次或以上的单位受到的全伤害提高29.9%，每次命中使当前手持武器回复6点能量。若无其他戒律增幅刻印，数值效果提高50%",
                "被「汝，当知罪愆如山」命中2次或以上的单位受到的全伤害提高33.8%，每次命中使当前手持武器回复7点能量。若无其他戒律增幅刻印，数值效果提高50%",
                "被「汝，当知罪愆如山」命中2次或以上的单位受到的全伤害提高39%，每次命中使当前手持武器回复8点能量。若无其他戒律增幅刻印，数值效果提高50%"),
        DISCIPLINE_BLESSING_VIRTUE(Signet.DISCIPLINE, "discipline_blessing_virtue", "汝，当见诸善得行",
                "「汝，当知罪愆如山」全伤害提高30%，命中时按「规诫」计数0.13%的比例提高伤害穿透，持续6秒。若无其他戒律增幅刻印，数值效果提高50%",
                "「汝，当知罪愆如山」全伤害提高34.5%，命中时按「规诫」计数0.1495%的比例提高伤害穿透，持续6秒。若无其他戒律增幅刻印，数值效果提高50%",
                "「汝，当知罪愆如山」全伤害提高39%，命中时按「规诫」计数0.169%的比例提高伤害穿透，持续6秒。若无其他戒律增幅刻印，数值效果提高50%",
                "「汝，当知罪愆如山」全伤害提高45%，命中时按「规诫」计数0.195%的比例提高伤害穿透，持续6秒。若无其他戒律增幅刻印，数值效果提高50%"),
        DISCIPLINE_BLESSING_RETURN(Signet.DISCIPLINE, "discipline_blessing_return", "汝，当见诸愿得归",
                "触发「汝，当知罪愆如山」时进入「戒罚」8秒：受到的全伤害降低20%，造成的伤害提高26%；受击时对伤害来源造成700%基础攻击力伤害，间隔5秒。若无其他戒律增幅刻印，数值效果提高50%",
                "触发「汝，当知罪愆如山」时进入「戒罚」8秒：受到的全伤害降低23%，造成的伤害提高29.9%；受击时对伤害来源造成805%基础攻击力伤害，间隔5秒。若无其他戒律增幅刻印，数值效果提高50%",
                "触发「汝，当知罪愆如山」时进入「戒罚」8秒：受到的全伤害降低26%，造成的伤害提高33.8%；受击时对伤害来源造成910%基础攻击力伤害，间隔5秒。若无其他戒律增幅刻印，数值效果提高50%",
                "触发「汝，当知罪愆如山」时进入「戒罚」8秒：受到的全伤害降低30%，造成的伤害提高39%；受击时对伤害来源造成1050%基础攻击力伤害，间隔5秒。若无其他戒律增幅刻印，数值效果提高50%"),

        DELIVERANCE_ULTIMATE(Signet.DELIVERANCE, "deliverance_ultimate", "施予者的金杯",
                "开启武器必杀技后，全伤害提高25%，持续7秒。",
                "开启武器必杀技后，全伤害提高28.75%，持续7秒。",
                "开启武器必杀技后，全伤害提高32.5%，持续7秒。",
                "开启武器必杀技后，全伤害提高37.5%，持续7秒。"),
        DELIVERANCE_EFFECT(Signet.DELIVERANCE, "deliverance_effect", "守望者的坠饰",
                "自身受到的负面效果持续时间缩短30%。",
                "自身受到的负面效果持续时间缩短34.5%。",
                "自身受到的负面效果持续时间缩短39%。",
                "自身受到的负面效果持续时间缩短45%。"),
        DELIVERANCE_ARMOR(Signet.DELIVERANCE, "deliverance_armor", "不死者的烙印",
                "自身的护甲值提高15%。",
                "自身的护甲值提高17.25%。",
                "自身的护甲值提高19.5%。",
                "自身的护甲值提高22.5%。"),
        DELIVERANCE_HUNTERS_MASK(Signet.DELIVERANCE, "deliverance_hunters_mask", "猎杀者的假面",
                "开启武器必杀技后，伤害穿透提高15%，持续7秒。",
                "开启武器必杀技后，伤害穿透提高20%，持续7秒。",
                "开启武器必杀技后，伤害穿透提高25%，持续7秒。",
                "开启武器必杀技后，伤害穿透提高30%，持续7秒。"),
        DELIVERANCE_RESTRAINERS_RELIC(Signet.DELIVERANCE, "deliverance_restrainers_relic", "制约者的圣器",
                "开启武器必杀技后，获得相当于最大生命值20%的护盾，并提高抗打断能力，持续7秒。",
                "开启武器必杀技后，获得相当于最大生命值25%的护盾，并提高抗打断能力，持续7秒。",
                "开启武器必杀技后，获得相当于最大生命值30%的护盾，并提高抗打断能力，持续7秒。",
                "开启武器必杀技后，获得相当于最大生命值35%的护盾，并提高抗打断能力，持续7秒。"),
        DELIVERANCE_SEEKERS_ROBE(Signet.DELIVERANCE, "deliverance_seekers_robe", "求道者的法衣",
                "开启武器必杀技后，每秒回复3点能量，持续7秒。",
                "开启武器必杀技后，每秒回复4点能量，持续7秒。",
                "开启武器必杀技后，每秒回复5点能量，持续7秒。",
                "开启武器必杀技后，每秒回复6点能量，持续7秒。"),

        DELIVERANCE_KINGS_MUSTER(Signet.DELIVERANCE, "deliverance_kings_muster", "救世者的麋集",
                "终末之战状态结束时，对状态期间命中过的敌方单位追加相当于累计伤害50%的伤害，并重置玩家的战斗状态。",
                "终末之战状态结束时，对状态期间命中过的敌方单位追加相当于累计伤害57.5%的伤害，并重置玩家的战斗状态。",
                "终末之战状态结束时，对状态期间命中过的敌方单位追加相当于累计伤害65%的伤害，并重置玩家的战斗状态。",
                "终末之战状态结束时，对状态期间命中过的敌方单位追加相当于累计伤害75%的伤害，并重置玩家的战斗状态。"),
        DELIVERANCE_KINGS_EXPEDITION(Signet.DELIVERANCE, "deliverance_kings_expedition", "救世者的远征",
                "处于终末之战状态时，造成的全伤害提高20%。",
                "处于终末之战状态时，造成的全伤害提高23%。",
                "处于终末之战状态时，造成的全伤害提高26%。",
                "处于终末之战状态时，造成的全伤害提高30%。"),
        DELIVERANCE_KINGS_ECHO(Signet.DELIVERANCE, "deliverance_kings_echo", "救世者的余响",
                "终末之战状态和所有「救世」普通刻印提供的加成效果的持续时间延长至9秒。",
                "终末之战状态和所有「救世」普通刻印提供的加成效果的持续时间延长至10秒。",
                "终末之战状态和所有「救世」普通刻印提供的加成效果的持续时间延长至11秒。",
                "终末之战状态和所有「救世」普通刻印提供的加成效果的持续时间延长至12秒。"),

        DELIVERANCE_LONE_RESIDUAL_DREAM(Signet.DELIVERANCE, "deliverance_lone_residual_dream", "救世者的残梦",
                "处于救世之战状态时，所有「救世」普通刻印提供的加成效果提高60%。",
                "处于救世之战状态时，所有「救世」普通刻印提供的加成效果提高69%。",
                "处于救世之战状态时，所有「救世」普通刻印提供的加成效果提高78%。",
                "处于救世之战状态时，所有「救世」普通刻印提供的加成效果提高90%。"),
        DELIVERANCE_LONE_DECISION(Signet.DELIVERANCE, "deliverance_lone_decision", "救世者的决断",
                "处于救世之战状态时，每次攻击命中敌方单位后，额外提高2%全伤害，最多提高至50%。",
                "处于救世之战状态时，每次攻击命中敌方单位后，额外提高2.3%全伤害，最多提高至50%。",
                "处于救世之战状态时，每次攻击命中敌方单位后，额外提高2.6%全伤害，最多提高至50%。",
                "处于救世之战状态时，每次攻击命中敌方单位后，额外提高3%全伤害，最多提高至50%。"),
        DELIVERANCE_LONE_TRIUMPH(Signet.DELIVERANCE, "deliverance_lone_triumph", "救世者的凯旋",
                "进入救世之战状态后获得强化。首次攻击命中敌方单位后的4秒内，攻击无视敌方单位的防御；强化结束时，使附近所有非玩家单位受到的全伤害提高20%。",
                "进入救世之战状态后获得强化。首次攻击命中敌方单位后的5秒内，攻击无视敌方单位的防御；强化结束时，使附近所有非玩家单位受到的全伤害提高23%。",
                "进入救世之战状态后获得强化。首次攻击命中敌方单位后的6秒内，攻击无视敌方单位的防御；强化结束时，使附近所有非玩家单位受到的全伤害提高26%。",
                "进入救世之战状态后获得强化。首次攻击命中敌方单位后的7秒内，攻击无视敌方单位的防御；强化结束时，使附近所有非玩家单位受到的全伤害提高30%。");

        private final Signet signet;
        private final String id;
        private final String displayName;
        private final String[] descriptions;

        Skill(Signet signet, String id, String displayName, String... descriptions) {
            this.signet = signet;
            this.id = id;
            this.displayName = displayName;
            this.descriptions = descriptions;
        }

        public Signet signet() {
            return this.signet;
        }

        public String id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public String description(int level) {
            return this.descriptions[Math.min(Mth.clamp(level, 0, MAX_LEVEL), this.descriptions.length - 1)];
        }

        public String tooltipDescription(int level) {
            String value = description(level);
            return switch (this) {
                case VICISSITUDE_STRENGTH, VICISSITUDE_RESISTANCE ->
                        "未受伤时" + value + "，受伤后层数清零";
                case DECIMATION_HEALTH -> value.replace("生命上限 +", "生命上限提高");
                case DECIMATION_LOW_HEALTH -> "生命越低受到的全伤害越低，"
                        + value.replace("每损失1生命减伤", "每损失1生命值，受到的伤害降低");
                case DECIMATION_FIRE -> value.split("，")[0].replace("火焰伤害 +", "灼烧伤害提高");
                case HELIX_MAGIC -> "使用武器技后，来源于武器技的全伤害提高"
                        + value.substring(value.indexOf('+') + 1) + "，最大叠加4层，效果持续10秒";
                case HELIX_PENDULUM -> "使用武器技后，使场上的敌方单位受到的全伤害提高"
                        + value.replace("武器技使敌方单位易伤", "") + "，持续4.5秒，重复触发时刷新时间。";
                case HELIX_PARADOX -> "附魔时获得高等级附魔（V–VIII）\n修复物品时，使用的材料和经验减少到1"
                        + (level > 0 ? "\n" + value : "");
                case GOLD_STREAM -> value.replace("正面效果 +", "正面效果时间延长")
                        .replace("，矿物与熔炉 +", "\n熔炉速度加快")
                        + "\n矿物获取提升" + value.substring(value.lastIndexOf('+') + 1);
                case GOLD_PARADISE -> "武器能量越多造成的伤害越高，"
                        + value.replace("每10能量伤害 +", "每有10能量提高");
                case GOLD_BIRD -> "能量越多受到的全伤害越低，"
                        + value.replace("每10能量减伤", "每有10点能量降低").replace("，上限", "受到的全伤害（上限") + "）";
                case GOLD_ORCHARD -> "能量越低攻击的能量回复效率越高，" + value.substring(value.indexOf("最多"));
                case DISCIPLINE_ONE -> "每秒增加5点「规诫」计数，最高100点，按「规诫」计数"
                        + value.replace("每点规诫使普攻伤害 +", "")
                        + "的比例提高普攻造成的伤害，触发极限闪避技能时扣除当前50%的「规诫」计数";
                case DISCIPLINE_TWO -> "按「规诫」计数"
                        + value.replace("每点规诫使伤害抵抗 +", "")
                        + "的比例提高角色的伤害抵抗，召唤物或协同者发动技能时扣除当前50%的「规诫」计数。";
                case DISCIPLINE_THREE -> "按「规诫」计数"
                        + value.replace("每点规诫使能量回复 +", "")
                        + "的比例提高回复的能量，连击数到达15时扣除当前50%的「规诫」计数";
                case DISCIPLINE_FOUR -> "按「规诫」计数"
                        + value.replace("每点规诫使武器必杀技伤害 +", "")
                        + "的比例提高武器必杀技的伤害，使用武器技时扣除当前50%的「规诫」计数";
                case DISCIPLINE_FIVE -> "每次触发降低规诫计数时，使场上敌方单位受到的全伤害提高"
                        + value.replace("降低规诫时使敌方单位受到的全伤害 +", "")
                        + "，持续5秒，可刷新";
                case DISCIPLINE_SIX -> "单位死亡后，造成的全伤害提高"
                        + value.replace("单位死亡后全伤害 +", "")
                        + "，规诫计数将不再减少，持续10秒，重复触发刷新持续时间";
                case DELIVERANCE_ARMOR -> value.replace("自身的护甲值", "开启武器必杀技后，护甲值");
                case DELIVERANCE_KINGS_MUSTER -> value.replace("对状态期间命中过的敌方单位追加相当于累计伤害", "造成期间的")
                        .replace("并重置玩家的战斗状态", "并重置玩家的状态");
                case DELIVERANCE_LONE_TRIUMPH -> value.replace("附近所有非玩家单位", "当前场上所有除玩家以外、64格内的单位");
                default -> value;
            };
        }

        public boolean isUpgradeable() {
            return this != SETSURA_CURTAIN_RAIN_FOUR_LIGHTS
                    && this != SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY;
        }

        public boolean isDeliveranceSecondary() {
            return switch (this) {
                case DELIVERANCE_HUNTERS_MASK,
                        DELIVERANCE_RESTRAINERS_RELIC,
                        DELIVERANCE_SEEKERS_ROBE -> true;
                default -> false;
            };
        }

        public int deliveranceSecondaryBit() {
            return switch (this) {
                case DELIVERANCE_HUNTERS_MASK -> 1;
                case DELIVERANCE_RESTRAINERS_RELIC -> 1 << 1;
                case DELIVERANCE_SEEKERS_ROBE -> 1 << 2;
                default -> 0;
            };
        }

        public boolean isDeliveranceCoreSignet() {
            return deliveranceCore() != null;
        }

        public DeliveranceCore deliveranceCore() {
            return switch (this) {
                case DELIVERANCE_KINGS_MUSTER,
                        DELIVERANCE_KINGS_EXPEDITION,
                        DELIVERANCE_KINGS_ECHO -> DeliveranceCore.KINGS_SWORD;
                case DELIVERANCE_LONE_RESIDUAL_DREAM,
                        DELIVERANCE_LONE_DECISION,
                        DELIVERANCE_LONE_TRIUMPH -> DeliveranceCore.LONE_SHADOW;
                default -> null;
            };
        }

        public int deliveranceCoreSignetBit() {
            return switch (this) {
                case DELIVERANCE_KINGS_MUSTER -> 1;
                case DELIVERANCE_KINGS_EXPEDITION -> 1 << 1;
                case DELIVERANCE_KINGS_ECHO -> 1 << 2;
                case DELIVERANCE_LONE_RESIDUAL_DREAM -> 1 << 3;
                case DELIVERANCE_LONE_DECISION -> 1 << 4;
                case DELIVERANCE_LONE_TRIUMPH -> 1 << 5;
                default -> 0;
            };
        }

        public boolean isDeliveranceOrdinary() {
            return this.signet == Signet.DELIVERANCE && !isDeliveranceCoreSignet();
        }

        public boolean isDisciplineCoreSignet() {
            return disciplineCore() != null;
        }

        public boolean isDisciplineSecondary() {
            return switch (this) {
                case DISCIPLINE_FOUR, DISCIPLINE_FIVE, DISCIPLINE_SIX -> true;
                default -> false;
            };
        }

        public int disciplineSecondaryBit() {
            return switch (this) {
                case DISCIPLINE_FOUR -> 1;
                case DISCIPLINE_FIVE -> 1 << 1;
                case DISCIPLINE_SIX -> 1 << 2;
                default -> 0;
            };
        }

        public DisciplineCore disciplineCore() {
            return switch (this) {
                case DISCIPLINE_ASCETIC_BODY,
                        DISCIPLINE_ASCETIC_LIFE,
                        DISCIPLINE_ASCETIC_PROOF -> DisciplineCore.ASCETICISM;
                case DISCIPLINE_BLESSING_PUNISHMENT,
                        DISCIPLINE_BLESSING_VIRTUE,
                        DISCIPLINE_BLESSING_RETURN -> DisciplineCore.BLESSING;
                default -> null;
            };
        }

        public int disciplineCoreSignetBit() {
            return switch (this) {
                case DISCIPLINE_ASCETIC_BODY -> 1;
                case DISCIPLINE_ASCETIC_LIFE -> 1 << 1;
                case DISCIPLINE_ASCETIC_PROOF -> 1 << 2;
                case DISCIPLINE_BLESSING_PUNISHMENT -> 1 << 3;
                case DISCIPLINE_BLESSING_VIRTUE -> 1 << 4;
                case DISCIPLINE_BLESSING_RETURN -> 1 << 5;
                default -> 0;
            };
        }

        public boolean isHelixSecondary() {
            return switch (this) {
                case HELIX_SPOON, HELIX_COBWEB, HELIX_SNEER -> true;
                default -> false;
            };
        }

        public int helixSecondaryBit() {
            return switch (this) {
                case HELIX_SPOON -> 1;
                case HELIX_COBWEB -> 1 << 1;
                case HELIX_SNEER -> 1 << 2;
                default -> 0;
            };
        }

        public boolean isHelixCoreSignet() {
            return helixCore() != null;
        }

        public HelixCore helixCore() {
            return switch (this) {
                case HELIX_REVERSED_DOOR, HELIX_REVERSED_HALL,
                        HELIX_REVERSED_TRUTH -> HelixCore.REVERSED_SPIRAL;
                case HELIX_CONCLUSION_PRISON, HELIX_CONCLUSION_ORIGIN,
                        HELIX_CONCLUSION_POINTER -> HelixCore.CONCLUSION_SPIRAL;
                default -> null;
            };
        }

        public int helixCoreSignetBit() {
            return switch (this) {
                case HELIX_REVERSED_DOOR -> 1;
                case HELIX_REVERSED_HALL -> 1 << 1;
                case HELIX_REVERSED_TRUTH -> 1 << 2;
                case HELIX_CONCLUSION_PRISON -> 1 << 3;
                case HELIX_CONCLUSION_ORIGIN -> 1 << 4;
                case HELIX_CONCLUSION_POINTER -> 1 << 5;
                default -> 0;
            };
        }

        public boolean isDecimationSecondary() {
            return switch (this) {
                case DECIMATION_BLADE, DECIMATION_WUWANG, DECIMATION_WEISHEN -> true;
                default -> false;
            };
        }

        public int decimationSecondaryBit() {
            return switch (this) {
                case DECIMATION_BLADE -> 1;
                case DECIMATION_WUWANG -> 1 << 1;
                case DECIMATION_WEISHEN -> 1 << 2;
                default -> 0;
            };
        }

        public boolean isDecimationCoreSignet() {
            return decimationCore() != null;
        }

        public DecimationCore decimationCore() {
            return switch (this) {
                case DECIMATION_RELEASE_NONHUMAN, DECIMATION_RELEASE_ONE_PATH,
                        DECIMATION_RELEASE_KALPAS -> DecimationCore.BLOOD_RELEASE;
                case DECIMATION_RAIMENT_UNDYING, DECIMATION_RAIMENT_BURNING_SOUL,
                        DECIMATION_RAIMENT_HOMELAND -> DecimationCore.BLOOD_RAIMENT;
                default -> null;
            };
        }

        public int decimationCoreSignetBit() {
            return switch (this) {
                case DECIMATION_RELEASE_NONHUMAN -> 1;
                case DECIMATION_RELEASE_ONE_PATH -> 1 << 1;
                case DECIMATION_RELEASE_KALPAS -> 1 << 2;
                case DECIMATION_RAIMENT_UNDYING -> 1 << 3;
                case DECIMATION_RAIMENT_BURNING_SOUL -> 1 << 4;
                case DECIMATION_RAIMENT_HOMELAND -> 1 << 5;
                default -> 0;
            };
        }

        public boolean isBodhiSecondary() {
            return switch (this) {
                case BODHI_HEAVENLY_EYE, BODHI_MIND_READING,
                        BODHI_DIVINE_FOOT, BODHI_EXHAUSTION -> true;
                default -> false;
            };
        }

        public int bodhiSecondaryBit() {
            return switch (this) {
                case BODHI_HEAVENLY_EYE -> 1;
                case BODHI_MIND_READING -> 1 << 1;
                case BODHI_DIVINE_FOOT -> 1 << 2;
                case BODHI_EXHAUSTION -> 1 << 3;
                default -> 0;
            };
        }

        public boolean isBodhiCoreSignet() {
            return bodhiCore() != null;
        }

        public BodhiCore bodhiCore() {
            return switch (this) {
                case BODHI_TRUE_SELFLESS, BODHI_TRUE_IMPERMANENCE,
                        BODHI_TRUE_SILENCE -> BodhiCore.TRUE_WORD;
                case BODHI_SECRET_IMPERMANENCE, BODHI_SECRET_SELFLESS,
                        BODHI_SECRET_SILENCE -> BodhiCore.SECRET_WORD;
                default -> null;
            };
        }

        public int bodhiCoreSignetBit() {
            return switch (this) {
                case BODHI_TRUE_SELFLESS -> 1;
                case BODHI_TRUE_IMPERMANENCE -> 1 << 1;
                case BODHI_TRUE_SILENCE -> 1 << 2;
                case BODHI_SECRET_IMPERMANENCE -> 1 << 3;
                case BODHI_SECRET_SELFLESS -> 1 << 4;
                case BODHI_SECRET_SILENCE -> 1 << 5;
                default -> 0;
            };
        }

        public boolean isDaybreakSecondary() {
            return switch (this) {
                case DAYBREAK_WING, DAYBREAK_HORN,
                        DAYBREAK_BREATH, DAYBREAK_HEART -> true;
                default -> false;
            };
        }

        public int daybreakSecondaryBit() {
            return switch (this) {
                case DAYBREAK_WING -> 1;
                case DAYBREAK_HORN -> 1 << 1;
                case DAYBREAK_BREATH -> 1 << 2;
                case DAYBREAK_HEART -> 1 << 3;
                default -> 0;
            };
        }

        public boolean isDaybreakCoreSignet() {
            return daybreakCore() != null;
        }

        public DaybreakCore daybreakCore() {
            return switch (this) {
                case DAYBREAK_LIGHT_HERO, DAYBREAK_LIGHT_GOOD_EVIL,
                        DAYBREAK_LIGHT_VOW -> DaybreakCore.UNFADING_LIGHT;
                case DAYBREAK_NIGHT_DAWN, DAYBREAK_NIGHT_CHOICE,
                        DAYBREAK_NIGHT_INTENT -> DaybreakCore.LONELY_NIGHT;
                default -> null;
            };
        }

        public int daybreakCoreSignetBit() {
            return switch (this) {
                case DAYBREAK_LIGHT_HERO -> 1;
                case DAYBREAK_LIGHT_GOOD_EVIL -> 1 << 1;
                case DAYBREAK_LIGHT_VOW -> 1 << 2;
                case DAYBREAK_NIGHT_DAWN -> 1 << 3;
                case DAYBREAK_NIGHT_CHOICE -> 1 << 4;
                case DAYBREAK_NIGHT_INTENT -> 1 << 5;
                default -> 0;
            };
        }

        public boolean isSetsuraSecondary() {
            return switch (this) {
                case SETSURA_RED_LEAVES, SETSURA_PEONY,
                        SETSURA_WISTERIA, SETSURA_IRIS -> true;
                default -> false;
            };
        }

        public int setsuraSecondaryBit() {
            return switch (this) {
                case SETSURA_RED_LEAVES -> 1;
                case SETSURA_PEONY -> 1 << 1;
                case SETSURA_WISTERIA -> 1 << 2;
                case SETSURA_IRIS -> 1 << 3;
                default -> 0;
            };
        }

        public boolean isSetsuraCoreSignet() {
            return setsuraCore() != null;
        }

        public SetsuraCore setsuraCore() {
            return switch (this) {
                case SETSURA_CURTAIN_RAIN_FOUR_LIGHTS,
                        SETSURA_CURTAIN_MOON_SAKE,
                        SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY -> SetsuraCore.SAKURA_CURTAIN;
                case SETSURA_YAKSHA_MOMENTARY_MYRIAD,
                        SETSURA_YAKSHA_ENDLESS_EVIL,
                        SETSURA_YAKSHA_ALL_FORMS -> SetsuraCore.NIGHT_YAKSHA;
                default -> null;
            };
        }

        public int setsuraCoreSignetBit() {
            return switch (this) {
                case SETSURA_CURTAIN_RAIN_FOUR_LIGHTS -> 1;
                case SETSURA_CURTAIN_MOON_SAKE -> 1 << 1;
                case SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY -> 1 << 2;
                case SETSURA_YAKSHA_MOMENTARY_MYRIAD -> 1 << 3;
                case SETSURA_YAKSHA_ENDLESS_EVIL -> 1 << 4;
                case SETSURA_YAKSHA_ALL_FORMS -> 1 << 5;
                default -> 0;
            };
        }

        public boolean isGoldCoreSignet() {
            return goldCore() != null;
        }

        public GoldCore goldCore() {
            return switch (this) {
                case GOLD_ARIA_FERTILE, GOLD_ARIA_LIFE, GOLD_ARIA_DAWN -> GoldCore.ARIA;
                case GOLD_ECHO_WITHERED, GOLD_ECHO_COLD_WIND,
                        GOLD_ECHO_SILENT_NIGHT -> GoldCore.ECHO;
                default -> null;
            };
        }

        public int goldCoreSignetBit() {
            return switch (this) {
                case GOLD_ARIA_FERTILE -> 1;
                case GOLD_ARIA_LIFE -> 1 << 1;
                case GOLD_ARIA_DAWN -> 1 << 2;
                case GOLD_ECHO_WITHERED -> 1 << 3;
                case GOLD_ECHO_COLD_WIND -> 1 << 4;
                case GOLD_ECHO_SILENT_NIGHT -> 1 << 5;
                default -> 0;
            };
        }

        public boolean isGoldSecondary() {
            return switch (this) {
                case GOLD_BIRD, GOLD_ORCHARD, GOLD_WINE, GOLD_JADE -> true;
                default -> false;
            };
        }

        public int goldSecondaryBit() {
            return switch (this) {
                case GOLD_BIRD -> 1;
                case GOLD_ORCHARD -> 1 << 1;
                case GOLD_WINE -> 1 << 2;
                case GOLD_JADE -> 1 << 3;
                default -> 0;
            };
        }

        public boolean isExtensionSecondary() {
            return switch (this) {
                case REVERIE_CAT_VERSE, REVERIE_BLADE_TAIL,
                        REVERIE_STREET_JOY, REVERIE_PC,
                        VICISSITUDE_TIME, VICISSITUDE_ROBE, VICISSITUDE_DREAM, VICISSITUDE_WORDS,
                        STARS_YELLOW, STARS_BLACK, STARS_WHITE, STARS_GRAY,
                        INFINITY_P, INFINITY_B, INFINITY_E -> true;
                default -> false;
            };
        }

        public int extensionSecondaryBit() {
            return switch (this) {
                case REVERIE_PC, VICISSITUDE_TIME, STARS_YELLOW, INFINITY_P -> 1;
                case REVERIE_CAT_VERSE, VICISSITUDE_ROBE, STARS_BLACK, INFINITY_B -> 1 << 1;
                case REVERIE_BLADE_TAIL, VICISSITUDE_DREAM, STARS_WHITE, INFINITY_E -> 1 << 2;
                case REVERIE_STREET_JOY, VICISSITUDE_WORDS, STARS_GRAY -> 1 << 3;
                default -> 0;
            };
        }

        public ExtensionCore extensionCore() {
            return switch (this) {
                case INFINITY_DEATH_X, INFINITY_UNKNOWN_X, INFINITY_NEWBORN_X -> ExtensionCore.INFINITY_X;
                case INFINITY_SACRIFICE_M, INFINITY_IMMORTAL_M, INFINITY_COLLAPSE_M -> ExtensionCore.INFINITY_M;
                case STARS_WILDFLOWER_SHINING, STARS_FLAME_SHINING, STARS_LEAF_SHINING -> ExtensionCore.STARS_SHINING;
                case STARS_WILDFLOWER_DIM, STARS_LEAF_DIM, STARS_FLAME_DIM -> ExtensionCore.STARS_DIM;
                case VICISSITUDE_RED_DUST, VICISSITUDE_PROSPERITY, VICISSITUDE_FAMILIAR -> ExtensionCore.VICISSITUDE_RESOLVE;
                case VICISSITUDE_REED, VICISSITUDE_FEATHER, VICISSITUDE_AWAKEN -> ExtensionCore.VICISSITUDE_MEMORY;
                case REVERIE_BOSS, REVERIE_CAT_PUNCH, REVERIE_LUCKY -> ExtensionCore.REVERIE_JOY;
                case REVERIE_ZERO, REVERIE_SNOW, REVERIE_DREAM_TRUE -> ExtensionCore.REVERIE_DREAM;
                default -> null;
            };
        }

        public boolean isExtensionCoreSignet() {
            return extensionCore() != null;
        }

        public int extensionCoreSignetBit() {
            ExtensionCore core = extensionCore();
            if (core == null) return 0;
            return switch (this) {
                case INFINITY_DEATH_X, INFINITY_SACRIFICE_M,
                        STARS_WILDFLOWER_SHINING, STARS_WILDFLOWER_DIM,
                        VICISSITUDE_RED_DUST, VICISSITUDE_REED,
                        REVERIE_BOSS, REVERIE_ZERO -> 1;
                case INFINITY_UNKNOWN_X, INFINITY_IMMORTAL_M,
                        STARS_FLAME_SHINING, STARS_LEAF_DIM,
                        VICISSITUDE_PROSPERITY, VICISSITUDE_FEATHER,
                        REVERIE_CAT_PUNCH, REVERIE_SNOW -> 1 << 1;
                default -> 1 << 2;
            };
        }
    }

    private SignetUpgradeData() {
    }

    public static Signet fromStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (Signet signet : Signet.values()) {
            if (stack.is(signet.item())) return signet;
        }
        return null;
    }

    public static boolean isUpgradeableSignet(ItemStack stack) {
        return fromStack(stack) != null;
    }

    public static Item upgradeItemFor(Signet signet) {
        if (signet == null) return null;
        return switch (signet) {
            case REVERIE -> ModItems.REVERIE_SIGNET_UPGRADE.get();
            case VICISSITUDE -> ModItems.VICISSITUDE_SIGNET_UPGRADE.get();
            case STARS -> ModItems.STARS_SIGNET_UPGRADE.get();
            case INFINITY -> ModItems.INFINITY_SIGNET_UPGRADE.get();
            case DAYBREAK -> ModItems.DAYBREAK_SIGNET_UPGRADE.get();
            case SETSURA -> ModItems.SETSURA_SIGNET_UPGRADE.get();
            case BODHI -> ModItems.BODHI_SIGNET_UPGRADE.get();
            case DECIMATION -> ModItems.DECIMATION_SIGNET_UPGRADE.get();
            case HELIX -> ModItems.HELIX_SIGNET_UPGRADE.get();
            case GOLD -> ModItems.GOLD_SIGNET_UPGRADE.get();
            case DISCIPLINE -> ModItems.DISCIPLINE_SIGNET_UPGRADE.get();
            case DELIVERANCE -> ModItems.DELIVERANCE_SIGNET_UPGRADE.get();
        };
    }

    public static int countUpgradeItems(Player player, Signet signet) {
        Item upgradeItem = upgradeItemFor(signet);
        if (upgradeItem == null) return 0;
        int count = 0;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(upgradeItem)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean removeUpgradeItems(Player player, Signet signet, int amount) {
        if (amount <= 0 || countUpgradeItems(player, signet) < amount) return false;
        Item upgradeItem = upgradeItemFor(signet);
        Inventory inventory = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(upgradeItem)) continue;
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }
        inventory.setChanged();
        return remaining == 0;
    }

    public static List<Skill> skillsFor(Signet signet) {
        if (signet == null) return List.of();
        return Arrays.stream(Skill.values())
                .filter(skill -> skill.signet() == signet
                        && !skill.isDeliveranceSecondary()
                        && !skill.isDeliveranceCoreSignet()
                        && !skill.isDisciplineSecondary()
                        && !skill.isDisciplineCoreSignet()
                        && !skill.isHelixSecondary()
                        && !skill.isHelixCoreSignet()
                        && !skill.isDecimationSecondary()
                        && !skill.isDecimationCoreSignet()
                        && !skill.isBodhiSecondary()
                        && !skill.isBodhiCoreSignet()
                        && !skill.isDaybreakSecondary()
                        && !skill.isDaybreakCoreSignet()
                        && !skill.isSetsuraSecondary()
                        && !skill.isSetsuraCoreSignet()
                        && !skill.isGoldSecondary()
                        && !skill.isGoldCoreSignet()
                        && !skill.isExtensionSecondary()
                        && !skill.isExtensionCoreSignet())
                .toList();
    }

    private static List<Skill> allSkillsFor(Signet signet) {
        if (signet == null) return List.of();
        return Arrays.stream(Skill.values())
                .filter(skill -> skill.signet() == signet)
                .toList();
    }

    public static int getDeliveranceSecondaryUnlockMask(Player player) {
        if (player == null) return 0;
        return getPlayerPersistedData(player).getInt(NBT_DELIVERANCE_SECONDARY_UNLOCKS);
    }

    public static int getStackDeliveranceSecondaryUnlockMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DELIVERANCE) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_DELIVERANCE_SECONDARY_UNLOCKS);
    }

    private static void setStackDeliveranceSecondaryUnlockMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DELIVERANCE) return;
        stack.getOrCreateTag().putInt(NBT_DELIVERANCE_SECONDARY_UNLOCKS, mask);
    }

    public static boolean isSecondarySkillUnlocked(Player player, Skill skill) {
        if (skill == null || !skill.isDeliveranceSecondary()) return false;
        return (getDeliveranceSecondaryUnlockMask(player) & skill.deliveranceSecondaryBit()) != 0;
    }

    public static Skill secondarySkillFromItem(ItemStack stack) {
        if (stack.is(ModItems.DELIVERANCE_HUNTERS_MASK.get())) {
            return Skill.DELIVERANCE_HUNTERS_MASK;
        }
        if (stack.is(ModItems.DELIVERANCE_RESTRAINERS_RELIC.get())) {
            return Skill.DELIVERANCE_RESTRAINERS_RELIC;
        }
        if (stack.is(ModItems.DELIVERANCE_SEEKERS_ROBE.get())) {
            return Skill.DELIVERANCE_SEEKERS_ROBE;
        }
        return null;
    }

    public static boolean isDeliveranceSecondaryItem(ItemStack stack) {
        return secondarySkillFromItem(stack) != null;
    }

    public static DeliveranceCore deliveranceCoreFromItem(ItemStack stack) {
        if (stack.is(ModItems.DELIVERANCE_KINGS_SWORD.get())) {
            return DeliveranceCore.KINGS_SWORD;
        }
        if (stack.is(ModItems.DELIVERANCE_LONE_SHADOW.get())) {
            return DeliveranceCore.LONE_SHADOW;
        }
        return null;
    }

    public static Skill deliveranceCoreSignetFromItem(ItemStack stack) {
        if (stack.is(ModItems.DELIVERANCE_KINGS_MUSTER.get())) {
            return Skill.DELIVERANCE_KINGS_MUSTER;
        }
        if (stack.is(ModItems.DELIVERANCE_KINGS_EXPEDITION.get())) {
            return Skill.DELIVERANCE_KINGS_EXPEDITION;
        }
        if (stack.is(ModItems.DELIVERANCE_KINGS_ECHO.get())) {
            return Skill.DELIVERANCE_KINGS_ECHO;
        }
        if (stack.is(ModItems.DELIVERANCE_LONE_RESIDUAL_DREAM.get())) {
            return Skill.DELIVERANCE_LONE_RESIDUAL_DREAM;
        }
        if (stack.is(ModItems.DELIVERANCE_LONE_DECISION.get())) {
            return Skill.DELIVERANCE_LONE_DECISION;
        }
        if (stack.is(ModItems.DELIVERANCE_LONE_TRIUMPH.get())) {
            return Skill.DELIVERANCE_LONE_TRIUMPH;
        }
        return null;
    }

    public static boolean isDeliveranceFusionItem(ItemStack stack) {
        return secondarySkillFromItem(stack) != null
                || deliveranceCoreFromItem(stack) != null
                || deliveranceCoreSignetFromItem(stack) != null;
    }

    public static DisciplineCore disciplineCoreFromItem(ItemStack stack) {
        if (stack.is(ModItems.DISCIPLINE_ASCETICISM.get())) {
            return DisciplineCore.ASCETICISM;
        }
        if (stack.is(ModItems.DISCIPLINE_BLESSING.get())) {
            return DisciplineCore.BLESSING;
        }
        return null;
    }

    public static Skill disciplineCoreSignetFromItem(ItemStack stack) {
        if (stack.is(ModItems.DISCIPLINE_ASCETIC_BODY.get())) return Skill.DISCIPLINE_ASCETIC_BODY;
        if (stack.is(ModItems.DISCIPLINE_ASCETIC_LIFE.get())) return Skill.DISCIPLINE_ASCETIC_LIFE;
        if (stack.is(ModItems.DISCIPLINE_ASCETIC_PROOF.get())) return Skill.DISCIPLINE_ASCETIC_PROOF;
        if (stack.is(ModItems.DISCIPLINE_BLESSING_PUNISHMENT.get())) return Skill.DISCIPLINE_BLESSING_PUNISHMENT;
        if (stack.is(ModItems.DISCIPLINE_BLESSING_VIRTUE.get())) return Skill.DISCIPLINE_BLESSING_VIRTUE;
        if (stack.is(ModItems.DISCIPLINE_BLESSING_RETURN.get())) return Skill.DISCIPLINE_BLESSING_RETURN;
        return null;
    }

    public static Skill disciplineSecondarySkillFromItem(ItemStack stack) {
        if (stack.is(ModItems.DISCIPLINE_FOUR.get())) return Skill.DISCIPLINE_FOUR;
        if (stack.is(ModItems.DISCIPLINE_FIVE.get())) return Skill.DISCIPLINE_FIVE;
        if (stack.is(ModItems.DISCIPLINE_SIX.get())) return Skill.DISCIPLINE_SIX;
        return null;
    }

    public static boolean isDisciplineFusionItem(ItemStack stack) {
        return disciplineSecondarySkillFromItem(stack) != null
                || disciplineCoreFromItem(stack) != null
                || disciplineCoreSignetFromItem(stack) != null;
    }

    public static HelixCore helixCoreFromItem(ItemStack stack) {
        if (stack.is(ModItems.HELIX_REVERSED_SPIRAL.get())) return HelixCore.REVERSED_SPIRAL;
        if (stack.is(ModItems.HELIX_CONCLUSION_SPIRAL.get())) return HelixCore.CONCLUSION_SPIRAL;
        return null;
    }

    public static Skill helixCoreSignetFromItem(ItemStack stack) {
        if (stack.is(ModItems.HELIX_REVERSED_DOOR.get())) return Skill.HELIX_REVERSED_DOOR;
        if (stack.is(ModItems.HELIX_REVERSED_HALL.get())) return Skill.HELIX_REVERSED_HALL;
        if (stack.is(ModItems.HELIX_REVERSED_TRUTH.get())) return Skill.HELIX_REVERSED_TRUTH;
        if (stack.is(ModItems.HELIX_CONCLUSION_PRISON.get())) return Skill.HELIX_CONCLUSION_PRISON;
        if (stack.is(ModItems.HELIX_CONCLUSION_ORIGIN.get())) return Skill.HELIX_CONCLUSION_ORIGIN;
        if (stack.is(ModItems.HELIX_CONCLUSION_POINTER.get())) return Skill.HELIX_CONCLUSION_POINTER;
        return null;
    }

    public static Skill helixSecondarySkillFromItem(ItemStack stack) {
        if (stack.is(ModItems.HELIX_SPOON.get())) return Skill.HELIX_SPOON;
        if (stack.is(ModItems.HELIX_COBWEB.get())) return Skill.HELIX_COBWEB;
        if (stack.is(ModItems.HELIX_SNEER.get())) return Skill.HELIX_SNEER;
        return null;
    }

    public static boolean isHelixFusionItem(ItemStack stack) {
        return helixSecondarySkillFromItem(stack) != null
                || helixCoreFromItem(stack) != null || helixCoreSignetFromItem(stack) != null;
    }

    public static DecimationCore decimationCoreFromItem(ItemStack stack) {
        if (stack.is(ModItems.DECIMATION_BLOOD_RELEASE.get())) return DecimationCore.BLOOD_RELEASE;
        if (stack.is(ModItems.DECIMATION_BLOOD_RAIMENT.get())) return DecimationCore.BLOOD_RAIMENT;
        return null;
    }

    public static Skill decimationCoreSignetFromItem(ItemStack stack) {
        if (stack.is(ModItems.DECIMATION_RELEASE_NONHUMAN.get())) return Skill.DECIMATION_RELEASE_NONHUMAN;
        if (stack.is(ModItems.DECIMATION_RELEASE_ONE_PATH.get())) return Skill.DECIMATION_RELEASE_ONE_PATH;
        if (stack.is(ModItems.DECIMATION_RELEASE_KALPAS.get())) return Skill.DECIMATION_RELEASE_KALPAS;
        if (stack.is(ModItems.DECIMATION_RAIMENT_UNDYING.get())) return Skill.DECIMATION_RAIMENT_UNDYING;
        if (stack.is(ModItems.DECIMATION_RAIMENT_BURNING_SOUL.get())) return Skill.DECIMATION_RAIMENT_BURNING_SOUL;
        if (stack.is(ModItems.DECIMATION_RAIMENT_HOMELAND.get())) return Skill.DECIMATION_RAIMENT_HOMELAND;
        return null;
    }

    public static Skill decimationSecondarySkillFromItem(ItemStack stack) {
        if (stack.is(ModItems.DECIMATION_BLADE.get())) return Skill.DECIMATION_BLADE;
        if (stack.is(ModItems.DECIMATION_WUWANG.get())) return Skill.DECIMATION_WUWANG;
        if (stack.is(ModItems.DECIMATION_WEISHEN.get())) return Skill.DECIMATION_WEISHEN;
        return null;
    }

    public static boolean isDecimationFusionItem(ItemStack stack) {
        return decimationSecondarySkillFromItem(stack) != null
                || decimationCoreFromItem(stack) != null || decimationCoreSignetFromItem(stack) != null;
    }

    public static BodhiCore bodhiCoreFromItem(ItemStack stack) {
        if (stack.is(ModItems.BODHI_TRUE_WORD.get())) return BodhiCore.TRUE_WORD;
        if (stack.is(ModItems.BODHI_SECRET_WORD.get())) return BodhiCore.SECRET_WORD;
        return null;
    }

    public static Skill bodhiCoreSignetFromItem(ItemStack stack) {
        if (stack.is(ModItems.BODHI_TRUE_SELFLESS.get())) return Skill.BODHI_TRUE_SELFLESS;
        if (stack.is(ModItems.BODHI_TRUE_IMPERMANENCE.get())) return Skill.BODHI_TRUE_IMPERMANENCE;
        if (stack.is(ModItems.BODHI_TRUE_SILENCE.get())) return Skill.BODHI_TRUE_SILENCE;
        if (stack.is(ModItems.BODHI_SECRET_IMPERMANENCE.get())) return Skill.BODHI_SECRET_IMPERMANENCE;
        if (stack.is(ModItems.BODHI_SECRET_SELFLESS.get())) return Skill.BODHI_SECRET_SELFLESS;
        if (stack.is(ModItems.BODHI_SECRET_SILENCE.get())) return Skill.BODHI_SECRET_SILENCE;
        return null;
    }

    public static Skill bodhiSecondarySkillFromItem(ItemStack stack) {
        if (stack.is(ModItems.BODHI_HEAVENLY_EYE.get())) return Skill.BODHI_HEAVENLY_EYE;
        if (stack.is(ModItems.BODHI_MIND_READING.get())) return Skill.BODHI_MIND_READING;
        if (stack.is(ModItems.BODHI_DIVINE_FOOT.get())) return Skill.BODHI_DIVINE_FOOT;
        if (stack.is(ModItems.BODHI_EXHAUSTION.get())) return Skill.BODHI_EXHAUSTION;
        return null;
    }

    public static boolean isBodhiFusionItem(ItemStack stack) {
        return bodhiSecondarySkillFromItem(stack) != null
                || bodhiCoreFromItem(stack) != null || bodhiCoreSignetFromItem(stack) != null;
    }

    public static DaybreakCore daybreakCoreFromItem(ItemStack stack) {
        if (stack.is(ModItems.DAYBREAK_UNFADING_LIGHT.get())) return DaybreakCore.UNFADING_LIGHT;
        if (stack.is(ModItems.DAYBREAK_LONELY_NIGHT.get())) return DaybreakCore.LONELY_NIGHT;
        return null;
    }

    public static Skill daybreakCoreSignetFromItem(ItemStack stack) {
        if (stack.is(ModItems.DAYBREAK_LIGHT_HERO.get())) return Skill.DAYBREAK_LIGHT_HERO;
        if (stack.is(ModItems.DAYBREAK_LIGHT_GOOD_EVIL.get())) return Skill.DAYBREAK_LIGHT_GOOD_EVIL;
        if (stack.is(ModItems.DAYBREAK_LIGHT_VOW.get())) return Skill.DAYBREAK_LIGHT_VOW;
        if (stack.is(ModItems.DAYBREAK_NIGHT_DAWN.get())) return Skill.DAYBREAK_NIGHT_DAWN;
        if (stack.is(ModItems.DAYBREAK_NIGHT_CHOICE.get())) return Skill.DAYBREAK_NIGHT_CHOICE;
        if (stack.is(ModItems.DAYBREAK_NIGHT_INTENT.get())) return Skill.DAYBREAK_NIGHT_INTENT;
        return null;
    }

    public static Skill daybreakSecondarySkillFromItem(ItemStack stack) {
        if (stack.is(ModItems.DAYBREAK_WING.get())) return Skill.DAYBREAK_WING;
        if (stack.is(ModItems.DAYBREAK_HORN.get())) return Skill.DAYBREAK_HORN;
        if (stack.is(ModItems.DAYBREAK_BREATH.get())) return Skill.DAYBREAK_BREATH;
        if (stack.is(ModItems.DAYBREAK_HEART.get())) return Skill.DAYBREAK_HEART;
        return null;
    }

    public static boolean isDaybreakFusionItem(ItemStack stack) {
        return daybreakSecondarySkillFromItem(stack) != null
                || daybreakCoreFromItem(stack) != null || daybreakCoreSignetFromItem(stack) != null;
    }

    public static ExtensionCore extensionCoreFromItem(ItemStack stack) {
        return stack.getItem() instanceof ExtensionFusionSignetItem item ? item.core() : null;
    }

    public static Skill extensionSkillFromItem(ItemStack stack) {
        return stack.getItem() instanceof ExtensionFusionSignetItem item ? item.skill() : null;
    }

    public static Skill extensionSecondarySkillFromItem(ItemStack stack) {
        Skill skill = extensionSkillFromItem(stack);
        return skill != null && skill.isExtensionSecondary() ? skill : null;
    }

    public static Skill extensionCoreSignetFromItem(ItemStack stack) {
        Skill skill = extensionSkillFromItem(stack);
        return skill != null && skill.isExtensionCoreSignet() ? skill : null;
    }

    public static boolean isExtensionFusionItem(ItemStack stack) {
        return extensionCoreFromItem(stack) != null || extensionSkillFromItem(stack) != null;
    }

    public static SetsuraCore setsuraCoreFromItem(ItemStack stack) {
        if (stack.is(ModItems.SETSURA_SAKURA_CURTAIN.get())) return SetsuraCore.SAKURA_CURTAIN;
        if (stack.is(ModItems.SETSURA_NIGHT_YAKSHA.get())) return SetsuraCore.NIGHT_YAKSHA;
        return null;
    }

    public static Skill setsuraCoreSignetFromItem(ItemStack stack) {
        if (stack.is(ModItems.SETSURA_CURTAIN_RAIN_FOUR_LIGHTS.get())) return Skill.SETSURA_CURTAIN_RAIN_FOUR_LIGHTS;
        if (stack.is(ModItems.SETSURA_CURTAIN_MOON_SAKE.get())) return Skill.SETSURA_CURTAIN_MOON_SAKE;
        if (stack.is(ModItems.SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY.get())) return Skill.SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY;
        if (stack.is(ModItems.SETSURA_YAKSHA_MOMENTARY_MYRIAD.get())) return Skill.SETSURA_YAKSHA_MOMENTARY_MYRIAD;
        if (stack.is(ModItems.SETSURA_YAKSHA_ENDLESS_EVIL.get())) return Skill.SETSURA_YAKSHA_ENDLESS_EVIL;
        if (stack.is(ModItems.SETSURA_YAKSHA_ALL_FORMS.get())) return Skill.SETSURA_YAKSHA_ALL_FORMS;
        return null;
    }

    public static Skill setsuraSecondarySkillFromItem(ItemStack stack) {
        if (stack.is(ModItems.SETSURA_RED_LEAVES.get())) return Skill.SETSURA_RED_LEAVES;
        if (stack.is(ModItems.SETSURA_PEONY.get())) return Skill.SETSURA_PEONY;
        if (stack.is(ModItems.SETSURA_WISTERIA.get())) return Skill.SETSURA_WISTERIA;
        if (stack.is(ModItems.SETSURA_IRIS.get())) return Skill.SETSURA_IRIS;
        return null;
    }

    public static boolean isSetsuraFusionItem(ItemStack stack) {
        return setsuraSecondarySkillFromItem(stack) != null
                || setsuraCoreFromItem(stack) != null || setsuraCoreSignetFromItem(stack) != null;
    }

    public static GoldCore goldCoreFromItem(ItemStack stack) {
        if (stack.is(ModItems.GOLD_ARIA.get())) return GoldCore.ARIA;
        if (stack.is(ModItems.GOLD_ECHO.get())) return GoldCore.ECHO;
        return null;
    }

    public static Skill goldCoreSignetFromItem(ItemStack stack) {
        if (stack.is(ModItems.GOLD_ARIA_FERTILE.get())) return Skill.GOLD_ARIA_FERTILE;
        if (stack.is(ModItems.GOLD_ARIA_LIFE.get())) return Skill.GOLD_ARIA_LIFE;
        if (stack.is(ModItems.GOLD_ARIA_DAWN.get())) return Skill.GOLD_ARIA_DAWN;
        if (stack.is(ModItems.GOLD_ECHO_WITHERED.get())) return Skill.GOLD_ECHO_WITHERED;
        if (stack.is(ModItems.GOLD_ECHO_COLD_WIND.get())) return Skill.GOLD_ECHO_COLD_WIND;
        if (stack.is(ModItems.GOLD_ECHO_SILENT_NIGHT.get())) return Skill.GOLD_ECHO_SILENT_NIGHT;
        return null;
    }

    public static Skill goldSecondarySkillFromItem(ItemStack stack) {
        if (stack.is(ModItems.GOLD_BIRD.get())) return Skill.GOLD_BIRD;
        if (stack.is(ModItems.GOLD_ORCHARD.get())) return Skill.GOLD_ORCHARD;
        if (stack.is(ModItems.GOLD_WINE.get())) return Skill.GOLD_WINE;
        if (stack.is(ModItems.GOLD_JADE.get())) return Skill.GOLD_JADE;
        return null;
    }

    public static boolean isGoldFusionItem(ItemStack stack) {
        return goldSecondarySkillFromItem(stack) != null
                || goldCoreFromItem(stack) != null || goldCoreSignetFromItem(stack) != null;
    }

    public static boolean isAnyFusionItem(ItemStack stack) {
        return isDeliveranceFusionItem(stack) || isDisciplineFusionItem(stack)
                || isGoldFusionItem(stack) || isHelixFusionItem(stack)
                || isDecimationFusionItem(stack) || isBodhiFusionItem(stack)
                || isSetsuraFusionItem(stack) || isDaybreakFusionItem(stack)
                || isExtensionFusionItem(stack);
    }

    public static boolean fuseSecondarySkill(Player player, ItemStack deliveranceStack, Skill skill) {
        if (player == null
                || fromStack(deliveranceStack) != Signet.DELIVERANCE
                || skill == null
                || !skill.isDeliveranceSecondary()) {
            return false;
        }
        synchronizeLevels(player, deliveranceStack, Signet.DELIVERANCE);
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.deliveranceSecondaryBit();
        int current = persisted.getInt(NBT_DELIVERANCE_SECONDARY_UNLOCKS);
        if ((current & bit) != 0) return false;
        int fusedMask = current | bit;
        persisted.putInt(NBT_DELIVERANCE_SECONDARY_UNLOCKS, fusedMask);
        setStackDeliveranceSecondaryUnlockMask(deliveranceStack, fusedMask);
        synchronizeLevels(player, deliveranceStack, Signet.DELIVERANCE);
        return true;
    }

    public static DeliveranceCore getDeliveranceCore(Player player) {
        if (player == null) return null;
        return DeliveranceCore.fromId(
                getPlayerPersistedData(player).getInt(NBT_DELIVERANCE_CORE)
        );
    }

    public static DeliveranceCore getStackDeliveranceCore(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DELIVERANCE) return null;
        CompoundTag tag = stack.getTag();
        return tag == null ? null : DeliveranceCore.fromId(tag.getInt(NBT_DELIVERANCE_CORE));
    }

    private static void setStackDeliveranceCore(ItemStack stack, DeliveranceCore core) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DELIVERANCE) return;
        if (core == null) {
            stack.getOrCreateTag().remove(NBT_DELIVERANCE_CORE);
        } else {
            stack.getOrCreateTag().putInt(NBT_DELIVERANCE_CORE, core.id());
        }
    }

    public static int getDeliveranceCoreSignetMask(Player player) {
        if (player == null) return 0;
        return getPlayerPersistedData(player).getInt(NBT_DELIVERANCE_CORE_SIGNETS);
    }

    public static int getStackDeliveranceCoreSignetMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DELIVERANCE) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_DELIVERANCE_CORE_SIGNETS);
    }

    private static void setStackDeliveranceCoreSignetMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DELIVERANCE) return;
        stack.getOrCreateTag().putInt(NBT_DELIVERANCE_CORE_SIGNETS, mask);
    }

    public static boolean hasDeliveranceCoreSignet(Player player, Skill skill) {
        if (skill == null || !skill.isDeliveranceCoreSignet()) return false;
        DeliveranceCore core = getDeliveranceCore(player);
        return core == skill.deliveranceCore()
                && (getDeliveranceCoreSignetMask(player)
                & skill.deliveranceCoreSignetBit()) != 0;
    }

    public static boolean fuseDeliveranceCore(
            Player player,
            ItemStack deliveranceStack,
            DeliveranceCore core
    ) {
        if (player == null
                || fromStack(deliveranceStack) != Signet.DELIVERANCE
                || core == null
                || getDeliveranceCore(player) != null) {
            return false;
        }
        DeliveranceCore selected = getSelectedDeliveranceCore(player);
        if (selected != null && selected != core) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        persisted.putInt(NBT_DELIVERANCE_CORE, core.id());
        setStackDeliveranceCore(deliveranceStack, core);
        return true;
    }

    public static boolean fuseDeliveranceCoreSignet(
            Player player,
            ItemStack deliveranceStack,
            Skill skill
    ) {
        if (player == null
                || fromStack(deliveranceStack) != Signet.DELIVERANCE
                || skill == null
                || !skill.isDeliveranceCoreSignet()
                || getDeliveranceCore(player) != skill.deliveranceCore()) {
            return false;
        }
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.deliveranceCoreSignetBit();
        int current = persisted.getInt(NBT_DELIVERANCE_CORE_SIGNETS);
        if ((current & bit) != 0) return false;
        int fusedMask = current | bit;
        persisted.putInt(NBT_DELIVERANCE_CORE_SIGNETS, fusedMask);
        setStackDeliveranceCoreSignetMask(deliveranceStack, fusedMask);
        synchronizeLevels(player, deliveranceStack, Signet.DELIVERANCE);
        return true;
    }

    public static DisciplineCore getDisciplineCore(Player player) {
        if (player == null) return null;
        return DisciplineCore.fromId(getPlayerPersistedData(player).getInt(NBT_DISCIPLINE_CORE));
    }

    public static int getDisciplineSecondaryUnlockMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_DISCIPLINE_SECONDARY_UNLOCKS);
    }

    public static int getStackDisciplineSecondaryUnlockMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DISCIPLINE) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_DISCIPLINE_SECONDARY_UNLOCKS);
    }

    private static void setStackDisciplineSecondaryUnlockMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DISCIPLINE) return;
        stack.getOrCreateTag().putInt(NBT_DISCIPLINE_SECONDARY_UNLOCKS, mask);
    }

    public static boolean isDisciplineSecondarySkillUnlocked(Player player, Skill skill) {
        if (skill == null || !skill.isDisciplineSecondary()) return false;
        return (getDisciplineSecondaryUnlockMask(player) & skill.disciplineSecondaryBit()) != 0;
    }

    public static boolean fuseDisciplineSecondarySkill(
            Player player,
            ItemStack stack,
            Skill skill
    ) {
        if (player == null || fromStack(stack) != Signet.DISCIPLINE
                || skill == null || !skill.isDisciplineSecondary()) return false;
        synchronizeLevels(player, stack, Signet.DISCIPLINE);
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.disciplineSecondaryBit();
        int mask = persisted.getInt(NBT_DISCIPLINE_SECONDARY_UNLOCKS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_DISCIPLINE_SECONDARY_UNLOCKS, mask);
        setStackDisciplineSecondaryUnlockMask(stack, mask);
        synchronizeLevels(player, stack, Signet.DISCIPLINE);
        return true;
    }

    public static DisciplineCore getStackDisciplineCore(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DISCIPLINE) return null;
        CompoundTag tag = stack.getTag();
        return tag == null ? null : DisciplineCore.fromId(tag.getInt(NBT_DISCIPLINE_CORE));
    }

    private static void setStackDisciplineCore(ItemStack stack, DisciplineCore core) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DISCIPLINE) return;
        if (core == null) stack.getOrCreateTag().remove(NBT_DISCIPLINE_CORE);
        else stack.getOrCreateTag().putInt(NBT_DISCIPLINE_CORE, core.id());
    }

    public static int getDisciplineCoreSignetMask(Player player) {
        return player == null ? 0 : getPlayerPersistedData(player).getInt(NBT_DISCIPLINE_CORE_SIGNETS);
    }

    public static int getStackDisciplineCoreSignetMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DISCIPLINE) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_DISCIPLINE_CORE_SIGNETS);
    }

    private static void setStackDisciplineCoreSignetMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DISCIPLINE) return;
        stack.getOrCreateTag().putInt(NBT_DISCIPLINE_CORE_SIGNETS, mask);
    }

    public static boolean hasDisciplineCoreSignet(Player player, Skill skill) {
        if (skill == null || !skill.isDisciplineCoreSignet()) return false;
        return getDisciplineCore(player) == skill.disciplineCore()
                && (getDisciplineCoreSignetMask(player) & skill.disciplineCoreSignetBit()) != 0;
    }

    public static boolean fuseDisciplineCore(Player player, ItemStack stack, DisciplineCore core) {
        if (player == null || fromStack(stack) != Signet.DISCIPLINE || core == null
                || getDisciplineCore(player) != null) return false;
        DisciplineCore selected = getSelectedDisciplineCore(player);
        if (selected != null && selected != core) return false;
        getPlayerPersistedData(player).putInt(NBT_DISCIPLINE_CORE, core.id());
        setStackDisciplineCore(stack, core);
        return true;
    }

    public static boolean fuseDisciplineCoreSignet(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.DISCIPLINE || skill == null
                || !skill.isDisciplineCoreSignet()
                || getDisciplineCore(player) != skill.disciplineCore()) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.disciplineCoreSignetBit();
        int mask = persisted.getInt(NBT_DISCIPLINE_CORE_SIGNETS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_DISCIPLINE_CORE_SIGNETS, mask);
        setStackDisciplineCoreSignetMask(stack, mask);
        synchronizeLevels(player, stack, Signet.DISCIPLINE);
        return true;
    }

    public static HelixCore getHelixCore(Player player) {
        return player == null ? null
                : HelixCore.fromId(getPlayerPersistedData(player).getInt(NBT_HELIX_CORE));
    }

    public static int getHelixSecondaryUnlockMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_HELIX_SECONDARY_UNLOCKS);
    }

    public static int getStackHelixSecondaryUnlockMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.HELIX) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_HELIX_SECONDARY_UNLOCKS);
    }

    private static void setStackHelixSecondaryUnlockMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.HELIX) return;
        stack.getOrCreateTag().putInt(NBT_HELIX_SECONDARY_UNLOCKS, mask);
    }

    public static boolean isHelixSecondarySkillUnlocked(Player player, Skill skill) {
        return skill != null && skill.isHelixSecondary()
                && (getHelixSecondaryUnlockMask(player) & skill.helixSecondaryBit()) != 0;
    }

    public static boolean fuseHelixSecondarySkill(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.HELIX || skill == null
                || !skill.isHelixSecondary()) return false;
        synchronizeLevels(player, stack, Signet.HELIX);
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.helixSecondaryBit();
        int mask = persisted.getInt(NBT_HELIX_SECONDARY_UNLOCKS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_HELIX_SECONDARY_UNLOCKS, mask);
        setStackHelixSecondaryUnlockMask(stack, mask);
        synchronizeLevels(player, stack, Signet.HELIX);
        return true;
    }

    public static HelixCore getStackHelixCore(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.HELIX) return null;
        CompoundTag tag = stack.getTag();
        return tag == null ? null : HelixCore.fromId(tag.getInt(NBT_HELIX_CORE));
    }

    private static void setStackHelixCore(ItemStack stack, HelixCore core) {
        if (stack.isEmpty() || fromStack(stack) != Signet.HELIX) return;
        if (core == null) stack.getOrCreateTag().remove(NBT_HELIX_CORE);
        else stack.getOrCreateTag().putInt(NBT_HELIX_CORE, core.id());
    }

    public static int getHelixCoreSignetMask(Player player) {
        return player == null ? 0 : getPlayerPersistedData(player).getInt(NBT_HELIX_CORE_SIGNETS);
    }

    public static int getStackHelixCoreSignetMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.HELIX) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_HELIX_CORE_SIGNETS);
    }

    private static void setStackHelixCoreSignetMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.HELIX) return;
        stack.getOrCreateTag().putInt(NBT_HELIX_CORE_SIGNETS, mask);
    }

    public static boolean hasHelixCoreSignet(Player player, Skill skill) {
        return skill != null && skill.isHelixCoreSignet()
                && getHelixCore(player) == skill.helixCore()
                && (getHelixCoreSignetMask(player) & skill.helixCoreSignetBit()) != 0;
    }

    public static boolean fuseHelixCore(Player player, ItemStack stack, HelixCore core) {
        if (player == null || fromStack(stack) != Signet.HELIX || core == null
                || getHelixCore(player) != null) return false;
        HelixCore selected = getSelectedHelixCore(player);
        if (selected != null && selected != core) return false;
        getPlayerPersistedData(player).putInt(NBT_HELIX_CORE, core.id());
        setStackHelixCore(stack, core);
        return true;
    }

    public static boolean fuseHelixCoreSignet(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.HELIX || skill == null
                || !skill.isHelixCoreSignet() || getHelixCore(player) != skill.helixCore()) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.helixCoreSignetBit();
        int mask = persisted.getInt(NBT_HELIX_CORE_SIGNETS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_HELIX_CORE_SIGNETS, mask);
        setStackHelixCoreSignetMask(stack, mask);
        synchronizeLevels(player, stack, Signet.HELIX);
        return true;
    }

    public static BodhiCore getBodhiCore(Player player) {
        return player == null ? null
                : BodhiCore.fromId(getPlayerPersistedData(player).getInt(NBT_BODHI_CORE));
    }

    public static int getBodhiSecondaryUnlockMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_BODHI_SECONDARY_UNLOCKS);
    }

    public static int getStackBodhiSecondaryUnlockMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.BODHI) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_BODHI_SECONDARY_UNLOCKS);
    }

    private static void setStackBodhiSecondaryUnlockMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.BODHI) return;
        stack.getOrCreateTag().putInt(NBT_BODHI_SECONDARY_UNLOCKS, mask);
    }

    public static boolean isBodhiSecondarySkillUnlocked(Player player, Skill skill) {
        return skill != null && skill.isBodhiSecondary()
                && (getBodhiSecondaryUnlockMask(player) & skill.bodhiSecondaryBit()) != 0;
    }

    public static boolean fuseBodhiSecondarySkill(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.BODHI || skill == null
                || !skill.isBodhiSecondary()) return false;
        synchronizeLevels(player, stack, Signet.BODHI);
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.bodhiSecondaryBit();
        int mask = persisted.getInt(NBT_BODHI_SECONDARY_UNLOCKS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_BODHI_SECONDARY_UNLOCKS, mask);
        setStackBodhiSecondaryUnlockMask(stack, mask);
        synchronizeLevels(player, stack, Signet.BODHI);
        return true;
    }

    public static BodhiCore getStackBodhiCore(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.BODHI) return null;
        CompoundTag tag = stack.getTag();
        return tag == null ? null : BodhiCore.fromId(tag.getInt(NBT_BODHI_CORE));
    }

    private static void setStackBodhiCore(ItemStack stack, BodhiCore core) {
        if (stack.isEmpty() || fromStack(stack) != Signet.BODHI || core == null) return;
        stack.getOrCreateTag().putInt(NBT_BODHI_CORE, core.id());
    }

    public static int getBodhiCoreSignetMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_BODHI_CORE_SIGNETS);
    }

    public static int getStackBodhiCoreSignetMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.BODHI) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_BODHI_CORE_SIGNETS);
    }

    private static void setStackBodhiCoreSignetMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.BODHI) return;
        stack.getOrCreateTag().putInt(NBT_BODHI_CORE_SIGNETS, mask);
    }

    public static boolean hasBodhiCoreSignet(Player player, Skill skill) {
        return skill != null && skill.isBodhiCoreSignet()
                && getBodhiCore(player) == skill.bodhiCore()
                && (getBodhiCoreSignetMask(player) & skill.bodhiCoreSignetBit()) != 0;
    }

    public static boolean fuseBodhiCore(Player player, ItemStack stack, BodhiCore core) {
        if (player == null || fromStack(stack) != Signet.BODHI || core == null
                || getBodhiCore(player) != null) return false;
        BodhiCore selected = getSelectedBodhiCore(player);
        if (selected != null && selected != core) return false;
        getPlayerPersistedData(player).putInt(NBT_BODHI_CORE, core.id());
        setStackBodhiCore(stack, core);
        return true;
    }

    public static boolean fuseBodhiCoreSignet(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.BODHI || skill == null
                || !skill.isBodhiCoreSignet() || getBodhiCore(player) != skill.bodhiCore()) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.bodhiCoreSignetBit();
        int mask = persisted.getInt(NBT_BODHI_CORE_SIGNETS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_BODHI_CORE_SIGNETS, mask);
        setStackBodhiCoreSignetMask(stack, mask);
        synchronizeLevels(player, stack, Signet.BODHI);
        return true;
    }

    public static DaybreakCore getDaybreakCore(Player player) {
        return player == null ? null
                : DaybreakCore.fromId(getPlayerPersistedData(player).getInt(NBT_DAYBREAK_CORE));
    }

    public static int getDaybreakSecondaryUnlockMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_DAYBREAK_SECONDARY_UNLOCKS);
    }

    public static int getStackDaybreakSecondaryUnlockMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DAYBREAK) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_DAYBREAK_SECONDARY_UNLOCKS);
    }

    private static void setStackDaybreakSecondaryUnlockMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DAYBREAK) return;
        stack.getOrCreateTag().putInt(NBT_DAYBREAK_SECONDARY_UNLOCKS, mask);
    }

    public static boolean isDaybreakSecondarySkillUnlocked(Player player, Skill skill) {
        return skill != null && skill.isDaybreakSecondary()
                && (getDaybreakSecondaryUnlockMask(player) & skill.daybreakSecondaryBit()) != 0;
    }

    public static boolean fuseDaybreakSecondarySkill(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.DAYBREAK || skill == null
                || !skill.isDaybreakSecondary()) return false;
        synchronizeLevels(player, stack, Signet.DAYBREAK);
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.daybreakSecondaryBit();
        int mask = persisted.getInt(NBT_DAYBREAK_SECONDARY_UNLOCKS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_DAYBREAK_SECONDARY_UNLOCKS, mask);
        setStackDaybreakSecondaryUnlockMask(stack, mask);
        synchronizeLevels(player, stack, Signet.DAYBREAK);
        return true;
    }

    public static DaybreakCore getStackDaybreakCore(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DAYBREAK) return null;
        CompoundTag tag = stack.getTag();
        return tag == null ? null : DaybreakCore.fromId(tag.getInt(NBT_DAYBREAK_CORE));
    }

    private static void setStackDaybreakCore(ItemStack stack, DaybreakCore core) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DAYBREAK || core == null) return;
        stack.getOrCreateTag().putInt(NBT_DAYBREAK_CORE, core.id());
    }

    public static int getDaybreakCoreSignetMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_DAYBREAK_CORE_SIGNETS);
    }

    public static int getStackDaybreakCoreSignetMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DAYBREAK) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_DAYBREAK_CORE_SIGNETS);
    }

    private static void setStackDaybreakCoreSignetMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DAYBREAK) return;
        stack.getOrCreateTag().putInt(NBT_DAYBREAK_CORE_SIGNETS, mask);
    }

    public static boolean hasDaybreakCoreSignet(Player player, Skill skill) {
        return skill != null && skill.isDaybreakCoreSignet()
                && getDaybreakCore(player) == skill.daybreakCore()
                && (getDaybreakCoreSignetMask(player) & skill.daybreakCoreSignetBit()) != 0;
    }

    public static boolean fuseDaybreakCore(Player player, ItemStack stack, DaybreakCore core) {
        if (player == null || fromStack(stack) != Signet.DAYBREAK || core == null
                || getDaybreakCore(player) != null) return false;
        DaybreakCore selected = getSelectedDaybreakCore(player);
        if (selected != null && selected != core) return false;
        getPlayerPersistedData(player).putInt(NBT_DAYBREAK_CORE, core.id());
        setStackDaybreakCore(stack, core);
        return true;
    }

    public static boolean fuseDaybreakCoreSignet(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.DAYBREAK || skill == null
                || !skill.isDaybreakCoreSignet()
                || getDaybreakCore(player) != skill.daybreakCore()) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.daybreakCoreSignetBit();
        int mask = persisted.getInt(NBT_DAYBREAK_CORE_SIGNETS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_DAYBREAK_CORE_SIGNETS, mask);
        setStackDaybreakCoreSignetMask(stack, mask);
        synchronizeLevels(player, stack, Signet.DAYBREAK);
        return true;
    }

    private static String extensionKey(Signet signet, String suffix) {
        return "LSEExtension" + signet.name() + suffix;
    }

    public static ExtensionCore getExtensionCore(Player player, Signet signet) {
        if (player == null || signet == null) return null;
        return ExtensionCore.fromId(signet,
                getPlayerPersistedData(player).getInt(extensionKey(signet, "Core")));
    }

    public static ExtensionCore getStackExtensionCore(ItemStack stack) {
        Signet signet = fromStack(stack);
        if (signet == null || stack.getTag() == null) return null;
        return ExtensionCore.fromId(signet,
                stack.getTag().getInt(extensionKey(signet, "Core")));
    }

    public static int getExtensionSecondaryUnlockMask(Player player, Signet signet) {
        return player == null || signet == null ? 0
                : getPlayerPersistedData(player).getInt(extensionKey(signet, "SecondaryUnlocks"));
    }

    public static int getStackExtensionSecondaryUnlockMask(ItemStack stack) {
        Signet signet = fromStack(stack);
        return signet == null || stack.getTag() == null ? 0
                : stack.getTag().getInt(extensionKey(signet, "SecondaryUnlocks"));
    }

    public static int getExtensionCoreSignetMask(Player player, Signet signet) {
        return player == null || signet == null ? 0
                : getPlayerPersistedData(player).getInt(extensionKey(signet, "CoreSignets"));
    }

    public static int getStackExtensionCoreSignetMask(ItemStack stack) {
        Signet signet = fromStack(stack);
        return signet == null || stack.getTag() == null ? 0
                : stack.getTag().getInt(extensionKey(signet, "CoreSignets"));
    }

    public static boolean isExtensionSecondarySkillUnlocked(Player player, Skill skill) {
        return skill != null && skill.isExtensionSecondary()
                && (getExtensionSecondaryUnlockMask(player, skill.signet())
                & skill.extensionSecondaryBit()) != 0;
    }

    public static boolean hasExtensionCoreSignet(Player player, Skill skill) {
        return skill != null && skill.isExtensionCoreSignet()
                && getExtensionCore(player, skill.signet()) == skill.extensionCore()
                && (getExtensionCoreSignetMask(player, skill.signet())
                & skill.extensionCoreSignetBit()) != 0;
    }

    public static boolean fuseExtensionSecondarySkill(Player player, ItemStack stack, Skill skill) {
        Signet signet = fromStack(stack);
        if (player == null || signet == null || skill == null
                || skill.signet() != signet || !skill.isExtensionSecondary()) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        String key = extensionKey(signet, "SecondaryUnlocks");
        int mask = persisted.getInt(key);
        int bit = skill.extensionSecondaryBit();
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(key, mask);
        stack.getOrCreateTag().putInt(key, mask);
        synchronizeLevels(player, stack, signet);
        return true;
    }

    public static boolean fuseExtensionCore(Player player, ItemStack stack, ExtensionCore core) {
        Signet signet = fromStack(stack);
        if (player == null || signet == null || core == null || core.signet() != signet
                || getExtensionCore(player, signet) != null) return false;
        ExtensionCore selected = getSelectedExtensionCore(player, signet);
        if (selected != null && selected != core) return false;
        String key = extensionKey(signet, "Core");
        getPlayerPersistedData(player).putInt(key, core.id());
        stack.getOrCreateTag().putInt(key, core.id());
        return true;
    }

    public static boolean fuseExtensionCoreSignet(Player player, ItemStack stack, Skill skill) {
        Signet signet = fromStack(stack);
        if (player == null || signet == null || skill == null || skill.signet() != signet
                || !skill.isExtensionCoreSignet()
                || getExtensionCore(player, signet) != skill.extensionCore()) return false;
        String key = extensionKey(signet, "CoreSignets");
        CompoundTag persisted = getPlayerPersistedData(player);
        int mask = persisted.getInt(key);
        int bit = skill.extensionCoreSignetBit();
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(key, mask);
        stack.getOrCreateTag().putInt(key, mask);
        synchronizeLevels(player, stack, signet);
        return true;
    }

    public static ExtensionCore getPurchasedExtensionCore(Player player, Signet signet) {
        if (player == null || signet == null) return null;
        return ExtensionCore.fromId(signet,
                getPlayerPersistedData(player).getInt(extensionKey(signet, "CorePurchased")));
    }

    public static ExtensionCore getSelectedExtensionCore(Player player, Signet signet) {
        if (player == null || signet == null) return null;
        ExtensionCore purchased = getPurchasedExtensionCore(player, signet);
        if (purchased != null) return purchased;
        ExtensionCore fused = getExtensionCore(player, signet);
        if (fused != null) return fused;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ExtensionCore held = extensionCoreFromItem(inventory.getItem(i));
            if (held != null && held.signet() == signet
                    && isCurrentCoreSystemItem(player, inventory.getItem(i), signet)) return held;
        }
        return null;
    }

    public static boolean hasPurchasedExtensionCoreSignet(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isExtensionCoreSignet()) return false;
        return (getPlayerPersistedData(player).getInt(
                extensionKey(skill.signet(), "CoreSignetsPurchased"))
                & skill.extensionCoreSignetBit()) != 0;
    }

    public static void recordExtensionCorePurchase(Player player, ExtensionCore core) {
        if (player == null || core == null) return;
        getPlayerPersistedData(player).putInt(
                extensionKey(core.signet(), "CorePurchased"), core.id());
    }

    public static void recordExtensionCoreSignetPurchase(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isExtensionCoreSignet()) return;
        CompoundTag data = getPlayerPersistedData(player);
        String key = extensionKey(skill.signet(), "CoreSignetsPurchased");
        data.putInt(key, data.getInt(key) | skill.extensionCoreSignetBit());
    }

    public static SetsuraCore getSetsuraCore(Player player) {
        return player == null ? null
                : SetsuraCore.fromId(getPlayerPersistedData(player).getInt(NBT_SETSURA_CORE));
    }

    public static int getSetsuraSecondaryUnlockMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_SETSURA_SECONDARY_UNLOCKS);
    }

    public static int getStackSetsuraSecondaryUnlockMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.SETSURA) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_SETSURA_SECONDARY_UNLOCKS);
    }

    private static void setStackSetsuraSecondaryUnlockMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.SETSURA) return;
        stack.getOrCreateTag().putInt(NBT_SETSURA_SECONDARY_UNLOCKS, mask);
    }

    public static boolean isSetsuraSecondarySkillUnlocked(Player player, Skill skill) {
        return skill != null && skill.isSetsuraSecondary()
                && (getSetsuraSecondaryUnlockMask(player) & skill.setsuraSecondaryBit()) != 0;
    }

    public static boolean fuseSetsuraSecondarySkill(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.SETSURA || skill == null
                || !skill.isSetsuraSecondary()) return false;
        synchronizeLevels(player, stack, Signet.SETSURA);
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.setsuraSecondaryBit();
        int mask = persisted.getInt(NBT_SETSURA_SECONDARY_UNLOCKS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_SETSURA_SECONDARY_UNLOCKS, mask);
        setStackSetsuraSecondaryUnlockMask(stack, mask);
        synchronizeLevels(player, stack, Signet.SETSURA);
        return true;
    }

    public static SetsuraCore getStackSetsuraCore(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.SETSURA) return null;
        CompoundTag tag = stack.getTag();
        return tag == null ? null : SetsuraCore.fromId(tag.getInt(NBT_SETSURA_CORE));
    }

    private static void setStackSetsuraCore(ItemStack stack, SetsuraCore core) {
        if (stack.isEmpty() || fromStack(stack) != Signet.SETSURA || core == null) return;
        stack.getOrCreateTag().putInt(NBT_SETSURA_CORE, core.id());
    }

    public static int getSetsuraCoreSignetMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_SETSURA_CORE_SIGNETS);
    }

    public static int getStackSetsuraCoreSignetMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.SETSURA) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_SETSURA_CORE_SIGNETS);
    }

    private static void setStackSetsuraCoreSignetMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.SETSURA) return;
        stack.getOrCreateTag().putInt(NBT_SETSURA_CORE_SIGNETS, mask);
    }

    public static boolean hasSetsuraCoreSignet(Player player, Skill skill) {
        return skill != null && skill.isSetsuraCoreSignet()
                && getSetsuraCore(player) == skill.setsuraCore()
                && (getSetsuraCoreSignetMask(player) & skill.setsuraCoreSignetBit()) != 0;
    }

    public static boolean fuseSetsuraCore(Player player, ItemStack stack, SetsuraCore core) {
        if (player == null || fromStack(stack) != Signet.SETSURA || core == null
                || getSetsuraCore(player) != null) return false;
        SetsuraCore selected = getSelectedSetsuraCore(player);
        if (selected != null && selected != core) return false;
        getPlayerPersistedData(player).putInt(NBT_SETSURA_CORE, core.id());
        setStackSetsuraCore(stack, core);
        return true;
    }

    public static boolean fuseSetsuraCoreSignet(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.SETSURA || skill == null
                || !skill.isSetsuraCoreSignet() || getSetsuraCore(player) != skill.setsuraCore()) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.setsuraCoreSignetBit();
        int mask = persisted.getInt(NBT_SETSURA_CORE_SIGNETS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_SETSURA_CORE_SIGNETS, mask);
        setStackSetsuraCoreSignetMask(stack, mask);
        synchronizeLevels(player, stack, Signet.SETSURA);
        return true;
    }

    public static DecimationCore getDecimationCore(Player player) {
        return player == null ? null
                : DecimationCore.fromId(getPlayerPersistedData(player).getInt(NBT_DECIMATION_CORE));
    }

    public static int getDecimationSecondaryUnlockMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_DECIMATION_SECONDARY_UNLOCKS);
    }

    public static int getStackDecimationSecondaryUnlockMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DECIMATION) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_DECIMATION_SECONDARY_UNLOCKS);
    }

    private static void setStackDecimationSecondaryUnlockMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DECIMATION) return;
        stack.getOrCreateTag().putInt(NBT_DECIMATION_SECONDARY_UNLOCKS, mask);
    }

    public static boolean isDecimationSecondarySkillUnlocked(Player player, Skill skill) {
        return skill != null && skill.isDecimationSecondary()
                && (getDecimationSecondaryUnlockMask(player) & skill.decimationSecondaryBit()) != 0;
    }

    public static boolean fuseDecimationSecondarySkill(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.DECIMATION || skill == null
                || !skill.isDecimationSecondary()) return false;
        synchronizeLevels(player, stack, Signet.DECIMATION);
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.decimationSecondaryBit();
        int mask = persisted.getInt(NBT_DECIMATION_SECONDARY_UNLOCKS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_DECIMATION_SECONDARY_UNLOCKS, mask);
        setStackDecimationSecondaryUnlockMask(stack, mask);
        synchronizeLevels(player, stack, Signet.DECIMATION);
        return true;
    }

    public static DecimationCore getStackDecimationCore(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DECIMATION) return null;
        CompoundTag tag = stack.getTag();
        return tag == null ? null : DecimationCore.fromId(tag.getInt(NBT_DECIMATION_CORE));
    }

    private static void setStackDecimationCore(ItemStack stack, DecimationCore core) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DECIMATION) return;
        if (core == null) stack.getOrCreateTag().remove(NBT_DECIMATION_CORE);
        else stack.getOrCreateTag().putInt(NBT_DECIMATION_CORE, core.id());
    }

    public static int getDecimationCoreSignetMask(Player player) {
        return player == null ? 0 : getPlayerPersistedData(player).getInt(NBT_DECIMATION_CORE_SIGNETS);
    }

    public static int getStackDecimationCoreSignetMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DECIMATION) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_DECIMATION_CORE_SIGNETS);
    }

    private static void setStackDecimationCoreSignetMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.DECIMATION) return;
        stack.getOrCreateTag().putInt(NBT_DECIMATION_CORE_SIGNETS, mask);
    }

    public static boolean hasDecimationCoreSignet(Player player, Skill skill) {
        return skill != null && skill.isDecimationCoreSignet()
                && getDecimationCore(player) == skill.decimationCore()
                && (getDecimationCoreSignetMask(player) & skill.decimationCoreSignetBit()) != 0;
    }

    public static boolean fuseDecimationCore(Player player, ItemStack stack, DecimationCore core) {
        if (player == null || fromStack(stack) != Signet.DECIMATION || core == null
                || getDecimationCore(player) != null) return false;
        DecimationCore selected = getSelectedDecimationCore(player);
        if (selected != null && selected != core) return false;
        getPlayerPersistedData(player).putInt(NBT_DECIMATION_CORE, core.id());
        setStackDecimationCore(stack, core);
        return true;
    }

    public static boolean fuseDecimationCoreSignet(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.DECIMATION || skill == null
                || !skill.isDecimationCoreSignet()
                || getDecimationCore(player) != skill.decimationCore()) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.decimationCoreSignetBit();
        int mask = persisted.getInt(NBT_DECIMATION_CORE_SIGNETS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_DECIMATION_CORE_SIGNETS, mask);
        setStackDecimationCoreSignetMask(stack, mask);
        synchronizeLevels(player, stack, Signet.DECIMATION);
        return true;
    }

    public static GoldCore getGoldCore(Player player) {
        return player == null ? null
                : GoldCore.fromId(getPlayerPersistedData(player).getInt(NBT_GOLD_CORE));
    }

    public static int getGoldSecondaryUnlockMask(Player player) {
        return player == null ? 0
                : getPlayerPersistedData(player).getInt(NBT_GOLD_SECONDARY_UNLOCKS);
    }

    public static int getStackGoldSecondaryUnlockMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.GOLD) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_GOLD_SECONDARY_UNLOCKS);
    }

    private static void setStackGoldSecondaryUnlockMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.GOLD) return;
        stack.getOrCreateTag().putInt(NBT_GOLD_SECONDARY_UNLOCKS, mask);
    }

    public static boolean isGoldSecondarySkillUnlocked(Player player, Skill skill) {
        return skill != null && skill.isGoldSecondary()
                && (getGoldSecondaryUnlockMask(player) & skill.goldSecondaryBit()) != 0;
    }

    public static boolean fuseGoldSecondarySkill(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.GOLD || skill == null
                || !skill.isGoldSecondary()) return false;
        synchronizeLevels(player, stack, Signet.GOLD);
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.goldSecondaryBit();
        int mask = persisted.getInt(NBT_GOLD_SECONDARY_UNLOCKS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_GOLD_SECONDARY_UNLOCKS, mask);
        setStackGoldSecondaryUnlockMask(stack, mask);
        synchronizeLevels(player, stack, Signet.GOLD);
        return true;
    }

    public static GoldCore getStackGoldCore(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.GOLD) return null;
        CompoundTag tag = stack.getTag();
        return tag == null ? null : GoldCore.fromId(tag.getInt(NBT_GOLD_CORE));
    }

    private static void setStackGoldCore(ItemStack stack, GoldCore core) {
        if (stack.isEmpty() || fromStack(stack) != Signet.GOLD) return;
        if (core == null) stack.getOrCreateTag().remove(NBT_GOLD_CORE);
        else stack.getOrCreateTag().putInt(NBT_GOLD_CORE, core.id());
    }

    public static int getGoldCoreSignetMask(Player player) {
        return player == null ? 0 : getPlayerPersistedData(player).getInt(NBT_GOLD_CORE_SIGNETS);
    }

    public static int getStackGoldCoreSignetMask(ItemStack stack) {
        if (stack.isEmpty() || fromStack(stack) != Signet.GOLD) return 0;
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(NBT_GOLD_CORE_SIGNETS);
    }

    private static void setStackGoldCoreSignetMask(ItemStack stack, int mask) {
        if (stack.isEmpty() || fromStack(stack) != Signet.GOLD) return;
        stack.getOrCreateTag().putInt(NBT_GOLD_CORE_SIGNETS, mask);
    }

    public static boolean hasGoldCoreSignet(Player player, Skill skill) {
        return skill != null && skill.isGoldCoreSignet()
                && getGoldCore(player) == skill.goldCore()
                && (getGoldCoreSignetMask(player) & skill.goldCoreSignetBit()) != 0;
    }

    public static boolean fuseGoldCore(Player player, ItemStack stack, GoldCore core) {
        if (player == null || fromStack(stack) != Signet.GOLD || core == null
                || getGoldCore(player) != null) return false;
        GoldCore selected = getSelectedGoldCore(player);
        if (selected != null && selected != core) return false;
        getPlayerPersistedData(player).putInt(NBT_GOLD_CORE, core.id());
        setStackGoldCore(stack, core);
        return true;
    }

    public static boolean fuseGoldCoreSignet(Player player, ItemStack stack, Skill skill) {
        if (player == null || fromStack(stack) != Signet.GOLD || skill == null
                || !skill.isGoldCoreSignet() || getGoldCore(player) != skill.goldCore()) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        int bit = skill.goldCoreSignetBit();
        int mask = persisted.getInt(NBT_GOLD_CORE_SIGNETS);
        if ((mask & bit) != 0) return false;
        mask |= bit;
        persisted.putInt(NBT_GOLD_CORE_SIGNETS, mask);
        setStackGoldCoreSignetMask(stack, mask);
        synchronizeLevels(player, stack, Signet.GOLD);
        return true;
    }

    public static List<Skill> workshopSkills(Player player, Signet signet) {
        if (signet == Signet.INFINITY || signet == Signet.STARS
                || signet == Signet.VICISSITUDE || signet == Signet.REVERIE) {
            ExtensionCore core = getExtensionCore(player, signet);
            int mask = getExtensionCoreSignetMask(player, signet);
            int secondaryMask = getExtensionSecondaryUnlockMask(player, signet);
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isExtensionSecondary()
                            || (secondaryMask & skill.extensionSecondaryBit()) != 0)
                            && (!skill.isExtensionCoreSignet()
                            || core == skill.extensionCore()
                            && (mask & skill.extensionCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.DAYBREAK) {
            DaybreakCore core = getDaybreakCore(player);
            int mask = getDaybreakCoreSignetMask(player);
            int secondaryMask = getDaybreakSecondaryUnlockMask(player);
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isDaybreakSecondary()
                            || (secondaryMask & skill.daybreakSecondaryBit()) != 0)
                            && (!skill.isDaybreakCoreSignet()
                            || core == skill.daybreakCore()
                            && (mask & skill.daybreakCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.SETSURA) {
            SetsuraCore core = getSetsuraCore(player);
            int mask = getSetsuraCoreSignetMask(player);
            int secondaryMask = getSetsuraSecondaryUnlockMask(player);
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isSetsuraSecondary()
                            || (secondaryMask & skill.setsuraSecondaryBit()) != 0)
                            && (!skill.isSetsuraCoreSignet()
                            || core == skill.setsuraCore()
                            && (mask & skill.setsuraCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.BODHI) {
            BodhiCore core = getBodhiCore(player);
            int mask = getBodhiCoreSignetMask(player);
            int secondaryMask = getBodhiSecondaryUnlockMask(player);
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isBodhiSecondary()
                            || (secondaryMask & skill.bodhiSecondaryBit()) != 0)
                            && (!skill.isBodhiCoreSignet()
                            || core == skill.bodhiCore()
                            && (mask & skill.bodhiCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.DECIMATION) {
            DecimationCore core = getDecimationCore(player);
            int mask = getDecimationCoreSignetMask(player);
            int secondaryMask = getDecimationSecondaryUnlockMask(player);
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isDecimationSecondary()
                            || (secondaryMask & skill.decimationSecondaryBit()) != 0)
                            && (!skill.isDecimationCoreSignet()
                            || core == skill.decimationCore()
                            && (mask & skill.decimationCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.HELIX) {
            HelixCore core = getHelixCore(player);
            int mask = getHelixCoreSignetMask(player);
            int secondaryMask = getHelixSecondaryUnlockMask(player);
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isHelixSecondary()
                            || (secondaryMask & skill.helixSecondaryBit()) != 0)
                            && (!skill.isHelixCoreSignet()
                            || core == skill.helixCore()
                            && (mask & skill.helixCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.GOLD) {
            GoldCore core = getGoldCore(player);
            int mask = getGoldCoreSignetMask(player);
            int secondaryMask = getGoldSecondaryUnlockMask(player);
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isGoldSecondary()
                            || (secondaryMask & skill.goldSecondaryBit()) != 0)
                            && (!skill.isGoldCoreSignet()
                            || core == skill.goldCore()
                            && (mask & skill.goldCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.DISCIPLINE) {
            DisciplineCore core = getDisciplineCore(player);
            int mask = getDisciplineCoreSignetMask(player);
            int secondaryMask = getDisciplineSecondaryUnlockMask(player);
            return allSkillsFor(signet).stream()
                    .filter(skill -> {
                        if (skill.isDisciplineSecondary()) {
                            return (secondaryMask & skill.disciplineSecondaryBit()) != 0;
                        }
                        return !skill.isDisciplineCoreSignet()
                                || core == skill.disciplineCore()
                                && (mask & skill.disciplineCoreSignetBit()) != 0;
                    })
                    .toList();
        }
        return workshopSkills(
                signet,
                getDeliveranceSecondaryUnlockMask(player),
                getDeliveranceCore(player),
                getDeliveranceCoreSignetMask(player)
        );
    }

    public static List<Skill> workshopSkills(Signet signet, int secondaryUnlockMask) {
        return workshopSkills(signet, secondaryUnlockMask, null, 0);
    }

    public static List<Skill> workshopSkills(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask
    ) {
        if (signet == null) return List.of();
        return allSkillsFor(signet).stream()
                .filter(skill -> {
                    if (skill.isDeliveranceSecondary()) {
                        return (secondaryUnlockMask & skill.deliveranceSecondaryBit()) != 0;
                    }
                    if (skill.isDeliveranceCoreSignet()) {
                        return core == skill.deliveranceCore()
                                && (coreSignetMask & skill.deliveranceCoreSignetBit()) != 0;
                    }
                    if (skill.isDisciplineCoreSignet()) return false;
                    if (skill.isGoldCoreSignet()) return false;
                    if (skill.isHelixCoreSignet()) return false;
                    if (skill.isDecimationCoreSignet()) return false;
                    if (skill.isBodhiCoreSignet()) return false;
                    if (skill.isSetsuraCoreSignet()) return false;
                    if (skill.isDaybreakCoreSignet()) return false;
                    if (skill.isExtensionSecondary() || skill.isExtensionCoreSignet()) return false;
                    return true;
                })
                .toList();
    }

    public static List<Skill> workshopSkills(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask
    ) {
        return workshopSkills(signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                null, 0, 0);
    }

    public static List<Skill> workshopSkills(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask
    ) {
        return workshopSkills(signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                null, 0, 0, null, 0, 0);
    }

    public static List<Skill> workshopSkills(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask
    ) {
        if (signet == Signet.DECIMATION) {
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isDecimationSecondary()
                            || (decimationSecondaryUnlockMask & skill.decimationSecondaryBit()) != 0)
                            && (!skill.isDecimationCoreSignet()
                            || decimationCore == skill.decimationCore()
                            && (decimationCoreSignetMask & skill.decimationCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.HELIX) {
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isHelixSecondary()
                            || (helixSecondaryUnlockMask & skill.helixSecondaryBit()) != 0)
                            && (!skill.isHelixCoreSignet()
                            || helixCore == skill.helixCore()
                            && (helixCoreSignetMask & skill.helixCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.GOLD) {
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isGoldSecondary()
                            || (goldSecondaryUnlockMask & skill.goldSecondaryBit()) != 0)
                            && (!skill.isGoldCoreSignet()
                            || goldCore == skill.goldCore()
                            && (goldCoreSignetMask & skill.goldCoreSignetBit()) != 0))
                    .toList();
        }
        if (signet == Signet.DISCIPLINE) {
            return allSkillsFor(signet).stream()
                    .filter(skill -> {
                        if (skill.isDisciplineSecondary()) {
                            return (disciplineSecondaryUnlockMask
                                    & skill.disciplineSecondaryBit()) != 0;
                        }
                        return !skill.isDisciplineCoreSignet()
                                || disciplineCore == skill.disciplineCore()
                                && (disciplineCoreSignetMask
                                & skill.disciplineCoreSignetBit()) != 0;
                    })
                    .toList();
        }
        return workshopSkills(signet, secondaryUnlockMask, core, coreSignetMask);
    }

    public static List<Skill> workshopSkillsWithBodhi(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask,
            BodhiCore bodhiCore,
            int bodhiCoreSignetMask,
            int bodhiSecondaryUnlockMask
    ) {
        if (signet == Signet.BODHI) {
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isBodhiSecondary()
                            || (bodhiSecondaryUnlockMask & skill.bodhiSecondaryBit()) != 0)
                            && (!skill.isBodhiCoreSignet()
                            || bodhiCore == skill.bodhiCore()
                            && (bodhiCoreSignetMask & skill.bodhiCoreSignetBit()) != 0))
                    .toList();
        }
        return workshopSkills(
                signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                helixCore, helixCoreSignetMask, helixSecondaryUnlockMask,
                decimationCore, decimationCoreSignetMask, decimationSecondaryUnlockMask
        );
    }

    public static Skill skillAtWithBodhi(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask,
            BodhiCore bodhiCore,
            int bodhiCoreSignetMask,
            int bodhiSecondaryUnlockMask,
            int index
    ) {
        List<Skill> skills = workshopSkillsWithBodhi(
                signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                helixCore, helixCoreSignetMask, helixSecondaryUnlockMask,
                decimationCore, decimationCoreSignetMask, decimationSecondaryUnlockMask,
                bodhiCore, bodhiCoreSignetMask, bodhiSecondaryUnlockMask
        );
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public static List<Skill> workshopSkillsWithSetsura(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask,
            BodhiCore bodhiCore,
            int bodhiCoreSignetMask,
            int bodhiSecondaryUnlockMask,
            SetsuraCore setsuraCore,
            int setsuraCoreSignetMask,
            int setsuraSecondaryUnlockMask
    ) {
        if (signet == Signet.SETSURA) {
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isSetsuraSecondary()
                            || (setsuraSecondaryUnlockMask & skill.setsuraSecondaryBit()) != 0)
                            && (!skill.isSetsuraCoreSignet()
                            || setsuraCore == skill.setsuraCore()
                            && (setsuraCoreSignetMask & skill.setsuraCoreSignetBit()) != 0))
                    .toList();
        }
        return workshopSkillsWithBodhi(
                signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                helixCore, helixCoreSignetMask, helixSecondaryUnlockMask,
                decimationCore, decimationCoreSignetMask, decimationSecondaryUnlockMask,
                bodhiCore, bodhiCoreSignetMask, bodhiSecondaryUnlockMask
        );
    }

    public static Skill skillAtWithSetsura(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask,
            BodhiCore bodhiCore,
            int bodhiCoreSignetMask,
            int bodhiSecondaryUnlockMask,
            SetsuraCore setsuraCore,
            int setsuraCoreSignetMask,
            int setsuraSecondaryUnlockMask,
            int index
    ) {
        List<Skill> skills = workshopSkillsWithSetsura(
                signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                helixCore, helixCoreSignetMask, helixSecondaryUnlockMask,
                decimationCore, decimationCoreSignetMask, decimationSecondaryUnlockMask,
                bodhiCore, bodhiCoreSignetMask, bodhiSecondaryUnlockMask,
                setsuraCore, setsuraCoreSignetMask, setsuraSecondaryUnlockMask
        );
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public static List<Skill> workshopSkillsWithDaybreak(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask,
            BodhiCore bodhiCore,
            int bodhiCoreSignetMask,
            int bodhiSecondaryUnlockMask,
            SetsuraCore setsuraCore,
            int setsuraCoreSignetMask,
            int setsuraSecondaryUnlockMask,
            DaybreakCore daybreakCore,
            int daybreakCoreSignetMask,
            int daybreakSecondaryUnlockMask
    ) {
        if (signet == Signet.DAYBREAK) {
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isDaybreakSecondary()
                            || (daybreakSecondaryUnlockMask & skill.daybreakSecondaryBit()) != 0)
                            && (!skill.isDaybreakCoreSignet()
                            || daybreakCore == skill.daybreakCore()
                            && (daybreakCoreSignetMask & skill.daybreakCoreSignetBit()) != 0))
                    .toList();
        }
        return workshopSkillsWithSetsura(
                signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                helixCore, helixCoreSignetMask, helixSecondaryUnlockMask,
                decimationCore, decimationCoreSignetMask, decimationSecondaryUnlockMask,
                bodhiCore, bodhiCoreSignetMask, bodhiSecondaryUnlockMask,
                setsuraCore, setsuraCoreSignetMask, setsuraSecondaryUnlockMask
        );
    }

    public static Skill skillAtWithDaybreak(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask,
            BodhiCore bodhiCore,
            int bodhiCoreSignetMask,
            int bodhiSecondaryUnlockMask,
            SetsuraCore setsuraCore,
            int setsuraCoreSignetMask,
            int setsuraSecondaryUnlockMask,
            DaybreakCore daybreakCore,
            int daybreakCoreSignetMask,
            int daybreakSecondaryUnlockMask,
            int index
    ) {
        List<Skill> skills = workshopSkillsWithDaybreak(
                signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                helixCore, helixCoreSignetMask, helixSecondaryUnlockMask,
                decimationCore, decimationCoreSignetMask, decimationSecondaryUnlockMask,
                bodhiCore, bodhiCoreSignetMask, bodhiSecondaryUnlockMask,
                setsuraCore, setsuraCoreSignetMask, setsuraSecondaryUnlockMask,
                daybreakCore, daybreakCoreSignetMask, daybreakSecondaryUnlockMask
        );
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public static List<Skill> workshopSkillsWithExtension(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask,
            BodhiCore bodhiCore,
            int bodhiCoreSignetMask,
            int bodhiSecondaryUnlockMask,
            SetsuraCore setsuraCore,
            int setsuraCoreSignetMask,
            int setsuraSecondaryUnlockMask,
            DaybreakCore daybreakCore,
            int daybreakCoreSignetMask,
            int daybreakSecondaryUnlockMask,
            ExtensionCore extensionCore,
            int extensionCoreSignetMask,
            int extensionSecondaryUnlockMask
    ) {
        if (signet == Signet.INFINITY || signet == Signet.STARS
                || signet == Signet.VICISSITUDE || signet == Signet.REVERIE) {
            return allSkillsFor(signet).stream()
                    .filter(skill -> (!skill.isExtensionSecondary()
                            || (extensionSecondaryUnlockMask & skill.extensionSecondaryBit()) != 0)
                            && (!skill.isExtensionCoreSignet()
                            || extensionCore == skill.extensionCore()
                            && (extensionCoreSignetMask & skill.extensionCoreSignetBit()) != 0))
                    .toList();
        }
        return workshopSkillsWithDaybreak(
                signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                helixCore, helixCoreSignetMask, helixSecondaryUnlockMask,
                decimationCore, decimationCoreSignetMask, decimationSecondaryUnlockMask,
                bodhiCore, bodhiCoreSignetMask, bodhiSecondaryUnlockMask,
                setsuraCore, setsuraCoreSignetMask, setsuraSecondaryUnlockMask,
                daybreakCore, daybreakCoreSignetMask, daybreakSecondaryUnlockMask);
    }

    public static Skill skillAtWithExtension(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask,
            BodhiCore bodhiCore,
            int bodhiCoreSignetMask,
            int bodhiSecondaryUnlockMask,
            SetsuraCore setsuraCore,
            int setsuraCoreSignetMask,
            int setsuraSecondaryUnlockMask,
            DaybreakCore daybreakCore,
            int daybreakCoreSignetMask,
            int daybreakSecondaryUnlockMask,
            ExtensionCore extensionCore,
            int extensionCoreSignetMask,
            int extensionSecondaryUnlockMask,
            int index
    ) {
        List<Skill> skills = workshopSkillsWithExtension(
                signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                helixCore, helixCoreSignetMask, helixSecondaryUnlockMask,
                decimationCore, decimationCoreSignetMask, decimationSecondaryUnlockMask,
                bodhiCore, bodhiCoreSignetMask, bodhiSecondaryUnlockMask,
                setsuraCore, setsuraCoreSignetMask, setsuraSecondaryUnlockMask,
                daybreakCore, daybreakCoreSignetMask, daybreakSecondaryUnlockMask,
                extensionCore, extensionCoreSignetMask, extensionSecondaryUnlockMask);
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public static Skill skillAt(Signet signet, int index) {
        List<Skill> skills = skillsFor(signet);
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public static Skill skillAt(Player player, Signet signet, int index) {
        List<Skill> skills = workshopSkills(player, signet);
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public static Skill skillAt(Signet signet, int secondaryUnlockMask, int index) {
        List<Skill> skills = workshopSkills(signet, secondaryUnlockMask);
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public static Skill skillAt(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            int index
    ) {
        List<Skill> skills = workshopSkills(signet, secondaryUnlockMask, core, coreSignetMask);
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public static Skill skillAt(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            int index
    ) {
        return skillAt(signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                null, 0, 0, null, 0, 0, index);
    }

    public static Skill skillAt(
            Signet signet,
            int secondaryUnlockMask,
            DeliveranceCore core,
            int coreSignetMask,
            DisciplineCore disciplineCore,
            int disciplineCoreSignetMask,
            int disciplineSecondaryUnlockMask,
            GoldCore goldCore,
            int goldCoreSignetMask,
            int goldSecondaryUnlockMask,
            HelixCore helixCore,
            int helixCoreSignetMask,
            int helixSecondaryUnlockMask,
            DecimationCore decimationCore,
            int decimationCoreSignetMask,
            int decimationSecondaryUnlockMask,
            int index
    ) {
        List<Skill> skills = workshopSkills(
                signet, secondaryUnlockMask, core, coreSignetMask,
                disciplineCore, disciplineCoreSignetMask, disciplineSecondaryUnlockMask,
                goldCore, goldCoreSignetMask, goldSecondaryUnlockMask,
                helixCore, helixCoreSignetMask, helixSecondaryUnlockMask,
                decimationCore, decimationCoreSignetMask, decimationSecondaryUnlockMask
        );
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public static int getLevel(ItemStack stack, Skill skill) {
        if (stack.isEmpty() || skill == null || fromStack(stack) != skill.signet()) return 0;
        var tag = stack.getTag();
        if (tag == null) return 0;
        if (tag.contains(NBT_SKILL_LEVELS)) {
            var levels = tag.getCompound(NBT_SKILL_LEVELS);
            if (levels.contains(skill.id())) {
                return Mth.clamp(levels.getInt(skill.id()), 0, MAX_LEVEL);
            }
        }
        if (skill.isDeliveranceSecondary() || skill.isDeliveranceCoreSignet()
                || skill.isDisciplineSecondary()
                || skill.isDisciplineCoreSignet()
                || skill.isHelixSecondary()
                || skill.isHelixCoreSignet()
                || skill.isDecimationSecondary()
                || skill.isDecimationCoreSignet()
                || skill.isBodhiSecondary()
                || skill.isBodhiCoreSignet()
                || skill.isDaybreakSecondary()
                || skill.isDaybreakCoreSignet()
                || skill.isGoldSecondary()
                || skill.isGoldCoreSignet()
                || skill.isExtensionSecondary()
                || skill.isExtensionCoreSignet()) return 0;
        return Mth.clamp(tag.getInt(LEGACY_NBT_LEVEL), 0, MAX_LEVEL);
    }

    private static void setStackLevel(ItemStack stack, Skill skill, int level) {
        if (stack.isEmpty() || skill == null || fromStack(stack) != skill.signet()) return;
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag levels = tag.getCompound(NBT_SKILL_LEVELS);
        levels.putInt(skill.id(), Mth.clamp(level, 0, MAX_LEVEL));
        tag.put(NBT_SKILL_LEVELS, levels);
    }

    private static CompoundTag getPlayerPersistedData(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(NBT_PLAYER_PERSISTED)) {
            root.put(NBT_PLAYER_PERSISTED, new CompoundTag());
        }
        return root.getCompound(NBT_PLAYER_PERSISTED);
    }

    public static int getPlayerLevel(Player player, Skill skill) {
        if (player == null || skill == null) return 0;
        CompoundTag levels = getPlayerPersistedData(player).getCompound(NBT_PLAYER_SKILL_LEVELS);
        return Mth.clamp(levels.getInt(skill.id()), 0, MAX_LEVEL);
    }

    private static void setPlayerLevel(Player player, Skill skill, int level) {
        if (player == null || skill == null) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        CompoundTag levels = persisted.getCompound(NBT_PLAYER_SKILL_LEVELS);
        levels.putInt(skill.id(), Mth.clamp(level, 0, MAX_LEVEL));
        persisted.put(NBT_PLAYER_SKILL_LEVELS, levels);
    }

    public static void synchronizeLevels(Player player, ItemStack stack, Signet signet) {
        if (player == null || stack.isEmpty() || signet == null || fromStack(stack) != signet) return;
        CompoundTag resetData = getPlayerPersistedData(player);
        String resetKey = extensionKey(signet, "CoreResetGeneration");
        int generation = resetData.getInt(resetKey);
        if (stack.getOrCreateTag().getInt(resetKey) < generation) {
            clearCoreStack(stack, signet, coreKeys(signet));
            stack.getOrCreateTag().putInt(resetKey, generation);
        }
        if (!isCoreStackCompatible(player, stack, signet)) return;
        if (signet == Signet.DELIVERANCE) {
            CompoundTag persisted = getPlayerPersistedData(player);
            int unlockMask = persisted.getInt(NBT_DELIVERANCE_SECONDARY_UNLOCKS)
                    | getStackDeliveranceSecondaryUnlockMask(stack);
            persisted.putInt(NBT_DELIVERANCE_SECONDARY_UNLOCKS, unlockMask);
            setStackDeliveranceSecondaryUnlockMask(stack, unlockMask);

            DeliveranceCore playerCore = DeliveranceCore.fromId(
                    persisted.getInt(NBT_DELIVERANCE_CORE)
            );
            DeliveranceCore stackCore = getStackDeliveranceCore(stack);
            DeliveranceCore core = playerCore != null ? playerCore : stackCore;
            if (core != null) {
                persisted.putInt(NBT_DELIVERANCE_CORE, core.id());
                setStackDeliveranceCore(stack, core);
            }

            int coreSignetMask = persisted.getInt(NBT_DELIVERANCE_CORE_SIGNETS)
                    | getStackDeliveranceCoreSignetMask(stack);
            persisted.putInt(NBT_DELIVERANCE_CORE_SIGNETS, coreSignetMask);
            setStackDeliveranceCoreSignetMask(stack, coreSignetMask);
        } else if (signet == Signet.DISCIPLINE) {
            CompoundTag persisted = getPlayerPersistedData(player);
            int secondaryMask = persisted.getInt(NBT_DISCIPLINE_SECONDARY_UNLOCKS)
                    | getStackDisciplineSecondaryUnlockMask(stack);
            persisted.putInt(NBT_DISCIPLINE_SECONDARY_UNLOCKS, secondaryMask);
            setStackDisciplineSecondaryUnlockMask(stack, secondaryMask);
            DisciplineCore playerCore = DisciplineCore.fromId(persisted.getInt(NBT_DISCIPLINE_CORE));
            DisciplineCore stackCore = getStackDisciplineCore(stack);
            DisciplineCore core = playerCore != null ? playerCore : stackCore;
            if (core != null) {
                persisted.putInt(NBT_DISCIPLINE_CORE, core.id());
                setStackDisciplineCore(stack, core);
            }
            int coreSignetMask = persisted.getInt(NBT_DISCIPLINE_CORE_SIGNETS)
                    | getStackDisciplineCoreSignetMask(stack);
            persisted.putInt(NBT_DISCIPLINE_CORE_SIGNETS, coreSignetMask);
            setStackDisciplineCoreSignetMask(stack, coreSignetMask);
        } else if (signet == Signet.BODHI) {
            CompoundTag persisted = getPlayerPersistedData(player);
            int secondaryMask = persisted.getInt(NBT_BODHI_SECONDARY_UNLOCKS)
                    | getStackBodhiSecondaryUnlockMask(stack);
            persisted.putInt(NBT_BODHI_SECONDARY_UNLOCKS, secondaryMask);
            setStackBodhiSecondaryUnlockMask(stack, secondaryMask);
            BodhiCore playerCore = BodhiCore.fromId(persisted.getInt(NBT_BODHI_CORE));
            BodhiCore stackCore = getStackBodhiCore(stack);
            BodhiCore core = playerCore != null ? playerCore : stackCore;
            if (core != null) {
                persisted.putInt(NBT_BODHI_CORE, core.id());
                setStackBodhiCore(stack, core);
            }
            int coreSignetMask = persisted.getInt(NBT_BODHI_CORE_SIGNETS)
                    | getStackBodhiCoreSignetMask(stack);
            persisted.putInt(NBT_BODHI_CORE_SIGNETS, coreSignetMask);
            setStackBodhiCoreSignetMask(stack, coreSignetMask);
        } else if (signet == Signet.DAYBREAK) {
            CompoundTag persisted = getPlayerPersistedData(player);
            int secondaryMask = persisted.getInt(NBT_DAYBREAK_SECONDARY_UNLOCKS)
                    | getStackDaybreakSecondaryUnlockMask(stack);
            persisted.putInt(NBT_DAYBREAK_SECONDARY_UNLOCKS, secondaryMask);
            setStackDaybreakSecondaryUnlockMask(stack, secondaryMask);
            DaybreakCore playerCore = DaybreakCore.fromId(persisted.getInt(NBT_DAYBREAK_CORE));
            DaybreakCore stackCore = getStackDaybreakCore(stack);
            DaybreakCore core = playerCore != null ? playerCore : stackCore;
            if (core != null) {
                persisted.putInt(NBT_DAYBREAK_CORE, core.id());
                setStackDaybreakCore(stack, core);
            }
            int coreSignetMask = persisted.getInt(NBT_DAYBREAK_CORE_SIGNETS)
                    | getStackDaybreakCoreSignetMask(stack);
            persisted.putInt(NBT_DAYBREAK_CORE_SIGNETS, coreSignetMask);
            setStackDaybreakCoreSignetMask(stack, coreSignetMask);
        } else if (signet == Signet.SETSURA) {
            CompoundTag persisted = getPlayerPersistedData(player);
            int secondaryMask = persisted.getInt(NBT_SETSURA_SECONDARY_UNLOCKS)
                    | getStackSetsuraSecondaryUnlockMask(stack);
            persisted.putInt(NBT_SETSURA_SECONDARY_UNLOCKS, secondaryMask);
            setStackSetsuraSecondaryUnlockMask(stack, secondaryMask);
            SetsuraCore playerCore = SetsuraCore.fromId(persisted.getInt(NBT_SETSURA_CORE));
            SetsuraCore stackCore = getStackSetsuraCore(stack);
            SetsuraCore core = playerCore != null ? playerCore : stackCore;
            if (core != null) {
                persisted.putInt(NBT_SETSURA_CORE, core.id());
                setStackSetsuraCore(stack, core);
            }
            int coreSignetMask = persisted.getInt(NBT_SETSURA_CORE_SIGNETS)
                    | getStackSetsuraCoreSignetMask(stack);
            persisted.putInt(NBT_SETSURA_CORE_SIGNETS, coreSignetMask);
            setStackSetsuraCoreSignetMask(stack, coreSignetMask);
        } else if (signet == Signet.DECIMATION) {
            CompoundTag persisted = getPlayerPersistedData(player);
            int secondaryMask = persisted.getInt(NBT_DECIMATION_SECONDARY_UNLOCKS)
                    | getStackDecimationSecondaryUnlockMask(stack);
            persisted.putInt(NBT_DECIMATION_SECONDARY_UNLOCKS, secondaryMask);
            setStackDecimationSecondaryUnlockMask(stack, secondaryMask);
            DecimationCore playerCore = DecimationCore.fromId(persisted.getInt(NBT_DECIMATION_CORE));
            DecimationCore stackCore = getStackDecimationCore(stack);
            DecimationCore core = playerCore != null ? playerCore : stackCore;
            if (core != null) {
                persisted.putInt(NBT_DECIMATION_CORE, core.id());
                setStackDecimationCore(stack, core);
            }
            int coreSignetMask = persisted.getInt(NBT_DECIMATION_CORE_SIGNETS)
                    | getStackDecimationCoreSignetMask(stack);
            persisted.putInt(NBT_DECIMATION_CORE_SIGNETS, coreSignetMask);
            setStackDecimationCoreSignetMask(stack, coreSignetMask);
        } else if (signet == Signet.HELIX) {
            CompoundTag persisted = getPlayerPersistedData(player);
            int secondaryMask = persisted.getInt(NBT_HELIX_SECONDARY_UNLOCKS)
                    | getStackHelixSecondaryUnlockMask(stack);
            persisted.putInt(NBT_HELIX_SECONDARY_UNLOCKS, secondaryMask);
            setStackHelixSecondaryUnlockMask(stack, secondaryMask);
            HelixCore playerCore = HelixCore.fromId(persisted.getInt(NBT_HELIX_CORE));
            HelixCore stackCore = getStackHelixCore(stack);
            HelixCore core = playerCore != null ? playerCore : stackCore;
            if (core != null) {
                persisted.putInt(NBT_HELIX_CORE, core.id());
                setStackHelixCore(stack, core);
            }
            int coreSignetMask = persisted.getInt(NBT_HELIX_CORE_SIGNETS)
                    | getStackHelixCoreSignetMask(stack);
            persisted.putInt(NBT_HELIX_CORE_SIGNETS, coreSignetMask);
            setStackHelixCoreSignetMask(stack, coreSignetMask);
        } else if (signet == Signet.GOLD) {
            CompoundTag persisted = getPlayerPersistedData(player);
            int secondaryMask = persisted.getInt(NBT_GOLD_SECONDARY_UNLOCKS)
                    | getStackGoldSecondaryUnlockMask(stack);
            persisted.putInt(NBT_GOLD_SECONDARY_UNLOCKS, secondaryMask);
            setStackGoldSecondaryUnlockMask(stack, secondaryMask);
            GoldCore playerCore = GoldCore.fromId(persisted.getInt(NBT_GOLD_CORE));
            GoldCore stackCore = getStackGoldCore(stack);
            GoldCore core = playerCore != null ? playerCore : stackCore;
            if (core != null) {
                persisted.putInt(NBT_GOLD_CORE, core.id());
                setStackGoldCore(stack, core);
            }
            int coreSignetMask = persisted.getInt(NBT_GOLD_CORE_SIGNETS)
                    | getStackGoldCoreSignetMask(stack);
            persisted.putInt(NBT_GOLD_CORE_SIGNETS, coreSignetMask);
            setStackGoldCoreSignetMask(stack, coreSignetMask);
        }
        if (signet == Signet.INFINITY || signet == Signet.STARS
                || signet == Signet.VICISSITUDE || signet == Signet.REVERIE) {
            CompoundTag persisted = getPlayerPersistedData(player);
            String secondaryKey = extensionKey(signet, "SecondaryUnlocks");
            int secondaryMask = persisted.getInt(secondaryKey)
                    | getStackExtensionSecondaryUnlockMask(stack);
            persisted.putInt(secondaryKey, secondaryMask);
            stack.getOrCreateTag().putInt(secondaryKey, secondaryMask);

            String coreKey = extensionKey(signet, "Core");
            ExtensionCore playerCore = ExtensionCore.fromId(signet, persisted.getInt(coreKey));
            ExtensionCore stackCore = getStackExtensionCore(stack);
            ExtensionCore extensionCore = playerCore != null ? playerCore : stackCore;
            if (extensionCore != null) {
                persisted.putInt(coreKey, extensionCore.id());
                stack.getOrCreateTag().putInt(coreKey, extensionCore.id());
            }

            String augmentsKey = extensionKey(signet, "CoreSignets");
            int augmentMask = persisted.getInt(augmentsKey)
                    | getStackExtensionCoreSignetMask(stack);
            persisted.putInt(augmentsKey, augmentMask);
            stack.getOrCreateTag().putInt(augmentsKey, augmentMask);
        }

        for (Skill skill : allSkillsFor(signet)) {
            int level = Math.max(getPlayerLevel(player, skill), getLevel(stack, skill));
            setPlayerLevel(player, skill, level);
            setStackLevel(stack, skill, level);
        }
    }

    public static void synchronizeOwnedLevels(Player player, Signet signet) {
        if (player == null || signet == null) return;
        CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(signet.item()))
                .ifPresent(slot -> synchronizeLevels(player, slot.stack(), signet));
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(signet.item())) {
                synchronizeLevels(player, stack, signet);
            }
        }
    }

    public static int getEquippedLevel(Player player, Skill skill) {
        synchronizeOwnedLevels(player, skill.signet());
        return getPlayerLevel(player, skill);
    }

    public static boolean upgradeStack(Player player, ItemStack stack, Skill skill) {
        if (skill == null || !skill.isUpgradeable() || fromStack(stack) != skill.signet()) return false;
        synchronizeLevels(player, stack, skill.signet());
        int current = getPlayerLevel(player, skill);
        if (current >= MAX_LEVEL) return false;
        setPlayerLevel(player, skill, current + 1);
        setStackLevel(stack, skill, current + 1);
        return true;
    }

    public static boolean isSignetMaxed(Player player, Signet signet) {
        if (player == null || signet == null) return false;
        synchronizeOwnedLevels(player, signet);
        List<Skill> skills = workshopSkills(player, signet);
        return !skills.isEmpty() && skills.stream()
                .allMatch(skill -> !skill.isUpgradeable()
                        || getPlayerLevel(player, skill) >= MAX_LEVEL);
    }

    public static DeliveranceCore getPurchasedDeliveranceCore(Player player) {
        if (player == null) return null;
        return DeliveranceCore.fromId(
                getPlayerPersistedData(player).getInt(NBT_DELIVERANCE_CORE_PURCHASED)
        );
    }

    public static DeliveranceCore getSelectedDeliveranceCore(Player player) {
        if (player == null) return null;
        DeliveranceCore purchased = getPurchasedDeliveranceCore(player);
        if (purchased != null) return purchased;
        DeliveranceCore fused = getDeliveranceCore(player);
        if (fused != null) return fused;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            DeliveranceCore held = deliveranceCoreFromItem(inventory.getItem(i));
            if (held != null && isCurrentCoreSystemItem(
                    player, inventory.getItem(i), Signet.DELIVERANCE)) return held;
        }
        return null;
    }

    public static DisciplineCore getPurchasedDisciplineCore(Player player) {
        if (player == null) return null;
        return DisciplineCore.fromId(
                getPlayerPersistedData(player).getInt(NBT_DISCIPLINE_CORE_PURCHASED)
        );
    }

    public static DisciplineCore getSelectedDisciplineCore(Player player) {
        if (player == null) return null;
        DisciplineCore purchased = getPurchasedDisciplineCore(player);
        if (purchased != null) return purchased;
        DisciplineCore fused = getDisciplineCore(player);
        if (fused != null) return fused;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            DisciplineCore held = disciplineCoreFromItem(inventory.getItem(i));
            if (held != null && isCurrentCoreSystemItem(
                    player, inventory.getItem(i), Signet.DISCIPLINE)) return held;
        }
        return null;
    }

    public static GoldCore getPurchasedGoldCore(Player player) {
        return player == null ? null
                : GoldCore.fromId(getPlayerPersistedData(player).getInt(NBT_GOLD_CORE_PURCHASED));
    }

    public static GoldCore getSelectedGoldCore(Player player) {
        if (player == null) return null;
        GoldCore purchased = getPurchasedGoldCore(player);
        if (purchased != null) return purchased;
        GoldCore fused = getGoldCore(player);
        if (fused != null) return fused;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            GoldCore held = goldCoreFromItem(inventory.getItem(i));
            if (held != null && isCurrentCoreSystemItem(
                    player, inventory.getItem(i), Signet.GOLD)) return held;
        }
        return null;
    }

    public static HelixCore getPurchasedHelixCore(Player player) {
        return player == null ? null
                : HelixCore.fromId(getPlayerPersistedData(player).getInt(NBT_HELIX_CORE_PURCHASED));
    }

    public static HelixCore getSelectedHelixCore(Player player) {
        if (player == null) return null;
        HelixCore purchased = getPurchasedHelixCore(player);
        if (purchased != null) return purchased;
        HelixCore fused = getHelixCore(player);
        if (fused != null) return fused;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            HelixCore held = helixCoreFromItem(inventory.getItem(i));
            if (held != null && isCurrentCoreSystemItem(
                    player, inventory.getItem(i), Signet.HELIX)) return held;
        }
        return null;
    }

    public static boolean hasPurchasedHelixCoreSignet(Player player, Skill skill) {
        return player != null && skill != null && skill.isHelixCoreSignet()
                && (getPlayerPersistedData(player).getInt(NBT_HELIX_CORE_SIGNETS_PURCHASED)
                & skill.helixCoreSignetBit()) != 0;
    }

    public static void recordHelixCorePurchase(Player player, HelixCore core) {
        if (player != null && core != null) {
            getPlayerPersistedData(player).putInt(NBT_HELIX_CORE_PURCHASED, core.id());
        }
    }

    public static void recordHelixCoreSignetPurchase(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isHelixCoreSignet()) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        persisted.putInt(NBT_HELIX_CORE_SIGNETS_PURCHASED,
                persisted.getInt(NBT_HELIX_CORE_SIGNETS_PURCHASED) | skill.helixCoreSignetBit());
    }

    public static BodhiCore getPurchasedBodhiCore(Player player) {
        return player == null ? null
                : BodhiCore.fromId(getPlayerPersistedData(player).getInt(NBT_BODHI_CORE_PURCHASED));
    }

    public static BodhiCore getSelectedBodhiCore(Player player) {
        if (player == null) return null;
        BodhiCore purchased = getPurchasedBodhiCore(player);
        if (purchased != null) return purchased;
        BodhiCore fused = getBodhiCore(player);
        if (fused != null) return fused;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            BodhiCore held = bodhiCoreFromItem(inventory.getItem(i));
            if (held != null && isCurrentCoreSystemItem(
                    player, inventory.getItem(i), Signet.BODHI)) return held;
        }
        return null;
    }

    public static boolean hasPurchasedBodhiCoreSignet(Player player, Skill skill) {
        return player != null && skill != null && skill.isBodhiCoreSignet()
                && (getPlayerPersistedData(player).getInt(NBT_BODHI_CORE_SIGNETS_PURCHASED)
                & skill.bodhiCoreSignetBit()) != 0;
    }

    public static void recordBodhiCorePurchase(Player player, BodhiCore core) {
        if (player != null && core != null) {
            getPlayerPersistedData(player).putInt(NBT_BODHI_CORE_PURCHASED, core.id());
        }
    }

    public static void recordBodhiCoreSignetPurchase(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isBodhiCoreSignet()) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        persisted.putInt(NBT_BODHI_CORE_SIGNETS_PURCHASED,
                persisted.getInt(NBT_BODHI_CORE_SIGNETS_PURCHASED)
                        | skill.bodhiCoreSignetBit());
    }

    public static DaybreakCore getPurchasedDaybreakCore(Player player) {
        return player == null ? null
                : DaybreakCore.fromId(getPlayerPersistedData(player).getInt(NBT_DAYBREAK_CORE_PURCHASED));
    }

    public static DaybreakCore getSelectedDaybreakCore(Player player) {
        if (player == null) return null;
        DaybreakCore purchased = getPurchasedDaybreakCore(player);
        if (purchased != null) return purchased;
        DaybreakCore fused = getDaybreakCore(player);
        if (fused != null) return fused;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            DaybreakCore held = daybreakCoreFromItem(inventory.getItem(i));
            if (held != null && isCurrentCoreSystemItem(
                    player, inventory.getItem(i), Signet.DAYBREAK)) return held;
        }
        return null;
    }

    public static boolean hasPurchasedDaybreakCoreSignet(Player player, Skill skill) {
        return player != null && skill != null && skill.isDaybreakCoreSignet()
                && (getPlayerPersistedData(player).getInt(NBT_DAYBREAK_CORE_SIGNETS_PURCHASED)
                & skill.daybreakCoreSignetBit()) != 0;
    }

    public static void recordDaybreakCorePurchase(Player player, DaybreakCore core) {
        if (player != null && core != null) {
            getPlayerPersistedData(player).putInt(NBT_DAYBREAK_CORE_PURCHASED, core.id());
        }
    }

    public static void recordDaybreakCoreSignetPurchase(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isDaybreakCoreSignet()) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        persisted.putInt(NBT_DAYBREAK_CORE_SIGNETS_PURCHASED,
                persisted.getInt(NBT_DAYBREAK_CORE_SIGNETS_PURCHASED)
                        | skill.daybreakCoreSignetBit());
    }

    public static SetsuraCore getPurchasedSetsuraCore(Player player) {
        return player == null ? null
                : SetsuraCore.fromId(getPlayerPersistedData(player).getInt(NBT_SETSURA_CORE_PURCHASED));
    }

    public static SetsuraCore getSelectedSetsuraCore(Player player) {
        if (player == null) return null;
        SetsuraCore purchased = getPurchasedSetsuraCore(player);
        if (purchased != null) return purchased;
        SetsuraCore fused = getSetsuraCore(player);
        if (fused != null) return fused;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            SetsuraCore held = setsuraCoreFromItem(inventory.getItem(i));
            if (held != null && isCurrentCoreSystemItem(
                    player, inventory.getItem(i), Signet.SETSURA)) return held;
        }
        return null;
    }

    public static boolean hasPurchasedSetsuraCoreSignet(Player player, Skill skill) {
        return player != null && skill != null && skill.isSetsuraCoreSignet()
                && (getPlayerPersistedData(player).getInt(NBT_SETSURA_CORE_SIGNETS_PURCHASED)
                & skill.setsuraCoreSignetBit()) != 0;
    }

    public static void recordSetsuraCorePurchase(Player player, SetsuraCore core) {
        if (player != null && core != null) {
            getPlayerPersistedData(player).putInt(NBT_SETSURA_CORE_PURCHASED, core.id());
        }
    }

    public static void recordSetsuraCoreSignetPurchase(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isSetsuraCoreSignet()) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        persisted.putInt(NBT_SETSURA_CORE_SIGNETS_PURCHASED,
                persisted.getInt(NBT_SETSURA_CORE_SIGNETS_PURCHASED)
                        | skill.setsuraCoreSignetBit());
    }

    public static DecimationCore getPurchasedDecimationCore(Player player) {
        return player == null ? null
                : DecimationCore.fromId(getPlayerPersistedData(player).getInt(NBT_DECIMATION_CORE_PURCHASED));
    }

    public static DecimationCore getSelectedDecimationCore(Player player) {
        if (player == null) return null;
        DecimationCore purchased = getPurchasedDecimationCore(player);
        if (purchased != null) return purchased;
        DecimationCore fused = getDecimationCore(player);
        if (fused != null) return fused;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            DecimationCore held = decimationCoreFromItem(inventory.getItem(i));
            if (held != null && isCurrentCoreSystemItem(
                    player, inventory.getItem(i), Signet.DECIMATION)) return held;
        }
        return null;
    }

    public static boolean hasPurchasedDecimationCoreSignet(Player player, Skill skill) {
        return player != null && skill != null && skill.isDecimationCoreSignet()
                && (getPlayerPersistedData(player).getInt(NBT_DECIMATION_CORE_SIGNETS_PURCHASED)
                & skill.decimationCoreSignetBit()) != 0;
    }

    public static void recordDecimationCorePurchase(Player player, DecimationCore core) {
        if (player != null && core != null) {
            getPlayerPersistedData(player).putInt(NBT_DECIMATION_CORE_PURCHASED, core.id());
        }
    }

    public static void recordDecimationCoreSignetPurchase(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isDecimationCoreSignet()) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        persisted.putInt(NBT_DECIMATION_CORE_SIGNETS_PURCHASED,
                persisted.getInt(NBT_DECIMATION_CORE_SIGNETS_PURCHASED)
                        | skill.decimationCoreSignetBit());
    }

    public static boolean hasPurchasedGoldCoreSignet(Player player, Skill skill) {
        return player != null && skill != null && skill.isGoldCoreSignet()
                && (getPlayerPersistedData(player).getInt(NBT_GOLD_CORE_SIGNETS_PURCHASED)
                & skill.goldCoreSignetBit()) != 0;
    }

    public static void recordGoldCorePurchase(Player player, GoldCore core) {
        if (player != null && core != null) {
            getPlayerPersistedData(player).putInt(NBT_GOLD_CORE_PURCHASED, core.id());
        }
    }

    public static void recordGoldCoreSignetPurchase(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isGoldCoreSignet()) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        persisted.putInt(NBT_GOLD_CORE_SIGNETS_PURCHASED,
                persisted.getInt(NBT_GOLD_CORE_SIGNETS_PURCHASED) | skill.goldCoreSignetBit());
    }

    public static boolean hasPurchasedDisciplineCoreSignet(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isDisciplineCoreSignet()) return false;
        return (getPlayerPersistedData(player).getInt(NBT_DISCIPLINE_CORE_SIGNETS_PURCHASED)
                & skill.disciplineCoreSignetBit()) != 0;
    }

    public static void recordDisciplineCorePurchase(Player player, DisciplineCore core) {
        if (player == null || core == null) return;
        getPlayerPersistedData(player).putInt(NBT_DISCIPLINE_CORE_PURCHASED, core.id());
    }

    public static void recordDisciplineCoreSignetPurchase(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isDisciplineCoreSignet()) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        persisted.putInt(
                NBT_DISCIPLINE_CORE_SIGNETS_PURCHASED,
                persisted.getInt(NBT_DISCIPLINE_CORE_SIGNETS_PURCHASED)
                        | skill.disciplineCoreSignetBit()
        );
    }

    public static boolean hasPurchasedDeliveranceCoreSignet(Player player, Skill skill) {
        if (player == null || skill == null || !skill.isDeliveranceCoreSignet()) return false;
        int mask = getPlayerPersistedData(player)
                .getInt(NBT_DELIVERANCE_CORE_SIGNETS_PURCHASED);
        return (mask & skill.deliveranceCoreSignetBit()) != 0;
    }

    public static void recordDeliveranceCorePurchase(
            Player player,
            DeliveranceCore core,
            int actualSilverPaid
    ) {
        if (player == null || core == null) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        persisted.putInt(NBT_DELIVERANCE_CORE_PURCHASED, core.id());
        recordCoreSilverSpent(player, Signet.DELIVERANCE, actualSilverPaid);
    }

    public static void recordDeliveranceCoreSignetPurchase(
            Player player,
            Skill skill,
            int actualSilverPaid
    ) {
        if (player == null || skill == null || !skill.isDeliveranceCoreSignet()) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        int mask = persisted.getInt(NBT_DELIVERANCE_CORE_SIGNETS_PURCHASED);
        persisted.putInt(
                NBT_DELIVERANCE_CORE_SIGNETS_PURCHASED,
                mask | skill.deliveranceCoreSignetBit()
        );
        recordCoreSilverSpent(player, Signet.DELIVERANCE, actualSilverPaid);
    }

    public static void setShopPaidSilverPerItem(ItemStack stack, int actualSilverPaid) {
        if (stack.isEmpty()) return;
        stack.getOrCreateTag().putInt(
                NBT_SHOP_PAID_SILVER_PER_ITEM,
                Math.max(0, actualSilverPaid)
        );
    }

    public static int removeUpgradeItemsWithTrackedCost(
            Player player,
            Signet signet,
            int amount
    ) {
        if (amount <= 0 || countUpgradeItems(player, signet) < amount) return -1;
        Item upgradeItem = upgradeItemFor(signet);
        Inventory inventory = player.getInventory();
        int remaining = amount;
        int silverCost = 0;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(upgradeItem)) continue;
            int removed = Math.min(remaining, stack.getCount());
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                silverCost += Math.max(0, tag.getInt(NBT_SHOP_PAID_SILVER_PER_ITEM)) * removed;
            }
            stack.shrink(removed);
            remaining -= removed;
        }
        inventory.setChanged();
        return remaining == 0 ? silverCost : -1;
    }

    public static void recordDeliveranceCoreUpgrade(
            Player player,
            int actualSilverSpent,
            int experienceLevelsSpent
    ) {
        recordCoreUpgrade(player, Signet.DELIVERANCE, actualSilverSpent,
                experienceLevelsSpent);
    }

    public static boolean isAnyCoreSignet(Skill skill) {
        return skill != null && (skill.isDeliveranceCoreSignet()
                || skill.isDisciplineCoreSignet()
                || skill.isGoldCoreSignet()
                || skill.isHelixCoreSignet()
                || skill.isDecimationCoreSignet()
                || skill.isBodhiCoreSignet()
                || skill.isSetsuraCoreSignet()
                || skill.isDaybreakCoreSignet()
                || skill.isExtensionCoreSignet());
    }

    public static void recordCoreUpgrade(Player player, Signet signet,
                                         int actualSilverSpent, int experienceLevelsSpent) {
        if (player == null || signet == null) return;
        recordCoreSilverSpent(player, signet, actualSilverSpent);
        if (experienceLevelsSpent <= 0) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        String key = coreKeys(signet).experienceSpent();
        long total = (long) Math.max(0, persisted.getInt(key)) + experienceLevelsSpent;
        persisted.putInt(key, (int) Math.min(Integer.MAX_VALUE, total));
    }

    public static void recordCoreSilverSpent(Player player, Signet signet, int amount) {
        if (player == null || signet == null || amount <= 0) return;
        CompoundTag persisted = getPlayerPersistedData(player);
        String key = coreKeys(signet).silverSpent();
        long total = (long) Math.max(0, persisted.getInt(key)) + amount;
        persisted.putInt(key, (int) Math.min(Integer.MAX_VALUE, total));
    }

    public static boolean hasDeliveranceCoreProgress(Player player) {
        return hasCoreProgress(player, Signet.DELIVERANCE);
    }

    public static boolean hasCoreProgress(Player player, Signet signet) {
        if (player == null || signet == null) return false;
        CompoundTag persisted = getPlayerPersistedData(player);
        CoreKeys keys = coreKeys(signet);
        return persisted.getInt(keys.purchased()) != 0
                || persisted.getInt(keys.core()) != 0
                || persisted.getInt(keys.signetsPurchased()) != 0
                || persisted.getInt(keys.signets()) != 0
                || hasSelectedCore(player, signet);
    }

    public record ResetRefund(int silver, int experienceLevels) {
    }

    public static ResetRefund resetDeliveranceCoreProgress(
            Player player,
            ItemStack workshopDeliveranceStack
    ) {
        return resetCoreProgress(player, Signet.DELIVERANCE, workshopDeliveranceStack);
    }

    public static ResetRefund resetCoreProgress(Player player, Signet signet,
                                                 ItemStack workshopSignetStack) {
        if (player == null || signet == null
                || fromStack(workshopSignetStack) != signet
                || !hasCoreProgress(player, signet)) return new ResetRefund(0, 0);
        CompoundTag persisted = getPlayerPersistedData(player);
        CoreKeys keys = coreKeys(signet);
        int silverRefund = Math.max(0, persisted.getInt(keys.silverSpent())) / 2;
        int experienceRefund = Math.max(0, persisted.getInt(keys.experienceSpent())) / 2;

        persisted.remove(keys.core());
        persisted.remove(keys.signets());
        persisted.remove(keys.purchased());
        persisted.remove(keys.signetsPurchased());
        persisted.remove(keys.silverSpent());
        persisted.remove(keys.experienceSpent());
        String resetKey = extensionKey(signet, "CoreResetGeneration");
        persisted.putInt(resetKey, (int) Math.min(Integer.MAX_VALUE,
                (long) Math.max(0, persisted.getInt(resetKey)) + 1L));

        CompoundTag playerLevels = persisted.getCompound(NBT_PLAYER_SKILL_LEVELS);
        for (Skill skill : Skill.values()) {
            if (skill.signet() == signet && isAnyCoreSignet(skill)) {
                playerLevels.remove(skill.id());
            }
        }
        persisted.put(NBT_PLAYER_SKILL_LEVELS, playerLevels);

        clearCoreStack(workshopSignetStack, signet, keys);
        CuriosApi.getCuriosHelper()
                .findCurios(player, signet.item())
                .forEach(slot -> clearCoreStack(slot.stack(), signet, keys));

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            clearCoreStack(stack, signet, keys);
        }
        inventory.setChanged();

        addSilver(player, silverRefund);
        if (experienceRefund > 0) {
            player.giveExperienceLevels(experienceRefund);
        }
        return new ResetRefund(silverRefund, experienceRefund);
    }

    public static boolean isDeliveranceCoreSystemItem(ItemStack stack) {
        return isCoreSystemItem(stack, Signet.DELIVERANCE);
    }

    public static boolean isCoreSystemItem(ItemStack stack, Signet signet) {
        if (stack.isEmpty() || signet == null) return false;
        return switch (signet) {
            case DELIVERANCE -> deliveranceCoreFromItem(stack) != null
                    || deliveranceCoreSignetFromItem(stack) != null;
            case DISCIPLINE -> disciplineCoreFromItem(stack) != null
                    || disciplineCoreSignetFromItem(stack) != null;
            case GOLD -> goldCoreFromItem(stack) != null
                    || goldCoreSignetFromItem(stack) != null;
            case HELIX -> helixCoreFromItem(stack) != null
                    || helixCoreSignetFromItem(stack) != null;
            case DECIMATION -> decimationCoreFromItem(stack) != null
                    || decimationCoreSignetFromItem(stack) != null;
            case BODHI -> bodhiCoreFromItem(stack) != null
                    || bodhiCoreSignetFromItem(stack) != null;
            case SETSURA -> setsuraCoreFromItem(stack) != null
                    || setsuraCoreSignetFromItem(stack) != null;
            case DAYBREAK -> daybreakCoreFromItem(stack) != null
                    || daybreakCoreSignetFromItem(stack) != null;
            case INFINITY, STARS, VICISSITUDE, REVERIE -> {
                ExtensionCore core = extensionCoreFromItem(stack);
                Skill augment = extensionCoreSignetFromItem(stack);
                yield core != null && core.signet() == signet
                        || augment != null && augment.signet() == signet;
            }
        };
    }

    public static void stampCoreSystemItem(Player player, ItemStack stack, Signet signet) {
        if (player == null || !isCoreSystemItem(stack, signet)) return;
        String key = extensionKey(signet, "CoreResetGeneration");
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(key, getPlayerPersistedData(player).getInt(key));
        tag.putUUID(NBT_CORE_ITEM_OWNER, player.getUUID());
    }

    public static boolean isCurrentCoreSystemItem(Player player, ItemStack stack, Signet signet) {
        if (player == null || !isCoreSystemItem(stack, signet)) return false;
        if (player.getAbilities().instabuild) return true;
        String key = extensionKey(signet, "CoreResetGeneration");
        int generation = getPlayerPersistedData(player).getInt(key);
        CompoundTag tag = stack.getTag();
        if (tag == null) return generation == 0;
        if (tag.hasUUID(NBT_CORE_ITEM_OWNER)
                && !player.getUUID().equals(tag.getUUID(NBT_CORE_ITEM_OWNER))) return true;
        return tag.getInt(key) == generation;
    }

    private static void clearCoreStack(ItemStack stack, Signet signet, CoreKeys keys) {
        if (stack.isEmpty() || fromStack(stack) != signet) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(keys.core());
        tag.remove(keys.signets());
        CompoundTag levels = tag.getCompound(NBT_SKILL_LEVELS);
        for (Skill skill : Skill.values()) {
            if (skill.signet() == signet && isAnyCoreSignet(skill)) {
                levels.remove(skill.id());
            }
        }
        tag.put(NBT_SKILL_LEVELS, levels);
    }

    private static boolean hasSelectedCore(Player player, Signet signet) {
        return selectedCoreId(player, signet) != 0;
    }

    private static boolean isCoreStackCompatible(Player player, ItemStack stack, Signet signet) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return true;
        CoreKeys keys = coreKeys(signet);
        int stackCore = tag.getInt(keys.core());
        if (stackCore == 0) return tag.getInt(keys.signets()) == 0;
        int selected = selectedCoreId(player, signet);
        return selected == 0 || selected == stackCore;
    }

    private static int selectedCoreId(Player player, Signet signet) {
        return switch (signet) {
            case DELIVERANCE -> {
                DeliveranceCore core = getSelectedDeliveranceCore(player);
                yield core == null ? 0 : core.id();
            }
            case DISCIPLINE -> {
                DisciplineCore core = getSelectedDisciplineCore(player);
                yield core == null ? 0 : core.id();
            }
            case GOLD -> {
                GoldCore core = getSelectedGoldCore(player);
                yield core == null ? 0 : core.id();
            }
            case HELIX -> {
                HelixCore core = getSelectedHelixCore(player);
                yield core == null ? 0 : core.id();
            }
            case DECIMATION -> {
                DecimationCore core = getSelectedDecimationCore(player);
                yield core == null ? 0 : core.id();
            }
            case BODHI -> {
                BodhiCore core = getSelectedBodhiCore(player);
                yield core == null ? 0 : core.id();
            }
            case SETSURA -> {
                SetsuraCore core = getSelectedSetsuraCore(player);
                yield core == null ? 0 : core.id();
            }
            case DAYBREAK -> {
                DaybreakCore core = getSelectedDaybreakCore(player);
                yield core == null ? 0 : core.id();
            }
            case INFINITY, STARS, VICISSITUDE, REVERIE -> {
                ExtensionCore core = getSelectedExtensionCore(player, signet);
                yield core == null ? 0 : core.id();
            }
        };
    }

    private record CoreKeys(String core, String signets, String purchased,
                            String signetsPurchased, String silverSpent,
                            String experienceSpent) {
    }

    private static CoreKeys coreKeys(Signet signet) {
        String core;
        String signets;
        String purchased;
        String signetsPurchased;
        if (signet == Signet.DELIVERANCE) {
            core = NBT_DELIVERANCE_CORE;
            signets = NBT_DELIVERANCE_CORE_SIGNETS;
            purchased = NBT_DELIVERANCE_CORE_PURCHASED;
            signetsPurchased = NBT_DELIVERANCE_CORE_SIGNETS_PURCHASED;
        } else if (signet == Signet.DISCIPLINE) {
            core = NBT_DISCIPLINE_CORE;
            signets = NBT_DISCIPLINE_CORE_SIGNETS;
            purchased = NBT_DISCIPLINE_CORE_PURCHASED;
            signetsPurchased = NBT_DISCIPLINE_CORE_SIGNETS_PURCHASED;
        } else if (signet == Signet.GOLD) {
            core = NBT_GOLD_CORE;
            signets = NBT_GOLD_CORE_SIGNETS;
            purchased = NBT_GOLD_CORE_PURCHASED;
            signetsPurchased = NBT_GOLD_CORE_SIGNETS_PURCHASED;
        } else if (signet == Signet.HELIX) {
            core = NBT_HELIX_CORE;
            signets = NBT_HELIX_CORE_SIGNETS;
            purchased = NBT_HELIX_CORE_PURCHASED;
            signetsPurchased = NBT_HELIX_CORE_SIGNETS_PURCHASED;
        } else if (signet == Signet.DECIMATION) {
            core = NBT_DECIMATION_CORE;
            signets = NBT_DECIMATION_CORE_SIGNETS;
            purchased = NBT_DECIMATION_CORE_PURCHASED;
            signetsPurchased = NBT_DECIMATION_CORE_SIGNETS_PURCHASED;
        } else if (signet == Signet.BODHI) {
            core = NBT_BODHI_CORE;
            signets = NBT_BODHI_CORE_SIGNETS;
            purchased = NBT_BODHI_CORE_PURCHASED;
            signetsPurchased = NBT_BODHI_CORE_SIGNETS_PURCHASED;
        } else if (signet == Signet.SETSURA) {
            core = NBT_SETSURA_CORE;
            signets = NBT_SETSURA_CORE_SIGNETS;
            purchased = NBT_SETSURA_CORE_PURCHASED;
            signetsPurchased = NBT_SETSURA_CORE_SIGNETS_PURCHASED;
        } else if (signet == Signet.DAYBREAK) {
            core = NBT_DAYBREAK_CORE;
            signets = NBT_DAYBREAK_CORE_SIGNETS;
            purchased = NBT_DAYBREAK_CORE_PURCHASED;
            signetsPurchased = NBT_DAYBREAK_CORE_SIGNETS_PURCHASED;
        } else {
            core = extensionKey(signet, "Core");
            signets = extensionKey(signet, "CoreSignets");
            purchased = extensionKey(signet, "CorePurchased");
            signetsPurchased = extensionKey(signet, "CoreSignetsPurchased");
        }
        String silverSpent = signet == Signet.DELIVERANCE
                ? NBT_DELIVERANCE_CORE_SILVER_SPENT
                : extensionKey(signet, "CoreSilverSpent");
        String experienceSpent = signet == Signet.DELIVERANCE
                ? NBT_DELIVERANCE_CORE_EXPERIENCE_SPENT
                : extensionKey(signet, "CoreExperienceSpent");
        return new CoreKeys(core, signets, purchased, signetsPurchased,
                silverSpent, experienceSpent);
    }

    public static float factorForLevel(int level) {
        return FACTORS[Mth.clamp(level, 0, MAX_LEVEL)];
    }

    public static float scaleBonus(Player player, Skill skill, float baseValue) {
        return baseValue * factorForLevel(getEquippedLevel(player, skill));
    }

    public static double scaleBonus(Player player, Skill skill, double baseValue) {
        return baseValue * factorForLevel(getEquippedLevel(player, skill));
    }

    public static int vicissitudeChargeTicks(Player player, Skill skill) {
        return switch (getEquippedLevel(player, skill)) {
            case 1 -> 180;
            case 2 -> 160;
            case 3 -> 140;
            default -> 200;
        };
    }

    public static float setsuraCooldownMultiplier(Player player) {
        return switch (getEquippedLevel(player, Skill.SETSURA_DODGE)) {
            case 1 -> 0.92F;
            case 2 -> 0.84F;
            case 3 -> 0.75F;
            default -> 1.0F;
        };
    }

    public static double infinityHealthLoss(Player player) {
        return switch (getEquippedLevel(player, Skill.INFINITY_REBIRTH)) {
            case 1 -> 0.18D;
            case 2 -> 0.16D;
            case 3 -> 0.14D;
            default -> 0.20D;
        };
    }

    public static int reverieShopPercent(Player player) {
        int base = switch (getEquippedLevel(player, Skill.REVERIE_DISCOUNT)) {
            case 1 -> 75;
            case 2 -> 72;
            case 3 -> 70;
            default -> 80;
        };
        float discount = (100 - base) * ReverieSignetSystem.ordinaryBonusMultiplier(player);
        base = Math.max(1, Math.round(100.0F - discount));
        if (hasExtensionCoreSignet(player, Skill.REVERIE_ZERO)) {
            base = Math.max(1, Math.round(base * 0.95F));
        }
        return base;
    }

    public static float helixRepairBonus(Player player) {
        return switch (getEquippedLevel(player, Skill.HELIX_PARADOX)) {
            case 1 -> 0.25F;
            case 2 -> 0.50F;
            case 3 -> 0.75F;
            default -> 0.0F;
        };
    }

    public static float egoMultiplier(Player player) {
        return EGO_MULTIPLIER;
    }

    public static int countSilver(Player player) {
        long count = 0L;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.SHINY_SILVER.get())) {
                count += stack.getCount();
            } else if (stack.is(ModItems.SILVER_POUCH.get())) {
                count += SilverPouchItem.getBalance(stack);
            }
            if (count >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) count;
    }

    public static boolean removeSilver(Player player, int amount) {
        if (amount <= 0 || countSilver(player) < amount) return false;
        Inventory inventory = player.getInventory();
        int remaining = amount;

        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(ModItems.SILVER_POUCH.get())) continue;
            long balance = SilverPouchItem.getBalance(stack);
            int removed = (int) Math.min((long) remaining, balance);
            if (removed <= 0) continue;
            SilverPouchItem.setBalance(stack, balance - removed);
            remaining -= removed;
        }

        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(ModItems.SHINY_SILVER.get())) continue;
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }
        inventory.setChanged();
        return remaining == 0;
    }

    public static void addSilver(Player player, int amount) {
        if (player == null || amount <= 0) return;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.SILVER_POUCH.get())) {
                SilverPouchItem.setBalance(
                        stack,
                        SilverPouchItem.getBalance(stack) + amount
                );
                inventory.setChanged();
                return;
            }
        }

        int remaining = amount;
        int maxStackSize = ModItems.SHINY_SILVER.get().getMaxStackSize();
        while (remaining > 0) {
            int count = Math.min(maxStackSize, remaining);
            ItemStack refund = new ItemStack(ModItems.SHINY_SILVER.get(), count);
            if (!player.addItem(refund)) {
                player.drop(refund, false);
            }
            remaining -= count;
        }
        inventory.setChanged();
    }

    public static String levelSuffix(ItemStack stack, Skill skill) {
        if (!skill.isUpgradeable()) return "";
        int level = getLevel(stack, skill);
        return level == 0 ? "" : "  +" + level;
    }

    public static void addSkillTooltips(List<Component> tooltip, ItemStack stack, Signet signet,
                                        int nameColor, int effectColor) {
        for (Skill skill : skillsFor(signet)) {
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(skill.displayName() + levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addText(tooltip, skill.tooltipDescription(level), effectColor);
        }
    }

    public static void addUnlockedDeliveranceSecondaryTooltips(
            List<Component> tooltip,
            ItemStack stack,
            int nameColor,
            int effectColor
    ) {
        int unlockMask = getStackDeliveranceSecondaryUnlockMask(stack);
        for (Skill skill : allSkillsFor(Signet.DELIVERANCE)) {
            if (!skill.isDeliveranceSecondary()
                    || (unlockMask & skill.deliveranceSecondaryBit()) == 0) {
                continue;
            }
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(skill.displayName() + levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            tooltip.add(ModText.literal(skill.tooltipDescription(level))
                    .withStyle(style -> style.withColor(effectColor).withItalic(true)));
        }
    }

    public static void addExtensionTooltips(
            List<Component> tooltip,
            ItemStack stack,
            int nameColor,
            int effectColor
    ) {
        Signet signet = fromStack(stack);
        if (signet == null) return;
        int secondaryMask = getStackExtensionSecondaryUnlockMask(stack);
        for (Skill skill : allSkillsFor(signet)) {
            if (!skill.isExtensionSecondary()
                    || (secondaryMask & skill.extensionSecondaryBit()) == 0) continue;
            int skillNameColor = ExtensionSignetColors.name(signet, skill.id());
            int skillEffectColor = ExtensionSignetColors.effect(signet, skill.id());
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .withStyle(style -> style.withColor(skillNameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id(), skillEffectColor);
        }
        ExtensionCore core = getStackExtensionCore(stack);
        if (core == null) return;
        String coreId = "extension_core_" + core.name().toLowerCase();
        int coreNameColor = ExtensionSignetColors.name(signet, coreId);
        int coreEffectColor = ExtensionSignetColors.effect(signet, coreId);
        tooltip.add(Component.empty());
        tooltip.add(ModText.literal("核心 · ")
                .append(Component.translatable("item.lastsongofelysian." + core.name().toLowerCase()))
                .withStyle(style -> style.withColor(coreNameColor)));
        SignetTooltipLines.addTranslated(tooltip,
                "tooltip.lastsongofelysian.signet." + coreId, coreEffectColor);
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.signet.extension_core_no_upgrade")
                .withStyle(style -> style.withColor(0xA8A8A8).withItalic(true)));
        int augmentMask = getStackExtensionCoreSignetMask(stack);
        for (Skill skill : allSkillsFor(signet)) {
            if (!skill.isExtensionCoreSignet() || skill.extensionCore() != core
                    || (augmentMask & skill.extensionCoreSignetBit()) == 0) continue;
            int skillNameColor = ExtensionSignetColors.name(signet, skill.id());
            int skillEffectColor = ExtensionSignetColors.effect(signet, skill.id());
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .withStyle(style -> style.withColor(skillNameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id(), skillEffectColor);
        }
    }

    public static void addDeliveranceCoreTooltips(
            List<Component> tooltip,
            ItemStack stack,
            int nameColor,
            int effectColor
    ) {
        DeliveranceCore core = getStackDeliveranceCore(stack);
        if (core == null) return;
        tooltip.add(Component.empty());
        tooltip.add(ModText.literal("核心 · " + core.displayName())
                .withStyle(style -> style.withColor(nameColor)));
        for (String line : core.description().split("\n")) {
            tooltip.add(ModText.literal(line)
                    .withStyle(style -> style.withColor(effectColor).withItalic(true)));
        }

        int mask = getStackDeliveranceCoreSignetMask(stack);
        for (Skill skill : Skill.values()) {
            if (!skill.isDeliveranceCoreSignet()
                    || skill.deliveranceCore() != core
                    || (mask & skill.deliveranceCoreSignetBit()) == 0) {
                continue;
            }
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(skill.displayName() + levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            tooltip.add(ModText.literal(skill.tooltipDescription(level))
                    .withStyle(style -> style.withColor(effectColor).withItalic(true)));
        }
    }

    public static void addUnlockedDisciplineSecondaryTooltips(
            List<Component> tooltip,
            ItemStack stack,
            int nameColor,
            int effectColor
    ) {
        int unlockMask = getStackDisciplineSecondaryUnlockMask(stack);
        for (Skill skill : allSkillsFor(Signet.DISCIPLINE)) {
            if (!skill.isDisciplineSecondary()
                    || (unlockMask & skill.disciplineSecondaryBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(skill.displayName() + levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            tooltip.add(Component.translatable(
                    "tooltip.lastsongofelysian.signet." + skill.id() + "." + level)
                    .withStyle(style -> style.withColor(effectColor).withItalic(true)));
        }
    }

    public static void addDisciplineCoreTooltips(
            List<Component> tooltip,
            ItemStack stack,
            int nameColor,
            int effectColor
    ) {
        DisciplineCore core = getStackDisciplineCore(stack);
        if (core == null) return;
        tooltip.add(Component.empty());
        tooltip.add(ModText.literal("核心 · " + core.displayName())
                .withStyle(style -> style.withColor(nameColor)));
        for (String line : core.description().split("\n")) {
            tooltip.add(ModText.literal(line)
                    .withStyle(style -> style.withColor(effectColor).withItalic(true)));
        }
        int mask = getStackDisciplineCoreSignetMask(stack);
        for (Skill skill : Skill.values()) {
            if (!skill.isDisciplineCoreSignet()
                    || skill.disciplineCore() != core
                    || (mask & skill.disciplineCoreSignetBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(skill.displayName() + levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            for (String line : skill.tooltipDescription(level).split("\n")) {
                tooltip.add(ModText.literal(line)
                        .withStyle(style -> style.withColor(effectColor).withItalic(true)));
            }
        }
    }

    public static void addGoldCoreTooltips(
            List<Component> tooltip,
            ItemStack stack,
            int nameColor,
            int effectColor
    ) {
        GoldCore core = getStackGoldCore(stack);
        if (core == null) return;
        tooltip.add(Component.empty());
        tooltip.add(ModText.literal("核心 · " + core.displayName())
                .withStyle(style -> style.withColor(nameColor)));
        SignetTooltipLines.addText(tooltip, core.description(), effectColor);
        int mask = getStackGoldCoreSignetMask(stack);
        for (Skill skill : Skill.values()) {
            if (!skill.isGoldCoreSignet() || skill.goldCore() != core
                    || (mask & skill.goldCoreSignetBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(skill.displayName() + levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addText(tooltip, skill.tooltipDescription(level), effectColor);
        }
    }

    public static void addUnlockedGoldSecondaryTooltips(
            List<Component> tooltip,
            ItemStack stack,
            int nameColor,
            int effectColor
    ) {
        int mask = getStackGoldSecondaryUnlockMask(stack);
        for (Skill skill : allSkillsFor(Signet.GOLD)) {
            if (!skill.isGoldSecondary() || (mask & skill.goldSecondaryBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(skill.displayName() + levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addText(tooltip, skill.tooltipDescription(level), effectColor);
        }
    }

    public static void addUnlockedHelixSecondaryTooltips(
            List<Component> tooltip,
            ItemStack stack,
            int nameColor,
            int effectColor
    ) {
        int mask = getStackHelixSecondaryUnlockMask(stack);
        for (Skill skill : allSkillsFor(Signet.HELIX)) {
            if (!skill.isHelixSecondary() || (mask & skill.helixSecondaryBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(skill.displayName() + levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addText(tooltip, skill.tooltipDescription(level), effectColor);
        }
    }

    public static void addHelixCoreTooltips(
            List<Component> tooltip,
            ItemStack stack,
            int nameColor,
            int effectColor
    ) {
        HelixCore core = getStackHelixCore(stack);
        if (core == null) return;
        tooltip.add(Component.empty());
        tooltip.add(ModText.literal("核心 · " + core.displayName())
                .withStyle(style -> style.withColor(nameColor)));
        SignetTooltipLines.addText(tooltip, core.description(), effectColor);
        int mask = getStackHelixCoreSignetMask(stack);
        for (Skill skill : Skill.values()) {
            if (!skill.isHelixCoreSignet() || skill.helixCore() != core
                    || (mask & skill.helixCoreSignetBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(skill.displayName() + levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addText(tooltip, skill.tooltipDescription(level), effectColor);
        }
    }

    public static void addUnlockedBodhiSecondaryTooltips(
            List<Component> tooltip, ItemStack stack, int nameColor, int effectColor
    ) {
        int mask = getStackBodhiSecondaryUnlockMask(stack);
        for (Skill skill : allSkillsFor(Signet.BODHI)) {
            if (!skill.isBodhiSecondary()
                    || (mask & skill.bodhiSecondaryBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .append(levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id() + "." + level, effectColor);
        }
    }

    public static void addUnlockedSetsuraSecondaryTooltips(
            List<Component> tooltip, ItemStack stack, int nameColor, int effectColor
    ) {
        int mask = getStackSetsuraSecondaryUnlockMask(stack);
        for (Skill skill : allSkillsFor(Signet.SETSURA)) {
            if (!skill.isSetsuraSecondary()
                    || (mask & skill.setsuraSecondaryBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .append(levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id() + "." + level, effectColor);
        }
    }

    public static void addUnlockedDaybreakSecondaryTooltips(
            List<Component> tooltip, ItemStack stack, int nameColor, int effectColor
    ) {
        int mask = getStackDaybreakSecondaryUnlockMask(stack);
        for (Skill skill : allSkillsFor(Signet.DAYBREAK)) {
            if (!skill.isDaybreakSecondary()
                    || (mask & skill.daybreakSecondaryBit()) == 0) continue;
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .append(levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id() + ".0", effectColor);
        }
    }

    public static void addDaybreakCoreTooltips(
            List<Component> tooltip, ItemStack stack, int nameColor, int effectColor
    ) {
        DaybreakCore core = getStackDaybreakCore(stack);
        if (core == null) return;
        tooltip.add(Component.empty());
        String coreId = core == DaybreakCore.UNFADING_LIGHT
                ? "daybreak_unfading_light" : "daybreak_lonely_night";
        tooltip.add(ModText.literal("核心 · ")
                .append(Component.translatable("item.lastsongofelysian." + coreId))
                .withStyle(style -> style.withColor(nameColor)));
        for (int line = 0; line < 2; line++) {
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + coreId + "." + line, effectColor);
        }
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.signet.extension_core_no_upgrade")
                .withStyle(style -> style.withColor(0xA8A8A8).withItalic(true)));
        int mask = getStackDaybreakCoreSignetMask(stack);
        for (Skill skill : Skill.values()) {
            if (!skill.isDaybreakCoreSignet() || skill.daybreakCore() != core
                    || (mask & skill.daybreakCoreSignetBit()) == 0) continue;
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .append(levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id() + ".0", effectColor);
        }
    }

    public static void addSetsuraCoreTooltips(
            List<Component> tooltip, ItemStack stack, int nameColor, int effectColor
    ) {
        SetsuraCore core = getStackSetsuraCore(stack);
        if (core == null) return;
        tooltip.add(Component.empty());
        String coreId = core == SetsuraCore.SAKURA_CURTAIN
                ? "setsura_sakura_curtain" : "setsura_night_yaksha";
        tooltip.add(ModText.literal("核心 · ")
                .append(Component.translatable("item.lastsongofelysian." + coreId))
                .withStyle(style -> style.withColor(nameColor)));
        for (int line = 0; line < 2; line++) {
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + coreId + "." + line, effectColor);
        }
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.signet.extension_core_no_upgrade")
                .withStyle(style -> style.withColor(0xA8A8A8).withItalic(true)));
        int mask = getStackSetsuraCoreSignetMask(stack);
        for (Skill skill : Skill.values()) {
            if (!skill.isSetsuraCoreSignet() || skill.setsuraCore() != core
                    || (mask & skill.setsuraCoreSignetBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .append(levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id() + "." + level, effectColor);
        }
    }

    public static void addBodhiCoreTooltips(
            List<Component> tooltip, ItemStack stack, int nameColor, int effectColor
    ) {
        BodhiCore core = getStackBodhiCore(stack);
        if (core == null) return;
        tooltip.add(Component.empty());
        String coreId = core == BodhiCore.TRUE_WORD ? "true_word" : "secret_word";
        tooltip.add(ModText.literal("核心 · ")
                .append(Component.translatable("item.lastsongofelysian.bodhi_" + coreId))
                .withStyle(style -> style.withColor(nameColor)));
        SignetTooltipLines.addTranslated(tooltip,
                "tooltip.lastsongofelysian.signet.bodhi_" + coreId + ".0", effectColor);
        SignetTooltipLines.addTranslated(tooltip,
                "tooltip.lastsongofelysian.signet.bodhi_" + coreId + ".1", effectColor);
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.signet.extension_core_no_upgrade")
                .withStyle(style -> style.withColor(0xA8A8A8).withItalic(true)));
        int mask = getStackBodhiCoreSignetMask(stack);
        for (Skill skill : Skill.values()) {
            if (!skill.isBodhiCoreSignet() || skill.bodhiCore() != core
                    || (mask & skill.bodhiCoreSignetBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .append(levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id() + "." + level, effectColor);
        }
    }

    public static void addUnlockedDecimationSecondaryTooltips(
            List<Component> tooltip, ItemStack stack, int nameColor, int effectColor
    ) {
        int mask = getStackDecimationSecondaryUnlockMask(stack);
        for (Skill skill : allSkillsFor(Signet.DECIMATION)) {
            if (!skill.isDecimationSecondary()
                    || (mask & skill.decimationSecondaryBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .append(levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id() + "." + level, effectColor);
        }
    }

    public static void addDecimationCoreTooltips(
            List<Component> tooltip, ItemStack stack, int nameColor, int effectColor
    ) {
        DecimationCore core = getStackDecimationCore(stack);
        if (core == null) return;
        tooltip.add(Component.empty());
        String coreId = core == DecimationCore.BLOOD_RELEASE
                ? "blood_release" : "blood_raiment";
        tooltip.add(ModText.literal("核心 · ")
                .append(Component.translatable("item.lastsongofelysian.decimation_" + coreId))
                .withStyle(style -> style.withColor(nameColor)));
        SignetTooltipLines.addTranslated(tooltip,
                "tooltip.lastsongofelysian.signet.decimation_" + coreId + ".0", effectColor);
        SignetTooltipLines.addTranslated(tooltip,
                "tooltip.lastsongofelysian.signet.decimation_" + coreId + ".1", effectColor);
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.signet.extension_core_no_upgrade")
                .withStyle(style -> style.withColor(0xA8A8A8).withItalic(true)));
        int mask = getStackDecimationCoreSignetMask(stack);
        for (Skill skill : Skill.values()) {
            if (!skill.isDecimationCoreSignet() || skill.decimationCore() != core
                    || (mask & skill.decimationCoreSignetBit()) == 0) continue;
            int level = getLevel(stack, skill);
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.lastsongofelysian." + skill.id())
                    .append(levelSuffix(stack, skill))
                    .withStyle(style -> style.withColor(nameColor)));
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + skill.id() + "." + level, effectColor);
        }
    }
}
