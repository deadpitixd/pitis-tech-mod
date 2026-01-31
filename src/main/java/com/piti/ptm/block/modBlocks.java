package com.piti.ptm.block;

import com.piti.ptm.PitisTech;
import com.piti.ptm.block.custom.BarrelBlock;
import com.piti.ptm.block.custom.LavaHeaterBlock;
import com.piti.ptm.block.custom.RotatableBlock;
import com.piti.ptm.item.ModItems;
import com.piti.ptm.item.custom.RadioactiveBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class modBlocks {
    public static final DeferredRegister<Block> BLOCKS=
            DeferredRegister.create(ForgeRegistries.BLOCKS, PitisTech.MOD_ID);

    public static final RegistryObject<Block> BRICK_CONCRETE = registerBlock("brick_concrete",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.LIGHT_GRAY_CONCRETE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0f, 300)));
    public static final RegistryObject<Block> CONCRETE = registerBlock("concrete",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.LIGHT_GRAY_CONCRETE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0f, 120)));
    public static final RegistryObject<Block> REINFORCED_LIGHT = registerBlock("reinforced_light",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SEA_LANTERN)
                    .lightLevel(state -> 15)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 260)));
    public static final RegistryObject<Block> URANIUM_ORE = registerRadioactiveBlock("uranium_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
                    .strength(3.0f)),5);
    public static final RegistryObject<Block> STEAM_TURBINE = registerBlock("steam_turbine",
            () -> new RotatableBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(5.0f,5)));
    public static final RegistryObject<Block> LAVA_HEATER = registerBlock("lava_heater",
            () -> new LavaHeaterBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(5.0f,5).noOcclusion()));
    public static final RegistryObject<Block> BARREL_STEEL = registerBlock("barrel_steel",
            () -> new BarrelBlock(BlockBehaviour.Properties.copy(Blocks.ANVIL)
                    .strength(5.0f,5).noOcclusion()));

    private static <T extends Block>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block){
        return ModItems.ITEMS.register(name, ()-> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> registerRadioactiveBlock(
            String name, Supplier<T> blockSupplier, double defaultRadPerSecond) {

        RegistryObject<T> regBlock = BLOCKS.register(name, blockSupplier);

        ModItems.ITEMS.register(name, () -> new RadioactiveBlockItem(regBlock.get(), new Item.Properties(), defaultRadPerSecond));

        return regBlock;
    }


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        RegistryObject<T> toReturn = BLOCKS.register(name,block);
        registerBlockItem(name,toReturn);
        return toReturn;
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
