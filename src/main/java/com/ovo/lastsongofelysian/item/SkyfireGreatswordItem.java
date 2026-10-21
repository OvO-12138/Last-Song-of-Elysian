package com.ovo.lastsongofelysian.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SkyfireGreatswordItem extends SwordItem {
    private static final int ORANGE = 0xFFAA00;

    public SkyfireGreatswordItem(Properties properties) {
        super(Tiers.NETHERITE, 20, -2.8F, properties);
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.skyfire_greatsword.power_zero")
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.skyfire_greatsword.power_zero.description.1")
                .withStyle(style -> style.withColor(ORANGE)));
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.skyfire_greatsword.power_zero.description.2")
                .withStyle(style -> style.withColor(ORANGE)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
