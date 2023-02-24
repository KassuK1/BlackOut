package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public class SpeedPlus extends BlackOutModule {
    public SpeedPlus() {super(BlackOut.BLACKOUT, "Speed+", "Speeeeeeeed");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPause = settings.createGroup("Pause");
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
    private final Setting<Boolean> pauseSneak = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Sneak")
        .description("Doesn't modify movement while sneaking.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseElytra = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Elytra")
        .description("Doesn't modify movement while flying with elytra.")
        .defaultValue(true)
        .build()
    );
    public enum SpeedMode {
        Strafe,
        Instant,
        Accelerate
    }
    boolean move = false;
    public double velocity;

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

            if (move) {
                ((IVec3d) event.movement).set(motion * x, y, motion * z);
            } else {
                ((IVec3d) event.movement).set(0, y, 0);
            }
        }
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
}
