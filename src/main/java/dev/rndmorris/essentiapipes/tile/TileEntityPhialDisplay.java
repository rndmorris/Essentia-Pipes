package dev.rndmorris.essentiapipes.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.essentiapipes.data.StoragePhialSet;
import thaumcraft.api.ItemApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.items.ItemEssence;

public class TileEntityPhialDisplay extends TileThaumcraft implements IAspectContainer, IEssentiaTransport {

    public static final ForgeDirection ACCESS_FROM = ForgeDirection.UP;
    public static final String HAS_TUBE = "hasTube";
    public static final String ID = "PhialDisplay";
    public static final byte MAX_PHIALS = 4;

    private static ItemEssence itemEssence;

    private static ItemEssence itemEssence() {
        if (itemEssence == null) {
            itemEssence = (ItemEssence) ItemApi.getItem("itemEssence", 0)
                .getItem();
        }
        return itemEssence;
    }

    private final StoragePhialSet phials = new StoragePhialSet(MAX_PHIALS);

    private boolean hasTube = false;

    public TileEntityPhialDisplay() {

    }

    public boolean hasTube() {
        return hasTube;
    }

    public void hasTube(boolean value) {
        if (value != hasTube) {
            hasTube = value;
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public boolean canAddPhial() {
        return phials.canAddPhial();
    }

    public StoragePhialSet getPhialSet() {
        return phials;
    }

    private boolean notPhial(ItemStack itemStack) {
        if (itemStack == null) {
            return true;
        }
        if (itemStack.getItem() != itemEssence()) {
            return true;
        }
        final var dmg = itemStack.getItemDamage();
        return !(dmg == 0 || dmg == 1);
    }

    public boolean addPhial(ItemStack heldItem) {
        if (notPhial(heldItem) || !canAddPhial()) {
            return false;
        }
        final var aspects = itemEssence().getAspects(heldItem);
        var result = false;
        if (aspects != null && aspects.visSize() > 0) {
            final var kv = aspects.aspects.entrySet()
                .iterator()
                .next();
            result = phials.addPhial(kv.getKey(), kv.getValue());

        } else {
            result = phials.addPhial();
        }
        if (result) {
            markDirty();
        }
        return result;
    }

    public int getLightValue() {
        return (int) (15 * ((float) phials.totalAmountStored() / (MAX_PHIALS * 8F)));
    }

    private void fillPhials() {
        final var tile = ThaumcraftApiHelper.getConnectableTile(worldObj, xCoord, yCoord, zCoord, ForgeDirection.UP);
        final var takeFromFace = ACCESS_FROM.getOpposite();
        if (!(tile instanceof IEssentiaTransport takeFrom && takeFrom.canOutputTo(takeFromFace))) {
            return;
        }

        if (takeFrom.getEssentiaAmount(takeFromFace) < 1) {
            return;
        }
        if (takeFrom.getSuctionAmount(takeFromFace) >= getSuctionAmount(ACCESS_FROM)) {
            return;
        }
        if (takeFrom.getMinimumSuction() >= getSuctionAmount(ACCESS_FROM)) {
            return;
        }
        var takeAspect = takeFrom.getEssentiaType(takeFromFace);
        if (!phials.acceptsAspect(takeAspect)) {
            return;
        }

        this.addToContainer(takeAspect, takeFrom.takeEssentia(takeAspect, 1, takeFromFace));
    }

    //
    // Overrides
    //

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        phials.readFromNBT(nbttagcompound);
        hasTube = nbttagcompound.getBoolean(HAS_TUBE);
    }

    @Override
    public void updateEntity() {
        if (!worldObj.isRemote && worldObj.getWorldTime() % 5 == 0 && phials.anyPhialNotFull()) {
            fillPhials();
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        phials.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean(HAS_TUBE, hasTube);
    }

    //
    // IAspectContainer
    //

    @Override
    public AspectList getAspects() {
        return this.phials.toAspectList();
    }

    @Override
    public void setAspects(AspectList aspects) {}

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return phials.acceptsAspect(aspect);
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (amount == 0) {
            return amount;
        }
        final var leftover = phials.addEssentia(aspect, amount);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
        return leftover;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (phials.totalAmountStored(aspect) >= amount) {
            phials.takeEssentia(aspect, amount);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList var1) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect var1, int var2) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList var1) {
        return false;
    }

    @Override
    public int containerContains(Aspect var1) {
        return 0;
    }

    //
    // IEssentiaTransport
    //

    @Override
    public boolean isConnectable(ForgeDirection face) {
        return hasTube() && face == ACCESS_FROM;
    }

    @Override
    public boolean canInputFrom(ForgeDirection face) {
        return hasTube() && face == ACCESS_FROM;
    }

    @Override
    public boolean canOutputTo(ForgeDirection face) {
        return hasTube() && face == ACCESS_FROM;
    }

    @Override
    public void setSuction(Aspect var1, int var2) {}

    @Override
    public Aspect getSuctionType(ForgeDirection var1) {
        return null;
    }

    @Override
    public int getSuctionAmount(ForgeDirection var1) {
        return hasTube() && phials.anyPhialNotFull() ? 1 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, ForgeDirection face) {
        return canOutputTo(face) && takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, ForgeDirection face) {
        return canInputFrom(face) ? amount - addToContainer(aspect, amount) : 0;
    }

    @Override
    public Aspect getEssentiaType(ForgeDirection face) {
        return face == ForgeDirection.UNKNOWN ? phials.firstStoredAspect() : null;
    }

    @Override
    public int getEssentiaAmount(ForgeDirection face) {
        return phials.totalAmountStored();
    }

    @Override
    public int getMinimumSuction() {
        return 1;
    }

    @Override
    public boolean renderExtendedTube() {
        return hasTube();
    }
}
