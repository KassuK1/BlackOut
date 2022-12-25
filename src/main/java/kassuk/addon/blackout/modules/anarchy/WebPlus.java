package kassuk.addon.blackout.modules.anarchy;

import com.mojang.blaze3d.platform.GlStateManager;
import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.EndCrystalEntityRendererMixin;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class WebPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> x = sgGeneral.add(new IntSetting.Builder()
        .name("X")
        .description(".")
        .defaultValue(0)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> y = sgGeneral.add(new IntSetting.Builder()
        .name("Y")
        .description(".")
        .defaultValue(0)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> z = sgGeneral.add(new IntSetting.Builder()
        .name("Z")
        .description(".")
        .defaultValue(0)
        .sliderMax(100)
        .build()
    );


    public WebPlus() {
        super(BlackOut.ANARCHY, "Web+", "Places an web inside you automatically");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(1.0F, -2000000F);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager._depthMask(false);
        GL11.glLineWidth(3);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glClearStencil(0xF);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }
}
