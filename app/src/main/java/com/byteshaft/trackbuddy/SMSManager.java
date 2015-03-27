package com.byteshaft.trackbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;


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
        boolean sirenBool = preferences.getBoolean("sirenPreference", false);
        boolean speedBool = preferences.getBoolean("speedPreference", true);

        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        message = SmsMessage.createFromPdu((byte[]) pdus[0]);

        Log.i(clipBoard, message.getMessageBody());
        if (message.getMessageBody().contentEquals(preferences.getString("trackerVariablePrefs", "TBgps"))) {
            phoneNumber = message.getOriginatingAddress();

            if (trackerBool) {
                gps = new LocationService(context);

                if (!helper.isLocationServiceAvailable()) {
                    Log.i("Tracker", "Location Service diabled. Sending SMS...");
                    helper.sendSms(phoneNumber, "TrackBuddy:\n\nLocation Service of the target device is disabled from the Android System Settings.");
                    LocationService.locationManager.removeUpdates(gps);

                } else {

                    gps.acquireLocation();
                    LocationService.locationManager.removeUpdates(gps);
                }

            } else {

                Log.i("TrackBuddy_Tracker", "TrackerSwitched OFF. Sending SMS...");
                helper.sendSms(phoneNumber, "TrackBuddy:\n\nTracking Service of the target device is switched off from the TrackBuddy application.");
            }
        } else if (message.getMessageBody().contentEquals(preferences.getString("sirenVariablePrefs", "TBsiren"))) {
                phoneNumber = message.getOriginatingAddress();

            if (sirenBool) {

                helper.playSiren();
                Log.i("Siren", "Siren produced. Sending SMS...");
                helper.sendSms(phoneNumber, "TrackBuddy:\n\nSiren Message successfully received.");

                } else {

                Log.i("TrackBuddy_Siren", "Siren Switched OFF. Sending SMS...");
                helper.sendSms(phoneNumber, "TrackBuddy:\n\nSiren Service of the target device is switched off from the TrackBuddy application.");
            }
        } else if (message.getMessageBody().contentEquals(preferences.getString("speedVariablePrefs", "TBspeed"))) {
            phoneNumber = message.getOriginatingAddress();
            if (speedBool) {

                if(!helper.isSpeedAcquirable()) {
                Log.i("Speed", "GPS Service disabled. Sending SMS...");
                helper.sendSms(phoneNumber, "TrackBuddy:\n\nGPS Service of the target device is disabled from the Android System Settings.");
                 } else {
                gps = new LocationService(context);
                gps.acquireSpeed();
                }
            } else {
                Log.i("Speed", "Speed Switched OFF. Sending SMS...");
                helper.sendSms(phoneNumber, "TrackBuddy:\n\nSpeed Service of the target device is switched off from the TrackBuddy application.");
            }
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
