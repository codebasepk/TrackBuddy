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
    Context mContext = null;
    LocationService gps;
    static String phoneNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Helper helper = new Helper(context);
        SharedPreferences preferences = helper.getPreferenceManager();
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

                if (!helper.isLocationServiceAvailable()) {
                    helper.sendSms(phoneNumber, "TrackBuddy:\n\nLocation Service of the target device is Disabled.");
                    gps.locationManager.removeUpdates(gps);
                } else {
                    gps.acquireLocation();
                    gps.locationManager.removeUpdates(gps);
                }

            } else {
                helper.sendSms(phoneNumber, "TrackBuddy:\n\nTracking Service of the target device is switched off by the user.");
                System.out.println("TrackerSwitched OFF");
            }
        } else if (message.getMessageBody().contentEquals(preferences.getString("sirenVariablePrefs", "TBsiren"))) {
                phoneNumber = message.getOriginatingAddress();

            if (sirenBool) {
                helper.playSiren();
                helper.sendSms(phoneNumber, "TrackBuddy:\n\nSiren Message successfully received.");
                System.out.println("Beep Message Sending...");
            } else {
                System.out.println("Siren Switched OFF");
                helper.sendSms(phoneNumber, "TrackBuddy:\n\nSiren Service of the target device is switched off by the user.");
            }
        } else if (message.getMessageBody().contentEquals("TBspeed")) {
            phoneNumber = message.getOriginatingAddress();
            gps = new LocationService(context);
            gps.acquireSpeed();
        }
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
