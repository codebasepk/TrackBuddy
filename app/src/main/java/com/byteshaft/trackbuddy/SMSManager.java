package com.byteshaft.trackbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    Context mContext = null;
    LocationService gps;
    private String phoneNumber;
    private static MediaPlayer mp;
    private GoogleApiClient mGoogleApiClient;

    @Override

    public void onReceive(Context context, Intent intent) {

        mContext = context;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean trackerBool = preferences.getBoolean("trackerPreference", true);
        boolean sirenBool = preferences.getBoolean("sirenPreference", true);

        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        message = SmsMessage.createFromPdu((byte[]) pdus[0]);

        Log.i(clipBoard, message.getMessageBody());
        if (message.getMessageBody().contentEquals(preferences.getString("trackerVariablePrefs", "TBgps"))) {
            phoneNumber = message.getOriginatingAddress();

            if (trackerBool) {
                gps = new LocationService(context);
                gps.isLocationServiceAvailable();

                if (!gps.isNetworkEnabled && !gps.isGPSEnabled) {
                    sms.sendTextMessage(phoneNumber, null, "TrackBuddy:\n\nLocation Service of the target device is Disabled.", null, null);
                    System.out.println("GPS Disabled. Sending SMS...");
                    gps.locationManager.removeUpdates(gps);
                } else {
                    mGoogleApiClient = new GoogleApiClient.Builder(context)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();
                    mGoogleApiClient.connect();
                    acquireLocation();
                    gps.locationManager.removeUpdates(gps);
                }

            } else {
                sms.sendTextMessage(phoneNumber, null, "TrackBuddy:\n\nTracking Service of the target device is switched off by the user.", null, null);
        System.out.println("TrackerSwitched OFF");
                    }

        } else if (message.getMessageBody().contentEquals(preferences.getString("sirenVariablePrefs", "TBsiren"))) {
                phoneNumber = message.getOriginatingAddress();

            if (sirenBool) {
                mp = MediaPlayer.create(context, R.raw.alarm);
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                AudioManager am =
                        (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                        0);
                mp.start();
                sms.sendTextMessage(phoneNumber, null, "TrackBuddy:\n\nSiren Message successfully received.", null, null);
                System.out.println("Beep Message Sending...");
            } else {
                System.out.println("Siren Switched OFF");
                sms.sendTextMessage(phoneNumber, null, "TrackBuddy:\n\nSiren Service of the target device is switched off by the user.", null, null);

            }
        } else if (message.getMessageBody().contentEquals("TBspeed")) {
            phoneNumber = message.getOriginatingAddress();
            gps = new LocationService(context);
            acquireSpeed();
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

    public void acquireSpeed() {
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
                       mSpeed = gps.speed;
                       counter++;
                   System.out.println(counter);
                   if (gps.speed == 0.0 && counter > 10) {
                       break;
                   }

               }while (gps.speed == 0.0);
                sms.sendTextMessage(phoneNumber, null, "TrackBuddy:\n\nCurrent speed of the target Device is: " + mSpeed*3600/1000 + "km/h" , null, null);
                gps.locationManager.removeUpdates(gps);
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
