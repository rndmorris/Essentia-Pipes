package dev.rndmorris.pressurizedessentia.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.blocks.PressurizedPipeBlock;

public class CreativeTab extends CreativeTabs {

    public CreativeTab() {
        super(PressurizedEssentia.MODID);
    }

    @Override
    public Item getTabIconItem() {
        return Item.getItemFromBlock(PressurizedPipeBlock.pressurizedPipe);
    }
}
