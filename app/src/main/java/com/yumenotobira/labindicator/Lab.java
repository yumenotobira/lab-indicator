package com.yumenotobira.labindicator;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yaa on 2015/07/14.
 */
public class Lab {
    private int labID;
    private String labName;
    private ArrayList<String> SSIDs;

    public Lab(int labID, String labName) {
        this.labID = labID;
        this.labName = labName;
    }

    public static Lab createLab(int labID, String labName) {
        return new Lab(labID, labName);
    }

    public String getLabName() {
        return labName;
    }
    public int getLabID() { return labID; }

    public static ArrayList<Lab> getLabList(JSONArray labsJSON) {
        ArrayList<Lab> labs = new ArrayList<>();

        try {
            for (int i = 0; i < labsJSON.length(); i++) {
                JSONObject lab = labsJSON.getJSONObject(i);
                labs.add(new Lab(lab.getInt("id"), lab.getString("name")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return labs;
    }

    public static ArrayList<User> getMembers(JSONArray membersJSON) {
        ArrayList<User> members = new ArrayList<>();

        try {
            for(int i = 0; i < membersJSON.length(); i++) {
                JSONObject member = membersJSON.getJSONObject(i);
                members.add(User.createUserFromJSON(member));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return members;
    }
}
