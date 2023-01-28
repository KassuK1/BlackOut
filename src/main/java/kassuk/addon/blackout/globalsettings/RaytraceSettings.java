package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.BODamageUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.mixininterface.IRaycastContext;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashMap;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/*
Made by OLEPOSSU
*/

public class RaytraceSettings extends Module {
    public RaytraceSettings() {
        super(BlackOut.SETTINGS, "Raytrace", "Global raytrace settings for every blackout module");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Placing");
    private final SettingGroup sgAttack = settings.createGroup("Attacking");

    //  Place Settings
    public final Setting<Boolean> placeTrace = sgPlace.add(new BoolSetting.Builder()
        .name("Place Traces")
        .description("Raytraces when placing")
        .defaultValue(false)
        .build()
    );
    private final Setting<RaytraceMode> placeMode = sgPlace.add(new EnumSetting.Builder<RaytraceMode>()
        .name("Place Mode")
        .description("Place trace mode.")
        .defaultValue(RaytraceMode.SinglePoint)
        .build()
    );
    private final Setting<Double> placeHeight = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-0.5, 1.5)
        .visible(() -> placeMode.get().equals(RaytraceMode.SinglePoint))
        .build()
    );
    private final Setting<Double> placeExposure = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Exposure")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(() -> placeMode.get().equals(RaytraceMode.Exposure))
        .build()
    );
    private final Setting<Double> placeHeight1 = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height 1")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-0.5, 1.5)
        .visible(() -> placeMode.get().equals(RaytraceMode.DoublePoint))
        .build()
    );
    private final Setting<Double> placeHeight2 = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height 2")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-0.5, 1.5)
        .visible(() -> placeMode.get().equals(RaytraceMode.DoublePoint))
        .build()
    );

    //  Attack Settings
    public final Setting<Boolean> attackTrace = sgAttack.add(new BoolSetting.Builder()
        .name("Attack Traces")
        .description("Raytraces when attacking")
        .defaultValue(false)
        .build()
    );
    private final Setting<RaytraceMode> attackMode = sgAttack.add(new EnumSetting.Builder<RaytraceMode>()
        .name("Attack Mode")
        .description("Attack trace mode.")
        .defaultValue(RaytraceMode.SinglePoint)
        .build()
    );
    private final Setting<Double> attackHeight = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-0.5, 1.5)
        .visible(() -> attackMode.get().equals(RaytraceMode.SinglePoint))
        .build()
    );
    private final Setting<Double> attackExposure = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Exposure %")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(() -> attackMode.get().equals(RaytraceMode.Exposure))
        .build()
    );
    private final Setting<Double> attackHeight1 = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height 1")
        .description("Raytraces to x * hitbox height above the bottom.")
        .defaultValue(0.25)
        .sliderRange(-1, 2)
        .visible(() -> attackMode.get().equals(RaytraceMode.DoublePoint))
        .build()
    );
    private final Setting<Double> attackHeight2 = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height 2")
        .description("Raytraces to x * hitbox height above the bottom.")
        .defaultValue(0.75)
        .sliderRange(-1, 2)
        .visible(() -> attackMode.get().equals(RaytraceMode.DoublePoint))
        .build()
    );
    public enum RaytraceMode {
        Exposure,
        Any,
        SinglePoint,
        DoublePoint
    }

    public boolean placeTrace(BlockPos pos) {
        if (!placeTrace.get()) {return true;}
        switch (placeMode.get()) {
            case SinglePoint -> {
                ((IRaycastContext) BODamageUtils.raycastContext).set(new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight.get(), pos.getZ() + 0.5), mc.player.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() == HitResult.Type.MISS;
            }
            case Exposure -> {
                return BODamageUtils.getExposure(mc.player.getEyePos(), OLEPOSSUtils.getBox(pos)) * 100 >= placeExposure.get();
            }
            case Any -> {
                return BODamageUtils.isExposed(mc.player.getEyePos(), OLEPOSSUtils.getBox(pos));
            }
            case DoublePoint -> {
                ((IRaycastContext) BODamageUtils.raycastContext).set(new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight1.get(), pos.getZ() + 0.5), mc.player.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                boolean hit1 = BODamageUtils.raycast(BODamageUtils.raycastContext).getType() == HitResult.Type.MISS;
                ((IRaycastContext) BODamageUtils.raycastContext).set(new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight2.get(), pos.getZ() + 0.5), mc.player.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() == HitResult.Type.MISS || hit1;
            }
        }
        return false;
    }

    public boolean attackTrace(Box box) {
        if (!attackTrace.get()) {return true;}
        switch (attackMode.get()) {
            case SinglePoint -> {
                ((IRaycastContext) BODamageUtils.raycastContext).set(new Vec3d((box.minX + box.maxX) / 2f, box.minY + attackHeight.get(), (box.minZ + box.maxZ) / 2f), mc.player.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() == HitResult.Type.MISS;
            }
            case Exposure -> {
                return BODamageUtils.getExposure(mc.player.getEyePos(), box) * 100 >= attackExposure.get();
            }
            case Any -> {
                return BODamageUtils.isExposed(mc.player.getEyePos(), box);
            }
            case DoublePoint -> {
                ((IRaycastContext) BODamageUtils.raycastContext).set(new Vec3d((box.minX + box.maxX) / 2f, box.minY + attackHeight1.get(), (box.minZ + box.maxZ) / 2f), mc.player.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                boolean hit1 = BODamageUtils.raycast(BODamageUtils.raycastContext).getType() == HitResult.Type.MISS;
                ((IRaycastContext) BODamageUtils.raycastContext).set(new Vec3d((box.minX + box.maxX) / 2f, box.minY + attackHeight2.get(), (box.minZ + box.maxZ) / 2f), mc.player.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() == HitResult.Type.MISS || hit1;
            }
        }
        return false;
    }
}
