package kassuk.addon.blackout.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class RenderUtils {
    private static final VertexConsumerProvider.Immediate vertex = VertexConsumerProvider.immediate(new BufferAllocator(2048));

    public static void rounded(MatrixStack stack, float x, float y, float w, float h, float radius, int p, int color) {

        Matrix4f matrix4f = stack.peek().getPositionMatrix();

        float a = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float r = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float b = (float) ColorHelper.Argb.getBlue(color) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        corner(x + w, y, radius, 360, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x, y, radius, 270, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x, y + h, radius, 180, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x + w, y + h, radius, 90, p, r, g, b, a, bufferBuilder, matrix4f);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void corner(float x, float y, float radius, int angle, float p, float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
        for (float i = angle; i > angle - 90; i -= 90 / p) {
            bufferBuilder.vertex(matrix4f, (float) (x + Math.cos(Math.toRadians(i)) * radius), (float) (y + Math.sin(Math.toRadians(i)) * radius), 0).color(r, g, b, a);
        }
    }

    public static void text(String text, MatrixStack stack, float x, float y, int color) {
        mc.textRenderer.draw(text, x, y, color, false, stack.peek().getPositionMatrix(), vertex, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        vertex.draw();
    }

    public static void quad(MatrixStack stack, float x, float y, float w, float h, int color) {
        Matrix4f matrix4f = stack.peek().getPositionMatrix();

        float a = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float r = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float b = (float) ColorHelper.Argb.getBlue(color) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x + w, y, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix4f, x, y, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix4f, x, y + h, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix4f, x + w, y + h, 0).color(r, g, b, a);


        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }
}
