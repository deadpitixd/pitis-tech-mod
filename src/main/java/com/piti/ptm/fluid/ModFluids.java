package com.piti.ptm.fluid;

import com.piti.ptm.PitisTech;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, PitisTech.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, PitisTech.MOD_ID);

    public static final RegistryObject<FluidType> STEAM_TYPE = FLUID_TYPES.register("steam",
            () -> new BaseFluidType(FluidType.Properties.create()
                    .descriptionId("fluid.ptm.steam")
                    .density(-1000)
                    .viscosity(200),
                    new net.minecraft.resources.ResourceLocation("block/water_still"),
                    new net.minecraft.resources.ResourceLocation("block/water_flow"),
                    0xA0E0E0E0));

    public static final RegistryObject<ForgeFlowingFluid.Source> STEAM_SOURCE = FLUIDS.register("steam",
            () -> new ForgeFlowingFluid.Source(makeSteamProperties()));

    public static final RegistryObject<ForgeFlowingFluid.Flowing> STEAM_FLOWING = FLUIDS.register("steam_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeSteamProperties()));

    private static ForgeFlowingFluid.Properties makeSteamProperties() {
        return new ForgeFlowingFluid.Properties(STEAM_TYPE, STEAM_SOURCE, STEAM_FLOWING);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}