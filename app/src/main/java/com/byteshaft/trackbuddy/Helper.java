package com.byteshaft.trackbuddy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class Helper extends ContextWrapper {

    public Helper(Context base) {
        super(base);
    }

    boolean isAnyLocationServiceAvailable() {
        LocationManager locationManager = getLocationManager();
        return isGpsEnabled(locationManager) || isNetworkBasedGpsEnabled(locationManager);
    }

    boolean isSpeedAcquirable() {
        LocationManager locationManager = getLocationManager();
        return isGpsEnabled(locationManager);
    }

    private LocationManager getLocationManager() {
        return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    boolean isGpsEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isNetworkBasedGpsEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER));
    }

    void sendSms(String phoneNumber, String message) {
        SmsManager smsManager = getSmsManager();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    private SmsManager getSmsManager() {
        return SmsManager.getDefault();
    }

    void playSiren() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.siren);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = getAudioManager();
        setVolumeToMax(audioManager);
        mediaPlayer.start();
    }

    private AudioManager getAudioManager() {
        return (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private void setVolumeToMax(AudioManager audioManager) {
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);
    }

    SharedPreferences getPreferenceManager() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

     boolean isFirstTime(final MainActivity activity) {

         activity.topLevelLayout = activity.findViewById(R.id.top_layout);
         activity.gpsSettingsLayout = activity.findViewById(R.id.gpsSettingsLayout);

        SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            activity.topLevelLayout.setVisibility(View.VISIBLE);
            if (!isAnyLocationServiceAvailable()) {
               activity.gpsSettingsLayout.setVisibility(View.VISIBLE);
            }
            ImageView myView = (ImageView) activity.findViewById(R.id.arrowImage);

            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(myView, "alpha",  1f, .3f);
            fadeOut.setDuration(1200);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(myView, "alpha", .3f, 1f);
            fadeIn.setDuration(1200);

            final AnimatorSet mAnimationSet = new AnimatorSet();

            mAnimationSet.play(fadeIn).after(fadeOut);
            mAnimationSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mAnimationSet.start();
                }
            });
            mAnimationSet.start();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.apply();

            activity.gpsSettingsCheckbox = (CheckBox) activity.findViewById(R.id.checkbox);
            activity.sButtonOk = (Button) activity.findViewById(R.id.okButton);
            activity.sButtonOk.setOnTouchListener(new View.OnTouchListener(){

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (activity.gpsSettingsCheckbox.isChecked() && !isAnyLocationServiceAvailable()) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                    activity.topLevelLayout.setVisibility(View.INVISIBLE);
                    activity.gpsSettingsLayout.setVisibility(View.INVISIBLE);
                    return false;
                }
            });
        }
        return ranBefore;
    }

    private Cursor getAllContacts(ContentResolver cr) {
        return cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
    }

    List<String> getAllContactNames() {
        List<String> contactNames = new ArrayList<>();
        Cursor cursor = getAllContacts(getContentResolver());
        while (cursor.moveToNext()) {
            String name = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            contactNames.add(name);
        }
        cursor.close();
        return contactNames;
    }

    List<String> getAllContactNumbers() {
        List<String> contactNumbers = new ArrayList<>();
        Cursor cursor = getAllContacts(getContentResolver());
        while (cursor.moveToNext()) {
            String number = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactNumbers.add(number);
        }
        cursor.close();
        return contactNumbers;
    }

    public boolean contactExists(String number, ContentResolver contentResolver) {
        Cursor phones = getAllContacts(contentResolver);
        while (phones.moveToNext()){
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(PhoneNumberUtils.compare(number, phoneNumber)){
                return true;
            }
        }
        return false;
    }

    public boolean contactExistsInWhitelist(String number, String checkedContacts) {
        boolean contactExistsInWhitelist = false;
        String[] checkContactsArray = getCheckedContacts(checkedContacts);
        for(String contact : checkContactsArray) {
            if (PhoneNumberUtils.compare(contact, number)) {
                contactExistsInWhitelist = true;
            }
        }
        return contactExistsInWhitelist;
    }

    private String[] getCheckedContacts(String checkedContacts) {
        return checkedContacts.split(",");
    }
}
