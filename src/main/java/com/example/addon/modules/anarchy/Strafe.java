package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public class Strafe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Speed")
        .defaultValue(0.3)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> knockBack = sgGeneral.add(new BoolSetting.Builder()
        .name("Damage Boost")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> effect = sgGeneral.add(new DoubleSetting.Builder()
        .name("Effect Multi")
        .description("Speed multiplied each speed level")
        .defaultValue(0.2)
        .min(0)
        .sliderMax(1)
        .build()
    );
    private final Setting<Double> kbFactor = sgGeneral.add(new DoubleSetting.Builder()
        .name("Damage Boost Factor")
        .description("yes")
        .defaultValue(0.3)
        .min(0)
        .sliderMax(10)
        .visible(knockBack::get)
        .build()
    );
    private final Setting<Double> decreaseSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Velocity Decrease Speed")
        .description("yes")
        .defaultValue(1.2)
        .min(1)
        .sliderMax(10)
        .visible(knockBack::get)
        .build()
    );
    private boolean move = false;
    private double velocity;

    public Strafe() {super(BlackOut.ANARCHY, "Strafe", "Very ass module");}

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
                    velocity = Math.sqrt(x * x + z * z) * kbFactor.get();
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
            double velX = mc.player.getVelocity().x;
            double velZ = mc.player.getVelocity().z;
            double multiplier = speed.get() * (velocity + 1);
            velocity /= decreaseSpeed.get();
            if (velocity < 0.01) {
                velocity = 0;
            }
            if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                multiplier += (mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.2;
            }
            double forward = mc.player.input.movementForward;
            double sideways = mc.player.input.movementSideways;
            double yaw = getYaw(forward, sideways);
            double x = Math.cos(Math.toRadians(yaw + 90.0f));
            double y = mc.player.getVelocity().y;
            double z = Math.sin(Math.toRadians(yaw + 90.0f));

            if (move) {
                ((IVec3d) event.movement).set(multiplier * x, y, multiplier * z);
            } else {
                ((IVec3d) event.movement).set(0, y, 0);
            }
        }
    }

    private double getYaw(double f, double s) {
        double yaw = mc.player.getYaw();
        if (f > 0) {
            move = true;
            if (s > 0){
                yaw -= 45;
            } else if (s < 0) {
                yaw += 45;
            }
        } else if (f < 0) {
            move = true;
            if (s > 0){
                yaw -= 135;
            } else if (s < 0) {
                yaw += 135;
            } else {
                yaw += 180;
            }
        } else {
            move = true;
            if (s > 0){
                yaw -= 90;
            } else if (s < 0) {
                yaw += 90;
            } else {
                move = false;
            }
        }
        return yaw;
    }
}
