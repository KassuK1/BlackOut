package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Vec2;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
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

public class BedBomb extends Module {
    public BedBomb() {super(BlackOut.BLACKOUT, "BedBomb", "Automatically places and breaks beds to cause damage to your opponents");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swing")
        .defaultValue(true)
        .build()
    );
    public final Setting<CalcMode> calcMode = sgGeneral.add(new EnumSetting.Builder<CalcMode>()
        .name("Calc Mode")
        .description("Logic for bullying kids.")
        .defaultValue(CalcMode.Tick)
        .build()
    );
    private final Setting<Boolean> silent = sgGeneral.add(new BoolSetting.Builder()
        .name("Silent")
        .description("Places even when you arent holding")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between places at same block (should be about 2 * ping)")
        .defaultValue(0.5)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Boolean> instantExp = sgGeneral.add(new BoolSetting.Builder()
        .name("Instant Explode")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> expDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Explode Delay")
        .description(".")
        .defaultValue(0.1)
        .range(0, 5)
        .sliderRange(0, 5)
        .visible(() -> !instantExp.get())
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
    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Double> animationSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Animation Move Speed")
        .description(".")
        .defaultValue(5)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> rotationSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Animation Rotation Speed")
        .description(".")
        .defaultValue(5)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> minDamage = sgGeneral.add(new DoubleSetting.Builder()
        .name("Min Damage")
        .description("Delay between places at same block (should be about 2 * ping)")
        .defaultValue(10)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxDamage = sgGeneral.add(new DoubleSetting.Builder()
        .name("Max Damage")
        .description("Delay between places at same block (should be about 2 * ping)")
        .defaultValue(6)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxDamageRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Damage Ratio")
        .description("Delay between places at same block (should be about 2 * ping)")
        .defaultValue(0.3)
        .range(0, 2)
        .sliderRange(0, 2)
        .build()
    );
    private final Setting<Boolean> notHoldingTotem = sgDamage.add(new BoolSetting.Builder()
        .name("No Totem")
        .description("Only targets enemies that arent holding totems.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> totemTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Totem Time")
        .description("")
        .defaultValue(0.5)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Boolean> popStuff = sgDamage.add(new BoolSetting.Builder()
        .name("Pop Stuff")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> forcePop = sgDamage.add(new IntSetting.Builder()
        .name("Force Pop")
        .description("Ignores damage limits if enemy will pop in x hits")
        .defaultValue(2)
        .range(0, 10)
        .sliderMax(10)
        .visible(popStuff::get)
        .build()
    );
    private final Setting<Integer> antiPop = sgDamage.add(new IntSetting.Builder()
        .name("Anti Pop")
        .description("Doesn't place if you can pop in x hits.")
        .defaultValue(1)
        .range(0, 10)
        .sliderMax(10)
        .visible(popStuff::get)
        .build()
    );
    public enum CalcMode {
        Tick,
        Move,
        Render,
        OnPlace
    }
    BlockPos bedPos;
    Direction bedDir;
    Vec3d renderPos;
    float renderDir;
    BlockPos lastPos = null;
    double timer = 0;
    boolean explode = false;
    List<NotHolding> holdings = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {if (calcMode.get().equals(CalcMode.Tick)) {update();}}
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {if (calcMode.get().equals(CalcMode.Move)) {update();}}
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        timer = Math.min(delay.get(), timer + event.frameTime);
        if (mc.world != null) {
            mc.world.getPlayers().forEach(player -> {
                String name = player.getName().getString();
                NotHolding holding = getHolding(name);
                if (holding != null) {
                    holding.update(event.frameTime, player.getMainHandStack().getItem().equals(Items.TOTEM_OF_UNDYING) || player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING));
                } else {
                    holdings.add(new NotHolding(name));
                }
            });
        }
        if (calcMode.get().equals(CalcMode.Render) || calcMode.get().equals(CalcMode.OnPlace) && timer >= delay.get()) {update();}
        if (bedPos != null) {
            renderPos = smoothMove(renderPos, new Vec3d(bedPos.offset(bedDir).getX(), bedPos.offset(bedDir).getY(), bedPos.offset(bedDir).getZ()), (float) (event.frameTime * animationSpeed.get() * 2));
            renderDir = smoothTurn(renderDir + 180, bedDir.getOpposite().asRotation() + 180, (float) (event.frameTime * rotationSpeed.get() * 2)) - 180;
        }
        if (bedPos != null) {
            double yaw = renderDir + 90;
            double cos = Math.cos(Math.toRadians(yaw + 90));
            double sin = Math.sin(Math.toRadians(yaw + 90));
            Vec3d vec = new Vec3d(renderPos.getX() + 0.5 - cos / 2 - sin / 2, renderPos.getY(), renderPos.getZ() + 0.5 + cos / 2 - sin / 2);
            Vec2 corner1 = new Vec2(vec.x, vec.z);
            Vec2 corner2 = new Vec2(vec.x + Math.cos(Math.toRadians(yaw)) * 2, vec.z + Math.sin(Math.toRadians(yaw)) * 2);
            Vec2 corner3 = new Vec2(vec.x + cos, vec.z + sin);
            Vec2 corner4 = new Vec2(vec.x + Math.cos(Math.toRadians(yaw + 26.50511770779)) * Math.sqrt(5), vec.z + Math.sin(Math.toRadians(yaw + 26.50511770779)) * Math.sqrt(5));

            event.renderer.line(corner1.x, vec.getY(), corner1.y, corner2.x, vec.getY(), corner2.y, new Color(255, 0, 0, 255));
            event.renderer.line(corner1.x, vec.getY(), corner1.y, corner3.x, vec.getY(), corner3.y, new Color(255, 255, 0, 255));
            event.renderer.line(corner2.x, vec.getY(), corner2.y, corner4.x, vec.getY(), corner4.y, new Color(0, 255, 0, 255));
            event.renderer.line(corner3.x, vec.getY(), corner3.y, corner4.x, vec.getY(), corner4.y, new Color(0, 0, 255, 255));

            event.renderer.side(corner1.x, vec.getY() + 0.5, corner1.y,
                            corner2.x, vec.getY() + 0.5, corner2.y,
                            corner4.x, vec.getY() + 0.5, corner4.y,
                            corner3.x, vec.getY() + 0.5, corner3.y,
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both);
            event.renderer.side(corner1.x, vec.getY(), corner1.y,
                corner2.x, vec.getY(), corner2.y,
                corner4.x, vec.getY(), corner4.y,
                corner3.x, vec.getY(), corner3.y,
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both);

            event.renderer.side(corner1.x, vec.getY() + 0.5, corner1.y,
                corner1.x, vec.getY(), corner1.y,
                corner2.x, vec.getY(), corner2.y,
                corner2.x, vec.getY() + 0.5, corner2.y,
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both);
            event.renderer.side(corner3.x, vec.getY() + 0.5, corner3.y,
                corner3.x, vec.getY(), corner3.y,
                corner4.x, vec.getY(), corner4.y,
                corner4.x, vec.getY() + 0.5, corner4.y,
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both);

            event.renderer.side(corner3.x, vec.getY() + 0.5, corner3.y,
                corner3.x, vec.getY(), corner3.y,
                corner1.x, vec.getY(), corner1.y,
                corner1.x, vec.getY() + 0.5, corner1.y,
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both);
            event.renderer.side(corner4.x, vec.getY() + 0.5, corner4.y,
                corner4.x, vec.getY(), corner4.y,
                corner2.x, vec.getY(), corner2.y,
                corner2.x, vec.getY() + 0.5, corner2.y,
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both);
        }
    }

    void update() {
        if (mc.player == null || mc.world == null) {return;}
        int slot =  bedSlot();
        Hand hand = getHand();
        bedPos = findBestPos();
        if (lastPos == null || bedPos == null || lastPos.equals(bedPos)) {
            lastPos = bedPos;
            Managers.DELAY.clear();
        }
        if (explode) {
            explode(bedPos);
            explode = false;
        }
        if ((hand != null || (silent.get() && slot != -1)) && bedPos != null) {
            if (timer >= delay.get()) {
                timer = 0;
                boolean swapped = false;
                if (hand == null) {
                    InvUtils.swap(slot, true);
                    swapped = true;
                }
                place(bedPos, bedDir, hand == null ? Hand.MAIN_HAND : hand);
                if (swapped) {
                    InvUtils.swapBack();
                }
                if (instantExp.get()) {
                    explode(bedPos);
                } else {
                    Managers.DELAY.add(() -> explode = true, (float) (expDelay.get() * 1));
                }
            }
        }
    }
    void place(BlockPos pos, Direction dir, Hand hand) {
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(dir.asRotation(), 0, Managers.ONGROUND.isOnGround()));
        if (air(pos.down())) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand,
                new BlockHitResult(OLEPOSSUtils.getMiddle(pos), Direction.UP, pos, false), 0));
        } else {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand,
                new BlockHitResult(OLEPOSSUtils.getMiddle(pos.down()), Direction.UP, pos.down(), false), 0));
        }
        if (swing.get()) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }
    }

    void explode(BlockPos position) {
        if (mc.player == null) {return;}
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
            new BlockHitResult(OLEPOSSUtils.getMiddle(position), Direction.UP, position, false), 0));
        if (swing.get()) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }

    BlockPos findBestPos() {
        BlockPos position = null;
        if (mc.player != null && mc.world != null) {
            double highestDMG = 0;
            double highestDist = 0;
            Direction direction = null;
            int calcRange = (int) Math.ceil(range.get());
            for (int y = calcRange; y >= -calcRange; y--) {
                for (int x = -calcRange; x <= calcRange; x++) {
                    for (int z = -calcRange; z <= calcRange; z++) {
                        BlockPos pos = new BlockPos(x + mc.player.getBlockPos().getX(),
                            y + mc.player.getBlockPos().getY(), z + mc.player.getBlockPos().getZ());
                        if (canBePlaced(pos)) {
                            for (Direction dir : OLEPOSSUtils.horizontals) {
                                if (air(pos.offset(dir)) || bed(pos.offset(dir))) {
                                    int changed = 0;
                                    BlockState state1 = mc.world.getBlockState(pos);
                                    BlockState state2 = mc.world.getBlockState(pos.offset(dir));
                                    if (bed(pos)) {
                                        mc.world.setBlockState(pos, mc.world.getBlockState(new BlockPos(0, 1000, 0)));
                                        changed += 1;
                                    }
                                    if (bed(pos.offset(dir))) {
                                        mc.world.setBlockState(pos.offset(dir), mc.world.getBlockState(new BlockPos(0, 1000, 0)));
                                        changed += 2;
                                    }
                                    double[] dmg = highestDmg(pos.offset(dir));
                                    double self = getSelfDamage(new Vec3d(pos.offset(dir).getX() + 0.5, pos.offset(dir).getY() + 0.5, pos.offset(dir).getZ() + 0.5));
                                    double dist = OLEPOSSUtils.distance(new Vec3d(x + mc.player.getBlockPos().getX() + 0.5,
                                        y + mc.player.getBlockPos().getY(), z + mc.player.getBlockPos().getZ() + 0.5), mc.player.getEyePos());
                                    if (changed == 1 || changed == 3) {
                                        mc.world.setBlockState(pos, state1);
                                    }
                                    if (changed == 2 || changed == 3) {
                                        mc.world.setBlockState(pos.offset(dir), state2);
                                    }
                                    if (placeDamageCheck(dmg[0], self, dmg[1], highestDMG, dist, highestDist)) {
                                        direction = dir;
                                        highestDMG = dmg[0];
                                        highestDist = dist;
                                        position = pos;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            bedDir = direction;
        }
        return position;
    }
    double getSelfDamage(Vec3d vec) {
        return DamageUtils.bedDamage(mc.player, vec);
    }
    double[] highestDmg(BlockPos pos) {
        double highest = 0;
        double highestHP = 0;
        if (mc.player != null && mc.world != null) {
            for (PlayerEntity enemy : mc.world.getPlayers()) {
                NotHolding holding = getHolding(enemy.getName().getString());
                if (enemy != mc.player && (holding != null && holding.timer > totemTime.get() || !notHoldingTotem.get()) &&
                    !Friends.get().isFriend(enemy) && enemy.getHealth() > 0 && !enemy.isSpectator()) {
                    double dmg = DamageUtils.bedDamage(enemy, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (dmg > highest) {
                        highest = dmg;
                        highestHP = enemy.getHealth() + enemy.getAbsorptionAmount();
                    }
                }
            }
        }
        return new double[] {highest, highestHP};
    }
    boolean canBePlaced(BlockPos pos) {
        if (mc.player != null && mc.world != null) {
            if (air(pos.down())) {
                return false;
            }

            if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) &&
                !bed(pos)) {
                return false;
            }
            if (!placeRangeCheck(pos)) {
                return false;
            }
            Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
            return !EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !(entity instanceof ItemEntity));
        }
        return false;
    }
    boolean placeRangeCheck(BlockPos pos) {
        return (OLEPOSSUtils.distance(mc.player.getEyePos(),
                new Vec3d(pos.getX() + 0.5, pos.getY() + 1.8, pos.getZ() + 0.5)) <= range.get());
    }


    boolean placeDamageCheck(double dmg, double self, double health, double highest, double distance, double highestDist) {
        if (dmg < highest) {return false;}
        if (dmg == highest && distance > highestDist) {return false;}

        //  Force pop check
        if (popStuff.get() && mc.player != null && forcePop.get() * dmg >= health &&
            mc.player.getHealth() + mc.player.getAbsorptionAmount() > self * antiPop.get()) {return true;}

        if (dmg < minDamage.get()) {return false;}
        if (self > maxDamage.get()) {return false;}
        return self / dmg <= maxDamageRatio.get();
    }
    boolean air(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);
    }
    boolean bed(BlockPos pos) {
        return OLEPOSSUtils.isBedBlock(mc.world.getBlockState(pos).getBlock());
    }
    Hand getHand() {
        if (OLEPOSSUtils.isBedItem(Managers.HOLDING.getStack().getItem())) {
            return Hand.MAIN_HAND;
        } else if (OLEPOSSUtils.isBedItem(mc.player.getOffHandStack().getItem())) {
            return Hand.OFF_HAND;
        }
        return null;
    }
    int bedSlot() {
        for (int i = 0; i < 9; i++) {
            if (OLEPOSSUtils.isBedItem(mc.player.getInventory().getStack(i).getItem())) {
                return i;
            }
        }
        return -1;
    }
    NotHolding getHolding(String name) {
        for (NotHolding holding : holdings) {
            if (holding.name.equals(name)) {
                return holding;
            }
        }
        return null;
    }
    Vec3d smoothMove(Vec3d current, Vec3d target, float speed) {
        if (current == null) {
            return target;
        }
        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);

        return new Vec3d(current.x < target.x ? Math.min(target.x, current.x + speed * absX) : Math.max(target.x, current.x - speed * absX),
            current.y < target.y ? Math.min(target.y, current.y + speed * absY) : Math.max(target.y, current.y - speed * absY),
            current.z < target.z ? Math.min(target.z, current.z + speed * absZ) : Math.max(target.z, current.z - speed * absZ));
    }
    float smoothTurn(float current, float target, float speed) {
        float abs = Math.abs(current - target);
        return current > target ? Math.max(target, current - speed * abs) : Math.min(target, current + speed * abs);
    }
    static class NotHolding {
        private final String name;
        private float timer;

        public NotHolding(String name) {
            this.name = name;
            this.timer = 0;
        }

        public void update(double delta, boolean isHolding) {
            timer = isHolding ? 0 : (float) (timer + delta);
        }
    }
}
