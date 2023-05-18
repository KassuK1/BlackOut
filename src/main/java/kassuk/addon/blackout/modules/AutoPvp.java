package kassuk.addon.blackout.modules;

import baritone.api.BaritoneAPI;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.RotationUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

/**
 * @author OLEPOSSU
 */

public class AutoPvp extends BlackOutModule {
    public AutoPvp() {
        super(BlackOut.BLACKOUT, "Auto CC", "Follows people using baritone. Best for crystalpvp.cc");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSuicide = settings.createGroup("Suicide");
    private final SettingGroup sgRotations = settings.createGroup("Rotations");
    private final SettingGroup sgEating = settings.createGroup("Eating");
    private final SettingGroup sgMovement = settings.createGroup("Movement");
    private final SettingGroup sgMisc = settings.createGroup("Misc");

    //--------------------General--------------------//
    private final Setting<Boolean> surround = sgGeneral.add(new BoolSetting.Builder()
        .name("Surround")
        .description("Enables surround when close to target.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> underY = sgGeneral.add(new IntSetting.Builder()
        .name("Under Y")
        .description("Target has to be under this y.")
        .defaultValue(500)
        .min(0)
        .sliderRange(0, 500)
        .build()
    );
    private final Setting<Boolean> autoMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Message")
        .description("Sends 'On Spawn' message when you respawn.")
        .defaultValue(false)
        .build()
    );
    private final Setting<String> onSpawn = sgGeneral.add(new StringSetting.Builder()
        .name("On Spawn")
        .description("What message should be sent on respawn.")
        .defaultValue("/kit Blizzard")
        .build()
    );

    //--------------------Suicide--------------------//
    private final Setting<Boolean> suicide = sgSuicide.add(new BoolSetting.Builder()
        .name("Suicide")
        .description("Enables suicide when running out of items.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> totemAmount = sgSuicide.add(new IntSetting.Builder()
        .name("Totem Amount")
        .description("Suicides if there is under x amount of crystals in inventory.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 16)
        .build()
    );
    private final Setting<Integer> crystalAmount = sgSuicide.add(new IntSetting.Builder()
        .name("Crystal Amount")
        .description("Suicides if there is under x amount of crystals in inventory.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 256)
        .build()
    );
    private final Setting<Integer> gappleAmount = sgSuicide.add(new IntSetting.Builder()
        .name("Gapple Amount")
        .description("Suicides if there is under x amount of crystals in inventory.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 256)
        .build()
    );
    private final Setting<Integer> expAmount = sgSuicide.add(new IntSetting.Builder()
        .name("Exp Amount")
        .description("Suicides if there is under x amount of experience bottles in inventory.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 256)
        .build()
    );
    private final Setting<Integer> obsidianAmount = sgSuicide.add(new IntSetting.Builder()
        .name("Obsidian Amount")
        .description("Suicides if there is under x amount of obsidian in inventory.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 256)
        .build()
    );
    private final Setting<Boolean> eChests = sgSuicide.add(new BoolSetting.Builder()
        .name("Count E-Chests")
        .description("Counts ender chests as 8 obsidian.")
        .defaultValue(true)
        .build()
    );

    //--------------------Rotations--------------------//
    private final Setting<Boolean> rotate = sgRotations.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Stares at target enemy.")
        .defaultValue(true)
        .build()
    );

    //--------------------Eating--------------------//
    private final Setting<Boolean> goldenApple = sgEating.add(new BoolSetting.Builder()
        .name("Golden Apple")
        .description("Eats golden apples when hp is under 'Gapple Health'.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> gappleHealth = sgEating.add(new IntSetting.Builder()
        .name("Gapple Health")
        .description("Check 'Golden Apple' description.")
        .defaultValue(35)
        .min(0)
        .sliderRange(0, 36)
        .build()
    );
    private final Setting<Boolean> chorus = sgEating.add(new BoolSetting.Builder()
        .name("Chorus")
        .description("Eats a chorus fruit when stuck.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> chorusHealth = sgEating.add(new IntSetting.Builder()
        .name("Chorus Health")
        .description("Only eats chorus fruit if above x hp.")
        .defaultValue(14)
        .min(0)
        .sliderRange(0, 36)
        .build()
    );
    private final Setting<Integer> stuckTicks = sgEating.add(new IntSetting.Builder()
        .name("Stuck Ticks")
        .description("Eats a chorus apple after being stuck for x ticks.")
        .defaultValue(100)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );
    private final Setting<Boolean> speedPotion = sgEating.add(new BoolSetting.Builder()
        .name("Speed Potion")
        .description("Drinks a speed potion.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> speedHealth = sgEating.add(new IntSetting.Builder()
        .name("Speed Health")
        .description("Only allows drinking potions when above x hp.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 36)
        .build()
    );

    //--------------------Movement--------------------//
    private final Setting<Boolean> speed = sgMovement.add(new BoolSetting.Builder()
        .name("Speed")
        .description("Uses max speed when moving.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> assumeStep = sgMovement.add(new BoolSetting.Builder()
        .name("Assume Step")
        .description(desc)
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> parkour = sgMovement.add(new BoolSetting.Builder()
        .name("Parkour")
        .description(desc)
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> maxFall = sgMovement.add(new IntSetting.Builder()
        .name("Max Fall")
        .description("How many blocks to fall without water.")
        .defaultValue(150)
        .min(0)
        .sliderRange(0, 500)
        .build()
    );

    public static final String desc = "A setting for baritone. Updated on module activation.";

    private PlayerEntity target = null;
    private boolean inRange = false;

    private int stuckTimer = 0;
    private int eatingSlot = -1;
    private BlockPos lastPos = null;

    private boolean shouldSuicide = false;

    private long lastRespawn = 0;

    @Override
    public void onActivate() {
        settings();
    }

    @Override
    public void onDeactivate() {
        command("stop");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Receive event) {
        if (autoMessage.get() && event.packet instanceof PlayerRespawnS2CPacket) {
            ChatUtils.sendPlayerMsg(onSpawn.get());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        if (speed.get()) {
            double velocity = Math.sqrt(event.movement.x * event.movement.x + event.movement.z * event.movement.z);

            mc.player.setVelocity(0, 0, 0);
            ((IVec3d) event.movement).setXZ(event.movement.x * (0.2873 / velocity), event.movement.z * (0.2873 / velocity));
        }

        if (!inRange) {
            if (lastPos == null) {
                lastPos = mc.player.getBlockPos();
            }

            if (mc.player.getBlockPos().equals(lastPos)) {
                stuckTimer++;
            } else {
                stuckTimer = 0;
            }

            lastPos = mc.player.getBlockPos();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) {return;}

        if (mc.currentScreen instanceof DeathScreen && System.currentTimeMillis() - lastRespawn > 1000) {
            mc.player.requestRespawn();
            lastRespawn = System.currentTimeMillis();
        }

        updateTarget();

        if (target == null) {
            stuckTimer = 0;
            return;
        }

        shouldSuicide = updateSuicide();
        if (shouldSuicide) {
            if (!Modules.get().isActive(Suicide.class)) {
                Modules.get().get(Suicide.class).toggle();
            }
        } else {
            if (Modules.get().isActive(Suicide.class)) {
                Modules.get().get(Suicide.class).toggle();
            }
        }

        eatUpdate();

        if (rotate.get()) {
            Managers.ROTATION.start(
                RotationUtils.getYaw(mc.player.getEyePos(), target.getEyePos()),
                RotationUtils.getPitch(mc.player.getEyePos(), target.getEyePos()),
                priority, RotationType.Other);
        }

        if (shouldSuicide) {return;}

        if (!inRange) {
            command("follow player " + target.getName().getString());
        } else {
            command("stop");
        }

        if (inRange && !Modules.get().isActive(SurroundPlus.class) && surround.get()) {
            Modules.get().get(SurroundPlus.class).toggle();
        }
    }

    private void eatUpdate() {
        Predicate<ItemStack> food = getFood();

        if (food == null) {
            if (eatingSlot > -1) {
                mc.options.useKey.setPressed(false);
            }
            return;
        }

        int slot = InvUtils.findInHotbar(food).slot();

        if (Managers.HOLDING.slot != slot) {
            InvUtils.swap(slot, false);
        }

        if (eatingSlot != slot || (!mc.player.isUsingItem())) {
            eatingSlot = slot;
            mc.options.useKey.setPressed(true);
            Utils.rightClick();
        }
    }

    private Predicate<ItemStack> getFood() {
        if (shouldSuicide) {
            return i -> i.getItem() == Items.CHORUS_FRUIT;
        }

        float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (speedPotion.get() &&
            hp >= speedHealth.get() &&
            available(this::isSpeed) &&
            !mc.player.hasStatusEffect(StatusEffects.SPEED)) {

            return this::isSpeed;
        }
        if (chorus.get() &&
            stuckTimer > stuckTicks.get() &&
            hp >= chorusHealth.get() &&
            available(i -> i.getItem() == Items.CHORUS_FRUIT)) {

            return i -> i.getItem() == Items.CHORUS_FRUIT;
        }
        if (goldenApple.get() &&
            hp <= gappleHealth.get() &&
            available(OLEPOSSUtils::isGapple)) {

            return OLEPOSSUtils::isGapple;
        }
        return null;
    }

    private boolean available(Predicate<ItemStack> predicate) {
        return InvUtils.findInHotbar(predicate).found();
    }

    private boolean isSpeed(ItemStack stack) {
        for (Object instance : PotionUtil.getPotionEffects(stack).stream().toArray()) {
            StatusEffectInstance i = (StatusEffectInstance) instance;

            if (i.getEffectType() == StatusEffects.SPEED) {
                return true;
            }
        }
        return false;
    }

    private boolean inRange() {
        return
            Math.abs(mc.player.getBlockX() - target.getBlockX()) < 4 &&
            Math.abs(mc.player.getBlockZ() - target.getBlockZ()) < 4 &&
            Math.abs(mc.player.getBlockY() - target.getBlockY()) < 4;
    }

    private void command(String command) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
    }

    private boolean updateSuicide() {
        if (!suicide.get()) {return false;}
        if (amountOf(i -> i.getItem() == Items.END_CRYSTAL) <= 0) {return false;}

        if (amountOf(OLEPOSSUtils::isGapple) <= gappleAmount.get()) {return true;}
        if (amountOf(i -> i.getItem() == Items.OBSIDIAN) + (eChests.get() ? amountOf(i -> i.getItem() == Items.ENDER_CHEST) * 8 : 0) <= obsidianAmount.get()) {return true;}
        if (amountOf(i -> i.getItem() == Items.END_CRYSTAL) <= crystalAmount.get()) {return true;}
        if (amountOf(i -> i.getItem() == Items.EXPERIENCE_BOTTLE) <= expAmount.get()) {return true;}
        if (amountOf(i -> i.getItem() == Items.TOTEM_OF_UNDYING) <= totemAmount.get()) {return true;}
        return false;
    }

    private int amountOf(Predicate<ItemStack> predicate) {
        int a = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);

            if (!predicate.test(stack)) {continue;}
            a += stack.getCount();
        }
        return a;
    }

    private void updateTarget() {
        PlayerEntity closest = null;

        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl == mc.player) {continue;}
            if (pl.isSpectator()) {continue;}
            if (Friends.get().isFriend(pl)) {continue;}
            if (pl.getHealth() <= 0) {continue;}

            if (pl.getBlockY() > underY.get()) {continue;}

            if (closest == null || mc.player.distanceTo(closest) > mc.player.distanceTo(pl)) {
                closest = pl;
            }
        }
        target = closest;

        inRange = target != null && inRange();
    }

    private void settings() {
        BaritoneAPI.getSettings().assumeStep.value = assumeStep.get();
        BaritoneAPI.getSettings().allowParkour.value = parkour.get();
        BaritoneAPI.getSettings().allowBreak.value = false;
        BaritoneAPI.getSettings().maxFallHeightNoWater.value = maxFall.get();
        BaritoneAPI.getSettings().allowPlace.value = true;
        BaritoneAPI.getSettings().allowParkourPlace.value = true;


        BaritoneAPI.getSettings().logger.value = text -> {};
    }
}
