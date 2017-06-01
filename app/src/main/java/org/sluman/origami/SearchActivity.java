package org.sluman.origami;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.sluman.origami.data.FirebaseIntentService;
import org.sluman.origami.models.User;
import org.sluman.origami.utils.ImeUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {
    SearchView mSearchView;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SearchActivity.RVAdapter mAdapter;
    private boolean mTwoPane;

    private static String TAG = SearchActivity.class.getName();

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FirebaseIntentService.ACTION_DISPLAY_USER)) {
                final Bundle bundle = intent.getExtras();
                User user = bundle.getParcelable(FirebaseIntentService.EXTRA_USER);
                if (mAdapter != null) {
                    mAdapter.addUser(user);
                    Log.d(TAG, " " + user.username);
                    mRecyclerView.setVisibility(View.VISIBLE);

                }
                hideNoResults();
            } else if (intent.getAction().equals(FirebaseIntentService.ACTION_DISPLAY_CONVERSATION)) {
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
            } else if (intent.getAction().equals(FirebaseIntentService.ACTION_NO_RESULTS)) {
                showNoResults();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mSearchView = (SearchView) findViewById(R.id.search_view);

        mAdapter = new SearchActivity.RVAdapter(new ArrayList<User>());

        mRecyclerView = (RecyclerView) findViewById(R.id.search_results);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(FirebaseIntentService.ACTION_DISPLAY_USER);
        filter.addAction(FirebaseIntentService.ACTION_NO_RESULTS);
        filter.addAction(FirebaseIntentService.ACTION_DISPLAY_CONVERSATION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);

        setupSearchView();

        TextView noResults = (TextView) findViewById(R.id.no_search_results);
        noResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShareDialog();
            }
        });

    }

    private void setupSearchView() {
        // hint, inputType & ime options seem to be ignored from XML! Set in code
        mSearchView.setQueryHint(getString(R.string.search));
        mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mSearchView.setImeOptions(mSearchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH |
                EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "SearchActivity: " + query);
                searchFor(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (TextUtils.isEmpty(query)) {
                    clearResults();
                }
                return true;
            }
        });
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            //no-op
            }
        });
    }

    private void clearResults() {
        mAdapter.clear();

    }

    private void showNoResults() {
        findViewById(R.id.no_search_results).setVisibility(View.VISIBLE);
    }

    private void hideNoResults() {
        findViewById(R.id.no_search_results).setVisibility(View.GONE);
    }

    private void searchFor(String query) {
        clearResults();
        ImeUtils.hideIme(mSearchView);
        mSearchView.clearFocus();
        Intent intent = new Intent(getBaseContext(), FirebaseIntentService.class);
        intent.setAction(FirebaseIntentService.ACTION_SEARCH_USERS);
        intent.putExtra(FirebaseIntentService.EXTRA_SEARCH_STRING, query);

        startService(intent);
    }

    public class RVAdapter extends RecyclerView.Adapter<SearchActivity.RVAdapter.UserViewHolder>{
        List<User> users;

        RVAdapter(List<User> users){
            this.users = users;
        }

        public void addUser(User user){
            this.users.add(0, user);
            notifyItemInserted(0);
            if (mRecyclerView != null) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        }


        public void clear() {
            this.users.clear();
            notifyDataSetChanged();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        @Override
        public SearchActivity.RVAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
            SearchActivity.RVAdapter.UserViewHolder cvh = new SearchActivity.RVAdapter.UserViewHolder(v);
            return cvh;
        }

        @Override
        public void onBindViewHolder(final SearchActivity.RVAdapter.UserViewHolder holder, int position) {
            holder.userName.setText(users.get(position).username);

            Glide.with(getApplicationContext())
                    .load(users.get(position).userAvatar)
                    .asBitmap().centerCrop().into(new BitmapImageViewTarget(holder.userAvatar) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    holder.userAvatar.setImageDrawable(circularBitmapDrawable);
                }
            });
            holder.displayName.setText(users.get(position).displayName);
            holder.Uid = users.get(position).getUid();
            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), FirebaseIntentService.class);
                intent.setAction(FirebaseIntentService.ACTION_NEW_CONVERSATION);
                intent.putExtra(FirebaseIntentService.EXTRA_UID, holder.Uid);

                startService(intent);
                }
            });
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rl;
            String Uid;
            TextView userName;
            TextView displayName;
            ImageView userAvatar;

            UserViewHolder(View itemView) {
                super(itemView);
                rl = (RelativeLayout)itemView.findViewById(R.id.item_layout);
                userAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
                userName = (TextView) itemView.findViewById(R.id.username);
                displayName = (TextView) itemView.findViewById(R.id.display_name);
            }
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}
