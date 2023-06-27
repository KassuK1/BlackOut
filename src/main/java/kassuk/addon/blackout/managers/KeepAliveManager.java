package kassuk.addon.blackout.managers;

import kassuk.addon.blackout.modules.PingSpoof;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class KeepAliveManager {
    private long nextId = 0;
    private long time = 0;
    private boolean waitingToSend = false;

    public KeepAliveManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (!waitingToSend) return;
        if (System.currentTimeMillis() < time) return;
        if (mc.getNetworkHandler() == null || mc.player == null || mc.world == null) return;

        mc.getNetworkHandler().sendPacket(new KeepAliveC2SPacket(nextId));
        waitingToSend = false;
    }

    public void set(long id) {
        nextId = id;
        time = System.currentTimeMillis() + Modules.get().get(PingSpoof.class).ping.get();
        waitingToSend = true;
    }
}
