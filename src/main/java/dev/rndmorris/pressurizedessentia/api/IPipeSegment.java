package dev.rndmorris.pressurizedessentia.api;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public interface IPipeSegment {

    boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection face);

    PipeColor getPipeColor(IBlockAccess world, int x, int y, int z);

}
