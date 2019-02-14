package com.example.glowup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

// Import widgets
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


public class DeviceList extends AppCompatActivity {
    // Initialize widgets
    Button btnPaired;
    ListView devicelist;

    // Bluetooth variables
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize variables
        btnPaired = (Button)findViewById(R.id.button);
        devicelist = (ListView)findViewById(R.id.listView);

        // check whether device has bluetooth adapter and whether it's activated
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        }
        else if (!myBluetooth.isEnabled()){
            // Ask user to turn bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
    }

    private void pairedDevicesList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0){
            for(BluetoothDevice bt : pairedDevices) {
                // get device's name and address
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        // format bluetooth list to list widget
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3) {
            // get MAC address
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Intent to start next activity
            Intent i = new Intent(DeviceList.this, DrinkRecord.class);

            // Change activity
            i.putExtra(EXTRA_ADDRESS, address); // (name, value)
            startActivity(i);
        }
    };
}
