package com.piti.ptm.screen;

import com.piti.ptm.PitisTech;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, PitisTech.MOD_ID);

    public static final RegistryObject<MenuType<LavaHeaterMenu>> LAVA_HEATER_MENU =
            registerMenuType("lava_heater_menu", LavaHeaterMenu::new);
    public static final RegistryObject<MenuType<BarrelMenu>> BARREL_MENU =
            registerMenuType("barrel_menu", BarrelMenu::new);
    public static final RegistryObject<MenuType<IndustrialFurnaceMenu>> INDUSTRIAL_FURNACE_MENU =
            registerMenuType("industrial_furnace_menu", IndustrialFurnaceMenu::new);


    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }
    public static void register(IEventBus eventBus){
        MENUS.register(eventBus);
    }
}
