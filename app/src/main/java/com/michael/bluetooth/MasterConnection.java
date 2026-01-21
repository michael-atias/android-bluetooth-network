package com.michael.bluetooth;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import androidx.collection.SimpleArrayMap;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public class MasterConnection extends Connection
{
    private final SimpleArrayMap<Long, Thread> backgroundThreads = new SimpleArrayMap<>();
    private static final long READ_STREAM_IN_BG_TIMEOUT = 5000;

    private List<ClientDevice> clientDevices;
    private TextView textView;
    private String masterID;
    private boolean isFirstMsg;
    private String masterName;

    public MasterConnection(Activity activity, List<ClientDevice> clientDevices)
    {
        super(activity);
        this.clientDevices = clientDevices;
        textView = activity.findViewById(R.id.txvMasterID);
        isFirstMsg = true;
        masterName = "Master";
    }

    public void sendMsg(String msg)
    {
        Message m = new Message(masterID, masterName ,msg);
        sendMsg(m);
    }

    public void sendMsg(Message msg)
    {
        textView.append(msg.toString());
        InputStream is = new ByteArrayInputStream(msg.toString().getBytes(Charset.forName("UTF-8")));
        Payload streamPayload = Payload.fromStream(is);
        for (ClientDevice c : clientDevices)
            send(c.getId(), streamPayload);
    }

    // פונקציה שמראה עדכונים לגביי הודעות שנשלחות ומתקבלות
    @Override
    public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update)
    {
        if (backgroundThreads.containsKey(update.getPayloadId()) && update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS)
        {
            backgroundThreads.get(update.getPayloadId()).interrupt();
            backgroundThreads.remove(update);
        }
    }

    // פונקציה שנקראת מתי שמתקבלת הודעה
    @Override
    public void onPayloadReceived(String endpointId, Payload payload)
    {
        switch (payload.getType())
        {
            case (Payload.Type.BYTES):
                if(isFirstMsg)
                {
                    handleFirstMsg(endpointId, payload);
                    break;
                }
                handleByteMsg(endpointId, payload);
                break;
            case (Payload.Type.FILE):
                handleFilemMsg(payload);
                break;
            case (Payload.Type.STREAM):
                handleStreamMsg(endpointId, payload);
                break;
            default:
                Log.d("mylog", "Payload type not exist.");
        }
    }

    private void handleFirstMsg(String endpointId, Payload payload)
    {
        Log.d("mylog", "we got to First Msg.");
        byte[] bytes = payload.asBytes();
        String msg = new String(bytes);
        if(isFirstMsg)
            masterID = new String(bytes);
        isFirstMsg = false;
    }

    private void handleByteMsg(String endpointId, Payload payload)
    {
        Log.d("mylog", "we got Byte Msg.");
        byte[] bytes = payload.asBytes();
        String msg = new String(bytes);
        ClientDevice client = searchClientById(endpointId);
        if (client != null)
        {
            Message message = new Message(client.getId(), client.getName(), msg);
            textView.append(message.toString());
            for (ClientDevice c: clientDevices)
                c.getConnection().sendMsg(c.getId(), message);
        }
        else
            Log.d("mylog", "Client sent msg but he is not in the clientList.");
    }

    private void handleFilemMsg(Payload payload)
    {
        Log.d("mylog", "we got File Msg.");
    }

    private void handleStreamMsg(String endpointId, Payload payload)
    {
        Log.d("mylog", "we got Stream Msg.");
        Thread backgroundThread = new Thread()
        {
            @Override
            public void run()
            {
                InputStream inputStream = payload.asStream().asInputStream();
                long lastRead = SystemClock.elapsedRealtime();
                while (!Thread.interrupted())
                {
                    if ((SystemClock.elapsedRealtime() - lastRead) >= READ_STREAM_IN_BG_TIMEOUT)
                    {
                        Log.d("mylog", "Read data from stream but timed out.");
                        break;
                    }
                    try
                    {
                        int availableBytes = inputStream.available();
                        if (availableBytes > 0)
                        {
                            byte[] bytes = new byte[availableBytes];
                            if (inputStream.read(bytes) == availableBytes)
                            {
                                lastRead = SystemClock.elapsedRealtime();
                                ClientDevice client = searchClientById(endpointId);
                                if (client != null)
                                {
                                    String msg = new String(bytes);
                                    Message message = new Message(client.getId(), client.getName(), msg);
                                    textView.append(message.toString());
                                    for (ClientDevice c: clientDevices)
                                        c.getConnection().sendMsg(c.getId(), message);
                                }
                                break;
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        Log.d("mylog","Failed to read bytes from InputStream.");
                        break;
                    }
                }
            }
        };
        backgroundThread.start();
        backgroundThreads.put(payload.getId(), backgroundThread);
    }

    private ClientDevice searchClientById(String id)
    {
        for (ClientDevice c : clientDevices)
            if(c.getId().equals(id))
                return c;
        return null;
    }

    //    private void makeConnectorToDB()
//    {
//        WifiManager wifiMan = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInf = wifiMan.getConnectionInfo();
//        int ipAddress = wifiInf.getIpAddress();
//        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
//        mongoDB = new MongoDB(ip);
//    }
}
