package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.HoleType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.utils.Hole;
import kassuk.addon.blackout.utils.HoleUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author OLEPOSSU
 */

public class HoleFillPlus extends BlackOutModule {
    public HoleFillPlus() {
        super(BlackOut.BLACKOUT, "Hole Fill Rewrite", "Automatically is a cunt to your enemies.");
    }

    private final SettingGroup sgNear = settings.createGroup("Near");
    private final SettingGroup sgWalking = settings.createGroup("Walking");
    private final SettingGroup sgLooking = settings.createGroup("Looking");
    private final SettingGroup sgPlacing = settings.createGroup("Placing");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgHole = settings.createGroup("Hole");

    //--------------------Near--------------------//
    private final Setting<Boolean> near = sgNear.add(new BoolSetting.Builder()
        .name("Near")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> nearDistance = sgNear.add(new DoubleSetting.Builder()
        .name("Near Distance")
        .description(".")
        .defaultValue(3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> nearExt = sgNear.add(new IntSetting.Builder()
        .name("Near Extrapolation")
        .description(".")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> efficient = sgNear.add(new BoolSetting.Builder()
        .name("Efficient")
        .description("Only places if the hole is closer to target")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> above = sgNear.add(new BoolSetting.Builder()
        .name("Above")
        .description("Only places if target is above the hole")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> iHole = sgNear.add(new BoolSetting.Builder()
        .name("Ignore Hole")
        .description("Doesn't place if enemy is in a hole")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> holeRange = sgNear.add(new DoubleSetting.Builder()
        .name("Hole Range")
        .description("Places when enemy is close enough to target hole")
        .defaultValue(3)
        .min(0)
        .sliderMax(10)
        .build()
    );

    //--------------------Placing--------------------//
    private final Setting<Boolean> pauseEat = addPauseEat(sgPlacing);
    private final Setting<SwitchMode> switchMode = sgPlacing.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. Silent is the most reliable but delays crystals on some servers.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<List<Block>> blocks = sgPlacing.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Which blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .build()
    );
    private final Setting<Double> placeDelay = sgPlacing.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0.125)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> places = sgPlacing.add(new IntSetting.Builder()
        .name("Places")
        .description("Blocks placed per place")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Double> delay = sgPlacing.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between places at single spot.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    //--------------------Hole--------------------//
    private final Setting<Boolean> single = sgHole.add(new BoolSetting.Builder()
        .name("Single")
        .description("Fills 1x1 holes")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> doubleHole = sgHole.add(new BoolSetting.Builder()
        .name("Double")
        .description("Fills 2x1 block holes")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> quad = sgHole.add(new BoolSetting.Builder()
        .name("Quad")
        .description("Fills 2x2 block holes")
        .defaultValue(true)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the outline.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Color")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final List<BlockPos> holes = new ArrayList<>();
    private final BlockTimerList timers = new BlockTimerList();
    private final List<Render> renders = new ArrayList<>();

    private boolean shouldSearch = false;

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Post event) {
        shouldSearch = true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (shouldSearch) updateHoles();
    }

    private void updateHoles() {
        holes.clear();

        int range = (int) Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()) + 1);
        BlockPos p = BlockPos.ofFloored(mc.player.getEyePos());

        List<Hole> holeList = new ArrayList<>();

        for (int x = range; x <= range; x++) {
            for (int y = range; y <= range; y++) {
                for (int z = range; z <= range; z++) {
                    Hole hole = HoleUtils.getHole(p.add(x, y, z));

                    if (hole.type == HoleType.NotHole) continue;
                    if (!single.get() && hole.type == HoleType.Single) continue;
                    if (!doubleHole.get() && (hole.type == HoleType.DoubleX || hole.type == HoleType.DoubleZ)) continue;
                    if (!quad.get() && hole.type == HoleType.Quad) continue;

                    holeList.add(hole);
                }
            }
        }

        holeList.forEach(hole -> {
            if (validHole(hole)) return;
        });
    }

    private boolean validHole(Hole hole) {
        double pDist = mc.player.getPos().distanceTo(hole.middle);

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player.isSpectator() || player == mc.player || player.getHealth() <= 0) continue;
            if (player.getY() <= hole.middle.y) continue;

            double eDist = player.getPos().distanceTo(hole.middle);
            if (eDist > holeRange.get()) continue;
            if (mc.player.getY() > hole.middle.y && eDist < pDist) continue;

            return true;
        }

        return false;
    }

    private record Render(BlockPos pos, Long time) {}

    public enum SwitchMode {
        Disabled,
        Silent,
        PickSilent,
        InvSwitch
    }

    public enum LookCheckMode {

    }
}
