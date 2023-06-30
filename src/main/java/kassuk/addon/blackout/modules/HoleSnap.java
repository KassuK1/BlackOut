package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.HoleType;
import kassuk.addon.blackout.utils.Hole;
import kassuk.addon.blackout.utils.HoleUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * @author OLEPOSSU
 */

public class HoleSnap extends BlackOutModule {
    public HoleSnap() {
        super(BlackOut.BLACKOUT, "Hole Snap", "For the times when you cant even press W.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgHole = settings.createGroup("Hole");

    //--------------------General--------------------//
    private final Setting<Boolean> jump = sgGeneral.add(new BoolSetting.Builder()
        .name("Jump")
        .description("Jumps to the hole (very useful).")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> jumpCoolDown = sgGeneral.add(new IntSetting.Builder()
        .name("Jump Cooldown")
        .description("Ticks between jumps.")
        .defaultValue(5)
        .min(0)
        .sliderMax(100)
        .visible(jump::get)
        .build()
    );
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("Range")
        .description("Horizontal range for finding holes.")
        .defaultValue(3)
        .range(0, 5)
        .sliderMax(5)
        .build()
    );
    private final Setting<Integer> downRange = sgGeneral.add(new IntSetting.Builder()
        .name("Down Range")
        .description("Vertical range for finding holes.")
        .defaultValue(3)
        .range(0, 5)
        .sliderMax(5)
        .build()
    );
    private final Setting<Integer> coll = sgGeneral.add(new IntSetting.Builder()
        .name("Collisions to disable")
        .description("0 = doesn't disable.")
        .defaultValue(15)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> rDisable = sgGeneral.add(new IntSetting.Builder()
        .name("Rubberbands to disable")
        .description("0 = doesn't disable.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );

    //--------------------Speed--------------------//
    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Movement Speed.")
        .defaultValue(0.2873)
        .min(0)
        .sliderMax(1)
        .build()
    );
    private final Setting<Boolean> boost = sgSpeed.add(new BoolSetting.Builder()
        .name("Speed Boost")
        .description("Jumps to the hole (very useful).")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> boostedSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Boosted Speed")
        .description("Movement Speed.")
        .defaultValue(0.5)
        .min(0)
        .sliderMax(1)
        .visible(boost::get)
        .build()
    );
    private final Setting<Integer> boostTicks = sgSpeed.add(new IntSetting.Builder()
        .name("Boost Ticks")
        .description("How many boosted speed packets should be sent before returning to normal speed.")
        .defaultValue(3)
        .min(1)
        .sliderMax(10)
        .visible(boost::get)
        .build()
    );
    private final Setting<Double> timer = sgSpeed.add(new DoubleSetting.Builder()
        .name("Timer")
        .description("Sends packets faster.")
        .defaultValue(10)
        .min(0)
        .sliderMax(100)
        .build()
    );

    //--------------------Hole--------------------//
    private final Setting<Boolean> singleTarget = sgHole.add(new BoolSetting.Builder()
        .name("Single Target")
        .description("Only chooses target hole once.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> depth = sgHole.add(new IntSetting.Builder()
        .name("Hole Depth")
        .description("How deep a hole has to be.")
        .defaultValue(3)
        .range(1, 5)
        .sliderRange(1, 5)
        .build()
    );
    private final Setting<Boolean> singleHoles = sgHole.add(new BoolSetting.Builder()
        .name("Single Holes")
        .description("Targets single block holes.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> doubleHoles = sgHole.add(new BoolSetting.Builder()
        .name("Double Holes")
        .description("Targets double holes.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> quadHoles = sgHole.add(new BoolSetting.Builder()
        .name("Quad Holes")
        .description("Targets quad holes.")
        .defaultValue(true)
        .build()
    );

    private Hole singleHole;
    private int collisions;
    private int rubberbands;
    private int ticks;
    private int boostLeft = 0;

    @Override
    public void onActivate() {
        super.onActivate();
        singleHole = findHole();
        rubberbands = 0;
        ticks = 0;
        boostLeft = boost.get() ? boostTicks.get() : 0;
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && rDisable.get() > 0) {
            rubberbands++;
            if (rubberbands >= rDisable.get() && rDisable.get() > 0) {
                this.toggle();
                sendDisableMsg("rubberbanding");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        if (mc.player != null && mc.world != null) {
            Hole hole = singleTarget.get() ? singleHole : findHole();

            if (hole != null && !singleBlocked()) {
                Modules.get().get(Timer.class).setOverride(timer.get());
                double yaw = Math.cos(Math.toRadians(getAngle(hole.middle) + 90.0f));
                double pit = Math.sin(Math.toRadians(getAngle(hole.middle) + 90.0f));

                if (mc.player.getX() == hole.middle.x && mc.player.getZ() == hole.middle.z) {
                    if (mc.player.getY() == hole.middle.y) {
                        this.toggle();
                        sendDisableMsg("in hole");
                        ((IVec3d) event.movement).setXZ(0, 0);
                    } else if (OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(0, -0.05, 0))) {
                        this.toggle();
                        sendDisableMsg("hole unreachable");
                    } else {
                        ((IVec3d) event.movement).setXZ(0, 0);
                    }
                } else {
                    double x = getSpeed() * yaw;
                    double dX = hole.middle.x - mc.player.getX();
                    double z = getSpeed() * pit;
                    double dZ = hole.middle.z - mc.player.getZ();

                    if (OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(x, 0, z))) {
                        collisions++;
                        if (collisions >= coll.get() && coll.get() > 0) {
                            this.toggle();
                            sendDisableMsg("collided");
                        }
                    } else {
                        collisions = 0;
                    }
                    if (ticks > 0) {
                        ticks--;
                    } else if (OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(0, -0.05, 0)) && jump.get()) {
                        ticks = jumpCoolDown.get();
                        ((IVec3d) event.movement).setY(0.42);
                    }
                    boostLeft--;
                    ((IVec3d) event.movement).setXZ(Math.abs(x) < Math.abs(dX) ? x : dX, Math.abs(z) < Math.abs(dZ) ? z : dZ);
                }
            } else {
                this.toggle();
                sendDisableMsg("no hole found");
            }
        }
    }

    private boolean singleBlocked() {
        if (!singleTarget.get()) {
            return false;
        }

        for (BlockPos pos : singleHole.positions) {
            if (OLEPOSSUtils.collidable(pos)) {
                return true;
            }
        }
        return false;
    }

    private Hole findHole() {
        Hole closest = null;

        for (int x = -range.get(); x <= range.get(); x++) {
            for (int y = -downRange.get(); y < 1; y++) {
                for (int z = -range.get(); z < range.get(); z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);

                    Hole hole = HoleUtils.getHole(pos, singleHoles.get(), doubleHoles.get(), quadHoles.get(), depth.get(), true);

                    if (hole.type == HoleType.NotHole) {
                        continue;
                    }

                    if (y == 0 && inHole(hole)) {
                        return hole;
                    }
                    if (closest == null ||
                        hole.middle.distanceTo(mc.player.getPos()) <
                            closest.middle.distanceTo(mc.player.getPos())) {
                        closest = hole;
                    }
                }
            }
        }

        return closest;
    }

    private boolean inHole(Hole hole) {
        for (BlockPos pos : hole.positions) {
            if (mc.player.getBlockPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private float getAngle(Vec3d pos) {
        return (float) Rotations.getYaw(pos);
    }

    private double getSpeed() {
        return boostLeft > 0 ? boostedSpeed.get() : speed.get();
    }
}
