package com.michael.bluetooth;

import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

public class MongoDB
{
    public enum Indication
    {
        ADD_SUCCESS,
        ADD_FAIL,
    }


    private final String ADD_REQUEST = "addMsg";
//    private final String GET_REQUEST = "getUser";
//    private final String GET_BY_NAME = "getUserByFriendlyName";
//    private final String GET_ALL_REQUEST = "getAllUser";
//    private final String UPDATE_REQUEST = "updateUser";
//    private final String DELETE_REQUEST = "removeUser";
//    private final String DELETE_BY_NAME_REQUEST = "removeByFriendlyName";

    private String localHost;

    public MongoDB(String localHost)
    {
        this.localHost = localHost;
    }

    public void addClientDevice(ClientDevice clientDevice)
    {
        String url = localHost + ADD_REQUEST;
        AndroidNetworking.post(url)
                .addApplicationJsonBody(clientDevice)
                .build()
                .getAsObject(Indication.class, new ParsedRequestListener<Indication>()
                {
                    @Override
                    public void onResponse(MongoDB.Indication response)
                    {
                        Log.d("mylog", response.toString());
                    }

                    @Override
                    public void onError(ANError anError)
                    {
                        Log.d("mylog", anError.toString());
                    }
                });
    }

    public void addMessage(Message msg)
    {
        // בדיקה האם האינטרנט פעיל
        String url = localHost + ADD_REQUEST;
        AndroidNetworking.post(url)
                .addApplicationJsonBody(msg)
                .build()
                .getAsObject(Indication.class, new ParsedRequestListener<Indication>()
                {
                    @Override
                    public void onResponse(MongoDB.Indication response)
                    {
                        Log.d("mylog", response.toString());
                    }

                    @Override
                    public void onError(ANError anError)
                    {
                        Log.d("mylog", anError.toString());
                    }
                });
    }
}
