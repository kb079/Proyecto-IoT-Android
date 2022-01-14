package com.myfridge.app.manager.fridge;

public class Location {

    private double lat;
    private double longg;

    public Location(){ }

    public Location(double lat, double longg){
        this.lat = lat;
        this.longg = longg;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(long lat) {
        this.lat = lat;
    }

    public double getLongg() {
        return longg;
    }

    public void setLongg(long longg) {
        this.longg = longg;
    }

}