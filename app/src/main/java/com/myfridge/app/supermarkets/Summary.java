package com.myfridge.app.supermarkets; 
public class Summary{
    public String queryType;
    public int queryTime;
    public int numResults;
    public int offset;
    public int totalResults;
    public int fuzzyLevel;
    public GeoBias geoBias;

    @Override
    public String toString() {
        return "Summary{" +
                "queryType='" + queryType + '\'' +
                ", queryTime=" + queryTime +
                ", numResults=" + numResults +
                ", offset=" + offset +
                ", totalResults=" + totalResults +
                ", fuzzyLevel=" + fuzzyLevel +
                ", geoBias=" + geoBias +
                '}';
    }
}
