package com.example.addon.managers;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BlockTimerList {
    public List<BlockTimer> timers;

    public BlockTimerList() {
        timers = new ArrayList<>();
    }

    public void add(BlockPos pos, double time) {timers.add(new BlockTimer(pos, time));}

    public void update(float delta) {
        List<BlockTimer> toRemove = new ArrayList<>();
        timers.forEach(item -> {
            item.update(delta);
            if (!item.isValid()) {
                toRemove.add(item);
            }
        });
        toRemove.forEach(timers::remove);
    }

    public boolean isPlaced(BlockPos pos) {
        for (BlockTimer timer : timers) {
            if (timer.pos.equals(pos)) {return true;}
        }
        return false;
    }

    private class BlockTimer {
        public BlockPos pos;
        public double time;

        public BlockTimer(BlockPos pos, double time) {
            this.pos = pos;
            this.time = time;
        }

        public void update(float delta) {time -= delta;}
        public boolean isValid() {return time > 0;}
    }
}
