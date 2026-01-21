package com.michael.bluetooth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.SimpleArrayMap;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AppCompatActivity
{
    private final Strategy STRATEGY = Strategy.P2P_STAR;

    private ClientReceiver receiver;
    private TextView clientLog;
    private EditText editText;
    private Button btnStartDiscovery;
    private Button btnStopDiscovery;
    private Button btnSendClient;
    private Button btnDisconnectFromMaster;
    private ListView listView;
    private ArrayAdapter arrayAdapter;
    private List<MasterDevice> allMasters;
    private MasterDevice myMaster;
    private ClientConnection clientConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        registerToReciver();
        findComponents();
        init();
    }

    private void registerToReciver()
    {
        receiver = new ClientReceiver(this);
        IntentFilter filter = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    private void findComponents()
    {
        listView = findViewById(R.id.listViewClientID);
        clientLog = findViewById(R.id.txvClientID);
        btnStartDiscovery = findViewById(R.id.btnStartDiscoveryID);
        btnStopDiscovery = findViewById(R.id.btnStopDiscoveryID);
        btnSendClient = findViewById(R.id.btnSendClientID);
        editText = findViewById(R.id.editTextClientID);
        btnDisconnectFromMaster = findViewById(R.id.btnDisconnectClientID);
        btnDisconnectFromMaster.setVisibility(View.INVISIBLE);
    }

    private void init()
    {
        btnStopDiscovery.setEnabled(false);
        editText.setEnabled(false);
        btnDisconnectFromMaster.setEnabled(false);
        btnSendClient.setEnabled(false);
        btnSendClient.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String msg = editText.getText().toString();
                editText.setText("");
                clientConnection.sendMsg(myMaster.getId(), msg);
            }
        });
        clientLog.setMovementMethod(new ScrollingMovementMethod());
        allMasters = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, allMasters);
        listView.setAdapter(arrayAdapter);
    }

    // פונקציה שמתחילה לחפש מאסטרים בסביבה
    public void startDiscovery(View view)
    {
        askPermisiions();
        btnStartDiscovery.setEnabled(false);
        btnStopDiscovery.setEnabled(true);
        String myAppName = getPackageName();
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(this)
                .startDiscovery(myAppName, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            clientLog.setText("started to Discovery.\n");
                            setListenerOnListView();
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            clientLog.setText("We're unable to start discovering.\nExeption: " + e.toString());
                            btnStartDiscovery.setEnabled(true);
                            btnStopDiscovery.setEnabled(false);
                        });
    }

    // הפסקת החיפוש של המאסטרים
    public void stopDiscovery(View view)
    {
        clientLog.setText("You stoped to discovery!\nFor continue to search masters tap on start discovery.\n");
        Nearby.getConnectionsClient(this).stopDiscovery();

        btnStopDiscovery.setEnabled(false);
        btnStartDiscovery.setEnabled(true);
        for(MasterDevice m : allMasters)
            allMasters.remove(m);
        listView.invalidateViews();
    }

    private void setListenerOnListView()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
            {
                Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
                myMaster = new MasterDevice((MasterDevice) adapter.getItemAtPosition(position));
                connectToMaster();
                for(MasterDevice m : allMasters)
                    allMasters.remove(m);
                allMasters.add(myMaster);
                listView.setOnItemClickListener(null);
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }

    // נקראת כשיש עדכון לגביי מסטר שנמא בסביבה
    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback()
    {
        // נקראת כשמצאנו מאסטר בסביבה
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info)
        {
            logit("\nEndpoint Found!\n");
            allMasters.add(new MasterDevice(endpointId, info.getEndpointName(), true));
            listView.invalidateViews();
        }

        // נקראת אם איבדנו מאסטר שהיה בסביבה
        @Override
        public void onEndpointLost(String endpointId)
        {
            MasterDevice m = searchMasterById(endpointId);
            logit("\nonEndpoinLost:" + m.toString() + "\n");
            allMasters.remove(m);
            listView.invalidateViews();
        }
    };

    private MasterDevice searchMasterById(String id)
    {
        for(MasterDevice master : allMasters)
            if(master.getId().equals(id))
                return master;
        return null;
    }

    // בקשת חיבור למאסטר
    private void connectToMaster()
    {
        Nearby.getConnectionsClient(this)
                .requestConnection(myMaster.getName(), myMaster.getId(), connectionLifecycleCallback)
                .addOnSuccessListener(
                        (Void unused) -> {
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            logit("\nNearby Connections failed to request the connection.\nExeption: " + e.toString());
                        });
    }

    // אחרי ששלחנו בקשה להתחבר למאסטר נגיע לפה
    ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback()
    {
        private AlertDialog alert;

        // מציג הודעה האם לאשר את החיבור
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo info)
        {
            clientConnection = new ClientConnection(ClientActivity.this);
            AlertDialog.Builder builder = alertDialogForConnection(endpointId, info);
            alert = builder.create();
            alert.show();
        }

        // פונקציה שבודקת מה הסטטוס של האישור בין המאסטר לקליינט
        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution connectionResolution)
        {
            switch (connectionResolution.getStatus().getStatusCode())
            {
                case ConnectionsStatusCodes.STATUS_OK:
                    clientLog.setText("We connected to Master!\nnow we can start sending and receiving data...\n\n");
                    btnStartDiscovery.setVisibility(View.INVISIBLE);
                    btnStopDiscovery.setVisibility(View.INVISIBLE);
                    btnDisconnectFromMaster.setVisibility(View.VISIBLE);
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                case ConnectionsStatusCodes.STATUS_ERROR:
                    clientLog.setText("The connection was lost.\ntap on start discovery fo find masters.");
                    alert.cancel();
                    disconnect();
            }
        }

        // פונקציה שבודקת אם יש התנתקות
        @Override
        public void onDisconnected(String endpointId)
        {
            clientLog.setText("your Master Disconnected from you.\ntap on start discovery for look another Master.");
            disconnect();
        }
    };

    private AlertDialog.Builder alertDialogForConnection(String endpointId, ConnectionInfo info)
    {
        return new AlertDialog.Builder(this)
                .setTitle("Accept connection to " + info.getEndpointName())
                .setMessage("Confirm the code matches on both devices: " + info.getAuthenticationDigits())
                .setPositiveButton("Accept", (DialogInterface dialog, int which) ->
                        Nearby.getConnectionsClient(this)
                                .acceptConnection(endpointId, clientConnection))
                .setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) ->
                        Nearby.getConnectionsClient(this).rejectConnection(endpointId))
                .setIcon(android.R.drawable.ic_dialog_alert);
    }

    public void disconnectFromMaster(View view)
    {
        Nearby.getConnectionsClient(this).disconnectFromEndpoint(myMaster.getId());
        clientLog.setText("You Disconnected from your Master.\ntap on start discovery for look another Master.");
        disconnect();
    }

    private void disconnect()
    {
        btnDisconnectFromMaster.setEnabled(false);
        btnStartDiscovery.setVisibility(View.VISIBLE);
        btnStartDiscovery.setEnabled(true);
        btnStopDiscovery.setVisibility(View.VISIBLE);
        btnStopDiscovery.setEnabled(false);
        editText.setEnabled(false);
        btnSendClient.setEnabled(false);
        allMasters.remove(myMaster);
        myMaster = null;
        clientConnection = null;
        listView.invalidateViews();
        setTitle("Bluetooth");
    }

    public void logit(String msg)
    {
        Log.d("mylog", " >>>> " + msg);
        clientLog.append(msg);
    }


    //-------------------------Permission----------------------------------

    private final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private final ActivityResultLauncher<String[]> ACCESS_PERMISSIONS =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resuslt -> {
                for (int i = 0; i < LOCATION_PERMISSIONS.length; i++)
                    checkPermissions(LOCATION_PERMISSIONS[i]);
            });

    private void askPermisiions()
    {
        tellUserWhyUsingPermissions().show();
        for (int i = 0; i < LOCATION_PERMISSIONS.length; i++)
            checkPermissions(LOCATION_PERMISSIONS[i]);
    }

    private AlertDialog.Builder tellUserWhyUsingPermissions()
    {
        return new AlertDialog.Builder(this)
                .setTitle("Why permission is required")
                .setMessage("We need the permissions for use the service of Bluetooth.\nwithout those permissions the Application not work..")
                .setPositiveButton("Give Permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
    }

    private void checkPermissions(String location_permission)
    {
        if (ContextCompat.checkSelfPermission(this, location_permission) == PackageManager.PERMISSION_GRANTED)
            return;
        else if(shouldShowRequestPermissionRationale(location_permission))
            AlertDialogForPermission().show();
        else
            ACCESS_PERMISSIONS.launch(LOCATION_PERMISSIONS);
    }

    private AlertDialog.Builder AlertDialogForPermission()
    {
        return new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Allow us to access Location fo find devices")
                .setPositiveButton("Give Permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ACCESS_PERMISSIONS.launch(LOCATION_PERMISSIONS);
                    }
                })
                .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed()
    {
        Log.d("mylog", "MasterActivity.onBackPressed()");
        alertDialogForOnBackePressed().show();
    }

    private AlertDialog.Builder alertDialogForOnBackePressed()
    {
        return new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
        unregisterReceiver(receiver);
    }
}