package com.myfridge.app.supermarkets; 
import java.util.List; 
public class Root{
    public Summary summary;
    public List<Result> results;

    @Override
    public String toString() {
        return "Root{" +
                "summary=" + summary +
                ", results=" + results +
                '}';
    }
}
