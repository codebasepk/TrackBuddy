package com.byteshaft.trackbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

class DrawerAdapter extends BaseAdapter {

    private Context context;
    private int[] images = {R.drawable.ic_tracker, R.drawable.ic_siren, R.drawable.ic_speed, R.drawable.ic_list};
    static String[] items;

    public DrawerAdapter(Context context) {
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
