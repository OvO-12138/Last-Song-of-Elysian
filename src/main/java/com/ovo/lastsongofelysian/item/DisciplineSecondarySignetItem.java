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

public class DisciplineSecondarySignetItem extends Item {
    private final SignetUpgradeData.Skill skill;

    public DisciplineSecondarySignetItem(Properties properties, SignetUpgradeData.Skill skill) {
        super(properties);
        this.skill = skill;
    }

    @Override
    public Component getName(ItemStack stack) {
        return ModText.literal(this.skill.displayName())
                .withStyle(style -> style.withColor(0xD4A72C));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        for (String line : this.skill.tooltipDescription(0).split("\n")) {
            tooltip.add(ModText.literal(line)
                    .withStyle(style -> style.withColor(0xFFFFAA).withItalic(true)));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
