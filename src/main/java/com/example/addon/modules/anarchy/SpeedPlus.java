package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class SpeedPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Speed")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> fallWhenMoving = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Fall Faster When Moving")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> fallSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Fall Speed")
        .description("Speed of falling")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .name("Timer")
        .description("Speed but better")
        .defaultValue(1.1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private boolean move = false;

    public SpeedPlus() {super(Addon.ANARCHY, "Speed+", "Very noice sped");}

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (Modules.get().get(HoleSnap.class).isActive()) {return;}
        Modules.get().get(Timer.class).setOverride(timer.get());
        double multiplier = speed.get();
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            multiplier += (mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.2;
        }
        double forward = mc.player.input.movementForward;
        double sideways = mc.player.input.movementSideways;
        boolean moving = Math.abs(forward) > 0 && Math.abs(sideways) > 0;
        double yaw = getYaw(forward, sideways);
        double x = Math.cos(Math.toRadians(yaw + 90.0f));
        double y = mc.player.getVelocity().y;
        double yVel = y < 0 && (moving || !fallWhenMoving.get()) ? y * fallSpeed.get() : y;
        double z = Math.sin(Math.toRadians(yaw + 90.0f));
        if (move) {
            ((IVec3d) event.movement).set(multiplier * x, yVel, multiplier * z);
        } else {
            ((IVec3d) event.movement).set(0, y, 0);
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
