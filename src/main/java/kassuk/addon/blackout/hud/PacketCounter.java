package kassuk.addon.blackout.hud;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.managers.Managers;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
/*
Made By KassuK & OLEPOSSU
 */

public class PacketCounter extends HudElement {

    public static final HudElementInfo<PacketCounter> INFO = new HudElementInfo<>(BlackOut.HUD_BLACKOUT, "PacketCounter", "A target hud the fuck you thinkin bruv", PacketCounter::new);
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Renderer scale")
        .description("Scale to render at")
        .defaultValue(1)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description(".")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    int packets = -1;
    int target = 0;

    public PacketCounter() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        target = Managers.PACKETS.getSent();
        packets = packets == -1 ? target : packets == target ? target : packets < target ? packets + 1 : packets - 1;
        setSize(scale.get() * scale.get() * renderer.textWidth("135 Packets/s"), renderer.textHeight(true, scale.get()));
        renderer.text(packets + " Packets/s", x, y, color.get(), true, scale.get());
    }
}
