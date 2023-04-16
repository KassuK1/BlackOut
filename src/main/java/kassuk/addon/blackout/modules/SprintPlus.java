package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

/*
Made by KassuK
*/

public class SprintPlus extends BlackOutModule {
    public SprintPlus() {
        super(BlackOut.BLACKOUT, "Sprint+", "Non shit sprint");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<SprintMode> sprintmode = sgGeneral.add(new EnumSetting.Builder<SprintMode>()
        .name("mode")
        .description("The method of sprinting.")
        .defaultValue(SprintMode.Vanilla)
        .build()
    );
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            switch (sprintmode.get()){
                case Vanilla -> {if (mc.options.forwardKey.isPressed()) mc.player.setSprinting(true);}
                case Omni -> {if (PlayerUtils.isMoving()){mc.player.setSprinting(true);}}
                case Rage ->mc.player.setSprinting(true);
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
