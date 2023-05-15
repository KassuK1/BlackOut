package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author KassuK
 */

public class OnTope extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Modify the size of the text.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("Text Shadow")
        .description("Should the text have a shadow.")
        .defaultValue(true)
        .build()
    );

    public static final HudElementInfo<OnTope> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "OnTope", "I don't even know what this is.", OnTope::new);

    public OnTope() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.player == null) {return;}
        String text = mc.player.getName().getString() + " on top!";

        setSize(renderer.textWidth(text, shadow.get(), scale.get()), renderer.textHeight(true, scale.get()));
        renderer.text(text, x, y, color.get(), shadow.get(), scale.get());
    }
}
