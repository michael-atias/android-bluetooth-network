package com.michael.bluetooth;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import androidx.collection.SimpleArrayMap;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import java.io.InputStream;


public class ClientConnection extends Connection
{
    private final SimpleArrayMap<Long, Thread> backgroundThreads = new SimpleArrayMap<>();
    private static final long READ_STREAM_IN_BG_TIMEOUT = 5000;

    private boolean isFirstMsg;
    private String clientName;
    private String clientID;
    private TextView txv;

    public ClientConnection(Activity activity)
    {
        super(activity);
        isFirstMsg = true;
        txv = activity.findViewById(R.id.txvClientID);
    }

    //  פונקציה שמראה עדכונים לגביי הודעות שנשלחות ומתקבלות
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
                    handleByteMsg(endpointId, payload);
                break;
            case (Payload.Type.FILE):
                handleFilemMsg(payload);
                break;
            case (Payload.Type.STREAM):
                handleStreamMsg(payload);
                break;
            default:
                Log.d("mylog", "Payload type not exist.");
        }
    }

    private void handleByteMsg(String masterId, Payload payload)
    {
        Log.d("mylog", "we got Byte Msg.");
        byte[] bytes = payload.asBytes();

        if(handleFirstMsg(masterId, bytes))
            return;
        showMsg(bytes);
    }

    private boolean handleFirstMsg(String masterId, byte bytes[])
    {
        String msg = new String(bytes);
        boolean ind = false;
        if (isFirstMsg)
        {
            ind = true;
            isFirstMsg = false;
            clientID = msg.substring(0, msg.indexOf(':'));
            clientName = msg.substring(msg.indexOf(':')+1, msg.lastIndexOf(':'));
            String indication = msg.substring(msg.lastIndexOf(':')+1);
            if(indication.equals("1"))
                sendFirstMsg(masterId, masterId); // send the id of the master to the master.
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    activity.setTitle(activity.getTitle() + ":" + clientName);
                    activity.findViewById(R.id.editTextClientID).setEnabled(true);
                    activity.findViewById(R.id.btnDisconnectClientID).setEnabled(true);
                    activity.findViewById(R.id.btnSendClientID).setEnabled(true);
                }
            });
        }
        return ind;
    }


    private void handleFilemMsg(Payload payload)
    {
        Log.d("mylog", "we got File Msg.");
    }

    private void handleStreamMsg(Payload payload)
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
                    Log.d("mylog", "inside the loop!");
                    if ((SystemClock.elapsedRealtime() - lastRead) >= READ_STREAM_IN_BG_TIMEOUT) {
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
                                showMsg(bytes);
                                break;
                            }
                        }
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.d("mylog", "Failed to read bytes from InputStream.");
                        break;
                    }
                }
            }
        };
        backgroundThread.start();
        backgroundThreads.put(payload.getId(), backgroundThread);
    }

    private void showMsg(byte bytes[])
    {
        String msg = new String(bytes);
        txv.append(msg);
    }
}
