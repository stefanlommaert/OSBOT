import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
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
import java.util.concurrent.TimeUnit;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "testingScript")
public class ScriptTemplate extends Script {
    CustomBreakManager customBreakManager = new CustomBreakManager();
    AntiBotDetection antiBotDetection = new AntiBotDetection(this, "mining");
    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
    private long nextPause = System.currentTimeMillis()+ (long) random(30, 60) *60*1000;
    private boolean breakingStatus = false;
    int durationUntilNextAntiBan = 60000*8;
    long start_time = System.currentTimeMillis();
    Skill skillToTrack = Skill.MINING;


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
        g.drawString("XP/H: "+ Formatter.formatValue(getExperienceTracker().getGainedXPPerHour(skillToTrack)), 10, 104);
        if (breakingStatus) {
            g.setColor(Color.red);
            g.fillOval(200, 200, 50, 50);
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (takeBreak()) {
                return 5000;
            } breakingStatus=false;


            doAntiBan();
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

    private boolean takeBreak() {
        if (System.currentTimeMillis() > nextPause) {
            log("Pausing for some minutes");
            breakingStatus = true;
            nextPause = System.currentTimeMillis() + (long) random(35, 65) * 1000 * 60;
            customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(random(5, 8)), true);
            return true;
        } else {
            return false;
        }
    }

    private void doAntiBan() throws InterruptedException {
        if ((System.currentTimeMillis() - start_time) > durationUntilNextAntiBan) {
            log("Executing anti ban measure");
            antiBotDetection.antiBan();
            start_time = System.currentTimeMillis();
            durationUntilNextAntiBan = 60000 *random(7,15);
            log("Time until next anti ban: "+Integer.toString(durationUntilNextAntiBan/60000) +"minutes");

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
