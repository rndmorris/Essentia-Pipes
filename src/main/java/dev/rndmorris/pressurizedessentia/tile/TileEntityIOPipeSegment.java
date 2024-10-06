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
    public static final byte INTERVAL = 5;
    public static final String REQUESTS = "requests";

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

    private void sendEssentiaRequests() {
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

                if (outgoingRequest.effectiveSuction() <= 0) {
                    // connections are ordered by ascending distance
                    // If our effective suction for one reaches 0 for one, it'll be 0 for everything after
                    break;
                }

                final var requestedAccepted = ioSegment.evaluateEssentiaRequest(outgoingRequest);
                if (requestedAccepted) {
                    break;
                }
            }
        }
    }

    private @Nullable EssentiaRequest getRequestFor(ForgeDirection dir) {
        final var destinationBlock = this.getCoordinate()
            .shift(dir);
        final var insertToFace = dir.getOpposite();
        final var transport = destinationBlock.getTileEntity(IEssentiaTransport.class);
        if (transport == null || transport instanceof IIOPipeSegment || !transport.canInputFrom(insertToFace)) {
            return null;
        }
        final var suctionAmount = transport.getSuctionAmount(insertToFace);
        if (suctionAmount < 1) {
            return null;
        }
        return new EssentiaRequest(
            destinationBlock,
            insertToFace,
            transport.getSuctionType(insertToFace),
            suctionAmount);
    }

    private void distributeEssentia() {
        final var amountToTake = 1;

        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            final var request = incomingRequests.getRequest(dir);
            if (request == null) {
                continue;
            }

            final var source = getEssentiaTransport(dir);
            final var takeFromFace = dir.getOpposite();
            if (source == null || !source.canOutputTo(takeFromFace)) {
                continue;
            }

            final var destination = request.destination.getTileEntity(IEssentiaTransport.class);
            if (destination == null || !destination.canInputFrom(request.destinationFace)) {
                continue;
            }

            // Some containers (e.g. jars) don't giving essentia when aspect is null
            final var takeAspect = request.aspect != null ? request.aspect : pickAspectToTake(source, takeFromFace);
            final var amountTaken = source.takeEssentia(takeAspect, amountToTake, takeFromFace);
            final var amountAdded = destination.addEssentia(takeAspect, amountTaken, request.destinationFace);

            final var leftovers = amountTaken - amountAdded;
            if (leftovers > 0) {
                // return any leftovers
                source.addEssentia(takeAspect, leftovers, takeFromFace);
            }
            markDirty(true);
        }

        incomingRequests.clear();
    }

    private Aspect pickAspectToTake(IEssentiaTransport source, ForgeDirection takeFromFace) {
        final var fromFace = source.getEssentiaType(takeFromFace);
        if (fromFace != null) {
            return fromFace;
        }
        if (source instanceof IAspectContainer container) {
            final var containedAspects = container.getAspects()
                .getAspects();
            return containedAspects[worldObj.rand.nextInt(containedAspects.length)];
        }
        return null;
    }

    private IEssentiaTransport getEssentiaTransport(ForgeDirection dir) {
        final var here = this.getCoordinate();
        final var there = here.shift(dir);
        final var transport = there.getTileEntity(IEssentiaTransport.class);
        if (transport == null || transport instanceof IIOPipeSegment) {
            return null;
        }
        return transport;
    }

    ///
    /// Overrides
    ///

    @Override
    public void updateEntity() {
        final var step = (byte) (worldObj.getTotalWorldTime() % INTERVAL);
        switch (step) {
            case 0 -> sendEssentiaRequests();
            case 1 -> distributeEssentia();
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
        incomingRequests.readFromNBT(compound.getCompoundTag(REQUESTS));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        final var connectionsTag = new NBTTagCompound();
        connections.writeToNBT(connectionsTag);
        compound.setTag(CONNECTIONS, connectionsTag);
        final var requestsTag = new NBTTagCompound();
        incomingRequests.writeToNBT(requestsTag);
        compound.setTag(REQUESTS, requestsTag);
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
    public boolean evaluateEssentiaRequest(EssentiaRequest incomingRequest) {
        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            final var sourceFace = dir.getOpposite();
            final var potentialSource = getEssentiaTransport(dir);

            // Can the source output in our direction?
            if (potentialSource == null || !potentialSource.canOutputTo(sourceFace)) {
                continue;
            }
            final var sourceCoords = WorldCoordinate.fromTileEntity(potentialSource);
            if (incomingRequest.destination.equals(sourceCoords)) {
                // no requesting from yourself
                continue;
            }
            if (potentialSource.getMinimumSuction() >= incomingRequest.effectiveSuction()) {
                continue;
            }

            // Can the transport provide the correct essentia?

            final var requestedAspect = incomingRequest.aspect;
            if (potentialSource.getEssentiaAmount(sourceFace) <= 0) {
                // It contains nothing
                continue;
            }

            if (potentialSource instanceof IAspectContainer container) {
                if (requestedAspect != null && !container.doesContainerContainAmount(requestedAspect, 1)) {
                    // It doesn't contain what's requested
                    continue;
                }
            } else {
                if (requestedAspect != null && potentialSource.getEssentiaType(sourceFace) != requestedAspect) {
                    // It doesn't contain what's requested
                    continue;
                }
            }

            // We *could* potentially fulfill the request at this point
            // Is it the best request we've gotten so far?

            final var savedRequest = incomingRequests.getRequest(dir);
            final var isBestRequest = savedRequest == null || savedRequest.isSuperceededBy(incomingRequest);

            if (isBestRequest) {
                // The request is currently the best contender to be fulfilled
                incomingRequests.setRequest(dir, incomingRequest);
                markDirty(true);
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
