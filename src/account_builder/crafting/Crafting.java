package account_builder.crafting;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.*;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Crafting")
public class Crafting extends Script {
    private long startTimeScript;


    private final String GEM_TO_CUT = "Uncut emerald"; //sapphire

    CustomBreakManager customBreakManager = new CustomBreakManager();
    AntiBotDetection antiBotDetection = new AntiBotDetection(this, "thieving");
    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
    private long nextPause = System.currentTimeMillis()+ (long) random(15, 20) *60*1000;
    private boolean breakingStatus = false;
    int durationUntilNextAntiBan = 60000*8;
    long start_time = System.currentTimeMillis();
    Skill skillToTrack = Skill.CRAFTING;

    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            customBreakManager.exchangeContext(getBot());
            getBot().getRandomExecutor().overrideOSBotRandom(customBreakManager);
            getExperienceTracker().start(skillToTrack);
            startTimeScript = System.currentTimeMillis();

            setMouseProfile();

        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }

    public void onPaint(Graphics2D g){
        long totalTime = System.currentTimeMillis() - startTimeScript;
        CURSOR.paint(g);
        TRAIL.paint(g);
        Font font = new Font("Open Sans", Font.PLAIN, 16);
        g.setFont(font);
        g.setColor(Color.white);
        g.drawString("Time run: "+ Formatter.formatTime(totalTime), 10, 104);
        g.drawString("XP/H: "+ Formatter.formatValue(getExperienceTracker().getGainedXPPerHour(skillToTrack)), 10, 104+16);
        if (breakingStatus) {
            g.setColor(Color.red);
            g.fillOval(200, 200, 50, 50);
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (System.currentTimeMillis() > nextPause) {
                log("Pausing for some minutes");
                breakingStatus = true;
                nextPause = System.currentTimeMillis() + (long) random(15, 20) * 1000 * 60;
                customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(random(5, 7)), true);
                return 5000;
            }
            breakingStatus=false;
            if ((System.currentTimeMillis() - start_time) > durationUntilNextAntiBan) {
                log("Executing anti ban measure");
                antiBotDetection.antiBan();
                start_time = System.currentTimeMillis();
                durationUntilNextAntiBan = 60000 *random(7,15);
                log("Time until next anti ban: "+Integer.toString(durationUntilNextAntiBan/60000) +"minutes");
            }

            long start_time = System.currentTimeMillis();
            new ConditionalSleep(1200) {
                @Override
                public boolean condition() {
                    return (myPlayer().isAnimating() || getDialogues().isPendingContinuation());
                }
            }.sleep();
            long end_time = System.currentTimeMillis();
            if (end_time-start_time>1200 || getDialogues().isPendingContinuation() || !getInventory().contains(GEM_TO_CUT)) {
//                if (!myPlayer().isAnimating() || !getInventory().contains(GEM_TO_CUT)) {   //TODO: This will give error when crafting gloves, so fix this "!getInventory().contains(GEM_TO_CUT)" will always return true
                    if (getSkills().getStatic(Skill.CRAFTING) < 20) {
                        if (!myPlayer().isAnimating()) {
                            craftLeatherGloves();
                            return random(150, 1000);
                        }
                    } else {
                            if (!myPlayer().isAnimating() || !getInventory().contains(GEM_TO_CUT)) {
                                cutSapphires();
                                return random(150, 1000);
                            }

                    }
            }
//            }
            return random(600,1200);

        } catch(Exception e) {
            log("ERROR");
            log(e);
            stop();
            return 30000;
        }

    }

    private void craftLeatherGloves() throws InterruptedException {
        if (getInventory().contains("Leather")) {
            getInventory().getItem("Leather").interact("Use");
            sleep(200,300);
            getInventory().getItem("Needle").interact("Use");
            sleep(600,700);
            if (getSkills().getStatic(Skill.CRAFTING) >= 14) {
                getKeyboard().typeString("5", false);

            } else {
                getKeyboard().typeString("1", false);
            }
        } else {
            bank.open();
            log("Opened bank");
            if (bank.isOpen()) {
                bank.depositAllExcept("Needle", "Thread","Chisel");
                sleep(800,1000);
                if (!getInventory().isFull()) {
                    bank.withdrawAll("Leather");
                    sleep(500,700);
                    bank.close();
                    sleep(300,400);
                }
            }

        }
    }

    private void cutSapphires() throws InterruptedException {
        if (!getInventory().contains(GEM_TO_CUT)) {
            if (random(1,100)<30) {
                sleep(3000,10000);
            }
            bank.open();
            log("Opened bank");
            if (bank.isOpen()) {
                bank.depositAllExcept("Chisel");
                new ConditionalSleep(600) {
                    @Override
                    public boolean condition() {
                        return !getInventory().isFull();
                    }
                }.sleep();
                if (!getInventory().isFull()) {
                    if (bank.contains(GEM_TO_CUT)) {
                        bank.withdrawAll(GEM_TO_CUT);
                    } else {
                        stop();
                    }
                    new ConditionalSleep(600) {
                        @Override
                        public boolean condition() {
                            return getInventory().isFull();
                        }
                    }.sleep();
                    bank.close();
                }
            }
        }
        if (getInventory().contains(GEM_TO_CUT)) {
            getInventory().getItem(GEM_TO_CUT).interact("Use");
            getInventory().getItem("Chisel").interact("Use");
            new ConditionalSleep(1000) {
                @Override
                public boolean condition() {
                    return getDialogues().inDialogue();
                }
            }.sleep();
            if (getDialogues().inDialogue() && !getDialogues().isPendingContinuation()) {
                sleep(500,800);
                getKeyboard().typeString(" ", false);
                sleep(500,1000);
                int rand = random(1,100);
                if (rand<30) {
                    RS2Object bank = getObjects().closest(10060); //Grand exchange bank booth
                    if (bank != null) {
                        bank.hover();
                    }
                } else if (rand < 80) {
                    getMouse().moveOutsideScreen();
                } else {
                    sleep(500,1000);
                }
                sleep(600,800);
            }
        }
    }

    private void sleep(int time1, int time2) {
        new ConditionalSleep(random(time1, time2)) {
            @Override
            public boolean condition() {
                return false;
            }
        }.sleep();
    }

    private void setMouseProfile() {
        MouseMoveProfile profile = new MouseMoveProfile();
        profile.setFlowVariety(MouseMoveProfile.FlowVariety.MEDIUM); //MEDIUM
        profile.setSpeedBaseTime(301); //185
        profile.setFlowSpeedModifier(1.1); //1.0
        profile.setDeviation(6); //7
        profile.setMinOvershootDistance(25); //25
        profile.setMinOvershootTime(375); //375
        profile.setNoise(2.10); //2.15
        profile.setOvershoots(1); //2
        getBot().setMouseMoveProfile(profile);
    }
}