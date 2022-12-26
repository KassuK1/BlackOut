package kassuk.addon.blackout.modules.anarchy;

import com.mojang.blaze3d.platform.GlStateManager;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.render.RenderAfterWorldEvent;
import meteordevelopment.meteorclient.renderer.PostProcessRenderer;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.Shader;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Vec3;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

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
    public final Setting<Boolean> texture = sgGeneral.add(new BoolSetting.Builder()
        .name("Texture")
        .description(".")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> walls = sgGeneral.add(new BoolSetting.Builder()
        .name("Trough Walls")
        .description(".")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> blend = sgGeneral.add(new BoolSetting.Builder()
        .name("Blend")
        .description(".")
        .defaultValue(true)
        .build()
    );
    public Renderer3D renderer = null;
    public List<MatrixStack> toRender = new ArrayList<>();
    public Shader red = new Shader("red.vert", "red.frag");

    public ESPPlus() {
        super(BlackOut.ANARCHY, "ESP+", ".");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
    }
}
