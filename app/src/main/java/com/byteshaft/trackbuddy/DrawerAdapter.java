package com.byteshaft.trackbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

class DrawerAdapter extends BaseAdapter {

    static String[] mDrawerItems;
    private Context mContext;
    private int[] mDrawerItemsIcons = {R.drawable.ic_tracker, R.drawable.ic_siren, R.drawable.ic_speed, R.drawable.ic_list};

    public DrawerAdapter(Context context) {
        mContext = context;
        mDrawerItems = mContext.getResources().getStringArray(R.array.items);
    }

    @Override
    public int getCount() {
        return mDrawerItems.length;
    }

    @Override
    public Object getItem(int position) {
        return mDrawerItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.drawer_layout, parent, false);
        } else {
            row = convertView;
        }
        ImageView iconImageView = (ImageView) row.findViewById(R.id.trackerIcon);
        iconImageView.setImageResource(mDrawerItemsIcons[position]);
        return row;
    }
}