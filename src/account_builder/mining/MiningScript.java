package account_builder.mining;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.RS2Object;
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
import java.util.Objects;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Mining")
public class MiningScript extends Script {

    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.red, this);

    Area miningArea = new Area(3281, 3374, 3288, 3372);
    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            log("Mining V3");
            if (!miningArea.contains(myPosition())) {
                walkToArea(miningArea);
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
            if (getSkills().getStatic(Skill.MINING)==11 && getInventory().contains("Black pickaxe")) {
                getInventory().getItem("Black pickaxe").interact("Wield");
            } else if (getSkills().getStatic(Skill.MINING)==21 && getInventory().contains("Mithril pickaxe")) {
                getInventory().getItem("Mithril pickaxe").interact("Wield");
            }
            if (getInventory().isFull()) {
                inventory.dropAllExcept("Mithril pickaxe","Black pickaxe");
            }
            else {
                if (!myPlayer().isAnimating()) {
                    mineOre();
                }

            }
            return random(200,400);

        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

    }

    public void mineOre() {

        if(getSkills().getStatic(Skill.MINING)>=15) {
            Filter<RS2Object> myFilter = new Filter<RS2Object>() {
                public boolean match(RS2Object obj) {
                    return ((obj.getId()==11364 && obj.getPosition().equals(new Position(3285, 3369, 0))) || (obj.getId()==11365 &&obj.getPosition().equals(new Position(3288, 3370, 0))));
                }
            };
            RS2Object ore = getObjects().closest(myFilter);
            if (ore != null) {
                interactionEvent(ore, "Mine");
                new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() {
                        return !ore.exists();
                    }
                }.sleep();
            }
        } else {
            Filter<RS2Object> myFilter = new Filter<RS2Object>() {
                public boolean match(RS2Object obj) {
                    return (obj.getId()==10943 && (obj.getPosition().equals(new Position(3282, 3369, 0))));
                }
            };
            RS2Object ore = getObjects().closest(myFilter);
            if (ore != null) {
                interactionEvent(ore, "Mine");
                new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() {
                        return !ore.exists();
                    }
                }.sleep();
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

    private void walkToArea(Area area) {
        log("Walking to area.");
        webWalkEvent(area);
    }




}
