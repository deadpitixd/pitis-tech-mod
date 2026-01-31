package com.piti.ptm.block.entity;

import com.piti.ptm.PitisTech;
import com.piti.ptm.block.modBlocks;
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
                            modBlocks.LAVA_HEATER.get()).build(null));

    public static final RegistryObject<BlockEntityType<BarrelBlockEntity>> BARREL_BE =
            BLOCK_ENTITIES.register("barrel_be", () ->
                    BlockEntityType.Builder.of(BarrelBlockEntity::new,
                            modBlocks.BARREL_STEEL.get()).build(null));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
