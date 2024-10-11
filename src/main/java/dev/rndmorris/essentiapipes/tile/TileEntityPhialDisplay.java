package dev.rndmorris.essentiapipes.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.ItemApi;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.items.ItemEssence;

public class TileEntityPhialDisplay extends TileThaumcraft implements IAspectContainer {

    public static final String ID = "PhialDisplay";
    public static final int MAX_PHIALS = 4;

    private static ItemEssence itemEssence;

    private static ItemEssence itemEssence() {
        if (itemEssence == null) {
            itemEssence = (ItemEssence) ItemApi.getItem("itemEssence", 0).getItem();
        }
        return itemEssence;
    }

    private AspectList storedAspects = new AspectList();
    private int phialCount = 0;

    public TileEntityPhialDisplay() {

    }

    public boolean onBlockActivated(EntityPlayer player) {
        var itemStack = player.getHeldItem();
        if (!isPhial(itemStack)) {
            return false;
        }

        if (player.isSneaking()) {
            return addPhial(player, itemStack);
        }
        return false;
//        else {
//            return addEssentiaFromPhial(player);
//        }
    }

    private boolean isPhial(ItemStack itemStack) {
        final var dmg = itemStack.getItemDamage();
        return itemStack.getItem() == itemEssence() && (dmg == 0 || dmg == 1);
    }

    private boolean addPhial(EntityPlayer player, ItemStack itemStack) {
        if (phialCount >= MAX_PHIALS || itemStack.getItemDamage() != 0) {
            return false;
        }
        if (worldObj.isRemote) {
            player.swingItem();
            return false;
        }

        phialCount += 1;
        itemStack.stackSize--;
        player.inventoryContainer.detectAndSendChanges();

        return true;
    }

    //
    // Overrides
    //

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        storedAspects.writeToNBT(nbttagcompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        storedAspects.writeToNBT(nbttagcompound);
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

}
