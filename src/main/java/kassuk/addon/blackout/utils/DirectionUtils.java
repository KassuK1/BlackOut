package kassuk.addon.blackout.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class DirectionUtils {

    public static Direction closestDir(BlockPos pos, Vec3d vec) {
        Direction closest = null;
        double closestDist = -1;
        for (Direction dir : Direction.values()) {
            double dist = DistanceUtils.distance(new Vec3d(pos.getX() + 0.5 + dir.getOffsetX() / 2f, pos.getY() + 0.5 + dir.getOffsetY() / 2f, pos.getZ() + 0.5 + dir.getOffsetZ() / 2f), vec);

            if (closest == null || dist < closestDist) {
                closest = dir;
                closestDist = dist;
            }
        }
        return closest;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean strictDir(BlockPos pos, Direction dir) {
        if (mc.player == null) {
            return false;
        }

        return switch (dir) {
            case DOWN -> mc.player.getEyePos().y <= pos.getY() + 0.5;
            case UP -> mc.player.getEyePos().y >= pos.getY() + 0.5;
            case NORTH -> mc.player.getZ() < pos.getZ();
            case SOUTH -> mc.player.getZ() >= pos.getZ() + 1;
            case WEST -> mc.player.getX() < pos.getX();
            case EAST -> mc.player.getX() >= pos.getX() + 1;
        };
    }

    public static Direction[] horizontals = new Direction[]{
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    };
}
