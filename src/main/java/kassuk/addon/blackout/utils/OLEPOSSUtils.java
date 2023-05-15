package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.mixins.MixinBlockSettings;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 * @author OLEPOSSU
 */

public class OLEPOSSUtils {
    public static double distance(Vec3d v1, Vec3d v2) {
        double x = v1.x - v2.x;
        double y = v1.y - v2.y;
        double z = v1.z - v2.z;

        return Math.sqrt(x * x + y * y + z * z);
    }

    public static Direction[] horizontals = new Direction[]{
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    };

    public static Vec3d getMiddle(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static Vec3d getMiddle(Box box) {
        return new Vec3d((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2);
    }

    public static boolean inside(PlayerEntity en, Box bb) {
        if (mc.world == null) {
            return false;
        }
        return mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }

    public static Box getBox(BlockPos pos) {
        return new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    public static int closerToZero(int x) {
        return (int) (x - Math.signum(x));
    }

    public static Vec3d getClosest(Vec3d pPos, Vec3d middle, double width, double height) {
        return new Vec3d(Math.min(Math.max(pPos.x, middle.x - width / 2), middle.x + width / 2),
            Math.min(Math.max(pPos.y, middle.y), middle.y + height),
            Math.min(Math.max(pPos.z, middle.z - width / 2), middle.z + width / 2));
    }

    @SuppressWarnings({"DataFlowIssue", "BooleanMethodIsAlwaysInverted"})
    public static boolean strictDir(BlockPos pos, Direction dir) {
        return switch (dir) {
            case DOWN -> mc.player.getEyePos().y <= pos.getY() + 0.5;
            case UP -> mc.player.getEyePos().y >= pos.getY() + 0.5;
            case NORTH -> mc.player.getZ() < pos.getZ();
            case SOUTH -> mc.player.getZ() >= pos.getZ() + 1;
            case WEST -> mc.player.getX() < pos.getX();
            case EAST -> mc.player.getX() >= pos.getX() + 1;
        };
    }

    public static Box getCrystalBox(BlockPos pos) {
        return new Box(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5);
    }

    public static Box getCrystalBox(Vec3d pos) {
        return new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean replaceable(BlockPos block) {
        return ((MixinBlockSettings) AbstractBlock.Settings.copy(mc.world.getBlockState(block).getBlock())).getMaterial().isReplaceable();
    }
    public static boolean solid2(BlockPos block) {
        return ((MixinBlockSettings) AbstractBlock.Settings.copy(mc.world.getBlockState(block).getBlock())).getMaterial().isSolid();
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "DataFlowIssue"})
    public static boolean solid(BlockPos block) {
        Block b = mc.world.getBlockState(block).getBlock();
        return !(b instanceof AbstractFireBlock || b instanceof FluidBlock || b instanceof AirBlock);
    }
    public static BlockPos toPos(Vec3d vec) {
        return new BlockPos((int) Math.floor(vec.x), (int) Math.floor(vec.y), (int) Math.floor(vec.z));
    }

    public static boolean isGapple(Item item) {
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
    }
    public static boolean isGapple(ItemStack stack) {
        return stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
    }
    public static boolean collidable(BlockPos block) {
        return ((AbstractBlockAccessor) mc.world.getBlockState(block).getBlock()).isCollidable();
    }
}
