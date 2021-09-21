package com.example.portablegateway.ui.dashboard;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.ViewModel;

import java.util.List;

public class DashboardViewModel extends ViewModel {

    private static List<String> listScanned,listConnected;
    private static List <BluetoothDevice> bluetoothDevice;
    private static boolean switchStatus, cacheEmpty;
    private static List <List<String>> toView, cache;
    private static Activity mainActivity;

    public DashboardViewModel() {

    }

    public Activity getMainActivity() {
        return mainActivity;
    }
    public List<String> getListScanned(){
        return listScanned;
    }
    public List <BluetoothDevice> getBluetoothDevice(){
        return bluetoothDevice;
    }
    public List<String> getListConnected(){
        return listConnected;
    }
    public boolean getSwitch(){
        return switchStatus;
    }
    public boolean isCacheEmpty(){
        return cacheEmpty;
    }
    public List<List<String>> getTSend(){
        return toView;
    }
    public List<List<String>> getCache(){
        return cache;
    }

    public void setSwitch(boolean data){
        switchStatus =data;
    }
    public void setMainActivity(Activity activity){
        this.mainActivity=activity;
    }
    public void setCacheEmpty(boolean data){
        cacheEmpty =data;
    }
    public void setListBluetoothDevice(List <BluetoothDevice> bt){
        bluetoothDevice = bt;
    }
    public void setListScanned(List<String> data){
        listScanned =data;
    }
    public void setToSend(List<List<String>> data){
        toView =data;
    }
    public void setCache(List<List<String>> data){
        cache =data;
    }
    public void setListConnected(List<String> data){
        listConnected =data;
    }
}