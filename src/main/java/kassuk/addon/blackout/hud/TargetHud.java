package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static meteordevelopment.meteorclient.MeteorClient.mc;
/*
Made By KassuK & OLEPOSSU
 */

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
    private final Setting<Boolean> damage = sgGeneral.add(new BoolSetting.Builder()
        .name("Shows Damage")
        .description(".")
        .defaultValue(false)
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
    double scaleAnim = 0;
    int health = 0;
    int lastHealth = 0;
    int difference = 0;
    String renderName = "HeavyMan";
    Random r = new Random();
    Map<Vec3d, Integer> damages = new HashMap<>();

    @Override
    public void render(HudRenderer renderer) {
        double[] size = new double[]{200 * scale.get() * scale.get(), 65 * scale.get() * scale.get()};
        setSize(size[0], size[1]);
        PlayerEntity playerEntity = getClosest();
        double[] renderSize = new double[]{size[0] * scaleAnim, size[1] * scaleAnim};
        double[] offsetPos = new double[]{x + size[0] * (1 - scaleAnim) / 2, y + size[1] * (1 - scaleAnim) / 2};
        if (playerEntity != null){
            renderName = playerEntity.getName().getString();
            health = Math.round((playerEntity.getHealth() + playerEntity.getAbsorptionAmount()));
            difference = health - lastHealth;
            if (difference < 0) {
                damages.put(new Vec3d(offsetPos[0] + renderSize[0] * 0.05 + r.nextFloat() * scale.get() * scale.get() * scaleAnim * 180 / 36f * health, offsetPos[1] + renderSize[1] * 0.7, 230 + r.nextFloat() * 25), difference);
            }
            lastHealth = health;
            scaleAnim = Math.min(1, scaleAnim + renderer.delta * (1 - scaleAnim) * 5);
        } else {
            scaleAnim = Math.max(0, scaleAnim - renderer.delta * (1.001 - scaleAnim) * 5);
        }

        // Calc
        Map<Vec3d, Integer> newDamages = new HashMap<>();
        damages.forEach((vec, i) -> {
            if (vec.z > renderer.delta * 100) {
                newDamages.put(new Vec3d(vec.x, vec.y - renderer.delta * 5, vec.z - renderer.delta * 100), i);
            }
        });
        damages = newDamages;

        // Render
        if (scaleAnim > 0.01) {
            renderer.quad(offsetPos[0], offsetPos[1], renderSize[0], renderSize[1], color.get(), color.get(), color.get(), color.get());

            if (damage.get()) {
                damages.forEach((vec, i) -> {
                    renderer.text(String.valueOf(i), vec.x, vec.y, getColor(i, (int) Math.round(vec.z)), true, scale.get());
                });
            }

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
    PlayerEntity getClosest() {
        PlayerEntity closest = null;
        float distance = -1;
        if (!mc.world.getPlayers().isEmpty()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player && !Friends.get().isFriend(player)) {
                    if (closest == null || OLEPOSSUtils.distance(mc.player.getPos(), player.getPos()) < distance) {
                        closest = player;
                        distance = (float) OLEPOSSUtils.distance(mc.player.getPos(), player.getPos());
                    }
                }
            }
        }
        return closest;
    }
    Color getColor(int dmg, int a) {
        int c = (int) Math.min(255, Math.max(0, Math.round(dmg * -15) + 122.5f));
        return new Color(c, 255 - c, 0, a);
    }
}
