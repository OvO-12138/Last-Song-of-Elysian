package com.ovo.lastsongofelysian.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AbyssFlowerResurrectionLedger extends SavedData {
    private final Set<UUID> revived = new HashSet<>();

    public static AbyssFlowerResurrectionLedger get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                AbyssFlowerResurrectionLedger::load, AbyssFlowerResurrectionLedger::new,
                "lastsongofelysian_abyss_flower_resurrections");
    }

    public boolean isRevived(UUID id) {
        return revived.contains(id);
    }

    public void markRevived(UUID id) {
        revived.add(id);
        setDirty();
    }

    public static AbyssFlowerResurrectionLedger load(CompoundTag tag) {
        AbyssFlowerResurrectionLedger result = new AbyssFlowerResurrectionLedger();
        for (Tag entry : tag.getList("Revived", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            if (record.hasUUID("Id")) result.revived.add(record.getUUID("Id"));
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        for (UUID id : revived) {
            CompoundTag record = new CompoundTag();
            record.putUUID("Id", id);
            entries.add(record);
        }
        tag.put("Revived", entries);
        return tag;
    }
}
