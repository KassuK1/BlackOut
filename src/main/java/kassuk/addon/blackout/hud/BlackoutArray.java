package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.text.TextColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BlackoutArray extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("The color the ArrayList will use")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("The scale the ArrayList will be rendered at")
        .defaultValue(1)
        .build()
    );

    public static final HudElementInfo<BlackoutArray> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "BlackoutArray", "ArrayList for blackout features",BlackoutArray::new);

    public BlackoutArray(){super(INFO);}
    private List<Module> getModules() {
        List<Module> list = new ArrayList<>();
        Modules.get().getActive().forEach(it -> {
            if (it.category.equals(BlackOut.ANARCHY) || (it.category.equals(BlackOut.GHOST))){
                list.add(it);
            }
        });
        return list;
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth("Blackout", true) * scale.get() * scale.get(), renderer.textHeight(true) * scale.get() * scale.get());
        List<Module> list = getModules();
        for (int i = 0; i < list.size(); i++){
            renderer.text(list.get(i).name + " " + (list.get(i).getInfoString() != null ? list.get(i).getInfoString(): ""), x, y + i * renderer.textHeight(true) * scale.get(), color.get(), true, scale.get());
        }
    }
}
