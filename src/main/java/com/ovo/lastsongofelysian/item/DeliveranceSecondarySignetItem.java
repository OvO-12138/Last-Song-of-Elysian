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

public class DeliveranceSecondarySignetItem extends Item {

    private final SignetUpgradeData.Skill skill;

    public DeliveranceSecondarySignetItem(Properties properties, SignetUpgradeData.Skill skill) {
        super(properties);
        this.skill = skill;
    }

    public SignetUpgradeData.Skill skill() {
        return this.skill;
    }

    @Override
    public Component getName(ItemStack stack) {
        return ModText.literal(this.skill.displayName())
                .withStyle(style -> style.withColor(0x3F64D8));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(ModText.literal(this.skill.description(0))
                .withStyle(style -> style.withColor(0x4444FF)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
