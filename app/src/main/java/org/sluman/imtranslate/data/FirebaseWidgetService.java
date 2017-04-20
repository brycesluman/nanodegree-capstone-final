package org.sluman.imtranslate.data;

import android.app.LauncherActivity;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.sluman.imtranslate.models.ConversationMessage;
import org.sluman.imtranslate.models.ConversationMessageView;
import org.sluman.imtranslate.utils.SharedPrefsUtils;
import org.sluman.imtranslate.utils.Utils;

import java.util.ArrayList;

public class FirebaseWidgetService extends Service {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    FirebaseDatabase database = Utils.getDatabase();
    DatabaseReference myRef = database.getReference();

    private static String TAG = FirebaseWidgetService.class.getName();

    public static ArrayList<ConversationMessageView> listItemList;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public FirebaseWidgetService() {
    }

    /*
     * Retrieve appwidget id from intent it is needed to update widget later
     * initialize our AQuery class
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!= null && intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
            appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.d(TAG, "Start Widget Service");
        fetchDataFromFirebase();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * method which fetches data(json) from web aquery takes params
     * remoteJsonUrl = from where data to be fetched String.class = return
     * format of data once fetched i.e. in which format the fetched data be
     * returned AjaxCallback = class to notify with data once it is fetched
     */
    private void fetchDataFromFirebase() {
        if (!TextUtils.isEmpty(SharedPrefsUtils.getUser(getApplicationContext()))) {
            myRef.child("user-conversations").child(SharedPrefsUtils.getUser(getApplicationContext())).orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
    //                Log.d(TAG, "dataSnapshot.exists(): " + dataSnapshot.exists());
                    if (dataSnapshot.exists()) {
                        listItemList = new ArrayList<ConversationMessageView>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            ConversationMessage message = child.getValue(ConversationMessage.class);
                            ConversationMessageView messageView = new ConversationMessageView(dataSnapshot.getKey(), message);
                            listItemList.add(messageView);
                            Log.d("FirebaseWidgetService", messageView.text);
                        }

                        populateWidget();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    /**
     * Method which sends broadcast to WidgetProvider
     * so that widget is notified to do necessary action
     * and here action == WidgetProvider.DATA_FETCHED
     */
    private void populateWidget() {
        Intent widgetUpdateIntent = new Intent();
        widgetUpdateIntent.setAction(FirebaseIntentService.ACTION_DATA_FETCHED);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId);
        sendBroadcast(widgetUpdateIntent);

        this.stopSelf();
    }
}
