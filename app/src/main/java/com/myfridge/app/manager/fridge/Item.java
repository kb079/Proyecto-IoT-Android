package com.myfridge.app.manager.fridge;

import java.io.Serializable;

public class Item implements Serializable {

    private String barCode;
    private int qty;
    private long expDate;

    public Item(){}

    public Item(String code, int q, long date){
        this.barCode = code;
        this.qty = q;
        this.expDate = date;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public long getExpDate() {
        return expDate;
    }

    public void setExpDate(long expDate) {
        this.expDate = expDate;
    }

}
