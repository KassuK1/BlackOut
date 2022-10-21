package com.example.addon.hud;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class GearHud extends HudElement {
    //EI TOIMI
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
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
    private static ItemStack[] items = new ItemStack[] {new ItemStack(Items.END_CRYSTAL),
        new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), new ItemStack(Items.TOTEM_OF_UNDYING),
        new ItemStack(Items.OBSIDIAN), new ItemStack(Items.ENCHANTED_BOOK)};

    @Override
    public void render(HudRenderer renderer) {
        for (int i = 0; i < items.length; i++) {
            RenderUtils.drawItem(items[i], x, (int) Math.round(y + i * 25 * scale.get()), scale.get(), true);
        }
    }
}
