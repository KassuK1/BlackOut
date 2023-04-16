package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoMend extends BlackOutModule {
    public AutoMend() {super(BlackOut.BLACKOUT, "AutoMend", "Automatically fixes your armor with experience bottles");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> autoCrystal = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Crystal Pause")
        .description("Only throws bottles if autocrystal isn't placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> autoCrystalTicks = sgGeneral.add(new IntSetting.Builder()
        .name("Auto Crystal Ticks")
        .description("How many ticks to wait after autocrytal places.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Boolean> surroundPause = sgGeneral.add(new BoolSetting.Builder()
        .name("Surround Pause")
        .description("Only throws bottles if surround is not placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> surroundTicks = sgGeneral.add(new IntSetting.Builder()
        .name("Surround Ticks")
        .description("How many ticks to wait after surround places.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Boolean> selfTrapPause = sgGeneral.add(new BoolSetting.Builder()
        .name("Self Trap Pause")
        .description("Only throws bottles if self trap is not placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> selfTrapTicks = sgGeneral.add(new IntSetting.Builder()
        .name("Self Trap Ticks")
        .description("How many ticks to wait after self trap places.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Boolean> movePause = sgGeneral.add(new BoolSetting.Builder()
        .name("Move Pause")
        .description("Only throws bottles if you aren't moving")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> moveTicks = sgGeneral.add(new IntSetting.Builder()
        .name("Move Ticks")
        .description("How many ticks to wait after moving.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Boolean> offGroundPause = sgGeneral.add(new BoolSetting.Builder()
        .name("Off Ground Pause")
        .description("Only throws bottles if not on ground.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> offGroundTicks = sgGeneral.add(new IntSetting.Builder()
        .name("Off Ground Ticks")
        .description("How many ticks to wait after being off ground.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Throw Speed")
        .description("Only send message if enemy died inside this range.")
        .defaultValue(20)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Integer> bottles = sgGeneral.add(new IntSetting.Builder()
        .name("Bottles")
        .description("Amount of bottles to throw every time.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description(".")
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

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        SilentBypass
    }

    double timer = 0;
    int bottleSlot = 0;
    int bottleAmount = 0;
    BlockPos lastPos = null;
    boolean started = false;
    boolean shouldRot = false;

    // Pause ticks
    int acTimer = 0;
    int surroundTimer = 0;
    int selfTrapTimer = 0;
    int moveTimer = 0;
    int offGroundTimer = 0;

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) {return;}

        if (AutoCrystalRewrite.placing) {
            acTimer = autoCrystalTicks.get();
        }
        if (SurroundPlus.placing) {
            surroundTimer = surroundTicks.get();
        }
        if (SelfTrapPlus.placing) {
            selfTrapTimer = selfTrapTicks.get();
        }
        if (!mc.player.getBlockPos().equals(lastPos)) {
            lastPos = mc.player.getBlockPos();
            moveTimer = moveTicks.get();
        }
        if (!mc.player.isOnGround()) {
            offGroundTimer = offGroundTicks.get();
        }
    }



    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) {return;}

        timer += speed.get() / 20D;

        updateTimers();

        if (timer >= 1) {
            Hand hand = Managers.HOLDING.isHolding(Items.EXPERIENCE_BOTTLE) ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE ? Hand.OFF_HAND : null;

            if (hand == null) {
                FindItemResult result = switchMode.get() == SwitchMode.SilentBypass ? InvUtils.find(item -> item.getItem() == Items.EXPERIENCE_BOTTLE) : InvUtils.findInHotbar(item -> item.getItem() == Items.EXPERIENCE_BOTTLE);

                bottleSlot = result.slot();
                bottleAmount = result.count();
            } else {
                bottleSlot = hand == Hand.MAIN_HAND ? Managers.HOLDING.slot : -1;
                bottleAmount = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() : mc.player.getOffHandStack().getCount();
            }


            if (bottleSlot >= 0 && shouldThrow()) {
                shouldRot = true;
                boolean rotated = !(switchMode.get() == SwitchMode.Disabled && hand == null) && Managers.ROTATION.startPitch(90, priority, RotationType.Use);

                if (rotated) {
                    boolean switched = hand != null;

                    if (!switched) {
                        switch (switchMode.get()) {
                            case Silent, Normal -> {
                                InvUtils.swap(bottleSlot, true);
                                switched = true;
                            }
                            case SilentBypass -> {
                                switched = BOInvUtils.invSwitch(bottleSlot);
                            }
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
                                case Silent -> {
                                    InvUtils.swapBack();
                                }
                                case SilentBypass -> {
                                    BOInvUtils.swapBack();
                                }
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

    boolean shouldThrow() {
        if (autoCrystal.get() && acTimer > 0) {return false;}
        if (surroundPause.get() && surroundTimer > 0) {return false;}
        if (selfTrapPause.get() && selfTrapTimer > 0) {return false;}
        if (movePause.get() && moveTimer > 0) {return false;}
        if (offGroundPause.get() && offGroundTimer > 0) {return false;}

        if (!shouldMend()) {return false;}

        return true;
    }

    void updateTimers() {
        acTimer--;
        surroundTimer--;
        selfTrapTimer--;
        moveTimer--;
        offGroundTimer--;
    }

    boolean shouldMend() {
        List<ItemStack> armors = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            armors.add(mc.player.getInventory().getArmorStack(i));
        }

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
        if (lowest <= forceMend.get()) {
            return true;
        }
        if (max >= antiWaste.get()) {
            return false;
        }

        return lowest <= minDur.get() || started;
    }

    void throwBottle(Hand hand) {
        SettingUtils.swing(SwingState.Pre, SwingType.Using, hand);

        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(hand, 0));

        SettingUtils.swing(SwingState.Post, SwingType.Using, hand);
    }
}
