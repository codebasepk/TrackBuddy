package com.byteshaft.trackbuddy;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends ActionBarActivity implements ListView.OnItemClickListener,
        Switch.OnCheckedChangeListener, Button.OnClickListener {

    private DrawerLayout mDrawerLayout;
    private ListView mListView;
    private ActionBarDrawerToggle mDrawerListener;

    Button sButtonOk;
    private EditText mTrackerCodeChangeEntry, mSirenCodeChangeEntry, mSpeedTrackingCodeChangeEntry;
    private TextView mTrackerSmsCodeLabel, mSirenSmsCodeLabel, mSpeedTrackerSmsCodeLabel, mTopInfoMainLayout;
    private SharedPreferences mPreferences;
    private String trackerVariable, sirenVariable, speedVariable;
    private Dialog mDialog;
    CheckBox gpsSettingsCheckbox;
    View topLevelLayout, gpsSettingsLayout;
    private RelativeLayout warningGooglePlayServices;
    private ListView lv;
    int mPositionGlobal = -1;
    final int DUMMY_POSITION = -1;
    static int radioInt;

    private LayoutInflater mLayoutInflater;
    
    private static class Settings {
        final static int TRACKER = 0;
        final static int SIREN = 1;
        final static int SPEED = 2;
        final static int WHITE_LIST = 3;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Home");
        Helper mHelpers = new Helper(this);



        mPreferences = mHelpers.getPreferenceManager();

        if (mHelpers.isFirstTime(this)) {
            topLevelLayout.setVisibility(View.INVISIBLE);
        }

        mLayoutInflater = getLayoutInflater();

        trackerVariable = mPreferences.getString("trackerVariablePrefs", "TBgps");
        mTrackerSmsCodeLabel = (TextView) findViewById(R.id.trackerSMSCode);
        mTrackerSmsCodeLabel.setText("Tracker Code: " + trackerVariable);

        sirenVariable = mPreferences.getString("sirenVariablePrefs", "TBsiren");
        mSirenSmsCodeLabel = (TextView) findViewById(R.id.sirenSMSCode);
        mSirenSmsCodeLabel.setText("Siren Code: " + sirenVariable);

        speedVariable = mPreferences.getString("speedVariablePrefs", "TBspeed");
        mSpeedTrackerSmsCodeLabel = (TextView) findViewById(R.id.speedSMSCode);
        mSpeedTrackerSmsCodeLabel.setText("Speed Code: " + speedVariable);

        mTopInfoMainLayout = (TextView) findViewById(R.id.topInfo);
        warningGooglePlayServices = (RelativeLayout) findViewById(R.id.playservices_layout);

        DrawerAdapter myAdapter = new DrawerAdapter(this);

        mListView = (ListView) findViewById(R.id.drawer_list);
        mListView.setAdapter(myAdapter);
        mListView.setOnItemClickListener(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListener = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                popDialog(mPositionGlobal);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerListener);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerListener.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerListener.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerListener.syncState();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        selectItem(position);
        mDrawerLayout.closeDrawer(mListView);
    }

    private void selectItem(int position) {
        mListView.setItemChecked(position, true);
        setTitle(DrawerAdapter.mDrawerItems[position]);
        mPositionGlobal = position;
    }

    private void popDialog(int window) {
        mDialog = new Dialog(MainActivity.this, R.style.PauseDialog);

        switch (window) {
            case Settings.TRACKER:
                RelativeLayout trackerLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.dialog_one, null);
                Switch trackerSwitch = (Switch) trackerLayout.findViewById(R.id.switchTracker);
                trackerSwitch.setChecked(mPreferences.getBoolean("trackerPreference", true));
                trackerSwitch.setOnCheckedChangeListener(this);

                Button trackerApplyButton = (Button) trackerLayout.findViewById(R.id.applyButtonTracker);
                mTrackerCodeChangeEntry = (EditText) trackerLayout.findViewById(R.id.editTextTracker);
                CheckBox trackerCheckbox = (CheckBox) trackerLayout.findViewById(R.id.trackerCheckbox);
                trackerCheckbox.setChecked(mPreferences.getBoolean("trackerCheckboxPrefs", false));
                trackerCheckbox.setOnCheckedChangeListener(this);
                setOnTextChangeListenerForInputField(mTrackerCodeChangeEntry, trackerApplyButton);
                setOnClickListenerForEditText(mTrackerCodeChangeEntry);
                trackerApplyButton.setOnClickListener(this);

                initiateDialog("Tracker", trackerLayout);
                break;
            case Settings.SIREN:
                RelativeLayout sirenLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.dialog_two, null);
                final Switch sirenSwitch = (Switch) sirenLayout.findViewById(R.id.switchSiren);
                sirenSwitch.setChecked(mPreferences.getBoolean("sirenPreference", false));
                sirenSwitch.setOnCheckedChangeListener(this);

                Button sirenApplyButton = (Button) sirenLayout.findViewById(R.id.applyButtonSiren);
                mSirenCodeChangeEntry = (EditText) sirenLayout.findViewById(R.id.editTextSiren);
                setOnTextChangeListenerForInputField(mSirenCodeChangeEntry, sirenApplyButton);
                setOnClickListenerForEditText(mSirenCodeChangeEntry);
                sirenApplyButton.setOnClickListener(this);

                initiateDialog("Siren", sirenLayout);
                break;
            case Settings.SPEED:
                RelativeLayout speedLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.dialog_three, null);
                final Switch speedSwitch = (Switch) speedLayout.findViewById(R.id.switchSpeed);
                speedSwitch.setChecked(mPreferences.getBoolean("speedPreference", true));
                speedSwitch.setOnCheckedChangeListener(this);

                Button speedApplyButton = (Button) speedLayout.findViewById(R.id.applyButtonSpeed);
                mSpeedTrackingCodeChangeEntry = (EditText) speedLayout.findViewById(R.id.editTextSpeed);
                setOnTextChangeListenerForInputField(mSpeedTrackingCodeChangeEntry, speedApplyButton);
                setOnClickListenerForEditText(mSpeedTrackingCodeChangeEntry);
                speedApplyButton.setOnClickListener(this);

                initiateDialog("Speed", speedLayout);
                break;
            case Settings.WHITE_LIST:
                RelativeLayout whitelistLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.dialog_four, null);
                RadioGroup radioGroup = (RadioGroup) whitelistLayout.findViewById(R.id.radioGroup);

                radioInt = mPreferences.getInt("radioPrefs", 0);

                ContactsAdapter ma = new ContactsAdapter(getApplicationContext());
                lv = (ListView) whitelistLayout.findViewById(R.id.lv);

                if (radioInt == 0) {
                    radioGroup.check(R.id.radioButtonOne);
                } else if (radioInt == 1) {
                    radioGroup.check(R.id.radioButtonTwo);
                } else if (radioInt == 2) {
                    radioGroup.check(R.id.radioButtonThree);
                    lv.setVisibility(View.VISIBLE);
                }

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        switch (checkedId) {
                            case R.id.radioButtonOne:
                                lv.setVisibility(View.GONE);
                                mPreferences.edit().putInt("radioPrefs", 0).apply();
                                break;
                            case R.id.radioButtonTwo:
                                lv.setVisibility(View.GONE);
                                mPreferences.edit().putInt("radioPrefs", 1).apply();
                                break;
                            case R.id.radioButtonThree:
                                lv.setVisibility(View.VISIBLE);
                                mPreferences.edit().putInt("radioPrefs", 2).apply();
                                break;
                        }
                    }
                });

                lv.setAdapter(ma);
                lv.setOnItemClickListener(this);
                lv.setItemsCanFocus(false);
                lv.setTextFilterEnabled(true);

                initiateDialog("Whitelist", whitelistLayout);
                break;
        }
        mPositionGlobal = DUMMY_POSITION;
    }

    private void setOnTextChangeListenerForInputField(final EditText editText, final Button button) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getText().toString().isEmpty()) {
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
                if (editText.getText().toString().equals("TB")) {
                    return;
                }
                if (editText.getText().toString().isEmpty() || editText.getText().toString().equals("T")) {
                    editText.setText("TB");
                    editText.setSelection(editText.getText().length());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().replaceAll(" ", "");
                    if(!s.toString().equals(result)) {
                        editText.setText(result);
                        editText.setSelection(result.length());
                    }
            }
        });
    }

    private void setOnClickListenerForEditText(final EditText editText) {
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().isEmpty() || editText.getText().toString().equals("T")) {
                    editText.setText("TB");
                    editText.setSelection(editText.getText().length());
                } else {
                    editText.setSelection(editText.getText().length());
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switchTracker:
                mPreferences.edit().putBoolean("trackerPreference", isChecked).apply();
            break;
            case R.id.switchSiren:
                mPreferences.edit().putBoolean("sirenPreference", isChecked).apply();
            break;
            case R.id.switchSpeed:
                mPreferences.edit().putBoolean("speedPreference", isChecked).apply();
            break;
            case R.id.trackerCheckbox:
                mPreferences.edit().putBoolean("trackerCheckboxPrefs", isChecked).apply();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.applyButtonTracker:
                trackerVariable = mTrackerCodeChangeEntry.getText().toString();
                mTrackerCodeChangeEntry.getText().clear();
                mPreferences.edit().putString("trackerVariablePrefs", trackerVariable).apply();
                mTrackerSmsCodeLabel.setText("Tracker Code: " + trackerVariable);
                mDialog.dismiss();
            break;
            case R.id.applyButtonSiren:
                sirenVariable = mSirenCodeChangeEntry.getText().toString();
                mSirenCodeChangeEntry.getText().clear();
                mPreferences.edit().putString("sirenVariablePrefs", sirenVariable).apply();
                mSirenSmsCodeLabel.setText("Siren Code: " + sirenVariable);
                mDialog.dismiss();
            break;
            case R.id.applyButtonSpeed:
                speedVariable = mSpeedTrackingCodeChangeEntry.getText().toString();
                mSpeedTrackingCodeChangeEntry.getText().clear();
                mPreferences.edit().putString("speedVariablePrefs", speedVariable).apply();
                mSpeedTrackerSmsCodeLabel.setText("Speed Code: " + speedVariable);
                mDialog.dismiss();
            break;
        }
    }

    private void initiateDialog(String title, RelativeLayout layout) {
        mDialog.setTitle(title);
        mDialog.setContentView(layout);
        mDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showGooglePlayServicesError();
    }

    public void showGooglePlayServicesError() {

        int googlePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable
                (getApplicationContext());

        if (googlePlayServicesAvailable != ConnectionResult.SUCCESS) {
            mTopInfoMainLayout.setVisibility(View.GONE);
            mTrackerSmsCodeLabel.setVisibility(View.GONE);
            mSirenSmsCodeLabel.setVisibility(View.GONE);
            mSpeedTrackerSmsCodeLabel.setVisibility(View.GONE);

            warningGooglePlayServices.setVisibility(View.VISIBLE);
        } else {
            warningGooglePlayServices.setVisibility(View.GONE);

            mTopInfoMainLayout.setVisibility(View.VISIBLE);
            mTrackerSmsCodeLabel.setVisibility(View.VISIBLE);
            mSirenSmsCodeLabel.setVisibility(View.VISIBLE);
            mSpeedTrackerSmsCodeLabel.setVisibility(View.VISIBLE);
        }

        warningGooglePlayServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms&hl=en"));
                startActivity(intent);
            }
        });
    }
}