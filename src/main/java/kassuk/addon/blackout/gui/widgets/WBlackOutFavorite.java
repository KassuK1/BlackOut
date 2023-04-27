/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/gui/themes/meteor/widgets/pressable/WMeteorFavorite.java
*/
package kassuk.addon.blackout.gui.widgets;

import kassuk.addon.blackout.gui.BlackOutWidget;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBlackOutFavorite extends WFavorite implements BlackOutWidget {
    public WBlackOutFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return new Color(255 ,255, 0, 255);
    }
}
