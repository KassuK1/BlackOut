package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoEz extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> onlyWhenHit = sgGeneral.add(new BoolSetting.Builder()
        .name("Only When Hit")
        .description("Only sends message if you hit the enemy")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description(".")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 50)
        .build()
    );
    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("Messages")
        .description("Messages to send when killing enemy")
        .defaultValue(List.of("Fucked by BlackOut!", "BlackOut on top", "BlackOut strong", "BlackOut gayming","BlackOut on top"))
        .build()
    );
    Random r = new Random();


    public AutoEz() {
        super(Addon.ANARCHY, "AutoEZ", "Self explanatory");
    }
    boolean lastState;

    @Override
    public void onActivate() {
        super.onActivate();
        lastState = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            if (anyDead(range.get())) {
                if (!lastState) {
                    lastState = true;
                    sendMessage();
                }
            } else {
                lastState = false;
            }
        }
    }

    private boolean anyDead(double range) {
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl) && OLEPOSSUtils.distance(pl.getPos(), mc.player.getPos()) < range) {
                if (pl.getHealth() <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendMessage() {
        ChatUtils.sendPlayerMsg(messages.get().get(r.nextInt(0, messages.get().size() - 1)));
    }
}
