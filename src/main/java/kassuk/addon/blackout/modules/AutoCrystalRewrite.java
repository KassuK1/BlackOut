package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.mixins.MixinSound;
import kassuk.addon.blackout.timers.IntTimerList;
import kassuk.addon.blackout.utils.meteor.BODamageUtils;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import kassuk.addon.blackout.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundEvents;
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
import java.util.concurrent.atomic.AtomicBoolean;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoCrystalRewrite extends BlackOutModule {
    public AutoCrystalRewrite() {super(BlackOut.BLACKOUT, "Auto Crystal Rewrite", "Breaks and places crystals automatically (but better).");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgExplode = settings.createGroup("Explode");
    private final SettingGroup sgSwitch = settings.createGroup("Switch");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgID = settings.createGroup("ID-Predict");
    private final SettingGroup sgExtrapolation = settings.createGroup("Extrapolation");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgSound = settings.createGroup("Sound");
    private final SettingGroup sgCompatibility = settings.createGroup("Compatibility");
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
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> performance = sgGeneral.add(new BoolSetting.Builder()
        .name("Performance Mode")
        .description("Doesn't calculate placements as often.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> smartRot = sgGeneral.add(new BoolSetting.Builder()
        .name("Smart Rotations")
        .description("Looks at the top of placement block to make the ca faster.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> ignoreTerrain = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Terrain")
        .description("Spams trough terrain to kill your enemy.")
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
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> instantPlace = sgPlace.add(new BoolSetting.Builder()
        .name("Instant Place")
        .description("Ignores delay after crystal hitbox has disappeared.")
        .defaultValue(true)
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
    private final Setting<DelayMode> placeDelayMode = sgPlace.add(new EnumSetting.Builder<DelayMode>()
        .name("Place Delay Mode")
        .description(".")
        .defaultValue(DelayMode.Seconds)
        .build()
    );
    private final Setting<Double> placeDelay = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("How many seconds after attacking a crystal should we place.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 1)
        .visible(() -> placeDelayMode.get() == DelayMode.Seconds)
        .build()
    );
    private final Setting<SequentialMode> sequentialMode = sgPlace.add(new EnumSetting.Builder<SequentialMode>()
        .name("Sequential Mode")
        .description("How strict should sequential delay be.")
        .defaultValue(SequentialMode.Weak)
        .visible(() -> placeDelayMode.get() == DelayMode.Sequential)
        .build()
    );
    private final Setting<Double> slowDamage = sgPlace.add(new DoubleSetting.Builder()
        .name("Slow Damage")
        .description("Switches to slow speed when the target would take under this amount of damage.")
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

    //  Explode Page
    private final Setting<Boolean> onlyOwn = sgExplode.add(new BoolSetting.Builder()
        .name("Only Own")
        .description("Only attacks own crystals.")
        .defaultValue(false)
        .build()
    );
    private final Setting<ExistedMode> existedMode = sgPlace.add(new EnumSetting.Builder<ExistedMode>()
        .name("Existed Mode")
        .description(".")
        .defaultValue(ExistedMode.Seconds)
        .build()
    );
    private final Setting<Double> existed = sgExplode.add(new DoubleSetting.Builder()
        .name("Existed")
        .description("How many seconds should the crystal exist before attacking.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 1)
        .visible(() -> existedMode.get() == ExistedMode.Seconds)
        .build()
    );
    private final Setting<Integer> existedTicks = sgExplode.add(new IntSetting.Builder()
        .name("Existed Ticks")
        .description("How many ticks should the crystal exist before attacking.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> existedMode.get() == ExistedMode.Ticks)
        .build()
    );
    private final Setting<Double> expSpeed = sgExplode.add(new DoubleSetting.Builder()
        .name("Explode Speed")
        .description("How many times to hit crystal each second.")
        .defaultValue(4)
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
    private final Setting<Double> switchPenalty = sgSwitch.add(new DoubleSetting.Builder()
        .name("Switch Penalty")
        .description("Time to wait after switching before hitting crystals.")
        .defaultValue(0.25)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    //  Damage Page
    private final Setting<DmgCheckMode> dmgCheckMode = sgDamage.add(new EnumSetting.Builder<DmgCheckMode>()
        .name("Dmg Check Mode")
        .description("How safe are the placements (normal is good).")
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
        .description("Max friend damage ratio for placing (friend damage / enemy damage).")
        .defaultValue(0.5)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<ExplodeMode> expMode = sgDamage.add(new EnumSetting.Builder<ExplodeMode>()
        .name("Explode Damage Mode")
        .description("Which things should be checked for exploding.")
        .defaultValue(ExplodeMode.FullCheck)
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
        .defaultValue(1)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiFriendPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Friend Pop")
        .description("Cancels any action if any friend will be popped in x hits.")
        .defaultValue(1)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiSelfPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Self Pop")
        .description("Cancels any action if you will be popped in x hits.")
        .defaultValue(1)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    //  ID-Predict Page
    private final Setting<Boolean> idPredict = sgID.add(new BoolSetting.Builder()
        .name("ID Predict")
        .description("Hits the crystal before it spawns.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> idOffset = sgID.add(new IntSetting.Builder()
        .name("Id Offset")
        .description("How many id's ahead should we attack.")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> idPackets = sgID.add(new IntSetting.Builder()
        .name("Id Packets")
        .description("How many packets to send.")
        .defaultValue(1)
        .min(1)
        .sliderMax(10)
        .build()
    );

    //  Extrapolation Page
    private final Setting<Integer> selfExt = sgExtrapolation.add(new IntSetting.Builder()
        .name("Self DMG Extrapolation")
        .description("How many ticks of movement should be predicted, should be lower than normal.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> extrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("DMG Extrapolation")
        .description("How many ticks of movement should be predicted.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> rangeExtrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Range Extrapolation")
        .description("How many ticks of movement should be predicted for attack ranges before placing.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> hitboxExtrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Hitbox Extrapolation")
        .description("How many ticks of movement should be predicted for hitboxes in placing checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> extSmoothness = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation Smoothening")
        .description("How many earlier ticks should be used in average calculation for extrapolation motion.")
        .defaultValue(2)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    //  Render Page
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders stuff when placing on placements.")
        .defaultValue(true)
        .build()
    );
    private final Setting<RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<RenderMode>()
        .name("Render Mode")
        .description("What should the render look like.")
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
    private final Setting<FadeMode> fadeMode = sgRender.add(new EnumSetting.Builder<FadeMode>()
        .name("Fade Mode")
        .description(".")
        .defaultValue(FadeMode.Normal)
        .visible(() -> renderMode.get() == RenderMode.BlackOut)
        .build()
    );
    private final Setting<EarthFadeMode> earthFadeMode = sgRender.add(new EnumSetting.Builder<EarthFadeMode>()
        .name("Earth Fade Mode")
        .description(".")
        .defaultValue(EarthFadeMode.Normal)
        .visible(() -> renderMode.get() == RenderMode.Earthhack)
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
        .name("Animation Move Speed")
        .description("How fast should blackout mode box move.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.BlackOut))
        .build()
    );
    private final Setting<Double> animationMoveExponent = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Move Exponent")
        .description("Moves faster when longer away from the target.")
        .defaultValue(1.3)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.BlackOut))
        .build()
    );
    private final Setting<Double> animationExponent = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Exponent")
        .description("How fast should blackout mode box grow.")
        .defaultValue(3)
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

    //  Sound Page
    private final Setting<Boolean> explodeSound = sgSound.add(new BoolSetting.Builder()
        .name("Explode Sound")
        .description("Sets crystal explode sound to high pitch anvil mining.")
        .defaultValue(false)
        .build()
    );

    //  Compatibility Page
    private final Setting<Boolean> surroundAttack = sgSound.add(new BoolSetting.Builder()
        .name("Surround Attack")
        .description("Attacks any crystal if surround placement is blocked.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> autoMineDamage = sgCompatibility.add(new DoubleSetting.Builder()
        .name("Auto Mine Damage")
        .description("Places at automine pos if it deals over 'highest dmg / x' damage.")
        .defaultValue(2)
        .sliderRange(1, 5)
        .min(1)
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

    long ticksEnabled = 0;
    double highestEnemy = 0;
    double enemyHP = 0;
    double highestFriend = 0;
    double friendHP = 0;
    double self = 0;
    double placeTimer = 0;
    double delayTimer = 0;
    int delayTicks = 0;

    BlockPos placePos = null;
    Direction placeDir = null;
    IntTimerList attacked = new IntTimerList(false);
    Map<BlockPos, Long> existedList = new HashMap<>();
    Map<BlockPos, Long> existedTicksList = new HashMap<>();
    Map<BlockPos, Long> own = new HashMap<>();
    Map<PlayerEntity, Box> extPos = new HashMap<>();
    Map<PlayerEntity, Box> extHitbox = new HashMap<>();
    Vec3d rangePos = null;
    Map<String, List<Vec3d>> motions = new HashMap<>();
    List<Box> blocked = new ArrayList<>();
    Map<Vec3d, double[][]> dmgCache = new HashMap<>();
    BlockPos lastPos = null;
    Map<BlockPos, Double[]> earthMap = new HashMap<>();
    double switchTimer = 0;
    int confirmed = Integer.MIN_VALUE;
    double infoCps = 0;
    double cps = 0;
    long cpsTime = System.currentTimeMillis();
    int explosions = 0;
    long lastMillis = System.currentTimeMillis();
    boolean suicide = false;
    public static boolean placing = false;

    //Used in placement calculation
    BlockPos bestPos;
    Direction bestDir;
    double[] highest;
    int r;
    BlockPos pos;
    Direction dir;
    double[][] result;

    //BlackOut Render
    Vec3d renderTarget = null;
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
        Gapple,
        Silent,
        SilentBypass
    }
    public enum DelayMode {
        Seconds,
        Sequential
    }
    public enum SequentialMode {
        Weak,
        Strong,
        Strict
    }
    public enum ExplodeMode {
        FullCheck,
        SelfDmgCheck,
        SelfDmgOwn,
        AlwaysOwn,
        Always
    }
    public enum ExistedMode {
        Seconds,
        Ticks
    }
    public enum EarthFadeMode {
        Normal,
        Up,
        Down,
        Shrink
    }
    public enum FadeMode {
        Up,
        Down,
        Normal
    }

    @Override
    public void onActivate() {
        super.onActivate();
        ticksEnabled = 0;

        earthMap.clear();
        existedTicksList.clear();
        existedList.clear();
        blocked.clear();
        motions.clear();
        extPos.clear();
        own.clear();
        dmgCache.clear();
        renderPos = null;
        renderProgress = 0;
        lastMillis = System.currentTimeMillis();
        cps = 0;
        infoCps = 0;
        cpsTime = System.currentTimeMillis();
    }

    @Override
    public String getInfoString() {
        super.getInfoString();

        return ((float) Math.round(infoCps * 10) / 10) + " CPS";
    }


    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onTickPre(TickEvent.Pre event) {
        delayTicks++;
        ticksEnabled++;
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
            rangePos = getRangeExt();
            extHitbox = getHitboxExt();
            if (debugRange.get()) {
                debug(debugRangeHeight(1) + "  " + dist(debugRangePos(1)) + "\n" +
                    debugRangeHeight(2) + "  " + dist(debugRangePos(2)) + "\n" +
                    debugRangeHeight(3) + "  " + dist(debugRangePos(3)) + "\n" +
                    debugRangeHeight(4) + "  " + dist(debugRangePos(4)) + "\n" +
                    debugRangeHeight(5) + "  " + dist(debugRangePos(5)) + "\n" +
                    SettingUtils.attackRangeTo(OLEPOSSUtils.getCrystalBox(new BlockPos(debugRangeX.get(), debugRangeY.get(), debugRangeZ.get())), new Vec3d(debugRangeX.get() + 0.5, debugRangeY.get(), debugRangeZ.get() + 0.5)));
            }
        }

        List<BlockPos> toRemove = new ArrayList<>();
        existedList.forEach((key, val) -> {
            if (System.currentTimeMillis() - val >= 5000 + existed.get() * 1000) {
                toRemove.add(key);
            }
        });
        toRemove.forEach(existedList::remove);

        toRemove.clear();
        existedTicksList.forEach((key, val) -> {
            if (ticksEnabled - val >= 100 + existedTicks.get()) {
                toRemove.add(key);
            }
        });
        toRemove.forEach(existedTicksList::remove);

        toRemove.clear();

        own.forEach((key, val) -> {
            if (System.currentTimeMillis() - val >= 5000) {
                toRemove.add(key);
            }
        });
        toRemove.forEach(own::remove);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTickPost(TickEvent.Post event) {
        if (performance.get()) {
            updatePlacement();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender3D(Render3DEvent event) {
        suicide = Modules.get().isActive(Suicide.class);
        double d = (System.currentTimeMillis() - lastMillis) / 1000f;
        lastMillis = System.currentTimeMillis();

        infoCps = smoothChange(cps, infoCps, d * 6);

        if (System.currentTimeMillis() - cpsTime >= 1000) {
            cps = explosions * (System.currentTimeMillis() - cpsTime) / 1000f;
            cpsTime = System.currentTimeMillis();
            explosions = 0;
        }

        attacked.update(d);
        placeTimer = Math.max(placeTimer - d * getSpeed(), 0);
        delayTimer += d;
        switchTimer = Math.max(0, switchTimer - d);
        update();

        //Rendering
        if (render.get()) {
            switch (renderMode.get()) {
                case BlackOut -> {
                    if (placePos != null && !pausedCheck() && holdingCheck()) {
                        renderProgress = Math.min(1, renderProgress + d);
                        renderTarget = new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ());
                    } else {
                        renderProgress = Math.max(0, renderProgress - d);
                    }

                    if (renderTarget != null) {
                        renderPos = smoothMove(renderPos, renderTarget, d * animationSpeed.get() * 5);
                    }

                    if (renderPos != null) {
                        double r = 0.5 - Math.pow(1 - renderProgress, animationExponent.get()) / 2f;

                        if (r >= 0.001) {
                            double down = -0.5;
                            double up = -0.5;
                            double width = 0.5;

                            switch (fadeMode.get()) {
                                case Up -> {
                                    up = 0;
                                    down = -(r * 2);
                                }
                                case Down -> {
                                    up = -1 + r * 2;
                                    down = -1;
                                }
                                case Normal -> {
                                    up = -0.5 + r;
                                    down = -0.5 - r;
                                    width = r;
                                }
                            }
                            Box box = new Box(renderPos.getX() + 0.5 - width, renderPos.getY() + down, renderPos.getZ() + 0.5 - width,
                                renderPos.getX() + 0.5 + width, renderPos.getY() + up, renderPos.getZ() + 0.5 + width);

                            event.renderer.box(box, new Color(color.get().r, color.get().g, color.get().b, Math.round(color.get().a)), lineColor.get(), shapeMode.get(), 0);
                        }
                    }
                }
                case Future -> {
                    if (placePos != null && !pausedCheck() && holdingCheck()) {
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
                            double r = Math.min(1, alpha[0] / alpha[1]) / 2f;
                            double down = -0.5;
                            double up = -0.5;
                            double width = 0.5;

                            switch (earthFadeMode.get()) {
                                case Normal -> {
                                    up = 1;
                                    down = 0;
                                    width = 0.5;
                                }
                                case Up -> {
                                    up = 1;
                                    down = 1 -(r * 2);
                                }
                                case Down -> {
                                    up = r * 2;
                                    down = 0;
                                }
                                case Shrink -> {
                                    up = 0.5 + r;
                                    down = 0.5 - r;
                                    width = r;
                                }
                            }

                            Box box = new Box(pos.getX() + 0.5 - width, pos.getY() + down, pos.getZ() + 0.5 - width,
                                pos.getX() + 0.5 + width, pos.getY() + up, pos.getZ() + 0.5 + width);

                            event.renderer.box(box,
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
                    if (renderSelfExt.get() || !name.equals(mc.player))
                        event.renderer.box(bb, color.get(), lineColor.get(), shapeMode.get(), 0);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntity(EntityAddedEvent event) {
        confirmed = event.entity.getId();
    }

    @EventHandler
    private void onSound(PlaySoundEvent event) {
        if (explodeSound.get()) {
            if (event.sound.getId() == SoundEvents.ENTITY_GENERIC_EXPLODE.getId()) {
                ((MixinSound) event.sound).setID(SoundEvents.BLOCK_ANVIL_FALL.getId());
                ((MixinSound) event.sound).setVolume(1);
                ((MixinSound) event.sound).setPitch(10);

            }
            if (event.sound.getId() == SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE.getId() ||
                event.sound.getId() == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK.getId()) {
                event.cancel();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSend(PacketEvent.Send event) {
        if (mc.player != null && mc.world != null) {
            if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
                switchTimer = switchPenalty.get();
            }
            if (event.packet instanceof PlayerInteractBlockC2SPacket packet && (packet.getHand() == Hand.MAIN_HAND ? Managers.HOLDING.isHolding(Items.END_CRYSTAL) : mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL)) {
                if (!isOwn(packet.getBlockHitResult().getBlockPos().up())) {
                    own.put(packet.getBlockHitResult().getBlockPos().up(), System.currentTimeMillis());
                } else {
                    own.remove(packet.getBlockHitResult().getBlockPos().up());
                    own.put(packet.getBlockHitResult().getBlockPos().up(), System.currentTimeMillis());
                }
                blocked.add(new Box(packet.getBlockHitResult().getBlockPos().getX() - 0.5, packet.getBlockHitResult().getBlockPos().getY() + 1,
                    packet.getBlockHitResult().getBlockPos().getZ() - 0.5, packet.getBlockHitResult().getBlockPos().getX() + 1.5,
                    packet.getBlockHitResult().getBlockPos().getY() + 2, packet.getBlockHitResult().getBlockPos().getZ() + 1.5));
                addExisted(packet.getBlockHitResult().getBlockPos().up());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ExplosionS2CPacket packet) {
            Vec3d pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());;
            if (isOwn(pos)) {
                explosions++;
            }
        }
    }

    // Other stuff

    void update() {
        placing = false;
        dmgCache.clear();
        Entity expEntity = null;
        double[] value = null;
        boolean shouldProtectSurround = surroundProt();
        Hand hand = getHand(Items.END_CRYSTAL);
        if (!pausedCheck() && (hand != null || switchMode.get() == SwitchMode.Silent || switchMode.get() == SwitchMode.SilentBypass) && explode.get()) {
            for (Entity en : mc.world.getEntities()) {
                if (en instanceof EndCrystalEntity) {
                    double[] dmg = getDmg(en.getPos())[0];
                    if (switchTimer <= 0 && canExplode(en.getPos(), shouldProtectSurround)) {
                        if ((expEntity == null || value == null) || ((dmgCheckMode.get().equals(DmgCheckMode.Normal) && dmg[0] > value[0]) || (dmgCheckMode.get().equals(DmgCheckMode.Safe) && dmg[2] / dmg[0] < value[2] / dmg[0]))) {
                            expEntity = en;
                            value = dmg;
                        }
                    }
                }
            }
        }
        if (expEntity != null) {
            if (!isAttacked(expEntity.getId())) {
                if (existedCheck(expEntity.getBlockPos())) {
                    if (!SettingUtils.shouldRotate(RotationType.Attacking) || (Managers.ROTATION.start(expEntity.getBoundingBox(), smartRot.get() ? expEntity.getPos() : null, 5.1, RotationType.Attacking) && ghostCheck(expEntity.getBoundingBox(), expEntity.getPos()))) {
                        explode(expEntity.getId(), expEntity, expEntity.getPos());
                    }
                }
            }
        }
        Hand handToUse = hand;
        if (!performance.get()) {
            updatePlacement();
        }
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
                    handToUse = getHand(Items.END_CRYSTAL);
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
                    handToUse = getHand(Items.END_CRYSTAL);
                } else if (Managers.HOLDING.isHolding(Items.END_CRYSTAL, Items.ENCHANTED_GOLDEN_APPLE)) {
                    int slot = InvUtils.findInHotbar(itemStack -> itemStack.getItem().equals(Items.END_CRYSTAL)).slot();
                    if (placePos != null && hand == null && slot >= 0) {
                        InvUtils.swap(slot, false);
                        handToUse = Hand.MAIN_HAND;
                    }
                }
            }
        }

        if (placePos != null && placeDir != null) {
            if (!placePos.equals(lastPos)) {
                lastPos = placePos;
                placeTimer = 0;
            }
            if (!pausedCheck()) {
                int silentSlot = InvUtils.find(Items.END_CRYSTAL).slot();
                int hotbar = InvUtils.findInHotbar(Items.END_CRYSTAL).slot();
                if (handToUse != null || (switchMode.get() == SwitchMode.Silent && hotbar >= 0) || (switchMode.get() == SwitchMode.SilentBypass && silentSlot >= 0)) {
                    placing = true;
                    if ((placeTimer <= 0 || (instantPlace.get() && !shouldSlow() && !isBlocked(placePos))) && delayCheck()) {
                        if (!SettingUtils.shouldRotate(RotationType.Crystal) || (Managers.ROTATION.start(placePos.down(), smartRot.get() ? new Vec3d(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5) : null, 5, RotationType.Crystal) && ghostCheck(placePos.down()))) {
                            placeTimer = 1;
                            placeCrystal(placePos.down(), placeDir, handToUse, silentSlot, hotbar);
                        }
                    }
                }
            }
        } else {
            lastPos = null;
        }
    }

    boolean surroundProt() {
        if (!surroundAttack.get() || !Modules.get().isActive(SurroundPlus.class)) {return false;}

        for (int i = 0; i < SurroundPlus.attack.size(); i++) {
            if (EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(SurroundPlus.attack.get(i)), entity -> entity instanceof EndCrystalEntity)) {
                return true;
            }
        }
        return false;
    }
    boolean ghostCheck(BlockPos pos) {
        if (!SettingUtils.shouldGhostCheck()) {return true;}
        return SettingUtils.placeRangeTo(pos) <= (SettingUtils.raytraceCheck(mc.player.getEyePos(), Managers.ROTATION.lastDir[0], Managers.ROTATION.lastDir[1], pos) ? SettingUtils.getPlaceRange() : SettingUtils.getPlaceWallsRange());
    }
    boolean ghostCheck(Box box, Vec3d feet) {
        if (!SettingUtils.shouldGhostCheck()) {return true;}
        return SettingUtils.attackRangeTo(box, feet) <= (SettingUtils.raytraceCheck(mc.player.getEyePos(), Managers.ROTATION.lastDir[0], Managers.ROTATION.lastDir[1], pos) ? SettingUtils.getPlaceRange() : SettingUtils.getPlaceWallsRange());
    }

    boolean holdingCheck() {
        switch (switchMode.get()) {
            case Silent -> {
                return InvUtils.findInHotbar(Items.END_CRYSTAL).slot() >= 0;
            }
            case SilentBypass -> {
                return InvUtils.find(Items.END_CRYSTAL).slot() >= 0;
            }
            default -> {
                return getHand(Items.END_CRYSTAL) != null;
            }
        }
    }

    void updatePlacement() {
        if (!place.get()) {
            placePos = null;
            placeDir = null;
            return;
        }
        placePos = getPlacePos();
    }

    boolean shouldGap(int slot) {
        if (slot < 0) {return false;}
        if (!mc.options.useKey.isPressed()) {return false;}
        if (placePos == null && !alwaysGap.get()) {return false;}
        if (!Managers.HOLDING.isHolding(Items.END_CRYSTAL) && !Managers.HOLDING.isHolding(Items.ENCHANTED_GOLDEN_APPLE) && onlyCrystal.get()) {return false;}
        return true;
    }

    void placeCrystal(BlockPos pos, Direction dir, Hand handToUse, int sl, int hsl) {
        if (pos != null && mc.player != null) {
            if (renderMode.get().equals(RenderMode.Earthhack)) {
                if (!earthMap.containsKey(pos)) {
                    earthMap.put(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
                } else {
                    earthMap.replace(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
                }
            }

            blocked.add(new Box(pos.getX() - 0.5, pos.getY() + 1, pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5));

            boolean switched = handToUse == null;
            if (switched) {
                switch (switchMode.get()) {
                    case SilentBypass -> {
                        BOInvUtils.invSwitch(sl);
                    }
                    case Silent -> {
                        InvUtils.swap(hsl, true);
                    }
                }
            }

            SettingUtils.swing(SwingState.Pre, SwingType.Crystal);

            addExisted(pos.up());

            if (!isOwn(pos.up())) {
                own.put(pos.up(), System.currentTimeMillis());
            } else {
                own.remove(pos.up());
                own.put(pos.up(), System.currentTimeMillis());
            }

            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(switched ? Hand.MAIN_HAND : handToUse,
                new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), dir, pos, false), 0));

            SettingUtils.swing(SwingState.Post, SwingType.Crystal);

            if (SettingUtils.shouldRotate(RotationType.Crystal)) {
                Managers.ROTATION.end(OLEPOSSUtils.getBox(pos));
            }

            if (switched) {
                switch (switchMode.get()) {
                    case SilentBypass -> {
                        BOInvUtils.swapBack();
                    }
                    case Silent -> {
                        InvUtils.swapBack();
                    }
                }
            }
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

    boolean delayCheck() {
        if (placeDelayMode.get() == DelayMode.Seconds) {
            return delayTimer >= placeDelay.get();
        } else {
            switch (sequentialMode.get()) {
                case Weak -> {
                    return delayTicks >= 1;
                }
                case Strong -> {
                    return delayTicks >= 2;
                }
                case Strict -> {
                    return delayTicks >= 3;
                }
            }
        }
        return false;
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
            attacked.add(en.getId(), 1 / expSpeed.get());
            delayTimer = 0;
            delayTicks = 0;

            Box box = new Box(en.getX() - 1, en.getY(), en.getZ() - 1, en.getX() + 1, en.getY() + 2, en.getZ() + 1);

            SettingUtils.swing(SwingState.Pre, SwingType.Attacking);

            removeExisted(en.getBlockPos());

            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));

            SettingUtils.swing(SwingState.Post, SwingType.Attacking);

            if (SettingUtils.shouldRotate(RotationType.Attacking)) {
                Managers.ROTATION.end(box);
            }

            blocked.clear();
            if (setDead.get()) {
                Managers.DELAY.add(() -> setEntityDead(en), setDeadDelay.get());
            }
        }
    }

    boolean existedCheck(BlockPos pos) {
        if (existedMode.get() == ExistedMode.Seconds) {
            return !existedList.containsKey(pos) || System.currentTimeMillis() > existedList.get(pos) + existed.get() * 1000;
        } else {
            return !existedTicksList.containsKey(pos) || ticksEnabled > existedTicksList.get(pos) + existedTicks.get();
        }
    }

    void addExisted(BlockPos pos) {
        if (existedMode.get() == ExistedMode.Seconds) {
            if (!existedList.containsKey(pos)) {
                existedList.put(pos, System.currentTimeMillis());
            }
        } else {
            if (!existedTicksList.containsKey(pos)) {
                existedTicksList.put(pos, ticksEnabled);
            }
        }
    }
    void removeExisted(BlockPos pos) {
        if (existedMode.get() == ExistedMode.Seconds) {
            if (existedList.containsKey(pos)) {
                existedList.remove(pos);
            }
        } else {
            if (existedTicksList.containsKey(pos)) {
                existedTicksList.remove(pos);
            }
        }
    }

    boolean canExplode(Vec3d vec, boolean sProt) {
        if (onlyOwn.get() && !isOwn(vec)) {return false;}
        if (!inExplodeRange(vec)) {return false;}

        double[][] result = getDmg(vec);
        return explodeDamageCheck(result[0], result[1], isOwn(vec), sProt);
    }

    boolean canExplodePlacing(Vec3d vec) {
        if (onlyOwn.get() && !isOwn(vec)) {return false;}
        if (!inExplodeRangePlacing(vec)) {return false;}

        double[][] result = getDmg(vec);
        return explodeDamageCheck(result[0], result[1], isOwn(vec), false);
    }

    Hand getHand(Item item) {
        return Managers.HOLDING.isHolding(item) ? Hand.MAIN_HAND:
            mc.player.getOffHandStack().getItem().equals(item) ? Hand.OFF_HAND : null;
    }

    boolean pausedCheck() {
        if (mc.player != null) {
            return pauseEat.get() && (mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE));
        }
        return true;
    }

    void setEntityDead(Entity en) {
        mc.world.removeEntity(en.getId(), Entity.RemovalReason.KILLED);
    }

    BlockPos getPlacePos() {

        r = (int) Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));
        bestPos = null;
        bestDir = null;
        highest = null;

        BlockPos pPos = new BlockPos(mc.player.getEyePos());

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    pos = pPos.add(x, y, z);
                    // Checks if crystal can be placed
                    if (!air(pos) || !(!oldVerPlacements.get() || air(pos.up())) || !crystalBlock(pos.down())) {continue;}

                    // Checks if there is possible placing direction
                    dir = SettingUtils.getPlaceOnDirection(pos.down());
                    if (dir == null) {continue;}

                    // Checks if the placement is in range
                    if (!inPlaceRange(pos.down()) || !inExplodeRangePlacing(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) {continue;}

                    // Calculates damages and healths
                    result = getDmg(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));

                    // Checks if damages are valid
                    if (!placeDamageCheck(result[0], result[1], highest)) {continue;}

                    // Checks if placement is blocked by other entities (other than players)
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + (ccPlacements.get() ? 1 : 2), pos.getZ() + 1);

                    if (BOEntityUtils.intersectsWithEntity(box, this::validForIntersect, extHitbox)) {continue;}

                    // Sets best pos to calculated one
                    bestDir = dir;
                    bestPos = pos;
                    highest = result[0];
                }
            }
        }

        placeDir = bestDir;
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

    boolean explodeDamageCheck(double[] dmg, double[] health, boolean own, boolean sProt) {
        boolean checkOwn = expMode.get() == ExplodeMode.FullCheck || expMode.get() == ExplodeMode.SelfDmgCheck
            || expMode.get() == ExplodeMode.SelfDmgOwn || expMode.get() == ExplodeMode.AlwaysOwn;
        boolean checkDmg = expMode.get() == ExplodeMode.FullCheck || (expMode.get() == ExplodeMode.SelfDmgOwn && !own) ||
            (expMode.get() == ExplodeMode.AlwaysOwn && !own);

        //  0 = enemy, 1 = friend, 2 = self

        //  Force/anti-pop check
        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (checkOwn) {
            if (playerHP >= 0 && dmg[2] * forcePop.get() >= playerHP) {
                return false;
            }
            if (health[1] >= 0 && dmg[1] * antiFriendPop.get() >= health[1]) {
                return false;
            }

        }
        if (checkDmg && !sProt) {
            if (health[0] >= 0 && dmg[0] * forcePop.get() >= health[0]) {
                return true;
            }

        }

        if (checkDmg && !sProt) {
            if (dmg[0] < minExplode.get()) {
                return false;
            }

            if (dmg[1] / dmg[0] > maxFriendExpRatio.get()) {
                return false;
            }
            if (dmg[2] / dmg[0] > maxSelfExpRatio.get()) {
                return false;
            }
        }


        if (checkOwn && !sProt) {
            if (dmg[1] > maxFriendExp.get()) {
                return false;
            }
            if (dmg[2] > maxSelfExp.get()) {
                return false;
            }
        }
        return true;
    }

    boolean isOwn(Vec3d vec) {
        return isOwn(new BlockPos(vec));
    }
    boolean isOwn(BlockPos pos) {
        for (Map.Entry<BlockPos, Long> entry : own.entrySet()) {
            if (entry.getKey().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    double[][] getDmg(Vec3d vec) {
        self = BODamageUtils.crystalDamage(mc.player, extPos.containsKey(mc.player) ? extPos.get(mc.player) : mc.player.getBoundingBox(), vec, null, ignoreTerrain.get());

        if (suicide) {
            return new double[][]{new double[] {self, 0, 0}, new double[]{20, 20}};
        }
        if (dmgCache.containsKey(vec)) {
            return dmgCache.get(vec);
        }
        highestEnemy = -1;
        highestFriend = -1;
        self = -1;
        enemyHP = -1;
        friendHP = -1;
        for (Map.Entry<PlayerEntity, Box> entry : extPos.entrySet()) {
            PlayerEntity player = entry.getKey();
            Box box = entry.getValue();
            if (player.getHealth() <= 0 || player == mc.player) {
                continue;
            }

            double dmg = BODamageUtils.crystalDamage(player, box, vec, null, ignoreTerrain.get());
            if (new BlockPos(vec).down().equals(AutoMine.targetPos)) {
                dmg *= autoMineDamage.get();
            }
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

        double[][] result = new double[][]{new double[] {highestEnemy, highestFriend, self}, new double[]{enemyHP, friendHP}};
        dmgCache.put(vec, result);
        return result;
    }

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
    boolean inExplodeRangePlacing(Vec3d vec) {
        return SettingUtils.inAttackRange(new Box(vec.getX() - 1, vec.getY(), vec.getZ() - 1, vec.getX() + 1, vec.getY() + 2, vec.getZ() + 1), rangePos != null ? rangePos : null);
    }
    boolean inExplodeRange(Vec3d vec) {
        return SettingUtils.inAttackRange(new Box(vec.getX() - 1, vec.getY(), vec.getZ() - 1, vec.getX() + 1, vec.getY() + 2, vec.getZ() + 1));
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

        double x = (absX + Math.pow(absX, animationMoveExponent.get() - 1)) * delta;
        double y = (absX + Math.pow(absY, animationMoveExponent.get() - 1)) * delta;
        double z = (absX + Math.pow(absZ, animationMoveExponent.get() - 1)) * delta;

        return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x),
            current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y),
            current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
    }

    Vec3d motionCalc(PlayerEntity en) {
        return new Vec3d(en.getX() - en.prevX, en.getY() - en.prevY, en.getZ() - en.prevZ);
    }

    Map<PlayerEntity, Box> getExtPos() {

        Map<PlayerEntity, Box> map = new HashMap<>();

        if (!mc.world.getPlayers().isEmpty()) {
            if (!motions.isEmpty()) {
                for (int p = 0; p < mc.world.getPlayers().size(); p++) {

                    PlayerEntity en = mc.world.getPlayers().get(p);

                    if (en.getHealth() <= 0) {
                        continue;
                    }

                    Vec3d motion = average(motions.get(en.getName().getString()));

                    double x = motion.x;
                    double y = motion.y;
                    double z = motion.z;

                    Box box = en.getBoundingBox();

                    if (!inside(en, box)) {
                        for (int i = 0; i < (en == mc.player ? selfExt.get() : extrapolation.get()); i++) {

                            // y
                            if (!inside(en, box.offset(0, -0.01, 0))) {
                                y = (y - 0.08) * 0.98;
                                if (!inside(en, box.offset(0, y, 0))) {
                                    box = box.offset(0, y, 0);
                                }
                            } else {
                                y = -0.0784;
                            }

                            // half block step check
                            if (inside(en, box.offset(x, 0, z)) && !inside(en, box.offset(x, 0.5, z))) {
                                box = box.offset(x, 0.5, z);
                                continue;
                            }

                            // x
                            if (!inside(en, box.offset(x, 0, 0))) {
                                box = box.offset(x, 0, 0);
                            }

                            // z
                            if (!inside(en, box.offset(0, 0, z))) {
                                box = box.offset(0, 0, z);
                            }
                        }
                    }

                    map.put(en, box);
                }
            }
        }
        return map;
    }

    Map<PlayerEntity, Box> getHitboxExt() {

        Map<PlayerEntity, Box> map = new HashMap<>();

        if (!mc.world.getPlayers().isEmpty()) {
            if (!motions.isEmpty()) {
                for (int p = 0; p < mc.world.getPlayers().size(); p++) {

                    PlayerEntity en = mc.world.getPlayers().get(p);

                    if (en == mc.player) {continue;}

                    if (en.getHealth() <= 0) {continue;}

                    Vec3d motion = average(motions.get(en.getName().getString()));

                    double x = motion.x;
                    double y = motion.y;
                    double z = motion.z;

                    Box box = en.getBoundingBox();

                    if (!inside(en, box)) {
                        for (int i = 0; i < hitboxExtrapolation.get(); i++) {

                            // y
                            if (!inside(en, box.offset(0, -0.01, 0))) {
                                y = (y - 0.08) * 0.98;
                                if (!inside(en, box.offset(0, y, 0))) {
                                    box = box.offset(0, y, 0);
                                }
                            } else {
                                y = -0.0784;
                            }

                            // half block step check
                            if (inside(en, box.offset(x, 0, z)) && !inside(en, box.offset(x, 0.5, z))) {
                                box = box.offset(x, 0.5, z);
                                continue;
                            }

                            // x
                            if (!inside(en, box.offset(x, 0, 0))) {
                                box = box.offset(x, 0, 0);
                            }

                            // z
                            if (!inside(en, box.offset(0, 0, z))) {
                                box = box.offset(0, 0, z);
                            }
                        }
                    }

                    map.put(en, box);
                }
            }
        }
        return map;
    }

    Vec3d getRangeExt() {
        if (!motions.containsKey(mc.player.getName().getString())) {return mc.player.getEyePos();}

        Vec3d motion = average(motions.get(mc.player.getName().getString()));

        double x = motion.x;
        double y = motion.y;
        double z = motion.z;

        Box box = mc.player.getBoundingBox();

        if (!inside(mc.player, box)) {
            for (int i = 0; i < rangeExtrapolation.get(); i++) {

                // y
                if (!inside(mc.player, box.offset(0, -0.01, 0))) {
                    y = (y - 0.08) * 0.98;
                    if (!inside(mc.player, box.offset(0, y, 0))) {
                        box = box.offset(0, y, 0);
                    }
                } else {
                    y = -0.0784;
                }

                // half block step check
                if (inside(mc.player, box.offset(x, 0, z)) && !inside(mc.player, box.offset(x, 0.5, z))) {
                    box = box.offset(x, 0.5, z);
                    continue;
                }

                // x
                if (!inside(mc.player, box.offset(x, 0, 0))) {
                    box = box.offset(x, 0, 0);
                }

                // z
                if (!inside(mc.player, box.offset(0, 0, z))) {
                    box = box.offset(0, 0, z);
                }
            }
        }
        return new Vec3d((box.minX + box.maxX) / 2, box.minY + mc.player.getEyeHeight(mc.player.getPose()), (box.minZ + box.maxZ) / 2);
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

    double smoothChange(double target, double current, double delta) {
        double d = target - current;
        double c = d * delta;
        return Math.abs(d) <= Math.abs(c) ? target : current + c;
    }

    boolean validForIntersect(Entity entity) {
        if (entity instanceof EndCrystalEntity && canExplodePlacing(entity.getPos())) {return false;}

        if (entity instanceof PlayerEntity && entity.isSpectator()) {return false;}

        return true;
    }
}
