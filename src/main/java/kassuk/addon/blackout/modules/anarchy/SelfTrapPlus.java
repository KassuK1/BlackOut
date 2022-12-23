package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.managers.BlockTimerList;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
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
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> silent = sgGeneral.add(new BoolSetting.Builder()
        .name("Silent")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description(".")
        .defaultValue(0)
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
        .description(".")
        .defaultValue(0.3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 150))
        .build()
    );

    private BlockTimerList timers = new BlockTimerList();
    private double placeTimer = 0;


    public SelfTrapPlus() {
        super(BlackOut.ANARCHY, "Self Trap+", "Traps yourself");
    }

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player != null && mc.world != null) {
            List<BlockTimerList> toRemove = new ArrayList<>();
            placeTimer = Math.min(placeDelay.get(), placeTimer + event.frameTime);
            List<BlockPos> render = getBlocks(getSize(mc.player.getBlockPos().up()), mc.player.getBoundingBox().intersects(OLEPOSSUtils.getBox(mc.player.getBlockPos().up(2))));
            List<BlockPos> blocks = getValid(render);
            render.forEach(item -> event.renderer.box(OLEPOSSUtils.getBox(item), new Color(color.get().r, color.get().g, color.get().b,
                (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both, 0));

            int[] obsidian = findObby();
            if ((!pauseEat.get() || !mc.player.isUsingItem()) && obsidian[1] > 0 &&
                (silent.get() || Managers.HOLDING.isHolding(Items.OBSIDIAN)) && !blocks.isEmpty() && placeTimer >= placeDelay.get()) {
                boolean swapped = false;
                if (!Managers.HOLDING.isHolding(Items.OBSIDIAN) && silent.get()) {
                    InvUtils.swap(obsidian[0], true);
                    swapped = true;
                }
                for (int i = 0; i < Math.min(Math.min(blocks.size(), obsidian[1]), places.get()); i++) {
                    place(blocks.get(i));
                }
                if (swapped) {
                    InvUtils.swapBack();
                }
            }
        }
    }

    private void place(BlockPos toPlace) {
        timers.add(toPlace, delay.get());
        placeTimer = 0;
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
            new BlockHitResult(new Vec3d(toPlace.getX() + 0.5, toPlace.getY() + 0.5, toPlace.getZ() + 0.5), Direction.UP, toPlace, false), 0));
        if (swing.get()) {
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }

    private List<BlockPos> getValid(List<BlockPos> blocks) {
        List<BlockPos> list = new ArrayList<>();
        blocks.forEach(item -> {
            if (!timers.isPlaced(item) &&
                !EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(item), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                list.add(item);
            }
        });
        return list;
    }

    private List<BlockPos> getBlocks(int[] size, boolean higher) {
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
                    if (isX != isZ && !ignore) {
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

    private boolean air(BlockPos pos) {return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);}

    private int[] getSize(BlockPos pos) {
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

    private int[] findObby() {
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
