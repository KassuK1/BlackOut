package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
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
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author OLEPOSSU
 */

public class PistonCrystal extends BlackOutModule {
    public PistonCrystal() {
        super(BlackOut.BLACKOUT, "Piston Crystal", "Pushes crystals into your enemies to deal massive damage.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSwitch = settings.createGroup("Switch");

    //--------------------General--------------------//
    private final Setting<Boolean> pauseEat = addPauseEat(sgGeneral);
    private final Setting<Boolean> oldVer = sgGeneral.add(new BoolSetting.Builder()
        .name("1.12 Crystals")
        .description("Crystals require 2 blocks tall space to be placed.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> fire = sgGeneral.add(new BoolSetting.Builder()
        .name("Fire")
        .description("Uses fire to blow up the crystal.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Redstone> redstone = sgGeneral.add(new EnumSetting.Builder<Redstone>()
        .name("Redstone")
        .description("What kind of redstone to use.")
        .defaultValue(Redstone.Torch)
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description(".")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> alwaysAttack = sgGeneral.add(new BoolSetting.Builder()
        .name("Always Attack")
        .description("Attacks all crystals blocking crystal placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> attackSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Attack Speed")
        .description("How many times to attack the crystal every second.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> attackDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Attack Delay")
        .description("Waits for x seconds after placing redstone before attacking the crystal.")
        .defaultValue(0.1)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    //--------------------Switch--------------------//
    private final Setting<SwitchMode> crystalSwitch = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Crystal Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SwitchMode> pistonSwitch = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Piston Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SwitchMode> redstoneSwitch = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Redstone Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SwitchMode> fireSwitch = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Fire Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );

    private long lastAttack = 0;
    private long lastCrystal = 0;
    private long lastPiston = 0;
    private long lastFire = 0;
    private long lastRedstone = 0;

    private BlockPos crystalPos = null;
    private BlockPos pistonPos = null;
    private BlockPos firePos = null;
    private BlockPos redstonePos = null;

    private Direction pistonDir = null;
    private PlaceData pistonData = null;
    private Direction crystalPlaceDir = null;
    private Direction crystalDir = null;
    private PlaceData redstoneData = null;

    private PlayerEntity targetPlayer = null;

    private int ticksleft = 0;
    private int ticksBroken = 0;

    private long redstoneTime = 0;

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        ticksleft--;
        ticksBroken++;
        mineUpdate();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) {return;}

        updatePos();

        if (crystalPos != null) {
            event.renderer.box(crystalPos, new Color(255, 0, 255, 50), null, ShapeMode.Sides, 0);
            event.renderer.box(pistonPos, new Color(255, 255, 0, 50), null, ShapeMode.Sides, 0);
            event.renderer.box(redstonePos, new Color(255, 0, 0, 50), null, ShapeMode.Sides, 0);
        }

        if (crystalPos == null) {return;}

        updateAttack();
        updatePiston();
        updateFire();
        updateCrystal();

        updateRedstone();
    }

    private void mineUpdate() {
        if (redstonePos == null) {return;}

        if (mc.world.getBlockState(redstonePos).getBlock() != redstone.get().b) {return;}

        if (Modules.get().isActive(AutoMine.class) && redstonePos.equals(Modules.get().get(AutoMine.class).targetPos())) {return;}

        Direction mineDir = SettingUtils.getPlaceOnDirection(redstonePos);
        if (mineDir != null) {
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, redstonePos, mineDir));
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, redstonePos, mineDir));
        }
        ticksBroken = 0;
    }

    private void updateAttack() {
        EndCrystalEntity crystal = null;
        double cd = 10000;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity c)) {continue;}
            if (c.getX() == crystalPos.getX() + 0.5 && c.getZ() == crystalPos.getZ() + 0.5) {continue;}
            if ((!alwaysAttack.get() && (c.getX() - c.getBlockX() == 0.5 && c.getZ() - c.getBlockZ() == 0.5)) || !c.getBoundingBox().intersects(OLEPOSSUtils.getBox(crystalPos).withMaxY(crystalPos.getY() + 1))) {continue;}

            double d = OLEPOSSUtils.distance(mc.player.getEyePos(), c.getPos());

            if (d < cd) {
                cd = d;
                crystal = c;
            }
        }

        if (crystal == null) {return;}

        if (SettingUtils.shouldRotate(RotationType.Attacking) && !Managers.ROTATION.start(crystal.getBoundingBox(), priority - 0.1, RotationType.Attacking)) {return;}

        if (System.currentTimeMillis() - redstoneTime < attackDelay.get() * 1000) {return;}
        if (System.currentTimeMillis() - lastAttack < 1000 / attackSpeed.get()) {return;}
        if (pauseEat.get() && mc.player.isUsingItem()) {return;}

        SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
        sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);

        lastAttack = System.currentTimeMillis();
        return;
    }

    private void updatePiston() {
        if (System.currentTimeMillis() - lastPiston < 1000 / speed.get()) {return;}
        if (pauseEat.get() && mc.player.isUsingItem()) {return;}

        if (pistonData == null) {return;}

        Hand hand = getHand(Items.PISTON);
        boolean available = hand != null;

        if (!available) {
            switch (pistonSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(Items.PISTON).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(Items.PISTON).found();
            }
        }

        if (!available) {return;}

        if (SettingUtils.shouldRotate(RotationType.Placing) && !Managers.ROTATION.start(pistonData.pos(), priority, RotationType.Placing)) {return;}

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

        if (hand == null && !switched) {return;}

        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(pistonDir.getOpposite().asRotation(), Managers.ROTATION.lastDir[1], Managers.ONGROUND.isOnGround()));

        hand = hand == null ? Hand.MAIN_HAND : hand;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, Hand.MAIN_HAND);
        sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(OLEPOSSUtils.getMiddle(pistonData.pos()), pistonData.dir(), pistonData.pos(), false), 0));
        SettingUtils.swing(SwingState.Post, SwingType.Placing, Hand.MAIN_HAND);

        lastPiston = System.currentTimeMillis();

        if (switched) {
            switch (pistonSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }

        return;
    }

    private void updateCrystal() {
        if (System.currentTimeMillis() - lastCrystal < 1000 / speed.get()) {return;}
        if (pauseEat.get() && mc.player.isUsingItem()) {return;}

        if (crystalPlaceDir == null) {return;}
        if (EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(crystalPos), entity -> !entity.isSpectator() && !(entity instanceof EndCrystalEntity))) {return;}

        Hand hand = getHand(Items.END_CRYSTAL);
        boolean available = hand != null;

        if (!available) {
            switch (crystalSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(Items.END_CRYSTAL).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(Items.END_CRYSTAL).found();
            }
        }

        if (!available) {return;}

        if (SettingUtils.shouldRotate(RotationType.Crystal) && !Managers.ROTATION.start(crystalPos.down(), priority, RotationType.Crystal)) {return;}

        boolean switched = false;

        if (hand == null) {
            switch (crystalSwitch.get()) {
                case Silent -> {
                    InvUtils.swap(InvUtils.findInHotbar(Items.END_CRYSTAL).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.find(Items.END_CRYSTAL).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(Items.END_CRYSTAL).slot());
            }
        }

        if (hand == null && !switched) {return;}

        hand = hand == null ? Hand.MAIN_HAND : hand;

        SettingUtils.swing(SwingState.Pre, SwingType.Crystal, Hand.MAIN_HAND);
        sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(OLEPOSSUtils.getMiddle(crystalPos.down()), crystalPlaceDir, crystalPos.down(), false), 0));
        SettingUtils.swing(SwingState.Post, SwingType.Crystal, Hand.MAIN_HAND);

        ticksleft = 2;
        lastCrystal = System.currentTimeMillis();

        if (switched) {
            switch (crystalSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }

        return;
    }

    private void updateRedstone() {
        Entity crystal = crystalAt();

        if (crystal == null) {return;}
        if (ticksBroken <= 1) {return;}
        if (ticksleft > 0) {return;}
        if (System.currentTimeMillis() - lastRedstone < 1000 / speed.get()) {return;}
        if (pauseEat.get() && mc.player.isUsingItem()) {return;}

        if (redstoneData == null) {return;}

        Hand hand = getHand(redstone.get().i);
        boolean available = hand != null;

        if (!available) {
            switch (redstoneSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(redstone.get().i).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(redstone.get().i).found();
            }
        }

        if (!available) {return;}

        if (SettingUtils.shouldRotate(RotationType.Placing) && !Managers.ROTATION.start(redstoneData.pos(), priority, RotationType.Placing)) {return;}

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

        if (hand == null && !switched) {return;}

        hand = hand == null ? Hand.MAIN_HAND : hand;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
        sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(OLEPOSSUtils.getMiddle(redstoneData.pos()), redstoneData.dir(), redstoneData.pos(), false), 0));
        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);

        redstoneTime = System.currentTimeMillis();

        lastRedstone = System.currentTimeMillis();

        if (switched) {
            switch (redstoneSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }

        return;
    }

    private void updateFire() {
        if (!fire.get()) {return;}
        if (System.currentTimeMillis() - lastFire < 1000 / speed.get()) {return;}
        if (pauseEat.get() && mc.player.isUsingItem()) {return;}

        double closesD = 10000;
        firePos = null;
        PlaceData data = null;
        boolean found = false;

        for (int x = (crystalDir.getOpposite().getOffsetX() == 0 ? -1 : Math.min(0, crystalDir.getOffsetX())); x <= (crystalDir.getOpposite().getOffsetX() == 0 ? 1 : Math.max(0, crystalDir.getOpposite().getOffsetX())); x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = (crystalDir.getOpposite().getOffsetZ() == 0 ? -1 : Math.min(0, crystalDir.getOffsetZ())); z <= (crystalDir.getOpposite().getOffsetZ() == 0 ? 1 : Math.max(0, crystalDir.getOpposite().getOffsetZ())); z++) {
                    if (found) {
                        break;
                    }

                    BlockPos pos = crystalPos.offset(crystalDir.getOpposite()).add(x, y, z);

                    if (pos.equals(crystalPos)) {continue;}
                    if (pos.equals(pistonPos)) {continue;}
                    if (pos.equals(redstonePos)) {continue;}
                    if (pos.equals(pistonPos.offset(pistonDir.getOpposite()))) {continue;}

                    if (mc.world.getBlockState(pos).getBlock() instanceof FireBlock) {
                        found = true;
                        firePos = pos;
                        data = SettingUtils.getPlaceData(pos);
                    }

                    if (!OLEPOSSUtils.solid(pos.down())) {continue;}
                    if (!(mc.world.getBlockState(pos).getBlock() instanceof AirBlock)) {continue;}

                    double d = OLEPOSSUtils.distance(pos.toCenterPos(), mc.player.getEyePos());
                    if (d >= closesD) {continue;}

                    PlaceData da = SettingUtils.getPlaceData(pos);

                    if (!da.valid()) {continue;}
                    if (!SettingUtils.inPlaceRange(da.pos())) {continue;}

                    data = da;
                    closesD = d;
                    firePos = pos;
                }
            }
        }

        if (firePos == null) {return;}

        if (data == null || !data.valid()) {return;}
        if (mc.world.getBlockState(firePos).getBlock() instanceof FireBlock) {return;}

        Hand hand = getHand(Items.FLINT_AND_STEEL);
        boolean available = hand != null;

        if (!available) {
            switch (fireSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(Items.FLINT_AND_STEEL).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(Items.FLINT_AND_STEEL).found();
            }
        }

        if (!available) {return;}

        if (SettingUtils.shouldRotate(RotationType.Placing) && !Managers.ROTATION.start(data.pos(), priority, RotationType.Placing)) {return;}

        boolean switched = false;

        if (hand == null) {
            switch (fireSwitch.get()) {
                case Silent -> {
                    InvUtils.swap(InvUtils.findInHotbar(Items.FLINT_AND_STEEL).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.find(Items.FLINT_AND_STEEL).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(Items.FLINT_AND_STEEL).slot());
            }
        }

        if (hand == null && !switched) {return;}

        hand = hand == null ? Hand.MAIN_HAND : hand;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
        sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(OLEPOSSUtils.getMiddle(data.pos()), data.dir(), data.pos(), false), 0));
        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);

        lastFire = System.currentTimeMillis();

        if (switched) {
            switch (fireSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
    }

    private void updatePos() {
        resetPos();

        mc.world.getPlayers().stream()
            .filter(player -> player != mc.player && OLEPOSSUtils.distance(player.getPos(), mc.player.getPos()) < 10 && player.getHealth() > 0 && !Friends.get().isFriend(player) && !player.isSpectator())
            .sorted(Comparator.comparingDouble(i -> OLEPOSSUtils.distance(i.getPos(), mc.player.getPos()))).forEach(player -> {

            if (crystalPos == null) {
                update(player, true);

                if (crystalPos != null) {
                    return;
                }

                update(player, false);
            }
        });
    }

    private void update(PlayerEntity player, boolean top) {
        Arrays.stream(OLEPOSSUtils.horizontals).sorted(Comparator.comparingDouble(i -> OLEPOSSUtils.distance(player.getBlockPos().offset(i).toCenterPos(), mc.player.getPos()))).toList().forEach(dir -> {
            if (crystalPos != null) {
                return;
            }

            BlockPos cPos = top ? OLEPOSSUtils.toPos(player.getEyePos()).offset(dir).up() : OLEPOSSUtils.toPos(player.getEyePos()).offset(dir);

            Block b = mc.world.getBlockState(cPos).getBlock();
            if (!(b instanceof AirBlock) && b != Blocks.PISTON_HEAD && b != Blocks.MOVING_PISTON) {return;}
            b = mc.world.getBlockState(cPos.up()).getBlock();
            if (oldVer.get() && !(b instanceof AirBlock) && b != Blocks.PISTON_HEAD && b != Blocks.MOVING_PISTON) {return;}
            if (mc.world.getBlockState(cPos.down()).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(cPos.down()).getBlock() != Blocks.BEDROCK) {return;}
            if (EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(cPos).withMaxY(cPos.getY() + 2), entity -> !entity.isSpectator() && entity instanceof PlayerEntity)) {return;}
            if (!SettingUtils.inPlaceRange(cPos)) {return;}

            Direction cDir = SettingUtils.getPlaceOnDirection(cPos);
            if (cDir == null) {
                return;
            }

            getPistonPos(cPos, dir);
            if (pistonPos == null) {
                return;
            }

            crystalPos = cPos;
            crystalPlaceDir = cDir;
            crystalDir = dir;

            targetPlayer = player;
        });
    }

    private void getPistonPos(BlockPos pos, Direction dir) {
        List<BlockPos> pistonBlocks = pistonBlocks(pos, dir);

        for (BlockPos position : pistonBlocks) {

            PlaceData placeData = SettingUtils.getPlaceDataAND(position, d -> true, b -> !isRedstone(b) &&
                !(mc.world.getBlockState(b).getBlock() instanceof PistonBlock ||
                mc.world.getBlockState(b).getBlock() instanceof PistonHeadBlock ||
                mc.world.getBlockState(b).getBlock() instanceof PistonExtensionBlock ||
                mc.world.getBlockState(b).getBlock() == Blocks.MOVING_PISTON ||
                mc.world.getBlockState(b).getBlock() instanceof FireBlock));

            if (!placeData.valid()) {continue;}
            if (!SettingUtils.inPlaceRange(placeData.pos())) {return;}

            redstonePos(position, dir.getOpposite(), pos);

            if (redstonePos == null) {continue;}

            pistonPos = position;
            pistonDir = dir.getOpposite();
            pistonData = placeData;

            return;
        }

        pistonPos = null;
        pistonData = null;
        pistonDir = null;
    }

    private List<BlockPos> pistonBlocks(BlockPos pos, Direction dir) {
        List<BlockPos> blocks = new ArrayList<>();

        for (int x = dir.getOffsetX() == 0 ? -1 : 1 - dir.getOffsetX(); x <= (dir.getOffsetX() == 0 ? 1 : dir.getOffsetX()); x++) {
            for (int z = dir.getOffsetZ() == 0 ? -1 : 1 - dir.getOffsetZ(); z <= (dir.getOffsetZ() == 0 ? 1 : dir.getOffsetZ()); z++) {
                for (int y = 0; y <= 1; y++) {
                    if (x == 0 && y == 0 && z == 0 || (oldVer.get() && x == 0 && y == 1 && z == 0)) {
                        continue;
                    }

                    if (!upCheck(pos.add(x, y, z))) {
                        continue;
                    }

                    blocks.add(pos.add(x, y, z));
                }
            }
        }

        return blocks.stream().filter(b -> {
            if (blocked(b.offset(dir.getOpposite()))) {return false;}
            if (EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(b), entity -> !entity.isSpectator() && entity instanceof PlayerEntity)) {return false;}

            if (mc.world.getBlockState(b).getBlock() instanceof PistonBlock ||
                mc.world.getBlockState(b).getBlock() == Blocks.MOVING_PISTON ||
                mc.world.getBlockState(b).getBlock() instanceof FireBlock) {return true;}

            return OLEPOSSUtils.replaceable(b);
        }).sorted(Comparator.comparingDouble(b -> OLEPOSSUtils.distance(b.toCenterPos(), mc.player.getEyePos()))).toList();
    }

    private void redstonePos(BlockPos pos, Direction pDir, BlockPos cPos) {
        if (redstone.get() == Redstone.Torch) {
            for (Direction direction : Arrays.stream(OLEPOSSUtils.noUp).sorted(Comparator.comparingDouble(i -> OLEPOSSUtils.distance(pos.offset(i).toCenterPos(), mc.player.getEyePos()))).toList()) {
                if (direction == pDir) {continue;}

                BlockPos position = pos.offset(direction);

                if (position.equals(cPos)) {continue;}
                if (oldVer.get() && position.equals(cPos.up())) {continue;}
                if (!OLEPOSSUtils.replaceable(position) && !(mc.world.getBlockState(position).getBlock() instanceof RedstoneTorchBlock) && !(mc.world.getBlockState(position).getBlock()  instanceof FireBlock)) {continue;}

                redstoneData = SettingUtils.getPlaceDataAND(position, d -> {
                    if (d == Direction.UP && !OLEPOSSUtils.solid(position.down())) {
                        return false;
                    }
                    return direction != d.getOpposite();
                }, b -> {
                    if (mc.world.getBlockState(b).getBlock() instanceof TorchBlock) {
                        return false;
                    }
                    if (mc.world.getBlockState(b).getBlock() instanceof PistonBlock ||
                        mc.world.getBlockState(b).getBlock() instanceof PistonHeadBlock) {

                    }
                    return true;
                });

                if (redstoneData.valid() && SettingUtils.inPlaceRange(redstoneData.pos()) && SettingUtils.inMineRange(position)) {
                    redstonePos = position;
                    return;
                }
            }
            redstonePos = null;
            return;
        }

        for (Direction direction : Arrays.stream(Direction.values()).sorted(Comparator.comparingDouble(i -> OLEPOSSUtils.distance(pos.offset(i).toCenterPos(), mc.player.getEyePos()))).toList()) {
            if (direction == pDir) {continue;}

            BlockPos position = pos.offset(direction);

            if (position.equals(cPos)) {continue;}
            if (!OLEPOSSUtils.replaceable(position) && mc.world.getBlockState(position).getBlock() != Blocks.REDSTONE_BLOCK) {continue;}
            if (OLEPOSSUtils.getBox(position).intersects(OLEPOSSUtils.getCrystalBox(cPos))) {continue;}
            if (EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position), entity -> !entity.isSpectator() && entity instanceof PlayerEntity)) {continue;}

            redstoneData = SettingUtils.getPlaceDataOR(position, pos::equals);

            if (redstoneData.valid()) {
                redstonePos = position;
                return;
            }
        }
        redstonePos = null;
    }



    private Entity crystalAt() {
        for (Entity entity : mc.world.getEntities()) {
            if (entity.getBlockPos().equals(crystalPos)) {
                return entity;
            }
        }
        return null;
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
        if (b == Blocks.MOVING_PISTON) {return false;}
        if (b == Blocks.PISTON_HEAD) {return false;}
        if (b == Blocks.REDSTONE_TORCH) {return false;}
        if (b instanceof FireBlock) {return false;}

        return !(mc.world.getBlockState(pos).getBlock() instanceof AirBlock);
    }

    private Hand getHand(Item item) {
        return Managers.HOLDING.isHolding(item) ? Hand.MAIN_HAND :
            mc.player.getOffHandStack().getItem() == item ? Hand.OFF_HAND :
                null;
    }

    private void resetPos() {
        crystalPos = null;
        pistonPos = null;
        firePos = null;
        redstonePos = null;

        pistonDir = null;
        pistonData = null;
        crystalPlaceDir = null;
        crystalDir = null;
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
