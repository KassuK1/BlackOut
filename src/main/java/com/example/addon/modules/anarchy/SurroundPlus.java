package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/*
Made by KassuK
*/

public class SurroundPlus extends Module {
    public SurroundPlus() {super(BlackOut.ANARCHY, "Surround+", "KasumsSoft surround");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> itemswitch = sgGeneral.add(new BoolSetting.Builder()
        .name("Switch")
        .description("Should we switch to obby")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> center = sgGeneral.add(new BoolSetting.Builder()
        .name("Center")
        .description("Should we center on da hole")
        .defaultValue(true)
        .build()
    );
    private List<BlockPos> check(BlockPos pos) {
        List<BlockPos> list = new ArrayList<>();
        for (Direction direction : OLEPOSSUtils.horizontals) {
            Box box = new Box(pos.offset(direction).getX(), pos.getY(), pos.offset(direction).getZ(), pos.offset(direction).getX() + 1, pos.getY() + 1, pos.offset(direction).getZ() + 1);
            if (EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                continue;
            }
            if (mc.world.getBlockState(pos.offset(direction)).getBlock().equals(Blocks.AIR)) {
                list.add(pos.offset(direction));
            }
        }
        return list;
    }
    @EventHandler
    private void onTick(TickEvent.Pre event){
        if (mc.player != null && mc.world != null){
            FindItemResult result = InvUtils.findInHotbar(Items.OBSIDIAN);
            int obsidian = result.count();
            if (mc.player.getMainHandStack().getItem() != Items.OBSIDIAN && itemswitch.get()) {
                InvUtils.swap(result.slot(), false);
            }
            if (center.get()){
                PlayerUtils.centerPlayer();
            }
            List<BlockPos> blocks = check(mc.player.getBlockPos());
            blocks.forEach(toplace -> {
                    mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                        new BlockHitResult(new Vec3d(toplace.getX(), toplace.getY(), toplace.getZ()), Direction.UP,  toplace, false), 0));
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            });
        }
    }
}
