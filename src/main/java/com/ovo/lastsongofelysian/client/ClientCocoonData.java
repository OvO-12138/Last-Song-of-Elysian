package com.ovo.lastsongofelysian.client;

public final class ClientCocoonData {

    private static int coreStage = 0;
    private static int acquiredCoreMask;
    private static boolean reversed;
    private static boolean trialsUnlocked;
    private static int completedTrialMask;
    private static int thunderStrikes;
    private static int thunderKills;
    private static int earthExplosions;

    private ClientCocoonData() {
    }

    public static int getCoreStage() {
        return coreStage;
    }

    public static void setCoreStage(int stage) {
        coreStage = Math.max(0, Math.min(12, stage));
    }

    public static int getAcquiredCoreMask() {
        return acquiredCoreMask;
    }

    public static void setAcquiredCoreMask(int mask) {
        acquiredCoreMask = mask & 0x1FFF;
    }

    public static boolean hasCore(int coreIndex) {
        return coreIndex >= 0
                && coreIndex < 12
                && (acquiredCoreMask & (1 << coreIndex)) != 0;
    }

    public static boolean isReversed() {
        return reversed;
    }

    public static void setReversed(boolean value) {
        reversed = value;
    }

    public static boolean areTrialsUnlocked() {
        return trialsUnlocked;
    }

    public static void setTrialsUnlocked(boolean value) {
        trialsUnlocked = value;
    }

    public static int getCompletedTrialMask() {
        return completedTrialMask;
    }

    public static void setCompletedTrialMask(int mask) {
        completedTrialMask = mask & 0xFFF;
    }

    public static int getThunderStrikes() {
        return thunderStrikes;
    }

    public static void setThunderStrikes(int value) {
        thunderStrikes = Math.max(0, Math.min(5, value));
    }

    public static int getThunderKills() {
        return thunderKills;
    }

    public static void setThunderKills(int value) {
        thunderKills = Math.max(0, Math.min(20, value));
    }

    public static int getEarthExplosions() {
        return earthExplosions;
    }

    public static void setEarthExplosions(int value) {
        earthExplosions = Math.max(0, Math.min(10, value));
    }

    public static boolean isTrialComplete(int trial) {
        return trial >= 0 && trial < 12 && (completedTrialMask & (1 << trial)) != 0;
    }

    public static void reset() {
        coreStage = 0;
        acquiredCoreMask = 0;
        reversed = false;
        trialsUnlocked = false;
        completedTrialMask = 0;
        thunderStrikes = 0;
        thunderKills = 0;
        earthExplosions = 0;
    }
}
