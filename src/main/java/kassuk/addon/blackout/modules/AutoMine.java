package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
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
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
    public AutoMine() {super(BlackOut.BLACKOUT, "AutoMine", "For the times your too lazy or bad to press your break bind");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgValue = settings.createGroup("Value");
    private final SettingGroup sgCrystal = settings.createGroup("Crystal");
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

    //  Value Page
    private final Setting<Integer> antiSurround = sgValue.add(new IntSetting.Builder()
        .name("Anti Surround Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> surroundCev = sgValue.add(new IntSetting.Builder()
        .name("Surround Cev Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> trapCev = sgValue.add(new IntSetting.Builder()
        .name("Trap Cev Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> autoCity = sgValue.add(new IntSetting.Builder()
        .name("Auto City Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> cev = sgValue.add(new IntSetting.Builder()
        .name("Cev Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );

    //  Crystal Page
    private final Setting<Boolean> crystal = sgCrystal.add(new BoolSetting.Builder()
        .name("Crystal")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> placeCrystal = sgCrystal.add(new BoolSetting.Builder()
        .name("Place Crystal")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> expCrystal = sgCrystal.add(new BoolSetting.Builder()
        .name("Explode Crystal")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> placeDelay = sgCrystal.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between crystal places")
        .defaultValue(0.3)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Boolean> instant = sgCrystal.add(new BoolSetting.Builder()
        .name("Instant Crystal")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> crystalDelay = sgCrystal.add(new DoubleSetting.Builder()
        .name("Crystal Delay")
        .description(".")
        .defaultValue(0.125)
        .range(0, 1)
        .sliderRange(0, 1)
        .visible(() -> !instant.get())
        .build()
    );
    private final Setting<Integer> antiBurrow = sgValue.add(new IntSetting.Builder()
        .name("Anti Burrow Value")
        .description("0 = disabled")
        .defaultValue(0)
        .range(0, 100)
        .sliderRange(0, 100)
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

    double tick;
    BlockPos targetPos;
    BlockPos crystalPos;
    int targetValue;
    int lastValue;
    double timer = 0;

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
        if (targetPos != null) {
            SettingUtils.swing(SwingState.Pre, SwingType.Mining);
            if (SettingUtils.shouldRotate(RotationType.Breaking)) {
                Managers.ROTATION.start(OLEPOSSUtils.getBox(targetPos), 9);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Post event) {
        if (targetPos != null) {
            SettingUtils.swing(SwingState.Post, SwingType.Mining);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) {return;}

        calc();
        tick = Math.max(0, tick - event.frameTime * 20);
        timer = Math.min(placeDelay.get(), timer + event.frameTime);

        if (targetPos != null) {
            //Render
            Vec3d v = OLEPOSSUtils.getMiddle(targetPos);
            double d = tick / getMineTicks(targetPos);
            double progress = 0.5 - (d * d * d / 2);
            double p = tick / getMineTicks(targetPos);
            int[] c = getColor(startColor.get(), endColor.get(), smooth.get() ? 1 - p : Math.floor(1 - p));

            Box toRender = new Box(v.x - progress, v.y - progress, v.z - progress, v.x + progress, v.y + progress, v.z + progress);
            event.renderer.box(toRender, new Color(c[0], c[1], c[2],
                    (int) Math.floor(c[3] / 5f)),
                new Color(c[0], c[1], c[2], c[3]), ShapeMode.Both, 0);

            //Other Stuff
            if (!SettingUtils.inPlaceRange(targetPos)) {
                calc();
            }
            if (tick <= 0 && (holdingBest(targetPos) || (silent.get()) && (!pauseEat.get() || !mc.player.isUsingItem()))) {
                if (crystal.get() && crystalPos != null) {
                    Entity at = isAt(targetPos, crystalPos);
                    Hand hand = getHand(Items.END_CRYSTAL);
                    if (at != null) {
                        end(targetPos);
                        if (expCrystal.get() && at instanceof EndCrystalEntity) {
                            if (instant.get()) {
                                attackID(at.getId(), at.getPos());
                            } else {
                                Managers.DELAY.add(() -> attackID(at.getId(), at.getPos()), (float) (crystalDelay.get() * 1f));
                            }
                        }
                        targetPos = null;
                        crystalPos = null;
                    } else if (hand != null && timer >= placeDelay.get() && placeCrystal.get()
                        && placeCrystal.get() && !EntityUtils.intersectsWithEntity(new Box(crystalPos), entity -> !entity.isSpectator())) {
                        timer = 0;
                        if (SettingUtils.shouldRotate(RotationType.Crystal)) {
                            Managers.ROTATION.start(crystalPos.down(), 9);
                        }

                        SettingUtils.swing(SwingState.Pre, SwingType.Crystal);

                        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand,
                            new BlockHitResult(OLEPOSSUtils.getMiddle(crystalPos.down()), Direction.UP, crystalPos.down(), false), 0));

                        SettingUtils.swing(SwingState.Post, SwingType.Crystal);

                        if (SettingUtils.shouldRotate(RotationType.Crystal)) {
                            Managers.ROTATION.end(crystalPos.down());
                        }
                    }
                } else {
                    end(targetPos);
                    targetPos = null;
                    crystalPos = null;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(BlockUpdateEvent event) {
        if (mc.player != null && mc.world != null && targetPos != null) {
            if (event.newState.getBlock() != Blocks.AIR && event.oldState.getBlock() == Blocks.AIR) {
                if (event.pos == targetPos) {
                    tick = getMineTicks(event.pos);
                }
            }
        }
    }

    void attackID(int id, Vec3d pos) {
        EndCrystalEntity en = new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z);
        en.setId(id);
        Box box = new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);

        if (SettingUtils.shouldRotate(RotationType.Attacking)) {
            Managers.ROTATION.start(box, 9);
        }

        SettingUtils.swing(SwingState.Post, SwingType.Attacking);

        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));

        SettingUtils.swing(SwingState.Post, SwingType.Attacking);

        if (SettingUtils.shouldRotate(RotationType.Attacking)) {
            Managers.ROTATION.end(box);
        }
    }

    Entity isAt(BlockPos pos, BlockPos crystalPos) {
        for (Entity en : mc.world.getEntities()) {
            if ((en.getBlockPos().equals(crystalPos) && en.getType().equals(EntityType.END_CRYSTAL)) ||
                (!(en instanceof ItemEntity) &&  en.getBoundingBox().intersects(OLEPOSSUtils.getBox(pos)))) {
                return en;
            }
        }
        return null;
    }

    void calc() {
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
            if ((!pos1.equals(targetPos) && (targetValue == -1 || (targetValue > lastValue)) && SettingUtils.inPlaceRange(pos1)) ||
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

    void reset() {
        targetPos = null;
        crystalPos = null;
        lastValue = -1;
        targetValue = -1;
    }

    BlockPos[] getPos() {
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

    boolean valueCheck(int currentValue, int value, BlockPos pos, BlockPos closest) {
        if (value == 0) {return false;}
        boolean rur;
        if (closest == null) {
            rur = true;
        } else {
            rur = OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(pos), mc.player.getEyePos()) <
                OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(closest), mc.player.getEyePos());
        }
        return ((currentValue <= value && rur) || currentValue < value) && SettingUtils.inPlaceRange(pos);
    }

    int is(BlockPos pos) {
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

    Hand getHand(Item item) {
        if (mc.player.getOffHandStack().getItem() == item) {
            return Hand.OFF_HAND;
        } else if (Managers.HOLDING.isHolding(Items.END_CRYSTAL)) {
            return Hand.MAIN_HAND;
        }
        return null;
    }


    boolean holdingBest(BlockPos pos) {
        int slot = fastestSlot(pos);
        return slot != 1 && Managers.HOLDING.slot == slot;
    }


    void start(BlockPos pos) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
            pos, Direction.UP));
    }

    void end(BlockPos pos) {
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

    int getMineTicks(BlockPos pos) {
        double multi = 1;
        if (fastestSlot(pos) != -1) {
            multi = mc.player.getInventory().getStack(fastestSlot(pos)).getMiningSpeedMultiplier(mc.world.getBlockState(pos));
        }
        return (int) Math.round(mc.world.getBlockState(pos).getBlock().getHardness() / multi / speed.get() * 20);
    }

    int fastestSlot(BlockPos pos) {
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

    int[] getColor(Color start, Color end, double progress) {
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
