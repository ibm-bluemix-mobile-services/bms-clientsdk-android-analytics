package com.ibm.mobilefirstplatform.clientsdk.android.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.widget.Toast;


import java.util.logging.Level;
import java.util.logging.Logger;



public class UIActivity extends Activity {


    private final int MY_PERMISSIONS_REQUEST_LOCATION=11;
    private static final Logger logger =Logger.getLogger(UIActivity.class.getName());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
        else
        {
            launchMainActivity();
        }



    }

    private void launchMainActivity()
    {
        ApplicationInfo ai = null;
        try {
            ai = getPackageManager().getApplicationInfo(this.getPackageName(),     PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Bundle bundle = ai.metaData;
        String mainActivity = bundle.getString("main_activity");

        Intent launchMain = null;
        try {
            launchMain = new Intent(this, Class.forName(mainActivity));
        } catch (ClassNotFoundException e) {

            logger.log(Level.SEVERE, "MainActivity name provided is not correct");
        }
        startActivity(launchMain);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                System.out.println("Inside Case");

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this,"Location Logging APi is activated ",Toast.LENGTH_LONG);

                } else {

                    Toast.makeText(this,"Location Logging APi is deactivated ",Toast.LENGTH_LONG);
                }

                launchMainActivity();

                 break;
               }
             
            default:    
            

        }
    }



}
