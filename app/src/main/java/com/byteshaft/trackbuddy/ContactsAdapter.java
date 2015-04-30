package com.byteshaft.trackbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;


public class ContactsAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {

    static SparseBooleanArray mCheckStates;
    private LayoutInflater mInflater;
    private SharedPreferences mPreferences;

    private List<String> mContactNames;
    private List<String> mContactNumbers;

    ContactsAdapter(Context context) {
        Helper helper = new Helper(context.getApplicationContext());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        mContactNames = helper.getAllContactNames();
        mContactNumbers = helper.getAllContactNumbers();
        if (mCheckStates == null) {
            mCheckStates = new SparseBooleanArray(mContactNames.size());
        }
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String[] checkedContacts = getCheckedContacts();
        int i = 0;
        for (String contact : mContactNumbers) {
            for (String checkedContact: checkedContacts) {
                if (contact.equals(checkedContact)) {
                    mCheckStates.put(i, true);
                }
            }
            i++;
        }
    }

    @Override
    public int getCount() {
        return mContactNames.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null) {
            vi = mInflater.inflate(R.layout.row, null);
        }
        TextView tv = (TextView) vi.findViewById(R.id.textView1);
        TextView tv1 = (TextView) vi.findViewById(R.id.textView2);
        CheckBox cb = (CheckBox) vi.findViewById(R.id.checkBox1);
        tv.setText(mContactNames.get(position));
        tv1.setText(mContactNumbers.get(position));
        cb.setTag(position);
        cb.setChecked(mCheckStates.get(position, false));
        cb.setOnCheckedChangeListener(this);
        return vi;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mCheckStates.put((Integer) buttonView.getTag(), isChecked);

        StringBuilder checkedContacts = new StringBuilder();
        for (int i = 0; i < mContactNames.size(); i++) {
            if (mCheckStates.get(i)) {
                checkedContacts.append(mContactNumbers.get(i));
                checkedContacts.append(",");
            }
        }
        mPreferences.edit().putString("checkedContactsPrefs", checkedContacts.toString()).apply();
    }

    private String[] getCheckedContacts() {
        String string = mPreferences.getString("checkedContactsPrefs", " ");
        return string.split(",");
    }

}