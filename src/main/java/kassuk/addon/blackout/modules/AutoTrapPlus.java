package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.HoleType;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.utils.*;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * @author OLEPOSSU
 */
public class AutoTrapPlus extends BlackOutModule {
    public AutoTrapPlus() {
        super(BlackOut.BLACKOUT, "Auto Trap+", "Traps enemies (literally selftrap but places on enemies).");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlacing = settings.createGroup("Placing");
    private final SettingGroup sgToggle = settings.createGroup("Toggle");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //   General Page
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> onlyConfirmed = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Confirmed")
        .description("Only places on blocks the server has confirmed to exist.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<TrapMode> trapMode = sgGeneral.add(new EnumSetting.Builder<TrapMode>()
        .name("Trap Mode")
        .description("Where should the blocks be placed at.")
        .defaultValue(TrapMode.Both)
        .build()
    );
    private final Setting<Boolean> onlyHole = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Hole")
        .description("Only places if enemy is in a hole.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> cevFriendly = sgGeneral.add(new BoolSetting.Builder()
        .name("Cev Friendly")
        .description("Doesn't place if there is a crystal on top of the block.")
        .defaultValue(true)
        .build()
    );

    //   Placing Page
    private final Setting<List<Block>> blocks = sgPlacing.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .build()
    );
    private final Setting<Double> placeDelay = sgPlacing.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0.1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> places = sgPlacing.add(new IntSetting.Builder()
        .name("Places")
        .description("Blocks placed per place.")
        .defaultValue(1)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Double> delay = sgPlacing.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between places at each spot. Should be at about 1.5x ping.")
        .defaultValue(0.3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    //  Toggle Page
    private final Setting<Boolean> toggleMove = sgToggle.add(new BoolSetting.Builder()
        .name("Toggle Move")
        .description("Toggles when you move horizontally.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SurroundPlus.ToggleYMode> toggleY = sgToggle.add(new EnumSetting.Builder<SurroundPlus.ToggleYMode>()
        .name("Toggle Y")
        .description("Toggles when you move vertically.")
        .defaultValue(SurroundPlus.ToggleYMode.Disabled)
        .build()
    );
    private final Setting<Boolean> toggleSneak = sgToggle.add(new BoolSetting.Builder()
        .name("Toggle Sneak")
        .description("Toggles when you sneak.")
        .defaultValue(false)
        .build()
    );

    //  Render Page
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of the boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the outlines")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<SettingColor> supportLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Support Line Color")
        .description("Color of the outlines for support blocks.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> supportSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Support Side Color")
        .description("Color of the sides for support blocks.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }
    public enum TrapMode {
        Top,
        Eyes,
        Both
    }
    BlockTimerList timers = new BlockTimerList();
    double placeTimer = 0;
    int placesLeft = 0;
    BlockPos startPos = new BlockPos(0, 0, 0);
    boolean lastSneak = false;
    List<Render> render = new ArrayList<>();
    BlockTimerList placed = new BlockTimerList();
    public static boolean placing = false;

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player == null || mc.world == null) {toggle();}
        startPos = mc.player.getBlockPos();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        placesLeft = places.get();
        placeTimer = 0;
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        placing = false;
        placeTimer = Math.min(placeDelay.get(), placeTimer + event.frameTime);
        if (placeTimer >= placeDelay.get()) {
            placesLeft = places.get();
            placeTimer = 0;
        }

        if (mc.player != null && mc.world != null) {

            // Move Check
            if (toggleMove.get() && (mc.player.getBlockPos().getX() != startPos.getX() || mc.player.getBlockPos().getZ() != startPos.getZ())) {
                sendDisableMsg("moved");
                toggle();
                return;
            }

            // Y Check
            switch (toggleY.get()) {
                case Full -> {
                    if (mc.player.getBlockPos().getY() != startPos.getY()) {
                        sendDisableMsg("moved vertically");
                        toggle();
                        return;
                    }
                }
                case Up -> {
                    if (mc.player.getBlockPos().getY() > startPos.getY()) {
                        sendDisableMsg("moved up");
                        toggle();
                        return;
                    }
                }
                case Down -> {
                    if (mc.player.getBlockPos().getY() < startPos.getY()) {
                        sendDisableMsg("moved down");
                        toggle();
                        return;
                    }
                }
            }

            // Sneak Check
            if (toggleSneak.get()) {
                boolean isClicked = mc.options.sneakKey.isPressed();
                if (isClicked && !lastSneak) {
                    sendDisableMsg("sneaked");
                    toggle();
                    return;
                }
                lastSneak = isClicked;
            }

            List<BlockPos> blocksList = new ArrayList<>();

            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player && !player.isSpectator() && player.getHealth() > 0 && !Friends.get().isFriend(player) && mc.player.distanceTo(player) < 10 && (!onlyHole.get() || holeCamping(player))) {
                    blocksList.addAll(getBlocks(player, getSize(player.getBlockPos().up(), player), player.getBoundingBox().intersects(OLEPOSSUtils.getBox(player.getBlockPos().up(2)))));
                }
            }

            render.clear();

            List<BlockPos> placements = getValid(blocksList);

            render.forEach(item -> event.renderer.box(OLEPOSSUtils.getBox(item.pos), item.support ? supportSideColor.get() : sideColor.get(), item.support ? supportLineColor.get() : lineColor.get(), shapeMode.get(), 0));

            FindItemResult hotbar = InvUtils.findInHotbar(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
            FindItemResult inventory = InvUtils.find(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
            Hand hand = isValid(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : isValid(mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;


            if ((!pauseEat.get() || !mc.player.isUsingItem()) &&
                (hand != null || ((switchMode.get() == SwitchMode.Silent || switchMode.get() == SwitchMode.Normal) && hotbar.slot() >= 0) ||
                    ((switchMode.get() == SwitchMode.PickSilent || switchMode.get() == SwitchMode.InvSwitch) && inventory.slot() >= 0)) && placesLeft > 0 && !placements.isEmpty()) {

                List<BlockPos> toPlace = new ArrayList<>();
                for (BlockPos placement : placements) {
                    if (toPlace.size() < placesLeft && canPlace(placement)) {
                        toPlace.add(placement);
                    }
                }

                if (!toPlace.isEmpty()) {
                    int obsidian = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() :
                        hand == Hand.OFF_HAND ? mc.player.getOffHandStack().getCount() : -1;


                    if (hand == null) {
                        switch (switchMode.get()) {
                            case Silent, Normal -> {
                                obsidian = hotbar.count();
                            }
                            case PickSilent, InvSwitch -> {
                                obsidian = inventory.slot() >= 0 ? inventory.count() : -1;
                            }
                        }
                    }

                    if (obsidian >= 0) {
                        if (hand == null) {
                            switch (switchMode.get()) {
                                case Silent, Normal -> {
                                    obsidian = hotbar.count();
                                    InvUtils.swap(hotbar.slot(), true);
                                }
                                case PickSilent -> obsidian = BOInvUtils.pickSwitch(inventory.slot()) ? inventory.count() : -1;
                                case InvSwitch -> obsidian = BOInvUtils.invSwitch(inventory.slot()) ? inventory.count() : -1;
                            }
                        }

                        if (obsidian <= 0) {
                            return;
                        }

                        placing = true;

                        for (int i = 0; i < Math.min(obsidian, toPlace.size()); i++) {
                            PlaceData placeData = onlyConfirmed.get() ? SettingUtils.getPlaceData(toPlace.get(i)) : SettingUtils.getPlaceDataOR(toPlace.get(i), pos -> placed.contains(pos));
                            if (placeData.valid()) {
                                boolean rotated = !SettingUtils.shouldRotate(RotationType.Placing) || Managers.ROTATION.start(placeData.pos().offset(placeData.dir()), 1, RotationType.Placing);

                                if (!rotated) {
                                    break;
                                }
                                place(placeData, toPlace.get(i), hand == null ? Hand.MAIN_HAND : hand);
                            }
                        }

                        if (hand == null) {
                            switch (switchMode.get()) {
                                case Silent -> InvUtils.swapBack();
                                case PickSilent -> BOInvUtils.swapBack();
                                case InvSwitch -> BOInvUtils.pickSwapBack();
                            }
                        }
                    }
                }
            }
        }
    }

    boolean isValid(ItemStack item) {
        return item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock());
    }

    boolean canPlace(BlockPos pos) {
        return SettingUtils.getPlaceData(pos).valid();
    }

    void place(PlaceData d, BlockPos ogPos, Hand hand) {
        timers.add(ogPos, delay.get());
        if (onlyConfirmed.get()) {
            placed.add(ogPos, 1);
        }

        placeTimer = 0;
        placesLeft--;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);

        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand,
            new BlockHitResult(new Vec3d(d.pos().getX() + 0.5, d.pos().getY() + 0.5, d.pos().getZ() + 0.5),
                d.dir(), d.pos(), false), 0));

        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);

        if (SettingUtils.shouldRotate(RotationType.Placing)) {
            Managers.ROTATION.end(d.pos());
        }
    }

    List<BlockPos> getValid(List<BlockPos> blocks) {
        List<BlockPos> list = new ArrayList<>();

        if (blocks.isEmpty()) {return list;}

        blocks.forEach(block -> {
            if (!OLEPOSSUtils.replaceable(block)) {return;}
            if (cevFriendly.get() && crystalAt(block.up())) {return;}

            PlaceData data = SettingUtils.getPlaceData(block);
            if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
                render.add(new Render(block, false));
                list.add(block);
                return;
            }

            // 1 block support
            Direction support1 = getSupport(block);

            if (support1 != null) {
                render.add(new Render(block, false));
                render.add(new Render(block.offset(support1), true));
                list.add(block.offset(support1));
                return;
            }

            // 2 block support
            for (Direction dir : Direction.values()) {
                if (!OLEPOSSUtils.replaceable(block.offset(dir)) || !SettingUtils.inPlaceRange(block.offset(dir))) {continue;}

                Direction support2 = getSupport(block.offset(dir));

                if (support2 != null) {
                    render.add(new Render(block, false));
                    render.add(new Render(block.offset(dir), true));
                    render.add(new Render(block.offset(dir).offset(support2), true));
                    list.add(block.offset(dir).offset(support2));
                    return;
                }
            }
        });
        return list;
    }

    Direction getSupport(BlockPos position) {
        Direction cDir = null;
        double cDist = 1000;
        int value = -1;

        for (Direction dir : Direction.values()) {
            PlaceData data = onlyConfirmed.get() ? SettingUtils.getPlaceData(position.offset(dir)) : SettingUtils.getPlaceDataOR(position.offset(dir), pos -> placed.contains(pos));

            if (!data.valid() || !SettingUtils.inPlaceRange(data.pos())) {continue;}

            if (!EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position.offset(dir)), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                double dist = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(position.offset(dir)));

                if (dist < cDist || value < 2) {
                    value = 2;
                    cDir = dir;
                    cDist = dist;
                }
            }

            if (!EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position.offset(dir)), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM && entity.getType() != EntityType.END_CRYSTAL)) {
                double dist = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(position.offset(dir)));

                if (dist < cDist || value < 1) {
                    value = 1;
                    cDir = dir;
                    cDist = dist;
                }
            }

        }
        return cDir;
    }

    List<BlockPos> getBlocks(PlayerEntity player, int[] size, boolean higher) {
        List<BlockPos> list = new ArrayList<>();
        BlockPos pos = player.getBlockPos().up(higher ? 2 : 1);

        for (int x = size[0] - 1; x <= size[1] + 1; x++) {
            for (int z = size[2] - 1; z <= size[3] + 1; z++) {

                boolean isX = x == size[0] - 1 || x == size[1] + 1;
                boolean isZ = z == size[2] - 1 || z == size[3] + 1;

                boolean ignore = isX && !isZ ? (!OLEPOSSUtils.replaceable(pos.add(OLEPOSSUtils.closerToZero(x), 0, z)) || placed.contains(pos.add(OLEPOSSUtils.closerToZero(x), 0, z))) :
                    !isX && isZ && (!OLEPOSSUtils.replaceable(pos.add(x, 0, OLEPOSSUtils.closerToZero(z))) || placed.contains(pos.add(x, 0, OLEPOSSUtils.closerToZero(z))));

                BlockPos bPos = null;

                if (eye() && isX != isZ && !ignore) {
                    bPos = new BlockPos(x, pos.getY() ,z).add(pos.getX(), 0, pos.getZ());
                } else if (top() && !isX && !isZ && OLEPOSSUtils.replaceable(pos.add(x, 0, z)) && !placed.contains(pos.add(x, 0, z))) {
                    bPos = new BlockPos(x, pos.getY() ,z).add(pos.getX(), 1, pos.getZ());
                }

                if (bPos != null) {
                    list.add(bPos);
                }
            }
        }

        return list;
    }

    boolean top() {
        return trapMode.get() == TrapMode.Both || trapMode.get() == TrapMode.Top;
    }

    boolean eye() {
        return trapMode.get() == TrapMode.Both || trapMode.get() == TrapMode.Eyes;
    }


    int[] getSize(BlockPos pos, PlayerEntity player) {
        int minX = 0;
        int maxX = 0;
        int minZ = 0;
        int maxZ = 0;
        if (mc.world != null) {
            Box box = player.getBoundingBox();
            if (box.intersects(OLEPOSSUtils.getBox(pos.north()))) {
                minZ--;
            }
            if (box.intersects(OLEPOSSUtils.getBox(pos.south()))) {
                maxZ++;
            }
            if (box.intersects(OLEPOSSUtils.getBox(pos.west()))) {
                minX--;
            }
            if (box.intersects(OLEPOSSUtils.getBox(pos.east()))) {
                maxX++;
            }
        }
        return new int[]{minX, maxX, minZ, maxZ};
    }
    boolean holeCamping(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();

        if (HoleUtils.getHole(pos, 1).type == HoleType.Single) {
            return true;
        }
        // DoubleX
        if (HoleUtils.getHole(pos, 1).type == HoleType.DoubleX ||
            HoleUtils.getHole(pos.add(-1, 0, 0), 1).type == HoleType.DoubleX) {
            return true;
        }

        // DoubleZ
        if (HoleUtils.getHole(pos, 1).type == HoleType.DoubleZ ||
            HoleUtils.getHole(pos.add(0, 0, -1), 1).type == HoleType.DoubleZ) {
            return true;
        }

        // Quad
        return HoleUtils.getHole(pos, 1).type == HoleType.Quad ||
            HoleUtils.getHole(pos.add(-1, 0, -1), 1).type == HoleType.Quad ||
            HoleUtils.getHole(pos.add(-1, 0, 0), 1).type == HoleType.Quad ||
            HoleUtils.getHole(pos.add(0, 0, -1), 1).type == HoleType.Quad;
    }

    boolean crystalAt(BlockPos pos) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity && entity.getBlockPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    record Render(BlockPos pos, boolean support) {}
}
