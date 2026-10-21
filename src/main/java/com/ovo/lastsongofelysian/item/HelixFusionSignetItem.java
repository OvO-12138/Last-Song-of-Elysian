package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.util.ModText;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.SignetTooltipLines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HelixFusionSignetItem extends Item {
    private final SignetUpgradeData.HelixCore core;
    private final SignetUpgradeData.Skill skill;

    public HelixFusionSignetItem(Properties properties, SignetUpgradeData.HelixCore core) {
        super(properties);
        this.core = core;
        this.skill = null;
    }

    public HelixFusionSignetItem(Properties properties, SignetUpgradeData.Skill skill) {
        super(properties);
        this.core = null;
        this.skill = skill;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack))
                .withStyle(style -> style.withColor(0xB88762));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        String description = this.core != null
                ? this.core.description() : this.skill.tooltipDescription(0);
        SignetTooltipLines.addText(tooltip, description, 0xCCAA88);
        if (this.core != null) {
            tooltip.add(ModText.literal("核心不可升级；需在螺旋工坊与螺旋刻印融合")
                    .withStyle(style -> style.withColor(0x888888).withItalic(true)));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
