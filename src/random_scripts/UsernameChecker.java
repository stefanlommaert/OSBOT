package random_scripts;

import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.event.interaction.MouseMoveProfile;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.MouseCursor;
import utils.MouseTrail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@ScriptManifest(info = "", logo = "", version = 1, author = "Gandhalf", name = "Username Checker")
public class UsernameChecker extends Script {
    private final MouseTrail TRAIL = new MouseTrail(0, 255, 255, 2000, this);
    private final MouseCursor CURSOR = new MouseCursor(25, 2, Color.red, this);
    List<String> usernames = new ArrayList<String>();
    String dataDirectory = this.getDirectoryData() + File.separator + "UsernameChecker" + File.separator;


    @Override
    public void onStart() throws InterruptedException {
        try {
            File directory = new File(this.dataDirectory);
            System.out.println("checking directory structure");
            if (!directory.exists()) {
                directory.mkdirs();
                log("Made directory");
                File usernamesFile = new File(this.dataDirectory + "availableUsernames.txt");
                usernamesFile.createNewFile();
            }
            File usernamesFile = new File(this.dataDirectory + "usernames.txt");
            try (BufferedReader br = new BufferedReader(new FileReader(usernamesFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    usernames.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            log("Bot started");
            setMouseProfile();

        } catch(Exception e) {
            log("error at onStart()");
            log(e);
        }
    }

    public void onPaint(Graphics2D g){
        CURSOR.paint(g);
        TRAIL.paint(g);
        Font font = new Font("Open Sans", Font.BOLD, 25);
        g.setFont(font);
        g.setColor(Color.red);
        g.drawString("Username Checker", 10, 4+20);
        Font font2 = new Font("Open Sans", Font.PLAIN, 20);
        g.setFont(font2);
        g.setColor(Color.white);
        g.drawString("Made by: Gandhalf", 10, 30+20);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            RS2Widget displayName = getWidgets().get(558,9);
            if (displayName==null) {
                log("Could not get display name widget.");
                return 600;
            }
            displayName.interact();

            for (int i = 0; i < 25; i++) {
                getKeyboard().pressKey(8);
                sleep(50,60);
            }
            getKeyboard().releaseKey(8);
            String username = usernames.remove(0);
            if (username.contains("\u00A0")) {
                log("Has weird character");
                username = username.replace("\u00A0", " ");
            }
            log(username);
            Pattern pattern = Pattern.compile("[^\\x00-\\x7F ]");
            Matcher matcher = pattern.matcher(username);
            username = matcher.replaceAll("");
            log(username);
            getKeyboard().typeString(username,false);
            sleep(300,350);

            RS2Widget lookupName = getWidgets().get(558,18,0);
            if (lookupName==null) {
                log("Could not get lookup name widget.");
                return 600;
            }
            lookupName.interact();

            sleep(1800,2000);

            RS2Widget messageName = getWidgets().get(558,13);
            if (messageName==null) {
                log("Could not get message widget.");
                return 600;
            }
            String message = messageName.getMessage();
            if (message==null) {
                log("Could not get message widget context.");
                return 600;
            }
            if (message.contains("Great!")) {
                log("Name is available!");
                File directory = new File(this.dataDirectory + "availableUsernames.txt");
                FileWriter myWriter = new FileWriter(directory,true);
                myWriter.write(System.getProperty("line.separator") + username);
                myWriter.close();
            } else {
                log("Name is not available!");
            }




            return 600;
        } catch(Exception e) {
            log("ERROR");
            log(e);
            return 30000;
        }

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
        profile.setSpeedBaseTime(220); //185
        profile.setFlowSpeedModifier(1); //1.0
        profile.setDeviation(7); //7
        profile.setMinOvershootDistance(25); //25
        profile.setMinOvershootTime(375); //375
        profile.setNoise(2.4); //2.15
        profile.setOvershoots(2); //2
        getBot().setMouseMoveProfile(profile);
    }


}
