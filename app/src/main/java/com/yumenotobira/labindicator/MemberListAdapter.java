package com.yumenotobira.labindicator;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yaa on 2015/07/16.
 */
public class MemberListAdapter extends ArrayAdapter<User> {
    private LayoutInflater inflater;
    private Resources resources;

    public MemberListAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = context.getResources();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.member_list, null, false);
        TextView memberNameView = (TextView)view.findViewById(R.id.member_name_item);
        TextView memberMemoView = (TextView)view.findViewById(R.id.member_memo_item);
        TextView memberPresenceTextView = (TextView)view.findViewById(R.id.member_presence_text);
        TextView memberAbsenceTextView = (TextView)view.findViewById(R.id.member_absence_text);

        User member = getItem(position);
        memberNameView.setText(member.name);

        if(member.presence) {
            memberPresenceTextView.setBackgroundColor(resources.getColor(R.color.indicator_background));
            memberAbsenceTextView.setTextColor(resources.getColor(R.color.no_indicator_textColor));
        } else {
            memberAbsenceTextView.setBackgroundColor(resources.getColor(R.color.indicator_background));
            memberPresenceTextView.setTextColor(resources.getColor(R.color.no_indicator_textColor));
        }

        memberMemoView.setText(member.memo);

        return view;
    }
}