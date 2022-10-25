package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> crystal = sgGeneral.add(new BoolSetting.Builder()
        .name("Crystal")
        .description("YES")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
        .name("Ticks")
        .description("Ticks")
        .defaultValue(50)
        .range(0, 100)
        .sliderMax(100)
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
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 255))
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
                        if (!targetPos.equals(target.getBlockPos().offset(side))) {
                            targetPos = target.getBlockPos().offset(side);
                            tick = speed.get();
                            start(target.getBlockPos().offset(side));
                        } else {
                            if (targetPos != null && tick > 0) {
                                tick--;
                                if (swing.get() && !swingOnce.get()) {
                                    mc.player.swingHand(Hand.MAIN_HAND);
                                }
                            } else if (targetPos != null) {
                                if (tick == 0 && holdingPick()) {
                                    end(target.getBlockPos().offset(side));
                                    targetPos = null;
                                    tick = -1;
                                }
                            }
                        }
                    } else {
                        targetPos = target.getBlockPos().offset(side);
                        tick = speed.get();
                        start(target.getBlockPos().offset(side));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(BlockUpdateEvent event) {
        if (mc.player != null && mc.world != null && targetPos != null && side != null) {
            if (event.newState.getBlock() == Blocks.OBSIDIAN && event.oldState.getBlock() == Blocks.AIR) {
                if (event.pos == targetPos) {
                    tick = speed.get();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player != null && mc.world != null && targetPos != null && side != null) {
            Vec3d v = new Vec3d(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
            double progress = 0.5 - (tick / speed.get() / 2);
            Box toRender = new Box(v.x - progress, v.y - progress, v.z - progress, v.x + progress, v.y + progress, v.z + progress);
            event.renderer.box(toRender, new Color(color.get().r, color.get().g, color.get().b,
                (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both, 0);
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


    private boolean holdingPick() {
        return mc.player.getMainHandStack().getItem() == pickaxes[0] ||
            mc.player.getMainHandStack().getItem() == pickaxes[1];
    }

    private Direction getSide(PlayerEntity en) {
        Direction cloSide = null;
        Double dist = null;
        for (Direction dir : horizontals) {
            BlockPos pos = en.getBlockPos().offset(dir);
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
        Hand handToUse = getHand(Items.END_CRYSTAL);
        if (handToUse != null && crystal.get()) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(handToUse,
                new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false), 0));
        }
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
            pos, Direction.UP));
    }

    private PlayerEntity getTarget() {
        PlayerEntity closest = null;
        Vec3d closestPos = null;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player) {
                if (getSide(pl) != null) {
                    BlockPos pos = pl.getBlockPos().offset(getSide(pl));
                    if (closest == null) {
                        closest = pl;
                        closestPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                    } else {
                        if (distance(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), mc.player.getEyePos()) <
                            distance(closestPos, mc.player.getEyePos())) {
                            closest = pl;
                            closestPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                        }
                    }
                }
            }
        }
        return closest;
    }

    private double distance(Vec3d v1, Vec3d v2) {
        double dX = Math.abs(v1.x - v2.x);
        double dY = Math.abs(v1.y - v2.y);
        double dZ = Math.abs(v1.z - v2.z);
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }
}
