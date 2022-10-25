package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
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
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoCrystal extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgExplode = settings.createGroup("Explode");
    private final SettingGroup sgMisc = settings.createGroup("Misc");
    private final SettingGroup sgSwitch = settings.createGroup("Switch");
    private final SettingGroup sgRaytrace = settings.createGroup("Raytrace");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //  General Page

    private final Setting<Boolean> place = sgGeneral.add(new BoolSetting.Builder()
        .name("Place")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> explode = sgGeneral.add(new BoolSetting.Builder()
        .name("Explode")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> eatPause = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause When Eating")
        .description(".")
        .defaultValue(true)
        .build()
    );

    //  Place Page
    private final Setting<Boolean> superSmartRangeChecks = sgGeneral.add(new BoolSetting.Builder()
        .name("Super Smart Range Checks")
        .description(".")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> placeRange = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Range")
        .description(".")
        .defaultValue(6)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> minPlaceDamage = sgPlace.add(new DoubleSetting.Builder()
        .name("Min Place Damage")
        .description(".")
        .defaultValue(5)
        .range(0, 20)
        .sliderMax(20)
        .build()
    );
    private final Setting<Double> maxSelfPlace = sgPlace.add(new DoubleSetting.Builder()
        .name("Max Place Damage")
        .description(".")
        .defaultValue(0.7)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> placeSwing = sgPlace.add(new BoolSetting.Builder()
        .name("Place Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> prePlaceSwing = sgPlace.add(new BoolSetting.Builder()
        .name("Pre Place Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingMode> placeSwingMode = sgPlace.add(new EnumSetting.Builder<SwingMode>()
        .name("Place Swing Mode")
        .description(".")
        .defaultValue(SwingMode.Full)
        .build()
    );

    //  Explode Page

    private final Setting<Double> explodeRange = sgExplode.add(new DoubleSetting.Builder()
        .name("Break Range")
        .description(".")
        .defaultValue(6)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> explodeWalls = sgExplode.add(new DoubleSetting.Builder()
        .name("Break Walls Range")
        .description(".")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> minExplodeDamage = sgExplode.add(new DoubleSetting.Builder()
        .name("Min Explode Damage")
        .description(".")
        .defaultValue(5)
        .range(0, 20)
        .sliderMax(20)
        .build()
    );
    private final Setting<Boolean> ignoreBreak = sgExplode.add(new BoolSetting.Builder()
        .name("Ignore Damage")
        .description("Removes crystals.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> maxSelfBreak = sgExplode.add(new DoubleSetting.Builder()
        .name("Max Explode Damage")
        .description(".")
        .defaultValue(0.7)
        .range(0, 10)
        .sliderMax(10)
        .visible(() -> !ignoreBreak.get())
        .build()
    );
    private final Setting<Boolean> onlyExplodeWhenHolding = sgExplode.add(new BoolSetting.Builder()
        .name("Only Explode When Holding")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> setDead = sgExplode.add(new BoolSetting.Builder()
        .name("SetDead")
        .description("Removes crystals.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> explodeBlocking = sgExplode.add(new BoolSetting.Builder()
        .name("Explode Blocking")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> alwaysExplodeBlocking = sgExplode.add(new BoolSetting.Builder()
        .name("Always Explode Blocking")
        .description(".")
        .defaultValue(false)
        .visible(explodeBlocking::get)
        .build()
    );
    private final Setting<Boolean> explodeSwing = sgExplode.add(new BoolSetting.Builder()
        .name("Explode Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> preExplodeSwing = sgExplode.add(new BoolSetting.Builder()
        .name("Pre Explode Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingMode> explodeSwingMode = sgExplode.add(new EnumSetting.Builder<SwingMode>()
        .name("Explode Swing Mode")
        .description(".")
        .defaultValue(SwingMode.Full)
        .build()
    );

    //  Misc Page
    private final Setting<Boolean> idPredict = sgMisc.add(new BoolSetting.Builder()
        .name("ID Predict")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> allowOffhand = sgMisc.add(new BoolSetting.Builder()
        .name("Allow Offhand")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> preferMainHand = sgMisc.add(new BoolSetting.Builder()
        .name("Prefer Mainhand")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .visible(allowOffhand::get)
        .build()
    );

    //  Switch Page



    //  Raytrace Page

    private final Setting<Boolean> placeRangeFromEyes = sgRaytrace.add(new BoolSetting.Builder()
        .name("Place Range From Eyes")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> placeRangeStartHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Place Range Start Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .visible(() -> !placeRangeFromEyes.get())
        .build()
    );
    private final Setting<Double> placeRangeHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Place Range End Height")
        .description(".")
        .defaultValue(1)
        .range(0, 2)
        .sliderMax(2)
        .build()
    );

    private final Setting<Boolean> breakRangeFromEyes = sgRaytrace.add(new BoolSetting.Builder()
        .name("Break Range From Eyes")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> breakRangeStartHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Break Range Start Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .visible(() -> !breakRangeFromEyes.get())
        .build()
    );
    private final Setting<Double> breakRangeHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Break Range End Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .build()
    );
    private final Setting<Boolean> raytraceFromEyes = sgRaytrace.add(new BoolSetting.Builder()
        .name("Raytrace From Eyes")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> playerRayStartHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Ray Start Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .visible(() -> !raytraceFromEyes.get())
        .build()
    );
    private final Setting<Double> rayEndHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Ray End Height")
        .description(".")
        .defaultValue(2)
        .range(0, 3)
        .sliderMax(3)
        .build()
    );
    public enum SwingMode {
        None,
        Full,
        Client,
        Packet
    }

    //  Render Page

    private final Setting<Boolean> animation = sgRender.add(new BoolSetting.Builder()
        .name("Animation")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> renderMoveSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("AnimationSpeed")
        .description(".")
        .defaultValue(5)
        .range(0, 10)
        .sliderMax(10)
        .visible(animation::get)
        .build()
    );

    private final Setting<Double> animationSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("AnimationSpeed")
        .description(".")
        .defaultValue(5)
        .range(0, 10)
        .sliderMax(10)
        .visible(animation::get)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private Direction[] horizontals = new Direction[] {
        Direction.WEST,
        Direction.EAST,
        Direction.NORTH,
        Direction.SOUTH
    };
    private int lowest;

    protected BlockPos placePos;
    protected BlockPos lastPos;
    protected boolean blocked;
    private int lastId;
    private boolean lastPaused;
    private double renderAnim;
    private Vec3d renderPos;
    private double height;

    public AutoCrystal() {
        super(Addon.ANARCHY, "Auto Crystal", "Breaks crystals automatically.");
    }

    // Listeners

    @Override
    public void onActivate() {
        super.onActivate();
        lowest = Integer.MIN_VALUE;
        resetVar();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            placePos = findBestPos();
            if (placePos != null) {
                if (pausedCheck() != lastPaused) {
                    lastPaused = pausedCheck();
                    resetVar();
                }
                if (!pausedCheck()) {
                    if (!placePos.equals(lastPos)) {
                        resetVar();
                    }
                    else if (crystalAtPos(placePos.up()) == null && isAround(placePos.up()) != null && alwaysExplodeBlocking.get()) {
                        cleanPos(placePos.up());
                    }
                    if (!blocked && place.get()) {
                        Hand swingHand = getHand(Items.END_CRYSTAL, preferMainHand.get(), true);
                        Hand handToUse = getHand(Items.END_CRYSTAL, preferMainHand.get(), false);
                        if (handToUse != null) {
                            swing(swingHand, placeSwingMode.get(), placeSwing.get(), prePlaceSwing.get(), true);
                            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(handToUse,
                                new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()), Direction.UP, placePos, false), 0));
                            swing(swingHand, placeSwingMode.get(), placeSwing.get(), prePlaceSwing.get(), false);
                            if (idPredict.get()) {
                                attackID(highestID() + 1, new Vec3d(placePos.getX() + 0.5, placePos.getY() + 1, placePos.getZ() + 0.5), false);
                            }
                            blocked = !idPredict.get();
                        }
                    }
                }
            } else {
                lastPos = null;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntity(EntityAddedEvent event) {
        if (event.entity.getId() > lowest) {
            lowest = event.entity.getId();
        }
        if (mc.player != null && mc.world != null) {
            if (!pausedCheck()) {
                if (event.entity.getType() == EntityType.END_CRYSTAL) {
                    if (placePos != null) {
                        Box box = new Box(placePos.getX(), placePos.getY() + 1, placePos.getZ(),
                            placePos.getX() + 1, placePos.getY() + 3, placePos.getZ() + 1);
                        if ((event.entity.getBoundingBox().intersects(box) && !event.entity.getBlockPos().equals(placePos)
                            && explodeBlocking.get())) {
                            if (event.entity.getBlockPos().equals(placePos.up())) {
                                blocked = false;
                            }
                            attackEntity(event.entity, true);
                            return;
                        }
                    }
                    if (canBreak(event.entity.getPos())) {
                        if (event.entity.getBlockPos().equals(placePos.up())) {
                            blocked = false;
                        }
                        attackEntity(event.entity, true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(PacketEvent.Send event) {
        if (mc.player != null && mc.world != null) {
            if (event.packet instanceof PlayerActionC2SPacket) {
                PlayerActionC2SPacket packet = (PlayerActionC2SPacket) event.packet;
                Direction dir = getDirectionToEnemy(packet.getPos());
                if (packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK && dir != null) {
                    BlockPos pos = packet.getPos().offset(dir);
                    Hand swingHand = getHand(Items.END_CRYSTAL, preferMainHand.get(), true);
                    Hand handToUse = getHand(Items.END_CRYSTAL, preferMainHand.get(), false);
                    if (handToUse != null) {
                        swing(swingHand, placeSwingMode.get(), placeSwing.get(), prePlaceSwing.get(), true);
                        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(handToUse,
                            new BlockHitResult(new Vec3d(pos.getX(), pos.getY() - 1, pos.getZ()),
                                Direction.UP, pos.down(), false), 0));
                        swing(swingHand, placeSwingMode.get(), placeSwing.get(), prePlaceSwing.get(), false);
                        if (idPredict.get()) {
                            attackID(highestID() + 1, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), false);
                        }
                        blocked = !idPredict.get();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player != null && mc.world != null) {
            if (placePos != null) {
                renderPos = smoothMove(renderPos,
                    new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()), (float) (animationSpeed.get() * event.tickDelta / 10));
            }
            if (animation.get()) {
                renderAnim = placePos != null ?
                    (renderAnim + animationSpeed.get() > 100 ? 100 : renderAnim + animationSpeed.get())
                    :
                    (renderAnim - animationSpeed.get() < 0 ? 0 : renderAnim - animationSpeed.get());
            }
            if (renderPos != null && renderAnim > 0) {
                Vec3d v = new Vec3d(renderPos.getX() + 0.5, renderPos.getY() + 0.5, renderPos.getZ() + 0.5);
                double progress = renderAnim / 100 / 2;
                Box toRender = new Box(v.x - progress, v.y - progress, v.z - progress, v.x + progress, v.y + progress, v.z + progress);
                event.renderer.box(toRender, new Color(color.get().r, color.get().g, color.get().b,
                    (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both, 0);
            }
        }
    }

    // Other stuff

    private BlockPos findBestPos() {
        BlockPos position = null;
        if (mc.player != null && mc.world != null) {
            double highestDMG = 0;
            double highestDist = 0;
            int calcRange = (int) Math.ceil(placeRange.get());
            for (int y = calcRange; y >= -calcRange; y--) {
                for (int x = -calcRange; x <= calcRange; x++) {
                    for (int z = -calcRange; z <= calcRange; z++) {
                        BlockPos pos = new BlockPos(x + mc.player.getBlockPos().getX(),
                            y + mc.player.getBlockPos().getY(), z + mc.player.getBlockPos().getZ());
                        if (canBePlaced(pos)) {
                            double dmg = highestDmg(pos);
                            double self = DamageUtils.crystalDamage(mc.player, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5),
                                false, pos, false);
                            double dist = OLEPOSSUtils.distance(new Vec3d(x + mc.player.getBlockPos().getX() + 0.5,
                                y + mc.player.getBlockPos().getY(), z + mc.player.getBlockPos().getZ() + 0.5),
                                playerRangePos(true));
                            if (placeDamageCheck(dmg, self, highestDMG, dist, highestDist)) {
                                highestDMG = dmg;
                                highestDist = dist;
                                position = pos;
                            }
                        }
                    }
                }
            }
        }
        return position;
    }

    private boolean placeDamageCheck(double dmg, double self, double highest, double distance, double highestDist) {
        if (dmg < highest) {return false;}
        if (dmg == highest && distance > highestDist) {return false;}
        if (dmg < minPlaceDamage.get()) {return false;}
        return self / dmg <= maxSelfPlace.get();
    }

    private boolean breakDamageCheck(double dmg, double self) {

        if (!ignoreBreak.get() && dmg < minExplodeDamage.get()) {return false;}
        return self / dmg <= maxSelfBreak.get();
    }

    protected double highestDmg(BlockPos pos) {
        double highest = 0;
        if (mc.player != null && mc.world != null) {
            for (PlayerEntity enemy : mc.world.getPlayers()) {
                if (enemy != mc.player && !Friends.get().isFriend(enemy)) {
                    double dmg = DamageUtils.crystalDamage(enemy, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5),
                        false, pos, false);
                    if (dmg > highest) {
                        highest = dmg;
                    }
                }
            }
        }
        return highest;
    }

    protected boolean canBePlaced(BlockPos pos) {
        if (mc.player != null && mc.world != null) {
            if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN &&
                mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK) {
                return false;
            }

            if (!mc.world.getBlockState(pos.offset(Direction.UP)).getBlock().equals(Blocks.AIR)) {
                return false;
            }
            if (!placeRangeCheck(pos)) {
                return false;
            }
            Box box = new Box(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
            return !EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !(
                entity.getBlockPos().equals(pos.up()) && entity.getType() == EntityType.END_CRYSTAL
            ));
        }
        return false;
    }

    private Vec3d playerRangePos(boolean place) {
        if (place) {
            return new Vec3d(mc.player.getX(),
                placeRangeFromEyes.get() ? mc.player.getEyePos().y : mc.player.getY() + placeRangeStartHeight.get(), mc.player.getZ());
        } else {
            return new Vec3d(mc.player.getX(),
                breakRangeFromEyes.get() ? mc.player.getEyePos().y : mc.player.getY() + breakRangeStartHeight.get(), mc.player.getZ());
        }
    }

    private boolean placeRangeCheck(BlockPos pos) {
        return (OLEPOSSUtils.distance(playerRangePos(true),
            new Vec3d(pos.getX() + 0.5, pos.getY() + placeRangeHeight.get(), pos.getZ() + 0.5)) <= placeRange.get()) &&
            (!superSmartRangeChecks.get() || breakRangeCheck(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5)));
    }

    private boolean breakRangeCheck(Vec3d pos) {
        return OLEPOSSUtils.distance(playerRangePos(false),
            new Vec3d(pos.getX(), pos.getY() + breakRangeHeight.get(), pos.getZ())) <= getBreakRange(pos);
    }

    private Hand getHand(Item item, boolean preferMain, boolean swing) {
        if (!mc.player.isHolding(item) && !swing) {
            return null;
        }
        if (allowOffhand.get() && mc.player.getOffHandStack().getItem() == item) {
            if (preferMain && mc.player.getMainHandStack().getItem() == item) {
                return Hand.MAIN_HAND;
            } else {
                return Hand.OFF_HAND;
            }
        } else if (mc.player.getMainHandStack().getItem() == item) {
            return Hand.MAIN_HAND;
        }
        return swing ? Hand.MAIN_HAND : null;
    }

    private double getBreakRange(Vec3d pos) {
        Vec3d vec1 = new Vec3d(mc.player.getX(), raytraceFromEyes.get() ? mc.player.getEyePos().getY() :
            mc.player.getY() + playerRayStartHeight.get(), mc.player.getZ());
        Vec3d vec2 = new Vec3d(pos.getX(), pos.getY() + rayEndHeight.get(), pos.getZ());
        if (mc.world.raycast(new RaycastContext(vec1, vec2,
            RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() != HitResult.Type.BLOCK) {
            return explodeRange.get();
        } else {
            return explodeWalls.get();
        }
    }

    private boolean canBreak(Vec3d pos) {
        if (!explode.get()) {return false;}
        double self = DamageUtils.crystalDamage(mc.player, new Vec3d(pos.x, pos.y, pos.z),
            false, new BlockPos(Math.floor(pos.x), Math.floor(pos.y) - 1, Math.floor(pos.z)), false);
        if (onlyExplodeWhenHolding.get() && getHand(Items.END_CRYSTAL, preferMainHand.get(), false) == null) {return false;}
        if (!breakDamageCheck(highestDmg(new BlockPos(Math.floor(pos.x), Math.floor(pos.y) - 1, Math.floor(pos.z))), self)) {return false;}
        return breakRangeCheck(new Vec3d(pos.x, pos.y, pos.z));
    }

    private int highestID() {
        int highest = lastId;
        if (mc.world != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity.getId() > highest) {
                    highest = entity.getId();
                }
            }
        }
        lastId = highest + 1;
        return highest;
    }

    private void cleanPos(BlockPos pos) {
        if (mc.player != null && mc.world != null) {
            Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
            if (EntityUtils.intersectsWithEntity(box, entity -> entity.getType() == EntityType.END_CRYSTAL)) {
                for (Entity en : mc.world.getEntities()) {
                    if (en.getBoundingBox().intersects(box) && en instanceof EndCrystalEntity) {
                        if (canBreak(en.getPos())) {
                            attackEntity(en, true);
                            blocked = false;
                            break;
                        }
                    }
                }
            }
        }
    }

    private EndCrystalEntity isAround(BlockPos pos) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        for (Entity en : mc.world.getEntities()) {
            if (en.getBoundingBox().intersects(box) && en instanceof EndCrystalEntity && !en.getBlockPos().equals(pos)) {
                if (canBreak(en.getPos())) {
                    attackEntity(en, true);
                    blocked = false;
                    break;
                }
            }
        }
        return null;
    }

    private EndCrystalEntity crystalAtPos(BlockPos pos) {
        if (mc.world != null) {
            for (Entity en : mc.world.getEntities()) {
                if (en instanceof EndCrystalEntity && en.getBlockPos().equals(pos)) {
                    return (EndCrystalEntity) en;
                }
            }
        }
       return null;
    }

    private void resetVar() {
        lastId = Integer.MIN_VALUE;
        if (placePos != null) {
            blocked = crystalAtPos(placePos.up()) != null;
        } else {
            blocked = false;
        }
        lastPos = placePos;
        if (blocked) {
            cleanPos(placePos.up());
        }
    }

    private void swing(Hand hand, SwingMode mode, boolean mainSetting, boolean timingSetting, boolean pre) {
        if (mainSetting && mc.player != null) {
            if (timingSetting == pre) {
                if (mode == SwingMode.Full || mode == SwingMode.Packet) {
                    mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
                }
                if (mode == SwingMode.Full || mode == SwingMode.Client) {
                    mc.player.swingHand(hand);
                }
            }
        }
    }

    private void attackID(int id, Vec3d pos, boolean checkSD) {
        EndCrystalEntity en = new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z);
        en.setId(id);
        attackEntity(en, checkSD);
    }

    private void attackEntity(Entity en, boolean checkSD) {
        if (mc.player != null) {
            Hand handToUse = getHand(Items.END_CRYSTAL, preferMainHand.get(), true);
            if (handToUse != null) {
                swing(handToUse, explodeSwingMode.get(), explodeSwing.get(), preExplodeSwing.get(), true);
                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));
                swing(handToUse, explodeSwingMode.get(), explodeSwing.get(), preExplodeSwing.get(), false);
                if (setDead.get() && checkSD) {
                    en.setRemoved(Entity.RemovalReason.KILLED);
                }
            }
        }
    }

    private boolean pausedCheck() {
        if (mc.player != null) {
            return eatPause.get() && (mc.player.isUsingItem() && mc.player.isHolding(Items.ENCHANTED_GOLDEN_APPLE));
        }
        return true;
    }

    public Vec3d smoothMove(Vec3d current, Vec3d target, float speed) {
        if (current == null) {
            return target;
        }
        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);
        height = (float) ((speed * absX + speed * absZ) * 1.5f / animationSpeed.get() * 10);
        return new Vec3d(
            current.x > target.x ?
                (absX <= speed * absX ? target.x : current.x - speed * absX) :
                current.x != target.x ?
                    (absX <= speed * absX ? target.x : current.x + speed * absX) :
                    target.x
            ,
            current.y > target.y ?
                (absY <= speed * absY ? target.y : current.y - speed * absY) :
                current.y != target.y ?
                    (absY <= speed * absY ? target.y : current.y + speed * absY) :
                    target.y
            ,
            current.z > target.z ?
                (absZ <= speed * absZ ? target.z : current.z - speed * absZ) :
                current.z != target.z ?
                    (absZ <= speed * absZ ? target.z : current.z + speed * absZ) :
                    target.z);
    }

    public Direction getDirectionToEnemy(BlockPos block) {
        for (PlayerEntity pl : mc.world.getPlayers()) {
            Direction dir = nextTo(pl, block);
            if (dir != null) {
                return dir;
            }
        }
        return null;
    }

    public Direction nextTo(PlayerEntity pl, BlockPos block) {
        for (Direction dir : horizontals) {
            if (pl.getBlockPos().offset(dir) == block) {
                return dir;
            }
        }
        return null;
    }
}
