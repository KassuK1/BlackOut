package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoPearl extends Module {
    public AutoPearl() {
        super(BlackOut.BLACKOUT, "AutoPearl", "Easily clip inside walls");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swings before placing")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> pitch = sgGeneral.add(new IntSetting.Builder()
        .name("Pitch")
        .description(".")
        .defaultValue(85)
        .range(0, 90)
        .sliderRange(0, 90)
        .build()
    );

    @Override
    public void onActivate() {
        super.onActivate();
        Vec3d pos = mc.player.getPos();
        FindItemResult res = InvUtils.findInHotbar(Items.ENDER_PEARL);
        if (res.count() > 0) {
            InvUtils.swap(res.slot(), true);
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(getYaw(pos), pitch.get(), mc.player.isOnGround()));
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
            if (swing.get()) {mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));}
            InvUtils.swapBack();
        }
        this.toggle();
    }

    private int getYaw(Vec3d pos) {
        int yaw = (int) Math.round(Rotations.getYaw(new Vec3d(Math.floor(pos.x) + 0.5, pos.y,
            Math.floor(pos.z) + 0.5)));
        return yaw > 0 ? yaw - 180 : yaw + 180;
    }
}
