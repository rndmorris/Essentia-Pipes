package dev.rndmorris.essentiapipes.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.rndmorris.essentiapipes.EssentiaPipes;
import dev.rndmorris.essentiapipes.blocks.BlockPhialDisplay;
import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.api.ItemApi;

public class PlayerEvents {

    private static PlayerEvents instance;


    public static void preInit() {
        instance = new PlayerEvents();
        MinecraftForge.EVENT_BUS.register(instance);
    }

    public static PlayerEvents getInstance() {
        return instance;
    }

    private BlockPhialDisplay block;
    private Item itemEssence;

    private BlockPhialDisplay block() {
        if (block == null) {
            block = BlockPhialDisplay.getInstance();
        }
        return block;
    }

    @SubscribeEvent
    public void onInteractEvent(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (isPhial(event.entityPlayer.getHeldItem())) {
            phialDisplayInteractions(event);
        }
    }

    private boolean isPhial(ItemStack stack) {
        if (itemEssence == null) {
            itemEssence = ItemApi.getItem("itemEssence", 0).getItem();
        }
        if (stack == null) {
            return false;
        }
        final var damage = stack.getItemDamage();
        return stack.getItem() == itemEssence && (damage == 0 || damage == 1);
    }

    private void phialDisplayInteractions(PlayerInteractEvent event) {
        if (event.entityPlayer.isSneaking()) {
            placePhial(event);
        }
        else {
            addEssenitaFromPhial(event);
        }
    }

    private void placePhial(PlayerInteractEvent event) {
        final var player = event.entityPlayer;
        final var world = event.world;
        final var face = ForgeDirection.getOrientation(event.face);

        if (face == ForgeDirection.UNKNOWN) {
            return;
        }
        int x = event.x,
            y = event.y,
            z = event.z;

        if (!(world.getBlock(x, y, z) == block() && world.getTileEntity(x, y, z) instanceof TileEntityPhialDisplay display && display.remainingPhialCapacity() > 0)) {
            x = event.x + face.offsetX;
            y = event.y + face.offsetY;
            z = event.z + face.offsetZ;
            if (!block().canPlaceBlockAt(world, x, y, z)) {
                return;
            }
            world.setBlock(x, y, z, block());
        }

        if (world.isRemote) {
            player.swingItem();
            return;
        }

        final var tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityPhialDisplay display) {
            display.addPhial(player, player.getHeldItem());
        }
    }

    private void addEssenitaFromPhial(PlayerInteractEvent event) {
        int x = event.x,
            y = event.y,
            z = event.z;
        final var tileEntity = event.world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityPhialDisplay display) {
            display.addEssentia(event.entityPlayer, event.entityPlayer.getHeldItem());
        }
    }
}
