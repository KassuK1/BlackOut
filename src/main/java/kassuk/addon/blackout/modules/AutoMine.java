package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.globalsettings.SwingSettings;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.entity.player.PlaceBlockEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoMine extends BlackOutModule {
    public AutoMine() {super(BlackOut.BLACKOUT, "AutoMine", "For the times your too lazy or bad to press your break bind");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgValue = settings.createGroup("Value");
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgCrystal = settings.createGroup("Crystal");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<AutoMineMode> mode = sgGeneral.add(new EnumSetting.Builder<AutoMineMode>()
        .name("Mode")
        .description("Pauses when you are eating")
        .defaultValue(AutoMineMode.AutoMine)
        .build()
    );
    private final Setting<Double> civDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("CIV Delay")
        .description("Seconds between sending civ packets")
        .defaultValue(0.2)
        .range(0.1, 10)
        .sliderMax(10)
        .visible(() -> mode.get() == AutoMineMode.CIV)
        .build()
    );
    private final Setting<Integer> civLimit = sgGeneral.add(new IntSetting.Builder()
        .name("CIV Limit")
        .description("Resets civ after this many breaks (0 = doesn't reset)")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> mode.get() == AutoMineMode.CIV)
        .build()
    );
    private final Setting<Boolean> resetOnEnd = sgGeneral.add(new BoolSetting.Builder()
        .name("Reset On End")
        .description("Resets Speedmine position when ending mining.")
        .defaultValue(true)
        .visible(() -> mode.get() == AutoMineMode.SpeedMine)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> resetOnSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("Reset On Switch")
        .description("Resets mining progress when switching (useful on strict)")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> resetOnPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("Reset On Place")
        .description("Resets Speedmine position when placing a block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> resetOnCrystal = sgGeneral.add(new BoolSetting.Builder()
        .name("Reset On Crystal")
        .description("Resets Speedmine position when placing a crystal.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> resetOnAttack = sgGeneral.add(new BoolSetting.Builder()
        .name("Reset On Attack")
        .description("Resets Speedmine position when attacking an entity.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("Debug")
        .description("Sends a message in chat when the module starts and ends mining.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description(".")
        .defaultValue(SwitchMode.SilentBypass)
        .build()
    );

    //  Value Page
    private final Setting<Integer> antiSurround = sgValue.add(new IntSetting.Builder()
        .name("Anti Surround Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> surroundCev = sgValue.add(new IntSetting.Builder()
        .name("Surround Cev Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> trapCev = sgValue.add(new IntSetting.Builder()
        .name("Trap Cev Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> autoCity = sgValue.add(new IntSetting.Builder()
        .name("Auto City Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> cev = sgValue.add(new IntSetting.Builder()
        .name("Cev Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> antiBurrow = sgValue.add(new IntSetting.Builder()
        .name("Anti Burrow Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );

    //  Speed Page
    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Vanilla mining speed multiplier")
        .defaultValue(1)
        .range(0.1, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> onGroundCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("On Ground Check")
        .description("Divides mining speed by 5 when not on ground.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> waterCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("Water Check")
        .description("Divides mining speed by 5 when underwater.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> hasteCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("Haste Check")
        .description("More speed when you have haste effect.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> mfCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("Mining Fatigue Check")
        .description("Slower when you have mining fatigue.")
        .defaultValue(true)
        .build()
    );

    //  Crystal Page
    private final Setting<Boolean> oldVerPlacements = sgCrystal.add(new BoolSetting.Builder()
        .name("1.12 Placements")
        .description("Uses 1.12 crystal mechanics.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> crystal = sgCrystal.add(new BoolSetting.Builder()
        .name("Crystal")
        .description("Requires crystal to be placed to end mining.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> placeCrystal = sgCrystal.add(new BoolSetting.Builder()
        .name("Place Crystal")
        .description("Places crystal before ending the mining.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> expCrystal = sgCrystal.add(new BoolSetting.Builder()
        .name("Explode Crystal")
        .description("Explodes a crystal before ending the mining.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> placeDelay = sgCrystal.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between crystal places")
        .defaultValue(0.3)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Boolean> forceHold = sgCrystal.add(new BoolSetting.Builder()
        .name("Force Hold")
        .description("Instantly places a new crystal at the block after attacking the crystal.")
        .defaultValue(true)
        .build()
    );

    //  Render Page
    private final Setting<Double> exp = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Exponent")
        .description("3 - 4 look cool")
        .defaultValue(3)
        .range(0, 10)
        .sliderRange(1, 5)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineStartColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Start Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 0))
        .build()
    );
    private final Setting<SettingColor> lineEndColor = sgRender.add(new ColorSetting.Builder()
        .name("Line End Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> startColor = sgRender.add(new ColorSetting.Builder()
        .name("Start Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 0))
        .build()
    );
    private final Setting<SettingColor> endColor = sgRender.add(new ColorSetting.Builder()
        .name("End Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<ShapeMode> waitingShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Waiting Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> waitingLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Waiting Line Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> waitingColor = sgRender.add(new ColorSetting.Builder()
        .name("Waiting Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    public enum AutoMineMode {
        SpeedMine,
        Smart,
        AutoMine,
        CIV
    }
    public enum SwitchMode {
        Silent,
        SilentBypass
    }
    public Block lastBlock = null;
    boolean speedmining = false;
    public double progress;
    public boolean shouldRestart;
    public static BlockPos targetPos;
    public Direction targetDir;
    BlockPos crystalPos;
    boolean shouldForce = false;
    int targetValue;
    int lastValue;
    double timer = 0;
    public double miningFor;
    public boolean ignore = false;
    public BlockPos civPos = null;
    public double civTimer = 0;
    public BlockPos waitingToStart = null;
    int civTries = 0;
    boolean hold = false;

    @Override
    public void onActivate() {
        super.onActivate();
        timer = 0;
        progress = 0;
        targetPos = null;
        crystalPos = null;
        waitingToStart = null;
        targetValue = -1;
        lastValue = -1;
        speedmining = false;
        civTries = 0;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (targetPos != null && getBlock(targetPos) != Blocks.AIR) {
            SettingUtils.mineSwing(SwingSettings.MiningSwingState.Full);
            if (SettingUtils.shouldRotate(RotationType.Breaking)) {
                Managers.ROTATION.start(OLEPOSSUtils.getBox(targetPos), 9, RotationType.Breaking);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Post event) {
        if (targetPos != null && getBlock(targetPos) != Blocks.AIR) {
            SettingUtils.swing(SwingState.Post, SwingType.Mining, Hand.MAIN_HAND);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSwitch(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket && resetOnSwitch.get() && targetPos != null) {
            shouldRestart = true;
            civPos = null;
            progress = 0;
            miningFor = 0;
        }
        if (event.packet instanceof PlayerInteractBlockC2SPacket && resetOnCrystal.get() && targetPos != null) {
            shouldRestart = true;
            civPos = null;
            progress = 0;
            miningFor = 0;
        }
        if (event.packet instanceof PlayerInteractEntityC2SPacket && resetOnAttack.get() && targetPos != null) {
            shouldRestart = true;
            civPos = null;
            progress = 0;
            miningFor = 0;
        }
    }

    @EventHandler
    private void onPlace(PlaceBlockEvent event) {
        if (resetOnPlace.get() && targetPos != null) {
            shouldRestart = true;
            civPos = null;
            progress = 0;
            miningFor = 0;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerActionC2SPacket packet) {
            if (!ignore) {
                if (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                    switch (mode.get()) {
                        case SpeedMine, CIV -> {
                            if (getBlock(packet.getPos()) == Blocks.BEDROCK) {
                                event.cancel();
                            } else if (targetPos != null && packet.getPos().equals(targetPos)) {
                                reset();
                            } else {
                                event.cancel();
                                waitingToStart = packet.getPos();
                                targetPos = null;
                                targetDir = null;
                                crystalPos = null;
                                progress = 0;
                                miningFor = 0;
                                shouldRestart = false;
                            }
                        }
                        case AutoMine -> {
                            if (getBlock(packet.getPos()) == Blocks.BEDROCK && targetPos != null) {
                                event.cancel();
                            } else {
                                reset();
                            }
                        }
                        case Smart -> {
                            if (getBlock(packet.getPos()) == Blocks.BEDROCK) {
                                event.cancel();
                            } else if (targetPos != null && packet.getPos().equals(targetPos)) {
                                speedmining = false;
                                reset();
                            } else {
                                event.cancel();
                                waitingToStart = packet.getPos();
                                speedmining = true;
                                crystalPos = null;
                                targetPos = null;
                                targetDir = null;
                                progress = 0;
                                miningFor = 0;
                            }
                        }
                    }
                } else if (targetPos != null && (packet.getAction() == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK || packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK)) {
                    event.cancel();
                }
            }
            ignore = false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) {return;}

        switch (mode.get()) {
            case AutoMine -> {
                calc();
            }
            case SpeedMine, CIV -> {
                if (targetPos != null && !SettingUtils.inMineRange(targetPos)) {
                    reset();
                }
            }
            case Smart -> {
                if (speedmining) {
                    if (targetPos != null && !SettingUtils.inMineRange(targetPos)) {
                        reset();
                    }
                } else {
                    calc();
                }
            }
        }
        civTimer += event.frameTime;
        timer = Math.min(placeDelay.get(), timer + event.frameTime);

        //Render
        if (targetPos != null) {
            Vec3d v = OLEPOSSUtils.getMiddle(targetPos);
            double p = 0.5 - (Math.pow((mode.get() == AutoMineMode.CIV && civPos == targetPos ? 0 : 1 - progress), exp.get())) / 2;

            Box toRender = new Box(v.x - p, v.y - p, v.z - p, v.x + p, v.y + p, v.z + p);
            event.renderer.box(toRender,
                getColor(startColor.get(), endColor.get(), p * 2),
                getColor(lineStartColor.get(), lineEndColor.get(), p * 2),
                shapeMode.get(), 0);
        } else if (waitingToStart != null) {
            event.renderer.box(waitingToStart, waitingColor.get(), waitingLineColor.get(), waitingShapeMode.get(), 0);
        }

        if (waitingToStart != null) {
            targetDir = SettingUtils.getPlaceOnDirection(waitingToStart);
            if (targetDir != null) {
                boolean rotated = !SettingUtils.startMineRot() || Managers.ROTATION.start(waitingToStart, priority, RotationType.Breaking);
                if (rotated) {
                    if (debug.get()) {
                        debug("accept-target");
                    }
                    targetPos = waitingToStart;
                    waitingToStart = null;
                    start(targetPos, targetDir);
                }
            }
        } else if (targetPos != null) {
            targetDir = SettingUtils.getPlaceOnDirection(targetPos);
            miningFor += event.frameTime;
            if (shouldRestart) {
                boolean rotated = !SettingUtils.startMineRot() || Managers.ROTATION.start(targetPos, priority, RotationType.Breaking);
                if (rotated) {
                    start(targetPos, targetDir);
                    shouldRestart = false;
                }
            }
            if (getBlock(targetPos) != Blocks.AIR) {
                progress = Math.min(1, progress + event.frameTime * getMineTicks(targetPos) * 20);
            } else {
                progress = 0;
            }

            //Other Stuff
            if ((progress >= 1 || (mode.get() == AutoMineMode.CIV && getBlock(targetPos) != Blocks.AIR && civTimer >= civDelay.get() && civPos == targetPos)) && (!pauseEat.get() || !mc.player.isUsingItem()) && targetDir != null) {
                if (crystal.get() && crystalPos != null) {

                    Entity at = isAt(targetPos, crystalPos);
                    Hand hand = getHand(Items.END_CRYSTAL);

                    int hotbar = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).slot();
                    int inv = InvUtils.find(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).slot();

                    Direction crystalDir = SettingUtils.getPlaceOnDirection(crystalPos.down());

                    if (at != null) {
                        boolean rotated = !SettingUtils.endMineRot() || Managers.ROTATION.start(targetPos, priority, RotationType.Breaking);

                        if (rotated) {
                            if (debug.get()) {
                                debug("holding-check");
                            }
                            int holding = 0;
                            if (holdingBest(targetPos)) {
                                holding = 1;
                            } else if (BOInvUtils.invSwitch(fastestSlot(targetPos))) {
                                holding = 2;
                            }
                            if (holding == 0) {
                                return;
                            }
                            end(targetPos, targetDir);
                            if (holding == 2) {
                                BOInvUtils.swapBack();
                            }
                            if (expCrystal.get() && at instanceof EndCrystalEntity) {
                                if (debug.get()) {
                                    debug("attack");
                                }
                                attack(at);
                            }

                            // Forcehold
                            if (canForce()) {
                                if (debug.get()) {
                                    debug("force");
                                }
                                forcePlace();
                            }

                            targetPos = null;
                            crystalPos = null;
                            shouldForce = false;
                        }
                    } else if ((hand != null || (switchMode.get() == SwitchMode.Silent && hotbar > 0) || (switchMode.get() == SwitchMode.SilentBypass && inv > 0)) && timer >= placeDelay.get() && placeCrystal.get()
                        && placeCrystal.get() && !EntityUtils.intersectsWithEntity(new Box(crystalPos), entity -> !entity.isSpectator()) && crystalDir != null) {
                        boolean rotated = !SettingUtils.shouldRotate(RotationType.Crystal) || Managers.ROTATION.start(crystalPos.down(), priority, RotationType.Crystal);
                        if (rotated) {
                            timer = 0;

                            int holding = 0;
                            if (hand == null) {
                                switch (switchMode.get()) {
                                    case Silent -> {
                                        InvUtils.swap(hotbar, true);
                                        holding = 2;
                                    }
                                    case SilentBypass -> {
                                        if (BOInvUtils.invSwitch(inv)) {
                                            holding = 2;
                                        }
                                    }
                                }
                            } else {
                                holding = 1;
                            }

                            if (holding == 0) {
                                return;
                            }


                            SettingUtils.swing(SwingState.Pre, SwingType.Crystal, hand == null ? Hand.MAIN_HAND : hand);

                            if (crystalPos != null) {
                                mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand == null ? Hand.MAIN_HAND : hand,
                                    new BlockHitResult(OLEPOSSUtils.getMiddle(crystalPos.down()), crystalDir, crystalPos.down(), false), 0));
                            }

                            SettingUtils.swing(SwingState.Post, SwingType.Crystal, hand == null ? Hand.MAIN_HAND : hand);

                            if (holding == 2) {
                                switch (switchMode.get()) {
                                    case Silent -> InvUtils.swapBack();
                                    case SilentBypass -> BOInvUtils.swapBack();
                                }
                            }

                            if (SettingUtils.shouldRotate(RotationType.Crystal)) {
                                Managers.ROTATION.end(crystalPos.down());
                            }
                        }
                    }
                } else {
                    boolean rotated = !SettingUtils.endMineRot() || Managers.ROTATION.start(targetPos, priority, RotationType.Breaking);

                    if (rotated) {
                        int holding = 0;
                        if (holdingBest(targetPos)) {
                            holding = 1;
                        } else {
                            switch (switchMode.get()) {
                                case Silent -> {
                                    int s = InvUtils.findFastestTool(mc.world.getBlockState(targetPos)).slot();
                                    if (s >= 0) {
                                        InvUtils.swap(s, true);
                                        holding = 2;
                                    }
                                }
                                case SilentBypass -> {
                                    if (BOInvUtils.invSwitch(fastestSlot(targetPos))) {
                                        holding = 2;
                                    }
                                }
                            }
                        }
                        if (holding == 0) {
                            return;
                        }

                        end(targetPos, targetDir);

                        civTimer = 0;
                        progress = 0;
                        miningFor = 0;

                        if (holding == 2) {
                            switch (switchMode.get()) {
                                case Silent -> InvUtils.swapBack();
                                case SilentBypass -> BOInvUtils.swapBack();
                            }
                        }
                        if (mode.get() == AutoMineMode.SpeedMine) {
                            if (resetOnEnd.get()) {
                                targetPos = null;
                            } else {
                                shouldRestart = true;
                            }
                        }
                        if (mode.get() == AutoMineMode.Smart && speedmining) {
                            speedmining = false;
                            targetPos = null;
                            shouldForce = false;
                        }
                    }
                }
            }
        }
    }
    boolean canForce() {
        if (!shouldForce || !forceHold.get()) {return false;}
        if (getBlock(targetPos.down()) == Blocks.AIR) {return false;}
        if (oldVerPlacements.get() && getBlock(targetPos.up()) != Blocks.AIR) {return false;}

        return !EntityUtils.intersectsWithEntity(new Box(targetPos), entity -> !entity.isSpectator() && !(entity instanceof EndCrystalEntity));
    }

    void forcePlace() {
        Hand hand = getHand(Items.END_CRYSTAL);

        int hotbar = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).slot();
        int inv = InvUtils.find(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).slot();

        int holding = 0;
        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> {
                    InvUtils.swap(hotbar, true);
                    holding = 2;
                }
                case SilentBypass -> {
                    if (BOInvUtils.invSwitch(inv)) {
                        holding = 2;
                    }
                }
            }
        } else {
            holding = 1;
        }

        if (holding == 0) {
            return;
        }


        SettingUtils.swing(SwingState.Pre, SwingType.Crystal, hand == null ? Hand.MAIN_HAND : hand);

        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand == null ? Hand.MAIN_HAND : hand,
            new BlockHitResult(OLEPOSSUtils.getMiddle(targetPos.down()), Direction.UP, targetPos.down(), false), 0));

        SettingUtils.swing(SwingState.Post, SwingType.Crystal, hand == null ? Hand.MAIN_HAND : hand);

        if (holding == 2) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case SilentBypass -> BOInvUtils.swapBack();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(PacketEvent.Receive event) {
        if (event.packet instanceof BlockUpdateS2CPacket packet) {
            if (mc.player != null && mc.world != null && targetPos != null) {
                if (packet.getPos().equals(targetPos)) {
                    if (packet.getState().getBlock() != Blocks.AIR && lastBlock == Blocks.AIR) {
                        if (mode.get() == AutoMineMode.SpeedMine) {
                            shouldRestart = true;
                        }
                        progress = 0;
                    }
                    lastBlock = getBlock(packet.getPos());
                }
            }
        }
    }

    void attack(Entity en) {
        SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);

        SettingUtils.registerAttack(en.getBoundingBox());
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));

        SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);

        if (SettingUtils.shouldRotate(RotationType.Attacking)) {
            Managers.ROTATION.end(en.getBoundingBox());
        }
    }

    Entity isAt(BlockPos pos, BlockPos crystalPos) {
        for (Entity en : mc.world.getEntities()) {
            if ((en.getBlockPos().equals(crystalPos) && en.getType().equals(EntityType.END_CRYSTAL)) ||
                (!(en instanceof ItemEntity) &&  en.getBoundingBox().intersects(OLEPOSSUtils.getBox(pos)))) {
                return en;
            }
        }
        return null;
    }

    void calc() {
        lastValue = targetValue;
        BlockPos[] pos = getPos();
        BlockPos pPos = pos[0];
        BlockPos cPos = pos[1];

        boolean valid = targetPos == null || getBlock(targetPos) == Blocks.AIR || !SettingUtils.inMineRange(targetPos);

        Direction dir = SettingUtils.getPlaceOnDirection(pPos);

        if (crystalPos != null && (!OLEPOSSUtils.isCrystalBlock(getBlock(crystalPos.down())) || !SettingUtils.inPlaceRange(crystalPos.down()) || !canPlaceCrystal(crystalPos))) {
            valid = true;
        }
        if (pPos != null && SettingUtils.inMineRange(pPos) && dir != null) {
            if (valid) {
                progress = 0;
                miningFor = 0;
                targetPos = null;
                waitingToStart = pPos;
                crystalPos = cPos;
                shouldForce = hold;
            }
        } else {
            waitingToStart = null;
            targetPos = null;
            lastValue = -1;
            targetValue = -1;
        }
    }

    void reset() {
        targetPos = null;
        lastValue = -1;
        targetValue = -1;
        shouldRestart = false;
        civPos = null;
        waitingToStart = null;
    }

    BlockPos[] getPos() {
        int value = 0;
        BlockPos closest = null;
        BlockPos crystal = null;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl)) {
                BlockPos pos = pl.getBlockPos();
                for (Direction dir : OLEPOSSUtils.horizontals) {
                    // Anti Surround
                    if (valueCheck(value, antiSurround.get(), pos.offset(dir), closest)
                        && getBlock(pos.offset(dir)) != Blocks.AIR && getBlock(pos.offset(dir)) != Blocks.BEDROCK && SettingUtils.inMineRange(pos.offset(dir)) &&
                        SettingUtils.getPlaceOnDirection(pos.offset(dir)) != null) {
                        value = antiSurround.get();
                        closest = pos.offset(dir);
                        crystal = null;
                        hold = false;
                    }

                    // Surround Cev
                    if (valueCheck(value, surroundCev.get(), pos.offset(dir), closest) &&
                        getBlock(pos.offset(dir)) == Blocks.OBSIDIAN && canPlaceCrystal(pos.offset(dir).up())
                        && SettingUtils.inMineRange(pos.offset(dir)) && SettingUtils.inPlaceRange(pos.offset(dir)) &&
                        SettingUtils.getPlaceOnDirection(pos.offset(dir)) != null) {
                        value = surroundCev.get();
                        closest = pos.offset(dir);
                        crystal = pos.offset(dir).up();
                        hold = true;
                    }
                    // Trap Cev
                    if (valueCheck(value, trapCev.get(), pos.offset(dir).up(), closest) &&
                        getBlock(pos.offset(dir).up()) == Blocks.OBSIDIAN && canPlaceCrystal(pos.offset(dir).up(2)) &&
                        SettingUtils.inMineRange(pos.offset(dir).up()) && SettingUtils.inPlaceRange(pos.offset(dir).up()) &&
                        SettingUtils.getPlaceOnDirection(pos.offset(dir).up()) != null) {
                        value = trapCev.get();
                        closest = pos.offset(dir).up();
                        crystal = pos.offset(dir).up(2);
                        hold = false;
                    }
                    // Auto City
                    if (valueCheck(value, autoCity.get(), pos.offset(dir), closest) &&
                        getBlock(pos.offset(dir)) != Blocks.AIR && getBlock(pos.offset(dir)) != Blocks.BEDROCK && canPlaceCrystal(pos.offset(dir).offset(dir)) &&
                        OLEPOSSUtils.isCrystalBlock(getBlock(pos.offset(dir).offset(dir).down())) && SettingUtils.inMineRange(pos.offset(dir)) &&
                        SettingUtils.inPlaceRange(pos.offset(dir).offset(dir).down()) &&
                        SettingUtils.getPlaceOnDirection(pos.offset(dir)) != null) {
                        value = autoCity.get();
                        closest = pos.offset(dir);
                        crystal = pos.offset(dir).offset(dir);
                        hold = true;
                    }
                    // Cev
                    if (valueCheck(value, cev.get(), pos.up(2), closest)
                        && getBlock(pos.up(2)) == Blocks.OBSIDIAN && canPlaceCrystal(pos.up(3)) &&
                        SettingUtils.inMineRange(pos.up(2)) && SettingUtils.inPlaceRange(pos.up(2)) &&
                        SettingUtils.getPlaceOnDirection(pos.up(2)) != null) {
                        value = cev.get();
                        closest = pos.up(2);
                        crystal = pos.up(3);
                        hold = false;
                    }
                    // Anti Burrow
                    if (valueCheck(value, antiBurrow.get(), pos, closest)
                        && getBlock(pos) != Blocks.AIR && getBlock(pos) != Blocks.BEDROCK && SettingUtils.inMineRange(pos) &&
                        SettingUtils.getPlaceOnDirection(pos) != null) {
                        value = antiBurrow.get();
                        closest = pl.getBlockPos();
                        crystal = null;
                        hold = false;
                    }
                }
            }
        }
        targetValue = value;
        return new BlockPos[] {closest, crystal};
    }

    boolean valueCheck(int currentValue, int value, BlockPos pos, BlockPos closest) {
        if (value == 0) {return false;}
        boolean rur;
        if (closest == null) {
            rur = true;
        } else {
            rur = OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(pos), mc.player.getEyePos()) <
                OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(closest), mc.player.getEyePos());
        }
        return ((currentValue <= value && rur) || currentValue < value) && SettingUtils.inPlaceRange(pos);
    }

    Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    Hand getHand(Item item) {
        if (mc.player.getOffHandStack().getItem() == item) {
            return Hand.OFF_HAND;
        } else if (Managers.HOLDING.isHolding(Items.END_CRYSTAL)) {
            return Hand.MAIN_HAND;
        }
        return null;
    }


    boolean holdingBest(BlockPos pos) {
        return miningFor >= getItemMineTicks(pos, Managers.HOLDING.getSlot()) / 20;
    }


    void start(BlockPos pos, Direction dir) {
        ignore = true;
        lastBlock = getBlock(pos);

        civPos = null;
        civTries = 0;
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
            pos, dir));

        SettingUtils.mineSwing(SwingSettings.MiningSwingState.Start);
        if (debug.get()) {
            debug("start");
        }

        if (SettingUtils.startMineRot()) {
            Managers.ROTATION.end(pos);
        }
    }

    void abort(BlockPos pos) {
        ignore = true;
        civPos = null;
        lastBlock = getBlock(pos);

        if (debug.get()) {
            debug("abort");
        }

        civTries++;
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
            pos, Direction.UP));

        if (civLimit.get() != 0 && civTries >= civLimit.get()) {
            shouldRestart = true;
        }

        if (SettingUtils.startMineRot()) {
            Managers.ROTATION.end(pos);
        }
    }

    void end(BlockPos pos, Direction dir) {
        ignore = true;
        if (pos != null) {

            if (debug.get()) {
                debug("end");
            }

            if (mode.get() == AutoMineMode.CIV) {
                if (civPos != null) {
                    civTries++;
                    if (civLimit.get() != 0 && civTries >= civLimit.get()) {
                        shouldRestart = true;
                    } else {
                        civPos = pos;
                    }
                } else {
                    civPos = pos;
                }
            }
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                pos, dir));

            SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);

            if (SettingUtils.endMineRot()) {
                Managers.ROTATION.end(pos);
            }
        }
    }

    float getTime(BlockPos pos, int slot) {
        BlockState state = mc.world.getBlockState(pos);
        float f = state.getHardness(mc.world, pos);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = !state.isToolRequired() || mc.player.getInventory().getStack(slot).isSuitableFor(state) ? 30 : 100;
            return getSpeed(state, slot) / f / (float) i;
        }
    }

    float getMineTicks(BlockPos pos) {
        int fastest = fastestSlot(pos);
        if (fastest != -1) {
            return (float) (getTime(pos ,fastest) * speed.get());
        }
        return -1;
    }
    float getItemMineTicks(BlockPos pos, int slot) {
        return (float) (1 / getTime(pos, slot) * speed.get());
    }
    float getSpeed(BlockState state, int slot) {
        ItemStack stack = mc.player.getInventory().getStack(slot);
        float f = mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
        if (f > 1.0) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (i > 0 && !stack.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }

        if (hasteCheck.get() && StatusEffectUtil.hasHaste(mc.player)) {
            f *= 1.0 + (float)(StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2F;
        }

        if (mfCheck.get() && mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            f *= Math.pow(0.3, mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier() + 1);
        }

        if (waterCheck.get() && mc.player.isSubmergedInWater() && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            f /= 5.0;
        }

        if (onGroundCheck.get() && !mc.player.isOnGround()) {
            f /= 5.0;
        }

        return f;
    }

    int fastestSlot(BlockPos pos) {
        int slot = -1;
        if (mc.player == null || mc.world == null) {return -1;}
        for (int i = 0; i < 35; i++) {
            if (slot == -1 || (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(pos)) >
                mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(mc.world.getBlockState(pos)))) {
                slot = i;
            }
        }
        return slot;
    }

    Color getColor(Color start, Color end, double progress) {
        double r = (end.r - start.r) * progress;
        double g = (end.g - start.g) * progress;
        double b = (end.b - start.b) * progress;
        double a = (end.a - start.a) * progress;
        return new Color((int) Math.round(start.r + r),
            (int) Math.round(start.g + g),
            (int) Math.round(start.b + b),
            (int) Math.round(start.a + a));
    }

    boolean canPlaceCrystal(BlockPos pos) {
        return getBlock(pos) == Blocks.AIR && (!oldVerPlacements.get() || getBlock(pos.up()) == Blocks.AIR);
    }
}
