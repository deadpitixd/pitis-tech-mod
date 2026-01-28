package com.piti.ptm.block.custom;

import com.piti.ptm.radioactive.Radioactive;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RadioactiveBlock extends Block implements Radioactive {
    public RadioactiveBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
