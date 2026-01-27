package com.piti.ptm.block;

import com.piti.ptm.PitisTech;
import com.piti.ptm.item.ModItems;
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

    private static <T extends Block>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block){
        return ModItems.ITEMS.register(name, ()-> new BlockItem(block.get(), new Item.Properties()));
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
