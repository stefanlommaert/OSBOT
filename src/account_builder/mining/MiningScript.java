package account_builder.mining;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
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

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Mining")
public class MiningScript extends Script {
    CustomBreakManager customBreakManager = new CustomBreakManager();
    AntiBotDetection antiBotDetection = new AntiBotDetection(this, "mining");
    InventoryManagement inventoryManagement = new InventoryManagement(this);

    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.red, this);
    Area varrockArea = new Area(3140, 3513, 3187, 3468);
    Area doricsArea = new Area(2943, 3458, 2961, 3438);
    Area miningArea = new Area(3281, 3374, 3288, 3372);
    private long nextPause = System.currentTimeMillis()+ (long) random(30, 60) *60*1000;
    private boolean breakingStatus = false;
    int durationUntilNextAntiBan = 60000*8;
    long start_time = System.currentTimeMillis();
    public boolean setupDone = false;


    int rockToMine = 0;
    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            log("Mining V3");
            customBreakManager.exchangeContext(getBot());
            getBot().getRandomExecutor().overrideOSBotRandom(customBreakManager);
            getExperienceTracker().start(Skill.MINING);
            setMouseProfile();
            sleep(100,200);
            getMouse().move(random(80,120), random(80,120));
            sleep(1000,2000);
            if (getMouse().isOnScreen()) {
                log("Mouse is on screen");
                log("Scrolling mouse");
                for (int i = 0; i < 35; i++) {
                    getMouse().scrollDown();
                    sleep(30,35);
                    if (i%9==0) {
                        sleep(200,250);
                    }
                }
                sleep(300,500);
                for (int i = 0; i < 3; i++) {
                    getMouse().scrollUp();
                    sleep(25,35);
                }

            } else {
                log("Mouse is not on screen");
            }
            getCamera().toTop();
            if (doricsArea.contains(myPosition())) {
                walkToArea(varrockArea);
            }
            sleep(3000,3600);

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
        g.drawString("XP/H: "+ Formatter.formatValue(getExperienceTracker().getGainedXPPerHour(Skill.MINING)), 10, 104);
        if (breakingStatus) {
            g.setColor(Color.red);
            g.fillOval(200, 200, 50, 50);
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (!setupDone && varrockArea.contains(myPlayer().getPosition())) {
                log("setting up inventory");
                setupInventory();
            }
            if (getSkills().getStatic(Skill.MINING)>=41) {
                log("Reached 41 mining");
                stop();
            }


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

            if (getSkills().getStatic(Skill.MINING)==11 && getInventory().contains("Black pickaxe") && getInventory().contains("Steel pickaxe")) {
                getInventory().getItem("Steel pickaxe").interact("Drop");
            } else if (getSkills().getStatic(Skill.MINING)==21 && getInventory().contains("Mithril pickaxe") && getInventory().contains("Black pickaxe")) {
                getInventory().getItem("Black pickaxe").interact("Drop");
            } else if (getSkills().getStatic(Skill.MINING)==31 && getInventory().contains("Adamant pickaxe") && getInventory().contains("Mithril pickaxe")) {
                getInventory().getItem("Mithril pickaxe").interact("Drop");
            }
            if (getInventory().isFull()) {
                if (getInventory().contains("Copper ore")) {
                    inventoryManagement.dropAll("Copper ore");
                } else if (getInventory().contains("Iron ore")) {
                    inventoryManagement.dropAll("Iron ore");
                }
            }
            else {
                if (!myPlayer().isAnimating()) {
//                    powerMine();
                    mineOre();
                }

            }
            return random(210,260);

        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

    }

    public void setupInventory() throws InterruptedException {
        Area GEArea = new Area(3162, 3486, 3167, 3485);
        getWalking().walk(GEArea.getRandomPosition());
        sleep(1000,1500);
        bank.open();
        new ConditionalSleep(8000) {
            @Override
            public boolean condition() {
                return getBank().isOpen();
            }
        }.sleep();

        if (bank.isOpen()) {
            log("Opened bank");
            bank.depositAll();
            sleep(1200,1800);
            getBank().withdraw("Steel pickaxe",1);
            sleep(1200,1800);
            getBank().withdraw("Black pickaxe",1);
            sleep(1200,1800);
            getBank().withdraw("Mithril pickaxe",1);
            sleep(1200,1800);
            getBank().withdraw("Adamant pickaxe",1);
            sleep(1200,1800);
            bank.close();
            setupDone = true;
            sleep(1200,1800);

            if (!miningArea.contains(myPosition())) {
                walkToArea(miningArea);
            }
        } else {
            log("Did not open bank");
            stop();
        }
    }

    public void mineOre() {

        if(getSkills().getStatic(Skill.MINING)>=15) {
            Filter<RS2Object> myFilter = new Filter<RS2Object>() {
                public boolean match(RS2Object obj) {
                    return ((obj.getId()==11364 && obj.getPosition().equals(new Position(3285, 3369, 0))) || (obj.getId()==11365 &&obj.getPosition().equals(new Position(3288, 3370, 0))));
//                    return ((obj.getId()==11364 && obj.getPosition().equals(new Position(3285, 3369, 0))) || (obj.getId()==11365 &&obj.getPosition().equals(new Position(3288, 3370, 0)))|| (obj.getId()==11365 &&obj.getPosition().equals(new Position(3286, 3369, 0))));
                }
            };
            RS2Object ore = getObjects().closest(myFilter);
            if (ore != null) {
                interactionEvent(ore, "Mine");
                new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() {
                        return !ore.exists();
                    }
                }.sleep();
            }
        } else {
            Filter<RS2Object> myFilter = new Filter<RS2Object>() {
                public boolean match(RS2Object obj) {
                    return (obj.getId()==10943 && (obj.getPosition().equals(new Position(3282, 3369, 0))));
                }
            };
            RS2Object ore = getObjects().closest(myFilter);
            if (ore != null) {
                interactionEvent(ore, "Mine");
                new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() {
                        return !ore.exists();
                    }
                }.sleep();
            }
        }
    }

    private int getRockID(int rock) {
        if (rockToMine == 0) {
            return 11364;
        } else if (rockToMine==1) {
            return 11364;
        } else {
            return 11365;
        }

    }
    private Position getRockPosition(int rockID) {
        if (rockToMine == 0) {
            return new Position(3294,3310,0);
        } else if (rockToMine==1) {
            return new Position(3295,3311,0);
        } else {
            return new Position(3295,3309,0);
        }
    }
    public void powerMine() {
        int rockID = getRockID(rockToMine);
        Position rockPosition = getRockPosition(rockToMine);
        Filter<RS2Object> myFilter = new Filter<RS2Object>() {
            public boolean match(RS2Object obj) {
                return (obj.getId()==rockID && obj.getPosition().equals(rockPosition));
            }
        };
        RS2Object ore = getObjects().closest(myFilter);
        if (ore != null) {
            interactionEvent(ore, "Mine");
            if (rockToMine<2) {
                rockToMine++;
            } else {
                rockToMine=0;
            }
            if (rockToMine==0) {
                getInventory().hover(0);
                new ConditionalSleep(2400, 10) {
                    @Override
                    public boolean condition() {
                        return !ore.exists();
                    }
                }.sleep();
                log("Dropping items now");
                getInventory().dropAll();
//                inventoryManagement.dropAll("Iron ore");
                log("Done dropping items");
            } else {
                int newRockID = getRockID(rockToMine);
                Position newRockPosition = getRockPosition(rockToMine);
                Filter<RS2Object> myFilter2 = new Filter<RS2Object>() {
                    public boolean match(RS2Object obj) {
                        return ((obj.getId() == newRockID && obj.getPosition().equals(newRockPosition)) || (obj.getId() == (newRockID + 26) && obj.getPosition().equals(newRockPosition)));
                    }
                };
                RS2Object nextOre = getObjects().closest(myFilter2);
                nextOre.hover();
                new ConditionalSleep(2400, 10) {
                    @Override
                    public boolean condition() {
                        return !ore.exists();
                    }
                }.sleep();
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

    private void walkToArea(Area area) {
        log("Walking to area.");
        webWalkEvent(area);
    }

    private void setMouseProfile() {
        MouseMoveProfile profile = new MouseMoveProfile();
        profile.setFlowVariety(MouseMoveProfile.FlowVariety.MEDIUM); //MEDIUM
        profile.setSpeedBaseTime(200); //185
        profile.setFlowSpeedModifier(1); //1.0
        profile.setDeviation(6); //7
        profile.setMinOvershootDistance(40); //25
        profile.setMinOvershootTime(300); //375
        profile.setNoise(2.3); //2.15
        profile.setOvershoots(1); //2
        getBot().setMouseMoveProfile(profile);
    }




}
