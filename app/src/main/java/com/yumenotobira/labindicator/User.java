package com.yumenotobira.labindicator;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by yaa on 2015/07/14.
 */
public class User {
    private String UUID;
    public String name;
    public int labID;
    public String labName;
    public boolean presence;
    public String memo;

    public User() {
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public static User createUserFromJSON(JSONObject object) {
        User user = new User();

        try {
            String UUID = object.getString("uuid");
            String name = object.getString("name");
            boolean presence = object.getBoolean("presence");
            String memo = object.getString("memo");
            int labID = object.getInt("labID");
            String labName = object.getString("labName");

            user.UUID = UUID;
            user.name = name;
            user.presence = presence;
            user.memo = memo;
            user.labID = labID;
            user.labName = labName;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    public void saveUUID(SharedPreferences pref, String UUID) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("UUID", UUID);
        editor.commit();

        this.UUID = UUID;
    }

    public void saveName(String name) {
        this.name = name;
    }

    public void saveLab(int labID, String labName) {
        this.labID = labID;
        this.labName = labName;
    }

    public String getUUID() {
        return UUID;
    }
}
