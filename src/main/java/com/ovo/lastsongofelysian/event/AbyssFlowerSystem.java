package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.entity.CorrosionMirrorEntity;
import com.ovo.lastsongofelysian.item.AbyssFlowerItem;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.network.AbyssFlowerStatePacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import com.ovo.lastsongofelysian.registry.ModEffects;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.AbyssFlowerEnergy;
import com.ovo.lastsongofelysian.util.AbyssFlowerRemainsData;
import com.ovo.lastsongofelysian.util.AbyssFlowerResurrectionLedger;
import com.ovo.lastsongofelysian.util.AbyssFlowerRules;
import com.ovo.lastsongofelysian.util.PlayerControlledAllies;
import com.ovo.lastsongofelysian.util.ElysianHeroExplosionProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.*;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AbyssFlowerSystem {
    public static final ResourceKey<DamageType> WITHER_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(lastsongofelysian.MODID, "black_abyss_wither"));
    private static final UUID HEALTH_MODIFIER = UUID.fromString("b58cfad6-b07a-49b1-ac60-6a1bb5cde8a7");
    private static final String LOST_HEALTH = "AbyssFlowerLostMaxHealth";
    private static final String RESURRECTION_ID = "AbyssFlowerResurrectionId";
    private static final String CASTER = "AbyssFlowerWitherCaster";
    private static final String HITS = "AbyssFlowerBlackHits";
    private static final String WEAPON_TOKEN = "AbyssFlowerWeaponToken";
    private static final Map<UUID, Chant> CHANTS = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<Mist>> MISTS = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<Detonation>> DETONATIONS = new HashMap<>();

    private AbyssFlowerSystem() {
    }

    private static CompoundTag permanentData(LivingEntity entity) {
        CompoundTag root = entity.getPersistentData();
        if (!(entity instanceof Player)) return root;
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static UUID token(ItemStack weapon) {
        CompoundTag tag = weapon.getOrCreateTag();
        if (!tag.hasUUID(WEAPON_TOKEN)) tag.putUUID(WEAPON_TOKEN, UUID.randomUUID());
        return tag.getUUID(WEAPON_TOKEN);
    }

    private static String cooldownKey(AbyssFlowerRules.Power power) {
        return "AbyssFlowerCooldown" + power.name();
    }

    private static void message(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("message.lastsongofelysian.abyss_flower." + key, args), true);
    }

    private static boolean ready(ServerPlayer player, ItemStack weapon, AbyssFlowerRules.Power power) {
        long remaining = permanentData(player).getLong(cooldownKey(power)) - player.level().getGameTime();
        if (remaining > 0L) {
            message(player, "cooldown", Component.translatable("tooltip.lastsongofelysian.abyss_flower."
                    + power.name().toLowerCase(Locale.ROOT)), (remaining + 19L) / 20L);
            return false;
        }
        if (AbyssFlowerEnergy.get(weapon) < power.cost) {
            message(player, "energy", AbyssFlowerEnergy.get(weapon), power.cost);
            return false;
        }
        if (power == AbyssFlowerRules.Power.FIRST && permanentData(player).getInt(HITS) < AbyssFlowerRules.REQUIRED_HITS) {
            message(player, "hits", permanentData(player).getInt(HITS), AbyssFlowerRules.REQUIRED_HITS);
            return false;
        }
        return true;
    }

    public static void beginChant(ServerPlayer player, AbyssFlowerRules.Power power) {
        ItemStack weapon = AbyssFlowerEnergy.findHeld(player);
        if (weapon.isEmpty() || CHANTS.containsKey(player.getUUID()) || !ready(player, weapon, power)) return;
        LivingEntity target = power == AbyssFlowerRules.Power.FIRST ? lookTarget(player, 16.0D) : null;
        if (power == AbyssFlowerRules.Power.FIRST && (target == null || isCompanion(player, target))) {
            message(player, "target");
            return;
        }
        Chant chant = new Chant(player, power, token(weapon), AbyssFlowerItem.isWhite(weapon),
                target == null ? null : target.getUUID());
        CHANTS.put(player.getUUID(), chant);
        sendState(player, chant, true);
    }

    private static void sendState(ServerPlayer player, Chant chant, boolean active) {
        ModNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> chant.dimension),
                new AbyssFlowerStatePacket(player.getId(), chant.power, active,
                        chant.center.x, chant.center.y, chant.center.z,
                        active ? (int) Math.max(1L, chant.endTick - player.level().getGameTime()) : 0));
    }

    public static void cancelChant(ServerPlayer player) {
        Chant chant = CHANTS.remove(player.getUUID());
        if (chant != null) sendState(player, chant, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        Chant chant = CHANTS.get(player.getUUID());
        if (chant == null) return;
        event.setCanceled(true);
        if (!valid(player, chant)) {
            cancelChant(player);
            return;
        }
        if (!chant.power.matches(event.getRawText())) {
            message(player, "chant_mismatch");
            return;
        }
        ItemStack weapon = AbyssFlowerEnergy.findHeld(player);
        cancelChant(player);
        if (!ready(player, weapon, chant.power)) return;
        switch (chant.power) {
            case ZEROTH -> releaseZeroth(player, weapon);
            case FIRST -> releaseFirst(player, weapon, chant.target);
            case SECOND -> releaseSecond(player, weapon);
        }
    }

    private static boolean valid(ServerPlayer player, Chant chant) {
        ItemStack weapon = AbyssFlowerEnergy.findHeld(player);
        return player.isAlive() && player.level().dimension().equals(chant.dimension)
                && player.level().getGameTime() < chant.endTick && !weapon.isEmpty()
                && token(weapon).equals(chant.weaponToken) && AbyssFlowerItem.isWhite(weapon) == chant.white;
    }

    private static void spend(ServerPlayer player, ItemStack weapon, AbyssFlowerRules.Power power) {
        AbyssFlowerEnergy.set(player, weapon, AbyssFlowerEnergy.get(weapon) - power.cost);
        if (power.cooldown > 0) permanentData(player).putLong(cooldownKey(power), player.level().getGameTime() + power.cooldown);
        if (power == AbyssFlowerRules.Power.FIRST) {
            SignetEffects.onWeaponSkillActivated(player);
        }
    }

    private static AABB resurrectionArea(Vec3 center) {
        return new AABB(center.x - 2.5D, center.y - 2.5D, center.z - 2.5D,
                center.x + 2.5D, center.y + 2.5D, center.z + 2.5D);
    }

    private static void releaseZeroth(ServerPlayer player, ItemStack weapon) {
        spend(player, weapon, AbyssFlowerRules.Power.ZEROTH);
        ServerLevel level = player.serverLevel();
        AABB area = resurrectionArea(player.position());
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            entity.setHealth(entity.getMaxHealth());
        }
        resurrectDrops(level, area);
        SignetEffects.addCorruptionByPercent(player, 0.50F);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 150, 2.5D, 1.2D, 2.5D, 0.04D);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.8F, 1.3F);
    }

    private static void releaseFirst(ServerPlayer player, ItemStack weapon, UUID targetId) {
        Entity found = targetId == null ? null : player.serverLevel().getEntity(targetId);
        if (!(found instanceof LivingEntity target) || !target.isAlive()
                || target.distanceToSqr(player) > 256.0D || isCompanion(player, target)) {
            message(player, "target");
            return;
        }
        spend(player, weapon, AbyssFlowerRules.Power.FIRST);
        permanentData(player).putInt(HITS, 0);
        AbyssFlowerEnergy.add(player, weapon, 30);
        target.getPersistentData().putUUID(CASTER, player.getUUID());
        target.addEffect(new MobEffectInstance(ModEffects.BLACK_ABYSS_WITHER.get(), AbyssFlowerRules.FIRST_DURATION, 39));
        Vec3 position = target.position();
        DETONATIONS.computeIfAbsent(player.level().dimension(), key -> new ArrayList<>())
                .add(new Detonation(player.getUUID(), targetId, position, player.level().getGameTime() + AbyssFlowerRules.FIRST_DURATION));
        target.hurt(witherSource(target), AbyssFlowerRules.FIRST_DAMAGE);
        player.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, position.x, position.y + 1, position.z,
                100, 1, 1.5D, 1, 0.1D);
    }

    private static void releaseSecond(ServerPlayer player, ItemStack weapon) {
        spend(player, weapon, AbyssFlowerRules.Power.SECOND);
        double radius = AbyssFlowerRules.CLEANSING_RADIUS;
        for (LivingEntity entity : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius), e -> e.isAlive() && e.distanceToSqr(player) <= radius * radius)) {
            if (entity instanceof Player other) {
                SignetEffects.reduceCorruptionByPercent(other, 0.20F);
            } else {
                CompoundTag data = entity.getPersistentData();
                if (data.contains("CorruptionEnergy", Tag.TAG_ANY_NUMERIC)) {
                    data.putFloat("CorruptionEnergy", Math.max(0.0F, data.getFloat("CorruptionEnergy") - 2000.0F));
                }
            }
        }
        player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(),
                100, radius, 1, radius, 0.03D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.5F, 1.6F);
    }

    public static void releaseMist(ServerPlayer player) {
        if (CHANTS.containsKey(player.getUUID())) return;
        ItemStack weapon = AbyssFlowerEnergy.findHeld(player);
        if (weapon.isEmpty()) return;
        long now = player.level().getGameTime();
        long remaining = permanentData(player).getLong("AbyssFlowerMistCooldown") - now;
        if (remaining > 0) {
            message(player, "mist_cooldown", (remaining + 19L) / 20L);
            return;
        }
        permanentData(player).putLong("AbyssFlowerMistCooldown", now + AbyssFlowerRules.MIST_COOLDOWN);
        MISTS.computeIfAbsent(player.level().dimension(), key -> new ArrayList<>())
                .add(new Mist(player.getUUID(), token(weapon), player.position(), now + AbyssFlowerRules.MIST_DURATION));
        SignetEffects.onWeaponSkillActivated(player);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1, 0.7F);
    }

    public static void healTarget(ServerPlayer player) {
        if (CHANTS.containsKey(player.getUUID())) return;
        LivingEntity target = lookTarget(player, 6.0D);
        if (target == null) {
            message(player, "target");
            return;
        }
        healTarget(player, target);
    }

    private static void healTarget(ServerPlayer player, LivingEntity target) {
        if (CHANTS.containsKey(player.getUUID()) || !target.isAlive() || target.distanceToSqr(player) > 36.0D) return;
        target.removeEffect(ModEffects.BLACK_ABYSS_WITHER.get());
        target.getPersistentData().remove(CASTER);
        float amount = AbyssFlowerRules.healingBudget(player.getHealth(), target.getMaxHealth() - target.getHealth());
        if (amount <= 0.0F) return;
        float before = target.getHealth();
        target.heal(amount);
        float restored = Math.max(0.0F, Math.min(amount, target.getHealth() - before));
        if (target.getHealth() > before + restored) target.setHealth(before + restored);
        if (restored > 0.0F) player.setHealth(Math.max(1.0F, player.getHealth() - restored));
        player.serverLevel().sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + 1, target.getZ(), 10, 0.4D, 0.6D, 0.4D, 0);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getItemStack().is(ModItems.ABYSS_FLOWER.get())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack weapon = event.getItemStack();
        if (!weapon.is(ModItems.ABYSS_FLOWER.get())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isShiftKeyDown()) weapon.getItem().use(player.level(), player, event.getHand());
        else if (AbyssFlowerItem.isWhite(weapon) && event.getTarget() instanceof LivingEntity target) healTarget(player, target);
        else if (!AbyssFlowerItem.isWhite(weapon)) releaseMist(player);
    }

    private static LivingEntity lookTarget(ServerPlayer player, double reach) {
        Vec3 start = player.getEyePosition();
        Vec3 end = player.pick(reach, 0, false).getLocation();
        LivingEntity best = null;
        double distance = start.distanceToSqr(end);
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                new AABB(start, end).inflate(1.0D), e -> e != player && e.isAlive() && !e.isSpectator())) {
            Optional<Vec3> hit = target.getBoundingBox().inflate(0.3D).clip(start, end);
            if (hit.isPresent() && start.distanceToSqr(hit.get()) <= distance) {
                distance = start.distanceToSqr(hit.get());
                best = target;
            }
        }
        return best;
    }

    private static boolean isCompanion(Player caster, Entity entity) {
        if (entity == caster || PlayerControlledAllies.isProtectedEffectTarget(caster, entity)) return true;
        if (entity instanceof OwnableEntity ownable && caster.getUUID().equals(ownable.getOwnerUUID())) return true;
        if (entity instanceof Projectile projectile && projectile.getOwner() == caster) return true;
        CompoundTag data = entity.getPersistentData();
        return data.getBoolean(CorrosionMirrorEntity.NBT_MIRROR_FRIENDLY)
                && data.hasUUID(CorrosionMirrorEntity.NBT_MIRROR_OWNER)
                && caster.getUUID().equals(data.getUUID(CorrosionMirrorEntity.NBT_MIRROR_OWNER));
    }

    public static DamageSource witherSource(LivingEntity target) {
        ServerLevel level = (ServerLevel) target.level();
        CompoundTag data = target.getPersistentData();
        Entity caster = data.hasUUID(CASTER) ? level.getEntity(data.getUUID(CASTER)) : null;
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(WITHER_DAMAGE), caster);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlackAttack(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getDirectEntity() instanceof ServerPlayer player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (!weapon.is(ModItems.ABYSS_FLOWER.get()) || AbyssFlowerItem.isWhite(weapon)) return;
        if (CHANTS.containsKey(player.getUUID())) {
            event.setAmount(0.0F);
            return;
        }
        LivingEntity target = event.getEntity();
        if (PlayerControlledAllies.isElysianHero(target)) return;
        double damage = event.getAmount();
        CompoundTag data = permanentData(target);
        data.putDouble(LOST_HEALTH, data.getDouble(LOST_HEALTH) + damage);
        applyHealthLoss(target, false);
        permanentData(player).putInt(HITS, Math.min(AbyssFlowerRules.REQUIRED_HITS, permanentData(player).getInt(HITS) + 1));
        AbyssFlowerEnergy.addFromAttack(player, weapon, 2);
    }

    private static void applyHealthLoss(LivingEntity entity) {
        applyHealthLoss(entity, true);
    }

    private static void applyHealthLoss(LivingEntity entity, boolean clampHealth) {
        double loss = permanentData(entity).getDouble(LOST_HEALTH);
        if (loss <= 0.0D) return;
        AttributeInstance attribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        AttributeModifier previous = attribute.getModifier(HEALTH_MODIFIER);
        double factor = previous == null ? 1.0D : 1.0D + previous.getAmount();
        double unrestricted = factor <= 0.0D ? attribute.getBaseValue() : attribute.getValue() / factor;
        double amount = AbyssFlowerRules.remainingMaxHealth(unrestricted, loss) / Math.max(1.0D, unrestricted) - 1.0D;
        if (previous == null || Math.abs(previous.getAmount() - amount) > 0.00000001D) {
            attribute.removeModifier(HEALTH_MODIFIER);
            attribute.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER, "abyss_flower_irreversible_loss", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (clampHealth && entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof LivingEntity entity) applyHealthLoss(entity);
        if (event.getEntity() instanceof ItemEntity item) AbyssFlowerRemainsData.migrateLegacy(item);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && !(entity instanceof Player) && entity.tickCount % 20 == 0) applyHealthLoss(entity);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag old = permanentData(event.getOriginal());
        CompoundTag data = permanentData(event.getEntity());
        if (old.contains(LOST_HEALTH)) data.putDouble(LOST_HEALTH, old.getDouble(LOST_HEALTH));
        for (AbyssFlowerRules.Power power : AbyssFlowerRules.Power.values()) {
            String key = cooldownKey(power);
            if (old.contains(key)) data.putLong(key, old.getLong(key));
        }
        if (old.contains(HITS)) data.putInt(HITS, old.getInt(HITS));
        if (old.contains("AbyssFlowerMistCooldown")) data.putLong("AbyssFlowerMistCooldown", old.getLong("AbyssFlowerMistCooldown"));
        applyHealthLoss(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.getDrops().isEmpty()) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        CompoundTag saved = new CompoundTag();
        entity.saveWithoutId(saved);
        CompoundTag details = new CompoundTag();
        for (String key : List.of("CustomName", "CustomNameVisible", "Attributes", "Age", "Variant", "Color", "Owner", "Tame", "Sitting",
                "CollarColor", "CatType", "RabbitType", "Type", "Size", "FoxType", "Sheared", "MirrorOwner", "MirrorFriendly", "MirrorExpire")) {
            if (saved.contains(key)) details.put(key, saved.get(key).copy());
        }
        CompoundTag remains = new CompoundTag();
        remains.putUUID("Id", entity instanceof Player ? UUID.randomUUID() : entity.getPersistentData().hasUUID(RESURRECTION_ID)
                ? entity.getPersistentData().getUUID(RESURRECTION_ID) : entity.getUUID());
        if (entity instanceof Player) remains.putUUID("Player", entity.getUUID());
        remains.putString("Type", EntityType.getKey(entity.getType()).toString());
        remains.put("Details", details);
        remains.putDouble("Loss", permanentData(entity).getDouble(LOST_HEALTH));
        for (ItemEntity drop : event.getDrops()) {
            if (!drop.getItem().isEmpty()) AbyssFlowerRemainsData.attach(drop, remains);
        }
    }

    private static void resurrectDrops(ServerLevel level, AABB area) {
        AbyssFlowerResurrectionLedger ledger = AbyssFlowerResurrectionLedger.get(level);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area, e -> e.isAlive() && !e.getItem().isEmpty())) {
            ItemStack stack = item.getItem();
            CompoundTag remains = AbyssFlowerRemainsData.firstAvailable(item, ledger);
            if (remains == null && AbyssFlowerRemainsData.hasTrackedHistory(item)) continue;
            EntityType<?> type;
            UUID id;
            if (remains != null) {
                if (!remains.hasUUID("Id") || ledger.isRevived(remains.getUUID("Id"))) continue;
                id = remains.getUUID("Id");
                ResourceLocation name = ResourceLocation.tryParse(remains.getString("Type"));
                type = name == null ? null : ForgeRegistries.ENTITY_TYPES.getValue(name);
            } else {
                type = typeFromDrop(stack.getItem());
                id = item.getUUID();
            }
            if (type == null) continue;
            if (type == EntityType.PLAYER) {
                ServerPlayer deadPlayer = playerFromRemains(level, stack, remains);
                if (deadPlayer == null || deadPlayer.isAlive() || ledger.isRevived(id)) continue;
                ServerPlayer revived = level.getServer().getPlayerList().respawn(deadPlayer, false);
                revived.connection.player = revived;
                revived.teleportTo(level, item.getX(), item.getY(), item.getZ(), deadPlayer.getYRot(), deadPlayer.getXRot());
                revived.setHealth(revived.getMaxHealth());
                revived.deathTime = 0;
                revived.clearFire();
                revived.setAirSupply(revived.getMaxAirSupply());
                revived.invulnerableTime = 60;
                ledger.markRevived(id);
                if (remains != null) AbyssFlowerRemainsData.consume(item, id);
                stack.shrink(1);
                if (stack.isEmpty()) item.discard(); else item.setItem(stack.copy());
                continue;
            }
            Entity created = type.create(level);
            if (!(created instanceof LivingEntity revived)) continue;
            CompoundTag details = remains == null ? new CompoundTag() : remains.getCompound("Details").copy();
            details.remove("UUID");
            revived.load(details);
            revived.getPersistentData().putUUID(RESURRECTION_ID, id);
            if (remains != null) {
                permanentData(revived).putDouble(LOST_HEALTH, remains.getDouble("Loss"));
                applyHealthLoss(revived);
            }
            revived.moveTo(item.getX(), item.getY(), item.getZ(), item.getYRot(), 0);
            revived.setHealth(revived.getMaxHealth());
            if (!level.getWorldBorder().isWithinBounds(revived.getBoundingBox()) || !level.noCollision(revived)) continue;
            if (revived instanceof Mob mob) mob.setPersistenceRequired();
            if (!level.addFreshEntity(revived)) continue;
            ledger.markRevived(id);
            if (remains != null) AbyssFlowerRemainsData.consume(item, id);
            stack.shrink(1);
            if (stack.isEmpty()) item.discard(); else item.setItem(stack.copy());
        }
    }

    private static EntityType<?> typeFromDrop(Item item) {
        if (item == Items.PLAYER_HEAD) return EntityType.PLAYER;
        if (item == Items.ZOMBIE_HEAD || item == Items.ROTTEN_FLESH) return EntityType.ZOMBIE;
        if (item == Items.SKELETON_SKULL || item == Items.BONE) return EntityType.SKELETON;
        if (item == Items.WITHER_SKELETON_SKULL) return EntityType.WITHER_SKELETON;
        if (item == Items.CREEPER_HEAD || item == Items.GUNPOWDER) return EntityType.CREEPER;
        if (item == Items.PIGLIN_HEAD) return EntityType.PIGLIN;
        if (item == Items.DRAGON_HEAD) return EntityType.ENDER_DRAGON;
        if (item == Items.STRING || item == Items.SPIDER_EYE) return EntityType.SPIDER;
        if (item == Items.ENDER_PEARL) return EntityType.ENDERMAN;
        if (item == Items.BEEF || item == Items.COOKED_BEEF || item == Items.LEATHER) return EntityType.COW;
        if (item == Items.PORKCHOP || item == Items.COOKED_PORKCHOP) return EntityType.PIG;
        if (item == Items.MUTTON || item == Items.COOKED_MUTTON || item == Items.WHITE_WOOL) return EntityType.SHEEP;
        if (item == Items.CHICKEN || item == Items.COOKED_CHICKEN || item == Items.FEATHER) return EntityType.CHICKEN;
        if (item == Items.RABBIT || item == Items.COOKED_RABBIT || item == Items.RABBIT_FOOT || item == Items.RABBIT_HIDE) return EntityType.RABBIT;
        if (item == Items.COD || item == Items.COOKED_COD) return EntityType.COD;
        if (item == Items.SALMON || item == Items.COOKED_SALMON) return EntityType.SALMON;
        if (item == Items.PUFFERFISH) return EntityType.PUFFERFISH;
        if (item == Items.TROPICAL_FISH) return EntityType.TROPICAL_FISH;
        if (item == Items.SLIME_BALL) return EntityType.SLIME;
        if (item == Items.MAGMA_CREAM) return EntityType.MAGMA_CUBE;
        if (item == Items.BLAZE_ROD) return EntityType.BLAZE;
        if (item == Items.GHAST_TEAR) return EntityType.GHAST;
        if (item == Items.INK_SAC) return EntityType.SQUID;
        if (item == Items.GLOW_INK_SAC) return EntityType.GLOW_SQUID;
        if (item == Items.PRISMARINE_SHARD || item == Items.PRISMARINE_CRYSTALS) return EntityType.GUARDIAN;
        if (item == Items.SHULKER_SHELL) return EntityType.SHULKER;
        if (item == Items.PHANTOM_MEMBRANE) return EntityType.PHANTOM;
        return null;
    }

    private static ServerPlayer playerFromRemains(ServerLevel level, ItemStack stack, CompoundTag remains) {
        if (remains != null && remains.hasUUID("Player")) return level.getServer().getPlayerList().getPlayer(remains.getUUID("Player"));
        if (!stack.hasTag()) return null;
        CompoundTag tag = stack.getTag();
        if (tag.contains("SkullOwner", Tag.TAG_STRING)) return level.getServer().getPlayerList().getPlayerByName(tag.getString("SkullOwner"));
        CompoundTag owner = tag.getCompound("SkullOwner");
        return owner.hasUUID("Id") ? level.getServer().getPlayerList().getPlayer(owner.getUUID("Id"))
                : level.getServer().getPlayerList().getPlayerByName(owner.getString("Name"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (entity instanceof ServerPlayer deadPlayer) cancelChant(deadPlayer);
        List<Mist> mists = MISTS.getOrDefault(level.dimension(), List.of());
        Set<UUID> credited = new HashSet<>();
        for (Mist mist : mists) {
            Entity found = level.getEntity(mist.caster);
            if (!(found instanceof ServerPlayer caster) || level.getGameTime() >= mist.endTick
                    || mist.center.distanceToSqr(entity.position()) > AbyssFlowerRules.MIST_RADIUS * AbyssFlowerRules.MIST_RADIUS
                    || isCompanion(caster, entity) || !credited.add(caster.getUUID())) continue;
            ItemStack weapon = AbyssFlowerEnergy.find(caster);
            if (!weapon.isEmpty() && token(weapon).equals(mist.weaponToken)) AbyssFlowerEnergy.add(caster, weapon, 3);
        }
    }

    public static boolean shouldFreeze(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return false;
        for (Chant chant : CHANTS.values()) {
            if (chant.dimension.equals(level.dimension()) && chant.endTick > level.getGameTime()
                    && !chant.caster.equals(entity.getUUID())
                    && chant.center.distanceToSqr(entity.position()) <= AbyssFlowerRules.CHANT_RADIUS * AbyssFlowerRules.CHANT_RADIUS) return true;
        }
        return false;
    }

    public static boolean shouldFreeze(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        for (Chant chant : CHANTS.values()) {
            if (chant.dimension.equals(level.dimension()) && chant.endTick > serverLevel.getGameTime()
                    && chant.center.distanceToSqr(Vec3.atCenterOf(pos)) <= AbyssFlowerRules.CHANT_RADIUS * AbyssFlowerRules.CHANT_RADIUS) return true;
        }
        return false;
    }

    public static boolean shouldFreezeChunk(ServerLevel level, LevelChunk chunk) {
        for (Chant chant : CHANTS.values()) {
            if (!chant.dimension.equals(level.dimension()) || chant.endTick <= level.getGameTime()) continue;
            double dx = chunk.getPos().getMiddleBlockX() - chant.center.x;
            double dz = chunk.getPos().getMiddleBlockZ() - chant.center.z;
            if (dx * dx + dz * dz <= Math.pow(AbyssFlowerRules.CHANT_RADIUS + 8.0D, 2)) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 == 0) {
            applyHealthLoss(player);
            boolean cleanedLegacyStack = false;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                cleanedLegacyStack |= AbyssFlowerRemainsData.removeLegacyTag(player.getInventory().getItem(slot));
            }
            if (cleanedLegacyStack) {
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
                player.inventoryMenu.broadcastChanges();
            }
        }
        Chant chant = CHANTS.get(player.getUUID());
        if (chant != null && !valid(player, chant)) cancelChant(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancelChant(player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        sendActiveChants(player);
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelChant(player);
            sendActiveChants(player);
        }
    }

    private static void sendActiveChants(ServerPlayer player) {
        for (Chant chant : CHANTS.values()) {
            ServerPlayer caster = player.getServer().getPlayerList().getPlayer(chant.caster);
            if (caster != null && chant.dimension.equals(player.level().dimension())) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new AbyssFlowerStatePacket(caster.getId(), chant.power, true, chant.center.x, chant.center.y, chant.center.z,
                                (int) Math.max(1L, chant.endTick - player.level().getGameTime())));
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        for (Chant chant : CHANTS.values()) {
            if (chant.dimension.equals(level.dimension()) && chant.endTick > level.getGameTime()) {
                level.setDayTime(chant.dayTime);
                break;
            }
        }
        long now = level.getGameTime();
        List<Mist> mists = MISTS.get(level.dimension());
        if (mists != null) {
            mists.removeIf(mist -> now > mist.endTick || level.getEntity(mist.caster) == null);
            for (Mist mist : mists) {
                if (shouldFreeze(level, BlockPos.containing(mist.center))
                        || FenghuangDownSystem.shouldFreeze(level, BlockPos.containing(mist.center))) {
                    mist.endTick++;
                    mist.nextPulse++;
                    continue;
                }
                level.sendParticles(new DustParticleOptions(new Vector3f(0.03F, 0.015F, 0.05F), 2.0F),
                        mist.center.x, mist.center.y + 1, mist.center.z, 30, 3.5D, 1, 3.5D, 0);
                level.sendParticles(ParticleTypes.SQUID_INK, mist.center.x, mist.center.y + 1, mist.center.z, 15, 3, 1, 3, 0.03D);
                if (now < mist.nextPulse) continue;
                mist.nextPulse += 20L;
                Entity found = level.getEntity(mist.caster);
                if (!(found instanceof ServerPlayer caster)) continue;
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(mist.center, mist.center).inflate(AbyssFlowerRules.MIST_RADIUS),
                        e -> e.isAlive() && e.distanceToSqr(mist.center) <= Math.pow(AbyssFlowerRules.MIST_RADIUS, 2) && !isCompanion(caster, e))) {
                    target.getPersistentData().putUUID(CASTER, caster.getUUID());
                    target.addEffect(new MobEffectInstance(ModEffects.BLACK_ABYSS_WITHER.get(), Math.max(1, (int) (mist.endTick - now)), 0));
                    target.hurt(witherSource(target), AbyssFlowerRules.MIST_DAMAGE);
                }
            }
            if (mists.isEmpty()) MISTS.remove(level.dimension());
        }
        List<Detonation> detonations = DETONATIONS.get(level.dimension());
        if (detonations != null) {
            Iterator<Detonation> iterator = detonations.iterator();
            while (iterator.hasNext()) {
                Detonation detonation = iterator.next();
                Entity target = level.getEntity(detonation.target);
                if (target != null) detonation.position = target.position();
                if (shouldFreeze(level, BlockPos.containing(detonation.position))
                        || FenghuangDownSystem.shouldFreeze(level, BlockPos.containing(detonation.position))) {
                    detonation.endTick++;
                    continue;
                }
                if (now < detonation.endTick) continue;
                Entity caster = level.getEntity(detonation.caster);
                ElysianHeroExplosionProtection.run(() -> level.explode(
                        caster, detonation.position.x, detonation.position.y + 0.5D,
                        detonation.position.z, 2.0F, Level.ExplosionInteraction.NONE));
                iterator.remove();
            }
            if (detonations.isEmpty()) DETONATIONS.remove(level.dimension());
        }
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {
        CHANTS.clear();
        MISTS.clear();
        DETONATIONS.clear();
    }

    private static final class Chant {
        final UUID caster;
        final AbyssFlowerRules.Power power;
        final UUID weaponToken;
        final boolean white;
        final UUID target;
        final ResourceKey<Level> dimension;
        final Vec3 center;
        final long endTick;
        final long dayTime;

        Chant(ServerPlayer player, AbyssFlowerRules.Power power, UUID weaponToken, boolean white, UUID target) {
            this.caster = player.getUUID();
            this.power = power;
            this.weaponToken = weaponToken;
            this.white = white;
            this.target = target;
            this.dimension = player.level().dimension();
            this.center = player.position();
            this.endTick = player.level().getGameTime() + AbyssFlowerRules.CHANT_TIMEOUT;
            this.dayTime = player.level().getDayTime();
        }
    }

    private static final class Mist {
        final UUID caster;
        final UUID weaponToken;
        final Vec3 center;
        long endTick;
        long nextPulse;

        Mist(UUID caster, UUID weaponToken, Vec3 center, long endTick) {
            this.caster = caster;
            this.weaponToken = weaponToken;
            this.center = center;
            this.endTick = endTick;
            this.nextPulse = endTick - AbyssFlowerRules.MIST_DURATION + 20L;
        }
    }

    private static final class Detonation {
        final UUID caster;
        final UUID target;
        Vec3 position;
        long endTick;

        Detonation(UUID caster, UUID target, Vec3 position, long endTick) {
            this.caster = caster;
            this.target = target;
            this.position = position;
            this.endTick = endTick;
        }
    }
}
