package com.piti.ptm.menu;

import com.piti.ptm.PitisTech;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, PitisTech.MOD_ID);

    public static final RegistryObject<MenuType<LavaHeaterMenu>> LAVA_HEATER_MENU =
            MENUS.register("lava_heater_menu",
                    () -> IForgeMenuType.create(LavaHeaterMenu::new));
}
