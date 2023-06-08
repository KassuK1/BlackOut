package kassuk.addon.blackout.globalsettings;

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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * @author OLEPOSSU
 */

public class RotationSettings extends BlackOutModule {
    public RotationSettings() {
        super(BlackOut.SETTINGS, "Rotate", "Global rotation settings for every blackout module.");
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
        .description("Accepts rotation of yaw angle to target is under this.")
        .defaultValue(90)
        .range(0, 180)
        .sliderRange(0, 180)
        .visible(() -> rotationCheckMode.get() == RotationCheckMode.Angle)
        .build()
    );
    public final Setting<Double> pitchAngle = sgGeneral.add(new DoubleSetting.Builder()
        .name("Pitch Angle")
        .description("Accepts rotation of pitch angle to target is under this.")
        .defaultValue(45)
        .range(0, 180)
        .sliderRange(0, 180)
        .visible(() -> rotationCheckMode.get() == RotationCheckMode.Angle)
        .build()
    );
    public final Setting<Boolean> vanillaRotation = sgGeneral.add(new BoolSetting.Builder()
        .name("Vanilla Rotation")
        .description("Turns your head.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> yawStep = sgGeneral.add(new DoubleSetting.Builder()
        .name("Yaw Step")
        .description("How many yaw degrees should be rotated each packet.")
        .defaultValue(180)
        .range(0, 180)
        .sliderRange(0, 180)
        .build()
    );
    public final Setting<Double> pitchStep = sgGeneral.add(new DoubleSetting.Builder()
        .name("Pitch Step")
        .description("How many pitch degrees should be rotated each packet.")
        .defaultValue(180)
        .range(0, 180)
        .sliderRange(0, 180)
        .build()
    );
    public final Setting<Integer> NCPPackets = sgGeneral.add(new IntSetting.Builder()
        .name("Rotation Memory")
        .description("Accepts rotation if looked at it x packets earlier.")
        .defaultValue(5)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    //  Crystal Page
    private final Setting<Boolean> crystalRotate = sgCrystal.add(new BoolSetting.Builder()
        .name("Crystal Rotate")
        .description("Rotates when placing crystals.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> crystalTime = sgCrystal.add(new DoubleSetting.Builder()
        .name("Crystal Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    // Attack Page
    private final Setting<Boolean> attackRotate = sgAttack.add(new BoolSetting.Builder()
        .name("Attack Rotate")
        .description("Rotates when attacking entities.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> attackTime = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    // Place Page
    private final Setting<Boolean> placeRotate = sgPlace.add(new BoolSetting.Builder()
        .name("Placing Rotate")
        .description("Rotates when placing blocks.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> placeTime = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    // Mine Page
    public final Setting<MiningRotMode> mineRotate = sgMine.add(new EnumSetting.Builder<MiningRotMode>()
        .name("Mine Rotate")
        .description("Rotates when mining.")
        .defaultValue(MiningRotMode.Disabled)
        .build()
    );
    public final Setting<Double> mineTime = sgMine.add(new DoubleSetting.Builder()
        .name("Mine Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    // Interact Page
    private final Setting<Boolean> interactRotate = sgInteract.add(new BoolSetting.Builder()
        .name("Interact Rotate")
        .description("Rotates when interacting with blocks. Crafting tables, chests...")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> interactTime = sgInteract.add(new DoubleSetting.Builder()
        .name("Interact Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    // Use Page
    public final Setting<Double> useTime = sgUse.add(new DoubleSetting.Builder()
        .name("Use Rotation Time")
        .description("Keeps the rotation for x seconds after ending.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    public enum MiningRotMode {
        Disabled,
        Start,
        End,
        Double
    }

    public enum RotationCheckMode {
        Raytrace,
        Angle
    }

    public Vec3d vec = new Vec3d(0, 0, 0);

    public boolean shouldRotate(RotationType type) {
        switch (type) {
            case Crystal -> {return crystalRotate.get();}
            case Attacking -> {return attackRotate.get();}
            case Placing -> {return placeRotate.get();}
            case Interact -> {return interactRotate.get();}
        }
        return false;
    }

    public boolean rotationCheck(Box box) {
        List<RotationManager.Rotation> history = RotationManager.history;
        if (box == null) {return false;}

        switch (rotationCheckMode.get()) {
            case Raytrace -> {
                for (int r = 0; r < NCPPackets.get(); r++) {
                    if (history.size() <= r) {break;}
                    RotationManager.Rotation rot = history.get(r);

                    if (raytraceCheck(rot.vec(), rot.yaw(), rot.pitch(), box)) {
                        return true;
                    }
                }
            }
            case Angle -> {
                for (int r = 0; r < NCPPackets.get(); r++) {
                    if (history.size() <= r) {break;}
                    RotationManager.Rotation rot = history.get(r);

                    if (angleCheck(rot.vec(), rot.yaw(), rot.pitch(), box)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean angleCheck(Vec3d pos, double y, double p, Box box) {
        return RotationUtils.yawAngle(y, RotationUtils.getYaw(pos, box.getCenter())) <= yawAngle.get() && Math.abs(p - RotationUtils.getPitch(pos, box.getCenter())) <= pitchAngle.get();
    }

    public boolean raytraceCheck(Vec3d pos, double y, double p, Box box) {
        double range = OLEPOSSUtils.distance(pos, OLEPOSSUtils.getMiddle(box)) + 3;
        Vec3d end = new Vec3d(range * Math.cos(Math.toRadians(y + 90)) * Math.abs(Math.cos(Math.toRadians(p))),
            range * -Math.sin(Math.toRadians(p)),
            range * Math.sin(Math.toRadians(y + 90)) * Math.abs(Math.cos(Math.toRadians(p)))).add(pos);

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

    public boolean endMineRot() {
        return mineRotate.get() == MiningRotMode.End || mineRotate.get() == MiningRotMode.Double;
    }

    public boolean startMineRot() {
        return mineRotate.get() == MiningRotMode.Start || mineRotate.get() == MiningRotMode.Double;
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
}
