package dev.rndmorris.pressurizedessentia.tile;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.api.ConnectionInfo;
import dev.rndmorris.pressurizedessentia.api.EssentiaRequest;
import dev.rndmorris.pressurizedessentia.api.IIOPipeSegment;
import dev.rndmorris.pressurizedessentia.api.PipeColor;
import dev.rndmorris.pressurizedessentia.api.PipeHelper;
import dev.rndmorris.pressurizedessentia.api.WorldCoordinate;
import dev.rndmorris.pressurizedessentia.blocks.BlockPipeSegment;
import dev.rndmorris.pressurizedessentia.data.ConnectionSet;
import dev.rndmorris.pressurizedessentia.data.EssentiaRequestSet;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.tiles.TileTube;

public class TileEntityIOPipeSegment extends TileTube implements IIOPipeSegment {

    public static final String CONNECTIONS = "connections";
    public static final String ID = PressurizedEssentia.modid("IOPipeSegment");
    public static final byte INTERVAL = 10;

    public final ConnectionSet connections = new ConnectionSet();
    public final EssentiaRequestSet incomingRequests = new EssentiaRequestSet();

    private WorldCoordinate coordinate;

    public TileEntityIOPipeSegment() {
        super();
    }

    public void markDirty(boolean internal) {
        super.markDirty();
        if (!internal) {
            BlockPipeSegment.verifyIOState(worldObj, xCoord, yCoord, zCoord);
        }
    }

    private void applySuctions() {
        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            final var outgoingRequest = getRequestFor(dir);
            if (outgoingRequest == null) {
                continue;
            }
            outgoingRequest.distance = 1;
            if (this.evaluateEssentiaRequest(outgoingRequest)) {
                continue;
            }
            for (var conn : connections) {
                final var ioSegment = conn.getIOSegment();
                if (ioSegment == null) {
                    continue;
                }
                outgoingRequest.distance = conn.distance() + 1;
                final var requestedAccepted = ioSegment.evaluateEssentiaRequest(outgoingRequest);
                if (requestedAccepted) {
                    break;
                }
            }
        }
    }

    private @Nullable EssentiaRequest getRequestFor(ForgeDirection dir) {
        final var here = this.getCoordinate();
        final var destFace = dir.getOpposite();
        final var transport = getEssentiaAcceptor(dir);
        if (transport == null) {
            return null;
        }
        final var suctionAmount = transport.getSuctionAmount(destFace);
        if (suctionAmount < 1) {
            return null;
        }
        return new EssentiaRequest(here, dir, transport.getSuctionType(destFace), suctionAmount);
    }

    private void distributeEssentia() {
        final var amountToTake = 1;

        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            final var savedRequest = incomingRequests.getRequest(dir);
            if (savedRequest == null) {
                continue;
            }

            final var source = getEssentiaProvider(dir);
            if (source == null) {
                continue;
            }

            final var requestor = PipeHelper.getIOSegment(savedRequest.requestor);
            if (requestor == null) {
                continue;
            }

            final var takeFromFace = dir.getOpposite();
            final var takenAmount = source.takeEssentia(savedRequest.aspect, amountToTake, takeFromFace);
            if (takenAmount < 1) {
                continue;
            }

            final var requestFulfilled = requestor.acceptRequestFulfillment(savedRequest, amountToTake);

            if (!requestFulfilled) {
                source.addEssentia(savedRequest.aspect, 1, takeFromFace);
            }
        }
    }

    private void cleanupRequests() {
        incomingRequests.clear();
    }

    /**
     * Check adjacent blocks for non-pipe essentia transports
     */
    private IEssentiaTransport getEssentiaAcceptor(ForgeDirection dir) {
        final var here = this.getCoordinate();
        final var there = here.shift(dir);
        final var transport = there.getTileEntity(IEssentiaTransport.class);
        if (transport == null || transport instanceof IIOPipeSegment) {
            return null;
        }
        final var canAccept = transport.canInputFrom(dir.getOpposite());
        return canAccept ? transport : null;

    }

    private IEssentiaTransport getEssentiaProvider(ForgeDirection dir) {
        final var here = this.getCoordinate();
        final var there = here.shift(dir);
        final var transport = there.getTileEntity(IEssentiaTransport.class);
        if (transport == null || transport instanceof IIOPipeSegment) {
            return null;
        }
        final var canProvide = transport.canOutputTo(dir.getOpposite());
        return canProvide ? transport : null;
    }

    ///
    /// Overrides
    ///

    @Override
    public void updateEntity() {
        final var step = (byte) (worldObj.getTotalWorldTime() % INTERVAL);
        switch (step) {
            case 0 -> applySuctions();
            case 1 -> distributeEssentia();
            case 2 -> cleanupRequests();
        }
    }

    @Override
    public void markDirty() {
        markDirty(false);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        coordinate = null;
        connections.readFromNBT(compound.getCompoundTag(CONNECTIONS));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        final var connectionsTag = new NBTTagCompound();
        connections.writeToNBT(connectionsTag);
        compound.setTag(CONNECTIONS, connectionsTag);
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
    /// IIOPipeSegment
    ///

    @Override
    public boolean acceptRequestFulfillment(EssentiaRequest fulfilledRequest, int amount) {
        final var here = getCoordinate();
        final var there = here.shift(fulfilledRequest.willOutputTo);
        final var transport = there.getTileEntity(IEssentiaTransport.class);
        if (transport == null) {
            return false;
        }
        final var destinationFace = fulfilledRequest.willOutputTo.getOpposite();
        final var amountRemaining = transport.addEssentia(fulfilledRequest.aspect, 1, destinationFace);
        return amountRemaining > 0;
    }

    @Override
    public boolean evaluateEssentiaRequest(EssentiaRequest incomingRequest) {
        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            final var sourceFace = dir.getOpposite();
            final var transport = getEssentiaProvider(dir);

            // Can the transport output in our direction?

            if (transport == null || !transport.canOutputTo(sourceFace)) {
                continue;
            }
            final var competingSuction = transport.getMinimumSuction();
            if (competingSuction >= incomingRequest.effectiveSuction()) {
                continue;
            }

            // Can the transport provide the correct essentia?

            if (transport.getEssentiaAmount(sourceFace) <= 0) {
                // It contains nothing
                continue;
            }

            final var requestedAspect = incomingRequest.aspect;
            if (transport instanceof IAspectContainer container) {
                if (requestedAspect != null && !container.doesContainerContainAmount(requestedAspect, 1)) {
                    // It doesn't contain what's requested
                    continue;
                }
            } else {
                if (requestedAspect != null && transport.getEssentiaType(sourceFace) != requestedAspect) {
                    // It doesn't contain what's requested
                    continue;
                }
            }

            // We *could* potentially fulfill the request at this point
            // Is it the best request we've gotten so far?

            final var savedRequest = incomingRequests.getRequest(dir);
            final var isBestRequest = savedRequest == null || savedRequest.isLowerPriorityThan(incomingRequest);

            if (isBestRequest) {
                // The request is currently the best contender to be fulfilled
                incomingRequests.setRequest(dir, incomingRequest);
            }
            return true;
        }
        return false;
    }

    @Override
    public WorldCoordinate getCoordinate() {
        if (coordinate == null) {
            coordinate = new WorldCoordinate(this.worldObj.provider.dimensionId, xCoord, yCoord, zCoord);
        }
        return coordinate;
    }

    @Override
    public void rebuildIOConnections() {
        this.connections.clear();
        PipeHelper.findIOSegmentsInNetwork(worldObj, PipeHelper.SearchType.BreadthFirst, getCoordinate())
            .stream()
            .filter(io -> !(io.x() == xCoord && io.y() == yCoord && io.z() == zCoord))
            .map(
                io -> new ConnectionInfo(
                    new WorldCoordinate(worldObj.provider.dimensionId, io.x(), io.y(), io.z()),
                    io.distance()))
            .forEach(connections::add);
        markDirty(true);
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
