package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.managers.RotationManager;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/*
Made by OLEPOSSU
*/

public class RotationSettings extends Module {
    public RotationSettings() {
        super(BlackOut.SETTINGS, "Rotation", "Global rotation settings for every blackout module");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCrystal = settings.createGroup("Crystal");
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgMine = settings.createGroup("Mine");
    private final SettingGroup sgInteract = settings.createGroup("Interact");

    //  General Settings
    private final Setting<RotationCheckMode> rotationCheckMode = sgGeneral.add(new EnumSetting.Builder<RotationCheckMode>()
        .name("Rotation Check Mode")
        .description(".")
        .defaultValue(RotationCheckMode.Raytrace)
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

    //  Crystal Page
    private final Setting<Boolean> crystalRotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Crystal Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> crystalExisted = sgGeneral.add(new IntSetting.Builder()
        .name("Crystal Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    // Attack Page
    private final Setting<Boolean> attackRotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Attack Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> attackExisted = sgGeneral.add(new IntSetting.Builder()
        .name("Attack Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    // Place Page
    private final Setting<Boolean> placeRotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Placing Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> placeExisted = sgGeneral.add(new IntSetting.Builder()
        .name("Place Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    // Mine Page
    public final Setting<MiningRotMode> mineRotate = sgGeneral.add(new EnumSetting.Builder<MiningRotMode>()
        .name("Mine Rotate")
        .description(".")
        .defaultValue(MiningRotMode.Disabled)
        .build()
    );
    private final Setting<Integer> mineExisted = sgGeneral.add(new IntSetting.Builder()
        .name("Mine Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    // Interact Page
    private final Setting<Boolean> interactRotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Interact Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> interactExisted = sgGeneral.add(new IntSetting.Builder()
        .name("Interact Rotation Existed")
        .description(".")
        .defaultValue(0)
        .range(0, 10)
        .sliderRange(0, 10)
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
        Raytrace
    }
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
        return rotationCheck(null, 0.0, 0.0, box, existed);
    }

    public boolean rotationCheck(Vec3d pPos, double yaw, double pitch, Box box, int existed) {
        List<RotationManager.Rotation> history = RotationManager.history;
        if (box == null){return false;}

        switch (rotationCheckMode.get()) {
            case Raytrace -> {
                if (pPos != null) {
                    if (!raytraceCheck(pPos, yaw, pitch, box)) {
                        return false;
                    }
                }
                for (int r = 0; r < existed; r++) {
                    if (history.size() <= r) {break;}
                    RotationManager.Rotation rot = history.get(r);
                    if (!raytraceCheck(rot.vec(), rot.yaw(), rot.pitch(), box)) {
                        return false;
                    }
                }
            }
        }
        return true;
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

    public boolean endMineRot() {
        return mineRotate.get() == MiningRotMode.End || mineRotate.get() == MiningRotMode.Double;
    }
    public boolean startMineRot() {
        return mineRotate.get() == MiningRotMode.Start || mineRotate.get() == MiningRotMode.Double;
    }
}
