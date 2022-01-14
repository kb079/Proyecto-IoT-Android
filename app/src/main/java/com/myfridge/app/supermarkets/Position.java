package com.myfridge.app.supermarkets; 
public class Position{
    public double lat;
    public double lon;

    @Override
    public String toString() {
        return "Position{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
