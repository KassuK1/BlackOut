package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.enums.HoleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * @author OLEPOSSU
 */

public class Hole {
    public final BlockPos pos;
    public final HoleType type;
    public final BlockPos[] positions;
    public final Vec3d middle;

    public Hole(BlockPos pos, HoleType type) {
        this.pos = pos;
        this.type = type;
        switch (type) {
            case Single -> {
                this.positions = new BlockPos[]{pos};
                this.middle = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            }
            case DoubleX -> {
                this.positions = new BlockPos[]{pos, pos.add(1, 0, 0)};
                this.middle = new Vec3d(pos.getX() + 1, pos.getY(), pos.getZ() + 0.5);
            }
            case DoubleZ -> {
                this.positions = new BlockPos[]{pos, pos.add(0, 0, 1)};
                this.middle = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 1);
            }
            case Quad -> {
                this.positions = new BlockPos[]{pos, pos.add(1, 0, 0), pos.add(0, 0, 1), pos.add(1, 0, 1)};
                this.middle = new Vec3d(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
            }
            default -> {
                this.positions = new BlockPos[]{};
                this.middle = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            }
        }
    }

    public BlockPos[] positions() {
        return positions;
    }
}
