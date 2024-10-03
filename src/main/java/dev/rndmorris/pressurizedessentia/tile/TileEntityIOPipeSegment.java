package dev.rndmorris.pressurizedessentia.tile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.api.IIOPipeSegment;
import dev.rndmorris.pressurizedessentia.api.PipeColor;
import dev.rndmorris.pressurizedessentia.api.PipeHelper;
import dev.rndmorris.pressurizedessentia.api.Position;
import dev.rndmorris.pressurizedessentia.blocks.BlockPipeSegment;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.TileTube;

public class TileEntityIOPipeSegment extends TileTube implements IIOPipeSegment {

    public static final String CONNECTIONS = "connections";
    public static final int MAX_CONNECTIONS = Integer.MAX_VALUE / 4;

    public static final String ID = PressurizedEssentia.modid("IOPipeSegment");
    public final Map<Position, Integer> connections = new HashMap<>();

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
            BlockPipeSegment.verifyIOState(worldObj, xCoord, yCoord, zCoord);
        }
        internalChange = false;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey(CONNECTIONS)) {
            connections.clear();
            final var data = compound.getIntArray(CONNECTIONS);
            for (var index = 0; index < data.length; index += 4) {
                final int x = data[index], y = data[index + 1], z = data[index + 2], distance = data[index + 3];
                connections.put(new Position(x, y, z), distance);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        final var data = new int[connections.size() * 4];
        var index = 0;
        for (var entry : connections.entrySet()) {
            data[index] = entry.getKey()
                .x();
            data[index + 1] = entry.getKey()
                .y();
            data[index + 2] = entry.getKey()
                .z();
            data[index + 3] = entry.getValue();
            index += 4;
        }
        compound.setIntArray(CONNECTIONS, data);
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
        final var results = PipeHelper
            .findIOSegmentsInNetwork(world, PipeHelper.SearchType.BreadthFirst, new Position(x, y, z))
            .stream()
            .sorted()
            .iterator();
        this.connections.clear();
        while (results.hasNext() && connections.size() < MAX_CONNECTIONS) {
            final var io = results.next();
            if (io.x() == xCoord && io.y() == yCoord && io.z() == zCoord) {
                continue;
            }
            connections.put(new Position(io.x(), io.y(), io.z()), io.distance());
        }
        this.internalChange = true;
        this.markDirty();
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
