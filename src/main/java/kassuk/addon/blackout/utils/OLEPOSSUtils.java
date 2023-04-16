package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.mixins.MixinBlockSettings;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.block.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class OLEPOSSUtils {
    public static double distance(Vec3d v1, Vec3d v2) {
        return Math.sqrt(PlayerUtils.squaredDistance(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z));
    }
    public static Direction[] horizontals = new Direction[] {
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    };
    public static boolean isBedItem(Item item) {
        return item instanceof BedItem;
    }
    public static boolean isBedBlock(Block block) {
        return block instanceof BedBlock;
    }
    public static boolean isHelmet(Item item) {
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.HEAD;
    }
    public static boolean isChestPlate(Item item) {
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.CHEST;
    }
    public static boolean isLegging(Item item) {
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.LEGS;
    }
    public static boolean isBoot(Item item) {
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.FEET;
    }
    public static boolean isSword(Item item) {
        return item instanceof SwordItem;
    }
    public static boolean isArmor(Item item) {
        return item instanceof ArmorItem;
    }
    public static String armorCategory(Item item) {
        if (item instanceof ArmorItem armorItem) {
            return switch (armorItem.getSlotType()) {
                case FEET -> "boots";
                case LEGS -> "leggings";
                case CHEST -> "chestplate";
                case HEAD -> "head";
                default -> null;
            };
        } else return null;
    }
    public static boolean isAnvilBlock(Block block) {
        return block instanceof AnvilBlock;
    }

    public static Vec3d getMiddle(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }
    public static Vec3d getMiddle(Box box) {
        return new Vec3d((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2);
    }

    public static void sendBlockPos(BlockPos pos) {
        ChatUtils.sendMsg(Text.literal("x" + pos.getX() + "  y" + pos.getY() + "  z" + pos.getZ()));
    }
    public static boolean inside(PlayerEntity en, Box bb) {
        if (mc.world == null) {return false;}
        return mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }
    public static Box getBox(BlockPos pos) {
        return new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
    public static int closerToZero(int x) {
        return (int) (x - Math.signum(x));
    }
    public static Direction closestDir(BlockPos pos, Vec3d vec) {
        Direction closest = null;
        double closestDist = -1;
        for (Direction dir : Direction.values()) {
            double dist = distance(new Vec3d(pos.getX() + 0.5 + dir.getOffsetX() / 2f, pos.getY() + 0.5 + dir.getOffsetY() / 2f, pos.getZ() + 0.5 + dir.getOffsetZ() / 2f), vec);

            if (closest == null || dist < closestDist) {
                closest = dir;
                closestDist = dist;
            }
        }
        return closest;
    }

    public static Vec3d getClosest(Vec3d pPos, Vec3d middle, double width, double height) {
        return new Vec3d(Math.min(Math.max(pPos.x, middle.x - width / 2), middle.x + width / 2),
            Math.min(Math.max(pPos.y, middle.y), middle.y + height),
            Math.min(Math.max(pPos.z, middle.z - width / 2), middle.z + width / 2));
    }

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
    public static boolean isCrystalBlock(Block block) {
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK;
    }
    public static Box getCrystalBox(Vec3d pos) {
        return new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }
    public static boolean replaceable(BlockPos block) {
        return ((MixinBlockSettings) AbstractBlock.Settings.copy(mc.world.getBlockState(block).getBlock())).getMaterial().isReplaceable();
    }
    public static boolean solid(BlockPos block) {
        Block b = mc.world.getBlockState(block).getBlock();
        return !(b instanceof AbstractFireBlock || b instanceof FluidBlock || b instanceof AirBlock);
    }
    public static boolean solid(Block b) {
        return !(b instanceof AbstractFireBlock || b instanceof FluidBlock || b instanceof AirBlock);
    }
}
