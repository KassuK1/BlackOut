package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import com.example.addon.managers.BlockTimerList;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ScaffoldPlus extends Module {
    public ScaffoldPlus() {
        super(BlackOut.ANARCHY, "Scaffold+", "KasumsSoft blockwalk");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> ssprint = sgGeneral.add(new BoolSetting.Builder()
        .name("StopSprint")
        .description("Stops you from sprinting")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> usetimer = sgGeneral.add(new BoolSetting.Builder()
        .name("Use timer")
        .description("Should we use timer")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .visible(usetimer::get)
        .name("Timer")
        .description("Speed but better")
        .defaultValue(1.088)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay.")
        .defaultValue(0.3)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Boolean> tower = sgGeneral.add(new BoolSetting.Builder()
        .name("Tower")
        .description("Smooth vertical movement")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> towerSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Tower Speed")
        .description(".")
        .defaultValue(0.2)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Integer> extrapolation = sgGeneral.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("Predicts movement.")
        .defaultValue(3)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> safewalk = sgGeneral.add(new BoolSetting.Builder()
        .name("SafeWalk")
        .description("Should SafeWalk be used")
        .defaultValue(true)
        .build()
    );

    private BlockTimerList timers = new BlockTimerList();
    private Vec3d motion = null;
    private float placeTimer;

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(1);
        if (Modules.get().get(SafeWalk.class).isActive()) {
            Modules.get().get(SafeWalk.class).toggle();
        }
    }

    @Override
    public void onActivate() {
        if (safewalk.get() && !Modules.get().get(SafeWalk.class).isActive()){
            Modules.get().get(SafeWalk.class).toggle();
        }
        motion = new Vec3d(0, 0, 0);
    }
    @EventHandler
    private void onMove(Render3DEvent event) {
        timers.update((float) event.frameTime);
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        if (mc.player != null && mc.world != null) {
            motion = event.movement;
            if (tower.get() && motion.y > 0) {
                ((IVec3d) event.movement).setY(Math.min(mc.player.getY() + towerSpeed.get(), Math.ceil(mc.player.getY())));
            }

            if (ssprint.get()) mc.player.setSprinting(false);
            if (usetimer.get()) Modules.get().get(Timer.class).setOverride(timer.get());

            List<BlockPos> blocks = getBlocks();

            for (BlockPos position : blocks) {
                place(position);
            }
        }
    }

    private List<BlockPos> getBlocks() {
        List<BlockPos> list = new ArrayList<>();
        double x = motion.x;
        double z = motion.z;
        ChatUtils.sendMsg(Text.of(x + "  " + z));
        Vec3d vec = mc.player.getPos();
        for (int i = 0; i < extrapolation.get(); i++) {
            vec = vec.add(x, 0, z);
            if (inside(getBox(vec))) {
                break;
            } else {
                addBlocks(list, vec);
            }
        }
        return list;
    }

    private void addBlocks(List<BlockPos> list, Vec3d vec) {
        BlockPos pos = new BlockPos(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z)).down();
        if (!timers.isPlaced(pos) && air(pos) && !list.contains(pos)) {
            list.add(pos);
        }
    }

    private Box getBox(Vec3d vec) {return new Box(vec.x - 0.3, vec.y, vec.z - 0.3, vec.x + 0.3, vec.y + mc.player.getEyeY(), vec.z + 0.3);}

    private boolean inside(Box bb) {
        return mc.world.getBlockCollisions(mc.player, bb).iterator().hasNext();
    }

    private boolean air(BlockPos pos) {return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);}

    private void place(BlockPos pos) {
        timers.add(pos, delay.get());
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
            new BlockHitResult(OLEPOSSUtils.getMiddle(pos), Direction.UP, pos, false), 0));
        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }
}
