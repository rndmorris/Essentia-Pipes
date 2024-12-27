package dev.rndmorris.essentiapipes.items;

import static dev.rndmorris.essentiapipes.EssentiaPipes.LOG;
import static dev.rndmorris.essentiapipes.EssentiaPipes.breakMouse;
import static dev.rndmorris.essentiapipes.blocks.BlockTinyJar.JarPositions;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.essentiapipes.api.WorldCoordinate;
import dev.rndmorris.essentiapipes.blocks.BlockTinyJar;

public class ItemBlockTinyJar extends ItemBlock {

    public ItemBlockTinyJar(Block p_i45328_1_) {
        super(p_i45328_1_);
        this.setMaxDamage(0);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {
        final var coord = new WorldCoordinate(world, x, y, z);

        final var block = coord.getBlock();
        if (block == null || !block.isReplaceable(coord.getWorld(), coord.x(), coord.y(), coord.z())) {
            return false;
        }

        final var targetJar = getClosestToHit(JarPositions.allPlaces(), x, y, z, hitX, hitY, hitZ);
        if (targetJar == null) {
            return false;
        }

        if (super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, targetJar.bitFlag)) {
            // to-do: update tile entity
            playPlaceSound(world, x, y, z);
            return true;
        }
        return false;
    }

    private boolean tryAddToJarBlock(ItemStack stack, EntityPlayer player, WorldCoordinate coord, float hitX,
        float hitY, float hitZ) {
        final var blockTinyJar = coord.getBlock(BlockTinyJar.class);
        if (blockTinyJar == null) {
            return false;
        }

        final var world = coord.getWorld();
        if (world == null) {
            return false;
        }
        final var placeJarAt = getClosestToHit(
            blockTinyJar.emptyJarSpaces(world, coord.x(), coord.y(), coord.z()),
            coord.x(),
            coord.y(),
            coord.z(),
            hitX,
            hitY,
            hitZ);
        if (placeJarAt == null) {
            return false;
        }
        var metadata = placeJarAt.setFlag(coord.getBlockMetadata());
        if (!coord.setBlockMetadataWithNotify(metadata, 3)) {
            return false;
        }
        // to-do: update tile entity
        playPlaceSound(world, coord.x(), coord.y(), coord.z());
        return true;
    }

    private JarPositions getClosestToHit(List<JarPositions> positions, int x, int y, int z, float hitX, float hitY,
        float hitZ) {
        JarPositions result = null;
        var nearest = Double.MAX_VALUE;
        for (var pos : positions) {
            final var center = pos.place.translate(x, y, z)
                .getCenter();
            final var dist = Math.pow(hitX - center[0], 2) + Math.pow(hitY - center[1], 2)
                + Math.pow(hitZ - center[2], 2);
            if (dist < nearest) {
                result = pos;
                nearest = dist;
            }
        }
        return result;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        breakMouse();
        LOG.info("{}, {}, {}, {}", hitX + x, hitY + y, hitZ + z, side);
        if (itemStack.stackSize == 0 || y >= 255 || !player.canPlayerEdit(x, y, z, side, itemStack)) {
            return false;
        }
        final var nearSide = ForgeDirection.getOrientation(side);

        final var here = new WorldCoordinate(world, x, y, z);
        final var there = here.shift(nearSide);

        return placeBlockAt(
            itemStack,
            player,
            world,
            there.x(),
            there.y(),
            there.z(),
            side,
            hitX,
            hitY,
            hitZ,
            itemStack.getItemDamage());
    }

    private void playPlaceSound(World world, int x, int y, int z) {
        world.playSoundEffect(
            .5F + x,
            .5F + y,
            .5F + z,
            this.field_150939_a.stepSound.func_150496_b(),
            (this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F,
            this.field_150939_a.stepSound.getPitch() * 0.8F);
    }
}
