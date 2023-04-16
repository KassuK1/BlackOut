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
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class HoleSnap extends BlackOutModule {
    public HoleSnap() {
        super(BlackOut.BLACKOUT, "HoleSnap", "For the time when you cant even press W");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> single = sgGeneral.add(new BoolSetting.Builder()
        .name("Single")
        .description("Only chooses target hole once")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> jump = sgGeneral.add(new BoolSetting.Builder()
        .name("Jump")
        .description("Jumps to the hole (very useful)")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> jumpCoolDown = sgGeneral.add(new IntSetting.Builder()
        .name("Jump Cooldown")
        .description("Ticks between jumps")
        .defaultValue(5)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Movement Speed")
        .defaultValue(0.2873)
        .min(0)
        .sliderMax(1)
        .build()
    );
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .name("Timer")
        .description("Sends packets faster")
        .defaultValue(30)
        .min(0)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("Range")
        .description("Horizontal range for finding holes")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> downRange = sgGeneral.add(new IntSetting.Builder()
        .name("Down Range")
        .description("Vertical range for finding holes")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> depth = sgGeneral.add(new IntSetting.Builder()
        .name("Hole Depth")
        .description("How deep a hole has to be")
        .defaultValue(3)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Integer> coll = sgGeneral.add(new IntSetting.Builder()
        .name("Collisions to disable")
        .description("0 = doesn't disable")
        .defaultValue(15)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> rDisable = sgGeneral.add(new IntSetting.Builder()
        .name("Rubberbands to disable")
        .description("0 = doesn't disable")
        .defaultValue(1)
        .sliderRange(0, 100)
        .build()
    );
    Hole singleHole;
    int collisions;
    int rubberbands;
    int ticks;
    List<BlockPos> holes = new ArrayList<>();

    @Override
    public void onActivate() {
        super.onActivate();
        singleHole = findHole();
        rubberbands = 0;
        ticks = 0;
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
            Hole hole = single.get() ? singleHole : findHole();

            if (hole != null && !singleBlocked()) {
                Modules.get().get(Timer.class).setOverride(timer.get());
                double yaw = Math.cos(Math.toRadians(getAngle(hole.middle) + 90.0f));
                double pit = Math.sin(Math.toRadians(getAngle(hole.middle) + 90.0f));

                if (mc.player.getX() == hole.middle.x && mc.player.getZ() == hole.middle.z) {
                    if (mc.player.getY() == hole.middle.y) {
                        this.toggle();
                        sendDisableMsg("in hole");
                    } else if (OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().offset(0, -0.05, 0))){
                        this.toggle();
                        sendDisableMsg("hole unreachable");
                    } else {
                        ((IVec3d) event.movement).setXZ(0, 0);
                    }
                } else {
                    double x = speed.get() * yaw;
                    double dX = hole.middle.x - mc.player.getX();
                    double z = speed.get() * pit;
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
                    ((IVec3d) event.movement).setXZ(Math.abs(x) < Math.abs(dX) ? x : dX, Math.abs(z) < Math.abs(dZ) ? z : dZ);
                }
            } else {
                this.toggle();
                sendDisableMsg("no hole found");
            }
        }
    }

    boolean singleBlocked() {
        for (BlockPos pos : singleHole.positions) {
            if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
                return true;
            }
        }
        return false;
    }

    Hole findHole() {
        Hole closest = null;

        for (int x = -range.get(); x <= range.get(); x++) {
            for (int y = -downRange.get(); y < 1; y++) {
                for (int z = -range.get(); z < range.get(); z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);

                    Hole hole = HoleUtils.getHole(pos, depth.get());

                    if (hole.type == HoleType.NotHole) {continue;}

                    if (y == 0 && inHole(hole)) {
                        return hole;
                    }
                    if (closest == null ||
                        OLEPOSSUtils.distance(hole.middle, mc.player.getPos()) <
                        OLEPOSSUtils.distance(closest.middle, mc.player.getPos())) {
                        closest = hole;
                    }
                }
            }
        }

        return closest;
    }

    boolean inHole(Hole hole) {
        for (BlockPos pos : hole.positions) {
            if (mc.player.getBlockPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    float getAngle(Vec3d pos)
    {
        return (float) Rotations.getYaw(pos);
    }
}
