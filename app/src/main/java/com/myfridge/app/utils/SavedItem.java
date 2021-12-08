package com.myfridge.app.utils;

public class SavedItem {

    private String name;
    private String brand;
    private String photoURL;
    private String nutriscore;
    private String barCode;

    public SavedItem(){ }

    public SavedItem(String name, String brand, String photoURL, String nutriscore) {
        this.name = name;
        this.brand = brand;
        this.photoURL = photoURL;
        this.nutriscore = nutriscore;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getNutriscore() {
        return nutriscore;
    }

    public void setNutriscore(String nutriscore) {
        this.nutriscore = nutriscore;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

}
