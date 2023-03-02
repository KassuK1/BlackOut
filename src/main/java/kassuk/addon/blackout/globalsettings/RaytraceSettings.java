package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.utils.BODamageUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.mixininterface.IRaycastContext;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/*
Made by OLEPOSSU
*/

public class RaytraceSettings extends BlackOutModule {
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
        SinglePoint,
        DoublePoint
    }
    Vec3d vec;

    public boolean placeTrace(BlockPos pos) {
        if (!placeTrace.get()) {return true;}
        switch (placeMode.get()) {
            case SinglePoint -> {
                return raytrace(mc.player.getEyePos(), new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight.get(), pos.getZ() + 0.5));
            }
            case DoublePoint -> {
                return raytrace(mc.player.getEyePos(), new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight1.get(), pos.getZ() + 0.5)) ||
                    raytrace(mc.player.getEyePos(), new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight2.get(), pos.getZ() + 0.5));
            }
        }
        return false;
    }

    public boolean attackTrace(Box box) {
        if (!attackTrace.get()) {return true;}
        switch (attackMode.get()) {
            case SinglePoint -> {
                ((IRaycastContext) BODamageUtils.raycastContext).set(new Vec3d((box.minX + box.maxX) / 2f, box.minY + attackHeight1.get(), (box.minZ + box.maxZ) / 2f), mc.player.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);

                return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() != HitResult.Type.BLOCK;
            }
            case DoublePoint -> {
                ((IRaycastContext) BODamageUtils.raycastContext).set(new Vec3d((box.minX + box.maxX) / 2f, box.minY + attackHeight1.get(), (box.minZ + box.maxZ) / 2f), mc.player.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                boolean hit1 = BODamageUtils.raycast(BODamageUtils.raycastContext).getType() != HitResult.Type.BLOCK;
                ((IRaycastContext) BODamageUtils.raycastContext).set(new Vec3d((box.minX + box.maxX) / 2f, box.minY + attackHeight2.get(), (box.minZ + box.maxZ) / 2f), mc.player.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() != HitResult.Type.BLOCK || hit1;
            }
        }
        return false;
    }

    boolean raytrace(Vec3d start, Vec3d end) {
        for (float i = 0; i <= 1; i += 0.01) {
            vec = new Vec3d(start.x + (end.x - start.x) * i, start.y + (end.y - start.y) * i, start.z + (end.z - start.z) * i);
            if (mc.world.getBlockState(new BlockPos(vec)).getBlock() != Blocks.AIR && !(mc.world.getBlockState(new BlockPos(vec)).getBlock() instanceof FluidBlock)) {
                return false;
            }
        }
        return true;
    }

}
