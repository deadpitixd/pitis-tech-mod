package com.piti.ptm.block.entity.machines;

import com.piti.ptm.block.custom.machines.IndustrialHatchBlock;
import com.piti.ptm.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IndustrialHatchBlockEntity extends BlockEntity {
    public IndustrialHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIAL_HATCH_BE.get(), pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockEntity be = level.getBlockEntity(worldPosition.offset(x, y, z));
                        if (be instanceof IndustrialFurnaceCoreBlockEntity core) {
                            boolean isOutput = getBlockState().getValue(IndustrialHatchBlock.IS_OUTPUT);
                            return LazyOptional.of(() -> new HatchInventoryWrapper(core.getItemHandler(), isOutput)).cast();
                        }
                    }
                }
            }
        }
        return super.getCapability(cap, side);
    }
}