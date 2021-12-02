package com.myfridge.app.manager.fridge;

import java.util.ArrayList;

public class Fridge {

    private String name;
    private int temp;
    private boolean opened;
    private Location loc;

    private ArrayList<Item> items;

    public Fridge(){ }

    public Fridge(String name, int temp, boolean opened, Location loc){
        this.name = name;
        this.temp = temp;
        this.opened = opened;
        this.loc = loc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

}

