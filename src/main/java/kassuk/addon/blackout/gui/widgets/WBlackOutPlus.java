/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/gui/themes/meteor/widgets/pressable/WMeteorPlus.java
*/
package kassuk.addon.blackout.gui.widgets;

import kassuk.addon.blackout.gui.BlackOutTheme;
import kassuk.addon.blackout.gui.BlackOutWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBlackOutPlus extends WPlus implements BlackOutWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        BlackOutTheme theme = theme();
        double pad = pad();
        double s = theme.scale(3);

        renderBackground(renderer, this, pressed, mouseOver);
        renderer.quad(x + pad, y + height / 2 - s / 2, width - pad * 2, s, theme.getOutlineColor());
        renderer.quad(x + width / 2 - s / 2, y + pad, s, height - pad * 2, theme.getOutlineColor());
    }
}
