package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class SpeedPlus extends BlackOutModule {
    public SpeedPlus() {super(BlackOut.BLACKOUT, "Speed+", "Speeeeeeeed");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final Setting<SpeedMode> mode = sgGeneral.add(new EnumSetting.Builder<SpeedMode>()
        .name("Mode")
        .description("How many blocks to move every movement tick")
        .defaultValue(SpeedMode.Strafe)
        .build()
    );
    private final Setting<Double> acceleration = sgGeneral.add(new DoubleSetting.Builder()
        .name("Acceleration")
        .description("How many blocks to move every movement tick")
        .defaultValue(0.3)
        .min(0)
        .sliderMax(10)
        .visible(() -> mode.get() == SpeedMode.Accelerate)
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("How many blocks to move every movement tick")
        .defaultValue(0.2873)
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
    private final Setting<Double> decreaseSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Velocity Decrease Speed")
        .description("How fast should the velocity end")
        .defaultValue(2)
        .min(0)
        .sliderMax(10)
        .visible(knockBack::get)
        .build()
    );

    //  Pause Page
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
    private final Setting<LiquidMode> pauseWater = sgPause.add(new EnumSetting.Builder<LiquidMode>()
        .name("Pause Water")
        .description(".")
        .defaultValue(LiquidMode.Submerged)
        .build()
    );
    private final Setting<LiquidMode> pauseLava = sgPause.add(new EnumSetting.Builder<LiquidMode>()
        .name("Pause Lava")
        .description(".")
        .defaultValue(LiquidMode.Both)
        .build()
    );
    public enum SpeedMode {
        Strafe,
        Instant,
        Accelerate
    }
    public enum LiquidMode {
        Disabled,
        Submerged,
        Touching,
        Both
    }
    boolean move = false;
    public double velocity;
    double lastX = 0;
    double lastZ = 0;

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler
    private void onKB(PacketEvent.Receive event) {
        if (mc.player != null && mc.world != null) {
            if (knockBack.get() && event.packet instanceof EntityVelocityUpdateS2CPacket) {
                EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket) event.packet;
                if (packet.getId() == mc.player.getId()) {
                    double x = packet.getVelocityX() / 8000f;
                    double z = packet.getVelocityZ() / 8000f;
                    velocity = Math.max(velocity, Math.sqrt(x * x + z * z) * kbFactor.get());
                }
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

            switch (pauseWater.get()) {
                case Touching -> {
                    if (mc.player.isTouchingWater()) {
                        return;
                    }
                }
                case Submerged -> {
                    if (mc.player.isSubmergedIn(FluidTags.WATER)) {
                        return;
                    }
                }
                case Both -> {
                    if (mc.player.isTouchingWater() || mc.player.isSubmergedIn(FluidTags.WATER)) {
                        return;
                    }
                }
            }

            switch (pauseLava.get()) {
                case Touching -> {
                    if (mc.player.isInLava()) {
                        return;
                    }
                }
                case Submerged -> {
                    if (mc.player.isSubmergedIn(FluidTags.LAVA)) {
                        return;
                    }
                }
                case Both -> {
                    if (mc.player.isInLava() || mc.player.isSubmergedIn(FluidTags.LAVA)) {
                        return;
                    }
                }
            }

            velocity = Math.max(speed.get(), velocity * (1 - decreaseSpeed.get() * 0.025));
            double motion = velocity;
            if (velocity < 0.01) {
                motion = 0;
            }
            if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                motion *= 1.2 + mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.2;
            }

            double forward = mc.player.input.movementForward;
            double sideways = mc.player.input.movementSideways;
            double yaw = getYaw(forward, sideways);
            double x = Math.cos(Math.toRadians(yaw + 90.0f));
            double y = mc.player.getVelocity().y;
            double z = Math.sin(Math.toRadians(yaw + 90.0f));

            switch (mode.get()) {
                case Strafe, Instant -> {
                    if (move) {
                        ((IVec3d) event.movement).set(motion * x, y, motion * z);
                    } else {
                        ((IVec3d) event.movement).set(0, y, 0);
                    }
                }
                case Accelerate -> {
                    double tx = move ? x : 0;
                    double tz = move ? z : 0;

                    lastX = smooth(lastX, tx, acceleration.get()) * slipperiness(move);
                    lastZ = smooth(lastZ, tz, acceleration.get()) * slipperiness(move);

                    Vec2f m = limit(lastX, lastZ, speed.get());
                    lastX = m.x;
                    lastZ = m.y;
                    ((IVec3d) event.movement).setXZ(lastX, lastZ);
                }
            }
        }
    }

    double slipperiness(boolean moving) {
        if (moving) {
            return 1;
        }
        return mc.player.isOnGround() ? mc.world.getBlockState(new BlockPos(mc.player.getX(), Math.ceil(mc.player.getY() - 1), mc.player.getZ())).getBlock().getSlipperiness() : 0.98;
    }

    double getYaw(double f, double s) {
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

    Vec2f limit(double x, double z, double speed) {
        double s = Math.sqrt(x * x + z * z) / speed;

        return new Vec2f((float) (x / s), (float) (z / s));
    }

    double smooth(double current, double target, double ac) {
        return current + (target - current) * ac / 20f;
    }
}
