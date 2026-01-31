package com.piti.ptm.fluid;

import com.piti.ptm.PitisTech;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {
    
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create((ResourceLocation) ForgeRegistries.FLUID_TYPES, PitisTech.MOD_ID);

    public static final RegistryObject<FluidType> STEAM = FLUID_TYPES.register("steam",
            () -> new FluidType(FluidType.Properties.create()) {});

    public static final RegistryObject<FluidType> WATER = FLUID_TYPES.register("water",
            () -> new FluidType(FluidType.Properties.create()) {});

    public static final RegistryObject<FluidType> LAVA = FLUID_TYPES.register("lava",
            () -> new FluidType(FluidType.Properties.create()) {});

    // Call this in your main mod class to register
    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
