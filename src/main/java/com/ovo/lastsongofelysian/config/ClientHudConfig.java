package com.ovo.lastsongofelysian.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientHudConfig {
    public static final ForgeConfigSpec SPEC;
    public static final HudElement CORRUPTION;
    public static final HudElement WEAPON_ENERGY;
    public static final HudElement BLOOD_RELEASE;
    public static final HudElement DISCIPLINE;
    public static final HudElement COMBO;
    public static final HudElement EVASION;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment(
                "HUD coordinates use the scaled game window.",
                "Positive X/Y values are measured from the left/top.",
                "Negative X/Y values are margins measured from the right/bottom."
        ).push("hud");
        CORRUPTION = define(builder, "corruption", true, 10, -36, 1.0D);
        WEAPON_ENERGY = define(builder, "weapon_energy", true, 10, -57, 1.0D);
        BLOOD_RELEASE = define(builder, "blood_release", true, 10, -78, 1.0D);
        DISCIPLINE = define(builder, "discipline", true, -14, 44, 1.0D);
        COMBO = define(builder, "combo", true, -14, 74, 1.0D);
        EVASION = define(builder, "evasion", true, -14, 148, 1.0D);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientHudConfig() {
    }

    private static HudElement define(ForgeConfigSpec.Builder builder, String name,
                                     boolean enabled, int x, int y, double scale) {
        builder.push(name);
        ForgeConfigSpec.BooleanValue enabledValue = builder
                .comment("Whether this HUD element is visible when its gameplay condition is met.")
                .define("enabled", enabled);
        ForgeConfigSpec.IntValue xValue = builder
                .comment("Horizontal position. Negative values anchor from the right edge.")
                .defineInRange("x", x, -10000, 10000);
        ForgeConfigSpec.IntValue yValue = builder
                .comment("Vertical position. Negative values anchor from the bottom edge.")
                .defineInRange("y", y, -10000, 10000);
        ForgeConfigSpec.DoubleValue scaleValue = builder
                .comment("HUD size multiplier.")
                .defineInRange("scale", scale, 0.25D, 4.0D);
        builder.pop();
        return new HudElement(enabledValue, xValue, yValue, scaleValue,
                enabled, x, y, scale);
    }

    public record HudElement(ForgeConfigSpec.BooleanValue enabled,
                             ForgeConfigSpec.IntValue x,
                             ForgeConfigSpec.IntValue y,
                             ForgeConfigSpec.DoubleValue scale,
                             boolean defaultEnabled,
                             int defaultX,
                             int defaultY,
                             double defaultScale) {
        public void setEnabled(boolean value) {
            enabled.set(value);
            enabled.save();
        }

        public void setX(int value) {
            x.set(Math.max(-10000, Math.min(10000, value)));
            x.save();
        }

        public void setY(int value) {
            y.set(Math.max(-10000, Math.min(10000, value)));
            y.save();
        }

        public void setScale(double value) {
            scale.set(Math.max(0.25D, Math.min(4.0D, value)));
            scale.save();
        }

        public void reset() {
            setEnabled(defaultEnabled);
            setX(defaultX);
            setY(defaultY);
            setScale(defaultScale);
        }
    }
}
