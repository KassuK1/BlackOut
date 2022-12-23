package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

//Made by KassuK

public class KassuKAura extends Module {
    public KassuKAura(){super(BlackOut.ANARCHY,"ForceField", "An Killaura made by KassuK probably should not be used");}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("Range")
        .description("Range to hit in")
        .defaultValue(3)
        .range(0, 6)
        .sliderMax(6)
        .build()
    );

    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay that will be used for hits")
        .defaultValue(0.420)
        .range(0, 2)
        .sliderMax(2)
        .build()
    );

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("Debug")
        .description("Prints debug stuff")
        .defaultValue(true)
        .build()
    );

    double timer = 0;

    Entity target = null;

    @EventHandler
    private void onRender(Render3DEvent event){
        timer = Math.min(delay.get(),timer + event.frameTime);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event){
        if (mc.player != null && mc.world != null){
            target = TargetUtils.getPlayerTarget(range.get(), SortPriority.ClosestAngle);
            if (debug.get()){
                info("timer" + timer);
            }
            if (timer >=delay.get()){
                timer = 0;
                if (debug.get()){info("Timer passed");}
                if (target != null){
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);
                if (debug.get()){info("Tried To Hit");}
                }
            }
        }
    }
}
