package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class CustomFOV extends Module {
    public CustomFOV() {super(BlackOut.BLACKOUT, "CustomFOV", "Allows more customisation to the FOV");}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> FOV = sgGeneral.add(new IntSetting.Builder()
        .name("FOV")
        .description("What the FOV should be")
        .defaultValue(120)
        .min(0)
        .sliderMax(360)
        .build()
    );

    @EventHandler
    private void onFov(GetFovEvent event) {
        event.fov = FOV.get();
    }
}

