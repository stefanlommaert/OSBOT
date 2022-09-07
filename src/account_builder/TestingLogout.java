package account_builder;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.CustomBreakManager;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@ScriptManifest(info = "",logo = "", version = 1, author = "stefan3140", name = "testing logout")
public class TestingLogout extends Script {

    CustomBreakManager customBreakManager = new CustomBreakManager();
    @Override
    public void onStart() throws InterruptedException {
        try {
            customBreakManager.exchangeContext(getBot());
            getBot().getRandomExecutor().overrideOSBotRandom(customBreakManager);
        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            log("Loop main");
            log("logging out now in main");
            customBreakManager.startBreaking(TimeUnit.MINUTES.toMillis(1), true);
            log("I think i logged back in? main");
            return 1000;
        } catch(Exception e) {
            log("error");
            log(e);
            return 30000;
        }

    }
}
