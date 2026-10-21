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

public class DeliveranceCoreSignetItem extends Item {

    private final SignetUpgradeData.DeliveranceCore core;

    public DeliveranceCoreSignetItem(
            Properties properties,
            SignetUpgradeData.DeliveranceCore core
    ) {
        super(properties);
        this.core = core;
    }

    public SignetUpgradeData.DeliveranceCore core() {
        return this.core;
    }

    @Override
    public Component getName(ItemStack stack) {
        return ModText.literal(this.core.displayName())
                .withStyle(style -> style.withColor(0x3F64D8));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(ModText.literal(this.core.description())
                .withStyle(style -> style.withColor(0x2244CC)));
        tooltip.add(ModText.literal("核心不可升级；需在螺旋工坊与救世刻印融合")
                .withStyle(style -> style.withColor(0x888888).withItalic(true)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
