package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ScaffoldPlus extends Module {
    public ScaffoldPlus() {super(Addon.ANARCHY, "Scaffold+", "KasumsSoft blockwalk");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> ssprint = sgGeneral.add(new BoolSetting.Builder()
        .name("StopSprint")
        .description("Stops you from sprinting")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> usetimer = sgGeneral.add(new BoolSetting.Builder()
        .name("Use timer")
        .description("Should we use timer")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .visible(usetimer::get)
        .name("Timer")
        .description("Speed but better")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay.")
        .defaultValue(5)
        .range(0, 60)
        .sliderMax(60)
        .build()
    );
    private int tdelay;
    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null){
            BlockPos pos = mc.player.getBlockPos();
            BlockPos ypos = mc.player.getBlockPos().down(1);
            if (ssprint.get())
                mc.player.setSprinting(false);
            if (usetimer.get())
                Modules.get().get(Timer.class).setOverride(timer.get());
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(pos.getX(), ypos.getY(), pos.getZ()), Direction.UP,  ypos, false), 0));

        }
    }

}
