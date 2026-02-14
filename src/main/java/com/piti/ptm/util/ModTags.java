package com.piti.ptm.util;

import com.piti.ptm.PitisTech;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks{
        public static final TagKey<Block> FURNACE_REQUIRED = tag("furnace_required");
        public static final TagKey<Block> FURNACE_OPTIONAL = tag("furnace_optional");
        private static TagKey<Block> tag(String name){
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, name));
        }
    }
    public static class Items{

        private static TagKey<Item> tag(String name){
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, name));
        }
    }
}