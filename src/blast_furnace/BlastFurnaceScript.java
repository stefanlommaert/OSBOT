package blast_furnace;

import org.osbot.Con;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Chatbox;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import sun.misc.Perf;
import utils.CustomBreakManager;
import utils.GUI;
import utils.MouseCursor;
import utils.MouseTrail;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@ScriptManifest(info = "", logo = "", version = 1, author = "stefan3140", name = "Stefan Blast Furnace")
public class BlastFurnaceScript extends Script {
    private int priceRuniteOre = 11275;
    CustomBreakManager customBreakManager = new CustomBreakManager();

    private long startTimeScript;
    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 1000, this);
    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
    private int mithrilOreInBank = 1;
    private int runiteOreInBank = 1;
    private int coalInBank = 1;
    private int staminaPotionInBank = 1;
    private int totalBarsMade = 0;
    private String state = "banking";
    private String stateGE = "idle";
    private boolean didCollectFromDeposit = false;
    private boolean breakingStatus = false;
    private int trip = 1;
    private String ore = "Coal";
    private String oreToSmelt = "Mithril ore";
    private boolean useCameraForInteract = false;
    private long timePayedForeman = 0;
    private long lastDrankEnergy = 0;
    private final Area BLAST_FURNACE_AREA = new Area(1934,4975,1958,4955);
    private final Area BANK_AREA = new Area(1943,4960,1950,4958);
    private final Area CONVEYOR_AREA = new Area(1930,4968,1940,4964);
    private int taskCounter = 0;
    private int errorCounter = 0;
    private long lastTimeReset = 0;
    private long nextPause = System.currentTimeMillis()+ (long) random(30, 60) *60*1000;
    private boolean checkedStateOnStart = false;
    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            customBreakManager.exchangeContext(getBot());
            getBot().getRandomExecutor().overrideOSBotRandom(customBreakManager);

            startTimeScript = System.currentTimeMillis();
            getExperienceTracker().start(Skill.SMITHING);
            sleep(3000);
            setMouseProfile();
            checkCamera();
            if (getSkills().getStatic(Skill.SMITHING)<50) {
                log("Smithing<50 -> smelting Iron ores");
                oreToSmelt = "Iron ore";
            }
//            if (getSkills().getStatic(Skill.SMITHING)>85) {
//                oreToSmelt = "Runite ore";
//            }
            sleep(100,200);
            getMouse().move(random(80,120), random(80,120));
            sleep(1000,2000);
            if (getMouse().isOnScreen()) {
                log("Mouse is on screen");
                log("Scrolling mouse");
                for (int i = 0; i < 35; i++) {
                    getMouse().scrollDown();
                    sleep(30,35);
                    if (i%9==0) {
                        sleep(200,250);
                    }
                }
                sleep(300,500);
                for (int i = 0; i < 3; i++) {
                    getMouse().scrollUp();
                    sleep(25,35);
                }

            } else {
                log("Mouse is not on screen");
            }
            if (!BLAST_FURNACE_AREA.contains(myPlayer().getPosition())) {
                log("Going to blast furnace");
                goToBlastFurnace();
                sleep(2000,3000);
            } else {
                checkStateOnStart();
            }

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
        long totalTime = System.currentTimeMillis() - startTimeScript;
        if ((totalBarsMade>500)&& getExperienceTracker().getGainedXPPerHour(Skill.SMITHING)<50000) { // if longer than 45 min and less than 50k XP/h
            log("Stopping script, because not enough xp/h");
            stop();
        }
        g.drawString("Time run: "+ utils.GUI.formatTime(totalTime), 10, 104);
        g.drawString("XP/H: "+ utils.GUI.formatValue(getExperienceTracker().getGainedXPPerHour(Skill.SMITHING)), 10, 104+16);
        g.drawString("Total bars made: "+ totalBarsMade, 10, 104+16*2);
        g.drawString(coalInBank + " coal", 10, 104+16*3);
        if (oreToSmelt.equals("Mithril ore")) {
            g.drawString(mithrilOreInBank + " mithril ore", 10, 104 + 16 * 4);
        } else if (oreToSmelt.equals("Runite ore")) {
            g.drawString(runiteOreInBank + " runite ore", 10, 104 + 16 * 4);
        }
        g.drawString(staminaPotionInBank+" stamina potions", 10, 104+16*5);
        if (breakingStatus) {
            g.setColor(Color.red);
            g.fillOval(200, 200, 50, 50);
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (taskCounter == 4) {
                useCameraForInteract = true;
            }
            if (taskCounter >= 5) {
                if ((System.currentTimeMillis() - lastTimeReset) > 1800000) {
                    log("Resetting script, bot is stuck at: " + state);
                    lastTimeReset = System.currentTimeMillis();
                    taskCounter = 0;
                    state = "banking";
                    if (BLAST_FURNACE_AREA.contains(myPlayer().getPosition())) {
                        checkStateOnStart();
                    }
                } else {
                    log("Stopping script, script has already been reset. Stuck at: " + state + ", " + stateGE);
                    log("Starting break of 60 minutes");
                    customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(60), true);
                    return 10000;
                }
            }
            if (!checkedStateOnStart) {
                if (BLAST_FURNACE_AREA.contains(myPlayer().getPosition())) {
                    checkStateOnStart();
                }
            }
            if (!BLAST_FURNACE_AREA.contains(myPlayer().getPosition()) && stateGE.equals("idle")) {
                log("Going to blast furnace");
                goToBlastFurnace();
                sleep(2000,3000);
            } else {
                switch (stateGE) {
                    case "checkGECollect":
                        checkGECollect();
                        return 100;
                    case "goToGE":
                        goToGE();
                        return 100;
                    case "buyFromGE":
                        buyFromGE();
                        return 100;
                }
                if (state.equals("banking") && (getConfigs().get(795)<100000)) {
                    log("Money deposit almost empty, restocking now");
                    restockMoneyDeposit();
                }

                checkCamera();
                if (state.equals("banking")&&((mithrilOreInBank != 1 && mithrilOreInBank < 100)|| (runiteOreInBank != 1 && runiteOreInBank < 56) || (coalInBank != 1 && coalInBank < 100) || (staminaPotionInBank != 1 && staminaPotionInBank < 5))) {
                    if (staminaPotionInBank<5) {
                        log("Out of stamina, please restock");
                        customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(60), true);
                        return 10000;
                    }
                    log("Out of materials, checking GE collect");
                    stateGE = "checkGECollect";
                    return 1000;
                }
                if (skills.getStatic(Skill.SMITHING) < 60 && state.equals("banking") && (System.currentTimeMillis() - timePayedForeman) > 500000) {
                    log("Paying foreman");
                    payForeman();
                }
                if ((state.equals("banking") || state.equals("putOnConveyor")) && (System.currentTimeMillis() > nextPause) && (skills.getStatic(Skill.SMITHING) >= 60)) {
                    log("Pausing for some minutes");
                    breakingStatus = true;
                    nextPause = System.currentTimeMillis() + (long) random(35, 65) * 1000 * 60;
                    customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(random(5, 8)), true);
                    return 5000;
                }

                switch (oreToSmelt) {
                    case "Mithril ore":
                        if (trip == 1) {
                            switch (state) {
                                case "banking":
                                    log("Banking trip 1");
                                    ore = "Coal";
                                    bankItems();
                                    break;
                                case "putOresOnConveyor":
                                    log("Going to conveyor");
                                    putOresOnConveyor();
                                    break;
                                case "emptyCoalbag":
                                    log("Emptying coalbag");
                                    emptyCoalbag();
                                    break;
                                case "getFromFurnace":
                                    log("Getting bars from furnace");
                                    getFromFurnace();
                                    break;
                            }
                        } else if (trip == 2) {
                            switch (state) {
                                case "banking":
                                    log("Banking trip 2");
                                    ore = "Mithril ore";
                                    bankItems();
                                    break;
                                case "putOresOnConveyor":
                                    log("Going to conveyor");
                                    putOresOnConveyor();
                                    break;
                                case "emptyCoalbag":
                                    log("Emptying coalbag");
                                    emptyCoalbag();
                                    break;
                            }
                        } else if (trip == 3) {
                            switch (state) {
                                case "banking":
                                    log("Banking trip 3");
                                    ore = "Mithril ore";
                                    bankItems();
                                    break;
                                case "putOresOnConveyor":
                                    log("Going to conveyor");
                                    putOresOnConveyor();
                                    break;
                                case "emptyCoalbag":
                                    log("Emptying coalbag");
                                    emptyCoalbag();
                                    break;
                                case "getFromFurnace":
                                    log("Getting bars from furnace");
                                    getFromFurnace();
                                    break;
                            }
                        }
                        break;
                    case "Iron ore":
                        if (trip == 1) {
                            switch (state) {
                                case "banking":
                                    log("Banking trip 1");
                                    ore = "Iron ore";
                                    bankItems();
                                    break;
                                case "putOresOnConveyor":
                                    log("Going to conveyor");
                                    putOresOnConveyor();
                                    break;
                                case "emptyCoalbag":
                                    log("Emptying coalbag");
                                    emptyCoalbag();
                                    break;
                                case "getFromFurnace":
                                    log("Getting bars from furnace");
                                    getFromFurnace();
                                    break;
                            }
                        }
                        break;
                    case "Runite ore":
                        if (trip == 1) {
                            switch (state) {
                                case "banking":
                                    log("Banking trip 1");
                                    ore = "Coal";
                                    bankItems();
                                    break;
                                case "putOresOnConveyor":
                                    log("Going to conveyor");
                                    putOresOnConveyor();
                                    break;
                                case "emptyCoalbag":
                                    log("Emptying coalbag");
                                    emptyCoalbag();
                                    break;
                                case "getFromFurnace":
                                    log("Getting bars from furnace");
                                    getFromFurnace();
                                    break;
                            }
                        } else if (trip == 2) {
                            switch (state) {
                                case "banking":
                                    log("Banking trip 2");
                                    ore = "Coal";
                                    bankItems();
                                    break;
                                case "putOresOnConveyor":
                                    log("Going to conveyor");
                                    putOresOnConveyor();
                                    break;
                                case "emptyCoalbag":
                                    log("Emptying coalbag");
                                    emptyCoalbag();
                                    break;
                            }
                        } else if (trip == 3) {
                            switch (state) {
                                case "banking":
                                    log("Banking trip 3");
                                    ore = "Runite ore";
                                    bankItems();
                                    break;
                                case "putOresOnConveyor":
                                    log("Going to conveyor");
                                    putOresOnConveyor();
                                    break;
                                case "emptyCoalbag":
                                    log("Emptying coalbag");
                                    emptyCoalbag();
                                    break;
                            }
                        } else if (trip == 4) {
                            switch (state) {
                                case "banking":
                                    log("Banking trip 4");
                                    ore = "Coal";
                                    bankItems();
                                    break;
                                case "putOresOnConveyor":
                                    log("Going to conveyor");
                                    putOresOnConveyor();
                                    break;
                                case "emptyCoalbag":
                                    log("Emptying coalbag");
                                    emptyCoalbag();
                                    break;
                                case "getFromFurnace":
                                    log("Getting bars from furnace");
                                    getFromFurnace();
                                    break;
                            }
                        } else if (trip == 5) {
                            switch (state) {
                                case "banking":
                                    log("Banking trip 5");
                                    ore = "Runite ore";
                                    bankItems();
                                    break;
                                case "putOresOnConveyor":
                                    log("Going to conveyor");
                                    putOresOnConveyor();
                                    break;
                                case "emptyCoalbag":
                                    log("Emptying coalbag");
                                    emptyCoalbag();
                                    break;
                            }
                        }
                        break;
                }
            }
            return 0;

        } catch(Exception e) {
            log("ERROR, script gave error at: "+state);
            log(e);
            if (errorCounter>=5) {
                log("Stopping script, script gave error at: " + state);
                log("Starting break of 60 minutes");
                customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(60), true);
                return 10000;
            } else {
                errorCounter++;
                return 3000;
            }
        }

    }

    private void bankItems() {
        RS2Object bankChest = getObjects().closest(26707);
        if (bankChest != null) {
            interactionEvent(bankChest, "Use");
        }
        breakingStatus=false;
        new ConditionalSleep(1000) {
            @Override
            public boolean condition() {
                return bank.isOpen() || myPlayer().isMoving();
            }
        }.sleep();
        if (bank.isOpen() || myPlayer().isMoving()) {
            if (random(0,10)!=2) {
                new ConditionalSleep(random(200, 1000)) {
                    @Override
                    public boolean condition() {
                        return bank.isOpen();
                    }
                }.sleep();
                if (oreToSmelt.equals("Mithril ore")) {
                    if (trip == 2 || trip == 1) {
                        getInventory().hover(1); // Hover over bar
                    } else {
                        getInventory().hover(0); // Hover over coalbag
                    }
                } else if (oreToSmelt.equals("Iron ore")) {
                    getInventory().hover(1); // Hover over bar
                }
            }
            new ConditionalSleep(10000) {
                @Override
                public boolean condition() {
                    return bank.isOpen();
                }
            }.sleep();
            if (bank.isOpen()) {
                bank.depositAllExcept("Coal bag");
                sleep(650,700);
                if (oreToSmelt.equals("Mithril ore")) {
                    mithrilOreInBank = getBank().getItem("Mithril ore").getAmount();
                } else if (oreToSmelt.equals("Runite ore")) {
                    runiteOreInBank = getBank().getItem("Runite ore").getAmount();
                }
                coalInBank = getBank().getItem("Coal").getAmount();
                staminaPotionInBank = getBank().getItem("Stamina potion(1)").getAmount();
                if (settings.getRunEnergy() < 75 || ((System.currentTimeMillis() - lastDrankEnergy) > 110000)) {
                    bank.withdraw("Stamina potion(1)", 1);
                    new ConditionalSleep(2000) {
                        @Override
                        public boolean condition() {
                            return getInventory().contains("Stamina potion(1)");
                        }
                    }.sleep();
                    if (getInventory().contains("Stamina potion(1)")) {
                        getInventory().getItem("Stamina potion(1)").interact("Drink");
                        new ConditionalSleep(2000) {
                            @Override
                            public boolean condition() {
                                return getInventory().contains("Vial");
                            }
                        }.sleep();
                        if (getInventory().contains("Vial")) {
                            lastDrankEnergy = System.currentTimeMillis();
                            sleep(700, 800);
                            bank.depositAllExcept("Coal bag");
                            sleep(200,300);
                            if (!getInventory().isEmptyExcept("Coal bag")) {
                                log("Did not deposit vial");
                                resetBanking();
                                return;
                            }
                        } else {
                            log("Did not drink stamina potion");
                            resetBanking();
                            return;
                        }
                    } else {
                        log("Did not get stamina potion");
                        resetBanking();
                        return;
                    }
                }
                Item coalbag = getInventory().getItem("Coal bag");
                if (coalbag!=null) {
                    coalbag.interact("Fill");
                } else {
                    log("No coalbag in inventory, getting coalbag from bank");
                    bank.withdraw("Coal bag", 1);
                    sleep(700,1000);
                    resetBanking();
                    return;

                }
                bank.withdrawAll(ore);
                if ((getChatbox().getMessages(Chatbox.MessageType.ALL).get(0).equals("The coal bag contains 27 pieces of coal.")) || (getChatbox().getMessages(Chatbox.MessageType.ALL).get(1).equals("The coal bag contains 27 pieces of coal.")) || (getChatbox().getMessages(Chatbox.MessageType.ALL).get(2).equals("The coal bag contains 27 pieces of coal."))){
                    bank.close();
                    new ConditionalSleep(1000) {
                        @Override
                        public boolean condition() {
                            return getInventory().isFull();
                        }
                    }.sleep();
                    if (getInventory().isFull()) {
                        state = "putOresOnConveyor";
                        taskCounter = 0;
                    } else {
                        log("Did not take ores from inventory");
                        resetBanking();
                    }
                } else {
                    log("Did not fill coalbag");
                    resetBanking();
                }
            } else {
                log("Did not open bank");
                taskCounter++;
            }
        } else {
            log("Walking to bank");
            walkEvent(BANK_AREA.getRandomPosition());
            new ConditionalSleep(1000) {
                @Override
                public boolean condition() {
                    return !myPlayer().isMoving();
                }
            }.sleep();
            taskCounter++;
        }
    }

    private void resetBanking() {
        if (bank.isOpen()) {
            bank.close();
        }
        sleep(300,500);
        taskCounter++;
    }

    private void putOresOnConveyor() {
        RS2Object conveyor = getObjects().closest(9100);
        if (conveyor != null) {
            interactionEvent(conveyor, "Put-ore-on");
            new ConditionalSleep(1000) {
                @Override
                public boolean condition() {
                    return myPlayer().isMoving() || !getInventory().isFull();
                }
            }.sleep();
            if (myPlayer().isMoving() || !getInventory().isFull()) {
                new ConditionalSleep(random(200,1000)) {
                    @Override
                    public boolean condition() {
                        return !myPlayer().isMoving() || !getInventory().isFull();
                    }
                }.sleep();
                if (oreToSmelt.equals("Mithril ore")) {
                    if (trip != 3 && random(0, 10) > 1) {
                        getInventory().hover(0); // Hover over coalbag
                    } else if (trip == 3) {
                        getInventory().hover(0); // Hover over coalbag
                    }
                } else {
                    if (random(0, 10) > 1) {
                        getInventory().hover(0); // Hover over coalbag
                    }

                }
                new ConditionalSleep(8000) {
                    @Override
                    public boolean condition() {
                        return !getInventory().isFull();
                    }
                }.sleep();
            }
            if (!getInventory().isFull()) {
                state = "emptyCoalbag";
                taskCounter = 0;
            } else {
                log("Walking to conveyor");
                walkEvent(CONVEYOR_AREA.getRandomPosition());
                sleep(500,700);
                taskCounter++;
            }
        }
    }

    private void emptyCoalbag() {
        getKeyboard().pressKey(16);
        sleep(50,100);
        getInventory().getItem("Coal bag").interact("Empty");
        sleep(50,100);
        getKeyboard().releaseKey(16);
        if (oreToSmelt.equals("Mithril ore")) {
            int mithrilBarInCollector = getConfigs().get(545) >> 24;
            if (mithrilBarInCollector == 28) {
                log("Bot was too slow to put coal in time, resetting furnace");
                checkStateOnStart();
                return;
            }
        }
        getObjects().closest(9100).hover(); // Hover over conveyor belt
        new ConditionalSleep(1000) {
            @Override
            public boolean condition() {
                return getInventory().isFull();
            }
        }.sleep();
        if (getInventory().isFull()) {
            RS2Object conveyor = getObjects().closest(9100);
            if (conveyor != null) {
                interactionEvent(conveyor, "Put-ore-on");
                sleep(100,200);
                if (oreToSmelt.equals("Mithril ore") || oreToSmelt.equals("Iron ore")) {
                    if (trip == 1 || trip == 3) {
                        if (random(0,10)!=2) {
                            getObjects().closest(9092).hover(); // Hover over bar dispenser
                        }
                    }
                } else if (oreToSmelt.equals("Runite ore")) {
                    if (trip == 1 || trip == 4) {
                        if (random(0,10)!=2) {
                            getObjects().closest(9092).hover(); // Hover over bar dispenser
                        }
                    }
                }
                new ConditionalSleep(1000) {
                    @Override
                    public boolean condition() {
                        return !getInventory().isFull();
                    }
                }.sleep();
                if (!getInventory().isFull()) {
                    if (oreToSmelt.equals("Mithril ore")) {
                        if (trip == 1 || trip == 3) {
                            state = "getFromFurnace";
                        } else {
                            state = "banking";
                            trip = 3;
                        }
                    } else if (oreToSmelt.equals("Iron ore")) {
                        sleep(3000,4000);
                        state = "getFromFurnace";
                    } else if (oreToSmelt.equals("Runite ore")) {
                        if (trip == 1 || trip == 4) {
                            state = "getFromFurnace";
                        } else {
                            state = "banking";
                            trip+=1;  //2,3->3,4 and 5->6 SO make 6->1
                            if (trip==6) {
                                trip = 1;
                            }
                        }
                    }
                    taskCounter = 0;
                } else {
                    log("Didn't put coal on conveyor");
                    log("Trip: "+trip);
                    taskCounter++;
                }
            }
        } else {
            log("Coalbag empty failed");
            log("Trip: "+trip);
            taskCounter++;
        }
    }

    private void getFromFurnace() {
        RS2Object furnace = getObjects().closest(9092);
        if (getDialogues().isPendingContinuation()) {
            log("Pending conversation for furnace");
            getDialogues().clickContinue();
        } else {
            if (furnace != null) {
                if (interactionEvent(furnace, "Take")) {
                    new ConditionalSleep(1000) {
                        @Override
                        public boolean condition() {
                            return getDialogues().inDialogue()|| myPlayer().isMoving();
                        }
                    }.sleep();
                    if (getDialogues().inDialogue() || myPlayer().isMoving()) {
                        new ConditionalSleep(5000) {
                            @Override
                            public boolean condition() {
                                return getDialogues().inDialogue();
                            }
                        }.sleep();
                        if (getDialogues().inDialogue() && !getDialogues().isPendingContinuation()) {
                            getKeyboard().typeString(" ", false);
                            new ConditionalSleep(1000) {
                                @Override
                                public boolean condition() {
                                    return getInventory().isFull();
                                }
                            }.sleep();
                            if (getInventory().isFull()) {
                                if (oreToSmelt.equals("Mithril ore")) {
                                    if (trip == 1) {
                                        trip = 2;
                                    } else if (trip == 3) {
                                        trip = 1;
                                    }
                                } else if (oreToSmelt.equals("Runite ore")) {
                                    trip+=1;
                                }
                                state = "banking";
                                taskCounter = 0;
                                totalBarsMade += 27;
                            } else {
                                log("Inventory not full after taking from furnace");
                                taskCounter++;
                            }
                        } else {
                            log("Did not open dialogue");
                            taskCounter++;
                        }
                    } else {
                        log("Not walking to furnace");
                        taskCounter++;
                    }
                } else {
                    log("Furnace failed");
                    taskCounter++;
                }
            } else {
                log("Furnace is null");
                taskCounter++;
            }

        }
    }

    private void checkCamera() {
        int pitch = getCamera().getPitchAngle();
        int yaw = getCamera().getYawAngle();
        if (yaw > 274 || yaw < 266){
            getCamera().moveYaw(random(266,275));
        }
        if (pitch!=67){
            getCamera().movePitch(67);
        }
    }

    private void checkStateOnStart() {
        if (getInventory().isFull()) {
            log("Walking to bank");
            walkEvent(BANK_AREA.getRandomPosition());
            new ConditionalSleep(10000) {
                @Override
                public boolean condition() {
                    return !myPlayer().isMoving();
                }
            }.sleep();
            RS2Object bankChest = getObjects().closest(26707);
            if (bankChest != null) {
                interactionEvent(bankChest, "Use");
            }
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return bank.isOpen();
                }
            }.sleep();
            if (bank.isOpen()) {
                sleep(200, 300);
                bank.depositAllExcept("Coal bag");
                sleep(200,300);
                getInventory().getItem("Coal bag").interact("Empty");
                sleep(600,1000);
                bank.close();
            }
        } else {
            getKeyboard().pressKey(16);
            sleep(100,200);
            getInventory().getItem("Coal bag").interact("Empty");
            sleep(100,200);
            getKeyboard().releaseKey(16);
            sleep(1000,1200);
            getDialogues().clickContinue();
            sleep(1000,1200);
            if (getInventory().isFull()) {
                RS2Object conveyor = getObjects().closest(9100);
                if (conveyor != null) {
                    interactionEvent(conveyor, "Put-ore-on");
                    new ConditionalSleep(10000) {
                        @Override
                        public boolean condition() {
                            return !getInventory().isFull();
                        }
                    }.sleep();
                }
            }
        }
        if (oreToSmelt.equals("Mithril ore")) {
            int mithrilOreInFurnace = getConfigs().get(547) >> 24;
            int mithrilBarInCollector = getConfigs().get(545) >> 24;
            int coalInFurnace = getConfigs().get(547) & 255;
            log("Mithril ore in furnace: " + mithrilOreInFurnace);
            log("Coal in furnace: " + coalInFurnace);
            log("Mithril bar in collector: " + mithrilBarInCollector);
            if (mithrilBarInCollector == 27) {
                if (coalInFurnace < 100) {
                    log("27 mithril bars & less then 100 coal -> trip 1");
                    trip = 1;
                } else {
                    log("27 mithril bars & more then 100 coal -> trip 3");
                    trip = 3;
                }
            } else if (mithrilBarInCollector == 28) {
                log("28 mithril bars -> trip 1");
                trip = 3;
                state = "getFromFurnace";
            } else if (mithrilBarInCollector == 0) {
                log("0 mithril bars -> trip 2");
                trip = 2;
            }
        } else if (oreToSmelt.equals("Runite ore")) {
            int runiteBarInCollector = (getConfigs().get(546) >> 8)&255;
            int coalInFurnace = getConfigs().get(547) & 255;
            log("Coal in furnace: " + coalInFurnace);
            log("Runite bar in collector: " + runiteBarInCollector);
            if (runiteBarInCollector==27) {
                if (coalInFurnace<=54) {
                    trip = 1;
                } else {
                    trip = 4;
                }
            } else if (coalInFurnace>135) {
                trip = 3;
            } else if (coalInFurnace>108) {
                trip = 5;
            } else {
                trip = 2;
            }
        }
        checkedStateOnStart = true;

    }

    private void payForeman() {
        walkEvent(BANK_AREA.getRandomPosition());
        new ConditionalSleep(1000) {
            @Override
            public boolean condition() {
                return !myPlayer().isMoving();
            }
        }.sleep();
        RS2Object bankChest = getObjects().closest(26707);
        if (bankChest != null) {
            interactionEvent(bankChest, "Use");
        }
        new ConditionalSleep(4000) {
            @Override
            public boolean condition() {
                return bank.isOpen();
            }
        }.sleep();
        if (bank.isOpen()) {
            bank.depositAllExcept("Coal bag");
            bank.withdraw("Coins", 2500);
            bank.close();
            NPC foreman = getNpcs().closest("Blast Furnace Foreman");
            if (foreman != null) {
                interactionEvent(foreman, "Pay");
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return getDialogues().inDialogue();
                    }
                }.sleep();
                getKeyboard().typeString("1", false);
            }
            timePayedForeman = System.currentTimeMillis();
            state = "banking";
        }
    }

    private void goToBlastFurnace() {
        Area keldagrimArea = new Area(2906, 10210, 2942, 10156);
        Area door1Area = new Area(2926, 10185, 2939, 10177);
        Area door2Area = new Area(2927, 10189, 2933, 10186);
        Area door3Area = new Area(2927, 10194, 2933, 10190);
        Area lastArea = new Area(2927, 10198, 2933, 10195);
        if (!keldagrimArea.contains(myPlayer().getPosition())) {
            getTabs().open(Tab.CLANCHAT);
            sleep(100, 200);
            getWidgets().get(76, 9).hover();
            sleep(100, 200);
            getMouse().click(false);
            getWidgets().getWidgetContainingText("Blast Furnace").hover();
            sleep(100, 200);
            getMouse().click(false);
            getWidgets().get(76, 30).hover();
            sleep(100, 200);
            getMouse().click(false);
            new ConditionalSleep(15000) {
                @Override
                public boolean condition() {
                    return keldagrimArea.contains(myPlayer().getPosition());
                }
            }.sleep();
            if (keldagrimArea.contains(myPlayer().getPosition())) {
                taskCounter=0;
            } else {
                log("Teleporting to blast furnace did not work, trying again");
                taskCounter++;
            }
        }
        else if (door1Area.contains(myPlayer().getPosition())) {
            sleep(1000,1500);
            RS2Object door1 = getObjects().closest(6977);
            if (door1 != null) {
                door1.interact("Open");
                sleep(1000, 1200);
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return !myPlayer().isMoving()&& door2Area.contains(myPlayer().getPosition());
                    }
                }.sleep();
                sleep(100,200);

            } else {
                log("Could not find first door");
            }
        }
        else if (door2Area.contains(myPlayer().getPosition())) {
            RS2Object door2 = getObjects().closest(6102);
            if (door2 != null) {
                door2.interact("Open");
                sleep(1000, 1200);
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return !myPlayer().isMoving();
                    }
                }.sleep();
            } else {
                log("Second door is already open");
            }
            sleep(100, 200);
            walkEvent(new Position(2930, 10194, 0));
            sleep(100, 200);
        }
        else if (door3Area.contains(myPlayer().getPosition())) {
            RS2Object door3 = getObjects().closest(6975);
            if (door3 != null) {
                door3.interact("Open");
                sleep(1000, 1200);
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return !myPlayer().isMoving();
                    }
                }.sleep();
            } else {
                log("Third door is already open");
            }
        }
        else if (lastArea.contains(myPlayer().getPosition())) {
            getObjects().closest(9084).interact("Climb-down");
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return !myPlayer().isMoving();
                }
            }.sleep();
        } else {
            log("Stuck at going to blast furnace, trying again");
            taskCounter++;
        }
    }

    private void checkGECollect() {
        log("Walking to blast furnace bank");
        walkEvent(BANK_AREA.getRandomPosition());

        new ConditionalSleep(10000) {
            @Override
            public boolean condition() {
                return !myPlayer().isMoving();
            }
        }.sleep();
        RS2Object bankChest = getObjects().closest(26707);
        if (bankChest != null) {
            interactionEvent(bankChest, "Collect");
            new ConditionalSleep(4000) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
            getWidgets().getWidgetContainingText("Bank").interact("Collect to bank");
            sleep(1000,1200);
            if (getChatbox().getMessages(Chatbox.MessageType.ALL).get(0).equals("You have nothing to collect.") || getChatbox().getMessages(Chatbox.MessageType.ALL).get(1).equals("You have nothing to collect.") || getChatbox().getMessages(Chatbox.MessageType.ALL).get(2).equals("You have nothing to collect.") || getChatbox().getMessages(Chatbox.MessageType.ALL).get(3).equals("You have nothing to collect.")) {
                log("Did not collect from collection box");
                stateGE = "goToGE";
                didCollectFromDeposit = false;
            } else {
                log("Collected items from collection box");
                stateGE = "goToGE";
                didCollectFromDeposit = true;

//                coalInBank = 200;
//                mithrilOreInBank = 200;
//                runiteOreInBank = 200;
//                staminaPotionInBank = 200;

            }
            taskCounter=0;
        } else {
            log("Was not able to interact with bank, trying again");
            taskCounter++;
        }
    }

    private void goToGE() {
        Area varrockArea = new Area(3141, 3513, 3187, 3467);
        if (!varrockArea.contains(myPlayer().getPosition())) {
            log("Walking to blast furnace bank");
            walkEvent(BANK_AREA.getRandomPosition());
            new ConditionalSleep(10000) {
                @Override
                public boolean condition() {
                    return !myPlayer().isMoving();
                }
            }.sleep();
            RS2Object bankChest = getObjects().closest(26707);
            if (bankChest != null) {
                interactionEvent(bankChest, "Use");
            }
            new ConditionalSleep(4000) {
                @Override
                public boolean condition() {
                    return bank.isOpen();
                }
            }.sleep();
            if (bank.isOpen()) {
                bank.depositAllExcept("Coal bag");
                log("Teleporting to GE");
                String ringOfWealth = "Ring of wealth (5)";
                if (bank.contains("Ring of wealth (1)")) {
                    ringOfWealth = "Ring of wealth (1)";
                } else if (bank.contains("Ring of wealth (2)")) {
                    ringOfWealth = "Ring of wealth (2)";
                } else if (bank.contains("Ring of wealth (3)")) {
                    ringOfWealth = "Ring of wealth (3)";
                } else if (bank.contains("Ring of wealth (4)")) {
                    ringOfWealth = "Ring of wealth (4)";
                } else if (bank.contains("Ring of wealth (5)")) {
                    ringOfWealth = "Ring of wealth (5)";
                } else {
                    log("Did not have ring of wealth");
                }
                bank.withdraw(ringOfWealth, 1);
                bank.enableMode(Bank.BankMode.WITHDRAW_NOTE);
                sleep(100, 200);
                if (oreToSmelt.equals("Runite ore")) {
                    bank.withdrawAll("Runite bar");
                } else {
                    bank.withdrawAll("Mithril bar");
                }
                bank.withdrawAll("Coins");
                sleep(1000, 1200);
                bank.close();
                getInventory().getItem(ringOfWealth).interact("Rub");
                sleep(700, 1000);
                getWidgets().getWidgetContainingText("Grand Exchange").hover();
                sleep(300, 500);
                getMouse().click(false);
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return varrockArea.contains(myPlayer().getPosition());
                    }
                }.sleep();
            }
        }
        if (varrockArea.contains(myPlayer().getPosition())) {
            stateGE = "buyFromGE";
            taskCounter=0;
        } else {
            log("Did not TP to varrock, trying again.");
            taskCounter++;
        }


    }

    private void buyFromGE() throws InterruptedException {
        Area GEArea = new Area(3162, 3486, 3167, 3485);
        getWalking().walk(GEArea.getRandomPosition());
        sleep(1000,1500);
        NPC clerk = getNpcs().closest("Grand Exchange Clerk");
        if (clerk!=null) {
            clerk.interact("Exchange");
            new ConditionalSleep(8000) {
                @Override
                public boolean condition() {
                    return getGrandExchange().isOpen();
                }
            }.sleep();
        }
        if (getGrandExchange().isOpen()) {
            log("Opened GE");
            sleep(300,500);
            getGrandExchange().sellItem(2364, 12000, getInventory().getItem(2364).getAmount()); //2364 runite bar      2360 mithril bar
            sleep(2000,3000);
            getGrandExchange().collect();
            log("Sold bars on GE");
            sleep(1000,2000);
            if (didCollectFromDeposit) {
                getGrandExchange().buyItem(451, "Runite o", priceRuniteOre, 1000);  //447 mithril ore     451 runite ore
                sleep(2000,3000);
            } else {
                getGrandExchange().buyItem(451, "Runite o", priceRuniteOre+500, 1000);  //447 mithril ore     451 runite ore
                sleep(2000,3000);
                getGrandExchange().collect();
                sleep(1000,2000);
            }
            getGrandExchange().close();
            sleep(600,1000);
//            getBank().open();
//            new ConditionalSleep(8000) {
//                @Override
//                public boolean condition() {
//                    return getBank().isOpen();
//                }
//            }.sleep();
//            sleep(400,500);
//            getBank().depositAllExcept("Coal bag");
//            log("Deposited all items to bank");
            stateGE = "idle";
            runiteOreInBank = 200;
            taskCounter=0;
        } else {
            log("Did not open GE");
            taskCounter++;
        }
    }

    private void restockMoneyDeposit() {
        ;
    }

    private boolean interactionEvent(Entity entity, String action) {
        InteractionEvent ev = new InteractionEvent(entity, action);
        if (useCameraForInteract){
            log("Camera is allowed to move");
            ev.setOperateCamera(true);
            ev.setWalkTo(true);
            useCameraForInteract=false;
        } else {
            ev.setOperateCamera(false);
            ev.setWalkTo(false);
        }
        execute(ev);
        return ev.hasFinished() && !ev.hasFailed();
    }

    private void walkEvent(Position position) {
        WalkingEvent ev = new WalkingEvent(position);
        ev.setOperateCamera(false);
        ev.setEnergyThreshold(40);
        execute(ev);
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
        profile.setSpeedBaseTime(200); //185
        profile.setFlowSpeedModifier(1); //1.0
        profile.setDeviation(7); //7
        profile.setMinOvershootDistance(50); //25
        profile.setMinOvershootTime(350); //375
        profile.setNoise(2.3); //2.15
        profile.setOvershoots(1); //2
        getBot().setMouseMoveProfile(profile);
    }

}