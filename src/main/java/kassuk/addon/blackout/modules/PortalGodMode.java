package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

public class PortalGodMode extends BlackOutModule {
    public PortalGodMode() {super(BlackOut.BLACKOUT, "Portal God Mode", "Prevents taking damage while in portals");}
    @EventHandler
    private void onSend(PacketEvent.Send event) {
        if (mc.player != null && event.packet instanceof TeleportConfirmC2SPacket packet) {
            event.cancel();
        }
    }
}
