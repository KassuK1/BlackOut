package kassuk.addon.blackout.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * @author OLEPOSSU
 */

public class OnGroundManager {

    private boolean onGround;

    public OnGroundManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        this.onGround = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            onGround = ((PlayerMoveC2SPacket) event.packet).isOnGround();
        }
    }

    public boolean isOnGround() {
        return onGround;
    }
}



