package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.globalsettings.RangeSettings;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class SettingUtils extends Utils {

    //  Range
    public static double getPlaceRange() {
        return Modules.get().get(RangeSettings.class).placeRange.get();
    }
    public static double getAttackRange() {
        return Modules.get().get(RangeSettings.class).attackRange.get();
    }
    public static boolean inPlaceRange(BlockPos pos) {
        return Modules.get().get(RangeSettings.class).inPlaceRange(pos);
    }
    public static boolean inAttackRange(Box bb) {
        return Modules.get().get(RangeSettings.class).inAttackRange(bb);
    }
    public static boolean inAttackRange(Box bb, Vec3d feet) {
        return Modules.get().get(RangeSettings.class).inAttackRange(bb, feet);
    }
}
