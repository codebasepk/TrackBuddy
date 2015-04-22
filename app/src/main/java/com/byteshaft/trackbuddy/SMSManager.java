package com.byteshaft.trackbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class SMSManager extends BroadcastReceiver {

    SmsMessage message = null;
    Context mContext = null;
    LocationService gps;
    static String originatingAddress;
    SharedPreferences preferences;
    Helper helper;
    boolean trackerBool, sirenBool, speedBool;
    static boolean trackerCheckbox;
    int googlePlayServicesAvailable;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        helper = new Helper(context);
        googlePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable
                (mContext.getApplicationContext());

        preferences = helper.getPreferenceManager();
        trackerBool = preferences.getBoolean("trackerPreference", true);
        sirenBool = preferences.getBoolean("sirenPreference", false);
        speedBool = preferences.getBoolean("speedPreference", true);

        trackerCheckbox = preferences.getBoolean("trackerCheckboxPrefs", false);

        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        message = SmsMessage.createFromPdu((byte[]) pdus[0]);
        Log.i("TrackBuddy", "Received Message: " + message.getMessageBody());

        MainActivity.radioInt = preferences.getInt("radioPrefs", 0);

        if (MainActivity.radioInt == 0) {
            messageHandler();
        } else if (MainActivity.radioInt == 1) {
            if (helper.contactExists(originatingAddress, mContext.getContentResolver())) {
                messageHandler();
            }
        } else if (MainActivity.radioInt == 2) {
            String selectedContacts = preferences.getString("checkedContactsPrefs", " ");
            if (helper.contactExistsInWhitelist(originatingAddress, selectedContacts)) {
                messageHandler();
            }
        }
    }

    public void messageHandler() {
        if (message.getMessageBody().contains(preferences.getString("trackerVariablePrefs", "TBgps"))) {
            originatingAddress = message.getOriginatingAddress();
            Log.i("TrackBuddy", "Originating Address: " + originatingAddress);
             if (trackerBool) {
                    if (!helper.isAnyLocationServiceAvailable()) {
                        Log.i("TrackerBuddy", "Location Service disabled. Sending SMS...");
                        helper.sendSms(originatingAddress, "TrackBuddy" +
                                "\n\nLocation Service of the target device is disabled from the Android System Settings."
                        );
                    } else {
                        if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
                            gps = new LocationService(mContext);
                            gps.acquireLocation();
                            Log.i("TrackBuddy", "GoogleApiClient successfully connected");
                        } else {
                            helper.sendSms(originatingAddress, "TrackBuddy" +
                                    "\n\nUnable to acquire location." +
                                    "\nLatest GooglePlayServices are not installed on the target device."
                            );
                            Log.i("TrackBuddy", "Unable to connect GoogleApiClient.");
                        }
                    }
             } else {
                    Log.i("TrackBuddy", "TrackerSwitched OFF. Sending SMS...");
                    helper.sendSms(originatingAddress, "TrackBuddy" +
                            "\n\nTracking feature of the target device is switched off from the TrackBuddy application."
                    );
             }
        } else if (message.getMessageBody().contains(preferences.getString("sirenVariablePrefs", "TBsiren"))) {
            originatingAddress = message.getOriginatingAddress();
            Log.i("TrackBuddy", "Originating Address: " + originatingAddress);
            if (sirenBool) {
                helper.playSiren();
                Log.i("TrackBuddy", "Siren emitted. Sending SMS...");
                helper.sendSms(originatingAddress, "TrackBuddy" +
                        "\n\nSiren Message successfully received."
                );
            } else {
                Log.i("TrackBuddy", "Siren Switched OFF. Sending SMS...");
                helper.sendSms(originatingAddress, "TrackBuddy" +
                        "\n\nSiren feature of the target device is switched off from the TrackBuddy application."
                );
            }
        } else if (message.getMessageBody().contains(preferences.getString("speedVariablePrefs", "TBspeed"))) {
            originatingAddress = message.getOriginatingAddress();
            Log.i("TrackBuddy", "Originating Address: " + originatingAddress);
            if (speedBool) {
                if (!helper.isSpeedAcquirable()) {
                    Log.i("TrackBuddy", "GPS Service disabled. Sending SMS...");
                    helper.sendSms(originatingAddress, "TrackBuddy" +
                            "\n\nGPS Service of the target device is disabled from the Android System Settings."
                    );
                } else {
                    if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
                        gps = new LocationService(mContext);
                        gps.acquireSpeed();
                    } else {
                    helper.sendSms(originatingAddress, "TrackBuddy" +
                            "\n\nUnable to acquire speed." +
                            "\nLatest GooglePlayServices are not installed on the target device."
                    );
                    Log.i("TrackBuddy", "Unable to connect GoogleApiClient.");
                    }
                }
            } else {
                Log.i("TrackBuddy", "Speed Switched OFF. Sending SMS...");
                helper.sendSms(originatingAddress, "TrackBuddy" +
                        "\n\nSpeed feature of the target device is switched off from the TrackBuddy application."
                );
            }
        }
    }
}