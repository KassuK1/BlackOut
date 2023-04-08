package kassuk.addon.blackout.timers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.ArrayList;
import java.util.List;

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
        timers.removeIf(item -> !item.isValid());
    }

    public boolean contains(int val) {
        for (IntTimer timer : timers) {
            if (timer.value == val) {return true;}
        }
        return false;
    }

    static class IntTimer {
        public final int value;
        public final double endTime;

        public IntTimer(int value, double time) {
            this.value = value;
            this.endTime = System.currentTimeMillis() + time * 1000;
        }
        public boolean isValid() {
            return System.currentTimeMillis() <= endTime;
        }
    }
}
