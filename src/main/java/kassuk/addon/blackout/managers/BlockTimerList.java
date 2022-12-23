package kassuk.addon.blackout.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

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
