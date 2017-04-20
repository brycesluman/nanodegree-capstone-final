package org.sluman.imtranslate.data;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.sluman.imtranslate.BuildConfig;
import org.sluman.imtranslate.R;
import org.sluman.imtranslate.models.ConversationMessage;
import org.sluman.imtranslate.models.LanguageView;
import org.sluman.imtranslate.models.Message;
import org.sluman.imtranslate.models.User;
import org.sluman.imtranslate.utils.SharedPrefsUtils;
import org.sluman.imtranslate.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FirebaseIntentService extends IntentService {
    private static final String API_KEY = BuildConfig.TRANSLATE_API_KEY;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_ADD_MESSAGE = "org.sluman.imtranslate.data.action.ACTION_ADD_MESSAGE";
    public static final String ACTION_ADD_USER = "org.sluman.imtranslate.data.action.ACTION_ADD_USER";
    public static final String ACTION_DISPLAY_MESSAGE = "org.sluman.imtranslate.data.action.ACTION_DISPLAY_MESSAGE";
    public static final String ACTION_DISPLAY_USER = "org.sluman.imtranslate.data.action.ACTION_DISPLAY_USER";
    public static final String ACTION_SEARCH_USERS = "org.sluman.imtranslate.data.action.ACTION_SEARCH_USERS";
    public static final String ACTION_NEW_CONVERSATION = "org.sluman.imtranslate.data.action.ACTION_NEW_CONVERSATION";
    public static final String ACTION_TRY_OPEN_CONVERSATION = "org.sluman.imtranslate.data.action.ACTION_TRY_OPEN_CONVERSATION";
    public static final String ACTION_DISPLAY_CONVERSATION = "org.sluman.imtranslate.data.action.ACTION_DISPLAY_CONVERSATION";
    public static final String ACTION_DISPLAY_USER_CONVERSATIONS = "org.sluman.imtranslate.data.action.ACTION_DISPLAY_USER_CONVERSATIONS";
    public static final String ACTION_UPDATE_CONVERSATION = "org.sluman.imtranslate.data.action.ACTION_UPDATE_CONVERSATION";
    public static final String ACTION_GET_USER = "org.sluman.imtranslate.data.action.ACTION_GET_USER";
    public static final String ACTION_RETURN_USER = "org.sluman.imtranslate.data.action.ACTION_RETURN_USER";
    public static final String ACTION_SET_LANGUAGE = "org.sluman.imtranslate.data.action.ACTION_SET_LANGUAGE";
    public static final String ACTION_DISPLAY_LANGUAGE_VIEW = "org.sluman.imtranslate.data.action.ACTION_DISPLAY_LANGUAGE_VIEW";
    public static final String ACTION_DISPLAY_LANGUAGES = "org.sluman.imtranslate.data.action.ACTION_DISPLAY_LANGUAGES";
    public static final String ACTION_NO_RESULTS = "org.sluman.imtranslate.data.action.ACTION_NO_RESULTS";
    public static final String ACTION_DATA_FETCHED = "org.sluman.imtranslate.data.action.ACTION_DATA_FETCHED";

    // TODO: Rename parameters
    public static final String EXTRA_USER = "org.sluman.imtranslate.data.extra.USER";
    public static final String EXTRA_MESSAGE = "org.sluman.imtranslate.data.extra.MESSAGE";
    public static final String EXTRA_UID = "org.sluman.imtranslate.data.extra.UID";
    public static final String EXTRA_TEXT = "org.sluman.imtranslate.data.extra.TEXT";
    public static final String EXTRA_PHOTO_URL = "org.sluman.imtranslate.data.extra.PHOTO_URL";
    public static final String EXTRA_DISPLAY_NAME = "org.sluman.imtranslate.data.extra.EXTRA_DISPLAY_NAME";
    public static final String EXTRA_USERNAME = "org.sluman.imtranslate.data.extra.EXTRA_USERNAME";
    public static final String EXTRA_SEARCH_STRING = "org.sluman.imtranslate.data.extra.EXTRA_SEARCH_STRING";
    public static final String EXTRA_CONVERSATION_ID = "org.sluman.imtranslate.data.extra.EXTRA_CONVERSATION_ID";
    public static final String EXTRA_SOURCE_LANG = "org.sluman.imtranslate.data.extra.EXTRA_SOURCE_LANG";
    public static final String EXTRA_TARGET_LANG = "org.sluman.imtranslate.data.extra.EXTRA_TARGET_LANG";
    public static final String EXTRA_LANGUAGE = "org.sluman.imtranslate.data.extra.EXTRA_LANGUAGE";
    public static final String EXTRA_LANGUAGE_VIEW = "org.sluman.imtranslate.data.extra.EXTRA_LANGUAGE_VIEW";
    public static final String EXTRA_OTHER_UID = "org.sluman.imtranslate.data.extra.EXTRA_OTHER_UID";
    public static final String EXTRA_OTHER_USERNAME = "org.sluman.imtranslate.data.extra.EXTRA_OTHER_USERNAME";
    public static final String EXTRA_OTHER_AVATAR = "org.sluman.imtranslate.data.extra.EXTRA_OTHER_AVATAR";

    FirebaseDatabase database = Utils.getDatabase();
    DatabaseReference myRef = database.getReference();
    private static String TAG = FirebaseIntentService.class.getName();

    private int mSearchCount = 0;


    public FirebaseIntentService() {
        super("FirebaseIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service started");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "service destroyed");
        super.onDestroy();
    }


    private void writeNewPost(String key,
                              String uid,
                              String text,
                              long timestamp,
                              String userAvatar,
                              String username,
                              String otherUid,
                              String otherUsername,
                              String otherAvatar,
                              String sourceLang,
                              String targetLang) {
        // Create new post at /user-conversations/$userid/$conversationid and at
        // /conversations/$conversationid simultaneously
        String translatedText = text;
        if (!sourceLang.equals(targetLang)) {
            Translate translate = createTranslateService();
            Translation translation =
                    translate.translate(
                            text,
                            Translate.TranslateOption.sourceLanguage(sourceLang),
                            Translate.TranslateOption.targetLanguage(targetLang));
            translatedText = translation.getTranslatedText();
        }
        String messageKey = myRef.child("messages").child(key).push().getKey();
        Message message = new Message(uid, text, timestamp, translatedText, userAvatar, username);

        Map<String, Object> messageValues = message.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        ConversationMessage conversationMessage;
        for (String userKey : Utils.getKeys(key)) {
            childUpdates.put("/conversations/" + key + "/" + userKey, true);
            if (userKey.equals(uid)) {
                conversationMessage = new ConversationMessage(otherUid, otherUsername, otherAvatar, uid, text, timestamp, translatedText, userAvatar, username);
            } else {
                conversationMessage = new ConversationMessage(uid, username, userAvatar, otherUid, translatedText, timestamp, text, otherAvatar, otherUsername);
            }
            childUpdates.put("/user-conversations/" + userKey + "/" + key, conversationMessage.toMap());
        }

        childUpdates.put("/messages/" + key + "/" + messageKey, messageValues);

        myRef.updateChildren(childUpdates);
    }

    private void tryAddUser(String uid, String username, String displayName, String userAvatar, long timestamp, boolean online, String language) {
        checkUserExists(uid, username, displayName, userAvatar, timestamp, online, language);
    }

    private void addUser(String uid, String username, String displayName, String userAvatar, long timestamp, boolean online, String language) {
        User user = new User(uid, username, displayName, userAvatar, timestamp, online, language);
        Map<String, Object> userValues = user.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + uid, userValues);

        myRef.updateChildren(childUpdates);
    }

    private void newConversation(String uid) {
        checkConversationExists(uid);
        Log.d(TAG, " userId: " + uid);

    }

    private Translate createTranslateService() {
        TranslateOptions options = TranslateOptions.newBuilder()
                .setApiKey(API_KEY)
                .build();
        return options.getService();
    }

    public void displaySupportedLanguages(Optional<String> tgtLang) {
        Translate translate = createTranslateService();
        Translate.LanguageListOption target = Translate.LanguageListOption.targetLanguage(null);
        List<Language> languages = translate.listSupportedLanguages(target);
        String selectedLanguage = SharedPrefsUtils.getUserLanguage(getApplicationContext());
        for (Language language : languages) {
            boolean selected = language.getCode().equals(selectedLanguage);
            broadcastLanguage(new LanguageView(language.getCode(), language.getName(), selected));
        }
    }

    private void setUserLanguage(final String languageCode) {
        SharedPrefsUtils.setUserLanguage(getApplicationContext(), languageCode);

        myRef.child("users").child(SharedPrefsUtils.getUser(getApplicationContext()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    addUser(user.getUid(), user.username, user.displayName, user.userAvatar, new Date().getTime(), true, languageCode);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean checkConversationExists(final String uid) {
        myRef.child("conversations")
                .child(Utils.buildKey(SharedPrefsUtils.getUser(getApplicationContext()), uid))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    openConversation(snapshot.getKey());
                } else {
                    addConversation(uid);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return false;
    }

    public void addConversation(String uid) {
        String key = Utils.buildKey(SharedPrefsUtils.getUser(getApplicationContext()), uid);
        Map<String, Object> childUpdates = new HashMap<>();
        for (String userKey : Utils.getKeys(key)) {
            childUpdates.put("/conversations/" + key + "/" + userKey, true);
        }

        myRef.updateChildren(childUpdates);
    }



    public void openConversation(String conversationId) {
        broadcastDisplayConversation(conversationId);

    }

    public void getUserByConversationId(final String conversationId) {
        myRef.child("users").child(conversationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    broadcastUser(user);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkUserExists(final String uid, final String username, final String displayName, final String userAvatar, final long timestamp, final boolean online, final String language) {
        myRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    addUser(uid, username, displayName, userAvatar, timestamp, online, language);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void searchUsers(final String searchString) {
        myRef.child("users").orderByChild("username").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                User user = dataSnapshot.getValue(User.class);
                if (user.searchUser(searchString)) {
                    mSearchCount++;
                    broadcastDisplayUser(user);
                } else {
                    broadcastNoResults();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                broadcastNoResults();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }
        });
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ADD_MESSAGE.equals(action)) {
                writeNewPost(
                        intent.getStringExtra(EXTRA_CONVERSATION_ID),
                        intent.getStringExtra(EXTRA_UID),
                        intent.getStringExtra(EXTRA_TEXT),
                        new Date().getTime(),
                        intent.getStringExtra(EXTRA_PHOTO_URL),
                        intent.getStringExtra(EXTRA_DISPLAY_NAME),
                        intent.getStringExtra(EXTRA_OTHER_UID),
                        intent.getStringExtra(EXTRA_OTHER_USERNAME),
                        intent.getStringExtra(EXTRA_OTHER_AVATAR),
                        intent.getStringExtra(EXTRA_SOURCE_LANG),
                        intent.getStringExtra(EXTRA_TARGET_LANG));
            } else if (ACTION_ADD_USER.equals(action)) {
                tryAddUser(intent.getStringExtra(EXTRA_UID),
                        intent.getStringExtra(EXTRA_USERNAME),
                        intent.getStringExtra(EXTRA_DISPLAY_NAME),
                        intent.getStringExtra(EXTRA_PHOTO_URL),
                        new Date().getTime(),
                        true,
                        intent.getStringExtra(EXTRA_LANGUAGE));
            } else if (ACTION_SEARCH_USERS.equals(action)) {
                Log.d(TAG, "search: " + intent.getStringExtra(EXTRA_SEARCH_STRING));
                searchUsers(intent.getStringExtra(EXTRA_SEARCH_STRING));
            } else if (ACTION_NEW_CONVERSATION.equals(action)) {
                Log.d(TAG, "new conversation: " + intent.getStringExtra(EXTRA_UID));
                newConversation(intent.getStringExtra(EXTRA_UID));
            } else if (ACTION_TRY_OPEN_CONVERSATION.equals(action)) {
                Log.d(TAG, "open conversation: " + intent.getStringExtra(EXTRA_CONVERSATION_ID));
                openConversation(intent.getStringExtra(EXTRA_CONVERSATION_ID));
            } else if (ACTION_DISPLAY_LANGUAGES.equals(action)) {
                Log.d(TAG, "show languages");
                displaySupportedLanguages(null);
            } else if (ACTION_SET_LANGUAGE.equals(action)) {
                Log.d(TAG, "show languages");
                setUserLanguage(intent.getStringExtra(EXTRA_LANGUAGE));
            } else if (ACTION_GET_USER.equals(action)) {
                Log.d(TAG, "get other user");
                getUserByConversationId(Utils.getOtherUserKey(intent.getStringExtra(EXTRA_CONVERSATION_ID),getApplicationContext()));
            }
        }
    }

    public void broadcastDisplayUser(User user) {
        Intent intent = new Intent(ACTION_DISPLAY_USER);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_USER, user);
        intent.putExtras(bundle);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }

    public void broadcastUser(User user) {
        Intent intent = new Intent(ACTION_RETURN_USER);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_USER, user);
        intent.putExtras(bundle);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }
    public void broadcastNoResults() {
        if (mSearchCount == 0) {
            mSearchCount = 0;
            Intent intent = new Intent(ACTION_NO_RESULTS);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);
        }
    }

    public void broadcastLanguage(LanguageView languageView) {
        Intent intent = new Intent(ACTION_DISPLAY_LANGUAGE_VIEW);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_LANGUAGE_VIEW, languageView);
        intent.putExtras(bundle);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }

    // called to send data to Activity
    public void broadcastDisplayConversation(String param) {
        Log.d(TAG, "open conversation: " + param);
        Intent intent = new Intent(ACTION_DISPLAY_CONVERSATION);
        intent.putExtra(EXTRA_CONVERSATION_ID, param);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }


}
