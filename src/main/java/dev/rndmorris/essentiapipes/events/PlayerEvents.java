package dev.rndmorris.essentiapipes.events;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.rndmorris.essentiapipes.blocks.BlockPhialDisplay;
import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;
import thaumcraft.api.ItemApi;
import thaumcraft.common.items.ItemEssence;

public class PlayerEvents {

    private static PlayerEvents instance;

    public static void preInit() {
        instance = new PlayerEvents();
        MinecraftForge.EVENT_BUS.register(instance);
    }

    public static PlayerEvents getInstance() {
        return instance;
    }

    private ItemEssence itemEssence;
    private ItemStack essentiaTube;

    @SubscribeEvent
    public void onInteractEvent(PlayerInteractEvent event) {
        if (itemEssence == null) {
            itemEssence = (ItemEssence) ItemApi.getItem("itemEssence", 0)
                .getItem();
        }
        if (essentiaTube == null) {
            essentiaTube = ItemApi.getBlock("blockTube", 0);
        }

        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final var stack = event.entityPlayer.getHeldItem();
        if (isPhial(stack)) {
            addPhialToPhialDisplay(event);
            return;
        }
        if (isTube(stack)) {
            addTubeToPhialDisplay(event);
            return;
        }
    }

    private boolean isPhial(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        final var damage = stack.getItemDamage();
        return stack.getItem() == itemEssence && (damage == 0 || damage == 1);
    }

    private boolean isTube(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getItem() == essentiaTube.getItem() && stack.getItemDamage() == essentiaTube.getItemDamage();
    }

    private void addPhialToPhialDisplay(PlayerInteractEvent event) {
        if (event.entityPlayer.isSneaking()) {
            placePhial(event);
        }
    }

    private void placePhial(PlayerInteractEvent event) {
        final var block = BlockPhialDisplay.getInstance();
        final var player = event.entityPlayer;
        final var heldItem = player.getHeldItem();
        final var world = event.world;
        final var face = ForgeDirection.getOrientation(event.face);

        if (face == ForgeDirection.UNKNOWN) {
            return;
        }

        var placed = false;
        var x = event.x;
        var y = event.y;
        var z = event.z;
        var tileEntity = world.getTileEntity(x, y, z) instanceof TileEntityPhialDisplay display ? display : null;
        if (world.getBlock(x, y, z) == block && tileEntity != null && tileEntity.canAddPhial()) {
            // we're directly targeting a phial display that can accept a new phial
            placed = tileEntity.addPhial(heldItem);
        } else {
            x = x + face.offsetX;
            y = y + face.offsetY;
            z = z + face.offsetZ;

            tileEntity = world.getTileEntity(x, y, z) instanceof TileEntityPhialDisplay display ? display : null;
            if (world.getBlock(x, y, z) == block && tileEntity != null && tileEntity.canAddPhial()) {
                // the block in front of what we're targeting is a phial display that can accept a new phial
                placed = tileEntity.addPhial(heldItem);
            }
            else {
                if (block.canPlaceBlockAt(world, x, y, z)) {
                    // the block in front of what we're targeting can be replaced with a new phial display
                    world.setBlock(x, y, z, block);
                    tileEntity = world.getTileEntity(x, y, z) instanceof TileEntityPhialDisplay display ? display : null;
                    if (tileEntity != null) {
                        placed = tileEntity.addPhial(heldItem);
                    }
                }
            }
        }

        if (!placed) {
            return;
        }

        if (!player.capabilities.isCreativeMode) {
            heldItem.stackSize -= 1;
            player.inventoryContainer.detectAndSendChanges();
        }

        if (world.isRemote) {
            world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
            player.swingItem();
        }
    }

    private void addTubeToPhialDisplay(PlayerInteractEvent event) {
        if (!event.entityPlayer.isSneaking()) {
            return;
        }
        int x = event.x, y = event.y, z = event.z;
        if (!(event.world.getTileEntity(x, y, z) instanceof TileEntityPhialDisplay display)) {
            return;
        }
        if (display.hasTube()) {
            return;
        }
        display.hasTube(true);
        event.useBlock = Event.Result.DENY;
        event.useItem = Event.Result.DENY;
        final var player = event.entityPlayer;
        if (!player.capabilities.isCreativeMode) {
            event.entityPlayer.getHeldItem().stackSize -= 1;
            event.entityPlayer.inventoryContainer.detectAndSendChanges();
        }
        final var world = event.world;
        if (world.isRemote) {
            world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
            player.swingItem();
        }
    }
}
