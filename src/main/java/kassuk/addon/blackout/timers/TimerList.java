package kassuk.addon.blackout.timers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TimerList<T> {
    public final List<Timer<T>> timers = new ArrayList<>();

    public void add(T value, double time) {
        timers.add(new Timer<>(value, time));
    }

    public void update() {
        timers.removeIf(item -> System.currentTimeMillis() > item.endTime);
    }

    public void clear() {
        timers.clear();
    }

    public Map<T, Double> getMap() {
        Map<T, Double> map = new HashMap<>();
        for (Timer<T> timer : timers) {
            map.put(timer.value, timer.time);
        }

        return map;
    }

    public List<T> getList() {
        List<T> l = new ArrayList<>();
        for (Timer<T> timer : timers) {
            l.add(timer.value);
        }
        return l;
    }

    public T remove(Predicate<? super Timer<T>> predicate) {
        for (Timer<T> timer : timers) {
            if (predicate.test(timer)) {
                timers.remove(timer);
                return timer.value;
            }
        }
        return null;
    }

    public boolean contains(T value) {
        for (Timer<T> timer : timers) {
            if (timer.value.equals(value)) return true;
        }
        return false;
    }

    public static class Timer<T> {
        public final T value;
        public final long endTime;
        public final double time;

        public Timer(T value, double time) {
            this.value = value;
            this.endTime = System.currentTimeMillis() + Math.round(time * 1000);
            this.time = time;
        }
    }
}
