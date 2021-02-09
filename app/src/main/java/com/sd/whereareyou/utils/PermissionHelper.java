package com.sd.whereareyou.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;

import com.sd.whereareyou.R;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class PermissionHelper {

    public static final int LOCATION_REQUEST_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;

    public static void requestLocationPermission(Context context) {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions((Activity) context, permissions, LOCATION_REQUEST_CODE);

    }

    public static void requestCameraPermission(Context context) {
        String[] permissions = {Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions((Activity) context, permissions, CAMERA_REQUEST_CODE);
    }

    public static void enableLocationPermission(final Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle(context.getString(R.string.enable_location));
            alertDialogBuilder.setMessage(context.getString(R.string.location_setting_enable_message));
           /* alertDialogBuilder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                }
            });*/
            alertDialogBuilder.setPositiveButton(context.getString(R.string.location_setting), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);

                }
            });

            final AlertDialog dialog = alertDialogBuilder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    public static void checkAndRequestRequiredPermission(Context context) {

        if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestLocationPermission(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestCameraPermission(context);

    }


}
