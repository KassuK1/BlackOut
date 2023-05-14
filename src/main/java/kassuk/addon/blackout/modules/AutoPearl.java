package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.RotationUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * @author OLEPOSSU
 */
public class AutoPearl extends BlackOutModule {
    public AutoPearl() {
        super(BlackOut.BLACKOUT, "Auto Pearl", "Easily clip inside walls with pearls.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Which method of switching should be used.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Integer> pitch = sgGeneral.add(new IntSetting.Builder()
        .name("Pitch")
        .description("How deep down to look.")
        .defaultValue(85)
        .range(-90, 90)
        .sliderRange(0, 90)
        .build()
    );

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) {return;}

        Hand hand = getHand();

        if (switch (switchMode.get()) {
            case Normal, Silent -> !InvUtils.findInHotbar(Items.ENDER_PEARL).found();
            case PickSilent, InvSwitch -> !InvUtils.find(Items.ENDER_PEARL).found();
        }) {return;}

        boolean rotated = Managers.ROTATION.start(getYaw(), pitch.get(), priority, RotationType.Other) || (RotationUtils.yawAngle(Managers.ROTATION.lastDir[0], getYaw()) < 0.1 && pitch.get() - Managers.ROTATION.lastDir[1] < 0.1);
        if (!rotated) {return;}

        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent -> {
                    InvUtils.swap(InvUtils.findInHotbar(Items.ENDER_PEARL).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.find(Items.ENDER_PEARL).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(Items.ENDER_PEARL).slot());
            }
        }

        if (!switched) {return;}

        SettingUtils.swing(SwingState.Pre, SwingType.Using, Hand.MAIN_HAND);
        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
        SettingUtils.swing(SwingState.Post, SwingType.Using, Hand.MAIN_HAND);

        toggle();
        sendToggledMsg("success");

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
    }

    private int getYaw() {
        return (int) Math.round(Rotations.getYaw(new Vec3d(Math.floor(mc.player.getX()) + 0.5, 0, Math.floor(mc.player.getZ()) + 0.5))) + 180;
    }

    private Hand getHand() {
        if (Managers.HOLDING.isHolding(Items.ENDER_PEARL)) {return Hand.MAIN_HAND;}
        if (mc.player.getOffHandStack().getItem() == Items.ENDER_PEARL) {return Hand.OFF_HAND;}
        return null;
    }

    public enum SwitchMode {
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }
}
