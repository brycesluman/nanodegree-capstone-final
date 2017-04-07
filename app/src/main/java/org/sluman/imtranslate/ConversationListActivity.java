package org.sluman.imtranslate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.sluman.imtranslate.data.FirebaseIntentService;
import org.sluman.imtranslate.models.ConversationMessage;
import org.sluman.imtranslate.models.ConversationMessageView;
import org.sluman.imtranslate.models.Message;
import org.sluman.imtranslate.models.User;
import org.sluman.imtranslate.utils.SharedPrefsUtils;
import org.sluman.imtranslate.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.sluman.imtranslate.data.FirebaseIntentService.ACTION_DISPLAY_USER_CONVERSATIONS;
import static org.sluman.imtranslate.data.FirebaseIntentService.ACTION_UPDATE_CONVERSATION;
import static org.sluman.imtranslate.data.FirebaseIntentService.EXTRA_MESSAGE;

/**
 * An activity representing a list of Conversations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ConversationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ConversationListActivity extends BaseActivity {
    // handler for received data from service
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FirebaseIntentService.ACTION_DISPLAY_USER_CONVERSATIONS)) {
                final Bundle bundle = intent.getExtras();
                ConversationMessageView message = bundle.getParcelable(FirebaseIntentService.EXTRA_MESSAGE);
                if (mAdapter != null) {
                    mAdapter.addMessage(message);
                    Log.d(TAG, " " + message.text);
                }
            } else if (intent.getAction().equals(FirebaseIntentService.ACTION_UPDATE_CONVERSATION)) {
                final Bundle bundle = intent.getExtras();
                ConversationMessageView message = bundle.getParcelable(FirebaseIntentService.EXTRA_MESSAGE);
                if (mAdapter != null) {
                    mAdapter.updateMessage(message);
                    Log.d(TAG, " " + message.text);
                }
            } else if (intent.getAction().equals(FirebaseIntentService.ACTION_DISPLAY_CONVERSATION)) {
                Log.d(TAG, "display conversation: " + intent.getStringExtra(FirebaseIntentService.EXTRA_CONVERSATION_ID));
                final Bundle bundle = intent.getExtras();
                User user = bundle.getParcelable(FirebaseIntentService.EXTRA_USER);
                Intent conversationIntent = new Intent(context, ConversationDetailActivity.class);
                Bundle conversationBundle = new Bundle();
                conversationBundle.putParcelable(ConversationDetailFragment.ARG_USER, user);
                conversationIntent.putExtra(ConversationDetailFragment.ARG_CONVERSATION_ID,
                        intent.getStringExtra(FirebaseIntentService.EXTRA_CONVERSATION_ID));
                conversationIntent.putExtras(conversationBundle);
                conversationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(conversationIntent);
            }
        }
    };
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    FirebaseDatabase database = Utils.getDatabase();
    DatabaseReference myRef = database.getReference();
    ChildEventListener mConversationChildEventListener;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean mTwoPane;
    private SimpleItemRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private static String TAG = ConversationListActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conversation_list);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mAdapter = new SimpleItemRecyclerViewAdapter(new ArrayList<ConversationMessageView>());
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.conversation_list);
        assert mRecyclerView != null;
        setupRecyclerView((RecyclerView) mRecyclerView);

        if (findViewById(R.id.conversation_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

    }
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_in_out:
                Intent intent = new Intent(this, GoogleSignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.open_language_picker:
                startActivity(new Intent(this, LanguagesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onStop() {
        if (mConversationChildEventListener != null) {
            myRef.child("user-conversations").child(SharedPrefsUtils.getUser(getApplicationContext())).removeEventListener(mConversationChildEventListener);
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mConversationChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                ConversationMessage message = dataSnapshot.getValue(ConversationMessage.class);
                ConversationMessageView messageView = new ConversationMessageView(dataSnapshot.getKey(), message);
                Log.d(TAG, "text: " + message.text + " name: " + message.username);
                broadcastDisplayUserConversations(messageView);
                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                ConversationMessage message = dataSnapshot.getValue(ConversationMessage.class);
                ConversationMessageView messageView = new ConversationMessageView(dataSnapshot.getKey(), message);
                Log.d(TAG, "text: " + message.text + " name: " + message.username);
                broadcastUpdateUserConversations(messageView);
                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.

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
        myRef.child("user-conversations").child(SharedPrefsUtils.getUser(getApplicationContext())).addChildEventListener(mConversationChildEventListener);

    }

    public void broadcastUpdateUserConversations(ConversationMessageView param) {
        Log.d(TAG, "update conversation: " + param);
        Intent intent = new Intent(ACTION_UPDATE_CONVERSATION);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_MESSAGE, param);
        intent.putExtras(bundle);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }

    public void broadcastDisplayUserConversations(ConversationMessage param) {
        Log.d(TAG, "broadcast user conversations: " + param.username);
        Intent intent = new Intent(ACTION_DISPLAY_USER_CONVERSATIONS);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_MESSAGE, param);
        intent.putExtras(bundle);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, GoogleSignInActivity.class));
            return;
        }
        mAdapter.clearMessages();

        IntentFilter filter = new IntentFilter();
        filter.addAction(FirebaseIntentService.ACTION_DISPLAY_USER_CONVERSATIONS);
        filter.addAction(FirebaseIntentService.ACTION_UPDATE_CONVERSATION);
        filter.addAction(FirebaseIntentService.ACTION_DISPLAY_CONVERSATION);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(mAdapter);
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class EmptyViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rl;
            public EmptyViewHolder(View itemView) {
                super(itemView);
                rl = (RelativeLayout)itemView.findViewById(R.id.item_layout);
            }
        }

        private final List<ConversationMessageView> messages;

        private static final int EMPTY_VIEW = 10;

        public void addMessage(ConversationMessageView message){
            this.messages.add(0, message);
            notifyItemRangeRemoved(0, 1);
            notifyItemInserted(0);
        }

        public void clearMessages() {
            this.messages.clear();
            notifyDataSetChanged();
        }

        public void updateMessage(ConversationMessageView message) {
            int index = 0;
            for(ConversationMessageView o : this.messages) {
                if(o != null && o.getKey().equals(message.getKey())) {
                    this.messages.set(index, message);
                    notifyItemChanged(index);
                }
                index++;
            }
        }

        public SimpleItemRecyclerViewAdapter(List<ConversationMessageView> messages){
            this.messages = messages;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;

            if (viewType == EMPTY_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
                EmptyViewHolder evh = new EmptyViewHolder(v);
                return evh;
            }
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.conversation_list_content, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final MyViewHolder vh = (MyViewHolder) holder;
                vh.mItem = messages.get(position);
                vh.mKey = messages.get(position).getKey();
                vh.userName.setText(messages.get(position).otherUsername);
                vh.text.setText(messages.get(position).text);
                vh.translatedText.setText(messages.get(position).translatedText);
                Glide.with(ConversationListActivity.this)
                        .load(messages.get(position).otherAvatar)
                        .asBitmap().centerCrop().into(new BitmapImageViewTarget(vh.userAvatar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        vh.userAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                });

                vh.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getBaseContext(), FirebaseIntentService.class);
                        intent.setAction(FirebaseIntentService.ACTION_TRY_OPEN_CONVERSATION);
                        intent.putExtra(FirebaseIntentService.EXTRA_CONVERSATION_ID, vh.mKey);

                        startService(intent);
                    }
                });
            } else {
                 final EmptyViewHolder vh = (EmptyViewHolder) holder;
                vh.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return messages.size() > 0 ? messages.size() : 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (messages.size() == 0) {
                return EMPTY_VIEW;
            }
            return super.getItemViewType(position);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rl;
            String mKey;
            TextView userName;
            TextView text;
            TextView translatedText;
            ImageView userAvatar;
            ConversationMessageView mItem;

            MyViewHolder(View itemView) {
                super(itemView);
                rl = (RelativeLayout)itemView.findViewById(R.id.item_layout);
                userName = (TextView)itemView.findViewById(R.id.username);
                text = (TextView)itemView.findViewById(R.id.text);
                translatedText = (TextView)itemView.findViewById(R.id.translated_text);
                userAvatar = (ImageView)itemView.findViewById(R.id.user_avatar);
            }
        }
    }
}
