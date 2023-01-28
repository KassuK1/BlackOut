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
        List<IntTimer> toRemove = new ArrayList<>();
        for (int i = 0; i < timers.size(); i++) {
            IntTimer item = timers.get(i);
            item.update(delta);
            if (!item.isValid()) {
                toRemove.add(item);
            }
        }
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

        public IntTimer(int value, double time) {
            this.value = value;
            this.time = time;
            this.ogTime = time;
        }

        public void update(double delta) {time -= delta;}
        public boolean isValid() {return time > 0;}
    }
}
