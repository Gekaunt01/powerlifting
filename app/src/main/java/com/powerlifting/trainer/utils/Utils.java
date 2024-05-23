package com.powerlifting.trainer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class Utils {

    public static final String ARG_PARENT_PAGE          = "parent_page";
    public static final String ARG_WORKOUT_ID           = "workout_id";
    public static final String ARG_WORKOUT_NAME         = "workout_name";
    public static final String ARG_WORKOUT_IDS          = "workout_ids";
    public static final String ARG_WORKOUT_NAMES        = "workout_names";
    public static final String ARG_WORKOUT_IMAGES       = "workout_images";
    public static final String ARG_WORKOUT_TIMES        = "workout_times";
    public static final String ARG_WORKOUTS 	        = "workouts";
    public static final String ARG_PROGRAMS 	        = "programs";
    public static final Locale ARG_LOCALE = Locale.US;
    public static final String ARG_DATABASE_PATH = "/data/data/com.powerlifting.trainer/databases/";
    public static final String ARG_DEFAULT_BREAK = "00:10";
    public static final String ARG_DEFAULT_START = "00:10";
    public static final int ARG_SOUND_VOLUME = 7;


    public static int loadPreferences(String param, Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("user_data", 0);
        return sharedPreferences.getInt(param, 0);
    }

    public static void savePreferences(String param, int value, Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("user_data", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(param, value);
        editor.apply();
    }
}