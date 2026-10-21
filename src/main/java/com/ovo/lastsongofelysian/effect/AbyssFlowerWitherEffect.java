package com.ovo.lastsongofelysian.effect;

import com.ovo.lastsongofelysian.event.AbyssFlowerSystem;
import com.ovo.lastsongofelysian.registry.ModEffects;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class AbyssFlowerWitherEffect extends MobEffect {
    public AbyssFlowerWitherEffect() {
        super(MobEffectCategory.HARMFUL, 0x23142E);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return amplifier > 0 && duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            DamageSource source = AbyssFlowerSystem.witherSource(entity);
            if (PlayerControlledAllies.isElysianHero(entity)
                    || source.getEntity() instanceof net.minecraft.world.entity.player.Player player
                    && PlayerControlledAllies.isProtectedAlly(player, entity)) {
                entity.removeEffect(ModEffects.BLACK_ABYSS_WITHER.get());
                return;
            }
            entity.hurt(source, 5.0F * (amplifier + 1));
        }
    }
}
