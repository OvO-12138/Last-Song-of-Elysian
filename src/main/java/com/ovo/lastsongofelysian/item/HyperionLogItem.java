package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

import javax.annotation.Nullable;
import java.util.List;

public final class HyperionLogItem extends Item {
    public static final ResourceLocation BOOK_ID =
            new ResourceLocation(lastsongofelysian.MODID, "hyperion_log");

    public HyperionLogItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            PatchouliAPI.get().openBookGUI(serverPlayer, BOOK_ID);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.lastsongofelysian.hyperion_log.open")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
