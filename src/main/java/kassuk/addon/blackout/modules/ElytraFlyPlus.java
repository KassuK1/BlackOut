package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.managers.Managers;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * @author OLEPOSSU
 */

public class ElytraFlyPlus extends BlackOutModule {
    public ElytraFlyPlus() {
        super(BlackOut.BLACKOUT, "Elytra Fly+", "Better efly.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Speed");

    //--------------------General--------------------//
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description(".")
        .defaultValue(Mode.Wasp)
        .build()
    );

    private final Setting<Boolean> stopWater = sgGeneral.add(new BoolSetting.Builder()
        .name("Stop Water")
        .description("Doesn't modify movement while in water.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> stopLava = sgGeneral.add(new BoolSetting.Builder()
        .name("Stop Lava")
        .description("Doesn't modify movement while in lava.")
        .defaultValue(true)
        .build()
    );

    //--------------------Speed--------------------//
    private final Setting<Double> horizontal = sgSpeed.add(new DoubleSetting.Builder()
        .name("Horizontal Speed")
        .description("How many blocks to move each tick horizontally.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 5)
        .visible(() -> mode.get() == Mode.Wasp)
        .build()
    );

    private final Setting<Double> up = sgSpeed.add(new DoubleSetting.Builder()
        .name("Up Speed")
        .description("How many blocks to move up each tick.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 5)
        .visible(() -> mode.get() == Mode.Wasp)
        .build()
    );

    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("How many blocks to move each tick.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 5)
        .visible(() -> mode.get() == Mode.Control)
        .build()
    );

    private final Setting<Double> upMultiplier = sgSpeed.add(new DoubleSetting.Builder()
        .name("Up Multiplier")
        .description("How many times faster should we fly up.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 5)
        .visible(() -> mode.get() == Mode.Control)
        .build()
    );

    private final Setting<Double> down = sgSpeed.add(new DoubleSetting.Builder()
        .name("Down Speed")
        .description("How many blocks to move down each tick.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 5)
        .visible(() -> mode.get() == Mode.Control || mode.get() == Mode.Wasp)
        .build()
    );

    private final Setting<Boolean> smartFall = sgSpeed.add(new BoolSetting.Builder()
        .name("Smart Fall")
        .description("Only falls down when looking down.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Wasp)
        .build()
    );

    private final Setting<Double> fallSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Fall Speed")
        .description("How many blocks to fall down each tick.")
        .defaultValue(0.01)
        .min(0)
        .sliderRange(0, 1)
        .visible(() -> mode.get() == Mode.Control || mode.get() == Mode.Wasp)
        .build()
    );

    private final Setting<Double> constSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Const Speed")
        .description("Maximum speed for constantiam mode.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 5)
        .visible(() -> mode.get() == Mode.Constantiam)
        .build()
    );

    private final Setting<Double> constAcceleration = sgSpeed.add(new DoubleSetting.Builder()
        .name("Const Acceleration")
        .description("Maximum speed for constantiam mode.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 5)
        .visible(() -> mode.get() == Mode.Constantiam)
        .build()
    );

    private final Setting<Boolean> constStop = sgSpeed.add(new BoolSetting.Builder()
        .name("Const Stop")
        .description("Stops movement when no input.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Constantiam)
        .build()
    );

    private boolean moving;
    private float yaw;
    private float pitch;
    private float p;
    private double velocity;
    private int activeFor;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        if (!active()) return;

        activeFor++;
        if (activeFor < 5) return;

        switch (mode.get()) {
            case Wasp -> waspTick(event);
            case Control -> controlTick(event);
            case Constantiam -> constantiamTick(event);
        }
    }

    private void constantiamTick(PlayerMoveEvent event) {
        Vec3d motion = getMotion(mc.player.getVelocity());
        if (motion != null) {
            ((IVec3d) event.movement).set(motion.getX(), motion.getY(), motion.getZ());
            event.movement = motion;
        }
    }

    private Vec3d getMotion(Vec3d velocity) {
        if (mc.player.input.movementForward == 0) {
            if (constStop.get()) return new Vec3d(0, 0, 0);
            return null;
        }

        boolean forward = mc.player.input.movementForward > 0;

        double yaw = Math.toRadians(mc.player.getYaw() + (forward ? 90 : -90));

        double x = Math.cos(yaw);
        double z = Math.sin(yaw);
        double maxAcc = calcAcceleration(velocity.x, velocity.z, x, z);
        double delta = Math.clamp(MathHelper.getLerpProgress(velocity.horizontalLength(), 0, 0.5), 0, 1);

        double acc = Math.min(maxAcc, constAcceleration.get() / 20 * (0.1 + delta * 0.9));
        return new Vec3d(velocity.getX() + x * acc, velocity.getY(), velocity.getZ() + z * acc);
    }

    private double calcAcceleration(double vx, double vz, double x, double z) {
        // crazy mathing
        double xz = x * x + z * z;
        return (Math.sqrt(xz * constSpeed.get() * constSpeed.get() - x * x * vz * vz - z * z * vx * vx + 2 * x * z * vx * vz) - x * vx - z * vz) / xz;
    }

    // Wasp
    private void waspTick(PlayerMoveEvent event) {
        if (!mc.player.isFallFlying()) return;

        updateWaspMovement();
        pitch = mc.player.getPitch();

        double cos = Math.cos(Math.toRadians(yaw + 90));
        double sin = Math.sin(Math.toRadians(yaw + 90));

        double x = moving ? cos * horizontal.get() : 0;
        double y = -fallSpeed.get();
        double z = moving ? sin * horizontal.get() : 0;

        if (smartFall.get()) {
            y *= Math.abs(Math.sin(Math.toRadians(pitch)));
        }

        if (mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed()) {
            y = -down.get();
        }
        if (!mc.options.sneakKey.isPressed() && mc.options.jumpKey.isPressed()) {
            y = up.get();
        }

        ((IVec3d) event.movement).set(x, y, z);
        mc.player.setVelocity(0, 0, 0);
    }

    private void updateWaspMovement() {
        float yaw = mc.player.getYaw();

        float f = mc.player.input.movementForward;
        float s = mc.player.input.movementSideways;

        if (f > 0) {
            moving = true;
            yaw += s > 0 ? -45 : s < 0 ? 45 : 0;
        } else if (f < 0) {
            moving = true;
            yaw += s > 0 ? -135 : s < 0 ? 135 : 180;
        } else {
            moving = s != 0;
            yaw += s > 0 ? -90 : s < 0 ? 90 : 0;
        }
        this.yaw = yaw;
    }

    // Pitch
    private void controlTick(PlayerMoveEvent event) {
        if (!mc.player.isFallFlying()) {return;}

        updateControlMovement();
        pitch = 0;

        boolean movingUp = false;

        if (!mc.options.sneakKey.isPressed() && mc.options.jumpKey.isPressed() && velocity > speed.get() * 0.4) {
            p = (float) Math.min(p + 0.1 * (1 - p) * (1 - p) * (1 - p), 1f);

            pitch = Math.max(Math.max(p, 0) * -90, -90);

            movingUp = true;
            moving = false;
        } else {
            velocity = speed.get();
            p = -0.2f;
        }

        velocity = moving ? speed.get() : Math.min(velocity + Math.sin(Math.toRadians(pitch)) * 0.08, speed.get());

        double cos = Math.cos(Math.toRadians(yaw + 90));
        double sin = Math.sin(Math.toRadians(yaw + 90));

        double x = moving && !movingUp ? cos * speed.get() : movingUp ? velocity * Math.cos(Math.toRadians(pitch)) * cos : 0;
        double y = pitch < 0 ? velocity * upMultiplier.get() * -Math.sin(Math.toRadians(pitch)) * velocity : -fallSpeed.get();
        double z = moving && !movingUp ? sin * speed.get() : movingUp ? velocity * Math.cos(Math.toRadians(pitch)) * sin : 0;

        y *= Math.abs(Math.sin(Math.toRadians(movingUp ? pitch : mc.player.getPitch())));

        if (mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed()) {
            y = -down.get();
        }

        ((IVec3d) event.movement).set(x, y, z);
        mc.player.setVelocity(0, 0, 0);
    }

    private void updateControlMovement() {
        float yaw = mc.player.getYaw();

        float f = mc.player.input.movementForward;
        float s = mc.player.input.movementSideways;

        if (f > 0) {
            moving = true;
            yaw += s > 0 ? -45 : s < 0 ? 45 : 0;
        } else if (f < 0) {
            moving = true;
            yaw += s > 0 ? -135 : s < 0 ? 135 : 180;
        } else {
            moving = s != 0;
            yaw += s > 0 ? -90 : s < 0 ? 90 : 0;
        }
        this.yaw = yaw;
    }

    public boolean active() {
        if (stopWater.get() && mc.player.isTouchingWater()) {
            activeFor = 0;
            return false;
        }
        if (stopLava.get() && mc.player.isInLava()) {
            activeFor = 0;
            return false;
        }
        return mc.player.isFallFlying();
    }

    public enum Mode {
        Wasp,
        Control,
        Constantiam
    }
}
