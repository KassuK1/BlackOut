package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoCrystalRewrite extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgShield = settings.createGroup("Shield");
    private final SettingGroup sgExplode = settings.createGroup("Explode");
    private final SettingGroup sgClosest = settings.createGroup("Closest");
    private final SettingGroup sgMisc = settings.createGroup("Misc");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgRotate = settings.createGroup("Rotate");
    private final SettingGroup sgExtrapolation = settings.createGroup("Extrapolation");
    private final SettingGroup sgRaytrace = settings.createGroup("Raytrace");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //settings
    boolean enemyExt = true;
    boolean selfExt = true;
    boolean friendExt = true;
    boolean oldVerPlacements = false;
    int extrapolation = 5;
    int extSmoothness = 3;
    float placeRange = 5;
    float placeRangeHeight = 0.5f;
    //damage
    float minPlace = 4;
    float maxSelfRatio = 0.3f;
    float maxFriendRatio = 0.5f;
    float maxSelf = 8;
    float maxFriend = 8;
    float forcePop = 2;
    float antiSelfPop = 2;
    float antiFriendPop = 1;

    double highestEnemy = 0;
    double enemyHP = 0;
    double highestFriend = 0;
    double friendHP = 0;
    double self = 0;
    DmgCheckMode dmgCheckMode = DmgCheckMode.Balanced;

    public AutoCrystalRewrite() {
        super(BlackOut.ANARCHY, "Auto Crystal Rewrite", "Breaks and places crystals automatically.");
    }

    private Map<PlayerEntity, Box> extPos = new HashMap<>();
    private Map<String, List<Vec3d>> motions = new HashMap<>();
    private enum DmgCheckMode {
        Suicidal,
        Balanced,
        Safe
    }

    @Override
    public void onActivate() {
        super.onActivate();

    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onTickPre(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender3D(Render3DEvent event) {
        double d = event.frameTime;


    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof EntitySpawnS2CPacket) {

        }
        if (event.packet instanceof ExplosionS2CPacket) {

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player != null && mc.world != null) {

        }
    }

    // Other stuff

    void calcExt() {
        for (PlayerEntity pl : mc.world.getPlayers()) {
            String key = pl.getName().getString();
            Vec3d currentVelocity = new Vec3d(pl.getX() - pl.prevX, pl.getY() - pl.prevY, pl.getZ() - pl.prevZ);
            if (motions.containsKey(key)) {
                List<Vec3d> vec = motions.get(key);
                if (vec != null) {
                    if (vec.size() >= extSmoothness) {
                        for (int i = vec.size() - extSmoothness; i <= 0; i++) {
                            vec.remove(0);
                        }
                    }
                    vec.add(currentVelocity);
                } else {
                    List<Vec3d> list = new ArrayList<>();
                    list.add(currentVelocity);
                    motions.put(key, list);
                }
            } else {
                List<Vec3d> list = new ArrayList<>();
                list.add(currentVelocity);
                motions.put(key, list);
            }
        }
        extPos = getExt();
    }

    Map<PlayerEntity, Box> getExt() {
        Map<PlayerEntity, Box> map = new HashMap<>();

        mc.world.getPlayers().forEach(player -> {
            Box box = player.getBoundingBox();
            if (!inside(player, box)) {
                Vec3d motion = average(motions.get(player.getName().getString()));
                double x = motion.x;
                double y = motion.y;
                double z = motion.z;
                for (int i = 0; i < extrapolation; i++) {
                    // x
                    if (!inside(player, box.offset(x, 0, 0))) {
                        box = box.offset(x, 0, 0);
                    }

                    // z
                    if (!inside(player, box.offset(x, 0, 0))) {
                        box = box.offset(x, 0, 0);
                    }

                    // y
                    if (inside(player, box.offset(0, -0.04, 0))) {
                        y = -0.08;
                    } else {
                        y -= 0.08;
                        if (!inside(player, box.offset(0, y, 0))) {
                            box = box.offset(0, y, 0);
                        }
                    }
                }
            }
            map.put(player, box);
        });
        return map;
    }

    Vec3d average(List<Vec3d> vec) {
        Vec3d total = new Vec3d(0, 0, 0);
        if (vec != null) {
            if (!vec.isEmpty()) {
                for (Vec3d vec3d : vec) {
                    total = total.add(vec3d);
                }
                return new Vec3d(total.x / vec.size(), total.y / vec.size(), total.z / vec.size());
            }
        }
        return total;
    }
    boolean inside(PlayerEntity en, Box bb) {
        return mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }

    private void update() {

    }

    private BlockPos getPlacePos(int r) {
        BlockPos bestPos = null;
        double[] highest = null;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    // Checks if crystal can be placed
                    if (!air(pos) || !(!oldVerPlacements || air(pos.up())) || !inPlaceRange(placeRangePos(pos)) ||
                        !crystalBlock(pos.down())) {continue;}

                    // Calculates damages and healths
                    double[][] result = getDmg(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));

                    // Checks if damages are valid
                    if (!placeDamageCheck(result[0], result[1], highest)) {continue;}


                }
            }
        }
        return bestPos;
    }

    private boolean placeDamageCheck(double[] dmg, double[] health, double[] highest) {
        //  0 = enemy, 1 = friend, 2 = self

        //  Dmg Check
        if (dmgCheckMode.equals(DmgCheckMode.Suicidal) && dmg[0] < highest[0]) {return false;}
        if (dmgCheckMode.equals(DmgCheckMode.Balanced) && dmg[2] / dmg[0] > highest[2] / highest[0]) {return false;}
        if (dmgCheckMode.equals(DmgCheckMode.Safe) && dmg[2] > highest[2]) {return false;}

        //  Force/anti-pop check
        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (dmg[2] * antiSelfPop >= playerHP) {return false;}
        if (dmg[1] * antiFriendPop >= health[1]) {return false;}
        if (dmg[0] * forcePop >= health[0]) {return true;}

        //  Min Damage
        if (dmg[0] < minPlace) {return false;}

        //  Max Damage
        if (dmg[1] > maxFriend) {return false;}
        if (dmg[1] / dmg[0] > maxFriendRatio) {return false;}
        if (dmg[2] > maxSelf) {return false;}
        if (dmg[2] / dmg[0] > maxSelfRatio) {return false;}
        return true;
    }

    private boolean explodeDamageCheck(double[] dmg, double[] health) {
        //  0 = enemy, 1 = friend, 2 = self

        //  Force/anti-pop check
        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (dmg[2] * forcePop >= playerHP) {return false;}
        if (dmg[1] * antiFriendPop >= health[1]) {return false;}
        if (dmg[0] * forcePop >= health[0]) {return true;}

        //  Min Damage
        if (dmg[0] < minPlace) {return false;}

        //  Max Damage
        if (dmg[1] > maxFriend) {return false;}
        if (dmg[1] / dmg[0] > maxFriendRatio) {return false;}
        if (dmg[2] > maxSelf) {return false;}
        if (dmg[2] / dmg[0] > maxSelfRatio) {return false;}
        return true;
    }

    double[][] getDmg(Vec3d vec) {
        extPos.forEach((player, box) -> {
            if (player.getHealth() <= 0 || player.isSpectator()) {return;}
            boolean shouldExt = shouldExt(player);
            Box originalBox = player.getBoundingBox();
            if (shouldExt) {
                player.setBoundingBox(box);
            }
            double dmg = DamageUtils.crystalDamage(player, vec);
            double hp = player.getHealth() + player.getAbsorptionAmount();

            //  self
            if (player == mc.player) {
                self = dmg;
            }
            //  friend
            else if (Friends.get().isFriend(player)) {
                highestFriend = dmg;
                friendHP = hp;
            }
            //  enemy
            else {
                highestEnemy = dmg;
                enemyHP = hp;
            }

            if (shouldExt) {
                player.setBoundingBox(originalBox);
            }
        });
        return new double[][]{new double[] {highestEnemy, highestFriend, self}, new double[]{enemyHP, friendHP}};
    }

    boolean shouldExt(PlayerEntity pl) {
        return (pl == mc.player && selfExt) || (Friends.get().isFriend(pl) && friendExt) ||
            (pl != mc.player && !Friends.get().isFriend(pl) && enemyExt);
    }

    boolean air(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);
    }
    boolean crystalBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) ||
            mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK);
    }
    boolean inPlaceRange(Vec3d vec) {
        return dist(mc.player.getPos().add(-vec.x, -vec.y, -vec.z)) <= placeRange;
    }
    double dist(Vec3d distances) {
        return Math.sqrt(distances.x * distances.x + distances.y * distances.y + distances.z * distances.z);
    }
    Vec3d placeRangePos(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + placeRangeHeight, pos.getZ() + 0.5);
    }
}

