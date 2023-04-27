/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/gui/themes/meteor/MeteorGuiTheme.java
*/
package kassuk.addon.blackout.gui;

import kassuk.addon.blackout.gui.widgets.*;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.*;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.pressable.WMeteorFavorite;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.*;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC;

public class BlackOutTheme extends GuiTheme {
    public BlackOutTheme(String name) {
        super(name);
        settingsFactory = new DefaultSettingsWidgetFactory(this);
    }

    @Override
    public WWindow window(WWidget icon, String title) {
        return w(new WBlackOutWindow(icon, title));
    }

    @Override
    public WLabel label(String text, boolean title, double maxWidth) {
        if (maxWidth == 0) return w(new WBlackOutLabel(text, title));
        return w(new WBlackOutMultiLabel(text, title, maxWidth));
    }

    @Override
    public WHorizontalSeparator horizontalSeparator(String text) {
        return w(new WBlackOutHorizontalSeparator(text));
    }

    @Override
    public WVerticalSeparator verticalSeparator() {
        return w(new WBlackOutVerticalSeparator());
    }

    @Override
    protected WButton button(String text, GuiTexture texture) {
        return w(new WBlackOutButton(text, texture));
    }

    @Override
    public WMinus minus() {
        return w(new WBlackOutMinus());
    }

    @Override
    public WPlus plus() {
        return w(new WBlackOutPlus());
    }

    @Override
    public WCheckbox checkbox(boolean checked) {
        return w(new WBlackOutCheckBox(checked));
    }

    @Override
    public WSlider slider(double value, double min, double max) {
        return w(new WBlackOutSlider(value, min, max));
    }

    @Override
    public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return w(new WBlackOutTextBox(text, placeholder, filter, renderer));
    }

    @Override
    public <T> WDropdown<T> dropdown(T[] values, T value) {
        return w(new WBlackOutDropDown<>(values, value));
    }

    @Override
    public WTriangle triangle() {
        return w(new WBlackOutTriangle());
    }

    @Override
    public WTooltip tooltip(String text) {
        return w(new WBlackOutToolTip(text));
    }

    @Override
    public WView view() {
        return w(new WBlackOutView());
    }

    @Override
    public WSection section(String title, boolean expanded, WWidget headerWidget) {
        return w(new WBlackOutSection(title, expanded, headerWidget));
    }

    @Override
    public WAccount account(WidgetScreen screen, Account<?> account) {
        return w(new WBlackOutAccount(screen, account));
    }

    @Override
    public WWidget module(Module module) {
        return w(new WBlackOutModule(module));
    }

    @Override
    public WQuad quad(Color color) {
        return w(new WMeteorQuad(color));
    }

    @Override
    public WTopBar topBar() {
        return w(new WBlackOutTopBar());
    }

    @Override
    public WFavorite favorite(boolean checked) {
        return w(new WBlackOutFavorite(checked));
    }

    // Colors

    @Override
    public Color textColor() {
        return new Color(255, 255, 255, 255);
    }

    @Override
    public Color textSecondaryColor() {
        return new Color(255, 255, 255, 255);
    }

    //     Starscript

    @Override
    public Color starscriptTextColor() {
        return new Color(255, 50, 255, 255);
    }

    @Override
    public Color starscriptBraceColor() {
        return new Color(0, 50, 255, 255);
    }

    @Override
    public Color starscriptParenthesisColor() {
        return new Color(0, 0, 0, 255);
    }

    @Override
    public Color starscriptDotColor() {
        return new Color(0, 0, 255, 255);
    }

    @Override
    public Color starscriptCommaColor() {
        return new Color(255, 0, 255, 255);
    }

    @Override
    public Color starscriptOperatorColor() {
        return new Color(255, 255, 0, 255);
    }

    @Override
    public Color starscriptStringColor() {
        return new Color(0, 0, 255, 255);
    }

    @Override
    public Color starscriptNumberColor() {
        return new Color(0, 255, 0, 255);
    }

    @Override
    public Color starscriptKeywordColor() {
        return new Color(255, 0, 0, 255);
    }

    @Override
    public Color starscriptAccessedObjectColor() {
        return new Color(255, 255, 255, 255);
    }

    // Other

    @Override
    public TextRenderer textRenderer() {
        return TextRenderer.get();
    }

    @Override
    public double scale(double value) {
        double scaled = value * 1;

        if (IS_SYSTEM_MAC) {
            scaled /= (double) mc.getWindow().getWidth() / mc.getWindow().getFramebufferWidth();
        }

        return scaled;
    }

    @Override
    public boolean categoryIcons() {
        return true;
    }

    @Override
    public boolean hideHUD() {
        return true;
    }

    public Color getTextColor() {
        return new Color(255, 255, 255, 255);
    }
    public Color getBackgroundColor() {
        return new Color(50, 50, 50, 255);
    }
    public Color getBackgroundColorLight() {
        return new Color(75, 75, 75, 255);
    }
    public Color getOutlineColor() {
        return new Color(100, 100, 100, 255);
    }
    public Color getTitleColor() {return new Color(100, 100, 100, 255);}
    public Color getScrollbarColor() {return new Color(100, 100, 100, 255);}
    public Color getSeparatorColor() {return new Color(100, 100, 100, 255);}
}
