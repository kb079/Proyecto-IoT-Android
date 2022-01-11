package com.myfridge.app.supermarkets; 
import java.util.List; 
public class Poi{
    public String name;
    public List<Brand> brands;
    public List<CategorySet> categorySet;
    public List<String> categories;
    public List<Classification> classifications;
    public String url;
    public String phone;

    @Override
    public String toString() {
        return "Poi{" +
                "name='" + name + '\'' +
                ", brands=" + brands +
                ", categorySet=" + categorySet +
                ", categories=" + categories +
                ", classifications=" + classifications +
                ", url='" + url + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
