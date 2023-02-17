package kassuk.addon.blackout.managers;

import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.globalsettings.RotationSettings;
import kassuk.addon.blackout.mixins.MixinPlayerMoveC2SPacket;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.RotationUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/*
Made by OLEPOSSU / Raksamies
*/
//Todo yaw step

public class RotationManager {

    public Box target = null;
    public double timer = 0;
    public float[] lastDir = new float[]{0, 0};
    public int priority = 1000;
    public float[] rot = new float[]{0, 0};
    public RotationSettings settings = null;
    public boolean unsent = false;
    public static List<Rotation> history = new ArrayList<>();

    public RotationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (settings == null) {
            settings = Modules.get().get(RotationSettings.class);
        }
        timer -= event.frameTime;
        if (timer > 0 && target != null && lastDir != null) {
            rot = lastDir;
        } else if (target != null) {
            target = null;
            priority = 1000;
        } else {
            priority = 1000;
        }
    }

    @EventHandler
    private void onMovePre(SendMovementPacketsEvent.Pre event) {
        unsent = true;
    }
    @EventHandler
    private void onMovePost(SendMovementPacketsEvent.Post event) {
        if (unsent && target != null && timer > 0) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(0, 0, Managers.ONGROUND.isOnGround()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet && target != null && timer > 0) {
            unsent = false;
            float[] next = new float[]{(float) RotationUtils.nextYaw(lastDir[0], Rotations.getYaw(OLEPOSSUtils.getMiddle(target)), settings.yawStep.get()), (float) RotationUtils.nextPitch(lastDir[1], Rotations.getPitch(OLEPOSSUtils.getMiddle(target)), settings.pitchStep.get())};

            ((MixinPlayerMoveC2SPacket) packet).setLook(true);
            ((MixinPlayerMoveC2SPacket) packet).setYaw(next[0]);
            ((MixinPlayerMoveC2SPacket) packet).setPitch(next[1]);
            addHistory(next[0], next[1]);

            lastDir = next;
        }
    }
    public boolean isTarget(Box box) {
        return target != null && box.minX == target.minX && box.minY == target.minY && box.minZ == target.minZ &&
            box.maxX == target.maxX && box.maxY == target.maxY && box.maxZ == target.maxZ;
    }
    public void end(Box box) {
        if (isTarget(box)) {
            priority = 1000;
        }
    }
    public void end(BlockPos pos) {
        end(OLEPOSSUtils.getBox(pos));
    }

    public boolean start(Box box, int p, RotationType type) {
        if (p <= priority && settings != null) {
            priority = p;
            target = box;
            timer = 1;

            if (SettingUtils.rotationCheckHistory(box, type)) {
                return true;
            }
            if (!isTarget(box) && SettingUtils.rotationCheck(mc.player.getEyePos(), RotationUtils.nextYaw(lastDir[0], Rotations.getYaw(OLEPOSSUtils.getMiddle(target)), settings.yawStep.get()),
                RotationUtils.nextPitch(lastDir[1], Rotations.getPitch(OLEPOSSUtils.getMiddle(target)), settings.pitchStep.get()), target, type)) {

                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(0, 0, Managers.ONGROUND.isOnGround()));
                return true;
            }
        }
        return false;
    }
    public boolean start(BlockPos pos, int p, RotationType type) {
        return start(OLEPOSSUtils.getBox(pos), p, type);
    }
    public void addHistory(double yaw, double pitch) {
        if (history.size() > 10) {
            for (int i = history.size() - 9; i > 0; i++) {
                history.remove(history.size() - 1);
            }
        } else if (history.size() == 10) {
            history.remove(9);
        }
        history.add(0, new Rotation(yaw, pitch, mc.player.getEyePos()));
    }
    public void endAny() {
        target = null;
        timer = 0;
    }
    public record Rotation(double yaw, double pitch, Vec3d vec) {}
}



