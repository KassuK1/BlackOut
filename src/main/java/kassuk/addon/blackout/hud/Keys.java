package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

// Spaghetti by KassuK

public class Keys extends HudElement {
    public static final HudElementInfo<Keys> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "Keys", "Draws current movement keys", Keys::new);
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
    private final Setting<drawMode> drawingMode = sgGeneral.add(new EnumSetting.Builder<drawMode>()
        .name("Draw mode")
        .description(".")
        .defaultValue(drawMode.Basic)
        .build()
    );

    public enum drawMode {
        Horizontal,
        Vertical,
        Basic,
    }

    public Keys() {
        super(INFO);
    }
    @Override
    public void render(HudRenderer renderer) {
        setSize(50 * scale.get() * scale.get(),20 * scale.get() * scale.get());
        if (drawingMode.get().equals(drawMode.Horizontal)){
            if (mc.options.forwardKey.isPressed()){
                renderer.text("W",x,y,textcolor.get(),shadow.get(),scale.get());
            }
            if (mc.options.leftKey.isPressed()){
                renderer.text("A",x + 15 * scale.get() * scale.get(),y,textcolor.get(),shadow.get(),scale.get());
            }
            if (mc.options.backKey.isPressed()){
                renderer.text("S",x + 30 * scale.get() * scale.get(),y,textcolor.get(),shadow.get(),scale.get());
            }
            if (mc.options.rightKey.isPressed()){
                renderer.text("D",x + 45 * scale.get() * scale.get(),y,textcolor.get(),shadow.get(),scale.get());
            }
        }
        if (drawingMode.get().equals(drawMode.Vertical)){
            if (mc.options.forwardKey.isPressed()){
                renderer.text("W",x,y,textcolor.get(),shadow.get(),scale.get());
            }
            if (mc.options.leftKey.isPressed()){
                renderer.text("A",x,y + 15 * scale.get() * scale.get(),textcolor.get(),shadow.get(),scale.get());
            }
            if (mc.options.backKey.isPressed()){
                renderer.text("S",x,y + 30 * scale.get() * scale.get(),textcolor.get(),shadow.get(),scale.get());
            }
            if (mc.options.rightKey.isPressed()){
                renderer.text("D",x,y + 45 * scale.get() * scale.get(),textcolor.get(),shadow.get(),scale.get());
            }
        }
        if (drawingMode.get().equals(drawMode.Basic)){
            if (mc.options.forwardKey.isPressed()){
                renderer.text("W",x,y -20 * scale.get() * scale.get(),textcolor.get(),shadow.get(),scale.get());
            }
            if (mc.options.leftKey.isPressed()){
                renderer.text("A",x - 20 * scale.get() * scale.get(),y,textcolor.get(),shadow.get(),scale.get());
            }
            if (mc.options.backKey.isPressed()){
                renderer.text("S",x,y,textcolor.get(),shadow.get(),scale.get());
            }
            if (mc.options.rightKey.isPressed()){
                renderer.text("D",x + 20 * scale.get() * scale.get(),y,textcolor.get(),shadow.get(),scale.get());
            }
        }
    if (isInEditor()){
        renderer.text("Keys",x,y,textcolor.get(),shadow.get(),scale.get());
    }
    }
}
