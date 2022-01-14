package com.myfridge.app.supermarkets; 
import java.util.List; 
public class Classification{
    public String code;
    public List<Name> names;

    @Override
    public String toString() {
        return "Classification{" +
                "code='" + code + '\'' +
                ", names=" + names +
                '}';
    }
}
