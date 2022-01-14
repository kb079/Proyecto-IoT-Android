package com.myfridge.app.manager.fridge;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ItemViewModel extends ViewModel {

    MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

    public void setText(String s){
        mutableLiveData.setValue(s);
    }

    public MutableLiveData<String> getText(){
        return mutableLiveData;
    }
}
