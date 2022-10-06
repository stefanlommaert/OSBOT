package blast_furnace;

import org.osbot.rs07.api.DepositBox;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.AntiBotDetection;
import utils.MouseCursor;
import utils.MouseTrail;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Motherlode Mine")
public class MotherlodeMineScript extends Script {
    AntiBotDetection antiBotDetection = new AntiBotDetection(this, "mining");
    long timeLastAntiBan = System.currentTimeMillis();
    int durationUntilNextAntiBan = 60000*8;
    private final MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private final MouseCursor cursor = new MouseCursor(25, 2, Color.red, this);

    private String state = "mining";
    long waitingTime = System.currentTimeMillis();
    Position[] positionOresList = {
            new Position(3729, 5660, 0),
            new Position(3729, 5661, 0),
            new Position(3729, 5662, 0),
            new Position(3733, 5663, 0),
            new Position(3732, 5665, 0),
            new Position(3733, 5667, 0)
    };
    ArrayList<Position> positionOres = new ArrayList<>(Arrays.asList(positionOresList));
Position[] hopperArea = {
        new Position(3748, 5674, 0),
        new Position(3747, 5674, 0),
        new Position(3746, 5674, 0),
        new Position(3745, 5674, 0),
        new Position(3744, 5674, 0),
        new Position(3745, 5675, 0),
        new Position(3746, 5675, 0),
        new Position(3747, 5675, 0),
        new Position(3748, 5675, 0),
        new Position(3744, 5675, 0),
        new Position(3744, 5676, 0),
        new Position(3745, 5676, 0),
        new Position(3746, 5676, 0),
        new Position(3747, 5676, 0),
        new Position(3748, 5676, 0),
        new Position(3749, 5674, 0),
        new Position(3750, 5674, 0),
        new Position(3750, 5673, 0),
        new Position(3750, 5672, 0),
        new Position(3751, 5673, 0),
        new Position(3749, 5675, 0)
};
    Position[] washerArea = {
            new Position(3749, 5661, 0),
            new Position(3750, 5661, 0),
            new Position(3751, 5661, 0),
            new Position(3751, 5660, 0),
            new Position(3750, 5660, 0),
            new Position(3749, 5660, 0),
            new Position(3750, 5662, 0),
            new Position(3749, 5659, 0),
            new Position(3750, 5659, 0),
            new Position(3751, 5659, 0),
            new Position(3750, 5658, 0),
            new Position(3749, 5658, 0),
            new Position(3749, 5657, 0),
            new Position(3748, 5657, 0),
            new Position(3748, 5658, 0),
            new Position(3747, 5658, 0),
            new Position(3746, 5658, 0),
            new Position(3746, 5657, 0),
            new Position(3747, 5657, 0)
    };
    Position[] depositArea = {
            new Position(3758, 5665, 0),
            new Position(3757, 5665, 0),
            new Position(3757, 5664, 0),
            new Position(3757, 5663, 0),
            new Position(3756, 5663, 0),
            new Position(3756, 5664, 0),
            new Position(3756, 5662, 0),
            new Position(3757, 5662, 0),
            new Position(3758, 5662, 0),
            new Position(3755, 5663, 0),
            new Position(3758, 5663, 0),
            new Position(3758, 5661, 0),
            new Position(3756, 5665, 0),
            new Position(3755, 5664, 0),
            new Position(3755, 5662, 0),
            new Position(3756, 5661, 0)
    };
    Position[] mineArea = {
            new Position(3730, 5666, 0),
            new Position(3730, 5665, 0),
            new Position(3730, 5663, 0),
            new Position(3730, 5664, 0),
            new Position(3730, 5662, 0),
            new Position(3730, 5661, 0),
            new Position(3730, 5660, 0),
            new Position(3730, 5659, 0),
            new Position(3731, 5659, 0),
            new Position(3731, 5658, 0),
            new Position(3732, 5658, 0),
            new Position(3732, 5659, 0),
            new Position(3732, 5660, 0),
            new Position(3731, 5660, 0),
            new Position(3731, 5661, 0),
            new Position(3732, 5661, 0),
            new Position(3731, 5662, 0),
            new Position(3731, 5663, 0),
            new Position(3731, 5664, 0),
            new Position(3731, 5665, 0),
            new Position(3731, 5666, 0),
            new Position(3731, 5667, 0),
            new Position(3732, 5667, 0),
            new Position(3732, 5663, 0),
            new Position(3732, 5662, 0),
            new Position(3732, 5661, 0),
            new Position(3733, 5659, 0)
    };
    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            log("Mother lode V2");
//            setMouseProfile();

        } catch(Exception e) {
            log("error at onStart()");
            log(e);
            stop();
        }
    }

    public void onPaint(Graphics2D g){
        trail.paint(g);
        cursor.paint(g);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if ((System.currentTimeMillis() - timeLastAntiBan) > durationUntilNextAntiBan) {
                log("Executing anti ban measure");
                antiBotDetection.antiBan();
                timeLastAntiBan = System.currentTimeMillis();
                durationUntilNextAntiBan = 60000 *random(7,15);
                log("Time until next anti ban: "+Integer.toString(durationUntilNextAntiBan/60000) +"minutes");

            }
            if (state.equals("mining")) {
                if (getInventory().isFull()) {
                    state = "goingToHopper";
                } else {
                    if (!myPlayer().isAnimating()) {
                        sleep(3000);
                        if (!myPlayer().isAnimating()) {
                            mineOre();
                        }
                    }
                }
            } else if (state.equals("goingToHopper")) {
                goToHopper();
            } else if (state.equals("waitForWashing")) {
                waitForWashing();
            } else if (state.equals("gettingWashedOres")) {
                goToWasher();
            } else if (state.equals("depositingOres")) {
                depositOres();
            }
            return random(200,400);

        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

    }

    public void mineOre() {
        Filter<RS2Object> myFilter = new Filter<RS2Object>() {
            public boolean match(RS2Object obj) {
                return (obj.getName().startsWith("Ore vein") && positionOres.contains(obj.getPosition()));
            }
        };
        RS2Object ore = getObjects().closest(myFilter);
        if (ore != null) {
            if (!getTabs().isOpen(Tab.INVENTORY)) {
                getTabs().open(Tab.INVENTORY);
                new ConditionalSleep(random(200,500)) {
                    @Override
                    public boolean condition() {
                        return false;
                    }
                }.sleep();
                camera.toTop();
            }
            interactionEvent(ore, "Mine");
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return !ore.exists() || myPlayer().isAnimating();
                }
            }.sleep();
        }
        else {
            log("Ores depleted, changing worlds");
            changeWorld();
        }
    }

    private void goToHopper() {
        log("Going to hopper");
        walkToArea(hopperArea[random(0,hopperArea.length-1)]);
        new ConditionalSleep(random(2000,3000)) {
            @Override
            public boolean condition() {
                return false;
            }
        }.sleep();
        RS2Object hopper = getObjects().closest("Hopper");
        if (hopper != null) {
            if (!interactionEvent(hopper, "Deposit")) {
                if (!interactionEvent(hopper, "Deposit", true)) {
                    log("Could not click on hopper");
                    stop();
                }

            }
            new ConditionalSleep(random(2000,3000)) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
            if (!getInventory().isFull()) {
                walkToArea(washerArea[random(0,washerArea.length-1)]);
                state = "waitForWashing";
                waitingTime = System.currentTimeMillis() + random(10000, 15000);
            } else {
                state = "goingToHopper";
            }
        }
    }

    private void waitForWashing() {
        RS2Object sack = getObjects().closest("Sack");
        if (sack != null) {
            state = "gettingWashedOres";
        } else if (waitingTime < System.currentTimeMillis()) {
            changeWorld();
            getTabs().open(Tab.INVENTORY);
            waitingTime = System.currentTimeMillis()+random(20000,25000);
        }
    }

    private void goToWasher() {
        RS2Object sack = getObjects().closest("Sack");
        if (sack!=null && !interactionEvent(sack, "Search")) {
            interactionEvent(sack, "Search", true);
        }
        new ConditionalSleep(random(7000,10000)) {
            @Override
            public boolean condition() {
                return getInventory().contains("Coal");
            }
        }.sleep();
        state = "depositingOres";
    }

    private void changeWorld() {
        getTabs().open(Tab.LOGOUT);
        RS2Widget worldSwitcher = getWidgets().getWidgetContainingText("World Switcher");

        if (worldSwitcher!=null) {
            worldSwitcher.interact("World Switcher");
            new ConditionalSleep(random(500,1000)) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
        }
        int hopTo = 303;
        if (myPlayer().getName().equals("SMITHINGaGOD")) {
            int[] worlds = {303,304,311,312,327,328};
            int rand = random(0, worlds.length - 1);
            hopTo = worlds[rand];
        } else if (myPlayer().getName().equals("SexySmithing")) {
            int[] worlds = {336,343,344,351,352};
            int rand = random(0, worlds.length - 1);
            hopTo = worlds[rand];
        } else if (myPlayer().getName().equals("aSmithingGod")) {
            int[] worlds = {359,360,367,368,375,376};
            int rand = random(0, worlds.length - 1);
            hopTo = worlds[rand];
        } else {
            log("Name did not match to custom worlds");
            int[] worlds = {463,464,465,466};
            int rand = random(0, worlds.length - 1);
            hopTo = worlds[rand];
        }

        log("Going to world:" + hopTo);
        RS2Widget worldWidget = getWidgets().get(69,16,hopTo);
        if (worldWidget!=null) {
            worldWidget.interact("Switch");
            new ConditionalSleep(random(500,1000)) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
        }
    }

    private void nchangeWorld() {
        int hopTo = -1;
        int[] worlds = {351,352,359,360,367,368,375,376};
        int rand = random(0, worlds.length - 1);
        hopTo = worlds[rand];
        log("Going to world:" + hopTo);
        if (hopTo != -1 && hopTo != getWorlds().getCurrentWorld()){
            getWorlds().hover(hopTo);
            new ConditionalSleep(random(200,400)) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
            mouse.click(false);
        }
    }

    private void depositOres() {
        walkToArea(depositArea[random(0,depositArea.length-1)]);
        new ConditionalSleep(random(1000,2000)) {
            @Override
            public boolean condition() {
                return false;
            }
        }.sleep();
        DepositBox depositBox = getDepositBox();
        if (depositBox.open()) {
            new ConditionalSleep(random(500, 1000)) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
            depositBox.depositAll();
            new ConditionalSleep(random(200, 600)) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
            depositBox.close();
            new ConditionalSleep(random(500, 2000)) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
            walkToArea(mineArea[random(0,mineArea.length-1)]);
            new ConditionalSleep(random(1000, 2000)) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
            state = "mining";
        }


    }

    private boolean interactionEvent(Entity entity, String action, boolean... useCamera) {
        boolean useCameraNew = (useCamera.length >= 1) ? useCamera[0] : false;
        InteractionEvent ev = new InteractionEvent(entity, action);
        if (!useCameraNew) {
            ev.setOperateCamera(false);
        }
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

    private boolean webWalkEvent(Position position) {
        WebWalkEvent ev = new WebWalkEvent(position);
        ev.setMoveCameraDuringWalking(false);
        ev.setEnergyThreshold(40);
        execute(ev);
        return ev.hasFinished() && !ev.hasFailed();
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
        getBot().setMouseMoveProfile(profile);
    }
//            [INFO][Bot #1][08/20 01:29:18 PM]: 185
//            [INFO][Bot #1][08/20 01:29:18 PM]: 1.0
//            [INFO][Bot #1][08/20 01:29:18 PM]: 7
//            [INFO][Bot #1][08/20 01:29:18 PM]: 25
//            [INFO][Bot #1][08/20 01:29:18 PM]: 375
//            [INFO][Bot #1][08/20 01:29:18 PM]: 2.15
//            [INFO][Bot #1][08/20 01:29:18 PM]: 2
    private void walkToArea(Area area) {
        webWalkEvent(area);
    }

    private void walkToArea(Position position) {
        webWalkEvent(position);
    }

}
