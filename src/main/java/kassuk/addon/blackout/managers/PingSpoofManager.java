package kassuk.addon.blackout.managers;

import kassuk.addon.blackout.modules.PingSpoof;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class PingSpoofManager {
    private final List<DelayedPacket> delayed = new ArrayList<>();
    
    public PingSpoofManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        List<DelayedPacket> toSend = new ArrayList<>();

        synchronized (delayed) {
            for (DelayedPacket d : delayed) {
                if (System.currentTimeMillis() > d.time) toSend.add(d);
            }

            toSend.forEach(d -> {
                mc.getNetworkHandler().sendPacket(d.packet);
                delayed.remove(d);
            });
        }

        toSend.clear();
    }

    public void addKeepAlive(long id) {
        delayed.add(new DelayedPacket(new KeepAliveC2SPacket(id), System.currentTimeMillis() + Modules.get().get(PingSpoof.class).ping.get()));
    }

    public void addPong(int id) {
        delayed.add(new DelayedPacket(new PlayPongC2SPacket(id), System.currentTimeMillis() + Modules.get().get(PingSpoof.class).ping.get()));
    }

    private record DelayedPacket(Packet<?> packet, long time) {}
}
