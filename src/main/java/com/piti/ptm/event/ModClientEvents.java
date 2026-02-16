package com.piti.ptm.event;

import com.piti.ptm.PitisTech;
import com.piti.ptm.block.entity.ModBlockEntities;
import com.piti.ptm.block.entity.PipeBlockEntity;
import com.piti.ptm.block.ModBlocks;
import com.piti.ptm.fluid.BaseFluidType;
import com.piti.ptm.item.ModCreativeModTabs;
import com.piti.ptm.item.ModItems;
import com.piti.ptm.item.custom.PunchedCardItem;
import com.piti.ptm.recipe.IndustrialFurnaceRecipe;
import com.piti.ptm.renderer.CableBlockRenderer;
import com.piti.ptm.renderer.PipeBlockRenderer;
import com.piti.ptm.screen.IndustrialFurnaceScreen;
import com.piti.ptm.screen.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;


@Mod.EventBusSubscriber(modid = PitisTech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex == 1) {
                if (stack.hasTag() && stack.getTag().contains("FluidID")) {
                    try {
                        String fluidId = stack.getTag().getString("FluidID");
                        Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidId));

                        if (fluid != null && fluid != Fluids.EMPTY) {
                            int color = IClientFluidTypeExtensions.of(fluid).getTintColor();

                            if (color == -1 || color == 0xFFFFFFFF || fluid.isSame(Fluids.LAVA)) {
                                if (fluid.getFluidType() instanceof BaseFluidType baseType) {
                                    return baseType.getTintColor();
                                }
                                if (fluid.isSame(Fluids.LAVA)) return 0xFFFF4500;
                            }
                            return color;
                        }
                    } catch (Exception e) {
                    }
                }
                return 0x80333333;
            }
            return -1;
        }, ModItems.FLUID_TEMPLATE.get());
    }
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && level.getBlockEntity(pos) instanceof PipeBlockEntity be) {
                return be.color;
            }
            return 0x80333333;
        }, ModBlocks.UNIVERSAL_PIPE.get());
    }
    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PIPE.get(), PipeBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CABLE_BE.get(), CableBlockRenderer::new);
    }
    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, "block/pipe_toggle"));
        event.register(ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, "block/gas_centrifuge"));
    }
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.INDUSTRIAL_FURNACE_MENU.get(), IndustrialFurnaceScreen::new);
        });
    }
}