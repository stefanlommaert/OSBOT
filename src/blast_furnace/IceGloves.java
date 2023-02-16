package blast_furnace;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
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

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Ice Gloves")
public class IceGloves extends Script {

    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);

    Area varrockArea = new Area(3140, 3513, 3187, 3468);


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
        Font font = new Font("Open Sans", Font.PLAIN, 16);
        g.setFont(font);
        g.setColor(Color.white);

    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (varrockArea.contains(myPosition())) {
                bank.open();
                new ConditionalSleep(10000) {
                    @Override
                    public boolean condition() {
                        return bank.isOpen();
                    }
                }.sleep();
                if (bank.isOpen()) {
                    bank.depositAll();
                    sleep(1200,1800);
                    getBank().withdrawAll("Fire rune");
                    sleep(1200,1800);
                    getBank().withdrawAll("Chaos rune");
                    sleep(1200,1800);
                    getBank().withdrawAll("Amulet of magic");
                    sleep(1200,1800);
                    getBank().withdrawAll("Blue wizard hat");
                    sleep(1200,1800);
                    getBank().withdrawAll("Blue wizard robe");
                    sleep(1200,1800);
                    getBank().withdrawAll("Staff of air");
                    sleep(1200,1800);
                    getBank().withdraw("Chocolate cake",5);
                    sleep(1200,1800);
                    getBank().withdrawAll("Shark");
                    sleep(1200,1800);
                    bank.close();
                    sleep(1200,1800);
                    getInventory().getItem("Amulet of magic").interact("Wear");
                    getInventory().getItem("Blue wizard hat").interact("Wear");
                    getInventory().getItem("Blue wizard robe").interact("Wear");
                    getInventory().getItem("Staff of air").interact("Wield");
                    sleep(1200,1800);
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
