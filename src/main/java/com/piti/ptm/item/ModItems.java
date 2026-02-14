package com.piti.ptm.item;

import com.piti.ptm.PitisTech;
import com.piti.ptm.item.custom.FluidTemplateItem;
import com.piti.ptm.item.custom.InfiniteWaterItem;
import com.piti.ptm.item.custom.RadioactiveItem;
import com.piti.ptm.item.custom.ScrewdriverItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PitisTech.MOD_ID);

    public static final RegistryObject<Item> GEIGERCOUNTER = ITEMS.register("geigercounter",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> URANIUM_INGOT = ModItems.ITEMS.register(
            "uranium_ingot",
            () -> new RadioactiveItem(new Item.Properties(), 1.0));
    public static final RegistryObject<Item> INFINITE_WATER = ITEMS.register("infinite_water",
            () -> new InfiniteWaterItem(new Item.Properties()));
    public static final RegistryObject<Item> FLUID_TEMPLATE = ITEMS.register("fluid_id",
            () -> new FluidTemplateItem(new Item.Properties()));
    public static final RegistryObject<Item> SCREWDRIVER = ITEMS.register("screwdriver",
            () -> new ScrewdriverItem(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> INSULATOR = ITEMS.register("insulator",
            () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
