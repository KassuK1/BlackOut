package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.globalsettings.*;
import kassuk.addon.blackout.modules.KassuKAura;
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

    private static final FacingSettings facing = Modules.get().get(FacingSettings.class);
    private static final RangeSettings range = Modules.get().get(RangeSettings.class);
    private static final RaytraceSettings raytrace = Modules.get().get(RaytraceSettings.class);
    private static final RotationSettings rotation = Modules.get().get(RotationSettings.class);
    private static final SwingSettings swing = Modules.get().get(SwingSettings.class);

    //  Range
    public static double getPlaceRange() {
        return range.placeRange.get();
    }
    public static double getPlaceWallsRange() {
        return range.placeRangeWalls.get();
    }
    public static double getAttackRange() {
        return range.attackRange.get();
    }
    public static double getAttackWallsRange() {
        return range.attackRangeWalls.get();
    }
    public static double getMineRange() {
        return range.miningRange.get();
    }
    public static double getMineWallsRange() {
        return range.miningRangeWalls.get();
    }
    public static double placeRangeTo(BlockPos pos) {return range.placeRangeTo(pos);}
    public static boolean inPlaceRange(BlockPos pos) {
        return range.inPlaceRange(pos);
    }
    public static boolean inPlaceRangeNoTrace(BlockPos pos) {
        return range.inPlaceRangeNoTrace(pos);
    }
    public static boolean inAttackRange(Box bb, double eyeHeight) {
        return range.inAttackRange(bb, eyeHeight);
    }
    public static boolean inAttackRange(Box bb, double eyeHeight, Vec3d feet) {
        return range.inAttackRange(bb, eyeHeight, feet);
    }
    public static double mineRangeTo(BlockPos pos) {return range.miningRangeTo(pos);}
    public static boolean inMineRange(BlockPos pos) {
        return range.inMineRange(pos);
    }
    public static boolean inMineRangeNoTrace(BlockPos pos) {
        return range.inMineRangeNoTrace(pos);
    }
    public static boolean inAttackRangeNoTrace(Box bb, double eyeHeight, Vec3d feet) {
        return range.inAttackRangeNoTrace(bb, eyeHeight, feet);
    }


    //  Rotate
    public static boolean startMineRot() {
        return rotation.startMineRot();
    }
    public static boolean endMineRot() {
        return rotation.endMineRot();
    }
    public static boolean shouldRotate(RotationType type) {
        return rotation.shouldRotate(type);
    }
    public static boolean rotationCheck(Vec3d pPos, double yaw, double pitch, Box box) {
        return rotation.rotationCheck(pPos, yaw, pitch, box);
    }
    public static boolean rotationCheck(double yaw, double pitch, Box box) {
        return rotation.rotationCheck(yaw, pitch, box);
    }

    //  Swing
    public static void swing(SwingState state, SwingType type) {
        swing.swing(state, type);
    }

    //  Facing
    public static Direction[] getPlaceDirection(BlockPos pos) {
        return facing.getDirection(pos);
    }
    public static Direction[] getPlaceDirection(BlockPos pos, Predicate<BlockState> predicate) {
        return facing.getDirection(pos, predicate);
    }
    public static Direction getPlaceOnDirection(BlockPos pos) {
        return facing.getPlaceOnDirection(pos);
    }

    //  Raytracing
    public static boolean shouldPlaceTrace() {
        return raytrace.placeTrace.get();
    }
    public static boolean shouldAttackTrace() {return raytrace.attackTrace.get();}
    public static boolean placeTrace(BlockPos pos) {
        return shouldPlaceTrace() && raytrace.placeTrace(pos);
    }
    public static boolean attackTrace(Box bb) {
        return shouldPlaceTrace() && raytrace.attackTrace(bb);
    }
}
