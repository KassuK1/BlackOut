package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoPearl extends BlackOutModule {
    public AutoPearl() {
        super(BlackOut.BLACKOUT, "AutoPearl", "Easily clip inside walls");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> invSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("Inventory Switch")
        .description("Moves pearl to hand before throwing it.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> pitch = sgGeneral.add(new IntSetting.Builder()
        .name("Pitch")
        .description("How deep down to look.")
        .defaultValue(85)
        .range(0, 90)
        .sliderRange(0, 90)
        .build()
    );

    @Override
    public void onActivate() {
        super.onActivate();
        Vec3d pos = mc.player.getPos();
        Hand hand = getHand();
        FindItemResult res = InvUtils.findInHotbar(Items.ENDER_PEARL);
        int slot = InvUtils.find(Items.ENDER_PEARL).slot();
        if (hand != null || (invSwitch.get() && slot >= 0) || (!invSwitch.get() && res.slot() >= 0)) {
            boolean switched = false;
            if (hand == null) {
                if (!invSwitch.get()) {
                    InvUtils.swap(res.slot(), true);
                } else if (BOInvUtils.invSwitch(slot)) {
                    switched = true;
                }
            }


            SettingUtils.swing(SwingState.Pre, SwingType.Using, Hand.MAIN_HAND);

            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(getYaw(pos), pitch.get(), mc.player.isOnGround()));
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));

            SettingUtils.swing(SwingState.Post, SwingType.Using, Hand.MAIN_HAND);

            if (!invSwitch.get() && hand == null) {
                InvUtils.swapBack();
            } else if (switched) {
                BOInvUtils.swapBack();
            }
        }
        this.toggle();
    }

    int getYaw(Vec3d pos) {
        int yaw = (int) Math.round(Rotations.getYaw(new Vec3d(Math.floor(pos.x) + 0.5, pos.y,
            Math.floor(pos.z) + 0.5)));
        return yaw > 0 ? yaw - 180 : yaw + 180;
    }

    Hand getHand() {
        return mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() == Items.ENDER_PEARL ? Hand.OFF_HAND : null;
    }
}
