package com.example.visionproject;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsUtil {
    private static final String PREF_NAME = "settings";

    public static void saveAlartEnabled(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("alartEnabled", enabled);
        editor.apply();
    }

    public static boolean getAlartEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("alartEnabled", false);
    }



}
