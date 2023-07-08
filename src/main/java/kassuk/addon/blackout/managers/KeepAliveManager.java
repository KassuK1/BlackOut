package kassuk.addon.blackout.managers;

import kassuk.addon.blackout.modules.PingSpoof;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class KeepAliveManager {
    // id, time
    private final Map<Long, Long> delayed = new HashMap<>();

    public KeepAliveManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        List<Long> toSend = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : delayed.entrySet()) {
            toSend.add(entry.getKey());
        }
        toSend.forEach(id -> {
            delayed.remove(id);
            mc.getNetworkHandler().sendPacket(new KeepAliveC2SPacket(id));
        });
        toSend.clear();
    }

    public void add(long id) {
        delayed.put(id, System.currentTimeMillis() + Modules.get().get(PingSpoof.class).ping.get());
    }
}
