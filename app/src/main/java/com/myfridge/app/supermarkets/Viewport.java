package com.myfridge.app.supermarkets; 
public class Viewport{
    public TopLeftPoint topLeftPoint;
    public BtmRightPoint btmRightPoint;

    @Override
    public String toString() {
        return "Viewport{" +
                "topLeftPoint=" + topLeftPoint +
                ", btmRightPoint=" + btmRightPoint +
                '}';
    }
}
