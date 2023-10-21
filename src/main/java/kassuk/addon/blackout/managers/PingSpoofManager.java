package kassuk.addon.blackout.managers;

import kassuk.addon.blackout.modules.PingSpoof;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class PingSpoofManager {
    private final List<DelayedPacket> delayed = new ArrayList<>();
    private DelayedPacket delayed1 = null;
    private DelayedPacket delayed2 = null;

    public PingSpoofManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        List<DelayedPacket> toSend = new ArrayList<>();

        if (delayed1 != null) {
            delayed.add(delayed1);
            delayed1 = null ;
        }
        if (delayed2 != null) {
            delayed.add(delayed2);
            delayed2 = null ;
        }

        for (DelayedPacket d : delayed) {
            if (System.currentTimeMillis() > d.time) toSend.add(d);
        }

        toSend.forEach(d -> {
            mc.getNetworkHandler().sendPacket(d.packet);
            delayed.remove(d);
        });

        toSend.clear();
    }

    public void addKeepAlive(long id) {
        delayed1 = new DelayedPacket(new KeepAliveC2SPacket(id), System.currentTimeMillis() + Modules.get().get(PingSpoof.class).ping.get());
    }

    public void addPong(int id) {
        delayed2 = new DelayedPacket(new CommonPongC2SPacket(id), System.currentTimeMillis() + Modules.get().get(PingSpoof.class).ping.get());
    }

    private record DelayedPacket(Packet<?> packet, long time) {}
}
