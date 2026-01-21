package com.michael.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.location.LocationManagerCompat;

import com.google.android.gms.nearby.Nearby;

public class ClientReceiver extends BroadcastReceiver
{
    private Activity activity;
    private LocationManager locationManager;

    private Button btnStartDiscovery;
    private Button btnStopDiscovery;
    private TextView txv;
    private boolean isBluetoothOn;
    private boolean isLocationOn;

    public ClientReceiver(Activity activity)
    {
        this.activity = activity;
        btnStartDiscovery = activity.findViewById(R.id.btnStartDiscoveryID);
        btnStopDiscovery = activity.findViewById(R.id.btnStopDiscoveryID);
        txv = activity.findViewById(R.id.txvClientID);
        btnStopDiscovery.setEnabled(false);
        isBluetoothOn = true;
        isLocationOn = true;
        checkBluetoothAndLocationStatus();
    }

    private void checkBluetoothAndLocationStatus()
    {
        BluetoothManager bluetoothManager = activity.getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        locationManager = activity.getSystemService(LocationManager.class);
        isBluetoothOn = bluetoothAdapter.isEnabled();
        isLocationOn = LocationManagerCompat.isLocationEnabled(locationManager);
        showIndication();
    }

    private void showIndication()
    {
        if(!isBluetoothOn && !isLocationOn)
        {
            txv.setText("for beggin to discovery you need to turn on Bluetooth and GPS.\n");
            isBluetoothOn = false;
            btnStartDiscovery.setEnabled(false);
        }
        else if (!isBluetoothOn)
        {
            txv.append("for beggin to discovery you need to turn on Bluetooth\n");
            isLocationOn = false;
            btnStartDiscovery.setEnabled(false);
        }
        else if(!isLocationOn)
        {
            txv.append("for beggin to discovery you need to turn on GPS\n");
            isLocationOn = false;
            btnStartDiscovery.setEnabled(false);
        }
    }

    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
        {
            switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR))
            {
                case BluetoothAdapter.STATE_TURNING_ON:
                case BluetoothAdapter.STATE_ON:
                    isBluetoothOn = true;
                    if(isLocationOn)
                        btnStartDiscovery.setEnabled(true);
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    txv.setText("for beggin to discovery you need to turn on Bluetooth\n");
                    isBluetoothOn = false;
                    setDiscoveryBtnsOff();
                    break;
            }
            return;
        }
        else if (intent.getAction().equals(LocationManager.MODE_CHANGED_ACTION))
        {
            if (LocationManagerCompat.isLocationEnabled(locationManager))
            {
                isLocationOn = true;
                if(isBluetoothOn)
                    btnStartDiscovery.setEnabled(true);
            }
            else
            {
                txv.setText("for beggin to discovery you need to turn on GPS\n");
                isLocationOn = false;
                setDiscoveryBtnsOff();
            }
        }
    }

    private void setDiscoveryBtnsOff()
    {
        Nearby.getConnectionsClient(activity).stopDiscovery();
        btnStartDiscovery.setEnabled(false);
        btnStopDiscovery.setEnabled(false);
    }
}
