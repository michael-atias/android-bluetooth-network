package com.michael.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

public class Permissions
{
//    private final String[] LOCATION_PERMISSIONS = {
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
//    };
//
//    private Context context;
//
//    public Permissions(Context context)
//    {
//        this.context = context;
//    }
//
//    private final ActivityResultLauncher<String[]> ACCESS_PERMISSIONS =
//            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resuslt -> {
//                for (int i = 0; i < LOCATION_PERMISSIONS.length; i++)
//                    checkPermissions(LOCATION_PERMISSIONS[i]);
//            });
//
//    private void askPermisiions()
//    {
//        tellUserWhyUsingPermissions().show();
//        for (int i = 0; i < LOCATION_PERMISSIONS.length; i++)
//            checkPermissions(LOCATION_PERMISSIONS[i]);
//        ACCESS_PERMISSIONS.launch(LOCATION_PERMISSIONS);
//    }
//
//    private AlertDialog.Builder tellUserWhyUsingPermissions()
//    {
//        return new AlertDialog.Builder(context)
//                .setTitle("Why permission is required")
//                .setMessage("We need the permissions for use the service of Bluetooth.\nwithout those permissions the Application not work..")
//                .setPositiveButton("OK", new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                    }
//                });
//    }
//
//    private void checkPermissions(String location_permission)
//    {
//        if (ContextCompat.checkSelfPermission(context, location_permission) == PackageManager.PERMISSION_GRANTED)
//            return;
//        else if(shouldShowRequestPermissionRationale(location_permission))
//            AlertDialogForPermission().show();
//        else
//            Toast.makeText(context, "You denied to access the Location", Toast.LENGTH_SHORT).show();
//    }
//
//    private AlertDialog.Builder AlertDialogForPermission()
//    {
//        return new AlertDialog.Builder(context)
//                .setTitle("Permission Required")
//                .setMessage("Allow us to access Location fo find devices")
//                .setPositiveButton("Give Permission", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        ACCESS_PERMISSIONS.launch(LOCATION_PERMISSIONS);
//                    }
//                })
//                .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//    }
}
