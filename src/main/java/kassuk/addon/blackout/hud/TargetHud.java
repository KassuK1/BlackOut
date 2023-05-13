package kassuk.addon.blackout.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.RenderUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author KassuK
 * @author OLEPOSSU
 */
public class TargetHud extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Scale to render at")
        .defaultValue(1)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<SettingColor> bgColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Background Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(0, 0, 0, 150))
        .build()
    );
    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Text Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<SettingColor> healthColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Health Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> absorptionColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Absorption Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 0, 255))
        .build()
    );

    public static final HudElementInfo<TargetHud> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "TargetHud", "A target hud the fuck you thinkin bruv.", TargetHud::new);

    public TargetHud() {
        super(INFO);
    }

    private AbstractClientPlayerEntity target;
    private String renderName = null;
    private Identifier renderSkin = null;
    private float renderHealth;
    private float renderAbsorption;
    private float renderPing;

    private double scaleProgress = 0;

    @Override
    public void render(HudRenderer renderer) {
        int height = 100;
        int width = 200;
        setSize(width * scale.get(), height * scale.get());
        updateTarget();
        MatrixStack stack = new MatrixStack();

        if (renderName == null) {return;}

        scaleProgress = MathHelper.clamp(scaleProgress + (target == null ? -renderer.delta : renderer.delta), 0, 1);
        float scaleAnimation = (float) (scaleProgress * scaleProgress * scaleProgress);

        stack.translate(x + (1 - scaleAnimation) * getWidth() / 2f, y + (1 - scaleAnimation) * getHeight() / 2f, 0);
        stack.scale((float) (scaleAnimation * scale.get()), (float) (scaleAnimation * scale.get()), 1);

        // Background
        RenderUtils.rounded(stack, 15, 15, width - 30, height - 30, 15, 10, bgColor.get().getPacked());
        // Face
        RenderSystem.setShaderTexture(0, renderSkin);
        PlayerSkinDrawer.draw(stack, 20, 18, 32, false, false);

        // Name
        RenderUtils.text(renderName, stack, 60, 20, textColor.get().getPacked());

        // Ping
        RenderUtils.text(renderPing + "ms", stack, 60, 30, textColor.get().getPacked());

        // Health
        RenderUtils.text(String.valueOf(Math.round((renderHealth + renderAbsorption) * 10) / 10f), stack, 30, 81 - mc.textRenderer.fontHeight / 2f, textColor.get().getPacked());

        float barStart = Math.max(mc.textRenderer.getWidth(String.valueOf(renderHealth + renderAbsorption)),
            mc.textRenderer.getWidth("36.0")) + 38;

        // Health Bar
        if (renderHealth > 0) {
            RenderUtils.rounded(stack, barStart, 80, MathHelper.clamp(renderHealth / 20, 0, 1) * (width - 30 - barStart), 2, 3, 10, healthColor.get().getPacked());
        }
        if (renderAbsorption > 0) {
            RenderUtils.rounded(stack, barStart, 80, MathHelper.clamp(renderAbsorption / 16, 0, 1) * (width - 30 - barStart), 2, 3, 10, absorptionColor.get().getPacked());
        }
    }


    private void updateTarget() {
        AbstractClientPlayerEntity closest = null;
        double distance = Double.MAX_VALUE;

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) {continue;}
            if (Friends.get().isFriend(player)) {continue;}

            double d = mc.player.distanceTo(player);

            if (d < distance) {
                closest = player;
            }
        }

        target = closest;

        if (target != null) {
            renderName = target.getName().getString();
            renderSkin = target.getSkinTexture();
            renderHealth = target.getHealth();
            renderAbsorption = target.getAbsorptionAmount();

            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(target.getUuid());
            renderPing = playerListEntry == null ? -1 : playerListEntry.getLatency();
        }
    }
}
