package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.mixins.IRaycastContext;
import kassuk.addon.blackout.utils.meteor.BODamageUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

/**
 * @author OLEPOSSU
 */

public class RaytraceSettings extends BlackOutModule {
    public RaytraceSettings() {
        super(BlackOut.SETTINGS, "Raytrace", "Global raytrace settings for every blackout module.");
    }

    private final SettingGroup sgPlace = settings.createGroup("Placing");
    private final SettingGroup sgAttack = settings.createGroup("Attacking");

    //--------------------Place-Settings--------------------//
    public final Setting<Boolean> placeTrace = sgPlace.add(new BoolSetting.Builder()
        .name("Place Traces")
        .description("Raytraces when placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<PlaceTraceMode> placeMode = sgPlace.add(new EnumSetting.Builder<PlaceTraceMode>()
        .name("Place Mode")
        .description("Place trace mode.")
        .defaultValue(PlaceTraceMode.SinglePoint)
        .visible(placeTrace::get)
        .build()
    );
    private final Setting<Double> placeHeight = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-2, 2)
        .visible(() -> placeMode.get() == PlaceTraceMode.SinglePoint && placeTrace.get())
        .build()
    );
    private final Setting<Double> placeHeight1 = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height 1")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.25)
        .sliderRange(-2, 1.5)
        .visible(() -> placeMode.get() == PlaceTraceMode.DoublePoint && placeTrace.get())
        .build()
    );
    private final Setting<Double> placeHeight2 = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height 2")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.75)
        .sliderRange(-2, 2)
        .visible(() -> placeMode.get() == PlaceTraceMode.DoublePoint && placeTrace.get())
        .build()
    );
    private final Setting<Double> exposure = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Exposure")
        .description("How many % of the block should be seen.")
        .defaultValue(50)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(() -> placeMode.get() == PlaceTraceMode.Exposure && placeTrace.get())
        .build()
    );

    //--------------------Place-Settings--------------------//
    public final Setting<Boolean> attackTrace = sgAttack.add(new BoolSetting.Builder()
        .name("Attack Traces")
        .description("Raytraces when attacking.")
        .defaultValue(false)
        .build()
    );
    private final Setting<AttackTraceMode> attackMode = sgAttack.add(new EnumSetting.Builder<AttackTraceMode>()
        .name("Attack Mode")
        .description("Attack trace mode.")
        .defaultValue(AttackTraceMode.SinglePoint)
        .visible(attackTrace::get)
        .build()
    );
    private final Setting<Double> attackHeight = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(1.5)
        .sliderRange(-2, 2)
        .visible(() -> attackMode.get().equals(AttackTraceMode.SinglePoint) && attackTrace.get())
        .build()
    );
    private final Setting<Double> attackHeight1 = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height 1")
        .description("Raytraces to x * hitbox height above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-2, 2)
        .visible(() -> attackMode.get().equals(AttackTraceMode.DoublePoint) && attackTrace.get())
        .build()
    );
    private final Setting<Double> attackHeight2 = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height 2")
        .description("Raytraces to x * hitbox height above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-2, 2)
        .visible(() -> attackMode.get().equals(AttackTraceMode.DoublePoint) && attackTrace.get())
        .build()
    );
    private final Setting<Double> attackExposure = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Exposure")
        .description("How many % of the entity should be seen.")
        .defaultValue(50)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(() -> placeMode.get() == PlaceTraceMode.Exposure && attackTrace.get())
        .build()
    );

    public enum PlaceTraceMode {
        SinglePoint,
        DoublePoint,
        Sides,
        Exposure,
        Any
    }

    public enum AttackTraceMode {
        SinglePoint,
        DoublePoint,
        Exposure,
        Any
    }

    private final Vec3d vec = new Vec3d(0, 0, 0);
    public RaycastContext raycastContext;
    public BlockHitResult result;
    public int hit = 0;

    public boolean placeTrace(BlockPos pos) {

        if (!placeTrace.get()) {
            return true;
        }

        updateContext();

        switch (placeMode.get()) {
            case SinglePoint -> {
                ((IRaycastContext) raycastContext).setEnd(new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight.get(), pos.getZ() + 0.5));

                result = BODamageUtils.raycast(raycastContext);
                return result.getBlockPos().equals(pos);
            }
            case DoublePoint -> {
                ((IRaycastContext) raycastContext).setEnd(new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight1.get(), pos.getZ() + 0.5));

                result = BODamageUtils.raycast(raycastContext);
                if (result.getBlockPos().equals(pos)) {
                    return true;
                }

                ((IRaycastContext) raycastContext).setEnd(new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight2.get(), pos.getZ() + 0.5));

                result = BODamageUtils.raycast(raycastContext);
                return result.getBlockPos().equals(pos);
            }
            case Sides -> {
                ((IVec3d) vec).set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                for (Direction dir : Direction.values()) {
                    ((IRaycastContext) raycastContext).setEnd(vec.add(dir.getOffsetX() / 2f, dir.getOffsetY() / 2f, dir.getOffsetZ() / 2f));

                    result = BODamageUtils.raycast(raycastContext);
                    if (result.getBlockPos().equals(pos)) {
                        return true;
                    }
                }
            }
            case Exposure -> {
                ((IVec3d) vec).set(pos.getX(), pos.getY(), pos.getZ());

                hit = 0;
                for (int x = 0; x <= 2; x += 1) {
                    for (int y = 0; y <= 2; y += 1) {
                        for (int z = 0; z <= 2; z += 1) {
                            ((IRaycastContext) raycastContext).setEnd(vec.add(0.1 + x * 0.4, 0.1 + y * 0.4, 0.1 + z * 0.4));

                            result = BODamageUtils.raycast(raycastContext);
                            if (result.getBlockPos().equals(pos)) {
                                hit++;
                                if (hit >= exposure.get() / 100 * 27) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            case Any -> {
                ((IVec3d) vec).set(pos.getX(), pos.getY(), pos.getZ());

                hit = 0;
                for (int x = 0; x <= 2; x += 1) {
                    for (int y = 0; y <= 2; y += 1) {
                        for (int z = 0; z <= 2; z += 1) {
                            ((IRaycastContext) raycastContext).setEnd(vec.add(0.1 + x * 0.4, 0.1 + y * 0.4, 0.1 + z * 0.4));

                            result = BODamageUtils.raycast(raycastContext);
                            if (result.getBlockPos().equals(pos)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean attackTrace(Box box) {
        if (!attackTrace.get()) {
            return true;
        }

        updateContext();

        switch (attackMode.get()) {
            case SinglePoint -> {
                ((meteordevelopment.meteorclient.mixininterface.IRaycastContext) BODamageUtils.raycastContext).set(mc.player.getEyePos(), new Vec3d((box.minX + box.maxX) / 2f, box.minY + attackHeight.get(), (box.minZ + box.maxZ) / 2f), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);

                return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() != HitResult.Type.BLOCK;
            }
            case DoublePoint -> {
                ((meteordevelopment.meteorclient.mixininterface.IRaycastContext) BODamageUtils.raycastContext).set(mc.player.getEyePos(), new Vec3d((box.minX + box.maxX) / 2f, box.minY + attackHeight1.get(), (box.minZ + box.maxZ) / 2f), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                if (BODamageUtils.raycast(BODamageUtils.raycastContext).getType() != HitResult.Type.BLOCK) {
                    return true;
                }

                ((meteordevelopment.meteorclient.mixininterface.IRaycastContext) BODamageUtils.raycastContext).set(mc.player.getEyePos(), new Vec3d((box.minX + box.maxX) / 2f, box.minY + attackHeight2.get(), (box.minZ + box.maxZ) / 2f), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() != HitResult.Type.BLOCK;
            }
            case Exposure -> {
                ((IVec3d) vec).set(box.minX, box.minY, box.minZ);
                double xw = box.maxX - box.minX;
                double yh = box.maxY - box.minY;
                double zw = box.maxZ - box.minZ;

                hit = 0;
                for (int x = 0; x <= 2; x += 1) {
                    for (int y = 0; y <= 2; y += 1) {
                        for (int z = 0; z <= 2; z += 1) {
                            ((IRaycastContext) raycastContext).setEnd(vec.add(MathHelper.lerp(x / 2f, 0.1, xw - 0.1), MathHelper.lerp(y / 2f, 0.0, yh - 0.1), MathHelper.lerp(z / 2f, 0.1, zw - 0.1)));

                            result = BODamageUtils.raycast(raycastContext);
                            if (result.getType() != HitResult.Type.BLOCK) {
                                hit++;
                                if (hit >= attackExposure.get() / 100 * 27) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            case Any -> {
                ((IVec3d) vec).set(box.minX, box.minY, box.minZ);
                double xw = box.maxX - box.minX;
                double yh = box.maxY - box.minY;
                double zw = box.maxZ - box.minZ;

                for (int x = 0; x <= 2; x += 1) {
                    for (int y = 0; y <= 2; y += 1) {
                        for (int z = 0; z <= 2; z += 1) {
                            ((IRaycastContext) raycastContext).setEnd(vec.add(MathHelper.lerp(x / 2f, 0.1, xw - 0.1), MathHelper.lerp(y / 2f, 0.0, yh - 0.1), MathHelper.lerp(z / 2f, 0.1, zw - 0.1)));

                            result = BODamageUtils.raycast(raycastContext);
                            if (result.getType() != HitResult.Type.BLOCK) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void updateContext() {
        if (raycastContext == null) {
            raycastContext = new RaycastContext(mc.player.getEyePos(), null, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player);
        } else {
            ((IRaycastContext) raycastContext).setStart(mc.player.getEyePos());
        }
    }
}
