package com.michael.bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class MongoActivity extends AppCompatActivity
{
    private final String LOCAL_HOST_CLASS = "http://192.168.211.90:8080/";
    private final String LOCAL_HOST_DORM = "http://192.168.88.136:8080/";
    private final String LOCAL_HOST_MY_PHONE = "http://192.168.166.206:8080/";
    private final String ADD_REQUEST = "addUser";
    private final String GET_REQUEST = "getUser";
    private final String GET_BY_NAME = "getUserByFriendlyName";
    private final String GET_ALL_REQUEST = "getAllUser";
    private final String UPDATE_REQUEST = "updateUser";
    private final String DELETE_REQUEST = "removeUser";
    private final String DELETE_BY_NAME_REQUEST = "removeByFriendlyName";
    private final String TEST = "test";

    private TextView txvLog;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mongo);

        AndroidNetworking.initialize(getApplicationContext());
        txvLog = findViewById(R.id.txvLogID);
        editText = findViewById(R.id.txfID);
        editText.setText("");
        txvLog.setText("\n");
    }

    public void addUser(View view)
    {
        String url = LOCAL_HOST_CLASS + ADD_REQUEST;
        //User user = new User("33:22:22", "friendly", true);
        AndroidNetworking.post(url)
                //.addApplicationJsonBody(user)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response)
                    {
                        txvLog.setText(response.toString());
                    }

                    @Override
                    public void onError(ANError anError)
                    {
                        txvLog.setText(anError.toString());
                    }
                });
    }

    public void getById(View view)
    {
        txvLog.setText("");
        String url = LOCAL_HOST_CLASS + GET_REQUEST;
        String parmeter = editText.getText().toString();
        if(parmeter.equals(""))
        {
            getAllUser();
            return;
        }
        //AndroidNetworking.get(url)
                //.addQueryParameter("id", parmeter)
                //.build()
                //.getAsObject(User.class, new ParsedRequestListener<User>()
//                {
//                    @Override
//                    public void onResponse(User response) {
//                        txvLog.setText(response.toString());
//                    }
//
//                    @Override
//                    public void onError(ANError error) {
//                        txvLog.setText("Not Found");
//                    }
//                });
    }

    public void getAllUser()
    {
        txvLog.setText("");
        String url = LOCAL_HOST_CLASS + GET_ALL_REQUEST;
        AndroidNetworking.get(url)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length();i++)
                        {
                            try {
                                txvLog.setText(txvLog.getText() + response.get(i).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        txvLog.setText("Not Found");
                    }
                });
    }

    public void update(View view)
    {
        String url = LOCAL_HOST_CLASS + UPDATE_REQUEST;
        AndroidNetworking.post(url)
                .addQueryParameter("macAddres", "31:22:11")
                .addQueryParameter("friendlyName", "friendly")
                .addQueryParameter("isMaster", "true")
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        txvLog.setText(response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        txvLog.setText(anError.toString());
                    }
                });
    }

    public void delete(View view)
    {
        txvLog.setText("");
        String parmeter = editText.getText().toString();
        if(!parmeter.equals(""))
            deleteByName(parmeter);
        String url = LOCAL_HOST_CLASS + DELETE_REQUEST;

        AndroidNetworking.delete(url)
                .addQueryParameter("id", parmeter)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                txvLog.setText(response.get(i).toString() + "\n");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        txvLog.setText("Amount of User Deleted: " + response.length() + "\n");
                    }

                    @Override
                    public void onError(ANError anError) {
                        txvLog.setText(txvLog.getText() + "Not Found");
                    }
                });
    }

    public void deleteByName(String parameter)
    {
        txvLog.setText("");
        String url = LOCAL_HOST_CLASS + DELETE_BY_NAME_REQUEST;

        AndroidNetworking.delete(url)
                .addQueryParameter("id", parameter)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                txvLog.setText(response.get(i).toString() + "\n");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        txvLog.setText("Amount of User Deleted: " + response.length() + "\n");
                    }

                    @Override
                    public void onError(ANError anError) {
                        txvLog.setText(txvLog.getText() + "Not Found");
                    }
                });
    }
}