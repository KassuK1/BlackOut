package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.EndCrystalEntityRendererMixin;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class WebPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> first = sgGeneral.add(new IntSetting.Builder()
        .name("First")
        .description(".")
        .defaultValue(100)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    private final Setting<Integer> second = sgGeneral.add(new IntSetting.Builder()
        .name("Second")
        .description(".")
        .defaultValue(100)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    int yees = 0;
    List<Entity> en = new ArrayList<>();

    public WebPlus() {
        super(BlackOut.ANARCHY, "Web+", "Places an web inside you automatically");
    }
    @Override
    public void onActivate() {
        super.onActivate();
        en.clear();
        yees = delay.get();
        if (mc.world != null && mc.player != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof EndCrystalEntity) {
                    en.add(entity);
                }
            }
        }

        en.forEach(it -> {
            mc.world.removeEntity(it.getId(), Entity.RemovalReason.KILLED);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.world != null && mc.player != null) {
            yees--;
            if (yees < 0) {
                en.forEach(it -> {
                    mc.world.addEntity(it.getId(), it);
                });
                this.toggle();
            }
        }
    }
}
