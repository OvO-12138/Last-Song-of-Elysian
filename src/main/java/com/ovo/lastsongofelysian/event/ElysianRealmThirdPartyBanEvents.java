package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.dimension.ModDimensions;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(
        modid = lastsongofelysian.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ElysianRealmThirdPartyBanEvents {

    private ElysianRealmThirdPartyBanEvents() {
    }

    private static boolean isElysianRealm(Level level) {
        return level.dimension().equals(
                ModDimensions.ELYSIAN_REALM
        );
    }

    private static boolean isAllowedNamespace(
            ResourceLocation id
    ) {
        if (id == null) {
            return false;
        }

        String namespace = id.getNamespace();

        return "minecraft".equals(namespace)
                || lastsongofelysian.MODID.equals(namespace);
    }

    private static boolean isBannedItem(ItemStack stack) {
        return !stack.isEmpty()
                && !isAllowedNamespace(
                        ForgeRegistries.ITEMS.getKey(
                                stack.getItem()
                        )
                );
    }

    private static boolean isBannedHeldItem(Player player) {
        return isBannedItem(player.getMainHandItem());
    }

    private static boolean isProtectedNpc(Entity entity) {
        return entity.getType() == ModEntities.PARDOFELIS_MERCHANT.get()
                || entity.getType() == ModEntities.APONIA_NPC.get()
                || entity.getType() == ModEntities.ELYSIA_NPC.get()
                || entity.getType() == ModEntities.GRISEO_NPC.get()
                || entity.getType() == ModEntities.VILL_V_NPC.get()
                || entity.getType() == ModEntities.EDEN_NPC.get();
    }

    private static boolean isNpcCarryAttempt(PlayerInteractEvent event, Entity target) {
        return isElysianRealm(event.getLevel())
                && event.getEntity().isShiftKeyDown()
                && isProtectedNpc(target);
    }

    private static void deny(
            PlayerInteractEvent event
    ) {
        event.setCanceled(true);
        event.setCancellationResult(
                InteractionResult.FAIL
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(
            PlayerInteractEvent.RightClickItem event
    ) {
        if (
                isElysianRealm(event.getLevel())
                        && isBannedItem(event.getItemStack())
        ) {
            deny(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event
    ) {
        if (
                isElysianRealm(event.getLevel())
                        && isBannedItem(event.getItemStack())
        ) {
            deny(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractEntity(
            PlayerInteractEvent.EntityInteract event
    ) {
        if (
                isNpcCarryAttempt(event, event.getTarget())
                        || isElysianRealm(event.getLevel())
                        && isBannedItem(event.getItemStack())
        ) {
            deny(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractEntitySpecific(
            PlayerInteractEvent.EntityInteractSpecific event
    ) {
        if (
                isNpcCarryAttempt(event, event.getTarget())
                        || isElysianRealm(event.getLevel())
                        && isBannedItem(event.getItemStack())
        ) {
            deny(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(
            AttackEntityEvent event
    ) {
        Player player = event.getEntity();

        if (
                isElysianRealm(player.level())
                        && isBannedHeldItem(player)
        ) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onStartUsingItem(
            LivingEntityUseItemEvent.Start event
    ) {
        if (
                event.getEntity() instanceof Player player
                        && isElysianRealm(player.level())
                        && isBannedItem(event.getItem())
        ) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST,
            receiveCanceled = true
    )
    public static void onThirdPartyDamage(
            LivingAttackEvent event
    ) {
        if (
                !isElysianRealm(event.getEntity().level())
                        || !(event.getSource().getEntity()
                        instanceof Player player)
                        || !isBannedHeldItem(player)
        ) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {
        if (!isElysianRealm(event.getLevel())) {
            return;
        }

        Entity entity = event.getEntity();
        ResourceLocation entityId =
                ForgeRegistries.ENTITY_TYPES.getKey(
                        entity.getType()
                );

        if (!isAllowedNamespace(entityId)) {
            event.setCanceled(true);
        }
    }
}
