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

    private SmsMessage mMessage;
    private Context mContext;
    static String sOriginatingAddress;
    private SharedPreferences mPreferences;
    private Helper mHelpers;
    private boolean mTrackerEnabled;
    private boolean mSirenEnabled;
    private boolean mSpeedTrackingEnabled;
    static boolean sTrackerCheckbox;
    private int mArePlayServicesAvailable;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mHelpers = new Helper(context);
        mArePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable
                (mContext.getApplicationContext());

        mPreferences = mHelpers.getPreferenceManager();
        mTrackerEnabled = mPreferences.getBoolean("trackerPreference", true);
        mSirenEnabled = mPreferences.getBoolean("sirenPreference", false);
        mSpeedTrackingEnabled = mPreferences.getBoolean("speedPreference", true);

        sTrackerCheckbox = mPreferences.getBoolean("sTrackerCheckboxPrefs", false);

        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        mMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
        Log.i("TrackBuddy", "Received Message: " + mMessage.getMessageBody());

        MainActivity.radioInt = mPreferences.getInt("radioPrefs", 0);

        if (MainActivity.radioInt == 0) {
            messageHandler();
        } else if (MainActivity.radioInt == 1) {
            if (mHelpers.contactExists(sOriginatingAddress, mContext.getContentResolver())) {
                messageHandler();
            }
        } else if (MainActivity.radioInt == 2) {
            String selectedContacts = mPreferences.getString("checkedContactsPrefs", " ");
            if (mHelpers.contactExistsInWhitelist(sOriginatingAddress, selectedContacts)) {
                messageHandler();
            }
        }
    }

    public void messageHandler() {
        LocationService gps;
        if (mMessage.getMessageBody().contains(mPreferences.getString("trackerVariablePrefs", "TBgps"))) {
            sOriginatingAddress = mMessage.getOriginatingAddress();
            Log.i("TrackBuddy", "Originating Address: " + sOriginatingAddress);
             if (mTrackerEnabled) {
                    if (!mHelpers.isAnyLocationServiceAvailable()) {
                        Log.i("TrackerBuddy", "Location Service disabled. Sending SMS...");
                        mHelpers.sendSms(sOriginatingAddress, "TrackBuddy" +
                                "\n\nLocation Service of the target device is disabled from the Android System Settings."
                        );
                    } else {
                        if (mArePlayServicesAvailable == ConnectionResult.SUCCESS) {
                            gps = new LocationService(mContext);
                            gps.acquireLocation();
                            Log.i("TrackBuddy", "GoogleApiClient successfully connected");
                        } else {
                            mHelpers.sendSms(sOriginatingAddress, "TrackBuddy" +
                                    "\n\nUnable to acquire location." +
                                    "\nLatest GooglePlayServices are not installed on the target device."
                            );
                            Log.i("TrackBuddy", "Unable to connect GoogleApiClient.");
                        }
                    }
             } else {
                    Log.i("TrackBuddy", "TrackerSwitched OFF. Sending SMS...");
                    mHelpers.sendSms(sOriginatingAddress, "TrackBuddy" +
                            "\n\nTracking feature of the target device is switched off from the TrackBuddy application."
                    );
             }
        } else if (mMessage.getMessageBody().contains(mPreferences.getString("sirenVariablePrefs", "TBsiren"))) {
            sOriginatingAddress = mMessage.getOriginatingAddress();
            Log.i("TrackBuddy", "Originating Address: " + sOriginatingAddress);
            if (mSirenEnabled) {
                mHelpers.playSiren();
                Log.i("TrackBuddy", "Siren emitted. Sending SMS...");
                mHelpers.sendSms(sOriginatingAddress, "TrackBuddy" +
                        "\n\nSiren Message successfully received."
                );
            } else {
                Log.i("TrackBuddy", "Siren Switched OFF. Sending SMS...");
                mHelpers.sendSms(sOriginatingAddress, "TrackBuddy" +
                        "\n\nSiren feature of the target device is switched off from the TrackBuddy application."
                );
            }
        } else if (mMessage.getMessageBody().contains(mPreferences.getString("speedVariablePrefs", "TBspeed"))) {
            sOriginatingAddress = mMessage.getOriginatingAddress();
            Log.i("TrackBuddy", "Originating Address: " + sOriginatingAddress);
            if (mSpeedTrackingEnabled) {
                if (!mHelpers.isSpeedAcquirable()) {
                    Log.i("TrackBuddy", "GPS Service disabled. Sending SMS...");
                    mHelpers.sendSms(sOriginatingAddress, "TrackBuddy" +
                            "\n\nGPS Service of the target device is disabled from the Android System Settings."
                    );
                } else {
                    if (mArePlayServicesAvailable == ConnectionResult.SUCCESS) {
                        gps = new LocationService(mContext);
                        gps.acquireSpeed();
                    } else {
                    mHelpers.sendSms(sOriginatingAddress, "TrackBuddy" +
                            "\n\nUnable to acquire speed." +
                            "\nLatest GooglePlayServices are not installed on the target device."
                    );
                    Log.i("TrackBuddy", "Unable to connect GoogleApiClient.");
                    }
                }
            } else {
                Log.i("TrackBuddy", "Speed Switched OFF. Sending SMS...");
                mHelpers.sendSms(sOriginatingAddress, "TrackBuddy" +
                        "\n\nSpeed feature of the target device is switched off from the TrackBuddy application."
                );
            }
        }
    }
}
