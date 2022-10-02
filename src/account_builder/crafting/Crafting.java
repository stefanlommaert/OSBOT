package account_builder.crafting;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.MouseCursor;
import utils.MouseTrail;

import java.awt.*;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Crafting")
public class Crafting extends Script {
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
            if (getSkills().getStatic(Skill.CRAFTING) >= 50) {
                log("Finished crafting, stopping now");
                stop();
            }
            long start_time = System.currentTimeMillis();
            new ConditionalSleep(2000) {
                @Override
                public boolean condition() {
                    return myPlayer().isAnimating();
                }
            }.sleep();
            long end_time = System.currentTimeMillis();
            if (end_time-start_time>1800) {
                if (!myPlayer().isAnimating()) {
                    if (getSkills().getStatic(Skill.CRAFTING) < 20) {
                        craftLeatherGloves();
                    } else {
                        cutSapphires();
                    }
                    return 600;
                }
            }
            return 600;

        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

    }

    private void craftLeatherGloves() throws InterruptedException {
        if (getInventory().contains("Leather")) {
            getInventory().getItem("Leather").interact("Use");
            sleep(200,300);
            getInventory().getItem("Needle").interact("Use");
            sleep(600,700);
            getKeyboard().typeString(" ", false);
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
        if (getInventory().contains("Uncut sapphire")) {
            getInventory().getItem("Uncut sapphire").interact("Use");
            sleep(200,300);
            getInventory().getItem("Chisel").interact("Use");
            sleep(600,700);
            getKeyboard().typeString(" ", false);
        } else {
            bank.open();
            log("Opened bank");
            if (bank.isOpen()) {
                bank.depositAllExcept("Chisel");
                sleep(800,1000);
                if (!getInventory().isFull()) {
                    bank.withdrawAll("Uncut sapphire");
                    sleep(500,700);
                    bank.close();
                    sleep(300,400);
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