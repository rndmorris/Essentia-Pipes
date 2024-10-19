package dev.rndmorris.essentiapipes.tile;

import dev.rndmorris.essentiapipes.EssentiaPipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.essentiapipes.data.StoragePhialSet;
import thaumcraft.api.ItemApi;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.items.ItemEssence;

public class TileEntityPhialDisplay extends TileThaumcraft implements IAspectContainer, IEssentiaTransport {

    public static final ForgeDirection ACCESS_FROM = ForgeDirection.UP;
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

    public TileEntityPhialDisplay() {

    }

    public StoragePhialSet getPhials() {
        return phials;
    }

    public byte remainingPhialCapacity() {
        return (byte) phials.remainingPhialCapacity();
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

    public void addEssentia(EntityPlayer player, ItemStack itemStack) {
        if (notPhial(itemStack)) {
            return;
        }
        final var itemAspects = itemEssence().getAspects(itemStack);
        if (itemAspects == null) {
            return;
        }
        final var aspectEntry = itemAspects.aspects.entrySet()
            .iterator()
            .next();
        final var addAspect = aspectEntry.getKey();
        final var addAmount = aspectEntry.getValue();

        final var amountAdded = addEssentia(addAspect, addAmount, ACCESS_FROM);
        // to-do: handle leftovers

        if (!player.capabilities.isCreativeMode && amountAdded > 0) {
            itemStack.stackSize--;
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    public int getLightValue() {
        return (int) (15 * ((float) phials.totalAmountStored() / (MAX_PHIALS * 8F)));
    }

    //
    // Overrides
    //

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        phials.readFromNBT(nbttagcompound);
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        phials.expandTo(getBlockMetadata());
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        phials.writeToNBT(nbttagcompound);
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
        return face == ACCESS_FROM;
    }

    @Override
    public boolean canInputFrom(ForgeDirection face) {
        return face == ACCESS_FROM;
    }

    @Override
    public boolean canOutputTo(ForgeDirection face) {
        return face == ACCESS_FROM;
    }

    @Override
    public void setSuction(Aspect var1, int var2) {}

    @Override
    public Aspect getSuctionType(ForgeDirection var1) {
        return null;
    }

    @Override
    public int getSuctionAmount(ForgeDirection var1) {
        return phials.allPhialsFull() ? 0 : 1;
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
        return phials.randomStoredAspect(worldObj.rand);
    }

    @Override
    public int getEssentiaAmount(ForgeDirection face) {
        return face == ACCESS_FROM ? phials.totalAmountStored() : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 1;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }
}
