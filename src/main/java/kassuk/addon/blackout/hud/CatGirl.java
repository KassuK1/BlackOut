package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CatGirl extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> girlScale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Catgirl Scale")
        .description("Modify the size of the Catgirl.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<SideMode> side = sgGeneral.add(new EnumSetting.Builder<SideMode>()
        .name("Kill Message Mode")
        .description("What kind of messages to send.")
        .defaultValue(SideMode.Right)
        .build()
    );
    private final Identifier catgirl = Identifier.of("blackout", "catgirl.png");

    public static final HudElementInfo<CatGirl> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "catgirl", "It's a Cat girl what do you want", CatGirl::new);

    public CatGirl() {super(INFO);}
    @Override
    public void render(HudRenderer renderer) {
        setSize(450 * girlScale.get(),755  * girlScale.get());
        MatrixStack matrixStack = new MatrixStack();

        GL.bindTexture(catgirl);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x + (side.get() == SideMode.Left ? girlScale.get() * 450 : 0),y, girlScale.get() * (side.get() == SideMode.Left ? girlScale.get() * -450 : 450), girlScale.get() * 755, new Color(255, 255, 255, 255));
        Renderer2D.TEXTURE.render(matrixStack);
    }
    public enum SideMode {
        Right,
        Left
    }
}
