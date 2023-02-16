//package random_scripts;
//
//import org.osbot.rs07.api.map.Area;
//import org.osbot.rs07.api.model.Entity;
//import org.osbot.rs07.event.InteractionEvent;
//import org.osbot.rs07.event.WebWalkEvent;
//import org.osbot.rs07.event.interaction.MouseMoveProfile;
//import org.osbot.rs07.script.Script;
//import org.osbot.rs07.script.ScriptManifest;
//import org.osbot.rs07.utility.ConditionalSleep;
//import utils.*;
//
//import javax.swing.*;
//import java.awt.*;
//import java.lang.reflect.InvocationTargetException;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.TimeUnit;
//
//@ScriptManifest(info = "", logo = "", version = 1, author = "Gandhalf", name = "BondBuyer")
//public class BondBuyer extends Script {
//    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
//    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
//    private BondBuyerGUI gui;
//
//
//
//    @Override
//    public void onStart() throws InterruptedException {
//        try {
//            log("Bot started");
//            this.gui = new BondBuyerGUI(this);
//            if (this.getParameters() != null && this.getParameters().length() > 0) {
//                Map<String, String> s = this.gui.loadSettings(this.getParameters());
//                if (Objects.nonNull(s) && !s.isEmpty()) {
//                    this.gui.startScript();
//                } else {
//                    this.gui.setVisible(true);
//                }
//            } else {
//                this.gui.setVisible(true);
//            }
//
//
//
//
//        } catch(Exception e) {
//            log("error at onStart()");
//            log(e);
//        }
//    }
//
//    public void onPaint(Graphics2D g){
//        CURSOR.paint(g);
//        TRAIL.paint(g);
//        Font font = new Font("Open Sans", Font.PLAIN, 16);
//        g.setFont(font);
//        g.setColor(Color.white);
//
//    }
//
//    @Override
//    public int onLoop() throws InterruptedException {
//        try {
//            return 600;
//
//        } catch(Exception e) {
//            log("ERROR");
//            log(e);
//            return 30000;
//        }
//
//    }
//
//    @Override
//    public void onExit() {
//        if (gui != null) {
//            gui.close();
//        }
//    }
//
//
//    private void sleep(int time1, int time2) {
//        new ConditionalSleep(random(time1, time2)) {
//            @Override
//            public boolean condition() {
//                return false;
//            }
//        }.sleep();
//    }
//
//    private boolean interactionEvent(Entity entity, String action) {
//        InteractionEvent ev = new InteractionEvent(entity, action);
//        ev.setOperateCamera(false);
//        ev.setWalkTo(true);
//        execute(ev);
//        return ev.hasFinished() && !ev.hasFailed();
//    }
//
//    private boolean webWalkEvent(Area area) {
//        WebWalkEvent ev = new WebWalkEvent(area);
//        ev.setMoveCameraDuringWalking(false);
//        ev.setEnergyThreshold(40);
//        execute(ev);
//        return ev.hasFinished() && !ev.hasFailed();
//    }
//
//
//
//}
