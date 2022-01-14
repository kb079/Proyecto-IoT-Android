package com.myfridge.app.supermarkets; 
import java.util.List; 
public class Result{
    public String type;
    public String id;
    public double score;
    public double dist;
    public String info;
    public Poi poi;
    public Address address;
    public Position position;
    public Viewport viewport;
    public List<EntryPoint> entryPoints;
    public DataSources dataSources;

    @Override
    public String toString() {
        return "Result{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", score=" + score +
                ", dist=" + dist +
                ", info='" + info + '\'' +
                ", poi=" + poi +
                ", address=" + address +
                ", position=" + position +
                ", viewport=" + viewport +
                ", entryPoints=" + entryPoints +
                ", dataSources=" + dataSources +
                '}';
    }
}
