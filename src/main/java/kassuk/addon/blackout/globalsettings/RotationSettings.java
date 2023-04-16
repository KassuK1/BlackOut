package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.managers.RotationManager;
import kassuk.addon.blackout.mixins.MixinRaycastContext;
import kassuk.addon.blackout.utils.RotationUtils;
import kassuk.addon.blackout.utils.meteor.BODamageUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.opengl.GL11;

import java.util.List;

/*
Made by OLEPOSSU
*/

public class RotationSettings extends BlackOutModule {
    public RotationSettings() {
        super(BlackOut.SETTINGS, "Rotate", "Global rotation settings for every blackout module");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCrystal = settings.createGroup("Crystal");
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgMine = settings.createGroup("Mine");
    private final SettingGroup sgInteract = settings.createGroup("Interact");
    private final SettingGroup sgUse = settings.createGroup("Use");

    //  General Settings
    public final Setting<RotationCheckMode> rotationCheckMode = sgGeneral.add(new EnumSetting.Builder<RotationCheckMode>()
        .name("Rotation Check Mode")
        .description(".")
        .defaultValue(RotationCheckMode.Raytrace)
        .build()
    );
    public final Setting<Double> yawAngle = sgGeneral.add(new DoubleSetting.Builder()
        .name("Yaw Angle")
        .description(".")
        .defaultValue(90)
        .range(0, 180)
        .sliderRange(0, 180)
        .visible(() -> rotationCheckMode.get() == RotationCheckMode.Angle)
        .build()
    );
    public final Setting<Double> pitchAngle = sgGeneral.add(new DoubleSetting.Builder()
        .name("Pitch Angle")
        .description(".")
        .defaultValue(45)
        .range(0, 180)
        .sliderRange(0, 180)
        .visible(() -> rotationCheckMode.get() == RotationCheckMode.Angle)
        .build()
    );
    public final Setting<Boolean> ghostRotation = sgCrystal.add(new BoolSetting.Builder()
        .name("Ghost Rotation")
        .description("Rotates at the closest seen point.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> vanillaRotation = sgCrystal.add(new BoolSetting.Builder()
        .name("Vanilla Rotation")
        .description("Turns your head.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> yawStep = sgGeneral.add(new DoubleSetting.Builder()
        .name("Yaw Step")
        .description(".")
        .defaultValue(180)
        .range(0, 180)
        .sliderRange(0, 180)
        .build()
    );
    public final Setting<Double> pitchStep = sgGeneral.add(new DoubleSetting.Builder()
        .name("Pitch Step")
        .description(".")
        .defaultValue(180)
        .range(0, 180)
        .sliderRange(0, 180)
        .build()
    );
    public final Setting<Boolean> NCPRotation = sgGeneral.add(new BoolSetting.Builder()
        .name("NCP Rotations")
        .description(".")
        .defaultValue(true)
        .build()
    );
    public final Setting<Integer> NCPPackets = sgGeneral.add(new IntSetting.Builder()
        .name("NCP Rotation packets")
        .description(".")
        .defaultValue(5)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    //  Crystal Page
    private final Setting<Boolean> crystalRotate = sgCrystal.add(new BoolSetting.Builder()
        .name("Crystal Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> crystalExisted = sgCrystal.add(new IntSetting.Builder()
        .name("Crystal Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<Double> crystalTime = sgCrystal.add(new DoubleSetting.Builder()
        .name("Crystal Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );

    // Attack Page
    private final Setting<Boolean> attackRotate = sgAttack.add(new BoolSetting.Builder()
        .name("Attack Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> attackExisted = sgAttack.add(new IntSetting.Builder()
        .name("Attack Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<Double> attackTime = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );

    // Place Page
    private final Setting<Boolean> placeRotate = sgPlace.add(new BoolSetting.Builder()
        .name("Placing Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> placeExisted = sgPlace.add(new IntSetting.Builder()
        .name("Place Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<Double> placeTime = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );

    // Mine Page
    public final Setting<MiningRotMode> mineRotate = sgMine.add(new EnumSetting.Builder<MiningRotMode>()
        .name("Mine Rotate")
        .description(".")
        .defaultValue(MiningRotMode.Disabled)
        .build()
    );
    private final Setting<Integer> mineExisted = sgMine.add(new IntSetting.Builder()
        .name("Mine Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<Double> mineTime = sgMine.add(new DoubleSetting.Builder()
        .name("Mine Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );

    // Interact Page
    private final Setting<Boolean> interactRotate = sgInteract.add(new BoolSetting.Builder()
        .name("Interact Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> interactExisted = sgInteract.add(new IntSetting.Builder()
        .name("Interact Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<Double> interactTime = sgInteract.add(new DoubleSetting.Builder()
        .name("Interact Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );

    // Use Page
    public final Setting<Double> useTime = sgUse.add(new DoubleSetting.Builder()
        .name("Use Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );

    public enum MiningRotMode {
        Disabled,
        Start,
        End,
        Double,
        Full
    }

    public enum RotationCheckMode {
        Raytrace,
        Angle,
        Ghost
    }

    public RaycastContext raycastContext;
    public BlockHitResult result;
    public Vec3d vec = new Vec3d(0, 0, 0);

    // Closest
    double closestDistance = 0;
    Vec3d closest = new Vec3d(0, 0, 0);

    Vec3d eyePos = new Vec3d(0, 0, 0);
    Vec3d offset = new Vec3d(0, 0, 0);

    double distance = 0;

    public boolean shouldRotate(RotationType type) {
        switch (type) {
            case Crystal -> {return crystalRotate.get();}
            case Attacking -> {return attackRotate.get();}
            case Placing -> {return placeRotate.get();}
            case Breaking -> {return mineRotate.get() == MiningRotMode.Full;}
            case Interact -> {return interactRotate.get();}
        }
        return false;
    }
    public int getExisted(RotationType type) {
        switch (type) {
            case Crystal -> {return crystalExisted.get();}
            case Attacking -> {return attackExisted.get();}
            case Placing -> {return placeExisted.get();}
            case Breaking -> {return mineExisted.get();}
            case Interact -> {return interactExisted.get();}
            default -> {return 1;}
        }
    }

    public boolean rotationCheckHistory(Box box, int existed) {
        if (box == null){return false;}
        if (mc.player == null) {return false;}
        return rotationCheck(null, 0.0, 0.0, box, existed, NCPRotation.get());
    }

    public boolean rotationCheck(Vec3d pPos, double yaw, double pitch, Box box, int existed, boolean ncp) {
        List<RotationManager.Rotation> history = RotationManager.history;
        if (box == null){return false;}

        switch (rotationCheckMode.get()) {
            case Raytrace -> {
                if (pPos != null) {
                    if (ncp) {
                        if (raytraceCheck(pPos, yaw, pitch, box)) {
                            return true;
                        }
                    } else {
                        if (!raytraceCheck(pPos, yaw, pitch, box)) {
                            return false;
                        }
                    }
                }
                for (int r = 0; r < existed; r++) {
                    if (history.size() <= r) {break;}
                    RotationManager.Rotation rot = history.get(r);
                    if (ncp) {
                        if (raytraceCheck(rot.vec(), rot.yaw(), rot.pitch(), box)) {
                            return true;
                        }
                    } else {
                        if (!raytraceCheck(rot.vec(), rot.yaw(), rot.pitch(), box)) {
                            return false;
                        }
                    }
                }
            }
            case Angle -> {
                if (pPos != null) {if (ncp) {
                        if (angleCheck(pPos, yaw, pitch, box)) {
                            return true;
                        }
                    } else {
                        if (!angleCheck(pPos, yaw, pitch, box)) {
                            return false;
                        }
                    }
                }
                for (int r = 0; r < existed; r++) {
                    if (history.size() <= r) {break;}
                    RotationManager.Rotation rot = history.get(r);
                    if (ncp) {
                        if (angleCheck(rot.vec(), rot.yaw(), rot.pitch(), box)) {
                            return true;
                        }
                    } else {
                        if (!angleCheck(rot.vec(), rot.yaw(), rot.pitch(), box)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    public boolean angleCheck(Vec3d pos, double y, double p, Box box) {
        return RotationUtils.yawAngle(y, RotationUtils.getYaw(pos, box.getCenter())) <= yawAngle.get() && Math.abs(p - RotationUtils.getPitch(pos, box.getCenter())) <= pitchAngle.get();
    }

    public boolean raytraceCheck(Vec3d pos, double y, double p, Box box) {
        double range = OLEPOSSUtils.distance(pos, OLEPOSSUtils.getMiddle(box)) + 3;
        Vec3d end = new Vec3d(range * Math.cos(Math.toRadians(y + 90)) * Math.abs(Math.cos(Math.toRadians(p))),
            range * -Math.sin(Math.toRadians(p)),
            range * Math.sin(Math.toRadians(y + 90)) * Math.abs(Math.cos(Math.toRadians(p)))).add(pos);

        Vec3d vec = new Vec3d(0, 0, 0);
        for (float i = 0; i < 1; i += 0.01) {
            ((IVec3d) vec).set(pos.x + (end.x - pos.x) * i, pos.y + (end.y - pos.y) * i, pos.z + (end.z - pos.z) * i);

            if (vec.x >= box.minX && vec.x <= box.maxX &&
                vec.y >= box.minY && vec.y <= box.maxY &&
                vec.z >= box.minZ && vec.z <= box.maxZ) {
                return true;
            }
        }
        return false;
    }
    public boolean raytraceCheck(Vec3d pos, double y, double p, BlockPos blockPos) {
        updateContext();

        double range = OLEPOSSUtils.distance(pos, OLEPOSSUtils.getMiddle(blockPos)) + 1;
        Vec3d end = new Vec3d(range * Math.cos(Math.toRadians(y + 90)) * Math.abs(Math.cos(Math.toRadians(p))),
            range * -Math.sin(Math.toRadians(p)),
            range * Math.sin(Math.toRadians(y + 90)) * Math.abs(Math.cos(Math.toRadians(p)))).add(pos);

        ((MixinRaycastContext) raycastContext).setEnd(end);
        result = BODamageUtils.raycast(raycastContext);

        return result.getBlockPos().equals(blockPos);
    }

    public boolean endMineRot() {
        return mineRotate.get() == MiningRotMode.End || mineRotate.get() == MiningRotMode.Double;
    }
    public boolean startMineRot() {
        return mineRotate.get() == MiningRotMode.Start || mineRotate.get() == MiningRotMode.Double;
    }

    public Vec3d getGhostRot(BlockPos pos, Vec3d targetVec) {
        updateContext();

        ((MixinRaycastContext) raycastContext).setEnd(targetVec);
        result = BODamageUtils.raycast(raycastContext);

        if (result.getBlockPos().equals(pos)) {return targetVec;}


        ((IVec3d) vec).set(pos.getX(), pos.getY(), pos.getZ());

        closestDistance = Double.MAX_VALUE;
        ((IVec3d) closest).set(-1, -1, -1);
        eyePos = mc.player.getEyePos();

        for (int x = 0; x <= 4; x += 1) {
            for (int y = 0; y <= 4; y += 1) {
                for (int z = 0; z <= 4; z += 1) {
                    ((MixinRaycastContext) raycastContext).setEnd(vec.add(0.1 + x * 0.16, 0.1 + y * 0.16, 0.1 + z * 0.16));

                    result = BODamageUtils.raycast(raycastContext);

                    if (result.getBlockPos().equals(pos)) {
                        offset = eyePos.subtract(vec.add(0.1 + x * 0.16, 0.1 + y * 0.16, 0.1 + z * 0.16));
                        distance = offset.lengthSquared();

                        if (closestDistance > distance) {
                            closestDistance = distance;
                            ((IVec3d) closest).set(x, y, z);
                        }
                    }
                }
            }
        }
        return closest.getX() == 0 && closest.getY() == 0 && closest.getZ() == 0 ? targetVec : vec.add(0.1 + closest.getX() * 0.16, 0.1 + closest.getY() * 0.16, 0.1 + closest.getZ() * 0.16);
    }

    public Vec3d getGhostRot(Box box, Vec3d targetVec) {
        updateContext();

        ((MixinRaycastContext) raycastContext).setEnd(targetVec);
        result = BODamageUtils.raycast(raycastContext);

        if (result.getType() != HitResult.Type.BLOCK) {return targetVec;}


        ((IVec3d) vec).set(box.minX, box.minY, box.minZ);

        closestDistance = Double.MAX_VALUE;
        ((IVec3d) closest).set(-1, -1, -1);
        eyePos = mc.player.getEyePos();

        for (int x = 0; x <= 4; x += 1) {
            for (int y = 0; y <= 4; y += 1) {
                for (int z = 0; z <= 4; z += 1) {
                    ((MixinRaycastContext) raycastContext).setEnd(vec.add(0.1 + x * 0.16, 0.1 + y * 0.16, 0.1 + z * 0.16));

                    result = BODamageUtils.raycast(raycastContext);

                    if (result.getType() != HitResult.Type.BLOCK) {
                        offset = eyePos.subtract(vec.add(0.1 + x * 0.16, 0.1 + y * 0.16, 0.1 + z * 0.16));
                        distance = offset.lengthSquared();

                        if (closestDistance > distance) {
                            closestDistance = distance;
                            ((IVec3d) closest).set(x, y, z);
                        }
                    }
                }
            }
        }
        return closest.getX() == 0 && closest.getY() == 0 && closest.getZ() == 0 ? targetVec : vec.add(0.1 + closest.getX() * 0.16, 0.1 + closest.getY() * 0.16, 0.1 + closest.getZ() * 0.16);
    }

    public double getTime(RotationType type) {
        return switch (type) {
            case Crystal -> crystalTime.get();
            case Attacking -> attackTime.get();
            case Placing -> placeTime.get();
            case Breaking -> mineTime.get();
            case Interact -> interactTime.get();
            case Use -> useTime.get();
            case Other -> 1;
        };
    }

    void updateContext() {
        if (raycastContext == null) {
            raycastContext = new RaycastContext(mc.player.getEyePos(), null, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player);
        } else {
            ((MixinRaycastContext) raycastContext).setStart(mc.player.getEyePos());
        }
    }
}
