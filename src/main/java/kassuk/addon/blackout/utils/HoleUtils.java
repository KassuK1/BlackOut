package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.enums.HoleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * @author OLEPOSSU
 */

public class HoleUtils {

    public static Hole getHole(BlockPos pos) {
        return getHole(pos, true, true, true, 3, true);
    }

    public static Hole getHole(BlockPos pos, int depth) {
        return getHole(pos, depth, true);
    }

    public static Hole getHole(BlockPos pos, int depth, boolean floor) {
        return getHole(pos, true, true, true, depth, floor);
    }

    public static Hole getHole(BlockPos pos, boolean s, boolean d, boolean q, int depth, boolean floor) {
        if (!isHole(pos, depth, floor)) {
            return new Hole(pos, HoleType.NotHole);
        }

        if (!isBlock(pos.west()) || !isBlock(pos.north())) {
            return new Hole(pos, HoleType.NotHole);
        }

        boolean x = isHole(pos.east(), depth, floor) && isBlock(pos.east().north()) && isBlock(pos.east(2));
        boolean z = isHole(pos.south(), depth, floor) && isBlock(pos.south().west()) && isBlock(pos.south(2));

        // Single
        if (s && !x && !z && isBlock(pos.east()) && isBlock(pos.south())) {
            return new Hole(pos, HoleType.Single);
        }

        // Quad
        if (q && x && z && isHole(pos.south().east(), depth, floor) && isBlock(pos.east().east().south()) && isBlock(pos.south().south().east())) {
            return new Hole(pos, HoleType.Quad);
        }

        if (!d) {
            return new Hole(pos, HoleType.NotHole);
        }

        // DoubleX
        if (x && !z && isBlock(pos.south()) && isBlock(pos.south().east())) {
            return new Hole(pos, HoleType.DoubleX);
        }

        // DoubleZ
        if (z && !x && isBlock(pos.east()) && isBlock(pos.south().east())) {
            return new Hole(pos, HoleType.DoubleZ);
        }


        return new Hole(pos, HoleType.NotHole);
    }

    static boolean isBlock(BlockPos pos) {
        return OLEPOSSUtils.collidable(pos);
    }

    static boolean isHole(BlockPos pos, int depth, boolean floor) {
        if (floor && !isBlock(pos.down())) {
            return false;
        }

        for (int i = 0; i < depth; i++) {
            if (isBlock(pos.up(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean inHole(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();

        if (getHole(pos, 1).type == HoleType.Single)
            return true;

        // DoubleX
        if (getHole(pos, 1).type == HoleType.DoubleX ||
            getHole(pos.add(-1, 0, 0), 1).type == HoleType.DoubleX) {
            return true;
        }

        // DoubleZ
        if (getHole(pos, 1).type == HoleType.DoubleZ ||
            getHole(pos.add(0, 0, -1), 1).type == HoleType.DoubleZ) {
            return true;
        }

        // Quad
        return getHole(pos, 1).type == HoleType.Quad ||
            getHole(pos.add(-1, 0, -1), 1).type == HoleType.Quad ||
            getHole(pos.add(-1, 0, 0), 1).type == HoleType.Quad ||
            getHole(pos.add(0, 0, -1), 1).type == HoleType.Quad;
    }
}
