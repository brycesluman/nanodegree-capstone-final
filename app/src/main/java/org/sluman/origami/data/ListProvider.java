package org.sluman.origami.data;

/*
 * Created by bryce on 4/13/17.
**/

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import org.sluman.origami.ConversationDetailFragment;
import org.sluman.origami.R;
import org.sluman.origami.models.ConversationMessageView;

/**
 * If you are familiar with Adapter of ListView,this is the same as adapter
 * with few changes
 * here it now takes RemoteFetchService ArrayList<ListItem> for data
 * which is a static ArrayList
 * and this example won't work if there are multiple widgets and
 * they update at same time i.e they modify RemoteFetchService ArrayList at same
 * time.
 * For that use Database or other techniquest
 */
public class ListProvider implements RemoteViewsFactory {
    private ArrayList<ConversationMessageView> listItemList = new ArrayList<ConversationMessageView>();
    private Context context;

    ListProvider(Context context, Intent intent) {
        this.context = context;
        Log.d("ListProvider", "ListProvider start");
        populateListItem();
    }

    private void populateListItem() {
        if(FirebaseWidgetService.listItemList !=null )
            listItemList = (ArrayList<ConversationMessageView>) FirebaseWidgetService.listItemList
                    .clone();
        else
            listItemList = new ArrayList<>();

        Log.d("ListProvider", "populateListItem() " + listItemList);
    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     *Similar to getView of Adapter where instead of View
     *we return RemoteViews
     *
     */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.conversation_list_content);
        Log.d("ListProvider", "getViewAt: " + position);

        ConversationMessageView listItem = listItemList.get(position);
        remoteView.setTextViewText(R.id.text, listItem.text);
        remoteView.setTextViewText(R.id.username, listItem.otherUsername);
        loadImageForListItem(context, listItem.otherAvatar, remoteView);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(ConversationDetailFragment.ARG_CONVERSATION_ID, listItem.getKey());
        remoteView.setOnClickFillInIntent(R.id.item_layout
                , fillInIntent);

        return remoteView;
    }
    private static void loadImageForListItem(
            Context context, String pathName, RemoteViews remoteViews) {
        int width  = 40;
        int height = 40;
        BitmapRequestBuilder builder =
                Glide.with(context)
                        .load(pathName)
                        .asBitmap()
                        .centerCrop();
        FutureTarget futureTarget = builder.into(width, height);
        try {
            remoteViews.setImageViewBitmap(R.id.user_avatar, (Bitmap) futureTarget.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

}