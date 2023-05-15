package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

/**
 * @author KassuK
 */

public class CustomFOV extends BlackOutModule {
    public CustomFOV() {
        super(BlackOut.BLACKOUT, "Custom FOV", "Allows more customisation to the FOV.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> FOV = sgGeneral.add(new IntSetting.Builder()
        .name("FOV")
        .description("What the FOV should be.")
        .defaultValue(120)
        .range(0, 358)
        .sliderRange(0, 358)
        .build()
    );

    @EventHandler
    private void onFov(GetFovEvent event) {
        event.fov = FOV.get();
    }
}

