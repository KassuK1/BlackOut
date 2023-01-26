package kassuk.addon.blackout.modules;

import io.netty.util.internal.MathUtil;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;

/*
Made by KassuK
Updated by OLEPOSSU
*/

public class SurroundPlus extends Module {
    public SurroundPlus() {super(BlackOut.BLACKOUT, "Surround+", "KasumsSoft surround");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<OrderMode> placeOrder = sgGeneral.add(new EnumSetting.Builder<OrderMode>()
        .description("Place Order")
        .defaultValue(OrderMode.Angle)
        .build()
    );
    private final Setting<Boolean> silent = sgGeneral.add(new BoolSetting.Builder()
        .name("Silent")
        .description("Silently switch to obby when placing")
        .defaultValue(true)
        .build()
    );
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
    private final Setting<Boolean> center = sgGeneral.add(new BoolSetting.Builder()
        .name("Center")
        .description("Should we center on da hole")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> floor = sgGeneral.add(new BoolSetting.Builder()
        .name("Floor")
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

    public enum OrderMode {
        Enemy,
        Angle,
        Dist
    }

    BlockTimerList timers = new BlockTimerList();
    BlockPos startPos = null;
    double placeTimer = 0;
    int placesLeft = 0;
    List<BlockPos> render = new ArrayList<>();

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player == null || mc.world == null) {toggle();}
        startPos = mc.player.getBlockPos();
        placesLeft = places.get();
        placeTimer = 0;
        render = new ArrayList<>();
    }

    @EventHandler
    private void onRender(Render3DEvent event){
        placeTimer = Math.min(placeDelay.get(), placeTimer + event.frameTime);
        if (placeTimer >= placeDelay.get()) {
            placesLeft = places.get();
            placeTimer = 0;
        }
        render.forEach(item -> event.renderer.box(OLEPOSSUtils.getBox(item), new Color(color.get().r, color.get().g, color.get().b,
            (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both, 0));
        update();
    }

    void update() {
        if (mc.player != null && mc.world != null) {

            //Check if player has moved
            if (!mc.player.getBlockPos().equals(startPos)) {
                toggle();
                return;
            }

            List<BlockPos> placements = check();
            int[] obsidian = findObby();
            if (obsidian[1] > 0 && (Managers.HOLDING.isHolding(Items.OBSIDIAN) || silent.get()) && !placements.isEmpty() &&
                (!pauseEat.get() || !mc.player.isUsingItem()) && placesLeft > 0) {
                boolean swapped = false;
                if (!Managers.HOLDING.isHolding(Items.OBSIDIAN) && silent.get()) {
                    InvUtils.swap(obsidian[0], true);
                    swapped = true;
                }
                if (center.get()) {
                    PlayerUtils.centerPlayer();
                }
                int p = Math.min(Math.min(obsidian[1], placesLeft), placements.size());
                for (int i = 0; i < p; i++) {
                    BlockPos toPlace = placements.get(i);
                    Direction[] result = SettingUtils.getPlaceDirection(toPlace);
                    if (result[0] != null || result[1] != null) {
                        timers.add(toPlace, delay.get());
                        placeTimer = 0;
                        placesLeft--;
                        if (result[1] != null) {
                            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                                new BlockHitResult(new Vec3d(toPlace.getX() + 0.5, toPlace.getY() + 0.5, toPlace.getZ() + 0.5),
                                    result[1], toPlace, false), 0));
                        } else {
                            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                                new BlockHitResult(new Vec3d(toPlace.offset(result[0]).getX() + 0.5, toPlace.offset(result[0]).getY() + 0.5, toPlace.offset(result[0]).getZ() + 0.5),
                                    result[0].getOpposite(), toPlace.offset(result[0]), false), 0));
                        }
                        if (swing.get()) {
                            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        }
                    }
                }
                if (swapped) {
                    InvUtils.swapBack();
                }
            }
        }
    }

    List<BlockPos> check() {
        List<BlockPos> list = new ArrayList<>();
        List<BlockPos> renders = new ArrayList<>();
        List<BlockPos> blocks = getBlocks(getSize());
        if (mc.player != null && mc.world != null) {
            for (BlockPos position : blocks) {
                if (mc.world.getBlockState(position).getBlock().equals(Blocks.AIR)) {
                    if (!timers.contains(position) && !EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                        list.add(position);
                    }
                    renders.add(position);
                }
            }
        }
        render = renders;
        return list;
    }

    int[] getSize() {
        if (mc.player == null || mc.world == null) {return new int[]{0, 0, 0, 0};}

        Vec3d offset = mc.player.getPos().add(-mc.player.getBlockX(), -mc.player.getBlockY(), -mc.player.getBlockZ());
        return new int[]{offset.x < 0.3 ? -1 : 0, offset.x > 0.7 ? 1 : 0, offset.z < 0.3 ? -1 : 0, offset.z > 0.7 ? 1 : 0};
    }

    List<BlockPos> getBlocks(int[] size) {
        List<BlockPos> list = new ArrayList<>();
        if (mc.player != null && mc.world != null) {
            BlockPos pPos = mc.player.getBlockPos();
            for (int x = size[0] - 1; x <= size[1] + 1; x++) {
                for (int z = size[2] - 1; z <= size[3] + 1; z++) {
                    boolean isX = x == size[0] - 1 || x == size[1] + 1;
                    boolean isZ = z == size[2] - 1 || z == size[3] + 1;
                    boolean ignore = (isX && !isZ ? !air(pPos.add(OLEPOSSUtils.closerToZero(x), 0, z)) :
                        !isX && isZ && !air(pPos.add(x, 0, OLEPOSSUtils.closerToZero(z)))) && !(x == 0 && z == 0);
                    if (isX != isZ && !ignore) {
                        list.add(pPos.add(x, 0, z));
                    } else if (!isX && !isZ && floor.get() && air(pPos.add(x, 0, z))) {
                        list.add(pPos.add(x, -1, z));
                    }
                }
            }
        }
        return list;
    }

    boolean air(BlockPos pos) {return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);}

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
