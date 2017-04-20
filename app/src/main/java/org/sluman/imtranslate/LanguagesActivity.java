package org.sluman.imtranslate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.sluman.imtranslate.data.FirebaseIntentService;
import org.sluman.imtranslate.models.LanguageView;

import java.util.ArrayList;
import java.util.List;

public class LanguagesActivity extends BaseActivity {
    RecyclerView mRecyclerView;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(FirebaseIntentService.ACTION_DISPLAY_LANGUAGE_VIEW)) {
            final Bundle bundle = intent.getExtras();
            LanguageView languageView = bundle.getParcelable(FirebaseIntentService.EXTRA_LANGUAGE_VIEW);
            if (mAdapter != null) {
                mAdapter.addLanguage(languageView);
            }
        }
        }
    };

    private SimpleItemRecyclerViewAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_languages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle(getString(R.string.support_languages));
        }

        mAdapter = new SimpleItemRecyclerViewAdapter(new ArrayList<LanguageView>());

        mRecyclerView = (RecyclerView) findViewById(R.id.language_list);
        assert mRecyclerView != null;
        setupRecyclerView(mRecyclerView);

        Intent intent = new Intent(this, FirebaseIntentService.class);
        intent.setAction(FirebaseIntentService.ACTION_DISPLAY_LANGUAGES);

        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.clearLanguages();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FirebaseIntentService.ACTION_DISPLAY_LANGUAGE_VIEW);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.scrollToPosition(0);
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private int selectedPosition;
        private final List<LanguageView> languages;

        public void addLanguage(LanguageView languageView){
            this.languages.add(this.languages.size(), languageView);
            notifyItemInserted(this.languages.size());
            if (languageView.isSelected()) {
                mRecyclerView.scrollToPosition(this.languages.size()-5);
            }
        }

        public void clearLanguages() {
            this.languages.clear();
            notifyDataSetChanged();
        }

        public SimpleItemRecyclerViewAdapter(List<LanguageView> languages){
            this.languages = languages;
        }

        @Override
        public SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.language_list_item, parent, false);
            return new SimpleItemRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {
            holder.mItem = languages.get(position);
            holder.mNameView.setText(languages.get(position).getName());
            holder.mCodeView.setText(languages.get(position).getCode());
            if (languages.get(position).isSelected()) {
                selectedPosition = position;
                holder.mView.setBackgroundColor(Color.LTGRAY);
            } else {
                holder.mView.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //reset view here
                    for (LanguageView languageView : languages) {
                        languageView.setSelected(false);
                    }
                    holder.mItem.setSelected(true);
                    notifyItemChanged(selectedPosition);
                    selectedPosition = holder.getAdapterPosition();
                    notifyItemChanged(selectedPosition);

                    Intent intent = new Intent(getBaseContext(), FirebaseIntentService.class);
                    intent.setAction(FirebaseIntentService.ACTION_SET_LANGUAGE);
                    intent.putExtra(FirebaseIntentService.EXTRA_LANGUAGE, holder.mItem.getCode());

                    startService(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return languages.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mNameView;
            public final TextView mCodeView;
            public LanguageView mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mNameView = (TextView) view.findViewById(R.id.language_name);
                mCodeView = (TextView) view.findViewById(R.id.language_code);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNameView.getText() + "'";
            }
        }
    }
}
