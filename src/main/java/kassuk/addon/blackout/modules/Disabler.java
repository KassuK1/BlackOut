package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.PlayerPositionLookS2CPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/*
Made by OLEPOSSU / Raksamies
*/

public class Disabler extends BlackOutModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public Disabler() {
        super(BlackOut.BLACKOUT, "Disabler", "Allows you to use high timer on some servers (mpvp).");
    }
    int id = -1;
    Map<Integer, Vec3d> validPos = new HashMap<>();
    boolean ignore = false;
    Random r = new Random();

    // Listeners

    @Override
    public void onActivate() {
        super.onActivate();
    }
    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onMove(PlayerMoveEvent event) {
        if (event.movement.y < -0.0625) {
            ((IVec3d) event.movement).setY(-0.0625);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onSend(PacketEvent.Send event) {
        if (mc.player != null && mc.world != null) {
            if (event.packet instanceof PlayerMoveC2SPacket packet) {
                if (packet.changesPosition()) {
                    if (OLEPOSSUtils.distance(new Vec3d(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getY())), mc.player.getPos()) < 30){
                        if (!ignore) {
                            event.cancel();
                            validPos.put(id + 1, new Vec3d(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ())));
                            ignore = true;
                            mc.player.networkHandler.sendPacket(packet);
                            ignore = false;
                            double yaw = r.nextInt(-180, 180);
                            double x = Math.cos(Math.toRadians(yaw + 90)) * 1000;
                            double z = Math.sin(Math.toRadians(yaw + 90)) * 1000;
                            sendBounds(mc.player.getPos(), new Vec3d(x, 0, z), mc.player.isOnGround());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
            if (mc.player != null) {
                Vec3d vec = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                if (validPos.containsKey(packet.getTeleportId()) && validPos.get(packet.getTeleportId()).equals(vec)) {
                    mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                    event.cancel();
                    validPos.remove(packet.getTeleportId());
                    return;
                }
                id = packet.getTeleportId();
                mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                ((PlayerPositionLookS2CPacketAccessor) packet).setPitch(mc.player.getPitch());
                ((PlayerPositionLookS2CPacketAccessor) packet).setYaw(mc.player.getYaw());
            }
        }
    }

    void sendBounds(Vec3d pos, Vec3d bounds, boolean onGround) {
        PlayerMoveC2SPacket.PositionAndOnGround bound = new PlayerMoveC2SPacket.PositionAndOnGround(pos.x + bounds.x, pos.y + bounds.y, pos.z + bounds.z, onGround);
        ignore = true;
        mc.player.networkHandler.sendPacket(bound);
        ignore = false;
        if (id < 0) {return;}

        id++;
        mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(id));
    }
}

