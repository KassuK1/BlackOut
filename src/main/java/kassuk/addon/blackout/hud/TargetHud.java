package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.player.PlayerEntity;

import static meteordevelopment.meteorclient.MeteorClient.mc;

//Made By KassuK & OLEPOSSU

public class TargetHud extends HudElement {

    public static final HudElementInfo<TargetHud> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "TargetHud", "A target hud the fuck you thinkin bruv", TargetHud::new);
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Renderer scale")
        .description("Scale to render at")
        .defaultValue(1)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Backround color")
        .description(".")
        .defaultValue(new SettingColor(0, 0, 0, 155))
        .build()
    );

    private final Setting<SettingColor> textcolor = sgGeneral.add(new ColorSetting.Builder()
        .name("Text color")
        .description(".")
        .defaultValue(new SettingColor(255, 255, 255, 155))
        .build()
    );

    private final Setting<SettingColor> bar = sgGeneral.add(new ColorSetting.Builder()
        .name("HealthBar color")
        .description(".")
        .defaultValue(new SettingColor(0, 255, 0, 200))
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("Text shadow")
        .description("Should the text have a shadow")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> outline = sgGeneral.add(new BoolSetting.Builder()
        .name("Outline")
        .description("Should we render an outline")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> outlineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Outline color")
        .description(".")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(outline::get)
        .build()
    );

    public TargetHud() {
        super(INFO);
    }
    int outlineWidth = 5;
    double scaleAnim = 0;
    int health = 0;
    String renderName = "Raksamies";

    @Override
    public void render(HudRenderer renderer) {
        double[] size = new double[]{200 * scale.get() * scale.get(), 65 * scale.get() * scale.get()};
        setSize(size[0], size[1]);
        PlayerEntity playerEntity = getClosest();
        if (playerEntity != null){
            renderName = playerEntity.getName().getString();
            health = (int) (playerEntity.getHealth() + playerEntity.getAbsorptionAmount());
            scaleAnim = Math.min(1, scaleAnim + renderer.delta * (1 - scaleAnim) * 5);
        } else {
            scaleAnim = Math.max(0, scaleAnim - renderer.delta * (1.01 - scaleAnim) * 5);
        }
        double[] renderSize = new double[]{size[0] * scaleAnim, size[1] * scaleAnim};
        double[] offsetPos = new double[]{x - renderSize[0] / 2, y - renderSize[1] / 2};
        if (scaleAnim > 0.01) {
            renderer.quad(offsetPos[0], offsetPos[1], renderSize[0], renderSize[1], color.get());
            renderer.text(renderName, offsetPos[0] + renderSize[0] * 0.05, offsetPos[1] + renderSize[1] / 13, textcolor.get(), shadow.get(), scale.get() * Math.sqrt(scaleAnim));
            renderer.text(String.valueOf(health), offsetPos[0] + renderSize[0] * 0.05, offsetPos[1] + renderSize[1] * 0.5, textcolor.get(), shadow.get(), scale.get() * Math.sqrt(scaleAnim));
            renderer.quad(offsetPos[0] + renderSize[0] * 0.05, offsetPos[1] + renderSize[1] * 0.8, 180 / 36f * scale.get() * scale.get() * scaleAnim * health, renderSize[1] * 0.1, bar.get());
            if (outline.get()) {
                renderer.line(offsetPos[0], offsetPos[1], offsetPos[0] + renderSize[0], offsetPos[1], outlineColor.get());
                renderer.line(offsetPos[0] + renderSize[0], offsetPos[1], offsetPos[0] + renderSize[0], offsetPos[1] + renderSize[1], outlineColor.get());
                renderer.line(offsetPos[0], offsetPos[1], offsetPos[0], offsetPos[1] + renderSize[1], outlineColor.get());
                renderer.line(offsetPos[0], offsetPos[1] + renderSize[1], offsetPos[0] + renderSize[0], offsetPos[1] + renderSize[1], outlineColor.get());
            }
        }
    }
    private PlayerEntity getClosest() {
        PlayerEntity closest = null;
        float distance = -1;
        if (!mc.world.getPlayers().isEmpty()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player) {
                    if (closest == null || OLEPOSSUtils.distance(mc.player.getPos(), player.getPos()) < distance) {
                        closest = player;
                        distance = (float) OLEPOSSUtils.distance(mc.player.getPos(), player.getPos());
                    }
                }
            }
        }
        return closest;
    }
}
