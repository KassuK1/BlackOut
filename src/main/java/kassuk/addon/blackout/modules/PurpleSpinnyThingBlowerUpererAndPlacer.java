package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.IntTimerList;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PurpleSpinnyThingBlowerUpererAndPlacer extends BlackOutModule {
    public PurpleSpinnyThingBlowerUpererAndPlacer() {super(BlackOut.BLACKOUT,"NN-Nuker","PurpleSpinnyThingBlowerUpererAndPlacer");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> pRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place range")
        .description("The range to place in")
        .defaultValue(4)
        .range(0, 6)
        .sliderMax(6)
        .build()
    );
    private final Setting<Double> bRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Break range")
        .description("The range to break in")
        .defaultValue(4)
        .range(0, 6)
        .sliderMax(6)
        .build()
    );
    private final Setting<Double> pDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place delay")
        .description("Delay for placing")
        .defaultValue(0.1)
        .range(0, 1)
        .sliderMax(1)
        .build()
    );
    private final Setting<Double> bDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Break delay")
        .description("Delay for breaking")
        .defaultValue(0.5)
        .range(0, 1)
        .sliderMax(1)
        .build()
    );
    private final Setting<Integer> minDamage = sgGeneral.add(new IntSetting.Builder()
        .name("Minimum damage")
        .description("balls")
        .defaultValue(3)
        .range(1, 36)
        .sliderMax(36)
        .build()
    );
    private final Setting<Boolean> iFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Friends")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("Debug")
        .description("Prints debug stuff")
        .defaultValue(true)
        .build()
    );
    PlayerEntity target = null;
    double timer = 0;
    IntTimerList attacked = new IntTimerList();

    @EventHandler
    private void onRender(Render3DEvent event){
        timer = Math.min(pDelay.get(),timer + event.frameTime);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event){
        if (mc.player != null && mc.world != null){
            target = getClosest();

            BlockPos bPos = null;
            double highest = -1;
            int c = (int) Math.ceil(pRange.get());
            for (int x = -c; x <= c; x++) {
                for (int y = -c; y <= c; y++) {
                    for (int z = -c; z <= c; z++) {
                        BlockPos pos = mc.player.getBlockPos().add(x, y ,z);
                        if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK){
                            if (OLEPOSSUtils.distance(mc.player.getEyePos(),OLEPOSSUtils.getMiddle(pos)) <= pRange.get() && !EntityUtils.intersectsWithEntity(new Box(pos.getX(),pos.getY(), pos.getZ(),pos.getX() + 1, pos.getY() + 2,pos.getZ() + 1),
                                entity -> !entity.isSpectator() && !(entity instanceof EndCrystalEntity))){
                                double dmg = DamageUtils.crystalDamage(target, new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
                                if (dmg > highest && dmg > minDamage.get()){
                                    bPos = pos;
                                    highest = dmg;
                                }
                            }
                        }
                    }
                }
            }
            Entity best = null;
            highest = -1;
            for (Entity entity: mc.world.getEntities()){
                if (entity instanceof EndCrystalEntity){
                    double dmg = DamageUtils.crystalDamage(target,entity.getPos());
                    if (dmg > highest && dmg >= minDamage.get()){
                        best = entity;
                        highest = dmg;
                    }
                }
            }
            if (best != null && !attacked.contains(best.getId())){
                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(best, mc.player.isSneaking()));
                mc.player.swingHand(Hand.MAIN_HAND);
                attacked.add(best.getId(), bDelay.get());
            }

            if (bPos != null && timer >= pDelay.get() && Managers.HOLDING.isHolding(Items.END_CRYSTAL)){
                mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(new Vec3d(bPos.getX() + 0.5, bPos.getY() - 0.5, bPos.getZ() + 0.5), Direction.UP, bPos.down(), false), 0));
                mc.player.swingHand(Hand.MAIN_HAND);
                if (debug.get()){info("tried to place at" + bPos);}
                timer = 0;
            }

        }
    }
    PlayerEntity getClosest() {
        PlayerEntity closest = null;
        float distance = -1;
        if (!mc.world.getPlayers().isEmpty()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player && (!iFriends.get() || !Friends.get().isFriend(player))) {
                    float dist = (float) OLEPOSSUtils.distance(mc.player.getEyePos(), player.getPos());
                    if ((closest == null || OLEPOSSUtils.distance(mc.player.getPos(), player.getPos()) < distance) && dist <= 30) {
                        closest = player;
                        distance = (float) OLEPOSSUtils.distance(mc.player.getPos(), player.getPos());
                    }
                }
            }
        }
        return closest;
    }
}
