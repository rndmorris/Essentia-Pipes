package dev.rndmorris.essentiapipes.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.rndmorris.essentiapipes.EssentiaPipes;
import dev.rndmorris.essentiapipes.blocks.BlockPhialDisplay;
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
        final var player = event.entityPlayer;
        if (player.isSneaking() && isPhial(player.getHeldItem())) {
            handlePhialDisplay(event);
        }
    }

    private boolean isPhial(ItemStack stack) {
        if (itemEssence == null) {
            itemEssence = ItemApi.getItem("itemEssence", 0).getItem();
        }
        final var damage = stack.getItemDamage();
        return stack.getItem() == itemEssence && (damage == 0 || damage == 1);
    }

    private void handlePhialDisplay(PlayerInteractEvent event) {
        final var player = event.entityPlayer;
        final var itemStack = player.getHeldItem();
        final var world = event.world;
        final var face = ForgeDirection.getOrientation(event.face);
        if (face == ForgeDirection.UNKNOWN) {
            return;
        }
        final int x = event.x + face.offsetX,
            y = event.y + face.offsetY,
            z = event.z + face.offsetZ;
        if (!block().canPlaceBlockAt(event.world, x, y, z)) {
            return;
        }
        if (world.isRemote) {
            player.swingItem();
        }
        // create a new display
        world.setBlock(x, y, z, block());

        if (player.capabilities.isCreativeMode) {
            return;
        }

        itemStack.stackSize -= 1;
        player.inventoryContainer.detectAndSendChanges();
    }
}
