package org.sluman.imtranslate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.PatternMatcher;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.sluman.imtranslate.data.FirebaseIntentService;
import org.sluman.imtranslate.dummy.DummyContent;
import org.sluman.imtranslate.models.Message;
import org.sluman.imtranslate.utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private RVAdapter mAdapter;
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
//                final String param = intent.getStringExtra(FirebaseIntentService.ACTION_DISPLAY_MESSAGE);
//                Toast.makeText(getApplicationContext(), param, Toast.LENGTH_LONG).show();
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
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
//            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            mAdapter = new RVAdapter(new ArrayList<Message>());



            Activity activity = this.getActivity();
//            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
//            if (appBarLayout != null) {
//                appBarLayout.setTitle(mItem.content);
//            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.clearMessages();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FirebaseIntentService.ACTION_DISPLAY_MESSAGE);
//            filter.addDataPath(getArguments().getString(ARG_CONVERSATION_ID), PatternMatcher.PATTERN_LITERAL);
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

        // Show the dummy content as text in a TextView.
//        if (mItem != null) {
//            ((TextView) rootView.findViewById(R.id.conversation_detail)).setText(mItem.details);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
//        }

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
                Log.d(TAG, "messages size: " + (getItemCount() - 1));
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
            holder.userName.setText(messages.get(position).username);
            holder.text.setText(messages.get(position).text);
            holder.translatedText.setText(messages.get(position).translatedText);
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

            ConversationViewHolder(View itemView) {
                super(itemView);
                rl = (RelativeLayout)itemView.findViewById(R.id.item_layout);
                userName = (TextView)itemView.findViewById(R.id.username);
                text = (TextView)itemView.findViewById(R.id.text);
                translatedText = (TextView)itemView.findViewById(R.id.translated_text);
                userAvatar = (ImageView)itemView.findViewById(R.id.user_avatar);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
