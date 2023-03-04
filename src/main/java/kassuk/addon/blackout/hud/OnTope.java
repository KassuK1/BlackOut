package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;
/*
Made by KassuK
*/

public class OnTope extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Size of the text")
        .defaultValue(1)
        .build()
    );
    public static final HudElementInfo<OnTope> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "OnTope", "I don't even know what this is", OnTope::new);

    public OnTope() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.player != null) {
            setSize(renderer.textWidth(mc.player.getName().getString() + " on top!", true) * scale.get() * scale.get(), renderer.textHeight(true) * scale.get() * scale.get());

            renderer.text(mc.player.getName().getString() + " on top!", x, y, color.get(), true, scale.get());
        }
    }
}
