package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.item.ShiJiBookItem;
import com.ovo.lastsongofelysian.network.ModNetwork;
import com.ovo.lastsongofelysian.network.MissingShiJiBookCrashPacket;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.ModText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShiJiProtectionEvents {

    private static final String NBT_GOT_SHI_JI = "GotShiJi";
    private static final String NBT_MISSING_TICKS = "ShiJiMissingTicks";
    private static final String NBT_CRASH_SENT = "ShiJiCrashSent";
    private static final int REPLACEMENT_INTERVAL_TICKS = 100;
    private static final int MISSING_GRACE_TICKS = 20 * 20;

    private static boolean isShiJi(ItemStack stack) {
        return stack.is(ModItems.SHI_JI.get());
    }

    private static boolean hasShiJi(ServerPlayer player) {
        Inventory inv = player.getInventory();

        for (ItemStack stack : inv.items) {
            if (isShiJi(stack)) return true;
        }

        for (ItemStack stack : inv.armor) {
            if (isShiJi(stack)) return true;
        }

        for (ItemStack stack : inv.offhand) {
            if (isShiJi(stack)) return true;
        }

        return false;
    }

    private static void giveShiJi(ServerPlayer player) {
        ItemStack stack = new ItemStack(ModItems.SHI_JI.get());
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
        player.displayClientMessage(
                ModText.exact("岁月史书不可遗失。新的史书已被补回。"), true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side != LogicalSide.SERVER) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        CompoundTag data = player.getPersistentData();
        boolean gotShiJi = data.getBoolean(NBT_GOT_SHI_JI);
        boolean hasBook = hasShiJi(player);

        if (!gotShiJi) {
            if (hasBook) {
                data.putBoolean(NBT_GOT_SHI_JI, true);
            } else {
                giveShiJi(player);
                data.putBoolean(NBT_GOT_SHI_JI, true);
            }
            return;
        }

        if (hasBook) {
            data.remove(NBT_MISSING_TICKS);
            data.remove(NBT_CRASH_SENT);
            return;
        }

        int missingTicks = Math.min(MISSING_GRACE_TICKS,
                data.getInt(NBT_MISSING_TICKS) + 1);
        data.putInt(NBT_MISSING_TICKS, missingTicks);

        if (missingTicks < MISSING_GRACE_TICKS
                && missingTicks % REPLACEMENT_INTERVAL_TICKS == 0) {
            giveShiJi(player);
        }

        if (missingTicks >= MISSING_GRACE_TICKS && !data.getBoolean(NBT_CRASH_SENT)) {
            data.putBoolean(NBT_CRASH_SENT, true);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MissingShiJiBookCrashPacket());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().remove(NBT_CRASH_SENT);
        }
    }

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        ItemEntity entity = event.getEntity();

        if (isShiJi(entity.getItem())) {
            event.setCanceled(true);
            entity.setInvulnerable(true);
            entity.clearFire();
            entity.setUnlimitedLifetime();
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity entity = event.getEntity();

        if (isShiJi(entity.getItem())) {
            ShiJiBookItem.rememberVoidOwner(entity, event.getPlayer());
            entity.setInvulnerable(true);
            entity.clearFire();
            entity.setUnlimitedLifetime();
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        List<?> affectedEntities = event.getAffectedEntities();

        affectedEntities.removeIf(entity -> {
            if (entity instanceof ItemEntity itemEntity) {
                return isShiJi(itemEntity.getItem());
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        boolean gotShiJi = event.getOriginal().getPersistentData().getBoolean(NBT_GOT_SHI_JI);
        if (gotShiJi) {
            event.getEntity().getPersistentData().putBoolean(NBT_GOT_SHI_JI, true);
        }
        event.getEntity().getPersistentData().remove(NBT_MISSING_TICKS);
        event.getEntity().getPersistentData().remove(NBT_CRASH_SENT);
    }
}
