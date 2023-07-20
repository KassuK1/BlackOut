package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class BurrowPlus extends BlackOutModule {
    public BurrowPlus() {
        super(BlackOut.BLACKOUT, "Burrow+", "Places a block inside your feet.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRubberband = settings.createGroup("Rubberband");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Which method of switching should be used.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.ENDER_CHEST)
        .build()
    );
    private final Setting<Boolean> instaRot = sgGeneral.add(new BoolSetting.Builder()
        .name("Instant Rotation")
        .description("Instantly rotates.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pFly = sgGeneral.add(new BoolSetting.Builder()
        .name("Packet Fly")
        .description("Enables packetfly after lagging back inside the block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> scaffold = sgGeneral.add(new BoolSetting.Builder()
        .name("Scaffold")
        .description("Enables scaffold+ after lagging back inside the block.")
        .defaultValue(false)
        .visible(pFly::get)
        .build()
    );

    //--------------------Rubberband--------------------//
    private final Setting<Double> rubberbandOffset = sgRubberband.add(new DoubleSetting.Builder()
        .name("Rubberband Offset")
        .description("Y offset of rubberband packet.")
        .defaultValue(9)
        .sliderRange(-10, 10)
        .build()
    );
    private final Setting<Integer> rubberbandPackets = sgRubberband.add(new IntSetting.Builder()
        .name("Rubberband Packets")
        .description("How many offset packets to send.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );

    private boolean success = false;
    private boolean enabledPFly = false;
    private boolean enabledScaffold = false;

    private final Predicate<ItemStack> predicate = itemStack -> {
        if (!(itemStack.getItem() instanceof BlockItem block)) return false;

        return blocks.get().contains(block.getBlock());
    };

    @Override
    public void onActivate() {
        success = false;

        enabledPFly = false;
        enabledScaffold = false;

        if (mc.player == null || mc.world == null) {return;}

        Hand hand = predicate.test(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND :
            predicate.test(mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;

        boolean blocksPresent = hand != null;

        if (!blocksPresent) {
            switch (switchMode.get()) {
                case Normal, Silent -> blocksPresent = InvUtils.findInHotbar(predicate).found();
                case PickSilent, InvSwitch -> blocksPresent = InvUtils.find(predicate).found();
            }
        }

        if (!blocksPresent) return;

        boolean rotated = instaRot.get() || !SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.startPitch(90, priority, RotationType.BlockPlace, Objects.hash(name + "placing"));
        if (!rotated) return;

        boolean switched = hand != null;

        if (!switched) {
            switched = switch (switchMode.get()) {
                case Normal, Silent -> InvUtils.swap(InvUtils.findInHotbar(predicate).slot(), true);
                case PickSilent -> BOInvUtils.pickSwitch(InvUtils.find(predicate).slot());
                case InvSwitch -> BOInvUtils.invSwitch(InvUtils.find(predicate).slot());
            };
        }

        if (!switched) {
            toggle();
            sendToggledMsg("correct blocks not found");
            return;
        }

        if (instaRot.get() && SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(Managers.ROTATION.lastDir[0], 90, Managers.ON_GROUND.isOnGround()));
        }

        double y = 0;
        double velocity = 0.42;

        while (y < 1.1) {
            y += velocity;
            velocity = (velocity - 0.08) * 0.98;

            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y, mc.player.getZ(), false));
        }

        placeBlock(Hand.MAIN_HAND, mc.player.getBlockPos().down().toCenterPos(), Direction.UP, mc.player.getBlockPos().down());
        if (!instaRot.get() && SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "placing"));

        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

        for (int i = 0; i < rubberbandPackets.get(); i++) {
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y + rubberbandOffset.get(), mc.player.getZ(), false));
        }

        success = true;

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }

        if (!pFly.get()) {
            toggle();
            sendToggledMsg("success");
        }
    }

    @Override
    public void onDeactivate() {
        if (enabledPFly && Modules.get().isActive(PacketFly.class)) {
            Modules.get().get(PacketFly.class).toggle();
            Modules.get().get(PacketFly.class).sendToggledMsg("disabled by burrow+");
        }
        if (enabledScaffold && Modules.get().isActive(ScaffoldPlus.class)) {
            Modules.get().get(ScaffoldPlus.class).toggle();
            Modules.get().get(ScaffoldPlus.class).sendToggledMsg("disabled by burrow+");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Receive event) {
        if (pFly.get() && success && event.packet instanceof PlayerPositionLookS2CPacket) {

            if (!Modules.get().isActive(PacketFly.class)) {
                Modules.get().get(PacketFly.class).toggle();
                Modules.get().get(PacketFly.class).sendToggledMsg("enabled by burrow+");
                enabledPFly = true;
            }
            if (scaffold.get() && !Modules.get().isActive(ScaffoldPlus.class)) {
                Modules.get().get(ScaffoldPlus.class).toggle();
                Modules.get().get(ScaffoldPlus.class).sendToggledMsg("enabled by burrow+");
                enabledScaffold = true;
            }
        }
    }

    public enum SwitchMode {
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }
}
