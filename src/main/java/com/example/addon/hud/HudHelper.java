package com.example.addon.hud;

import com.example.addon.Addon;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HudHelper extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> durability = sgGeneral.add(new BoolSetting.Builder()
        .name("Durability Alert")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> dur = sgGeneral.add(new IntSetting.Builder()
        .name("Durability")
        .description("The scale.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Boolean> weakness = sgGeneral.add(new BoolSetting.Builder()
        .name("Weakness Alert")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> strength = sgGeneral.add(new BoolSetting.Builder()
        .name("Strength End Alert")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> strTime = sgGeneral.add(new IntSetting.Builder()
        .name("Strength time")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderRange(0, 100)
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

    String item = null;
    public static final HudElementInfo<HudHelper> INFO = new HudElementInfo<>(Addon.HUD_ANARCHY, "HudHelper", "Helps", HudHelper::new);

    public HudHelper() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(90 * scale.get(), 80 * scale.get());
        List<String> alert = getAlerts();
        for (int i = 0; i < alert.size(); i++) {
            renderer.text(alert.get(i), x, y + (15 * i) * scale.get(), color.get(), true, scale.get() / 2.5);
        }
    }

    public List<String> getAlerts() {
        List<String> alerts = new ArrayList<>();
        if (mc.player != null && mc.world != null) {
            if (durability.get()) {
                int durr = armorCheck();
                if (durr != 1000) {
                    alerts.add("Durability of " + item + " is low (" + durr + "%)");
                }
            }
            if (weakness.get()) {
                boolean hasWeakness = mc.player.hasStatusEffect(StatusEffect.byRawId(18));
                if (hasWeakness) {
                    StatusEffectInstance rur = mc.player.getActiveStatusEffects().get(StatusEffect.byRawId(18));
                    alerts.add("You have weakness (" + Math.round(rur.getDuration() / 20f) + "s)");
                }
            }
            if (strength.get()) {
                boolean hasStrength = mc.player.hasStatusEffect(StatusEffect.byRawId(5));
                if (hasStrength) {
                    StatusEffectInstance rur = mc.player.getActiveStatusEffects().get(StatusEffect.byRawId(5));
                    if (rur.getDuration() < strTime.get() * 20) {
                        alerts.add("Strength is ending in " + Math.round(rur.getDuration() / 20f) + " seconds");
                    }
                }
            }
        }
        return alerts;
    }

    public int armorCheck() {
        int rur = 1000;
        if (mc.player != null) {
            for (int i = 0; i < 4; i++) {
                ItemStack itemStack = mc.player.getInventory().armor.get(i);
                if (OLEPOSSUtils.isArmor(itemStack.getItem())) {
                    float dmg = itemStack.getDamage();
                    float max = itemStack.getMaxDamage();
                    int veryDur = Math.round((max - dmg) / max * 100);
                    if (veryDur <= dur.get() && veryDur < rur) {
                        rur = veryDur;
                        item = OLEPOSSUtils.armorCategory(itemStack.getItem());
                    }
                }
            }
        }
        return rur;
    }
}
