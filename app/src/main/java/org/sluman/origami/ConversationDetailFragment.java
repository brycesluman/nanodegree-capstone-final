package org.sluman.origami;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.sluman.origami.data.FirebaseIntentService;
import org.sluman.origami.models.Message;
import org.sluman.origami.models.MessageView;
import org.sluman.origami.utils.SharedPrefsUtils;
import org.sluman.origami.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.sluman.origami.data.FirebaseIntentService.ACTION_DISPLAY_MESSAGE;
import static org.sluman.origami.data.FirebaseIntentService.EXTRA_CONVERSATION_ID;
import static org.sluman.origami.data.FirebaseIntentService.EXTRA_MESSAGE;

/**
 * A fragment representing a single ConversationMessage detail screen.
 * This fragment is either contained in a {@link ConversationListActivity}
 * in two-pane mode (on tablets) or a {@link ConversationDetailActivity}
 * on handsets.
 */
public class ConversationDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_CONVERSATION_ID = "conversation_id";
    public static final String ARG_USER = "user";

    FirebaseDatabase database = Utils.getDatabase();
    DatabaseReference myRef = database.getReference();
    Query mMessageQuery;
    ChildEventListener mMessageChildEventListener;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private RVAdapter mAdapter;

    private String mConversationId;
    private static String TAG = ConversationDetailFragment.class.getName();
    // ...
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(FirebaseIntentService.ACTION_DISPLAY_MESSAGE)) {
            final Bundle bundle = intent.getExtras();
            Message message = bundle.getParcelable(FirebaseIntentService.EXTRA_MESSAGE);
            if (mAdapter != null) {
                mAdapter.addMessage(message);
                Log.d(TAG, " " + message.text);
            }
        }
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConversationDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (getArguments().containsKey(ARG_CONVERSATION_ID)) {
            mConversationId = getArguments().getString(ConversationDetailFragment.ARG_CONVERSATION_ID);
            mAdapter = new RVAdapter(new ArrayList<Message>());

            Activity activity = this.getActivity();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.clearMessages();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FirebaseIntentService.ACTION_DISPLAY_MESSAGE);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getActivity());
        bm.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getActivity());
        bm.unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.conversation_detail, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }


    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ConversationViewHolder>{
        List<Message> messages;

        RVAdapter(List<Message> messages){
            this.messages = messages;
        }

        public void addMessage(Message message){
            this.messages.add(getItemCount(), message);
            notifyItemInserted(getItemCount() - 1);
            notifyItemRangeChanged(1, this.messages.size());
            if (mRecyclerView != null) {
                mRecyclerView.getLayoutManager().scrollToPosition(getItemCount() - 1);
                mRecyclerView.scrollToPosition(getItemCount() - 1);
            }
        }

        public void clearMessages() {
            this.messages.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (messages.get(position).uid.equals(SharedPrefsUtils.getUser(getContext()))){
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @Override
        public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int viewId = R.layout.conversation_detail_item;
            switch (viewType) {
                case 0:
                    viewId = R.layout.conversation_detail_item;
                    break;
                case 1:
                    viewId = R.layout.conversation_detail_alt_item;
                    break;
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(viewId, parent, false);
            ConversationViewHolder cvh = new ConversationViewHolder(v);
            return cvh;
        }

        @Override
        public void onBindViewHolder(final ConversationViewHolder holder, int position) {
            holder.userName.setText(Html.fromHtml(messages.get(position).username));
            holder.text.setText(Html.fromHtml(messages.get(position).text));
            holder.translatedText.setText(Html.fromHtml(messages.get(position).translatedText));
            holder.userAvatar.setContentDescription(messages.get(position).username);
            if (!messages.get(position).text.equalsIgnoreCase(messages.get(position).translatedText)) {
                if (holder.attribution != null) {
                    holder.attribution.setVisibility(View.VISIBLE);
                }
            }
            Glide.with(getActivity())
                    .load(messages.get(position).userAvatar)
                    .asBitmap().centerCrop().into(new BitmapImageViewTarget(holder.userAvatar) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    holder.userAvatar.setImageDrawable(circularBitmapDrawable);
                }
            });
            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (holder.text.getVisibility() == View.VISIBLE) {
                    holder.text.setVisibility(View.GONE);
                    holder.translatedText.setVisibility(View.VISIBLE);
                } else {
                    holder.text.setVisibility(View.VISIBLE);
                    holder.translatedText.setVisibility(View.GONE);
                }
                }
            });
        }

        public class ConversationViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rl;
            TextView userName;
            TextView text;
            TextView translatedText;
            ImageView userAvatar;
            ImageView attribution;

            ConversationViewHolder(View itemView) {
                super(itemView);
                rl = (RelativeLayout)itemView.findViewById(R.id.item_layout);
                userName = (TextView)itemView.findViewById(R.id.username);
                text = (TextView)itemView.findViewById(R.id.text);
                translatedText = (TextView)itemView.findViewById(R.id.translated_text);
                userAvatar = (ImageView)itemView.findViewById(R.id.user_avatar);
                attribution = (ImageView)itemView.findViewById(R.id.attribution);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        listenForMessages();
    }



    private void listenForMessages() {
        mMessageChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                Message message = dataSnapshot.getValue(Message.class);
                MessageView messageView = new MessageView(dataSnapshot.getKey(), message);
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

    // called to send data to Activity
    public void broadcastDisplayMessage(MessageView param) {
        Utils.refreshWidget(getContext());

        Intent intent = new Intent(ACTION_DISPLAY_MESSAGE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_MESSAGE, param);

        intent.putExtras(bundle);
        intent.putExtra(EXTRA_CONVERSATION_ID, param);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getActivity());
        bm.sendBroadcast(intent);
    }

    @Override
    public void onStop() {
        if (mMessageChildEventListener != null) {
            mMessageQuery.removeEventListener(mMessageChildEventListener);
        }
        super.onStop();
    }
}
