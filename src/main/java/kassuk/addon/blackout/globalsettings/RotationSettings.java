package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU
*/

public class RotationSettings extends Module {
    public RotationSettings() {
        super(BlackOut.SETTINGS, "Rotation", "Global rotation settings for every blackout module");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Placing");
    private final SettingGroup sgAttack = settings.createGroup("Attacking");

    //  General Settings
    private final Setting<RotationCheckMode> rotationCheckMode = sgPlace.add(new EnumSetting.Builder<RotationCheckMode>()
        .name("Rotation Check Mode")
        .description(".")
        .defaultValue(RotationCheckMode.Raytrace)
        .build()
    );
    private final Setting<Boolean> crystalRotate = sgPlace.add(new BoolSetting.Builder()
        .name("Crystal Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> attackRotate = sgPlace.add(new BoolSetting.Builder()
        .name("Attack Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> placeRotate = sgPlace.add(new BoolSetting.Builder()
        .name("Placing Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> breakRotate = sgPlace.add(new BoolSetting.Builder()
        .name("Breaking Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> interactRotate = sgPlace.add(new BoolSetting.Builder()
        .name("Interact Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );


    public enum RotationCheckMode {
        Raytrace,
        Angle,
        CustomRaytrace
    }
    public boolean shouldRotate(RotationType type) {
        switch (type) {
            case Crystal -> {return crystalRotate.get();}
            case Attacking -> {return attackRotate.get();}
            case Placing -> {return placeRotate.get();}
            case Breaking -> {return breakRotate.get();}
            case Interact -> {return interactRotate.get();}
        }
        return false;
    }
    public boolean rotationCheck(double yaw, double pitch, Box box) {
        if (box == null){return false;}
        if (mc.player == null) {return false;}
        return rotationCheck(mc.player.getPos(), yaw, pitch, box);
    }

    public boolean rotationCheck(Vec3d pPos, double yaw, double pitch, Box box) {
        if (box == null){return false;}
        switch (rotationCheckMode.get()) {
            case Raytrace -> {
                double range = OLEPOSSUtils.distance(pPos, OLEPOSSUtils.getMiddle(box)) + 3;
                Vec3d end = new Vec3d(range * Math.cos(Math.toRadians(yaw + 90)) * Math.abs(Math.cos(Math.toRadians(pitch))),
                    range * -Math.sin(Math.toRadians(pitch)),
                    range * Math.sin(Math.toRadians(yaw + 90)) * Math.abs(Math.cos(Math.toRadians(pitch)))).add(pPos);

                Vec3d vec = new Vec3d(0, 0, 0);
                for (float i = 0; i < 1; i += 0.01) {
                    ((IVec3d)vec).set(pPos.x + (end.x - pPos.x) * i, pPos.y + (end.y - pPos.y) * i, pPos.z + (end.z - pPos.z) * i);

                    if (vec.x >= box.minX && vec.x <= box.maxX &&
                        vec.y >= box.minY && vec.y <= box.maxY &&
                        vec.z >= box.minZ && vec.z <= box.maxZ) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }
}
