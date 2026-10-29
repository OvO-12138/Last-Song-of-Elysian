package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.event.CocoonCoreProgress;
import com.ovo.lastsongofelysian.event.CocoonTrialProgress;
import com.ovo.lastsongofelysian.client.ClientCocoonData;
import com.ovo.lastsongofelysian.client.ClientCorrosionCurseData;
import com.ovo.lastsongofelysian.util.CocoonTrialPalette;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class CocoonOfFinalityItem extends Item implements ICurioItem {

    private static final String NBT_TOOLTIP_CORE_STAGE = "CocoonTooltipCoreStage";

    private static final String[][] TRIAL_TEXT = {
            {"理 · 失序的真理", "成功完成50次合成，并完成5次最高档附魔。失败的合成和附魔不计数"},
            {"空 · 虚空的漠视", "被空之诅咒随机传送后，在30秒内击败诅咒召唤的敌人；累计完成10次"},
            {"雷 · 天罚的见证", "在雨天或雷暴中承受5次诅咒雷击且不死亡，并在雷暴期间击败20只敌对生物"},
            {"风 · 无风的嘶哑", "在自然生命回复被封锁时，通过药水、金苹果、信标等方式累计恢复200点生命"},
            {"冰 · 冻结的深渊", "不使用水下呼吸效果，在寒冷群系水下累计停留120秒，并击败20只溺尸"},
            {"死 · 死生的边界", "在生命不高于20%的情况下累计生存360秒，同时击败20只敌对生物；使用不死图腾会重置本次连续进度"},
            {"炎 · 永燃的薪炎", "处于燃烧状态累计90秒，并击败20个正在燃烧的敌对生物"},
            {"识 · 幻觉的彼岸", "在反胃、失明或黑暗效果生效时击败25只敌对生物"},
            {"岩 · 坚韧的壁垒", "穿戴头盔、胸甲、护腿、靴子承受10次爆炸伤害；TNT、苦力怕、末影水晶等造成实际爆炸伤害时计数。脱下或损坏任一件防具会清空进度"},
            {"支配 · 断裂的人偶", "在经验获取、攻击和护甲仍受压制时，累计提升30级，并击败100只敌对生物"},
            {"约束 · 束缚的行路", "在挖掘疲劳生效时破坏128个石质或矿石方块；牛奶、急迫和信标会暂停计数"},
            {"侵蚀 · 认知的崩解", "获得侵蚀之核心后，在任意维度连续击败30个敌方单位；死亡会重置进度"}
    };

    private static final String[] TRIAL_BLESSINGS = {
            "祝福：附魔等级提高10%",
            "祝福：清除诅咒召唤物，跨维度后获得10秒抗性提升",
            "祝福：移动速度和攻击速度提高15%",
            "祝福：治疗效果提高20%",
            "祝福：水中及寒冷群系移动速度提高10%，免疫冰冻",
            "祝福：最大生命值提高20%，低生命时受到伤害降低20%",
            "祝福：免疫火焰伤害，对燃烧目标造成伤害提高20%",
            "祝福：增益时间延长20%，负面效果时间缩短20%",
            "祝福：受到的爆炸伤害降低20%",
            "祝福：攻击伤害和护甲提高10%",
            "祝福：获得急迫效果",
            "祝福：经验获取提高30%；佩戴终焉之茧时造成的伤害提高15%；完成全部12项终焉试炼后，崩坏能侵蚀度清零"
    };

    private static final String[][] PERMANENT_CORROSION_CURSE_TEXT = {
            {"Ⅰ · 古神低语", "偶尔听见并不存在的脚步、低语、咆哮与爆炸声。"},
            {"Ⅱ · 屏幕故障", "侵蚀纹路覆盖视野，并周期性隐藏部分状态栏。"},
            {"Ⅲ · 刻印反噬", "每永久获得一种不同刻印，所有侵蚀诅咒效果增强10%；重复刻印不计。"},
            {"Ⅳ · 战斗侵蚀", "战斗中每秒增加0.12%侵蚀；命中每0.25秒增加0.05%，释放武器必杀技增加1%。"},
            {"Ⅴ · 刻印压制", "侵蚀达到50%、75%、90%时，战斗刻印加成分别保留90%、75%、55%。"},
            {"Ⅵ · 回复削弱", "侵蚀越高，受到的治疗效果越低；受伤叠加伤口并进一步削弱治疗。"},
            {"Ⅶ · 行动受限", "周期性受到移动、攻击或行动能力削弱，攻击与物品技能可能失控。"},
            {"Ⅷ · 虚数共鸣", "侵蚀达到75%时敌人造成伤害提高15%、受到伤害降低20%。"},
            {"Ⅸ · 深度共鸣", "侵蚀达到90%时敌人造成伤害提高25%、受到伤害降低30%。"},
            {"Ⅹ · 精神错乱", "持续战斗可能生成敌对侵蚀镜像。"},
            {"Ⅺ · 工具报废", "武器、工具与护甲可能额外损失耐久。"},
            {"Ⅻ · 终末脉冲", "战斗中每45秒爆发，造成最大生命值6%加2点伤害，并使治疗降低50%持续5秒。"},
            {"ⅩⅢ · 自我崩解", "侵蚀满溢后40秒内最大生命降低至50%，并每2秒受到4点魔法伤害。"}
    };

    public CocoonOfFinalityItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canUnequip(
            String identifier,
            LivingEntity livingEntity,
            ItemStack stack
    ) {

        return livingEntity instanceof Player player && player.isCreative();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player
                && !player.level().isClientSide()
                && player.tickCount % 20 == 0) {
            safeSyncTooltipStage(stack, player);
        }
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity entity,
            int slotId,
            boolean isSelected
    ) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide()
                && entity instanceof Player player
                && player.tickCount % 20 == 0) {
            safeSyncTooltipStage(stack, player);
        }
    }

    private static void safeSyncTooltipStage(
            ItemStack stack,
            Player player
    ) {
        try {
            syncTooltipStage(stack, player);
        } catch (Throwable throwable) {
            System.err.println(
                    "[LastSongOfElysian] Cocoon tooltip sync failed:"
            );
            throwable.printStackTrace(System.err);
        }
    }

    private static void syncTooltipStage(ItemStack stack, Player player) {
        int stage = clampStage(CocoonCoreProgress.getStage(player));
        CompoundTag tag = stack.getOrCreateTag();

        if (tag.getInt(NBT_TOOLTIP_CORE_STAGE) != stage) {
            tag.putInt(NBT_TOOLTIP_CORE_STAGE, stage);
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {

        int stage = clampStage(ClientCocoonData.getCoreStage());
        float multiplier = getMultiplier(stage);

        tooltip.add(
                ModText.literal("命运曾向世界提问")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
        );
        tooltip.add(
                ModText.literal("于是，英雄给出了回答")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
        );
        tooltip.add(
                ModText.literal("跨越漫长的轮回")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
        );
        tooltip.add(
                ModText.literal("创造属于每一个人的故事")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
        );

        tooltip.add(Component.empty());

        if (ClientCocoonData.isReversed()) {
            addReversedBlessingTooltip(tooltip);
            return;
        }

        tooltip.add(
                ModText.literal("律者核心进度：")
                        .withStyle(ChatFormatting.GRAY)
                        .append(
                                ModText.literal(stage + "/12")
                                        .withStyle(
                                                stage >= CocoonCoreProgress.MAX_STAGE
                                                        ? ChatFormatting.GREEN
                                                        : ChatFormatting.GOLD,
                                                ChatFormatting.BOLD
                                        )
                        )
        );

        tooltip.add(
                ModText.literal("侵蚀强度：")
                        .withStyle(ChatFormatting.GRAY)
                        .append(
                                ModText.literal("×" + formatNumber(multiplier))
                                        .withStyle(
                                                ChatFormatting.RED,
                                                ChatFormatting.BOLD
                                        )
                        )
        );

        if (ClientCocoonData.areTrialsUnlocked()) {
            int completed = Integer.bitCount(ClientCocoonData.getCompletedTrialMask());
            tooltip.add(
                    ModText.literal("终焉试炼：")
                            .withStyle(ChatFormatting.GRAY)
                            .append(ModText.literal(completed + "/12")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
            );
        }

        if (stage >= CocoonCoreProgress.MAX_STAGE) {
            tooltip.add(ModText.literal(
                    ClientCocoonData.areTrialsUnlocked()
                            ? "十二项终焉试炼等待完成"
                            : "等待始源之核心开启终焉试炼"
            ).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        }

        boolean shiftDown = Screen.hasShiftDown();
        boolean controlDown = Screen.hasControlDown();
        boolean corrosionCurseAcquired =
                ClientCorrosionCurseData.isAcquired();

        if (!shiftDown && !controlDown) {
            tooltip.add(Component.empty());
            tooltip.add(
                    ModText.literal(ClientCocoonData.areTrialsUnlocked()
                                    ? "按住 [Shift] 查看终焉试炼"
                                    : "按住 [Shift] 查看正常诅咒")
                            .withStyle(ChatFormatting.GRAY)
            );

            if (corrosionCurseAcquired) {
                tooltip.add(
                        ModText.literal("按住 [Ctrl] 查看侵蚀诅咒")
                                .withStyle(ChatFormatting.DARK_PURPLE)
                );
            }
            return;
        }

        if (shiftDown) {
            tooltip.add(Component.empty());
            if (ClientCocoonData.areTrialsUnlocked()) {
                addTrialTooltip(tooltip);
            } else {
                addReasonTooltip(tooltip, stage);
                addVoidTooltip(tooltip, stage);
                addThunderTooltip(tooltip, stage);
                addWindTooltip(tooltip, stage);
                addIceTooltip(tooltip, stage);
                addDeathTooltip(tooltip, stage);
                addFireTooltip(tooltip, stage);
                addSentienceTooltip(tooltip, stage);
                addEarthTooltip(tooltip, stage);
                addDominanceTooltip(tooltip, stage);
                addBindingTooltip(tooltip, stage);
                if (!ClientCocoonData.hasCore(CocoonTrialProgress.CORROSION)) {
                    addCorrosionTooltip(tooltip, stage);
                }
            }
        }

        if (controlDown && corrosionCurseAcquired) {
            addPermanentCorrosionCurseTooltip(tooltip);
        }
    }

    private static void addTrialTooltip(List<Component> tooltip) {
        tooltip.add(ModText.literal("终焉试炼")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        int mask = ClientCocoonData.getCompletedTrialMask();
        for (int index = 0; index < TRIAL_TEXT.length; index++) {
            boolean complete = (mask & (1 << index)) != 0;
            tooltip.add(Component.empty());
            tooltip.add(ModText.literal(TRIAL_TEXT[index][0])
                    .withStyle(CocoonTrialPalette.color(index), ChatFormatting.BOLD)
                    .append(ModText.literal(complete ? "  [已完成]" : "  [未完成]")
                            .withStyle(complete ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY,
                                    ChatFormatting.BOLD)));
            tooltip.add(ModText.literal(TRIAL_TEXT[index][1])
                    .withStyle(CocoonTrialPalette.color(index)));
            if (index == CocoonTrialProgress.THUNDER) {
                tooltip.add(ModText.literal("进度：诅咒雷击 " + ClientCocoonData.getThunderStrikes()
                                + "/5；雷暴期间击败敌对生物 " + ClientCocoonData.getThunderKills() + "/20")
                        .withStyle(ChatFormatting.GRAY));
            } else if (index == CocoonTrialProgress.EARTH) {
                tooltip.add(ModText.literal("防具：头盔、胸甲、护腿、靴子；爆炸必须实际对你造成伤害。进度："
                                + ClientCocoonData.getEarthExplosions() + "/10")
                        .withStyle(ChatFormatting.GRAY));
            }
            if (complete) {
                tooltip.add(ModText.literal(TRIAL_BLESSINGS[index])
                        .withStyle(CocoonTrialPalette.color(index)));
            }
        }
    }

    private static void addReversedBlessingTooltip(List<Component> tooltip) {
        if (!Screen.hasShiftDown()) {
            tooltip.add(
                    ModText.literal("按住 [Shift] 查看终焉祝福")
                            .withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        tooltip.add(
                ModText.literal("终焉祝福")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
        );
        for (int index = 0; index < TRIAL_BLESSINGS.length; index++) {
            String trialName = TRIAL_TEXT[index][0];
            int separator = trialName.indexOf(" ·");
            String coreName = separator < 0 ? trialName : trialName.substring(0, separator);
            addReversedBlessingLine(tooltip, index,
                    coreName + "：" + TRIAL_BLESSINGS[index].replace("祝福：", ""), index > 0);
        }
    }

    private static void addReversedBlessingLine(
            List<Component> tooltip,
            int trial,
            String text,
            boolean addSpacing
    ) {
        if (addSpacing) tooltip.add(Component.empty());
        tooltip.add(ModText.literal(text).withStyle(CocoonTrialPalette.color(trial)));
    }

    private static void addPermanentCorrosionCurseTooltip(
            List<Component> tooltip
    ) {
        tooltip.add(Component.empty());
        tooltip.add(
                ModText.literal("侵蚀诅咒")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
        );

        for (String[] entry : PERMANENT_CORROSION_CURSE_TEXT) {
            tooltip.add(
                    ModText.literal(entry[0])
                            .withStyle(ChatFormatting.RED)
            );
            tooltip.add(
                    ModText.literal("  " + entry[1])
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private static void addReasonTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.REASON);
        addHeader(
                tooltip,
                "理之诅咒",
                CocoonTrialPalette.color(CocoonTrialProgress.REASON),
                restored
        );

        tooltip.add(
                ModText.literal(
                        "附魔失败 " + percent(scaledChance(stage, 0.25F))
                                + "；合成失败 " + percent(scaledChance(stage, 0.08F))
                ).withStyle(ChatFormatting.BLUE)
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.REASON,
                "理之核心",
                "失败时催化并召唤怪物攻击时 "
                        + percent(scaledChance(stage, 0.025F))
                        + " 概率催化"
        );
        tooltip.add(Component.empty());
    }

    private static void addVoidTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.VOID);
        addHeader(
                tooltip,
                "空之诅咒",
                CocoonTrialPalette.color(CocoonTrialProgress.VOID),
                restored
        );

        tooltip.add(
                ModText.literal(
                        "进入其他维度时有 "
                                + percent(scaledChance(stage, 0.05F))
                                + " 概率坠入虚空"
                ).withStyle(CocoonTrialPalette.color(CocoonTrialProgress.VOID))
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.VOID,
                "空之核心",
                "攻击或受击时有 "
                        + percent(scaledChance(stage, 0.05F))
                        + " 概率随机传送并召怪"
        );
        tooltip.add(Component.empty());
    }

    private static void addThunderTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.THUNDER);
        addHeader(
                tooltip,
                "雷之诅咒",
                CocoonTrialPalette.color(CocoonTrialProgress.THUNDER),
                restored
        );

        tooltip.add(
                ModText.literal(
                        "移动速度 -" + percent(scaledPercent(stage, 0.20F))
                                + "；攻击速度 -" + percent(scaledPercent(stage, 0.30F))
                ).withStyle(ChatFormatting.LIGHT_PURPLE)
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.THUNDER,
                "雷之核心",
                "每5秒：雨天 "
                        + percent(scaledChance(stage, 0.10F))
                        + "／雷暴 "
                        + percent(scaledChance(stage, 0.20F))
                        + " 概率遭雷击"
        );
        tooltip.add(Component.empty());
    }

    private static void addWindTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.WIND);
        addHeader(
                tooltip,
                "风之诅咒",
                CocoonTrialPalette.color(CocoonTrialProgress.WIND),
                restored
        );

        tooltip.add(
                ModText.literal(
                        "治疗效率 -" + percent(scaledPercent(stage, 0.25F))
                                + "；饱食消耗 +" + percent(0.15F * getMultiplier(stage))
                ).withStyle(ChatFormatting.GREEN)
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.WIND,
                "风之核心",
                "无法自然恢复生命"
        );
        tooltip.add(Component.empty());
    }

    private static void addIceTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.ICE);
        addHeader(
                tooltip,
                "冰之诅咒",
                CocoonTrialPalette.color(CocoonTrialProgress.ICE),
                restored
        );

        tooltip.add(
                ModText.literal(
                        "水下氧气消耗约 +"
                                + percent(scaledPercent(stage, 0.25F))
                                + "；寒冷群系移速 -"
                                + percent(scaledPercent(stage, 0.10F))
                ).withStyle(ChatFormatting.AQUA)
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.ICE,
                "冰之核心",
                "水下获得虚弱；寒冷群系每5秒受到 "
                        + formatNumber(getMultiplier(stage))
                        + " 点冻结伤害"
        );
        tooltip.add(Component.empty());
    }

    private static void addDeathTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.DEATH);
        addHeader(
                tooltip,
                "死之诅咒",
                CocoonTrialPalette.color(CocoonTrialProgress.DEATH),
                restored
        );

        tooltip.add(
                ModText.literal(
                        "最大生命值 -" + percent(scaledPercent(stage, 0.25F))
                                + "；生命低于20%时受伤 +"
                                + percent(scaledPercent(stage, 0.10F))
                ).withStyle(CocoonTrialPalette.color(CocoonTrialProgress.DEATH))
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.DEATH,
                "死之核心",
                "生命不高于1%时，下一次致命伤害强制结算"
        );
        tooltip.add(Component.empty());
    }

    private static void addFireTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.FIRE);
        addHeader(
                tooltip,
                "炎之诅咒",
                ChatFormatting.RED,
                restored
        );

        tooltip.add(
                ModText.literal(
                        "火焰伤害 +" + percent(scaledPercent(stage, 0.20F))
                                + "；燃烧时间 +" + percent(scaledPercent(stage, 0.25F))
                ).withStyle(ChatFormatting.RED)
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.FIRE,
                "炎之核心",
                "潜行踩岩浆块仍受伤；燃烧时每秒额外受到 "
                        + formatNumber(getMultiplier(stage))
                        + " 点伤害"
        );
        tooltip.add(Component.empty());
    }

    private static void addSentienceTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.SENTIENCE);
        addHeader(
                tooltip,
                "识之诅咒",
                ChatFormatting.YELLOW,
                restored
        );

        tooltip.add(
                ModText.literal(
                        "正面效果时间 -" + percent(scaledPercent(stage, 0.15F))
                                + "；负面效果时间 +"
                                + percent(scaledPercent(stage, 0.15F))
                ).withStyle(ChatFormatting.YELLOW)
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.SENTIENCE,
                "识之核心",
                "每30～60秒生成幻觉，并短暂获得反胃、失明或黑暗"
        );
        tooltip.add(Component.empty());
    }

    private static void addEarthTooltip(
            List<Component> tooltip,
            int stage
    ) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.EARTH);

        addHeader(
                tooltip,
                "岩之诅咒",
                ChatFormatting.GOLD,
                restored
        );

        tooltip.add(
                ModText.literal(
                        "受击时有 "
                                + percent(
                                scaledChance(
                                        stage,
                                        0.20F
                                )
                        )
                                + " 概率额外损耗防具；受到击退 +"
                                + percent(
                                scaledPercent(
                                        stage,
                                        0.15F
                                )
                        )
                ).withStyle(ChatFormatting.GOLD)
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.EARTH,
                "岩之核心",
                percent(
                        scaledChance(
                                stage,
                                0.03F
                        )
                )
                        + " 概率使随机防具损失剩余耐久的 "
                        + percent(
                        Math.min(
                                0.50F,
                                scaledPercent(
                                        stage,
                                        0.20F
                                )
                        )
                )
        );

        tooltip.add(
                ModText.literal(
                        "无法破坏的装备不受影响；装备不会被直接摧毁，至少保留1点耐久"
                ).withStyle(ChatFormatting.DARK_GRAY)
        );

        tooltip.add(Component.empty());
    }

    private static void addDominanceTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.DOMINANCE);
        addHeader(
                tooltip,
                "支配之诅咒",
                CocoonTrialPalette.color(CocoonTrialProgress.DOMINANCE),
                restored
        );

        tooltip.add(
                ModText.literal(
                        "经验获取 -80%；攻击伤害 -"
                                + percent(scaledPercent(stage, 0.10F))
                                + "；护甲 -"
                                + percent(scaledPercent(stage, 0.10F))
                ).withStyle(CocoonTrialPalette.color(CocoonTrialProgress.DOMINANCE))
        );

        tooltip.add(
                ModText.literal(
                        "受击时有 " + percent(scaledChance(stage, 0.15F))
                                + " 概率额外损耗主手装备"
                ).withStyle(CocoonTrialPalette.color(CocoonTrialProgress.DOMINANCE))

        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.DOMINANCE,
                "支配之核心",
                "每20秒有 " + percent(scaledChance(stage, 0.10F))
                        + " 概率定身 "
                        + formatNumber(scaleDurationSeconds(stage, 1.5F))
                        + " 秒"
        );
        tooltip.add(Component.empty());
    }

    private static void addBindingTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.BINDING);
        addHeader(
                tooltip,
                "约束之诅咒",
                CocoonTrialPalette.color(CocoonTrialProgress.BINDING),
                restored
        );

        tooltip.add(
                ModText.literal(
                        "崩坏能容量 -"
                                + percent(scaledPercent(stage, 0.20F))
                ).withStyle(CocoonTrialPalette.color(CocoonTrialProgress.BINDING))
        );

        if (!restored) {
            tooltip.add(
                    ModText.literal(
                            "每30秒获得挖掘疲劳 I，持续 "
                                    + formatNumber(scaleDurationSeconds(stage, 5.0F))
                                    + " 秒"
                    ).withStyle(CocoonTrialPalette.color(CocoonTrialProgress.BINDING))
            );
        }

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.BINDING,
                "约束之核心",
                "永久挖掘疲劳 I；破坏方块时有 "
                        + percent(scaledChance(stage, 0.50F))
                        + " 概率额外损耗工具"
        );
        tooltip.add(Component.empty());
    }

    private static void addCorrosionTooltip(List<Component> tooltip, int stage) {
        boolean restored = ClientCocoonData.hasCore(CocoonTrialProgress.CORROSION);
        addHeader(
                tooltip,
                "侵蚀之诅咒",
                ChatFormatting.LIGHT_PURPLE,
                restored
        );

        float damageReduction = scaledPercent(stage, 0.15F);
        if (restored) {
            damageReduction = Math.min(0.80F, damageReduction + 0.17F);
        }

        tooltip.add(
                ModText.literal(
                        "造成伤害 -" + percent(damageReduction)
                                + "；侵蚀增长与满溢效果 ×"
                                + formatNumber(getMultiplier(stage))
                ).withStyle(CocoonTrialPalette.color(CocoonTrialProgress.CORROSION))
        );

        tooltip.add(
                ModText.literal(
                        "侵蚀满溢时每秒受到 "
                                + formatNumber(5.0F * getMultiplier(stage))
                                + " 点伤害"
                ).withStyle(CocoonTrialPalette.color(CocoonTrialProgress.CORROSION))
        );

        addRestoredLine(
                tooltip,
                restored,
                CocoonTrialProgress.CORROSION,
                "侵蚀之核心",
                "每5秒随机隐藏一个属性栏"
        );
    }

    private static void addHeader(
            List<Component> tooltip,
            String name,
            ChatFormatting color,
            boolean restored
    ) {
        MutableComponent line = ModText.literal(name)
                .withStyle(color, ChatFormatting.BOLD);

        line.append(
                ModText.literal(
                        restored
                                ? "  [完整机制已恢复]"
                                : "  [削弱状态]"
                ).withStyle(color)
        );

        tooltip.add(line);
    }

    private static void addRestoredLine(
            List<Component> tooltip,
            boolean restored,
            int trial,
            String coreName,
            String effectText
    ) {
        ChatFormatting color = CocoonTrialPalette.color(trial);
        if (restored) {
            tooltip.add(
                    ModText.literal("已恢复：")
                            .withStyle(color)
                            .append(
                                    ModText.literal(effectText)
                                            .withStyle(color)
                            )
            );
        } else {
            tooltip.add(
                ModText.literal("获得" + coreName + "后恢复：")
                            .withStyle(color)
                            .append(
                                    ModText.literal(effectText)
                                            .withStyle(color)
                            )
            );
        }
    }

    private static int getTooltipStage(ItemStack stack) {
        if (!stack.hasTag()) {
            return 0;
        }

        return clampStage(
                stack.getTag().getInt(NBT_TOOLTIP_CORE_STAGE)
        );
    }

    private static int clampStage(int stage) {
        return Math.max(
                0,
                Math.min(CocoonCoreProgress.MAX_STAGE, stage)
        );
    }

    private static float getMultiplier(int stage) {
        return 1.0F + clampStage(stage) * 0.10F;
    }

    private static float scaledChance(int stage, float baseChance) {
        return Math.min(
                0.95F,
                baseChance * getMultiplier(stage)
        );
    }

    private static float scaledPercent(int stage, float basePercent) {
        return Math.min(
                0.90F,
                basePercent * getMultiplier(stage)
        );
    }

    private static float scaleDurationSeconds(
            int stage,
            float baseSeconds
    ) {
        return baseSeconds * Math.min(3.0F, getMultiplier(stage));
    }

    private static String percent(float value) {
        return formatNumber(value * 100.0F) + "%";
    }

    private static String formatNumber(float value) {
        float rounded = Math.round(value * 10.0F) / 10.0F;

        if (Math.abs(rounded - Math.round(rounded)) < 0.001F) {
            return Integer.toString(Math.round(rounded));
        }

        return String.format(Locale.ROOT, "%.1f", rounded);
    }
}
