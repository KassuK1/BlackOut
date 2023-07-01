package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.RenderUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class ArmorHudPlus extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Scale to render at.")
        .defaultValue(1)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Integer> rounding = sgGeneral.add(new IntSetting.Builder()
        .name("Rounding")
        .description("How rounded should the background be.")
        .defaultValue(50)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Boolean> bg = sgGeneral.add(new BoolSetting.Builder()
        .name("Background")
        .description("Renders a background behind armor pieces.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SettingColor> bgColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Background Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(0, 0, 0, 150))
        .build()
    );
    private final Setting<SettingColor> durColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Durability Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<DurMode> durMode = sgGeneral.add(new EnumSetting.Builder<DurMode>()
        .name("Durability Mode")
        .description("Where should durability be rendered at.")
        .defaultValue(DurMode.Bottom)
        .build()
    );

    public static final HudElementInfo<ArmorHudPlus> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "ArmorHud+", "A target hud the fuck you thinkin bruv.", ArmorHudPlus::new);

    public ArmorHudPlus() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.player == null) {return;}

        setSize(100 * scale.get() * 2, 28 * scale.get() * 2);
        MatrixStack stack = new MatrixStack();

        stack.translate(x, y, 0);
        stack.scale((float)(scale.get() * 2), (float)(scale.get() * 2), 1);

        if (bg.get()) {
            RenderUtils.rounded(stack, rounding.get() * 0.14f, rounding.get() * 0.14f, 100 - rounding.get() * 0.28f, 28 - rounding.get() * 0.28f, rounding.get() * 0.14f, 10, bgColor.get().getPacked());
        }

        MatrixStack drawStack = renderer.drawContext.getMatrices();
        drawStack.push();

        drawStack.translate(x, y, 0);
        drawStack.scale((float)(scale.get() * 2), (float)(scale.get() * 2), 1);

        for (int i = 3; i >= 0; i--) {
            ItemStack itemStack = mc.player.getInventory().armor.get(i);

            renderer.drawContext.drawItem(itemStack, i * 20 + 12, durMode.get() == DurMode.Top ? 10 : 0);

            if (itemStack.isEmpty()) {continue;}

            centeredText(stack,
                String.valueOf(Math.round(100 - (float) itemStack.getDamage() / itemStack.getMaxDamage() * 100f)),
                i * 20 + 20, durMode.get() == DurMode.Top ? 3 : 17, durColor.get().getPacked());
        }
        drawStack.pop();
    }

    private void centeredText(MatrixStack stack, String text, int x, int y, int color) {
        RenderUtils.text(text, stack, x - mc.textRenderer.getWidth(text) / 2f, y, color);
    }

    public enum DurMode {
        Top, // 3, 10
        Bottom // 0, 17
    }
}
