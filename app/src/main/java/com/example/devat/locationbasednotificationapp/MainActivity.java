package com.example.devat.locationbasednotificationapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;


    private Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;

    private boolean mRequestingLocationUpdate = false;

    private LocationRequest mLocationRequest;



    private static int UPDATE_INTERVAL = 10000;
    private static int FASTEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10 ;


    private TextView iblLocation;
    private Button btnShowLocation,btnstartLocationUpdate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        iblLocation = (TextView) findViewById(R.id.lblLocation);
        btnShowLocation = (Button) findViewById(R.id.btnshowlocation);
        btnstartLocationUpdate = (Button) findViewById(R.id.btnlocationupdate);


        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdate){
                startLocationUpdates();
            }


        }

        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });


        btnstartLocationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();
            }
        });





    }

    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            iblLocation.setText(latitude + ", " + longitude);
            displayNotification();

        } else {

            iblLocation
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    public void displayNotification() {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Resources r = getResources();

        String notification_text = iblLocation.getText().toString();
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.marker2)
                .setContentTitle(r.getString(R.string.app_name))
                .setContentText(notification_text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();

        checkPlayServices();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdate) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);


    }

    protected void onPause(){
        super.onPause();
        stopLocationUpdates();

    }

    private void stopLocationUpdates () {





    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdate) {
            // Changing the button text
            btnstartLocationUpdate
                    .setText(getString(R.string.btn_stop_location_update));

            mRequestingLocationUpdate = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d(TAG, "Periodic location updates started!");

        } else {
            // Changing the button text
            btnstartLocationUpdate
                    .setText(getString(R.string.btn_start_location_update));

            mRequestingLocationUpdate = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onConnected(Bundle bundle) {

        if(mRequestingLocationUpdate){
            startLocationUpdates();
        }

        displayLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {


        Log.i(TAG, "Connection Failed" + connectionResult.getErrorCode());

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        displayLocation();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
