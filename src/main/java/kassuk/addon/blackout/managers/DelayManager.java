package kassuk.addon.blackout.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.ArrayList;
import java.util.List;


/**
 * @author OLEPOSSU
 */
public class DelayManager {

    private final List<Delayed> tasks;

    public DelayManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        tasks = new ArrayList<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (!tasks.isEmpty()) {
            List<Delayed> toRemove = new ArrayList<>();
            for (int i = 0; i < tasks.size(); i++) {
                Delayed task = tasks.get(i);
                if (System.currentTimeMillis() > task.time) {
                    task.runnable.run();
                    toRemove.add(task);
                }
            }
            toRemove.forEach(tasks::remove);
        }
    }

    public void add(Runnable run, double delay) {
        tasks.add(new Delayed(run, delay));
    }

    static class Delayed {
        private final Runnable runnable;
        private final long time;

        public Delayed(Runnable runnable, double delay) {
            this.runnable = runnable;
            this.time = Math.round(System.currentTimeMillis() + delay * 1000);
        }
    }
}



