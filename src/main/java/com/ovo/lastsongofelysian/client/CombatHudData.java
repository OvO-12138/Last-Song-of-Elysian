package com.ovo.lastsongofelysian.client;

public final class CombatHudData {
    public static boolean bloodReleaseActive;
    public static int bloodReleaseCharge;
    public static boolean vipActive;
    public static int vipLevel;
    public static boolean resolveActive;
    public static int resolve;
    public static boolean evasionActive;
    public static int evasionCooldown;
    public static int evasionCooldownMaximum;
    public static int evasionUses;
    public static int evasionMaximumUses;

    private CombatHudData() {
    }

    public static void update(boolean bloodActive, int bloodCharge,
                              boolean hasVip, int vip,
                              boolean hasResolve, int resolveCount,
                              boolean hasEvasion, int cooldown, int cooldownMaximum,
                              int uses, int maximumUses) {
        bloodReleaseActive = bloodActive;
        bloodReleaseCharge = Math.max(0, Math.min(1000, bloodCharge));
        vipActive = hasVip;
        vipLevel = Math.max(0, vip);
        resolveActive = hasResolve;
        resolve = Math.max(0, Math.min(6, resolveCount));
        evasionActive = hasEvasion;
        evasionCooldown = Math.max(0, cooldown);
        evasionCooldownMaximum = Math.max(1, cooldownMaximum);
        evasionUses = Math.max(0, uses);
        evasionMaximumUses = Math.max(1, maximumUses);
    }
}
