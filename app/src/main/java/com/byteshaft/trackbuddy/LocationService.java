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

    private final long INTERVAL = 0;
    private final long FASTEST_INTERVAL = 0;
    private Helper mHelpers;
    private GoogleApiClient mGoogleApiClient;
    private String mAddress;

    private int mSpeedRecursionCounter = 0;
    private int mLocationRecursionCounter = 0;
    private int mLocationChangedCounter = 0;

    static double speed = 0.0;

    private LocationRequest mLocationRequest;
    private Location mLocation;

    public LocationService(Context context) {
        super(context);
        mHelpers = new Helper(context);
        connectingGoogleApiClient();
    }

    void acquireLocation() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLocation == null && mLocationRecursionCounter > 120) {
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLocation != null) {
                        String latLast = String.valueOf(mLocation.getLatitude());
                        String lonLast = String.valueOf(mLocation.getLongitude());
                        mHelpers.sendSms(SMSManager.sOriginatingAddress, "TrackBuddy\n\nCurrent location cannot be acquired at the moment."
                                + "\n\nLastKnownLocation is:\nhttps://maps.google.com/maps?q="
                                + latLast
                                + ","
                                + lonLast
                        );
                        Log.i("TrackBuddy", "Location cannot be acquired. Sending lastKnownLocation SMS...");
                        stopLocationService();
                    } else {
                        mHelpers.sendSms(SMSManager.sOriginatingAddress, "TrackBuddy\n\nDevice cannot be located at the moment."
                                + "\n\nMake sure the Location Service of the target device is on High-Accuracy Mode."
                        );
                        Log.i("TrackBuddy", "Device cannot be Located. Sending SMS...");
                        stopLocationService();
                    }
                } else if (mLocation == null) {
                    acquireLocation();
                    mLocationRecursionCounter++;
                    Log.i("TrackBuddy", "Tracker Thread Running... " + mLocationRecursionCounter);
                } else {
                    double accuracy;
                    String lat = String.valueOf(mLocation.getLatitude());
                    String lon = String.valueOf(mLocation.getLongitude());
                    accuracy = mLocation.getAccuracy();
                    int roundedAccuracy = (int) accuracy;
                    mHelpers.sendSms(SMSManager.sOriginatingAddress, "TrackBuddy\n\nMy current location is:"
                            + "\nhttps://maps.google.com/maps?q="
                            + lat
                            + ","
                            + lon
                            +"\n\n(Accuracy: "
                            + roundedAccuracy
                            + "m)"
                    );
                    Log.i("TrackBuddy", "Current location acquired. Sending SMS...");

                    if (SMSManager.sTrackerCheckbox) {
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        try {
                            List<Address> result = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
                            mAddress = addressToText(result.get(0)).toString();
                        }catch (Exception e) {
                            mAddress = null;
                        }
                        if (mAddress != null) {
                            mHelpers.sendSms(SMSManager.sOriginatingAddress, mAddress);
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
                if (speed < 1.0 && mSpeedRecursionCounter > 30) {
                    mHelpers.sendSms(SMSManager.sOriginatingAddress, "TrackBuddy\n\nTarget device appears to be still.");
                    Log.i("TrackBuddy", "Target device appears to be still. Sending SMS...");
                    stopLocationService();
                } else if (speed < 1.0) {
                    acquireSpeed();
                    mSpeedRecursionCounter++;
                    Log.i("TrackBuddy", "Speed Thread Running..." + mSpeedRecursionCounter);
                } else {
                    int roundedValueSpeed = (int) speed;
                    mHelpers.sendSms(SMSManager.sOriginatingAddress, "TrackBuddy\n\nI am travelling at "
                            + roundedValueSpeed * 3600 / 1000
                            + " Km/h\n\n(Accuracy: +/- 5 Km/h)"
                    );
                    Log.i("TrackBuddy", "Speed acquired. Sending SMS...");
                    speed = 0.0;
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
        mLocationChangedCounter++;
        if (mLocationChangedCounter == 5) {
            mLocation = location;
        }
        speed = location.getSpeed();
        Log.i("TrackBuddy", "onLocationChanged CALLED..." + mLocationChangedCounter);
    }
}
