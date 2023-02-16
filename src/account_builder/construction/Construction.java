package account_builder.construction;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.*;

import java.awt.*;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Construction")
public class Construction extends Script {
    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
    Skill skillToTrack = Skill.CONSTRUCTION;
    String status = "building";


    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
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
        g.drawString("XP/H: "+ Formatter.formatValue(getExperienceTracker().getGainedXPPerHour(skillToTrack)), 10, 104);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            NPC cook = getNpcs().closest("Cook");
            if (cook==null) {
                if (getInventory().contains("Oak plank") && status.equals("building")) {
                    RS2Object larderSpace = getObjects().closest(15403);
                    if (larderSpace!=null) {
                        larderSpace.interact("Build");
                        sleep(850,950);
                        getKeyboard().typeString("2");
                        sleep(850,950);
                        status = "removing";
                    }
                } if (status.equals("removing")) {
                    RS2Object larder = getObjects().closest(13566);
                    if (larder!=null) {
                        larder.interact("Remove");
                        sleep(850,950);
                        getKeyboard().typeString("1");
                        sleep(850,950);
                        status = "building";
                    }
                }
            } else {
                sleep(850,950);
                RS2Widget chatMessage = getWidgets().getWidgetContainingText("work");
                if (chatMessage != null) {
                    getDialogues().clickContinue();
                    sleep(850,950);
                    getKeyboard().typeString("1");
                    sleep(850,950);
                    cook.interact("Talk-to");
                    sleep(850,950);
                }
                cook.interact("Talk-to");
                sleep(850,950);
                getKeyboard().typeString("1");
                sleep(850,950);
            }

            return 200;

        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
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
