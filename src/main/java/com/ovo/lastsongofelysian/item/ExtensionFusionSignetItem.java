package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import com.ovo.lastsongofelysian.util.SignetTooltipLines;
import com.ovo.lastsongofelysian.util.ExtensionSignetColors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ExtensionFusionSignetItem extends Item {
    private final SignetUpgradeData.ExtensionCore core;
    private final SignetUpgradeData.Skill skill;

    public ExtensionFusionSignetItem(Properties properties, SignetUpgradeData.ExtensionCore core) {
        super(properties);
        this.core = core;
        this.skill = null;
    }

    public ExtensionFusionSignetItem(Properties properties, SignetUpgradeData.Skill skill) {
        super(properties);
        this.core = null;
        this.skill = skill;
    }

    public SignetUpgradeData.ExtensionCore core() { return this.core; }
    public SignetUpgradeData.Skill skill() { return this.skill; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack))
                .withStyle(style -> style.withColor(ExtensionSignetColors.name(signet(), id())));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        String id = id();
        SignetTooltipLines.addTranslated(
                tooltip, "tooltip.lastsongofelysian.signet." + id,
                ExtensionSignetColors.effect(signet(), id));
        if (this.core != null) {
            tooltip.add(Component.translatable(
                            "tooltip.lastsongofelysian.signet.extension_core_fusion")
                    .withStyle(style -> style.withColor(0xA8A8A8).withItalic(true)));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private SignetUpgradeData.Signet signet() {
        return this.core != null ? this.core.signet() : this.skill.signet();
    }

    private String id() {
        return this.core != null
                ? "extension_core_" + this.core.name().toLowerCase()
                : this.skill.id();
    }
}
