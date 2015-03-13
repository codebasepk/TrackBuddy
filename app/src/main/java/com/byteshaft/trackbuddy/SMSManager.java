package com.byteshaft.trackbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class SMSManager extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String clipBoard = "Message Received: ";
    SmsMessage message = null;
    SmsManager sms = SmsManager.getDefault();
    LocationService gps;
    private String phoneNumber;
    private GoogleApiClient mGoogleApiClient;

    @Override

    public void onReceive(Context context, Intent intent) {
        gps = new LocationService(context);
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        message = SmsMessage.createFromPdu((byte[]) pdus[0]);

        Log.i(clipBoard, message.getMessageBody());
        if (message.getMessageBody().contentEquals("TB007")) {
            phoneNumber = message.getOriginatingAddress();
            gps.isLocationServiceAvailable();

            if (!gps.isNetworkEnabled && !gps.isGPSEnabled) {
                sms.sendTextMessage(phoneNumber, null, "TrackBuddy:\n\nLocation Service of the target device is Disabled.", null, null);
                System.out.println("GPS Disabled. Sending SMS...");
            } else {
                mGoogleApiClient = new GoogleApiClient.Builder(context)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
                acquireLocation();
            }
        }
    }

    public void acquireLocation() {
        System.out.println("Thread running...");
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
                        sms.sendTextMessage(phoneNumber, null, "TrackBuddy:\n\n" + "https://maps.google.com/maps?q=" + lat + "," + lon, null, null);
                        System.out.println("Location acquired. Sending SMS...");
                    } else {
                        sms.sendTextMessage(phoneNumber, null, "TrackBuddy:\n\n" + "Device cannot be located at the moment.\n\nMake sure the Location Service of the target device is on High-Accuracy mode.", null, null);
                        System.out.println("Device cannot be Located. Sending SMS...");
                    }
            }
        }).start();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
