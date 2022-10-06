package account_builder.woodcutting;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.AntiBotDetection;
import utils.InventoryManagement;
import utils.MouseCursor;
import utils.MouseTrail;

import java.awt.*;
import java.util.ArrayList;

@ScriptManifest(info = "",logo = "", version = 1, author = "stefan3140", name = "Stefan Woodcutting")
public class WoodcuttingScript extends Script {
    Area bankArea = new Area(3180, 3447, 3185, 3433);
    Area treeArea = new Area(3157, 3418, 3172, 3401);
    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.red, this);
    AntiBotDetection antiBotDetection = new AntiBotDetection(this, "woodcutting");
    InventoryManagement inventoryManagement = new InventoryManagement(this);

    String treeName = "Tree";
    long start_time = System.currentTimeMillis();
    int durationUntilNextAntiBan = 60000*8;
    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            log("Woodcutting V2");
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
        trail.paint(g);
        cursor.paint(g);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (treeName.equals("Tree") && getSkills().getStatic(Skill.WOODCUTTING) >= 15) {
                treeName = "Oak";
            }
            if ((System.currentTimeMillis() - start_time) > durationUntilNextAntiBan) {
                log("Executing anti ban measure");
                antiBotDetection.antiBan();
                start_time = System.currentTimeMillis();
                durationUntilNextAntiBan = 60000 *random(7,15);
                log("Time until next anti ban: "+Integer.toString(durationUntilNextAntiBan/60000) +"minutes");

            }
            if (getInventory().isFull()) {
                inventoryManagement.dropAll("Oak logs");
//                bankDeposit();
//                walkToTree();
            }

            Entity tree = getObjects().closest(treeName);
            if (!myPlayer().isAnimating() && tree != null && interactionEvent(tree, "Chop down")) {
                log("clicked on tree");
                if (random(1,100) <30) {
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

        bank.depositAll();
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