package utils;

import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;

public class AntiBotDetection {

    private final MethodProvider bot;
    private final String script;

    public AntiBotDetection(MethodProvider bot, String script) {
        this.bot = bot;
        this.script = script;
    }

    public void antiBan() throws InterruptedException {
        int i = MethodProvider.random(1,100);
        if (i < 15) {
            bot.log("Anti ban: moving camera");
            moveCamera();
        } else if (i<20) {
            bot.log("Anti ban: checking random window");
            checkRandomWindow();
        } else if (i<40) {
            bot.log("Anti ban: checking XP");
            checkXP();
        } else if (i<50) {
            bot.log("Anti ban: checking online friends");
            checkFriends();
        } else if (i<100) {
            bot.log("Anti ban: moving mouse out of screen");
            moveMouseOutOfScreen();
        }
    }


    public void moveCamera() {
        int pitch = bot.camera.getPitchAngle();
        int yaw = bot.camera.getYawAngle();
        if (MethodProvider.random(1,100)<50) {
            pitch += MethodProvider.random(3,7);
        } else {
            pitch -=MethodProvider.random(3,7);
        }
        if (MethodProvider.random(1,100)<50) {
            yaw += MethodProvider.random(3,7);
        } else {
            yaw -=MethodProvider.random(3,7);
        }
        bot.camera.movePitch(pitch);
        bot.camera.moveYaw(yaw);
    }

    public void checkFriends() throws InterruptedException {
        bot.getTabs().open(Tab.FRIENDS);
        MethodProvider.sleep(MethodProvider.random(2000,4000));
        bot.getTabs().open(Tab.INVENTORY);
    }

    public void checkXP() throws InterruptedException {
        bot.getTabs().open(Tab.SKILLS);
        int id = 17;
        switch (script) {
            case "mining":
                id = 17;
                break;
            case "fishing":
                id = 19;
                break;
            case "woodcutting":
                id = 22;
                break;
        }
        RS2Widget xpIcon = bot.getWidgets().get(320, id);
        if (xpIcon!=null) {
            xpIcon.hover();
            MethodProvider.sleep(MethodProvider.random(1000,3000));
            bot.getTabs().open(Tab.INVENTORY);
        }
    }

    public void checkRandomWindow() throws InterruptedException {
        int i = MethodProvider.random(1,100);
        if (i < 20) {
            bot.getTabs().open(Tab.MAGIC);
            MethodProvider.sleep(MethodProvider.random(2000,4000));
            bot.getTabs().open(Tab.INVENTORY);
        } else if (i<40) {
            bot.getTabs().open(Tab.PRAYER);
            MethodProvider.sleep(MethodProvider.random(2000,4000));
            bot.getTabs().open(Tab.INVENTORY);
        } else if (i<60) {
            bot.getTabs().open(Tab.ATTACK);
            MethodProvider.sleep(MethodProvider.random(2000,4000));
            bot.getTabs().open(Tab.INVENTORY);
        } else if (i<80) {
            bot.getTabs().open(Tab.CLANCHAT);
            MethodProvider.sleep(MethodProvider.random(2000,4000));
            bot.getTabs().open(Tab.INVENTORY);
        } else if (i<100) {
            bot.getTabs().open(Tab.MUSIC);
            MethodProvider.sleep(MethodProvider.random(2000,4000));
            bot.getTabs().open(Tab.INVENTORY);
        }
    }

    public void moveMouseOutOfScreen() {
        bot.getMouse().moveOutsideScreen();
    }

}
