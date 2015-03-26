package com.byteshaft.trackbuddy;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends ActionBarActivity implements ListView.OnItemClickListener {

    private DrawerLayout drawerLayout;
    private ListView listView;
    private ActionBarDrawerToggle drawerListener;
    private MyAdapter myAdapter;
    Button trackerApplyButton;
    Button sirenApplyButton;
    EditText trackerEditText;
    EditText sirenEditText;
    SharedPreferences preferences;

    String trackerVariable;
    String sirenVariable;
    Dialog dialog;
    String[] items;
    TextView trackerSMSCode;
    TextView sirenSMSCode;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Home");
        trackerSMSCode = (TextView) findViewById(R.id.trackerSMSCode);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        trackerVariable = preferences.getString("trackerVariablePrefs", "TBgps");
        trackerSMSCode.setText("Tracker Code: " + trackerVariable);
        sirenSMSCode = (TextView) findViewById(R.id.sirenSMSCode);
        sirenVariable = preferences.getString("sirenVariablePrefs", "TBsiren");
        sirenSMSCode.setText("Siren Code: " + sirenVariable);
        listView = (ListView) findViewById(R.id.drawer_list);
        myAdapter = new MyAdapter(this);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(this);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        listView.setOnItemClickListener(this);

        drawerListener = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

                super.onDrawerClosed(drawerView);
                System.out.println("oyoyo");
            }
        };
        drawerLayout.setDrawerListener(drawerListener);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerListener.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
        popDialog(position);
    }

    private void selectItem(int position) {

        listView.setItemChecked(position, true);
        setTitle(items[position]);
        drawerLayout.closeDrawer(listView);
    }

    private void popDialog(int window) {
        dialog = new Dialog(MainActivity.this, R.style.PauseDialog);


        switch (window) {
            case 0:
                LayoutInflater trackerInflater = getLayoutInflater();
                RelativeLayout trackerRelativeLayout = (RelativeLayout) trackerInflater.inflate(R.layout.dialog_one, null);
                final Switch trackerSwitch = (Switch) trackerRelativeLayout.findViewById(R.id.switchTracker);
                boolean trackerPref = preferences.getBoolean("trackerPreference", true);
                trackerSwitch.setChecked(trackerPref);
                trackerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        preferences.edit().putBoolean("trackerPreference", isChecked).apply();
                        }
                });

                trackerApplyButton = (Button) trackerRelativeLayout.findViewById(R.id.applyButtonTracker);
                trackerEditText = (EditText) trackerRelativeLayout.findViewById(R.id.editTextTracker);
                setOnTextChangeListenerForInputField(trackerEditText);
                setOnClickListenerForEditText(trackerEditText);
                trackerApplyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        trackerVariable = trackerEditText.getText().toString();
                        trackerEditText.getText().clear();
                        preferences.edit().putString("trackerVariablePrefs", trackerVariable).commit();
                        trackerSMSCode.setText("Tracker Code: " + trackerVariable);
                        dialog.dismiss();
                    }
                });
                dialog.setTitle("Tracker");
                dialog.setContentView(trackerRelativeLayout);
                break;
            case 1:
                LayoutInflater sirenInflater = getLayoutInflater();
                RelativeLayout sirenRelativeLayout = (RelativeLayout) sirenInflater.inflate(R.layout.dialog_two, null);
                final Switch sirenSwitch = (Switch) sirenRelativeLayout.findViewById(R.id.switchSiren);
                boolean sirenPref = preferences.getBoolean("sirenPreference", true);
                sirenSwitch.setChecked(sirenPref);
                sirenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        preferences.edit().putBoolean("sirenPreference", isChecked).apply();
                    }
                });

                sirenApplyButton = (Button) sirenRelativeLayout.findViewById(R.id.applyButtonSiren);
                sirenEditText = (EditText) sirenRelativeLayout.findViewById(R.id.editTextSiren);
                sirenApplyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sirenVariable = sirenEditText.getText().toString();
                        sirenEditText.getText().clear();
                        preferences.edit().putString("sirenVariablePrefs", sirenVariable).commit();
                        sirenSMSCode.setText("Siren Code: " + sirenVariable);
                        dialog.dismiss();
                    }
                });
                dialog.setContentView(sirenRelativeLayout);
                break;
            case 2:
                dialog.setTitle("Speed");
                dialog.setContentView(R.layout.dialog_three);
                break;
            case 3:
                dialog.setTitle("Blacklist/Whitelist");
                dialog.setContentView(R.layout.dialog_four);
                break;
        }

        dialog.show();
    }


    class MyAdapter extends BaseAdapter {

        private Context context;
        int[] images = {R.drawable.ic_tracker, R.drawable.ic_siren, R.drawable.ic_speed, R.drawable.ic_list};

        public MyAdapter(Context context) {
            this.context = context;
            items = context.getResources().getStringArray(R.array.items);
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.drawer_layout, parent, false);
            } else {
                row = convertView;
            }
            ImageView iconImageView = (ImageView) row.findViewById(R.id.trackerIcon);
            iconImageView.setImageResource(images[position]);
            return row;
        }
    }

    private void setOnTextChangeListenerForInputField(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getText().toString().equals("TB")) {
                    return;
                }
                if(editText.getText().toString().isEmpty() || editText.getText().toString().equals("T")) {
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
                if(editText.getText().toString().isEmpty() || editText.getText().toString().equals("T")) {
                    editText.setText("TB");
                    editText.setSelection(editText.getText().length());
                } else {
                    editText.setSelection(editText.getText().length());
                }
            }

        });
    }

}


