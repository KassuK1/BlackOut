package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.mixins.MixinDirection;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.PlaceData;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

/*
Made by OLEPOSSU
*/

public class FacingSettings extends BlackOutModule {
    public FacingSettings() {
        super(BlackOut.SETTINGS, "Facing", "Global facing settings for every blackout module");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //  Place Ranges
    public final Setting<Boolean> strictDir = sgGeneral.add(new BoolSetting.Builder()
        .name("Strict Direction")
        .description("Doesn't place on faces which aren't in your direction.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> unblocked = sgGeneral.add(new BoolSetting.Builder()
        .name("Unblocked")
        .description("Doesn't place on faces that have block on them.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("Air Place")
        .description("Can place blocks in air.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> maxHeight = sgGeneral.add(new BoolSetting.Builder()
        .name("Max Height")
        .description("Doesn't place on top sides of blocks at max height.")
        .defaultValue(true)
        .build()
    );


    public PlaceData getPlaceDataOR(BlockPos pos, Predicate<BlockPos> predicate, boolean ignoreContainers) {
        if (pos == null) {return new PlaceData(null, null, false);}
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            if (airPlace.get()) {
                return new PlaceData(pos, Direction.UP, true);
            } else {
                double cDist = -1;
                for (Direction dir : MixinDirection.getAll()) {

                    // Doesn't place on top of max height
                    if (maxHeight.get() && pos.offset(dir).getY() > mc.world.getHeight()) {continue;}

                    // Checks if block is an entity (chests, shulkers)
                    if (ignoreContainers && mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) {continue;}

                    // Test if there is block in the side and if predicate is valid
                    if (!OLEPOSSUtils.solid(pos.offset(dir)) && (predicate != null && !predicate.test(pos.offset(dir)))) {continue;}

                    // Strict dir check (checks if face is on opposite side of the block to player)
                    if (strictDir.get() && !OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite())) {continue;}

                    // Only accepts if closer than previous accepted direction
                    double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                    if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                        best = dir;
                        cDist = dist;
                    }
                }
            }
        }
        return best == null ? new PlaceData(null, null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
    }

    public PlaceData getPlaceDataAND(BlockPos pos, Predicate<Direction> predicate, Predicate<BlockPos> predicatePos, boolean ignoreContainers) {
        if (pos == null) {return new PlaceData(null, null, false);}
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            if (airPlace.get()) {
                return new PlaceData(pos, Direction.UP, true);
            } else {
                double cDist = -1;
                for (Direction dir : MixinDirection.getAll()) {

                    // Doesn't place on top of max height
                    if (maxHeight.get() && pos.offset(dir).getY() > mc.world.getHeight()) {continue;}

                    // Checks if block is an entity (chests, shulkers)
                    if (ignoreContainers && mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) {continue;}

                    // Test if there is block in the side and if predicate is valid
                    if (!OLEPOSSUtils.solid(pos.offset(dir)) || (predicate != null && !predicate.test(dir)) || (predicatePos != null && !predicatePos.test(pos.offset(dir)))) {continue;}

                    // Strict dir check (checks if face is on opposite side of the block to player)
                    if (strictDir.get() && !OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite())) {continue;}

                    // Only accepts if closer than previous accepted direction
                    double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                    if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                        best = dir;
                        cDist = dist;
                    }
                }
            }
        }
        return best == null ? new PlaceData(null, null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
    }

    public PlaceData getPlaceData(BlockPos pos, boolean ignoreContainers) {
        if (pos == null) {return new PlaceData(null, null, false);}
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            if (airPlace.get()) {
                return new PlaceData(pos, Direction.UP, true);
            } else {
                double cDist = -1;
                for (Direction dir : MixinDirection.getAll()) {

                    // Doesn't place on top of max height
                    if (maxHeight.get() && pos.offset(dir).getY() > mc.world.getHeight()) {continue;}

                    // Checks if block is an entity (chests, shulkers)
                    if (ignoreContainers && mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) {continue;}

                    // Test if there is block in the side and if predicate is valid
                    if (!OLEPOSSUtils.solid(pos.offset(dir))) {continue;}

                    // Strict dir check (checks if face is on opposite side of the block to player)
                    if (strictDir.get() && !OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite())) {continue;}

                    // Only accepts if closer than previous accepted direction
                    double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                    if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                        best = dir;
                        cDist = dist;
                    }
                }
            }
        }
        return best == null ? new PlaceData(null, null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
    }

    public Direction getPlaceOnDirection(BlockPos pos) {
        if (!strictDir.get()) {return Direction.UP;}
        if (pos == null) {return null;}
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            double cDist = -1;
            for (Direction dir : MixinDirection.getAll()) {

                // Doesn't place on top of max height
                if (maxHeight.get() && pos.offset(dir).getY() > mc.world.getHeight()) {continue;}

                // Unblocked check (mostly for autocrystal placement facings)
                if (unblocked.get() && !(getBlock(pos.offset(dir)) == Blocks.AIR)) {continue;}

                // Strict dir check (checks if face isnt on opposite side of the block to player)
                if (strictDir.get() && !OLEPOSSUtils.strictDir(pos, dir)) {continue;}

                // Only accepts if closer than last accepted direction
                double dist = dist(pos, dir);
                if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                    best = dir;
                    cDist = dist;
                }
            }
        }
        return best;
    }

    double dist(BlockPos pos, Direction dir) {
        if (mc.player == null) {return 0;}
        Vec3d vec = new Vec3d(pos.getX() + dir.getOffsetX() / 2f, pos.getY() + dir.getOffsetY() / 2f, pos.getZ() + dir.getOffsetZ() / 2f);
        Vec3d dist = mc.player.getEyePos().add(-vec.x, -vec.y, -vec.z);
        return Math.sqrt(dist.x * dist.x + dist.y * dist.y + dist.z * dist.z);
    }

    Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }
}
