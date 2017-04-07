package org.sluman.imtranslate.utils;

import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bryce on 3/31/17.
 */

public class Utils {
    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public static String buildKey(String thisUser, String thatUser) {
        List<String> list = new ArrayList<String>();
        list.add(thisUser);
        list.add(thatUser);
        Collections.sort(list);
        return android.text.TextUtils.join("_", list);
    }

    public static String[] getKeys(String key) {
        return android.text.TextUtils.split(key, "_");
    }

    public static String getOtherUserKey(String conversationId, Context context) {
        String key = "";
        for (String userKey : getKeys(conversationId)) {
            if (!userKey.equals(SharedPrefsUtils.getUser(context))) {
                key = userKey;
            }
        }
        return key;
    }

    public static String getUserKey(String conversationId, Context context) {
        String key = "";
        for (String userKey : getKeys(conversationId)) {
            if (userKey.equals(SharedPrefsUtils.getUser(context))) {
                key = userKey;
            }
        }
        return key;
    }
}
