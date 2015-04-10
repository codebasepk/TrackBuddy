package com.byteshaft.trackbuddy;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {

    static SparseBooleanArray mCheckStates;
    LayoutInflater mInflater;
    TextView tv1,tv;
    CheckBox cb;


    List<String> name1 = new ArrayList<String>();
    List<String> phno1 = new ArrayList<String>();


    public  void getAllContacts(ContentResolver cr) {

        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            name1.add(name);
            phno1.add(phoneNumber);
        }

        phones.close();
    }


    ContactsAdapter(Context context) {
        mCheckStates = new SparseBooleanArray(name1.size());
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return name1.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi=convertView;
        if(convertView==null)
            vi = mInflater.inflate(R.layout.row, null);
        tv= (TextView) vi.findViewById(R.id.textView1);
        tv1= (TextView) vi.findViewById(R.id.textView2);
        cb = (CheckBox) vi.findViewById(R.id.checkBox1);
        tv.setText("Name: "+ name1.get(position));
        tv1.setText("Phone No: "+ phno1.get(position));
        cb.setTag(position);
        cb.setChecked(mCheckStates.get(position, false));
        cb.setOnCheckedChangeListener(this);

        return vi;
    }
    public boolean isChecked(int position) {
        return mCheckStates.get(position, false);
    }

    public void setChecked(int position, boolean isChecked) {
        mCheckStates.put(position, isChecked);
    }


    public void toggle(int position) {
        setChecked(position, !isChecked(position));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView,
                                 boolean isChecked) {
        // TODO Auto-generated method stub

        mCheckStates.put((Integer) buttonView.getTag(), isChecked);
    }

}
