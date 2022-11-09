package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class BedBomb extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swing")
        .defaultValue(true)
        .build()
    );
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


    public enum LogicMode {
        PlaceBreak,
        BreakPlace
    }
    BlockPos placePos;



    public BedBomb() {
        super(BlackOut.ANARCHY, "BedBomb", "So u don't need to die");
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
            if (OLEPOSSUtils.isBedBlock(event.newState.getBlock()) && event.pos.equals(placePos)) {
                if (!instant.get() && mode.get() == LogicMode.PlaceBreak) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                        new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()),
                            Direction.UP, placePos, false), 0));
                    if (swing.get()) {
                        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }
                }
            }
        }
    }

    private PlayerEntity findTarget() {
        PlayerEntity closest = null;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl)) {
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
            if (OLEPOSSUtils.isBedBlock(mc.world.getBlockState(pos.up()).getBlock()) && OLEPOSSUtils.isBedBlock(mc.world.getBlockState(dirPos).getBlock())) {
                return dir;
            }
            if (mc.world.getBlockState(dirPos).getBlock() == Blocks.AIR) {
                Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                if (closest == null) {
                    closest = dir;
                }
                else if (OLEPOSSUtils.distance(mc.player.getPos(), new Vec3d(dirPos.getX() + 0.5, dirPos.getY() + 0.5, dirPos.getZ() + 0.5)) < range.get() &&
                    EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !(
                        entity.getBlockPos().equals(pos.up()) && entity.getType() != EntityType.ITEM))) {
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
            if (swing.get()) {
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        };

        if (mode.get() == LogicMode.PlaceBreak) {
            run.run();
        }
        if ((instant.get() && mode.get() == LogicMode.PlaceBreak) || (mode.get() == LogicMode.BreakPlace &&
            mc.world.getBlockState(pos).getBlock() != Blocks.AIR)) {
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()),
                    Direction.UP, placePos, false), 0));
            if (swing.get()) {
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
        if (mode.get() == LogicMode.BreakPlace) {
            if (OLEPOSSUtils.isBedBlock(mc.world.getBlockState(placePos).getBlock())) {
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()),
                        Direction.UP, placePos, false), 0));
                if (swing.get()) {
                    mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }
            run.run();
        }
    }

    private boolean isHolding() {
        for (Item item : OLEPOSSUtils.bedItems) {
            if (mc.player.getMainHandStack().getItem() == item) {
                return true;
            }
        }
        return false;
    }
}
