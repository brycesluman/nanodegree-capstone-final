package org.sluman.origami.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.sluman.origami.models.User;

/**
 * Created by bryce on 3/2/17.
 */

public class SharedPrefsUtils {
    private final static String PATH = "org.sluman.imtranslate";
    private final static String USER_KEY = ".user_key";
    private final static String USER_LANGUAGE = ".user_language";
    private final static String USER_TOKEN = ".user_token";

    public static void saveUser(Context context, User user) {

        SharedPreferences sharedPrefs = context.getSharedPreferences(
               PATH, Context.MODE_PRIVATE);

        sharedPrefs.edit().putString(PATH + USER_KEY, user.getUid()).apply();
        sharedPrefs.edit().putString(PATH + USER_LANGUAGE, user.getLanguage()).apply();
    }

    public static void removeUser(Context context) {

    }

    public static String getUser(Context context) {

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PATH, Context.MODE_PRIVATE);
        return sharedPrefs.getString(PATH + USER_KEY, "");
    }

    public static void setUserLanguage(Context context, String languageCode) {

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PATH, Context.MODE_PRIVATE);

        sharedPrefs.edit().putString(PATH + USER_LANGUAGE, languageCode).apply();
    }

    public static String getUserLanguage(Context context) {

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PATH, Context.MODE_PRIVATE);
        return sharedPrefs.getString(PATH + USER_LANGUAGE, "");
    }

    public static void setUserToken(Context context, String token) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PATH, Context.MODE_PRIVATE);

        sharedPrefs.edit().putString(PATH + USER_TOKEN, token).apply();
    }

    public static String getUserToken(Context context) {

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PATH, Context.MODE_PRIVATE);
        return sharedPrefs.getString(PATH + USER_TOKEN, "");
    }
}
