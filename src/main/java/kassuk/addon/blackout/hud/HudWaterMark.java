package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * @author KassuK
 */

public class HudWaterMark extends HudElement {

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
        .defaultValue(1.5)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> logo = sgGeneral.add(new BoolSetting.Builder()
        .name("Logo")
        .description("Renders BlackOut logo.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> logoScale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Logo Scale")
        .description("Modify the size of the logo.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    private final Identifier LOGO = Identifier.of("blackout", "logo.png");

    public static final HudElementInfo<HudWaterMark> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "BlackoutWatermark", "The Blackout watermark.", HudWaterMark::new);

    public HudWaterMark() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth(BlackOut.BLACKOUT_NAME + " v" + BlackOut.BLACKOUT_VERSION, true) * scale.get() * scale.get(), renderer.textHeight(true) * scale.get() * scale.get());

        String text = BlackOut.BLACKOUT_NAME + " v" + BlackOut.BLACKOUT_VERSION;

        renderer.text(text, x, y, color.get(), true, scale.get());

        if (!logo.get()) {return;}
        MatrixStack matrixStack = new MatrixStack();

        GL.bindTexture(LOGO);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x + renderer.textWidth(BlackOut.BLACKOUT_NAME + " v" + BlackOut.BLACKOUT_VERSION) * scale.get() * scale.get(),
            y + renderer.textHeight(true) * scale.get() * scale.get() / 2 - logoScale.get() * 128 / 2,
            logoScale.get() * 128, logoScale.get() * 128, new Color(255, 255, 255, 255));
        Renderer2D.TEXTURE.render(matrixStack);
    }
}
