package com.example.portablegateway.ui.dashboard;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portablegateway.MainActivity;
import com.example.portablegateway.MyRecyclerViewAdapter;
import com.example.portablegateway.R;
import com.example.portablegateway.databinding.FragmentDashboardBinding;
import com.example.portablegateway.ui.notifications.NotificationsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DashboardFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener{

    private DashboardViewModel dashboardViewModel;
    private NotificationsViewModel notificationsViewModel;
    private FragmentDashboardBinding binding;
    private Context myContext;
    BluetoothGatt btGatt;
    List <BluetoothDevice> bluetoothDevice;
    boolean switchStatus;
    private boolean connectedSelected;
    boolean isChecked;
    private int pos;
    View myRoot;

    private static final UUID ccc = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    BluetoothGattCharacteristic btGattCharSuhu, btGattCharHum, btGattCharPh, btGattCharWrite;

    private static final UUID myUUIDServ = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID readSuhu = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID readHum = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID readPh = UUID.fromString("6E400004-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID myUUIDWrite = UUID.fromString("6E400005-B5A3-F393-E0A9-E50E24DCCA9E");

    String TAG = DashboardFragment.class.getSimpleName();

    Switch mySwitch;

    List<String> bluetoothDeviceConnected, dataReceived, bluetoothDeviceScanned, bluetoothDataReceived;
    MyRecyclerViewAdapter scannedAdapter, connectedAdapter, receievedAdapter;
    private RecyclerView scannedDevices,connectedDevices,dataReceivedRv;
    Activity mainActivity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        myRoot= root;
        myContext = root.getContext();
        final TextView textView = binding.textDashboard;
        bluetoothDeviceScanned = dashboardViewModel.getListScanned();
        bluetoothDeviceConnected = dashboardViewModel.getListConnected();
        bluetoothDevice = dashboardViewModel.getBluetoothDevice();
        dataReceived = notificationsViewModel.getDataReceived();
        switchStatus = dashboardViewModel.getSwitch();
        mySwitch = myRoot.findViewById(R.id.switchAllow);
        mySwitch.setChecked(switchStatus);


        init(root);
        initClicker(root);
        return root;
    }
    public void initClicker(View view){
        Button bt = view.findViewById(R.id.connectDevices);
        RecyclerView rv = view.findViewById(R.id.connectedDevices);
        bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                connectDeviceLe(bluetoothDevice.get(pos));

                //if(connectedSelected){
                //    if(bluetoothDeviceConnected!=null) {
                //        bluetoothDeviceScanned.add(bluetoothDeviceScanned.size(), bluetoothDeviceConnected.remove(pos));
                //        connectedAdapter.notifyItemRemoved(pos);
                //        scannedAdapter.notifyItemInserted(bluetoothDeviceConnected.size());
                //    }
                // }
                //else{
                //   if(bluetoothDeviceScanned!=null){
                //       bluetoothDeviceConnected.add(bluetoothDeviceConnected.size(),bluetoothDeviceScanned.get(pos));
                //       connectedAdapter.notifyItemInserted(bluetoothDeviceConnected.size());
                //       connectDeviceLe(bluetoothDevice.get(pos));
                //   }
                //}
            }
        });
        Switch mySwitch = myRoot.findViewById(R.id.switchAllow);
        mySwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i("Switch",""+mySwitch.isChecked());
                dashboardViewModel.setSwitch(mySwitch.isChecked());
                isChecked=mySwitch.isChecked();
            }
        });
    }

    public void init(View view){

        scannedDevices = view.findViewById(R.id.scannedDevices);
        connectedDevices = view.findViewById(R.id.connectedDevices);

        scannedDevices.setLayoutManager(new LinearLayoutManager(getActivity()));
        connectedDevices.setLayoutManager(new LinearLayoutManager(getActivity()));

        scannedAdapter = new MyRecyclerViewAdapter(view.getContext(), bluetoothDeviceScanned);
        connectedAdapter =new MyRecyclerViewAdapter(view.getContext(), bluetoothDeviceConnected);

        scannedAdapter.setClickListener(this);
        connectedAdapter.setClickListener(this);

        scannedDevices.setAdapter(scannedAdapter);
        connectedDevices.setAdapter(connectedAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(View view, int position) {
        View m = (View) view.getParent();
        if(view.getSolidColor()==Color.parseColor("#E7E7E7")){
            view.setBackgroundColor(Color.parseColor("#EFEFEF"));
        }
        else{
            view.setBackgroundColor(Color.parseColor("#E7E7E7"));
        }
        pos=position;
        switch(m.getId()){
            case R.id.connectedDevices:
                connectedSelected=true;
                break;
            case R.id.scannedDevices:
                connectedSelected=false;
                break;
        }
        bluetoothDeviceConnected.clear();
        connectedAdapter.notifyItemRemoved(0);
        BluetoothDevice dev = bluetoothDevice.get(pos);
        String name = "Device Name: " + dev.getName()+"\n\n";
        String address = "Address: "+ dev.getAddress()+"\n\n";
        String bond = "Bond state: "+ dev.getBondState()+"\n\n";

        bluetoothDeviceConnected.add(name+address+bond);
    }

    public void connectDeviceLe(BluetoothDevice dev){
        dev.connectGatt(myContext, false, btGattCallback);
    }
    private BluetoothGattCallback btGattCallback = 	new BluetoothGattCallback(){
        List<BluetoothGattCharacteristic> chars = new ArrayList<>();

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
            String devAddress = gatt.getDevice().getAddress();
            //TextView statusText = (TextView) findViewById(R.id.statusConnection);

            if(status==BluetoothGatt.GATT_SUCCESS){
                if (newState == BluetoothProfile.STATE_CONNECTED){
                    gatt.discoverServices();
                    String stats= "Successfully connected to: "+devAddress;
                    Button bt = myRoot.findViewById(R.id.connectDevices);
                    bt.setText("Connected");
                    //statusText.setText(stats);
                    Log.i(TAG, stats);
                }
                else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    String stats= "Successfully disconnected from: " +devAddress;
                    //statusText.setText(stats);
                    Button bt = myRoot.findViewById(R.id.connectDevices);
                    bt.setText("Disconnected");
                    Log.i(TAG, stats);
                    gatt.close();
                }
            }
            else {
                String stats= "Error"+status +"encountered for: "+devAddress+" Disconnecting...";
                //statusText.setText(stats);
                Button bt = myRoot.findViewById(R.id.connectDevices);
                bt.setText("Error, (Try Again)");
                Log.i(TAG, stats);
                gatt.close();
            }
        }


        public void updateData(List<String> data, String status){
            String item="";
            for (int i = 0; i<data.size(); i++){
                item= item+data.get(i)+"||";
            }
            item+=" ("+ status+")";
            Log.i("Data Recv", item);
            dataReceived.add(item);
            notificationsViewModel.setDataReceived(dataReceived);
        }
        public void addToCache(List<String> data){
            List<List<String>> tmp = dashboardViewModel.getCache();
            tmp.add(data);
            updateData(data, "Cached");
            dashboardViewModel.setCache(tmp);
            dashboardViewModel.setCacheEmpty(false);
        }
        public void uploadCache(List <String> data) throws InterruptedException {

            List<List<String>> cached = dashboardViewModel.getCache();
            cached.add(data);
            while(!cached.isEmpty()){
                List<String> tmp = cached.remove(0);
                ((MainActivity)dashboardViewModel.getMainActivity()).sendingData(tmp, "Cached");
                Thread.sleep(500);
            }
            dashboardViewModel.setCacheEmpty(true);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            btGatt.readCharacteristic(characteristic);
            //String print2 = Base64.getEncoder().encodeToString(btGattCharRead.getValue());

            //TextView dataText = (TextView) findViewById(R.id.dataReceived);
            String suhu ="0", hum = "0", ph = "0";
            if(btGattCharSuhu.getValue()!=null){
                suhu = String.valueOf((short) (btGattCharSuhu.getValue()[0] & 0xff));
            }
            if(btGattCharHum.getValue()!=null) {
                hum = String.valueOf((short) (btGattCharHum.getValue()[0] & 0xff));
            }
            if(btGattCharPh.getValue()!=null) {
                ph = String.valueOf((short) (btGattCharPh.getValue()[0] & 0xff));
            }
            List <List<String>> toSend = dashboardViewModel.getTSend();
            List<String> tmp = new ArrayList<>();
            tmp.add(suhu);
            tmp.add(hum);
            tmp.add(ph);
            toSend.add(tmp);
            dashboardViewModel.setToSend(toSend);

            //Log.i(TAG, "Suhu: "+ String.valueOf(suhu));
            //Log.i(TAG,"Humidity: "+String.valueOf(hum));
            //Log.i(TAG,"Ph: "+String.valueOf(ph));

            //Log.i("Switch Status", ""+ switchStatus);
            Log.i("Check Status", ""+isChecked);
            if(isChecked){
                if(((MainActivity)dashboardViewModel.getMainActivity()).checkConnection()){
                    //send here
                    if(dashboardViewModel.isCacheEmpty()){
                        ((MainActivity)dashboardViewModel.getMainActivity()).sendingData(tmp, "Real Time");
                        updateData(tmp, "Real Time");
                        Log.i("Switch active", "Connected and sending data");
                    }
                    else{
                        try {
                            uploadCache(tmp);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    //cache here
                    addToCache(tmp);
                    Log.i("Switch active", "not Connected");
                }
            }
            else{
                //cache here
                addToCache(tmp);
                Log.i("Switch not active", "??? Connected");
            }
        }

        public void onServicesDiscovered (BluetoothGatt gatt, int status){
            List <BluetoothGattService> services = gatt.getServices();
            gatt.requestMtu(1024);
            if (services.isEmpty()){
                Log.i(TAG, "Empty Services");
            }
            else{
                BluetoothGattService serv = gatt.getService(myUUIDServ);
                BluetoothGattCharacteristic gattChar = serv.getCharacteristic(readSuhu);
                //Log.i(TAG, "Here the data is: "+gattChar.getStringValue(1052));
                btGatt = gatt;

                btGattCharSuhu =serv.getCharacteristic(readSuhu);
                //btGatt.writeCharacteristic(btGattCharSuhu);
                //btGatt.setCharacteristicNotification(btGattCharSuhu, true);

                btGattCharHum =serv.getCharacteristic(readHum);
                //btGatt.writeCharacteristic(btGattCharHum);
                //btGatt.setCharacteristicNotification(btGattCharHum, true);

                btGattCharPh =serv.getCharacteristic(readPh);
                //btGatt.writeCharacteristic(btGattCharPh);
                //btGatt.setCharacteristicNotification(btGattCharPh, true);

                chars.add(btGattCharSuhu);
                chars.add(btGattCharHum);
                chars.add(btGattCharPh);
                charsSubscribe(btGatt);

                btGattCharWrite= serv.getCharacteristic(myUUIDWrite);



                //Log.i("BLE", "Descriptor is " + descPh); // this is not null
            }
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i("DESCRIPTOR", "WROTE DESCRIPTOR FOR CHARACTERISTIC");
            super.onDescriptorWrite(gatt, descriptor, status);
            chars.remove(0);
            charsSubscribe(gatt);
        }

        public void charsSubscribe(BluetoothGatt gatt){
            if(chars.size() == 0) return;
            BluetoothGattCharacteristic characteristic = chars.get(0);
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            if(descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
    };

}