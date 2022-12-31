package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class FeetESP extends Module {
    public FeetESP() {
        super(BlackOut.BLACKOUT, "FeetESP", "No, it doesn't show you pictures of feet");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> self = sgGeneral.add(new BoolSetting.Builder()
        .name("Self")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> friend = sgGeneral.add(new BoolSetting.Builder()
        .name("Friend")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> other = sgGeneral.add(new BoolSetting.Builder()
        .name("Other")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description(".")
        .defaultValue(new SettingColor(0, 0, 255, 255))
        .build()
    );
    private final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("Height")
        .description(".")
        .defaultValue(0)
        .sliderRange(-0.5, 0.5)
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description(".")
        .defaultValue(25)
        .sliderRange(0, 25)
        .build()
    );
    List<Render> renders = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        mc.world.getPlayers().forEach(player -> {
            String name = player.getName().getString();
            Render existing = getByName(name);

            if (existing == null) {
                renders.add(new Render(name, player.getPos()));
            }
        });
        mc.world.getPlayers().forEach(player -> {
            if (OLEPOSSUtils.distance(player.getPos(), mc.player.getEyePos()) <= range.get() &&
                (player != mc.player || self.get()) && (!Friends.get().isFriend(player) || friend.get()) &&
                (Friends.get().isFriend(player) || player == mc.player || other.get())) {
                Render render = getByName(player.getName().getString());
                if (render != null) {render.update(event, color.get(), player.getPos());}
            }
        });
    }

    private Render getByName(String name) {
        for (Render render : renders) {
            if (render.getName().equals(name)) {
                return render;
            }
        }
        return null;
    }

    private class Render {
        private final String name;
        private Vec3d vec;
        public Render(String name, Vec3d vec) {
            this.name = name;
            this.vec = vec;
        }

        public String getName() {
            return name;
        }

        public void update(Render3DEvent event, Color color, Vec3d newVec) {
            double absX = Math.abs(vec.x - newVec.x);
            double absY = Math.abs(vec.y - newVec.y);
            double absZ = Math.abs(vec.z - newVec.z);
            float speed = (float) (event.frameTime * 2 + event.frameTime * Math.sqrt(absX * absX + absY * absY + absZ * absZ) * 50);

            vec = new Vec3d(
                vec.x > newVec.x ?
                    (absX <= speed * absX ? newVec.x : vec.x - speed * absX) :
                    vec.x != newVec.x ?
                        (absX <= speed * absX ? newVec.x : vec.x + speed * absX) :
                        newVec.x
                ,
                vec.y > newVec.y ?
                    (absY <= speed * absY ? newVec.y : vec.y - speed * absY) :
                    vec.y != newVec.y ?
                        (absY <= speed * absY ? newVec.y : vec.y + speed * absY) :
                        newVec.y
                ,
                vec.z > newVec.z ?
                    (absZ <= speed * absZ ? newVec.z : vec.z - speed * absZ) :
                    vec.z != newVec.z ?
                        (absZ <= speed * absZ ? newVec.z : vec.z + speed * absZ) :
                        newVec.z);
            event.renderer.box(new Box(vec.getX() - 0.3, vec.getY(), vec.getZ() - 0.3, vec.getX() + 0.3, vec.getY() + height.get(), vec.getZ() + 0.3),
                new Color(color.r, color.g, color.b, color.a / 5), color, ShapeMode.Both, 0);

        }
    }
}
