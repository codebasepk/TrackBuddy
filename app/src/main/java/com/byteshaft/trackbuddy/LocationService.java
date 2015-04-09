package com.byteshaft.trackbuddy;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;


public class LocationService extends ContextWrapper implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long INTERVAL = 0;
    private static final long FASTEST_INTERVAL = 0;
    private Helper mHelpers = null;
    private GoogleApiClient mGoogleApiClient;

    int locationChangedCounter = 0;

    static double speed;
    static float direction;

    LocationRequest mLocationRequest;
    Location mLocation;

    public LocationService(Context context) {
        super(context);
        mHelpers = new Helper(context);
    }

    void acquireLocation() {
        settingUpGoogleApiClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
            double accuracy;
                int counter = 0;
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counter++;
                    System.out.println("Tracker Thread Running... " + counter);
                    if (counter > 120 && mLocation == null) {
                        break;
                    }
                } while (mLocation == null);

                if (mLocation != null) {
                    String lat = String.valueOf(mLocation.getLatitude());
                    String lon = String.valueOf(mLocation.getLongitude());
                    accuracy = mLocation.getAccuracy();
                    int roundedAccuracy = (int) accuracy;
                    mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nCurrent location of the target device is:\nhttps://maps.google.com/maps?q=" + lat + "," + lon + "\n\n(Accuracy: " + roundedAccuracy + "m)");
                    Log.i("Location", "Location acquired. Sending SMS...");
                    stopLocationService();
                    mLocation = null;
                } else {
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLocation != null) {
                        String latLast = String.valueOf(mLocation.getLatitude());
                        String lonLast = String.valueOf(mLocation.getLongitude());
                        mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nCurrent location cannot be acquired at the moment.\n\nLast Known Location of the device is:\nhttps://maps.google.com/maps?q=" + latLast + "," + lonLast);
                        Log.i("Location", "Location cannot be acquired. Sending lastKnownLocation SMS...");
                        stopLocationService();
                    } else {
                        mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nTarget device cannot be located at the moment.\n\nMake sure the Location Service of the target device is on High-Accuracy Mode.");
                        Log.i("Location", "Device cannot be Located. Sending SMS...");
                        stopLocationService();
                    }
                }
            }
        }).start();
    }

    void acquireSpeed() {
        settingUpGoogleApiClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                double mSpeed;
                int counter = 0;
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    counter++;
                    System.out.println("Speed Thread Running... " + counter);
                    if (speed == 0.0 && counter > 30) {
                        break;
                    }
                } while (speed == 0.0);

                mSpeed = speed;
                int roundedValueSpeed = (int) mSpeed;
                mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nTarget device is currently travelling at the speed of "+ roundedValueSpeed * 3600 / 1000 + " Km/h\n\n(Accuracy +/- 5 Km/h)");
                Log.i("Speed", "Current Speed acquired. Sending SMS...");
                speed = 0.0;
                stopLocationService();
            }
        }).start();
    }

    private void settingUpGoogleApiClient() {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationService() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onLocationChanged(Location location) {

        locationChangedCounter++;

        if (locationChangedCounter == 3) {
            mLocation = location;
        }
        speed = location.getSpeed();
        direction = location.getBearing();
        System.out.println("onLocationChanged CALLED..." + locationChangedCounter);
    }
}


