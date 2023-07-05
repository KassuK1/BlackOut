package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * @author OLEPOSSU
 */

public class StepPlus extends BlackOutModule {
    public StepPlus() {
        super(BlackOut.BLACKOUT, "Step+", "Step but works.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> slow = sgGeneral.add(new BoolSetting.Builder()
        .name("Slow")
        .description("Moves up slowly to prevent lagbacks.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> strict = sgGeneral.add(new BoolSetting.Builder()
        .name("Strict")
        .description("Strict 2b2tpvp step.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("Height")
        .description("Starts stepping if target block can be reached in x movement ticks.")
        .defaultValue(2.5)
        .min(0.6)
        .sliderRange(0.6, 2.5)
        .visible(() -> !strict.get())
        .build()
    );
    public final Setting<Double> cooldown = sgGeneral.add(new DoubleSetting.Builder()
        .name("Cooldown")
        .description("Waits x seconds between steps.")
        .defaultValue(0.25)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    public boolean stepping = false;

    double targetY = 0;

    public int index = 0;
    public double[] currentOffsets = null;
    public long lastStep = 0;

    public void onActivate() {
        index = 0;
        stepping = false;
        currentOffsets = null;
        targetY = 0;
    }

    public void slowStep(Entity entity, Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        Box box = entity.getBoundingBox();
        List<VoxelShape> list = entity.getWorld().getEntityCollisions(entity, box.stretch(movement));
        Vec3d vec3d = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(entity, movement, box, entity.getWorld(), list);

        if ((movement.x != vec3d.x || movement.z != vec3d.z) || stepping) {
            if (entity.isOnGround() && !stepping && System.currentTimeMillis() - lastStep > cooldown.get() * 1000) {
                Vec3d vec3d2 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, height.get(), movement.z), box, entity.getWorld(), list);
                Vec3d vec3d3 = Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, height.get(), 0.0), box.stretch(movement.x, 0.0, movement.z), entity.getWorld(), list);
                if (vec3d3.y < height.get()) {
                    Vec3d vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, 0.0, movement.z), box.offset(vec3d3), entity.getWorld(), list).add(vec3d3);
                    if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                        vec3d2 = vec3d4;
                    }
                }

                if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                    Vec3d vec = vec3d2.add(Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, -vec3d2.y + movement.y, 0.0), box.offset(vec3d2), entity.getWorld(), list));

                    double[] o = getOffsets(vec.y);

                    if (o != null) {
                        lastStep = System.currentTimeMillis();
                        currentOffsets = o;
                        targetY = mc.player.getY() + vec.y;
                        stepping = true;
                        index = -1;
                    }
                }
            }

            if (stepping && currentOffsets != null) {
                index++;
                double offset = 0;
                if (index < currentOffsets.length) {
                    offset = currentOffsets[index];
                }

                if (index >= currentOffsets.length) {
                    if (!strict.get()) {
                        offset = targetY - mc.player.getY();
                    }
                    stepping = false;
                }

                Vec3d vec3d4;
                if (!strict.get() || index > 1) {
                    Vec3d vec3d3 = Entity.adjustMovementForCollisions(entity, new Vec3d(0, offset, 0), box.stretch(0, 0.0, 0), entity.getWorld(), list);
                    vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, 0.0, movement.z), box.offset(vec3d3), entity.getWorld(), list).add(vec3d3);
                } else {
                    vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(0, offset, 0), box.stretch(0, 0.0, 0), entity.getWorld(), list);
                }

                cir.setReturnValue(vec3d4);
                return;
            }
        }

        cir.setReturnValue(vec3d);
    }

    public double[] getOffsets(double step) {
        if (strict.get()) {
            if (step > 0.6 && step <= 1.000001) {
                // by me (OLEPOSSU)
                return new double[]{0.424, 0.33712, 0.25197759999999997};
            }
            return null;
        }

        // By Doogie13
        if (step > 2.019) {
            return new double[]{0.425, 0.39599999999999996, -0.122, -0.09999999999999998, 0.42300000000000004, 0.3500000000000001, 0.2799999999999998, 0.21700000000000008, 0.15000000000000013, -0.10000000000000009};
        } else if (step > 1.5) {
            return new double[]{0.42, 0.36000000000000004, -0.15000000000000002, -0.12, 0.39, 0.30999999999999994, 0.24, -0.020000000000000018};
        } else if (step > 1.015) {
            return new double[]{0.42, 0.3332, 0.25680000000000003, 0.08299999999999996, -0.07800000000000007};
        } else if (step > 0.6) {
            return new double[]{0.42, 0.3332};
        }
        return null;
    }

    public void step(double[] offsets) {
        if (offsets == null) return;
        double offset = 0;
        for (double v : offsets) {
            offset += v;
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + offset, mc.player.getZ(), false));
        }
        lastStep = System.currentTimeMillis();
    }

    private boolean i(Box b) {
        return OLEPOSSUtils.inside(mc.player, b);
    }
}
