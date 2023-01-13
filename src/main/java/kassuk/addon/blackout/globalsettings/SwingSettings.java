package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

/*
Made by OLEPOSSU
*/

public class SwingSettings extends Module {
    public SwingSettings() {
        super(BlackOut.SETTINGS, "Swing", "Global swing settings for every blackout module");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAC = settings.createGroup("Auto Crystal");
    private final SettingGroup sgMining = settings.createGroup("Mining");
    private final SettingGroup sgPlace = settings.createGroup("Placing");
    private final SettingGroup sgAttack = settings.createGroup("Attacking");
    private final Setting<SwingMode> acPlace = sgAC.add(new EnumSetting.Builder<SwingMode>()
        .name("AC Place Swing")
        .defaultValue(SwingMode.Disabled)
        .build()
    );
    private final Setting<SwingHand> acPlaceHand = sgAC.add(new EnumSetting.Builder<SwingHand>()
        .name("AC Place Hand")
        .defaultValue(SwingHand.MainHand)
        .build()
    );
    private final Setting<SwingState> acPlaceState = sgAC.add(new EnumSetting.Builder<SwingState>()
        .name("AC Place Swing State")
        .defaultValue(SwingState.Post)
        .build()
    );
    private final Setting<SwingMode> acExplode = sgAC.add(new EnumSetting.Builder<SwingMode>()
        .name("AC Explode Swing")
        .defaultValue(SwingMode.Full)
        .build()
    );private final Setting<SwingHand> acExplodeHand = sgAC.add(new EnumSetting.Builder<SwingHand>()
        .name("AC Explode Hand")
        .defaultValue(SwingHand.MainHand)
        .build()
    );
    private final Setting<SwingState> acExplodeState = sgAC.add(new EnumSetting.Builder<SwingState>()
        .name("AC Explode Swing State")
        .defaultValue(SwingState.Post)
        .build()
    );
    private final Setting<SwingMode> mining = sgMining.add(new EnumSetting.Builder<SwingMode>()
        .name("Mining Swing")
        .defaultValue(SwingMode.Full)
        .build()
    );
    private final Setting<SwingHand> miningHand = sgMining.add(new EnumSetting.Builder<SwingHand>()
        .name("Mining Hand")
        .defaultValue(SwingHand.MainHand)
        .build()
    );
    private final Setting<SwingState> miningState = sgMining.add(new EnumSetting.Builder<SwingState>()
        .name("Mining Swing State")
        .defaultValue(SwingState.Post)
        .build()
    );
    private final Setting<SwingMode> placing = sgPlace.add(new EnumSetting.Builder<SwingMode>()
        .name("Placing Swing")
        .defaultValue(SwingMode.Full)
        .build()
    );
    private final Setting<SwingHand> placinghand = sgPlace.add(new EnumSetting.Builder<SwingHand>()
        .name("Placing Hand")
        .defaultValue(SwingHand.MainHand)
        .build()
    );
    private final Setting<SwingState> placingState = sgPlace.add(new EnumSetting.Builder<SwingState>()
        .name("Placing Swing State")
        .defaultValue(SwingState.Post)
        .build()
    );
    private final Setting<SwingMode> attacking = sgAttack.add(new EnumSetting.Builder<SwingMode>()
        .name("Attacking Swing")
        .defaultValue(SwingMode.Full)
        .build()
    );
    private final Setting<SwingHand> attackingHand = sgAttack.add(new EnumSetting.Builder<SwingHand>()
        .name("Attacking Hand")
        .defaultValue(SwingHand.MainHand)
        .build()
    );
    private final Setting<SwingState> attackingState = sgAttack.add(new EnumSetting.Builder<SwingState>()
        .name("Attacking Swing State")
        .defaultValue(SwingState.Post)
        .build()
    );

    public enum SwingType {
        AutoCrystalPlace,
        AutoCrystalExplode,
        Mining,
        Placing,
        Attacking
    }
    public enum SwingMode {
        Disabled,
        Client,
        Packet,
        Full
    }
    public enum SwingHand {
        MainHand,
        OffHand
    }
    public enum SwingState {
        Pre,
        Post
    }

    public void swing(SwingState state, SwingType type) {
        if (mc.player == null) {return;}
        if (!state.equals(getState(type))) {return;}
        Hand hand = gethand(type);
        switch (type) {
            case AutoCrystalPlace -> swing(acPlace.get(), hand);
            case AutoCrystalExplode -> swing(acExplode.get(), hand);
            case Mining -> swing(mining.get(), hand);
            case Placing -> swing(placing.get(), hand);
            case Attacking -> swing(attacking.get(), hand);
        }
    }
    Hand gethand(SwingType type) {
        SwingHand swingHand = SwingHand.MainHand;
        switch (type) {
            case AutoCrystalPlace -> swingHand = acPlaceHand.get();
            case AutoCrystalExplode -> swingHand = acExplodeHand.get();
            case Mining -> swingHand = miningHand.get();
            case Placing -> swingHand = placinghand.get();
            case Attacking -> swingHand = attackingHand.get();
        }
        return getHand(swingHand);
    }
    Hand getHand(SwingHand hand) {
        switch (hand) {
            case MainHand -> {
                return Hand.MAIN_HAND;
            }
            case OffHand -> {
                return Hand.OFF_HAND;
            }
        }
        return Hand.MAIN_HAND;
    }
    SwingState getState(SwingType type) {
        switch (type) {
            case AutoCrystalPlace -> {
                return acPlaceState.get();
            }
            case AutoCrystalExplode -> {
                return acExplodeState.get();
            }
            case Mining -> {
                return miningState.get();
            }
            case Placing -> {
                return placingState.get();
            }
            case Attacking -> {
                return attackingState.get();
            }
        }
        return SwingState.Post;
    }
    void swing(SwingMode mode, Hand hand) {
        switch (mode) {
            case Full -> mc.player.swingHand(hand);
            case Packet -> mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
            case Client -> mc.player.swingHand(hand, true);
        }
    }
}
