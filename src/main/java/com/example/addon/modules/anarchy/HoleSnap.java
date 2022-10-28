package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
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
        .description("Speed but better")
        .defaultValue(1)
        .min(0)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("Range")
        .description("Range")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> downRange = sgGeneral.add(new IntSetting.Builder()
        .name("DownRange")
        .description(".")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private Direction[] horizontals = new Direction[] {
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    };
    BlockPos singleHole;


    public HoleSnap() {
        super(Addon.ANARCHY, "HoleSnap", "So u don't need to die");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        singleHole = findHole();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        if (mc.player != null && mc.world != null) {
            BlockPos hole = single.get() ? singleHole : findHole();
            if (hole != null) {
                Modules.get().get(Timer.class).setOverride(timer.get());
                double yaw =
                    Math.cos(Math.toRadians(
                        getAngle(hole.getX() + 0.5, hole.getZ() + 0.5, mc.player.getX(), mc.player.getZ())  + 90.0f));
                double pit =
                    Math.sin(Math.toRadians(
                        getAngle(hole.getX() + 0.5, hole.getZ() + 0.5, mc.player.getX(), mc.player.getZ()) + 90.0f));
                if (Math.abs(mc.player.getX() - hole.getX() - 0.5) < 0.200 && Math.abs(mc.player.getZ() - hole.getZ() - 0.5) < 0.200) {
                    if (Math.floor(mc.player.getY()) == hole.getY()) {
                        this.toggle();
                    } else {
                        mc.player.addVelocity(-mc.player.getVelocity().x, 0, -mc.player.getVelocity().z);
                    }
                } else {
                    mc.player.addVelocity(-mc.player.getVelocity().x, 0, -mc.player.getVelocity().z);
                    double x = speed.get() * yaw / 100;
                    double dX = Math.abs(hole.getX() + 0.5 - mc.player.getX());
                    double z = speed.get() * pit / 100;
                    double dZ = Math.abs(hole.getZ() + 0.5 - mc.player.getZ());
                    mc.player.addVelocity(Math.min(x, dX), 0, Math.min(z, dZ));
                }
            } else {
                this.toggle();
            }
        }
    }

    private BlockPos findHole() {
        Map<BlockPos, Double> holeMap = new HashMap<>();
        List<BlockPos> holes = new ArrayList<>();
        if (isHole(mc.player.getBlockPos())) {return mc.player.getBlockPos();}
        for (int y = -downRange.get(); y < 0; y++) {
            for (int x = -range.get(); x <= range.get(); x++) {
                for (int z = -range.get(); z <= range.get(); z++) {
                    BlockPos position = mc.player.getBlockPos().add(x, y, z);
                    if (isHole(position)) {
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
        if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {return false;}
        for (Direction dir : horizontals) {
            if (mc.world.getBlockState(pos.offset(dir)).getBlock() == Blocks.AIR) {return false;}
        }
        if (mc.world.getBlockState(pos.up()).getBlock() != Blocks.AIR) {return false;}
        return mc.world.getBlockState(pos.down()).getBlock() != Blocks.AIR;
    }

    private float getAngle(double x, double z, double x2, double z2)
    {
        //credits to 3arthqu4ke
        float yaw = (float) (Math.atan2(z - z2, x - x2) * 180.0 / Math.PI) - 90.0f;
        float prevYaw = mc.player.prevYaw;
        float diff = yaw - prevYaw;

        if (diff < -180.0f || diff > 180.0f)
        {
            float round = Math.round(Math.abs(diff / 360.0f));
            diff = diff < 0.0f ? diff + 360.0f * round : diff - (360.0f * round);
        }

        return prevYaw + diff;
    }
}
