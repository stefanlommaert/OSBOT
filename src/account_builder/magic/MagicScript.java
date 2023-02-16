package account_builder.magic;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.ArrayList;

@ScriptManifest(info = "",logo = "", version = 1, author = "stefan3140", name = "Stefan Magic Trainer")
public class MagicScript extends Script {
    private ArrayList<Area> areas;
    Area bankArea = Banks.LUMBRIDGE_UPPER;
    Area frogArea = new Area(3196, 3177, 3203, 3171);
    Area cowArea = new Area(3253, 3281, 3265, 3255);
    Area varrockArea = new Area(3140, 3513, 3187, 3468);
    boolean setupDone = false;
    int magicLvlGoal = random(35,37);
    Area fightingArea = cowArea;
    String monster = "Cow";
    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            log("Magic V2");
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
            getCamera().toTop();

            if (getSkills().getStatic(Skill.MAGIC)<13) {
                monster = "Cow";
                fightingArea = cowArea;
//                if (!cowArea.contains(myPosition())) {
//                    walkToArea(cowArea);
//                }
            } else {
                monster = "Giant frog";
                fightingArea = frogArea;
//                if (!frogArea.contains(myPosition())) {
//                    walkToArea(frogArea);
//                }
            }
        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }
    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (!setupDone && varrockArea.contains(myPlayer().getPosition())) {
                log("setting up inventory");
                setupInventory();
            }
            if (getSkills().getStatic(Skill.MAGIC) ==13 && monster.equals("Cow")) {
                monster = "Giant frog";
                fightingArea = frogArea;
                getWalking().webWalk(fightingArea);
            }
            if (!hasFood()) {
                bankDeposit();
            }
            spellManager();
            attack();
            return random(100,300);



        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

    }
    public void setupInventory() throws InterruptedException {
        Area GEArea = new Area(3162, 3486, 3167, 3485);
        getWalking().walk(GEArea.getRandomPosition());
        sleep(1000,1500);
        bank.open();
        new ConditionalSleep(8000) {
            @Override
            public boolean condition() {
                return getBank().isOpen();
            }
        }.sleep();

        if (bank.isOpen()) {
            log("Opened bank");
            bank.depositAll();
            sleep(1200,1800);
            getBank().withdrawAll("Air rune");
            sleep(1200,1800);
            getBank().withdrawAll("Water rune");
            sleep(1200,1800);
            getBank().withdrawAll("Earth rune");
            sleep(1200,1800);
            getBank().withdrawAll("Fire rune");
            sleep(1200,1800);
            getBank().withdrawAll("Mind rune");
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
            getBank().withdrawAll("Chocolate cake");
            sleep(1200,1800);
            bank.close();
            setupDone = true;
            sleep(1200,1800);
            getInventory().getItem("Amulet of magic").interact("Wear");
            getInventory().getItem("Blue wizard hat").interact("Wear");
            getInventory().getItem("Blue wizard robe").interact("Wear");
            getInventory().getItem("Staff of air").interact("Wield");
            sleep(1200,1800);
            if (!fightingArea.contains(myPosition())) {
                walkToArea(fightingArea);
            }
        } else {
            log("Did not open bank");
            stop();
        }
    }
    public void spellManager() {
        if (getConfigs().get(108)==0) {
            changeSpell(1);
        }
        if (getConfigs().get(108)==3 && getSkills().getStatic(Skill.MAGIC)==5) {
            changeSpell(2);
        } else if (getConfigs().get(108)==5 && getSkills().getStatic(Skill.MAGIC)==9) {
            changeSpell(3);
        } else if (getConfigs().get(108)==7 && getSkills().getStatic(Skill.MAGIC)==13) {
            changeSpell(4);
        } else if (getConfigs().get(108)==9 && getSkills().getStatic(Skill.MAGIC)==17) {
            changeSpell(5);
        } else if (getConfigs().get(108)==11 && getSkills().getStatic(Skill.MAGIC)==23) {
            changeSpell(6);
        } else if (getConfigs().get(108)==13 && getSkills().getStatic(Skill.MAGIC)==29) {
            changeSpell(7);
        } else if (getConfigs().get(108)==15 && getSkills().getStatic(Skill.MAGIC)==35) {
            changeSpell(8);
        }
    }

    public void changeSpell(int spellID) {
        log("Trying to change spell");
        getTabs().open(Tab.ATTACK);
        new ConditionalSleep(1800) {
            @Override
            public boolean condition() {
                return getTabs().isOpen(Tab.ATTACK);
            }
        }.sleep();
        RS2Widget autoCast = getWidgets().get(593,26);
        if (autoCast!=null) {
            autoCast.interact("Choose spell");
            sleep(600,800);
            RS2Widget spell = getWidgets().get(201,1,spellID);
            if (spell!=null) {
                spell.hover();
                sleep(600,800);
                getMouse().click(false);
            }
        } else {
            log("Could not find widget");
        }
        getTabs().open(Tab.INVENTORY);
    }

    public boolean hasFood() {
        return getInventory().contains(1897) || getInventory().contains(1899) || getInventory().contains(1901);
    }

    public boolean isReadyToAttack() {
        if (!hasFood() || getCombat().isFighting() || myPlayer().isUnderAttack() || myPlayer().isAnimating()) {
            return false;
        }
        return true;
    }

    public void attack() throws InterruptedException {
        if (getHp() < 8) {
            heal();
        }

        if (!isReadyToAttack()) {
            return;
        }
        if (getSkills().getStatic(Skill.MAGIC)>=magicLvlGoal) {
            log("Reached magic level target.");
            heal();
            heal();
            heal();
            stop();
        }
        Filter<NPC> myFilter = new Filter<NPC>() {
            public boolean match(NPC obj) {
                return (obj.getName().startsWith(monster) && !obj.isUnderAttack());
            }
        };
        sleep(2000);
        NPC enemy = getNpcs().closest(myFilter);
        if (enemy != null && !enemy.isUnderAttack()) {
            interactionEvent(enemy, "Attack");
            new ConditionalSleep(3000) {
                @Override
                public boolean condition() {
                    return !enemy.exists();
                }
            }.sleep();
        }

    }

    public void heal() throws InterruptedException {
        if (getInventory().contains(1901)) {
            if (getInventory().getSelectedItemName() == null) {
                getInventory().getItem(1901).interact("Eat");
            } else {
                getInventory().deselectItem();
            }
        }
        else if (getInventory().contains(1899)) {
            if (getInventory().getSelectedItemName() == null) {
                getInventory().getItem(1899).interact("Eat");
            } else {
                getInventory().deselectItem();
            }
        }
        else if (getInventory().contains(1897)) {
            if (getInventory().getSelectedItemName() == null) {
                getInventory().getItem(1897).interact("Eat");
            } else {
                getInventory().deselectItem();
            }
        }
        sleep(random(400,1000));
        log("eating cake");
    }

    private int getHp() {
        return getSkills().getDynamic(Skill.HITPOINTS);
    }

    private boolean interactionEvent(NPC enemy, String action) {
        InteractionEvent ev = new InteractionEvent(enemy, action);
        ev.setOperateCamera(false);
        ev.setWalkTo(true);
        execute(ev);

        return ev.hasFinished() && !ev.hasFailed();
    }

    private void bankDeposit() throws InterruptedException {
        if (!bankArea.contains(myPosition())) {
            log("Walking to bank");
            walkToArea(bankArea);
            new ConditionalSleep(5000,1000) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
        }

        bank.open();
        log("Bank opened");
        new ConditionalSleep(2000,500) {
            @Override
            public boolean condition() {
                return false;
            }
        }.sleep();
        sleep(random(200,2000));
        bank.withdrawAll(1897);
        sleep(random(500,2000));
        bank.close();
        sleep(random(200,2000));
        walkToArea(fightingArea);
        sleep(random(500,2000));
    }

    private void sleep(int time1, int time2) {
        new ConditionalSleep(random(time1, time2)) {
            @Override
            public boolean condition() {
                return false;
            }
        }.sleep();
    }

    private void walkToArea(Area area) {
        log("Walking to area.");
        getWalking().webWalk(area);
    }




}