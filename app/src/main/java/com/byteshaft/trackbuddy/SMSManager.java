package com.byteshaft.trackbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;


public class SMSManager extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    SmsMessage message = null;
    Context mContext = null;
    LocationService gps;
    static String originatingAddress;
    SharedPreferences preferences;
    Helper helper;
    boolean trackerBool, sirenBool, speedBool;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        helper = new Helper(context);

        preferences = helper.getPreferenceManager();
        trackerBool = preferences.getBoolean("trackerPreference", true);
        sirenBool = preferences.getBoolean("sirenPreference", false);
        speedBool = preferences.getBoolean("speedPreference", true);

        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        message = SmsMessage.createFromPdu((byte[]) pdus[0]);
        originatingAddress = message.getOriginatingAddress();
        Log.i("Originating Address: ", originatingAddress);
        Log.i("Received Message: ", message.getMessageBody());

        MainActivity.radioInt = preferences.getInt("radioPrefs", 0);

        if (MainActivity.radioInt == 0) {
            messageHandler();
        } else if (MainActivity.radioInt == 1) {
            if (Helper.contactExists(mContext, originatingAddress, mContext.getContentResolver())) {
                messageHandler();
            }
        } else if (MainActivity.radioInt == 2) {
            String selectedContacts = preferences.getString("checkedContactsPrefs", " ");
            if (PhoneNumberUtils.compare(selectedContacts, originatingAddress)) {
                messageHandler();
            }
        }
    }

    public void messageHandler() {
        if (message.getMessageBody().contains(preferences.getString("trackerVariablePrefs", "TBgps"))) {
            if (trackerBool) {
                gps = new LocationService(mContext);
                if (!helper.isAnyLocationServiceAvailable()) {
                    Log.i("Tracker", "Location Service disabled. Sending SMS...");
                    helper.sendSms(originatingAddress, "TrackBuddy:\n\nLocation Service of the target device is disabled from the Android System Settings.");
                } else {
                    gps.acquireLocation();
                }
            } else {
                Log.i("TrackBuddy_Tracker", "TrackerSwitched OFF. Sending SMS...");
                helper.sendSms(originatingAddress, "TrackBuddy:\n\nTracking Service of the target device is switched off from the TrackBuddy application.");
            }
        } else if (message.getMessageBody().contains(preferences.getString("sirenVariablePrefs", "TBsiren"))) {
            originatingAddress = message.getOriginatingAddress();
            if (sirenBool) {
                helper.playSiren();
                Log.i("Siren", "Siren produced. Sending SMS...");
                helper.sendSms(originatingAddress, "TrackBuddy:\n\nSiren Message successfully received.");
            } else {
                Log.i("TrackBuddy_Siren", "Siren Switched OFF. Sending SMS...");
                helper.sendSms(originatingAddress, "TrackBuddy:\n\nSiren Service of the target device is switched off from the TrackBuddy application.");
            }
        } else if (message.getMessageBody().contains(preferences.getString("speedVariablePrefs", "TBspeed"))) {
            originatingAddress = message.getOriginatingAddress();
            if (speedBool) {
                if (!helper.isSpeedAcquirable()) {
                    Log.i("Speed", "GPS Service disabled. Sending SMS...");
                    helper.sendSms(originatingAddress, "TrackBuddy:\n\nGPS Service of the target device is disabled from the Android System Settings.");
                } else {
                    gps = new LocationService(mContext);
                    gps.acquireSpeed();
                }
            } else {
                Log.i("Speed", "Speed Switched OFF. Sending SMS...");
                helper.sendSms(originatingAddress, "TrackBuddy:\n\nSpeed Service of the target device is switched off from the TrackBuddy application.");
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {}
    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}
}
