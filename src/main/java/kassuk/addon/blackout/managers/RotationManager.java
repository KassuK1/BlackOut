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
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/*
Made by OLEPOSSU / Raksamies
*/

public class RotationManager {

    public Target target = null;
    public double timer = 0;

    public float[] lastDir = new float[]{0, 0};
    public double priority = 1000;
    public float[] rot = new float[]{0, 0};
    public RotationSettings settings = null;
    public boolean unsent = false;
    public static List<Rotation> history = new ArrayList<>();
    public double rotationsLeft = 0;
    public Target lastTarget = null;

    public RotationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null) {return;}
        rotationsLeft = Math.min(rotationsLeft + event.frameTime * (SettingUtils.rotationPackets() - 20), Math.ceil((SettingUtils.rotationPackets() - 20) / 20f));
        if (settings == null) {
            settings = Modules.get().get(RotationSettings.class);
        }
        timer -= event.frameTime;
        if (timer > 0 && target != null && lastDir != null) {
            rot = lastDir;
            if (SettingUtils.shouldVanillaRotate()) {
                mc.player.setYaw(lastDir[0]);
                mc.player.setPitch(lastDir[1]);
            }
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
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            if (target != null && timer > 0) {
                unsent = false;
                float[] next;
                if (target instanceof BoxTarget) {
                    ((BoxTarget) target).vec = getTargetPos();
                    next = new float[]{(float) RotationUtils.nextYaw(lastDir[0], Rotations.getYaw(((BoxTarget) target).vec), settings.yawStep.get()), (float) RotationUtils.nextPitch(lastDir[1], Rotations.getPitch(((BoxTarget) target).vec), settings.pitchStep.get())};
                } else {
                    next = new float[]{(float) RotationUtils.nextYaw(lastDir[0], ((AngleTarget)target).yaw, settings.yawStep.get()), (float) RotationUtils.nextPitch(lastDir[1], ((AngleTarget)target).pitch, settings.pitchStep.get())};
                }

                ((MixinPlayerMoveC2SPacket) packet).setLook(true);
                ((MixinPlayerMoveC2SPacket) packet).setYaw(next[0]);
                ((MixinPlayerMoveC2SPacket) packet).setPitch(next[1]);
                addHistory(next[0], next[1]);
            }
            if (packet.changesLook()) {
                lastDir = new float[]{((MixinPlayerMoveC2SPacket) packet).getYaw(), ((MixinPlayerMoveC2SPacket) packet).getPitch()};
            }
        }
    }
    public boolean isTarget(Box box) {
        if (!(lastTarget instanceof BoxTarget)) {return false;}

        return box.minX == ((BoxTarget)lastTarget).box.minX && box.minY == ((BoxTarget)lastTarget).box.minY && box.minZ == ((BoxTarget)lastTarget).box.minZ &&
            box.maxX == ((BoxTarget)lastTarget).box.maxX && box.maxY == ((BoxTarget)lastTarget).box.maxY && box.maxZ == ((BoxTarget)lastTarget).box.maxZ;
    }
    public boolean isTarget(double yaw, double pitch) {
        if (!(lastTarget instanceof AngleTarget)) {return false;}

        return yaw == ((AngleTarget) lastTarget).yaw && pitch == ((AngleTarget) lastTarget).pitch;
    }
    public void end(Box box) {
        if (isTarget(box)) {
            priority = 1000;
        }
    }
    public void end(BlockPos pos) {
        end(OLEPOSSUtils.getBox(pos));
    }
    public boolean startYaw(double yaw, double p) {
        return start(yaw, lastDir[1], p);
    }
    public boolean startPitch(double pitch, double p) {
        return start(lastDir[0], pitch, p);
    }
    public boolean start(double yaw, double pitch, double p) {
        if (p <= priority && settings != null) {
            priority = p;
            lastTarget = target;

            target = new AngleTarget(yaw, pitch);
            timer = 1;

            if (lastDir[0] == yaw && lastDir[1] == pitch) {
                return true;
            }

            if (!isTarget(yaw, pitch) && RotationUtils.nextYaw(lastDir[0], yaw, settings.yawStep.get()) == yaw &&
                RotationUtils.nextPitch(lastDir[1], pitch, settings.pitchStep.get()) == pitch && rotationsLeft >= 1) {
                rotationsLeft -= 1;
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(0, 0, Managers.ONGROUND.isOnGround()));
                return true;
            }
        }
        return false;
    }

    public boolean start(BlockPos pos, Box box, Vec3d vec, double p, RotationType type) {
        if (p <= priority && settings != null) {
            priority = p;
            lastTarget = target;

            target = pos != null ? new BoxTarget(pos, vec != null ? vec : OLEPOSSUtils.getMiddle(box), p, type) : new BoxTarget(box, vec != null ? vec : OLEPOSSUtils.getMiddle(box), p, type);
            timer = 1;

            ((BoxTarget)target).vec = getTargetPos();

            if (SettingUtils.rotationCheckHistory(box, type)) {
                return true;
            }

            if (!isTarget(box) && SettingUtils.rotationCheck(mc.player.getEyePos(), RotationUtils.nextYaw(lastDir[0], Rotations.getYaw(((BoxTarget)target).vec), settings.yawStep.get()),
                RotationUtils.nextPitch(lastDir[1], Rotations.getPitch(((BoxTarget)target).vec), settings.pitchStep.get()), ((BoxTarget)target).box, type) && rotationsLeft >= 1) {
                rotationsLeft -= 1;
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(0, 0, Managers.ONGROUND.isOnGround()));
                return true;
            }
        }
        return false;
    }

    public boolean start(Box box, Vec3d vec, double p, RotationType type) {
        return start(null, box, vec, p, type);
    }
    public boolean start(Box box, double p, RotationType type) {
        return start(box, OLEPOSSUtils.getMiddle(box), p, type);
    }
    public boolean start(BlockPos pos, double p, RotationType type) {
        return start(OLEPOSSUtils.getBox(pos), p, type);
    }
    public boolean start(BlockPos pos, Vec3d vec, double p, RotationType type) {
        return start(pos, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), vec, p, type);
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

    public Vec3d getTargetPos() {
        if (SettingUtils.shouldGhostRotate()) {
            if (((BoxTarget)target).pos == null) {
                return SettingUtils.getGhostRot(((BoxTarget)target).box, ((BoxTarget)target).vec);
            } else {
                return SettingUtils.getGhostRot(((BoxTarget)target).pos, ((BoxTarget)target).vec);
            }
        }
        return ((BoxTarget)target).vec;
    }



    class Target {}
    class BoxTarget extends Target {
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

    class AngleTarget extends Target {
        public final double yaw;
        public final double pitch;

        public AngleTarget(double yaw, double pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}



