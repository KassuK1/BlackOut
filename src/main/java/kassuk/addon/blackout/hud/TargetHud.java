package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.RenderUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author KassuK
 * @author OLEPOSSU
 */

public class TargetHud extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("What mode to use for the TargetHud.")
        .defaultValue(Mode.Blackout)
        .build()
    );

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
        .defaultValue(new SettingColor(0, 0, 0, 200))
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

    private final Map<AbstractClientPlayerEntity, Integer> tog = new HashMap<>();

    @EventHandler(priority = 10000)
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        List<AbstractClientPlayerEntity> toRemove = new ArrayList<>();

        for (Map.Entry<AbstractClientPlayerEntity, Integer> entry : tog.entrySet()) {
            if (mc.world.getPlayers().contains(entry.getKey()) && !entry.getKey().isSpectator() && entry.getKey().getHealth() > 0) {
                continue;
            }

            toRemove.add(entry.getKey());
        }

        toRemove.forEach(tog::remove);

        mc.world.getPlayers().forEach(player -> {
            if (player.isOnGround()) {
                if (tog.containsKey(player)) {
                    tog.replace(player, tog.get(player) + 1);
                } else {
                    tog.put(player, 1);
                }
            }
        });

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
        if (mode.get() == Mode.Blackout) {
            int height = 100;
            int width = 200;

            setSize(width * scale.get(), height * scale.get());

            updateTarget();
            if (renderName == null) {
                return;
            }

            MatrixStack stack = new MatrixStack();

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
            drawFace(renderer, scaleAnimation * scale.get().floatValue(), x + (1 - scaleAnimation) * getWidth() / 2f, y + (1 - scaleAnimation) * getHeight() / 2f, tilt);

            // Name
            RenderUtils.text(renderName, stack, 60, 20, textColor.get().getPacked());

            // Ping
            RenderUtils.text(Math.round(renderPing) + "ms", stack, 60, 30, textColor.get().getPacked());

            // Health
            RenderUtils.text(String.valueOf(Math.round((renderHealth) * 10) / 10f), stack, 20, 81 - mc.textRenderer.fontHeight / 2f, textColor.get().getPacked());

            float barAnimation = MathHelper.lerp(mc.getRenderTickCounter().getTickDelta(true) / 10, lastHp, renderHealth);

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
        if (mode.get() == Mode.ExhibitionOld) {
            int height = 60;
            int width = 240;
            setSize(width * scale.get(), height * scale.get());

            updateTarget();
            MatrixStack stack = new MatrixStack();

            if (target == null || renderName == null) {
                return;
            }

            stack.translate(x, y, 0);
            stack.scale((float) (scale.get() * 1f), (float) (scale.get() * 1f), 1);


            // Background
            RenderUtils.quad(stack, 0, 0, width, height, bgColor.get().getPacked());

            // Face
            RenderUtils.quad(stack, 1, 1, 58, 58, new Color(102, 102, 102, 255).getPacked());

            drawFace(renderer, scale.get().floatValue(), x, y, 0);

            // Name
            stack.scale(2.0f,2.0f,1);
            RenderUtils.text(renderName, stack, 33, 2, textColor.get().getPacked());

            // Health
            stack.scale(0.5f,0.5f,1);
            RenderUtils.text(Math.round((renderHealth) * 10) / 10f + " Dist: " + Math.round(mc.player.distanceTo(target) * 10) / 10f, stack, 66, 35 - mc.textRenderer.fontHeight / 2f, textColor.get().getPacked());

            // Bar
            stack.scale(2, 2, 1);

            int progress = (int) (Math.ceil(MathHelper.clamp(renderHealth, 0, 20)));

            for (int i = 0; i < 10; i++) {
                RenderUtils.quad(stack, 33 + i * 8, 11, 3 * Math.min(progress, 2), 3, new Color(204, 204, 0, 255).getPacked());
                progress -= 2;

                if (progress <= 0) {
                    break;
                }
            }

            stack.scale(0.5f,0.5f,1);
            // Misc info
            RenderUtils.text("Yaw: " + Math.round((target.getYaw()) * 10) / 10f + " Pitch: " + Math.round(target.getPitch() * 10) / 10f + " BodyYaw: " + Math.round((target.getBodyYaw()) * 10) / 10f , stack, 66, 45 - mc.textRenderer.fontHeight / 2f, textColor.get().getPacked());
            RenderUtils.text("TOG: " + (tog.getOrDefault(target, 0)) + " HURT: " + ((target.hurtTime) * 10) / 10f + " TE: " + target.age, stack, 66, 55 - mc.textRenderer.fontHeight / 2f, textColor.get().getPacked());
        }
        if (mode.get() == Mode.Exhibition){
            int height = 60;
            int width = 190;
            setSize(width * scale.get(), height * scale.get());

            updateTarget();
            MatrixStack stack = new MatrixStack();

            if (target == null || renderName == null) {
                return;
            }

            stack.translate(x, y, 0);
            stack.scale((float) (scale.get() * 1f), (float) (scale.get() * 1f), 1);

            // Background
            RenderUtils.quad(stack, -2, -2, width + 4, height + 4, new Color(52, 52, 52, 255).getPacked());
            RenderUtils.quad(stack, -1, -1, width + 2, height + 2, new Color(32, 32, 32, 255).getPacked());
            RenderUtils.quad(stack, 0, 0, width, height, new Color(52, 52, 52, 255).getPacked());

            //PlayerModel

            // Name
            stack.scale(1.5f,1.5f,1);
            RenderUtils.text(renderName, stack, 41, 2, textColor.get().getPacked());

            // Health and Distance
            stack.scale(0.5f,0.5f,1);
            RenderUtils.text(Math.round((renderHealth) * 10) / 10f + " Dist: " + Math.round(mc.player.distanceTo(target) * 10) / 10f, stack, 83, 40 - mc.textRenderer.fontHeight / 2f, textColor.get().getPacked());

            // Bar
            stack.scale(2, 2, 1);

            int progress = (int) (Math.ceil(MathHelper.clamp(renderHealth, 0, 20)));

            for (int i = 0; i < 10; i++) {
                RenderUtils.quad(stack, 41 + i * 8, 12, 3 * Math.min(progress, 2), 3, new Color(204, 204, 0, 255).getPacked());
                progress -= 2;

                if (progress <= 0) {
                    break;
                }
            }

            //Armor
            stack.scale(0.9f,0.9f,1);
            MatrixStack drawStack = renderer.drawContext.getMatrices();
            drawStack.push();

            drawStack.translate(x, y, 0);
            drawStack.scale(scale.get().floatValue() * 1.35f, scale.get().floatValue() * 1.35f, 1);

            for (int i = 0; i < 4; i++) {
                ItemStack itemStack = target.getInventory().armor.get(i);

                renderer.drawContext.drawItem(itemStack, (3 - i) * 20 + 42, 25);
            }

            //Item
            ItemStack itemStack = target.getMainHandStack();

            renderer.drawContext.drawItem(itemStack, 122, 25);

            drawStack.pop();
            // gayer model
        }
    }

    private void drawFace(HudRenderer renderer, float scale, double x, double y, float tilt) {
        MatrixStack drawStack = renderer.drawContext.getMatrices();

        drawStack.push();

        drawStack.translate(x, y, 0);
        drawStack.scale(scale, scale, 1);
        drawStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tilt));

        PlayerSkinDrawer.draw(renderer.drawContext, renderSkin,20, 18, 32, false, false);

        drawStack.pop();
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
            renderSkin = target.getSkinTextures().texture();
            renderHealth = target.getHealth() + target.getAbsorptionAmount();

            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(target.getUuid());
            renderPing = playerListEntry == null ? -1 : playerListEntry.getLatency();
        }
    }
    public enum Mode {
        Blackout,
        ExhibitionOld,
        Exhibition
    }
}
