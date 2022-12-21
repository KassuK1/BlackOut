package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class CrystalBait extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in ticks.")
        .defaultValue(10)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Integer> floatTime = sgGeneral.add(new IntSetting.Builder()
        .name("Float Time")
        .description(".")
        .defaultValue(2)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    int tick = 0;
    int time = 0;
    int y = 0;
    List<PlayerMoveC2SPacket> allowed = new ArrayList<>();
    public CrystalBait() {
        super(BlackOut.ANARCHY, "Crystal Bait", "Does the funny");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        tick = 0;
        allowed.clear();
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        if (mc.player != null) {
            tick += tick == -1 ? 0 : 1;
            time++;
            if (tick >= delay.get() && tick != -1) {
                tick = -1;
                y = (int) Math.floor(mc.player.getY());
                time = 0;
                up();
            }
            if (time >= floatTime.get()) {
                tick = 0;
                down();
            }
            ((IVec3d) event.movement).set(0, 0, 0);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSend(PacketEvent.Send e) {
        if (e.packet instanceof PlayerMoveC2SPacket packet) {
            if (packet.changesPosition()) {
                if (allowed.contains(packet)) {
                    allowed.remove(packet);
                } else {
                    e.cancel();
                }
            }
        }
    }

    private void send(float y, int playerY) {
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), playerY + y, mc.player.getZ(), y == 0));
    }
    private void up() {
        send(0.42f, y);
        send(0.7616f, y);
        send(1.0248f, y);
        send(1.2096f, y);
        send(1.316f, y);
        send(1.344f, y);
    }
    private void down() {
        send(1.2936f, y);
        send(1.1648f, y);
        send(0.9576f, y);
        send(0.672f, y);
        send(0.308f, y);
        send(0, y);
    }
}
