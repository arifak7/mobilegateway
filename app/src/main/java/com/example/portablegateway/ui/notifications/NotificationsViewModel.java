package com.example.portablegateway.ui.notifications;

import android.app.Activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class NotificationsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private static List<String> dataReceived;

    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
    }

    public void setDataReceived(List <String> dataReceived) {
        this.dataReceived = dataReceived;
    }
    public List <String> getDataReceived(){
        return this.dataReceived;
    }

    public LiveData<String> getText() {
        return mText;
    }
}