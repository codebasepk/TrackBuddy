package com.byteshaft.trackbuddy;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ListView.OnItemClickListener,
        Switch.OnCheckedChangeListener, Button.OnClickListener {

    private DrawerLayout drawerLayout;
    private ListView listView;
    private ActionBarDrawerToggle drawerListener;

    Button trackerApplyButton;
    Button sirenApplyButton;
    Button speedApplyButton;

    EditText trackerEditText;
    EditText sirenEditText;
    EditText speedEditText;

    TextView trackerSMSCode;
    TextView sirenSMSCode;
    TextView speedSMSCode;

    SharedPreferences preferences;

    String trackerVariable;
    String sirenVariable;
    String speedVariable;

    Dialog dialog;

    int positionGlobal = -1;
    final int dummyPosition = -1;

    View topLevelLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Home");
        Helper helper = new Helper(this);

        preferences = helper.getPreferenceManager();

        topLevelLayout = findViewById(R.id.top_layout);



        if (isFirstTime()) {
            topLevelLayout.setVisibility(View.INVISIBLE);
        }

        trackerVariable = preferences.getString("trackerVariablePrefs", "TBgps");
        trackerSMSCode = (TextView) findViewById(R.id.trackerSMSCode);
        trackerSMSCode.setText("Tracker Code: " + trackerVariable);

        sirenVariable = preferences.getString("sirenVariablePrefs", "TBsiren");
        sirenSMSCode = (TextView) findViewById(R.id.sirenSMSCode);
        sirenSMSCode.setText("Siren Code: " + sirenVariable);

        speedVariable = preferences.getString("speedVariablePrefs", "TBspeed");
        speedSMSCode = (TextView) findViewById(R.id.speedSMSCode);
        speedSMSCode.setText("Speed Code: " + speedVariable);

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

    public void selectItem(int position) {
        listView.setItemChecked(position, true);
        setTitle(DrawerAdapter.items[position]);
        positionGlobal = position;
    }

    public void popDialog(int window) {
        dialog = new Dialog(MainActivity.this, R.style.PauseDialog);
        LayoutInflater layoutInflater = getLayoutInflater();

        switch (window) {
            case 0:
                RelativeLayout trackerRelativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_one, null);
                final Switch trackerSwitch = (Switch) trackerRelativeLayout.findViewById(R.id.switchTracker);
                boolean trackerPref = preferences.getBoolean("trackerPreference", true);
                trackerSwitch.setChecked(trackerPref);
                trackerSwitch.setOnCheckedChangeListener(this);

                trackerApplyButton = (Button) trackerRelativeLayout.findViewById(R.id.applyButtonTracker);
                trackerEditText = (EditText) trackerRelativeLayout.findViewById(R.id.editTextTracker);
                setOnTextChangeListenerForInputField(trackerEditText, trackerApplyButton);
                setOnClickListenerForEditText(trackerEditText);
                trackerApplyButton.setOnClickListener(this);
                initiateDialog("Tracker", trackerRelativeLayout);
                break;
            case 1:
                RelativeLayout sirenRelativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_two, null);
                final Switch sirenSwitch = (Switch) sirenRelativeLayout.findViewById(R.id.switchSiren);
                boolean sirenPref = preferences.getBoolean("sirenPreference", false);
                sirenSwitch.setChecked(sirenPref);
                sirenSwitch.setOnCheckedChangeListener(this);

                sirenApplyButton = (Button) sirenRelativeLayout.findViewById(R.id.applyButtonSiren);
                sirenEditText = (EditText) sirenRelativeLayout.findViewById(R.id.editTextSiren);
                setOnTextChangeListenerForInputField(sirenEditText, sirenApplyButton);
                setOnClickListenerForEditText(sirenEditText);
                sirenApplyButton.setOnClickListener(this);
                initiateDialog("Siren", sirenRelativeLayout);
                break;
            case 2:
                RelativeLayout speedRelativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_three, null);
                final Switch speedSwitch = (Switch) speedRelativeLayout.findViewById(R.id.switchSpeed);
                boolean speedPref = preferences.getBoolean("speedPreference", true);
                speedSwitch.setChecked(speedPref);
                speedSwitch.setOnCheckedChangeListener(this);

                speedApplyButton = (Button) speedRelativeLayout.findViewById(R.id.applyButtonSpeed);
                speedEditText = (EditText) speedRelativeLayout.findViewById(R.id.editTextSpeed);
                setOnTextChangeListenerForInputField(speedEditText, speedApplyButton);
                setOnClickListenerForEditText(speedEditText);
                speedApplyButton.setOnClickListener(this);
                initiateDialog("Speed", speedRelativeLayout);
                break;
            case 3:
                RelativeLayout blacklistWhitelistRelativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_four, null);
                initiateDialog("Blacklist/Whitelist", blacklistWhitelistRelativeLayout);
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
        }
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getApplicationContext(), "Setting Applied",
                Toast.LENGTH_SHORT).show();
        switch (v.getId()) {
            case R.id.applyButtonTracker:
                trackerVariable = trackerEditText.getText().toString();
                trackerEditText.getText().clear();
                preferences.edit().putString("trackerVariablePrefs", trackerVariable).apply();
                trackerSMSCode.setText("Tracker Code: " + trackerVariable);
                dialog.dismiss();
                break;
            case R.id.applyButtonSiren:
                sirenVariable = sirenEditText.getText().toString();
                sirenEditText.getText().clear();
                preferences.edit().putString("sirenVariablePrefs", sirenVariable).apply();
                sirenSMSCode.setText("Siren Code: " + sirenVariable);
                dialog.dismiss();
                break;
            case R.id.applyButtonSpeed:
                speedVariable = speedEditText.getText().toString();
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

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {

            topLevelLayout.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.commit();

            topLevelLayout.setVisibility(View.VISIBLE);
            topLevelLayout.setOnTouchListener(new View.OnTouchListener(){

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    topLevelLayout.setVisibility(View.INVISIBLE);
                    return false;
                }

            });

        }
        return ranBefore;

    }
}




