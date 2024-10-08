package dev.rndmorris.essentiapipes.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import dev.rndmorris.essentiapipes.EssentiaPipes;
import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;

public class CreativeTab extends CreativeTabs {

    public CreativeTab() {
        super(EssentiaPipes.MODID);
    }

    @Override
    public Item getTabIconItem() {
        return Item.getItemFromBlock(BlockPipeSegment.pipe_segment);
    }
}
