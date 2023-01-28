package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

/*
Made by OLEPOSSU / Raksamies
*/

public class CrystalBait extends Module {
    public CrystalBait() {super(BlackOut.BLACKOUT, "CrystalBait", "Jumps fast to make enemy place crystals.");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between jumps")
        .defaultValue(2)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> timer = sgGeneral.add(new IntSetting.Builder()
        .name("Timer")
        .description("Jump timer.")
        .defaultValue(5)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    double jumpTimer = 0;
    boolean lastOnGround;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        jumpTimer -= event.frameTime;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        if (mc.player.isOnGround()) {
            if (lastOnGround && jumpTimer < 0) {
                jumpTimer = delay.get();
                ((IVec3d) event.movement).setY(0.420);
            } else {
                Modules.get().get(Timer.class).setOverride(Timer.OFF);
            }
            lastOnGround = true;
        } else {
            Modules.get().get(Timer.class).setOverride(timer.get());
            lastOnGround = false;
        }
        ((IVec3d) event.movement).setXZ(0, 0);

    }
}
