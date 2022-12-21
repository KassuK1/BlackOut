package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import com.example.addon.managers.DelayManager;
import com.example.addon.managers.HoldingManager;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
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
    private final Setting<Boolean> silent = sgGeneral.add(new BoolSetting.Builder()
        .name("Silent")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> antiSurround = sgGeneral.add(new IntSetting.Builder()
        .name("Anti Surround Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> surroundCev = sgGeneral.add(new IntSetting.Builder()
        .name("Surround Cev Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> trapCev = sgGeneral.add(new IntSetting.Builder()
        .name("Trap Cev Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> autoCity = sgGeneral.add(new IntSetting.Builder()
        .name("Auto City Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> cev = sgGeneral.add(new IntSetting.Builder()
        .name("Cev Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> antiBurrow = sgGeneral.add(new IntSetting.Builder()
        .name("Anti Burrow Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between crystal places")
        .defaultValue(0.3)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Boolean> instant = sgGeneral.add(new BoolSetting.Builder()
        .name("Instant Crystal")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> instantPick = sgGeneral.add(new BoolSetting.Builder()
        .name("Instant Pickaxe")
        .description(".")
        .defaultValue(true)
        .visible(() -> !instant.get())
        .build()
    );
    private final Setting<Double> crystalDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Crystal Delay")
        .description(".")
        .defaultValue(0.02)
        .range(0, 1)
        .sliderRange(0, 1)
        .visible(() -> !instant.get())
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
        .description("Range for mining")
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

    private double tick;
    private BlockPos targetPos;
    private BlockPos crystalPos;
    private int targetValue;
    private int lastValue;
    private final DelayManager DELAY = new DelayManager();
    private final HoldingManager HOLDING = new HoldingManager();
    private float timer = 0;

    public AutoMine() {
        super(BlackOut.ANARCHY, "AutoMine", "For the times your too lazy or bad to press your break bind");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        timer = 0;
        tick = 0;
        targetPos = null;
        crystalPos = null;
        targetValue = -1;
        lastValue = -1;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            calc();
            if (targetPos != null) {
                if (OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(targetPos), mc.player.getEyePos()) > range.get()) {
                    calc();
                }
                if (tick > 0) {
                    tick--;
                } else if (holdingBest(targetPos) || (silent.get()) && (!pauseEat.get() || !mc.player.isUsingItem())) {
                    if (crystalPos != null) {
                        Entity at = isAt(crystalPos);
                        Hand hand = getHand(Items.END_CRYSTAL);
                        if (at != null) {
                            end(targetPos);
                            if (instant.get() || (instantPick.get() && holdingBest(targetPos))) {
                                attackID(at.getId(), at.getPos());
                            } else {
                                DELAY.add(() -> attackID(at.getId(), at.getPos()), (float) (crystalDelay.get() * 1f));
                            }
                            targetPos = null;
                            crystalPos = null;
                        } else if (hand != null && timer >= placeDelay.get()) {
                            Box box = new Box(crystalPos);
                            if (!EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator())) {
                                timer = 0;
                                mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand,
                                    new BlockHitResult(OLEPOSSUtils.getMiddle(crystalPos.down()), Direction.UP, crystalPos.down(), false), 0));
                            }
                        }
                    } else {
                        abort(targetPos);
                        targetPos = null;
                        crystalPos = null;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(BlockUpdateEvent event) {
        if (mc.player != null && mc.world != null && targetPos != null) {
            BlackOut.LOG.info("AutoMine: Block Change");
            if (event.newState.getBlock() != Blocks.AIR && event.oldState.getBlock() == Blocks.AIR) {
                if (event.pos == targetPos) {
                    tick = getMineTicks(event.pos);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        timer = (float) Math.min(placeDelay.get(), timer + event.frameTime);

        if (mc.player != null && mc.world != null && targetPos != null) {
            Vec3d v = OLEPOSSUtils.getMiddle(targetPos);
            double progress = 0.5 - (tick / getMineTicks(targetPos) / 2);
            double p = tick / getMineTicks(targetPos);
            int[] c = getColor(startColor.get(), endColor.get(), smooth.get() ? 1 - p : Math.floor(1 - p));

            Box toRender = new Box(v.x - progress, v.y - progress, v.z - progress, v.x + progress, v.y + progress, v.z + progress);
            event.renderer.box(toRender, new Color(c[0], c[1], c[2],
                (int) Math.floor(c[3] / 5f)),
                new Color(c[0], c[1], c[2], c[3]), ShapeMode.Both, 0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSpawn(EntityAddedEvent event) {
        if (mc.player != null && mc.world != null && targetPos != null && crystalPos != null) {
            BlockPos pos = event.entity.getBlockPos();
            if (event.entity.getType().equals(EntityType.END_CRYSTAL) && pos.equals(crystalPos) && fastestSlot(pos) >= 0 && tick <= 0) {
                end(targetPos);
                int id = event.entity.getId();
                if (instant.get() || (instantPick.get() && holdingBest(targetPos))) {
                    attackID(id, event.entity.getPos());
                } else {
                    DELAY.add(() -> attackID(id, event.entity.getPos()), (float) (crystalDelay.get() * 1f));
                }
                crystalPos = null;
                targetPos = null;
            }
        }
    }

    private void attackID(int id, Vec3d pos) {
        EndCrystalEntity en = new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z);
        en.setId(id);
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private Entity isAt(BlockPos pos) {
        for (Entity en : mc.world.getEntities()) {
            if (en.getBlockPos().equals(pos) && en.getType().equals(EntityType.END_CRYSTAL)) {
                return en;
            }
        }
        return null;
    }

    private void calc() {
        lastValue = targetValue;
        BlockPos[] pos = getPos();
        BlockPos pos1 = pos[0];
        BlockPos pos2 = pos[1];
        boolean valid;
        if (targetPos == null) {
            valid = true;
        } else {
            valid = is(targetPos) == 1;
            if (crystalPos != null) {
                if (is(crystalPos) == 0) {
                    valid = true;
                }
            }
        }
        if (pos1 != null) {
            if ((!pos1.equals(targetPos) && (targetValue == -1 || (targetValue > lastValue)) &&
                OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(pos1), mc.player.getEyePos()) <= range.get()) ||
                valid) {
                tick = getMineTicks(pos1);
                targetPos = pos1;
                crystalPos = pos2;
                start(targetPos);
            }
        } else {
            reset();
        }
    }

    private void reset() {
        targetPos = null;
        crystalPos = null;
        lastValue = -1;
        targetValue = -1;
    }

    private BlockPos[] getPos() {
        int value = 0;
        BlockPos closest = null;
        BlockPos crystal = null;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl)) {
                BlockPos pos = pl.getBlockPos();
                for (Direction dir : OLEPOSSUtils.horizontals) {
                    // Anti Surround
                    if (valueCheck(value, antiSurround.get(), pos.offset(dir), closest)
                        && is(pos.offset(dir)) == 0) {
                        value = antiSurround.get();
                        closest = pos.offset(dir);
                        crystal = null;
                    }

                    // Surround Cev
                    if (valueCheck(value, surroundCev.get(), pos.offset(dir), closest) &&
                        is(pos.offset(dir)) == 0 && is(pos.offset(dir).up()) == 1) {
                        value = surroundCev.get();
                        closest = pos.offset(dir);
                        crystal = pos.offset(dir).up();
                    }
                    // Trap Cev
                    if (valueCheck(value, trapCev.get(), pos.offset(dir).up(), closest) &&
                        is(pos.offset(dir).up()) == 0 && is(pos.offset(dir).up(2)) == 1) {
                        value = trapCev.get();
                        closest = pos.offset(dir).up();
                        crystal = pos.offset(dir).up(2);
                    }
                    // Auto City
                    if (valueCheck(value, autoCity.get(), pos.offset(dir), closest) &&
                        is(pos.offset(dir)) == 0 && is(pos.offset(dir).offset(dir)) == 1 &&
                        is(pos.offset(dir).offset(dir).down()) == 0) {
                        value = autoCity.get();
                        closest = pos.offset(dir);
                        crystal = pos.offset(dir).offset(dir);
                    }
                    // Cev
                    if (valueCheck(value, cev.get(), pos.up(2), closest)
                        && is(pos.up(2)) == 0 && is(pos.up(3)) == 1) {
                        value = cev.get();
                        closest = pos.up(2);
                        crystal = pos.up(3);
                    }
                    // Anti Burrow
                    if (valueCheck(value, antiBurrow.get(), pos, closest)
                        && is(pl.getBlockPos()) == 0) {
                        value = antiBurrow.get();
                        closest = pl.getBlockPos();
                        crystal = null;
                    }
                }
            }
        }
        targetValue = value;
        return new BlockPos[] {closest, crystal};
    }

    private boolean valueCheck(int currentValue, int value, BlockPos pos, BlockPos closest) {
        if (value == 0) {return false;}
        boolean rur;
        if (closest == null) {
            rur = true;
        } else {
            rur = OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(pos), mc.player.getEyePos()) <
                OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(closest), mc.player.getEyePos());
        }
        return ((currentValue <= value && rur) || currentValue < value) &&
            OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(pos), mc.player.getEyePos()) < range.get();
    }

    private int is(BlockPos pos) {
        if (mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK)) {
            return -1;
        }
        if (mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
            return 1;
        }
        if (mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN)) {
            return 0;
        }
        return 2;
    }

    private Hand getHand(Item item) {
        if (mc.player.getOffHandStack().getItem() == item) {
            return Hand.OFF_HAND;
        } else if (HOLDING.isHolding(Items.END_CRYSTAL)) {
            return Hand.MAIN_HAND;
        }
        return null;
    }


    private boolean holdingBest(BlockPos pos) {
        int slot = fastestSlot(pos);
        return slot != 1 && HOLDING.slot == slot;
    }


    private void start(BlockPos pos) {
        BlackOut.LOG.info("AutoMine: Start");
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
            pos, Direction.UP));
        if (swing.get() && swingOnce.get()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void end(BlockPos pos) {
        BlackOut.LOG.info("AutoMine: End");
        int slot = fastestSlot(pos);
        boolean swapped = false;
        if (silent.get() && !holdingBest(pos) && slot != -1) {
            InvUtils.swap(slot, true);
            swapped = true;
        }
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
            pos, Direction.UP));
        if (swapped) {
            InvUtils.swapBack();
        }
    }
    private void abort(BlockPos pos) {
        BlackOut.LOG.info("AutoMine: Abort");
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
            pos, Direction.UP));
    }

    private int getMineTicks(BlockPos pos) {
        double multi = 1;
        if (fastestSlot(pos) != -1) {
            multi = mc.player.getInventory().getStack(fastestSlot(pos)).getMiningSpeedMultiplier(mc.world.getBlockState(pos));
        }
        return (int) Math.round(mc.world.getBlockState(pos).getBlock().getHardness() / multi / speed.get() * 20);
    }

    private int fastestSlot(BlockPos pos) {
        int slot = -1;
        if (mc.player == null || mc.world == null) {return -1;}
        for (int i = 0; i < 9; i++) {
            if (slot == -1 || (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(pos)) >
                mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(mc.world.getBlockState(pos)))) {
                slot = i;
            }
        }
        return slot;
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
