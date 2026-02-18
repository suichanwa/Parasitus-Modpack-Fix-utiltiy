package com.toomda.parasitusfix.intro;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class FirstJoinIntroData extends WorldSavedData {
    private static final String DATA_NAME = "parasitusfix_first_join_intro";
    private static final String KEY_SEEN = "seenPlayers";

    private NBTTagCompound seenPlayers = new NBTTagCompound();

    public FirstJoinIntroData() {
        super(DATA_NAME);
    }

    public FirstJoinIntroData(String name) {
        super(name);
    }

    public static FirstJoinIntroData get(World world) {
        MapStorage storage = world.getMapStorage();
        FirstJoinIntroData data = (FirstJoinIntroData) storage.getOrLoadData(FirstJoinIntroData.class, DATA_NAME);
        if (data == null) {
            data = new FirstJoinIntroData();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    public boolean markShown(UUID playerId) {
        String key = playerId.toString();
        if (seenPlayers.getBoolean(key)) return false;
        seenPlayers.setBoolean(key, true);
        markDirty();
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        seenPlayers = nbt.hasKey(KEY_SEEN, 10) ? nbt.getCompoundTag(KEY_SEEN) : new NBTTagCompound();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag(KEY_SEEN, seenPlayers);
        return compound;
    }
}
