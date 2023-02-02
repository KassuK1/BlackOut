package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class PacketFly extends Module {

    public PacketFly() {
        super(BlackOut.BLACKOUT, "Packet Fly", "Flies with packets");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> packets = sgGeneral.add(new IntSetting.Builder()
        .name("Packets")
        .description("How many packets to send every movement tick.")
        .defaultValue(5)
        .min(1)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Distance to travel each packet.")
        .defaultValue(0.2873)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> fastVertical = sgGeneral.add(new BoolSetting.Builder()
        .name("Fast Vertical")
        .description("Sends multiple packets every movement tick.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> downSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Down Speed")
        .description("How fast to fly down.")
        .defaultValue(0.62)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> upSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Up Speed")
        .description("How fast to fly up.")
        .defaultValue(0.62)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> onGroundSpoof = sgGeneral.add(new BoolSetting.Builder()
        .name("On Ground Spoof")
        .description("Spoofs on ground in packets.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> onGround = sgGeneral.add(new BoolSetting.Builder()
        .name("On Ground")
        .description("Should we tell the server that u are onground.")
        .defaultValue(false)
        .visible(onGroundSpoof::get)
        .build()
    );
    private final Setting<Integer> xzBound = sgGeneral.add(new IntSetting.Builder()
        .name("XZ Bound")
        .description("Bounds offset horizontally.")
        .defaultValue(512)
        .sliderRange(-1337, 1337)
        .build()
    );
    private final Setting<Integer> yBound = sgGeneral.add(new IntSetting.Builder()
        .name("Y Bound")
        .description("Bounds offset vertically.")
        .defaultValue(215)
        .sliderRange(-1337, 1337)
        .build()
    );
    private final Setting<Double> antiKick = sgGeneral.add(new DoubleSetting.Builder()
        .name("Anti Kick")
        .description("Slowly glides down.")
        .defaultValue(1)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> antiKickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Anti Kick Delay")
        .description("Tick delay between moving down.")
        .defaultValue(10)
        .min(1)
        .sliderRange(0, 100)
        .build()
    );

    int ticks = 0;
    int id = -1;
    int sent = 0;
    int rur = 0;
    String info = null;
    Map<Integer, Vec3d> validPos = new HashMap<>();
    List<PlayerMoveC2SPacket> validPackets = new ArrayList<>();

    @Override
    public void onActivate() {
        super.onActivate();
        ticks = 0;
        validPos = new HashMap<>();
    }
    @EventHandler
    private void onTick(TickEvent.Post e) {
        ticks++;
        rur++;
        if (rur % 20 == 0) {
            info = "Packets: " + sent;
            sent = 0;
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (mc.player == null || mc.world == null) {return;}
        boolean shouldAntiKick = ticks % antiKickDelay.get() == 0;
        double x = 0, y = shouldAntiKick ? -0.04 * antiKick.get() : 0, z = 0;
        double[] result = getYaw(mc.player.input.movementForward, mc.player.input.movementSideways);

        if (mc.options.jumpKey.isPressed() && y == 0) {y = upSpeed.get();}
        else if (mc.options.sneakKey.isPressed()) {
            shouldAntiKick = false;
            y = -downSpeed.get();
        }
        if (result[1] != 0 && y == 0) {
            x = Math.cos(Math.toRadians(result[0] + 90));
            z = Math.sin(Math.toRadians(result[0] + 90));
        }
        Vec3d motion = new Vec3d(0, 0, 0);

        for (int i = 0; i < (shouldAntiKick ? 1 : (y == 0 || fastVertical.get() ? packets.get() : 1)); i++) {
            motion = motion.add(x * speed.get(), y, z * speed.get());
            send(motion.add(mc.player.getPos()), new Vec3d(xzBound.get() * Math.cos(Math.toRadians(result[0] + 90)), yBound.get(), xzBound.get() * Math.sin(Math.toRadians(result[0] + 90))),
                onGroundSpoof.get() ? onGround.get() : mc.player.isOnGround());
        }

        ((IVec3d) e.movement).set(motion.x, motion.y, motion.z);
    }

    @EventHandler
    public void onSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            if (!validPackets.contains((PlayerMoveC2SPacket) event.packet)) {
                event.cancel();
            } else {
                sent++;
            }
        } else {
            sent++;
        }
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive e) {
        if (e.packet instanceof PlayerPositionLookS2CPacket packet) {
            if (mc.player != null) {
                Vec3d vec = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                if (validPos.containsKey(packet.getTeleportId()) && validPos.get(packet.getTeleportId()).equals(vec)) {
                    mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                    e.cancel();
                    validPos.remove(packet.getTeleportId());
                    return;
                }
                id = packet.getTeleportId();
                mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));
            }
        }
    }
    @Override
    public String getInfoString() {
        return info;
    }

    private void send(Vec3d pos, Vec3d bounds, boolean onGround) {
        PlayerMoveC2SPacket.PositionAndOnGround normal = new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, onGround);
        PlayerMoveC2SPacket.PositionAndOnGround bound = new PlayerMoveC2SPacket.PositionAndOnGround(pos.x + bounds.x, pos.y + bounds.y, pos.z + bounds.z, onGround);

        validPackets.add(normal);
        mc.player.networkHandler.sendPacket(normal);
        validPos.put(id + 1, pos);

        validPackets.add(bound);
        mc.player.networkHandler.sendPacket(bound);
        if (id < 0) {return;}

        id++;
        mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(id));
    }

    double[] getYaw(double f, double s) {
        double yaw = mc.player.getYaw();
        double move;
        if (f > 0) {
            move = 1;
            yaw += s > 0 ? -45 : s < 0 ? 45 : 0;
        } else if (f < 0) {
            move = 1;
            yaw += s > 0 ? -135 : s < 0 ? 135 : 180;
        } else {
            move = s != 0 ? 1 : 0;
            yaw += s > 0 ? -90 : s < 0 ? 90 : 0;
        }
        return new double[]{yaw, move};
    }
}
