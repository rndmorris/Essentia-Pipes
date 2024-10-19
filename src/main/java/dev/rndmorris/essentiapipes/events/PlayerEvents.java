package dev.rndmorris.essentiapipes.events;

import dev.rndmorris.essentiapipes.EssentiaPipes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

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

    @SubscribeEvent
    public void onInteractEvent(PlayerInteractEvent event) {
        if (itemEssence == null) {
            itemEssence = (ItemEssence) ItemApi.getItem("itemEssence", 0)
                .getItem();
        }

        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (isPhial(event.entityPlayer.getHeldItem())) {
            phialDisplayInteractions(event);
        }
    }

    private boolean isPhial(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        final var damage = stack.getItemDamage();
        return stack.getItem() == itemEssence && (damage == 0 || damage == 1);
    }

    private void phialDisplayInteractions(PlayerInteractEvent event) {
        if (event.entityPlayer.isSneaking()) {
            placePhial(event);
        } else {
            addEssenitaFromPhial(event);
        }
    }

    private void placePhial(PlayerInteractEvent event) {
        final var block = BlockPhialDisplay.getInstance();
        final var player = event.entityPlayer;
        final var world = event.world;
        final var face = ForgeDirection.getOrientation(event.face);

        if (face == ForgeDirection.UNKNOWN) {
            return;
        }

        var placed = false;
        var x = event.x;
        var y = event.y;
        var z = event.z;
        var metadata = world.getBlockMetadata(x, y, z);

        if (world.getBlock(x, y, z) == block && metadata < BlockPhialDisplay.MAX_METADATA) {
            // add a phial to an existing block
            world.setBlockMetadataWithNotify(x, y, z, metadata + 1, 1 & 2);
            world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
            placed = true;
        }
        else {
            x = x + face.offsetX;
            y = y + face.offsetY;
            z = z + face.offsetZ;
            if (block.canPlaceBlockAt(world, x, y, z)) {
                // place a new block
                world.setBlock(x, y, z, block, 1, 1 & 2);
                world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
                placed = true;
            }
        }

        if (!placed) {
            return;
        }

        var tileEntity = (TileEntityPhialDisplay) world.getTileEntity(x, y, z);
        tileEntity.addEssentia(player, player.getHeldItem());

        if (world.isRemote) {
            player.swingItem();
        }
    }

    private void addEssenitaFromPhial(PlayerInteractEvent event) {
        int x = event.x, y = event.y, z = event.z;
        final var tileEntity = event.world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityPhialDisplay display) {
            display.addEssentia(event.entityPlayer, event.entityPlayer.getHeldItem());
        }
    }
}
