package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

/*
Made by OLEPOSSU / Raksamies
*/

public class Swing extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description(".")
        .defaultValue(1)

        .build()
    );
    public float progress = 0;

    public Swing() {
        super(BlackOut.ANARCHY, "Swing", ".");
    }


}
