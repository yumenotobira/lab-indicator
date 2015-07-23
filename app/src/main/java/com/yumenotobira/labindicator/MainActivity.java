package com.yumenotobira.labindicator;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private final String TAG = "MainActivity";

    private SharedPreferences pref;
    private final String prefFile = "uuid";
    private User user;
    private String UUID;

    private BroadcastReceiver wifiBroadcastReceiver;
    private String ssid;

    private final String rootPath = "https://lab-indicator.herokuapp.com/";
    private RequestQueue requestQueue;
    private ArrayList<User> members;
    private MemberListAdapter adapter;
    private ListView memberListView;

    private EditText memoEditText;
    private Button memoButton;

    private TextView myLabTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    if(wifiInfo == null) {
                        ssid = "";
                        Log.d(TAG, "Wifi receive ssid: null");
                    } else {
                        ssid = wifiInfo.getSSID().replace("\"", "");
                        Log.d(TAG, "Wifi receive ssid:" + ssid);

                        if(user != null) {
                            //ユーザーのラボを取ってくる
                            Log.d(TAG, "check SSID");
                            requestQueue.add(new JsonObjectRequest(rootPath + "users/" + user.labID + "/lab.json", userLabListener, null));

                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiBroadcastReceiver, intentFilter);

        pref = getSharedPreferences(prefFile, MODE_PRIVATE);
        UUID = pref.getString("UUID", java.util.UUID.randomUUID().toString());
        Log.d(TAG, "UUID: " + UUID);

        myLabTextView = (TextView)findViewById(R.id.my_lab_name);

        members = new ArrayList<>();
        adapter = new MemberListAdapter(getApplicationContext(), 0, members);
        memberListView = (ListView)findViewById(R.id.my_lab_list);
        memberListView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                user = User.createUserFromJSON(response);

                if(!user.getUUID().equals(UUID)) {
                    user.saveUUID(pref, UUID);
                    Log.d(TAG, "Users#create params:" + UUID);
                    HashMap<String, String> params = new HashMap<>();
                    params.put("uuid", UUID);

                    // ユーザーの新規登録
                    requestQueue.add(new JsonObjectRequest(rootPath + "users.json", new JSONObject(params), createUserListener, null));
                } else {
                    Log.d(TAG, "Labs#members/index");

                    // 所属ラボのメンバー一覧取得
                    requestQueue.add(new JsonArrayRequest(rootPath + "labs/" + String.valueOf(user.labID) + "/members.json", getMembersListener, null));

                    // wifi状態の変更
                    WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    ssid = wifiInfo.getSSID().replace("\"", "");

                    requestQueue.add(new JsonObjectRequest(rootPath + "users/" + user.labID + "/lab.json", userLabListener, null));
                }
            }
        };
        requestQueue.add(new JsonObjectRequest(rootPath + "users/" + UUID + ".json", jsonObjectListener, null));

        memoEditText = (EditText)findViewById(R.id.main_memo_edit_text);
        memoButton = (Button)findViewById(R.id.main_memo_button);
        memoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String memo = memoEditText.getText().toString().replace("\n", "");
                HashMap<String, String> params = new HashMap<>();
                params.put("memo", memo);
                requestQueue.add(new JsonArrayRequest(rootPath + "users/" + user.getUUID() + "/memo.json", new JSONObject(params), getMembersListener, null));
                memoEditText.setText("");

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("メモ変更");
                dialog.setMessage("メモを変更しました");
                dialog.setPositiveButton("OK", null);
                dialog.show();
            }
        });
    }

    private Response.Listener<JSONObject> userLabListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                boolean isPresence = false;
                JSONArray JSONSSIDs = response.getJSONArray("ssids");
                for(int i = 0; i < JSONSSIDs.length(); i++) {
                    String SSID = JSONSSIDs.getJSONObject(i).getString("ssid");
                    Log.d(TAG, "userLabListener SSID:" + SSID);

                    if(ssid.equals(SSID)) {
                        isPresence = true;
                        Log.d(TAG, "isPresence SSID:" + SSID);
                    }
                }
                HashMap<String, Boolean> params = new HashMap<>();
                params.put("presence", isPresence);
                requestQueue.add(new JsonArrayRequest(rootPath + "users/" + user.getUUID() + "/presence.json", new JSONObject(params), getMembersListener, null));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

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
            myLabTextView.setText(user.labName + "研究室");
        }
    };

    private Response.Listener<JSONObject> createUserListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                Log.d(TAG, "Users#add response:" + response.getString("response"));
                //新しいユーザーのUUIDのセット
                user.setUUID(UUID);
                myLabTextView.setText("所属研究室無し");
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_lab_list) {
            Intent intent = new Intent(this, LabsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
