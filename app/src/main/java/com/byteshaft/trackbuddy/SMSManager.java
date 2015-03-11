package com.byteshaft.trackbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.browse.MediaBrowser;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.ContextThemeWrapper;


public class SMSManager extends BroadcastReceiver {

    private static final String clipBoard = "BUDDY";
    SmsMessage message = null;
    private String phoneNumber;
    SmsManager sms = SmsManager.getDefault();

    @Override

    public void onReceive(Context context, Intent intent) {
        LocationService gps = new LocationService(context);
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        message = SmsMessage.createFromPdu((byte[]) pdus[0]);

        Log.i(clipBoard, message.getMessageBody());
        if (message.getMessageBody().contains("TB007")) {
            phoneNumber = message.getOriginatingAddress();
            gps.getLocation();

            if (!gps.isNetworkEnabled && !gps.isGPSEnabled) {
               sms.sendTextMessage(phoneNumber, null, "TrackBuddy: Location Service of the Target Device is Disabled.", null, null);
            }
            else {
                LocationService.latitude = gps.getLatitude();
                LocationService.longitude = gps.getLongitude();


                if (LocationService.latitude == 0.0) {
                   sms.sendTextMessage(phoneNumber, null, "TrackBuddy: Target Device can't be Located at the moment.", null, null);
                } else {
                    sms.sendTextMessage(phoneNumber, null, "TrackBuddy: " + "https://maps.google.com/maps?q=" + LocationService.latitude + "," + LocationService.longitude, null, null);
                    System.out.println(LocationService.latitude);
                }
            }
        }
    }
}


