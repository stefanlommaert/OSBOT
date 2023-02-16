package account_builder.magic;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.*;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Enchanting")
public class NecklaceEnchanter extends Script {
    CustomBreakManager customBreakManager = new CustomBreakManager();
    AntiBotDetection antiBotDetection = new AntiBotDetection(this, "mining");
    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.red, this);

    private long nextPause = System.currentTimeMillis()+ (long) random(30, 60) *60*1000;
    private boolean breakingStatus = false;
    int durationUntilNextAntiBan = 60000*8;
    long start_time = System.currentTimeMillis();
    private boolean openedMagicTab = false;


    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            log("Enchanter V3");
            customBreakManager.exchangeContext(getBot());
            getBot().getRandomExecutor().overrideOSBotRandom(customBreakManager);
            getExperienceTracker().start(Skill.MAGIC);
            setMouseProfile();
        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }

    public void onPaint(Graphics2D g){
        trail.paint(g);
        cursor.paint(g);
        Font font = new Font("Open Sans", Font.PLAIN, 16);
        g.setFont(font);
        g.setColor(Color.white);
        g.drawString("XP/H: "+ Formatter.formatValue(getExperienceTracker().getGainedXPPerHour(Skill.MAGIC)), 10, 104);
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
                nextPause = System.currentTimeMillis() + (long) random(35, 65) * 1000 * 60;
                customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(random(5, 8)), true);
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
            if (!openedMagicTab) {
                getTabs().open(Tab.MAGIC);
                new ConditionalSleep(1000) {
                    @Override
                    public boolean condition() {
                        return getTabs().isOpen(Tab.MAGIC);
                    }
                }.sleep();
                if (getTabs().isOpen(Tab.MAGIC)) {
                    openedMagicTab = true;
                }

            }
            if (!getInventory().contains("Diamond amulet")) {
                goBanking();
            } else {
                enchantAmulet();
            }
            return random(30,70);

        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

    }

    private void enchantAmulet() {
        if (getTabs().isOpen(Tab.MAGIC)) {
            RS2Widget enchantSpell = getWidgets().get(218,42);
            enchantSpell.interact("Cast");
            new ConditionalSleep(1200) {
                @Override
                public boolean condition() {
                    return getTabs().isOpen(Tab.INVENTORY);
                }
            }.sleep();
            if (getTabs().isOpen(Tab.INVENTORY)) {
                getInventory().getItem("Diamond amulet").interact("Cast");
            }
        } else {
            new ConditionalSleep(1800) {
                @Override
                public boolean condition() {
                    return getTabs().isOpen(Tab.MAGIC);
                }
            }.sleep();
            if (!getTabs().isOpen(Tab.MAGIC)) {
                getTabs().open(Tab.MAGIC);
            }
        }
    }

    private void goBanking() throws InterruptedException {
         if (!bank.open()) {
             getMouse().click(false);
         }
        log("Opened bank");
        if (bank.isOpen()) {
            openedMagicTab = false;
            bank.depositAllExcept("Cosmic rune");
            sleep(800,1000);
            if (!getInventory().isFull()) {
                if (bank.contains("Diamond amulet")) {
                    bank.withdrawAll("Diamond amulet");
                    sleep(500, 700);
                    bank.close();
                    sleep(300, 400);
                } else {
                    log("Out of amulets, please restock");
                    stop();
                }
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

    private boolean interactionEvent(Entity entity, String action) {
        InteractionEvent ev = new InteractionEvent(entity, action);
        ev.setOperateCamera(false);
        ev.setWalkTo(false);
        execute(ev);
        return ev.hasFinished() && !ev.hasFailed();
    }

    private boolean webWalkEvent(Area area) {
        WebWalkEvent ev = new WebWalkEvent(area);
        ev.setMoveCameraDuringWalking(false);
        ev.setEnergyThreshold(40);
        execute(ev);
        return ev.hasFinished() && !ev.hasFailed();
    }

    private void setMouseProfile() {
        MouseMoveProfile profile = new MouseMoveProfile();
        profile.setFlowVariety(MouseMoveProfile.FlowVariety.MEDIUM); //MEDIUM
        profile.setSpeedBaseTime(250); //185
        profile.setFlowSpeedModifier(1); //1.0
        profile.setDeviation(6); //7
        profile.setMinOvershootDistance(40); //25
        profile.setMinOvershootTime(300); //375
        profile.setNoise(2.3); //2.15
        profile.setOvershoots(1); //2
        getBot().setMouseMoveProfile(profile);
    }




}
