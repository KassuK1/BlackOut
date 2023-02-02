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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/*
Made by OLEPOSSU / Raksamies
*/
//Todo yaw step

public class RotationManager {

    public Box target = null;
    public double timer = 0;
    public float[] lastDir = null;
    public int priority = 1000;
    public float[] rot = new float[]{0, 0};

    public RotationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        timer -= event.frameTime;
        if (timer > 0 && target != null) {
            rot = new float[]{(float) Rotations.getYaw(OLEPOSSUtils.getMiddle(target)), (float) Rotations.getPitch(OLEPOSSUtils.getMiddle(target))};
        } else if (target != null) {
            target = null;
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
                float[] next = new float[]{(float) Rotations.getYaw(OLEPOSSUtils.getMiddle(target)), (float) Rotations.getPitch(OLEPOSSUtils.getMiddle(target))};
                ((MixinPlayerMoveC2SPacket) packet).setYaw(next[0]);
                ((MixinPlayerMoveC2SPacket) packet).setPitch(next[1]);
                lastDir = next;
            }
        }
    }

    public void end(Box box) {
        if (target != null && box.minX == target.minX && box.minY == target.minY && box.minZ == target.minZ &&
            box.maxX == target.maxX && box.maxY == target.maxY && box.maxZ == target.maxZ) {
            priority = 1000;
        }
    }
    public void end(BlockPos pos) {
        end(OLEPOSSUtils.getBox(pos));
    }

    public void start(Box box, int p) {
        if (p <= priority) {
            priority = p;
            target = box;
            timer = 1;

            if (lastDir == null || !SettingUtils.rotationCheck(lastDir[0], lastDir[1], box)) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(0, 0, Managers.ONGROUND.isOnGround()));
            }
        }
    }
    public void start(BlockPos pos, int p) {
        start(OLEPOSSUtils.getBox(pos), p);
    }
    public void endAny() {
        target = null;
        timer = 0;
    }
}



