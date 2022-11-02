package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoMine extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swing")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> swingOnce = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing Once")
        .description("Swing but once")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> antiSurround = sgGeneral.add(new BoolSetting.Builder()
        .name("Surround")
        .description("Mines web")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> antiWeb = sgGeneral.add(new BoolSetting.Builder()
        .name("Web")
        .description("Mines web")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> antiBurrow = sgGeneral.add(new BoolSetting.Builder()
        .name("Burrow")
        .description("Mines burrow")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Ticks")
        .defaultValue(1)
        .range(0.1, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("hmm yes")
        .defaultValue(6)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> smooth = sgGeneral.add(new BoolSetting.Builder()
        .name("Smooth Color")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> startColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Start Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> endColor = sgGeneral.add(new ColorSetting.Builder()
        .name("End Color")
        .description(".")
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .build()
    );
    private Direction[] horizontals = new Direction[] {
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    };

    private Item[] pickaxes = new Item[] {
        Items.NETHERITE_PICKAXE,
        Items.DIAMOND_PICKAXE
    };
    private double tick;
    private BlockPos targetPos;
    private Direction side;


    public AutoMine() {
        super(Addon.ANARCHY, "AutoMine", "Evil");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        tick = 0;
        targetPos = null;
        side = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            PlayerEntity target = getTarget();
            if (target != null) {
                side = getSide(target);
                if (side != null) {
                    if (targetPos != null) {
                        if (side == Direction.UP) {
                            if (!targetPos.equals(target.getBlockPos())) {
                                targetPos = target.getBlockPos();
                                tick = getMineTicks(targetPos);
                                start(target.getBlockPos());
                            } else {
                                if (targetPos != null && tick > 0) {
                                    tick--;
                                    if (swing.get() && !swingOnce.get()) {
                                        mc.player.swingHand(Hand.MAIN_HAND);
                                    }
                                } else if (targetPos != null) {
                                    if (tick == 0 && holdingBest(targetPos)) {
                                        end(target.getBlockPos());
                                        targetPos = null;
                                        tick = -1;
                                    }
                                }
                            }
                        } else {
                            if (!targetPos.equals(target.getBlockPos().offset(side))) {
                                targetPos = target.getBlockPos().offset(side);
                                tick = getMineTicks(targetPos);
                                start(target.getBlockPos().offset(side));
                            } else {
                                if (targetPos != null && tick > 0) {
                                    tick--;
                                    if (swing.get() && !swingOnce.get()) {
                                        mc.player.swingHand(Hand.MAIN_HAND);
                                    }
                                } else if (targetPos != null) {
                                    if (tick == 0 && holdingBest(targetPos)) {
                                        end(target.getBlockPos().offset(side));
                                        targetPos = null;
                                        tick = -1;
                                    }
                                }
                            }
                        }
                    } else {
                        if (side == Direction.UP) {
                            targetPos = target.getBlockPos();
                            tick = getMineTicks(targetPos);
                            start(target.getBlockPos());
                        } else {
                            targetPos = target.getBlockPos().offset(side);
                            tick = getMineTicks(targetPos);
                            start(target.getBlockPos().offset(side));
                        }
                    }
                } else {
                    targetPos = null;
                }
            } else {
                targetPos = null;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(BlockUpdateEvent event) {
        if (mc.player != null && mc.world != null && targetPos != null && side != null) {
            if (event.newState.getBlock() != Blocks.AIR && event.oldState.getBlock() == Blocks.AIR) {
                if (event.pos == targetPos) {
                    tick = getMineTicks(event.pos);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player != null && mc.world != null && targetPos != null && side != null) {
            Vec3d v = new Vec3d(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
            double progress = 0.5 - (tick / getMineTicks(targetPos) / 2);
            double p = tick / getMineTicks(targetPos);
            int[] c = getColor(startColor.get(), endColor.get(), smooth.get() ? 1 - p : Math.floor(1 - p));

            Box toRender = new Box(v.x - progress, v.y - progress, v.z - progress, v.x + progress, v.y + progress, v.z + progress);
            event.renderer.box(toRender, new Color(c[0], c[1], c[2],
                (int) Math.floor(c[3] / 5f)),
                new Color(c[0], c[1], c[2], c[3]), ShapeMode.Both, 0);
        }
    }

    private Hand getHand(Item item) {
        if (mc.player.getOffHandStack().getItem() == item) {
            return Hand.OFF_HAND;
        } else if (mc.player.getMainHandStack().getItem() == item) {
            return Hand.MAIN_HAND;
        }
        return null;
    }


    private boolean holdingBest(BlockPos pos) {
        return InvUtils.findFastestTool(mc.world.getBlockState(pos)).isMainHand();
    }

    private Direction getSide(PlayerEntity en) {
        Direction cloSide = null;
        Double dist = null;
        BlockPos pos = en.getBlockPos();
        if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR || en == mc.player &&
            distance(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                mc.player.getEyePos()) <= range.get()) {
            return Direction.UP;
        }
        for (Direction dir : horizontals) {
            pos = en.getBlockPos().offset(dir);
            if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN
            && distance(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                mc.player.getEyePos()) <= range.get()) {
                if (dist == null || distance(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                    mc.player.getEyePos()) < dist) {
                    cloSide = dir;
                    dist = distance(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                        mc.player.getEyePos());
                }
            }
        }
        return cloSide;
    }

    private void start(BlockPos pos) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
            pos, Direction.UP));
        if (swing.get() && swingOnce.get()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void end(BlockPos pos) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
            pos, Direction.UP));
    }

    private PlayerEntity getTarget() {
        PlayerEntity closest = null;
        Vec3d closestPos = null;
        int value = 0;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl)) {
                if (antiBurrow.get())  {
                    BlockPos pos = pl.getBlockPos();
                    if (mc.world.getBlockState(pl.getBlockPos()).getBlock() != Blocks.AIR) {
                        if (closest == null) {
                            closest = pl;
                            value = 3;
                            closestPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                        } else if (distance(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), mc.player.getEyePos()) <
                            distance(closestPos, mc.player.getEyePos()) || value < 3) {
                            closest = pl;
                            value = 3;
                            closestPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                        }
                    }
                }

                if (antiSurround.get() && value <= 2) {
                    if (getSide(pl) != null) {
                        BlockPos pos = pl.getBlockPos().offset(getSide(pl));
                        if (closest == null) {
                            closest = pl;
                            value = 2;
                            closestPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                        } else {
                            if (distance(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), mc.player.getEyePos()) <
                                distance(closestPos, mc.player.getEyePos())) {
                                closest = pl;
                                value = 2;
                                closestPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                            }
                        }
                    }
                }
            }

            if (antiWeb.get() && value <= 1) {
                BlockPos pos = mc.player.getBlockPos();
                if (mc.world.getBlockState(pl.getBlockPos()).getBlock() == Blocks.COBWEB) {
                    closest = mc.player;
                    value = 1;
                    closestPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return closest;
    }

    private int getMineTicks(BlockPos pos) {
        double multi = 1;
        if (fastestSlot(pos) != -1) {
            multi = mc.player.getInventory().getStack(fastestSlot(pos)).getMiningSpeedMultiplier(mc.world.getBlockState(pos));
        }
        return (int) Math.round(mc.world.getBlockState(pos).getBlock().getHardness() / multi / speed.get());
    }

    private int fastestSlot(BlockPos pos) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(pos)) >
                mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(pos))) {
                slot = i;
            }
        }
        return slot;
    }

    private double distance(Vec3d v1, Vec3d v2) {
        double dX = Math.abs(v1.x - v2.x);
        double dY = Math.abs(v1.y - v2.y);
        double dZ = Math.abs(v1.z - v2.z);
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    private int[] getColor(Color start, Color end, double progress) {
        double r = (end.r - start.r) * progress;
        double g = (end.g - start.g) * progress;
        double b = (end.b - start.b) * progress;
        double a = (end.a - start.a) * progress;
        return new int[] {
            (int) Math.round(start.r + r),
            (int) Math.round(start.g + g),
            (int) Math.round(start.b + b),
            (int) Math.round(start.a + a)};
    }
}
