package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GearHud extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("Items")
        .description("Items to show.")
        .defaultValue(Items.END_CRYSTAL, Items.EXPERIENCE_BOTTLE, Items.OBSIDIAN, Items.TOTEM_OF_UNDYING)
        .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("The scale.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(0, 0, 0, 255))
        .build()
    );
    public static final HudElementInfo<GearHud> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "GearHud", "Gear.", GearHud::new);

    public GearHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(55 * scale.get() * scale.get(), 20 * scale.get() * scale.get() * items.get().size());
        for (int i = 0; i < items.get().size(); i++) {
            RenderUtils.drawItem(new ItemStack(items.get().get(i).asItem()), x, (int) Math.round(y + i * 20 * scale.get() * scale.get()), (float) (scale.get() * scale.get()), true);
            renderer.text(getText(items.get().get(i).asItem()), x + 25 * scale.get() * scale.get(), (int) Math.round(y + i * 20 * scale.get() * scale.get()), color.get(), true, scale.get());
        }
    }

    int amountOf(Item item) {
        return InvUtils.find(itemStack -> itemStack.getItem().equals(item)).count();
    }

    String getText(Item item) {
        if (!(item == Items.EXPERIENCE_BOTTLE && armorDur() != 0)) {return String.valueOf(amountOf(item));}
        else {return amountOf(item) + "  (" + Math.round(amountOf(item) * 14 / armorDur() * 100) + "%)";}
    }

    double armorDur() {
        double rur = 0;
        if (mc.player != null) {
            for (int i = 0; i < 4; i++) {
                rur += mc.player.getInventory().armor.get(i).getMaxDamage();
            }
        }
        return rur;
    }
}
