package com.piti.ptm.block;

import com.piti.ptm.PitisTech;
import com.piti.ptm.block.custom.*;
import com.piti.ptm.block.custom.machines.*;
import com.piti.ptm.item.ModItems;
import com.piti.ptm.item.custom.RadShieldItem;
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

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS=
            DeferredRegister.create(ForgeRegistries.BLOCKS, PitisTech.MOD_ID);

    public static final RegistryObject<Block> BRICK_CONCRETE = registerRadShieldBlock("brick_concrete",
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
            () -> new SteamTurbineBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(5.0f,5).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> LAVA_HEATER = registerBlock("lava_heater",
            () -> new LavaHeaterBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(5.0f,5).noOcclusion().requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> BARREL_STEEL = registerBlock("barrel_steel",
            () -> new BarrelBlock(BlockBehaviour.Properties.copy(Blocks.ANVIL)
                    .strength(5.0f,5).noOcclusion().requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> UNIVERSAL_PIPE = registerBlock("universal_pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(5.0f,5).noOcclusion().requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> GAS_CENTRIFUGE = registerBlock("gas_centrifuge",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(5.0f, 5)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> INDUSTRIAL_STEEL = registerBlock("industrial_steel",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 180)));
    public static final RegistryObject<Block> INDUSTRIAL_FURNACE_ACCESSPORT = registerBlock("industrial_furnace_accessport",
            () -> new IndustrialFurnaceAccessPortBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 180)));
    public static final RegistryObject<Block> INDUSTRIAL_FURNACE_CABLEPORT = registerBlock("industrial_furnace_cableport",
            () -> new IndustrialCablePortBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 180)));
    public static final RegistryObject<Block> INDUSTRIAL_FURNACE_CORE = registerBlock("industrial_furnace_core",
            () -> new IndustrialFurnaceCoreBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 180)));
    public static final RegistryObject<Block> INDUSTRIAL_FURNACE_FLUIDPORT = registerBlock("industrial_furnace_fluidport",
            () -> new IndustrialFluidPortBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 180)));
    public static final RegistryObject<Block> INDUSTRIAL_FURNACE_STORAGE = registerBlock("industrial_furnace_storage",
            () -> new IndustrialHatchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 180)));
    public static final RegistryObject<Block> BINARY_PRESS_BOTTOM = registerBlock("binary_press_bottom",
            () -> new BinaryPressBaseBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> BINARY_PRESS_TOP = registerBlock("binary_press_upper",
            () -> new BinaryPressTopBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));
    public static final RegistryObject<Block> CABLE = registerBlock("cable",
            () -> new CableBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(5.0f,5).noOcclusion().requiresCorrectToolForDrops()));



    private static <T extends Block>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block){
        return ModItems.ITEMS.register(name, ()-> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> registerRadioactiveBlock(
            String name, Supplier<T> blockSupplier, double defaultRadPerSecond) {

        RegistryObject<T> regBlock = BLOCKS.register(name, blockSupplier);

        ModItems.ITEMS.register(name, () -> new RadioactiveBlockItem(regBlock.get(), new Item.Properties(), defaultRadPerSecond));

        return regBlock;
    }
    private static <T extends Block> RegistryObject<T> registerRadShieldBlock(
            String name, Supplier<T> blockSupplier) {

        RegistryObject<T> regBlock = BLOCKS.register(name, blockSupplier);

        ModItems.ITEMS.register(name, () -> new RadShieldItem(regBlock.get(), new Item.Properties()));

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
