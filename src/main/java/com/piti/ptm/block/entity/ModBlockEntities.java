package com.piti.ptm.block.entity;

import com.piti.ptm.PitisTech;
import com.piti.ptm.block.entity.machines.IndustrialFurnaceCoreBlockEntity;
import com.piti.ptm.block.ModBlocks;
import com.piti.ptm.block.entity.machines.IndustrialFurnacePortBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PitisTech.MOD_ID);

    public static final RegistryObject<BlockEntityType<LavaHeaterBlockEntity>> LAVA_HEATER_BE =
            BLOCK_ENTITIES.register("lava_heater_be", () ->
                    BlockEntityType.Builder.of(LavaHeaterBlockEntity::new,
                            ModBlocks.LAVA_HEATER.get()).build(null));

    public static final RegistryObject<BlockEntityType<BarrelBlockEntity>> BARREL_BE =
            BLOCK_ENTITIES.register("barrel_be", () ->
                    BlockEntityType.Builder.of(BarrelBlockEntity::new,
                            ModBlocks.BARREL_STEEL.get()).build(null));
    public static final RegistryObject<BlockEntityType<PipeBlockEntity>> PIPE =
            BLOCK_ENTITIES.register("pipe_be", () ->
                    BlockEntityType.Builder.of(PipeBlockEntity::new,
                            ModBlocks.UNIVERSAL_PIPE.get()).build(null));
    public static final RegistryObject<BlockEntityType<IndustrialFurnaceCoreBlockEntity>> INDUSTRIALFURNACE_BE =
            BLOCK_ENTITIES.register("indfurnace_be", () ->
                    BlockEntityType.Builder.of(IndustrialFurnaceCoreBlockEntity::new,
                            ModBlocks.INDUSTRIAL_FURNACE_CORE.get()).build(null));
    public static final RegistryObject<BlockEntityType<IndustrialFurnacePortBlockEntity>> INDUSTRIALFURNACEPORT_BE =
            BLOCK_ENTITIES.register("indfurnaceport_be", () ->
                    BlockEntityType.Builder.of(IndustrialFurnacePortBlockEntity::new,
                            ModBlocks.INDUSTRIAL_FURNACE_ACCESSPORT.get(),ModBlocks.INDUSTRIAL_FURNACE_CABLEPORT.get(),
                            ModBlocks.INDUSTRIAL_FURNACE_FLUIDPORT.get()).build(null));



    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
