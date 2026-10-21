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

public class BodhiFusionSignetItem extends Item {
    private final SignetUpgradeData.BodhiCore core;
    private final SignetUpgradeData.Skill skill;

    public BodhiFusionSignetItem(Properties properties, SignetUpgradeData.BodhiCore core) {
        super(properties);
        this.core = core;
        this.skill = null;
    }

    public BodhiFusionSignetItem(Properties properties, SignetUpgradeData.Skill skill) {
        super(properties);
        this.core = null;
        this.skill = skill;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack))
                .withStyle(style -> style.withColor(0x66CFAF));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (this.core != null) {
            String id = this.core == SignetUpgradeData.BodhiCore.TRUE_WORD
                    ? "true_word" : "secret_word";
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet.bodhi_" + id + ".0", 0xAAF5E0);
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet.bodhi_" + id + ".1", 0xAAF5E0);
            tooltip.add(Component.translatable(
                    "tooltip.lastsongofelysian.signet.bodhi_core_fusion")
                    .withStyle(style -> style.withColor(0x888888).withItalic(true)));
        } else {
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + this.skill.id() + ".0", 0xAAF5E0);
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
