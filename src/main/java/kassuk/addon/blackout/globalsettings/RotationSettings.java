package kassuk.addon.blackout.globalsettings;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.managers.RotationManager;
import kassuk.addon.blackout.modules.AutoMine;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.RotationUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.block.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author OLEPOSSU
 */

public class RotationSettings extends BlackOutModule {
    public RotationSettings() {
        super(BlackOut.SETTINGS, "Rotate", "Global rotation settings for every blackout module.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgInteract = settings.createGroup("Interact");
    private final SettingGroup sgBlockPlace = settings.createGroup("Block Place");
    private final SettingGroup sgMining = settings.createGroup("Mining");
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgUse = settings.createGroup("Use");

    //--------------------General--------------------//
    public final Setting<Boolean> vanillaRotation = sgGeneral.add(new BoolSetting.Builder()
        .name("Vanilla Rotation")
        .description("Turns your head.")
        .defaultValue(false)
        .build()
    );

    //--------------------Interact--------------------//
    private final Setting<Boolean> interactRotate = rotateSetting("Interact", "interacting with a block", sgInteract);
    public final Setting<Double> interactTime = timeSetting("Interact", sgInteract);
    public final Setting<RotationCheckMode> interactMode = modeSetting("Interact", sgInteract);
    public final Setting<Double> interactYawAngle = yawAngleSetting("Interact", sgInteract, () -> interactMode.get() == RotationCheckMode.Angle);
    public final Setting<Double> interactPitchAngle = pitchAngleSetting("Interact", sgInteract, () -> interactMode.get() == RotationCheckMode.Angle);
    public final Setting<Double> interactYawStep = yawStepSetting("Interact", sgInteract);
    public final Setting<Double> interactPitchStep = pitchStepSetting("Interact", sgInteract);
    public final Setting<Integer> interactMemory = memorySetting("Interact", sgInteract);

    //--------------------Block-Place--------------------//
    private final Setting<Boolean> blockRotate = rotateSetting("Block Place", "placing a block", sgBlockPlace);
    public final Setting<Double> blockTime = timeSetting("Block Place", sgBlockPlace);
    public final Setting<RotationCheckMode> blockMode = modeSetting("Block Place", sgBlockPlace);
    public final Setting<Double> blockYawAngle = yawAngleSetting("Block Place", sgBlockPlace, () -> blockMode.get() == RotationCheckMode.Angle);
    public final Setting<Double> blockPitchAngle = pitchAngleSetting("Block Place", sgBlockPlace, () -> blockMode.get() == RotationCheckMode.Angle);
    public final Setting<Double> blockYawStep = yawStepSetting("Block Place", sgBlockPlace);
    public final Setting<Double> blockPitchStep = pitchStepSetting("Block Place", sgBlockPlace);
    public final Setting<Integer> blockMemory = memorySetting("Block Place", sgBlockPlace);

    //--------------------Mining--------------------//
    private final Setting<Boolean> mineRotate = rotateSetting("Mining", "mining a block", sgMining);
    public final Setting<Double> mineTime = timeSetting("Mining", sgMining);
    public final Setting<RotationCheckMode> mineMode = modeSetting("Mining", sgMining);
    public final Setting<MiningRotMode> mineTiming = sgMining.add(new EnumSetting.Builder<MiningRotMode>().name("Mining Rotate Timing").description(".").defaultValue(MiningRotMode.Disabled).build());
    public final Setting<Double> mineYawAngle = yawAngleSetting("Mining", sgMining, () -> mineMode.get() == RotationCheckMode.Angle);
    public final Setting<Double> minePitchAngle = pitchAngleSetting("Mining", sgMining, () -> mineMode.get() == RotationCheckMode.Angle);
    public final Setting<Double> mineYawStep = yawStepSetting("Mining", sgMining);
    public final Setting<Double> minePitchStep = pitchStepSetting("Mining", sgMining);
    public final Setting<Integer> mineMemory = memorySetting("Mining", sgMining);

    //--------------------Attack--------------------//
    private final Setting<Boolean> attackRotate = rotateSetting("Attack", "attacking an entity", sgAttack);
    public final Setting<Double> attackTime = timeSetting("Attack", sgAttack);
    public final Setting<RotationCheckMode> attackMode = modeSetting("Attack", sgAttack);
    public final Setting<Double> attackYawAngle = yawAngleSetting("Attack", sgAttack, () -> attackMode.get() == RotationCheckMode.Angle);
    public final Setting<Double> attackPitchAngle = pitchAngleSetting("Attack", sgAttack, () -> attackMode.get() == RotationCheckMode.Angle);
    public final Setting<Double> attackYawStep = yawStepSetting("Attack", sgAttack);
    public final Setting<Double> attackPitchStep = pitchStepSetting("Attack", sgAttack);
    public final Setting<Integer> attackMemory = memorySetting("Attack", sgAttack);

    //--------------------Use--------------------//
    public final Setting<Double> useTime = timeSetting("Use", sgUse);
    public final Setting<Double> useYawStep = yawStepSetting("Use", sgUse);
    public final Setting<Double> usePitchStep = pitchStepSetting("Use", sgUse);

    public enum MiningRotMode {
        Disabled,
        Start,
        End,
        Double
    }

    public enum RotationCheckMode {
        Raytrace,
        StrictRaytrace,
        Angle
    }

    private Setting<Boolean> rotateSetting(String type, String verb, SettingGroup sg) {
        return sg.add(new BoolSetting.Builder().name(type + " Rotate").description("Rotates when + " + verb).defaultValue(false).build());
    }

    private Setting<Double> timeSetting(String type, SettingGroup sg) {
        return sg.add(new DoubleSetting.Builder().name(type + " Rotation Time").description("Keeps the rotation for x seconds after ending.").defaultValue(0.5).min(0).sliderRange(0, 1).build());
    }

    private Setting<RotationCheckMode> modeSetting(String type, SettingGroup sg) {
        return sg.add(new EnumSetting.Builder<RotationCheckMode>().name(type + " Rotation Mode").description(".").defaultValue(RotationCheckMode.Raytrace).build());
    }

    private Setting<Double> yawAngleSetting(String type, SettingGroup sg, IVisible visible) {
        return sg.add(new DoubleSetting.Builder().name(type + " Yaw Angle").description("Accepts rotation if yaw angle to target is under this.").defaultValue(90).range(0, 180).sliderRange(0, 180).visible(visible).build());
    }

    private Setting<Double> pitchAngleSetting(String type, SettingGroup sg, IVisible visible) {
        return sg.add(new DoubleSetting.Builder().name(type + " Pitch Angle").description("Accepts rotation if pitch angle to target is under this.").defaultValue(45).range(0, 180).sliderRange(0, 180).visible(visible).build());
    }

    private Setting<Double> yawStepSetting(String type, SettingGroup sg) {
        return sg.add(new DoubleSetting.Builder().name(type + " Yaw Step").description("How many yaw degrees should be rotated each packet.").defaultValue(90).range(0, 180).sliderRange(0, 180).build());
    }

    private Setting<Double> pitchStepSetting(String type, SettingGroup sg) {
        return sg.add(new DoubleSetting.Builder().name(type + " Pitch Step").description("How many pitch degrees should be rotated each packet.").defaultValue(45).range(0, 180).sliderRange(0, 180).build());
    }

    private Setting<Integer> memorySetting(String type, SettingGroup sg) {
        return sg.add(new IntSetting.Builder().name(type + " Memory").description("Accepts rotation if looked at it x packets earlier.").defaultValue(1).range(1, 20).sliderRange(1, 20).build());
    }

    public final Vec3d vec = new Vec3d(0, 0, 0);

    public boolean rotationCheck(Box box, RotationType type) {
        List<RotationManager.Rotation> history = RotationManager.history;
        if (box == null) return false;

        switch (mode(type)) {
            case Raytrace -> {
                for (int r = 0; r < memory(type); r++) {
                    if (history.size() <= r) {break;}
                    RotationManager.Rotation rot = history.get(r);

                    if (raytraceCheck(rot.vec(), rot.yaw(), rot.pitch(), box)) return true;
                }
            }
            case StrictRaytrace -> {
                for (int r = 0; r < memory(type); r++) {
                    if (history.size() <= r) {break;}
                    RotationManager.Rotation rot = history.get(r);

                    double range = mc.player.getEyePos().distanceTo(box.getCenter()) + 3;

                    Vec3d end = new Vec3d(
                        range * Math.cos(Math.toRadians(rot.yaw() + 90)) * Math.abs(Math.cos(Math.toRadians(rot.pitch()))),
                        range * -Math.sin(Math.toRadians(rot.pitch())),
                        range * Math.sin(Math.toRadians(rot.yaw() + 90)) * Math.abs(Math.cos(Math.toRadians(rot.pitch()))))
                        .add(mc.player.getEyePos());

                    if (constRotCheck(rot.vec(), end, box)) return true;
                }
            }
            case Angle -> {
                for (int r = 0; r < memory(type); r++) {
                    if (history.size() <= r) break;
                    RotationManager.Rotation rot = history.get(r);

                    if (angleCheck(rot.vec(), rot.yaw(), rot.pitch(), box, type)) return true;
                }
            }
        }

        return false;
    }

    public boolean shouldRotate(RotationType type) {
        return switch (type) {
            case Interact -> interactRotate.get();
            case BlockPlace -> blockRotate.get();
            case Attacking -> attackRotate.get();
            case Mining -> mineRotate.get();
            default -> true;
        };
    }

    public RotationCheckMode mode(RotationType type) {
        return switch (type) {
            case Interact -> interactMode.get();
            case BlockPlace -> blockMode.get();
            case Attacking -> attackMode.get();
            case Mining -> mineMode.get();
            default -> null;
        };
    }

    public double time(RotationType type) {
        return switch (type) {
            case Interact -> interactTime.get();
            case BlockPlace -> blockTime.get();
            case Attacking -> attackTime.get();
            case Mining -> mineTime.get();
            case Use -> useTime.get();
            case Other -> 1.0;
        };
    }

    public int memory(RotationType type) {
        return switch (type) {
            case Interact -> interactMemory.get();
            case BlockPlace -> blockMemory.get();
            case Attacking -> attackMemory.get();
            case Mining -> mineMemory.get();
            default -> 1;
        };
    }

    public double yawStep(RotationType type) {
        return switch (type) {
            case Interact -> interactYawStep.get();
            case BlockPlace -> blockYawStep.get();
            case Attacking -> attackYawStep.get();
            case Mining -> mineYawStep.get();
            case Use -> useYawStep.get();
            case Other -> 42069;
        };
    }

    public double pitchStep(RotationType type) {
        return switch (type) {
            case Interact -> interactPitchStep.get();
            case BlockPlace -> blockPitchStep.get();
            case Attacking -> attackPitchStep.get();
            case Mining -> minePitchStep.get();
            case Use -> usePitchStep.get();
            case Other -> 42069;
        };
    }

    public double yawAngle(RotationType type) {
        return switch (type) {
            case Interact -> interactYawAngle.get();
            case BlockPlace -> blockYawAngle.get();
            case Attacking -> attackYawAngle.get();
            case Mining -> mineYawAngle.get();
            default -> 0.0;
        };
    }

    public double pitchAngle(RotationType type) {
        return switch (type) {
            case Interact -> interactPitchAngle.get();
            case BlockPlace -> blockPitchAngle.get();
            case Attacking -> attackPitchAngle.get();
            case Mining -> minePitchAngle.get();
            default -> 0.0;
        };
    }

    public boolean angleCheck(Vec3d pos, double y, double p, Box box, RotationType type) {
        return RotationUtils.yawAngle(y, RotationUtils.getYaw(pos, box.getCenter())) <= yawAngle(type) && Math.abs(p - RotationUtils.getPitch(pos, box.getCenter())) <= pitchAngle(type);
    }

    public boolean raytraceCheck(Vec3d pos, double y, double p, Box box) {
        double range = pos.distanceTo(OLEPOSSUtils.getMiddle(box)) + 3;

        Vec3d end = new Vec3d(range * Math.cos(Math.toRadians(y + 90)) * Math.abs(Math.cos(Math.toRadians(p))),
            range * -Math.sin(Math.toRadians(p)),
            range * Math.sin(Math.toRadians(y + 90)) * Math.abs(Math.cos(Math.toRadians(p)))).add(pos);

        for (float i = 0; i < 1; i += 0.01) {
            if (box.contains(pos.x + (end.x - pos.x) * i, pos.y + (end.y - pos.y) * i, pos.z + (end.z - pos.z) * i)) return true;
        }
        return false;
    }

    public boolean constCheck(Vec3d from, Vec3d to, Box box) {
        int lx = 0, ly = 0, lz = 0;

        for (float i = 0; i < 1; i += 0.01) {
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
        return true;
    }

    public boolean constRotCheck(Vec3d from, Vec3d to, Box box) {
        int lx = 0, ly = 0, lz = 0;

        for (float i = 0; i < 1; i += 0.01) {
            double x = lerp(from.x, to.x, i);
            double y = lerp(from.y, to.y, i);
            double z = lerp(from.z, to.z, i);

            if (box.contains(x, y, z)) break;

            int ix = (int) Math.floor(x);
            int iy = (int) Math.floor(y);
            int iz = (int) Math.floor(z);

            if (lx != ix &&
                ly != iy &&
                lz != iz) {

                BlockPos pos = new BlockPos(ix, iy, iz);
                if (validForCheck(pos, mc.world.getBlockState(pos))) return false;
            }

            lx = ix;
            ly = iy;
            lz = iz;
        }
        return true;
    }

    private double lerp(double from, double to, double delta) {
        return from + (to - from) * delta;
    }

    private boolean validForCheck(BlockPos pos, BlockState state) {
        if (state.isSolid()) return true;
        if (state.getBlock() instanceof FluidBlock) return false;
        if (state.getBlock() instanceof StairsBlock) return false;
        if (state.hasBlockEntity()) return false;

        return state.isFullCube(mc.world, pos);
    }

    public boolean endMineRot() {
        return mineTiming.get() == MiningRotMode.End || mineTiming.get() == MiningRotMode.Double;
    }

    public boolean startMineRot() {
        return mineTiming.get() == MiningRotMode.Start || mineTiming.get() == MiningRotMode.Double;
    }
}
