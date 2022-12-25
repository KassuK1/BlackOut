package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Vec3;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import static org.lwjgl.opengl.GL11.*;

/*
Made by OLEPOSSU / Raksamies
*/

public class ESPPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> players = sgGeneral.add(new BoolSetting.Builder()
        .name("Players")
        .description(".")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> playerWalls = sgGeneral.add(new BoolSetting.Builder()
        .name("Players Trough Walls")
        .description(".")
        .defaultValue(true)
        .build()
    );
    public final Setting<Double> playerScale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Player Scale")
        .description(".")
        .defaultValue(1)
        .range(0.1, 10)
        .sliderRange(0.1, 10)
        .visible(players::get)
        .sliderMax(20)
        .build()
    );
    public Renderer3D renderer = null;
    public MatrixStack matrixStack = null;

    public ESPPlus() {super(BlackOut.ANARCHY, "ESP+", ".");}

    /*
    @EventHandler
    private void onRender(Render3DEvent event) {
        renderer = event.renderer;

        if (matrixStack != null && renderer != null && mc.player != null) {
            Vec3d vec = mc.player.getPos();


            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glLineWidth(1f);
            glColor4d(1.0, 1.0, 1.0, 1.0);
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glLineWidth(1f);

            renderer.render(matrixStack);

            glPopAttrib();
            glPopMatrix();
        }
    }

     */
}
