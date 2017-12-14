package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.LogPersister;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;



/**
 * Created by krishnendu on 08/11/17.
 */

public class MFPAnalyticsLocationListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static Logger logger = Logger.getLogger(LogPersister.INTERNAL_PREFIX + MFPAnalyticsLocationListener.class.getSimpleName());

    private static MFPAnalyticsLocationListener instance = null;

    private static Context Context = null;

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;


    private Location mLastLocation ;

    private double Latitude = 0.0;

    private double Longitude = 0.0;

    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FASTEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private boolean initLocationRequests = false;

    public boolean GoogleApiClient_is_connected=false;

    public static MFPAnalyticsLocationListener getInstance(Context context) {

        Context = context;
   
        if (instance == null) {
            instance = new MFPAnalyticsLocationListener();
        }
      
        return instance;
    }

    public void init() {
 
        try {
            if (checkPlayServices()) {

                // Building the GoogleApi client
                buildGoogleApiClient();

                createLocationRequest();

                initLocationRequests = true;


            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private boolean checkPlayServices() {

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(Context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(resultCode)) {
                Toast.makeText(Context,
                        "This device is supported. Please download google play services", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(Context,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                return true;
            }
            return false;
        }
        return true;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected  void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(Context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();



    }

    public double getLatitude() {
       
        if (!checkPermission() || !initLocationRequests) {
            logger.error("Check to see if your location permissions have been enabled.");
        }
        if (mLastLocation != null) {
            Latitude = mLastLocation.getLatitude();
        }

        return Latitude;
    }


    public double getLongitude() throws SecurityException {
        if (!checkPermission() || !initLocationRequests) {
            logger.error("Check to see if your location permissions have been enabled.");
        }

        if (mLastLocation != null) {
            Longitude = mLastLocation.getLongitude();
        }
        return Longitude;
    }

    private static boolean checkPermission() {
 
        return !(Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission(Context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED);
    }

    public boolean getInitLocationRequests() {
        return initLocationRequests;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        GoogleApiClient_is_connected=true;

        updateLocation();

        BMSAnalytics.setInitialUserIdentity();

            startLocationUpdates();

    }


    protected void updateLocation() {
 
        if (ActivityCompat.checkSelfPermission(Context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

         //   ActivityCompat.requestPermissions(mParentActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
         //   Log.d("TAG", "Permission Was Requested" );
            return;
        }
 
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

    }
    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
 
        if (ActivityCompat.checkSelfPermission(Context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //no op
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }
}
