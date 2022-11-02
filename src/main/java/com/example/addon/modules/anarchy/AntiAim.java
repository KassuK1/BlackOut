package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.List;
import java.util.Random;

/*
Made by OLEPOSSU / Raksamies
*/

public class AntiAim extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgIgnore = settings.createGroup("Ignore");
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
        .name("Spin Speed")
        .description(".")
        .defaultValue(5)
        .range(0, 100)
        .sliderMin(0)
        .visible(() -> mode.get().equals(Modes.Spin))
        .sliderMax(100)
        .build()
    );
    private final Setting<Boolean> rYaw = sgGeneral.add(new BoolSetting.Builder()
        .name("Random Yaw")
        .description(".")
        .defaultValue(true)
        .visible(() -> mode.get().equals(Modes.CSGO))
        .build()
    );
    private final Setting<Boolean> rPitch = sgGeneral.add(new BoolSetting.Builder()
        .name("Random Pitch")
        .description(".")
        .defaultValue(false)
        .visible(() -> mode.get().equals(Modes.CSGO))
        .build()
    );
    private final Setting<Integer> csgoPitch = sgGeneral.add(new IntSetting.Builder()
        .name("CS Pitch")
        .description("Yaw")
        .defaultValue(90)
        .range(-90, 90)
        .sliderMin(-90)
        .visible(() -> mode.get().equals(Modes.CSGO) && !rPitch.get())
        .sliderMax(90)
        .build()
    );
    private final Setting<Double> csDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("CSGO Delay")
        .description(".")
        .defaultValue(5)
        .range(0, 100)
        .sliderMin(0)
        .visible(() -> mode.get().equals(Modes.CSGO))
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
    private final Setting<Boolean> iYaw = sgIgnore.add(new BoolSetting.Builder()
        .name("Ignore Yaw")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<List<Item>> yItems = sgIgnore.add(new ItemListSetting.Builder()
        .name("Ignore Yaw when holding")
        .description(".")
        .build()
    );
    private final Setting<Boolean> iPitch = sgIgnore.add(new BoolSetting.Builder()
        .name("Ignore Pitch")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<List<Item>> pItems = sgIgnore.add(new ItemListSetting.Builder()
        .name("Ignore Pitch when holding")
        .description(".")
        .build()
    );

    public enum Modes {
        Enemy,
        Spin,
        CSGO,
        Custom
    }
    Random r = new Random();

    double spinYaw;
    double csTick = 0;
    private double csYaw;
    private double csPitch;

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
            double y = mc.player.getYaw();
            double p = mc.player.getPitch();
            switch (mode.get()) {
                case Enemy -> {
                    PlayerEntity enemy = getClosest();
                    if (enemy != null) {
                        y = Rotations.getYaw(enemy.getEyePos());
                        p = Rotations.getPitch(enemy.getEyePos());
                    }
                }
                case Spin -> {
                    spinYaw = nextYaw(spinYaw, spinSpeed.get());
                    y = spinYaw;
                    p = mc.player.getPitch();
                }
                case CSGO -> {
                    if (!rYaw.get()) {
                        csYaw = mc.player.getYaw();
                    }
                    if (!rPitch.get()) {
                        csPitch = mc.player.getPitch();
                    }
                    if (csTick <= 0) {
                        csTick += csDelay.get();
                        csYaw = r.nextInt(-180, 180);
                        csPitch = r.nextInt(-90, 90);
                    } else {
                        csTick--;
                    }
                    y = csYaw;
                    p = !rPitch.get() ? csgoPitch.get() : csPitch;
                }
                case Custom -> {
                    y = yaw.get();
                    p = pitch.get();
                }
            }

            Rotations.rotate(contains(yItems.get(),
                mc.player.getMainHandStack().getItem()) && iYaw.get() ? mc.player.getYaw() : y,
                contains(pItems.get(), mc.player.getMainHandStack().getItem()) && iPitch.get() ? mc.player.getPitch() : p);
        }
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }

    private PlayerEntity getClosest() {
        PlayerEntity closest = null;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl) && OLEPOSSUtils.distance(mc.player.getPos(), pl.getPos()) <= enemyRange.get()) {
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

    private boolean contains(List<Item> l, Item item) {
        for (Item i : l) {
            if (i.equals(item)) {
                return true;
            }
        }
        return false;
    }
}
