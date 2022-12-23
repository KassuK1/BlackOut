package kassuk.addon.blackout.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class RotationManager {

    private List<BORotation> rotations;
    private float[] target = null;
    private boolean ignore = false;

    public RotationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        this.rotations = new ArrayList<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            if (packet.changesLook() && !ignore) {

            }
        }
    }

    private void send(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket.Full packet) {
            ignore = true;
            event.cancel();
        }
        if (event.packet instanceof PlayerMoveC2SPacket.LookAndOnGround packet) {
            ignore = true;
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(target[0], target[1], packet.isOnGround()));
            event.cancel();
        }
    }

    private void calc() {
        if (!rotations.isEmpty()) {
            for (BORotation rot : rotations) {

            }
        }
    }

    private class BORotation {
        final int priority;
        Vec3d target;

        public BORotation(Vec3d pos, int priority) {
            this.priority = priority;
            this.target = pos;
        }


    }
}



