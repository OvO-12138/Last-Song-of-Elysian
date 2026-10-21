package com.ovo.lastsongofelysian.mixin;

import com.ovo.lastsongofelysian.event.FenghuangDownSystem;
import com.ovo.lastsongofelysian.event.AbyssFlowerSystem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Level.class)
public abstract class LevelBlockEntityTimeStopMixin {
    @Redirect(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/TickingBlockEntity;tick()V"
            )
    )
    private void lastsongofelysian$freezeBlockEntity(TickingBlockEntity ticker) {
        Level level = (Level) (Object) this;
        if (!FenghuangDownSystem.shouldFreeze(level, ticker.getPos()) && !AbyssFlowerSystem.shouldFreeze(level, ticker.getPos())) {
            ticker.tick();
        }
    }
}
