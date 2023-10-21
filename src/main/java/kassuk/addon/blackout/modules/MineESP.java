package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/**
 * @author OLEPOSSU
 */

public class MineESP extends BlackOutModule {
    public MineESP() {
        super(BlackOut.BLACKOUT, "Mine ESP,", "Renders a box at blocks being mined by other players.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Only renders inside this range.")
        .defaultValue(10)
        .min(0)
        .sliderRange(0, 50)
        .build()
    );
    private final Setting<Double> maxTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Max Time")
        .description("Removes rendered box after this time.")
        .defaultValue(10)
        .min(0)
        .sliderRange(0, 50)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    public final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the outline.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final List<Render> renders = new ArrayList<>();
    Render render = null;

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (render != null && contains()) render = null;

        renders.removeIf(r -> System.currentTimeMillis() > r.time + Math.round(maxTime.get() * 1000) || (render != null && r.id == render.id) || !OLEPOSSUtils.solid2(r.pos));

        if (render != null) {
            renders.add(render);
            render = null;
        }

        renders.forEach(r -> {
            double delta = Math.min((System.currentTimeMillis() - r.time) / (maxTime.get() * 1000d), 1);
            event.renderer.box(getBox(r.pos, getProgress(Math.min(delta * 4, 1))), getColor(sideColor.get(), 1 - delta), getColor(lineColor.get(), 1 - delta), shapeMode.get(), 0);
        });
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockBreakingProgressS2CPacket packet) {
            render = new Render(packet.getPos(), packet.getEntityId(), System.currentTimeMillis());
        }
    }

    private boolean contains() {
        for (Render r : renders) {
            if (r.id == render.id && r.pos.equals(render.pos)) return true;
        }
        return false;
    }

    private Color getColor(Color color, double delta) {
        return new Color(color.r, color.g, color.b, (int) Math.floor(color.a * delta));
    }

    private double getProgress(double delta) {
        return 1 - Math.pow(1 - (delta), 5);
    }

    private Box getBox(BlockPos pos, double progress) {
        return new Box(pos.getX() + 0.5 - progress / 2, pos.getY() + 0.5 - progress / 2,pos.getZ() + 0.5 - progress / 2, pos.getX() + 0.5 + progress / 2, pos.getY() + 0.5 + progress / 2, pos.getZ() + 0.5 + progress / 2);
    }

    private record Render(BlockPos pos, int id, long time) {}
}
