package com.example.portablegateway;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.portablegateway.databinding.FragmentDashboardBinding;
import com.example.portablegateway.databinding.FragmentHomeBinding;
import com.example.portablegateway.databinding.FragmentNotificationsBinding;
import com.example.portablegateway.ui.dashboard.DashboardFragment;
import com.example.portablegateway.ui.dashboard.DashboardViewModel;
import com.example.portablegateway.ui.notifications.NotificationsViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portablegateway.databinding.ActivityMainBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;
    private final static String TAG = MainActivity.class.getSimpleName();
    private RecyclerView scannedDevices,connectedDevices,dataReceived;
    private View homeView, dashboardView, notificationView;

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    BluetoothGatt btGatt;
    BluetoothGattCharacteristic btGattCharSuhu, btGattCharHum, btGattCharPh, btGattCharWrite;
    BluetoothSocket btSocket;
    boolean scanning;

    List <BluetoothDevice> btRemoteDevice;
    List <String> cacheSuhu, cacheHum, cachePh;
    List <BluetoothDevice> bluetoothDevice;
    List <String> bluetoothDeviceConnected, bluetoothDeviceScanned, bluetoothDataReceived;

    private static final UUID myUUIDServ = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID readSuhu = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID readHum = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID readPh = UUID.fromString("6E400004-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID myUUIDWrite = UUID.fromString("6E400005-B5A3-F393-E0A9-E50E24DCCA9E");

    MyRecyclerViewAdapter scannedAdapter, connectedAdapter, receievedAdapter;
    boolean scanStopped,cacheEmpty, initRv, connected;
    DashboardViewModel dashboardViewModel;
    NotificationsViewModel notificationsViewModel;

    int insertIndex;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private ScanCallback leScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType,result);
            if(!bluetoothDevice.contains(result.getDevice())) {

                //Log.i(TAG, "Address"+result.getDevice().getAddress()+","+result.getDevice().fetchUuidsWithSdp());
                //Log.i(TAG, ""+result.getRssi());
                bluetoothDevice.add(bluetoothDevice.size(),result.getDevice());
                String item = "Address: "+result.getDevice().getAddress();
                item+="("+result.getDevice().getName()+")";
                bluetoothDeviceScanned.add(item);
                dashboardViewModel.setListBluetoothDevice(bluetoothDevice);
                dashboardViewModel.setListScanned(bluetoothDeviceScanned);
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        checkConnection();
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void init(){
        initArray();
        initBluetooth();
    }

    public void initArray(){
        scanning=false;
        bluetoothDeviceScanned = new ArrayList<>();
        bluetoothDeviceConnected = new ArrayList<>();
        List <List<String>> toSend = new ArrayList<>();
        List <List<String>> cache = new ArrayList<>();
        List <String> dataReceived = new ArrayList<>();
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        dashboardViewModel.setListScanned(bluetoothDeviceScanned);
        dashboardViewModel.setListConnected(bluetoothDeviceConnected);
        dashboardViewModel.setToSend(toSend);
        dashboardViewModel.setCache(cache);
        dashboardViewModel.setCacheEmpty(true);
        dashboardViewModel.setMainActivity(this);
        notificationsViewModel.setDataReceived(dataReceived);
    }
    public void startScanning(View view){
        Button scanButton = findViewById(R.id.startScan);
        scanButton.setText((scanning)? "Start Scan":"Scanning...");
        String myColour = (scanning)? "#9AD4FF":"#FF7161";
        scanButton.setBackgroundColor(Color.parseColor(myColour));
        scanning= !(scanning);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if(scanning){
                    btScanner.startScan(leScanCallback);
                }
                else{
                    btScanner.stopScan(leScanCallback);
                }
            }
        });
    }

    public void sendingData(List<String> data, String status) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(currentTime);
        status = strDate+"("+status+")";
        DatabaseReference myRef = database.getReference(status);
        Log.i(TAG,"Sending Data: "+ data);
        myRef.setValue(data);

        //   Firebsae.setAndroidContext(this);
        // mRef = new Firebase("https://my-projects-dcffa.firebaseio.com/");
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initBluetooth(){
        bluetoothDevice = new ArrayList<>();
        btRemoteDevice = new ArrayList<>();
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();


        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }
    public boolean checkConnection(){
        LinearLayout internetStatus = findViewById(R.id.linearLayout2);
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            if(internetStatus!=null){
                internetStatus.setBackgroundColor(Color.parseColor("#ADFF2F"));
            }

            //cachedButton.setVisibility(View.INVISIBLE);
        }
        else{
            if(internetStatus!=null){
                internetStatus.setBackgroundColor(Color.parseColor("#FF7161"));
            }
            connected = false;
            //cachedButton.setVisibility(View.VISIBLE);
        }
        String tmp = (connected)? "Connected to the Internet": "Disconnected to the Internet";

        return connected;
    }


}


