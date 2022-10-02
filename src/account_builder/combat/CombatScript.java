package account_builder.combat;

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

@ScriptManifest(info = "",logo = "", version = 1, author = "stefan3140", name = "Stefan Combat")
public class CombatScript extends Script {
    private ArrayList<Area> areas;
    Area bankArea = Banks.LUMBRIDGE_UPPER;
    Area frogArea = new Area(3196, 3177, 3203, 3171);
    Area chickenArea = new Area(3228, 3300, 3231, 3298);
    Area fightingArea = chickenArea;
    String monster = "Chicken";

    @Override
    public void onStart() throws InterruptedException {
        try {
            log("Bot started");
            log("Combat V3");
            log(getSkills().getStatic(Skill.ATTACK)+getSkills().getStatic(Skill.DEFENCE) + getSkills().getStatic(Skill.STRENGTH));
            if (getSkills().getStatic(Skill.ATTACK)+getSkills().getStatic(Skill.DEFENCE) + getSkills().getStatic(Skill.STRENGTH)<30) {
                log("Training at chickens");
                monster = "Chicken";
                fightingArea = chickenArea;
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
            if (monster.equals("Chicken") && (getSkills().getStatic(Skill.ATTACK)+getSkills().getStatic(Skill.DEFENCE) + getSkills().getStatic(Skill.STRENGTH)>=30)) {
                monster = "Giant frog";
                log("Now training at frogs");
                fightingArea = frogArea;
                walkToArea(fightingArea);
            }
            if (getSkills().getStatic(Skill.ATTACK)==5 && getInventory().contains("Steel scimitar")) {
                getInventory().getItem("Steel scimitar").interact("Wield");
            } else if (getSkills().getStatic(Skill.ATTACK)==10 && getInventory().contains("Black scimitar")) {
                getInventory().getItem("Black scimitar").interact("Wield");
            } else if (getSkills().getStatic(Skill.ATTACK)==20 && getInventory().contains("Mithril scimitar")) {
                getInventory().getItem("Mithril scimitar").interact("Wield");
            } else if (getSkills().getStatic(Skill.ATTACK)==30 && getInventory().contains("Adamant scimitar")) {
                getInventory().getItem("Adamant scimitar").interact("Wield");
            } if (getConfigs().get(43)==0 && getSkills().getStatic(Skill.ATTACK)==10 && getSkills().getStatic(Skill.STRENGTH)<10) {
                getTabs().open(Tab.ATTACK);
                sleep(600);
                getWidgets().get(593,9).hover();
                sleep(600);
                getMouse().click(false);
            } if (getConfigs().get(43)==1 && getSkills().getStatic(Skill.STRENGTH)==10 && getSkills().getStatic(Skill.DEFENCE)<10) {
                getTabs().open(Tab.ATTACK);
                sleep(600);
                getWidgets().get(593,16).hover();
                sleep(600);
                getMouse().click(false);
            } if (getConfigs().get(43)==3 && getSkills().getStatic(Skill.DEFENCE)==10) {
                getTabs().open(Tab.ATTACK);
                sleep(600);
                getWidgets().get(593,4).hover();
                sleep(600);
                getMouse().click(false);
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
        if (getHp() < 8) {
            heal();
        }

        if (!isReadyToAttack()) {
            return;
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
        walkToArea(frogArea);
        sleep(random(500,2000));
    }

    private void walkToArea(Area area) {
        log("Walking to area.");
        getWalking().webWalk(area);
    }




}