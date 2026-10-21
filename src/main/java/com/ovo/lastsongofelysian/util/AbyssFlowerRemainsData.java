package com.ovo.lastsongofelysian.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class AbyssFlowerRemainsData {
    public static final String KEY = "AbyssFlowerRemains";
    private static final String TRACKED = "AbyssFlowerRemainsTracked";

    private AbyssFlowerRemainsData() {
    }

    public static void attach(ItemEntity item, CompoundTag remains) {
        if (!remains.hasUUID("Id")) return;
        ListTag records = records(item);
        UUID id = remains.getUUID("Id");
        for (Tag entry : records) {
            CompoundTag record = (CompoundTag) entry;
            if (record.hasUUID("Id") && record.getUUID("Id").equals(id)) return;
        }
        records.add(remains.copy());
        item.getPersistentData().put(KEY, records);
        item.getPersistentData().putBoolean(TRACKED, true);
    }

    public static CompoundTag firstAvailable(ItemEntity item, AbyssFlowerResurrectionLedger ledger) {
        for (Tag entry : records(item)) {
            CompoundTag record = (CompoundTag) entry;
            if (record.hasUUID("Id") && !ledger.isRevived(record.getUUID("Id"))) return record;
        }
        return null;
    }

    public static boolean hasTrackedHistory(ItemEntity item) {
        return item.getPersistentData().getBoolean(TRACKED);
    }

    public static void consume(ItemEntity item, UUID id) {
        ListTag records = records(item);
        records.removeIf(entry -> entry instanceof CompoundTag record
                && record.hasUUID("Id") && record.getUUID("Id").equals(id));
        if (records.isEmpty()) item.getPersistentData().remove(KEY);
        else item.getPersistentData().put(KEY, records);
    }

    public static void merge(ItemEntity target, ItemEntity source) {
        if (hasTrackedHistory(source)) target.getPersistentData().putBoolean(TRACKED, true);
        for (Tag entry : records(source)) attach(target, (CompoundTag) entry);
    }

    public static void migrateLegacy(ItemEntity item) {
        ItemStack stack = item.getItem();
        if (!stack.hasTag() || !stack.getTag().contains(KEY, Tag.TAG_COMPOUND)) return;
        attach(item, stack.getTag().getCompound(KEY));
        stack.removeTagKey(KEY);
        item.setItem(stack);
    }

    public static boolean removeLegacyTag(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(KEY)) return false;
        stack.removeTagKey(KEY);
        return true;
    }

    private static ListTag records(ItemEntity item) {
        CompoundTag data = item.getPersistentData();
        return data.contains(KEY, Tag.TAG_LIST) ? data.getList(KEY, Tag.TAG_COMPOUND) : new ListTag();
    }
}
