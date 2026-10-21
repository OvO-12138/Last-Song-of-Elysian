package com.ovo.lastsongofelysian.util;

public final class ExtensionSignetColors {
    private ExtensionSignetColors() {
    }

    public static int name(SignetUpgradeData.Signet signet, String id) {
        return switch (signet) {
            case REVERIE -> 0x555555;
            case VICISSITUDE -> 0xD4680A;
            case INFINITY -> 0x2F8B57;
            case STARS -> stars(id, false);
            default -> 0xD8B4FF;
        };
    }

    public static int effect(SignetUpgradeData.Signet signet, String id) {
        return switch (signet) {
            case REVERIE -> 0xAAAAAA;
            case VICISSITUDE -> 0xFFA040;
            case INFINITY -> 0x6ED69A;
            case STARS -> stars(id, true);
            default -> 0xD8B4FF;
        };
    }

    private static int stars(String id, boolean light) {
        if (id.contains("wildflower")) return light ? 0xD8A6FF : 0xA95BE0;
        if (id.contains("flame")) return light ? 0xFFB36B : 0xE87424;
        if (id.contains("leaf")) return light ? 0x9BE59B : 0x51AF68;
        if (id.contains("yellow")) return light ? 0xFFE990 : 0xE8C52F;
        if (id.contains("black")) return light ? 0xA9A3B0 : 0x55505C;
        if (id.contains("white")) return light ? 0xFFFFFF : 0xE8E8F0;
        if (id.contains("gray")) return light ? 0xC4C4CC : 0x888894;
        if (id.contains("dim")) return light ? 0xAAA8FF : 0x6D68D9;
        return light ? 0xBCEBFF : 0x69BFE7;
    }
}
