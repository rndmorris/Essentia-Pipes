package dev.rndmorris.pressurizedessentia.tile;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.api.EssentiaRequest;
import dev.rndmorris.pressurizedessentia.api.IIOPipeSegment;
import dev.rndmorris.pressurizedessentia.api.PipeHelper;
import dev.rndmorris.pressurizedessentia.api.WorldCoordinate;
import dev.rndmorris.pressurizedessentia.blocks.BlockPipeSegment;
import dev.rndmorris.pressurizedessentia.data.ConnectionSet;
import dev.rndmorris.pressurizedessentia.data.EssentiaRequestSet;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;

public class TileEntityIOPipeSegment extends TileThaumcraft implements IIOPipeSegment {

    public static final String CONNECTIONS = "connections";
    public static final String ID = PressurizedEssentia.modid("IOPipeSegment");
    public static final byte CYCLE_LENGTH = 20;
    public static final String REQUESTS = "requests";
    public static final byte TRANSFER_RATE = 4;

    public final ConnectionSet connections = new ConnectionSet();
    public final EssentiaRequestSet incomingRequests = new EssentiaRequestSet();

    private int rescanTickOffset = -1;
    private int requestTickOffset = -1;
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

            if (destination.getSuctionAmount(request.destinationFace) <= 0
                || destination.getSuctionType(request.destinationFace) != request.aspect) {
                continue;
            }

            // Some containers (e.g. jars) don't like giving essentia when aspect is null
            final var takeAspect = request.aspect != null ? request.aspect : pickAspectToTake(source, takeFromFace);
            final var takeAmount = calculateAmountToTake(source, takeFromFace, takeAspect);
            final var amountTaken = source.takeEssentia(takeAspect, takeAmount, takeFromFace);
            final var amountAdded = destination.addEssentia(takeAspect, amountTaken, request.destinationFace);

            final var leftovers = amountTaken - amountAdded;
            if (leftovers > 0) {
                // return any leftovers
                source.addEssentia(takeAspect, leftovers, takeFromFace);
            }
            incomingRequests.setRequest(dir, null);
            markDirty(true);
        }

        incomingRequests.clear();
        markDirty(true);
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

    private int calculateAmountToTake(IEssentiaTransport source, ForgeDirection takeFromFace, Aspect aspect) {
        if (source instanceof IAspectContainer container) {
            final var containedAmount = container.getAspects().getAmount(aspect);
            return Integer.min(TRANSFER_RATE, containedAmount);
        }
        final var contained = source.getEssentiaAmount(takeFromFace);
        return Integer.min(TRANSFER_RATE, contained);
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
        if (rescanTickOffset < 0) {
            rescanTickOffset = worldObj.rand.nextInt(10);
        }
        if (requestTickOffset < 0) {
            requestTickOffset = worldObj.rand.nextInt(5);
        }
        final var step = (byte) (worldObj.getTotalWorldTime() % CYCLE_LENGTH);

        // rescan valid connections (because sometimes things seem to break)
        if (step < 10 && step % 10 == rescanTickOffset) {
            rebuildIOConnections();
            return;
        }
        // send requests (spread out across multiple ticks so they aren't all in syn
        if (step < 15 && step % 5 == requestTickOffset) {
            sendEssentiaRequests();
            return;
        }
        if (step % 5 == requestTickOffset) {
            distributeEssentia();
            return;
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
    public boolean isConnectable(ForgeDirection dir) {
        return true;
    }

    @Override
    public boolean canInputFrom(ForgeDirection dir) {
        return true;
    }

    @Override
    public boolean canOutputTo(ForgeDirection dir) {
        return true;
    }

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
        return false;
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
        final var here = getCoordinate();
        final var foundConnections = PipeHelper.findIOPipeSegments(PipeHelper.SearchType.BreadthFirst, here);

        connections.clear();
        for (var ci : foundConnections) {
            if (here.equals(ci.coordinate())) {
                continue;
            }
            connections.add(ci);
        }
        markDirty(true);
    }
}
