package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NCPDamageFly extends Module {
    public NCPDamageFly() {super(BlackOut.ANARCHY, "NCPDamageFly", "Epik fly");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> damage = sgGeneral.add(new IntSetting.Builder()
        .name("amomogusgus")
        .description("How much damage to do")
        .defaultValue(1)
        .range(1, 20)
        .sliderMax(20)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null){
            if ((mc.player.getMainHandStack().getItem() == Items.BOW))
                if (BowItem.getPullProgress(mc.player.getItemUseTime()) >= 0.12){
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), -90, mc.player.isOnGround()));
                    mc.options.useKey.setPressed(false);
                    mc.interactionManager.stopUsingItem(mc.player);
                }
        }
    }
}
