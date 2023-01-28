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

public class IntTimerList {
    public List<IntTimer> timers;
    public IntTimerList() {
        MeteorClient.EVENT_BUS.subscribe(this);
        timers = new ArrayList<>();
    }
    public IntTimerList(boolean autoUpdate) {
        if (autoUpdate) {
            MeteorClient.EVENT_BUS.subscribe(this);
        }
        timers = new ArrayList<>();
    }

    public void add(int val, double time) {timers.add(new IntTimer(val, time));}
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        update(event.frameTime);
    }

    public void update(double delta) {
        List<IntTimer> toRemove = new ArrayList<>();
        timers.forEach(item -> {
            if (!item.isValid()) {
                toRemove.add(item);
            }
        });
        toRemove.forEach(timers::remove);
    }

    public boolean contains(int val) {
        for (IntTimer timer : timers) {
            if (timer.value == val) {return true;}
        }
        return false;
    }

    static class IntTimer {
        public int value;
        public double time;
        public double ogTime;
        public long startTime;

        public IntTimer(int value, double time) {
            this.value = value;
            this.startTime = System.currentTimeMillis();
            this.time = time;
            this.ogTime = time;
        }
        public boolean isValid() {return System.currentTimeMillis() <= startTime + time * 1000;}
    }
}
