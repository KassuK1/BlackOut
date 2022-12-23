package kassuk.addon.blackout.modules.ghost;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;

/*
Made by KassuK
*/

public class LegitTotem extends Module {

    public LegitTotem() {
        super(BlackOut.GHOST, "LegitTotem", "More legit autototem");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay.")
        .defaultValue(1)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );

    double timer = 0;

    @EventHandler
    private void onRender(Render3DEvent event){
        timer = Math.min(delay.get(),timer + event.frameTime);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
            int totems = result.count();
            if (mc.currentScreen != null) {
                if (totems != 0 && mc.currentScreen instanceof InventoryScreen && timer >=delay.get()) {
                    if (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                        InvUtils.move().from(result.slot()).toOffhand();
                        timer = 0;
                    }
                }
            }
            else timer = 0;
        }
    }
}
