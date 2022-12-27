package kassuk.addon.blackout.timers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Made by OLEPOSSU / Raksamies
*/

public class BlockTimerList {
    public List<BlockTimer> timers;

    public BlockTimerList() {
        MeteorClient.EVENT_BUS.subscribe(this);
        timers = new ArrayList<>();
    }

    public void add(BlockPos pos, double time) {timers.add(new BlockTimer(pos, time));}
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        List<BlockTimer> toRemove = new ArrayList<>();
        timers.forEach(item -> {
            item.update((float) event.frameTime);
            if (!item.isValid()) {
                toRemove.add(item);
            }
        });
        toRemove.forEach(timers::remove);
    }

    public Map<BlockPos, Double[]> get() {
        Map<BlockPos, Double[]> map = new HashMap<>();
        for (BlockTimer timer : timers) {
            map.put(timer.pos, new Double[]{timer.time, timer.ogTime});
        }
        return map;
    }

    public boolean contains(BlockPos pos) {
        for (BlockTimer timer : timers) {
            if (timer.pos.equals(pos)) {return true;}
        }
        return false;
    }

    private class BlockTimer {
        public BlockPos pos;
        public double time;
        public double ogTime;

        public BlockTimer(BlockPos pos, double time) {
            this.pos = pos;
            this.time = time;
            this.ogTime = time;
        }

        public void update(float delta) {time -= delta;}
        public boolean isValid() {return time > 0;}
    }
}
