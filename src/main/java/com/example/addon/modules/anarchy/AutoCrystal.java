package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
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
        .visible(place::get)
        .build()
    );
    private final Setting<Double> minPlaceDamage = sgPlace.add(new DoubleSetting.Builder()
        .name("Min Place Damage")
        .description(".")
        .defaultValue(5)
        .range(0, 20)
        .sliderMax(20)
        .visible(place::get)
        .build()
    );
    private final Setting<Double> maxSelfPlace = sgPlace.add(new DoubleSetting.Builder()
        .name("Max Place Damage")
        .description(".")
        .defaultValue(0.7)
        .range(0, 10)
        .sliderMax(10)
        .visible(place::get)
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
        .name("Break Range Height")
        .description(".")
        .defaultValue(1)
        .range(0, 2)
        .sliderMax(2)
        .build()
    );

    private final Setting<Boolean> breakRangeFromEyes = sgRaytrace.add(new BoolSetting.Builder()
        .name("Place Range From Eyes")
        .description("Removes crystals Instantly after spawning.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> breakRangeStartHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Place Range Start Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .visible(() -> !breakRangeFromEyes.get())
        .build()
    );
    private final Setting<Double> breakRangeHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Break Range Height")
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
        .name("Break Range Height")
        .description(".")
        .defaultValue(1)
        .range(0, 3)
        .sliderMax(3)
        .visible(() -> !raytraceFromEyes.get())
        .build()
    );
    private final Setting<Double> rayEndHeight = sgRaytrace.add(new DoubleSetting.Builder()
        .name("Raytrace End Height")
        .description(".")
        .defaultValue(2)
        .range(0, 3)
        .sliderMax(3)
        .build()
    );
    private int lowest;

    protected BlockPos placePos;
    protected BlockPos lastPos;
    protected boolean blocked;
    private int lastId;

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
                if (!placePos.equals(lastPos)) {
                    resetVar();
                }
                if (!blocked && place.get()) {
                    if (getHand(Items.END_CRYSTAL, preferMainHand.get()) != null) {
                        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(getHand(Items.END_CRYSTAL, preferMainHand.get()),
                            new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()), Direction.UP, placePos, false), 0));
                        if (idPredict.get()) {
                            attackID(highestID() + 1, new Vec3d(placePos.getX() + 0.5, placePos.getY() + 1, placePos.getZ() + 0.5), false);
                        }
                        blocked = !idPredict.get();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntity(EntityAddedEvent event) {
        if (event.entity.getId() > lowest) {
            lowest = event.entity.getId();
        }
        if (mc.player != null && mc.world != null) {
            if (event.entity.getType() == EntityType.END_CRYSTAL){
                Box box = new Box(placePos.getX(), placePos.getY() + 1, placePos.getZ(),
                    placePos.getX() + 1, placePos.getY() + 3, placePos.getZ() + 1);

                if (canBreak(event.entity.getPos()) ||
                    (event.entity.getBoundingBox().intersects(box) && !event.entity.getBlockPos().equals(placePos)
                    && explodeBlocking.get())) {
                    if (event.entity.getBlockPos().equals(placePos.up())) {
                        blocked = false;
                    }
                    attackEntity(event.entity, true);
                }
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
                            double dist = distance(new Vec3d(x + mc.player.getBlockPos().getX() + 0.5,
                                y + mc.player.getBlockPos().getY(), z + mc.player.getBlockPos().getZ() + 0.5),
                                mc.player.getEyePos());
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

    private double distance(Vec3d v1, Vec3d v2) {
        double dX = Math.abs(v1.x - v2.x);
        double dY = Math.abs(v1.y - v2.y);
        double dZ = Math.abs(v1.z - v2.z);
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
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
                if (enemy != mc.player) {
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
        return (distance(playerRangePos(true),
            new Vec3d(pos.getX() + 0.5, pos.getY() + placeRangeHeight.get(), pos.getZ() + 0.5)) <= placeRange.get()) &&
            (!superSmartRangeChecks.get() || breakRangeCheck(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5)));
    }

    private boolean breakRangeCheck(Vec3d pos) {
        return distance(playerRangePos(false),
            new Vec3d(pos.getX(), pos.getY() + breakRangeHeight.get(), pos.getZ())) <= getBreakRange(pos);
    }

    private Hand getHand(Item item, boolean preferMain) {
        if (!mc.player.isHolding(item)) {
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
        return null;
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
        if (onlyExplodeWhenHolding.get() && getHand(Items.END_CRYSTAL, preferMainHand.get()) == null) {return false;}
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

    private void attackID(int id, Vec3d pos, boolean checkSD) {
        EndCrystalEntity en = new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z);
        en.setId(id);
        attackEntity(en, checkSD);
    }

    private void attackEntity(Entity en, boolean checkSD) {
        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));
        if (setDead.get() && checkSD) {
            en.setRemoved(Entity.RemovalReason.KILLED);
        }
    }
}
