package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.util.ModText;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisciplineCoreSignetItem extends Item {
    private final SignetUpgradeData.DisciplineCore core;

    public DisciplineCoreSignetItem(Properties properties, SignetUpgradeData.DisciplineCore core) {
        super(properties);
        this.core = core;
    }

    @Override
    public Component getName(ItemStack stack) {
        return ModText.literal(this.core.displayName())
                .withStyle(style -> style.withColor(0xD4A72C));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        for (String line : this.core.description().split("\n")) {
            tooltip.add(ModText.literal(line).withStyle(style -> style.withColor(0xFFAA00)));
        }
        tooltip.add(ModText.literal("核心不可升级；需在螺旋工坊与戒律刻印融合")
                .withStyle(style -> style.withColor(0x888888).withItalic(true)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
