package com.byteshaft.trackbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.ContextThemeWrapper;


public class SMSManager extends BroadcastReceiver  {

    private static final String clipBoard = "BUDDY";
    SmsMessage message = null;

    @Override

    public void onReceive(Context context, Intent intent) {
        LocationService gps = new LocationService(context);
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        message = SmsMessage.createFromPdu((byte[]) pdus[0]);

        Log.i(clipBoard, message.getMessageBody());
        if(message.getMessageBody().contains("yo")) {
            gps.getLocation();
            if (gps.canGetLocation) {
             MainActivity.mEditText2.setText("https://maps.google.com/maps?q" + MainActivity.latitude + "," + MainActivity.longitude);
            }
        }
        }
        }


