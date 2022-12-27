package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class ButtonAura extends Module {

    public ButtonAura() {super(BlackOut.ANARCHY, "ButtonAura", ".");}
    private SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> packetRotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Packet Rotate")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description(".")
        .defaultValue(4)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description(".")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> coolDown = sgGeneral.add(new DoubleSetting.Builder()
        .name("Cooldown")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 150))
        .build()
    );
    private BlockTimerList timers = new BlockTimerList();
    private double timer = 0;

    @EventHandler
    private void onRender(Render3DEvent event) {
        timer = Math.min(delay.get(), timer + event.frameTime);
        Map<BlockPos, Double[]> map = timers.get();
        map.forEach((pos, time) -> {
            event.renderer.box(new Box(pos.getX() + 0.5 - time[0] / time[1] / 2, pos.getY() + 0.5 - time[0] / time[1] / 2, pos.getZ() + 0.5 - time[0] / time[1] / 2,
                pos.getX() + 0.5 + time[0] / time[1] / 2, pos.getY() + 0.5 + time[0] / time[1] / 2, pos.getZ() + 0.5 + time[0] / time[1] / 2),
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both, 0);
        });
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        BlockPos block = getButton(mc.player.getEyePos(), range.get());

        if (block != null && timer >= delay.get()) {
            timers.add(block, coolDown.get());
            timer = 0;
            if (packetRotate.get()) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    (float) Rotations.getYaw(OLEPOSSUtils.getMiddle(block)), (float) Rotations.getPitch(OLEPOSSUtils.getMiddle(block)),
                    Managers.ONGROUND.isOnGround()));
            }
            if (rotate.get()) {
                Rotations.rotate(Rotations.getYaw(OLEPOSSUtils.getMiddle(block)), Rotations.getPitch(OLEPOSSUtils.getMiddle(block)));
            }
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5), OLEPOSSUtils.closestDir(block, mc.player.getEyePos()), block, false), 0));
            if (swing.get()) {
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
    }

    private BlockPos getButton(Vec3d vec, double r) {
        int c = (int) (Math.ceil(r) + 1);
        BlockPos closest = null;
        float closestDist = -1;
        for (int x = -c; x <= c; x++) {
            for (int y = -c; y <= c; y++) {
                for (int z = -c; z <= c; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock().equals(Blocks.DARK_OAK_BUTTON) && !timers.contains(pos)) {
                        float dist = (float) OLEPOSSUtils.distance(vec, OLEPOSSUtils.getMiddle(pos));
                        if (dist <= r && (closest == null || dist < closestDist)) {
                            closest = pos;
                            closestDist = dist;
                        }
                    }
                }
            }
        }
        return closest;
    }
}
