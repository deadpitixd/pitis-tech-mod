package com.piti.ptm.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class PlayerRadiationData {
    public static final Capability<PlayerRadiationData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    private double radExposure = 0.0f;

    public double getRadExposure() { return this.radExposure; }
    public void setRadExposure(double value) { this.radExposure = value; }

    public void saveNBT(CompoundTag nbt) {
        nbt.putDouble("radExposure", this.radExposure);
    }

    public void loadNBT(CompoundTag nbt) {
        this.radExposure = nbt.getDouble("radExposure");
    }
}