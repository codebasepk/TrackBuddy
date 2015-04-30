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
    LayoutInflater mInflater;
    TextView tv1, tv;
    CheckBox cb;
    SharedPreferences preferences;

    private List<String> name1;
    private List<String> phno1;

    ContactsAdapter(Context context) {
        Helper helper = new Helper(context.getApplicationContext());
        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        name1 = helper.getAllContactNames();
        phno1 = helper.getAllContactNumbers();
        if (mCheckStates == null) {
            mCheckStates = new SparseBooleanArray(name1.size());
        }
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String[] checkedContacts = getCheckedContacts();
        int i = 0;
        for (String contact : phno1) {
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
        return name1.size();
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
        tv= (TextView) vi.findViewById(R.id.textView1);
        tv1= (TextView) vi.findViewById(R.id.textView2);
        cb = (CheckBox) vi.findViewById(R.id.checkBox1);
        tv.setText(name1.get(position));
        tv1.setText(phno1.get(position));
        cb.setTag(position);
        cb.setChecked(mCheckStates.get(position, false));
        cb.setOnCheckedChangeListener(this);
        return vi;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mCheckStates.put((Integer) buttonView.getTag(), isChecked);

        StringBuilder checkedContacts = new StringBuilder();
        for (int i = 0; i < name1.size(); i++) {
            if (mCheckStates.get(i)) {
                checkedContacts.append(phno1.get(i));
                checkedContacts.append(",");
            }
        }
        preferences.edit().putString("checkedContactsPrefs", checkedContacts.toString()).apply();
    }

    private String[] getCheckedContacts() {
        String string = preferences.getString("checkedContactsPrefs", " ");
        return string.split(",");
    }

}