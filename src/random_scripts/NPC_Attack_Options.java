package random_scripts;

import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.*;
import java.awt.*;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "NPC_Attack_Options")
public class NPC_Attack_Options extends Script {
    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
    private int totalCounter = 0;


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
        CURSOR.paint(g);
        TRAIL.paint(g);
        Font font = new Font("Open Sans", Font.BOLD, 25);
        g.setFont(font);
        g.setColor(Color.red);
        g.drawString("NPC Attack Options", 10, 4+20);
        Font font2 = new Font("Open Sans", Font.PLAIN, 20);
        g.setFont(font2);
        g.setColor(Color.white);
        g.drawString("Made by: Gandhalf", 10, 30+20);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (getConfigs().get(1306)==2) {
                log("NPC: Left-click where available is ENABLED. Closing script now.");
                sleep(2000,2100);
                stop();
            } else if (totalCounter>5){
                log("Could not change NPC Attack options :( Closing script now.");
                sleep(2000,2100);
                stop();
            } else {
                totalCounter++;
                getTabs().open(Tab.SETTINGS);
                new ConditionalSleep(1000) {
                    @Override
                    public boolean condition() {
                        return getTabs().isOpen(Tab.SETTINGS);
                    }
                }.sleep();
                if (getTabs().isOpen(Tab.SETTINGS)) {
                    getWidgets().get(116,106).hover();
                    sleep(200, 220);
                    getMouse().click(false);
                    sleep(200,220);
                    getWidgets().get(116,7,4).hover();
                    sleep(200, 220);
                    getMouse().click(false);
                    if (getWidgets().getWidgetContainingText("Left-click")!=null) {
                        sleep(200,220);
                        getWidgets().get(116,82,3).hover();
                        sleep(200, 220);
                        getMouse().click(false);
                    } else {
                        log("Did not open NPC Attack options menu.");
                        return 600;
                    }
                } else {
                    log("Did not open Settings Tab.");
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


    private void setMouseProfile() {
        MouseMoveProfile profile = new MouseMoveProfile();
        profile.setFlowVariety(MouseMoveProfile.FlowVariety.MEDIUM); //MEDIUM
        profile.setSpeedBaseTime(140); //185
        profile.setFlowSpeedModifier(1); //1.0
        profile.setDeviation(7); //7
        profile.setMinOvershootDistance(25); //25
        profile.setMinOvershootTime(375); //375
        profile.setNoise(2.4); //2.15
        profile.setOvershoots(2); //2
        getBot().setMouseMoveProfile(profile);
    }


}
