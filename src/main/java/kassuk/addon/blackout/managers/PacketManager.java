package kassuk.addon.blackout.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

/*
Made by OLEPOSSU / Raksamies
*/

public class PacketManager {

    int packets;
    float timer;
    int sent;

    public PacketManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        this.packets = -1;
        this.timer = 0;
        this.sent = 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        timer += event.frameTime;
        if (timer >= 0.25) {
            packets = Math.round(sent * 4 / timer);
            timer = 0;
            sent = 0;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Send event) {
        sent++;
    }

    public int getSent() {return packets;}
    public double getTimer() {return timer;}
}



