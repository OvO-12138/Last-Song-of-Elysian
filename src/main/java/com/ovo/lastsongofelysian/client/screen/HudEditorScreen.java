package com.ovo.lastsongofelysian.client.screen;

import com.ovo.lastsongofelysian.config.ClientHudConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class HudEditorScreen extends Screen {
    private static final List<Entry> ENTRIES = List.of(
            new Entry("hud.editor.lastsongofelysian.corruption", ClientHudConfig.CORRUPTION,
                    128, 16, 0xFF9B55E8),
            new Entry("hud.editor.lastsongofelysian.weapon_energy", ClientHudConfig.WEAPON_ENERGY,
                    128, 16, 0xFFFFA23A),
            new Entry("hud.editor.lastsongofelysian.blood_release", ClientHudConfig.BLOOD_RELEASE,
                    126, 18, 0xFFFF3939),
            new Entry("hud.editor.lastsongofelysian.discipline", ClientHudConfig.DISCIPLINE,
                    120, 28, 0xFFFFC95D),
            new Entry("hud.editor.lastsongofelysian.combo", ClientHudConfig.COMBO,
                    120, 26, 0xFF8CE7FF),
            new Entry("hud.editor.lastsongofelysian.evasion", ClientHudConfig.EVASION,
                    126, 22, 0xFF79E8FF)
    );

    private int selected;
    private Button visibilityButton;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public HudEditorScreen() {
        super(Component.translatable("screen.lastsongofelysian.hud_editor"));
    }

    @Override
    protected void init() {
        int listX = 10;
        int listY = 42;
        for (int i = 0; i < ENTRIES.size(); i++) {
            int index = i;
            Entry entry = ENTRIES.get(i);
            addRenderableWidget(Button.builder(Component.translatable(entry.translationKey), button -> {
                        selected = index;
                        refreshButtons();
                    })
                    .bounds(listX, listY + i * 22, 126, 20)
                    .build());
        }

        int controlsX = Math.max(148, width - 172);
        addRenderableWidget(Button.builder(Component.literal("−"), button -> changeX(-5))
                .bounds(controlsX + 4, 68, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> changeX(5))
                .bounds(controlsX + 132, 68, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal("−"), button -> changeY(-5))
                .bounds(controlsX + 4, 108, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> changeY(5))
                .bounds(controlsX + 132, 108, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal("−"), button -> changeScale(-0.1D))
                .bounds(controlsX + 4, 148, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> changeScale(0.1D))
                .bounds(controlsX + 132, 148, 32, 20).build());

        visibilityButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    ClientHudConfig.HudElement settings = selected().settings;
                    settings.setEnabled(!settings.enabled().get());
                    refreshButtons();
                })
                .bounds(controlsX + 4, 182, 160, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(
                        "button.lastsongofelysian.hud_editor.reset"), button -> {
                    selected().settings.reset();
                    refreshButtons();
                })
                .bounds(controlsX + 4, 206, 78, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(controlsX + 86, 206, 78, 20).build());
        refreshButtons();
    }

    private void changeX(int amount) {
        ClientHudConfig.HudElement settings = selected().settings;
        settings.setX(settings.x().get() + amount);
    }

    private void changeY(int amount) {
        ClientHudConfig.HudElement settings = selected().settings;
        settings.setY(settings.y().get() + amount);
    }

    private void changeScale(double amount) {
        ClientHudConfig.HudElement settings = selected().settings;
        settings.setScale(Math.round((settings.scale().get() + amount) * 10.0D) / 10.0D);
    }

    private void refreshButtons() {
        if (visibilityButton == null) return;
        visibilityButton.setMessage(Component.translatable(
                selected().settings.enabled().get()
                        ? "button.lastsongofelysian.hud_editor.visible"
                        : "button.lastsongofelysian.hud_editor.hidden"));
    }

    private Entry selected() {
        return ENTRIES.get(selected);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderPreviews(graphics);
        graphics.fill(4, 8, 142, 204, 0xD014111B);
        int controlsX = Math.max(148, width - 172);
        graphics.fill(controlsX, 8, controlsX + 168, 232, 0xD014111B);
        graphics.drawCenteredString(font, title, 73, 17, 0xFFFFE5F3);
        graphics.drawCenteredString(font, Component.translatable(selected().translationKey),
                controlsX + 84, 17, selected().color);
        graphics.drawCenteredString(font, Component.translatable(
                        "screen.lastsongofelysian.hud_editor.drag_hint"),
                width / 2, height - 14, 0xFFE0D9E7);

        ClientHudConfig.HudElement settings = selected().settings;
        graphics.drawCenteredString(font,
                Component.translatable("screen.lastsongofelysian.hud_editor.x", settings.x().get()),
                controlsX + 84, 48, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Integer.toString(settings.x().get()),
                controlsX + 84, 74, 0xFFFFD8EE);
        graphics.drawCenteredString(font,
                Component.translatable("screen.lastsongofelysian.hud_editor.y", settings.y().get()),
                controlsX + 84, 88, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Integer.toString(settings.y().get()),
                controlsX + 84, 114, 0xFFFFD8EE);
        graphics.drawCenteredString(font, Component.translatable(
                        "screen.lastsongofelysian.hud_editor.scale", settings.scale().get()),
                controlsX + 84, 128, 0xFFFFFFFF);
        graphics.drawCenteredString(font, String.format("%.1f×", settings.scale().get()),
                controlsX + 84, 154, 0xFFFFD8EE);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPreviews(GuiGraphics graphics) {
        for (int i = 0; i < ENTRIES.size(); i++) {
            Entry entry = ENTRIES.get(i);
            Rect rect = previewRect(entry);
            int color = entry.color;
            int background = (i == selected ? 0x70000000 : 0x3A000000) | (color & 0x00FFFFFF);
            graphics.fill(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, background);
            int border = i == selected ? 0xFFFFFFFF : color;
            graphics.fill(rect.x, rect.y, rect.x + rect.width, rect.y + 1, border);
            graphics.fill(rect.x, rect.y + rect.height - 1, rect.x + rect.width,
                    rect.y + rect.height, border);
            graphics.fill(rect.x, rect.y, rect.x + 1, rect.y + rect.height, border);
            graphics.fill(rect.x + rect.width - 1, rect.y, rect.x + rect.width,
                    rect.y + rect.height, border);
            if (rect.width >= 50 && rect.height >= 10) {
                Component label = Component.translatable(entry.translationKey);
                graphics.drawString(font, label, rect.x + 4,
                        rect.y + Math.max(1, (rect.height - 8) / 2), 0xFFFFFFFF, true);
            }
        }
    }

    private Rect previewRect(Entry entry) {
        double scale = entry.settings.scale().get();
        int scaledWidth = Math.max(1, (int) Math.ceil(entry.width * scale));
        int scaledHeight = Math.max(1, (int) Math.ceil(entry.height * scale));
        int configuredX = entry.settings.x().get();
        int configuredY = entry.settings.y().get();
        int x = configuredX >= 0 ? configuredX : width - scaledWidth + configuredX;
        int y = configuredY >= 0 ? configuredY : height - scaledHeight + configuredY;
        return new Rect(x, y, scaledWidth, scaledHeight);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        Rect rect = previewRect(selected());
        if (button == 0 && rect.contains(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = mouseX - rect.x;
            dragOffsetY = mouseY - rect.y;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (!dragging || button != 0) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        Entry entry = selected();
        Rect rect = previewRect(entry);
        int desiredX = (int) Math.round(mouseX - dragOffsetX);
        int desiredY = (int) Math.round(mouseY - dragOffsetY);
        entry.settings.setX(entry.settings.x().get() < 0
                ? desiredX - (width - rect.width) : desiredX);
        entry.settings.setY(entry.settings.y().get() < 0
                ? desiredY - (height - rect.height) : desiredY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Entry(String translationKey, ClientHudConfig.HudElement settings,
                         int width, int height, int color) {
    }

    private record Rect(int x, int y, int width, int height) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
