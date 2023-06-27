package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.globalsettings.SwingSettings;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.TorchBlock;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * @author KassuK
 */

public class LightsOut extends BlackOutModule {
    public LightsOut() {
        super(BlackOut.BLACKOUT, "Lights Out", "A tribute to Reliant.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Tick Delay")
        .description("Delay between breaking torches.")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when breaking a torch.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> swingHand = sgGeneral.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(swing::get)
        .build()
    );

    private double timer = 0;

    @EventHandler
    private void onTick(TickEvent.Post event) {
        BlockPos block = getLightSource(mc.player.getEyePos(), SettingUtils.getMineRange());
        if (block != null && timer >= delay.get()) {
            timer = 0;

            SettingUtils.mineSwing(SwingSettings.MiningSwingState.Start);

            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                block, Direction.UP));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                block, Direction.UP));

            SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
            if (swing.get()) clientSwing(swingHand.get(), Hand.MAIN_HAND);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        timer = Math.min(delay.get(), timer + event.frameTime);
    }

    private BlockPos getLightSource(Vec3d vec, double r) {
        int c = (int) (Math.ceil(r) + 1);
        BlockPos closest = null;
        float closestDist = -1;
        for (int x = -c; x <= c; x++) {
            for (int y = -c; y <= c; y++) {
                for (int z = -c; z <= c; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    //best code ever fr
                    if (mc.world.getBlockState(pos).getBlock() instanceof TorchBlock) {
                        float dist = (float) vec.distanceTo(pos.toCenterPos());
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
