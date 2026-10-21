package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.dimension.ModDimensions;
import com.ovo.lastsongofelysian.item.HonkaiEnergyInhibitorItem;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.util.ModAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AdvancementEvents {

    private static final String ELYSIAN_RETURN_MARKER = "ElysianEntranceHasReturn";

    private AdvancementEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()
                || event.player.tickCount % 20 != 0) return;

        if (CuriosApi.getCuriosHelper().findFirstCurio(
                event.player,
                stack -> stack.is(ModItems.COCOON_OF_FINALITY.get())
        ).isPresent()) {
            ModAdvancements.award(event.player, "obtain_cocoon");
        }
        if (CocoonTrialProgress.isUnlocked(event.player)) {
            ModAdvancements.award(event.player, "start_finality_trials");
        }
        if (CocoonTrialProgress.completedCount(event.player) == CocoonTrialProgress.TRIAL_COUNT) {
            ModAdvancements.award(event.player, "complete_finality_trials");
        }
        if (event.player.level().dimension().equals(ModDimensions.ELYSIAN_REALM)
                || event.player.getPersistentData().getBoolean(ELYSIAN_RETURN_MARKER)) {
            ModAdvancements.award(event.player, "enter_elysian_realm");
        }
        awardOwnedSignets(event.player);
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        if (!event.getEntity().level().isClientSide()
                && crafted.getItem() instanceof HonkaiEnergyInhibitorItem) {
            ModAdvancements.award(event.getEntity(), "craft_inhibitor");
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer
                && event.getTo().equals(ModDimensions.ELYSIAN_REALM)) {
            ModAdvancements.award(event.getEntity(), "enter_elysian_realm");
        }
    }

    private static void awardOwnedSignets(Player player) {
        awardIfOwned(player, ModItems.SIGNET_OF_REVERIE.get(), "signet_reverie");
        awardIfOwned(player, ModItems.SIGNET_OF_VICISSITUDE.get(), "signet_vicissitude");
        awardIfOwned(player, ModItems.SIGNET_OF_STARS.get(), "signet_stars");
        awardIfOwned(player, ModItems.SIGNET_OF_INFINITY.get(), "signet_infinity");
        awardIfOwned(player, ModItems.SIGNET_OF_DAYBREAK.get(), "signet_daybreak");
        awardIfOwned(player, ModItems.SIGNET_OF_SETSURA.get(), "signet_setsuna");
        awardIfOwned(player, ModItems.SIGNET_OF_BODHI.get(), "signet_bodhi");
        awardIfOwned(player, ModItems.SIGNET_OF_DECIMATION.get(), "signet_decimation");
        awardIfOwned(player, ModItems.SIGNET_OF_HELIX.get(), "signet_helix");
        awardIfOwned(player, ModItems.SIGNET_OF_GOLD.get(), "signet_gold");
        awardIfOwned(player, ModItems.SIGNET_OF_DISCIPLINE.get(), "signet_discipline");
        awardIfOwned(player, ModItems.SIGNET_OF_DELIVERANCE.get(), "signet_deliverance");
    }

    private static void awardIfOwned(Player player, Item item, String advancement) {
        boolean inInventory = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(item)) {
                inInventory = true;
                break;
            }
        }
        if (inInventory || CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(item))
                .isPresent()) {
            ModAdvancements.award(player, advancement);
        }
    }
}
