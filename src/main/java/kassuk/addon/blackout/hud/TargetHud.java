package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.player.PlayerEntity;

import static meteordevelopment.meteorclient.MeteorClient.mc;

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

    public TargetHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(200 * scale.get(),65 * scale.get());
        PlayerEntity playerEntity = getClosest();
        if (playerEntity != null){
            double health = playerEntity.getHealth() + playerEntity.getAbsorptionAmount();
            renderer.quad(x,y,200 * scale.get() *scale.get(),65 * scale.get() * scale.get(), color.get());
            renderer.text(playerEntity.getName().getString(),x + 10, y + 5, textcolor.get(),shadow.get(), scale.get());
            renderer.text(String.valueOf(health),x + 10, y + 30, textcolor.get(),shadow.get(), scale.get());
            renderer.quad(x + 10, y + 50,180/36f * scale.get() * scale.get() * health,10 * scale.get() * scale.get(), bar.get());
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
