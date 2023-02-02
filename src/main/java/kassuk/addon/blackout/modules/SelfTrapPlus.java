package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class SelfTrapPlus extends Module {
    public SelfTrapPlus() {
        super(BlackOut.BLACKOUT, "Self Trap+", "Traps yourself");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description(".")
        .defaultValue(SwitchMode.SilentBypass)
        .build()
    );
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .build()
    );
    private final Setting<Boolean> onlyTop = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Top")
        .description("Only places the top blocks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0.125)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> places = sgGeneral.add(new IntSetting.Builder()
        .name("Places")
        .description("Blocks placed per place")
        .defaultValue(1)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between places at each spot.")
        .defaultValue(0.3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the outlines")
        .defaultValue(new SettingColor(255, 0, 0, 150))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        SilentBypass
    }
    BlockTimerList timers = new BlockTimerList();
    double placeTimer = 0;
    int placesLeft = 0;

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        placesLeft = places.get();
        placeTimer = 0;
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        placeTimer = Math.min(placeDelay.get(), placeTimer + event.frameTime);
        if (placeTimer >= placeDelay.get()) {
            placesLeft = places.get();
            placeTimer = 0;
        }

        if (mc.player != null && mc.world != null) {
            List<BlockPos> render = getBlocks(getSize(mc.player.getBlockPos().up()), mc.player.getBoundingBox().intersects(OLEPOSSUtils.getBox(mc.player.getBlockPos().up(2))));
            List<BlockPos> placements = getValid(render);

            render.forEach(item -> event.renderer.box(OLEPOSSUtils.getBox(item), sideColor.get(), lineColor.get(), shapeMode.get(), 0));

            FindItemResult hotbar = InvUtils.findInHotbar(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
            FindItemResult inventory = InvUtils.find(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
            Hand hand = isValid(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : isValid(mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;


            if ((!pauseEat.get() || !mc.player.isUsingItem()) &&
                (hand != null || ((switchMode.get() == SwitchMode.Silent || switchMode.get() == SwitchMode.Normal) && hotbar.slot() >= 0) ||
                    (switchMode.get() == SwitchMode.SilentBypass && inventory.slot() >= 0)) && placesLeft > 0 && !placements.isEmpty()) {

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
                                InvUtils.swap(hotbar.slot(), true);
                            }
                            case SilentBypass -> {
                                obsidian = BOInvUtils.invSwitch(inventory.slot()) ? inventory.count() : -1;
                            }
                        }
                    }

                    if (obsidian >= 0) {
                        for (int i = 0; i < Math.min(obsidian, placements.size()); i++) {
                            place(placements.get(i));
                        }

                        if (hand == null) {
                            switch (switchMode.get()) {
                                case Silent -> {
                                    InvUtils.swapBack();
                                }
                                case SilentBypass -> {
                                    BOInvUtils.swapBack();
                                }
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
        Direction[] dir = SettingUtils.getPlaceDirection(pos);
        if (dir[0] == null && dir[1] == null) {return false;}
        return true;
    }

    void place(BlockPos toPlace) {
        Direction[] result = SettingUtils.getPlaceDirection(toPlace);
        if (result[0] == null && result[1] == null) {return;}
        timers.add(toPlace, delay.get());
        placeTimer = 0;
        placesLeft--;
        if (result[1] != null) {
            if (SettingUtils.shouldRotate(RotationType.Placing)) {
                Managers.ROTATION.start(toPlace, 3);
            }

            SettingUtils.swing(SwingState.Pre, SwingType.Placing);

            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(toPlace.getX() + 0.5, toPlace.getY() + 0.5, toPlace.getZ() + 0.5),
                    result[1], toPlace, false), 0));

            SettingUtils.swing(SwingState.Post, SwingType.Placing);

            if (SettingUtils.shouldRotate(RotationType.Placing)) {
                Managers.ROTATION.end(toPlace);
            }
        } else {
            if (SettingUtils.shouldRotate(RotationType.Placing)) {
                Managers.ROTATION.start(toPlace.offset(result[0]), 3);
            }

            SettingUtils.swing(SwingState.Pre, SwingType.Placing);

            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(toPlace.offset(result[0]).getX() + 0.5, toPlace.offset(result[0]).getY() + 0.5, toPlace.offset(result[0]).getZ() + 0.5),
                    result[0].getOpposite(), toPlace.offset(result[0]), false), 0));

            SettingUtils.swing(SwingState.Post, SwingType.Placing);

            if (SettingUtils.shouldRotate(RotationType.Placing)) {
                Managers.ROTATION.end(toPlace.offset(result[0]));
            }
        }
    }

    List<BlockPos> getValid(List<BlockPos> blocks) {
        List<BlockPos> list = new ArrayList<>();
        blocks.forEach(item -> {
            if (!timers.contains(item) &&
                !EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(item), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                list.add(item);
            }
        });
        return list;
    }

    List<BlockPos> getBlocks(int[] size, boolean higher) {
        List<BlockPos> list = new ArrayList<>();
        BlockPos pos = mc.player.getBlockPos().up(higher ? 2 : 1);
        if (mc.player != null && mc.world != null) {
            for (int x = size[0] - 1; x <= size[1] + 1; x++) {
                for (int z = size[2] - 1; z <= size[3] + 1; z++) {
                    boolean isX = x == size[0] - 1 || x == size[1] + 1;
                    boolean isZ = z == size[2] - 1 || z == size[3] + 1;
                    boolean ignore = isX && !isZ ? !air(pos.add(OLEPOSSUtils.closerToZero(x), 0, z)) :
                        !isX && isZ && !air(pos.add(x, 0, OLEPOSSUtils.closerToZero(z)));
                    BlockPos bPos = null;
                    if (!onlyTop.get() && isX != isZ && !ignore) {
                        bPos = new BlockPos(x, pos.getY() ,z).add(pos.getX(), 0, pos.getZ());
                    } else if (!isX && !isZ && air(pos.add(x, 0, z))) {
                        bPos = new BlockPos(x, pos.getY() ,z).add(pos.getX(), 1, pos.getZ());
                    }
                    if (bPos != null && mc.world.getBlockState(bPos).getBlock().equals(Blocks.AIR)) {
                        list.add(bPos);
                    }
                }
            }
        }
        return list;
    }

    boolean air(BlockPos pos) {return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);}

    int[] getSize(BlockPos pos) {
        int minX = 0;
        int maxX = 0;
        int minZ = 0;
        int maxZ = 0;
        if (mc.player != null && mc.world != null) {
            Box box = mc.player.getBoundingBox();
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

    int[] findObby() {
        int num = 0;
        int slot = 0;
        if (mc.player != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getCount() > num && stack.getItem().equals(Items.OBSIDIAN)) {
                    num = stack.getCount();
                    slot = i;
                }
            }
        }
        return new int[] {slot, num};
    }
}
