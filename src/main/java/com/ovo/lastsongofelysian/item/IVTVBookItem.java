package com.ovo.lastsongofelysian.item;

import com.ovo.lastsongofelysian.client.ClientScreenHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class IVTVBookItem extends Item {
    public IVTVBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> ClientScreenHooks::openIVTVBookScreen
            );
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
