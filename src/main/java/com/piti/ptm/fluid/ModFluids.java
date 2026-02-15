package com.piti.ptm.fluid;

import com.piti.ptm.PitisTech;
import net.minecraft.resources.ResourceLocation;
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
                    ResourceLocation.parse("block/water_still"),
                    ResourceLocation.parse("block/water_flow"),
                    0xA0E0E0E0));
    public static final RegistryObject<FluidType> LPSTEAM_TYPE = FLUID_TYPES.register("lpsteam",
            () -> new BaseFluidType(FluidType.Properties.create()
                    .descriptionId("fluid.ptm.lpsteam")
                    .density(-1000)
                    .viscosity(200),
                    ResourceLocation.parse("block/water_still"),
                    ResourceLocation.parse("block/water_flow"),
                    0xB7DBE8));
    public static final RegistryObject<FluidType> ELECTRICFLUID_TYPE = FLUID_TYPES.register("electricfluid",
            () -> new BaseFluidType(FluidType.Properties.create()
                    .descriptionId("fluid.ptm.electricfluid")
                    .density(10)
                    .viscosity(1000),
                    ResourceLocation.parse("block/water_still"),
                    ResourceLocation.parse("block/water_flow"),
                    0xFF8CEF));
    public static final RegistryObject<FluidType> NONE_TYPE = FLUID_TYPES.register("nonefluid",
            () -> new BaseFluidType(FluidType.Properties.create()
                    .descriptionId("fluid.ptm.none")
                    .density(0)
                    .viscosity(0),
                    ResourceLocation.parse("block/water_still"),
                    ResourceLocation.parse("block/water_flow"),
                    0x80333333));


    public static final RegistryObject<ForgeFlowingFluid.Source> STEAM_SOURCE = FLUIDS.register("steam",
            () -> new ForgeFlowingFluid.Source(makeSteamProperties()));

    public static final RegistryObject<ForgeFlowingFluid.Flowing> STEAM_FLOWING = FLUIDS.register("steam_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeSteamProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Source> LPSTEAM_SOURCE = FLUIDS.register("lpsteam",
            () -> new ForgeFlowingFluid.Source(makeLPSteamProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> LPSTEAM_FLOWING = FLUIDS.register("lpsteam_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeLPSteamProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Source> ELECTRICFLUID_SOURCE = FLUIDS.register("electricfluid",
            () -> new ForgeFlowingFluid.Source(makeElectricFluidProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> ELECTRICFLUID_FLOWING = FLUIDS.register("electricfluid_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeElectricFluidProperties()));

    public static final RegistryObject<ForgeFlowingFluid.Source> NONE_SOURCE = FLUIDS.register("none",
            () -> new ForgeFlowingFluid.Source(makeNoneProperties()));

    private static ForgeFlowingFluid.Properties makeNoneProperties() {
        return new ForgeFlowingFluid.Properties(NONE_TYPE, NONE_SOURCE, NONE_FLOWING);
    }

    public static final RegistryObject<ForgeFlowingFluid.Flowing> NONE_FLOWING = FLUIDS.register("none_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeNoneProperties()));


    private static ForgeFlowingFluid.Properties makeSteamProperties() {
        return new ForgeFlowingFluid.Properties(STEAM_TYPE, STEAM_SOURCE, STEAM_FLOWING);
    }
    private static ForgeFlowingFluid.Properties makeLPSteamProperties() {
        return new ForgeFlowingFluid.Properties(LPSTEAM_TYPE, LPSTEAM_SOURCE, LPSTEAM_FLOWING);
    }
    private static ForgeFlowingFluid.Properties makeElectricFluidProperties() {
        return new ForgeFlowingFluid.Properties(ELECTRICFLUID_TYPE, ELECTRICFLUID_SOURCE, ELECTRICFLUID_FLOWING);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}