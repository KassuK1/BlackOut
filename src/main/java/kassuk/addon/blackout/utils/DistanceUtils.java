package kassuk.addon.blackout.utils;

import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.util.math.Vec3d;

public class DistanceUtils {
    public static double distance(Vec3d v1, Vec3d v2) {
        return Math.sqrt(PlayerUtils.squaredDistance(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z));
    }

    public static Vec3d getClosest(Vec3d pPos, Vec3d middle, double width, double height) {
        return new Vec3d(Math.min(Math.max(pPos.x, middle.x - width / 2), middle.x + width / 2), Math.min(Math.max(pPos.y, middle.y), middle.y + height), Math.min(Math.max(pPos.z, middle.z - width / 2), middle.z + width / 2));
    }

    public static int closerToZero(int x) {
        return (int) (x - Math.signum(x));
    }
}
