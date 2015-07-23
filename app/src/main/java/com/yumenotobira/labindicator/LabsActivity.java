package com.yumenotobira.labindicator;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;


public class LabsActivity extends ActionBarActivity {
    private final String TAG = "LabsActivity";

    private final String rootPath = "https://lab-indicator.herokuapp.com/";
    private RequestQueue requestQueue;
    private ArrayList<Lab> labList;

    private LabListAdapter adapter;
    private ArrayList<Lab> labArrayList;
    private ListView labListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labs);

        labListView = (ListView)findViewById(R.id.labs_lab_list);
        labArrayList = new ArrayList<>();
        adapter = new LabListAdapter(getApplicationContext(), 0, labArrayList);
        labListView.setAdapter(adapter);
        labListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView)parent;
                Lab lab = (Lab)listView.getItemAtPosition(position);
                Log.d(TAG, lab.getLabID() + ":" + lab.getLabName());
                Intent intent = new Intent(LabsActivity.this, IndicatorActivity.class);
                intent.putExtra("labName", lab.getLabName());
                intent.putExtra("labID", lab.getLabID());
                startActivity(intent);
            }
        });

        // 研究室リストの取得
        requestQueue = Volley.newRequestQueue(this);
        Response.Listener<JSONArray> labsListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                labList = Lab.getLabList(response);
                for(int i = 0; i < labList.size(); i++) {
                    Lab lab = labList.get(i);
                    labArrayList.add(lab);
                    Log.d(TAG, lab.getLabName());
                    adapter.notifyDataSetChanged();
                }
            }
        };
        requestQueue.add(new JsonArrayRequest(rootPath + "labs/index.json", labsListener, null));
    }

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
        }

        return super.onOptionsItemSelected(item);
    }
}
