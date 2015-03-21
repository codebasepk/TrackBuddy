package com.byteshaft.trackbuddy;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.Locale;


public class MainActivity extends ActionBarActivity implements ListView.OnItemClickListener {

    private DrawerLayout drawerLayout;
    private ListView listView;
    private ActionBarDrawerToggle drawerListener;
    private MyAdapter myAdapter;

    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        switch (position) {
            case 0:
                showDialog();
        }
        System.out.println("ok");
    }

    private void selectItem(int position) {

        listView.setItemChecked(position, true);
        setTitle(items[position]);
        drawerLayout.closeDrawer(listView);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_one, null));
        builder.create();
        builder.show();
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

}


