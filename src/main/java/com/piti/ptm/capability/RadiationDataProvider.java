package com.piti.ptm.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RadiationDataProvider implements ICapabilitySerializable<CompoundTag> {
    private final PlayerRadiationData data = new PlayerRadiationData();
    private final LazyOptional<PlayerRadiationData> optional = LazyOptional.of(() -> data);

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        data.saveNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.loadNBT(nbt);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == PlayerRadiationData.INSTANCE ? optional.cast() : LazyOptional.empty();
    }

    public void invalidate() {
        optional.invalidate();
    }
}