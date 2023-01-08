package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.render.FogShape;
import net.minecraft.entity.effect.StatusEffect;

/*
Made by OLEPOSSU
*/


public class Fog extends Module {
    public Fog() {
        super(BlackOut.BLACKOUT, "Fog", "Customizable fog");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<FogShape> shape = sgGeneral.add(new EnumSetting.Builder<FogShape>()
        .description(".")
        .defaultValue(FogShape.SPHERE)
        .build()
    );
    public final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("Distance")
        .description(".")
        .defaultValue(25)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("The color")
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .build()
    );
}
