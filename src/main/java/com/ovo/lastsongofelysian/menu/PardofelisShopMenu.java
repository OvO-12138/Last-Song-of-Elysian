package com.ovo.lastsongofelysian.menu;

import com.ovo.lastsongofelysian.util.ModText;
import com.ovo.lastsongofelysian.util.ModAdvancements;

import com.ovo.lastsongofelysian.event.CocoonCoreProgress;
import com.ovo.lastsongofelysian.event.ReverieSignetSystem;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.ovo.lastsongofelysian.registry.ModMenus;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class PardofelisShopMenu extends AbstractContainerMenu {

    public static final int OFFER_COUNT = 10;
    public static final int UPGRADE_PRICE = 256;
    public static final int SECONDARY_SIGNET_PRICE = 386;
    public static final int DELIVERANCE_CORE_PRICE = 824;
    public static final int DELIVERANCE_CORE_SIGNET_PRICE = 496;
    public static final int HERRSCHER_CORE_PRICE = 3000;
    public static final int BASE_SIGNET_PRICE = 4280;
    public static final int REROLL_PRICE = 50;
    public static final int REROLL_BUTTON = OFFER_COUNT;
    private static final int EMPTY_OFFER = -1;

    private static final int REVERIE_BASE_PRICE = 1300;
    private static final int DATA_OFFER_START = 0;
    private static final int DATA_PURCHASED_START = DATA_OFFER_START + OFFER_COUNT;
    private static final int DATA_SILVER = DATA_PURCHASED_START + OFFER_COUNT;
    private static final int DATA_SHOP_PERCENT = DATA_SILVER + 1;
    private static final int DATA_REROLL_FREE = DATA_SHOP_PERCENT + 1;
    private static final int DATA_COUNT = DATA_REROLL_FREE + 1;
    private static final String NBT_PLAYER_PERSISTED = "PlayerPersisted";
    private static final String NBT_STOCK = "LSEPardofelisUpgradeStock";
    private static final String NBT_PURCHASED_MASK = "LSEPardofelisUpgradePurchasedMask";
    private static final String NBT_ONE_TIME_PURCHASES = "LSEPardofelisOneTimePurchases";
    private static final String NBT_PURCHASED_REVERIE = "LSEPardofelisPurchasedReverie";
    private static final String NBT_PURCHASED_DELIVERANCE_SECONDARIES =
            "LSEPardofelisPurchasedDeliveranceSecondaries";
    private static final String NBT_PURCHASED_DISCIPLINE_SECONDARIES =
            "LSEPardofelisPurchasedDisciplineSecondaries";
    private static final String NBT_PURCHASED_GOLD_SECONDARIES =
            "LSEPardofelisPurchasedGoldSecondaries";
    private static final String NBT_PURCHASED_HELIX_SECONDARIES =
            "LSEPardofelisPurchasedHelixSecondaries";
    private static final String NBT_PURCHASED_DECIMATION_SECONDARIES =
            "LSEPardofelisPurchasedDecimationSecondaries";
    private static final String NBT_PURCHASED_BODHI_SECONDARIES =
            "LSEPardofelisPurchasedBodhiSecondaries";
    private static final String NBT_PURCHASED_SETSURA_SECONDARIES =
            "LSEPardofelisPurchasedSetsuraSecondaries";
    private static final String NBT_PURCHASED_DAYBREAK_SECONDARIES =
            "LSEPardofelisPurchasedDaybreakSecondaries";

    private final Inventory playerInventory;
    private final ContainerData shopData;

    public PardofelisShopMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.PARDOFELIS_SHOP_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.shopData = new SimpleContainerData(DATA_COUNT);
        this.addDataSlots(this.shopData);
        if (!playerInventory.player.level().isClientSide()) {
            ensureStock(playerInventory.player);
            refreshData();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (player.level().isClientSide()) return true;
        if (buttonId >= 0 && buttonId < OFFER_COUNT) {
            buyOffer(player, buttonId);
            return true;
        }
        if (buttonId == REROLL_BUTTON) {
            reroll(player);
            return true;
        }
        return false;
    }

    private void buyOffer(Player player, int offerIndex) {
        int[] stock = getStock(player);
        if (offerIndex < 0 || offerIndex >= stock.length) return;

        ShopProduct product = ShopProduct.fromOrdinal(stock[offerIndex]);
        if (product == null || product.isUnavailable(player)) {
            generateStock(player);
            refreshData();
            this.broadcastChanges();
            return;
        }
        int mask = getPersistedData(player).getInt(NBT_PURCHASED_MASK);
        boolean alreadyPurchased = (mask & (1 << offerIndex)) != 0
                || (product.isPermanentProduct() && hasPurchasedProduct(player, product));
        if (alreadyPurchased) {
            refreshData();
            return;
        }

        int price = product.price(player);
        int silver = SignetUpgradeData.countSilver(player);
        if (silver < price) {
            player.displayClientMessage(
                    ModText.literal("银币不足：" + silver + "/" + price)
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            return;
        }
        if (!SignetUpgradeData.removeSilver(player, price)) {
            refreshData();
            return;
        }
        ReverieSignetSystem.onSilverSpent(player, price);

        SignetUpgradeData.Signet coreSignet = product.coreSystemSignet();
        ItemStack purchased = new ItemStack(product.item());
        if (product.signet != null || coreSignet != null) {
            SignetUpgradeData.setShopPaidSilverPerItem(purchased, price);
        }
        if (coreSignet != null) {
            SignetUpgradeData.stampCoreSystemItem(player, purchased, coreSignet);
        }
        if (!player.addItem(purchased)) {
            player.drop(purchased, false);
        }
        ModAdvancements.award(player, "buy_from_pardofelis");

        if (product.unlockSkill != null) {
            markSecondaryPurchased(player, product.unlockSkill);
            player.displayClientMessage(
                    ModText.literal("已购买：" + product.unlockSkill.displayName())
                            .withStyle(ChatFormatting.DARK_BLUE),
                    true
            );
        }

        if (product.deliveranceCore != null) {
            SignetUpgradeData.recordDeliveranceCorePurchase(
                    player,
                    product.deliveranceCore,
                    0
            );
        }
        if (product.deliveranceCoreSignet != null) {
            SignetUpgradeData.recordDeliveranceCoreSignetPurchase(
                    player,
                    product.deliveranceCoreSignet,
                    0
            );
        }
        if (product.disciplineCore != null) {
            SignetUpgradeData.recordDisciplineCorePurchase(player, product.disciplineCore);
        }
        if (product.disciplineCoreSignet != null) {
            SignetUpgradeData.recordDisciplineCoreSignetPurchase(player, product.disciplineCoreSignet);
        }
        if (product.goldCore != null) {
            SignetUpgradeData.recordGoldCorePurchase(player, product.goldCore);
        }
        if (product.goldCoreSignet != null) {
            SignetUpgradeData.recordGoldCoreSignetPurchase(player, product.goldCoreSignet);
        }
        if (product.helixCore != null) {
            SignetUpgradeData.recordHelixCorePurchase(player, product.helixCore);
        }
        if (product.helixCoreSignet != null) {
            SignetUpgradeData.recordHelixCoreSignetPurchase(player, product.helixCoreSignet);
        }
        if (product.decimationCore != null) {
            SignetUpgradeData.recordDecimationCorePurchase(player, product.decimationCore);
        }
        if (product.decimationCoreSignet != null) {
            SignetUpgradeData.recordDecimationCoreSignetPurchase(player, product.decimationCoreSignet);
        }
        if (product.bodhiCore != null) {
            SignetUpgradeData.recordBodhiCorePurchase(player, product.bodhiCore);
        }
        if (product.bodhiCoreSignet != null) {
            SignetUpgradeData.recordBodhiCoreSignetPurchase(player, product.bodhiCoreSignet);
        }
        if (product.setsuraCore != null) {
            SignetUpgradeData.recordSetsuraCorePurchase(player, product.setsuraCore);
        }
        if (product.setsuraCoreSignet != null) {
            SignetUpgradeData.recordSetsuraCoreSignetPurchase(player, product.setsuraCoreSignet);
        }
        if (product.daybreakCore != null) {
            SignetUpgradeData.recordDaybreakCorePurchase(player, product.daybreakCore);
        }
        if (product.daybreakCoreSignet != null) {
            SignetUpgradeData.recordDaybreakCoreSignetPurchase(player, product.daybreakCoreSignet);
        }
        if (product.extensionCore != null) {
            SignetUpgradeData.recordExtensionCorePurchase(player, product.extensionCore);
        }
        if (product.extensionCoreSignet != null) {
            SignetUpgradeData.recordExtensionCoreSignetPurchase(
                    player, product.extensionCoreSignet);
        }
        if (coreSignet != null) {
            SignetUpgradeData.recordCoreSilverSpent(player, coreSignet, price);
        }
        if (product.herrscherCore) {
            CocoonCoreProgress.recordAcquiredCore(
                    player,
                    product.item()
            );
        }

        CompoundTag persisted = getPersistedData(player);
        persisted.putInt(NBT_PURCHASED_MASK, mask | (1 << offerIndex));
        if (product.isPermanentProduct()) {
            markProductPurchased(player, product);
        }
        if (product == ShopProduct.REVERIE_SIGNET) {
            persisted.putBoolean(NBT_PURCHASED_REVERIE, true);
        }
        if (product.deliveranceCore != null || product.disciplineCore != null
                || product.goldCore != null || product.helixCore != null
                || product.decimationCore != null || product.bodhiCore != null
                || product.setsuraCore != null || product.daybreakCore != null
                || product.extensionCore != null) {
            generateStock(player);
        }
        refreshData();
        this.broadcastChanges();
    }

    private void reroll(Player player) {
        boolean free = ReverieSignetSystem.isNextRerollFree(player);
        int silver = SignetUpgradeData.countSilver(player);
        if (!free && silver < REROLL_PRICE) {
            player.displayClientMessage(
                    ModText.literal("银币不足：" + silver + "/" + REROLL_PRICE)
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            return;
        }
        if (!free && !SignetUpgradeData.removeSilver(player, REROLL_PRICE)) {
            refreshData();
            return;
        }
        if (!free) ReverieSignetSystem.onSilverSpent(player, REROLL_PRICE);
        generateStock(player);
        ReverieSignetSystem.recordSuccessfulReroll(player);
        refreshData();
        this.broadcastChanges();
    }

    private void refreshData() {
        Player player = this.playerInventory.player;
        if (player.level().isClientSide()) return;
        int[] stock = getStock(player);
        int mask = getPersistedData(player).getInt(NBT_PURCHASED_MASK);
        for (int i = 0; i < OFFER_COUNT; i++) {
            ShopProduct product = ShopProduct.fromOrdinal(stock[i]);
            this.shopData.set(DATA_OFFER_START + i, product == null ? EMPTY_OFFER : product.ordinal());
            boolean purchased = product == null
                    || (mask & (1 << i)) != 0
                    || (product.isPermanentProduct() && hasPurchasedProduct(player, product));
            this.shopData.set(DATA_PURCHASED_START + i, purchased ? 1 : 0);
        }
        this.shopData.set(DATA_SILVER, SignetUpgradeData.countSilver(player));
        this.shopData.set(DATA_SHOP_PERCENT, getReverieShopPercent(player));
        this.shopData.set(DATA_REROLL_FREE, ReverieSignetSystem.isNextRerollFree(player) ? 1 : 0);
    }

    private static void ensureStock(Player player) {
        int[] stock = getPersistedData(player).getIntArray(NBT_STOCK);
        if (!isValidStock(player, stock)) {
            generateStock(player);
        }
    }

    private static boolean isValidStock(Player player, int[] stock) {
        if (stock.length != OFFER_COUNT) return false;
        boolean[] seen = new boolean[ShopProduct.values().length];
        int purchasedMask = getPersistedData(player).getInt(NBT_PURCHASED_MASK);
        for (int index = 0; index < stock.length; index++) {
            int value = stock[index];
            if (value == EMPTY_OFFER) continue;
            if (value < 0 || value >= seen.length || seen[value]) return false;
            ShopProduct product = ShopProduct.fromOrdinal(value);
            boolean purchasedPermanentProduct = product != null
                    && product.isPermanentProduct()
                    && (purchasedMask & (1 << index)) != 0;
            if (product == null || product.isUnavailable(player) && !purchasedPermanentProduct) {
                return false;
            }
            seen[value] = true;
        }
        return true;
    }

    private static void generateStock(Player player) {
        List<Integer> products = new ArrayList<>();
        for (ShopProduct product : ShopProduct.values()) {
            if (product.isUnavailable(player)) continue;
            products.add(product.ordinal());
        }
        for (int i = products.size() - 1; i > 0; i--) {
            int swap = player.getRandom().nextInt(i + 1);
            int value = products.get(i);
            products.set(i, products.get(swap));
            products.set(swap, value);
        }
        int[] stock = new int[OFFER_COUNT];
        Arrays.fill(stock, EMPTY_OFFER);
        for (int i = 0; i < Math.min(OFFER_COUNT, products.size()); i++) {
            stock[i] = products.get(i);
        }
        CompoundTag persisted = getPersistedData(player);
        persisted.putIntArray(NBT_STOCK, stock);
        persisted.putInt(NBT_PURCHASED_MASK, 0);
    }

    private static int[] getStock(Player player) {
        ensureStock(player);
        return getPersistedData(player).getIntArray(NBT_STOCK);
    }

    public ItemStack getDisplayedOffer(int index) {
        if (index < 0 || index >= OFFER_COUNT) return ItemStack.EMPTY;
        ShopProduct product = ShopProduct.fromOrdinal(
                this.shopData.get(DATA_OFFER_START + index)
        );
        return product == null ? ItemStack.EMPTY : new ItemStack(product.item());
    }

    public int getDisplayedOfferPrice(int index) {
        if (index < 0 || index >= OFFER_COUNT) return 0;
        ShopProduct product = ShopProduct.fromOrdinal(
                this.shopData.get(DATA_OFFER_START + index)
        );
        if (product == null) return 0;
        return product.price * this.shopData.get(DATA_SHOP_PERCENT) / 100;
    }

    public boolean isDisplayedOfferEmpty(int index) {
        return getDisplayedOffer(index).isEmpty();
    }

    public boolean isDisplayedOfferPurchased(int index) {
        return index >= 0
                && index < OFFER_COUNT
                && this.shopData.get(DATA_PURCHASED_START + index) != 0;
    }

    public int getDisplayedSilver() {
        return this.shopData.get(DATA_SILVER);
    }

    public boolean isDisplayedRerollFree() {
        return this.shopData.get(DATA_REROLL_FREE) != 0;
    }

    private static int getReverieShopPercent(Player player) {
        boolean hasReverie = CuriosApi.getCuriosHelper()
                .findFirstCurio(
                        player,
                        stack -> stack.is(ModItems.SIGNET_OF_REVERIE.get())
                )
                .isPresent();
        return hasReverie
                ? SignetUpgradeData.reverieShopPercent(player)
                : 100;
    }

    private static int getShopPrice(Player player, int basePrice) {
        return basePrice * getReverieShopPercent(player) / 100;
    }

    private static boolean hasItemOrCurio(Player player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(item)) return true;
        }
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(item))
                .isPresent();
    }

    private static CompoundTag getPersistedData(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(NBT_PLAYER_PERSISTED)) {
            root.put(NBT_PLAYER_PERSISTED, new CompoundTag());
        }
        return root.getCompound(NBT_PLAYER_PERSISTED);
    }

    public static void invalidateStock(Player player) {
        if (player == null) return;
        CompoundTag persisted = getPersistedData(player);
        persisted.remove(NBT_STOCK);
        persisted.remove(NBT_PURCHASED_MASK);
    }

    public static void preserveDeliverancePurchases(Player player) {
        preserveCorePurchases(player, SignetUpgradeData.Signet.DELIVERANCE);
    }

    public static void preserveCorePurchases(Player player, SignetUpgradeData.Signet signet) {
        if (player == null || signet == null) return;
        for (ShopProduct product : ShopProduct.values()) {
            if (product.coreSystemSignet() == signet && product.wasCorePurchased(player)) {
                markProductPurchased(player, product);
            }
        }
    }

    private static boolean hasPurchasedSecondary(
            Player player,
            SignetUpgradeData.Skill skill
    ) {
        if (skill == null) return false;
        if (skill.isDeliveranceSecondary()) {
            int purchasedMask = getPersistedData(player)
                    .getInt(NBT_PURCHASED_DELIVERANCE_SECONDARIES);
            return (purchasedMask & skill.deliveranceSecondaryBit()) != 0
                    || SignetUpgradeData.isSecondarySkillUnlocked(player, skill);
        }
        if (skill.isDisciplineSecondary()) {
            int purchasedMask = getPersistedData(player)
                    .getInt(NBT_PURCHASED_DISCIPLINE_SECONDARIES);
            return (purchasedMask & skill.disciplineSecondaryBit()) != 0
                    || SignetUpgradeData.isDisciplineSecondarySkillUnlocked(player, skill);
        }
        if (skill.isGoldSecondary()) {
            int purchasedMask = getPersistedData(player).getInt(NBT_PURCHASED_GOLD_SECONDARIES);
            return (purchasedMask & skill.goldSecondaryBit()) != 0
                    || SignetUpgradeData.isGoldSecondarySkillUnlocked(player, skill);
        }
        if (skill.isHelixSecondary()) {
            int purchasedMask = getPersistedData(player).getInt(NBT_PURCHASED_HELIX_SECONDARIES);
            return (purchasedMask & skill.helixSecondaryBit()) != 0
                    || SignetUpgradeData.isHelixSecondarySkillUnlocked(player, skill);
        }
        if (skill.isDecimationSecondary()) {
            int purchasedMask = getPersistedData(player)
                    .getInt(NBT_PURCHASED_DECIMATION_SECONDARIES);
            return (purchasedMask & skill.decimationSecondaryBit()) != 0
                    || SignetUpgradeData.isDecimationSecondarySkillUnlocked(player, skill);
        }
        if (skill.isBodhiSecondary()) {
            int purchasedMask = getPersistedData(player).getInt(NBT_PURCHASED_BODHI_SECONDARIES);
            return (purchasedMask & skill.bodhiSecondaryBit()) != 0
                    || SignetUpgradeData.isBodhiSecondarySkillUnlocked(player, skill);
        }
        if (skill.isSetsuraSecondary()) {
            int purchasedMask = getPersistedData(player).getInt(NBT_PURCHASED_SETSURA_SECONDARIES);
            return (purchasedMask & skill.setsuraSecondaryBit()) != 0
                    || SignetUpgradeData.isSetsuraSecondarySkillUnlocked(player, skill);
        }
        if (skill.isDaybreakSecondary()) {
            int purchasedMask = getPersistedData(player).getInt(NBT_PURCHASED_DAYBREAK_SECONDARIES);
            return (purchasedMask & skill.daybreakSecondaryBit()) != 0
                    || SignetUpgradeData.isDaybreakSecondarySkillUnlocked(player, skill);
        }
        if (skill.isExtensionSecondary()) {
            int purchasedMask = getPersistedData(player).getInt(
                    "LSEPardofelisPurchasedExtension" + skill.signet().name());
            return (purchasedMask & skill.extensionSecondaryBit()) != 0
                    || SignetUpgradeData.isExtensionSecondarySkillUnlocked(player, skill);
        }
        return false;
    }

    private static void markSecondaryPurchased(
            Player player,
            SignetUpgradeData.Skill skill
    ) {
        if (skill == null) return;
        CompoundTag persisted = getPersistedData(player);
        if (skill.isDeliveranceSecondary()) {
            int purchasedMask = persisted.getInt(NBT_PURCHASED_DELIVERANCE_SECONDARIES);
            persisted.putInt(
                    NBT_PURCHASED_DELIVERANCE_SECONDARIES,
                    purchasedMask | skill.deliveranceSecondaryBit()
            );
        } else if (skill.isDisciplineSecondary()) {
            int purchasedMask = persisted.getInt(NBT_PURCHASED_DISCIPLINE_SECONDARIES);
            persisted.putInt(
                    NBT_PURCHASED_DISCIPLINE_SECONDARIES,
                    purchasedMask | skill.disciplineSecondaryBit()
            );
        } else if (skill.isGoldSecondary()) {
            int purchasedMask = persisted.getInt(NBT_PURCHASED_GOLD_SECONDARIES);
            persisted.putInt(
                    NBT_PURCHASED_GOLD_SECONDARIES,
                    purchasedMask | skill.goldSecondaryBit()
            );
        } else if (skill.isHelixSecondary()) {
            int purchasedMask = persisted.getInt(NBT_PURCHASED_HELIX_SECONDARIES);
            persisted.putInt(
                    NBT_PURCHASED_HELIX_SECONDARIES,
                    purchasedMask | skill.helixSecondaryBit()
            );
        } else if (skill.isDecimationSecondary()) {
            int purchasedMask = persisted.getInt(NBT_PURCHASED_DECIMATION_SECONDARIES);
            persisted.putInt(
                    NBT_PURCHASED_DECIMATION_SECONDARIES,
                    purchasedMask | skill.decimationSecondaryBit()
            );
        } else if (skill.isBodhiSecondary()) {
            int purchasedMask = persisted.getInt(NBT_PURCHASED_BODHI_SECONDARIES);
            persisted.putInt(
                    NBT_PURCHASED_BODHI_SECONDARIES,
                    purchasedMask | skill.bodhiSecondaryBit()
            );
        } else if (skill.isSetsuraSecondary()) {
            int purchasedMask = persisted.getInt(NBT_PURCHASED_SETSURA_SECONDARIES);
            persisted.putInt(
                    NBT_PURCHASED_SETSURA_SECONDARIES,
                    purchasedMask | skill.setsuraSecondaryBit()
            );
        } else if (skill.isDaybreakSecondary()) {
            int purchasedMask = persisted.getInt(NBT_PURCHASED_DAYBREAK_SECONDARIES);
            persisted.putInt(
                    NBT_PURCHASED_DAYBREAK_SECONDARIES,
                    purchasedMask | skill.daybreakSecondaryBit()
            );
        } else if (skill.isExtensionSecondary()) {
            String key = "LSEPardofelisPurchasedExtension" + skill.signet().name();
            persisted.putInt(key, persisted.getInt(key) | skill.extensionSecondaryBit());
        }
    }

    private static boolean hasPurchasedReverie(Player player) {
        return getPersistedData(player).getBoolean(NBT_PURCHASED_REVERIE);
    }

    private static boolean hasPurchasedProduct(Player player, ShopProduct product) {
        if (product == null || !product.isPermanentProduct()) return false;
        return getPersistedData(player).getCompound(NBT_ONE_TIME_PURCHASES)
                .getBoolean(product.name());
    }

    private static void markProductPurchased(Player player, ShopProduct product) {
        CompoundTag persisted = getPersistedData(player);
        CompoundTag purchased = persisted.getCompound(NBT_ONE_TIME_PURCHASES);
        purchased.putBoolean(product.name(), true);
        persisted.put(NBT_ONE_TIME_PURCHASES, purchased);
    }

    @Override
    public void broadcastChanges() {
        refreshData();
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private enum ShopProduct {
        REVERIE_SIGNET(
                () -> ModItems.SIGNET_OF_REVERIE.get(),
                REVERIE_BASE_PRICE,
                (SignetUpgradeData.Signet) null
        ),
        REVERIE_UPGRADE(() -> ModItems.REVERIE_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.REVERIE),
        VICISSITUDE_UPGRADE(() -> ModItems.VICISSITUDE_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.VICISSITUDE),
        STARS_UPGRADE(() -> ModItems.STARS_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.STARS),
        INFINITY_UPGRADE(() -> ModItems.INFINITY_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.INFINITY),
        REVERIE_CAT_VERSE(() -> ModItems.REVERIE_CAT_VERSE.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_CAT_VERSE, true),
        REVERIE_BLADE_TAIL(() -> ModItems.REVERIE_BLADE_TAIL.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_BLADE_TAIL, true),
        REVERIE_STREET_JOY(() -> ModItems.REVERIE_STREET_JOY.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_STREET_JOY, true),
        REVERIE_PC(() -> ModItems.REVERIE_PC.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_PC, true),
        REVERIE_JOY(() -> ModItems.REVERIE_JOY.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.ExtensionCore.REVERIE_JOY),
        REVERIE_BOSS(() -> ModItems.REVERIE_BOSS.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_BOSS, SignetUpgradeData.ExtensionCore.REVERIE_JOY),
        REVERIE_CAT_PUNCH(() -> ModItems.REVERIE_CAT_PUNCH.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_CAT_PUNCH, SignetUpgradeData.ExtensionCore.REVERIE_JOY),
        REVERIE_LUCKY(() -> ModItems.REVERIE_LUCKY.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_LUCKY, SignetUpgradeData.ExtensionCore.REVERIE_JOY),
        REVERIE_DREAM(() -> ModItems.REVERIE_DREAM.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.ExtensionCore.REVERIE_DREAM),
        REVERIE_ZERO(() -> ModItems.REVERIE_ZERO.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_ZERO, SignetUpgradeData.ExtensionCore.REVERIE_DREAM),
        REVERIE_SNOW(() -> ModItems.REVERIE_SNOW.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_SNOW, SignetUpgradeData.ExtensionCore.REVERIE_DREAM),
        REVERIE_DREAM_TRUE(() -> ModItems.REVERIE_DREAM_TRUE.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.REVERIE_DREAM_TRUE, SignetUpgradeData.ExtensionCore.REVERIE_DREAM),
        VICISSITUDE_TIME(() -> ModItems.VICISSITUDE_TIME.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_TIME, true),
        VICISSITUDE_ROBE(() -> ModItems.VICISSITUDE_ROBE.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_ROBE, true),
        VICISSITUDE_DREAM(() -> ModItems.VICISSITUDE_DREAM.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_DREAM, true),
        VICISSITUDE_WORDS(() -> ModItems.VICISSITUDE_WORDS.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_WORDS, true),
        VICISSITUDE_RESOLVE(() -> ModItems.VICISSITUDE_RESOLVE.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE),
        VICISSITUDE_RED_DUST(() -> ModItems.VICISSITUDE_RED_DUST.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_RED_DUST, SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE),
        VICISSITUDE_PROSPERITY(() -> ModItems.VICISSITUDE_PROSPERITY.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_PROSPERITY, SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE),
        VICISSITUDE_FAMILIAR(() -> ModItems.VICISSITUDE_FAMILIAR.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_FAMILIAR, SignetUpgradeData.ExtensionCore.VICISSITUDE_RESOLVE),
        VICISSITUDE_MEMORY(() -> ModItems.VICISSITUDE_MEMORY.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.ExtensionCore.VICISSITUDE_MEMORY),
        VICISSITUDE_REED(() -> ModItems.VICISSITUDE_REED.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_REED, SignetUpgradeData.ExtensionCore.VICISSITUDE_MEMORY),
        VICISSITUDE_FEATHER(() -> ModItems.VICISSITUDE_FEATHER.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_FEATHER, SignetUpgradeData.ExtensionCore.VICISSITUDE_MEMORY),
        VICISSITUDE_AWAKEN(() -> ModItems.VICISSITUDE_AWAKEN.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.VICISSITUDE_AWAKEN, SignetUpgradeData.ExtensionCore.VICISSITUDE_MEMORY),
        STARS_YELLOW(() -> ModItems.STARS_YELLOW.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_YELLOW, true),
        STARS_BLACK(() -> ModItems.STARS_BLACK.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_BLACK, true),
        STARS_WHITE(() -> ModItems.STARS_WHITE.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_WHITE, true),
        STARS_GRAY(() -> ModItems.STARS_GRAY.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_GRAY, true),
        STARS_SHINING(() -> ModItems.STARS_SHINING.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.ExtensionCore.STARS_SHINING),
        STARS_WILDFLOWER_SHINING(() -> ModItems.STARS_WILDFLOWER_SHINING.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_WILDFLOWER_SHINING, SignetUpgradeData.ExtensionCore.STARS_SHINING),
        STARS_FLAME_SHINING(() -> ModItems.STARS_FLAME_SHINING.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_FLAME_SHINING, SignetUpgradeData.ExtensionCore.STARS_SHINING),
        STARS_LEAF_SHINING(() -> ModItems.STARS_LEAF_SHINING.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_LEAF_SHINING, SignetUpgradeData.ExtensionCore.STARS_SHINING),
        STARS_DIM(() -> ModItems.STARS_DIM.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.ExtensionCore.STARS_DIM),
        STARS_WILDFLOWER_DIM(() -> ModItems.STARS_WILDFLOWER_DIM.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_WILDFLOWER_DIM, SignetUpgradeData.ExtensionCore.STARS_DIM),
        STARS_LEAF_DIM(() -> ModItems.STARS_LEAF_DIM.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_LEAF_DIM, SignetUpgradeData.ExtensionCore.STARS_DIM),
        STARS_FLAME_DIM(() -> ModItems.STARS_FLAME_DIM.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.STARS_FLAME_DIM, SignetUpgradeData.ExtensionCore.STARS_DIM),
        INFINITY_P(() -> ModItems.INFINITY_P.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.INFINITY_P, true),
        INFINITY_B(() -> ModItems.INFINITY_B.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.INFINITY_B, true),
        INFINITY_E(() -> ModItems.INFINITY_E.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.INFINITY_E, true),
        INFINITY_X(() -> ModItems.INFINITY_X.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.ExtensionCore.INFINITY_X),
        INFINITY_DEATH_X(() -> ModItems.INFINITY_DEATH_X.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.INFINITY_DEATH_X, SignetUpgradeData.ExtensionCore.INFINITY_X),
        INFINITY_UNKNOWN_X(() -> ModItems.INFINITY_UNKNOWN_X.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.INFINITY_UNKNOWN_X, SignetUpgradeData.ExtensionCore.INFINITY_X),
        INFINITY_NEWBORN_X(() -> ModItems.INFINITY_NEWBORN_X.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.INFINITY_NEWBORN_X, SignetUpgradeData.ExtensionCore.INFINITY_X),
        INFINITY_M(() -> ModItems.INFINITY_M.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.ExtensionCore.INFINITY_M),
        INFINITY_SACRIFICE_M(() -> ModItems.INFINITY_SACRIFICE_M.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.INFINITY_SACRIFICE_M, SignetUpgradeData.ExtensionCore.INFINITY_M),
        INFINITY_IMMORTAL_M(() -> ModItems.INFINITY_IMMORTAL_M.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.INFINITY_IMMORTAL_M, SignetUpgradeData.ExtensionCore.INFINITY_M),
        INFINITY_COLLAPSE_M(() -> ModItems.INFINITY_COLLAPSE_M.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.INFINITY_COLLAPSE_M, SignetUpgradeData.ExtensionCore.INFINITY_M),
        DAYBREAK_UPGRADE(() -> ModItems.DAYBREAK_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.DAYBREAK),
        DAYBREAK_WING(
                () -> ModItems.DAYBREAK_WING.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_WING, true
        ),
        DAYBREAK_HORN(
                () -> ModItems.DAYBREAK_HORN.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_HORN, true
        ),
        DAYBREAK_BREATH(
                () -> ModItems.DAYBREAK_BREATH.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_BREATH, true
        ),
        DAYBREAK_HEART(
                () -> ModItems.DAYBREAK_HEART.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_HEART, true
        ),
        DAYBREAK_UNFADING_LIGHT(
                () -> ModItems.DAYBREAK_UNFADING_LIGHT.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.DaybreakCore.UNFADING_LIGHT
        ),
        DAYBREAK_LONELY_NIGHT(
                () -> ModItems.DAYBREAK_LONELY_NIGHT.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.DaybreakCore.LONELY_NIGHT
        ),
        DAYBREAK_LIGHT_HERO(
                () -> ModItems.DAYBREAK_LIGHT_HERO.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_LIGHT_HERO,
                SignetUpgradeData.DaybreakCore.UNFADING_LIGHT
        ),
        DAYBREAK_LIGHT_GOOD_EVIL(
                () -> ModItems.DAYBREAK_LIGHT_GOOD_EVIL.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_LIGHT_GOOD_EVIL,
                SignetUpgradeData.DaybreakCore.UNFADING_LIGHT
        ),
        DAYBREAK_LIGHT_VOW(
                () -> ModItems.DAYBREAK_LIGHT_VOW.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_LIGHT_VOW,
                SignetUpgradeData.DaybreakCore.UNFADING_LIGHT
        ),
        DAYBREAK_NIGHT_DAWN(
                () -> ModItems.DAYBREAK_NIGHT_DAWN.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_NIGHT_DAWN,
                SignetUpgradeData.DaybreakCore.LONELY_NIGHT
        ),
        DAYBREAK_NIGHT_CHOICE(
                () -> ModItems.DAYBREAK_NIGHT_CHOICE.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_NIGHT_CHOICE,
                SignetUpgradeData.DaybreakCore.LONELY_NIGHT
        ),
        DAYBREAK_NIGHT_INTENT(
                () -> ModItems.DAYBREAK_NIGHT_INTENT.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DAYBREAK_NIGHT_INTENT,
                SignetUpgradeData.DaybreakCore.LONELY_NIGHT
        ),
        SETSURA_UPGRADE(() -> ModItems.SETSURA_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.SETSURA),
        SETSURA_RED_LEAVES(
                () -> ModItems.SETSURA_RED_LEAVES.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_RED_LEAVES, true
        ),
        SETSURA_PEONY(
                () -> ModItems.SETSURA_PEONY.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_PEONY, true
        ),
        SETSURA_WISTERIA(
                () -> ModItems.SETSURA_WISTERIA.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_WISTERIA, true
        ),
        SETSURA_IRIS(
                () -> ModItems.SETSURA_IRIS.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_IRIS, true
        ),
        SETSURA_SAKURA_CURTAIN(
                () -> ModItems.SETSURA_SAKURA_CURTAIN.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.SetsuraCore.SAKURA_CURTAIN
        ),
        SETSURA_NIGHT_YAKSHA(
                () -> ModItems.SETSURA_NIGHT_YAKSHA.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.SetsuraCore.NIGHT_YAKSHA
        ),
        SETSURA_CURTAIN_RAIN_FOUR_LIGHTS(
                () -> ModItems.SETSURA_CURTAIN_RAIN_FOUR_LIGHTS.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_CURTAIN_RAIN_FOUR_LIGHTS,
                SignetUpgradeData.SetsuraCore.SAKURA_CURTAIN
        ),
        SETSURA_CURTAIN_MOON_SAKE(
                () -> ModItems.SETSURA_CURTAIN_MOON_SAKE.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_CURTAIN_MOON_SAKE,
                SignetUpgradeData.SetsuraCore.SAKURA_CURTAIN
        ),
        SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY(
                () -> ModItems.SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_CURTAIN_BOAR_DEER_BUTTERFLY,
                SignetUpgradeData.SetsuraCore.SAKURA_CURTAIN
        ),
        SETSURA_YAKSHA_MOMENTARY_MYRIAD(
                () -> ModItems.SETSURA_YAKSHA_MOMENTARY_MYRIAD.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_YAKSHA_MOMENTARY_MYRIAD,
                SignetUpgradeData.SetsuraCore.NIGHT_YAKSHA
        ),
        SETSURA_YAKSHA_ENDLESS_EVIL(
                () -> ModItems.SETSURA_YAKSHA_ENDLESS_EVIL.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_YAKSHA_ENDLESS_EVIL,
                SignetUpgradeData.SetsuraCore.NIGHT_YAKSHA
        ),
        SETSURA_YAKSHA_ALL_FORMS(
                () -> ModItems.SETSURA_YAKSHA_ALL_FORMS.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.SETSURA_YAKSHA_ALL_FORMS,
                SignetUpgradeData.SetsuraCore.NIGHT_YAKSHA
        ),
        BODHI_UPGRADE(() -> ModItems.BODHI_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.BODHI),
        DECIMATION_UPGRADE(() -> ModItems.DECIMATION_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.DECIMATION),
        HELIX_UPGRADE(() -> ModItems.HELIX_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.HELIX),
        GOLD_UPGRADE(() -> ModItems.GOLD_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.GOLD),
        DISCIPLINE_UPGRADE(() -> ModItems.DISCIPLINE_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.DISCIPLINE),
        DELIVERANCE_UPGRADE(() -> ModItems.DELIVERANCE_SIGNET_UPGRADE.get(), UPGRADE_PRICE, SignetUpgradeData.Signet.DELIVERANCE),
        DELIVERANCE_HUNTERS_MASK(
                () -> ModItems.DELIVERANCE_HUNTERS_MASK.get(),
                SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DELIVERANCE_HUNTERS_MASK,
                true
        ),
        DELIVERANCE_RESTRAINERS_RELIC(
                () -> ModItems.DELIVERANCE_RESTRAINERS_RELIC.get(),
                SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DELIVERANCE_RESTRAINERS_RELIC,
                true
        ),
        DELIVERANCE_SEEKERS_ROBE(
                () -> ModItems.DELIVERANCE_SEEKERS_ROBE.get(),
                SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DELIVERANCE_SEEKERS_ROBE,
                true
        ),
        DELIVERANCE_KINGS_SWORD(
                () -> ModItems.DELIVERANCE_KINGS_SWORD.get(),
                DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.DeliveranceCore.KINGS_SWORD
        ),
        DELIVERANCE_LONE_SHADOW(
                () -> ModItems.DELIVERANCE_LONE_SHADOW.get(),
                DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.DeliveranceCore.LONE_SHADOW
        ),
        DELIVERANCE_KINGS_MUSTER(
                () -> ModItems.DELIVERANCE_KINGS_MUSTER.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DELIVERANCE_KINGS_MUSTER
        ),
        DELIVERANCE_KINGS_EXPEDITION(
                () -> ModItems.DELIVERANCE_KINGS_EXPEDITION.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DELIVERANCE_KINGS_EXPEDITION
        ),
        DELIVERANCE_KINGS_ECHO(
                () -> ModItems.DELIVERANCE_KINGS_ECHO.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DELIVERANCE_KINGS_ECHO
        ),
        DELIVERANCE_LONE_RESIDUAL_DREAM(
                () -> ModItems.DELIVERANCE_LONE_RESIDUAL_DREAM.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DELIVERANCE_LONE_RESIDUAL_DREAM
        ),
        DELIVERANCE_LONE_DECISION(
                () -> ModItems.DELIVERANCE_LONE_DECISION.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DELIVERANCE_LONE_DECISION
        ),
        DELIVERANCE_LONE_TRIUMPH(
                () -> ModItems.DELIVERANCE_LONE_TRIUMPH.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DELIVERANCE_LONE_TRIUMPH
        ),
        DISCIPLINE_FOUR(
                () -> ModItems.DISCIPLINE_FOUR.get(),
                SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DISCIPLINE_FOUR,
                true
        ),
        DISCIPLINE_FIVE(
                () -> ModItems.DISCIPLINE_FIVE.get(),
                SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DISCIPLINE_FIVE,
                true
        ),
        DISCIPLINE_SIX(
                () -> ModItems.DISCIPLINE_SIX.get(),
                SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DISCIPLINE_SIX,
                true
        ),
        DISCIPLINE_ASCETICISM(
                () -> ModItems.DISCIPLINE_ASCETICISM.get(),
                DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.DisciplineCore.ASCETICISM
        ),
        DISCIPLINE_BLESSING(
                () -> ModItems.DISCIPLINE_BLESSING.get(),
                DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.DisciplineCore.BLESSING
        ),
        DISCIPLINE_ASCETIC_BODY(
                () -> ModItems.DISCIPLINE_ASCETIC_BODY.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DISCIPLINE_ASCETIC_BODY,
                SignetUpgradeData.DisciplineCore.ASCETICISM
        ),
        DISCIPLINE_ASCETIC_LIFE(
                () -> ModItems.DISCIPLINE_ASCETIC_LIFE.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DISCIPLINE_ASCETIC_LIFE,
                SignetUpgradeData.DisciplineCore.ASCETICISM
        ),
        DISCIPLINE_ASCETIC_PROOF(
                () -> ModItems.DISCIPLINE_ASCETIC_PROOF.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DISCIPLINE_ASCETIC_PROOF,
                SignetUpgradeData.DisciplineCore.ASCETICISM
        ),
        DISCIPLINE_BLESSING_PUNISHMENT(
                () -> ModItems.DISCIPLINE_BLESSING_PUNISHMENT.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DISCIPLINE_BLESSING_PUNISHMENT,
                SignetUpgradeData.DisciplineCore.BLESSING
        ),
        DISCIPLINE_BLESSING_VIRTUE(
                () -> ModItems.DISCIPLINE_BLESSING_VIRTUE.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DISCIPLINE_BLESSING_VIRTUE,
                SignetUpgradeData.DisciplineCore.BLESSING
        ),
        DISCIPLINE_BLESSING_RETURN(
                () -> ModItems.DISCIPLINE_BLESSING_RETURN.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DISCIPLINE_BLESSING_RETURN,
                SignetUpgradeData.DisciplineCore.BLESSING
        ),
        BODHI_HEAVENLY_EYE(
                () -> ModItems.BODHI_HEAVENLY_EYE.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_HEAVENLY_EYE, true
        ),
        BODHI_MIND_READING(
                () -> ModItems.BODHI_MIND_READING.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_MIND_READING, true
        ),
        BODHI_DIVINE_FOOT(
                () -> ModItems.BODHI_DIVINE_FOOT.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_DIVINE_FOOT, true
        ),
        BODHI_EXHAUSTION(
                () -> ModItems.BODHI_EXHAUSTION.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_EXHAUSTION, true
        ),
        BODHI_TRUE_WORD(
                () -> ModItems.BODHI_TRUE_WORD.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.BodhiCore.TRUE_WORD
        ),
        BODHI_SECRET_WORD(
                () -> ModItems.BODHI_SECRET_WORD.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.BodhiCore.SECRET_WORD
        ),
        BODHI_TRUE_SELFLESS(
                () -> ModItems.BODHI_TRUE_SELFLESS.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_TRUE_SELFLESS,
                SignetUpgradeData.BodhiCore.TRUE_WORD
        ),
        BODHI_TRUE_IMPERMANENCE(
                () -> ModItems.BODHI_TRUE_IMPERMANENCE.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_TRUE_IMPERMANENCE,
                SignetUpgradeData.BodhiCore.TRUE_WORD
        ),
        BODHI_TRUE_SILENCE(
                () -> ModItems.BODHI_TRUE_SILENCE.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_TRUE_SILENCE,
                SignetUpgradeData.BodhiCore.TRUE_WORD
        ),
        BODHI_SECRET_IMPERMANENCE(
                () -> ModItems.BODHI_SECRET_IMPERMANENCE.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_SECRET_IMPERMANENCE,
                SignetUpgradeData.BodhiCore.SECRET_WORD
        ),
        BODHI_SECRET_SELFLESS(
                () -> ModItems.BODHI_SECRET_SELFLESS.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_SECRET_SELFLESS,
                SignetUpgradeData.BodhiCore.SECRET_WORD
        ),
        BODHI_SECRET_SILENCE(
                () -> ModItems.BODHI_SECRET_SILENCE.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.BODHI_SECRET_SILENCE,
                SignetUpgradeData.BodhiCore.SECRET_WORD
        ),
        DECIMATION_BLADE(
                () -> ModItems.DECIMATION_BLADE.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DECIMATION_BLADE, true
        ),
        DECIMATION_WUWANG(
                () -> ModItems.DECIMATION_WUWANG.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DECIMATION_WUWANG, true
        ),
        DECIMATION_WEISHEN(
                () -> ModItems.DECIMATION_WEISHEN.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.DECIMATION_WEISHEN, true
        ),
        DECIMATION_BLOOD_RELEASE(
                () -> ModItems.DECIMATION_BLOOD_RELEASE.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.DecimationCore.BLOOD_RELEASE
        ),
        DECIMATION_BLOOD_RAIMENT(
                () -> ModItems.DECIMATION_BLOOD_RAIMENT.get(), DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.DecimationCore.BLOOD_RAIMENT
        ),
        DECIMATION_RELEASE_NONHUMAN(
                () -> ModItems.DECIMATION_RELEASE_NONHUMAN.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DECIMATION_RELEASE_NONHUMAN,
                SignetUpgradeData.DecimationCore.BLOOD_RELEASE
        ),
        DECIMATION_RELEASE_ONE_PATH(
                () -> ModItems.DECIMATION_RELEASE_ONE_PATH.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DECIMATION_RELEASE_ONE_PATH,
                SignetUpgradeData.DecimationCore.BLOOD_RELEASE
        ),
        DECIMATION_RELEASE_KALPAS(
                () -> ModItems.DECIMATION_RELEASE_KALPAS.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DECIMATION_RELEASE_KALPAS,
                SignetUpgradeData.DecimationCore.BLOOD_RELEASE
        ),
        DECIMATION_RAIMENT_UNDYING(
                () -> ModItems.DECIMATION_RAIMENT_UNDYING.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DECIMATION_RAIMENT_UNDYING,
                SignetUpgradeData.DecimationCore.BLOOD_RAIMENT
        ),
        DECIMATION_RAIMENT_BURNING_SOUL(
                () -> ModItems.DECIMATION_RAIMENT_BURNING_SOUL.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DECIMATION_RAIMENT_BURNING_SOUL,
                SignetUpgradeData.DecimationCore.BLOOD_RAIMENT
        ),
        DECIMATION_RAIMENT_HOMELAND(
                () -> ModItems.DECIMATION_RAIMENT_HOMELAND.get(), DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.DECIMATION_RAIMENT_HOMELAND,
                SignetUpgradeData.DecimationCore.BLOOD_RAIMENT
        ),
        HELIX_SPOON(
                () -> ModItems.HELIX_SPOON.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.HELIX_SPOON, true
        ),
        HELIX_COBWEB(
                () -> ModItems.HELIX_COBWEB.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.HELIX_COBWEB, true
        ),
        HELIX_SNEER(
                () -> ModItems.HELIX_SNEER.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.HELIX_SNEER, true
        ),
        HELIX_REVERSED_SPIRAL(
                () -> ModItems.HELIX_REVERSED_SPIRAL.get(),
                DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.HelixCore.REVERSED_SPIRAL
        ),
        HELIX_CONCLUSION_SPIRAL(
                () -> ModItems.HELIX_CONCLUSION_SPIRAL.get(),
                DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.HelixCore.CONCLUSION_SPIRAL
        ),
        HELIX_REVERSED_DOOR(
                () -> ModItems.HELIX_REVERSED_DOOR.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.HELIX_REVERSED_DOOR,
                SignetUpgradeData.HelixCore.REVERSED_SPIRAL
        ),
        HELIX_REVERSED_HALL(
                () -> ModItems.HELIX_REVERSED_HALL.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.HELIX_REVERSED_HALL,
                SignetUpgradeData.HelixCore.REVERSED_SPIRAL
        ),
        HELIX_REVERSED_TRUTH(
                () -> ModItems.HELIX_REVERSED_TRUTH.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.HELIX_REVERSED_TRUTH,
                SignetUpgradeData.HelixCore.REVERSED_SPIRAL
        ),
        HELIX_CONCLUSION_PRISON(
                () -> ModItems.HELIX_CONCLUSION_PRISON.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.HELIX_CONCLUSION_PRISON,
                SignetUpgradeData.HelixCore.CONCLUSION_SPIRAL
        ),
        HELIX_CONCLUSION_ORIGIN(
                () -> ModItems.HELIX_CONCLUSION_ORIGIN.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.HELIX_CONCLUSION_ORIGIN,
                SignetUpgradeData.HelixCore.CONCLUSION_SPIRAL
        ),
        HELIX_CONCLUSION_POINTER(
                () -> ModItems.HELIX_CONCLUSION_POINTER.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.HELIX_CONCLUSION_POINTER,
                SignetUpgradeData.HelixCore.CONCLUSION_SPIRAL
        ),
        GOLD_BIRD(
                () -> ModItems.GOLD_BIRD.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_BIRD, true
        ),
        GOLD_ORCHARD(
                () -> ModItems.GOLD_ORCHARD.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_ORCHARD, true
        ),
        GOLD_WINE(
                () -> ModItems.GOLD_WINE.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_WINE, true
        ),
        GOLD_JADE(
                () -> ModItems.GOLD_JADE.get(), SECONDARY_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_JADE, true
        ),
        GOLD_ARIA(
                () -> ModItems.GOLD_ARIA.get(),
                DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.GoldCore.ARIA
        ),
        GOLD_ECHO(
                () -> ModItems.GOLD_ECHO.get(),
                DELIVERANCE_CORE_PRICE,
                SignetUpgradeData.GoldCore.ECHO
        ),
        GOLD_ARIA_FERTILE(
                () -> ModItems.GOLD_ARIA_FERTILE.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_ARIA_FERTILE,
                SignetUpgradeData.GoldCore.ARIA
        ),
        GOLD_ARIA_LIFE(
                () -> ModItems.GOLD_ARIA_LIFE.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_ARIA_LIFE,
                SignetUpgradeData.GoldCore.ARIA
        ),
        GOLD_ARIA_DAWN(
                () -> ModItems.GOLD_ARIA_DAWN.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_ARIA_DAWN,
                SignetUpgradeData.GoldCore.ARIA
        ),
        GOLD_ECHO_WITHERED(
                () -> ModItems.GOLD_ECHO_WITHERED.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_ECHO_WITHERED,
                SignetUpgradeData.GoldCore.ECHO
        ),
        GOLD_ECHO_COLD_WIND(
                () -> ModItems.GOLD_ECHO_COLD_WIND.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_ECHO_COLD_WIND,
                SignetUpgradeData.GoldCore.ECHO
        ),
        GOLD_ECHO_SILENT_NIGHT(
                () -> ModItems.GOLD_ECHO_SILENT_NIGHT.get(),
                DELIVERANCE_CORE_SIGNET_PRICE,
                SignetUpgradeData.Skill.GOLD_ECHO_SILENT_NIGHT,
                SignetUpgradeData.GoldCore.ECHO
        ),
        CORE_OF_REASON(() -> ModItems.CORE_OF_REASON.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_VOID(() -> ModItems.CORE_OF_VOID.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_THUNDER(() -> ModItems.CORE_OF_THUNDER.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_WIND(() -> ModItems.CORE_OF_WIND.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_ICE(() -> ModItems.CORE_OF_ICE.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_DEATH(() -> ModItems.CORE_OF_DEATH.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_FIRE(() -> ModItems.CORE_OF_FIRE.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_SENTIENCE(() -> ModItems.CORE_OF_SENTIENCE.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_EARTH(() -> ModItems.CORE_OF_EARTH.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_DOMINANCE(() -> ModItems.CORE_OF_DOMINANCE.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_BINDING(() -> ModItems.CORE_OF_BINDING.get(), HERRSCHER_CORE_PRICE, true),
        CORE_OF_CORROSION(() -> ModItems.CORE_OF_CORROSION.get(), HERRSCHER_CORE_PRICE, true),
        VICISSITUDE_SIGNET(
                () -> ModItems.SIGNET_OF_VICISSITUDE.get(), BASE_SIGNET_PRICE, false
        ),
        STARS_SIGNET(() -> ModItems.SIGNET_OF_STARS.get(), BASE_SIGNET_PRICE, false),
        INFINITY_SIGNET(() -> ModItems.SIGNET_OF_INFINITY.get(), BASE_SIGNET_PRICE, false),
        DAYBREAK_SIGNET(() -> ModItems.SIGNET_OF_DAYBREAK.get(), BASE_SIGNET_PRICE, false),
        SETSURA_SIGNET(() -> ModItems.SIGNET_OF_SETSURA.get(), BASE_SIGNET_PRICE, false),
        BODHI_SIGNET(() -> ModItems.SIGNET_OF_BODHI.get(), BASE_SIGNET_PRICE, false),
        DECIMATION_SIGNET(() -> ModItems.SIGNET_OF_DECIMATION.get(), BASE_SIGNET_PRICE, false),
        HELIX_SIGNET(() -> ModItems.SIGNET_OF_HELIX.get(), BASE_SIGNET_PRICE, false),
        GOLD_SIGNET(() -> ModItems.SIGNET_OF_GOLD.get(), BASE_SIGNET_PRICE, false),
        DISCIPLINE_SIGNET(() -> ModItems.SIGNET_OF_DISCIPLINE.get(), BASE_SIGNET_PRICE, false),
        DELIVERANCE_SIGNET(
                () -> ModItems.SIGNET_OF_DELIVERANCE.get(), BASE_SIGNET_PRICE, false
        );

        private final Supplier<Item> item;
        private final int price;
        private final SignetUpgradeData.Signet signet;
        private final SignetUpgradeData.Skill unlockSkill;
        private final SignetUpgradeData.DeliveranceCore deliveranceCore;
        private final SignetUpgradeData.Skill deliveranceCoreSignet;
        private final SignetUpgradeData.DisciplineCore disciplineCore;
        private final SignetUpgradeData.Skill disciplineCoreSignet;
        private final SignetUpgradeData.GoldCore goldCore;
        private final SignetUpgradeData.Skill goldCoreSignet;
        private SignetUpgradeData.HelixCore helixCore;
        private SignetUpgradeData.Skill helixCoreSignet;
        private SignetUpgradeData.DecimationCore decimationCore;
        private SignetUpgradeData.Skill decimationCoreSignet;
        private SignetUpgradeData.BodhiCore bodhiCore;
        private SignetUpgradeData.Skill bodhiCoreSignet;
        private SignetUpgradeData.SetsuraCore setsuraCore;
        private SignetUpgradeData.Skill setsuraCoreSignet;
        private SignetUpgradeData.DaybreakCore daybreakCore;
        private SignetUpgradeData.Skill daybreakCoreSignet;
        private SignetUpgradeData.ExtensionCore extensionCore;
        private SignetUpgradeData.Skill extensionCoreSignet;
        private final boolean herrscherCore;

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Signet signet) {
            this.item = item;
            this.price = price;
            this.signet = signet;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, boolean herrscherCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.herrscherCore = herrscherCore;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Skill unlockSkill,
                    boolean directUnlock) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = unlockSkill;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(
                Supplier<Item> item,
                int price,
                SignetUpgradeData.DeliveranceCore deliveranceCore
        ) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = deliveranceCore;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Skill coreSignet) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = coreSignet;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.DisciplineCore disciplineCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = disciplineCore;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Skill coreSignet,
                    SignetUpgradeData.DisciplineCore ignoredCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = coreSignet;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.GoldCore goldCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = goldCore;
            this.goldCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Skill coreSignet,
                    SignetUpgradeData.GoldCore ignoredCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = coreSignet;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.HelixCore helixCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.helixCore = helixCore;
            this.helixCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price,
                    SignetUpgradeData.DecimationCore decimationCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.decimationCore = decimationCore;
            this.decimationCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Skill coreSignet,
                    SignetUpgradeData.DecimationCore ignoredCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.decimationCore = null;
            this.decimationCoreSignet = coreSignet;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.BodhiCore bodhiCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.bodhiCore = bodhiCore;
            this.bodhiCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Skill coreSignet,
                    SignetUpgradeData.BodhiCore ignoredCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.bodhiCore = null;
            this.bodhiCoreSignet = coreSignet;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.SetsuraCore setsuraCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.setsuraCore = setsuraCore;
            this.setsuraCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.DaybreakCore daybreakCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.daybreakCore = daybreakCore;
            this.daybreakCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Skill coreSignet,
                    SignetUpgradeData.DaybreakCore ignoredCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.daybreakCore = null;
            this.daybreakCoreSignet = coreSignet;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Skill coreSignet,
                    SignetUpgradeData.SetsuraCore ignoredCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.setsuraCore = null;
            this.setsuraCoreSignet = coreSignet;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price, SignetUpgradeData.Skill coreSignet,
                    SignetUpgradeData.HelixCore ignoredCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.helixCore = null;
            this.helixCoreSignet = coreSignet;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price,
                    SignetUpgradeData.ExtensionCore extensionCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.extensionCore = extensionCore;
            this.extensionCoreSignet = null;
            this.herrscherCore = false;
        }

        ShopProduct(Supplier<Item> item, int price,
                    SignetUpgradeData.Skill coreSignet,
                    SignetUpgradeData.ExtensionCore ignoredCore) {
            this.item = item;
            this.price = price;
            this.signet = null;
            this.unlockSkill = null;
            this.deliveranceCore = null;
            this.deliveranceCoreSignet = null;
            this.disciplineCore = null;
            this.disciplineCoreSignet = null;
            this.goldCore = null;
            this.goldCoreSignet = null;
            this.extensionCore = null;
            this.extensionCoreSignet = coreSignet;
            this.herrscherCore = false;
        }

        Item item() {
            return this.item.get();
        }

        int price(Player player) {
            return getShopPrice(player, this.price);
        }

        SignetUpgradeData.Signet coreSystemSignet() {
            if (this.deliveranceCore != null || this.deliveranceCoreSignet != null) {
                return SignetUpgradeData.Signet.DELIVERANCE;
            }
            if (this.disciplineCore != null || this.disciplineCoreSignet != null) {
                return SignetUpgradeData.Signet.DISCIPLINE;
            }
            if (this.goldCore != null || this.goldCoreSignet != null) {
                return SignetUpgradeData.Signet.GOLD;
            }
            if (this.helixCore != null || this.helixCoreSignet != null) {
                return SignetUpgradeData.Signet.HELIX;
            }
            if (this.decimationCore != null || this.decimationCoreSignet != null) {
                return SignetUpgradeData.Signet.DECIMATION;
            }
            if (this.bodhiCore != null || this.bodhiCoreSignet != null) {
                return SignetUpgradeData.Signet.BODHI;
            }
            if (this.setsuraCore != null || this.setsuraCoreSignet != null) {
                return SignetUpgradeData.Signet.SETSURA;
            }
            if (this.daybreakCore != null || this.daybreakCoreSignet != null) {
                return SignetUpgradeData.Signet.DAYBREAK;
            }
            if (this.extensionCore != null) return this.extensionCore.signet();
            return this.extensionCoreSignet == null ? null : this.extensionCoreSignet.signet();
        }

        boolean wasCorePurchased(Player player) {
            if (this.deliveranceCore != null) {
                return this.deliveranceCore == SignetUpgradeData.getPurchasedDeliveranceCore(player);
            }
            if (this.deliveranceCoreSignet != null) {
                return SignetUpgradeData.hasPurchasedDeliveranceCoreSignet(
                        player, this.deliveranceCoreSignet);
            }
            if (this.disciplineCore != null) {
                return this.disciplineCore == SignetUpgradeData.getPurchasedDisciplineCore(player);
            }
            if (this.disciplineCoreSignet != null) {
                return SignetUpgradeData.hasPurchasedDisciplineCoreSignet(
                        player, this.disciplineCoreSignet);
            }
            if (this.goldCore != null) {
                return this.goldCore == SignetUpgradeData.getPurchasedGoldCore(player);
            }
            if (this.goldCoreSignet != null) {
                return SignetUpgradeData.hasPurchasedGoldCoreSignet(player, this.goldCoreSignet);
            }
            if (this.helixCore != null) {
                return this.helixCore == SignetUpgradeData.getPurchasedHelixCore(player);
            }
            if (this.helixCoreSignet != null) {
                return SignetUpgradeData.hasPurchasedHelixCoreSignet(player, this.helixCoreSignet);
            }
            if (this.decimationCore != null) {
                return this.decimationCore == SignetUpgradeData.getPurchasedDecimationCore(player);
            }
            if (this.decimationCoreSignet != null) {
                return SignetUpgradeData.hasPurchasedDecimationCoreSignet(
                        player, this.decimationCoreSignet);
            }
            if (this.bodhiCore != null) {
                return this.bodhiCore == SignetUpgradeData.getPurchasedBodhiCore(player);
            }
            if (this.bodhiCoreSignet != null) {
                return SignetUpgradeData.hasPurchasedBodhiCoreSignet(player, this.bodhiCoreSignet);
            }
            if (this.setsuraCore != null) {
                return this.setsuraCore == SignetUpgradeData.getPurchasedSetsuraCore(player);
            }
            if (this.setsuraCoreSignet != null) {
                return SignetUpgradeData.hasPurchasedSetsuraCoreSignet(player, this.setsuraCoreSignet);
            }
            if (this.daybreakCore != null) {
                return this.daybreakCore == SignetUpgradeData.getPurchasedDaybreakCore(player);
            }
            if (this.daybreakCoreSignet != null) {
                return SignetUpgradeData.hasPurchasedDaybreakCoreSignet(
                        player, this.daybreakCoreSignet);
            }
            if (this.extensionCore != null) {
                return this.extensionCore == SignetUpgradeData.getPurchasedExtensionCore(
                        player, this.extensionCore.signet());
            }
            return this.extensionCoreSignet != null
                    && this.extensionCoreSignet.extensionCore()
                    == SignetUpgradeData.getSelectedExtensionCore(
                            player, this.extensionCoreSignet.signet())
                    && SignetUpgradeData.hasPurchasedExtensionCoreSignet(
                            player, this.extensionCoreSignet);
        }

        boolean isUnavailable(Player player) {
            if (hasPurchasedProduct(player, this)) return true;
            if (isBaseSignet() && hasItemOrCurio(player, item())) return true;
            if (this == REVERIE_SIGNET) return hasPurchasedReverie(player);
            if (this.herrscherCore) {
                return CocoonCoreProgress.hasAcquiredCore(
                        player,
                        item()
                );
            }
            if (this.unlockSkill != null) {
                return hasPurchasedSecondary(player, this.unlockSkill);
            }
            if (this.deliveranceCore != null) {
                return SignetUpgradeData.getSelectedDeliveranceCore(player) != null;
            }
            if (this.deliveranceCoreSignet != null) {
                SignetUpgradeData.DeliveranceCore selected =
                        SignetUpgradeData.getSelectedDeliveranceCore(player);
                return selected != this.deliveranceCoreSignet.deliveranceCore()
                        || SignetUpgradeData.hasPurchasedDeliveranceCoreSignet(
                        player,
                        this.deliveranceCoreSignet
                )
                        || SignetUpgradeData.hasDeliveranceCoreSignet(
                        player,
                        this.deliveranceCoreSignet
                );
            }
            if (this.disciplineCore != null) {
                return SignetUpgradeData.getSelectedDisciplineCore(player) != null;
            }
            if (this.disciplineCoreSignet != null) {
                SignetUpgradeData.DisciplineCore selected =
                        SignetUpgradeData.getSelectedDisciplineCore(player);
                return selected != this.disciplineCoreSignet.disciplineCore()
                        || SignetUpgradeData.hasPurchasedDisciplineCoreSignet(
                        player, this.disciplineCoreSignet)
                        || SignetUpgradeData.hasDisciplineCoreSignet(
                        player, this.disciplineCoreSignet);
            }
            if (this.goldCore != null) {
                return SignetUpgradeData.getSelectedGoldCore(player) != null;
            }
            if (this.goldCoreSignet != null) {
                SignetUpgradeData.GoldCore selected = SignetUpgradeData.getSelectedGoldCore(player);
                return selected != this.goldCoreSignet.goldCore()
                        || SignetUpgradeData.hasPurchasedGoldCoreSignet(player, this.goldCoreSignet)
                        || SignetUpgradeData.hasGoldCoreSignet(player, this.goldCoreSignet);
            }
            if (this.helixCore != null) {
                return SignetUpgradeData.getSelectedHelixCore(player) != null;
            }
            if (this.helixCoreSignet != null) {
                SignetUpgradeData.HelixCore selected = SignetUpgradeData.getSelectedHelixCore(player);
                return selected != this.helixCoreSignet.helixCore()
                        || SignetUpgradeData.hasPurchasedHelixCoreSignet(player, this.helixCoreSignet)
                        || SignetUpgradeData.hasHelixCoreSignet(player, this.helixCoreSignet);
            }
            if (this.decimationCore != null) {
                return SignetUpgradeData.getSelectedDecimationCore(player) != null;
            }
            if (this.decimationCoreSignet != null) {
                SignetUpgradeData.DecimationCore selected =
                        SignetUpgradeData.getSelectedDecimationCore(player);
                return selected != this.decimationCoreSignet.decimationCore()
                        || SignetUpgradeData.hasPurchasedDecimationCoreSignet(
                        player, this.decimationCoreSignet)
                        || SignetUpgradeData.hasDecimationCoreSignet(
                        player, this.decimationCoreSignet);
            }
            if (this.bodhiCore != null) {
                return SignetUpgradeData.getSelectedBodhiCore(player) != null;
            }
            if (this.bodhiCoreSignet != null) {
                SignetUpgradeData.BodhiCore selected = SignetUpgradeData.getSelectedBodhiCore(player);
                return selected != this.bodhiCoreSignet.bodhiCore()
                        || SignetUpgradeData.hasPurchasedBodhiCoreSignet(
                        player, this.bodhiCoreSignet)
                        || SignetUpgradeData.hasBodhiCoreSignet(player, this.bodhiCoreSignet);
            }
            if (this.setsuraCore != null) {
                return SignetUpgradeData.getSelectedSetsuraCore(player) != null;
            }
            if (this.setsuraCoreSignet != null) {
                SignetUpgradeData.SetsuraCore selected = SignetUpgradeData.getSelectedSetsuraCore(player);
                return selected != this.setsuraCoreSignet.setsuraCore()
                        || SignetUpgradeData.hasPurchasedSetsuraCoreSignet(
                        player, this.setsuraCoreSignet)
                        || SignetUpgradeData.hasSetsuraCoreSignet(player, this.setsuraCoreSignet);
            }
            if (this.daybreakCore != null) {
                return SignetUpgradeData.getSelectedDaybreakCore(player) != null;
            }
            if (this.daybreakCoreSignet != null) {
                SignetUpgradeData.DaybreakCore selected = SignetUpgradeData.getSelectedDaybreakCore(player);
                return selected != this.daybreakCoreSignet.daybreakCore()
                        || SignetUpgradeData.hasPurchasedDaybreakCoreSignet(
                        player, this.daybreakCoreSignet)
                        || SignetUpgradeData.hasDaybreakCoreSignet(player, this.daybreakCoreSignet);
            }
            if (this.extensionCore != null) {
                return SignetUpgradeData.getSelectedExtensionCore(
                        player, this.extensionCore.signet()) != null;
            }
            if (this.extensionCoreSignet != null) {
                SignetUpgradeData.ExtensionCore selected =
                        SignetUpgradeData.getSelectedExtensionCore(
                                player, this.extensionCoreSignet.signet());
                return selected != this.extensionCoreSignet.extensionCore()
                        || SignetUpgradeData.hasPurchasedExtensionCoreSignet(
                        player, this.extensionCoreSignet)
                        || SignetUpgradeData.hasExtensionCoreSignet(
                        player, this.extensionCoreSignet);
            }
            return this.signet != null && SignetUpgradeData.isSignetMaxed(player, this.signet);
        }

        boolean isPermanentProduct() {
            return this == REVERIE_SIGNET
                    || isBaseSignet()
                    || this.unlockSkill != null
                    || this.deliveranceCore != null
                    || this.deliveranceCoreSignet != null
                    || this.disciplineCore != null
                    || this.disciplineCoreSignet != null
                    || this.goldCore != null
                    || this.goldCoreSignet != null
                    || this.helixCore != null
                    || this.helixCoreSignet != null
                    || this.decimationCore != null
                    || this.decimationCoreSignet != null
                    || this.bodhiCore != null
                    || this.bodhiCoreSignet != null
                    || this.setsuraCore != null
                    || this.setsuraCoreSignet != null
                    || this.daybreakCore != null
                    || this.daybreakCoreSignet != null
                    || this.extensionCore != null
                    || this.extensionCoreSignet != null
                    || this.herrscherCore;
        }

        boolean isBaseSignet() {
            return this == VICISSITUDE_SIGNET
                    || this == STARS_SIGNET
                    || this == INFINITY_SIGNET
                    || this == DAYBREAK_SIGNET
                    || this == SETSURA_SIGNET
                    || this == BODHI_SIGNET
                    || this == DECIMATION_SIGNET
                    || this == HELIX_SIGNET
                    || this == GOLD_SIGNET
                    || this == DISCIPLINE_SIGNET
                    || this == DELIVERANCE_SIGNET;
        }

        static ShopProduct fromOrdinal(int ordinal) {
            ShopProduct[] products = values();
            return ordinal >= 0 && ordinal < products.length
                    ? products[ordinal]
                    : null;
        }
    }
}
