package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

/**
 * @author OLEPOSSU
 */

public class SwingSettings extends BlackOutModule {
    public SwingSettings() {
        super(BlackOut.SETTINGS, "Swing", "Global swing settings for every blackout module.");
    }

    private final SettingGroup sgInteract = settings.createGroup("Interact");
    private final SettingGroup sgBlockPlace = settings.createGroup("Block Place");
    private final SettingGroup sgMining = settings.createGroup("Mining");
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgUse = settings.createGroup("Use");

    public final Setting<Boolean> interact = sgInteract.add(new BoolSetting.Builder()
        .name("Interact Swing")
        .description("Swings your hand when you interact with a block.")
        .defaultValue(true)
        .build()
    );
    public final Setting<SwingState> interactState = sgInteract.add(new EnumSetting.Builder<SwingState>()
        .name("Interact State")
        .description("Should we swing our hand before or after the action.")
        .defaultValue(SwingState.Post)
        .visible(interact::get)
        .build()
    );
    public final Setting<Boolean> blockPlace = sgBlockPlace.add(new BoolSetting.Builder()
        .name("Block Place Swing")
        .description("Swings your hand when you interact with a block.")
        .defaultValue(true)
        .build()
    );
    public final Setting<SwingState> blockPlaceState = sgBlockPlace.add(new EnumSetting.Builder<SwingState>()
        .name("Block Place State")
        .description("Should we swing our hand before or after the action.")
        .defaultValue(SwingState.Post)
        .visible(blockPlace::get)
        .build()
    );
    public final Setting<MiningSwingState> mining = sgMining.add(new EnumSetting.Builder<MiningSwingState>()
        .name("Mining Swing")
        .description("Swings your hand when you place a crystal.")
        .defaultValue(MiningSwingState.Double)
        .build()
    );
    public final Setting<Boolean> attack = sgAttack.add(new BoolSetting.Builder()
        .name("Attack Swing")
        .description("Swings your hand when you attack any entity.")
        .defaultValue(true)
        .build()
    );
    public final Setting<SwingState> attackState = sgAttack.add(new EnumSetting.Builder<SwingState>()
        .name("Attack State")
        .description("Should we swing our hand before or after the action.")
        .defaultValue(SwingState.Post)
        .visible(attack::get)
        .build()
    );
    public final Setting<Boolean> use = sgUse.add(new BoolSetting.Builder()
        .name("Use Swing")
        .description("Swings your hand when using an item. NCP doesn't check this.")
        .defaultValue(true)
        .build()
    );
    public final Setting<SwingState> useState = sgUse.add(new EnumSetting.Builder<SwingState>()
        .name("Using State")
        .description("Should we swing our hand before or after the action.")
        .defaultValue(SwingState.Post)
        .visible(use::get)
        .build()
    );

    public void swing(SwingState state, SwingType type, Hand hand) {
        if (mc.player == null) {
            return;
        }
        if (!state.equals(getState(type))) {
            return;
        }

        switch (type) {
            case Interact -> swing(interact.get(), hand);
            case Placing -> swing(blockPlace.get(), hand);
            case Attacking -> swing(attack.get(), hand);
            case Using -> swing(use.get(), hand);
        }
    }

    public void mineSwing(MiningSwingState state) {
        switch (state) {
            case Start -> {
                if (mining.get() != MiningSwingState.Start) {
                    return;
                }
            }
            case End -> {
                if (mining.get() != MiningSwingState.End) {
                    return;
                }
            }
            case Disabled -> {
                return;
            }
        }
        if (mc.player == null) {
            return;
        }

        swing(true, Hand.MAIN_HAND);
    }

    private SwingState getState(SwingType type) {
        return switch (type) {
            case Interact -> interactState.get();
            case Mining -> SwingState.Post;
            case Placing -> blockPlaceState.get();
            case Attacking -> attackState.get();
            case Using -> useState.get();
        };
    }

    private void swing(boolean shouldSwing, Hand hand) {
        if (mc.player == null) {
            return;
        }

        if (shouldSwing) mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
    }

    public enum MiningSwingState {
        Disabled,
        Start,
        End,
        Double
    }
}
