package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.globalsettings.FacingSettings;
import kassuk.addon.blackout.globalsettings.RangeSettings;
import kassuk.addon.blackout.globalsettings.RotationSettings;
import kassuk.addon.blackout.globalsettings.SwingSettings;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public class SettingUtils extends Utils {

    //  Range
    public static double getPlaceRange() {
        return Modules.get().get(RangeSettings.class).placeRange.get();
    }
    public static double getAttackRange() {
        return Modules.get().get(RangeSettings.class).attackRange.get();
    }
    public static double placeRangeTo(BlockPos pos) {return Modules.get().get(RangeSettings.class).placeRangeTo(pos);}
    public static boolean inPlaceRange(BlockPos pos) {
        return Modules.get().get(RangeSettings.class).inPlaceRange(pos);
    }
    public static boolean inAttackRange(Box bb, double eyeHeight) {
        return Modules.get().get(RangeSettings.class).inAttackRange(bb, eyeHeight);
    }
    public static boolean inAttackRange(Box bb, double eyeHeight, Vec3d feet) {
        return Modules.get().get(RangeSettings.class).inAttackRange(bb, eyeHeight, feet);
    }

    //  Rotate
    public static boolean rotationCheck(Vec3d pPos, double yaw, double pitch, Box box) {
        return Modules.get().get(RotationSettings.class).rotationCheck(pPos, yaw, pitch, box);
    }
    public static boolean rotationCheck(double yaw, double pitch, Box box) {
        return Modules.get().get(RotationSettings.class).rotationCheck(yaw, pitch, box);
    }

    //  Swing
    public static void swing(SwingSettings.SwingState state, SwingSettings.SwingType type) {
        Modules.get().get(SwingSettings.class).swing(state, type);
    }

    //  Facing
    public static Direction[] getPlaceDirection(BlockPos pos) {
        return Modules.get().get(FacingSettings.class).getDirection(pos);
    }
    public static Direction[] getPlaceDirection(BlockPos pos, Predicate<BlockState> predicate) {
        return Modules.get().get(FacingSettings.class).getDirection(pos, predicate);
    }
    public static Direction getPlaceOnDirection(BlockPos pos) {
        return Modules.get().get(FacingSettings.class).getPlaceOnDirection(pos);
    }
}
