package com.yumenotobira.labindicator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yaa on 2015/07/16.
 */
public class LabListAdapter extends ArrayAdapter<Lab> {
    private LayoutInflater inflater;

    public LabListAdapter(Context context, int resource, List<Lab> objects) {
        super(context, resource, objects);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.lab_list, null, false);
        TextView labNameView = (TextView)view.findViewById(R.id.lab_name_item);
        Lab lab = getItem(position);
        labNameView.setText(lab.getLabName() + "研究室");
        return view;
    }
}
