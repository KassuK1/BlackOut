package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.systems.modules.Modules;


/*
Made by KassuK
*/

public class ResetVL extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .name("Timer")
        .description("What timer to use")
        .defaultValue(1.1)
        .min(0)
        .sliderMax(5)
        .build()
    );

    public ResetVL() {
        super(BlackOut.ANARCHY, "ResetVL", "Tries to reset your violation level");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player != null && mc.world != null){
            Modules.get().get(Timer.class).setOverride(timer.get());
            if (mc.player.isOnGround())
                mc.player.jump();}
        }

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(Timer.OFF);
    }
    }

