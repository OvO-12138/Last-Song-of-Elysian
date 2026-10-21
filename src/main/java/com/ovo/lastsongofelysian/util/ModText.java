package com.ovo.lastsongofelysian.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class ModText {
    private static final Map<String, String> KEYS = loadKeys();
    private static final Node ROOT = createTrie();

    private ModText() {
    }

    public static MutableComponent exact(String text) {
        String key = KEYS.get(text);
        return key == null ? Component.literal(text) : Component.translatable(key);
    }

    public static MutableComponent literal(String text) {
        if (text.isEmpty() || KEYS.containsKey(text)) {
            return exact(text);
        }
        MutableComponent result = Component.empty();
        int plainStart = 0;
        int position = 0;
        while (position < text.length()) {
            Node node = ROOT;
            String key = null;
            int end = position;
            for (int i = position; i < text.length(); i++) {
                node = node.children.get(text.charAt(i));
                if (node == null) break;
                if (node.key != null) {
                    key = node.key;
                    end = i + 1;
                }
            }
            if (key == null) {
                position++;
                continue;
            }
            if (plainStart < position) {
                result.append(Component.literal(text.substring(plainStart, position)));
            }
            result.append(Component.translatable(key));
            position = end;
            plainStart = end;
        }
        if (plainStart == 0) return Component.literal(text);
        if (plainStart < text.length()) {
            result.append(Component.literal(text.substring(plainStart)));
        }
        return result;
    }

    public static Component display(String text) {
        return literal(text);
    }

    public static Component display(Component text) {
        return text;
    }

    public static FormattedCharSequence display(FormattedCharSequence text) {
        return text;
    }

    private static Map<String, String> loadKeys() {
        try (InputStream stream = ModText.class.getResourceAsStream(
                "/assets/lastsongofelysian/localization/source_strings.json")) {
            if (stream == null) throw new IllegalStateException("Missing localization source map");
            Map<String, String> keys = new HashMap<>();
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                for (Map.Entry<String, JsonElement> entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
                    keys.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            return Map.copyOf(keys);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load localization source map", exception);
        }
    }

    private static Node createTrie() {
        Node root = new Node();
        for (Map.Entry<String, String> entry : KEYS.entrySet()) {
            Node node = root;
            for (char character : entry.getKey().toCharArray()) {
                node = node.children.computeIfAbsent(character, ignored -> new Node());
            }
            node.key = entry.getValue();
        }
        return root;
    }

    private static final class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private String key;
    }
}
