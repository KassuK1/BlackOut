package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Welcomer extends HudElement {

    public static final HudElementInfo<Welcomer> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "Welcomer", "Welcomes you", Welcomer::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Renderer scale")
        .description("Scale to render at")
        .defaultValue(1)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<SettingColor> textcolor = sgGeneral.add(new ColorSetting.Builder()
        .name("Text color")
        .description(".")
        .defaultValue(new SettingColor(255, 255, 255, 155))
        .build()
    );
    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("Text shadow")
        .description("Should the text have a shadow")
        .defaultValue(true)
        .build()
    );


    public Welcomer() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.player != null && mc.world != null){
        setSize(50 * scale.get() * scale.get(),20 * scale.get() * scale.get());
        renderer.text("Welcome " + mc.player.getName(),x,y,textcolor.get(),shadow.get(),scale.get());
        }
        if (isInEditor()){
            renderer.text("BlackOut Welcomer",x,y,textcolor.get(),shadow.get(),scale.get());
        }
    }
}
