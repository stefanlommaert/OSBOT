package account_builder.woodcutting;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.*;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@ScriptManifest(info = "",logo = "", version = 1, author = "stefan3140", name = "Stefan Woodcutting")
public class WoodcuttingScript extends Script {
    Area bankArea = new Area(3180, 3447, 3185, 3433);
    Area treeArea = new Area(3157, 3418, 3172, 3401);
    private MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
    AntiBotDetection antiBotDetection = new AntiBotDetection(this, "woodcutting");
    InventoryManagement inventoryManagement = new InventoryManagement(this);
    CustomBreakManager customBreakManager = new CustomBreakManager();
    private long startTimeScript;
    Skill skillToTrack = Skill.WOODCUTTING;
    private int totalLogs = 0;
    private int logsPerHour = 0;


    String treeName = "Tree";
    long start_time = System.currentTimeMillis();
    int durationUntilNextAntiBan = 60000*5;
    private long nextPause = System.currentTimeMillis()+ (long) random(10, 15) *60*1000;
    private boolean breakingStatus = false;
    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            log("Woodcutting V2");
            customBreakManager.exchangeContext(getBot());
            getBot().getRandomExecutor().overrideOSBotRandom(customBreakManager);
            getExperienceTracker().start(skillToTrack);
            startTimeScript = System.currentTimeMillis();


            if (getSkills().getStatic(Skill.WOODCUTTING) >= 15) {
                treeName = "Oak";
            }
            if (!treeArea.contains(myPosition())) {
                walkToTree();
            }
        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }

    public void onPaint(Graphics2D g){
        long totalTime = System.currentTimeMillis() - startTimeScript;
        CURSOR.paint(g);
        TRAIL.paint(g);
        Font font = new Font("Open Sans", Font.BOLD, 16);
        g.setFont(font);
        g.setColor(Color.white);
        g.drawString("Time run: "+ Formatter.formatTime(totalTime), 10, 16);
        g.drawString("XP/H: "+ Formatter.formatValue(getExperienceTracker().getGainedXPPerHour(skillToTrack)), 10, 16+16);
        g.drawString("Logs/H: "+ logsPerHour, 10, 16+16*2);

        if (breakingStatus) {
            g.setColor(Color.red);
            g.fillOval(200, 200, 50, 50);
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (System.currentTimeMillis() > nextPause) {
                log("Pausing for some minutes");
                breakingStatus = true;
                int timeToBreak = random(5, 11);
                nextPause = System.currentTimeMillis() + (long) (random(8, 15)+timeToBreak) * 1000 * 60;
                customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(timeToBreak), true);
                return 5000;
            }
            breakingStatus=false;

            if (treeName.equals("Tree") && getSkills().getStatic(Skill.WOODCUTTING) >= 15) {
                treeName = "Oak";
            }
            if ((System.currentTimeMillis() - start_time) > durationUntilNextAntiBan) {
                log("Executing anti ban measure");
                antiBotDetection.antiBan();
                start_time = System.currentTimeMillis();
                durationUntilNextAntiBan = 60000 *random(4,8);
                log("Time until next anti ban: "+Integer.toString(durationUntilNextAntiBan/60000) +"minutes");

            }
            if (getInventory().isFull()) {
//                inventoryManagement.dropAll("Oak logs");
                bankDeposit();
                walkToTree();
            }

            Entity tree = getObjects().closest(treeName);
            if (!myPlayer().isAnimating() && tree != null && interactionEvent(tree, "Chop down")) {
                log("clicked on tree");
                if (random(1,100) <10) {
                    log("Moving mouse out of screen");
                    antiBotDetection.moveMouseOutOfScreen();
                }
                new ConditionalSleep(10000) {
                    @Override
                    public boolean condition() {
                        return !tree.exists();
                    }
                }.sleep();
            }
            if (random(1,100) <10) {
                sleep(random(2000,5000));
            }
            return random(200,500);



        } catch(Exception e) {
            log("error");
            log(e);
            return 30000;
        }

    }

    private boolean interactionEvent(Entity tree, String action) {
        InteractionEvent ev = new InteractionEvent(tree, action);
        ev.setOperateCamera(false);
        ev.setWalkTo(true);
        execute(ev);

        return ev.hasFinished() && !ev.hasFailed();
    }

    private void bankDeposit() throws InterruptedException {
        if (!bankArea.contains(myPosition())) {
            log("Walking to bank");
            getWalking().webWalk(bankArea);
            new ConditionalSleep(1000,200) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
        }

        bank.open();
        log("Bank opened");
        new ConditionalSleep(1000,200) {
            @Override
            public boolean condition() {
                return false;
            }
        }.sleep();

        bank.depositAll("Oak logs","Logs");
        totalLogs+=27;
        long totalTimeInMS = System.currentTimeMillis() - startTimeScript;
        double totalTimeInHours = ((double) totalTimeInMS)/(1000*60*60);
        logsPerHour = (int) (totalLogs /totalTimeInHours);
        sleep(random(200,500));
        bank.close();
        sleep(random(200,2000));
    }

    private void walkToTree() {
        log("Walking to area.");
        if (!getWalking().webWalk(treeArea.getRandomPosition())) {
            getWalking().webWalk(treeArea);
        }
    }



}