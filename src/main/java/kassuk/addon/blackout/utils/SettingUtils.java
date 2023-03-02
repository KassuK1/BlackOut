package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.globalsettings.*;
import kassuk.addon.blackout.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;
import static meteordevelopment.meteorclient.MeteorClient.mc;

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
    public static boolean inAttackRange(Box bb) {
        return range.inAttackRange(bb);
    }
    public static boolean inAttackRange(Box bb, Vec3d feet) {return range.inAttackRange(bb, feet);}
    public static double mineRangeTo(BlockPos pos) {return range.miningRangeTo(pos);}
    public static boolean inMineRange(BlockPos pos) {
        return range.inMineRange(pos);
    }
    public static boolean inMineRangeNoTrace(BlockPos pos) {
        return range.inMineRangeNoTrace(pos);
    }
    public static boolean inAttackRangeNoTrace(Box bb, double eyeHeight, Vec3d feet) {return range.inAttackRangeNoTrace(bb, feet);}
    public static double attackRangeTo(Box bb, Vec3d feet) {return range.attackRangeTo(bb, feet);}


    //  Rotate
    public static int rotationPackets() {return rotation.packets.get();}
    public static boolean startMineRot() {
        return rotation.startMineRot();
    }
    public static boolean endMineRot() {
        return rotation.endMineRot();
    }
    public static boolean shouldRotate(RotationType type) {
        return rotation.shouldRotate(type);
    }
    public static boolean rotationCheckHistory(Box box, RotationType type) {return rotation.rotationCheckHistory(box, rotation.getExisted(type) + 1);}
    public static boolean rotationCheck(Vec3d pPos, double yaw, double pitch, Box box, RotationType type) {return rotation.rotationCheck(pPos, yaw, pitch, box, rotation.getExisted(type));}

    //  Swing
    public static void swing(SwingState state, SwingType type) {
        swing.swing(state, type);
    }
    public static void mineSwing(SwingSettings.MiningSwingState state) {
        swing.mineSwing(state);
    }

    //  Facing
    public static PlaceData getPlaceData(BlockPos pos) {
        return facing.getPlaceData(pos);
    }
    public static PlaceData getPlaceData(BlockPos pos, Predicate<BlockState> predicate) {return facing.getPlaceData(pos, predicate);}
    public static Direction getPlaceOnDirection(BlockPos pos) {
        return facing.getPlaceOnDirection(pos);
    }

    //  Raytracing
    public static boolean shouldPlaceTrace() {
        return raytrace.placeTrace.get();
    }
    public static boolean shouldAttackTrace() {return raytrace.attackTrace.get();}
    public static boolean placeTrace(BlockPos pos) {
        return !shouldPlaceTrace() && raytrace.placeTrace(pos);
    }
    public static boolean attackTrace(Box bb) {
        return !shouldAttackTrace() || raytrace.attackTrace(bb);
    }
}
