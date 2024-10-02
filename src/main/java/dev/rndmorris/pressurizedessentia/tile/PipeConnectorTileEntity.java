package dev.rndmorris.pressurizedessentia.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.Vec;
import dev.rndmorris.pressurizedessentia.api.IPipeConnector;
import dev.rndmorris.pressurizedessentia.api.PipeColor;
import dev.rndmorris.pressurizedessentia.api.PipeHelper;
import thaumcraft.api.aspects.AspectList;

public class PipeConnectorTileEntity extends TileEntity implements IPipeConnector {

    public static final String ID = PressurizedEssentia.modid("PipeConnector");

    public PipeConnectorTileEntity() {
        super();
        PressurizedEssentia.LOG.info("Initialized at ({}, {}, {}).", xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    ///
    /// IPipeConnector
    ///

    @Override
    public AspectList wantsEssentia(World world, Vec pos) {
        return new AspectList();
    }

    ///
    /// IPipeSegment
    ///

    @Override
    public PipeColor getPipeColor(World world, int x, int y, int z) {
        final var segment = PipeHelper.getPipeSegment(world, x, y, z);
        return segment != null ? segment.getPipeColor(world, x, y, z) : null;
    }
}
