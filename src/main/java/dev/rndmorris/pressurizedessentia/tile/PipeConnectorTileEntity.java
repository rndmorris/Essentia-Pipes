package dev.rndmorris.pressurizedessentia.tile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.Vec;
import dev.rndmorris.pressurizedessentia.api.IIOPipeSegment;
import dev.rndmorris.pressurizedessentia.api.PipeColor;
import dev.rndmorris.pressurizedessentia.api.PipeHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class PipeConnectorTileEntity extends TileEntity implements IIOPipeSegment {

    public static final String ID = PressurizedEssentia.modid("PipeConnector");

    public final Map<Vec, Integer> connections = new HashMap<>();

    public PipeConnectorTileEntity() {
        super();
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

    public boolean isConnectable(ForgeDirection direction) {
        return direction != ForgeDirection.UNKNOWN;
    }

    public boolean canInputFrom(ForgeDirection direction) {
        return direction != ForgeDirection.UNKNOWN;
    }

    public boolean canOutputTo(ForgeDirection direction) {
        return direction != ForgeDirection.UNKNOWN;
    }

    public void setSuction(Aspect aspect, int strength) {}

    public Aspect getSuctionType(ForgeDirection direction) {
        return null;
    }

    public int getSuctionAmount(ForgeDirection direction) {
        return 0;
    }

    public int takeEssentia(Aspect aspect, int amount, ForgeDirection direction) {
        return 0;
    }

    public int addEssentia(Aspect var1, int var2, ForgeDirection var3) {
        return 0;
    }

    public Aspect getEssentiaType(ForgeDirection direction) {
        return null;
    }

    public int getEssentiaAmount(ForgeDirection direction) {
        return 0;
    }

    public int getMinimumSuction() {
        return 0;
    }

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
