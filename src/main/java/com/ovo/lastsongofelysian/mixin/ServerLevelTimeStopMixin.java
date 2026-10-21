package com.ovo.lastsongofelysian.mixin;

import com.ovo.lastsongofelysian.event.FenghuangDownSystem;
import com.ovo.lastsongofelysian.event.AbyssFlowerSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelTimeStopMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void lastsongofelysian$freezeEntity(Entity entity, CallbackInfo callback) {
        if (FenghuangDownSystem.shouldFreeze(entity) || AbyssFlowerSystem.shouldFreeze(entity)) {
            entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
            entity.setOldPosAndRot();
            callback.cancel();
        }
    }

    @Inject(method = "tickBlock", at = @At("HEAD"), cancellable = true)
    private void lastsongofelysian$freezeScheduledBlock(
            BlockPos pos,
            Block block,
            CallbackInfo callback
    ) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (FenghuangDownSystem.shouldFreeze(level, pos) || AbyssFlowerSystem.shouldFreeze(level, pos)) {
            level.scheduleTick(pos, block, 1);
            callback.cancel();
        }
    }

    @Inject(method = "tickFluid", at = @At("HEAD"), cancellable = true)
    private void lastsongofelysian$freezeScheduledFluid(
            BlockPos pos,
            Fluid fluid,
            CallbackInfo callback
    ) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (FenghuangDownSystem.shouldFreeze(level, pos) || AbyssFlowerSystem.shouldFreeze(level, pos)) {
            level.scheduleTick(pos, fluid, 1);
            callback.cancel();
        }
    }

    @Inject(method = "tickChunk", at = @At("HEAD"), cancellable = true)
    private void lastsongofelysian$freezeRandomTicks(
            LevelChunk chunk,
            int randomTickSpeed,
            CallbackInfo callback
    ) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (FenghuangDownSystem.shouldFreezeChunk(level, chunk) || AbyssFlowerSystem.shouldFreezeChunk(level, chunk)) {
            callback.cancel();
        }
    }
}
