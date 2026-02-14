package com.piti.ptm.item;

import com.piti.ptm.PitisTech;
import com.piti.ptm.block.modBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PitisTech.MOD_ID);

    public static void register(IEventBus eventbus){
        CREATIVE_MODE_TABS.register(eventbus);
    }

    public static final RegistryObject<CreativeModeTab> PTMITEMS_TAB = CREATIVE_MODE_TABS.register("ptmitems_tab",
            ()-> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.GEIGERCOUNTER.get()))
                    .title(Component.translatable("creativetab.ptmitems_tab"))
                    .displayItems((pParameters, pOutput) ->{
                        pOutput.accept(ModItems.GEIGERCOUNTER.get());
                        pOutput.accept(ModItems.INFINITE_WATER.get());
                        pOutput.accept(ModItems.SCREWDRIVER.get());
                        pOutput.accept(ModItems.STEEL_INGOT.get());
                        pOutput.accept(ModItems.INSULATOR.get());
                    })
                    .build());


    public static final RegistryObject<CreativeModeTab> PTMBLOCKS_TAB = CREATIVE_MODE_TABS.register("ptmblocks_tab",
            ()-> CreativeModeTab.builder().icon(() -> new ItemStack(modBlocks.BRICK_CONCRETE.get()))
                    .title(Component.translatable("creativetab.ptmblocks_tab"))
                    .displayItems((pParameters, pOutput) ->{
                        pOutput.accept(modBlocks.BRICK_CONCRETE.get());
                        pOutput.accept(modBlocks.CONCRETE.get());
                        pOutput.accept(modBlocks.REINFORCED_LIGHT.get());
                        pOutput.accept(modBlocks.INDUSTRIAL_STEEL.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> PTMFUELMACHINES_TAB = CREATIVE_MODE_TABS.register("ptmfuelmachines_tab",
            ()-> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.URANIUM_INGOT.get()))
                    .title(Component.translatable("creativetab.ptm.fuelmachines_tab"))
                    .displayItems((pParameters, pOutput) ->{
                        pOutput.accept(modBlocks.UNIVERSAL_PIPE.get());
                        pOutput.accept(modBlocks.STEAM_TURBINE.get());
                        pOutput.accept(modBlocks.URANIUM_ORE.get());
                        pOutput.accept(ModItems.URANIUM_INGOT.get());
                        pOutput.accept(modBlocks.LAVA_HEATER.get());
                        pOutput.accept(modBlocks.BARREL_STEEL.get());
                        // ONLY ENABLE THIS WHEN THE GAS CENTRIFUGE WILL BE FIXED - piti
                        //pOutput.accept(modBlocks.GAS_CENTRIFUGE.get());
                        pOutput.accept(modBlocks.INDUSTRIAL_STEEL.get());
                        pOutput.accept(modBlocks.INDUSTRIAL_FURNACE_ACCESSPORT.get());
                        pOutput.accept(modBlocks.INDUSTRIAL_FURNACE_CABLEPORT.get());
                        pOutput.accept(modBlocks.INDUSTRIAL_FURNACE_CORE.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> PTM_TAB = CREATIVE_MODE_TABS.register("ptm_machinetemplates",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.ptm.templates"))
                    .icon(() -> new ItemStack(ModItems.FLUID_TEMPLATE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FLUID_TEMPLATE.get());

                        for (Fluid fluid : ForgeRegistries.FLUIDS) {
                            if (fluid.isSource(fluid.defaultFluidState())) {
                                ItemStack stack = new ItemStack(ModItems.FLUID_TEMPLATE.get());
                                CompoundTag nbt = stack.getOrCreateTag();

                                String fluidId = ForgeRegistries.FLUIDS.getKey(fluid).toString();
                                nbt.putString("FluidID", fluidId);

                                output.accept(stack);
                            }
                        }
                    }).build());

}
