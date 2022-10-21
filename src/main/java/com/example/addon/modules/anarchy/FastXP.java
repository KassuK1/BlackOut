package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;

/*
Made by KassuK
*/

public class FastXP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> YeetDelay = sgGeneral.add(new IntSetting.Builder()
        .name("YeetDelay")
        .description("Delay of yeeting")
        .defaultValue(0)
        .range(0, 3)
        .sliderMax(3)
        .build()
    );

    private final Setting<Integer> Pitch = sgGeneral.add(new IntSetting.Builder()
        .name("Pitch")
        .description("Where to set Pitch")
        .defaultValue(45)
        .range(20, 90)
        .sliderMax(90)
        .build()
    );

    private final Setting<Boolean> Rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Should we do a bit of rotating")
        .defaultValue(true)
        .build()
    );

    public FastXP() {
        super(Addon.ANARCHY, "FastXP", "XP spamming moment");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            int Ticks = Math.min(((MinecraftClientAccessor) mc).getItemUseCooldown(), YeetDelay.get());
            ((MinecraftClientAccessor) mc).setItemUseCooldown(Ticks);
            if (Rotate.get())
                mc.player.setPitch(Pitch.get());
    }
}}
