package account_builder.fishing;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.AntiBotDetection;
import utils.InventoryManagement;
import utils.MouseCursor;
import utils.MouseTrail;

import java.awt.*;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Fishing")
public class Fishing extends Script {
    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.red, this);
    Area shrimpArea = new Area(3233, 3161, 3249, 3142);
    Area troutArea = new Area(3099, 3436, 3112, 3423);
    String state = "fishing";
    InventoryManagement inventoryManagement = new InventoryManagement(this);
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
//Fire id: 43475
    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (getSkills().getStatic(Skill.FISHING) >= 53) {
                log("Finished fishing, stopping now");
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
                    if (getSkills().getStatic(Skill.FISHING) < 20) {
                        if (shrimpArea.contains(myPosition())) {
                            if (getInventory().isFull()) {
//                                inventoryManagement.dropAll("");
                                getInventory().dropAllExcept("Small fishing net");
                                sleep(200,400);
                            }
                            NPC fishingSpot = getNpcs().closest("Fishing spot");
                            if (fishingSpot!=null) {
                                fishingSpot.interact("Net");
                            }
                        } else {
                            getWalking().webWalk(shrimpArea);
                        }
                    } else {
                        if (troutArea.contains(myPosition())) {
                            if (state.equals("fishing")) {
                                if (getInventory().isFull()) {
                                    if (getSkills().getStatic(Skill.COOKING) < 35) {
                                        state = "cooking";
                                    } else {
                                        getInventory().dropAllExcept("Fly fishing rod", "Feather");
                                        sleep(200, 400);
                                    }
                                }
                                NPC fishingSpot = getNpcs().closest("Rod Fishing spot");
                                if (fishingSpot != null) {
                                    fishingSpot.interact("Lure");
                                }
                            } else if (state.equals("cooking")) {
                                if (getInventory().contains("Raw trout")) {
                                    RS2Object fire = getObjects().closest(43475);
                                    if (fire != null) {
                                        fire.interact("Cook");
                                        sleep(600,650);
                                        new ConditionalSleep(4000) {
                                            @Override
                                            public boolean condition() {
                                                return !myPlayer().isMoving();
                                            }
                                        }.sleep();
                                        getKeyboard().typeString(" ", false);
                                    }
                                } else {
                                    getInventory().dropAllExcept("Fly fishing rod", "Feather");
                                    sleep(300,400);
                                    state = "fishing";
                                }
                            }
                        } else {
                            getWalking().webWalk(troutArea);
                        }
                    }
                    return random(500,700);
                }
            }
            return 600;

        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
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