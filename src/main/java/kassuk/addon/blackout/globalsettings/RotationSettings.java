package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
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

    public enum RotationCheckMode {
        Raytrace,
        Angle,
        CustomRaytrace
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

                for (int i = 0; i < 1; i += 0.01) {
                    Vec3d vec = new Vec3d(pPos.x + (end.x - pPos.x) * i, pPos.y + (end.y - pPos.y) * i, pPos.z + (end.z - pPos.z) * i);

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
