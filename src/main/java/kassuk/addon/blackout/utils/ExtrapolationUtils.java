package kassuk.addon.blackout.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ExtrapolationUtils {
    private static Map<AbstractClientPlayerEntity, List<Vec3d>> motions = new HashMap<>();

    @PreInit
    public static void preInit() {
        MeteorClient.EVENT_BUS.subscribe(ExtrapolationUtils.class);
    }

    @EventHandler(priority = 1000000)
    private static void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || mc.world.getPlayers().isEmpty()) return;

        Map<AbstractClientPlayerEntity, List<Vec3d>> newMotions = new HashMap<>();

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            Vec3d vec = player.getPos().subtract(player.prevX, player.prevY, player.prevZ);

            if (!motions.containsKey(player)) {
                List<Vec3d> v = new ArrayList<>();
                v.add(vec);
                newMotions.put(player, v);
                continue;
            }

            List<Vec3d> v = motions.get(player);
            v.add(0, vec);

            if (v.size() > 20) {
                v.subList(20, v.size()).clear();
            }

            newMotions.put(player, v);
        }

        motions = newMotions;
    }

    public static void extrapolateMap(Map<AbstractClientPlayerEntity, Box> old, EpicInterface<AbstractClientPlayerEntity, Integer> extrapolation, EpicInterface<AbstractClientPlayerEntity, Integer> smoothening) {
        old.clear();

        motions.forEach((player, m) -> {
            if (m == null) return;
            old.put(player, extrapolate(player, m, extrapolation.get(player), smoothening.get(player)));
        });
    }

    public static Box extrapolate(AbstractClientPlayerEntity player, int extrapolation, int smoothening) {
        List<Vec3d> m = motions.get(player);
        if (m == null) return null;
        return extrapolate(player, m, extrapolation, smoothening);
    }

    public static Box extrapolate(AbstractClientPlayerEntity player, List<Vec3d> m, int extrapolation, int smoothening) {
        Vec3d motion = getMotion(m, smoothening);

        double x = motion.x;
        double y = motion.y;
        double z = motion.z;

        double stepHeight = 0.6;

        Box box = new Box(player.getX() - 0.3, player.getY(), player.getZ() - 0.3, player.getX() + 0.3, player.getY() + (player.getBoundingBox().maxY - player.getBoundingBox().minY), player.getZ() + 0.3);
        boolean onGround = inside(player, box.offset(0, -0.04, 0));

        for (int i = 0; i < extrapolation; i++) {
            // y
            List<VoxelShape> list = mc.world.getEntityCollisions(player, box.stretch(x, y, z));
            Vec3d movement = new Vec3d(x, y, z);
            Vec3d vec3d = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(player, movement, box, mc.world, list);

            boolean canStep = (onGround || (y < 0 && vec3d.y != y)) && (vec3d.x != x || vec3d.z != z);

            if (canStep) {
                Vec3d vec3d2 = Entity.adjustMovementForCollisions(player, new Vec3d(x, stepHeight, z), box, mc.world, list);
                Vec3d vec3d3 = Entity.adjustMovementForCollisions(player, new Vec3d(0.0, stepHeight, 0.0), box.stretch(x, 0.0, z), mc.world, list);
                if (vec3d3.y < stepHeight) {
                    Vec3d vec3d4 = Entity.adjustMovementForCollisions(player, new Vec3d(movement.x, 0.0, movement.z), box.offset(vec3d3), mc.world, list).add(vec3d3);
                    if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                        vec3d2 = vec3d4;
                    }
                }

                if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                    Vec3d vec = vec3d2.add(Entity.adjustMovementForCollisions(player, new Vec3d(0.0, -vec3d2.y + movement.y, 0.0), box.offset(vec3d2), mc.world, list));
                    box = box.offset(vec);
                    onGround = true;
                    continue;
                }
            }

            box = box.offset(vec3d);
            onGround = inside(player, box.offset(0, -0.04, 0));

            if (onGround) y = 0;
            y = (y - 0.08) * 0.98;
        }

        return box;
    }

    private static boolean inside(PlayerEntity player, Box box) {
        return OLEPOSSUtils.inside(player, box);
    }

    private static Vec3d getMotion(List<Vec3d> vecs, int max) {
        Vec3d avg = new Vec3d(0, (vecs.get(0).y - 0.08) * 0.98, 0);

        int s = Math.min(vecs.size(), max);
        for (int i = 0; i < s; i++) {
            avg = avg.add(vecs.get(i).x, 0, vecs.get(i).z);
        }

        return avg.multiply(1 / (float) s, 1, 1 / (float) s);
    }
}
