/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorVerticalSeparator.java
*/
package kassuk.addon.blackout.gui.widgets;

import kassuk.addon.blackout.gui.BlackOutTheme;
import kassuk.addon.blackout.gui.BlackOutWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBlackOutVerticalSeparator extends WVerticalSeparator implements BlackOutWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        BlackOutTheme theme = theme();
        Color colorEdges = theme.getOutlineColor();
        Color colorCenter = theme.getBackgroundColor();

        double s = theme.scale(1);
        double offsetX = Math.round(width / 2.0);

        renderer.quad(x + offsetX, y, s, height / 2, colorEdges, colorEdges, colorCenter, colorCenter);
        renderer.quad(x + offsetX, y + height / 2, s, height / 2, colorCenter, colorCenter, colorEdges, colorEdges);
    }
}
