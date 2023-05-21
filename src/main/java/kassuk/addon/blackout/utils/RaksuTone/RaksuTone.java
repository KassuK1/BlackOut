package kassuk.addon.blackout.utils.RaksuTone;

import net.minecraft.util.math.BlockPos;

public class RaksuTone {
    public static RaksuPath getPath(int length, BlockPos target) {
        RaksuPath path = new RaksuPath();
        path.calculate(length, target, false);
        return path;
    }
    public static RaksuPath runAway(int length, BlockPos target) {
        RaksuPath path = new RaksuPath();
        path.calculate(length, target, true);
        return path;
    }
}
