package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoCraftingTable extends Module {
    public AutoCraftingTable() {super(BlackOut.BLACKOUT, "AutoCraftingTable", "Automatically places and opens an Crafting table");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> preSwing = sgGeneral.add(new BoolSetting.Builder()
        .name("Pre Swing")
        .description("Swings before placing")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> postSwing = sgGeneral.add(new BoolSetting.Builder()
        .name("Post Swing")
        .description("Swings after placing")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Range")
        .defaultValue(5)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> minValue = sgGeneral.add(new IntSetting.Builder()
        .name("Min Value")
        .description("Don't change if you dont knwo what it does")
        .defaultValue(-1)
        .range(-6, 12)
        .sliderRange(-6, 12)
        .build()
    );

    BlockPos placePos;

    @Override
    public void onActivate() {
        super.onActivate();
        placePos = findPos();
        if (placePos != null) {
            place(placePos);
        } else {
            this.toggle();
        }
    }

    @EventHandler
    private void onInventory(InventoryEvent event) {
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (event.packet.getSyncId() == handler.syncId) {
            if (handler instanceof CraftingScreenHandler) {
                ChatUtils.sendMsg(Text.of(String.valueOf(handler.slots.size())));
                for (int i = 0; i < 9; i++) {
                    InvUtils.move().from(InvUtils.find(itemStack -> itemStack.getItem().equals(Items.END_CRYSTAL)).slot()).to(i);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(BlockUpdateEvent event) {
        if (mc.player != null && mc.world != null && placePos != null) {
            if (event.newState.getBlock() == Blocks.CRAFTING_TABLE) {
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()),
                        Direction.UP, placePos, false), 0));
            }
        }
    }

    private BlockPos findPos() {
        BlockPos bestPos = null;
        int value = minValue.get();
        for (int x = (int) -Math.ceil(range.get()); x <= Math.ceil(range.get()); x++) {
            for (int y = (int) -Math.ceil(range.get()); y <= Math.ceil(range.get()); y++) {
                for (int z = (int) -Math.ceil(range.get()); z <= Math.ceil(range.get()); z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                    if (pos.getY() > 1 && OLEPOSSUtils.distance(mc.player.getPos(),
                        new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) <= range.get() &&
                        mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) &&
                        !EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !(
                            entity.getBlockPos().equals(pos.up()) && entity.getType() != EntityType.ITEM)))  {
                        if (bestPos == null) {
                            int v = getValue(pos);
                            bestPos = pos;
                            value = v;
                        } else {
                            int v = getValue(pos);
                            if (v > value) {
                                bestPos = pos;
                                value = v;
                            }
                        }
                    }
                }
            }
        }
        return bestPos;
    }

    private int getValue(BlockPos pos) {
        int value = 0;
        for (Direction dir : Direction.values()) {
            if (mc.world.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.BEDROCK)) {
                value += 2;
            } else if (mc.world.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.OBSIDIAN)) {
            } else if (mc.world.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.AIR)) {
                value--;
            }
        }
        return value;
    }

    private void place(BlockPos pos) {
        if (preSwing.get()) {
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        InvUtils.swap(InvUtils.findInHotbar(Items.CRAFTING_TABLE).slot(), true);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
            new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                Direction.UP, pos, false), 0));
        if (postSwing.get()) {
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        InvUtils.swapBack();
    }
}
