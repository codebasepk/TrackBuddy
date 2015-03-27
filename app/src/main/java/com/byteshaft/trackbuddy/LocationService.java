
package com.byteshaft.trackbuddy;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class LocationService extends ContextWrapper implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static double speed;
    private GoogleApiClient mGoogleApiClient;

    static LocationManager locationManager;
    private Helper mHelpers = null;

    public LocationService(Context context) {
        super(context);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mHelpers = new Helper(context);
    }

    void acquireLocation() {
        connectGoogleApiClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Location mLocation;
                int counter = 0;
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    counter++;
                    System.out.println(counter);
                    if (counter > 120 && mLocation == null) {
                        break;
                    }
                } while (mLocation == null);
                if (mLocation != null) {
                    String lat = String.valueOf(mLocation.getLatitude());
                    String lon = String.valueOf(mLocation.getLongitude());
                    mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nhttps://maps.google.com/maps?q=" + lat + "," + lon);
                    System.out.println("Location acquired. Sending SMS...");
                } else {
                    mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nDevice cannot be located at the moment.\n\nMake sure the Location Service of the target device is on High-Accuracy mode.");
                    System.out.println("Device cannot be Located. Sending SMS...");
                }
            }
        }).start();
    }

    void acquireSpeed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                double mSpeed;
                int counter = 0;
                do {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mSpeed = speed;
                    counter++;
                    System.out.println(counter);
                    if (speed == 0.0 && counter > 10) {
                        break;
                    }

                } while (speed == 0.0);
                mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nCurrent speed of the target Device is: " + mSpeed*3600/1000 + "km/h");
                locationManager.removeUpdates(LocationService.this);
            }
        }).start();
    }

    private void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override public void onConnectionFailed(ConnectionResult connectionResult) {}
    @Override public void onConnected(Bundle bundle) {}
    @Override public void onConnectionSuspended(int i) {}

    @Override
    public void onLocationChanged(Location location) {
        location.getLatitude();
        speed = location.getSpeed();
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}
}

