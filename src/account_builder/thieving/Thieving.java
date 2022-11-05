package account_builder.thieving;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Thieving")
public class Thieving extends Script {
    CustomBreakManager customBreakManager = new CustomBreakManager();
    AntiBotDetection antiBotDetection = new AntiBotDetection(this, "thieving");
    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
    private long nextPause = System.currentTimeMillis()+ (long) random(30, 60) *60*1000;
    private boolean breakingStatus = false;
    int durationUntilNextAntiBan = 60000*8;
    long start_time = System.currentTimeMillis();
    Skill skillToTrack = Skill.THIEVING;
    private int whenToHeal = 20;
    private int whenToOpenPouch = 25;
    private int timeBetweenClick = 185;
    private boolean stopRunning = false;
    private String state = "thieving";
    private final Area THIEVING_AREA = new Area(2675, 3319, 2668, 3315);
    InventoryManagement inventoryManagement = new InventoryManagement(this);


    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            customBreakManager.exchangeContext(getBot());
            getBot().getRandomExecutor().overrideOSBotRandom(customBreakManager);
            getExperienceTracker().start(skillToTrack);
            setMouseProfile();

        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }

    public void onPaint(Graphics2D g){
        CURSOR.paint(g);
        TRAIL.paint(g);
        Font font = new Font("Open Sans", Font.PLAIN, 16);
        g.setFont(font);
        g.setColor(Color.white);
        g.drawString("XP/H: "+ GUI.formatValue(getExperienceTracker().getGainedXPPerHour(skillToTrack)), 10, 104);
        if (breakingStatus) {
            g.setColor(Color.red);
            g.fillOval(200, 200, 50, 50);
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (stopRunning) {
                log("Stopping script now");
                customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(120), true);
            }
            if (getInventory().getSelectedItemName() != null) {
                getInventory().deselectItem();
            }
            if (System.currentTimeMillis() > nextPause) {
                log("Pausing for some minutes");
                breakingStatus = true;
                nextPause = System.currentTimeMillis() + (long) random(30, 60) * 1000 * 60;
                customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(random(5, 15)), true);
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
            if (state.equals("banking")) {
                goBanking();
            } else if (state.equals("thieving")) {
                if (THIEVING_AREA.contains(myPosition())) {
                    return stealFromKnight();
                } else {
                    goToThievingArea();
                }
            }

            return random(600,800);


        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

    }

    private int stealFromKnight() {
        if (!getEquipment().isWearingItem(EquipmentSlot.AMULET, "Dodgy necklace")) {
            log("Not wearing Dodgy necklace");
            if (getInventory().contains("Dodgy necklace")) {
                getInventory().getItem("Dodgy necklace").interact("Wear");
                log("Equipped new Dodgy necklace");
                return 600;
            } else {
                log("Out of Dodgy necklace");
//                stopRunning=true;
                state = "banking";
            }
        } if (getInventory().contains("Coin pouch")) {
            if (getInventory().getItem("Coin pouch").getAmount()>=whenToOpenPouch) {
                log("Opening coin pouches");
                getInventory().getItem("Coin pouch").interact("Open-all");
                whenToOpenPouch = random(20,28);
                return 600;
            }
        } if (getSkills().getDynamic(Skill.HITPOINTS)<whenToHeal) {
            if (getInventory().contains("Half a summer pie")) {
                getInventory().getItem("Half a summer pie").interact("Eat");
                whenToHeal = random(15,30);
            } else if (getInventory().contains("Summer pie")) {
                getInventory().getItem("Summer pie").interact("Eat");
                whenToHeal = random(15,30);
            } else {
                log("Out of food");
//                stopRunning=true;
                state = "banking";
            }
            return 600;
        }
        if (!(myPosition().equals(new Position(2672,3316,0)) || myPosition().equals(new Position(2671,3316,0)))) {
            log("Oops, not on good thieving spot, walking there now");
            webWalkEvent(new Position(2672,3316,0));
            sleep(600,800);
        }
        NPC knight = getNpcs().closest("Knight of Ardougne");
        if (knight != null) {
            interactionEvent(knight, "Pickpocket");
        }

        int randomNumber = random(1,200);
        if (randomNumber==50) {
            if (random(1,10)==5) {
                log("Taking break for 20seconds");
                getMouse().moveOutsideScreen();
                return (random(20000, 30000));
            }
        } else if (randomNumber==51) {
            timeBetweenClick=230;
        } else if (randomNumber<5) {
            timeBetweenClick=185;
        }


        if (random(1,5)==4) {
            if (timeBetweenClick<180) {
                timeBetweenClick = 190;
            }
            if (random(1, 10) < 5) {
                timeBetweenClick += 1;
            } else {
                timeBetweenClick -= 1;
            }
        }

        return random(timeBetweenClick-5,timeBetweenClick+5);
    }

    private void goToThievingArea() {
        getWalking().webWalk(new Position(2668,3315,0));
        sleep(1200,1800);
        webWalkEvent(new Position(2668,3315,0));
        sleep(1200,1800);
//        RS2Object door = getObjects().closest("Door");
//        if (door!=null) {
//            if (Objects.equals(door.getPosition(), new Position(2669,3316,0))) {
//                interactionEvent(door, "Open");
//                log("Opened door");
//            }
//        } else {
//            log("Did not find door to open");
//            stopRunning = true;
//        }
//        sleep(600,800);
        webWalkEvent(new Position(2672,3316,0));
        sleep(1200,1800);
    }

    private void goBanking() throws InterruptedException {
        if (!Banks.ARDOUGNE_SOUTH.contains(myPosition())) {
            getWalking().webWalk(Banks.ARDOUGNE_SOUTH);
        } else {
            bank.open();
            sleep(1800,2400);
            if (bank.isOpen()) {
                bank.depositAllExcept("Coins","Coin pouch");
                sleep(800,1200);
                bank.withdraw("Dodgy necklace",6);
                bank.withdraw("Summer pie",20);
                sleep(800,1200);
                bank.close();
                sleep(600,800);
                state = "thieving";
            }
        }
    }

    private void fixCamera() {

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

    private boolean webWalkEvent(Position position) {
        WebWalkEvent ev = new WebWalkEvent(position);
        ev.setMoveCameraDuringWalking(false);
        ev.setEnergyThreshold(40);
        ev.setMinDistanceThreshold(0);
        execute(ev);
        return ev.hasFinished() && !ev.hasFailed();
    }

    private void setMouseProfile() {
        MouseMoveProfile profile = new MouseMoveProfile();
        profile.setFlowVariety(MouseMoveProfile.FlowVariety.MEDIUM); //MEDIUM
        profile.setSpeedBaseTime(200); //185
        profile.setFlowSpeedModifier(1); //1.0
        profile.setDeviation(7); //7
        profile.setMinOvershootDistance(25); //25
        profile.setMinOvershootTime(375); //375
        profile.setNoise(2.4); //2.15
        profile.setOvershoots(2); //2
        getBot().setMouseMoveProfile(profile);
    }

}

//            if (getCamera().getPitchAngle()<66) {
//                getCamera().toTop();
//            }
//            if (getInventory().isFull()) {
//                getTabs().open(Tab.INVENTORY);
//                inventoryManagement.dropAllItems();
//                return 600;
//            }
//            RS2Object thievingStall = getObjects().closest(28823);
//            if (thievingStall!=null) {
//                if (Objects.equals(thievingStall.getPosition(), new Position(1801,3607,0))) {
//                    interactionEvent(thievingStall, "Steal-from");
//                    log("Stole from fruit stall");
//                    return random(600,1200);
//                }
//            }
//            RS2Object thievingStall = getObjects().closest(11730);
//            if (thievingStall!=null) {
//                if (Objects.equals(thievingStall.getPosition(), new Position(2667, 3310, 0))) {
//                    log("Ready to steal cake");
//                    interactionEvent(thievingStall, "Steal-from");
//                    log("Stole from cake stall");
//                    return random(1200,1800);
//                }
//            }
