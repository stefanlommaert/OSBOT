package utils;

import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.ArrayList;
import java.util.Arrays;

import static org.osbot.rs07.script.MethodProvider.random;

public class InventoryManagement {

    private final MethodProvider bot;
    private final int[] DROP_PATTERN_1 = {0,1,2,3,7,6,5,4,8,9,10,11,15,14,13,12,16,17,18,19,23,22,21,20,24,25,26,27};

    public InventoryManagement(MethodProvider bot) {
        this.bot = bot;
    }

    public void dropAll(String itemName) {
        final Inventory inventory = bot.getInventory();
        ArrayList<Integer> itemSlots = new ArrayList<Integer>();
        for (int i = 0;i<28;i++) {
            if (inventory.getItemInSlot(i).getName().equals(itemName)) {
                itemSlots.add(i);
            }
        }
        bot.getKeyboard().pressKey(16);
        new ConditionalSleep(random(50,100)) {
            @Override
            public boolean condition() {
                return false;
            }
        }.sleep();
        for (int slot : DROP_PATTERN_1) {
            if (itemSlots.contains((slot))) {
                inventory.hover(slot);
                bot.mouse.click(false);
            }
        }
        new ConditionalSleep(random(50,100)) {
            @Override
            public boolean condition() {
                return false;
            }
        }.sleep();
        bot.getKeyboard().releaseKey(16);


    }
}
