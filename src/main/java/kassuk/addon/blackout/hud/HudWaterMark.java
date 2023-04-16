package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.RenderUtils;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
/*
Made by KassuK
*/

public class HudWaterMark extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Size of the text")
        .defaultValue(1)
        .build()
    );
    public static final HudElementInfo<HudWaterMark> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "BlackoutWatermark", "The Blackout watermark.", HudWaterMark::new);

    public HudWaterMark() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth(BlackOut.BLACKOUT_NAME + " v" + BlackOut.BLACKOUT_VERSION, true) * scale.get() * scale.get(), renderer.textHeight(true) * scale.get() * scale.get());

        renderer.text(BlackOut.BLACKOUT_NAME + " v" + BlackOut.BLACKOUT_VERSION, x, y, color.get(), true, scale.get());
        RenderUtils.renderText(null, "Large garge sus sus maximus");
    }
}
