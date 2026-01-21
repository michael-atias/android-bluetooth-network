package com.michael.bluetooth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MasterActivity extends AppCompatActivity
{
    private final Strategy STRATEGY = Strategy.P2P_STAR;

    private TextView masterLog;
    private EditText editText;
    private MasterReceiver receiver;
    private Button btnStartAdversite;
    private Button btnStopAdversite;
    private Button btnSendMaster;
    private Button btnDisconnect;
    private ListView listView;
    private ArrayAdapter arrayAdapter;
    private List<ClientDevice> allClients;
    private int numOfClient;
    private String masterName;
    private MasterConnection masterConnection;
    private List<ClientConnection> allConnections;
    private boolean isFirstClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        registerToReciver();
        findAllComponent();
        init();
    }

    private void registerToReciver()
    {
        receiver = new MasterReceiver(this);
        IntentFilter filter = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    private void findAllComponent()
    {
        masterLog = findViewById(R.id.txvMasterID);
        btnStartAdversite =  findViewById(R.id.btnStartAdversitingID);
        btnStopAdversite = findViewById(R.id.btnStopAdversiteID);
        btnSendMaster = findViewById(R.id.btnSendMasterID);
        listView = findViewById(R.id.masterListViewID);
        editText = findViewById(R.id.editTextMasterID);
        btnDisconnect = findViewById(R.id.btnDisconnectMasterID);
    }

    private void init()
    {
        Random rand = new Random();
        allClients = new ArrayList<>();
        allConnections = new ArrayList<>();
        masterConnection = new MasterConnection(this, allClients);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, allClients);
        listView.setAdapter(arrayAdapter);
        numOfClient = 0;
        editText.setEnabled(false);
        btnStopAdversite.setEnabled(false);
        btnSendMaster.setEnabled(false);
        btnDisconnect.setVisibility(View.INVISIBLE);
        btnSendMaster.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String msg = editText.getText().toString();
                editText.setText("");
                masterConnection.sendMsg(msg);
            }
        });
        masterLog.setMovementMethod(new ScrollingMovementMethod());
        masterName = "master" + (rand.nextInt(10000) + 1);
        setTitle(getTitle() + ": " + masterName);
        isFirstClient = true;
    }

    // פונקציה שמתחילה את האזנה לקליינטים
    public void startAdvertising(View view)
    {
        askPermisiions();
        btnStartAdversite.setEnabled(false);
        btnStopAdversite.setEnabled(true);
        String myAppName = getPackageName();
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(this)
                .startAdvertising(masterName, myAppName, con, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            masterLog.setText("started to Advertise.\n");
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            masterLog.setText("problem in adversiting : " + e);
                            btnStartAdversite.setEnabled(false);
                            btnStopAdversite.setEnabled(true);
                        });
    }

    // מפסיק את האזנה לקליינטים
    // אם יש קליינטים מחוברים מתנתק מהם
    public void stopAdversiting(View view)
    {
        Nearby.getConnectionsClient(this).stopAdvertising();
        masterLog.append("You stoped to adversiting!\n");
        btnStopAdversite.setEnabled(false);
        btnStartAdversite.setEnabled(true);
    }

    // מתי שקליינטים רוצים להתחבר
    private ConnectionLifecycleCallback con = new ConnectionLifecycleCallback()
    {
        private AlertDialog alert;

        // מציג הודעה האם לאשר את החיבור
        @Override
        public synchronized void onConnectionInitiated(String endpointId, ConnectionInfo info)
        {
            allConnections.add(new ClientConnection(MasterActivity.this));
            AlertDialog.Builder builder = alertDialogForConnection(endpointId, info);
            alert = builder.create();
            alert.show();
        }

        // פונקציה שבודקת מה הסטטוס של האישור בין המאסטר לקליינט
        @Override
        public synchronized void onConnectionResult(String endpointId, ConnectionResolution connectionResolution)
        {
            switch (connectionResolution.getStatus().getStatusCode())
            {
                case ConnectionsStatusCodes.STATUS_OK:
                    String clientName = "Client" + numOfClient++;
                    btnSendMaster.setEnabled(true);
                    btnDisconnect.setVisibility(View.VISIBLE);
                    editText.setEnabled(true);
                    allClients.add(new ClientDevice(endpointId, clientName, false, allConnections.get(allConnections.size()-1)));
                    logit(clientName + " connected.");
                    listView.invalidateViews();
                    String idClientName;
                    if(isFirstClient)
                    {
                        idClientName = endpointId + ":" + clientName + ":1";
                        isFirstClient = false;
                    }
                    else
                        idClientName = endpointId + ":" + clientName + ":0";

                    Log.d("mylog", "Indicatin = " + idClientName);
                    masterConnection.sendFirstMsg(endpointId, idClientName);
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                case ConnectionsStatusCodes.STATUS_ERROR:
                    logit("\nThe connection was lost.\n");
                    allConnections.remove(allConnections.get(allConnections.size()-1));
                    alert.cancel();
                    break;
            }
        }

        // פונקציה שבודקת אם יש התנתקות
        @Override
        public synchronized void onDisconnected(String endpointId)
        {
            ClientDevice c = searchClientById(endpointId);
            ClientConnection connectionsClient = searchConnectiontById(endpointId);
            logit(c.getName() + ": disconnected.");
            allClients.remove(c);
            allConnections.remove(connectionsClient);
            listView.invalidateViews();
            if(allClients.size() == 0)
            {
                btnSendMaster.setEnabled(false);
                editText.setEnabled(false);
                numOfClient = 0;
                btnDisconnect.setVisibility(View.INVISIBLE);
            }
        }
    };

    private AlertDialog.Builder alertDialogForConnection(String endpointId, ConnectionInfo info)
    {
        return new AlertDialog.Builder(this)
                .setTitle("Accept connection to " + info.getEndpointName())
                .setMessage("Confirm the code matches on both devices: " + info.getAuthenticationDigits())
                .setPositiveButton("Accept", (DialogInterface dialog, int which) ->
                        Nearby.getConnectionsClient(this)
                                .acceptConnection(endpointId, masterConnection))
                .setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) ->
                        Nearby.getConnectionsClient(this).rejectConnection(endpointId))
                .setIcon(android.R.drawable.ic_dialog_alert);
    }

    public void disconnect(View view)
    {
        btnSendMaster.setEnabled(false);
        editText.setEnabled(false);
        for (ClientDevice c: allClients)
        {
            Nearby.getConnectionsClient(this).disconnectFromEndpoint(c.getId());
            allConnections.remove(searchConnectiontById(c.getId()));
            allClients.remove(c);
        }
        listView.invalidateViews();
        numOfClient = 0;
        btnDisconnect.setVisibility(View.INVISIBLE);
        isFirstClient = true;
    }

    private ClientDevice searchClientById(String id)
    {
        for (ClientDevice c : allClients)
            if(c.getId().equals(id))
                return c;
        return null;
    }

    private ClientConnection searchConnectiontById(String id)
    {
        for (ClientDevice c : allClients)
            if(c.getId().equals(id))
                return c.getConnection();
        return null;
    }

    private void logit(String msg)
    {
        masterLog.append(msg+"\n");
        Log.d("mylog", " >>>> " + msg);
    }

//------------------------------------------ when tap on back button -------------------------------------------

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
        Nearby.getConnectionsClient(this).stopAdvertising();
        unregisterReceiver(receiver);
    }


//-------------------------Permission---------------------------------- help!!!

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
            alertDialogForPermission().show();
        else
            ACCESS_PERMISSIONS.launch(LOCATION_PERMISSIONS);
    }

    private AlertDialog.Builder alertDialogForPermission()
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
}