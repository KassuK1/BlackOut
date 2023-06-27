package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

/**
 * @author OLEPOSSU
 */

public class StepPlus extends BlackOutModule {
    public StepPlus() {
        super(BlackOut.BLACKOUT, "Step+", "Step but works.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private double yOffset = 0;
    private boolean stepped = false;

    private final double[] v1 = new double[]{0.42, 0.33319999999999994, 0.2468};

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        if (stepped) {
            stepped = false;
            ((IVec3d) event.movement).setY(0);
            return;
        }
        if (!mc.player.isOnGround()) return;

        yOffset = 0;
        // 1 blocks
        if (!i(mc.player.getBoundingBox().offset(event.movement.x, 1, event.movement.z)) && i(mc.player.getBoundingBox().offset(event.movement.x, 0.95, event.movement.z))) {
            step(v1);
        }

        if (yOffset == 0) return;

        ((IVec3d) event.movement).setY(yOffset);
        stepped = true;
    }

    private void step(double[] offsets) {
        yOffset = 0;
        for (int i = 0; i < offsets.length; i++) {
            yOffset += offsets[i];
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + yOffset, mc.player.getZ(), i == offsets.length - 1));
        }
    }

    private boolean i(Box b) {
        return OLEPOSSUtils.inside(mc.player, b);
    }
}
