package kassuk.addon.blackout.managers;

import kassuk.addon.blackout.mixins.MixinPlayerMoveC2SPacket;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/*
Made by OLEPOSSU / Raksamies
*/
//Todo yaw step

public class RotationManager {

    public Box target = null;
    public double timer = 0;
    public float[] lastDir = null;

    public RotationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        timer -= event.frameTime;
        if (timer > 0 && target != null) {
            float[] rot = new float[]{(float) Rotations.getYaw(OLEPOSSUtils.getMiddle(target)), (float) Rotations.getPitch(OLEPOSSUtils.getMiddle(target))};
            mc.player.headYaw = rot[0];
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet && target != null && timer > 0) {
            if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround) {
                float[] next = new float[]{(float) Rotations.getYaw(OLEPOSSUtils.getMiddle(target)), (float) Rotations.getPitch(OLEPOSSUtils.getMiddle(target))};
                ((MixinPlayerMoveC2SPacket) packet).setYaw(next[0]);
                ((MixinPlayerMoveC2SPacket) packet).setPitch(next[1]);
                lastDir = next;
            }
            if (packet instanceof PlayerMoveC2SPacket.Full) {
                ((MixinPlayerMoveC2SPacket) packet).setYaw((float) Rotations.getYaw(OLEPOSSUtils.getMiddle(target)));
                ((MixinPlayerMoveC2SPacket) packet).setPitch((float) Rotations.getPitch(OLEPOSSUtils.getMiddle(target)));
            }
        }
    }

    public void end(Box box) {
        if (target != null && box.minX == target.minX && box.minY == target.minY && box.minZ == target.minZ &&
            box.maxX == target.maxX && box.maxY == target.maxY && box.maxZ == target.maxZ) {
            target = null;
            timer = 0;
        }
    }

    public void start(Box box) {
        target = box;
        timer = 1;
    }
}



