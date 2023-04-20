package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.*;
import kassuk.addon.blackout.utils.meteor.BODamageUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.*;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class BedAuraPlus extends BlackOutModule {
    public BedAuraPlus() {super(BlackOut.BLACKOUT, "Bed Aura+", "Automatically places and breaks beds to cause damage to your opponents but better");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlacing = settings.createGroup("Placing");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //   General Page
    private final Setting<Boolean> fiveB = sgGeneral.add(new BoolSetting.Builder()
        .name("5B5T")
        .description("For example requires floor for both bed blocks and allows placing inside entities")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> doubleInteract = sgGeneral.add(new BoolSetting.Builder()
        .name("Double Interact")
        .description("Clicks both bed blocks")
        .defaultValue(true)
        .build()
    );
    private final Setting<LogicMode> logicMode = sgGeneral.add(new EnumSetting.Builder<LogicMode>()
        .name("Logic Mode")
        .description("Logic for bullying kids.")
        .defaultValue(LogicMode.BreakPlace)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description(".")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<RotationMode> rotMode = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
        .name("Rotation Mode")
        .description(".")
        .defaultValue(RotationMode.Packet)
        .build()
    );

    //   Placing Page
    private final Setting<SpeedMode> speedMode = sgPlacing.add(new EnumSetting.Builder<SpeedMode>()
        .name("Speed Mode")
        .description("Normal mode should be used in everywhere else than 5B.")
        .defaultValue(SpeedMode.Normal)
        .build()
    );
    private final Setting<Double> speed = sgPlacing.add(new DoubleSetting.Builder()
        .name("Speed")
        .description(".")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> speedMode.get() == SpeedMode.Normal)
        .build()
    );
    private final Setting<Double> damageSpeed = sgPlacing.add(new DoubleSetting.Builder()
        .name("Damage Speed Factor")
        .description(".")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> speedMode.get() == SpeedMode.Damage)
        .build()
    );
    private final Setting<Double> maxSpeed = sgPlacing.add(new DoubleSetting.Builder()
        .name("Damage Speed")
        .description(".")
        .defaultValue(12)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> speedMode.get() == SpeedMode.Damage)
        .build()
    );

    //   Damage Page
    private final Setting<Double> minDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Damage")
        .description(".")
        .defaultValue(8)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Damage")
        .description(".")
        .defaultValue(6)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxFriendDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Damage")
        .description(".")
        .defaultValue(6)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> minRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Damage Ratio")
        .description(".")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> minFriendRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Friend Damage Ratio")
        .description(".")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> forcePop = sgDamage.add(new DoubleSetting.Builder()
        .name("Force Pop")
        .description(".")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Pop")
        .description(".")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiFriendPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Friend Pop")
        .description(".")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> friendSacrifice = sgDamage.add(new BoolSetting.Builder()
        .name("Friend Sacrifice")
        .description("Kills your friend if you can also kill any enemy with same bed")
        .defaultValue(true)
        .build()
    );

    //   Render Page
    public final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Head Line Color")
        .description("Line color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Head Side Color")
        .description("Side color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<SettingColor> fLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Feet Line Color")
        .description("Line color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> fColor = sgRender.add(new ColorSetting.Builder()
        .name("Feet Side Color")
        .description("Side color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    public enum LogicMode {
        PlaceBreak,
        BreakPlace
    }
    public enum RotationMode {
        Packet,
        Manager
    }
    public enum SwitchMode {
        Silent,
        Normal,
        SilentBypass,
        Disabled
    }
    public enum SpeedMode {
        Normal,
        Damage
    }

    BlockPos[] blocks = new BlockPos[]{};
    int lastIndex = 0;
    int length = 0;
    long tickTime = -1;
    double bestDmg = 0;
    long lastTime = 0;

    BlockPos placePos = null;
    Direction bedDir = null;
    PlaceData placeData = null;
    BlockPos calcPos = null;
    Direction calcDir = null;
    PlaceData calcData = null;
    BlockPos renderPos = null;
    Direction renderDir = null;
    List<PlayerEntity> targets = new ArrayList<>();
    List<PlayerEntity> friends = new ArrayList<>();
    List<Bed> beds = new ArrayList<>();

    double timer = 0;

    double dmg;
    double enemyHP;
    double self;
    double selfHP;
    double friend;
    double friendHP;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTickPre(TickEvent.Post event) {
        calculate(length - 1);
        renderPos = calcPos;
        placePos = calcPos;
        renderDir = calcDir;
        bedDir = calcDir;
        placeData = calcData;

        blocks = getBlocks(mc.player.getEyePos(), Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));

        // Reset stuff
        tickTime = System.currentTimeMillis();
        length = blocks.length;
        lastIndex = 0;
        bestDmg = 0;
        calcPos = null;
        calcDir = null;
        calcData = null;

        updateTargets();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        double delta = (System.currentTimeMillis() - lastTime) / 1000f;
        timer += delta;
        lastTime = System.currentTimeMillis();

        List<Bed> toRemove = new ArrayList<>();
        beds.forEach(bed -> {
            if (System.currentTimeMillis() - bed.time > 500) {
                toRemove.add(bed);
            }
        });
        toRemove.forEach(beds::remove);

        if (tickTime < 0 || mc.player == null || mc.world == null) {return;}

        if (pauseCheck()) {
            update();
        }

        int index = Math.min((int) Math.ceil((System.currentTimeMillis() - tickTime) / 50f * length), length - 1);
        calculate(index);

        if (renderPos != null && pauseCheck()) {
            event.renderer.box(bedBox(renderPos), color.get(), lineColor.get(), shapeMode.get(), 0);
            if (renderDir != null) {
                event.renderer.box(bedBox(renderPos.offset(renderDir)), fColor.get(), fLineColor.get(), shapeMode.get(), 0);
            }
        }

    }

    boolean pauseCheck() {
        return !pauseEat.get() || !mc.player.isUsingItem();
    }

    void calculate(int index) {
        BlockPos pos;

        for (int i = lastIndex; i < index; i++) {
            pos = blocks[i];

            damageCalc(pos);

            if (!dmgCheck()) {continue;}

            for (Direction dir : OLEPOSSUtils.horizontals) {
                PlaceData data = getData(pos, dir);

                if (!data.valid()) {continue;}

                if (!OLEPOSSUtils.replaceable(pos.offset(dir)) && !(mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof BedBlock)) {continue;}

                if (!SettingUtils.inPlaceRange(data.pos())) {continue;}

                if (!fiveB.get() && EntityUtils.intersectsWithEntity(new Box(pos.offset(dir)), entity -> !(entity instanceof ItemEntity))) {continue;}

                calcData = data;
                calcPos = pos;
                calcDir = dir;
                bestDmg = dmg;
            }
        }
        lastIndex = index;
    }

    void updateTargets() {
        friends.clear();
        targets.clear();
        List<PlayerEntity> players = new ArrayList<>();
        double closestDist = 1000;
        PlayerEntity closest;
        double dist;
        for (int i = 3; i > 0; i--) {

            closest = null;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (players.contains(player) || Friends.get().isFriend(player) || player == mc.player) {continue;}

                dist = player.distanceTo(mc.player);

                if (dist > 15) {continue;}

                if (closest == null || dist < closestDist) {
                    closestDist = dist;
                    closest = player;
                }
            }
            if (closest != null) {
                players.add(closest);
                if (Friends.get().isFriend(closest)) {
                    friends.add(closest);
                } else {
                    targets.add(closest);
                }
            }
        }
    }

    BlockPos[] getBlocks(Vec3d middle, double radius) {
        ArrayList<BlockPos> result = new ArrayList<>();
        int i = (int) Math.ceil(radius);
        BlockPos pos;

        for (int x = -i; x <= i; x++) {
            for (int y = -i; y <= i; y++) {
                for (int z = -i; z <= i; z++) {
                    pos = new BlockPos(Math.floor(middle.x) + x, Math.floor(middle.y) + y, Math.floor(middle.z) + z);

                    if (!OLEPOSSUtils.replaceable(pos) && !(mc.world.getBlockState(pos).getBlock() instanceof BedBlock)) {continue;}

                    if (fiveB.get() && (mc.world.getBlockState(pos.down()).getBlock() == Blocks.AIR || mc.world.getBlockState(pos.down()).hasBlockEntity())) {continue;}

                    if (!inRangeToTargets(pos)) {continue;}
                    result.add(pos);
                }
            }
        }
        return result.toArray(new BlockPos[0]);
    }

    boolean inRangeToTargets(BlockPos pos) {
        for (PlayerEntity target : targets) {
            if (OLEPOSSUtils.distance(target.getPos().add(0, 1, 0), new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) < 3.5) {
                return true;
            }
        }
        return false;
    }

    void update() {
        if (placePos == null || placeData == null || !placeData.valid() || bedDir == null) {return;}

        if (logicMode.get() == LogicMode.PlaceBreak) {
            List<BlockPos> in = interactUpdate();
            if (in != null && !in.isEmpty()) {
                in.forEach(this::removeBed);
            }

            if (timer <= 1 / getSpeed()) {return;}

            if (OLEPOSSUtils.replaceable(placePos) && OLEPOSSUtils.replaceable(placePos.offset(bedDir)) && placeUpdate()) {
                removeBed2(placePos);
                beds.add(new Bed(placePos, placePos.offset(bedDir), true, System.currentTimeMillis()));
                timer = 0;
            }
        } else {
            if (!isBed(placePos) && !isBed(placePos.offset(bedDir)) && placeUpdate()) {
                removeBed2(placePos);
                beds.add(new Bed(placePos, placePos.offset(bedDir), true, System.currentTimeMillis()));
            }

            if (timer <= 1 / getSpeed()) {return;}

            List<BlockPos> in = interactUpdate();
            if (in != null && !in.isEmpty()) {
                in.forEach(this::removeBed);
                timer = 0;
            }
        }
    }

    void removeBed(BlockPos pos) {
        List<Bed> toRemove = new ArrayList<>();
        beds.forEach(bed -> {
            if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
                toRemove.add(bed);
            }
        });
        toRemove.forEach(bed -> {
            beds.remove(bed);
            beds.add(new Bed(bed.feetBlock, bed.headBlock, false, System.currentTimeMillis()));
        });
    }
    void removeBed2(BlockPos pos) {
        List<Bed> toRemove = new ArrayList<>();
        beds.forEach(bed -> {
            if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
                toRemove.add(bed);
            }
        });
        toRemove.forEach(bed -> {
            beds.remove(bed);
        });
    }

    void place(Hand hand) {
        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);

        sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(OLEPOSSUtils.getMiddle(placeData.pos()), placeData.dir(), placeData.pos(), false), 0));

        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
    }

    List<BlockPos> interactUpdate() {
        if (doubleInteract.get()) {
            if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(placePos, priority, RotationType.Interact)) {return null;}

            List<BlockPos> list = new ArrayList<>();

            if (isBed(placePos) || isBed(placePos.offset(bedDir))) {
                if (SettingUtils.inPlaceRange(placePos) && interact(placePos)) {
                    list.add(placePos);
                }
                if (SettingUtils.inPlaceRange(placePos.offset(bedDir)) && interact(placePos.offset(bedDir))) {
                    list.add(placePos.offset(bedDir));
                }
            }

            if (SettingUtils.shouldRotate(RotationType.Interact)) {
                Managers.ROTATION.end(placePos);
            }

            return list;
        }

        BlockPos interactPos = getInteractPos();

        if (interactPos == null) {return null;}

        Direction interactDir = SettingUtils.getPlaceOnDirection(interactPos);

        if (interactDir == null) {return null;}

        if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(interactPos, priority, RotationType.Interact)) {return null;}

        SettingUtils.swing(SwingState.Pre, SwingType.Interact, Hand.MAIN_HAND);

        sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(OLEPOSSUtils.getMiddle(interactPos), interactDir, interactPos, false), 0));

        SettingUtils.swing(SwingState.Post, SwingType.Interact, Hand.MAIN_HAND);

        if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end(interactPos);
        }
        List<BlockPos> list = new ArrayList<>();
        list.add(interactPos);
        return list;
    }

    boolean interact(BlockPos pos) {
        Direction dir = SettingUtils.getPlaceOnDirection(pos);

        if (dir == null) {return false;}

        SettingUtils.swing(SwingState.Pre, SwingType.Interact, Hand.MAIN_HAND);

        sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(OLEPOSSUtils.getMiddle(pos), dir, pos, false), 0));

        SettingUtils.swing(SwingState.Post, SwingType.Interact, Hand.MAIN_HAND);
        return true;
    }

    BlockPos getInteractPos() {
        if (isBed(placePos.offset(bedDir)) && SettingUtils.inPlaceRange(placePos.offset(bedDir)) && SettingUtils.getPlaceOnDirection(placePos.offset(bedDir)) != null) {
            return placePos.offset(bedDir);
        }
        if (isBed(placePos) && SettingUtils.inPlaceRange(placePos) && SettingUtils.getPlaceOnDirection(placePos) != null) {
            return placePos;
        }
        return null;
    }

    boolean isBed(BlockPos pos) {
        for (Bed bed : beds) {
            if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
                return bed.isBed;
            }
        }
        return mc.world.getBlockState(pos).getBlock() instanceof BedBlock;
    }

    boolean placeUpdate() {
        Hand hand = Managers.HOLDING.getStack().getItem() instanceof BedItem ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() instanceof BedItem ? Hand.OFF_HAND : null;

        int beds = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() :
            hand == Hand.OFF_HAND ? mc.player.getOffHandStack().getCount() : 0;

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(item -> item.getItem() instanceof BedItem);
                    beds = result.count();
                }
                case SilentBypass -> {
                    FindItemResult result = InvUtils.find(item -> item.getItem() instanceof BedItem);
                    beds = result.slot() >= 0 ? result.count() : -1;
                }
            }
        }

        if (beds <= 0) {return false;}

        if (SettingUtils.shouldRotate(RotationType.Placing) && !Managers.ROTATION.start(placeData.pos(), priority, RotationType.Placing)) {return false;}

        boolean switched = hand != null;

        if (rotMode.get() == RotationMode.Packet) {
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(bedDir.getOpposite().asRotation(), Managers.ROTATION.lastDir[1], Managers.ONGROUND.isOnGround()));
        } else {
            Managers.ROTATION.startYaw(bedDir.getOpposite().asRotation(), priority, RotationType.Other);
            if (Math.abs(RotationUtils.yawAngle(Managers.ROTATION.lastDir[0], bedDir.getOpposite().asRotation())) > 45) {
                return false;
            }
        }

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(item -> item.getItem() instanceof BedItem);
                    InvUtils.swap(result.slot(), true);
                    switched = true;
                }
                case SilentBypass -> {
                    FindItemResult result = InvUtils.find(item -> item.getItem() instanceof BedItem);
                    switched = BOInvUtils.invSwitch(result.slot());
                }
            }
        }

        if (!switched) {return false;}

        place(hand == null ? Hand.MAIN_HAND : hand);

        if (SettingUtils.shouldRotate(RotationType.Placing)) {
            Managers.ROTATION.end(placeData.pos());
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case SilentBypass -> BOInvUtils.swapBack();
            }
        }
        return true;
    }

    boolean dmgCheck() {
        if (dmg < bestDmg) {return false;}

        if (self * antiPop.get() >= selfHP) {return false;}

        if (!friendSacrifice.get() && friendHP >= 0 && friend * antiFriendPop.get() >= friendHP) {return false;}
        if (enemyHP >= 0 && dmg * forcePop.get() >= enemyHP) {return true;}
        if (friendHP >= 0 && friend * antiFriendPop.get() >= friendHP) {return false;}

        if (dmg < minDmg.get()) {return false;}

        if (self > maxDmg.get()) {return false;}
        if (friend > maxFriendDmg.get()) {return false;}

        if (dmg / self < minRatio.get()) {return false;}
        if (friendHP >= 0 && dmg / friend < minFriendRatio.get()) {return false;}

        return true;
    }

    double getDmg(BlockPos pos) {
        double highest = -1;
        for (PlayerEntity target : targets) {
            highest = Math.max(highest, BODamageUtils.bedDamage(target, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
        }
        return highest;
    }

    void damageCalc(BlockPos pos) {
        // Enemy
        double highest = -1;
        double highestHP = -1;
        for (PlayerEntity target : targets) {
            if (target.getHealth() <= 0) {continue;}

            highest = Math.max(highest, BODamageUtils.bedDamage(target, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
            highestHP = target.getHealth() + target.getAbsorptionAmount();
        }
        dmg = highest;
        enemyHP = highestHP;

        // Self
        self = BODamageUtils.bedDamage(mc.player, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        selfHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();

        // Friend
        highest = -1;
        highestHP = -1;
        for (PlayerEntity friend : friends) {
            if (friend.getHealth() <= 0) {continue;}

            highest = Math.max(highest, BODamageUtils.bedDamage(friend, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
            highestHP = friend.getHealth() + friend.getAbsorptionAmount();
        }
        friend = highest;
        friendHP = highestHP;
    }

    Box bedBox(BlockPos pos) {
        return new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.5, pos.getZ() + 1);
    }

    PlaceData getData(BlockPos pos, Direction dir) {
        if (fiveB.get()) {
            return SettingUtils.getPlaceDataAND(pos.offset(dir), direction -> direction == Direction.DOWN, pos1 -> !(mc.world.getBlockState(pos1).getBlock() instanceof BedBlock));
        } else {
            return SettingUtils.getPlaceDataAND(pos.offset(dir), direction -> direction != dir, pos1 -> !(mc.world.getBlockState(pos1).getBlock() instanceof BedBlock));
        }
    }

    double getSpeed() {
        switch (speedMode.get()) {
            case Normal -> {
                return speed.get();
            }
            case Damage -> {
                if (placePos == null) {
                    return maxSpeed.get();
                }

                double dmg = getDmg(placePos);
                return Math.min(dmg * damageSpeed.get(), maxSpeed.get());
            }
        }
        return 2;
    }

    record Bed(BlockPos feetBlock, BlockPos headBlock, boolean isBed, long time) {}
}
