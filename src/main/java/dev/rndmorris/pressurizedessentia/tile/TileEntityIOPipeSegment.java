package dev.rndmorris.pressurizedessentia.tile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.Vec;
import dev.rndmorris.pressurizedessentia.api.IIOPipeSegment;
import dev.rndmorris.pressurizedessentia.api.PipeColor;
import dev.rndmorris.pressurizedessentia.api.PipeHelper;
import dev.rndmorris.pressurizedessentia.blocks.BlockPipeSegment;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.tiles.TileTube;

public class TileEntityIOPipeSegment extends TileTube implements IIOPipeSegment {

    public static final String ID = PressurizedEssentia.modid("IOPipeSegment");
    public final Map<Vec, Integer> connections = new HashMap<>();

    private boolean internalChange = false;

    public TileEntityIOPipeSegment() {
        super();
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!internalChange) {
            BlockPipeSegment.checkIfShouldBeIO(worldObj, xCoord, yCoord, zCoord);
        }
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
    /// IEssentiaTransport
    ///

    @Override
    public void setSuction(Aspect aspect, int strength) {}

    @Override
    public Aspect getSuctionType(ForgeDirection direction) {
        return null;
    }

    @Override
    public int getSuctionAmount(ForgeDirection direction) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, ForgeDirection direction) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect var1, int var2, ForgeDirection var3) {
        return 0;
    }

    @Override
    public Aspect getEssentiaType(ForgeDirection direction) {
        return null;
    }

    @Override
    public int getEssentiaAmount(ForgeDirection direction) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }

    ///
    /// IPipeConnector
    ///

    @Override
    public void rebuildIOConnections(World world, int x, int y, int z) {
        final var connectors = PipeHelper.findAllIOSegments(worldObj, xCoord, yCoord, zCoord);
        this.connections.clear();
        for (var con : connectors) {
            if (con.x() == xCoord && con.y() == yCoord && con.z() == zCoord) {
                continue;
            }
            connections.put(new Vec(con.x(), con.y(), con.z()), con.distance());
        }
    }

    @Override
    public AspectList wantsEssentia(World world, int x, int y, int z) {
        return new AspectList();
    }

    ///
    /// IPipeSegment
    ///

    @Override
    public PipeColor getPipeColor(World world, int x, int y, int z) {
        final var segment = PipeHelper.getPipeSegment(world, xCoord, yCoord, zCoord);
        return segment != null ? segment.getPipeColor(world, xCoord, yCoord, zCoord) : null;
    }
}
