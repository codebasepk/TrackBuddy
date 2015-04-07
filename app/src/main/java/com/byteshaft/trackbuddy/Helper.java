package com.byteshaft.trackbuddy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;


public class Helper extends ContextWrapper {

    public Helper(Context base) {
        super(base);
    }

//    boolean isLocationServiceAvailable() {
//        LocationManager locationManager = getLocationManager();
//        return isGpsEnabled(locationManager) && isNetworkBasedGpsEnabled(locationManager);
//    }

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
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
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
            fadeOut.setDuration(500);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(myView, "alpha", .3f, 1f);
            fadeIn.setDuration(500);

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
            editor.commit();

            activity.gpsSettingsCheckbox = (CheckBox) activity.findViewById(R.id.checkbox);

            activity.okButton = (Button) activity.findViewById(R.id.okButton);
            activity.okButton.setOnClickListener(activity);
            activity.okButton.setOnTouchListener(new View.OnTouchListener(){

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (!isAnyLocationServiceAvailable() && activity.gpsSettingsCheckbox.isChecked()) {
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
}



