package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.util.ModText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class WhisperOfThePastItem extends BowItem {
    private static final Style OLD_BLESSING_STYLE = Style.EMPTY
            .withColor(TextColor.fromRgb(0xFFB6C1))
            .withItalic(true);

    private static final String[] OLD_BLESSING_NAMES = {
            "绚丽，如繁花初绽",
            "青春，如萌芽新生",
            "圣洁，如白鸟齐舞",
            "甘洌，如清泉落花",
            "嫣然，如少女回眸",
            "沉醉，如烛光余香",
            "幽雅，如淡云蔽月",
            "恬静，如夜露微明",
            "缱绻，如薰风卷雪",
            "光辉，如朝阳初升",
            "闪耀，如明星天坠",
            "隽永，如牧歌长鸣",
            "永恒，如往世折枝",
            "无暇，如神佑乐土",
            "纯美，如爱莉希雅"
    };

    public WhisperOfThePastItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        for (String name : OLD_BLESSING_NAMES) {
            tooltip.add(ModText.literal(name).setStyle(OLD_BLESSING_STYLE));
        }
    }
}
