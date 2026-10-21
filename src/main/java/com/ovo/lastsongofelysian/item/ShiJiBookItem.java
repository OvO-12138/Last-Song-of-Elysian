package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.util.ModText;
import com.ovo.lastsongofelysian.util.ModAdvancements;

import com.ovo.lastsongofelysian.client.ClientScreenHooks;
import com.ovo.lastsongofelysian.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ShiJiBookItem extends Item {

    private static final String VOID_OWNER = "ShiJiVoidOwner";

    public ShiJiBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFireResistant() {
        return true;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        entity.clearFire();
        entity.setInvulnerable(true);
        entity.setUnlimitedLifetime();

        if (!entity.level().isClientSide()
                && entity.getY() < entity.level().getMinBuildHeight() - 64) {
            ServerPlayer player = getVoidOwner(entity);
            if (player != null) {
                ItemStack result = new ItemStack(ModItems.IVTV_BOOK.get());
                if (!player.addItem(result)) {
                    player.drop(result, false);
                }
                entity.discard();
                return true;
            }
        }

        return false;
    }

    public static void rememberVoidOwner(ItemEntity entity, Player player) {
        entity.getPersistentData().putUUID(VOID_OWNER, player.getUUID());
    }

    private static ServerPlayer getVoidOwner(ItemEntity entity) {
        if (entity.getOwner() instanceof ServerPlayer player) {
            return player;
        }
        if (!entity.getPersistentData().hasUUID(VOID_OWNER) || entity.getServer() == null) {
            return null;
        }
        UUID ownerId = entity.getPersistentData().getUUID(VOID_OWNER);
        return entity.getServer().getPlayerList().getPlayer(ownerId);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> ClientScreenHooks::openShiJiScreen
            );
        } else {
            ModAdvancements.award(player, "open_history_book");
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(ModText.literal("岁月史书")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));

        tooltip.add(ModText.literal("by刑部尚书/钦天监首领/流光忆庭首领/无漏净子/司马迁/构史爵/浮黎/灰暗之手/遐蝶/虚构史学家")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        tooltip.add(ModText.literal("此书不可焚，不可毁，不可存，不可遗。")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(ModText.literal("")
                .withStyle(ChatFormatting.DARK_RED));

        tooltip.add(ModText.literal("在此")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(ModText.literal("我向他们许下承诺")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(ModText.literal("史")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(ModText.literal("一定会战胜人类")
                .withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
