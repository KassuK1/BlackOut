package kassuk.addon.blackout.managers;

import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.events.PreRotationEvent;
import kassuk.addon.blackout.globalsettings.RotationSettings;
import kassuk.addon.blackout.utils.NCPRaytracer;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.RotationUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class RotationManager {

    private Target target = null;
    private double timer = 0;

    public final float[] prevDir = new float[2];
    public final float[] currentDir = new float[2];
    public final float[] lastDir = new float[2];

    private double priority = 1000;
    private RotationSettings settings = null;
    private boolean unsent = false;
    public static final List<Rotation> history = new ArrayList<>();
    boolean shouldRotate = false;
    public float[] next;
    private boolean rotated = false;
    private long key = 0;

    private Vec3d eyePos = new Vec3d(0, 0, 0);

    public RotationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        prevDir[0] = currentDir[0];
        prevDir[1] = currentDir[1];

        currentDir[0] = lastDir[0];
        currentDir[1] = lastDir[1];
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMovePre(SendMovementPacketsEvent.Pre event) {
        unsent = true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMovePost(SendMovementPacketsEvent.Post event) {
        if (unsent) {
            onPreRotate();

            if (updateShouldRotate()) {
                setEyePos(mc.player.getPos());
                updateNextRotation();

                if (rotated) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(next[0], next[1], Managers.ON_GROUND.isOnGround()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null) return;
        if (settings == null) settings = Modules.get().get(RotationSettings.class);

        timer -= event.frameTime;
        if (timer > 0 && target != null && lastDir != null) {
            if (SettingUtils.shouldVanillaRotate()) {
                float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
                mc.player.setYaw(MathHelper.lerpAngleDegrees(tickDelta, prevDir[0], currentDir[0]));
                mc.player.setPitch(MathHelper.lerp(tickDelta, prevDir[1], currentDir[1]));
            }
        } else if (target != null) {
            target = null;
            priority = 1000;
        } else {
            priority = 1000;
        }
    }

    public PlayerMoveC2SPacket onFull(PlayerMoveC2SPacket.Full packet) {
        unsent = false;
        onPreRotate();
        if (!updateShouldRotate()) {
            return packet;
        }

        setEyePos(new Vec3d(packet.getX(0), packet.getY(0), packet.getZ(0)));
        updateNextRotation();

        if (rotated) {
            return new PlayerMoveC2SPacket.Full(packet.getX(0), packet.getY(0), packet.getZ(0), next[0], next[1], packet.isOnGround());
        }

        return new PlayerMoveC2SPacket.PositionAndOnGround(packet.getX(0), packet.getY(0), packet.getZ(0), packet.isOnGround());
    }

    public PlayerMoveC2SPacket onPositionOnGround(PlayerMoveC2SPacket.PositionAndOnGround packet) {
        unsent = false;
        onPreRotate();
        if (!updateShouldRotate()) {
            return packet;
        }

        setEyePos(new Vec3d(packet.getX(0), packet.getY(0), packet.getZ(0)));
        updateNextRotation();

        if (rotated) {
            return new PlayerMoveC2SPacket.Full(packet.getX(0), packet.getY(0), packet.getZ(0), next[0], next[1], packet.isOnGround());
        }

        return packet;
    }

    public PlayerMoveC2SPacket onLookAndOnGround(PlayerMoveC2SPacket.LookAndOnGround packet) {
        unsent = false;
        onPreRotate();
        if (!updateShouldRotate()) {
            return packet;
        }

        setEyePos(mc.player.getPos());
        updateNextRotation();

        if (rotated) {
            return new PlayerMoveC2SPacket.LookAndOnGround(next[0], next[1], packet.isOnGround());
        }
        if (packet.isOnGround() != Managers.ON_GROUND.isOnGround()) {
            return new PlayerMoveC2SPacket.OnGroundOnly(packet.isOnGround());
        }

        return null;
    }

    public PlayerMoveC2SPacket onOnlyOnground(PlayerMoveC2SPacket.OnGroundOnly packet) {
        unsent = false;
        onPreRotate();
        if (!updateShouldRotate()) {
            return packet;
        }

        setEyePos(mc.player.getPos());
        updateNextRotation();

        if (rotated) {
            return new PlayerMoveC2SPacket.LookAndOnGround(next[0], next[1], packet.isOnGround());
        }

        return packet;
    }

    private void onPreRotate() {
        MeteorClient.EVENT_BUS.post(PreRotationEvent.INSTANCE);
    }

    private boolean updateShouldRotate() {
        shouldRotate = target != null && timer > 0;
        return shouldRotate;
    }

    private void updateNextRotation() {
        if (shouldRotate) {
            if (target instanceof BoxTarget) {
                ((BoxTarget) target).vec = getTargetPos();
                next = new float[]{RotationUtils.nextYaw(lastDir[0], RotationUtils.getYaw(eyePos, ((BoxTarget) target).vec), settings.yawStep(((BoxTarget) target).type)), RotationUtils.nextPitch(lastDir[1], RotationUtils.getPitch(eyePos, ((BoxTarget) target).vec), settings.pitchStep(((BoxTarget) target).type))};
            } else {
                next = new float[]{RotationUtils.nextYaw(lastDir[0], ((AngleTarget) target).yaw, settings.yawStep(((AngleTarget) target).type)), RotationUtils.nextPitch(lastDir[1], ((AngleTarget) target).pitch, settings.pitchStep(((AngleTarget) target).type))};
            }

            rotated = Math.abs(RotationUtils.yawAngle(next[0], lastDir[0])) > 0 || Math.abs(next[1] - lastDir[1]) > 0;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 100)
    private void onSend(PacketEvent.Sent event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet && packet.changesLook()) {
            lastDir[0] = packet.getYaw(0);
            lastDir[1] = packet.getPitch(0);
            addHistory(lastDir[0], lastDir[1]);
        }
    }

    public void end(long k) {
        if (k == key) priority = 1000;
    }

    public void endYaw(double yaw, boolean reset) {
        if (!(target instanceof AngleTarget)) {
            return;
        }

        if (yaw == ((AngleTarget) target).yaw) {
            priority = 1000;
            if (reset) {
                target = null;
            }
        }
    }

    public void endPitch(double pitch, boolean reset) {
        if (!(target instanceof AngleTarget)) {
            return;
        }

        if (pitch == ((AngleTarget) target).pitch) {
            priority = 1000;
            if (reset) {
                target = null;
            }
        }
    }

    public boolean startYaw(double yaw, double p, RotationType type, long key) {
        return start(yaw, lastDir[1], p, type, key);
    }

    public boolean startPitch(double pitch, double p, RotationType type, long key) {
        return start(lastDir[0], pitch, p, type, key);
    }

    public boolean start(double yaw, double pitch, double p, RotationType type, long key) {
        if (settings == null) {
            return false;
        }

        if (p <= priority) {
            this.key = key;
            priority = p;

            target = new AngleTarget(yaw, pitch, type);
            timer = settings.time(type);
        }

        return lastDir[0] == yaw && lastDir[1] == pitch;
    }

    public boolean start(BlockPos pos, Box box, Vec3d vec, double p, RotationType type, long key) {
        if (settings == null) {
            return false;
        }

        boolean alreadyRotated = SettingUtils.rotationCheck(box, type);

        if (p < priority || key == this.key || (p == priority && (!(target instanceof BoxTarget) || SettingUtils.rotationCheck(((BoxTarget) target).box, type)))) {
            if (!alreadyRotated) {
                priority = p;
            }

            this.key = key;
            target = pos != null ? new BoxTarget(pos, vec != null ? vec : OLEPOSSUtils.getMiddle(box), p, type) : new BoxTarget(box, vec != null ? vec : OLEPOSSUtils.getMiddle(box), p, type);
            timer = settings.time(type);
        }

        return alreadyRotated;
    }

    public boolean start(Box box, Vec3d vec, double p, RotationType type, long key) {
        return start(null, box, vec, p, type, key);
    }

    public boolean start(Box box, double p, RotationType type, long key) {
        return start(box, OLEPOSSUtils.getMiddle(box), p, type, key);
    }

    public boolean start(BlockPos pos, double p, RotationType type, long key) {
        return start(pos, Box.from(new BlockBox(pos)), pos.toCenterPos(), p, type, key);
    }

    public boolean start(BlockPos pos, Vec3d vec, double p, RotationType type, long key) {
        return start(pos, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), vec, p, type, key);
    }

    private void setEyePos(Vec3d vec3d) {
        eyePos = vec3d.add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
    }

    public void addHistory(double yaw, double pitch) {
        history.add(0, new Rotation(yaw, pitch, mc.player.getEyePos()));

        for (int i = history.size(); i > 20; i--) {
            if (history.size() > i) history.remove(i);
        }
    }

    public record Rotation(double yaw, double pitch, Vec3d vec) {}

    public Vec3d getTargetPos() {
        BoxTarget t = (BoxTarget) target;

        if (settings.mode(t.type) != RotationSettings.RotationCheckMode.StrictRaytrace ||
            NCPRaytracer.raytrace(mc.player.getEyePos(), t.targetVec, t.box)) {
            return new Vec3d(MathHelper.clamp(t.targetVec.x + (Math.random() - 0.5) * 0.05, t.box.minX, t.box.maxX), MathHelper.clamp(t.targetVec.y + (Math.random() - 0.5) * 0.05, t.box.minY, t.box.maxY), MathHelper.clamp(t.targetVec.z + (Math.random() - 0.5) * 0.05, t.box.minZ, t.box.maxZ));
        }

        Vec3d eye = mc.player.getEyePos();
        double cd = 1000000;
        Vec3d closest = null;

        for (double x = 0; x <= 1; x += 0.1) {
            for (double y = 0; y <= 1; y += 0.1) {
                for (double z = 0; z <= 1; z += 0.1) {
                    Vec3d vec = new Vec3d(lerp(t.box.minX, t.box.maxX, x), lerp(t.box.minY, t.box.maxY, y), lerp(t.box.minZ, t.box.maxZ, z));

                    double d = t.targetVec.distanceTo(vec);
                    if (d > cd) continue;

                    if (!NCPRaytracer.raytrace(eye, vec, ((BoxTarget) target).box)) continue;

                    cd = d;
                    closest = vec;
                }
            }
        }

        return closest == null ? t.targetVec : closest;
    }

    private double lerp(double from, double to, double delta) {
        return from + (to - from) * delta;
    }

    public void setHeadYaw(Args args) {
        if (!shouldRotate) {return;}

        args.set(1, prevDir[0]);
        args.set(2, currentDir[0]);
    }
    public void setBodyYaw(Args args) {
        if (!shouldRotate) {return;}

        args.set(1, prevDir[0]);
        args.set(2, currentDir[0]);
    }
    public void setPitch(Args args) {
        if (!shouldRotate) {return;}

        args.set(1, prevDir[1]);
        args.set(2, currentDir[1]);
    }

    private static class Target {}

    private static class BoxTarget extends Target {
        public final BlockPos pos;
        public final Box box;
        public final Vec3d targetVec;
        public Vec3d vec;
        public final double priority;
        public final RotationType type;

        public BoxTarget(BlockPos pos, Vec3d vec, double priority, RotationType type) {
            this.pos = pos;
            this.box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            this.vec = vec;
            this.targetVec = vec;
            this.priority = priority;
            this.type = type;
        }

        public BoxTarget(Box box, Vec3d vec, double priority, RotationType type) {
            this.pos = null;
            this.box = box;
            this.vec = vec;
            this.targetVec = vec;
            this.priority = priority;
            this.type = type;
        }
    }

    private static class AngleTarget extends Target {
        public final double yaw;
        public final double pitch;
        public boolean ended;
        public final RotationType type;

        public AngleTarget(double yaw, double pitch, RotationType type) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.ended = false;
            this.type = type;
        }
    }
}
