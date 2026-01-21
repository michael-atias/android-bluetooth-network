package com.michael.bluetooth;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public abstract class Connection extends PayloadCallback
{
    protected Activity activity;

    public Connection(Activity activity)
    {
        this.activity = activity;
    }

    public void sendFirstMsg(String id, String msg)
    {
        ConnectionsClient cc = Nearby.getConnectionsClient(activity);
        Payload payload = Payload.fromBytes(msg.getBytes(Charset.forName("UTF-8")));
        send(id, payload);
    }

    public void sendMsg(String id, String msg)
    {
        ConnectionsClient cc = Nearby.getConnectionsClient(activity);
        Payload payload = Payload.fromBytes(msg.getBytes(Charset.forName("UTF-8")));
        send(id, payload);
    }
    // מקבלת הודעה והופכת אותה לפיילוד
    public void sendMsg(String id, Message msg)
    {
        InputStream is = new ByteArrayInputStream(msg.toString().getBytes(Charset.forName("UTF-8")));
        Payload streamPayload = Payload.fromStream(is);
        send(id, streamPayload);
    }

    // שולחת את ההודעה למי ששייך המזהה
    protected void send(String id, Payload payload)
    {
        ConnectionsClient cc = Nearby.getConnectionsClient(activity);
        Task task = cc.sendPayload(id, payload);
        task.addOnCompleteListener(new OnCompleteListener()
        {
            @Override
            public void onComplete(Task task)
            {
                boolean isSucc = task.isSuccessful();
                Log.d("mylog", "Connection.onComplete() isSuccessful? " + isSucc);
            }
        });
    }


    //    private InputStream makeInputStream(Object obj)
//    {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = null;
//        try {
//            oos = new ObjectOutputStream(baos);
//            oos.writeObject(obj);
//            oos.flush();
//            oos.close();
//        } catch (IOException e)
//        {
//            Log.d("mylog",">>>> StreamTransferMaster.makeInputStream()");
//            e.printStackTrace();
//        }
//        return new ByteArrayInputStream(baos.toByteArray());
//    }

    //הודעות בצורה כללית
//    public void masterSendAnyMsg(Object obj)
//    {
//        TextView masterLog = activity.findViewById(R.id.txvMasterID);
//        masterLog.append(obj.toString());
//        InputStream is = makeInputStream(obj);
//        Payload streamPayload = Payload.fromStream(is);
//        for (ClientDevice c : clientDevices)
//            Nearby.getConnectionsClient(activity).sendPayload(c.getId(), streamPayload);
//    }
}
