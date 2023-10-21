package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BOInvUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author OLEPOSSU
 */

public class AutoMend extends BlackOutModule {
    public AutoMend() {
        super(BlackOut.BLACKOUT, "Auto Mend", "Automatically mends your armor with experience bottles.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> antiCharity = sgGeneral.add(new BoolSetting.Builder()
        .name("Anti Charity")
        .description("Doesn't mend if any enemy is at same position.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Throw Speed")
        .description("How many bottles to throw every second. 20 is recommended.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Integer> bottles = sgGeneral.add(new IntSetting.Builder()
        .name("Bottles")
        .description("Amount of bottles to throw every time.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Integer> minDur = sgGeneral.add(new IntSetting.Builder()
        .name("Min Durability")
        .description("Uses experience if any armor piece is under this durability.")
        .defaultValue(60)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> antiWaste = sgGeneral.add(new IntSetting.Builder()
        .name("Anti Waste")
        .description("Doesn't use experience if any armor piece is above this durability.")
        .defaultValue(90)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> forceMend = sgGeneral.add(new IntSetting.Builder()
        .name("Force Mend")
        .description("Ignores anti waste if any armor piece if under this durability.")
        .defaultValue(30)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );

    //--------------------Pause--------------------//
    private final Setting<Boolean> autoCrystal = sgPause.add(new BoolSetting.Builder()
        .name("Auto Crystal Pause")
        .description("Only throws bottles if auto crystal isn't placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> autoCrystalTicks = sgPause.add(new IntSetting.Builder()
        .name("Auto Crystal Ticks")
        .description("How many ticks to wait after auto crystal places.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .visible(autoCrystal::get)
        .build()
    );
    private final Setting<Boolean> surroundPause = sgPause.add(new BoolSetting.Builder()
        .name("Surround Pause")
        .description("Only throws bottles if surround is not placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> surroundTicks = sgPause.add(new IntSetting.Builder()
        .name("Surround Ticks")
        .description("How many ticks to wait after surround places.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .visible(surroundPause::get)
        .build()
    );
    private final Setting<Boolean> selfTrapPause = sgPause.add(new BoolSetting.Builder()
        .name("Self Trap Pause")
        .description("Only throws bottles if self trap is not placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> selfTrapTicks = sgPause.add(new IntSetting.Builder()
        .name("Self Trap Ticks")
        .description("How many ticks to wait after self trap places.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .visible(selfTrapPause::get)
        .build()
    );
    private final Setting<Boolean> movePause = sgPause.add(new BoolSetting.Builder()
        .name("Move Pause")
        .description("Only throws bottles if you aren't moving")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> moveTicks = sgPause.add(new IntSetting.Builder()
        .name("Move Ticks")
        .description("How many ticks to wait after moving.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .visible(movePause::get)
        .build()
    );
    private final Setting<Boolean> offGroundPause = sgPause.add(new BoolSetting.Builder()
        .name("Off Ground Pause")
        .description("Only throws bottles if not on ground.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> offGroundTicks = sgPause.add(new IntSetting.Builder()
        .name("Off Ground Ticks")
        .description("How many ticks to wait after being off ground.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .visible(offGroundPause::get)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when throwing an exp bottle.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> swingHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(swing::get)
        .build()
    );

    private double timer = 0;
    private BlockPos lastPos = null;
    private boolean started = false;
    private boolean shouldRot = false;

    private int acTimer = 0;
    private int surroundTimer = 0;
    private int selfTrapTimer = 0;
    private int moveTimer = 0;
    private int offGroundTimer = 0;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (AutoCrystalPlus.placing) acTimer = autoCrystalTicks.get();

        if (SurroundPlus.placing) surroundTimer = surroundTicks.get();

        if (SelfTrapPlus.placing) selfTrapTimer = selfTrapTicks.get();

        if (!mc.player.getBlockPos().equals(lastPos)) {
            lastPos = mc.player.getBlockPos();
            moveTimer = moveTicks.get();
        }

        if (!mc.player.isOnGround()) offGroundTimer = offGroundTicks.get();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || Modules.get().isActive(Suicide.class)) return;

        timer += speed.get() / 20D;

        updateTimers();

        if (timer >= 1) {
            Hand hand = Managers.HOLDING.isHolding(Items.EXPERIENCE_BOTTLE) ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE ? Hand.OFF_HAND : null;

            int bottleSlot;
            int bottleAmount;
            if (hand == null) {
                FindItemResult result = switchMode.get() == SwitchMode.PickSilent || switchMode.get() == SwitchMode.InvSwitch ? InvUtils.find(item -> item.getItem() == Items.EXPERIENCE_BOTTLE) : InvUtils.findInHotbar(item -> item.getItem() == Items.EXPERIENCE_BOTTLE);

                bottleSlot = result.slot();
                bottleAmount = result.count();
            } else {
                bottleSlot = hand == Hand.MAIN_HAND ? Managers.HOLDING.slot : -1;
                bottleAmount = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() : mc.player.getOffHandStack().getCount();
            }


            if (bottleSlot >= 0 && shouldThrow()) {
                shouldRot = true;
                boolean rotated = !(switchMode.get() == SwitchMode.Disabled && hand == null) && Managers.ROTATION.startPitch(90, priority, RotationType.Use, Objects.hash(name + "look"));

                if (rotated) {
                    boolean switched = hand != null;

                    if (!switched) {
                        switch (switchMode.get()) {
                            case Silent, Normal -> {
                                InvUtils.swap(bottleSlot, true);
                                switched = true;
                            }
                            case PickSilent -> switched = BOInvUtils.pickSwitch(bottleSlot);
                            case InvSwitch -> switched = BOInvUtils.invSwitch(bottleSlot);
                        }
                    }

                    if (switched) {
                        started = true;
                        for (int i = Math.min(bottleAmount, bottles.get()); i > 0; i--) {
                            throwBottle(hand == null ? Hand.MAIN_HAND : hand);
                            bottleAmount--;
                        }
                        timer--;

                        if (hand == null) {
                            switch (switchMode.get()) {
                                case Silent -> InvUtils.swapBack();
                                case PickSilent -> BOInvUtils.pickSwapBack();
                                case InvSwitch -> BOInvUtils.swapBack();
                            }
                        }
                    }
                }
            } else {
                if (shouldRot) {
                    Managers.ROTATION.endPitch(90, true);
                    shouldRot = false;
                }
                started = false;
            }
        }

        timer = Math.min(1, timer);
    }

    private boolean shouldThrow() {
        return shouldMend() && !(
            autoCrystal.get() && acTimer > 0 ||
                surroundPause.get() && surroundTimer > 0 ||
                selfTrapPause.get() && selfTrapTimer > 0 ||
                movePause.get() && moveTimer > 0 ||
                offGroundPause.get() && offGroundTimer > 0
        );
    }

    private void updateTimers() {
        acTimer--;
        surroundTimer--;
        selfTrapTimer--;
        moveTimer--;
        offGroundTimer--;
    }

    private boolean shouldMend() {
        List<ItemStack> armors = new ArrayList<>();

        for (int i = 0; i < 4; i++) armors.add(mc.player.getInventory().getArmorStack(i));

        float max = -1;
        float lowest = 500;
        float dur;

        for (ItemStack stack : armors) {
            dur = (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage() * 100;

            if (dur > max) {
                max = dur;
            }
            if (dur < lowest) {
                lowest = dur;
            }
        }
        if (lowest <= forceMend.get()) return true;

        if (antiCharity.get() && playerAtPos()) return false;

        if (max >= antiWaste.get()) return false;

        return lowest <= minDur.get() || started;
    }

    private boolean playerAtPos() {
        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (Friends.get().isFriend(player)) continue;
            if (player.getBlockPos().equals(mc.player.getBlockPos())) return true;
        }
        return false;
    }

    private void throwBottle(Hand hand) {
        useItem(hand);

        if (swing.get()) clientSwing(swingHand.get(), hand);
    }

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }
}
