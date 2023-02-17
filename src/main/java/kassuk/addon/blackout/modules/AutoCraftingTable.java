package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
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
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Places in this range.")
        .defaultValue(5)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> minValue = sgGeneral.add(new IntSetting.Builder()
        .name("Min Value")
        .description("Don't change if you don't know what it does")
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
                SettingUtils.swing(SwingState.Pre, SwingType.Interact);
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()),
                        Direction.UP, placePos, false), 0));
                SettingUtils.swing(SwingState.Post, SwingType.Interact);
            }
        }
    }

    BlockPos findPos() {
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

    int getValue(BlockPos pos) {
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

    void place(BlockPos pos) {
        int slot = 420;
        if (Managers.HOLDING.isHolding(Items.CRAFTING_TABLE)) {
            slot = InvUtils.findInHotbar(Items.CRAFTING_TABLE).slot();
            if (slot >= 0 && slot < 420) {
                InvUtils.swap(slot, true);
            }
        }
        if (slot >= 0) {
            if (SettingUtils.shouldRotate(RotationType.Placing)) {
                Managers.ROTATION.start(pos, 4, RotationType.Placing);
            }

            SettingUtils.swing(SwingState.Pre, SwingType.Placing);

            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                    Direction.UP, pos, false), 0));

            SettingUtils.swing(SwingState.Post, SwingType.Placing);

            if (SettingUtils.shouldRotate(RotationType.Placing)) {
                Managers.ROTATION.end(pos);
            }

            if (slot < 420) {
                InvUtils.swapBack();
            }
        }
    }
}
