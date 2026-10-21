package com.ovo.lastsongofelysian.mixin;

import com.ovo.lastsongofelysian.util.AbyssFlowerRemainsData;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"))
    private static void lastsongofelysian$mergeRemains(ItemEntity target, ItemStack targetStack,
                                                        ItemEntity source, ItemStack sourceStack,
                                                        CallbackInfo callbackInfo) {
        AbyssFlowerRemainsData.merge(target, source);
    }
}
