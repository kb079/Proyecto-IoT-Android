package com.example.proyectoapp.manager.fridge;


import com.example.proyectoapp.Location;

public class Fridge {

    private String name;
    private int temp;
    private boolean opened;
    private Location loc;

    public String getName() {
        return name;
    }

    public Fridge(String name, int temp, boolean opened, Location loc){
        this.name = name;
        this.temp = temp;
        this.opened = opened;
        this.loc = loc;
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


}

