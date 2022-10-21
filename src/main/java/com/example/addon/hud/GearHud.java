package com.example.addon.hud;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.block.Block;
import net.minecraft.entity.boss.BossBar;
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
    public static final HudElementInfo<GearHud> INFO = new HudElementInfo<>(Addon.HUD_ANARCHY, "GearHud", "Gear.", GearHud::new);

    public GearHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(30 * scale.get() + 15, 22 * scale.get() * items.get().size() + 40);
        for (int i = 0; i < items.get().size(); i++) {
            RenderUtils.drawItem(new ItemStack(items.get().get(i).asItem()), x, (int) Math.round(y + i * 22 * scale.get()) + 20, scale.get(), true);
            renderer.text(String.valueOf(amountOf(items.get().get(i).asItem())), x + 25 * scale.get(), y + i * 22 * scale.get() + 4 * scale.get() + 20, Color.MAGENTA, true);
        }
    }

    public int amountOf(Item item) {
        return InvUtils.find(itemStack -> itemStack.getItem().equals(item)).count();
    }
}
