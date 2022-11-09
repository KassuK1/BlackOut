package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import net.minecraft.item.Items;

/*
Made by KassuK
*/

public class FastXP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<rotationmode> rotmode = sgGeneral.add(new EnumSetting.Builder<rotationmode>()
        .name("Rotation mode")
        .description("ken i put mi balls in yo jawzz")
        .defaultValue(rotationmode.Silent)
        .build()
    );
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
        .range(0, 90)
        .sliderMax(90)
        .build()
    );

    private final Setting<Boolean> Rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Should we do a bit of rotating")
        .defaultValue(true)
        .build()
    );
    public enum rotationmode {
        Silent,
        Vanilla,
    }

    public FastXP() {
        super(BlackOut.ANARCHY, "FastXP", "XP spamming moment");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            int Ticks = Math.min(((MinecraftClientAccessor) mc).getItemUseCooldown(), YeetDelay.get());
            ((MinecraftClientAccessor) mc).setItemUseCooldown(Ticks);
            if (mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE  && mc.options.useKey.isPressed()){
                if (rotmode.get().equals(rotationmode.Silent) && Rotate.get()){
                    Rotations.rotate(mc.player.getYaw(),Pitch.get());
                }
                if (rotmode.get().equals(rotationmode.Vanilla) && Rotate.get())
                    mc.player.setPitch(Pitch.get());}
    }
}}
