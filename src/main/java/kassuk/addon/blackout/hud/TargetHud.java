package kassuk.addon.blackout.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.RenderUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.UUID;

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
    private final Setting<Double> damageTilt = sgGeneral.add(new DoubleSetting.Builder()
        .name("Damage Tilt")
        .description("How many degrees should the box be rotated when enemy takes damage.")
        .defaultValue(10)
        .min(0)
        .sliderRange(0, 45)
        .build()
    );

    public static final HudElementInfo<TargetHud> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "TargetHud", "A target hud the fuck you thinkin bruv.", TargetHud::new);

    public TargetHud() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    private AbstractClientPlayerEntity target;
    private String renderName = null;
    private Identifier renderSkin = null;
    private float renderHealth;
    private float renderPing;

    private double scaleProgress = 0;
    private long damageTime;

    private UUID lastTarget = null;
    private float lastHp = 0;
    private boolean popped = false;

    @EventHandler(priority = 10000)
    private void onTick(TickEvent.Pre event) {
        if (target != null) {
            if (target.getUuid().equals(lastTarget)) {
                float diff = Math.max(lastHp - target.getHealth() - target.getAbsorptionAmount(), 0);

                if (diff > 1) {
                    damageTime = System.currentTimeMillis();
                }
            }
            lastTarget = target.getUuid();
            lastHp = popped ? 0 : target.getHealth() + target.getAbsorptionAmount();
            popped = false;
        } else {
            lastTarget = null;
            lastHp = 0;
            damageTime = 0;
        }
    }

    @EventHandler(priority = 10000)
    private void onReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket packet)) {return;}

        if (packet.getStatus() != 35) {return;}

        Entity entity = packet.getEntity(mc.world);

        if (entity instanceof PlayerEntity player && player == target) {
            popped = true;
        }
    }

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

        // Damage tilt
        float tilt = (float) (Math.max(0, 500 - System.currentTimeMillis() + damageTime) / 500f * damageTilt.get());
        stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((tilt)));

        // Background
        RenderUtils.rounded(stack, 15, 15, width - 30, height - 30, 15, 10, bgColor.get().getPacked());
        // Face
        RenderSystem.setShaderTexture(0, renderSkin);
        PlayerSkinDrawer.draw(stack, 20, 18, 32, false, false);

        // Name
        RenderUtils.text(renderName, stack, 60, 20, textColor.get().getPacked());

        // Ping
        RenderUtils.text(Math.round(renderPing) + "ms", stack, 60, 30, textColor.get().getPacked());

        // Health
        RenderUtils.text(String.valueOf(Math.round((renderHealth) * 10) / 10f), stack, 20, 81 - mc.textRenderer.fontHeight / 2f, textColor.get().getPacked());

        float barAnimation = MathHelper.lerp(mc.getTickDelta() / 10, lastHp, renderHealth);

        float barStart = Math.max(mc.textRenderer.getWidth(String.valueOf(Math.round((renderHealth) * 10) / 10f)),
            mc.textRenderer.getWidth("36.0")) + 28;

        // Health Bar
        if (barAnimation > 0) {
            RenderUtils.rounded(stack, barStart, 80, MathHelper.clamp(barAnimation / 20, 0, 1) * (width - 30 - barStart), 2, 3, 10, healthColor.get().getPacked());
        }
        if (barAnimation > 20) {
            RenderUtils.rounded(stack, barStart, 80, MathHelper.clamp((barAnimation - 20) / 16, 0, 1) * (width - 30 - barStart), 2, 3, 10, absorptionColor.get().getPacked());
        }
    }

    private void updateTarget() {
        target = null;
        if (mc.world == null) {return;}

        AbstractClientPlayerEntity closest = null;
        double distance = Double.MAX_VALUE;

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) {continue;}
            if (Friends.get().isFriend(player)) {continue;}

            double d = mc.player.distanceTo(player);

            if (d < distance) {
                closest = player;
                distance = d;
            }
        }

        target = closest;
        if (target == null && isInEditor()) {
            target = mc.player;
        }

        if (target != null) {
            renderName = target.getName().getString();
            renderSkin = target.getSkinTexture();
            renderHealth = target.getHealth() + target.getAbsorptionAmount();

            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(target.getUuid());
            renderPing = playerListEntry == null ? -1 : playerListEntry.getLatency();
        }
    }
}
