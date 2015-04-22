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

    private DrawerLayout drawerLayout;
    private ListView listView;
    private ActionBarDrawerToggle drawerListener;

    Button trackerApplyButton, sirenApplyButton, speedApplyButton, okButton;
    RadioGroup radioGroup;
    EditText trackerEditText, sirenEditText, speedEditText;
    TextView trackerSMSCode, sirenSMSCode, speedSMSCode,topInfoMainLayout ;
    SharedPreferences preferences;
    String trackerVariable, sirenVariable, speedVariable;
    Dialog dialog;
    CheckBox gpsSettingsCheckbox, trackerCheckbox;
    View topLevelLayout, gpsSettingsLayout;
    RelativeLayout warningGooglePlayservices;
    ListView lv;
    ContactsAdapter ma;
    int positionGlobal = -1;
    final int dummyPosition = -1;
    static int radioInt;

    LayoutInflater layoutInflater;
    
    private static class Settings {
        final static int TRACKER = 0;
        final static int SIREN = 1;
        final static int SPEED = 2;
        final static int WHITELIST = 3;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Home");
        Helper mHelpers = new Helper(this);



        preferences = mHelpers.getPreferenceManager();

        if (mHelpers.isFirstTime(this)) {
            topLevelLayout.setVisibility(View.INVISIBLE);
        }

        layoutInflater = getLayoutInflater();

        trackerVariable = preferences.getString("trackerVariablePrefs", "TBgps");
        trackerSMSCode = (TextView) findViewById(R.id.trackerSMSCode);
        trackerSMSCode.setText("Tracker Code: " + trackerVariable);

        sirenVariable = preferences.getString("sirenVariablePrefs", "TBsiren");
        sirenSMSCode = (TextView) findViewById(R.id.sirenSMSCode);
        sirenSMSCode.setText("Siren Code: " + sirenVariable);

        speedVariable = preferences.getString("speedVariablePrefs", "TBspeed");
        speedSMSCode = (TextView) findViewById(R.id.speedSMSCode);
        speedSMSCode.setText("Speed Code: " + speedVariable);

        topInfoMainLayout = (TextView) findViewById(R.id.topInfo);
        warningGooglePlayservices = (RelativeLayout) findViewById(R.id.playservices_layout);

        DrawerAdapter myAdapter = new DrawerAdapter(this);

        listView = (ListView) findViewById(R.id.drawer_list);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListener = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                popDialog(positionGlobal);
            }
        };
        drawerLayout.setDrawerListener(drawerListener);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerListener.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerListener.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerListener.syncState();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        selectItem(position);
        drawerLayout.closeDrawer(listView);
    }

    private void selectItem(int position) {
        listView.setItemChecked(position, true);
        setTitle(DrawerAdapter.items[position]);
        positionGlobal = position;
    }

    private void popDialog(int window) {
        dialog = new Dialog(MainActivity.this, R.style.PauseDialog);

        switch (window) {
            case Settings.TRACKER:
                RelativeLayout trackerLayout = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_one, null);
                Switch trackerSwitch = (Switch) trackerLayout.findViewById(R.id.switchTracker);
                trackerSwitch.setChecked(preferences.getBoolean("trackerPreference", true));
                trackerSwitch.setOnCheckedChangeListener(this);

                trackerApplyButton = (Button) trackerLayout.findViewById(R.id.applyButtonTracker);
                trackerEditText = (EditText) trackerLayout.findViewById(R.id.editTextTracker);
                trackerCheckbox = (CheckBox) trackerLayout.findViewById(R.id.trackerCheckbox);
                trackerCheckbox.setChecked(preferences.getBoolean("trackerCheckboxPrefs", false));
                trackerCheckbox.setOnCheckedChangeListener(this);
                setOnTextChangeListenerForInputField(trackerEditText, trackerApplyButton);
                setOnClickListenerForEditText(trackerEditText);
                trackerApplyButton.setOnClickListener(this);

                initiateDialog("Tracker", trackerLayout);
                break;
            case Settings.SIREN:
                RelativeLayout sirenLayout = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_two, null);
                final Switch sirenSwitch = (Switch) sirenLayout.findViewById(R.id.switchSiren);
                sirenSwitch.setChecked(preferences.getBoolean("sirenPreference", false));
                sirenSwitch.setOnCheckedChangeListener(this);

                sirenApplyButton = (Button) sirenLayout.findViewById(R.id.applyButtonSiren);
                sirenEditText = (EditText) sirenLayout.findViewById(R.id.editTextSiren);
                setOnTextChangeListenerForInputField(sirenEditText, sirenApplyButton);
                setOnClickListenerForEditText(sirenEditText);
                sirenApplyButton.setOnClickListener(this);

                initiateDialog("Siren", sirenLayout);
                break;
            case Settings.SPEED:
                RelativeLayout speedLayout = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_three, null);
                final Switch speedSwitch = (Switch) speedLayout.findViewById(R.id.switchSpeed);
                speedSwitch.setChecked(preferences.getBoolean("speedPreference", true));
                speedSwitch.setOnCheckedChangeListener(this);

                speedApplyButton = (Button) speedLayout.findViewById(R.id.applyButtonSpeed);
                speedEditText = (EditText) speedLayout.findViewById(R.id.editTextSpeed);
                setOnTextChangeListenerForInputField(speedEditText, speedApplyButton);
                setOnClickListenerForEditText(speedEditText);
                speedApplyButton.setOnClickListener(this);

                initiateDialog("Speed", speedLayout);
                break;
            case Settings.WHITELIST:
                RelativeLayout whitelistLayout = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_four, null);
                radioGroup = (RadioGroup) whitelistLayout.findViewById(R.id.radioGroup);

                radioInt = preferences.getInt("radioPrefs", 0);

                ma = new ContactsAdapter(getApplicationContext());
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
                                preferences.edit().putInt("radioPrefs", 0).apply();
                            break;
                            case R.id.radioButtonTwo:
                                lv.setVisibility(View.GONE);
                                preferences.edit().putInt("radioPrefs", 1).apply();
                            break;
                            case R.id.radioButtonThree:
                                lv.setVisibility(View.VISIBLE);
                                preferences.edit().putInt("radioPrefs", 2).apply();
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
        positionGlobal = dummyPosition;
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
                preferences.edit().putBoolean("trackerPreference", isChecked).apply();
            break;
            case R.id.switchSiren:
                preferences.edit().putBoolean("sirenPreference", isChecked).apply();
            break;
            case R.id.switchSpeed:
                preferences.edit().putBoolean("speedPreference", isChecked).apply();
            break;
            case R.id.trackerCheckbox:
                preferences.edit().putBoolean("trackerCheckboxPrefs", isChecked).apply();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.applyButtonTracker:
                trackerVariable = trackerEditText.getText().toString();
                trackerVariable = trackerVariable.replaceAll("\\W", "");
                trackerEditText.getText().clear();
                preferences.edit().putString("trackerVariablePrefs", trackerVariable).apply();
                trackerSMSCode.setText("Tracker Code: " + trackerVariable);
                dialog.dismiss();
            break;
            case R.id.applyButtonSiren:
                sirenVariable = sirenEditText.getText().toString();
                sirenVariable = sirenVariable.replaceAll("\\W", "");
                sirenEditText.getText().clear();
                preferences.edit().putString("sirenVariablePrefs", sirenVariable).apply();
                sirenSMSCode.setText("Siren Code: " + sirenVariable);
                dialog.dismiss();
            break;
            case R.id.applyButtonSpeed:
                speedVariable = speedEditText.getText().toString();
                speedVariable = speedVariable.replaceAll("\\W", "");
                speedEditText.getText().clear();
                preferences.edit().putString("speedVariablePrefs", speedVariable).apply();
                speedSMSCode.setText("Speed Code: " + speedVariable);
                dialog.dismiss();
            break;
        }
    }

    private void initiateDialog(String title, RelativeLayout layout) {
        dialog.setTitle(title);
        dialog.setContentView(layout);
        dialog.show();
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
            topInfoMainLayout.setVisibility(View.GONE);
            trackerSMSCode.setVisibility(View.GONE);
            sirenSMSCode.setVisibility(View.GONE);
            speedSMSCode.setVisibility(View.GONE);

            warningGooglePlayservices.setVisibility(View.VISIBLE);
        } else {
            warningGooglePlayservices.setVisibility(View.GONE);

            topInfoMainLayout.setVisibility(View.VISIBLE);
            trackerSMSCode.setVisibility(View.VISIBLE);
            sirenSMSCode.setVisibility(View.VISIBLE);
            speedSMSCode.setVisibility(View.VISIBLE);
        }

        warningGooglePlayservices.setOnClickListener(new View.OnClickListener() {
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