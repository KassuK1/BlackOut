package kassuk.addon.blackout.utils.RaksuTone;

import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class RaksuPath {
    public final List<Movement> path = new ArrayList<>();

    public int step = 2;
    public int reverseStep = 3;
    public double speed = 0.2873;
    public int fallDist = 150;

    private List<Direction> dirs = null;

    public void calculate(int blocks, BlockPos target, boolean opposite) {
        BlockPos pos = mc.player.getBlockPos().toImmutable();

        if (!is(pos.down()) && !OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(0, -0.2, 0))) {
            for (int i = 0; i < fallDist; i++) {

                if (is(pos.down(i + 1))) {
                    path.add(new Movement(true, pos.down(i), MovementType.Fall));
                    break;
                }
            }
        }

        for (int i = 0; i < blocks; i++) {
            Movement m = nextPos(pos, target, true, opposite);

            if (m == null || !m.valid) {
                return;
            }

            if (pos.equals(m.pos)) {
                return;
            }

            pos = m.pos;
            path.add(m);
        }
    }

    private Movement nextPos(BlockPos pos, BlockPos target, boolean stuckCheck, boolean reversed) {
        closestDir(pos, target, reversed);

        for (Direction dir : dirs) {
            Movement m = getMovement(pos, dir);

            if (!m.valid()) {
                continue;
            }
            // Stuck check
            if (stuckCheck) {
                Movement m1 = nextPos(m.pos, target, false, reversed);
                if (m1 != null && m1.valid && m1.pos.equals(pos)) {
                    continue;
                }
            }
            return m;
        }
        return null;
    }

    private Movement getMovement(BlockPos pos, Direction dir) {
        if (canWalkTrough(pos, dir)) {

            if (is(pos.offset(dir).down())) {
                return new Movement(true, pos.offset(dir), MovementType.Move);
            } else {
                Movement m = getFall(pos, dir);

                if (m.valid) {
                    return m;
                }
            }
            return new Movement(false, null, null);
        }

        Movement m = getStep(pos, dir);

        if (m.valid) {
            return m;
        }
        return new Movement(false, null, null);
    }

    private Movement getStep(BlockPos pos, Direction dir) {
        for (int i = 1; i <= step; i++) {
            if (is(pos.up(i + 1))) {
                return new Movement(false, null, null);
            }
            if (!is(pos.offset(dir).up(i - 1))) {
                continue;
            }
            if (is(pos.offset(dir).up(i)) || is(pos.offset(dir).up(i + 1))) {
                continue;
            }

            return new Movement(true, pos.offset(dir).up(i), MovementType.Step);
        }
        return new Movement(false, null, null);
    }

    private Movement getFall(BlockPos pos, Direction dir) {
        for (int i = 0; i < fallDist; i++) {
            if (is(pos.offset(dir).down(i + 1))) {
                if (i < reverseStep) return new Movement(true, pos.offset(dir).down(i), MovementType.Reverse);
                else return new Movement(true, pos.offset(dir).down(i), MovementType.Fall);
            }
        }
        return new Movement(false, null, null);
    }

    private boolean canWalkTrough(BlockPos pos, Direction dir) {
        return !is(pos.offset(dir)) && !is(pos.offset(dir).up());
    }

    private void closestDir(BlockPos from, BlockPos target, boolean reversed) {
        if (reversed) {
            Comparator<Direction> c = Comparator.comparingDouble(i -> from.offset(i).toCenterPos().distanceTo(target.toCenterPos()));
            dirs = Arrays.stream(new Direction[]{
                Direction.EAST,
                Direction.WEST,
                Direction.NORTH,
                Direction.SOUTH
            }).sorted(c.reversed()).toList();
        } else {
            dirs = Arrays.stream(new Direction[]{
                Direction.EAST,
                Direction.WEST,
                Direction.NORTH,
                Direction.SOUTH
            }).sorted(Comparator.comparingDouble(i -> from.offset(i).toCenterPos().distanceTo(target.toCenterPos()))).toList();
        }
    }

    private boolean is(BlockPos pos) {
        return ((AbstractBlockAccessor) mc.world.getBlockState(pos).getBlock()).isCollidable();
    }

    public record Movement(boolean valid, BlockPos pos, MovementType type) {
    }

    public enum MovementType {
        Step,
        Reverse,
        Fall,
        Move
    }
}
