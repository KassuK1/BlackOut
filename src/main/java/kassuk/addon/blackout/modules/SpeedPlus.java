package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;

/**
 * @author OLEPOSSU
 */

public class SpeedPlus extends BlackOutModule {
    public SpeedPlus() {
        super(BlackOut.BLACKOUT, "Speed+", "Speeeeeeeed.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPause = settings.createGroup("Pause");

    //--------------------General--------------------//
    private final Setting<SpeedMode> mode = sgGeneral.add(new EnumSetting.Builder<SpeedMode>()
        .name("Mode")
        .description("Mode for speed.")
        .defaultValue(SpeedMode.Instant)
        .build()
    );
    private final Setting<Double> accelerationAmount = sgGeneral.add(new DoubleSetting.Builder()
        .name("Acceleration")
        .description("How much should the speed increase every movement tick.")
        .defaultValue(0.3)
        .min(0)
        .sliderMax(10)
        .visible(() -> mode.get() == SpeedMode.Accelerate)
        .build()
    );
    private final Setting<Boolean> rbReset = sgGeneral.add(new BoolSetting.Builder()
        .name("Reset On Rubberband")
        .description("Resets speed when rubberbanding.")
        .defaultValue(false)
        .visible(() -> mode.get() == SpeedMode.Accelerate)
        .build()
    );
    private final Setting<Boolean> airStrafe = sgGeneral.add(new BoolSetting.Builder()
        .name("Air Strafe")
        .description("Lets you move fast in air too.")
        .defaultValue(false)
        .visible(() -> mode.get() == SpeedMode.Accelerate)
        .build()
    );
    private final Setting<Boolean> onlyPressed = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Pressed")
        .description("Uses instant mode when you arent pressing jump key.")
        .defaultValue(false)
        .visible(() -> mode.get() == SpeedMode.CCStrafe)
        .build()
    );
    private final Setting<Keybind> strafeBind = sgGeneral.add(new KeybindSetting.Builder()
        .name("Strafe Bind")
        .description("Strafes when this key is pressed.")
        .defaultValue(Keybind.fromKey(-1))
        .visible(() -> mode.get() == SpeedMode.CCStrafe && onlyPressed.get())
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("How many blocks to move every movement tick")
        .defaultValue(0.287)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> knockBack = sgGeneral.add(new BoolSetting.Builder()
        .name("Damage Boost")
        .description("Turns knockback into velocity.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> kbFactor = sgGeneral.add(new DoubleSetting.Builder()
        .name("Damage Boost Factor")
        .description("Knockback multiplier")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .visible(knockBack::get)
        .build()
    );

    //--------------------Pause--------------------//
    private final Setting<Boolean> pauseSneak = sgPause.add(new BoolSetting.Builder()
        .name("Pause Sneak")
        .description("Doesn't modify movement while sneaking.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseElytra = sgPause.add(new BoolSetting.Builder()
        .name("Pause Elytra")
        .description("Doesn't modify movement while flying with elytra.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pauseFly = sgPause.add(new BoolSetting.Builder()
        .name("Pause Fly")
        .description("Doesn't modify movement while flying.")
        .defaultValue(true)
        .build()
    );
    private final Setting<LiquidMode> pauseWater = sgPause.add(new EnumSetting.Builder<LiquidMode>()
        .name("Pause Water")
        .description("Doesn't modify movement when in water.")
        .defaultValue(LiquidMode.Submerged)
        .build()
    );
    private final Setting<LiquidMode> pauseLava = sgPause.add(new EnumSetting.Builder<LiquidMode>()
        .name("Pause Lava")
        .description("Doesn't modify movement when in lava.")
        .defaultValue(LiquidMode.Both)
        .build()
    );

    private boolean move = false;
    public double velocity;
    private double acceleration = 0;
    private double ax = 0;
    private double az = 0;
    private int jumpPhase = 1;

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler
    private void onKB(PacketEvent.Receive event) {
        if (mc.player != null && mc.world != null) {
            if (knockBack.get() && event.packet instanceof EntityVelocityUpdateS2CPacket packet && packet.getEntityId() == mc.player.getId()) {
                double x = packet.getVelocityX() / 8000f;
                double z = packet.getVelocityZ() / 8000f;
                velocity = Math.max(velocity, Math.sqrt(x * x + z * z) * kbFactor.get());
            }
            if (rbReset.get() && event.packet instanceof PlayerPositionLookS2CPacket) {
                acceleration = 0;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (mc.player != null && mc.world != null) {
            if (Modules.get().get(HoleSnap.class).isActive()) {
                return;
            }
            if (pauseSneak.get() && mc.player.isSneaking()) {
                return;
            }
            if (pauseElytra.get() && mc.player.isFallFlying()) {
                return;
            }
            if (pauseFly.get() && mc.player.getAbilities().flying) {
                return;
            }

            switch (pauseWater.get()) {
                case Touching -> {
                    if (mc.player.isTouchingWater()) return;
                }
                case Submerged -> {
                    if (mc.player.isSubmergedIn(FluidTags.WATER)) return;
                }
                case Both -> {
                    if (mc.player.isTouchingWater() || mc.player.isSubmergedIn(FluidTags.WATER)) return;
                }
            }

            switch (pauseLava.get()) {
                case Touching -> {
                    if (mc.player.isInLava()) return;
                }
                case Submerged -> {
                    if (mc.player.isSubmergedIn(FluidTags.LAVA)) return;
                }
                case Both -> {
                    if (mc.player.isInLava() || mc.player.isSubmergedIn(FluidTags.LAVA)) return;
                }
            }

            double forward = mc.player.input.movementForward;
            double sideways = mc.player.input.movementSideways;

            double yaw = getYaw(forward, sideways);

            if (mode.get() == SpeedMode.CCStrafe && (!onlyPressed.get() || strafeBind.get().isPressed())) {
                if (jumpPhase == 4) {
                    velocity *= 0.9888888889;

                    if (mc.player.isOnGround()) {
                        jumpPhase = 1;
                    }
                }
                if (jumpPhase == 3) {
                    velocity = velocity + (0.2873 - velocity) * 0.6;
                    jumpPhase = 4;
                }
                if (jumpPhase == 2) {
                    ((IVec3d) event.movement).setY(0.4);
                    velocity *= 1.85;
                    jumpPhase = 3;
                }
                if (jumpPhase == 1) {
                    if (mc.player.isOnGround() && move) {
                        velocity = 0.2873;
                        jumpPhase = 2;
                    }
                }

                velocity = Math.max(velocity, 0.2873);
            } else
                velocity = Math.max(speed.get(), velocity * 0.98);

            double motion = velocity;
            if (velocity < 0.01) {
                motion = 0;
            }
            if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                motion *= 1.2 + mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.2;
            }
            if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                motion /= 1.2 + mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() * 0.2;
            }

            double x = Math.cos(Math.toRadians(yaw + 90.0f));
            double y = mc.player.getVelocity().getY();
            double z = Math.sin(Math.toRadians(yaw + 90.0f));

            switch (mode.get()) {
                case CCStrafe, Instant -> {
                    if (move) {
                        ((IVec3d) event.movement).set(motion * x, y, motion * z);
                    } else {
                        ((IVec3d) event.movement).set(0, y, 0);
                    }
                }
                case Accelerate -> {
                    acceleration = Math.min(1, (move ? acceleration + (mc.player.isOnGround() || airStrafe.get() ? accelerationAmount.get() / 10 : 0.02) : acceleration) * slipperiness(move));

                    if ((move && mc.player.isOnGround()) || airStrafe.get()) {
                        ax = x;
                        az = z;
                    }

                    ((IVec3d) event.movement).setXZ(speed.get() * ax * acceleration, speed.get() * az * acceleration);
                }
            }
        }
    }

    private double slipperiness(boolean moving) {
        if (moving) {
            return 1;
        }
        return mc.player.isOnGround() ? mc.world.getBlockState(new BlockPos((int) mc.player.getX(), (int) Math.ceil(mc.player.getY() - 1), (int) mc.player.getZ())).getBlock().getSlipperiness() : 0.98;
    }

    private double getYaw(double f, double s) {
        double yaw = mc.player.getYaw();
        if (f > 0) {
            move = true;
            yaw += s > 0 ? -45 : s < 0 ? 45 : 0;
        } else if (f < 0) {
            move = true;
            yaw += s > 0 ? -135 : s < 0 ? 135 : 180;
        } else {
            move = s != 0;
            yaw += s > 0 ? -90 : s < 0 ? 90 : 0;
        }
        return yaw;
    }

    public enum SpeedMode {
        CCStrafe,
        Instant,
        Accelerate
    }

    public enum LiquidMode {
        Disabled,
        Submerged,
        Touching,
        Both
    }
}
