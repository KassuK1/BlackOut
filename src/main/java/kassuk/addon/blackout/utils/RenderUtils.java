package kassuk.addon.blackout.utils;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class RenderUtils {
    // these 2 vertex things are from meteor
    private static final BufferBuilder buffer = new BufferBuilder(2048);
    private static final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(buffer);

    public static void renderText(Vec3d vec3d, String text) {
        MatrixStack stack = new MatrixStack();
        stack.translate(5, 5, 5);
        Matrix4f matrix = stack.peek().getPositionMatrix();;
        mc.textRenderer.draw("SUS", -mc.textRenderer.getWidth(text) / 2f, -mc.textRenderer.fontHeight / 2f, new Color(255, 255, 255, 255).getRGB(), false, matrix, immediate, true, 69, 15728880);
    }

}
