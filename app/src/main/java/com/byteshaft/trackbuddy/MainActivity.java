package com.byteshaft.trackbuddy;

import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener{

    private DrawerLayout drawerLayout;
    private ListView listView;
    private String[] items;
    private ActionBarDrawerToggle drawerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        items = getResources().getStringArray(R.array.items);
        listView = (ListView) findViewById(R.id.drawer_list);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        listView.setOnItemClickListener(this);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListener = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.setDrawerListener(drawerListener);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#B3000000")));


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerListener.onOptionsItemSelected(item)){
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

        Toast.makeText(this, items[position], Toast.LENGTH_LONG).show();
        selectItem(position);
    }

    private void selectItem(int position) {
        listView.setItemChecked(position, true);
    }
}

