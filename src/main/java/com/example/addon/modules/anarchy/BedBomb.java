package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class BedBomb extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<LogicMode> mode = sgGeneral.add(new EnumSetting.Builder<LogicMode>()
        .name("Mode")
        .description("Logic for bullying kids.")
        .defaultValue(LogicMode.BreakPlace)
        .build()
    );
    private final Setting<Boolean> instant = sgGeneral.add(new BoolSetting.Builder()
        .name("Instant")
        .description("Sends place and break packets at once")
        .defaultValue(true)
        .visible(() -> mode.get() == LogicMode.PlaceBreak)
        .build()
    );
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Speed")
        .defaultValue(10)
        .range(0, 10)
        .sliderMax(10)
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
    private Direction[] horizontals = new Direction[] {
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    };
    private int ticks;
    private Item[] bedItems = new Item[] {
        Items.BLACK_BED,
        Items.BLUE_BED,
        Items.BROWN_BED,
        Items.CYAN_BED,
        Items.GRAY_BED,
        Items.GREEN_BED,
        Items.LIGHT_BLUE_BED,
        Items.LIGHT_GRAY_BED,
        Items.LIME_BED,
        Items.MAGENTA_BED,
        Items.ORANGE_BED,
        Items.PINK_BED,
        Items.RED_BED,
        Items.WHITE_BED,
        Items.YELLOW_BED,
        Items.PURPLE_BED
    };
    private Block[] bedBlocks = new Block[] {
        Blocks.BLACK_BED,
        Blocks.BLUE_BED,
        Blocks.BROWN_BED,
        Blocks.CYAN_BED,
        Blocks.GRAY_BED,
        Blocks.GREEN_BED,
        Blocks.LIGHT_BLUE_BED,
        Blocks.LIGHT_GRAY_BED,
        Blocks.LIME_BED,
        Blocks.MAGENTA_BED,
        Blocks.ORANGE_BED,
        Blocks.PINK_BED,
        Blocks.RED_BED,
        Blocks.WHITE_BED,
        Blocks.YELLOW_BED,
        Blocks.PURPLE_BED
    };

    public enum LogicMode {
        PlaceBreak,
        BreakPlace
    }
    BlockPos placePos;



    public BedBomb() {
        super(Addon.ANARCHY, "BedBomb", "So u don't need to die");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            PlayerEntity target = findTarget();
            ticks++;
            if (target != null && isHolding()) {
                Direction direction = findSide(target);
                if (direction != null) {
                    if (ticks >= delay.get()) {
                        ticks = 0;
                        place(direction, target);
                    }
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(BlockUpdateEvent event) {
        if (mc.player != null && mc.world != null && placePos != null) {
            if (isBedBlock(event.newState.getBlock()) && event.pos.equals(placePos)) {
                if (!instant.get() && mode.get() == LogicMode.PlaceBreak) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                        new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()),
                            Direction.UP, placePos, false), 0));
                }
            }
        }
    }

    private PlayerEntity findTarget() {
        PlayerEntity closest = null;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player) {
                if (closest == null) {
                    closest = pl;
                } else if (OLEPOSSUtils.distance(mc.player.getPos(), pl.getPos()) < OLEPOSSUtils.distance(mc.player.getPos(), closest.getPos())) {
                    closest = pl;
                }
            }
        }
        return closest;
    }

    private Direction findSide(PlayerEntity pl) {
        BlockPos pos = pl.getBlockPos();
        Direction closest = null;
        for (Direction dir : horizontals) {
            BlockPos dirPos = pos.offset(dir).up();
            if (mc.world.getBlockState(dirPos).getBlock() == Blocks.AIR) {
                if (closest == null) {
                    closest = dir;
                }
                else if (OLEPOSSUtils.distance(mc.player.getPos(), new Vec3d(dirPos.getX() + 0.5, dirPos.getY() + 0.5, dirPos.getZ() + 0.5)) < range.get()) {
                    BlockPos epicPos = pos.offset(closest).up();
                    if (OLEPOSSUtils.distance(mc.player.getPos(), new Vec3d(dirPos.getX() + 0.5, dirPos.getY() + 0.5, dirPos.getZ() + 0.5)) <
                        OLEPOSSUtils.distance(mc.player.getPos(), new Vec3d(epicPos.getX() + 0.5, epicPos.getY() + 0.5, epicPos.getZ() + 0.5))) {
                        closest = dir;
                    }
                }
            }
        }
        return closest;
    }

    private void place(Direction dir, PlayerEntity pl) {
        BlockPos pos = pl.getBlockPos().offset(dir).up();
        placePos = pos;
        Runnable run = () -> {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(dir.getOpposite().asRotation(),
                mc.player.getPitch(), mc.player.isOnGround()));
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(pos.getX(), pos.getY() - 1, pos.getZ()),
                    Direction.UP, pos.down(), false), 0));
        };

        if (mode.get() == LogicMode.PlaceBreak) {
            run.run();
        }
        if ((instant.get() && mode.get() == LogicMode.PlaceBreak) || (mode.get() == LogicMode.BreakPlace &&
            mc.world.getBlockState(pos).getBlock() != Blocks.AIR)) {
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()),
                    Direction.UP, placePos, false), 0));
        }
        if (mode.get() == LogicMode.BreakPlace) {
            run.run();
        }
    }

    private boolean isHolding() {
        for (Item item : bedItems) {
            if (mc.player.getMainHandStack().getItem() == item) {
                return true;
            }
        }
        return false;
    }

    private boolean isBedBlock(Block block) {
        for (Block bed : bedBlocks) {
            if (block == bed) {
                return true;
            }
        }
        return false;
    }
    private boolean isBedItem(Item item) {
        for (Item bed : bedItems) {
            if (item == bed) {
                return true;
            }
        }
        return false;
    }
}
