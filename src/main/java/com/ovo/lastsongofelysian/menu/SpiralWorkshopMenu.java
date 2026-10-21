package com.ovo.lastsongofelysian.menu;

import com.ovo.lastsongofelysian.util.ModText;
import com.ovo.lastsongofelysian.util.ModAdvancements;

import com.ovo.lastsongofelysian.event.SignetEffects;
import com.ovo.lastsongofelysian.event.BodhiSignetSystem;
import com.ovo.lastsongofelysian.event.DaybreakSignetSystem;
import com.ovo.lastsongofelysian.event.DecimationSignetSystem;
import com.ovo.lastsongofelysian.event.GoldSignetSystem;
import com.ovo.lastsongofelysian.event.HelixSignetSystem;
import com.ovo.lastsongofelysian.event.InfinitySignetSystem;
import com.ovo.lastsongofelysian.event.ReverieSignetSystem;
import com.ovo.lastsongofelysian.event.SetsunaSignetSystem;
import com.ovo.lastsongofelysian.event.StarsSignetSystem;
import com.ovo.lastsongofelysian.event.VicissitudeSignetSystem;
import com.ovo.lastsongofelysian.registry.ModMenus;
import com.ovo.lastsongofelysian.util.SignetUpgradeData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SpiralWorkshopMenu extends AbstractContainerMenu {

    public static final int SIGNET_SLOT_X = 42;
    public static final int SIGNET_SLOT_Y = 67;
    public static final int SECONDARY_SLOT_X = 42;
    public static final int SECONDARY_SLOT_Y = 116;
    public static final int PLAYER_INVENTORY_X = 112;
    public static final int PLAYER_INVENTORY_Y = 224;
    public static final int PLAYER_HOTBAR_Y = 282;
    public static final int MAX_SKILL_ROWS = 3;
    public static final int FUSE_BUTTON = MAX_SKILL_ROWS;
    public static final int PAGE_BUTTON = FUSE_BUTTON + 1;
    public static final int RESET_BUTTON = PAGE_BUTTON + 1;

    private static final int SIGNET_SLOT = 0;
    private static final int SECONDARY_SLOT = 1;
    private static final int PLAYER_INVENTORY_START = 2;
    private static final int PLAYER_INVENTORY_END = 29;
    private static final int PLAYER_HOTBAR_START = 29;
    private static final int PLAYER_HOTBAR_END = 38;
    private static final int DATA_UPGRADE_ITEMS = 0;
    private static final int DATA_EXPERIENCE_LEVEL = 1;
    private static final int DATA_DELIVERANCE_SECONDARY_UNLOCKS = 2;
    private static final int DATA_DELIVERANCE_CORE = 3;
    private static final int DATA_DELIVERANCE_CORE_SIGNETS = 4;
    private static final int DATA_SKILL_PAGE = 5;
    private static final int DATA_HAS_CORE_PROGRESS = 6;
    private static final int DATA_DISCIPLINE_CORE = 7;
    private static final int DATA_DISCIPLINE_CORE_SIGNETS = 8;
    private static final int DATA_DISCIPLINE_SECONDARY_UNLOCKS = 9;
    private static final int DATA_GOLD_CORE = 10;
    private static final int DATA_GOLD_CORE_SIGNETS = 11;
    private static final int DATA_GOLD_SECONDARY_UNLOCKS = 12;
    private static final int DATA_HELIX_CORE = 13;
    private static final int DATA_HELIX_CORE_SIGNETS = 14;
    private static final int DATA_HELIX_SECONDARY_UNLOCKS = 15;
    private static final int DATA_DECIMATION_CORE = 16;
    private static final int DATA_DECIMATION_CORE_SIGNETS = 17;
    private static final int DATA_DECIMATION_SECONDARY_UNLOCKS = 18;
    private static final int DATA_BODHI_CORE = 19;
    private static final int DATA_BODHI_CORE_SIGNETS = 20;
    private static final int DATA_BODHI_SECONDARY_UNLOCKS = 21;
    private static final int DATA_SETSURA_CORE = 22;
    private static final int DATA_SETSURA_CORE_SIGNETS = 23;
    private static final int DATA_SETSURA_SECONDARY_UNLOCKS = 24;
    private static final int DATA_DAYBREAK_CORE = 25;
    private static final int DATA_DAYBREAK_CORE_SIGNETS = 26;
    private static final int DATA_DAYBREAK_SECONDARY_UNLOCKS = 27;
    private static final int DATA_EXTENSION_CORE = 28;
    private static final int DATA_EXTENSION_CORE_SIGNETS = 29;
    private static final int DATA_EXTENSION_SECONDARY_UNLOCKS = 30;
    private static final int DATA_COUNT = 31;

    private final Inventory playerInventory;
    private final Container signetInput;
    private final Container secondaryInput;
    private final ContainerData workshopData;

    public SpiralWorkshopMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.SPIRAL_WORKSHOP_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.signetInput = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                SpiralWorkshopMenu.this.slotsChanged(this);
            }
        };
        this.secondaryInput = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                SpiralWorkshopMenu.this.slotsChanged(this);
            }
        };
        this.workshopData = new SimpleContainerData(DATA_COUNT);

        this.addSlot(new Slot(this.signetInput, 0, SIGNET_SLOT_X, SIGNET_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SignetUpgradeData.isUpgradeableSignet(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(this.secondaryInput, 0, SECONDARY_SLOT_X, SECONDARY_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (!SignetUpgradeData.isAnyFusionItem(stack)) return false;
                SignetUpgradeData.ExtensionCore extensionItemCore =
                        SignetUpgradeData.extensionCoreFromItem(stack);
                SignetUpgradeData.Skill extensionCoreSignet =
                        SignetUpgradeData.extensionCoreSignetFromItem(stack);
                if (extensionItemCore != null || extensionCoreSignet != null) {
                    SignetUpgradeData.Signet signet = extensionItemCore != null
                            ? extensionItemCore.signet() : extensionCoreSignet.signet();
                    SignetUpgradeData.ExtensionCore selected =
                            SignetUpgradeData.getSelectedExtensionCore(playerInventory.player, signet);
                    return extensionItemCore != null
                            ? selected == null || selected == extensionItemCore
                            : selected == null || selected == extensionCoreSignet.extensionCore();
                }
                SignetUpgradeData.DaybreakCore daybreakItemCore =
                        SignetUpgradeData.daybreakCoreFromItem(stack);
                SignetUpgradeData.Skill daybreakCoreSignet =
                        SignetUpgradeData.daybreakCoreSignetFromItem(stack);
                if (daybreakItemCore != null || daybreakCoreSignet != null) {
                    SignetUpgradeData.DaybreakCore selected =
                            SignetUpgradeData.getSelectedDaybreakCore(playerInventory.player);
                    return daybreakItemCore != null
                            ? selected == null || selected == daybreakItemCore
                            : selected == null || selected == daybreakCoreSignet.daybreakCore();
                }
                SignetUpgradeData.SetsuraCore setsuraItemCore =
                        SignetUpgradeData.setsuraCoreFromItem(stack);
                SignetUpgradeData.Skill setsuraCoreSignet =
                        SignetUpgradeData.setsuraCoreSignetFromItem(stack);
                if (setsuraItemCore != null || setsuraCoreSignet != null) {
                    SignetUpgradeData.SetsuraCore selected =
                            SignetUpgradeData.getSelectedSetsuraCore(playerInventory.player);
                    return setsuraItemCore != null
                            ? selected == null || selected == setsuraItemCore
                            : selected == null || selected == setsuraCoreSignet.setsuraCore();
                }
                SignetUpgradeData.BodhiCore bodhiItemCore =
                        SignetUpgradeData.bodhiCoreFromItem(stack);
                SignetUpgradeData.Skill bodhiCoreSignet =
                        SignetUpgradeData.bodhiCoreSignetFromItem(stack);
                if (bodhiItemCore != null || bodhiCoreSignet != null) {
                    SignetUpgradeData.BodhiCore selected =
                            SignetUpgradeData.getSelectedBodhiCore(playerInventory.player);
                    return bodhiItemCore != null
                            ? selected == null || selected == bodhiItemCore
                            : selected == null || selected == bodhiCoreSignet.bodhiCore();
                }
                SignetUpgradeData.DecimationCore decimationItemCore =
                        SignetUpgradeData.decimationCoreFromItem(stack);
                SignetUpgradeData.Skill decimationCoreSignet =
                        SignetUpgradeData.decimationCoreSignetFromItem(stack);
                if (decimationItemCore != null || decimationCoreSignet != null) {
                    SignetUpgradeData.DecimationCore selected =
                            SignetUpgradeData.getSelectedDecimationCore(playerInventory.player);
                    return decimationItemCore != null
                            ? selected == null || selected == decimationItemCore
                            : selected == null || selected == decimationCoreSignet.decimationCore();
                }
                SignetUpgradeData.HelixCore helixItemCore = SignetUpgradeData.helixCoreFromItem(stack);
                SignetUpgradeData.Skill helixCoreSignet = SignetUpgradeData.helixCoreSignetFromItem(stack);
                if (helixItemCore != null || helixCoreSignet != null) {
                    SignetUpgradeData.HelixCore selected =
                            SignetUpgradeData.getSelectedHelixCore(playerInventory.player);
                    return helixItemCore != null
                            ? selected == null || selected == helixItemCore
                            : selected == null || selected == helixCoreSignet.helixCore();
                }
                SignetUpgradeData.GoldCore goldItemCore = SignetUpgradeData.goldCoreFromItem(stack);
                SignetUpgradeData.Skill goldCoreSignet = SignetUpgradeData.goldCoreSignetFromItem(stack);
                if (goldItemCore != null || goldCoreSignet != null) {
                    SignetUpgradeData.GoldCore selected =
                            SignetUpgradeData.getSelectedGoldCore(playerInventory.player);
                    return goldItemCore != null
                            ? selected == null || selected == goldItemCore
                            : selected == null || selected == goldCoreSignet.goldCore();
                }
                SignetUpgradeData.DisciplineCore disciplineItemCore =
                        SignetUpgradeData.disciplineCoreFromItem(stack);
                SignetUpgradeData.Skill disciplineCoreSignet =
                        SignetUpgradeData.disciplineCoreSignetFromItem(stack);
                if (disciplineItemCore != null || disciplineCoreSignet != null) {
                    SignetUpgradeData.DisciplineCore selected =
                            SignetUpgradeData.getSelectedDisciplineCore(playerInventory.player);
                    return disciplineItemCore != null
                            ? selected == null || selected == disciplineItemCore
                            : selected == null || selected == disciplineCoreSignet.disciplineCore();
                }
                SignetUpgradeData.DeliveranceCore itemCore =
                        SignetUpgradeData.deliveranceCoreFromItem(stack);
                SignetUpgradeData.DeliveranceCore selectedCore =
                        SignetUpgradeData.getSelectedDeliveranceCore(playerInventory.player);
                if (itemCore != null) {
                    return selectedCore == null || selectedCore == itemCore;
                }
                SignetUpgradeData.Skill coreSignet =
                        SignetUpgradeData.deliveranceCoreSignetFromItem(stack);
                return coreSignet == null
                        || selectedCore == null
                        || selectedCore == coreSignet.deliveranceCore();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        PLAYER_INVENTORY_X + column * 18,
                        PLAYER_INVENTORY_Y + row * 18
                ));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(
                    playerInventory,
                    column,
                    PLAYER_INVENTORY_X + column * 18,
                    PLAYER_HOTBAR_Y
            ));
        }

        this.addDataSlots(this.workshopData);
        refreshData();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == FUSE_BUTTON) {
            if (!player.level().isClientSide()) {
                fuseSecondarySignet(player);
            }
            return true;
        }
        if (buttonId == PAGE_BUTTON) {
            if (!player.level().isClientSide()) {
                int skillCount = SignetUpgradeData.workshopSkills(
                        player,
                        SignetUpgradeData.fromStack(this.signetInput.getItem(0))
                ).size();
                int pageCount = Math.max(1, (skillCount + MAX_SKILL_ROWS - 1) / MAX_SKILL_ROWS);
                this.workshopData.set(
                        DATA_SKILL_PAGE,
                        (this.workshopData.get(DATA_SKILL_PAGE) + 1) % pageCount
                );
                this.broadcastChanges();
            }
            return true;
        }
        if (buttonId == RESET_BUTTON) {
            if (!player.level().isClientSide()) {
                resetCore(player);
            }
            return true;
        }
        if (buttonId < 0 || buttonId >= MAX_SKILL_ROWS) return false;
        if (player.level().isClientSide()) return true;

        ItemStack stack = this.signetInput.getItem(0);
        SignetUpgradeData.Signet signet = SignetUpgradeData.fromStack(stack);
        int skillIndex = this.workshopData.get(DATA_SKILL_PAGE) * MAX_SKILL_ROWS + buttonId;
        SignetUpgradeData.Skill skill = SignetUpgradeData.skillAt(player, signet, skillIndex);
        if (signet == null || skill == null) return true;

        if (!skill.isUpgradeable()) {
            player.displayClientMessage(
                    ModText.literal(skill.displayName() + " 无法升级")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
            return true;
        }

        SignetUpgradeData.synchronizeLevels(player, stack, signet);
        int level = SignetUpgradeData.getPlayerLevel(player, skill);
        if (level >= SignetUpgradeData.MAX_LEVEL) {
            player.displayClientMessage(
                    ModText.literal(skill.displayName() + " 已满级")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
            return true;
        }

        boolean creative = player.getAbilities().instabuild;
        int trackedSilverCost = 0;
        if (!creative) {
            int upgradeItems = SignetUpgradeData.countUpgradeItems(player, signet);
            if (upgradeItems < SignetUpgradeData.UPGRADE_ITEM_COST) {
                player.displayClientMessage(
                        ModText.literal("缺少" + signet.displayName() + "刻印升级")
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
                return true;
            }

            if (player.experienceLevel < SignetUpgradeData.EXPERIENCE_LEVEL_COST) {
                player.displayClientMessage(
                        ModText.literal("等级不足：" + player.experienceLevel + "/" + SignetUpgradeData.EXPERIENCE_LEVEL_COST)
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
                return true;
            }

            if (SignetUpgradeData.isAnyCoreSignet(skill)) {
                trackedSilverCost = SignetUpgradeData.removeUpgradeItemsWithTrackedCost(
                        player,
                        signet,
                        SignetUpgradeData.UPGRADE_ITEM_COST
                );
                if (trackedSilverCost < 0) {
                    refreshData();
                    return true;
                }
            } else if (!SignetUpgradeData.removeUpgradeItems(
                    player,
                    signet,
                    SignetUpgradeData.UPGRADE_ITEM_COST
            )) {
                refreshData();
                return true;
            }

            player.giveExperienceLevels(-SignetUpgradeData.EXPERIENCE_LEVEL_COST);
        }

        if (!SignetUpgradeData.upgradeStack(player, stack, skill)) return true;
        ModAdvancements.award(player, "upgrade_signet");

        if (!creative && SignetUpgradeData.isAnyCoreSignet(skill)) {
            SignetUpgradeData.recordCoreUpgrade(
                    player,
                    signet,
                    trackedSilverCost,
                    SignetUpgradeData.EXPERIENCE_LEVEL_COST
            );
        }

        this.signetInput.setChanged();
        player.getInventory().setChanged();
        refreshData();
        this.broadcastChanges();
        player.displayClientMessage(
                ModText.literal(skill.displayName() + "  +" + (level + 1))
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                true
        );
        return true;
    }

    private void fuseSecondarySignet(Player player) {
        ItemStack signetStack = this.signetInput.getItem(0);
        SignetUpgradeData.Signet inputSignet = SignetUpgradeData.fromStack(signetStack);
        ItemStack fusionInput = this.secondaryInput.getItem(0);
        if (inputSignet != null
                && SignetUpgradeData.isCoreSystemItem(fusionInput, inputSignet)
                && !SignetUpgradeData.isCurrentCoreSystemItem(
                player, fusionInput, inputSignet)) {
            player.displayClientMessage(
                    Component.translatable("message.lastsongofelysian.workshop.expired_core_item")
                            .withStyle(ChatFormatting.YELLOW), true);
            return;
        }
        if (inputSignet == SignetUpgradeData.Signet.INFINITY
                || inputSignet == SignetUpgradeData.Signet.STARS
                || inputSignet == SignetUpgradeData.Signet.VICISSITUDE
                || inputSignet == SignetUpgradeData.Signet.REVERIE) {
            ItemStack fusionStack = this.secondaryInput.getItem(0);
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.extensionSecondarySkillFromItem(fusionStack);
            SignetUpgradeData.ExtensionCore core =
                    SignetUpgradeData.extensionCoreFromItem(fusionStack);
            SignetUpgradeData.Skill augment =
                    SignetUpgradeData.extensionCoreSignetFromItem(fusionStack);
            if ((secondary != null && secondary.signet() != inputSignet)
                    || (core != null && core.signet() != inputSignet)
                    || (augment != null && augment.signet() != inputSignet)) {
                secondary = null;
                core = null;
                augment = null;
            }
            if (secondary == null && core == null && augment == null) {
                player.displayClientMessage(
                        ModText.literal("请放入对应刻印的附加、核心或增幅刻印")
                                .withStyle(ChatFormatting.YELLOW), true);
                return;
            }
            boolean fused = secondary != null
                    ? SignetUpgradeData.fuseExtensionSecondarySkill(player, signetStack, secondary)
                    : core != null
                    ? SignetUpgradeData.fuseExtensionCore(player, signetStack, core)
                    : SignetUpgradeData.fuseExtensionCoreSignet(player, signetStack, augment);
            String displayName = secondary != null ? secondary.displayName()
                    : core != null ? core.displayName() : augment.displayName();
            if (!fused) {
                String reason = augment != null
                        && SignetUpgradeData.getExtensionCore(player, inputSignet)
                        != augment.extensionCore()
                        ? "需要先融合对应核心" : displayName + " 已融合或与当前核心冲突";
                player.displayClientMessage(ModText.literal(reason)
                        .withStyle(ChatFormatting.GOLD), true);
                return;
            }
            finishFusion(player, fusionStack, displayName);
            return;
        }
        if (inputSignet != SignetUpgradeData.Signet.DELIVERANCE
                && inputSignet != SignetUpgradeData.Signet.DISCIPLINE
                && inputSignet != SignetUpgradeData.Signet.DAYBREAK
                && inputSignet != SignetUpgradeData.Signet.SETSURA
                && inputSignet != SignetUpgradeData.Signet.BODHI
                && inputSignet != SignetUpgradeData.Signet.HELIX
                && inputSignet != SignetUpgradeData.Signet.DECIMATION
                && inputSignet != SignetUpgradeData.Signet.GOLD) {
            player.displayClientMessage(
                    ModText.literal("请先放入救世、戒律、旭光、刹那、天慧、鏖灭、螺旋或黄金刻印").withStyle(ChatFormatting.YELLOW),
                    true
            );
            return;
        }

        ItemStack secondaryStack = this.secondaryInput.getItem(0);
        if (inputSignet == SignetUpgradeData.Signet.DAYBREAK) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.daybreakSecondarySkillFromItem(secondaryStack);
            SignetUpgradeData.DaybreakCore core =
                    SignetUpgradeData.daybreakCoreFromItem(secondaryStack);
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.daybreakCoreSignetFromItem(secondaryStack);
            if (secondary == null && core == null && coreSignet == null) {
                player.displayClientMessage(
                        ModText.literal("请放入旭光附加、核心或增幅刻印").withStyle(ChatFormatting.YELLOW), true);
                return;
            }
            boolean fused = secondary != null
                    ? SignetUpgradeData.fuseDaybreakSecondarySkill(player, signetStack, secondary)
                    : core != null ? SignetUpgradeData.fuseDaybreakCore(player, signetStack, core)
                    : SignetUpgradeData.fuseDaybreakCoreSignet(player, signetStack, coreSignet);
            String displayName = secondary != null ? secondary.displayName()
                    : core != null ? core.displayName() : coreSignet.displayName();
            if (!fused) {
                String reason = coreSignet != null
                        && SignetUpgradeData.getDaybreakCore(player) != coreSignet.daybreakCore()
                        ? "需要先融合对应核心" : displayName + " 已融合或与当前核心冲突";
                player.displayClientMessage(ModText.literal(reason).withStyle(ChatFormatting.GOLD), true);
                return;
            }
            finishFusion(player, secondaryStack, displayName);
            return;
        }
        if (inputSignet == SignetUpgradeData.Signet.SETSURA) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.setsuraSecondarySkillFromItem(secondaryStack);
            SignetUpgradeData.SetsuraCore core =
                    SignetUpgradeData.setsuraCoreFromItem(secondaryStack);
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.setsuraCoreSignetFromItem(secondaryStack);
            if (secondary == null && core == null && coreSignet == null) {
                player.displayClientMessage(
                        ModText.literal("请放入刹那附加、核心或增幅刻印").withStyle(ChatFormatting.YELLOW), true);
                return;
            }
            boolean fused = secondary != null
                    ? SignetUpgradeData.fuseSetsuraSecondarySkill(player, signetStack, secondary)
                    : core != null ? SignetUpgradeData.fuseSetsuraCore(player, signetStack, core)
                    : SignetUpgradeData.fuseSetsuraCoreSignet(player, signetStack, coreSignet);
            String displayName = secondary != null ? secondary.displayName()
                    : core != null ? core.displayName() : coreSignet.displayName();
            if (!fused) {
                String reason = coreSignet != null
                        && SignetUpgradeData.getSetsuraCore(player) != coreSignet.setsuraCore()
                        ? "需要先融合对应核心" : displayName + " 已融合或与当前核心冲突";
                player.displayClientMessage(ModText.literal(reason).withStyle(ChatFormatting.GOLD), true);
                return;
            }
            finishFusion(player, secondaryStack, displayName);
            return;
        }
        if (inputSignet == SignetUpgradeData.Signet.BODHI) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.bodhiSecondarySkillFromItem(secondaryStack);
            SignetUpgradeData.BodhiCore core =
                    SignetUpgradeData.bodhiCoreFromItem(secondaryStack);
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.bodhiCoreSignetFromItem(secondaryStack);
            if (secondary == null && core == null && coreSignet == null) {
                player.displayClientMessage(
                        ModText.literal("请放入天慧附加、核心或增幅刻印").withStyle(ChatFormatting.YELLOW), true);
                return;
            }
            boolean fused = secondary != null
                    ? SignetUpgradeData.fuseBodhiSecondarySkill(player, signetStack, secondary)
                    : core != null ? SignetUpgradeData.fuseBodhiCore(player, signetStack, core)
                    : SignetUpgradeData.fuseBodhiCoreSignet(player, signetStack, coreSignet);
            String displayName = secondary != null ? secondary.displayName()
                    : core != null ? core.displayName() : coreSignet.displayName();
            if (!fused) {
                String reason = coreSignet != null
                        && SignetUpgradeData.getBodhiCore(player) != coreSignet.bodhiCore()
                        ? "需要先融合对应核心" : displayName + " 已融合或与当前核心冲突";
                player.displayClientMessage(ModText.literal(reason).withStyle(ChatFormatting.GOLD), true);
                return;
            }
            finishFusion(player, secondaryStack, displayName);
            return;
        }
        if (inputSignet == SignetUpgradeData.Signet.DECIMATION) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.decimationSecondarySkillFromItem(secondaryStack);
            SignetUpgradeData.DecimationCore core =
                    SignetUpgradeData.decimationCoreFromItem(secondaryStack);
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.decimationCoreSignetFromItem(secondaryStack);
            if (secondary == null && core == null && coreSignet == null) {
                player.displayClientMessage(
                        ModText.literal("请放入鏖灭附加、核心或增幅刻印").withStyle(ChatFormatting.YELLOW), true);
                return;
            }
            boolean fused = secondary != null
                    ? SignetUpgradeData.fuseDecimationSecondarySkill(player, signetStack, secondary)
                    : core != null ? SignetUpgradeData.fuseDecimationCore(player, signetStack, core)
                    : SignetUpgradeData.fuseDecimationCoreSignet(player, signetStack, coreSignet);
            String displayName = secondary != null ? secondary.displayName()
                    : core != null ? core.displayName() : coreSignet.displayName();
            if (!fused) {
                String reason = coreSignet != null
                        && SignetUpgradeData.getDecimationCore(player) != coreSignet.decimationCore()
                        ? "需要先融合对应核心" : displayName + " 已融合或与当前核心冲突";
                player.displayClientMessage(ModText.literal(reason).withStyle(ChatFormatting.GOLD), true);
                return;
            }
            finishFusion(player, secondaryStack, displayName);
            return;
        }
        if (inputSignet == SignetUpgradeData.Signet.HELIX) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.helixSecondarySkillFromItem(secondaryStack);
            SignetUpgradeData.HelixCore core = SignetUpgradeData.helixCoreFromItem(secondaryStack);
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.helixCoreSignetFromItem(secondaryStack);
            if (secondary == null && core == null && coreSignet == null) {
                player.displayClientMessage(
                        ModText.literal("请放入螺旋附加、核心或增幅刻印").withStyle(ChatFormatting.YELLOW), true);
                return;
            }
            boolean fused = secondary != null
                    ? SignetUpgradeData.fuseHelixSecondarySkill(player, signetStack, secondary)
                    : core != null ? SignetUpgradeData.fuseHelixCore(player, signetStack, core)
                    : SignetUpgradeData.fuseHelixCoreSignet(player, signetStack, coreSignet);
            String displayName = secondary != null ? secondary.displayName()
                    : core != null ? core.displayName() : coreSignet.displayName();
            if (!fused) {
                String reason = coreSignet != null
                        && SignetUpgradeData.getHelixCore(player) != coreSignet.helixCore()
                        ? "需要先融合对应核心" : displayName + " 已融合或与当前核心冲突";
                player.displayClientMessage(ModText.literal(reason).withStyle(ChatFormatting.GOLD), true);
                return;
            }
            finishFusion(player, secondaryStack, displayName);
            return;
        }
        if (inputSignet == SignetUpgradeData.Signet.GOLD) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.goldSecondarySkillFromItem(secondaryStack);
            SignetUpgradeData.GoldCore core = SignetUpgradeData.goldCoreFromItem(secondaryStack);
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.goldCoreSignetFromItem(secondaryStack);
            if (secondary == null && core == null && coreSignet == null) {
                player.displayClientMessage(
                        ModText.literal("请放入黄金核心或增幅刻印").withStyle(ChatFormatting.YELLOW), true);
                return;
            }
            boolean fused = secondary != null
                    ? SignetUpgradeData.fuseGoldSecondarySkill(player, signetStack, secondary)
                    : core != null ? SignetUpgradeData.fuseGoldCore(player, signetStack, core)
                    : SignetUpgradeData.fuseGoldCoreSignet(player, signetStack, coreSignet);
            String displayName = secondary != null ? secondary.displayName()
                    : core != null ? core.displayName() : coreSignet.displayName();
            if (!fused) {
                String reason = coreSignet != null
                        && SignetUpgradeData.getGoldCore(player) != coreSignet.goldCore()
                        ? "需要先融合对应核心" : displayName + " 已融合或与当前核心冲突";
                player.displayClientMessage(ModText.literal(reason).withStyle(ChatFormatting.GOLD), true);
                return;
            }
            finishFusion(player, secondaryStack, displayName);
            return;
        }
        if (inputSignet == SignetUpgradeData.Signet.DISCIPLINE) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.disciplineSecondarySkillFromItem(secondaryStack);
            SignetUpgradeData.DisciplineCore core =
                    SignetUpgradeData.disciplineCoreFromItem(secondaryStack);
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.disciplineCoreSignetFromItem(secondaryStack);
            if (secondary == null && core == null && coreSignet == null) {
                player.displayClientMessage(
                        ModText.literal("请放入戒律附加、核心或增幅刻印").withStyle(ChatFormatting.YELLOW),
                        true
                );
                return;
            }
            boolean fused = secondary != null
                    ? SignetUpgradeData.fuseDisciplineSecondarySkill(
                    player, signetStack, secondary)
                    : core != null
                    ? SignetUpgradeData.fuseDisciplineCore(player, signetStack, core)
                    : SignetUpgradeData.fuseDisciplineCoreSignet(player, signetStack, coreSignet);
            String displayName = secondary != null
                    ? secondary.displayName()
                    : core != null ? core.displayName() : coreSignet.displayName();
            if (!fused) {
                String reason = coreSignet != null
                        && SignetUpgradeData.getDisciplineCore(player) != coreSignet.disciplineCore()
                        ? "需要先融合对应核心"
                        : displayName + " 已融合或与当前核心冲突";
                player.displayClientMessage(ModText.literal(reason).withStyle(ChatFormatting.GOLD), true);
                return;
            }
            finishFusion(player, secondaryStack, displayName);
            return;
        }

        SignetUpgradeData.Skill skill = SignetUpgradeData.secondarySkillFromItem(secondaryStack);
        SignetUpgradeData.DeliveranceCore core =
                SignetUpgradeData.deliveranceCoreFromItem(secondaryStack);
        SignetUpgradeData.Skill coreSignet =
                SignetUpgradeData.deliveranceCoreSignetFromItem(secondaryStack);
        if (skill == null && core == null && coreSignet == null) {
            player.displayClientMessage(
                    ModText.literal("请放入救世附加刻印").withStyle(ChatFormatting.YELLOW),
                    true
            );
            return;
        }

        boolean fused;
        String displayName;
        if (core != null) {
            fused = SignetUpgradeData.fuseDeliveranceCore(player, signetStack, core);
            displayName = core.displayName();
        } else if (coreSignet != null) {
            fused = SignetUpgradeData.fuseDeliveranceCoreSignet(
                    player,
                    signetStack,
                    coreSignet
            );
            displayName = coreSignet.displayName();
        } else {
            fused = SignetUpgradeData.fuseSecondarySkill(player, signetStack, skill);
            displayName = skill.displayName();
        }

        if (!fused) {
            String reason = coreSignet != null
                    && SignetUpgradeData.getDeliveranceCore(player) != coreSignet.deliveranceCore()
                    ? "需要先融合对应核心"
                    : displayName + " 已融合或与当前核心冲突";
            player.displayClientMessage(
                    ModText.literal(reason).withStyle(ChatFormatting.GOLD),
                    true
            );
            return;
        }

        finishFusion(player, secondaryStack, displayName);
    }

    private void finishFusion(Player player, ItemStack secondaryStack, String displayName) {
        secondaryStack.shrink(1);
        if (secondaryStack.isEmpty()) {
            this.secondaryInput.setItem(0, ItemStack.EMPTY);
        } else {
            this.secondaryInput.setChanged();
        }
        this.signetInput.setChanged();
        player.getInventory().setChanged();
        refreshData();
        this.broadcastChanges();
        player.displayClientMessage(
                ModText.literal("已融合：" + displayName)
                        .withStyle(ChatFormatting.DARK_BLUE),
                true
        );
    }

    private void resetCore(Player player) {
        ItemStack signetStack = this.signetInput.getItem(0);
        SignetUpgradeData.Signet signet = SignetUpgradeData.fromStack(signetStack);
        if (signet == null) {
            player.displayClientMessage(
                    Component.translatable("message.lastsongofelysian.workshop.insert_signet")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            return;
        }
        if (!SignetUpgradeData.hasCoreProgress(player, signet)) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.lastsongofelysian.workshop.core_reset_no_progress",
                            ModText.exact(signet.displayName()))
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            return;
        }

        if (hasUnfusedCoreItem(player, signet)) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.lastsongofelysian.workshop.core_reset_unfused",
                            ModText.exact(signet.displayName()))
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            return;
        }

        PardofelisShopMenu.preserveCorePurchases(player, signet);
        SignetUpgradeData.ResetRefund refund =
                SignetUpgradeData.resetCoreProgress(
                        player,
                        signet,
                        signetStack
                );
        PardofelisShopMenu.invalidateStock(player);
        switch (signet) {
            case DELIVERANCE -> SignetEffects.resetDeliveranceCoreRuntime(player);
            case DISCIPLINE -> SignetEffects.resetDisciplineCoreRuntime(player);
            case GOLD -> GoldSignetSystem.resetCoreRuntime(player);
            case HELIX -> HelixSignetSystem.resetCoreRuntime(player);
            case DECIMATION -> DecimationSignetSystem.resetCoreRuntime(player);
            case BODHI -> BodhiSignetSystem.resetCoreRuntime(player);
            case SETSURA -> SetsunaSignetSystem.resetCoreRuntime(player);
            case DAYBREAK -> DaybreakSignetSystem.resetCoreRuntime(player);
            case INFINITY -> InfinitySignetSystem.resetCoreRuntime(player);
            case STARS -> StarsSignetSystem.resetCoreRuntime(player);
            case VICISSITUDE -> VicissitudeSignetSystem.resetCoreRuntime(player);
            case REVERIE -> ReverieSignetSystem.resetCoreRuntime(player);
        }
        this.workshopData.set(DATA_SKILL_PAGE, 0);
        refreshData();
        this.broadcastChanges();
        player.displayClientMessage(
                Component.translatable(
                        "message.lastsongofelysian.workshop.core_reset_success",
                        ModText.exact(signet.displayName()),
                        refund.silver(), refund.experienceLevels()
                ).withStyle(ChatFormatting.AQUA),
                true
        );
    }

    private boolean hasUnfusedCoreItem(Player player, SignetUpgradeData.Signet signet) {
        if (SignetUpgradeData.isCurrentCoreSystemItem(
                player, this.secondaryInput.getItem(0), signet)) {
            return true;
        }
        if (SignetUpgradeData.isCurrentCoreSystemItem(player, this.getCarried(), signet)) {
            return true;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (SignetUpgradeData.isCurrentCoreSystemItem(
                    player, inventory.getItem(slot), signet)) {
                return true;
            }
        }
        return false;
    }

    private void refreshData() {
        if (this.playerInventory.player.level().isClientSide()) return;
        ItemStack stack = getInputStack();
        SignetUpgradeData.Signet signet = SignetUpgradeData.fromStack(stack);
        SignetUpgradeData.synchronizeLevels(this.playerInventory.player, stack, signet);
        this.workshopData.set(
                DATA_UPGRADE_ITEMS,
                SignetUpgradeData.countUpgradeItems(
                        this.playerInventory.player,
                        signet
                )
        );
        this.workshopData.set(
                DATA_EXPERIENCE_LEVEL,
                this.playerInventory.player.experienceLevel
        );
        this.workshopData.set(
                DATA_DELIVERANCE_SECONDARY_UNLOCKS,
                SignetUpgradeData.getDeliveranceSecondaryUnlockMask(this.playerInventory.player)
        );
        SignetUpgradeData.DeliveranceCore core = signet == SignetUpgradeData.Signet.DELIVERANCE
                ? SignetUpgradeData.getDeliveranceCore(this.playerInventory.player)
                : null;
        this.workshopData.set(DATA_DELIVERANCE_CORE, core == null ? 0 : core.id());
        this.workshopData.set(
                DATA_DELIVERANCE_CORE_SIGNETS,
                signet == SignetUpgradeData.Signet.DELIVERANCE
                        ? SignetUpgradeData.getDeliveranceCoreSignetMask(this.playerInventory.player)
                        : 0
        );
        this.workshopData.set(
                DATA_HAS_CORE_PROGRESS,
                signet != null && SignetUpgradeData.hasCoreProgress(
                        this.playerInventory.player, signet) ? 1 : 0
        );
        SignetUpgradeData.DisciplineCore disciplineCore = signet == SignetUpgradeData.Signet.DISCIPLINE
                ? SignetUpgradeData.getDisciplineCore(this.playerInventory.player)
                : null;
        this.workshopData.set(DATA_DISCIPLINE_CORE, disciplineCore == null ? 0 : disciplineCore.id());
        this.workshopData.set(
                DATA_DISCIPLINE_CORE_SIGNETS,
                signet == SignetUpgradeData.Signet.DISCIPLINE
                        ? SignetUpgradeData.getDisciplineCoreSignetMask(this.playerInventory.player)
                        : 0
        );
        this.workshopData.set(
                DATA_DISCIPLINE_SECONDARY_UNLOCKS,
                signet == SignetUpgradeData.Signet.DISCIPLINE
                        ? SignetUpgradeData.getDisciplineSecondaryUnlockMask(
                        this.playerInventory.player)
                        : 0
        );
        SignetUpgradeData.GoldCore goldCore = signet == SignetUpgradeData.Signet.GOLD
                ? SignetUpgradeData.getGoldCore(this.playerInventory.player) : null;
        this.workshopData.set(DATA_GOLD_CORE, goldCore == null ? 0 : goldCore.id());
        this.workshopData.set(
                DATA_GOLD_CORE_SIGNETS,
                signet == SignetUpgradeData.Signet.GOLD
                        ? SignetUpgradeData.getGoldCoreSignetMask(this.playerInventory.player) : 0
        );
        this.workshopData.set(
                DATA_GOLD_SECONDARY_UNLOCKS,
                signet == SignetUpgradeData.Signet.GOLD
                        ? SignetUpgradeData.getGoldSecondaryUnlockMask(this.playerInventory.player) : 0
        );
        SignetUpgradeData.HelixCore helixCore = signet == SignetUpgradeData.Signet.HELIX
                ? SignetUpgradeData.getHelixCore(this.playerInventory.player) : null;
        this.workshopData.set(DATA_HELIX_CORE, helixCore == null ? 0 : helixCore.id());
        this.workshopData.set(
                DATA_HELIX_CORE_SIGNETS,
                signet == SignetUpgradeData.Signet.HELIX
                        ? SignetUpgradeData.getHelixCoreSignetMask(this.playerInventory.player) : 0
        );
        this.workshopData.set(
                DATA_HELIX_SECONDARY_UNLOCKS,
                signet == SignetUpgradeData.Signet.HELIX
                        ? SignetUpgradeData.getHelixSecondaryUnlockMask(this.playerInventory.player) : 0
        );
        SignetUpgradeData.DecimationCore decimationCore = signet == SignetUpgradeData.Signet.DECIMATION
                ? SignetUpgradeData.getDecimationCore(this.playerInventory.player) : null;
        this.workshopData.set(DATA_DECIMATION_CORE,
                decimationCore == null ? 0 : decimationCore.id());
        this.workshopData.set(
                DATA_DECIMATION_CORE_SIGNETS,
                signet == SignetUpgradeData.Signet.DECIMATION
                        ? SignetUpgradeData.getDecimationCoreSignetMask(this.playerInventory.player) : 0
        );
        this.workshopData.set(
                DATA_DECIMATION_SECONDARY_UNLOCKS,
                signet == SignetUpgradeData.Signet.DECIMATION
                        ? SignetUpgradeData.getDecimationSecondaryUnlockMask(this.playerInventory.player) : 0
        );
        SignetUpgradeData.BodhiCore bodhiCore = signet == SignetUpgradeData.Signet.BODHI
                ? SignetUpgradeData.getBodhiCore(this.playerInventory.player) : null;
        this.workshopData.set(DATA_BODHI_CORE, bodhiCore == null ? 0 : bodhiCore.id());
        this.workshopData.set(
                DATA_BODHI_CORE_SIGNETS,
                signet == SignetUpgradeData.Signet.BODHI
                        ? SignetUpgradeData.getBodhiCoreSignetMask(this.playerInventory.player) : 0
        );
        this.workshopData.set(
                DATA_BODHI_SECONDARY_UNLOCKS,
                signet == SignetUpgradeData.Signet.BODHI
                        ? SignetUpgradeData.getBodhiSecondaryUnlockMask(this.playerInventory.player) : 0
        );
        SignetUpgradeData.SetsuraCore setsuraCore = signet == SignetUpgradeData.Signet.SETSURA
                ? SignetUpgradeData.getSetsuraCore(this.playerInventory.player) : null;
        this.workshopData.set(DATA_SETSURA_CORE,
                setsuraCore == null ? 0 : setsuraCore.id());
        this.workshopData.set(
                DATA_SETSURA_CORE_SIGNETS,
                signet == SignetUpgradeData.Signet.SETSURA
                        ? SignetUpgradeData.getSetsuraCoreSignetMask(this.playerInventory.player) : 0
        );
        this.workshopData.set(
                DATA_SETSURA_SECONDARY_UNLOCKS,
                signet == SignetUpgradeData.Signet.SETSURA
                        ? SignetUpgradeData.getSetsuraSecondaryUnlockMask(this.playerInventory.player) : 0
        );
        SignetUpgradeData.DaybreakCore daybreakCore = signet == SignetUpgradeData.Signet.DAYBREAK
                ? SignetUpgradeData.getDaybreakCore(this.playerInventory.player) : null;
        this.workshopData.set(DATA_DAYBREAK_CORE,
                daybreakCore == null ? 0 : daybreakCore.id());
        this.workshopData.set(
                DATA_DAYBREAK_CORE_SIGNETS,
                signet == SignetUpgradeData.Signet.DAYBREAK
                        ? SignetUpgradeData.getDaybreakCoreSignetMask(this.playerInventory.player) : 0
        );
        this.workshopData.set(
                DATA_DAYBREAK_SECONDARY_UNLOCKS,
                signet == SignetUpgradeData.Signet.DAYBREAK
                        ? SignetUpgradeData.getDaybreakSecondaryUnlockMask(this.playerInventory.player) : 0
        );
        SignetUpgradeData.ExtensionCore extensionCore = signet == null ? null
                : SignetUpgradeData.getExtensionCore(this.playerInventory.player, signet);
        this.workshopData.set(DATA_EXTENSION_CORE,
                extensionCore == null ? 0 : extensionCore.id());
        this.workshopData.set(DATA_EXTENSION_CORE_SIGNETS,
                signet == null ? 0 : SignetUpgradeData.getExtensionCoreSignetMask(
                        this.playerInventory.player, signet));
        this.workshopData.set(DATA_EXTENSION_SECONDARY_UNLOCKS,
                signet == null ? 0 : SignetUpgradeData.getExtensionSecondaryUnlockMask(
                        this.playerInventory.player, signet));
        int skillCount = SignetUpgradeData.workshopSkills(
                this.playerInventory.player,
                signet
        ).size();
        int maxPage = Math.max(0, (skillCount - 1) / MAX_SKILL_ROWS);
        if (this.workshopData.get(DATA_SKILL_PAGE) > maxPage) {
            this.workshopData.set(DATA_SKILL_PAGE, maxPage);
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        refreshData();
    }

    @Override
    public void broadcastChanges() {
        refreshData();
        super.broadcastChanges();
    }

    public ItemStack getInputStack() {
        return this.signetInput.getItem(0);
    }

    public ItemStack getSecondaryInputStack() {
        return this.secondaryInput.getItem(0);
    }

    public SignetUpgradeData.Skill getSecondaryInputSkill() {
        return SignetUpgradeData.secondarySkillFromItem(getSecondaryInputStack());
    }

    public boolean canFuseSecondary() {
        SignetUpgradeData.Signet inputSignet = getInputSignet();
        if (inputSignet == SignetUpgradeData.Signet.INFINITY
                || inputSignet == SignetUpgradeData.Signet.STARS
                || inputSignet == SignetUpgradeData.Signet.VICISSITUDE
                || inputSignet == SignetUpgradeData.Signet.REVERIE) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.extensionSecondarySkillFromItem(getSecondaryInputStack());
            SignetUpgradeData.ExtensionCore core =
                    SignetUpgradeData.extensionCoreFromItem(getSecondaryInputStack());
            SignetUpgradeData.Skill augment =
                    SignetUpgradeData.extensionCoreSignetFromItem(getSecondaryInputStack());
            if (secondary != null) {
                return secondary.signet() == inputSignet
                        && (this.workshopData.get(DATA_EXTENSION_SECONDARY_UNLOCKS)
                        & secondary.extensionSecondaryBit()) == 0;
            }
            SignetUpgradeData.ExtensionCore selected = getDisplayedExtensionCore();
            if (core != null) return core.signet() == inputSignet && selected == null;
            return augment != null && augment.signet() == inputSignet
                    && selected == augment.extensionCore()
                    && (this.workshopData.get(DATA_EXTENSION_CORE_SIGNETS)
                    & augment.extensionCoreSignetBit()) == 0;
        }
        if (getInputSignet() == SignetUpgradeData.Signet.DAYBREAK) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.daybreakSecondarySkillFromItem(getSecondaryInputStack());
            if (secondary != null) {
                return (this.workshopData.get(DATA_DAYBREAK_SECONDARY_UNLOCKS)
                        & secondary.daybreakSecondaryBit()) == 0;
            }
            SignetUpgradeData.DaybreakCore core =
                    SignetUpgradeData.daybreakCoreFromItem(getSecondaryInputStack());
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.daybreakCoreSignetFromItem(getSecondaryInputStack());
            SignetUpgradeData.DaybreakCore selected = getDisplayedDaybreakCore();
            if (core != null) return selected == null;
            return coreSignet != null && selected == coreSignet.daybreakCore()
                    && (this.workshopData.get(DATA_DAYBREAK_CORE_SIGNETS)
                    & coreSignet.daybreakCoreSignetBit()) == 0;
        }
        if (getInputSignet() == SignetUpgradeData.Signet.SETSURA) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.setsuraSecondarySkillFromItem(getSecondaryInputStack());
            if (secondary != null) {
                return (this.workshopData.get(DATA_SETSURA_SECONDARY_UNLOCKS)
                        & secondary.setsuraSecondaryBit()) == 0;
            }
            SignetUpgradeData.SetsuraCore core =
                    SignetUpgradeData.setsuraCoreFromItem(getSecondaryInputStack());
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.setsuraCoreSignetFromItem(getSecondaryInputStack());
            SignetUpgradeData.SetsuraCore selected = getDisplayedSetsuraCore();
            if (core != null) return selected == null;
            return coreSignet != null && selected == coreSignet.setsuraCore()
                    && (this.workshopData.get(DATA_SETSURA_CORE_SIGNETS)
                    & coreSignet.setsuraCoreSignetBit()) == 0;
        }
        if (getInputSignet() == SignetUpgradeData.Signet.BODHI) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.bodhiSecondarySkillFromItem(getSecondaryInputStack());
            if (secondary != null) {
                return (this.workshopData.get(DATA_BODHI_SECONDARY_UNLOCKS)
                        & secondary.bodhiSecondaryBit()) == 0;
            }
            SignetUpgradeData.BodhiCore core =
                    SignetUpgradeData.bodhiCoreFromItem(getSecondaryInputStack());
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.bodhiCoreSignetFromItem(getSecondaryInputStack());
            SignetUpgradeData.BodhiCore selected = getDisplayedBodhiCore();
            if (core != null) return selected == null;
            return coreSignet != null && selected == coreSignet.bodhiCore()
                    && (this.workshopData.get(DATA_BODHI_CORE_SIGNETS)
                    & coreSignet.bodhiCoreSignetBit()) == 0;
        }
        if (getInputSignet() == SignetUpgradeData.Signet.DECIMATION) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.decimationSecondarySkillFromItem(getSecondaryInputStack());
            if (secondary != null) {
                return (this.workshopData.get(DATA_DECIMATION_SECONDARY_UNLOCKS)
                        & secondary.decimationSecondaryBit()) == 0;
            }
            SignetUpgradeData.DecimationCore core =
                    SignetUpgradeData.decimationCoreFromItem(getSecondaryInputStack());
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.decimationCoreSignetFromItem(getSecondaryInputStack());
            SignetUpgradeData.DecimationCore selected = getDisplayedDecimationCore();
            if (core != null) return selected == null;
            return coreSignet != null && selected == coreSignet.decimationCore()
                    && (this.workshopData.get(DATA_DECIMATION_CORE_SIGNETS)
                    & coreSignet.decimationCoreSignetBit()) == 0;
        }
        if (getInputSignet() == SignetUpgradeData.Signet.HELIX) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.helixSecondarySkillFromItem(getSecondaryInputStack());
            if (secondary != null) {
                return (this.workshopData.get(DATA_HELIX_SECONDARY_UNLOCKS)
                        & secondary.helixSecondaryBit()) == 0;
            }
            SignetUpgradeData.HelixCore core = SignetUpgradeData.helixCoreFromItem(getSecondaryInputStack());
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.helixCoreSignetFromItem(getSecondaryInputStack());
            SignetUpgradeData.HelixCore selected = getDisplayedHelixCore();
            if (core != null) return selected == null;
            return coreSignet != null && selected == coreSignet.helixCore()
                    && (this.workshopData.get(DATA_HELIX_CORE_SIGNETS)
                    & coreSignet.helixCoreSignetBit()) == 0;
        }
        if (getInputSignet() == SignetUpgradeData.Signet.GOLD) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.goldSecondarySkillFromItem(getSecondaryInputStack());
            if (secondary != null) {
                return (this.workshopData.get(DATA_GOLD_SECONDARY_UNLOCKS)
                        & secondary.goldSecondaryBit()) == 0;
            }
            SignetUpgradeData.GoldCore core = SignetUpgradeData.goldCoreFromItem(getSecondaryInputStack());
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.goldCoreSignetFromItem(getSecondaryInputStack());
            SignetUpgradeData.GoldCore selected = getDisplayedGoldCore();
            if (core != null) return selected == null;
            return coreSignet != null && selected == coreSignet.goldCore()
                    && (this.workshopData.get(DATA_GOLD_CORE_SIGNETS)
                    & coreSignet.goldCoreSignetBit()) == 0;
        }
        if (getInputSignet() == SignetUpgradeData.Signet.DISCIPLINE) {
            SignetUpgradeData.Skill secondary =
                    SignetUpgradeData.disciplineSecondarySkillFromItem(
                            getSecondaryInputStack());
            SignetUpgradeData.DisciplineCore core =
                    SignetUpgradeData.disciplineCoreFromItem(getSecondaryInputStack());
            SignetUpgradeData.Skill coreSignet =
                    SignetUpgradeData.disciplineCoreSignetFromItem(getSecondaryInputStack());
            SignetUpgradeData.DisciplineCore selected = getDisplayedDisciplineCore();
            if (secondary != null) {
                return (this.workshopData.get(DATA_DISCIPLINE_SECONDARY_UNLOCKS)
                        & secondary.disciplineSecondaryBit()) == 0;
            }
            if (core != null) return selected == null;
            return coreSignet != null && selected == coreSignet.disciplineCore()
                    && (this.workshopData.get(DATA_DISCIPLINE_CORE_SIGNETS)
                    & coreSignet.disciplineCoreSignetBit()) == 0;
        }
        SignetUpgradeData.Skill skill = getSecondaryInputSkill();
        SignetUpgradeData.DeliveranceCore core =
                SignetUpgradeData.deliveranceCoreFromItem(getSecondaryInputStack());
        SignetUpgradeData.Skill coreSignet =
                SignetUpgradeData.deliveranceCoreSignetFromItem(getSecondaryInputStack());
        if (getInputSignet() != SignetUpgradeData.Signet.DELIVERANCE) return false;
        if (skill != null) {
            return (this.workshopData.get(DATA_DELIVERANCE_SECONDARY_UNLOCKS)
                    & skill.deliveranceSecondaryBit()) == 0;
        }
        SignetUpgradeData.DeliveranceCore selected = getDisplayedDeliveranceCore();
        if (core != null) return selected == null;
        return coreSignet != null
                && selected == coreSignet.deliveranceCore()
                && (this.workshopData.get(DATA_DELIVERANCE_CORE_SIGNETS)
                & coreSignet.deliveranceCoreSignetBit()) == 0;
    }

    public boolean isSecondaryInputFused() {
        SignetUpgradeData.Skill extensionSecondary =
                SignetUpgradeData.extensionSecondarySkillFromItem(getSecondaryInputStack());
        if (extensionSecondary != null) {
            return (this.workshopData.get(DATA_EXTENSION_SECONDARY_UNLOCKS)
                    & extensionSecondary.extensionSecondaryBit()) != 0;
        }
        SignetUpgradeData.ExtensionCore extensionCore =
                SignetUpgradeData.extensionCoreFromItem(getSecondaryInputStack());
        if (extensionCore != null) return getDisplayedExtensionCore() != null;
        SignetUpgradeData.Skill extensionAugment =
                SignetUpgradeData.extensionCoreSignetFromItem(getSecondaryInputStack());
        if (extensionAugment != null) {
            return (this.workshopData.get(DATA_EXTENSION_CORE_SIGNETS)
                    & extensionAugment.extensionCoreSignetBit()) != 0;
        }
        SignetUpgradeData.Skill daybreakSecondary =
                SignetUpgradeData.daybreakSecondarySkillFromItem(getSecondaryInputStack());
        if (daybreakSecondary != null) {
            return (this.workshopData.get(DATA_DAYBREAK_SECONDARY_UNLOCKS)
                    & daybreakSecondary.daybreakSecondaryBit()) != 0;
        }
        SignetUpgradeData.DaybreakCore daybreakCore =
                SignetUpgradeData.daybreakCoreFromItem(getSecondaryInputStack());
        if (daybreakCore != null) return getDisplayedDaybreakCore() != null;
        SignetUpgradeData.Skill daybreakCoreSignet =
                SignetUpgradeData.daybreakCoreSignetFromItem(getSecondaryInputStack());
        if (daybreakCoreSignet != null) {
            return (this.workshopData.get(DATA_DAYBREAK_CORE_SIGNETS)
                    & daybreakCoreSignet.daybreakCoreSignetBit()) != 0;
        }
        SignetUpgradeData.Skill setsuraSecondary =
                SignetUpgradeData.setsuraSecondarySkillFromItem(getSecondaryInputStack());
        if (setsuraSecondary != null) {
            return (this.workshopData.get(DATA_SETSURA_SECONDARY_UNLOCKS)
                    & setsuraSecondary.setsuraSecondaryBit()) != 0;
        }
        SignetUpgradeData.SetsuraCore setsuraCore =
                SignetUpgradeData.setsuraCoreFromItem(getSecondaryInputStack());
        if (setsuraCore != null) return getDisplayedSetsuraCore() != null;
        SignetUpgradeData.Skill setsuraCoreSignet =
                SignetUpgradeData.setsuraCoreSignetFromItem(getSecondaryInputStack());
        if (setsuraCoreSignet != null) {
            return (this.workshopData.get(DATA_SETSURA_CORE_SIGNETS)
                    & setsuraCoreSignet.setsuraCoreSignetBit()) != 0;
        }
        SignetUpgradeData.Skill bodhiSecondary =
                SignetUpgradeData.bodhiSecondarySkillFromItem(getSecondaryInputStack());
        if (bodhiSecondary != null) {
            return (this.workshopData.get(DATA_BODHI_SECONDARY_UNLOCKS)
                    & bodhiSecondary.bodhiSecondaryBit()) != 0;
        }
        SignetUpgradeData.BodhiCore bodhiCore =
                SignetUpgradeData.bodhiCoreFromItem(getSecondaryInputStack());
        if (bodhiCore != null) return getDisplayedBodhiCore() != null;
        SignetUpgradeData.Skill bodhiCoreSignet =
                SignetUpgradeData.bodhiCoreSignetFromItem(getSecondaryInputStack());
        if (bodhiCoreSignet != null) {
            return (this.workshopData.get(DATA_BODHI_CORE_SIGNETS)
                    & bodhiCoreSignet.bodhiCoreSignetBit()) != 0;
        }
        SignetUpgradeData.Skill decimationSecondary =
                SignetUpgradeData.decimationSecondarySkillFromItem(getSecondaryInputStack());
        if (decimationSecondary != null) {
            return (this.workshopData.get(DATA_DECIMATION_SECONDARY_UNLOCKS)
                    & decimationSecondary.decimationSecondaryBit()) != 0;
        }
        SignetUpgradeData.DecimationCore decimationCore =
                SignetUpgradeData.decimationCoreFromItem(getSecondaryInputStack());
        if (decimationCore != null) return getDisplayedDecimationCore() != null;
        SignetUpgradeData.Skill decimationCoreSignet =
                SignetUpgradeData.decimationCoreSignetFromItem(getSecondaryInputStack());
        if (decimationCoreSignet != null) {
            return (this.workshopData.get(DATA_DECIMATION_CORE_SIGNETS)
                    & decimationCoreSignet.decimationCoreSignetBit()) != 0;
        }
        SignetUpgradeData.Skill helixSecondary =
                SignetUpgradeData.helixSecondarySkillFromItem(getSecondaryInputStack());
        if (helixSecondary != null) {
            return (this.workshopData.get(DATA_HELIX_SECONDARY_UNLOCKS)
                    & helixSecondary.helixSecondaryBit()) != 0;
        }
        SignetUpgradeData.HelixCore helixCore =
                SignetUpgradeData.helixCoreFromItem(getSecondaryInputStack());
        if (helixCore != null) return getDisplayedHelixCore() != null;
        SignetUpgradeData.Skill helixCoreSignet =
                SignetUpgradeData.helixCoreSignetFromItem(getSecondaryInputStack());
        if (helixCoreSignet != null) {
            return (this.workshopData.get(DATA_HELIX_CORE_SIGNETS)
                    & helixCoreSignet.helixCoreSignetBit()) != 0;
        }
        SignetUpgradeData.Skill goldSecondary =
                SignetUpgradeData.goldSecondarySkillFromItem(getSecondaryInputStack());
        if (goldSecondary != null) {
            return (this.workshopData.get(DATA_GOLD_SECONDARY_UNLOCKS)
                    & goldSecondary.goldSecondaryBit()) != 0;
        }
        SignetUpgradeData.GoldCore goldCore =
                SignetUpgradeData.goldCoreFromItem(getSecondaryInputStack());
        if (goldCore != null) return getDisplayedGoldCore() != null;
        SignetUpgradeData.Skill goldCoreSignet =
                SignetUpgradeData.goldCoreSignetFromItem(getSecondaryInputStack());
        if (goldCoreSignet != null) {
            return (this.workshopData.get(DATA_GOLD_CORE_SIGNETS)
                    & goldCoreSignet.goldCoreSignetBit()) != 0;
        }
        SignetUpgradeData.Skill disciplineSecondary =
                SignetUpgradeData.disciplineSecondarySkillFromItem(
                        getSecondaryInputStack());
        if (disciplineSecondary != null) {
            return (this.workshopData.get(DATA_DISCIPLINE_SECONDARY_UNLOCKS)
                    & disciplineSecondary.disciplineSecondaryBit()) != 0;
        }
        SignetUpgradeData.DisciplineCore disciplineCore =
                SignetUpgradeData.disciplineCoreFromItem(getSecondaryInputStack());
        if (disciplineCore != null) return getDisplayedDisciplineCore() != null;
        SignetUpgradeData.Skill disciplineCoreSignet =
                SignetUpgradeData.disciplineCoreSignetFromItem(getSecondaryInputStack());
        if (disciplineCoreSignet != null) {
            return (this.workshopData.get(DATA_DISCIPLINE_CORE_SIGNETS)
                    & disciplineCoreSignet.disciplineCoreSignetBit()) != 0;
        }
        SignetUpgradeData.Skill skill = getSecondaryInputSkill();
        if (skill != null) {
            return (this.workshopData.get(DATA_DELIVERANCE_SECONDARY_UNLOCKS)
                    & skill.deliveranceSecondaryBit()) != 0;
        }
        SignetUpgradeData.DeliveranceCore core =
                SignetUpgradeData.deliveranceCoreFromItem(getSecondaryInputStack());
        if (core != null) return getDisplayedDeliveranceCore() != null;
        SignetUpgradeData.Skill coreSignet =
                SignetUpgradeData.deliveranceCoreSignetFromItem(getSecondaryInputStack());
        return coreSignet != null
                && (this.workshopData.get(DATA_DELIVERANCE_CORE_SIGNETS)
                & coreSignet.deliveranceCoreSignetBit()) != 0;
    }

    public SignetUpgradeData.Signet getInputSignet() {
        return SignetUpgradeData.fromStack(getInputStack());
    }

    public SignetUpgradeData.Skill getDisplayedSkill(int index) {
        return SignetUpgradeData.skillAtWithExtension(
                getInputSignet(),
                this.workshopData.get(DATA_DELIVERANCE_SECONDARY_UNLOCKS),
                getDisplayedDeliveranceCore(),
                this.workshopData.get(DATA_DELIVERANCE_CORE_SIGNETS),
                getDisplayedDisciplineCore(),
                this.workshopData.get(DATA_DISCIPLINE_CORE_SIGNETS),
                this.workshopData.get(DATA_DISCIPLINE_SECONDARY_UNLOCKS),
                getDisplayedGoldCore(),
                this.workshopData.get(DATA_GOLD_CORE_SIGNETS),
                this.workshopData.get(DATA_GOLD_SECONDARY_UNLOCKS),
                getDisplayedHelixCore(),
                this.workshopData.get(DATA_HELIX_CORE_SIGNETS),
                this.workshopData.get(DATA_HELIX_SECONDARY_UNLOCKS),
                getDisplayedDecimationCore(),
                this.workshopData.get(DATA_DECIMATION_CORE_SIGNETS),
                this.workshopData.get(DATA_DECIMATION_SECONDARY_UNLOCKS),
                getDisplayedBodhiCore(),
                this.workshopData.get(DATA_BODHI_CORE_SIGNETS),
                this.workshopData.get(DATA_BODHI_SECONDARY_UNLOCKS),
                getDisplayedSetsuraCore(),
                this.workshopData.get(DATA_SETSURA_CORE_SIGNETS),
                this.workshopData.get(DATA_SETSURA_SECONDARY_UNLOCKS),
                getDisplayedDaybreakCore(),
                this.workshopData.get(DATA_DAYBREAK_CORE_SIGNETS),
                this.workshopData.get(DATA_DAYBREAK_SECONDARY_UNLOCKS),
                getDisplayedExtensionCore(),
                this.workshopData.get(DATA_EXTENSION_CORE_SIGNETS),
                this.workshopData.get(DATA_EXTENSION_SECONDARY_UNLOCKS),
                this.workshopData.get(DATA_SKILL_PAGE) * MAX_SKILL_ROWS + index
        );
    }

    public SignetUpgradeData.DeliveranceCore getDisplayedDeliveranceCore() {
        return SignetUpgradeData.DeliveranceCore.fromId(
                this.workshopData.get(DATA_DELIVERANCE_CORE)
        );
    }

    public SignetUpgradeData.DisciplineCore getDisplayedDisciplineCore() {
        return SignetUpgradeData.DisciplineCore.fromId(
                this.workshopData.get(DATA_DISCIPLINE_CORE)
        );
    }

    public SignetUpgradeData.GoldCore getDisplayedGoldCore() {
        return SignetUpgradeData.GoldCore.fromId(this.workshopData.get(DATA_GOLD_CORE));
    }

    public SignetUpgradeData.HelixCore getDisplayedHelixCore() {
        return SignetUpgradeData.HelixCore.fromId(this.workshopData.get(DATA_HELIX_CORE));
    }

    public SignetUpgradeData.DecimationCore getDisplayedDecimationCore() {
        return SignetUpgradeData.DecimationCore.fromId(
                this.workshopData.get(DATA_DECIMATION_CORE));
    }

    public SignetUpgradeData.BodhiCore getDisplayedBodhiCore() {
        return SignetUpgradeData.BodhiCore.fromId(this.workshopData.get(DATA_BODHI_CORE));
    }

    public SignetUpgradeData.SetsuraCore getDisplayedSetsuraCore() {
        return SignetUpgradeData.SetsuraCore.fromId(this.workshopData.get(DATA_SETSURA_CORE));
    }

    public SignetUpgradeData.DaybreakCore getDisplayedDaybreakCore() {
        return SignetUpgradeData.DaybreakCore.fromId(this.workshopData.get(DATA_DAYBREAK_CORE));
    }

    public SignetUpgradeData.ExtensionCore getDisplayedExtensionCore() {
        return SignetUpgradeData.ExtensionCore.fromId(
                getInputSignet(), this.workshopData.get(DATA_EXTENSION_CORE));
    }

    public int getDisplayedSkillPage() {
        return this.workshopData.get(DATA_SKILL_PAGE);
    }

    public int getDisplayedSkillPageCount() {
        int count = SignetUpgradeData.workshopSkillsWithExtension(
                getInputSignet(),
                this.workshopData.get(DATA_DELIVERANCE_SECONDARY_UNLOCKS),
                getDisplayedDeliveranceCore(),
                this.workshopData.get(DATA_DELIVERANCE_CORE_SIGNETS),
                getDisplayedDisciplineCore(),
                this.workshopData.get(DATA_DISCIPLINE_CORE_SIGNETS),
                this.workshopData.get(DATA_DISCIPLINE_SECONDARY_UNLOCKS),
                getDisplayedGoldCore(),
                this.workshopData.get(DATA_GOLD_CORE_SIGNETS),
                this.workshopData.get(DATA_GOLD_SECONDARY_UNLOCKS),
                getDisplayedHelixCore(),
                this.workshopData.get(DATA_HELIX_CORE_SIGNETS),
                this.workshopData.get(DATA_HELIX_SECONDARY_UNLOCKS),
                getDisplayedDecimationCore(),
                this.workshopData.get(DATA_DECIMATION_CORE_SIGNETS),
                this.workshopData.get(DATA_DECIMATION_SECONDARY_UNLOCKS),
                getDisplayedBodhiCore(),
                this.workshopData.get(DATA_BODHI_CORE_SIGNETS),
                this.workshopData.get(DATA_BODHI_SECONDARY_UNLOCKS),
                getDisplayedSetsuraCore(),
                this.workshopData.get(DATA_SETSURA_CORE_SIGNETS),
                this.workshopData.get(DATA_SETSURA_SECONDARY_UNLOCKS),
                getDisplayedDaybreakCore(),
                this.workshopData.get(DATA_DAYBREAK_CORE_SIGNETS),
                this.workshopData.get(DATA_DAYBREAK_SECONDARY_UNLOCKS),
                getDisplayedExtensionCore(),
                this.workshopData.get(DATA_EXTENSION_CORE_SIGNETS),
                this.workshopData.get(DATA_EXTENSION_SECONDARY_UNLOCKS)
        ).size();
        return Math.max(1, (count + MAX_SKILL_ROWS - 1) / MAX_SKILL_ROWS);
    }

    public boolean hasDisplayedCoreProgress() {
        return this.workshopData.get(DATA_HAS_CORE_PROGRESS) != 0;
    }

    public int getDisplayedLevel(int index) {
        SignetUpgradeData.Skill skill = getDisplayedSkill(index);
        return skill == null ? 0 : SignetUpgradeData.getLevel(getInputStack(), skill);
    }

    public int getDisplayedUpgradeItemCount() {
        return this.workshopData.get(DATA_UPGRADE_ITEMS);
    }

    public int getDisplayedExperienceLevel() {
        return this.workshopData.get(DATA_EXPERIENCE_LEVEL);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == SIGNET_SLOT || index == SECONDARY_SLOT) {
            if (!this.moveItemStackTo(
                    stack,
                    PLAYER_INVENTORY_START,
                    PLAYER_HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else if (SignetUpgradeData.isUpgradeableSignet(stack)
                && !this.slots.get(SIGNET_SLOT).hasItem()) {
            if (!this.moveItemStackTo(stack, SIGNET_SLOT, SIGNET_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SignetUpgradeData.isAnyFusionItem(stack)
                && !this.slots.get(SECONDARY_SLOT).hasItem()) {
            if (!this.moveItemStackTo(stack, SECONDARY_SLOT, SECONDARY_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
            if (!this.moveItemStackTo(
                    stack,
                    PLAYER_HOTBAR_START,
                    PLAYER_HOTBAR_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_HOTBAR_START && index < PLAYER_HOTBAR_END) {
            if (!this.moveItemStackTo(
                    stack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.signetInput);
        this.clearContainer(player, this.secondaryInput);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
