package kassuk.addon.blackout.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class DelayManager {

    private List<Delayed> tasks;
    public DelayManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        this.tasks = new ArrayList<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        List<Delayed> toRemove = new ArrayList<>();
        if (!tasks.isEmpty()) {
            for (Delayed task : tasks) {
                if (task != null) {
                    if (task.shouldRun()) {
                        task.run();
                        toRemove.add(task);
                    } else {
                        task.update(event.frameTime);
                    }
                }
            }
            toRemove.forEach(tasks::remove);
        }
    }

    public void add(Runnable run, double delay) {
        tasks.add(new Delayed(run, delay));
    }
    public void clear() {tasks.clear();}

    static class Delayed {
        private final Runnable runnable;
        private double time;

        public Delayed(Runnable runnable, double delay) {
            this.runnable = runnable;
            this.time = delay;
        }
        public void update(double delta) {
            time = Math.max(0, time - delta);
        }
        public boolean shouldRun() {
            return time <= 0;
        }
        public void run() {
            runnable.run();
        }
    }
}



