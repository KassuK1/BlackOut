package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

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
        .build()
    );

    private boolean moving;
    private float yaw;
    private float pitch;
    private float p;
    private double velocity;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        switch (mode.get()) {
            case Wasp -> waspTick(event);
            case Control -> controlTick(event);
        }
    }

    // Wasp
    private void waspTick(PlayerMoveEvent event) {
        if (!mc.player.isFallFlying()) {return;}

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

    public enum Mode {
        Wasp,
        Control
    }
}
