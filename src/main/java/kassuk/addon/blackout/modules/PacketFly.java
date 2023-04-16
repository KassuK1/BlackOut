package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class PacketFly extends BlackOutModule {

    public PacketFly() {
        super(BlackOut.BLACKOUT, "Packet Fly", "Flies with packets");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFly = settings.createGroup("Fly");
    private final SettingGroup sgPhase = settings.createGroup("Phase");

    //  Fly Page
    private final Setting<Integer> packets = sgFly.add(new IntSetting.Builder()
        .name("Fly Packets")
        .description("How many packets to send every movement tick.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> speed = sgFly.add(new DoubleSetting.Builder()
        .name("Fly Speed")
        .description("Distance to travel each packet.")
        .defaultValue(0.2873)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> fastVertical = sgFly.add(new BoolSetting.Builder()
        .name("Fast Vertical Fly")
        .description("Sends multiple packets every movement tick while going up.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> downSpeed = sgFly.add(new DoubleSetting.Builder()
        .name("Fly Down Speed")
        .description("How fast to fly down.")
        .defaultValue(0.062)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> upSpeed = sgFly.add(new DoubleSetting.Builder()
        .name("Fly Up Speed")
        .description("How fast to fly up.")
        .defaultValue(0.062)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    //  Phase Page
    private final Setting<Integer> phasePackets = sgPhase.add(new IntSetting.Builder()
        .name("Phase Packets")
        .description("How many packets to send every movement tick.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> phaseSpeed = sgPhase.add(new DoubleSetting.Builder()
        .name("Phase Speed")
        .description("Distance to travel each packet.")
        .defaultValue(0.2873)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> phaseFastVertical = sgPhase.add(new BoolSetting.Builder()
        .name("Fast Vertical Phase")
        .description("Sends multiple packets every movement tick while going up.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> phaseDownSpeed = sgPhase.add(new DoubleSetting.Builder()
        .name("Phase Down Speed")
        .description("How fast to fly down.")
        .defaultValue(0.062)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> phaseUpSpeed = sgPhase.add(new DoubleSetting.Builder()
        .name("Phase Up Speed")
        .description("How fast to fly up.")
        .defaultValue(0.062)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    //  General Page
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
        .defaultValue(0)
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
    int packetsToSend = 0;
    Random random = new Random();
    String info = null;
    Map<Integer, Vec3d> validPos = new HashMap<>();
    List<PlayerMoveC2SPacket> validPackets = new ArrayList<>();

    public boolean moving = false;

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

        boolean phasing = isPhasing();
        mc.player.noClip = phasing;

        packetsToSend += phasing ? phasePackets.get() : packets.get();

        boolean shouldAntiKick = ticks % antiKickDelay.get() == 0;

        double yaw = getYaw();
        double motion = phasing ? phaseSpeed.get() : speed.get();

        double x = 0, y = 0, z = 0;

        if (jumping()) {
            y = phasing ? phaseUpSpeed.get() : upSpeed.get();
        } else if (sneaking()) {
            y = phasing ? -phaseDownSpeed.get() : -downSpeed.get();
        }

        if (y != 0) {
            moving = false;
        }

        if (moving) {
            x = Math.cos(Math.toRadians(yaw + 90)) * motion;
            z = Math.sin(Math.toRadians(yaw + 90)) * motion;
        } else {
            if (phasing && !phaseFastVertical.get()) {
                packetsToSend = Math.min(packetsToSend, 1);
            }
            if (!phasing && !fastVertical.get()){
                packetsToSend = Math.min(packetsToSend, 1);
            }
        }

        Vec3d newPosition = new Vec3d(0, 0, 0);
        boolean antiKickSent = false;
        for (; packetsToSend >= 1; packetsToSend--) {
            newPosition = newPosition.add(x, 0, z);

            if (shouldAntiKick && !phasing && y >= 0 && !antiKickSent) {
                newPosition = newPosition.add(0, antiKick.get() * -0.04, 0);
                antiKickSent = true;
            } else {
                newPosition = newPosition.add(0, y, 0);
            }

            send(newPosition.add(mc.player.getPos()), getBounds(), getOnGround());

            if (x == 0 && z == 0 && y == 0) {
                break;
            }
        }

        ((IVec3d) e.movement).set(newPosition.x, newPosition.y, newPosition.z);

        packetsToSend = Math.min(packetsToSend, 1);


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
    Vec3d getBounds() {
        int yaw = random.nextInt(0, 360);
        return new Vec3d(Math.cos(Math.toRadians(yaw)) * xzBound.get(), yBound.get(), Math.sin(Math.toRadians(yaw)) * xzBound.get());
    }
    boolean getOnGround() {
        return onGroundSpoof.get() ? onGround.get() : mc.player.isOnGround();
    }
    boolean isPhasing() {
        return OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox());
    }
    boolean jumping() {return mc.options.jumpKey.isPressed();}
    boolean sneaking() {return mc.options.sneakKey.isPressed();}
    void send(Vec3d pos, Vec3d bounds, boolean onGround) {
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

    double getYaw() {
        double f = mc.player.input.movementForward, s = mc.player.input.movementSideways;

        double yaw = mc.player.getYaw();
        if (f > 0) {
            moving = true;
            yaw += s > 0 ? -45 : s < 0 ? 45 : 0;
        } else if (f < 0) {
            moving = true;
            yaw += s > 0 ? -135 : s < 0 ? 135 : 180;
        } else {
            moving = s != 0;
            yaw += s > 0 ? -90 : s < 0 ? 90 : 0;
        }
        return yaw;
    }
}
