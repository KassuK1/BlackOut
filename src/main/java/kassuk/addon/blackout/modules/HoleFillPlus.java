package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.HoleType;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.TimerList;
import kassuk.addon.blackout.utils.*;
import kassuk.addon.blackout.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.*;

/**
 * @author OLEPOSSU
 */

public class HoleFillPlus extends BlackOutModule {
    public HoleFillPlus() {
        super(BlackOut.BLACKOUT, "Hole Fill Rewrite", "Automatically is a cunt to your enemies.");
    }

    private final SettingGroup sgNear = settings.createGroup("Near");
    private final SettingGroup sgWalking = settings.createGroup("Walking");
    private final SettingGroup sgLooking = settings.createGroup("Looking");
    private final SettingGroup sgSelf = settings.createGroup("Self");
    private final SettingGroup sgPlacing = settings.createGroup("Placing");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgHole = settings.createGroup("Hole");

    //--------------------Near--------------------//
    private final Setting<Boolean> near = sgNear.add(new BoolSetting.Builder()
        .name("Near")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> nearDistance = sgNear.add(new DoubleSetting.Builder()
        .name("Near Distance")
        .description(".")
        .defaultValue(3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> nearExt = sgNear.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description(".")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Integer> selfExt = sgNear.add(new IntSetting.Builder()
        .name("Self Extrapolation")
        .description(".")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Integer> extSmooth = sgNear.add(new IntSetting.Builder()
        .name("Extrapolation Smoothening")
        .description(".")
        .defaultValue(2)
        .min(1)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> efficient = sgNear.add(new BoolSetting.Builder()
        .name("Efficient")
        .description("Only places if the hole is closer to target.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> above = sgNear.add(new BoolSetting.Builder()
        .name("Above")
        .description("Only places if target is above the hole.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> iHole = sgNear.add(new BoolSetting.Builder()
        .name("Ignore Hole")
        .description("Doesn't place if enemy is in a hole.")
        .defaultValue(true)
        .build()
    );

    //--------------------Walking--------------------//
    private final Setting<Boolean> walking = sgWalking.add(new BoolSetting.Builder()
        .name("Walking")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> walkingDist = sgWalking.add(new DoubleSetting.Builder()
        .name("Walking Dist")
        .description(".")
        .defaultValue(6)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> walkMemory = sgWalking.add(new IntSetting.Builder()
        .name("Walk Memory")
        .description("Fills the hole is enemy was walking to it during previous x ticks.")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Look--------------------//
    private final Setting<Boolean> look = sgLooking.add(new BoolSetting.Builder()
        .name("Looking")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> lookDist = sgLooking.add(new DoubleSetting.Builder()
        .name("Look Dist")
        .description(".")
        .defaultValue(10)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> lookMemory = sgWalking.add(new IntSetting.Builder()
        .name("Look Memory")
        .description("Fills the hole is enemy was looking at it during previous x ticks.")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Self--------------------//
    private final Setting<Boolean> iSelfHole = sgSelf.add(new BoolSetting.Builder()
        .name("Ignore Self Hole")
        .description("Doesn't check 'efficient' if you are in a hole.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> selfAbove = sgSelf.add(new BoolSetting.Builder()
        .name("Self Above")
        .description("Only checks 'efficient' if you are above the hole.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> selfDistance = sgSelf.add(new DoubleSetting.Builder()
        .name("Self Distance")
        .description("Doesn't place if the block is this close to you.")
        .defaultValue(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> selfWalking = sgSelf.add(new BoolSetting.Builder()
        .name("Self Walking")
        .description("Doesn't check 'efficient' if you are in a hole.")
        .defaultValue(true)
        .visible(efficient::get)
        .build()
    );
    private final Setting<Double> selfWalkingDist = sgSelf.add(new DoubleSetting.Builder()
        .name("Self Walk Dist")
        .description(".")
        .defaultValue(3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> selfWalkMemory = sgSelf.add(new IntSetting.Builder()
        .name("Self Walk Memory")
        .description("Doesn't fill any hole you were walking to during past x ticks.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Placing--------------------//
    private final Setting<SwitchMode> switchMode = sgPlacing.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. Silent is the most reliable but delays crystals on some servers.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SurroundPlus.PlaceDelayMode> placeDelayMode = sgPlacing.add(new EnumSetting.Builder<SurroundPlus.PlaceDelayMode>()
        .name("Place Delay Mode")
        .description(".")
        .defaultValue(SurroundPlus.PlaceDelayMode.Ticks)
        .build()
    );
    private final Setting<Integer> placeDelayT = sgPlacing.add(new IntSetting.Builder()
        .name("Place Tick Delay")
        .description("Tick delay between places.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0, 20)
        .visible(() -> placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Ticks)
        .build()
    );
    private final Setting<Double> placeDelayS = sgPlacing.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0.1)
        .min(0)
        .sliderRange(0, 1)
        .visible(() -> placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Seconds)
        .build()
    );
    private final Setting<Integer> places = sgPlacing.add(new IntSetting.Builder()
        .name("Places")
        .description("How many blocks to place each time.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> delay = sgPlacing.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Waits x seconds before trying to place at the same position if there is more than 1 missing block.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<List<Block>> blocks = sgPlacing.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
        .defaultValue(Blocks.OBSIDIAN)
        .build()
    );
    private final Setting<Integer> boxExt = sgPlacing.add(new IntSetting.Builder()
        .name("Box Extrapolation")
        .description("Enemy hitbox extrapolation")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Integer> boxExtSmooth = sgPlacing.add(new IntSetting.Builder()
        .name("Box Extrapolation Smoothening")
        .description(".")
        .defaultValue(2)
        .min(1)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Hole--------------------//
    private final Setting<Boolean> single = sgHole.add(new BoolSetting.Builder()
        .name("Single")
        .description("Fills 1x1 holes")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> doubleHole = sgHole.add(new BoolSetting.Builder()
        .name("Double")
        .description("Fills 2x1 block holes")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> quad = sgHole.add(new BoolSetting.Builder()
        .name("Quad")
        .description("Fills 2x2 block holes")
        .defaultValue(true)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final List<BlockPos> holes = new ArrayList<>();
    private final TimerList<BlockPos> timers = new TimerList<>();
    private final List<Render> render = new ArrayList<>();

    private final Map<AbstractClientPlayerEntity, List<Movement>> walkAngles = new HashMap<>();
    private final Map<AbstractClientPlayerEntity, List<Look>> lookAngles = new HashMap<>();

    private final Map<AbstractClientPlayerEntity, Box> nearPosition = new HashMap<>();
    private final Map<AbstractClientPlayerEntity, Box> boxes = new HashMap<>();

    private boolean shouldUpdate = false;

    private Hand hand = null;
    private int blocksLeft = 0;
    private int placesLeft = 0;
    private FindItemResult result = null;
    private boolean switched = false;

    private int tickTimer = 0;
    private long lastTime = 0;
    public static boolean placing = false;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Post event) {
        shouldUpdate = true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        timers.update();

        if (mc.player == null || mc.world == null) return;

        if (shouldUpdate) {
            update();
            shouldUpdate = false;
        }

        render.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

        render.forEach(r -> {
            double progress = 1 - Math.min(System.currentTimeMillis() - r.time + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);

            event.renderer.box(r.pos, new Color(sideColor.get().r, sideColor.get().g, sideColor.get().b, (int) Math.round(sideColor.get().a * progress)), new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
        });
    }

    private void update() {
        tickTimer++;

        updateMaps();
        updateHoles();
        updateResult();
        updatePlaces();
        updatePlacing();
    }

    private void updatePlacing() {
        blocksLeft = Math.min(placesLeft, result.count());
        hand = getHand();
        switched = false;

        holes.stream().sorted(Comparator.comparingDouble(pos -> pos.toCenterPos().distanceTo(mc.player.getEyePos()))).forEach(this::place);

        if (switched && hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
    }

    private void updateResult() {
        result = switch (switchMode.get()) {
            case Disabled -> null;
            case Normal, Silent -> InvUtils.findInHotbar(this::valid);
            case PickSilent, InvSwitch -> InvUtils.find(this::valid);
        };
    }

    private Hand getHand() {
        if (valid(Managers.HOLDING.getStack())) {
            return Hand.MAIN_HAND;
        }
        if (valid(mc.player.getOffHandStack())) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    private boolean valid(ItemStack stack) {
        return stack.getItem() instanceof BlockItem block && blocks.get().contains(block.getBlock());
    }

    private void updateMaps() {
        updateWalk();
        updateLook();

        ExtrapolationUtils.extrapolateMap(nearPosition, player -> player == mc.player ? selfExt.get() : nearExt.get(), player -> extSmooth.get());
        ExtrapolationUtils.extrapolateMap(boxes, player -> player == mc.player ? 0 : boxExt.get(), player -> boxExtSmooth.get());
    }

    private void updateWalk() {
        Map<AbstractClientPlayerEntity, List<Movement>> newMap = new HashMap<>();

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            Movement m = new Movement(MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(player.getZ() - player.prevZ, player.getX() - player.prevX)) - 90f), player.getPos());

            if (!walkAngles.containsKey(player)) {
                List<Movement> l = new ArrayList<>();
                l.add(m);
                newMap.put(player, l);
                continue;
            }
            List<Movement> l = walkAngles.get(player);
            l.add(0, m);

            if (l.size() > 20) {
                l.subList(20, l.size()).clear();
            }

            newMap.put(player, l);
        }

        walkAngles.clear();
        walkAngles.putAll(newMap);
        newMap.clear();
    }

    private void updateLook() {
        Map<AbstractClientPlayerEntity, List<Look>> newMap = new HashMap<>();

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            Look e = new Look(MathHelper.wrapDegrees(player.getYaw()), player.getPitch(), player.getEyePos());

            if (!lookAngles.containsKey(player)) {
                List<Look> l = new ArrayList<>();
                l.add(e);
                newMap.put(player, l);
                continue;
            }
            List<Look> l = lookAngles.get(player);
            l.add(0, e);

            if (l.size() > 20) {
                l.subList(20, l.size()).clear();
            }

            newMap.put(player, l);
        }

        lookAngles.clear();
        lookAngles.putAll(newMap);
        newMap.clear();
    }

    private void updateHoles() {
        holes.clear();

        int range = (int) Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()) + 1);
        BlockPos p = BlockPos.ofFloored(mc.player.getEyePos());

        List<Hole> holeList = new ArrayList<>();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    Hole hole = HoleUtils.getHole(p.add(x, y, z));

                    if (hole.type == HoleType.NotHole) continue;
                    if (!single.get() && hole.type == HoleType.Single) continue;
                    if (!doubleHole.get() && (hole.type == HoleType.DoubleX || hole.type == HoleType.DoubleZ)) continue;
                    if (!quad.get() && hole.type == HoleType.Quad) continue;

                    holeList.add(hole);
                }
            }
        }

        holeList.forEach(hole -> {
            if (!validHole(hole)) return;

            Arrays.stream(hole.positions).filter(this::validPos).forEach(holes::add);
        });
    }

    private boolean validPos(BlockPos pos) {
        if (timers.contains(pos)) return false;
        if (!OLEPOSSUtils.replaceable(pos)) return false;

        PlaceData data = SettingUtils.getPlaceData(pos);
        if (!data.valid()) return false;
        if (!SettingUtils.inPlaceRange(data.pos())) return false;
        return !BOEntityUtils.intersectsWithEntity(Box.from(new BlockBox(pos)), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity), boxes);
    }

    private boolean validHole(Hole hole) {
        double pDist = (nearPosition.containsKey(mc.player) ? feet(nearPosition.get(mc.player)) : mc.player.getPos()).distanceTo(hole.middle);

        if (selfCheck(hole)) return false;

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player.isSpectator() || player == mc.player || player.getHealth() <= 0 || Friends.get().isFriend(player)) continue;

            if (nearCheck(player, hole, pDist)) return true;
            if (walkingCheck(player, hole)) return true;
            if (lookCheck(player, hole)) return true;
        }

        return false;
    }

    private boolean selfCheck(Hole hole) {
        if (selfNearCheck(hole)) return true;
        return selfWalking.get() && walkCheck(mc.player, hole, selfWalkMemory.get(), selfWalkingDist.get());
    }

    private boolean selfNearCheck(Hole hole) {
        BlockPos pos = new BlockPos(mc.player.getBlockX(), (int) Math.round(mc.player.getY()), mc.player.getBlockZ());

        if (iSelfHole.get() && (HoleUtils.inHole(mc.player) || OLEPOSSUtils.collidable(pos))) return false;
        if (selfAbove.get() && mc.player.getY() <= hole.middle.y) return false;

        return mc.player.getPos().distanceTo(hole.middle) <= selfDistance.get();
    }

    private boolean nearCheck(AbstractClientPlayerEntity player, Hole hole, double pDist) {
        if (!near.get()) return false;

        BlockPos pos = new BlockPos(player.getBlockX(), (int) Math.round(player.getY()), player.getBlockZ());
        if ((HoleUtils.inHole(player) || OLEPOSSUtils.collidable(pos)) && iHole.get()) return false;

        if (above.get() && player.getY() <= hole.middle.y) return false;

        double eDist = (nearPosition.containsKey(player) ? feet(nearPosition.get(player)) : player.getPos()).distanceTo(hole.middle);
        if (eDist > nearDistance.get()) return false;

        return !efficient.get() || pDist >= eDist;
    }

    private boolean walkingCheck(AbstractClientPlayerEntity player, Hole hole) {
        if (!walking.get()) return false;

        return walkCheck(player, hole, walkMemory.get(), walkingDist.get());
    }

    private boolean walkCheck(AbstractClientPlayerEntity player, Hole hole, int ticks, double dist) {
        if (walkAngles.get(player) == null) return false;

        int i = 0;
        for (Movement m : walkAngles.get(player)) {
            i++;
            if (i > ticks) break;
            if (m.movementAngle == null) continue;
            if (m.vec().distanceTo(hole.middle) > dist) continue;

            double yawToHole = RotationUtils.getYaw(m.vec(), hole.middle);
            double highestAngle = MathHelper.lerp(Math.min(player.getPos().distanceTo(hole.middle) / 8, 1), 90, 0);
            if (Math.abs(RotationUtils.yawAngle(yawToHole, m.movementAngle)) < highestAngle) return true;
        }
        return false;
    }

    private boolean lookCheck(AbstractClientPlayerEntity player, Hole hole) {
        if (!look.get()) return false;
        if (lookAngles.get(player) == null) return false;

        int i = 0;
        for (Look l : lookAngles.get(player)) {
            i++;
            if (i > lookMemory.get()) break;
            if (l.vec().distanceTo(hole.middle) > lookDist.get()) continue;

            double yawToHole = RotationUtils.getYaw(l.vec(), hole.middle);
            double highestAngle = MathHelper.lerp(Math.min(player.getPos().distanceTo(hole.middle) / 20, 1), 35, 5);
            if (Math.abs(RotationUtils.yawAngle(yawToHole, l.yaw)) < highestAngle &&
                Math.abs(RotationUtils.getPitch(l.vec, hole.middle) - l.pitch()) < highestAngle) return true;
        }
        return false;
    }

    private void updatePlaces() {
        switch (placeDelayMode.get()) {
            case Ticks -> {
                if (placesLeft >= places.get() || tickTimer >= placeDelayT.get()) {
                    placesLeft = places.get();
                    tickTimer = 0;
                }
            }
            case Seconds -> {
                if (placesLeft >= places.get() || System.currentTimeMillis() - lastTime >= placeDelayS.get() * 1000) {
                    placesLeft = places.get();
                    lastTime = System.currentTimeMillis();
                }
            }
        }
    }

    private void place(BlockPos pos) {
        if (blocksLeft <= 0) {
            return;
        }

        PlaceData data = SettingUtils.getPlaceData(pos);

        if (data == null || !data.valid()) {
            return;
        }

        placing = true;

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) return;

        if (!switched && hand == null) {
            switch (switchMode.get()) {
                case Normal, Silent -> {
                    InvUtils.swap(result.slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(result.slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(result.slot());
            }
        }

        if (!switched && hand == null) {
            return;
        }

        render.add(new Render(pos, System.currentTimeMillis()));
        timers.add(pos, delay.get());

        placeBlock(hand == null ? Hand.MAIN_HAND : hand, data.pos().toCenterPos(), data.dir(), data.pos());

        if (placeSwing.get()) clientSwing(placeHand.get(), hand == null ? Hand.MAIN_HAND : hand);

        blocksLeft--;
        placesLeft--;

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }
    }

    private Vec3d feet(Box box) {
        return new Vec3d((box.minX + box.maxX) / 2d, box.minY, (box.minZ + box.maxZ) / 2d);
    }

    private record Movement(Float movementAngle, Vec3d vec) {}
    private record Look(float yaw, float pitch, Vec3d vec) {}

    private record Render(BlockPos pos, Long time) {}

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }

    public enum LookCheckMode {

    }
}
