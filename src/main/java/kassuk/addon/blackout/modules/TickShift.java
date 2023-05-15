package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;

/**
 * @author OLEPOSSU
 */

public class TickShift extends BlackOutModule {
    public TickShift() {
        super(BlackOut.BLACKOUT, "Tick Shift", "Stores packets when standing still and uses them when you start moving.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<SmoothMode> smooth = sgGeneral.add(new EnumSetting.Builder<SmoothMode>()
        .name("Smoothness")
        .description(".")
        .defaultValue(SmoothMode.Exponent)
        .build()
    );
    public final Setting<Integer> packets = sgGeneral.add(new IntSetting.Builder()
        .name("Packets")
        .description("How many packets to store for later use.")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .name("Timer")
        .description("How many packets to send every movement tick.")
        .defaultValue(2)
        .min(1)
        .sliderRange(0, 10)
        .build()
    );

    public int unSent = 0;
    private boolean lastTimer = false;
    private boolean lastMoving = false;
    private final Timer timerModule = Modules.get().get(Timer.class);

    @Override
    public void onActivate() {
        super.onActivate();
        unSent = 0;
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        if (lastTimer) {
            lastTimer = false;
            timerModule.setOverride(Timer.OFF);
        }
    }

    @Override
    public String getInfoString() {
        return String.valueOf(unSent);
    }

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        if (unSent > 0 && lastMoving) {
            lastMoving = false;
            lastTimer = true;
            timerModule.setOverride(getTimer());
        } else if (lastTimer) {
            lastTimer = false;
            timerModule.setOverride(Timer.OFF);
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (e.movement.length() > 0 && !(e.movement.length() > 0.0784 && e.movement.length() < 0.0785)) {
            unSent = Math.max(0, unSent - 1);
            lastMoving = true;
        }
    }

    private double getTimer() {
        if (smooth.get() == SmoothMode.Disabled) {
            return timer.get();
        }
        double progress = 1 - (unSent / (float) packets.get());

        if (smooth.get() == SmoothMode.Exponent) {
            progress *= progress * progress * progress * progress;
        }

        return 1 + (timer.get() - 1) * (1 - progress);
    }

    public enum SmoothMode {
        Disabled,
        Normal,
        Exponent
    }
}
