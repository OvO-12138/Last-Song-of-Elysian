package com.ovo.lastsongofelysian.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FenghuangDownItem extends Item {
    private static final int ORANGE = 0xFFAA00;

    public FenghuangDownItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        addPower(tooltip, "tooltip.lastsongofelysian.fenghuang_down.power_zero",
                "tooltip.lastsongofelysian.fenghuang_down.power_zero.description.1",
                "tooltip.lastsongofelysian.fenghuang_down.power_zero.description.2");
        tooltip.add(Component.empty());
        addPower(tooltip, "tooltip.lastsongofelysian.fenghuang_down.power_one",
                "tooltip.lastsongofelysian.fenghuang_down.power_one.description.1",
                "tooltip.lastsongofelysian.fenghuang_down.power_one.description.2",
                "tooltip.lastsongofelysian.fenghuang_down.power_one.description.3");
        tooltip.add(Component.empty());
        addPower(tooltip, "tooltip.lastsongofelysian.fenghuang_down.power_two",
                "tooltip.lastsongofelysian.fenghuang_down.power_two.description.1",
                "tooltip.lastsongofelysian.fenghuang_down.power_two.description.2");
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static void addPower(List<Component> tooltip, String title, String... description) {
        tooltip.add(Component.translatable(title).withStyle(ChatFormatting.RED));
        for (String line : description) {
            tooltip.add(Component.translatable(line).withStyle(style -> style.withColor(ORANGE)));
        }
    }
}
