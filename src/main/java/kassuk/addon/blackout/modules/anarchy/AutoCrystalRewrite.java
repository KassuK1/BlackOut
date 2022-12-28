package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.IntTimerList;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
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

    //  General Page
    private final Setting<Boolean> place = sgGeneral.add(new BoolSetting.Builder()
        .name("Place")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> explode = sgGeneral.add(new BoolSetting.Builder()
        .name("Explode")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description(".")
        .defaultValue(true)
        .build()
    );

    //  Place Page
    private final Setting<Boolean> oldVerPlacements = sgGeneral.add(new BoolSetting.Builder()
        .name("1.12 Placements")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> placeSpeed = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Range")
        .description(".")
        .defaultValue(10)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> placeRange = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Range")
        .description(".")
        .defaultValue(5)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> placeHeight = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height")
        .description(".")
        .defaultValue(0.5)
        .range(0, 2)
        .sliderRange(0, 2)
        .build()
    );
    private final Setting<Boolean> placeSwing = sgGeneral.add(new BoolSetting.Builder()
        .name("Explode Swing")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<SwingMode> placeSwingMode = sgGeneral.add(new EnumSetting.Builder<SwingMode>()
        .name("Place Swing Mode")
        .description(".")
        .defaultValue(SwingMode.Full)
        .build()
    );
    private final Setting<Boolean> clearSend = sgGeneral.add(new BoolSetting.Builder()
        .name("Clear Send")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> clearReceive = sgGeneral.add(new BoolSetting.Builder()
        .name("Clear Receive")
        .description(".")
        .defaultValue(true)
        .build()
    );

    //  Explode Page
    private final Setting<Double> expSpeed = sgPlace.add(new DoubleSetting.Builder()
        .name("Explode Speed")
        .description(".")
        .defaultValue(2)
        .range(0.01, 20)
        .sliderRange(0.01, 20)
        .build()
    );
    private final Setting<Double> expHeight = sgPlace.add(new DoubleSetting.Builder()
        .name("Explode Height")
        .description(".")
        .defaultValue(1)
        .range(0, 2)
        .sliderRange(0, 2)
        .build()
    );
    private final Setting<Boolean> setDead = sgGeneral.add(new BoolSetting.Builder()
        .name("Set Dead")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> setDeadDelay = sgDamage.add(new DoubleSetting.Builder()
        .name("Set Dead Delay")
        .description(".")
        .defaultValue(0.05)
        .range(0, 1)
        .sliderRange(0, 1)
        .visible(setDead::get)
        .build()
    );
    private final Setting<Boolean> explodeSwing = sgGeneral.add(new BoolSetting.Builder()
        .name("Explode Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingMode> explodeSwingMode = sgGeneral.add(new EnumSetting.Builder<SwingMode>()
        .name("Explode Swing Mode")
        .description(".")
        .defaultValue(SwingMode.Full)
        .build()
    );

    //  Damage Page
    private final Setting<DmgCheckMode> cmgCheckMode = sgGeneral.add(new EnumSetting.Builder<DmgCheckMode>()
        .name("Dmg Check Mode")
        .description(".")
        .defaultValue(DmgCheckMode.Balanced)
        .build()
    );
    private final Setting<Double> minPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Place Height")
        .description(".")
        .defaultValue(0.5)
        .range(0, 2)
        .sliderRange(0, 2)
        .build()
    );
    private final Setting<Double> maxSelfPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Place")
        .description(".")
        .defaultValue(8)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxSelfPlaceRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Place Ratio")
        .description(".")
        .defaultValue(0.3)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> maxFriendPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Place")
        .description(".")
        .defaultValue(8)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxFriendPlaceRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Place Ratio")
        .description(".")
        .defaultValue(0.3)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> forcePop = sgDamage.add(new DoubleSetting.Builder()
        .name("Force Pop")
        .description(".")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiFriendPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Friend Pop")
        .description(".")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiSelfPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Self Pop")
        .description(".")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    //  Extrapolation Page
    private final Setting<Integer> extrapolation = sgDamage.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description(".")
        .defaultValue(2)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> extSmoothness = sgDamage.add(new IntSetting.Builder()
        .name("Extrapolation Smoothness")
        .description(".")
        .defaultValue(5)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );
    private final Setting<Boolean> enemyExt = sgGeneral.add(new BoolSetting.Builder()
        .name("Enemy Extrapolation")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> selfExt = sgGeneral.add(new BoolSetting.Builder()
        .name("Self Extrapolation")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> friendExt = sgGeneral.add(new BoolSetting.Builder()
        .name("Friend Extrapolation")
        .description(".")
        .defaultValue(true)
        .build()
    );

    double highestEnemy = 0;
    double enemyHP = 0;
    double highestFriend = 0;
    double friendHP = 0;
    double self = 0;
    double placeTimer = 0;
    double delayTimer = 0;
    BlockPos placePos = null;
    DmgCheckMode dmgCheckMode = DmgCheckMode.Balanced;
    IntTimerList attacked = new IntTimerList();

    public AutoCrystalRewrite() {
        super(BlackOut.ANARCHY, "Auto Crystal Rewrite", "Breaks and places crystals automatically (but better).");
    }

    private Map<PlayerEntity, Box> extPos = new HashMap<>();
    private Map<String, List<Vec3d>> motions = new HashMap<>();
    private Map<Integer, Box> blocked = new HashMap<>();
    BlockPos lastPos = null;
    public enum DmgCheckMode {
        Suicidal,
        Balanced,
        Safe
    }
    public enum SwingMode {
        Client,
        Packet,
        Full
    }

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onTickPre(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            calcExt();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender3D(Render3DEvent event) {
        double d = event.frameTime;
        placeTimer = Math.max(placeTimer - d * getSpeed(), 0);
        update();
        if (placePos != null) {
            event.renderer.box(placePos.down(), new Color(255, 0, 0, 50), new Color(255, 0, 0, 255), ShapeMode.Both, 0);
        }
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
                    if (vec.size() >= extSmoothness.get()) {
                        for (int i = vec.size() - extSmoothness.get(); i <= 0; i++) {
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
                for (int i = 0; i < extrapolation.get(); i++) {
                    // x
                    if (!inside(player, box.offset(x, 0, 0))) {
                        box = box.offset(x, 0, 0);
                    }

                    // z
                    if (!inside(player, box.offset(0, 0, z))) {
                        box = box.offset(0, 0, z);
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

    void update() {
        placePos = getPlacePos((int) Math.ceil(placeRange.get()));
        Entity expEntity = null;
        double[] value = null;
        Hand handToUse = getHand(Items.END_CRYSTAL);
        if (!pausedCheck() && handToUse != null) {
            for (Entity en : mc.world.getEntities()) {
                if (en.getType().equals(EntityType.END_CRYSTAL)) {
                    if (canExplode(en.getPos())) {
                        double[] dmg = getDmg(en.getPos())[0];
                        if ((expEntity == null || value == null) ||
                            (dmgCheckMode.equals(DmgCheckMode.Suicidal) && dmg[0] > value[0]) ||
                            (dmgCheckMode.equals(DmgCheckMode.Balanced) && dmg[2] / dmg[0] < value[2] / dmg[0]) ||
                            (dmgCheckMode.equals(DmgCheckMode.Safe) && dmg[2] < value[2])) {
                            expEntity = en;
                            value = dmg;
                        }
                    }
                }
            }
        }
        if (expEntity != null) {
            if (!isAttacked(expEntity.getId())) {
                explode(expEntity.getId(), expEntity, expEntity.getPos(), true);
            }
        }
        if (placePos != null) {
            if (handToUse != null) {
                if (!placePos.equals(lastPos)) {
                    lastPos = placePos;
                    placeTimer = 0;
                }
                if (!pausedCheck()) {
                    if (placeTimer <= 0) {
                        placeTimer = 1;
                        placeCrystal(placePos.down(), handToUse);
                    }
                }
            }
        } else {
            lastPos = null;
        }
    }

    boolean blocked(BlockPos pos) {
        return false;
    }

    void placeCrystal(BlockPos pos, Hand handToUse) {
        if (handToUse != null && pos != null && mc.player != null) {
            //own.add(pos.up());
            //blocked.add(new Box(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5));
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(handToUse,
                new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, false), 0));
            swing(handToUse, placeSwingMode.get());
            //if (idPredict.get()) {
            //    predictAdd(highestID() + 1, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), false,
            //        idDelay.get());
            //}
        }
    }

    boolean isAttacked(int id) {
        return attacked.contains(id);
    }

    void explode(int id, Entity en, Vec3d vec, boolean checkSetDead) {
        if (en != null) {
            attackEntity(en, checkSetDead, true, true);
        } else {
            attackID(id, vec, checkSetDead, true, true);
        }
    }

    private void attackID(int id, Vec3d pos, boolean checkSD, boolean swing, boolean confirm) {
        Hand handToUse = getHand(Items.END_CRYSTAL);
        if (handToUse != null && !pausedCheck()) {
            EndCrystalEntity en = new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z);
            en.setId(id);
            attackEntity(en, checkSD, swing, confirm);
        }
    }

    private void attackEntity(Entity en, boolean checkSD, boolean swing, boolean confirm) {
        if (mc.player != null) {
            Hand handToUse = getHand(Items.END_CRYSTAL);
            if (handToUse != null) {
                if (swing && explodeSwing.get()) {swing(handToUse, explodeSwingMode.get());}
                attacked.add(en.getId(), expSpeed.get());
                delayTimer = 0;
                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));
                if (swing) {swing(handToUse, explodeSwingMode.get());}
                if (confirm && clearSend.get()) {blocked.clear();}
                if (setDead.get() && checkSD) {
                    Managers.DELAY.add(() -> setEntityDead(en), setDeadDelay.get());
                }
            }
        }
    }

    boolean canExplode(Vec3d vec) {
        if (!inExplodeRange(vec.add(0, expHeight.get(), 0))) {return false;}
        double[][] result = getDmg(vec);
        if (!explodeDamageCheck(result[0], result[1])) {return false;}
        return true;
    }

    private Hand getHand(Item item) {
        if (Managers.HOLDING.isHolding(item)) {
            return Hand.MAIN_HAND;
        }
        if (mc.player.getOffHandStack().getItem().equals(item)) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    private void swing(Hand hand, SwingMode mode) {
        if (mode == SwingMode.Full || mode == SwingMode.Packet) {
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
        }
        if (mode == SwingMode.Full || mode == SwingMode.Client) {
            mc.player.swingHand(hand);
        }
    }

    private boolean pausedCheck() {
        if (mc.player != null) {
            return pauseEat.get() && (mc.player.isUsingItem() && mc.player.isHolding(Items.ENCHANTED_GOLDEN_APPLE));
        }
        return true;
    }

    void setEntityDead(Entity en) {
        en.remove(Entity.RemovalReason.KILLED);
    }

    BlockPos getPlacePos(int r) {
        BlockPos bestPos = null;
        double[] highest = null;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    // Checks if crystal can be placed
                    if (!air(pos) || !(!oldVerPlacements.get() || air(pos.up())) || !inPlaceRange(placeRangePos(pos)) ||
                        !crystalBlock(pos.down())) {continue;}

                    // Calculates damages and healths
                    double[][] result = getDmg(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));

                    // Checks if damages are valid
                    if (!placeDamageCheck(result[0], result[1], highest)) {continue;}
                    bestPos = pos;
                    highest = result[0];
                }
            }
        }
        return bestPos;
    }

    boolean placeDamageCheck(double[] dmg, double[] health, double[] highest) {
        //  0 = enemy, 1 = friend, 2 = self

        //  Dmg Check
        if (highest != null) {
            if (dmgCheckMode.equals(DmgCheckMode.Suicidal) && dmg[0] < highest[0]) {
                return false;
            }
            if (dmgCheckMode.equals(DmgCheckMode.Balanced) && dmg[2] / dmg[0] > highest[2] / highest[0]) {
                return false;
            }
            if (dmgCheckMode.equals(DmgCheckMode.Safe) && dmg[2] > highest[2]) {
                return false;
            }
        }

        //  Force/anti-pop check
        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (dmg[2] * antiSelfPop.get() >= playerHP) {return false;}
        if (dmg[1] * antiFriendPop.get() >= health[1]) {return false;}
        if (dmg[0] * forcePop.get() >= health[0]) {return true;}

        //  Min Damage
        if (dmg[0] < minPlace.get()) {return false;}

        //  Max Damage
        if (dmg[1] > maxFriendPlace.get()) {return false;}
        if (dmg[1] / dmg[0] > maxFriendPlaceRatio.get()) {return false;}
        if (dmg[2] > maxSelfPlace.get()) {return false;}
        if (dmg[2] / dmg[0] > maxSelfPlaceRatio.get()) {return false;}
        return true;
    }

    boolean explodeDamageCheck(double[] dmg, double[] health) {
        //  0 = enemy, 1 = friend, 2 = self

        //  Force/anti-pop check
        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (dmg[2] * forcePop.get() >= playerHP) {return false;}
        if (dmg[1] * antiFriendPop.get() >= health[1]) {return false;}
        if (dmg[0] * forcePop.get() >= health[0]) {return true;}

        //  Min Damage
        if (dmg[0] < minPlace.get()) {return false;}

        //  Max Damage
        if (dmg[1] > maxFriendPlace.get()) {return false;}
        if (dmg[1] / dmg[0] > maxFriendPlaceRatio.get()) {return false;}
        if (dmg[2] > maxSelfPlace.get()) {return false;}
        if (dmg[2] / dmg[0] > maxSelfPlaceRatio.get()) {return false;}
        return true;
    }

    double[][] getDmg(Vec3d vec) {
        highestEnemy = -1;
        highestFriend = -1;
        self = -1;
        enemyHP = 0;
        friendHP = 0;
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
        return (pl == mc.player && selfExt.get()) || (Friends.get().isFriend(pl) && friendExt.get()) ||
            (pl != mc.player && !Friends.get().isFriend(pl) && enemyExt.get());
    }

    boolean air(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);
    }
    boolean crystalBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) ||
            mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK);
    }
    boolean inPlaceRange(Vec3d vec) {
        return dist(mc.player.getPos().add(-vec.x, -vec.y, -vec.z)) <= placeRange.get();
    }
    boolean inExplodeRange(Vec3d vec) {
        return dist(mc.player.getPos().add(-vec.x, -vec.y, -vec.z)) <= placeRange.get();
    }
    double dist(Vec3d distances) {
        return Math.sqrt(distances.x * distances.x + distances.y * distances.y + distances.z * distances.z);
    }
    Vec3d placeRangePos(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight.get(), pos.getZ() + 0.5);
    }
    double getSpeed() {
        return placeSpeed.get();
    }
}

