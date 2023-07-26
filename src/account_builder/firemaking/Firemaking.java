package account_builder.firemaking;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.Formatter;
import utils.MouseCursor;
import utils.MouseTrail;

import java.awt.*;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Firemaking")
public class Firemaking extends Script {

    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.red, this);
    private boolean positionToStartFire = false;

    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            getExperienceTracker().start(Skill.FIREMAKING);

            setMouseProfile();

        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }

    public void onPaint(Graphics2D g){
        g.drawString("XP/H: "+ Formatter.formatValue(getExperienceTracker().getGainedXPPerHour(Skill.FIREMAKING)), 10, 104);
        trail.paint(g);
        cursor.paint(g);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (getSkills().getStatic(Skill.FIREMAKING) >= 75) {
                log("Finished firemaking, stopping now");
                stop();
            }
            long start_time = System.currentTimeMillis();
            new ConditionalSleep(300) {
                @Override
                public boolean condition() {
                    return myPlayer().isAnimating();
                }
            }.sleep();
            long end_time = System.currentTimeMillis();
            if (end_time-start_time>200) {
                if (!myPlayer().isAnimating()) {
                    if (getSkills().getStatic(Skill.FIREMAKING) < 15) {
                        lightLog("Logs");
                    } else if (getSkills().getStatic(Skill.FIREMAKING) < 30) {
                        lightLog("Oak logs");
                    } else if (getSkills().getStatic(Skill.FIREMAKING) < 45){
                        lightLog("Willow logs");
                    } else {
                        lightLog("Maple logs");
                    }
                    return random(200,400);
                }
            }
            return 600;

        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

    }

    private void lightLog(String log) throws InterruptedException {
        if (getInventory().contains(log)) {
            getInventory().getItem("Tinderbox").interact("Use");
            sleep(200,300);
            getInventory().getItem(log).interact("Use");
            sleep(200,500);
            getInventory().getItem("Tinderbox").hover();
        } else {
            goToBank(log);
        }
    }

    private void goToBank(String log) throws InterruptedException {
        Area GEArea = new Area(3161, 3488, 3159, 3491);
        getWalking().walk(GEArea.getRandomPosition());
        bank.open();
        log("Opened bank");
        if (bank.isOpen()) {
            bank.depositAllExcept("Tinderbox");
            sleep(800,1000);
            if (!getInventory().isFull()) {
                bank.withdrawAll(log);
                sleep(500,700);
                bank.close();
                sleep(300,400);
                if (positionToStartFire) {
                    walkingEvent(new Position(3179, 3506, 0));
//                    getWalking().walk(new Position(3179, 3506, 0));
                    positionToStartFire = false;
                } else {
                    walkingEvent(new Position(3179, 3503, 0));
                    positionToStartFire = true;

                }
            }
        }
    }

    private boolean interactionEvent(Entity entity, String action) {
        InteractionEvent ev = new InteractionEvent(entity, action);
        ev.setOperateCamera(false);
        ev.setWalkTo(true);
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

    private boolean walkingEvent(Position pos) {
        WalkingEvent ev = new WalkingEvent(pos);
        ev.setEnergyThreshold(40);
        ev.setMinDistanceThreshold(1);
        execute(ev);
        return ev.hasFinished() && !ev.hasFailed();
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
        profile.setSpeedBaseTime(185); //185
        profile.setFlowSpeedModifier(1); //1.0
        profile.setDeviation(7); //7
        profile.setMinOvershootDistance(25); //25
        profile.setMinOvershootTime(375); //375
        profile.setNoise(2.15); //2.15
        profile.setOvershoots(2); //2
//        getBot().setMouseMoveProfile(profile);
    }

    private void walkToArea(Area area) {
        log("Walking to area.");
        webWalkEvent(area);
    }




}