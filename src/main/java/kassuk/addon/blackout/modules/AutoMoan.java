package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Random;

/**
 * @author KassuK
 */

public class AutoMoan extends BlackOutModule {

    public AutoMoan() {
        super(BlackOut.BLACKOUT, "Auto Moan", "Moans sexual things to the closest person.");
    }

    //Where the fuck did I go so wrong in life to end up coding fucking AutoMoan

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<MoanMode> moanmode = sgGeneral.add(new EnumSetting.Builder<MoanMode>()
        .name("Message Mode")
        .description("What kind of messages to send.")
        .defaultValue(MoanMode.Submissive)
        .build()
    );

    private final Setting<Boolean> iFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Friends")
        .description("Doesn't send messages targeted to friends.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Tick delay between moans.")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );

    //Most haram module in Blackout

    public enum MoanMode {
        Dominant,
        Submissive,
    }

    private int lastNum;
    private double timer = 0;
    private static final String[] Submissive = new String[]{
        //please fucking end me
        "fuck me harder daddy",
        "deeper! daddy deeper!",
        "Fuck yes your so big!",
        "I love your cock %s!",
        //I want to deepthroat a fucking loaded 12 gauge shotgun and pull the trigger
        "Do not stop fucking my ass before i cum!",
        "Oh your so hard for me",
        "Want to widen my ass up %s?",
        "I love you daddy",
        //what the fuck am I doing with my fucking life holy shit
        "Make my bussy pop",
        //I like how you can see me losing my mind while making this
        "%s loves my bussy so much",
        "i made %s cum so hard with my tight bussy",
        "Your cock is so big and juicy daddy!",
        "Please fuck me as hard as you can",
        "im %s's personal femboy cumdumpster!",
        //The fact that someone has actually said all of these non ironically shows how weird the world is
        "Please shoot your hot load deep inside me daddy!",
        "I love how %s's dick feels inside of me!",
        "%s gets so hard when he sees my ass!",
        "%s really loves fucking my ass really hard!",
        //I just want to stop writing these, but I just feel like it's not finished yet
        "why wont u say the last message",
    };

    private static final String[] Dominant = new String[]{
        //Oh god, why the fuck am I making this
        "Be a good boy for daddy",
        //If heaven is real im not getting there
        "I love pounding your ass %s!",
        "Give your bussy to daddy!",
        //this is surprisingly hard to do I cant think of anything
        "I love how you drip pre-cum while i fuck your ass %s",
        //how do gay people think of shit to say they gotta be like Shakespeare
        "Slurp up and down my cock like a good boy",
        "Come and jump on daddy's cock %s",
        //Why do I even know what bussy is (its boy pussy aka a man's asshole)
        "I love how you look at me while you suck me off %s",
        "%s looks so cute when i fuck him",
        //if nothing else in life works I can at least write gay fanfic to pay for rent
        "%s's bussy is so incredibly tight!",
        //ill add more shit to this everytime I can think of something new
        "%s takes dick like the good boy he is",
        //this one was inspired by a gif of a black man twerking why the fuck does the cheating com find those funny
        "I love how you shake your ass on my dick",
        //the amount of braincells and self-respect I lost while making this is insane
        "%s moans so cutely when i fuck his ass",
        "%s is the best cumdupster there is!",
        //I'm waiting for this to become a regular module in every client
        "%s is always horny and ready for his daddy's dick",
        //Like the fuck am I doing with my life I should be outside with friends not making this
        "My dick gets rock hard every time i see %s",
        "why wont u say the last message",
        //if anyone asks what I did during my vacation I can proudly tell them I wasted multiple hours writing things gay people can say during sex
        //The fact that I have said a few of these frightens me : SigmaClientWasTaken 2023 wtf sigma
    };
    private final Random r = new Random();


    @EventHandler
    private void onRender(Render3DEvent event) {
        timer = Math.min(delay.get(), timer + event.frameTime);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        timer++;
        //I fucking got perm banned on a really important server to test this out you all better fucking enjoy using this
        if (mc.player != null && mc.world != null && timer >= delay.get()) {
            MOAN();
            timer = 0;
        }
    }

    private void MOAN() {
        PlayerEntity target = getClosest();
        if (target == null) {
            return;
        }

        String name = target.getName().getString();
        switch (moanmode.get()) {
            //Skidding AutoEz for this is harder than just making this fully myself but what I start I finish
            case Submissive -> {
                //it took me way too long to understand what this did I have braindamage :(
                int num = r.nextInt(0, Submissive.length - 1);
                if (num == lastNum) {
                    num = num < Submissive.length - 1 ? num + 1 : 0;
                }
                lastNum = num;
                //the way i did the name is so ass bro pls fix this at one point
                //please add the thing that prevents it from saying the same thing twice in a row
                ChatUtils.sendPlayerMsg(Submissive[num].replace("%s", name));
            }
            case Dominant -> {
                int num = r.nextInt(0, Dominant.length - 1);
                if (num == lastNum) {
                    num = num < Dominant.length - 1 ? num + 1 : 0;
                }
                lastNum = num;
                ChatUtils.sendPlayerMsg(Dominant[num].replace("%s", name));
            }
        }
    }

    private PlayerEntity getClosest() {
        assert mc.player != null && mc.world != null;
        PlayerEntity closest = null;
        float distance = -1;
        if (!mc.world.getPlayers().isEmpty()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player && (!iFriends.get() || !Friends.get().isFriend(player))) {
                    if (closest == null || mc.player.getPos().distanceTo(player.getPos()) < distance) {
                        closest = player;
                        distance = (float) mc.player.getPos().distanceTo(player.getPos());
                    }
                }
            }
        }
        return closest;
    }
}
