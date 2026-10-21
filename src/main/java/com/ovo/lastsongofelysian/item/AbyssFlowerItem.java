package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.event.AbyssFlowerSystem;
import com.ovo.lastsongofelysian.util.AbyssFlowerEnergy;
import com.ovo.lastsongofelysian.util.AbyssFlowerRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AbyssFlowerItem extends SwordItem {
    public AbyssFlowerItem(Properties properties) {
        super(Tiers.NETHERITE, 5, -2.4F, properties);
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    public static boolean isWhite(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("AbyssFlowerWhite");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                AbyssFlowerSystem.cancelChant(serverPlayer);
                stack.getOrCreateTag().putBoolean("AbyssFlowerWhite", !isWhite(stack));
                AbyssFlowerEnergy.set(player, stack, AbyssFlowerEnergy.get(stack));
                player.displayClientMessage(Component.translatable(isWhite(stack)
                        ? "message.lastsongofelysian.abyss_flower.white"
                        : "message.lastsongofelysian.abyss_flower.black"), true);
            } else if (isWhite(stack)) {
                AbyssFlowerSystem.healTarget(serverPlayer);
            } else {
                AbyssFlowerSystem.releaseMist(serverPlayer);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.abyss_flower.warning").withStyle(ChatFormatting.RED));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.abyss_flower.black").withStyle(ChatFormatting.BLACK));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.abyss_flower.white").withStyle(ChatFormatting.WHITE));
        for (AbyssFlowerRules.Power power : AbyssFlowerRules.Power.values()) {
            ChatFormatting color = power == AbyssFlowerRules.Power.FIRST ? ChatFormatting.BLACK : ChatFormatting.WHITE;
            String key = "tooltip.lastsongofelysian.abyss_flower." + power.name().toLowerCase(java.util.Locale.ROOT);
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable(key).withStyle(color, ChatFormatting.BOLD));
            tooltip.add(Component.translatable(key + ".chant").withStyle(color));
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable(key + ".description").withStyle(color));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
