package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.IntTimerList;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import kassuk.addon.blackout.utils.meteor.BODamageUtils;
import kassuk.addon.blackout.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
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
import java.util.function.Predicate;

/**
 * @author OLEPOSSU
 */
public class AutoCrystalRewrite extends BlackOutModule {
    public AutoCrystalRewrite() {
        super(BlackOut.BLACKOUT, "Auto Crystal Rewrite", "Breaks and places crystals automatically (but better).");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgExplode = settings.createGroup("Explode");
    private final SettingGroup sgSwitch = settings.createGroup("Switch");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgID = settings.createGroup("ID Predict");
    private final SettingGroup sgExtrapolation = settings.createGroup("Extrapolation");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgCompatibility = settings.createGroup("Compatibility");
    private final SettingGroup sgDebug = settings.createGroup("Debug");

    //--------------------General--------------------//
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

    //--------------------Place--------------------//
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
        .name("Instant Place")
        .description("Ignores delay after crystal has disappeared.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> speedLimit = sgPlace.add(new DoubleSetting.Builder()
        .name("Speed Limit")
        .description("Maximum amount of place packets every second. 0 = no limit")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 20)
        .visible(instantPlace::get)
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
        .description("Should we count the delay in seconds or ticks.")
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
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> slowSpeed = sgPlace.add(new DoubleSetting.Builder()
        .name("Slow Speed")
        .description("How many times should the module place per second when damage is under slow damage.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Explode--------------------//
    private final Setting<Boolean> onlyOwn = sgExplode.add(new BoolSetting.Builder()
        .name("Only Own")
        .description("Only attacks own crystals.")
        .defaultValue(false)
        .build()
    );
    private final Setting<ExistedMode> existedMode = sgPlace.add(new EnumSetting.Builder<ExistedMode>()
        .name("Existed Mode")
        .description("Should crystal existed times be counted in seconds or ticks.")
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
    private final Setting<Double> expSpeedLimit = sgExplode.add(new DoubleSetting.Builder()
        .name("Explode Speed Limit")
        .description("How many times to hit any crystal each second. 0 = no limit")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> setDead = sgExplode.add(new BoolSetting.Builder()
        .name("Set Dead")
        .description("Hides the crystal after hitting it. Not needed since the module already is smart enough.")
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

    //--------------------Switch--------------------//
    private final Setting<SwitchMode> switchMode = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Mode for switching to crystal in main hand.")
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

    //--------------------Damage--------------------//
    private final Setting<DmgCheckMode> dmgCheckMode = sgDamage.add(new EnumSetting.Builder<DmgCheckMode>()
        .name("Dmg Check Mode")
        .description("How safe are the placements (normal is good).")
        .defaultValue(DmgCheckMode.Normal)
        .build()
    );
    private final Setting<Double> minPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Place")
        .description("Minimum damage to place.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Place")
        .description("Max self damage for placing.")
        .defaultValue(8)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> minPlaceRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Place Ratio")
        .description("Max self damage ratio for placing (enemy / self).")
        .defaultValue(3)
        .min(0)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> maxFriendPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Place")
        .description("Max friend damage for placing.")
        .defaultValue(8)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> minFriendPlaceRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Friend Place Ratio")
        .description("Max friend damage ratio for placing (enemy / friend).")
        .defaultValue(2)
        .min(0)
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
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxExp = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Explode")
        .description("Max self damage for exploding a crystal.")
        .defaultValue(9)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> minExpRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Explode Ratio")
        .description("Max self damage ratio for exploding a crystal (enemy / self).")
        .defaultValue(2.5)
        .min(0)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> maxFriendExp = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Explode")
        .description("Max friend damage for exploding a crystal.")
        .defaultValue(12)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> minFriendExpRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Friend Explode Ratio")
        .description("Min friend damage ratio for exploding a crystal (enemy / friend).")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> forcePop = sgDamage.add(new DoubleSetting.Builder()
        .name("Force Pop")
        .description("Ignores damage checks if any enemy will be popped in x hits.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiFriendPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Friend Pop")
        .description("Cancels any action if any friend will be popped in x hits.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiSelfPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Self Pop")
        .description("Cancels any action if you will be popped in x hits.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    //--------------------ID-Predict--------------------//
    private final Setting<Boolean> idPredict = sgID.add(new BoolSetting.Builder()
        .name("ID Predict")
        .description("Hits the crystal before it spawns.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> idStartOffset = sgID.add(new IntSetting.Builder()
        .name("Id Start Offset")
        .description("How many id's ahead should we attack.")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> idOffset = sgID.add(new IntSetting.Builder()
        .name("Id Packet Offset")
        .description("How many id's ahead should we attack between id packets.")
        .defaultValue(1)
        .min(1)
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
    private final Setting<Double> idDelay = sgID.add(new DoubleSetting.Builder()
        .name("ID Start Delay")
        .description("Starts sending id predict packets after this many seconds.")
        .defaultValue(0.05)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Double> idPacketDelay = sgID.add(new DoubleSetting.Builder()
        .name("ID Packet Delay")
        .description("Waits this many seconds between sending ID packets.")
        .defaultValue(0.05)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    //--------------------Extrapolation--------------------//
    private final Setting<Integer> selfExt = sgExtrapolation.add(new IntSetting.Builder()
        .name("Self Extrapolation")
        .description("How many ticks of movement should be predicted for self damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("How many ticks of movement should be predicted for enemy damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> rangeExtrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Range Extrapolation")
        .description("How many ticks of movement should be predicted for attack ranges before placing.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> hitboxExtrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Hitbox Extrapolation")
        .description("How many ticks of movement should be predicted for hitboxes in placing checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
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

    //--------------------Render--------------------//
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders box on placement.")
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
        .description("Which parts of render should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha value.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.Earthhack) || renderMode.get().equals(RenderMode.Future))
        .build()
    );
    private final Setting<FadeMode> fadeMode = sgRender.add(new EnumSetting.Builder<FadeMode>()
        .name("Fade Mode")
        .description("How long the fading should take.")
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
        .defaultValue(2)
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
        .description("Line color of rendered boxes")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered boxes")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    //--------------------Compatibility--------------------//
    private final Setting<Boolean> surroundAttack = sgCompatibility.add(new BoolSetting.Builder()
        .name("Surround Attack")
        .description("Attacks any crystal blocking surround placement.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> alwaysAttack = sgCompatibility.add(new BoolSetting.Builder()
        .name("Always Attack")
        .description("Attacks any crystal even when the block is already placed.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> autoMineDamage = sgCompatibility.add(new DoubleSetting.Builder()
        .name("Auto Mine Damage")
        .description("Prioritizes placing on automine target block.")
        .defaultValue(1.1)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    //--------------------Debug--------------------//
    private final Setting<Boolean> renderExt = sgDebug.add(new BoolSetting.Builder()
        .name("Render Extrapolation")
        .description("Renders boxes at players' predicted positions.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> renderSelfExt = sgDebug.add(new BoolSetting.Builder()
        .name("Render Self Extrapolation")
        .description("Renders box at your predicted position.")
        .defaultValue(false)
        .build()
    );

    private long ticksEnabled = 0;
    private double placeTimer = 0;
    private double placeLimitTimer = 0;
    private double delayTimer = 0;
    private int delayTicks = 0;

    private BlockPos placePos = null;
    private Direction placeDir = null;
    private Entity expEntity = null;
    private Box expEntityBB = null;
    private final IntTimerList attacked = new IntTimerList(false);
    private final Map<BlockPos, Long> existedList = new HashMap<>();
    private final Map<BlockPos, Long> existedTicksList = new HashMap<>();
    private final Map<BlockPos, Long> own = new HashMap<>();
    private Map<PlayerEntity, Box> extPos = new HashMap<>();
    private Map<PlayerEntity, Box> extHitbox = new HashMap<>();
    private Vec3d rangePos = null;
    private final Map<String, List<Vec3d>> motions = new HashMap<>();
    private final List<Box> blocked = new ArrayList<>();
    private final Map<BlockPos, Double[]> earthMap = new HashMap<>();
    private double attackTimer = 0;
    private double switchTimer = 0;
    private int confirmed = Integer.MIN_VALUE;
    private double infoCps = 0;
    private double cps = 0;
    private long cpsTime = System.currentTimeMillis();
    private int explosions = 0;
    private long lastMillis = System.currentTimeMillis();
    private boolean suicide = false;
    public static boolean placing = false;

    private Vec3d renderTarget = null;
    private Vec3d renderPos = null;
    private double renderProgress = 0;
    private AutoMine autoMine = null;

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
        if (autoMine == null) {
            autoMine = Modules.get().get(AutoMine.class);
        }
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
        attackTimer = Math.max(attackTimer - d, 0);
        placeTimer = Math.max(placeTimer - d * getSpeed(), 0);
        placeLimitTimer += d;
        delayTimer += d;
        switchTimer = Math.max(0, switchTimer - d);
        update();

        //Rendering
        if (render.get()) {
            switch (renderMode.get()) {
                case BlackOut -> {
                    if (placePos != null && !isPaused() && holdingCheck()) {
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
                    if (placePos != null && !isPaused() && holdingCheck()) {
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
                    packet.getBlockHitResult().getBlockPos().getY() + 3, packet.getBlockHitResult().getBlockPos().getZ() + 1.5));
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
    private void update() {
        placing = false;
        expEntity = null;

        double[] value = null;
        boolean shouldProtectSurround = surroundProt();
        Hand hand = getHand(stack -> stack.getItem() == Items.END_CRYSTAL);
        if (!isPaused() && (hand != null || switchMode.get() == SwitchMode.Silent || switchMode.get() == SwitchMode.PickSilent || switchMode.get() == SwitchMode.InvSilent) && explode.get()) {
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
            if (!isAttacked(expEntity.getId()) && attackTimer <= 0) {
                if (existedCheck(expEntity.getBlockPos())) {
                    if (!SettingUtils.shouldRotate(RotationType.Attacking) || startAttackRot()) {
                        if (SettingUtils.shouldRotate(RotationType.Attacking)) {
                            expEntityBB = expEntity.getBoundingBox();
                        }
                        explode(expEntity.getId(), expEntity, expEntity.getPos());
                    }
                }
            }
        }
        if (!isAlive(expEntityBB) && SettingUtils.shouldRotate(RotationType.Attacking)) {
            Managers.ROTATION.end(expEntityBB);
        }
        Hand handToUse = hand;
        if (!performance.get()) {
            updatePlacement();
        }
        switch (switchMode.get()) {
            case Simple -> {
                int slot = InvUtils.findInHotbar(Items.END_CRYSTAL).slot();
                if (placePos != null && hand == null && slot >= 0) {
                    InvUtils.swap(slot, false);
                    handToUse = Hand.MAIN_HAND;
                }
            }
            case Smart -> {
                int gapSlot = InvUtils.findInHotbar(OLEPOSSUtils::isGapple).slot();
                if (shouldGap(gapSlot)) {
                    if (getHand(OLEPOSSUtils::isGapple) == null) {
                        InvUtils.swap(gapSlot, false);
                    }
                    handToUse = getHand(itemStack -> itemStack.getItem() == Items.END_CRYSTAL);
                } else if (!expFriendly.get() || !mc.player.isUsingItem()) {
                    int slot = InvUtils.findInHotbar(itemStack -> itemStack.getItem().equals(Items.END_CRYSTAL)).slot();
                    if (placePos != null && hand == null && slot >= 0) {
                        InvUtils.swap(slot, false);
                        handToUse = Hand.MAIN_HAND;
                    }
                }
            }
            case Gapple -> {
                int gapSlot = InvUtils.findInHotbar(OLEPOSSUtils::isGapple).slot();
                if (mc.options.useKey.isPressed() && Managers.HOLDING.isHolding(Items.END_CRYSTAL, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE) && gapSlot >= 0) {
                    if (getHand(OLEPOSSUtils::isGapple) == null) {
                        InvUtils.swap(gapSlot, false);
                    }
                    handToUse = getHand(itemStack -> itemStack.getItem() == Items.END_CRYSTAL);
                } else if (Managers.HOLDING.isHolding(Items.END_CRYSTAL, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE)) {
                    int slot = InvUtils.findInHotbar(Items.END_CRYSTAL).slot();
                    if (placePos != null && hand == null && slot >= 0) {
                        InvUtils.swap(slot, false);
                        handToUse = Hand.MAIN_HAND;
                    }
                }
            }
        }

        if (placePos != null && placeDir != null) {
            if (!isPaused()) {
                int silentSlot = InvUtils.find(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).slot();
                int hotbar = InvUtils.findInHotbar(Items.END_CRYSTAL).slot();
                if (handToUse != null || (switchMode.get() == SwitchMode.Silent && hotbar >= 0) || ((switchMode.get() == SwitchMode.PickSilent || switchMode.get() == SwitchMode.InvSilent) && silentSlot >= 0)) {
                    placing = true;
                    if (speedCheck() && delayCheck()) {
                        if (!SettingUtils.shouldRotate(RotationType.Crystal) || Managers.ROTATION.start(placePos.down(), smartRot.get() ? new Vec3d(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5) : null, priority - 0.1, RotationType.Crystal)) {
                            placeCrystal(placePos.down(), placeDir, handToUse, silentSlot, hotbar);
                        }
                    }
                }
            }
        } else {
            BlockPos lastPos = null;
        }
    }

    private boolean startAttackRot() {
        expEntityBB = expEntity.getBoundingBox();
        return (Managers.ROTATION.start(expEntity.getBoundingBox(), smartRot.get() ? expEntity.getPos() : null, priority, RotationType.Attacking));
    }

    private boolean isAlive(Box box) {
        if (box == null) {return true;}

        for (Entity en : mc.world.getEntities()) {
            if (!(en instanceof EndCrystalEntity)) {continue;}

            if (bbEquals(box, en.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private boolean bbEquals(Box box1, Box box2) {
        return box1.minX == box2.minX &&
            box1.minY == box2.minY &&
            box1.minZ == box2.minZ &&
            box1.maxX == box2.maxX &&
            box1.maxY == box2.maxY &&
            box1.maxZ == box2.maxZ;
    }

    private boolean speedCheck() {

        if (speedLimit.get() > 0 && placeLimitTimer < 1 / speedLimit.get()) {return false;}

        if (instantPlace.get() && !shouldSlow() && !isBlocked(placePos)) {return true;}

        return placeTimer <= 0;
    }

    private boolean surroundProt() {
        if (!surroundAttack.get() || !Modules.get().isActive(SurroundPlus.class)) {return false;}

        for (BlockPos attack : SurroundPlus.attack) {
            if ((alwaysAttack.get() || OLEPOSSUtils.replaceable(attack)) &&
                EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(attack), entity -> entity instanceof EndCrystalEntity)) {
                return true;
            }
        }
        return false;
    }

    private boolean holdingCheck() {
        switch (switchMode.get()) {
            case Silent -> {
                return InvUtils.findInHotbar(Items.END_CRYSTAL).slot() >= 0;
            }
            case PickSilent, InvSilent -> {
                return InvUtils.find(Items.END_CRYSTAL).slot() >= 0;
            }
            default -> {
                return getHand(itemStack -> itemStack.getItem() == Items.END_CRYSTAL) != null;
            }
        }
    }

    private void updatePlacement() {
        if (!place.get()) {
            placePos = null;
            placeDir = null;
            return;
        }
        placePos = getPlacePos();
    }

    private boolean shouldGap(int slot) {
        if (slot < 0) {return false;}
        if (!mc.options.useKey.isPressed()) {return false;}
        if (placePos == null && !alwaysGap.get()) {return false;}
        if (!Managers.HOLDING.isHolding(Items.END_CRYSTAL) && !Managers.HOLDING.isHolding(Items.ENCHANTED_GOLDEN_APPLE) && onlyCrystal.get()) {return false;}
        return true;
    }

    private void placeCrystal(BlockPos pos, Direction dir, Hand handToUse, int sl, int hsl) {
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
                    case PickSilent -> BOInvUtils.pickSwitch(sl);
                    case Silent -> InvUtils.swap(hsl, true);
                    case InvSilent -> BOInvUtils.invSwitch(sl);
                }
            }

            SettingUtils.swing(SwingState.Pre, SwingType.Crystal, switched ? Hand.MAIN_HAND : handToUse);

            addExisted(pos.up());

            if (!isOwn(pos.up())) {
                own.put(pos.up(), System.currentTimeMillis());
            } else {
                own.remove(pos.up());
                own.put(pos.up(), System.currentTimeMillis());
            }

            placeLimitTimer = 0;
            placeTimer = 1;

            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(switched ? Hand.MAIN_HAND : handToUse,
                new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), dir, pos, false), 0));

            SettingUtils.swing(SwingState.Post, SwingType.Crystal, switched ? Hand.MAIN_HAND : handToUse);

            if (SettingUtils.shouldRotate(RotationType.Crystal)) {
                Managers.ROTATION.end(OLEPOSSUtils.getBox(pos));
            }

            if (switched) {
                switch (switchMode.get()) {
                    case PickSilent -> BOInvUtils.pickSwapBack();
                    case Silent -> InvUtils.swapBack();
                    case InvSilent -> BOInvUtils.swapBack();
                }
            }
            if (idPredict.get()) {
                int id = getHighest() + idStartOffset.get();
                for (int i = 0; i < idPackets.get() * idOffset.get(); i += idOffset.get()) {
                    Entity en = mc.world.getEntityById(id + i);
                    if (en instanceof ItemEntity) {continue;}

                    int finalI = i;
                    Managers.DELAY.add(() -> explode(id + finalI, null, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5)), idDelay.get() + idPacketDelay.get() * i);
                }
                confirmed++;
            }
        }
    }

    private boolean delayCheck() {
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

    private int getHighest() {
        int highest = confirmed;
        for (Entity entity : mc.world.getEntities()) {
            if (entity.getId() > highest) {
                highest = entity.getId();
            }
        }
        return highest;
    }

    private boolean isBlocked(BlockPos pos) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        for (Box bb : blocked) {
            if (bb.intersects(box)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAttacked(int id) {
        return attacked.contains(id);
    }

    private void explode(int id, Entity en, Vec3d vec) {
        if (en != null) {
            attackEntity(en, en.getBoundingBox());
        } else {
            attackID(id, vec);
        }
    }

    private void attackID(int id, Vec3d pos) {
        Hand handToUse = getHand(itemStack -> itemStack.getItem() == Items.END_CRYSTAL);
        if (handToUse != null && !isPaused()) {
            EndCrystalEntity en = new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z);
            en.setId(id);
            attackEntity(en, OLEPOSSUtils.getCrystalBox(pos));
        }
    }

    private void attackEntity(Entity en, Box bb) {
        if (mc.player != null) {
            attacked.add(en.getId(), 1 / expSpeed.get());
            attackTimer = expSpeedLimit.get() <= 0 ? 0 : 1 / expSpeedLimit.get();

            delayTimer = 0;
            delayTicks = 0;

            SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);

            removeExisted(en.getBlockPos());

            SettingUtils.registerAttack(bb);
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));

            if (surroundAttack.get()) {
                SurroundPlus.attacked = 2;
            }

            SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);

            blocked.clear();
            if (setDead.get()) {
                Managers.DELAY.add(() -> setEntityDead(en), setDeadDelay.get());
            }
        }
    }

    private boolean existedCheck(BlockPos pos) {
        if (existedMode.get() == ExistedMode.Seconds) {
            return !existedList.containsKey(pos) || System.currentTimeMillis() > existedList.get(pos) + existed.get() * 1000;
        } else {
            return !existedTicksList.containsKey(pos) || ticksEnabled >= existedTicksList.get(pos) + existedTicks.get();
        }
    }

    private void addExisted(BlockPos pos) {
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
    private void removeExisted(BlockPos pos) {
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

    private boolean canExplode(Vec3d vec, boolean sProt) {
        if (onlyOwn.get() && !isOwn(vec)) {return false;}
        if (!inExplodeRange(vec)) {return false;}

        double[][] result = getDmg(vec);
        return explodeDamageCheck(result[0], result[1], isOwn(vec), sProt);
    }

    private boolean canExplodePlacing(Vec3d vec) {
        if (onlyOwn.get() && !isOwn(vec)) {return false;}
        if (!inExplodeRangePlacing(vec)) {return false;}

        double[][] result = getDmg(vec);
        return explodeDamageCheck(result[0], result[1], isOwn(vec), false);
    }

    private Hand getHand(Predicate<ItemStack> predicate) {
        return predicate.test(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND :
            predicate.test(mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;
    }

    private boolean isPaused() {
        return pauseEat.get() && mc.player.isUsingItem();
    }

    private void setEntityDead(Entity en) {
        mc.world.removeEntity(en.getId(), Entity.RemovalReason.KILLED);
    }

    private BlockPos getPlacePos() {

        int r = (int) Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));
        //Used in placement calculation
        BlockPos bestPos = null;
        Direction bestDir = null;
        double[] highest = null;

        BlockPos pPos = OLEPOSSUtils.toPos(mc.player.getEyePos());

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = pPos.add(x, y, z);
                    // Checks if crystal can be placed
                    if (!air(pos) || !(!oldVerPlacements.get() || air(pos.up())) || !crystalBlock(pos.down())) {continue;}

                    // Checks if there is possible placing direction
                    Direction dir = SettingUtils.getPlaceOnDirection(pos.down());
                    if (dir == null) {continue;}

                    // Checks if the placement is in range
                    if (!inPlaceRange(pos.down()) || !inExplodeRangePlacing(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) {continue;}

                    // Calculates damages and healths
                    double[][] result = getDmg(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));

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

    private boolean placeDamageCheck(double[] dmg, double[] health, double[] highest) {
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
        if (dmg[1] >= 0 && dmg[0] / dmg[1] < minFriendPlaceRatio.get()) {return false;}
        if (dmg[2] > maxPlace.get()) {return false;}
        if (dmg[2] >= 0 && dmg[0] / dmg[2] < minPlaceRatio.get()) {return false;}

        return true;
    }

    private boolean explodeDamageCheck(double[] dmg, double[] health, boolean own, boolean sProt) {
        boolean checkOwn = expMode.get() == ExplodeMode.FullCheck
            || expMode.get() == ExplodeMode.SelfDmgCheck
            || expMode.get() == ExplodeMode.SelfDmgOwn
            || expMode.get() == ExplodeMode.AlwaysOwn;

        boolean checkDmg = expMode.get() == ExplodeMode.FullCheck
            || (expMode.get() == ExplodeMode.SelfDmgOwn && !own)
            || (expMode.get() == ExplodeMode.AlwaysOwn && !own);

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

            if (dmg[1] >= 0 && dmg[0] / dmg[1] < minFriendExpRatio.get()) {
                return false;
            }
            if (dmg[2] >= 0 && dmg[0] / dmg[2] < minExpRatio.get()) {
                return false;
            }
        }


        if (checkOwn && !sProt) {
            if (dmg[1] > maxFriendExp.get()) {
                return false;
            }
            if (dmg[2] > maxExp.get()) {
                return false;
            }
        }
        return true;
    }

    private boolean isOwn(Vec3d vec) {
        return isOwn(OLEPOSSUtils.toPos(vec));
    }
    private boolean isOwn(BlockPos pos) {
        for (Map.Entry<BlockPos, Long> entry : own.entrySet()) {
            if (entry.getKey().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private double[][] getDmg(Vec3d vec) {
        double self = BODamageUtils.crystalDamage(mc.player, extPos.containsKey(mc.player) ? extPos.get(mc.player) : mc.player.getBoundingBox(), vec, null, ignoreTerrain.get());

        if (suicide) {
            return new double[][]{new double[] {self, -1, -1}, new double[]{20, 20}};
        }
        double highestEnemy = -1;
        double highestFriend = -1;
        double enemyHP = -1;
        double friendHP = -1;
        for (Map.Entry<PlayerEntity, Box> entry : extPos.entrySet()) {
            PlayerEntity player = entry.getKey();
            Box box = entry.getValue();
            if (player.getHealth() <= 0 || player == mc.player) {
                continue;
            }

            double dmg = BODamageUtils.crystalDamage(player, box, vec, null, ignoreTerrain.get());
            if (OLEPOSSUtils.toPos(vec).down().equals(autoMine.targetPos())) {
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

        return new double[][]{new double[] {highestEnemy, highestFriend, self}, new double[]{enemyHP, friendHP}};
    }

    private boolean air(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);
    }

    private boolean crystalBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) ||
            mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK);
    }

    private boolean inPlaceRange(BlockPos pos) {
        return SettingUtils.inPlaceRange(pos);
    }

    private boolean inExplodeRangePlacing(Vec3d vec) {
        return SettingUtils.inAttackRange(new Box(vec.getX() - 1, vec.getY(), vec.getZ() - 1, vec.getX() + 1, vec.getY() + 2, vec.getZ() + 1), rangePos != null ? rangePos : null);
    }

    private boolean inExplodeRange(Vec3d vec) {
        return SettingUtils.inAttackRange(new Box(vec.getX() - 1, vec.getY(), vec.getZ() - 1, vec.getX() + 1, vec.getY() + 2, vec.getZ() + 1));
    }

    private double getSpeed() {
        return shouldSlow() ? slowSpeed.get() : placeSpeed.get();
    }

    private boolean shouldSlow() {return placePos != null && getDmg(new Vec3d(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5))[0][0] <= slowDamage.get();}

    private Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
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

    private Vec3d motionCalc(PlayerEntity en) {
        return new Vec3d(en.getX() - en.prevX, en.getY() - en.prevY, en.getZ() - en.prevZ);
    }

    private Map<PlayerEntity, Box> getExtPos() {

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

    private Map<PlayerEntity, Box> getHitboxExt() {

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

    private Vec3d getRangeExt() {
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
        return new Vec3d((box.minX + box.maxX) / 2, box.minY, (box.minZ + box.maxZ) / 2);
    }

    private Vec3d average(List<Vec3d> vec) {
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

    private boolean inside(PlayerEntity en, Box bb) {
        return mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }

    private double smoothChange(double target, double current, double delta) {
        double d = target - current;
        double c = d * delta;
        return Math.abs(d) <= Math.abs(c) ? target : current + c;
    }

    private boolean validForIntersect(Entity entity) {
        if (entity instanceof EndCrystalEntity && canExplodePlacing(entity.getPos())) {
            return false;
        }

        return !(entity instanceof PlayerEntity) || !entity.isSpectator();
    }

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
        InvSilent,
        PickSilent
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
}
