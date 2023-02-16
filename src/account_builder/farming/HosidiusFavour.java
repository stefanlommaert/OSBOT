package account_builder.farming;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.Formatter;
import utils.MouseCursor;
import utils.MouseTrail;

import java.awt.*;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Hosidius compost")
public class HosidiusFavour extends Script {
    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
    private String firstIngredient = "Compost";
    private String secondaryIngredient = "Saltpetre";
    private int farmingXP = 0;


    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            setMouseProfile();
            getExperienceTracker().start(Skill.FARMING);


        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }

    public void onPaint(Graphics2D g){
        TRAIL.paint(g);
        CURSOR.paint(g);
        Font font = new Font("Open Sans", Font.PLAIN, 16);
        g.setFont(font);
        g.setColor(Color.white);
        g.drawString("XP/H: "+ Formatter.formatValue(getExperienceTracker().getGainedXPPerHour(Skill.FARMING)), 10, 104);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (getInventory().contains(firstIngredient)) {
                long start_time = System.currentTimeMillis();
                new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() {
                        return hasGainedXP();
                    }
                }.sleep();
                long end_time = System.currentTimeMillis();
                if (end_time-start_time>2900) {
                    getInventory().getItem(secondaryIngredient).interact("Use");
                    getInventory().getItem(firstIngredient).interact("Use");
                    return random(600,650);
                }
                return 600;
            } else {
                bank.open();
                log("Opened bank");
                if (bank.isOpen()) {
                    bank.withdrawAll(firstIngredient);
                    sleep(400,600);
                    bank.depositAllExcept(firstIngredient);
                    sleep(400,600);
                    bank.withdrawAll(secondaryIngredient);
                    bank.close();
                    return 600;
                }
                return 600;
            }

        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

    }

    private boolean hasGainedXP() {
        if (getExperienceTracker().getGainedXP(Skill.FARMING)!=farmingXP) {
            farmingXP = getExperienceTracker().getGainedXP(Skill.FARMING);
            return true;
        } return false;
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