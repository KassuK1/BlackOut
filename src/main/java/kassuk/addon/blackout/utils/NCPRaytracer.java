package kassuk.addon.blackout.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class NCPRaytracer {
    public static boolean raytrace(Vec3d from, Vec3d to, Box box) {
        int lx = 0, ly = 0, lz = 0;

        for (float i = 0; i < 1; i += 0.001) {
            double x = lerp(from.x, to.x, i);
            double y = lerp(from.y, to.y, i);
            double z = lerp(from.z, to.z, i);

            if (box.contains(x, y, z)) return true;

            int ix = (int) Math.floor(x);
            int iy = (int) Math.floor(y);
            int iz = (int) Math.floor(z);

            if (lx != ix ||
                ly != iy ||
                lz != iz) {

                BlockPos pos = new BlockPos(ix, iy, iz);

                if (validForCheck(pos, mc.world.getBlockState(pos))) return false;
            }

            lx = ix;
            ly = iy;
            lz = iz;
        }
        return false;
    }

    private static double lerp(double from, double to, double delta) {
        return from + (to - from) * delta;
    }

    public static boolean validForCheck(BlockPos pos, BlockState state) {
        if (state.isSolid()) return true;
        if (state.getBlock() instanceof FluidBlock) return false;
        if (state.getBlock() instanceof StairsBlock) return false;
        if (state.hasBlockEntity()) return false;

        return state.isFullCube(mc.world, pos);
    }
}
