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

public class DecimationFusionSignetItem extends Item {
    private final SignetUpgradeData.DecimationCore core;
    private final SignetUpgradeData.Skill skill;

    public DecimationFusionSignetItem(Properties properties, SignetUpgradeData.DecimationCore core) {
        super(properties);
        this.core = core;
        this.skill = null;
    }

    public DecimationFusionSignetItem(Properties properties, SignetUpgradeData.Skill skill) {
        super(properties);
        this.core = null;
        this.skill = skill;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack))
                .withStyle(style -> style.withColor(0xFF5555));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (this.core != null) {
            String id = this.core == SignetUpgradeData.DecimationCore.BLOOD_RELEASE
                    ? "blood_release" : "blood_raiment";
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet.decimation_" + id + ".0", 0xFF9999);
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet.decimation_" + id + ".1", 0xFF9999);
        } else {
            SignetTooltipLines.addTranslated(tooltip,
                    "tooltip.lastsongofelysian.signet." + this.skill.id() + ".0", 0xFF9999);
        }
        if (this.core != null) {
            tooltip.add(Component.translatable(
                            "tooltip.lastsongofelysian.signet.decimation_core_fusion")
                    .withStyle(style -> style.withColor(0x888888).withItalic(true)));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
