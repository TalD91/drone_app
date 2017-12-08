package io.github.crazygoatstudio.mambodroid;



import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;
//import com.parrot.sdksample.R;
//import com.parrot.sdksample.discovery.DroneDiscoverer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class DeviceListActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE_SERVICE = "EXTRA_DEVICE_SERVICE";
    private static final String TAG = "DeviceListActivity";

    // BLE / Drone components
    public DroneDiscoverer mDroneDiscoverer;
    BluetoothAdapter BT_adapter = BluetoothAdapter.getDefaultAdapter();
    private ARDiscoveryServicesDevicesListUpdatedReceiver receiver;
   // private final List<ARDiscoveryDeviceService> mDronesList = new ArrayList<>();

    List<ARDiscoveryDeviceService> deviceList;

    // this block loads the native libraries
    // it is mandatory
    static {
        ARSDK.loadSDKLibs();
    }

    // GUI components
    private Button btn_on_off;
    private ListView drone_list;
    private Button refresh_btn;



    //runtime components
    private boolean stop_btn_clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT);
        setContentView(R.layout.activity_welcome);


        // init GUI components
        btn_on_off = (Button) findViewById(R.id.power_btn);
        drone_list = (ListView) findViewById(R.id.drone_discovered_list);


        BT_adapter = BluetoothAdapter.getDefaultAdapter();
        btn_on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Enable_disable_BT();
            }
        });
        final Vector<String> names = new Vector<>();

        final ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),  android.R.layout.simple_list_item_1, names);
        drone_list.setAdapter(adapter);
        mDroneDiscoverer = new DroneDiscoverer(getApplicationContext());
        mDroneDiscoverer.setup();


        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDroneDiscoverer.startDiscovering();
            }
        });



    }

    private void wait30sec () {
        long time = System.currentTimeMillis()+11000;
        while(System.currentTimeMillis() < time) {}
    }
    // Method to enable / disable BT
    private void Enable_disable_BT() {
        if (BT_adapter != null) {
            if (!BT_adapter.isEnabled()) {
                Intent IT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(IT);
                Toast t = Toast.makeText(getApplicationContext(),"Bluetooth Enabled!",Toast.LENGTH_LONG);
                t.show();
            }
            else {
                BT_adapter.disable();
                Toast t = Toast.makeText(getApplicationContext(),"Bluetooth Disabled!",Toast.LENGTH_LONG);
                t.show();
            }
        }
        else {
            Toast t = Toast.makeText(getApplicationContext(),"Bluetooth Adapted Not Present!",Toast.LENGTH_LONG);
            t.show();
        }
    }
}