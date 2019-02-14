package com.example.glowup;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class DrinkRecord extends AppCompatActivity {

    // Initialize widgets
    Button shotButton;
    Button gulpButton;
    Button resetButton;
    TextView DrunkLevel;

    // Bluetooth
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    String address = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Drunk level tracker
    int drunkLvl = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_record);

        // receive address of bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        shotButton = (Button)findViewById(R.id.shot_button);
        gulpButton = (Button)findViewById(R.id.gulp_button);
        resetButton = (Button)findViewById(R.id.reset);
        DrunkLevel = (TextView)findViewById(R.id.drunk_lvl);

        // connect selected bluetooth
        new ConnectBT().execute();

        shotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                addDrunkLvl(30);
                sendToHeadband();
            }
        });

        gulpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                addDrunkLvl(10);
                sendToHeadband();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                drunkLvl = 0;
                DrunkLevel.setText(Integer.toString(drunkLvl));
                sendToHeadband();
            }
        });
    }

    private void addDrunkLvl(int addDrunk){
        drunkLvl += addDrunk;
        if (drunkLvl > 100) {
            drunkLvl = 100;
        }
        DrunkLevel.setText(Integer.toString(drunkLvl));
    }
    private void sendToHeadband(){
        if (btSocket != null){
            try {
                btSocket.getOutputStream().write(drunkLvl);
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    // Async class to start BT connection with paired devices
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(DrinkRecord.this, "Connecting ...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) //while progress dialog is shown, connection done in backgound
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    // Create RFCOMM (SPP) connection
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect(); // start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess){
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

}
