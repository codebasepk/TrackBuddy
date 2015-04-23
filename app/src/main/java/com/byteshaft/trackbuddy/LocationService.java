package com.byteshaft.trackbuddy;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import java.util.List;


public class LocationService extends ContextWrapper implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long INTERVAL = 0;
    private static final long FASTEST_INTERVAL = 0;
    private Helper mHelpers = null;
    private GoogleApiClient mGoogleApiClient;
    String address;

    int speedRecursionCounter = 0;
    int locationRecursionCounter = 0;
    int locationChangedCounter = 0;

    static double speed = 0.0;

    LocationRequest mLocationRequest;
    Location mLocation;

    public LocationService(Context context) {
        super(context);
        mHelpers = new Helper(context);
        connectingGoogleApiClient();
    }

    void acquireLocation() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLocation == null && locationRecursionCounter > 120) {
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLocation != null) {
                        String latLast = String.valueOf(mLocation.getLatitude());
                        String lonLast = String.valueOf(mLocation.getLongitude());
                        mHelpers.sendSms(SMSManager.originatingAddress, "TrackBuddy\n\nCurrent location cannot be acquired at the moment."
                                + "\n\nLastKnownLocation is:\nhttps://maps.google.com/maps?q="
                                + latLast
                                + ","
                                + lonLast
                        );
                        Log.i("TrackBuddy", "Location cannot be acquired. Sending lastKnownLocation SMS...");
                        stopLocationService();
                    } else {
                        mHelpers.sendSms(SMSManager.originatingAddress, "TrackBuddy\n\nDevice cannot be located at the moment."
                                + "\n\nMake sure the Location Service of the target device is on High-Accuracy Mode."
                        );
                        Log.i("TrackBuddy", "Device cannot be Located. Sending SMS...");
                        stopLocationService();
                    }
                } else if (mLocation == null) {
                    acquireLocation();
                    locationRecursionCounter++;
                    Log.i("TrackBuddy", "Tracker Thread Running... " + locationRecursionCounter);
                } else {
                    double accuracy;
                    String lat = String.valueOf(mLocation.getLatitude());
                    String lon = String.valueOf(mLocation.getLongitude());
                    accuracy = mLocation.getAccuracy();
                    int roundedAccuracy = (int) accuracy;
                    mHelpers.sendSms(SMSManager.originatingAddress, "TrackBuddy\n\nMy current location is:"
                            + "\nhttps://maps.google.com/maps?q="
                            + lat
                            + ","
                            + lon
                            +"\n\n(Accuracy: "
                            + roundedAccuracy
                            + "m)"
                    );
                    Log.i("TrackBuddy", "Current location acquired. Sending SMS...");

                    if (SMSManager.trackerCheckbox) {
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        try {
                            List<Address> result = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
                            address = addressToText(result.get(0)).toString();
                        }catch (Exception e) {
                            address = null;
                        }
                        if (address != null) {
                            mHelpers.sendSms(SMSManager.originatingAddress, address);
                            Log.i("TrackBuddy", "Address acquired. Sending SMS...");
                        }
                    }
                    stopLocationService();
                    mLocation = null;
                }
            }
        },1000);
    }

    void acquireSpeed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (speed == 0.0 && speedRecursionCounter > 30) {
                    mHelpers.sendSms(SMSManager.originatingAddress, "TrackBuddy\n\nTarget device appears to be still.");
                    Log.i("TrackBuddy", "Target device appears to be still. Sending SMS...");
                    stopLocationService();
                } else if (speed == 0.0) {
                    acquireSpeed();
                    speedRecursionCounter++;
                    Log.i("TrackBuddy", "Speed Thread Running..." + speedRecursionCounter);
                } else {
                    int roundedValueSpeed = (int) speed;
                    mHelpers.sendSms(SMSManager.originatingAddress, "TrackBuddy\n\nI am travelling at "
                            + roundedValueSpeed * 3600 / 1000
                            + " Km/h\n\n(Accuracy: +/- 5 Km/h)"
                    );
                    Log.i("TrackBuddy", "Speed acquired. Sending SMS...");
                    stopLocationService();
                }
            }
        },1000);
    }

    private void connectingGoogleApiClient() {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private StringBuffer addressToText(Address address) throws Exception {
        final StringBuffer addressText = new StringBuffer();
        for (int i = 0, max = address.getMaxAddressLineIndex(); i < max; ++i) {
            addressText.append(address.getAddressLine(i));
            if ((i+1) < max) {
                addressText.append(", ");
            }
        }
        addressText.append(", ");
        addressText.append(address.getCountryName());
        addressText.append(".");
        return addressText;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationService() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
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
        if (locationChangedCounter == 5) {
            mLocation = location;
        }
        speed = location.getSpeed();
        Log.i("TrackBuddy", "onLocationChanged CALLED..." + locationChangedCounter);
    }
}