package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

/**
 * @author KassuK
 */

public class SprintPlus extends BlackOutModule {
    public SprintPlus() {
        super(BlackOut.BLACKOUT, "Sprint+", "Non shit sprint!");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<SprintMode> sprintMode = sgGeneral.add(new EnumSetting.Builder<SprintMode>()
        .name("Mode")
        .description("The method of sprinting.")
        .defaultValue(SprintMode.Vanilla)
        .build()
    );
    public final Setting <Boolean> hungerCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("HungerCheck")
        .description("Should we check if we have enough hunger to sprint")
        .defaultValue(true)
        .build()
    );

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (ScaffoldPlus.shouldStopSprinting && Modules.get().isActive(ScaffoldPlus.class)) {return;}

        if (mc.player != null && mc.world != null) {
            if (hungerCheck.get()) {
                if (mc.player.getHungerManager().getFoodLevel() < 6) {
                    mc.player.setSprinting(false);
                    return;
                }
            }
            switch (sprintMode.get()) {
                case Vanilla -> {
                    if (mc.options.forwardKey.isPressed()) mc.player.setSprinting(true);
                }
                case Omni -> {
                    if (PlayerUtils.isMoving()) {
                        mc.player.setSprinting(true);
                    }
                }
                case Rage -> mc.player.setSprinting(true);
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null && mc.world != null)
            mc.player.setSprinting(false);
    }

    public enum SprintMode {
        Vanilla,
        Omni,
        Rage
    }
}
