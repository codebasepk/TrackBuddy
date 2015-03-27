package com.byteshaft.trackbuddy;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

public class Helper extends ContextWrapper {

    public Helper(Context base) {
        super(base);
    }

    boolean isLocationServiceAvailable() {
        LocationManager locationManager = getLocationManager();
        return isGpsEnabled(locationManager) && isNetworkBasedGpsEnabled(locationManager);
    }

    private LocationManager getLocationManager() {
        return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private boolean isGpsEnabled(LocationManager locationManager) {
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
}
