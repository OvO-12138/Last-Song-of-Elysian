package com.ovo.lastsongofelysian.screen;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.network.ModNetwork;
import com.ovo.lastsongofelysian.network.SilverPouchActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SilverPouchScreen extends Screen {

    private final long balance;
    private EditBox amountInput;
    private Button customWithdrawButton;

    public SilverPouchScreen(long balance) {
        super(ModText.literal("银币袋"));
        this.balance = balance;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;

        addRenderableWidget(Button.builder(
                ModText.literal("存入所有银币"),
                btn -> {
                    ModNetwork.CHANNEL.sendToServer(
                            new SilverPouchActionPacket(SilverPouchActionPacket.Action.DEPOSIT_ALL));
                    onClose();
                }
        ).bounds(cx - 75, cy + 5, 150, 20).build());

        addRenderableWidget(Button.builder(
                ModText.literal("取出 64 枚"),
                btn -> {
                    ModNetwork.CHANNEL.sendToServer(
                            new SilverPouchActionPacket(SilverPouchActionPacket.Action.WITHDRAW_64));
                    onClose();
                }
        ).bounds(cx - 75, cy + 30, 150, 20).build());

        amountInput = new EditBox(
                font,
                cx - 75,
                cy + 55,
                95,
                20,
                ModText.literal("取出数量")
        );
        amountInput.setMaxLength(19);
        amountInput.setFilter(SilverPouchScreen::isValidNumberInput);
        amountInput.setHint(ModText.literal("输入数量"));
        addRenderableWidget(amountInput);

        customWithdrawButton = addRenderableWidget(Button.builder(
                ModText.literal("取出"),
                btn -> withdrawCustomAmount()
        ).bounds(cx + 25, cy + 55, 50, 20).build());
        customWithdrawButton.active = false;
    }

    @Override
    public void tick() {
        amountInput.tick();
        customWithdrawButton.active = getRequestedAmount() > 0L && balance > 0L;
    }

    private void withdrawCustomAmount() {
        long amount = getRequestedAmount();
        if (amount <= 0L) return;
        ModNetwork.CHANNEL.sendToServer(
                new SilverPouchActionPacket(
                        SilverPouchActionPacket.Action.WITHDRAW_CUSTOM,
                        amount
                )
        );
        onClose();
    }

    private long getRequestedAmount() {
        if (amountInput == null || amountInput.getValue().isEmpty()) return 0L;
        try {
            return Long.parseLong(amountInput.getValue());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static boolean isValidNumberInput(String value) {
        if (value.isEmpty()) return true;
        for (int index = 0; index < value.length(); index++) {
            if (!Character.isDigit(value.charAt(index))) return false;
        }
        try {
            return Long.parseLong(value) > 0L;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, ModText.display("✦ 银币袋 ✦"), width / 2, height / 2 - 35, 0xFFD700);
        guiGraphics.drawCenteredString(font, ModText.display("余额：" + balance + " 枚"), width / 2, height / 2 - 12, 0xFFEE88);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
