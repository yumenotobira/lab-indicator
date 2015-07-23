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
public class MemberListAdapter extends ArrayAdapter<User> {
    private LayoutInflater inflater;

    public MemberListAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.member_list, null, false);
        TextView memberNameView = (TextView)view.findViewById(R.id.member_name_item);
        TextView memberPresenceView = (TextView)view.findViewById(R.id.member_presence_item);
        TextView memberMemoView = (TextView)view.findViewById(R.id.member_memo_item);

        User member = getItem(position);
        memberNameView.setText(member.name);

        if(member.presence) {
            memberPresenceView.setText("在室");
        } else {
            memberPresenceView.setText("不在");
        }

        memberMemoView.setText(member.memo);

        return view;
    }
}