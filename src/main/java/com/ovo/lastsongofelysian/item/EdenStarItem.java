package com.ovo.lastsongofelysian.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EdenStarItem extends Item {
    private static final int BROWN = 0x8B5A2B;
    private static final int YELLOW = 0xFFD75A;

    public EdenStarItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addSection(tooltip, "zeroth", 4);
        addSection(tooltip, "first", 2);
        addSection(tooltip, "third", 2);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static void addSection(List<Component> tooltip, String power, int lines) {
        if (!tooltip.isEmpty()) tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.eden_star." + power)
                .withStyle(style -> style.withColor(BROWN)));
        for (int line = 1; line <= lines; line++) {
            tooltip.add(Component.translatable(
                    "tooltip.lastsongofelysian.eden_star." + power + ".description." + line
            ).withStyle(style -> style.withColor(YELLOW)));
        }
    }
}
