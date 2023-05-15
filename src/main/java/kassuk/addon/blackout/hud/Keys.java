package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author KassuK
 * @author OLEPOSSU
 */

public class Keys extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Scale to render at.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Key Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(30, 30, 30, 255))
        .build()
    );
    private final Setting<SettingColor> cTextColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Clicked Key Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Boolean> textBG = sgGeneral.add(new BoolSetting.Builder()
        .name("Key Background")
        .description("Should there be a background for keys.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> bgColor = sgGeneral.add(new ColorSetting.Builder()
        .name("BG Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(50, 50, 50, 255))
        .visible(textBG::get)
        .build()
    );
    private final Setting<SettingColor> cbgColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Clicked BG Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(50, 50, 50, 255))
        .visible(textBG::get)
        .build()
    );
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Mode for key locations.")
        .defaultValue(Mode.Basic)
        .build()
    );
    private final Setting<Double> renderTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("Seconds to keep full color before fading.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Double> fadeTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How many seconds should fading take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 5)
        .build()
    );

    private List<Key> keys = null;

    public static final HudElementInfo<Keys> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "Keys", "Draws pressed movement keys.", Keys::new);

    public Keys() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (keys == null) {
            keys = new ArrayList<>();
            KeyBinding[] binds = new KeyBinding[]{mc.options.forwardKey, mc.options.leftKey, mc.options.backKey, mc.options.rightKey};
            for (int i = 0; i < 4; i++) {
                KeyBinding bind = binds[i];

                String key = bind.getBoundKeyLocalizedText().getString().toUpperCase();

                keys.add(new Key(key, bind, i));
            }
        }

        setSize(switch (mode.get()) {
            case Horizontal -> 160;
            case Vertical -> 40;
            case Basic -> 120;
        } * scale.get() * scale.get(), switch (mode.get()) {
            case Horizontal -> 40;
            case Vertical -> 160;
            case Basic -> 80;
        } * scale.get() * scale.get());

        keys.forEach(key -> {
            key.updatePos();
            key.checkClick();
            if (textBG.get()) {
                renderer.quad(key.posX + 2 * scale.get() * scale.get(), key.posY + 2 * scale.get() * scale.get(), 36 * scale.get() * scale.get(), 36 * scale.get() * scale.get(), getBGColor(key));
            }
            renderer.text(key.key, key.posX + xOffset(key.key, renderer), key.posY + yOffset(renderer), getTextColor(key), false, scale.get());
        });
    }

    private Color getBGColor(Key k) {
        return lerpColor(MathHelper.clamp((k.sinceClick() - renderTime.get() * 1000) / fadeTime.get() / 1000, 0, 1), cbgColor.get(), bgColor.get());
    }

    private Color getTextColor(Key k) {
        return lerpColor(MathHelper.clamp((k.sinceClick() - renderTime.get() * 1000) / fadeTime.get() / 1000, 0, 1), cTextColor.get(), textColor.get());
    }

    private Color lerpColor(double delta, Color s, Color e) {
        return new Color((int) Math.round(MathHelper.lerp(delta, s.r, e.r)), (int) Math.round(MathHelper.lerp(delta, s.g, e.g)), (int) Math.round(MathHelper.lerp(delta, s.b, e.b)), (int) Math.round(MathHelper.lerp(delta, s.a, e.a)));
    }

    private double xOffset(String string, HudRenderer renderer) {
        return (20 - renderer.textWidth(string, false) / 2) * scale.get() * scale.get();
    }

    private double yOffset(HudRenderer renderer) {
        return (20 - renderer.textHeight(false) / 2) * scale.get() * scale.get();
    }

    private double getX(int i) {
        return switch (mode.get()) {
            case Horizontal -> i * 40;
            case Vertical -> 0.0;
            case Basic -> i == 0 ? 40 : (i - 1) * 40;
        };
    }

    private double getY(int i) {
        return switch (mode.get()) {
            case Horizontal -> 0.0;
            case Vertical -> i * 40;
            case Basic -> i == 0 ? 0 : 40;
        };
    }

    private class Key {
        public final String key;
        public final KeyBinding bind;
        public final int i;
        public double posX = 0;
        public double posY = 0;
        public long lastClicked = 0;

        public Key(String key, KeyBinding bind, int i) {
            this.key = key;
            this.bind = bind;
            this.i = i;
        }

        public void updatePos() {
            posX = x + (getX(i)) * scale.get() * scale.get();
            posY = y + (getY(i)) * scale.get() * scale.get();
        }

        public void checkClick() {
            if (bind.isPressed()) {
                lastClicked = System.currentTimeMillis();
            }
        }

        public long sinceClick() {
            return System.currentTimeMillis() - lastClicked;
        }
    }

    public enum Mode {
        Horizontal,
        Vertical,
        Basic,
    }
}
