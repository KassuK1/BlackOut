package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.globalsettings.SwingSettings;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author OLEPOSSU
 */
public class AutoMinePlus extends BlackOutModule {
    public AutoMinePlus() {
        super(BlackOut.BLACKOUT, "Auto Mine+", "Automatically mines blocks to destroy your enemies.");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgManual = settings.createGroup("Manual");

    private final SettingGroup sgCev = settings.createGroup("Cev");
    private final SettingGroup sgAntiSurround = settings.createGroup("Anti Surround");
    private final SettingGroup sgAntiBurrow = settings.createGroup("Anti Burrow");

    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> pauseEat = addPauseEat(sgGeneral);
    private final Setting<Boolean> newVer = sgGeneral.add(new BoolSetting.Builder()
        .name("1.12.2 Crystals")
        .description("Uses 1.12.2 crystal mechanics.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. InvSwitch is used in most clients.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );

    //--------------------Manual--------------------//
    private final Setting<Boolean> startManually = sgManual.add(new BoolSetting.Builder()
        .name("Start Manually")
        .description("Sets target block to the block you clicked.")
        .defaultValue(true)
        .build()
    );

    //--------------------Speed--------------------//
    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Vanilla speed multiplier.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );
    private final Setting<Double> civDelay = sgSpeed.add(new DoubleSetting.Builder()
        .name("Civ Delay")
        .description("Delay between civ mines.")
        .defaultValue(0.5)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Boolean> onGroundCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("On Ground Check")
        .description("Mines 5x slower when not on ground.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> effectCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("Effect Check")
        .description("Modifies mining speed depending on haste and mining fatigue.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> waterCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("Water Check")
        .description("Mines 5x slower while submerged in water.")
        .defaultValue(true)
        .build()
    );

    //--------------------Cev--------------------//
    private final Setting<Priority> cevPriority = sgCev.add(new EnumSetting.Builder<Priority>()
        .name("Cev Priority")
        .description("Priority of cev.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> cevCiv = sgCev.add(new BoolSetting.Builder()
        .name("Cev CIV")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Priority> trapCevPriority = sgCev.add(new EnumSetting.Builder<Priority>()
        .name("Trap Cev Priority")
        .description("Priority of trap cev.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> trapCevCiv = sgCev.add(new BoolSetting.Builder()
        .name("Trap Cev CIV")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Priority> surroundCevPriority = sgCev.add(new EnumSetting.Builder<Priority>()
        .name("Surround Cev Priority")
        .description("Priority of trap cev.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> surroundCevCiv = sgCev.add(new BoolSetting.Builder()
        .name("Surround Cev CIV")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );

    //--------------------Anti-Surround--------------------//
    private final Setting<Priority> surroundMinerPriority = sgAntiSurround.add(new EnumSetting.Builder<Priority>()
        .name("Surround Miner Priority")
        .description("Priority of surround miner.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> surroundMinerCiv = sgAntiSurround.add(new BoolSetting.Builder()
        .name("Surround Miner CIV")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Priority> autoCityPriority = sgAntiSurround.add(new EnumSetting.Builder<Priority>()
        .name("Auto City Priority")
        .description("Priority of anti surround. Places crystal next to enemy's surround block.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> autoCityCiv = sgAntiSurround.add(new BoolSetting.Builder()
        .name("Auto City CIV")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> explodeCrystal = sgAntiSurround.add(new BoolSetting.Builder()
        .name("Explode Crystal")
        .description("Attacks the crystal we placed.")
        .defaultValue(false)
        .build()
    );

    //--------------------Anti-Burrow--------------------//
    private final Setting<Priority> antiBurrowPriority = sgAntiBurrow.add(new EnumSetting.Builder<Priority>()
        .name("Anti Burrow Priority")
        .description("Priority of anti burrow.")
        .defaultValue(Priority.Normal)
        .build()
    );

    //--------------------General--------------------//
    private final Setting<Double> animationExp = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Exponent")
        .description("3 - 4 look cool.")
        .defaultValue(3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of render should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineStartColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Start Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 0))
        .build()
    );
    private final Setting<SettingColor> lineEndColor = sgRender.add(new ColorSetting.Builder()
        .name("Line End Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> startColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Start Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 0))
        .build()
    );
    private final Setting<SettingColor> endColor = sgRender.add(new ColorSetting.Builder()
        .name("Side End Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private double minedFor = 0;
    private Target target = null;
    private boolean started = false;
    private BlockPos civPos = null;

    private List<AbstractClientPlayerEntity> enemies = new ArrayList<>();

    private long lastTime = 0;
    private long lastPlace = 0;
    private long lastExplode = 0;
    private long lastCiv = 0;

    private double delta = 0;
    private boolean ignore = false;

    private Map<BlockPos, Long> explodeAt = new HashMap<>();

    @Override
    public void onActivate() {
        target = null;
        minedFor = 0;
        started = false;
        lastTime = System.currentTimeMillis();
        civPos = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSent(PacketEvent.Send event) {
        if (event.packet instanceof PlayerActionC2SPacket packet) {
            if (ignore) {return;}

            switch (packet.getAction()) {
                case START_DESTROY_BLOCK -> {
                    handleStart(event);
                }
                case ABORT_DESTROY_BLOCK -> {
                    handleAbort(event);
                }
                case STOP_DESTROY_BLOCK -> {
                    handleStop(event);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null) {return;}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        delta = (System.currentTimeMillis() - lastTime) / 1000d;
        lastTime = System.currentTimeMillis();

        update();
        render(event.renderer);
    }

    void render(Renderer3D r) {
        if (target == null) {return;}

        double p = MathHelper.clamp(minedFor / getMineTicks(fastestSlot()), 0, 1);

        r.box(getRenderBox(p / 2), getColor(startColor.get(), endColor.get(), p), getColor(lineStartColor.get(), lineEndColor.get(), p), shapeMode.get(), 0);
    }

    void update() {
        if (mc.world == null) {return;}
        enemies = mc.world.getPlayers().stream().filter(player -> player != mc.player && !Friends.get().isFriend(player) && player.distanceTo(mc.player) < 10).toList();

        Entity targetCrystal = null;
        List<BlockPos> toRemove = new ArrayList<>();
        for (Map.Entry<BlockPos, Long> entry : explodeAt.entrySet()) {
            if (System.currentTimeMillis() - entry.getValue() > 1000) {toRemove.add(entry.getKey());}
            EndCrystalEntity crystal = crystalAt(entry.getKey());
            if (crystal != null) {
                targetCrystal = crystal;
                break;
            }
        }
        toRemove.forEach(explodeAt::remove);

        if (targetCrystal != null && System.currentTimeMillis() - lastExplode > 500) {

            if (!SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(targetCrystal.getBoundingBox(), priority, RotationType.Breaking)) {

                SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
                sendPacket(PlayerInteractEntityC2SPacket.attack(targetCrystal, mc.player.isSneaking()));
                SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);

                lastExplode = System.currentTimeMillis();

                if (SettingUtils.shouldRotate(RotationType.Attacking)) {
                    Managers.ROTATION.end(targetCrystal.getBoundingBox());
                }
            }
        }

        BlockPos lastPos = target == null || target.pos == null ? null : target.pos;
        target = target != null && target.manual ? target : getTarget();

        if (target == null) {return;}

        if (target.pos != null && !target.pos.equals(lastPos)) {
            if (started) {
                Direction dir = SettingUtils.getPlaceOnDirection(target.pos);
                send(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, target.pos, dir == null ? Direction.UP : dir, 0));
            }
            started = false;
        }

        if (!started) {
            boolean rotated = !SettingUtils.startMineRot() || Managers.ROTATION.start(target.pos, priority, RotationType.Breaking);

            if (rotated) {
                started = true;
                minedFor = 0;
                civPos = null;

                Direction dir = SettingUtils.getPlaceOnDirection(target.pos);

                send(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target.pos, dir == null ? Direction.UP : dir, 0));
                SettingUtils.mineSwing(SwingSettings.MiningSwingState.Start);

                if (SettingUtils.startMineRot()) {
                    Managers.ROTATION.end(target.pos);
                }
            }
        }

        if (!started) {return;}

        minedFor += delta * 20;

        if (!miningCheck(fastestSlot())) {return;}

        if (!civCheck()) {return;}

        if (!crystalCheck()) {return;}

        boolean rotated = !SettingUtils.endMineRot() || Managers.ROTATION.start(target.pos, priority, RotationType.Breaking);

        if (!rotated) {return;}

        if (!endMine()) {return;}
    }

    boolean civCheck() {
        if (civPos == null) {return true;}
        if (System.currentTimeMillis() - lastCiv < civDelay.get() * 1000) {return false;}
        return OLEPOSSUtils.solid2(civPos);
    }

    boolean endMine() {
        int slot = fastestSlot();

        boolean switched = miningCheck(Managers.HOLDING.slot);
        boolean swapBack = false;

        Direction dir = SettingUtils.getPlaceOnDirection(target.pos);

        if (dir == null) {return false;}

        if (SettingUtils.shouldRotate(RotationType.Breaking) && !Managers.ROTATION.start(target.pos, priority, RotationType.Breaking)) {return false;}

        if (!switched) {
            switch (switchMode.get()) {
                case Silent -> {
                    switched = true;
                    InvUtils.swap(slot, true);
                }
                case PickSilent -> {
                    switched = true;
                    BOInvUtils.pickSwitch(slot);
                }
                case InvSwitch -> switched = BOInvUtils.invSwitch(slot);
            }
            swapBack = switched;
        }

        if (!switched) {return false;}

        send(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target.pos, dir, 0));
        SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);

        if (target.civ) {
            civPos = target.pos;
        }

        if (SettingUtils.endMineRot()) {
            Managers.ROTATION.end(target.pos);
        }

        if (swapBack) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }

        if (!target.civ) {
            target = null;
            minedFor = 0;
        } else {
            civPos = target.pos;
            lastCiv = System.currentTimeMillis();
        }

        return true;
    }

    boolean crystalCheck() {
        switch (target.type) {
            case Cev, TrapCev, SurroundCev -> {
                if (crystalAt(target.crystalPos) != null) {return true;}
                if (!EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(target.crystalPos).withMaxY(target.crystalPos.getY() + 2), entity -> !entity.isSpectator())) {
                    placeCrystal();
                    return false;
                }
            }
            case AutoCity -> {
                if (crystalAt(target.crystalPos) != null) {return true;}
                if (!EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(target.crystalPos).withMaxY(target.crystalPos.getY() + 2), entity -> !entity.isSpectator())) {
                    return placeCrystal();
                }
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    EndCrystalEntity crystalAt(BlockPos pos) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal && entity.getBlockPos().equals(pos)) {
                return crystal;
            }
        }
        return null;
    }

    boolean placeCrystal() {
        if (System.currentTimeMillis() - lastPlace < 250) {return false;}

        Hand hand = getHand();

        int crystalSlot = InvUtils.find(Items.END_CRYSTAL).slot();
        if (hand == null && crystalSlot < 0) {return false;}

        boolean rotated = !SettingUtils.shouldRotate(RotationType.Placing) || Managers.ROTATION.start(target.crystalPos.down(), priority, RotationType.Placing);

        if (!rotated) {return false;}

        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent -> {
                    switched = true;
                    InvUtils.swap(crystalSlot, true);
                }
                case PickSilent -> {
                    switched = true;
                    BOInvUtils.pickSwitch(crystalSlot);
                }
                case InvSwitch -> {
                    switched = BOInvUtils.invSwitch(crystalSlot);
                }
            }
        }

        if (!switched) {return false;}

        Direction dir = SettingUtils.getPlaceOnDirection(target.crystalPos.down());

        if (dir == null) {return false;}

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand == null ? Hand.MAIN_HAND : hand);
        sendPacket(new PlayerInteractBlockC2SPacket(hand == null ? Hand.MAIN_HAND : hand, new BlockHitResult(OLEPOSSUtils.getMiddle(target.crystalPos.down()), dir, target.crystalPos.down(), false), 0));
        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand == null ? Hand.MAIN_HAND : hand);

        lastPlace = System.currentTimeMillis();

        if (shouldExplode()) {
            addExplode();
        }

        Managers.ROTATION.end(target.crystalPos.down());

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }

        return true;
    }

    void addExplode() {
        explodeAt.remove(target.crystalPos);
        explodeAt.put(target.crystalPos, System.currentTimeMillis());
    }

    boolean shouldExplode() {
        return switch (target.type) {
            case Cev, SurroundCev, TrapCev -> true;
            case SurroundMiner, AntiBurrow -> false;
            case AutoCity -> explodeCrystal.get();
            case Manual -> false;
        };
    }

    Target getTarget() {
        Target target = null;

        if (priorityCheck(target, cevPriority.get())) {
            Target t = getCev();
            if (t != null) {
                target = t;
            }
        }
        if (priorityCheck(target, trapCevPriority.get())) {
            Target t = getTrapCev();
            if (t != null) {
                target = t;
            }
        }
        if (priorityCheck(target, surroundCevPriority.get())) {
            Target t = getSurroundCev();
            if (t != null) {
                target = t;
            }
        }
        if (priorityCheck(target, surroundMinerPriority.get())) {
            Target t = getSurroundMiner();
            if (t != null) {
                target = t;
            }
        }
        if (priorityCheck(target, autoCityPriority.get())) {
            Target t = getAutoCity();
            if (t != null) {
                target = t;
            }
        }
        if (priorityCheck(target, antiBurrowPriority.get())) {
            Target t = getAntiBurrow();
            if (t != null) {
                target = t;
            }
        }
        return target;
    }

    Target getCev() {
        boolean civ = cevCiv.get();
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            BlockPos pos = new BlockPos(player.getBlockX(), (int) Math.floor(player.getBoundingBox().maxY) + 1, player.getBlockZ());

            if (!(civ && pos.equals(civPos)) && getBlock(pos) != Blocks.OBSIDIAN) {continue;}
            if ((civ && pos.equals(civPos)) && !(getBlock(pos) instanceof AirBlock) && getBlock(pos) != Blocks.OBSIDIAN) {continue;}

            if (getBlock(pos.up()) != Blocks.AIR) {continue;}
            if (newVer.get() && getBlock(pos.up(2)) != Blocks.AIR) {continue;}

            if (!SettingUtils.inMineRange(pos)) {continue;}
            if (!SettingUtils.inPlaceRange(pos)) {continue;}
            if (!SettingUtils.inAttackRange(OLEPOSSUtils.getCrystalBox(pos.up()))) {continue;}

            double d = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(pos));

            if (distanceCheck(civ, pos, distance, d)) {
                best = new Target(pos, pos.up(), MineType.Cev, cevPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                distance = d;
            }
        }
        return best;
    }
    Target getTrapCev() {
        boolean civ = trapCevCiv.get();
            Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            for (Direction dir : OLEPOSSUtils.horizontals) {
                BlockPos pos = new BlockPos(player.getBlockX(), (int) Math.floor(player.getBoundingBox().maxY), player.getBlockZ()).offset(dir);

                if (!(civ && pos.equals(civPos)) && getBlock(pos) != Blocks.OBSIDIAN) {continue;}
                if ((civ && pos.equals(civPos)) && !(getBlock(pos) instanceof AirBlock) && getBlock(pos) != Blocks.OBSIDIAN) {continue;}

                if (getBlock(pos.up()) != Blocks.AIR) {continue;}
                if (newVer.get() && getBlock(pos.up(2)) != Blocks.AIR) {continue;}

                if (!SettingUtils.inMineRange(pos)) {continue;}
                if (!SettingUtils.inPlaceRange(pos)) {continue;}
                if (!SettingUtils.inAttackRange(OLEPOSSUtils.getCrystalBox(pos.up()))) {continue;}

                double d = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(pos));

                if (distanceCheck(civ, pos, distance, d)) {
                    best = new Target(pos, pos.up(), MineType.TrapCev, trapCevPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                    distance = d;
                }
            }
        }
        return best;
    }
    Target getSurroundCev() {
        boolean civ = surroundCevCiv.get();
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            for (Direction dir : OLEPOSSUtils.horizontals) {
                BlockPos pos = player.getBlockPos().offset(dir);

                if (!(civ && pos.equals(civPos)) && getBlock(pos) != Blocks.OBSIDIAN) {continue;}
                if ((civ && pos.equals(civPos)) && !(getBlock(pos) instanceof AirBlock) && getBlock(pos) != Blocks.OBSIDIAN) {continue;}

                if (getBlock(pos.up()) != Blocks.AIR) {continue;}
                if (newVer.get() && getBlock(pos.up(2)) != Blocks.AIR) {continue;}

                if (!SettingUtils.inMineRange(pos)) {continue;}
                if (!SettingUtils.inPlaceRange(pos)) {continue;}
                if (!SettingUtils.inAttackRange(OLEPOSSUtils.getCrystalBox(pos.up()))) {continue;}

                double d = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(pos));

                if (distanceCheck(civ, pos, distance, d)) {
                    best = new Target(pos, pos.up(), MineType.SurroundCev, surroundCevPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                    distance = d;
                }
            }
        }
        return best;
    }

    Target getSurroundMiner() {
        boolean civ = surroundMinerCiv.get();
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            for (Direction dir : OLEPOSSUtils.horizontals) {
                BlockPos pos = player.getBlockPos().offset(dir);

                if (((!civ || !pos.equals(civPos)) && !OLEPOSSUtils.solid2(pos)) || getBlock(pos) == Blocks.BEDROCK) {continue;}

                if (!SettingUtils.inMineRange(pos)) {continue;}

                double d = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(pos));

                if (distanceCheck(civ, pos, distance, d)) {
                    best = new Target(pos, null, MineType.SurroundMiner, surroundMinerPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                    distance = d;
                }
            }
        }
        return best;
    }
    Target getAutoCity() {
        boolean civ = autoCityCiv.get();
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            for (Direction dir : OLEPOSSUtils.horizontals) {
                BlockPos pos = player.getBlockPos().offset(dir);

                if (((!civ || !pos.equals(civPos)) && !OLEPOSSUtils.solid2(pos)) || getBlock(pos) == Blocks.BEDROCK) {continue;}

                if (getBlock(pos.offset(dir)) != Blocks.AIR) {continue;}
                if (newVer.get() && getBlock(pos.offset(dir).up()) != Blocks.AIR) {continue;}
                if (!crystalBlock(pos.offset(dir).down())) {continue;}

                if (!SettingUtils.inMineRange(pos)) {continue;}
                if (!SettingUtils.inPlaceRange(pos.offset(dir).down())) {continue;}

                double d = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(pos));

                if (distanceCheck(civ, pos, distance, d)) {
                    best = new Target(pos, pos.offset(dir), MineType.AutoCity, autoCityPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                    distance = d;
                }
            }
        }
        return best;
    }

    Target getAntiBurrow() {
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            BlockPos pos = player.getBlockPos();

            if (!OLEPOSSUtils.solid2(pos) || getBlock(pos) == Blocks.BEDROCK) {continue;}

            if (!SettingUtils.inMineRange(pos)) {continue;}

            double d = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(pos));

            if (d < distance) {
                best = new Target(pos, null, MineType.AntiBurrow, antiBurrowPriority.get().priority, false, false);
                distance = d;
            }
        }
        return best;
    }

    boolean distanceCheck(boolean civ, BlockPos pos, double closest, double distance) {
        if (civ && pos.equals(civPos)) {return true;}
        return distance < closest;
    }

    boolean priorityCheck(Target current, Priority priority) {
        if (priority.priority < 0) {return false;}
        if (current == null) {return true;}

        return priority.priority >= current.priority;
    }
    void send(Packet<?> packet) {
        ignore = true;
        sendPacket(packet);
        ignore = false;
    }
    void handleStart(PacketEvent.Send event) {
        event.cancel();
        BlockPos pos = ((PlayerActionC2SPacket) event.packet).getPos();
        if (startManually.get() && getBlock(pos) != Blocks.BEDROCK) {
            started = false;
            target = new Target(pos, null, MineType.Manual, 0, false, true);
        }
    }
    void handleAbort(PacketEvent.Send event) {
        event.cancel();
    }
    void handleStop(PacketEvent.Send event) {
        event.cancel();
    }

    Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    Hand getHand() {
        if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
            return Hand.OFF_HAND;
        }
        if (Managers.HOLDING.isHolding(Items.END_CRYSTAL)) {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    boolean miningCheck(int slot) {
        if (target == null || target.pos == null) {return false;}
        return minedFor * speed.get() >= getMineTicks(slot);
    }

    float getTime(BlockPos pos, int slot) {
        BlockState state = mc.world.getBlockState(pos);
        float f = state.getHardness(mc.world, pos);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            float i = !state.isToolRequired() || mc.player.getInventory().getStack(slot).isSuitableFor(state) ? 30 : 100;
            return getSpeed(state, slot) / f / i;
        }
    }

    float getMineTicks(int slot) {
        if (slot != -1) {
            return (float) (1 / (getTime(target.pos, slot) * speed.get()));
        }
        return -1;
    }

    float getSpeed(BlockState state, int slot) {
        ItemStack stack = mc.player.getInventory().getStack(slot);
        float f = mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
        if (f > 1.0) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (i > 0 && !stack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }

        if (effectCheck.get()) {
            if (StatusEffectUtil.hasHaste(mc.player)) {
                f *= 1.0 + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2F;
            }
            if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                f *= Math.pow(0.3, mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier() + 1);
            }
        }

        if (waterCheck.get() && mc.player.isSubmergedInWater() && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            f /= 5.0;
        }

        if (onGroundCheck.get() && !mc.player.isOnGround()) {
            f /= 5.0;
        }

        return f;
    }

    int fastestSlot() {
        int slot = -1;
        if (mc.player == null || mc.world == null) {
            return -1;
        }
        for (int i = 0; i < (switchMode.get() == SwitchMode.Silent ? 9 : 35); i++) {
            if (slot == -1 || (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(target.pos)) > mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(mc.world.getBlockState(target.pos)))) {
                slot = i;
            }
        }
        return slot;
    }

    Color getColor(Color start, Color end, double progress) {
        double r = (end.r - start.r) * progress;
        double g = (end.g - start.g) * progress;
        double b = (end.b - start.b) * progress;
        double a = (end.a - start.a) * progress;
        return new Color((int) Math.round(start.r + r), (int) Math.round(start.g + g), (int) Math.round(start.b + b), (int) Math.round(start.a + a));
    }

    boolean crystalBlock(BlockPos pos) {
        return getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.BEDROCK;
    }

    Box getRenderBox(double progress) {
        return new Box(target.pos.getX() + 0.5 - progress, target.pos.getY() + 0.5 - progress, target.pos.getZ() + 0.5 - progress, target.pos.getX() + 0.5 + progress, target.pos.getY() + 0.5 + progress, target.pos.getZ() + 0.5 + progress);
    }

    public enum AutoMineMode {
        SpeedMine,
        Smart,
        AutoMine,
        CIV
    }

    public enum SwitchMode {
        Silent,
        PickSilent,
        InvSwitch
    }

    public enum Priority {
        Highest(6),
        Higher(5),
        High(4),
        Normal(3),
        Low(2),
        Lower(1),
        Lowest(0),
        Disabled(-1);

        public final int priority;
        Priority(int priority) {
            this.priority = priority;
        }
    }
    public enum MineType {
        Cev,
        TrapCev,
        SurroundCev,
        SurroundMiner,
        AutoCity,
        AntiBurrow,
        Manual
    }
    private record Target(BlockPos pos, BlockPos crystalPos, MineType type,  double priority, boolean civ, boolean manual) {}
}
