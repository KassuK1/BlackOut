package kassuk.addon.blackout.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.ArrayList;
import java.util.List;

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
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i) != null) {
                    Delayed item = tasks.get(i);
                    if (item != null) {
                        if (item.shouldRun()) {
                            item.run();
                            toRemove.add(item);
                        } else {
                            item.update((float) event.frameTime);
                        }
                    }
                }
            }
            toRemove.forEach(tasks::remove);
        }
    }

    public void add(Runnable run, float delay) {
        tasks.add(new Delayed(run, delay));
    }
    public void clear() {tasks.clear();}

    private class Delayed {
        private final Runnable runnable;
        private float time;

        public Delayed(Runnable runnable, float delay) {
            this.runnable = runnable;
            this.time = delay;
        }
        public void update(float delta) {
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



