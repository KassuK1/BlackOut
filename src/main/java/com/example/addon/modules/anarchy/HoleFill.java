package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
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
Made by OLEPOSSU / Raksamies
*/

public class HoleFill extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swing")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> placeRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Range")
        .description("Range for placing")
        .defaultValue(5)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> holeRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Hole Range")
        .description("Places when enemy is close enough to target hole")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> renderTicks = sgGeneral.add(new DoubleSetting.Builder()
        .name("Render Ticks")
        .description(".")
        .defaultValue(20)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private List<BlockPos> holes = new ArrayList<>();
    private List<BlockPos> toRender = new ArrayList<>();
    private List<Double> renderProgress = new ArrayList<>();

    public HoleFill() {
        super(BlackOut.ANARCHY, "Hole Filler", "Epic cham");
    }

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            updateHoles(placeRange.get());
            List<BlockPos> toPlace = getValid(holes);
            if (!toPlace.isEmpty()) {
                place(toPlace);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player != null && mc.world != null) {
            if (!toRender.isEmpty()) {
                for (int i = 0; i < toRender.size(); i++) {
                    if (toRender.get(i) != null) {
                        BlockPos pos = toRender.get(i);
                        double progress = renderProgress.get(i);
                        double progress2 = event.frameTime;
                        render(event, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), progress / renderTicks.get() / 2);
                        if (progress <= progress2) {
                            renderProgress.set(i, -1.0);
                            toRender.set(i, null);
                        } else {
                            renderProgress.set(i, progress - progress2);
                        }
                    } else {
                        renderProgress.set(i, -1.0);
                    }
                }
                for (int i = 0; i < toRender.size(); i++) {
                    if (renderProgress.contains(-1.0)) {
                        int p = renderProgress.indexOf(-1.0);
                        renderProgress.remove(p);
                        toRender.remove(p);
                    }
                }
            }
        }
    }

    private List<BlockPos> getValid(List<BlockPos> positions) {
        List<BlockPos> list = new ArrayList<>();
        for (BlockPos pos : positions) {
            if (!toRender.contains(pos)) {
                list.add(pos);
            }
        }
        return list;
    }

    private void render(Render3DEvent event,Vec3d v, double progress) {
        Box toRender = new Box(v.x - progress, v.y - progress, v.z - progress, v.x + progress, v.y + progress, v.z + progress);
        event.renderer.box(toRender, new Color(color.get().r, color.get().g, color.get().b,
            (int) Math.floor(color.get().a / 10f)), color.get(), ShapeMode.Both, 0);
    }

    private void updateHoles(double range) {
        holes = new ArrayList<>();
        for(int x1 = (int) -Math.ceil(range); x1 <= Math.ceil(range); x1++) {
            for(int y1 = (int) -Math.ceil(range); y1 <= Math.ceil(range); y1++) {
                for(int z1 = (int) -Math.ceil(range); z1 <= Math.ceil(range); z1++) {
                    int x = x1 + mc.player.getBlockPos().getX();
                    int y = y1 + mc.player.getBlockPos().getY();
                    int z = z1 + mc.player.getBlockPos().getZ();
                    if (isHole(x, y, z) && OLEPOSSUtils.distance(new Vec3d(x + 0.5, y + 0.5, z + 0.5), mc.player.getEyePos()) < range) {
                        holes.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    private boolean isHole(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) ||
            !mc.world.getBlockState(pos.up()).getBlock().equals(Blocks.AIR)||
            !mc.world.getBlockState(pos.up(2)).getBlock().equals(Blocks.AIR) ||
            mc.world.getBlockState(pos.down()).getBlock().equals(Blocks.AIR) ||
            EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !(
                entity.getBlockPos().equals(pos.up()) && entity.getType() != EntityType.ITEM))) {return false;}

        for (Direction dir : OLEPOSSUtils.horizontals) {
            if (mc.world.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.AIR)) {return false;}
        }
        if (!inRange(new Vec3d(x + 0.5, y + 0.5, z + 0.5))) {return false;}
        for (int i = 1; i <= (int) Math.ceil(holeRange.get()); i++) {
            for (Direction dir : OLEPOSSUtils.horizontals) {
                if (isHoleInWall(pos.offset(dir).up(i), dir)) {return true;}
            }
        }
        return false;
    }

    private boolean inRange(Vec3d pos) {
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl)) {
                if (OLEPOSSUtils.distance(pos, pl.getPos()) < holeRange.get()) {return true;}
            }
        }
        return false;
    }

    private boolean isHoleInWall(BlockPos pos, Direction dir) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.AIR && (
            (mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR ||
                mc.world.getBlockState(pos.offset(dir.getOpposite())).getBlock() == Blocks.AIR) ||
            mc.world.getBlockState(pos.down()).getBlock() == Blocks.AIR);
    }

    private void place(List<BlockPos> pos) {
        FindItemResult res = InvUtils.findInHotbar(itemStack -> itemStack.getItem().equals(Items.OBSIDIAN));
        if (res.count() > 0) {
            InvUtils.swap(res.slot(), true);
            for (BlockPos position : pos) {
                mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(new Vec3d(position.getX(), position.getY(), position.getZ()), Direction.UP, position, false), 0));
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                toRender.add(position);
                renderProgress.add(renderTicks.get());
            }
            InvUtils.swapBack();
        }
    }
}
