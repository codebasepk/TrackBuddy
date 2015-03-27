
package com.byteshaft.trackbuddy;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

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
                    System.out.println("Tracker Thread Runnning... " + counter);
                    if (counter > 120 && mLocation == null) {
                        break;
                    }
                } while (mLocation == null);
                if (mLocation != null) {
                    String lat = String.valueOf(mLocation.getLatitude());
                    String lon = String.valueOf(mLocation.getLongitude());
                    mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nhttps://maps.google.com/maps?q=" + lat + "," + lon);
                    Log.i("Location", "Location acquired. Sending SMS...");
                } else {
                    mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nTarget device cannot be located at the moment.\n\nMake sure the Location Service of the target device is on High-Accuracy Mode.");
                    Log.i("Location", "Device cannot be Located. Sending SMS...");
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
                    System.out.println("Speed Thread Running... " + counter);
                    if (speed == 0.0 && counter > 9) {
                        break;
                    }

                } while (speed == 0.0);
                int roundedValueSpeed = (int)mSpeed;
                mHelpers.sendSms(SMSManager.phoneNumber, "TrackBuddy:\n\nCurrent speed of the target device is: " + roundedValueSpeed*3600/1000 + " Km/h\n\n(Accuracy +/- 5 Km/h)");
                Log.i("Speed", "Current Speed acquired. Sending SMS...");
                locationManager.removeUpdates(LocationService.this);
                speed = 0.0;

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

