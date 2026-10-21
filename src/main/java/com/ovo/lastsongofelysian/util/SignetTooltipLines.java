package com.ovo.lastsongofelysian.util;

import net.minecraft.network.chat.Component;

import java.util.List;

public final class SignetTooltipLines {
    private static final int MAX_COLUMNS = 52;

    private SignetTooltipLines() {
    }

    public static void addTranslated(List<Component> tooltip, String key, int color) {
        addText(tooltip, Component.translatable(key).getString(), color);
    }

    public static void addText(List<Component> tooltip, String text, int color) {
        for (String paragraph : text.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                tooltip.add(Component.empty());
                continue;
            }
            StringBuilder line = new StringBuilder();
            int columns = 0;
            for (String word : paragraph.split("(?<=\\s)|(?=\\s)")) {
                int width = displayColumns(word);
                if (columns > 0 && columns + width > MAX_COLUMNS) {
                    tooltip.add(styled(line.toString().stripTrailing(), color));
                    line.setLength(0);
                    columns = 0;
                }
                if (width > MAX_COLUMNS) {
                    for (int offset = 0; offset < word.length(); offset++) {
                        char character = word.charAt(offset);
                        int charWidth = character < 128 ? 1 : 2;
                        if (columns + charWidth > MAX_COLUMNS) {
                            tooltip.add(styled(line.toString().stripTrailing(), color));
                            line.setLength(0);
                            columns = 0;
                        }
                        line.append(character);
                        columns += charWidth;
                    }
                } else if (!word.isBlank() || columns > 0) {
                    line.append(word);
                    columns += width;
                }
            }
            if (line.length() > 0) tooltip.add(styled(line.toString().stripTrailing(), color));
        }
    }

    private static int displayColumns(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) width += text.charAt(i) < 128 ? 1 : 2;
        return width;
    }

    private static Component styled(String text, int color) {
        return Component.literal(text).withStyle(style -> style.withColor(color).withItalic(true));
    }
}
