package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class BlackoutArray extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Module Color")
        .description("The color the ArrayList will use for module names")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> infoColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Info Color")
        .description("The color the ArrayList will use for info strings")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Boolean> wave = sgGeneral.add(new BoolSetting.Builder()
        .name("Wave")
        .description("Wave color")
        .defaultValue(false)
        .build()
    );
    private final Setting<SettingColor> waveColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Module Wave Color")
        .description("The color the ArrayList will use for module names")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<SettingColor> infoWaveColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Info Wave Color")
        .description("The color the ArrayList will use for info strings")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Wave Speed")
        .description("Speed of color waves")
        .defaultValue(1)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Side> side = sgGeneral.add(new EnumSetting.Builder<Side>()
        .name("Side")
        .description(".")
        .defaultValue(Side.Right)
        .build()
    );
    private final Setting<From> from = sgGeneral.add(new EnumSetting.Builder<From>()
        .name("From")
        .description(".")
        .defaultValue(From.Top)
        .build()
    );
    private final Setting<Boolean> onlyBlackout = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Blackout")
        .description("Only shows blackout modules in hud")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("The scale the ArrayList will be rendered at")
        .defaultValue(1)
        .build()
    );

    public enum Side {
        Right,
        Left
    }
    public enum From {
        Top,
        Bottom
    }

    List<Line> sorted = new ArrayList<>();
    List<Line> unsorted = new ArrayList<>();
    double length;
    double longest;
    Line longestLine;

    public static final HudElementInfo<BlackoutArray> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "BlackoutArray", "ArrayList for blackout features",BlackoutArray::new);

    public BlackoutArray(){super(INFO);}
    List<Module> getModules() {
        List<Module> list = new ArrayList<>();
        Modules.get().getActive().forEach(it -> {
            if (onlyBlackout.get() && !it.category.equals(BlackOut.BLACKOUT)) {return;}
            list.add(it);
        });
        return list;
    }

    @Override
    public void render(HudRenderer renderer) {
        List<Module> list = getModules();

        sorted.clear();
        unsorted.clear();

        list.forEach(item -> {
            unsorted.add(new Line(item.name, item.getInfoString() == null ? "" : item.getInfoString(), width(renderer, item.name)));
        });

        setSize(120 * scale.get() * scale.get(), unsorted.size() > 0 ? height(renderer) * unsorted.size() : 30 * scale.get() * scale.get());


        for (int i = unsorted.size(); i > 0; i--) {
            length = 0;
            longest = -1;
            longestLine = null;

            if (from.get() == From.Top) {
                unsorted.forEach(item -> {
                    if (item.length > longest) {
                        longestLine = item;
                        longest = item.length;
                    }
                });
            } else {
                unsorted.forEach(item -> {
                    if (longest < 0 || item.length < longest) {
                        longestLine = item;
                        longest = item.length;
                    }
                });
            }

            sorted.add(longestLine);
            unsorted.remove(longestLine);
        }

        for (int i = 0; i < sorted.size(); i++) {
            Line line = sorted.get(i);
            renderer.text(line.name, side.get() == Side.Left ? x : x + 120 - width(renderer, line.name + " " + line.info), y + i * height(renderer) * scale.get() * scale.get(), getColor(color.get(), waveColor.get(), (Math.sin(i * 6 + System.currentTimeMillis() / 1000D * speed.get()) + 1) / 2D), true, scale.get());
            renderer.text(line.info, side.get() == Side.Left ? x + width(renderer, line.name + " ") : x + 120 - width(renderer, line.info), y + i * height(renderer) * scale.get() * scale.get(), getColor(infoColor.get(), infoWaveColor.get(), (Math.sin(i * 6 + System.currentTimeMillis() / 1000D * speed.get()) + 1) / 2D), true, scale.get());
        }
    }

    Color getColor(SettingColor color, SettingColor waveColor, double f) {
        if (!wave.get()) {return color;}
        return new Color(colorVal(color.r, waveColor.r, f), colorVal(color.g, waveColor.g, f), colorVal(color.b, waveColor.b, f), color.a);
    }

    int colorVal(int original, int wave, double f) {
        return MathHelper.clamp((int) Math.round(original + (wave - original) * f), 0, 255);
    }

    public record Line(String name, String info, double length) {};

    double width(HudRenderer renderer, String text) {
        return renderer.textWidth(text) * scale.get() * scale.get();
    }

    double height(HudRenderer renderer) {
        return renderer.textHeight(true) * scale.get() * scale.get();
    }
}
