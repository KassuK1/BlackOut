package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class AntiAim extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Modes> mode = sgGeneral.add(new EnumSetting.Builder<Modes>()
        .name("Place Swing Mode")
        .description(".")
        .defaultValue(Modes.Custom)
        .build()
    );
    private final Setting<Double> enemyRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Enemy Range")
        .description(".")
        .defaultValue(20)
        .range(0, 1000)
        .sliderMin(0)
        .visible(() -> mode.get().equals(Modes.Enemy))
        .sliderMax(1000)
        .build()
    );

    private final Setting<Double> spinSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Enemy Range")
        .description(".")
        .defaultValue(5)
        .range(0, 100)
        .sliderMin(0)
        .visible(() -> mode.get().equals(Modes.Spin))
        .sliderMax(100)
        .build()
    );

    private final Setting<Integer> yaw = sgGeneral.add(new IntSetting.Builder()
        .name("Yaw")
        .description("Yaw")
        .defaultValue(0)
        .range(-180, 180)
        .sliderMin(-180)
        .visible(() -> mode.get().equals(Modes.Custom))
        .sliderMax(180)
        .build()
    );
    private final Setting<Integer> pitch = sgGeneral.add(new IntSetting.Builder()
        .name("Yaw")
        .description("Yaw")
        .defaultValue(90)
        .range(-90, 90)
        .sliderMin(-90)
        .sliderMax(90)
        .visible(() -> mode.get().equals(Modes.Custom))
        .build()
    );

    public enum Modes {
        Enemy,
        Spin,
        Custom
    }

    double spinYaw;

    public AntiAim() {
        super(Addon.ANARCHY, "AntiAim", "Very");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        spinYaw = 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            switch (mode.get()) {
                case Enemy -> {
                    PlayerEntity enemy = getClosest();
                    Rotations.rotate(Rotations.getYaw(enemy.getEyePos()), Rotations.getPitch(enemy.getEyePos()));
                }
                case Spin -> {
                    spinYaw = nextYaw(spinYaw, spinSpeed.get());
                    Rotations.rotate(spinYaw, 0);
                }
                case Custom -> {
                    Rotations.rotate(yaw.get(), pitch.get());
                }
            }
        }
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }

    private PlayerEntity getClosest() {
        PlayerEntity closest = null;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl)) {
                if (closest == null) {
                    closest = pl;
                } else {
                    if (OLEPOSSUtils.distance(pl.getPos(), mc.player.getPos()) <
                        OLEPOSSUtils.distance(closest.getPos(), mc.player.getPos())) {
                        closest = pl;
                    }
                }
            }
        }
        return closest;
    }

    private double nextYaw(double current, double speed) {
        if (current + speed > 180) {
            return -360 + current + speed;
        } else {
            return current + speed;
        }
    }
}
