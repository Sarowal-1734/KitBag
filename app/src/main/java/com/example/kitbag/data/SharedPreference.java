package com.example.kitbag.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    private static final String PREFERENCE_DARK_MODE_ENABLED = "SharedPreference_Dark_Mode_Enabled_True_Or_False";
    private static final String PREFERENCE_LANGUAGE = "SharedPreference_Language_Bangla_Or_English";

    public static void setDarkModeEnableValue(Context context, Boolean value) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_DARK_MODE_ENABLED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREFERENCE_DARK_MODE_ENABLED, value);
        editor.apply();
    }

    public static Boolean getDarkModeEnableValue(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_DARK_MODE_ENABLED, Context.MODE_PRIVATE);
        return pref.getBoolean(PREFERENCE_DARK_MODE_ENABLED, true);
    }

    public static void setLanguageValue(Context context, String language) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_LANGUAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREFERENCE_LANGUAGE, language);
        editor.apply();
    }

    public static String getLanguageValue(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_LANGUAGE, Context.MODE_PRIVATE);
        return pref.getString(PREFERENCE_LANGUAGE, "en");
    }

}
