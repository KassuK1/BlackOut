package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.HoleType;
import kassuk.addon.blackout.utils.HoleUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * @author OLEPOSSU
 */

public class Automation extends BlackOutModule {
    public Automation() {
        super(BlackOut.BLACKOUT, "Automation", "Automatically enables modules in certain situations.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> holeSurround = sgGeneral.add(new BoolSetting.Builder()
        .name("Hole Surround")
        .description("Enables surround when you enter a hole.")
        .defaultValue(true)
        .build()
    );

    private BlockPos lastPos = null;
    private SurroundPlus surround = null;

    @EventHandler
    private void onRender(Render3DEvent event) {

        if (mc.player == null || mc.world == null) {return;}

        if (surround == null) {
            surround = Modules.get().get(SurroundPlus.class);
        }

        if (!mc.player.getBlockPos().equals(lastPos) && inAHole(mc.player)) {
            if (holeSurround.get() && !surround.isActive()) {
                surround.toggle();
                surround.sendToggledMsg("enabled by Automation");
            }
        }

        lastPos = mc.player.getBlockPos();
    }

    private boolean inAHole(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();

        if (HoleUtils.getHole(pos, 1).type == HoleType.Single) {
            return true;
        }
        // DoubleX
        if (HoleUtils.getHole(pos, 1).type == HoleType.DoubleX ||
            HoleUtils.getHole(pos.add(-1, 0, 0), 1).type == HoleType.DoubleX) {
            return true;
        }

        // DoubleZ
        if (HoleUtils.getHole(pos, 1).type == HoleType.DoubleZ ||
            HoleUtils.getHole(pos.add(0, 0, -1), 1).type == HoleType.DoubleZ) {
            return true;
        }

        // Quad
        return HoleUtils.getHole(pos, 1).type == HoleType.Quad ||
            HoleUtils.getHole(pos.add(-1, 0, -1), 1).type == HoleType.Quad ||
            HoleUtils.getHole(pos.add(-1, 0, 0), 1).type == HoleType.Quad ||
            HoleUtils.getHole(pos.add(0, 0, -1), 1).type == HoleType.Quad;
    }
}
