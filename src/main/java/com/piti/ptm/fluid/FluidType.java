package com.piti.ptm.fluid;

public enum FluidType {
    WATER,
    LAVA,
    STEAM;

    public String getTranslationKey() {
        return "fluid.ptm." + this.name().toLowerCase();
    }
}
