package com.ovo.lastsongofelysian.mixin;

import com.ovo.lastsongofelysian.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "snownee.jade.addon.vanilla.StatusEffectsProvider", remap = false)
public abstract class JadeStatusEffectsProviderMixin {

    @Inject(method = "getEffectName", at = @At("HEAD"), cancellable = true, remap = false)
    private static void lastsongofelysian$showLacerationStacks(
            MobEffectInstance instance,
            CallbackInfoReturnable<Component> cir
    ) {
        if (instance.getEffect() == ModEffects.LACERATION.get()
                && instance.getAmplifier() >= 10) {
            cir.setReturnValue(Component.translatable(
                    "effect.lastsongofelysian.laceration.stacks",
                    instance.getAmplifier() + 1
            ));
        }
    }
}
