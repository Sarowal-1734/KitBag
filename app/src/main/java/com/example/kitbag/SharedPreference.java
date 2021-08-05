package com.example.kitbag;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    private static final String PREFERENCE_PASSWORD_RESETTED = "SharedPreference_Password_Reset_Successfully";

    public static void setPasswordResettedValue(Context context, Boolean value) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_PASSWORD_RESETTED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREFERENCE_PASSWORD_RESETTED, value);
        editor.apply();
    }

    public static Boolean getPasswordResettedValue(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_PASSWORD_RESETTED, Context.MODE_PRIVATE);
        return pref.getBoolean(PREFERENCE_PASSWORD_RESETTED, true);
    }

}
