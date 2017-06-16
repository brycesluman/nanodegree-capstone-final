package org.sluman.origami.utils;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import com.google.common.collect.ImmutableList;
import com.google.firebase.database.FirebaseDatabase;

import org.sluman.origami.BuildConfig;
import org.sluman.origami.R;
import org.sluman.origami.data.FirebaseWidgetService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bryce on 3/31/17.
 */

public class Utils {
    private static final String API_KEY = BuildConfig.TRANSLATE_API_KEY;
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

    public static void refreshWidget(Context context) {
        Intent widgetIntent = new Intent(context, FirebaseWidgetService.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = {R.xml.new_app_widget_info};
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(widgetIntent);
    }

    public static String translateText(String text, String source, String target) {
        Translate translate = createTranslateService();
        Translation translation =
                translate.translate(
                        text,
                        Translate.TranslateOption.sourceLanguage(source),
                        Translate.TranslateOption.targetLanguage(target));
        return translation.getTranslatedText();
    }

    public static Translate createTranslateService() {
        TranslateOptions options = TranslateOptions.newBuilder()
                .setApiKey(API_KEY)
                .build();
        return options.getService();
    }
    public String detectLanguage(String sourceText) {
        String language = "";
        Translate translate = createTranslateService();
        List<Detection> detections = translate.detect(ImmutableList.of(sourceText));
        System.out.println("Language(s) detected:");
        for (Detection detection : detections) {
            language = detection.getLanguage();
        }
        return language;
    }
}
