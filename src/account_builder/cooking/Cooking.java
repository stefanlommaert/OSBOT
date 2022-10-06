package account_builder.cooking;

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
import utils.MouseCursor;
import utils.MouseTrail;

import java.awt.*;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Cooking")
public class Cooking extends Script {
    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.red, this);

    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            setMouseProfile();

        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }

    public void onPaint(Graphics2D g){
        trail.paint(g);
        cursor.paint(g);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (getSkills().getStatic(Skill.COOKING) >= 53 || getSkills().getStatic(Skill.COOKING) < 35) {
                log("Finished firemaking, stopping now. Or your level is not above 35.");
                stop();
            }
            if (!getInventory().contains("Jug of water")) {
                goBanking();
            }
            long start_time = System.currentTimeMillis();
            new ConditionalSleep(1000) {
                @Override
                public boolean condition() {
                    return myPlayer().isAnimating();
                }
            }.sleep();
            long end_time = System.currentTimeMillis();
            if (end_time-start_time>900) {
                if (!myPlayer().isAnimating()) {
                    if (getInventory().contains("Jug of water")) {
                        getInventory().getItem("Jug of water").interact("Use");
                        sleep(100,300);
                        getInventory().getItem("Grapes").interact("Use");
                        sleep(600,800);
                        getKeyboard().typeString(" ", false);

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

    private void goBanking() throws InterruptedException {
        bank.open();
        log("Opened bank");
        if (bank.isOpen()) {
            bank.withdrawAll("Grapes");
            bank.depositAll("Unfermented wine");
            bank.depositAll("Jug of bad wine");
            bank.depositAll("Jug of wine");
            bank.withdrawAll("Jug of water");
            sleep(400,600);
            bank.close();

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