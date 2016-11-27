package com.sam_chordas.android.stockhawk.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.WidgetItem;
import com.sam_chordas.android.stockhawk.ui.WidgetProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by griffin on 12/11/16.
 */

public class WidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private List<WidgetItem> mWidgetItems = new ArrayList<>();
        private Context mContext;
        private int mAppWidgetId;
        private String widgetSize;

        public ListRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            widgetSize = intent.getStringExtra(WidgetProvider.WIDGET_SIZE);

        }

        @Override
        public void onCreate() {
            // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
            // for example downloading or creating content etc, should be deferred to onDataSetChanged()
            // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.


            // We sleep for 3 seconds here to show how the empty view appears in the interim.
            // The empty view is set in the StackWidgetProvider and should be a sibling of the
            // collection view.
            /*try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }

        @Override
        public void onDataSetChanged() {
            mWidgetItems.clear();
            Cursor data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    null,
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null);

            if (data == null) {
                return;
            }
            if (!data.moveToFirst()) {
                data.close();
            } else {
                do {
                    WidgetItem widgetItem = new WidgetItem();
                    widgetItem.setSymbol(data.getString(data.getColumnIndex(QuoteColumns.SYMBOL)));
                    widgetItem.setBidPrice(data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));
                    widgetItem.setChange(data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                    widgetItem.setIs_up(data.getInt(data.getColumnIndex("is_up")));
                    mWidgetItems.add(widgetItem);
                }
                while (data.moveToNext());
            }

        }


        @Override
        public void onDestroy() {
            // In onDestroy() you should tear down anything that was setup for your data source,
            // eg. cursors, connections, etc.
            mWidgetItems.clear();
        }

        @Override
        public int getCount() {
            return mWidgetItems.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // We construct a remote views item based on our widget item xml file, and set the
            // text based on the position.
            if (widgetSize == null) {
                widgetSize = "default";
            }

            // Next, set a fill-intent, which will be used to fill in the pending intent template
            // that is set on the collection view in StackWidgetProvider.
            Bundle extras = new Bundle();
            extras.putInt(WidgetProvider.EXTRA_ITEM, position);
            extras.putString(WidgetProvider.SYMBOL, mWidgetItems.get(position).getSymbol());
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            // Make it possible to distinguish the individual on-click
            // action of a given item

            RemoteViews rv;
            if (widgetSize.equals("large")){
                rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_large);
                rv.setTextViewText(R.id.stock_symbol, mWidgetItems.get(position).getSymbol());
                rv.setTextViewText(R.id.bid_price, mWidgetItems.get(position).getBidPrice());
                rv.setTextViewText(R.id.change, mWidgetItems.get(position).getChange());

                if (mWidgetItems.get(position).getIs_up() == 1) {
                    rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
                return rv;
            }

            if(widgetSize.equals("small")){
                rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_small);
                rv.setTextViewText(R.id.stock_symbol, mWidgetItems.get(position).getSymbol());
                rv.setTextViewText(R.id.change, mWidgetItems.get(position).getChange());

                if (mWidgetItems.get(position).getIs_up() == 1) {
                    rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
                return rv;
            }
            else {
                rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_small);
                rv.setTextViewText(R.id.stock_symbol, mWidgetItems.get(position).getSymbol());
                rv.setTextViewText(R.id.change, mWidgetItems.get(position).getChange());

                if (mWidgetItems.get(position).getIs_up() == 1) {
                    rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
                return rv;
            }


        }

        @Override
        public RemoteViews getLoadingView() {
            // You can create a custom loading view (for instance when getViewAt() is slow.) If you
            // return null here, you will get the default loading view.
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }


}
