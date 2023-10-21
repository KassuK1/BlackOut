package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.HoleType;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.*;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author OLEPOSSU
 */

public class PistonPush extends BlackOutModule {
    public PistonPush() {
        super(BlackOut.BLACKOUT, "Piston Push", "Pushes people out of their safe holes.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDelay = settings.createGroup("Delay");
    private final SettingGroup sgSwing = settings.createGroup("Swing");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> pauseEat = addPauseEat(sgGeneral);
    private final Setting<Redstone> redstone = sgGeneral.add(new EnumSetting.Builder<Redstone>()
        .name("Redstone")
        .description("What kind of redstone to use.")
        .defaultValue(Redstone.Torch)
        .build()
    );
    private final Setting<Boolean> onlyHole = sgSwing.add(new BoolSetting.Builder()
        .name("Only Hole")
        .description("Toggles when enemy moves.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> toggleMove = sgSwing.add(new BoolSetting.Builder()
        .name("Toggle Move")
        .description("Toggles when enemy moves.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwitchMode> pistonSwitch = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Piston Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SwitchMode> redstoneSwitch = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Redstone Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );

    //--------------------Delay--------------------//
    private final Setting<Double> prDelay = sgDelay.add(new DoubleSetting.Builder()
        .name("Piston > Redstone")
        .description("How many seconds to wait between placing piston and redstone.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> rmDelay = sgDelay.add(new DoubleSetting.Builder()
        .name("Redstone > Mine")
        .description("How many seconds to wait between placing redstone and starting to mine it.")
        .defaultValue(0.2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> mpDelay = sgDelay.add(new DoubleSetting.Builder()
        .name("Mine > Piston")
        .description("How many seconds to wait after mining the redstone before starting a new cycle.")
        .defaultValue(0.2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Swing--------------------//
    private final Setting<Boolean> pistonSwing = sgSwing.add(new BoolSetting.Builder()
        .name("Piston Swing")
        .description("Renders swing animation when placing a piston.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> pistonHand = sgSwing.add(new EnumSetting.Builder<SwingHand>()
        .name("Piston Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(pistonSwing::get)
        .build()
    );
    private final Setting<Boolean> redstoneSwing = sgSwing.add(new BoolSetting.Builder()
        .name("Piston Swing")
        .description("Renders swing animation when placing redstone.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> redstoneHand = sgSwing.add(new EnumSetting.Builder<SwingHand>()
        .name("Redstone Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(redstoneSwing::get)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<ShapeMode> pistonShape = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Piston Shape Mode")
        .description("Which parts should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> psColor = sgRender.add(new ColorSetting.Builder()
        .name("Piston Side Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    private final Setting<SettingColor> plColor = sgRender.add(new ColorSetting.Builder()
        .name("Piston Line Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<ShapeMode> redstoneShape = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Redstone Shape Mode")
        .description("Which parts should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> rsColor = sgRender.add(new ColorSetting.Builder()
        .name("Redstone Side Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<SettingColor> rlColor = sgRender.add(new ColorSetting.Builder()
        .name("Redstone Line Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    private long pistonTime = 0;
    private long redstoneTime = 0;
    private long mineTime = 0;
    private boolean minedThisTick = false;

    private boolean pistonPlaced = false;
    private boolean redstonePlaced = false;
    private boolean mined = false;

    private BlockPos pistonPos = null;
    private BlockPos redstonePos = null;
    private Direction pistonDir = null;
    private PlaceData pistonData = null;
    private PlaceData redstoneData = null;

    private BlockPos lastPiston = null;
    private BlockPos lastRedstone = null;
    private Direction lastDirection = null;

    private BlockPos startPos = null;
    private BlockPos currentPos = null;

    @Override
    public void onActivate() {
        lastPiston = null;
        lastRedstone = null;
        lastDirection = null;
        startPos = null;
        redstonePlaced = false;
        pistonPlaced = false;
        mined = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        minedThisTick = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (startPos != null && toggleMove.get()) {
            if (!startPos.equals(currentPos)) {
                toggle();
                sendToggledMsg("enemy moved");
                return;
            }
        }

        update();

        if (pistonPos == null) {
            lastPiston = null;
            lastRedstone = redstonePos;
            lastDirection = pistonDir;
            return;
        }

        event.renderer.box(getBox(pistonPos), psColor.get(), plColor.get(), pistonShape.get(), 0);
        event.renderer.box(getBox(redstonePos), rsColor.get(), rlColor.get(), redstoneShape.get(), 0);

        if ((System.currentTimeMillis() - mineTime > mpDelay.get() * 1000 && redstonePlaced && pistonPlaced && mined) || !pistonPos.equals(lastPiston) || !redstonePos.equals(lastRedstone) || !pistonDir.equals(lastDirection)) {
            redstonePlaced = false;
            pistonPlaced = false;
            mined = false;
        }

        lastPiston = pistonPos;
        lastRedstone = redstonePos;
        lastDirection = pistonDir;

        if (pauseEat.get() && mc.player.isUsingItem()) return;

        placePiston();
        placeRedstone();
        mineUpdate();
    }

    private void placePiston() {
        if (pistonPlaced) return;

        Hand hand = getHand(Items.PISTON);
        boolean available = hand != null;

        if (!available) {
            switch (pistonSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(Items.PISTON).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(Items.PISTON).found();
            }
        }

        if (!available) {
            return;
        }

        if (!mc.player.isOnGround()) return;
        if (EntityUtils.intersectsWithEntity(Box.from(new BlockBox(pistonPos)), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) return;
        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(pistonData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "piston"))) return;
        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(pistonDir.asRotation(), Managers.ROTATION.lastDir[1], Managers.ON_GROUND.isOnGround()));

        boolean switched = false;

        if (hand == null) {
            switch (pistonSwitch.get()) {
                case Silent -> {
                    InvUtils.swap(InvUtils.findInHotbar(Items.PISTON).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.find(Items.PISTON).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(Items.PISTON).slot());
            }
        }

        if (hand == null && !switched) {
            return;
        }

        hand = hand == null ? Hand.MAIN_HAND : hand;

        placeBlock(hand, pistonData.pos().toCenterPos(), pistonData.dir(), pistonData.pos());

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "piston"));
        pistonTime = System.currentTimeMillis();
        pistonPlaced = true;

        if (pistonSwing.get()) clientSwing(pistonHand.get(), hand);

        if (switched) {
            switch (pistonSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
    }

    private void placeRedstone() {
        if (!pistonPlaced || redstonePlaced) return;
        if (System.currentTimeMillis() - pistonTime < prDelay.get() * 1000) return;

        Hand hand = getHand(redstone.get().i);
        boolean available = hand != null;

        if (!available) {
            switch (redstoneSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(redstone.get().i).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(redstone.get().i).found();
            }
        }

        if (!available) {
            return;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(redstoneData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "redstone"))) return;

        boolean switched = false;

        if (hand == null) {
            switch (redstoneSwitch.get()) {
                case Silent -> {
                    InvUtils.swap(InvUtils.findInHotbar(redstone.get().i).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.find(redstone.get().i).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(redstone.get().i).slot());
            }
        }

        if (hand == null && !switched) {
            return;
        }

        hand = hand == null ? Hand.MAIN_HAND : hand;

        placeBlock(hand, redstoneData.pos().toCenterPos(), redstoneData.dir(), redstoneData.pos());

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "redstone"));
        redstonePlaced = true;
        redstoneTime = System.currentTimeMillis();

        if (redstoneSwing.get()) clientSwing(redstoneHand.get(), hand);

        if (switched) {
            switch (redstoneSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
    }

    private Box getBox(BlockPos pos) {
        return new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    private void mineUpdate() {
        if (!pistonPlaced || !redstonePlaced) return;
        if (minedThisTick) return;
        if (System.currentTimeMillis() - redstoneTime < rmDelay.get() * 1000) return;

        if (redstonePos == null) {
            return;
        }

        if (redstone.get() == Redstone.Torch && !(mc.world.getBlockState(redstonePos).getBlock() instanceof RedstoneTorchBlock)) {
            return;
        }
        if (redstone.get() == Redstone.Block && mc.world.getBlockState(redstonePos).getBlock() != Blocks.REDSTONE_BLOCK) {
            return;
        }

        if (Modules.get().isActive(AutoMine.class) && redstonePos.equals(Modules.get().get(AutoMine.class).targetPos())) {
            return;
        }

        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (autoMine.isActive()) {
            if (redstonePos.equals(autoMine.targetPos())) return;

            autoMine.onStart(redstonePos);
        } else {
            Direction mineDir = SettingUtils.getPlaceOnDirection(redstonePos);

            if (mineDir != null) {
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, redstonePos, mineDir));
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, redstonePos, mineDir));
            }
        }

        if (!mined) mineTime = System.currentTimeMillis();

        mined = true;
        minedThisTick = true;
    }

    private void update() {
        pistonPos = null;

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {

            if (Friends.get().isFriend(player)) continue;
            if (player == mc.player) continue;
            if (mc.player.distanceTo(player) > 10) continue;
            if (player.getHealth() <= 0) continue;
            if (player.isSpectator()) continue;
            if (!OLEPOSSUtils.solid2(player.getBlockPos()) && onlyHole.get() && HoleUtils.getHole(player.getBlockPos(), true, true, false, 1, true).type == HoleType.NotHole) return;

            updatePos(player);
            if (pistonPos != null) return;
        }
    }

    private void updatePos(PlayerEntity player) {
        BlockPos eyePos = BlockPos.ofFloored(player.getEyePos());

        if (OLEPOSSUtils.solid2(eyePos.up())) return;

        for (Direction dir : Direction.Type.HORIZONTAL.stream().sorted(Comparator.comparingDouble(d -> eyePos.offset(d).toCenterPos().distanceTo(mc.player.getEyePos()))).toList()) {
            resetPos();

            BlockPos pos = eyePos.offset(dir);
            if (!upCheck(pos)) continue;

            if (!OLEPOSSUtils.replaceable(pos) && !(mc.world.getBlockState(pos).getBlock() instanceof PistonBlock) && mc.world.getBlockState(pos).getBlock() != Blocks.MOVING_PISTON) continue;
            if (OLEPOSSUtils.solid2(eyePos.offset(dir.getOpposite()))) continue;
            if (OLEPOSSUtils.solid2(eyePos.offset(dir.getOpposite()).up())) continue;
            if (!OLEPOSSUtils.solid2(eyePos.offset(dir.getOpposite()).down())) continue;

            PlaceData data = SettingUtils.getPlaceData(pos);
            if (data == null || !data.valid()) continue;

            pistonData = data;
            pistonDir = dir;
            updateRedstone(pos);

            if (redstonePos == null) continue;

            if (startPos == null) {
                startPos = player.getBlockPos();
            }
            currentPos = player.getBlockPos();
            pistonPos = pos;
            return;
        }
    }

    private void updateRedstone(BlockPos pos) {
        if (redstone.get() == Redstone.Torch) {
            for (Direction direction : Arrays.stream(Direction.values()).sorted(Comparator.comparingDouble(i -> pos.offset(i).toCenterPos().distanceTo(mc.player.getEyePos()))).toList()) {
                if (direction == pistonDir.getOpposite() || direction == Direction.DOWN || direction == Direction.UP) continue;

                BlockPos position = pos.offset(direction);

                if (!OLEPOSSUtils.replaceable(position) && !(mc.world.getBlockState(position).getBlock() instanceof RedstoneTorchBlock)) {
                    continue;
                }

                redstoneData = SettingUtils.getPlaceDataAND(position, d -> {
                    if (d == Direction.UP && !OLEPOSSUtils.solid(position.down())) {
                        return false;
                    }
                    return direction != d.getOpposite();
                }, b -> {
                    if (pos.equals(b)) {
                        return false;
                    }
                    if (mc.world.getBlockState(b).getBlock() instanceof TorchBlock) {
                        return false;
                    }
                    return !(mc.world.getBlockState(b).getBlock() instanceof PistonBlock) && !(mc.world.getBlockState(b).getBlock() instanceof PistonHeadBlock);
                });

                if (redstoneData.valid() && SettingUtils.inPlaceRange(redstoneData.pos()) && SettingUtils.inMineRange(position)) {
                    redstonePos = position;
                    return;
                }
            }
            redstonePos = null;
            return;
        }

        for (Direction direction : Arrays.stream(Direction.values()).sorted(Comparator.comparingDouble(i -> pos.offset(i).toCenterPos().distanceTo(mc.player.getEyePos()))).toList()) {
            if (direction == pistonDir.getOpposite() || direction == Direction.DOWN) {
                continue;
            }

            BlockPos position = pos.offset(direction);

            if (!OLEPOSSUtils.replaceable(position) && mc.world.getBlockState(position).getBlock() != Blocks.REDSTONE_BLOCK) {
                continue;
            }
            if (EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position)), entity -> !entity.isSpectator() && entity instanceof PlayerEntity)) {
                continue;
            }

            redstoneData = SettingUtils.getPlaceDataOR(position, pos::equals);

            if (redstoneData.valid()) {
                redstonePos = position;
                return;
            }
        }
        redstonePos = null;
    }

    private boolean upCheck(BlockPos pos) {
        double dx = mc.player.getEyePos().x - pos.getX() - 0.5;
        double dz = mc.player.getEyePos().z - pos.getZ() - 0.5;


        return Math.sqrt(dx * dx + dz * dz) > Math.abs(mc.player.getEyePos().y - pos.getY() - 0.5);
    }

    private boolean isRedstone(BlockPos pos) {
        return mc.world.getBlockState(pos).emitsRedstonePower();
    }

    private boolean blocked(BlockPos pos) {
        Block b = mc.world.getBlockState(pos).getBlock();
        if (b == Blocks.MOVING_PISTON) {
            return false;
        }
        if (b == Blocks.PISTON_HEAD) {
            return false;
        }
        if (b == Blocks.REDSTONE_TORCH) {
            return false;
        }
        if (b instanceof FireBlock) {
            return false;
        }

        return !(mc.world.getBlockState(pos).getBlock() instanceof AirBlock);
    }

    private Hand getHand(Item item) {
        return Managers.HOLDING.isHolding(item) ? Hand.MAIN_HAND :
            mc.player.getOffHandStack().getItem() == item ? Hand.OFF_HAND :
                null;
    }

    private void resetPos() {
        pistonPos = null;
        redstonePos = null;

        pistonDir = null;
        pistonData = null;
        redstoneData = null;
    }

    public enum SwitchMode {
        Disabled,
        Silent,
        PickSilent,
        InvSwitch
    }


    public enum Redstone {
        Torch(Items.REDSTONE_TORCH, Blocks.REDSTONE_TORCH),
        Block(Items.REDSTONE_BLOCK, Blocks.REDSTONE_BLOCK);

        public final Item i;
        public final Block b;

        Redstone(Item i, Block b) {
            this.i = i;
            this.b = b;
        }
    }
}
