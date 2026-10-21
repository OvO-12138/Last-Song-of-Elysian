package com.ovo.lastsongofelysian.mixin;

import com.ovo.lastsongofelysian.client.FenghuangDownClientEffects;
import com.ovo.lastsongofelysian.client.AbyssFlowerClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelTimeStopMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void lastsongofelysian$freezeClientEntity(Entity entity, CallbackInfo callback) {
        if (FenghuangDownClientEffects.shouldFreeze(entity) || AbyssFlowerClient.shouldFreeze(entity)) {
            entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
            entity.setOldPosAndRot();
            callback.cancel();
        }
    }
}
