package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class ButtonAura extends BlackOutModule {

    public ButtonAura() {super(BlackOut.BLACKOUT, "ButtonAura", "Presses nearby buttons.");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Range for clicking buttons.")
        .defaultValue(4)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between button clicks.")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> coolDown = sgGeneral.add(new DoubleSetting.Builder()
        .name("Cooldown")
        .description("Cooldown for each button.")
        .defaultValue(10)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("Color of the render?")
        .defaultValue(new SettingColor(255, 0, 0, 150))
        .build()
    );
    BlockTimerList timers = new BlockTimerList();
    double timer = 0;

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

            if (SettingUtils.shouldRotate(RotationType.Interact)) {
                Managers.ROTATION.start(block, 10, RotationType.Interact);
            }

            SettingUtils.swing(SwingState.Pre, SwingType.Interact, Hand.MAIN_HAND);

            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5), OLEPOSSUtils.closestDir(block, mc.player.getEyePos()), block, false), 0));

            SettingUtils.swing(SwingState.Post, SwingType.Interact, Hand.MAIN_HAND);

            if (SettingUtils.shouldRotate(RotationType.Interact)) {
                Managers.ROTATION.end(block);
            }
        }
    }

    BlockPos getButton(Vec3d vec, double r) {
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
