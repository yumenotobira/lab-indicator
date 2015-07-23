package com.yumenotobira.labindicator;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;

import javax.xml.transform.Templates;


public class IndicatorActivity extends ActionBarActivity {
    private final String TAG = "IndicatorActivity";

    private final String rootPath = "https://lab-indicator.herokuapp.com/";
    private RequestQueue requestQueue;
    private ArrayList<User> members;
    private MemberListAdapter adapter;
    private ListView memberListView;

    private TextView labNameTextView;
    private String labName;
    private int labID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicator);

        Intent intent = getIntent();
        labName = intent.getStringExtra("labName");
        labID = intent.getIntExtra("labID", 0);

        members = new ArrayList<>();
        adapter = new MemberListAdapter(getApplicationContext(), 0, members);
        memberListView = (ListView)findViewById(R.id.indicator_lab_list);
        memberListView.setAdapter(adapter);
        labNameTextView = (TextView)findViewById(R.id.indicator_lab_name);

        requestQueue = Volley.newRequestQueue(this);

        // 指定ラボのメンバー一覧取得
        requestQueue.add(new JsonArrayRequest(rootPath + "labs/" + String.valueOf(labID) + "/members.json", getMembersListener, null));
    }

    private Response.Listener<JSONArray> getMembersListener = new Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response) {
            ArrayList<User> membersArrayList = Lab.getMembers(response);
            members.clear();
            for(int i = 0; i < membersArrayList.size(); i++) {
                members.add(membersArrayList.get(i));
                Log.d(TAG, "getMembersListener memberName:" + members.get(i).name);
                adapter.notifyDataSetChanged();
            }
            labNameTextView.setText(labName + "研究室");
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_my_lab) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_lab_list) {
            Intent intent = new Intent(this, LabsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
