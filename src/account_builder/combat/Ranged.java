package account_builder.combat;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.ArrayList;

@ScriptManifest(info = "",logo = "", version = 1, author = "stefan3140", name = "Stefan Ranged")
public class Ranged extends Script {
    private ArrayList<Area> areas;
    Area bankArea = Banks.LUMBRIDGE_UPPER;
    Area frogArea = new Area(3196, 3177, 3203, 3171);
    Area chickenArea = new Area(3228, 3300, 3231, 3298);
    Area cowArea = new Area(3253, 3281, 3265, 3255);
    Area fightingArea = cowArea;
    String monster = "Chicken";

    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            log("Combat V3");
            if (getSkills().getStatic(Skill.RANGED)<20) {
                log("Training at cows");
                monster = "Cow";
                fightingArea = cowArea;
            } else {
                log("Training at giant frogs");
                monster = "Giant frog";
                fightingArea = frogArea;
            }
            if (!fightingArea.contains(myPosition())) {
                walkToArea(fightingArea);
            }

        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }
    @Override
    public int onLoop() throws InterruptedException {
        try {
            if (monster.equals("Cow") && (getSkills().getStatic(Skill.RANGED)>=20)) {
                monster = "Giant frog";
                log("Now training at frogs");
                fightingArea = frogArea;
                walkToArea(fightingArea);
            }
            if (getSkills().getStatic(Skill.RANGED)==5 && getInventory().contains("Oak shortbow")) {
                getInventory().getItem("Oak shortbow").interact("Wield");
            } else if (getSkills().getStatic(Skill.RANGED)==20 && getInventory().contains("Willow shortbow")) {
                getInventory().getItem("Willow shortbow").interact("Wield");
            } else if (getSkills().getStatic(Skill.RANGED)==30 && getInventory().contains("Maple shortbow")) {
                getInventory().getItem("Maple shortbow").interact("Wield");
            }
            if (getSkills().getStatic(Skill.RANGED)==5 && getInventory().contains("Steel arrow")) {
                getInventory().getItem("Steel arrow").interact("Wield");
            } else if (getSkills().getStatic(Skill.RANGED)==20 && getInventory().contains("Mithril arrow")) {
                getInventory().getItem("Mithril arrow").interact("Wield");
            } else if (getSkills().getStatic(Skill.RANGED)==30 && getInventory().contains("Adamant arrow")) {
                getInventory().getItem("Adamant arrow").interact("Wield");
            }
            if (getSkills().getStatic(Skill.RANGED)==20 && getInventory().contains("Coif")) {
                getInventory().getItem("Coif").interact("Wield");
            }
            if (getSkills().getStatic(Skill.RANGED)==20 && getInventory().contains("Studded chaps")) {
                getInventory().getItem("Studded chaps").interact("Wield");
            } else if (getSkills().getStatic(Skill.RANGED)==40 && getInventory().contains("Green d'hide chaps")) {
                getInventory().getItem("Green d'hide chaps").interact("Wield");
            }
            if (getSkills().getStatic(Skill.RANGED)==40 && getInventory().contains("Green d'hide vambraces")) {
                getInventory().getItem("Green d'hide vambraces").interact("Wield");
            }

            if (!hasFood()) {
                bankDeposit();
            }
            attack();
            return random(100,300);



        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

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
        if (getHp() < 30) {
            heal();
        }

        if (!isReadyToAttack()) {
            return;
        }
        Filter<NPC> myFilter = new Filter<NPC>() {
            public boolean match(NPC obj) {
                return (obj.getName().equals(monster) && !obj.isUnderAttack() && !obj.isHitBarVisible());
            }
        };
        NPC enemy = getNpcs().closest(myFilter);
        if (enemy != null && !enemy.isUnderAttack()) {
            interactionEvent(enemy, "Attack");
            new ConditionalSleep(600) {
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
        walkToArea(frogArea);
        sleep(random(500,2000));
    }

    private void walkToArea(Area area) {
        log("Walking to area.");
        getWalking().webWalk(area);
    }




}