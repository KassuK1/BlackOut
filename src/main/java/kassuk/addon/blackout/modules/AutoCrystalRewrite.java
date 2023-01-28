package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.globalsettings.SwingSettings;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BODamageUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.timers.IntTimerList;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
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
    public AutoCrystalRewrite() {super(BlackOut.BLACKOUT, "Auto Crystal Rewrite", "Breaks and places crystals automatically (but better).");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgExplode = settings.createGroup("Explode");
    private final SettingGroup sgSwitch = settings.createGroup("Switch");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgRotate = settings.createGroup("Rotate");
    private final SettingGroup sgID = settings.createGroup("ID-Predict");
    private final SettingGroup sgExtrapolation = settings.createGroup("Extrapolation");
    private final SettingGroup sgRaytrace = settings.createGroup("Raytrace");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgDebug = settings.createGroup("Debug");

    //  General Page
    private final Setting<Boolean> place = sgGeneral.add(new BoolSetting.Builder()
        .name("Place")
        .description("Places crystals.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> explode = sgGeneral.add(new BoolSetting.Builder()
        .name("Explode")
        .description("Explodes crystals.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses while eating.")
        .defaultValue(true)
        .build()
    );

    //  Place Page
    private final Setting<Boolean> oldVerPlacements = sgPlace.add(new BoolSetting.Builder()
        .name("1.12 Placements")
        .description("Uses 1.12 crystal mechanics.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> ccPlacements = sgPlace.add(new BoolSetting.Builder()
        .name("CC Placements")
        .description("Uses crystalpvp.cc hitboxes.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> instantPlace = sgPlace.add(new BoolSetting.Builder()
        .name("Instant Non-Blocked")
        .description("Ignores delay after crystal hitbox has disappeared.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> instant = sgPlace.add(new BoolSetting.Builder()
        .name("Instant Place")
        .description("Places after exploding a crystal.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> placeSpeed = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Speed")
        .description("How many times should the module place per second.")
        .defaultValue(10)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> placeDelay = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("How many seconds after attacking a crystal should we place.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Double> slowDamage = sgPlace.add(new DoubleSetting.Builder()
        .name("Slow Damage")
        .description("Switches to slow speed when the target would take low damage.")
        .defaultValue(3)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> slowSpeed = sgPlace.add(new DoubleSetting.Builder()
        .name("Slow Speed")
        .description("How many times should the module place per second when damage is under slow damage.")
        .defaultValue(2)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> clearSend = sgPlace.add(new BoolSetting.Builder()
        .name("Clear Send")
        .description("Clears blocked positions when sending explode packet.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> clearReceive = sgPlace.add(new BoolSetting.Builder()
        .name("Clear Receive")
        .description("Clears blocked positions when receiving explode packet.")
        .defaultValue(false)
        .build()
    );

    //  Explode Page
    private final Setting<Boolean> instantExp = sgExplode.add(new BoolSetting.Builder()
        .name("Instant Explode")
        .description("Instantly sends attack packet after end crystal has spawned.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> expSpeed = sgExplode.add(new DoubleSetting.Builder()
        .name("Explode Speed")
        .description("How many times to hit crystal each second.")
        .defaultValue(2)
        .range(0.01, 20)
        .sliderRange(0.01, 20)
        .build()
    );
    private final Setting<Boolean> setDead = sgExplode.add(new BoolSetting.Builder()
        .name("Set Dead")
        .description("Hides the crystal after hitting it.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> setDeadDelay = sgExplode.add(new DoubleSetting.Builder()
        .name("Set Dead Delay")
        .description("How long after hitting should the crystal disappear.")
        .defaultValue(0.05)
        .range(0, 1)
        .sliderRange(0, 1)
        .visible(setDead::get)
        .build()
    );

    //  Switch Page
    private final Setting<SwitchMode> switchMode = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Mode for switching to crystal in mainhand. \nSimple - Switches to crystal when placing\nSmart - Switches to gapple (complicated)\nGapple - Switches to crystal when holding gapple and gapple when holding use key and crystals")
        .defaultValue(SwitchMode.Disabled)
        .build()
    );
    private final Setting<Boolean> expFriendly = sgSwitch.add(new BoolSetting.Builder()
        .name("Exp Friendly")
        .description("Doesn't switch to crystals when using any item.")
        .defaultValue(false)
        .visible(() -> switchMode.get().equals(SwitchMode.Smart))
        .build()
    );
    private final Setting<Boolean> alwaysGap = sgSwitch.add(new BoolSetting.Builder()
        .name("Always Gapple")
        .description("Switches to gapple even when not placing.")
        .defaultValue(false)
        .visible(() -> switchMode.get().equals(SwitchMode.Smart))
        .build()
    );
    private final Setting<Boolean> onlyCrystal = sgSwitch.add(new BoolSetting.Builder()
        .name("Only On Crystal")
        .description("Only switches to gapple when holding crystal.")
        .defaultValue(true)
        .visible(() -> !alwaysGap.get() && switchMode.get().equals(SwitchMode.Smart))
        .build()
    );
    private final Setting<Double> afterSwitchDelay = sgSwitch.add(new DoubleSetting.Builder()
        .name("After Switch Delay")
        .description("Time to wait after switching before hitting crystals.")
        .defaultValue(0.1)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    //  Damage Page
    private final Setting<DmgCheckMode> dmgCheckMode = sgDamage.add(new EnumSetting.Builder<DmgCheckMode>()
        .name("Dmg Check Mode")
        .description(".")
        .defaultValue(DmgCheckMode.Normal)
        .build()
    );
    private final Setting<Double> minPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Place")
        .description("Minimum enemy damage for placing.")
        .defaultValue(4)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxSelfPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Place")
        .description("Max self damage for placing.")
        .defaultValue(8)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxSelfPlaceRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Place Ratio")
        .description("Max self damage ratio for placing (self damage / enemy damage).")
        .defaultValue(0.3)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> maxFriendPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Place")
        .description("Max friend damage for placing.")
        .defaultValue(8)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxFriendPlaceRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Place Ratio")
        .description("Max friend damage ratio for placing (friend damage / enemy damage)..")
        .defaultValue(0.5)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> minExplode = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Explode")
        .description("Minimum enemy damage for exploding a crystal.")
        .defaultValue(2.5)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxSelfExp = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Explode")
        .description("Max self damage for exploding a crystal.")
        .defaultValue(9)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxSelfExpRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Explode Ratio")
        .description("Max self damage ratio for exploding a crystal (self damage / enemy damage).")
        .defaultValue(0.4)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> maxFriendExp = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Explode")
        .description("Max friend damage for exploding a crystal.")
        .defaultValue(12)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxFriendExpRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Explode Ratio")
        .description("Max friend damage ratio for exploding a crystal (friend damage / enemy damage).")
        .defaultValue(0.5)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> forcePop = sgDamage.add(new DoubleSetting.Builder()
        .name("Force Pop")
        .description("Ignores damage checks if any enemy will be popped in x hits.")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiFriendPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Friend Pop")
        .description("Cancels any action if any friend will be popped in x hits.")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiSelfPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Self Pop")
        .description("Cancels any action if you will be popped in x hits.")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    //  Rotation Page
    private final Setting<Boolean> rotate = sgRotate.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Very simple.")
        .defaultValue(true)
        .build()
    );

    //  ID-Predict Page
    private final Setting<Boolean> idPredict = sgID.add(new BoolSetting.Builder()
        .name("ID Predict")
        .description("yes.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> idOffset = sgID.add(new IntSetting.Builder()
        .name("Id Offset")
        .description(".")
        .defaultValue(0)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> idPackets = sgID.add(new IntSetting.Builder()
        .name("Id Packets")
        .description(".")
        .defaultValue(1)
        .min(1)
        .sliderMax(10)
        .build()
    );

    //  Extrapolation Page
    private final Setting<Boolean> enemyExt = sgExtrapolation.add(new BoolSetting.Builder()
        .name("Enemy Extrapolation")
        .description("Predicts enemy motion.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> selfExt = sgExtrapolation.add(new BoolSetting.Builder()
        .name("Self Extrapolation")
        .description("Predicts own motion.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> friendExt = sgExtrapolation.add(new BoolSetting.Builder()
        .name("Friend Extrapolation")
        .description("Predicts friend motion.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> extrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("How many ticks of movement should be predicted.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> extSmoothness = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation Smoothening")
        .description("How many earlier ticks should be used in average calculation for extrapolation motion.")
        .defaultValue(5)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    //  Render Page
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders stuff when placing crystals.")
        .defaultValue(true)
        .build()
    );
    private final Setting<RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<RenderMode>()
        .name("Render Mode")
        .description(".")
        .defaultValue(RenderMode.BlackOut)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.Earthhack) || renderMode.get().equals(RenderMode.Future))
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.Earthhack) || renderMode.get().equals(RenderMode.Future))
        .build()
    );
    private final Setting<Double> animationSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Speed")
        .description("How fast should blackout mode box move.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.BlackOut))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Line color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    //  Debug Page
    private final Setting<Boolean> renderExt = sgDebug.add(new BoolSetting.Builder()
        .name("Render Extrapolation")
        .description("Renders box at players' predicted positions.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> renderSelfExt = sgDebug.add(new BoolSetting.Builder()
        .name("Render Self Extrapolation")
        .description("Renders box at your predicted position.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> debugRange = sgDebug.add(new BoolSetting.Builder()
        .name("Debug Range")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> debugRangeX = sgDebug.add(new DoubleSetting.Builder()
        .name("Range Pos X")
        .description(".")
        .defaultValue(0)
        .sliderRange(-1000, 1000)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeY = sgDebug.add(new DoubleSetting.Builder()
        .name("Range Pos Y")
        .description(".")
        .defaultValue(0)
        .sliderRange(-1000, 1000)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeZ = sgDebug.add(new DoubleSetting.Builder()
        .name("Range Pos Z")
        .description(".")
        .defaultValue(0)
        .sliderRange(-1000, 1000)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight1 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 1")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight2 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 2")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight3 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 3")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight4 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 4")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight5 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 5")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
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
    IntTimerList attacked = new IntTimerList(false);
    Map<String, Box> extPos = new HashMap<>();
    List<PlayerEntity> enemies = new ArrayList<>();
    Map<String, List<Vec3d>> motions = new HashMap<>();
    List<Box> blocked = new ArrayList<>();
    Map<Vec3d, double[][]> dmgCache = new HashMap<>();
    BlockPos lastPos = null;
    Map<BlockPos, Double[]> earthMap = new HashMap<>();
    double switchTimer = 0;
    int confirmed = Integer.MIN_VALUE;

    //BlackOut Render
    Vec3d renderPos = null;
    double renderProgress = 0;


    public enum DmgCheckMode {
        Normal,
        Safe
    }
    public enum RenderMode {
        BlackOut,
        Future,
        Earthhack
    }
    public enum SwitchMode {
        Disabled,
        Simple,
        Smart,
        Gapple
    }

    @Override
    public void onActivate() {
        super.onActivate();
        earthMap.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onTickPre(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            for (PlayerEntity pl : mc.world.getPlayers()) {
                String key = pl.getName().getString();
                if (motions.containsKey(key)) {
                    List<Vec3d> vec = motions.get(key);
                    if (vec != null) {
                        if (vec.size() >= extSmoothness.get()) {
                            int p = vec.size() - extSmoothness.get();
                            for (int i = 0; i < p; i++) {
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
            extPos = getExtPos();
            if (debugRange.get()) {
                ChatUtils.sendMsg(Text.of(debugRangeHeight(1) + "  " + dist(debugRangePos(1)) + "\n" +
                    debugRangeHeight(2) + "  " + dist(debugRangePos(2)) + "\n" +
                    debugRangeHeight(3) + "  " + dist(debugRangePos(3)) + "\n" +
                    debugRangeHeight(4) + "  " + dist(debugRangePos(4)) + "\n" +
                    debugRangeHeight(5) + "  " + dist(debugRangePos(5))));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender3D(Render3DEvent event) {
        double d = event.frameTime;
        attacked.update(d);
        placeTimer = Math.max(placeTimer - d * getSpeed(), 0);
        delayTimer += d;
        switchTimer = Math.max(0, switchTimer - d);
        update();

        //Rendering
        if (render.get()) {
            switch (renderMode.get()) {
                case BlackOut -> {
                    if (placePos != null && !pausedCheck()) {
                        renderProgress = Math.min(1, renderProgress + d);
                        renderPos = smoothMove(renderPos, new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()), event.frameTime * animationSpeed.get());
                    } else {
                        renderProgress = Math.max(0, renderProgress - d);
                    }
                    if (renderPos != null) {
                        Box box = new Box(renderPos.getX() + 0.5 - renderProgress / 2, renderPos.getY() - 0.5 - renderProgress / 2, renderPos.getZ() + 0.5 - renderProgress / 2,
                            renderPos.getX() + 0.5 + renderProgress / 2, renderPos.getY() - 0.5 + renderProgress / 2, renderPos.getZ() + 0.5 + renderProgress / 2);
                        event.renderer.box(box, new Color(color.get().r, color.get().g, color.get().b, Math.round(color.get().a / 5f)), color.get(), shapeMode.get(), 0);
                    }
                }
                case Future -> {
                    if (placePos != null && !pausedCheck()) {
                        renderPos = new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ());
                        renderProgress = fadeTime.get() + renderTime.get();
                    } else {
                        renderProgress = Math.max(0, renderProgress - d);
                    }
                    if (renderProgress > 0 && renderPos != null) {
                        event.renderer.box(new Box(renderPos.getX(), renderPos.getY() - 1, renderPos.getZ(),
                                renderPos.getX() + 1, renderPos.getY(), renderPos.getZ() + 1),
                            new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, renderProgress / fadeTime.get()))),
                            new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, renderProgress / fadeTime.get()))), shapeMode.get(), 0);
                    }
                }
                case Earthhack -> {
                    List<BlockPos> toRemove = new ArrayList<>();
                    for (Map.Entry<BlockPos, Double[]> entry : earthMap.entrySet()) {
                        BlockPos pos = entry.getKey();
                        Double[] alpha = entry.getValue();
                        if (alpha[0] <= d) {
                            toRemove.add(pos);
                        } else {
                            event.renderer.box(OLEPOSSUtils.getBox(pos),
                                new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, alpha[0] / alpha[1]))),
                                new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, alpha[0] / alpha[1]))), shapeMode.get(), 0);
                            entry.setValue(new Double[]{alpha[0] - d, alpha[1]});
                        }
                    }
                    toRemove.forEach(earthMap::remove);
                }
            }
        }

        if (mc.player != null) {
            //Render extrapolation
            if (renderExt.get()) {
                extPos.forEach((name, bb) -> {
                    if (!renderSelfExt.get() || !name.equals(mc.player.getName().getString()))
                        event.renderer.box(bb, color.get(), lineColor.get(), shapeMode.get(), 0);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntity(EntityAddedEvent event) {
        if (event.entity instanceof EndCrystalEntity entity) {
            confirmed = entity.getId();
            if (instantExp.get()) {
                Vec3d vec = entity.getPos();
                if (canExplode(vec, false) && switchTimer <= 0) {
                    explode(entity.getId(), null, vec);
                    if (instant.get() && placePos != null && placePos.equals(entity.getBlockPos())) {
                        Hand hand = getHand(Items.END_CRYSTAL);
                        if (hand != null && !pausedCheck()) {
                            placeCrystal(placePos.down(), hand);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ExplosionS2CPacket) {
            if (clearReceive.get()) {blocked.clear();}
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSend(PacketEvent.Send event) {
        if (mc.player != null && mc.world != null) {
            if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
                switchTimer = afterSwitchDelay.get();
            }
        }
    }

    // Other stuff

    void update() {
        dmgCache.clear();
        Entity expEntity = null;
        double[] value = null;
        Hand hand = getHand(Items.END_CRYSTAL);
        if (!pausedCheck() && hand != null && explode.get()) {
            for (Entity en : mc.world.getEntities()) {
                if (en.getType().equals(EntityType.END_CRYSTAL)) {
                    double[] dmg = getDmg(en.getPos())[0];
                    if (switchTimer <= 0 && canExplode(en.getPos(), false)) {
                        if ((expEntity == null || value == null) ||
                            (dmgCheckMode.get().equals(DmgCheckMode.Normal) && dmg[0] > value[0]) ||
                            (dmgCheckMode.get().equals(DmgCheckMode.Safe) && dmg[2] / dmg[0] < value[2] / dmg[0])) {
                            expEntity = en;
                            value = dmg;
                        }
                    }
                }
            }
        }
        if (expEntity != null) {
            if (!isAttacked(expEntity.getId())) {
                explode(expEntity.getId(), expEntity, expEntity.getPos());
            }
        }
        Hand handToUse = hand;
        placePos = place.get() ? getPlacePos((int) Math.ceil(SettingUtils.getPlaceRange())) : null;
        switch (switchMode.get()) {
            case Simple -> {
                int slot = InvUtils.findInHotbar(itemStack -> itemStack.getItem().equals(Items.END_CRYSTAL)).slot();
                if (placePos != null && hand == null && slot >= 0) {
                    InvUtils.swap(slot, false);
                    handToUse = Hand.MAIN_HAND;
                }
            }
            case Smart -> {
                int gapSlot = InvUtils.findInHotbar(itemStack -> itemStack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)).slot();
                if (shouldGap(gapSlot)) {
                    if (getHand(Items.ENCHANTED_GOLDEN_APPLE) == null) {
                        InvUtils.swap(gapSlot, false);
                    }
                    handToUse = null;
                } else if (!expFriendly.get() || !mc.player.isUsingItem()) {
                    int slot = InvUtils.findInHotbar(itemStack -> itemStack.getItem().equals(Items.END_CRYSTAL)).slot();
                    if (placePos != null && hand == null && slot >= 0) {
                        InvUtils.swap(slot, false);
                        handToUse = Hand.MAIN_HAND;
                    }
                }
            }
            case Gapple -> {
                int gapSlot = InvUtils.findInHotbar(itemStack -> itemStack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)).slot();
                if (mc.options.useKey.isPressed() && Managers.HOLDING.isHolding(Items.END_CRYSTAL, Items.ENCHANTED_GOLDEN_APPLE) && gapSlot >= 0) {
                    if (getHand(Items.ENCHANTED_GOLDEN_APPLE) == null) {
                        InvUtils.swap(gapSlot, false);
                    }
                    handToUse = null;
                } else if (Managers.HOLDING.isHolding(Items.END_CRYSTAL, Items.ENCHANTED_GOLDEN_APPLE)) {
                    int slot = InvUtils.findInHotbar(itemStack -> itemStack.getItem().equals(Items.END_CRYSTAL)).slot();
                    if (placePos != null && hand == null && slot >= 0) {
                        InvUtils.swap(slot, false);
                        handToUse = Hand.MAIN_HAND;
                    }
                }
            }
        }

        if (placePos != null) {
            if (handToUse != null) {
                if (!placePos.equals(lastPos)) {
                    lastPos = placePos;
                    placeTimer = 0;
                }
                if (!pausedCheck()) {
                    if (rotate.get()) {
                        Managers.ROTATION.start(OLEPOSSUtils.getBox(placePos.down()));
                    }
                    if ((placeTimer <= 0 || (instantPlace.get() && !shouldSlow() && !isBlocked(placePos))) && delayTimer >= placeDelay.get()) {
                        placeTimer = 1;
                        placeCrystal(placePos.down(), handToUse);
                    }
                } else {
                    if (rotate.get()) {
                        Managers.ROTATION.end(OLEPOSSUtils.getBox(placePos.down()));
                    }
                }
            }
        } else {
            lastPos = null;
        }
    }

    boolean shouldGap(int slot) {
        if (slot < 0) {return false;}
        if (!mc.options.useKey.isPressed()) {return false;}
        if (placePos == null && !alwaysGap.get()) {return false;}
        if (!Managers.HOLDING.isHolding(Items.END_CRYSTAL) && !Managers.HOLDING.isHolding(Items.ENCHANTED_GOLDEN_APPLE) && onlyCrystal.get()) {return false;}
        return true;
    }

    void placeCrystal(BlockPos pos, Hand handToUse) {
        if (handToUse != null && pos != null && mc.player != null) {
            if (renderMode.get().equals(RenderMode.Earthhack)) {
                if (!earthMap.containsKey(pos)) {
                    earthMap.put(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
                } else {
                    earthMap.replace(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
                }
            }

            blocked.add(new Box(pos.getX() - 0.5, pos.getY() + 1, pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5));
            SettingUtils.swing(SwingSettings.SwingState.Pre, SwingSettings.SwingType.AutoCrystalPlace);
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(handToUse,
                new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, false), 0));
            SettingUtils.swing(SwingSettings.SwingState.Post, SwingSettings.SwingType.AutoCrystalPlace);

            if (idPredict.get()) {
                int id = getHighest() + idOffset.get();
                for (int i = 0; i < idPackets.get(); i++) {
                    Entity en = mc.world.getEntityById(id + i);
                    if (en == null || (en instanceof EndCrystalEntity)) {
                        explode(id + i, null, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5));
                    }
                }
                confirmed++;
            }
        }
    }

    int getHighest() {
        int highest = confirmed;
        for (Entity entity : mc.world.getEntities()) {
            if (entity.getId() > highest) {
                highest = entity.getId();
            }
        }
        return highest;
    }

    boolean isBlocked(BlockPos pos) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        for (Box bb : blocked) {
            if (bb.intersects(box)) {
                return true;
            }
        }
        return false;
    }

    boolean isAttacked(int id) {
        return attacked.contains(id);
    }

    void explode(int id, Entity en, Vec3d vec) {
        if (en != null) {
            attackEntity(en);
        } else {
            attackID(id, vec);
        }
    }

    void attackID(int id, Vec3d pos) {
        Hand handToUse = getHand(Items.END_CRYSTAL);
        if (handToUse != null && !pausedCheck()) {
            EndCrystalEntity en = new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z);
            en.setId(id);
            attackEntity(en);
        }
    }

    void attackEntity(Entity en) {
        if (mc.player != null) {
            Hand handToUse = getHand(Items.END_CRYSTAL);
            if (handToUse != null) {
                attacked.add(en.getId(), 1 / expSpeed.get());
                delayTimer = 0;

                SettingUtils.swing(SwingSettings.SwingState.Pre, SwingSettings.SwingType.AutoCrystalExplode);
                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));
                SettingUtils.swing(SwingSettings.SwingState.Post, SwingSettings.SwingType.AutoCrystalExplode);
                if (clearSend.get()) {blocked.clear();}
                if (setDead.get()) {
                    Managers.DELAY.add(() -> setEntityDead(en), setDeadDelay.get());
                }
            }
        }
    }

    boolean canExplode(Vec3d vec, boolean onlyCache) {
        if (!inExplodeRange(vec)) {return false;}
        if (!dmgCache.containsKey(vec) && onlyCache) {return true;}
        double[][] result = onlyCache ? dmgCache.get(vec) : getDmg(vec);
        return explodeDamageCheck(result[0], result[1]);
    }

    Hand getHand(Item item) {
        return Managers.HOLDING.isHolding(item) ? Hand.MAIN_HAND:
            mc.player.getOffHandStack().getItem().equals(item) ? Hand.OFF_HAND : null;
    }

    boolean pausedCheck() {
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
                    if (!air(pos) || !(!oldVerPlacements.get() || air(pos.up())) || !inPlaceRange(pos.down()) ||
                        !crystalBlock(pos.down()) || !inExplodeRange(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) {continue;}

                    // Calculates damages and healths
                    double[][] result = getDmg(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));

                    // Checks if damages are valid
                    if (!placeDamageCheck(result[0], result[1], highest)) {continue;}

                    // Checks if placement is blocked by other entities
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + (ccPlacements.get() ? 1 : 2), pos.getZ() + 1);
                    if (EntityUtils.intersectsWithEntity(box, entity -> !(entity.getType().equals(EntityType.END_CRYSTAL) && canExplode(entity.getPos(), true)))) {continue;}
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
            if (dmgCheckMode.get().equals(DmgCheckMode.Normal) && dmg[0] < highest[0]) {return false;}
            if (dmgCheckMode.get().equals(DmgCheckMode.Safe) && dmg[2] / dmg[0] > highest[2] / highest[0]) {return false;}
        }

        //  Force/anti-pop check
        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (playerHP >= 0 && dmg[2] * antiSelfPop.get() >= playerHP) {return false;}
        if (health[1] >= 0 && dmg[1] * antiFriendPop.get() >= health[1]) {return false;}
        if (health[0] >= 0 && dmg[0] * forcePop.get() >= health[0]) {return true;}

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
        if (playerHP >= 0 && dmg[2] * forcePop.get() >= playerHP) {return false;}
        if (health[1] >= 0 && dmg[1] * antiFriendPop.get() >= health[1]) {return false;}
        if (health[0] >= 0 && dmg[0] * forcePop.get() >= health[0]) {return true;}

        //  Min Damage
        if (dmg[0] < minExplode.get()) {return false;}

        //  Max Damage
        if (dmg[1] > maxFriendExp.get()) {return false;}
        if (dmg[1] / dmg[0] > maxFriendExpRatio.get()) {return false;}
        if (dmg[2] > maxSelfExp.get()) {return false;}
        if (dmg[2] / dmg[0] > maxSelfExpRatio.get()) {return false;}
        return true;
    }

    double[][] getDmg(Vec3d vec) {
        if (dmgCache.containsKey(vec)) {
            return dmgCache.get(vec);
        }
        highestEnemy = -1;
        highestFriend = -1;
        self = -1;
        enemyHP = -1;
        friendHP = -1;
        List<AbstractClientPlayerEntity> players = mc.world.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            AbstractClientPlayerEntity player = players.get(i);
            if (player.getHealth() <= 0 || player.isSpectator() || player == mc.player) {
                continue;
            }

            String key = player.getName().getString();
            double dmg = BODamageUtils.crystalDamage(player, extPos.containsKey(key) && shouldExt(player) ? extPos.get(key) : player.getBoundingBox(),vec);
            double hp = player.getHealth() + player.getAbsorptionAmount();

            //  friend
            if (Friends.get().isFriend(player)) {
                if (dmg > highestFriend) {
                    highestFriend = dmg;
                    friendHP = hp;
                }
            }
            //  enemy
            else if (dmg > highestEnemy) {
                highestEnemy = dmg;
                enemyHP = hp;
            }
        }
        self = BODamageUtils.crystalDamage(mc.player, selfExt.get() && extPos.containsKey(mc.player.getName().getString()) ? extPos.get(mc.player.getName().getString()) : mc.player.getBoundingBox(),vec);
        double[][] result = new double[][]{new double[] {highestEnemy, highestFriend, self}, new double[]{enemyHP, friendHP}};
        dmgCache.put(vec, result);
        return result;
    }

    boolean shouldExt(PlayerEntity pl) {return (enemyExt.get() && !Friends.get().isFriend(pl)) || (friendExt.get() && Friends.get().isFriend(pl));}

    boolean air(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);
    }
    boolean crystalBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) ||
            mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK);
    }
    boolean inPlaceRange(BlockPos pos) {
        return SettingUtils.inPlaceRange(pos);
    }
    boolean inExplodeRange(Vec3d vec) {
        return SettingUtils.inAttackRange(new Box(vec.getX() - 1, vec.getY(), vec.getZ() - 1, vec.getX() + 1, vec.getY() + 2, vec.getZ() + 1), 1.75, vec);
    }
    double dist(Vec3d distances) {
        return Math.sqrt(distances.x * distances.x + distances.y * distances.y + distances.z * distances.z);
    }
    Vec3d debugRangePos(int id) {
        return mc.player.getEyePos().add(-debugRangeX.get() - 0.5, -debugRangeY.get() - debugRangeHeight(id), -debugRangeZ.get() - 0.5);
    }
    double debugRangeHeight(int id) {return id == 1 ? debugRangeHeight1.get() : id == 2 ? debugRangeHeight2.get() : id == 3 ? debugRangeHeight3.get() : id == 4 ? debugRangeHeight4.get() : debugRangeHeight5.get();}
    double getSpeed() {
        return shouldSlow() ? slowSpeed.get() : placeSpeed.get();
    }
    boolean shouldSlow() {return placePos != null && getDmg(new Vec3d(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5))[0][0] <= slowDamage.get();}
    Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
        if (current == null) {return target;}
        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);

        double x = absX * delta;
        double y = absY * delta;
        double z = absZ * delta;
        return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x),
            current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y),
            current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
    }

    Vec3d motionCalc(PlayerEntity en) {
        return new Vec3d(en.getX() - en.prevX, en.getY() - en.prevY, en.getZ() - en.prevZ);
    }

    Map<String, Box> getExtPos() {
        Map<String, Box> map = new HashMap<>();
        enemies = new ArrayList<>();
        if (!mc.world.getPlayers().isEmpty()) {
            for (int p = 0; p < mc.world.getPlayers().size(); p++) {
                PlayerEntity en = mc.world.getPlayers().get(p);
                if (en.getHealth() <= 0) {continue;}
                if (!motions.isEmpty()) {
                    Vec3d motion = average(motions.get(en.getName().getString()));
                    double x = motion.x;
                    double y = motion.y;
                    double z = motion.z;
                    Box box = en.getBoundingBox();
                    if (!inside(en, box)) {
                        for (int i = 0; i < extrapolation.get(); i++) {

                            //x
                            if (!inside(en, box.offset(x, 0, 0))) {
                                box = box.offset(x, 0, 0);
                            }

                            //z
                            if (!inside(en, box.offset(0, 0, z))) {
                                box = box.offset(0, 0, z);
                            }

                            if (!inside(en, box.offset(0, -0.05, 0))) {
                                y -= 0.08;
                                if (!inside(en, box.offset(0, y, 0))) {
                                    box = box.offset(0, y, 0);
                                }
                            } else {
                                y = 0;
                            }
                        }
                    }
                    map.put(en.getName().getString(), box);
                }
            }
        }
        return map;
    }

    Vec3d average(List<Vec3d> vec) {
        Vec3d total = new Vec3d(0, 0, 0);
        if (vec != null) {
            if (!vec.isEmpty()) {
                for (Vec3d vec3d : vec) {
                    total = total.add(vec3d);
                }
                return new Vec3d(total.x / vec.size(), vec.get(vec.size() - 1).y, total.z / vec.size());
            }
        }
        return total;
    }

    boolean inside(PlayerEntity en, Box bb) {
        return mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }
}
