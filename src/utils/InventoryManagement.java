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
        for (int slot : itemSlots) {
            inventory.hover(slot);
            bot.mouse.click(false);
            new ConditionalSleep(random(5,10)) {
                @Override
                public boolean condition() {
                    return false;
                }
            }.sleep();
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
