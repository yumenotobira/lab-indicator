package com.yumenotobira.labindicator;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class SettingsActivity extends ActionBarActivity
    implements View.OnClickListener, ListView.OnItemClickListener {
    private final String TAG = "SettingsActivity";

    private SharedPreferences pref;
    private final String prefFile = "uuid";
    private User user;
    private String UUID;

    private final String rootPath = "https://lab-indicator.herokuapp.com/";
    private RequestQueue requestQueue;
    private ArrayList<Lab> labList;

    private TextView userNameText;
    private Button userNameButton;
    private EditText userNameEditText;

    private ListView labListView;
    private Lab selectedLab;
    private TextView labNameText;
    private Button labNameButton;

    private LabListAdapter adapter;
    private ArrayList<Lab> labArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        pref = getSharedPreferences(prefFile, MODE_PRIVATE);
        UUID = pref.getString("UUID", java.util.UUID.randomUUID().toString());
        Log.d(TAG, "UUID: " + UUID);

        userNameText = (TextView)findViewById(R.id.settings_user_name);

        userNameButton = (Button)findViewById(R.id.settings_user_name_button);
        userNameButton.setOnClickListener(this);

        userNameEditText = (EditText)findViewById(R.id.settings_user_name_edit_text);

        labNameText = (TextView)findViewById(R.id.settings_my_lab);

        labNameButton = (Button)findViewById(R.id.settings_my_lab_button);
        labNameButton.setOnClickListener(this);

        labListView = (ListView)findViewById(R.id.settings_lab_list);
        labArrayList = new ArrayList<>();
        adapter = new LabListAdapter(getApplicationContext(), 0, labArrayList);
        labListView.setAdapter(adapter);
        labListView.setOnItemClickListener(this);

        // 研究室リストの取得
        requestQueue = Volley.newRequestQueue(this);
        Response.Listener<JSONObject> userListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                user = User.createUserFromJSON(response);

                userNameText.setText(user.name);
                labNameText.setText(user.labName + "研究室");
                selectedLab = Lab.createLab(user.labID, user.labName);
                Log.d(TAG, "userListener name:" + user.name);
                Log.d(TAG, "userListener UUID:" + user.getUUID());
            }
        };
        requestQueue.add(new JsonObjectRequest(rootPath + "users/" + UUID + ".json", userListener, null));

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
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
        requestQueue.add(new JsonArrayRequest(rootPath + "labs/index.json", listener, null));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.settings_user_name_button) {
            String name = userNameEditText.getText().toString();
            if(name.length() == 0) {
                Toast.makeText(this, "ユーザー名が入力されていません", Toast.LENGTH_SHORT).show();
            } else {
                //サーバー、クラスのユーザーネームを変更・保存
                HashMap<String, String> params = new HashMap<>();
                params.put("name", name);
                JsonObjectRequest req = new JsonObjectRequest(rootPath + "users/" + user.getUUID() + ".json", new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d(TAG, "Users#update[:name] response:" + response.getString("response"));
                                    //新しいユーザーのnameのセット
                                    user.saveName(response.getString("name"));

                                    AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this);
                                    dialog.setTitle("ユーザー名変更");
                                    dialog.setMessage("ユーザー名を\"" + user.name + "\"に変更しました");
                                    dialog.setPositiveButton("OK", null);
                                    dialog.show();

                                    userNameText.setText(user.name);
                                    userNameEditText.setText("");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, null);
                requestQueue.add(req);
            }
        } else if(v.getId() == R.id.settings_my_lab_button) {
            if(user.labName.equals(selectedLab.getLabName())) {
                ;
            } else {
                //サーバー、クラスのユーザーのラボを変更・保存
                HashMap<String, String> params = new HashMap<>();
                params.put("labID", String.valueOf(selectedLab.getLabID()));
                Log.d(TAG, "Users#update params labID:" + selectedLab.getLabName());
                //params.put("labName", user.labName);
                JsonObjectRequest req = new JsonObjectRequest(rootPath + "users/" + user.getUUID() + ".json", new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d(TAG, "Users#update[:name] response:" + response.getString("response"));
                                    //新しいユーザーのlab関係のセット
                                    user.saveLab(response.getInt("labID"), response.getString("labName"));

                                    AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this);
                                    dialog.setTitle("所属研究室変更");
                                    dialog.setMessage("所属研究室を\"" + user.labName + "研究室\"に変更しました");
                                    dialog.setPositiveButton("OK", null);
                                    dialog.show();

                                    labNameText.setText(user.labName + "研究室");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, null);
                requestQueue.add(req);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = (ListView)parent;
        Lab lab = (Lab)listView.getItemAtPosition(position);
        Log.d(TAG, lab.getLabID() + ":" + lab.getLabName());
        selectedLab = lab;
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
        } else if (id == R.id.action_lab_list) {
            Intent intent = new Intent(this, LabsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
