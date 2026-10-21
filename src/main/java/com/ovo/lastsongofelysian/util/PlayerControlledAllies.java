package com.ovo.lastsongofelysian.util;

import com.ovo.lastsongofelysian.entity.ElysianHeroNpcEntity;
import com.ovo.lastsongofelysian.entity.PardofelisMerchantEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayerControlledAllies {
    private static final String[] OWNER_KEYS = {
            "Owner", "OwnerUUID", "owner", "ownerUUID", "Master", "MasterUUID",
            "master", "masterUUID", "TameOwner", "Summoner", "SummonerUUID"
    };
    private static final String[] OWNER_METHOD_NAMES = {
            "getOwnerUUID", "getOwnerUuid", "getOwner", "getMaster",
            "getThrower", "getThrowerUUID"
    };
    private static final ClassValue<Method[]> OWNER_METHODS = new ClassValue<>() {
        @Override
        protected Method[] computeValue(Class<?> type) {
            List<Method> methods = new ArrayList<>();
            for (String name : OWNER_METHOD_NAMES) {
                try {
                    methods.add(type.getMethod(name));
                } catch (NoSuchMethodException | SecurityException ignored) {
                }
            }
            return methods.toArray(Method[]::new);
        }
    };

    private PlayerControlledAllies() {
    }

    public static boolean isProtectedAlly(Player player, Entity entity) {
        if (player == null || entity == null || entity == player) return entity == player;
        if (entity.isAlliedTo(player) || player.isAlliedTo(entity)) return true;
        if (player.getTeam() != null && player.getTeam() == entity.getTeam()) return true;
        if (entity instanceof TamableAnimal tamable
                && player.getUUID().equals(tamable.getOwnerUUID())) return true;
        UUID reflectedOwner = reflectedOwnerUuid(entity);
        if (player.getUUID().equals(reflectedOwner)) return true;
        CompoundTag data = entity.getPersistentData();
        for (String key : OWNER_KEYS) {
            if (data.hasUUID(key) && player.getUUID().equals(data.getUUID(key))) return true;
            if (data.contains(key)) {
                try {
                    if (player.getUUID().toString().equalsIgnoreCase(data.getString(key))) return true;
                } catch (RuntimeException ignored) {
                }
            }
        }
        return false;
    }

    public static boolean isValidEnemy(Player player, LivingEntity entity) {
        return entity != null && entity.isAlive() && entity != player
                && !isProtectedEffectTarget(player, entity);
    }

    public static boolean isElysianHero(Entity entity) {
        return entity instanceof ElysianHeroNpcEntity
                || entity instanceof PardofelisMerchantEntity;
    }

    public static boolean isProtectedEffectTarget(Player player, Entity entity) {
        return isElysianHero(entity) || isProtectedAlly(player, entity);
    }

    public static boolean isSignetTimeStopExempt(Entity entity,
                                                 Iterable<? extends Player> players) {
        return isSignetTimeStopExempt(entity, players, 0);
    }

    private static boolean isSignetTimeStopExempt(Entity entity,
                                                  Iterable<? extends Player> players,
                                                  int ownerDepth) {
        if (entity == null) return false;
        if (entity instanceof Player || isElysianHero(entity)) return true;

        UUID owner = reflectedOwnerUuid(entity);
        UUID tameOwner = entity instanceof TamableAnimal tamable
                ? tamable.getOwnerUUID() : null;
        CompoundTag data = entity.getPersistentData();
        for (Player player : players) {
            UUID playerId = player.getUUID();
            if (playerId.equals(owner) || playerId.equals(tameOwner)
                    || entity.isAlliedTo(player) || player.isAlliedTo(entity)
                    || player.getTeam() != null && player.getTeam() == entity.getTeam()) {
                return true;
            }
            for (String key : OWNER_KEYS) {
                if (data.hasUUID(key) && playerId.equals(data.getUUID(key))) return true;
                if (data.contains(key) && playerId.toString().equalsIgnoreCase(data.getString(key))) {
                    return true;
                }
            }
        }

        if (ownerDepth < 3 && owner != null && entity.level() instanceof ServerLevel level) {
            Entity ownerEntity = level.getEntity(owner);
            if (ownerEntity != null && ownerEntity != entity) {
                return isSignetTimeStopExempt(ownerEntity, players, ownerDepth + 1);
            }
        }
        return false;
    }

    public static boolean hurt(Player player, LivingEntity target,
                               DamageSource source, float amount) {
        return isValidEnemy(player, target) && amount > 0.0F && target.hurt(source, amount);
    }

    public static Player findController(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof Player player) return player;
        if (!(entity.level() instanceof ServerLevel level)) return null;
        for (Player player : level.players()) {
            if (isProtectedAlly(player, entity)) return player;
        }
        return null;
    }

    private static UUID reflectedOwnerUuid(Entity entity) {
        if (entity instanceof Projectile projectile && projectile.getOwner() != null) {
            return projectile.getOwner().getUUID();
        }
        for (Method method : OWNER_METHODS.get(entity.getClass())) {
            try {
                Object value = method.invoke(entity);
                if (value instanceof UUID uuid) return uuid;
                if (value instanceof Entity owner) return owner.getUUID();
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return null;
    }
}
