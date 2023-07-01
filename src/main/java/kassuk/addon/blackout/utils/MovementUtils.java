package kassuk.addon.blackout.utils;

import meteordevelopment.meteorclient.mixininterface.IVec3d;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MovementUtils {
    public static double xMovement(double speed, double yaw) {
        return Math.cos(Math.toRadians(yaw + 90)) * speed;
    }
    public static double zMovement(double speed, double yaw) {
        return Math.sin(Math.toRadians(yaw + 90)) * speed;
    }
    public static double getSpeed(double baseSpeed) {
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            baseSpeed *= 1.2 + mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.2;
        }
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            baseSpeed /= 1.2 + mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() * 0.2;
        }
        if (mc.player.isSneaking()) {
            baseSpeed *= 0.3;
        }
        return baseSpeed;
    }
    public static void moveTowards(Vec3d movement, double baseSpeed, Vec3d vec, int step, int reverseStep) {
        double speed = getSpeed(baseSpeed);

        double yaw = RotationUtils.getYaw(mc.player.getPos(), vec);

        double xm = xMovement(speed, yaw);
        double zm = zMovement(speed, yaw);

        double xd = vec.x - mc.player.getX();
        double zd = vec.z - mc.player.getZ();

        double x = Math.abs(xm) <= Math.abs(xd) ? xm : xd;
        double z = Math.abs(zm) <= Math.abs(zd) ? zm : zd;

        y(movement, x, z, step, reverseStep);

        ((IVec3d) movement).setXZ(x, z);
    }

    private static void y(Vec3d movement, double x, double z, int step, int rev) {
        // Step
        if (mc.player.isOnGround() &&
            !OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox()) &&
            OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(x, 0, z))) {

            double s = getStep(mc.player.getBoundingBox().offset(x, 0, z), step);

            if (s > 0) {
                ((IVec3d) movement).setY(s);
                mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
            }
            return;
        }

        // Reverse
        if (mc.player.isOnGround() &&
            !OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(x, -0.04, z))) {

            double s = getReverse(mc.player.getBoundingBox(), rev);

            if (s > 0) {
                ((IVec3d) movement).setY(-s);
                mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
            }
        }
    }

    private static double getStep(Box box, int step) {
        for (double i = 0; i <= step + 0.125; i += 0.125) {
            if (!OLEPOSSUtils.inside(mc.player, box.offset(0, i, 0))) {
                return i;
            }
        }
        return 0;
    }
    private static double getReverse(Box box, int reverse) {
        for (double i = 0; i <= reverse; i += 0.125) {
            if (OLEPOSSUtils.inside(mc.player, box.offset(0, -i - 0.125, 0))) {
                return i;
            }
        }
        return 0;
    }
}
