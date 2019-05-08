package org.sluman.origami;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.sluman.origami.data.FirebaseIntentService;
import org.sluman.origami.models.User;
import org.sluman.origami.utils.SharedPrefsUtils;
import org.sluman.origami.utils.Utils;

/**
 * An activity representing a single ConversationMessage detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ConversationListActivity}.
 */
public class ConversationDetailActivity extends BaseActivity {

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FirebaseIntentService.ACTION_RETURN_USER)) {
                final Bundle bundle = intent.getExtras();
                mOtherUser = bundle.getParcelable(FirebaseIntentService.EXTRA_USER);
                listenForUserChanges();
                finishOnCreate();
            }
        }
    };
    ActionBar mActionBar;

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseUser mUser;

    private AdView mAdView;
    private User mOtherUser;
    private String mConversationId;
    boolean mFirstLoad;

    FirebaseDatabase database = Utils.getDatabase();
    DatabaseReference myRef = database.getReference();
    Query mUserQuery;
    ValueEventListener mUserValueEventListener;

    private static String TAG = ConversationDetailActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_detail);

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder builder = new AdRequest.Builder();
        if (BuildConfig.DEBUG) {
            builder.addTestDevice("5D0A8F1C9E3730350A3DF50974E00A36");
        }
        AdRequest adRequest = builder.build();
        mAdView.loadAd(adRequest);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mConversationId = getIntent().getStringExtra(ConversationDetailFragment.ARG_CONVERSATION_ID);

        Intent intent = new Intent(this, FirebaseIntentService.class);
        intent.setAction(FirebaseIntentService.ACTION_GET_USER);
        intent.putExtra(FirebaseIntentService.EXTRA_CONVERSATION_ID, mConversationId);

        startService(intent);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);


        // Show the Up button in the action bar.
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);

        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            mFirstLoad = true;

            Bundle arguments = new Bundle();
            arguments.putString(ConversationDetailFragment.ARG_CONVERSATION_ID,
                    getIntent().getStringExtra(ConversationDetailFragment.ARG_CONVERSATION_ID));
            ConversationDetailFragment fragment = new ConversationDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.conversation_detail_container, fragment)
                    .commit();
        } else {
            mFirstLoad = false;
        }

    }

    private void finishOnCreate() {
        if (mActionBar != null) {
            mActionBar.setTitle(mOtherUser.getDisplayName());
        }
        if (mFirstLoad) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Log.d(TAG, "clicked fab");
                EditText editText = (EditText) ConversationDetailActivity.this.findViewById(R.id.message_text);
                if (!TextUtils.isEmpty(editText.getText().toString())) {
                    Intent intent = new Intent(getBaseContext(), FirebaseIntentService.class);
                    intent.setAction(FirebaseIntentService.ACTION_ADD_MESSAGE);
                    intent.putExtra(FirebaseIntentService.EXTRA_CONVERSATION_ID, mConversationId);
                    intent.putExtra(FirebaseIntentService.EXTRA_UID, mUser.getUid());
                    intent.putExtra(FirebaseIntentService.EXTRA_TEXT, editText.getText().toString());
                    intent.putExtra(FirebaseIntentService.EXTRA_PHOTO_URL, mUser.getPhotoUrl().toString());
                    intent.putExtra(FirebaseIntentService.EXTRA_DISPLAY_NAME, mUser.getDisplayName());
                    intent.putExtra(FirebaseIntentService.EXTRA_OTHER_UID, mOtherUser.getUid());
                    intent.putExtra(FirebaseIntentService.EXTRA_OTHER_USERNAME, mOtherUser.getDisplayName());
                    intent.putExtra(FirebaseIntentService.EXTRA_OTHER_AVATAR, mOtherUser.getUserAvatar());
                    intent.putExtra(FirebaseIntentService.EXTRA_SOURCE_LANG, SharedPrefsUtils.getUserLanguage(ConversationDetailActivity.this));
                    intent.putExtra(FirebaseIntentService.EXTRA_TARGET_LANG, mOtherUser.getLanguage());

                    startService(intent);
                    editText.setText("");
                }
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            startActivity(new Intent(this, GoogleSignInActivity.class));
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(FirebaseIntentService.ACTION_RETURN_USER);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);
    }

    private void listenForUserChanges() {
        mUserValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mOtherUser = snapshot.getValue(User.class);
                    Log.d(TAG, "new language: " + mOtherUser.getLanguage());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        Log.d(TAG, "userId: " + mOtherUser.getUid());
        mUserQuery = myRef.child("users").child(mOtherUser.getUid());
        mUserQuery.addValueEventListener(mUserValueEventListener);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, ConversationListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

