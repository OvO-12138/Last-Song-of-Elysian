package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.SignetTooltipLines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetsuraFusionSignetItem extends Item {
    private final SignetUpgradeData.SetsuraCore core;
    private final SignetUpgradeData.Skill skill;

    public SetsuraFusionSignetItem(Properties properties, SignetUpgradeData.SetsuraCore core) {
        super(properties);
        this.core = core;
        this.skill = null;
    }

    public SetsuraFusionSignetItem(Properties properties, SignetUpgradeData.Skill skill) {
        super(properties);
        this.core = null;
        this.skill = skill;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack))
                .withStyle(style -> style.withColor(0x55CFE1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        int effectColor = 0xCCFFFF;
        if (this.core != null) {
            String id = this.core == SignetUpgradeData.SetsuraCore.SAKURA_CURTAIN
                    ? "setsura_sakura_curtain" : "setsura_night_yaksha";
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + id + ".0", effectColor);
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + id + ".1", effectColor);
            tooltip.add(Component.translatable(
                    "tooltip.lastsongofelysian.signet.setsura_core_fusion")
                    .withStyle(style -> style.withColor(0x888888).withItalic(true)));
        } else {
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + this.skill.id() + ".0", effectColor);
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
