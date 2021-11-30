package com.example.kitbag.notification;

import android.app.Activity;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender {
    String userFcmToken;
    String userIdReceiver;
    String title;
    String body;
    Context mContext;
    Activity mActivity;

    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    //private final String fcmServerKey = "copy_and_paste_the_Server_Key_from_firebase/project_overview/project_settings/cloud_messaging/server_key";
    private final String fcmServerKey = "AAAAP-npIfc:APA91bFrBTW9b4TZEoiKFdlDl4If2soyNd4gXnd5hN6PGrpff8vIF05bqBtHcZtaygBOW7iw9TKoXAL7r6-FNbRZK7yrVr6H7fmwaP6SkPY1ZN-RGBfoPN4FrQoWX08uwNfoPxHqZTo5";

    public FcmNotificationsSender(String userFcmToken, String userIdReceiver, String title, String body, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.userIdReceiver = userIdReceiver;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public void SendNotifications() {
        requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("tag", userIdReceiver);
            notiObject.put("body", body);
            notiObject.put("icon", "ic_notifications"); // enter icon that exists in drawable only
            mainObj.put("notification", notiObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // code run is got response
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // code run is got error
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
