package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.managers.DelayManager;
import kassuk.addon.blackout.managers.HoldingManager;
import kassuk.addon.blackout.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
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
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoCrystalPlus extends Module {
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
    private final Setting<Boolean> shield = sgGeneral.add(new BoolSetting.Builder()
        .name("Shield")
        .description("Spams crystals to save you.")
        .defaultValue(true)
        .visible(place::get)
        .build()
    );
    private final Setting<Boolean> explode = sgGeneral.add(new BoolSetting.Builder()
        .name("Explode")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> eatPause = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause When Eating")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<ListenerMode> calcMode = sgGeneral.add(new EnumSetting.Builder<ListenerMode>()
        .name("Calculation Mode")
        .description(".")
        .defaultValue(ListenerMode.TickPre)
        .build()
    );

    //  Place Page

    private final Setting<Boolean> strictPlace = sgPlace.add(new BoolSetting.Builder()
        .name("Strict Place")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> instantPlace = sgPlace.add(new BoolSetting.Builder()
        .name("Instant Place")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> placeSpeed = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Speed")
        .description(".")
        .defaultValue(10)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> slowDamage = sgPlace.add(new DoubleSetting.Builder()
        .name("Slow Damage")
        .description(".")
        .defaultValue(4)
        .range(0, 36)
        .sliderRange(0, 1000)
        .build()
    );
    private final Setting<Double> slowSpeed = sgPlace.add(new DoubleSetting.Builder()
        .name("Slow Speed")
        .description(".")
        .defaultValue(2)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> multiPlace = sgPlace.add(new BoolSetting.Builder()
        .name("Multi Place")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> multiDelay = sgPlace.add(new DoubleSetting.Builder()
        .name("Multi Delay")
        .description(".")
        .defaultValue(0.05)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Double> placeRange = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Range")
        .description(".")
        .defaultValue(6)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> placeSwing = sgPlace.add(new BoolSetting.Builder()
        .name("Place Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> prePlaceSwing = sgPlace.add(new BoolSetting.Builder()
        .name("Pre Place Swing")
        .description(".")
        .defaultValue(true)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<SwingMode> placeSwingMode = sgPlace.add(new EnumSetting.Builder<SwingMode>()
        .name("Place Swing Mode")
        .description(".")
        .defaultValue(SwingMode.Full)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> clearSend = sgPlace.add(new BoolSetting.Builder()
        .name("Clear Blocked On Send")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> clearExplode = sgPlace.add(new BoolSetting.Builder()
        .name("Clear Blocked On Explode")
        .description(".")
        .defaultValue(true)
        .build()
    );

    //  Shield Page
    private final Setting<Double> shieldHealth = sgShield.add(new DoubleSetting.Builder()
        .name("Shield Health")
        .description(".")
        .defaultValue(15)
        .range(0, 20)
        .sliderRange(0, 20)
        .visible(() -> shield.get() && shield.isVisible())
        .build()
    );
    private final Setting<Boolean> shieldMultiPlace = sgShield.add(new BoolSetting.Builder()
        .name("Shield Multi Place")
        .description(".")
        .defaultValue(false)
        .visible(() -> shield.get() && shield.isVisible())
        .build()
    );
    private final Setting<Double> shieldMax = sgShield.add(new DoubleSetting.Builder()
        .name("Shield Max Damage")
        .description(".")
        .defaultValue(6)
        .range(0, 20)
        .sliderMax(20)
        .visible(() -> shield.get() && shield.isVisible())
        .build()
    );
    private final Setting<Double> shieldMin = sgShield.add(new DoubleSetting.Builder()
        .name("Shield Min Damage")
        .description(".")
        .defaultValue(1)
        .range(0, 20)
        .sliderMax(10)
        .visible(() -> shield.get() && shield.isVisible())
        .build()
    );

    //  Explode Page
    private final Setting<Boolean> instantExplode = sgExplode.add(new BoolSetting.Builder()
        .name("Instant Explode")
        .description(".")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> explodeSpeed = sgExplode.add(new DoubleSetting.Builder()
        .name("Explode Speed")
        .description(".")
        .defaultValue(10)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> explodeRange = sgExplode.add(new DoubleSetting.Builder()
        .name("Break Range")
        .description(".")
        .defaultValue(6)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> explodeWalls = sgExplode.add(new DoubleSetting.Builder()
        .name("Break Walls Range")
        .description(".")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> onlyExplodeWhenHolding = sgExplode.add(new BoolSetting.Builder()
        .name("Only Explode When Holding")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> setDead = sgExplode.add(new BoolSetting.Builder()
        .name("Set Dead")
        .description("Removes crystals.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> instantSetDead = sgExplode.add(new BoolSetting.Builder()
        .name("Instant Set Dead")
        .description(".")
        .defaultValue(false)
        .visible(setDead::get)
        .build()
    );
    private final Setting<Double> sdDelay = sgExplode.add(new DoubleSetting.Builder()
        .name("Set Dead Delay (ms)")
        .description(".")
        .defaultValue(50)
        .range(0, 1000)
        .sliderRange(0, 1000)
        .visible(() -> setDead.get() && !instantSetDead.get())
        .build()
    );
    private final Setting<Boolean> explodeSwing = sgExplode.add(new BoolSetting.Builder()
        .name("Explode Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> preExplodeSwing = sgExplode.add(new BoolSetting.Builder()
        .name("Pre Explode Swing")
        .description(".")
        .defaultValue(true)
        .visible(explodeSwing::get)
        .build()
    );
    private final Setting<SwingMode> explodeSwingMode = sgExplode.add(new EnumSetting.Builder<SwingMode>()
        .name("Explode Swing Mode")
        .description(".")
        .defaultValue(SwingMode.Full)
        .visible(explodeSwing::get)
        .build()
    );

    //  Closest Page
    private final Setting<Boolean> closestPlace = sgClosest.add(new BoolSetting.Builder()
        .name("Closest Place")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> closestPlaceRange = sgClosest.add(new BoolSetting.Builder()
        .name("Closest Place Range")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> closestExpRange = sgClosest.add(new BoolSetting.Builder()
        .name("Closest Explode Range")
        .description(".")
        .defaultValue(false)
        .build()
    );

    //  Misc Page
    private final Setting<Boolean> idPredict = sgMisc.add(new BoolSetting.Builder()
        .name("ID Predict")
        .description("Attacks crystals before they spawn.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> idDelay = sgMisc.add(new DoubleSetting.Builder()
        .name("ID Packet Delay (ms)")
        .description(".")
        .defaultValue(50)
        .range(0, 1000)
        .sliderRange(0, 1000)
        .visible(idPredict::get)
        .build()
    );
    private final Setting<Boolean> idSingleSwing = sgMisc.add(new BoolSetting.Builder()
        .name("Single Swing")
        .description("Attacks crystals before they spawn.")
        .defaultValue(true)
        .visible(idPredict::get)
        .build()
    );
    private final Setting<Integer> idOffset = sgMisc.add(new IntSetting.Builder()
        .name("ID Offset")
        .description("Attacks crystals before they spawn.")
        .defaultValue(0)
        .sliderRange(-10, 10)
        .visible(idPredict::get)
        .build()
    );
    private final Setting<Integer> idPackets = sgMisc.add(new IntSetting.Builder()
        .name("ID Packets")
        .description("Attacks crystals before they spawn.")
        .defaultValue(1)
        .sliderRange(-10, 10)
        .visible(idPredict::get)
        .build()
    );

    private final Setting<Boolean> allowOffhand = sgMisc.add(new BoolSetting.Builder()
        .name("Allow Offhand")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> preferMainHand = sgMisc.add(new BoolSetting.Builder()
        .name("Prefer Mainhand")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .visible(allowOffhand::get)
        .build()
    );

    //  Damage Page

    private final Setting<Integer> forcePop = sgDamage.add(new IntSetting.Builder()
        .name("Force Pop")
        .description("Ignores damage limits if enemy will pop in x hits")
        .defaultValue(2)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> antiPop = sgDamage.add(new IntSetting.Builder()
        .name("Anti Pop")
        .description("Doesn't place or explode if you can pop in x hits.")
        .defaultValue(1)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> suicidal = sgDamage.add(new BoolSetting.Builder()
        .name("Suicidal")
        .description("Attacks if both pop")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> minHealthLeft = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Health Left")
        .description(".")
        .defaultValue(4)
        .range(0, 36)
        .sliderMax(36)
        .build()
    );
    private final Setting<Double> minPlaceDamage = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Place Damage")
        .description(".")
        .defaultValue(5)
        .range(0, 20)
        .sliderMax(20)
        .build()
    );
    private final Setting<Double> maxSelfPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Place Damage")
        .description(".")
        .defaultValue(0.7)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> alwaysOwn = sgDamage.add(new BoolSetting.Builder()
        .name("Always Own")
        .description("Always breaks own crystals")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> ignoreBreak = sgDamage.add(new BoolSetting.Builder()
        .name("Ignore Explode Damage")
        .description("Ignores explode damage")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> minExplodeDamage = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Explode Damage")
        .description(".")
        .defaultValue(5)
        .range(0, 20)
        .sliderMax(20)
        .visible(() -> !ignoreBreak.get())
        .build()
    );
    private final Setting<Double> maxSelfBreak = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Explode Damage")
        .description(".")
        .defaultValue(0.7)
        .range(0, 10)
        .sliderMax(10)
        .visible(() -> !ignoreBreak.get())
        .build()
    );

    //  Rotate Page

    private final Setting<Boolean> rotate = sgRotate.add(new BoolSetting.Builder()
        .name("Rotate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> rotationHeight = sgRotate.add(new DoubleSetting.Builder()
        .name("Rotation Height")
        .description(".")
        .defaultValue(0.3)
        .range(0, 3)
        .sliderMax(3)
        .visible(rotate::get)
        .build()
    );

    //  Extrapolation Page

    private final Setting<Boolean> extrapolate = sgExtrapolation.add(new BoolSetting.Builder()
        .name("Extrapolate")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> extTicks = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation Ticks")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> motionTicks = sgExtrapolation.add(new IntSetting.Builder()
        .name("Motion Smoothening")
        .description(".")
        .defaultValue(3)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );
    private final Setting<Boolean> stepPredict = sgExtrapolation.add(new BoolSetting.Builder()
        .name("Step Predict")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> stepHeight = sgExtrapolation.add(new DoubleSetting.Builder()
        .name("Step Height")
        .description(".")
        .defaultValue(2)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Integer> stepTicks = sgExtrapolation.add(new IntSetting.Builder()
        .name("Step Ticks")
        .description(".")
        .defaultValue(4)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );
    private final Setting<Boolean> fallPredict = sgExtrapolation.add(new BoolSetting.Builder()
        .name("Fall Predict")
        .description(".")
        .defaultValue(true)
        .build()
    );

    //  Raytrace Page

    private final Setting<Boolean> placeRangeFromEyes = sgRaytrace.add(new BoolSetting.Builder()
        .name("Place Range From Eyes")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> placeRangeStartHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Place Range Start Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .visible(() -> !placeRangeFromEyes.get())
        .build()
    );
    private final Setting<Double> placeRangeHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Place Range End Height")
        .description(".")
        .defaultValue(1)
        .range(0, 2)
        .sliderMax(2)
        .build()
    );

    private final Setting<Boolean> breakRangeFromEyes = sgRaytrace.add(new BoolSetting.Builder()
        .name("Break Range From Eyes")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> breakRangeStartHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Break Range Start Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .visible(() -> !breakRangeFromEyes.get())
        .build()
    );
    private final Setting<Double> breakRangeHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Break Range End Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .build()
    );
    private final Setting<Boolean> raytraceFromEyes = sgRaytrace.add(new BoolSetting.Builder()
        .name("Raytrace From Eyes")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> playerRayStartHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Ray Start Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .visible(() -> !raytraceFromEyes.get())
        .build()
    );
    private final Setting<Double> rayEndHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Ray End Height")
        .description(".")
        .defaultValue(2)
        .range(0, 3)
        .sliderMax(3)
        .build()
    );

    //  Render Page

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 150))
        .build()
    );
    private final Setting<Boolean> animation = sgRender.add(new BoolSetting.Builder()
        .name("Animation")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> renderHitBox = sgRender.add(new BoolSetting.Builder()
        .name("Render Hitboxes")
        .description("Renders hitboxes calculated by extrapolation.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> renderMoveSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Move Speed")
        .description(".")
        .defaultValue(5)
        .range(0, 10)
        .sliderMax(10)
        .visible(animation::get)
        .build()
    );

    private final Setting<Double> animationSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Speed")
        .description(".")
        .defaultValue(5)
        .range(0, 10)
        .sliderMax(10)
        .visible(animation::get)
        .build()
    );
    public enum SwingMode {
        None,
        Full,
        Client,
        Packet
    }

    public enum ListenerMode {
        TickPre,
        TickPost,
        Tick,
        Render,
        MotionUpdate
    }
    private int lowest;

    protected BlockPos placePos;
    protected BlockPos multiPos;
    protected BlockPos lastPos;
    private double renderAnim;
    private Vec3d renderPos;
    private double height;
    private List<Box> extPos = new ArrayList<>();
    private List<PlayerEntity> enemies = new ArrayList<>();
    private Map<String, List<Vec3d>> motions = new HashMap<>();
    private List<BlockPos> own;
    private float placeTimer = 0;
    private List<AttackTimer> attacked = new ArrayList<>();
    private List<Box> blocked = new ArrayList<>();
    private DelayManager DELAY = new DelayManager();
    private HoldingManager HOLDING = new HoldingManager();
    public AutoCrystalPlus() {
        super(BlackOut.ANARCHY, "Auto Crystal+", "Breaks and places crystals automatically.");
    }

    // Listeners

    @Override
    public void onActivate() {
        super.onActivate();
        motions = new HashMap<>();
        own = new ArrayList<>();
        renderAnim = 0;
        lowest = Integer.MIN_VALUE;
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onTickPre(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            for (PlayerEntity pl : mc.world.getPlayers()) {
                String key = pl.getName().getString();
                if (motions.containsKey(key)) {
                    List<Vec3d> vec = motions.get(key);
                    if (vec != null) {
                        if (vec.size() >= motionTicks.get()) {
                            for (int i = vec.size() - motionTicks.get(); i <= 0; i++) {
                                vec.remove(0);
                            }
                        }
                        vec.add(motionCalc(pl));
                    } else {
                        List<Vec3d> list = new ArrayList<>();
                        list.add(motionCalc(pl));
                        motions.put(key, list);
                    }
                } else {
                    List<Vec3d> list = new ArrayList<>();
                    list.add(motionCalc(pl));
                    motions.put(key, list);
                }
            }
        }
        if (rotate.get() && placePos != null) {
            double yaw = Rotations.getYaw(new Vec3d(placePos.getX() + 0.5, placePos.getY() + rotationHeight.get(), placePos.getZ() + 0.5));
            double pitch = Rotations.getPitch(new Vec3d(placePos.getX() + 0.5, placePos.getY() + rotationHeight.get(), placePos.getZ() + 0.5));

            Rotations.rotate(yaw, pitch);
        }
        if (calcMode.get().equals(ListenerMode.Tick) || calcMode.get().equals(ListenerMode.TickPre)) {update();}
    }
    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onTickPost(TickEvent.Pre event) {
        if (calcMode.get().equals(ListenerMode.Tick) || calcMode.get().equals(ListenerMode.TickPost)) {update();}
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onMoveUpdate(PlayerMoveEvent event) {
        if (calcMode.get().equals(ListenerMode.MotionUpdate)) {update();}
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender3D(Render3DEvent event) {
        double d = event.frameTime;
        placeTimer = (float) Math.max(placeTimer - d * getSpeed(), 0);
        List<AttackTimer> toRemove2 = new ArrayList<>();
        if (attacked != null && !attacked.isEmpty()) {
            for (AttackTimer item : attacked) {
                if (item != null) {
                    if (!item.isValid()) {
                        toRemove2.add(item);
                    } else {
                        item.update((float) (d * explodeSpeed.get()));
                    }
                }
            }
            toRemove2.forEach(attacked::remove);
        }

        if (calcMode.get().equals(ListenerMode.Render)) {update();}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof EntitySpawnS2CPacket) {
            EntitySpawnS2CPacket packet = (EntitySpawnS2CPacket) event.packet;
            lowest = packet.getId();
        }
        if (event.packet instanceof ExplosionS2CPacket) {
            if (clearExplode.get()) {
                blocked.clear();
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntity(PacketEvent.Receive event) {
        if (event.packet instanceof EntitySpawnS2CPacket) {
            EntitySpawnS2CPacket packet = (EntitySpawnS2CPacket) event.packet;
            List<Box> toRemove = new ArrayList<>();
            blocked.forEach(item -> {
                if (new BlockPos((item.minX + item.maxX) / 2, item.minY + 0.5, (item.minZ + item.maxZ) / 2).equals(new BlockPos(packet.getX(), packet.getY(), packet.getZ()))) {
                    toRemove.add(item);
                }
            });
            toRemove.forEach(blocked::remove);
            if (mc.player != null && mc.world != null && explode.get() && instantExplode.get()) {
                if (!pausedCheck()) {
                    if (packet.getEntityTypeId() == EntityType.END_CRYSTAL) {
                        Vec3d pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                        if (canBreak(pos) && !isAttacked(packet.getId())) {
                            explode(packet.getId(), null, pos, true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player != null && mc.world != null) {
            if (placePos != null) {
                if (animation.get()) {
                    renderPos = smoothMove(event, renderPos,
                        new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()), renderMoveSpeed.get());
                } else {
                    renderPos = new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ());
                }
            }
            if (multiPos != null) {
                event.renderer.box(new Box(multiPos.getX(), multiPos.getY(), multiPos.getZ(),
                    multiPos.getX() + 1, multiPos.getY() + 1, multiPos.getZ() + 1), new Color(color.get().r, 255, color.get().b,
                    (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both, 0);
            }
            if (animation.get()) {
                renderAnim = placePos != null && !pausedCheck() ?
                    (renderAnim + animationSpeed.get() > 100 ? 100 : renderAnim + animationSpeed.get())
                    :
                    (renderAnim - animationSpeed.get() < 0 ? 0 : renderAnim - animationSpeed.get());
            }
            if (renderPos != null && (!animation.get() || renderAnim > 0)) {
                Vec3d v = new Vec3d(renderPos.getX() + 0.5, renderPos.getY() + 0.5, renderPos.getZ() + 0.5);
                double progress = renderAnim / 100 / 2;
                Box toRender = new Box(v.x - progress, v.y - progress + height, v.z - progress, v.x + progress, v.y + progress - height, v.z + progress);
                event.renderer.box(toRender, new Color(color.get().r, color.get().g, color.get().b,
                    (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both, 0);
            }
            if (!extPos.isEmpty() && renderHitBox.get()) {
                for (Box box : extPos) {
                    event.renderer.box(box, new Color(color.get().r, color.get().g, color.get().b,
                        (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both, 0);
                }
            }
        }
    }

    // Other stuff

    private void update() {
        if (mc.player != null && mc.world != null) {
            extPos = getExtPos();
            Hand handToUse = getHand(Items.END_CRYSTAL, preferMainHand.get(), false);
            placePos = findBestPos(null);
            Entity expEntity = null;
            double highest = 0;
            if (!pausedCheck()) {
                for (Entity en : mc.world.getEntities()) {
                    if (en.getType().equals(EntityType.END_CRYSTAL)) {
                        if (canBreak(en.getPos())) {
                            double[] dmg = highestDmg(en.getBlockPos().down());
                            if (expEntity == null || dmg[0] > highest) {
                                expEntity = en;
                                highest = dmg[0];
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
                multiPos = multiPlace.get() || (shouldShield() && shieldMultiPlace.get()) ? findBestPos(placePos) : null;
                if (handToUse != null) {
                    if (!placePos.equals(lastPos)) {
                        lastPos = placePos;
                        placeTimer = 0;
                    }
                    if (!pausedCheck()) {
                        if ((!blocked(placePos) || !strictPlace.get()) && place.get() && (placeTimer <= 0 || (instantPlace.get() && !shouldSlow(placePos) && !blocked(placePos)))) {
                            placeTimer = 1;
                            placeCrystal(placePos);
                        }
                    }
                }
            } else {
                multiPos = null;
                lastPos = null;
            }
        }
    }

    private double getSpeed() {
        if (placePos == null) {placeSpeed.get();}
        return shouldSlow(placePos) ? slowSpeed.get() : placeSpeed.get();
    }

    private boolean blocked(BlockPos pos) {
        for (Box box : blocked) {
            if (new Box(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5,
                pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5).intersects(box)) {
                return true;
            }
        }
        return false;
    }

    private Vec3d motionCalc(PlayerEntity en) {
        return new Vec3d(en.getX() - en.prevX, en.getY() - en.prevY, en.getZ() - en.prevZ);
    }

    private List<Box> getExtPos() {
        List<Box> list = new ArrayList<>();
        enemies = new ArrayList<>();
        if (!mc.world.getPlayers().isEmpty()) {
            for (int p = 0; p < mc.world.getPlayers().size(); p++) {
                PlayerEntity en = mc.world.getPlayers().get(p);
                if (en != mc.player && !Friends.get().isFriend(en)) {
                    if (!extrapolate.get()) {
                        list.add(en.getBoundingBox());
                        continue;
                    }
                    if (!motions.isEmpty()) {
                        Vec3d motion = average(motions.get(en.getName().getString()));
                        double x = motion.x;
                        double y = motion.y;
                        double z = motion.z;
                        Box box = en.getBoundingBox();
                        if (inside(en, box)) {
                            list.add(box);
                            enemies.add(en);
                            continue;
                        }
                        for (int i = 0; i < extTicks.get(); i++) {
                            box = box.offset(x, 0, z);
                            if (inside(en, box)) {
                                if (stepPredict.get()) {
                                    Box stepSimulation = box;
                                    for (int j = 0; j < stepTicks.get(); j++) {
                                        stepSimulation = stepSimulation.offset(0, stepHeight.get() / stepTicks.get(), 0);
                                        if (!inside(en, stepSimulation)) {
                                            box = stepSimulation;
                                            break;
                                        }
                                    }
                                    box = box.offset(-x, 0, -z);
                                } else {
                                    box = box.offset(-x, 0, -z);
                                    list.add(box);
                                    enemies.add(en);
                                    break;
                                }
                            } else {
                                if (fallPredict.get()) {
                                    if (!inside(en, box.offset(0, -0.05, 0))) {
                                        y -= 0.08;
                                        box = box.offset(0, y, 0);
                                        if (inside(en, box)) {
                                            box = box.offset(0, -y, 0);
                                        }
                                    } else {
                                        y = 0;
                                    }
                                }
                            }
                        }
                        list.add(box);
                        enemies.add(en);
                    }
                }
            }
        }
        return list;
    }

    private Vec3d average(List<Vec3d> vec) {
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

    private boolean inside(PlayerEntity en, Box bb) {
        return mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }

    private boolean shouldSlow(BlockPos pos) {
        if (pos == null) {return false;}
        return highestDmg(pos)[0] <= slowDamage.get() && !shouldShield();
    }

    private void placeCrystal(BlockPos pos) {
        Hand handToUse = getHand(Items.END_CRYSTAL, preferMainHand.get(), false);
        if (handToUse != null && !pausedCheck() && pos != null) {
            BlackOut.LOG.info("AutoCrystal: Place");
            Hand swingHand = getHand(Items.END_CRYSTAL, preferMainHand.get(), true);
            swing(swingHand, placeSwingMode.get(), placeSwing.get(), prePlaceSwing.get(), true);
            own.add(pos.up());
            blocked.add(new Box(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5));
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(handToUse,
                new BlockHitResult(closestPlace.get() ? closestPoint(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) :
                    new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, false), 0));
            swing(swingHand, placeSwingMode.get(), placeSwing.get(), prePlaceSwing.get(), false);
            if (idPredict.get()) {
                predictAdd(highestID() + 1, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), false,
                    idDelay.get());
            }
        }
    }

    private void explode(int id, Entity en, Vec3d pos, boolean checkSetDead) {
        BlackOut.LOG.info("AutoCrystal: Explode");
        if (en != null) {
            attackEntity(en, checkSetDead, true, true);
        } else {
            attackID(id, pos, checkSetDead, true, true);
        }
        if (multiPlace.get() || (shouldShield() && shieldMultiPlace.get()) && multiPos != null && placePos != null && new BlockPos(pos).equals(placePos.up())) {
            Hand handToUse = getHand(Items.END_CRYSTAL, preferMainHand.get(), false);
            if (handToUse != null) {
                if (!pausedCheck()) {
                    BlackOut.LOG.info("AutoCrystal: MultiPlace");
                    DELAY.add(() -> placeCrystal(multiPos), (float) (multiDelay.get() * 1f));
                }
            }
        }
    }

    private BlockPos findBestPos(BlockPos blockedPos) {
        BlockPos position = null;
        if (mc.player != null && mc.world != null) {
            double highestDMG = 0;
            double highestDist = 0;
            int calcRange = (int) Math.ceil(placeRange.get());
            for (int y = calcRange; y >= -calcRange; y--) {
                for (int x = -calcRange; x <= calcRange; x++) {
                    for (int z = -calcRange; z <= calcRange; z++) {
                        BlockPos pos = new BlockPos(x + mc.player.getBlockPos().getX(),
                            y + mc.player.getBlockPos().getY(), z + mc.player.getBlockPos().getZ());
                        if (canBePlaced(pos, blockedPos)) {
                            double[] dmg = highestDmg(pos);
                            double self = getSelfDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), pos);
                            double dist = OLEPOSSUtils.distance(new Vec3d(x + mc.player.getBlockPos().getX() + 0.5,
                                    y + mc.player.getBlockPos().getY(), z + mc.player.getBlockPos().getZ() + 0.5),
                                playerRangePos(true));
                            if (placeDamageCheck(dmg[0], self, dmg[1], highestDMG, dist, highestDist)) {
                                highestDMG = dmg[0];
                                highestDist = dist;
                                position = pos;
                            }
                        }
                    }
                }
            }
        }
        return position;
    }

    private boolean placeDamageCheck(double dmg, double self, double health, double highest, double distance, double highestDist) {
        if (dmg < highest) {return false;}
        if (dmg == highest && distance > highestDist) {return false;}

        //  Force pop check
        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        boolean[] valid = new boolean[] {forcePop.get() * dmg > health, playerHP - (antiPop.get() * dmg) > minHealthLeft.get()};
        if (valid[0] && (valid[1] || suicidal.get())) {return true;}

        return (self / dmg <= maxSelfPlace.get() || (shouldShield(playerHP) && self <= shieldMax.get())) &&
            (dmg >= minPlaceDamage.get() || (shouldShield(playerHP) && dmg >= shieldMin.get()));
    }

    private boolean breakDamageCheck(double dmg, double self, double health, BlockPos pos) {
        if (ignoreBreak.get()) {return true;}
        if (own.contains(pos) && alwaysOwn.get()) {
            own.remove(pos);
            return true;
        }

        //  Force pop check
        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        boolean[] valid = new boolean[] {forcePop.get() * dmg > health, playerHP - (antiPop.get() * dmg) > minHealthLeft.get()};
        if (valid[0] && (valid[1] || suicidal.get())) {return true;}

        return (self / dmg <= maxSelfBreak.get() || (shouldShield(playerHP) && self <= shieldMax.get())) &&
            (dmg >= minExplodeDamage.get() || (shouldShield(playerHP) && dmg >= shieldMin.get()));
    }
    private boolean shouldShield() {
        if (mc.player == null) {return false;}
        return shouldShield(mc.player.getHealth() + mc.player.getAbsorptionAmount());
    }
    private boolean shouldShield(double hp) {return shield.get() && hp <= shieldHealth.get();}

    protected double[] highestDmg(BlockPos pos) {
        double highest = 0;
        double highestHP = 0;
        if (mc.player != null && mc.world != null) {
            for (int i = 0; i < enemies.size(); i++) {
                PlayerEntity enemy = enemies.get(i);
                Box ogBB = enemy.getBoundingBox();
                Vec3d ogPos = enemy.getPos();
                if (enemy != mc.player && !Friends.get().isFriend(enemy) && enemy.getHealth() > 0 && !enemy.isSpectator()) {
                    Vec3d vec = boxToPos(extPos.get(i));
                    enemy.setBoundingBox(extPos.get(i));
                    enemy.setPos(vec.x, vec.y, vec.z);
                    double dmg = DamageUtils.crystalDamage(enemy, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5),
                        false, pos, false);
                    enemy.setBoundingBox(ogBB);
                    if (mc.world.getPlayers().contains(enemy)) {
                        enemy.setPos(ogPos.x, ogPos.y, ogPos.z);
                    }
                    enemy.setPos(ogPos.x, ogPos.y, ogPos.z);
                    if (dmg > highest) {
                        highest = dmg;
                        highestHP = enemy.getHealth() + enemy.getAbsorptionAmount();
                    }
                }
            }
        }
        return new double[] {highest, highestHP};
    }

    private Vec3d boxToPos(Box box) {
        return new Vec3d((box.minX + box.maxX) / 2, box.minY, (box.minZ + box.maxZ) / 2);
    }

    protected boolean canBePlaced(BlockPos pos, BlockPos blockedPos) {
        if (mc.player != null && mc.world != null) {
            if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN &&
                mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK) {
                return false;
            }
            Box box = new Box(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
            if (blockedPos != null && box.intersects(new Box(blockedPos.getX() - 0.5, blockedPos.getY(), blockedPos.getZ() - 0.5,
                blockedPos.getX() + 1.5, blockedPos.getY() + 2, blockedPos.getZ() + 1.5))) {return false;}
            if (!mc.world.getBlockState(pos.offset(Direction.UP)).getBlock().equals(Blocks.AIR)) {
                return false;
            }
            if (!placeRangeCheck(pos)) {
                return false;
            }
            return !EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !(entity instanceof EndCrystalEntity && canBreak(entity.getPos())));
        }
        return false;
    }

    private Vec3d playerRangePos(boolean place) {
        if (place) {
            return new Vec3d(mc.player.getX(),
                placeRangeFromEyes.get() ? mc.player.getEyePos().y : mc.player.getY() + placeRangeStartHeight.get(), mc.player.getZ());
        } else {
            return new Vec3d(mc.player.getX(),
                breakRangeFromEyes.get() ? mc.player.getEyePos().y : mc.player.getY() + breakRangeStartHeight.get(), mc.player.getZ());
        }
    }

    private boolean placeRangeCheck(BlockPos pos) {
        return (OLEPOSSUtils.distance(playerRangePos(true),
            closestPlaceRange.get() ? closestPoint(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) :
                new Vec3d(pos.getX() + 0.5, pos.getY() + placeRangeHeight.get(), pos.getZ() + 0.5)) <= placeRange.get()) &&
            breakRangeCheck(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5));
    }

    private boolean breakRangeCheck(Vec3d pos) {
        return OLEPOSSUtils.distance(playerRangePos(false),
            closestExpRange.get() ? closestPoint(new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1)) :
                new Vec3d(pos.getX(), pos.getY() + breakRangeHeight.get(), pos.getZ())) <= getBreakRange(pos);
    }

    private Hand getHand(Item item, boolean preferMain, boolean swing) {
        if (!HOLDING.isHolding(item) && !mc.player.getOffHandStack().getItem().equals(item) && !swing) {
            return null;
        }
        if (allowOffhand.get() && mc.player.getOffHandStack().getItem().equals(item)) {
            if (preferMain && HOLDING.isHolding(item)) {
                return Hand.MAIN_HAND;
            } else {
                return Hand.OFF_HAND;
            }
        } else if (HOLDING.isHolding(item)) {
            return Hand.MAIN_HAND;
        }
        return swing ? Hand.MAIN_HAND : null;
    }

    private double getBreakRange(Vec3d pos) {
        Vec3d vec1 = new Vec3d(mc.player.getX(), raytraceFromEyes.get() ? mc.player.getEyePos().getY() :
            mc.player.getY() + playerRayStartHeight.get(), mc.player.getZ());
        Vec3d vec2 = new Vec3d(pos.getX(), pos.getY() + rayEndHeight.get(), pos.getZ());
        if (mc.world.raycast(new RaycastContext(vec1, vec2,
            RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() != HitResult.Type.BLOCK) {
            return explodeRange.get();
        } else {
            return explodeWalls.get();
        }
    }

    private boolean canBreak(Vec3d pos) {
        if (!explode.get()) {return false;}
        double self = getSelfDamage(pos ,new BlockPos(Math.floor(pos.x), Math.floor(pos.y) - 1, Math.floor(pos.z)));
        if (onlyExplodeWhenHolding.get() && getHand(Items.END_CRYSTAL, preferMainHand.get(), false) == null) {return false;}
        double[] dmg = highestDmg(new BlockPos(Math.floor(pos.x), Math.floor(pos.y) - 1, Math.floor(pos.z)));
        if (!breakDamageCheck(dmg[0], self, dmg[1], new BlockPos(Math.floor(pos.x), Math.floor(pos.y), Math.floor(pos.z)))) {return false;}
        return breakRangeCheck(new Vec3d(pos.x, pos.y, pos.z));
    }

    private boolean isAttacked(int id) {
        for (AttackTimer att : attacked) {
            if (att.id == id) {
                return true;
            }
        }
        return false;
    }

    private int highestID() {
        int highest = lowest;
        if (mc.world != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity.getId() > highest) {
                    highest = entity.getId();
                }
            }
        }
        return highest;
    }

    private void swing(Hand hand, SwingMode mode, boolean mainSetting, boolean timingSetting, boolean pre) {
        if (mainSetting && mc.player != null) {
            if (timingSetting == pre) {
                if (mode == SwingMode.Full || mode == SwingMode.Packet) {
                    mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
                }
                if (mode == SwingMode.Full || mode == SwingMode.Client) {
                    mc.player.swingHand(hand);
                }
            }
        }
    }
    private void predictAdd(int id, Vec3d pos, boolean checkSD, double delay) {
        DELAY.add(() -> predictAttack(id, pos, checkSD), (float) (delay / 1000));
    }
    private void predictAttack(int id, Vec3d pos, boolean checkSD) {
        if (idPackets.get() > 0) {
            for (int i = 0; i < idPackets.get(); i++) {
                int p = id + idOffset.get() + i;
                if (p != mc.player.getId()) {
                    attackID(p, pos, checkSD, !idSingleSwing.get() || i == 0, false);
                }
            }
        }
    }

    private void attackID(int id, Vec3d pos, boolean checkSD, boolean swing, boolean confirm) {
        Hand handToUse = getHand(Items.END_CRYSTAL, preferMainHand.get(), false);
        if (handToUse != null && !pausedCheck()) {
            EndCrystalEntity en = new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z);
            en.setId(id);
            attackEntity(en, checkSD, swing, confirm);
        }
    }

    private void attackEntity(Entity en, boolean checkSD, boolean swing, boolean confirm) {
        if (mc.player != null) {
            Hand handToUse = getHand(Items.END_CRYSTAL, preferMainHand.get(), true);
            if (handToUse != null) {
                if (swing) {swing(handToUse, explodeSwingMode.get(), explodeSwing.get(), preExplodeSwing.get(), true);}
                attacked.add(new AttackTimer(en.getId(), (float) (1 / explodeSpeed.get())));
                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));
                if (swing) {swing(handToUse, explodeSwingMode.get(), explodeSwing.get(), preExplodeSwing.get(), false);}
                if (confirm && clearSend.get()) {blocked.clear();}
                if (setDead.get() && checkSD) {
                    if (instantSetDead.get()) {
                        setEntityDead(en);
                    } else {
                        DELAY.add(() -> setEntityDead(en), (float) (sdDelay.get() * 1000));
                    }
                }
            }
        }
    }

    private boolean pausedCheck() {
        if (mc.player != null) {
            return eatPause.get() && (mc.player.isUsingItem() && mc.player.isHolding(Items.ENCHANTED_GOLDEN_APPLE));
        }
        return true;
    }

    public Vec3d smoothMove(Render3DEvent event, Vec3d current, Vec3d target, double speed) {
        if (current == null) {
            return target;
        }
        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);
        height = Math.sqrt(absX * absX + absZ * absZ) * speed / 100;
        double d = speed * event.frameTime;

        return new Vec3d(
            current.x > target.x ?
                (absX <= d * absX ? target.x : current.x - d * absX) :
                current.x != target.x ?
                    (absX <= d * absX ? target.x : current.x + d * absX) :
                    target.x
            ,
            current.y > target.y ?
                (absY <= d * absY ? target.y : current.y - d * absY) :
                current.y != target.y ?
                    (absY <= d * absY ? target.y : current.y + d * absY) :
                    target.y
            ,
            current.z > target.z ?
                (absZ <= d * absZ ? target.z : current.z - d * absZ) :
                current.z != target.z ?
                    (absZ <= d * absZ ? target.z : current.z + d * absZ) :
                    target.z);
    }

    private double getSelfDamage(Vec3d vec, BlockPos pos) {
        return DamageUtils.crystalDamage(mc.player, vec,
            false, pos.down(), false);
    }

    private void setEntityDead(Entity en) {
        if (mc.world != null) {
            en.kill();
        }
    }

    private Vec3d closestPoint(Box box) {
        Vec3d pos = mc.player.getEyePos();
        return new Vec3d(pos.x <= box.minX ? box.minX : Math.min(pos.x, box.maxX),
            pos.y <= box.minY ? box.minY : Math.min(pos.y, box.maxY),
            pos.z <= box.minZ ? box.minZ : Math.min(pos.z, box.maxZ));
    }

    private class AttackTimer {
        public int id;
        public float time;
        public AttackTimer(int id, float time) {
            this.id = id;
            this.time = time;
        }
        public void update(float delta) {
            time -= delta;
        }
        public boolean isValid() {
            return time >= 0;
        }
    }
}

