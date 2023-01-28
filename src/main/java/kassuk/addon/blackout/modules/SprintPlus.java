package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.settings.SettingGroup;

/*
Made by KassuK
*/

public class SprintPlus extends Module {
    public SprintPlus() {
        super(BlackOut.BLACKOUT, "Sprint+", "Non shit sprint");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> sprintTime = sgGeneral.add(new BoolSetting.Builder()
        .name("sprintTime")
        .description("Rak is autistic and thinks this is needed")
        .defaultValue(true)
        .build()
    );
    public final Setting<SprintMode> sprintmode = sgGeneral.add(new EnumSetting.Builder<SprintMode>()
        .name("mode")
        .description("The method of sprinting.")
        .defaultValue(SprintMode.Vanilla)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            switch (sprintmode.get()){
                case Vanilla -> {
                    if (mc.options.forwardKey.isPressed()){
                        mc.player.setSprinting(true);
                        if (sprintTime.get())
                            mc.player.ticksSinceSprintingChanged = 100;}
                }
                case Omni -> {
                    if (PlayerUtils.isMoving()){
                        mc.player.setSprinting(true);
                        if (sprintTime.get())
                            mc.player.ticksSinceSprintingChanged = 100;}
                }
                case Rage ->{
                    mc.player.setSprinting(true);
                    if (sprintTime.get())
                        mc.player.ticksSinceSprintingChanged = 100;}
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
