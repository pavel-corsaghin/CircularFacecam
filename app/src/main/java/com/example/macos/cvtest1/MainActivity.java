package com.example.macos.cvtest1;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_CODE = 1;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {

        String cameraPermissions = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                cameraPermissions) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{cameraPermissions},
                    PERMISSIONS_REQUEST_ACCESS_CODE);
        } else {
            startFacecameService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "permissions granted");
                    startFacecameService();
                } else {
                    Log.v(TAG, "permissions not granted");
                }
            }
        }
        startFacecameService();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void startFacecameService(){
        Intent intent = new Intent(this, FaceCamService.class);
        intent.setAction(AppConstants.ACTION_START_FACE_CAM);
        startService(intent);
    }
}
