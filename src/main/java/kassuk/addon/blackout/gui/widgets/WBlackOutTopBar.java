/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorTopBar.java
*/
package kassuk.addon.blackout.gui.widgets;

import kassuk.addon.blackout.gui.BlackOutWidget;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBlackOutTopBar extends WTopBar implements BlackOutWidget {
    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().getOutlineColor();
    }

    @Override
    protected Color getNameColor() {
        return theme().getTextColor();
    }
}
