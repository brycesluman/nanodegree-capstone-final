package org.sluman.imtranslate.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.sluman.imtranslate.models.User;

/**
 * Created by bryce on 3/2/17.
 */

public class SharedPrefsUtils {
    private final static String PATH = "org.sluman.imtranslate";
    private final static String USER_KEY = ".user_key";
    private final static String USER_LANGUAGE = ".user_language";

    public static void saveUser(Context context, User user) {

        SharedPreferences sharedPrefs = context.getSharedPreferences(
               PATH, Context.MODE_PRIVATE);

        sharedPrefs.edit().putString(PATH + USER_KEY, user.getUid()).apply();
        sharedPrefs.edit().putString(PATH + USER_LANGUAGE, user.getLanguage()).apply();
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
}
