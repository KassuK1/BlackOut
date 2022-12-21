package com.example.addon.modules.ghost;

import com.example.addon.BlackOut;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class CustomFOV extends Module {
    public CustomFOV() {super(BlackOut.GHOST, "CustomFOV", "Allows more customisation to the FOV");}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> FOV = sgGeneral.add(new IntSetting.Builder()
        .name("FOV")
        .description("What the FOV should be")
        .defaultValue(120)
        .min(0)
        .sliderMax(360)
        .build()
    );

    int original = 0;

    @Override
    public void onActivate() {
        var original = mc.options.getFov();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null){
            mc.options.getFov().setValue(FOV.get());
        }
    }

    @Override
    public void onDeactivate() {
        mc.options.getFov().setValue(FOV.get());
    }
}
