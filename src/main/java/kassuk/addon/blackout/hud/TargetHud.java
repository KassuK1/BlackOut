package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author KassuK
 * @author OLEPOSSU
 */
public class TargetHud extends HudElement {

    public static final HudElementInfo<TargetHud> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "TargetHud", "A target hud the fuck you thinkin bruv.", TargetHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Scale to render at")
        .defaultValue(1)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Background Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(0, 0, 0, 155))
        .build()
    );
    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Text Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 155))
        .build()
    );
    private final Setting<SettingColor> maxHP = sgGeneral.add(new ColorSetting.Builder()
        .name("Max Health Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 0, 255))
        .build()
    );
    private final Setting<SettingColor> highHP = sgGeneral.add(new ColorSetting.Builder()
        .name("High Health Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .build()
    );
    private final Setting<SettingColor> lowHP = sgGeneral.add(new ColorSetting.Builder()
        .name("Low Health Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("Text Shadow")
        .description("Should the text have a shadow.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> damage = sgGeneral.add(new BoolSetting.Builder()
        .name("Damage")
        .description("Renders damage dealt to target.")
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
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(outline::get)
        .build()
    );

    public TargetHud() {
        super(INFO);
    }

    private double scaleAnim = 0;
    private int health = 0;
    private int lastHealth = 0;
    private String renderName = "HeavyMan";
    private final Random r = new Random();
    private Map<Vec3d, Integer> damages = new HashMap<>();

    @Override
    public void render(HudRenderer renderer) {
        float[] size = new float[]{(float) (200 * scale.get() * scale.get()), (float) (65 * scale.get() * scale.get())};

        setSize(size[0], size[1]);
        PlayerEntity playerEntity = isInEditor() ? mc.player : getClosest();
        float[] renderSize = new float[]{(float) (size[0] * scaleAnim), (float) (size[1] * scaleAnim)};
        float[] offsetPos = new float[]{(float) (x + size[0] * (1 - scaleAnim) / 2), (float) (y + size[1] * (1 - scaleAnim) / 2)};
        if (playerEntity != null) {
            renderName = playerEntity.getName().getString();
            health = Math.round((playerEntity.getHealth() + playerEntity.getAbsorptionAmount()));
            int difference = health - lastHealth;
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
            renderer.quad(offsetPos[0], offsetPos[1], renderSize[0], renderSize[1], color.get());

            if (damage.get()) {
                damages.forEach((vec, i) -> {
                    renderer.text(String.valueOf(i), vec.x, vec.y, getColor(i, (int) Math.round(vec.z)), true, scale.get());
                });
            }

            renderer.text(renderName, offsetPos[0] + renderSize[0] * 0.05, offsetPos[1] + renderSize[1] / 13, textColor.get(), shadow.get(), scale.get() * Math.sqrt(scaleAnim));
            renderer.text(String.valueOf(health), offsetPos[0] + renderSize[0] * 0.05, offsetPos[1] + renderSize[1] * 0.5, textColor.get(), shadow.get(), scale.get() * Math.sqrt(scaleAnim));

            renderer.quad(offsetPos[0] + renderSize[0] * 0.05, offsetPos[1] + renderSize[1] * 0.8, 180 / 36f * scale.get() * scale.get() * scaleAnim * health, renderSize[1] * 0.1, barColor());

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

    private Color getColor(int dmg, int a) {
        int c = (int) Math.min(255, Math.max(0, dmg * -15 + 122.5f));
        return new Color(c, 255 - c, 0, a);
    }

    private Color barColor() {
        if (health <= 20) {
            return new Color(lerp(health / 20d, lowHP.get().r, highHP.get().r),
                lerp(health / 20d, lowHP.get().g, highHP.get().g),
                lerp(health / 20d, lowHP.get().b, highHP.get().b),
                lerp(health / 20d, lowHP.get().a, highHP.get().a));
        } else {
            return new Color(lerp((health - 20) / 16d, highHP.get().r, maxHP.get().r),
                lerp((health - 20) / 16d, highHP.get().g, maxHP.get().g),
                lerp((health - 20) / 16d, highHP.get().b, maxHP.get().b),
                lerp((health - 20) / 16d, highHP.get().a, maxHP.get().a));
        }
    }
    private int lerp(double delta, double min, double max) {
        return (int) Math.round(min + (max - min) * delta);
    }
}
