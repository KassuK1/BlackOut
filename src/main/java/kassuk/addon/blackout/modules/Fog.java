package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.effect.StatusEffect;

/*
Made by OLEPOSSU
*/


public class Fog extends Module {
    public Fog() {
        super(BlackOut.BLACKOUT, "Fog", "Customizable fog");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("Terrain Distance")
        .description(".")
        .defaultValue(25)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );

    public final Setting<Integer> smoothness = sgGeneral.add(new IntSetting.Builder()
        .name("Terrain Smoothness")
        .description(".")
        .defaultValue(5)
        .min(0)
        .sliderMax(10)
        .build()
    );
    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("The color")
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .build()
    );
}
