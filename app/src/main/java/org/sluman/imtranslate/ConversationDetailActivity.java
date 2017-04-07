package org.sluman.imtranslate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.sluman.imtranslate.data.FirebaseIntentService;
import org.sluman.imtranslate.models.Message;
import org.sluman.imtranslate.models.MessageView;
import org.sluman.imtranslate.models.User;
import org.sluman.imtranslate.utils.SharedPrefsUtils;
import org.sluman.imtranslate.utils.Utils;

import static org.sluman.imtranslate.data.FirebaseIntentService.ACTION_DISPLAY_MESSAGE;
import static org.sluman.imtranslate.data.FirebaseIntentService.EXTRA_CONVERSATION_ID;
import static org.sluman.imtranslate.data.FirebaseIntentService.EXTRA_MESSAGE;

/**
 * An activity representing a single ConversationMessage detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ConversationListActivity}.
 */
public class ConversationDetailActivity extends BaseActivity {
    FirebaseDatabase database = Utils.getDatabase();
    private FirebaseAnalytics mFirebaseAnalytics;
    DatabaseReference myRef = database.getReference();
    private FirebaseUser mUser;
//    private User mOtherUser;
    private String mConversationId;
    private String mTargetLang;
    Query mMessageQuery;
    ChildEventListener mMessageChildEventListener;
    private static String TAG = ConversationDetailActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_detail);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        mDatabase = FirebaseDatabase.getInstance().getReference();
        mConversationId = getIntent().getStringExtra(ConversationDetailFragment.ARG_CONVERSATION_ID);
        final Bundle bundle = getIntent().getExtras();
        final User mOtherUser = bundle.getParcelable(ConversationDetailFragment.ARG_USER);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);


        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mOtherUser.getDisplayName());
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

            Bundle arguments = new Bundle();
            arguments.putString(ConversationDetailFragment.ARG_CONVERSATION_ID,
                    getIntent().getStringExtra(ConversationDetailFragment.ARG_CONVERSATION_ID));
            ConversationDetailFragment fragment = new ConversationDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.conversation_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMessageChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                Message message = dataSnapshot.getValue(Message.class);
                MessageView messageView = new MessageView(dataSnapshot.getKey(), message);
                Log.d(TAG, "text: " + message.text + " name: " + message.username);
                broadcastDisplayMessage(messageView);
                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                Message newComment = dataSnapshot.getValue(Message.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                Message movedComment = dataSnapshot.getValue(Message.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());

            }
        };
        mMessageQuery = myRef.child("messages").child(mConversationId).limitToLast(20);
        mMessageQuery.addChildEventListener(mMessageChildEventListener);
    }

    @Override
    protected void onResume() {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            // Name, email address, and profile photo Url
            String name = mUser.getDisplayName();
            String email = mUser.getEmail();
            Uri photoUrl = mUser.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = mUser.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = mUser.getUid();
        }

        super.onResume();
    }
    // called to send data to Activity
    public void broadcastDisplayMessage(MessageView param) {
        Intent intent = new Intent(ACTION_DISPLAY_MESSAGE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_MESSAGE, param);

        intent.putExtras(bundle);
        intent.putExtra(EXTRA_CONVERSATION_ID, param);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mMessageChildEventListener != null) {
            mMessageQuery.removeEventListener(mMessageChildEventListener);
        }
        super.onStop();
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

