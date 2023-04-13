package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

/*
Made by OLEPOSSU
*/

public class SwingSettings extends BlackOutModule {
    public SwingSettings() {
        super(BlackOut.SETTINGS, "Swing", "Global swing settings for every blackout module");
    }

    private final SettingGroup sgCrystal = settings.createGroup("Crystal");
    private final SettingGroup sgInteract = settings.createGroup("Interact");
    private final SettingGroup sgMining = settings.createGroup("Mining");
    private final SettingGroup sgPlace = settings.createGroup("Placing");
    private final SettingGroup sgAttack = settings.createGroup("Attacking");
    private final SettingGroup sgUse = settings.createGroup("Using");
    private final Setting<SwingMode> crystalPlace = sgCrystal.add(new EnumSetting.Builder<SwingMode>()
        .name("Crystal Place Swing")
        .defaultValue(SwingMode.Disabled)
        .build()
    );
    private final Setting<SwingHand> crystalHand = sgCrystal.add(new EnumSetting.Builder<SwingHand>()
        .name("Crystal Hand")
        .defaultValue(SwingHand.MainHand)
        .build()
    );
    private final Setting<SwingState> crystalState = sgCrystal.add(new EnumSetting.Builder<SwingState>()
        .name("Crystal Swing State")
        .defaultValue(SwingState.Post)
        .build()
    );
    private final Setting<SwingMode> interact = sgInteract.add(new EnumSetting.Builder<SwingMode>()
        .name("Interact Swing")
        .defaultValue(SwingMode.Full)
        .build()
    );private final Setting<SwingHand> interactHand = sgInteract.add(new EnumSetting.Builder<SwingHand>()
        .name("Interact Hand")
        .defaultValue(SwingHand.MainHand)
        .build()
    );
    private final Setting<SwingState> interactState = sgInteract.add(new EnumSetting.Builder<SwingState>()
        .name("Interact Swing State")
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
    private final Setting<MiningSwingState> miningState = sgMining.add(new EnumSetting.Builder<MiningSwingState>()
        .name("Mining Swing State")
        .defaultValue(MiningSwingState.End)
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
    private final Setting<SwingMode> using = sgUse.add(new EnumSetting.Builder<SwingMode>()
        .name("Using Swing")
        .defaultValue(SwingMode.Full)
        .build()
    );
    private final Setting<SwingHand> usingHand = sgUse.add(new EnumSetting.Builder<SwingHand>()
        .name("Using Hand")
        .defaultValue(SwingHand.MainHand)
        .build()
    );
    private final Setting<SwingState> usingState = sgUse.add(new EnumSetting.Builder<SwingState>()
        .name("Using Swing State")
        .defaultValue(SwingState.Post)
        .build()
    );

    public enum SwingMode {
        Disabled,
        Client,
        Packet,
        Full
    }
    public enum MiningSwingState {
        Full,
        Start,
        End,
        Double
    }
    public enum SwingHand {
        MainHand,
        OffHand,
        RealHand
    }

    public void swing(SwingState state, SwingType type, Hand hand) {
        if (mc.player == null) {return;}
        if (!state.equals(getState(type))) {return;}
        Hand renderHand = gethand(type, hand);
        switch (type) {
            case Crystal -> swing(crystalPlace.get(), renderHand, hand);
            case Interact -> swing(interact.get(), renderHand, hand);
            case Placing -> swing(placing.get(), renderHand, hand);
            case Attacking -> swing(attacking.get(), renderHand, hand);
            case Using -> swing(using.get(), renderHand, hand);
        }
    }
    public void mineSwing(MiningSwingState state) {
        switch (state) {
            case Start -> {
                if (miningState.get() != MiningSwingState.Double && miningState.get() != MiningSwingState.Start && miningState.get() != MiningSwingState.Full) {
                    return;
                }
            }
            case End -> {
                if (miningState.get() != MiningSwingState.Double && miningState.get() != MiningSwingState.End && miningState.get() != MiningSwingState.Full) {
                    return;
                }
            }
            case Full -> {
                if (miningState.get() != MiningSwingState.Full) {
                    return;
                }
            }
        }
        if (mc.player == null) {return;}
        Hand hand = gethand(SwingType.Mining, Hand.MAIN_HAND);
        swing(mining.get(), hand, Hand.MAIN_HAND);
    }
    Hand gethand(SwingType type, Hand realHand) {
        SwingHand swingHand = switch (type) {
            case Crystal -> crystalHand.get();
            case Interact -> interactHand.get();
            case Mining -> miningHand.get();
            case Placing -> placinghand.get();
            case Attacking -> attackingHand.get();
            case Using -> usingHand.get();
        };
        return getHand(swingHand, realHand);
    }
    Hand getHand(SwingHand hand, Hand realHand) {
        return switch (hand) {
            case MainHand -> Hand.MAIN_HAND;
            case OffHand -> Hand.OFF_HAND;
            case RealHand -> realHand;
        };
    }
    SwingState getState(SwingType type) {
        return switch (type) {
            case Crystal -> crystalState.get();
            case Interact -> interactState.get();
            case Mining -> SwingState.Post;
            case Placing -> placingState.get();
            case Attacking -> attackingState.get();
            case Using -> usingState.get();
        };
    }
    void swing(SwingMode mode, Hand renderHand, Hand hand) {
        if (mc.player == null) {return;}
        switch (mode) {
            case Full -> {
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
                mc.player.swingHand(renderHand, true);
            }
            case Packet -> mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
            case Client -> mc.player.swingHand(renderHand, true);
        }
    }
}
