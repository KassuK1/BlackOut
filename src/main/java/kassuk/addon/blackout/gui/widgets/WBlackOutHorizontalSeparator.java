/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorHorizontalSeparator.java
*/
package kassuk.addon.blackout.gui.widgets;

import kassuk.addon.blackout.gui.BlackOutTheme;
import kassuk.addon.blackout.gui.BlackOutWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBlackOutHorizontalSeparator extends WHorizontalSeparator implements BlackOutWidget {
    public WBlackOutHorizontalSeparator(String text) {
        super(text);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (text == null) renderWithoutText(renderer);
        else renderWithText(renderer);
    }

    private void renderWithoutText(GuiRenderer renderer) {
        BlackOutTheme theme = theme();
        double s = theme.scale(1);
        double w = width / 2;

        renderer.quad(x, y + s, w, s, theme.getSeparatorColor(), theme.getBackgroundColor());
        renderer.quad(x + w, y + s, w, s, theme.getBackgroundColor(), theme.getSeparatorColor());
    }

    private void renderWithText(GuiRenderer renderer) {
        BlackOutTheme theme = theme();
        double s = theme.scale(2);
        double h = theme.scale(1);

        double textStart = Math.round(width / 2.0 - textWidth / 2.0 - s);
        double textEnd = s + textStart + textWidth + s;

        double offsetY = Math.round(height / 2.0);

        renderer.quad(x, y + offsetY, textStart, h, theme.getSeparatorColor(), theme.getBackgroundColor());
        renderer.text(text, x + textStart + s, y, theme.getTextColor(), false);
        renderer.quad(x + textEnd, y + offsetY, width - textEnd, h, theme.getBackgroundColor(), theme.getSeparatorColor());
    }
}
