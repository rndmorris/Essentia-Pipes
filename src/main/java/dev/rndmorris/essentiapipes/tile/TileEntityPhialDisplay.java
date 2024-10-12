package dev.rndmorris.essentiapipes.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.ItemApi;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.items.ItemEssence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityPhialDisplay extends TileThaumcraft implements IAspectContainer, IEssentiaTransport {

    public static final String ID = "PhialDisplay";
    public static final ForgeDirection ACCESS_FROM = ForgeDirection.UP;
    public static final byte MAX_ASPECTS = 4;
    public static final byte MAX_PHIALS = 4;
    public static final byte MAX_VIS = 32;

    public static final String PHIAL_COUNT = "phialCount";

    private static ItemEssence itemEssence;

    private static ItemEssence itemEssence() {
        if (itemEssence == null) {
            itemEssence = (ItemEssence) ItemApi.getItem("itemEssence", 0).getItem();
        }
        return itemEssence;
    }

    private AspectList storedAspects = new AspectList();
    private byte phialCount = 0;

    public TileEntityPhialDisplay() {

    }

    public byte remainingPhialCapacity() {
        return (byte)(MAX_PHIALS - phialCount);
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

    public void addPhial(EntityPlayer player, ItemStack itemStack) {
        if (notPhial(itemStack) || phialCount >= MAX_PHIALS) {
            return;
        }

        final var itemAspects = itemEssence().getAspects(itemStack);
        if (itemAspects != null) {
            final var aspectEntry = itemAspects.aspects.entrySet().iterator().next();
            addEssentia(aspectEntry.getKey(), aspectEntry.getValue(), ACCESS_FROM);
        }

        phialCount += 1;
        if (!player.capabilities.isCreativeMode) {
            itemStack.stackSize--;
            player.inventoryContainer.detectAndSendChanges();
        }
        markDirty();
    }

    public void addEssentia(EntityPlayer player, ItemStack itemStack) {
        if (notPhial(itemStack)) {
            return;
        }
        final var itemAspects = itemEssence().getAspects(itemStack);
        if (itemAspects == null) {
            return;
        }
        final var aspectEntry = itemAspects.aspects.entrySet().iterator().next();
        final var addAspect = aspectEntry.getKey();
        final var addAmount = aspectEntry.getValue();

        final var amountAdded = addEssentia(addAspect, addAmount, ACCESS_FROM);
        // to-do: handle leftovers

        if (!player.capabilities.isCreativeMode && amountAdded > 0) {
            itemStack.stackSize--;
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    //
    // Overrides
    //

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        storedAspects.readFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey(PHIAL_COUNT)) {
            phialCount = nbttagcompound.getByte(PHIAL_COUNT);
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        storedAspects.writeToNBT(nbttagcompound);
        nbttagcompound.setByte(PHIAL_COUNT, phialCount);
    }

    //
    // IAspectContainer
    //

    @Override
    public AspectList getAspects() {
        return this.storedAspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
        this.storedAspects = aspects.copy();
    }

    @Override
    public boolean doesContainerAccept(Aspect var1) {
        return false;
    }

    @Override
    public int addToContainer(Aspect var1, int var2) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect var1, int var2) {
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
    public void setSuction(Aspect var1, int var2) {

    }

    @Override
    public Aspect getSuctionType(ForgeDirection var1) {
        return null;
    }

    @Override
    public int getSuctionAmount(ForgeDirection var1) {
        return storedAspects.visSize() < MAX_VIS ? 1 : 0;
    }

    @Override
    public int takeEssentia(Aspect var1, int var2, ForgeDirection var3) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect addAspect, int var2, ForgeDirection face) {
        if (face != ACCESS_FROM) {
            return 0;
        }
        return 0;
    }

    @Override
    public Aspect getEssentiaType(ForgeDirection face) {
        return face == ACCESS_FROM ? storedAspects.getAspects()[this.worldObj.rand.nextInt(storedAspects.getAspects().length)] : null;
    }

    @Override
    public int getEssentiaAmount(ForgeDirection face) {
        return face == ACCESS_FROM ? storedAspects.visSize() : 0;
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
