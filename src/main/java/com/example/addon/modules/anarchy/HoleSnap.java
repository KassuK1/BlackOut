package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Made by OLEPOSSU / Raksamies
*/

public class HoleSnap extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> single = sgGeneral.add(new BoolSetting.Builder()
        .name("Single")
        .description("Only chooses target hole once")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> jump = sgGeneral.add(new BoolSetting.Builder()
        .name("Jump")
        .description("Jumps to the hole (very useful)")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> jumpCoolDown = sgGeneral.add(new IntSetting.Builder()
        .name("Jump Cooldown")
        .description("Ticks between jumps")
        .defaultValue(5)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
        .name("Speed")
        .description("Speed")
        .defaultValue(27)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .name("Timer")
        .description("Sends packets faster")
        .defaultValue(1)
        .min(0)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("Range")
        .description("Horizontal range for finding holes")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> downRange = sgGeneral.add(new IntSetting.Builder()
        .name("Down Range")
        .description("Vertical range for finding holes")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> depth = sgGeneral.add(new IntSetting.Builder()
        .name("Hole Depth")
        .description("How deep a hole has to be")
        .defaultValue(3)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Integer> coll = sgGeneral.add(new IntSetting.Builder()
        .name("Collisions to disable")
        .description("0 = doesn't disable")
        .defaultValue(15)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> rDisable = sgGeneral.add(new IntSetting.Builder()
        .name("Rubberbands to disable")
        .description("0 = doesn't disable")
        .defaultValue(1)
        .sliderRange(0, 100)
        .build()
    );
    private Direction[] horizontals = new Direction[] {
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    };
    BlockPos singleHole;
    private int collisions;
    private int rubberbands;
    private int ticks;


    public HoleSnap() {
        super(BlackOut.ANARCHY, "HoleSnap", "For the time that you cant even press W");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        singleHole = findHole();
        rubberbands = 0;
        ticks = 0;
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && rDisable.get() > 0) {
            rubberbands++;
            if (rubberbands >= rDisable.get() && rDisable.get() > 0) {
                this.toggle();
                info("Toggled: Rubberbanding");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        if (mc.player != null && mc.world != null) {
            BlockPos hole = single.get() ? singleHole : findHole();
            if (hole != null && mc.world.getBlockState(singleHole).getBlock() == Blocks.AIR) {
                Modules.get().get(Timer.class).setOverride(timer.get());
                double yaw =
                    Math.cos(Math.toRadians(
                        getAngle(new Vec3d(hole.getX() + 0.5, hole.getY(), hole.getZ() + 0.5)) + 90.0f));
                double pit =
                    Math.sin(Math.toRadians(
                        getAngle(new Vec3d(hole.getX() + 0.5, hole.getY(), hole.getZ() + 0.5)) + 90.0f));

                if (mc.player.getX() == hole.getX() + 0.5 && mc.player.getZ() == hole.getZ() + 0.5) {
                    if (mc.player.getY() == hole.getY()) {
                        this.toggle();
                        info("Toggled: In hole");
                    } else if (OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(0, -0.05, 0))){
                        this.toggle();
                        info("Toggled: Hole unreachable");
                    } else {
                        ((IVec3d) event.movement).setXZ(0, 0);
                    }
                } else {
                    double x = speed.get() * yaw / 100;
                    double dX = hole.getX() + 0.5 - mc.player.getX();
                    double z = speed.get() * pit / 100;
                    double dZ = hole.getZ() + 0.5 - mc.player.getZ();
                    if (OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(x, 0, z))) {
                        collisions++;
                        if (collisions >= coll.get() && coll.get() > 0) {
                            this.toggle();
                            info("Toggled: Collided");
                        }
                    } else {
                        collisions = 0;
                    }
                    if (ticks > 0) {
                        ticks--;
                    } else if (OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(0, -0.05, 0)) && jump.get()) {
                        ticks = jumpCoolDown.get();
                        ((IVec3d) event.movement).setY(0.42);
                    }
                    ((IVec3d) event.movement).setXZ(Math.abs(x) < Math.abs(dX) ? x : dX, Math.abs(z) < Math.abs(dZ) ? z : dZ);
                }
            } else {
                this.toggle();
                info("Toggled: no hole found");
            }
        }
    }

    private BlockPos findHole() {
        Map<BlockPos, Double> holeMap = new HashMap<>();
        List<BlockPos> holes = new ArrayList<>();
        if (isHole(mc.player.getBlockPos())) {return mc.player.getBlockPos();}
        for (int y = -downRange.get(); y <= 0; y++) {
            for (int x = -range.get(); x <= range.get(); x++) {
                for (int z = -range.get(); z <= range.get(); z++) {
                    BlockPos position = mc.player.getBlockPos().add(x, y, z);
                    if (isHole(position) && ((x == 0 && z == 0 && y == 0) || y < 0)) {
                        holes.add(position);
                        holeMap.put(position, OLEPOSSUtils.distance(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()),
                            new Vec3d(position.getX() + 0.5, position.getY(), position.getZ() + 0.5)));
                    }
                }
            }
        }
        double closestDist = Integer.MAX_VALUE;
        BlockPos closestHole = null;
        if (holes.size() > 0) {
            for (BlockPos pos : holes) {
                if (closestHole == null) {
                    closestHole = pos;
                } else if (holeMap.get(pos) < closestDist) {
                    closestDist = holeMap.get(pos);
                    closestHole = pos;
                }
            }
        }
        return closestHole;
    }

    private boolean isHole(BlockPos pos) {
        for (Direction dir : horizontals) {
            if (mc.world.getBlockState(pos.offset(dir)).getBlock() == Blocks.AIR) {return false;}
        }
        for (int i = 0; i <= depth.get(); i++) {
            if (!air(pos.up(i))) {
                return false;
            }
        }
        return mc.world.getBlockState(pos.down()).getBlock() != Blocks.AIR;
    }

    private boolean air(BlockPos pos) {return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);}

    private float getAngle(Vec3d pos)
    {
        return (float) Rotations.getYaw(pos);
    }
}
