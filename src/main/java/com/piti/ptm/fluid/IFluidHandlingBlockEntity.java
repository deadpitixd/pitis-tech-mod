package com.piti.ptm.fluid;

import net.minecraftforge.fluids.capability.templates.FluidTank;

public interface IFluidHandlingBlockEntity {
    FluidTank getTank(int index);

    String getTankLabel(int index);
}