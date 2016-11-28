package com.sam_chordas.android.stockhawk.ui;

import android.animation.PropertyValuesHolder;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.animation.Animation;
import com.db.chart.animation.easing.BounceEase;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.DetailStockPojo;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by griffin on 30/10/16.
 */

public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = StockDetailActivity.class.getSimpleName();
    private Context mContext;

    private String mYearLow;
    private String mYearHigh;

    private final static String START_DATE = "start_date";
    private final static String END_DATE = "end_date";
    private String mStartDate;
    private String mEndDate;
    private Tooltip mTip;

    private LineChartView mChart;
    private static final int CURSOR_LOADER_ID = 1;

    private String mSymbol;

    public static final String SYMBOL = "symbol";

    @BindView(R.id.dayslow)
    TextView mTextViewDaysLow;
    @BindView(R.id.textViewStockTitle)
    TextView mTextViewStockTitle;
    @BindView(R.id.dayshigh)
    TextView mTextViewDaysHigh;
    @BindView(R.id.textViewPreviousClose)
    TextView mTextViewPreviousClose;
    @BindView(R.id.textViewOpen)
    TextView mTextViewOpen;
    @BindView(R.id.textView_currency)
    TextView mTextViewCurrency;
    @BindView(R.id.textViewDate)
    TextView mTextViewDate;
    @BindView (R.id.textViewBid)
    TextView mTextViewBid;
    @BindView(R.id.toolbar_detail)
    Toolbar toolbar;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_line_graph);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LineChartReceiver lineChartReceiver = new LineChartReceiver(this);
        IntentFilter filter = new IntentFilter("overtime_data");
        LocalBroadcastManager.getInstance(this).registerReceiver(lineChartReceiver, filter);

        //get the mSymbol from the intent
        mSymbol = getIntent().getStringExtra(SYMBOL);

        if(savedInstanceState == null){
            startIntentService(null, null);
        }
        else {
            mStartDate = savedInstanceState.getString(START_DATE);
            mEndDate = savedInstanceState.getString(END_DATE);
            startIntentService(mStartDate, mEndDate);
        }

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        // Tooltip
        mTip = new Tooltip(this, R.layout.line_chart_tooltip, R.id.value);

        ((TextView) mTip.findViewById(R.id.value)).setTypeface(
                Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/OpenSans-Semibold.ttf"));

        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(58), (int) Tools.fromDpToPx(25));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

            mTip.setPivotX(Tools.fromDpToPx(65) / 2);
            mTip.setPivotY(Tools.fromDpToPx(25));
        }

        mChart = (LineChartView) findViewById(R.id.linechart);

        if (mChart != null) {
            mChart.setTooltips(mTip);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(START_DATE, mStartDate);
        outState.putString(END_DATE, mEndDate);
        super.onSaveInstanceState(outState);

    }

    private void startIntentService(String startDate, String endDate){
        mStartDate = startDate;
        mEndDate = endDate;
        Intent intent = new Intent(this, StockIntentService.class);
        intent.putExtra("tag", "overtime");
        intent.putExtra("symbol", mSymbol);
        intent.putExtra("startDate", mStartDate);
        intent.putExtra("endDate", mEndDate);
        startService(intent);
    }


    public void onClickButtonHistoricalData(View view) {
        int idButton = view.getId();
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String endDate = df.format(c.getTime());
        String startDate;

        switch (idButton) {
            case R.id.button_10d:
                c.add(Calendar.DAY_OF_MONTH, -10);
                startDate = df.format(c.getTime());
                startIntentService(startDate, endDate);
                break;
            case R.id.button_1m:
                c.add(Calendar.MONTH, -1);
                startDate = df.format(c.getTime());
                startIntentService(startDate, endDate);
                break;
            case R.id.button_3m:
                c.add(Calendar.MONTH, -3);
                startDate = df.format(c.getTime());
                startIntentService(startDate, endDate);
                break;
            case R.id.button_6m:
                c.add(Calendar.MONTH, -6);
                startDate = df.format(c.getTime());
                startIntentService(startDate, endDate);
                break;
            case R.id.button_1y:
                c.add(Calendar.YEAR, -1);
                startDate = df.format(c.getTime());
                startIntentService(startDate, endDate);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                null,
                QuoteColumns.SYMBOL + " = ? and " + QuoteColumns.ISCURRENT + " = ?",
                new String[]{mSymbol, "1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (data.moveToFirst()) {
                setTextView(data);
            }
        }
    }

    private void setTextView(Cursor data){
        String mDayHigh = data.getString(data.getColumnIndex(QuoteColumns.DAY_HIGH));
        String mDayLow = data.getString(data.getColumnIndex(QuoteColumns.DAY_LOW));
        mTextViewDaysLow.setText(mDayLow);
        mTextViewDaysHigh.setText(mDayHigh);
        mYearHigh = data.getString(data.getColumnIndex(QuoteColumns.YEAR_HIGH));
        mYearLow = data.getString(data.getColumnIndex(QuoteColumns.YEAR_LOW));
        mTextViewOpen.setText(data.getString(data.getColumnIndex(QuoteColumns.OPEN)));
        mTextViewPreviousClose.setText(data.getString(data.getColumnIndex(QuoteColumns.PREVIOUS_CLOSE)));
        mTextViewCurrency.setText(data.getString(data.getColumnIndex(QuoteColumns.CURRENCY)));
        //mTextViewBid.setText(data.getString(data.getColumnIndex("bid_price")));

        int sdk = Build.VERSION.SDK_INT;
        if (data.getInt(data.getColumnIndex("is_up")) == 1){
            if (sdk < Build.VERSION_CODES.JELLY_BEAN){
                mTextViewBid.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }else {
                mTextViewBid.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }
        } else{
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                mTextViewBid.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            } else{
                mTextViewBid.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            }
        }
        if (Utils.showPercent){
            mTextViewBid.setText(data.getString(data.getColumnIndex("percent_change")));
        } else{
            mTextViewBid.setText(data.getString(data.getColumnIndex("change")));
        }

        String date = data.getString(data.getColumnIndex(QuoteColumns.LAST_TRADE_DATE));
        DateFormat df1 = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        DateFormat df = new SimpleDateFormat("EEE, MMM d, ''yy", Locale.getDefault());

        try {
            Date d = df1.parse(date);
            mTextViewDate.setText(df.format(d));
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing date : " + e.getMessage());
        }


        StringBuilder s = new StringBuilder();
        s.append(data.getString(data.getColumnIndex(QuoteColumns.NAME)));
        s.append(" ");
        s.append(data.getString(data.getColumnIndex(QuoteColumns.SYMBOL)));

        mTextViewStockTitle.setText(s.toString());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private class LineChartReceiver extends BroadcastReceiver {

        ProgressDialog progressDialog;

        public LineChartReceiver(Context context) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetailStockPojo> details = intent.getParcelableArrayListExtra("data");

            int position = 0;
            String rangeLow;
            String rangeHigh;

            if (details.size() != 0) {

                Collections.sort(details, new maxComparator());
                DetailStockPojo ds = details.get(details.size()-1);
                rangeHigh = ds.getClose();
                rangeLow = details.get(0).getClose();
                //Sort the date
                Collections.sort(details, new dateComparator());

                //Instantiate the 2 array used in the data lineset
                String[] mLabels = new String[details.size()];
                final float[] mValues = new float[details.size()];

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                DateFormat df2 = new SimpleDateFormat("MMM", Locale.getDefault());
                DateFormat df3 = new SimpleDateFormat("MMM d", Locale.getDefault());

                for (int i = 0; i < details.size(); i++) {
                    DetailStockPojo detailStockPojo = details.get(i);

                    if(detailStockPojo.equals(ds)){
                        position = i;
                    }

                    try {
                        Date d = df.parse(detailStockPojo.getDate());
                        Calendar c = Calendar.getInstance();
                        c.setTime(d);
                        if(details.size() < 15){
                            mLabels[i] = df3.format(d);
                        }
                        if(details.size() >15 && details.size() <30){
                            if(i==0 || i==7 || i==15 || i==22 || i== details.size()-1){
                                mLabels[i] = df3.format(d);
                            }
                            else {
                                mLabels[i] = "";
                            }
                        }
                        if(details.size() > 30){
                            if(i==0){
                                mLabels[i] = df2.format(d);
                            }
                            else {
                                //get the day from the date
                                int day = c.get(Calendar.DAY_OF_MONTH);
                                int month = c.get(Calendar.MONTH);

                                //set the calendar to the first day of the month
                                c.set(Calendar.DAY_OF_MONTH, 1);

                                if (month == Calendar.JANUARY) {
                                    c.set(Calendar.DAY_OF_MONTH, 2);
                                }

                                //get the first day of week
                                int firstDayOfWeek = c.get(Calendar.DAY_OF_WEEK);

                                if (firstDayOfWeek == Calendar.SATURDAY) {
                                    c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 2);
                                }
                                if (firstDayOfWeek == Calendar.SUNDAY) {
                                    c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
                                }

                                int firstDay = c.get(Calendar.DAY_OF_MONTH);

                                if (day == firstDay) {
                                    mLabels[i] = df2.format(d);
                                } else {
                                    mLabels[i] = "";
                                }
                            }
                        }

                        //Add the point value
                        mValues[i] = Float.parseFloat(detailStockPojo.getClose());

                    } catch (ParseException e) {
                        Log.e(LOG_TAG, "Error parsing date : " + e.getMessage());
                    }

                }

                // Data
                LineSet dataset = new LineSet(mLabels, mValues);
                dataset.setColor(Color.parseColor("#b3b5bb"))
                        .setFill(Color.parseColor("#2d374c"))
                        .setDotsColor(Color.parseColor("#ffc755"))
                        .setDotsRadius(5)
                        .setThickness(4)
                        .beginAt(0);
                mChart.getData().clear();
                mChart.addData(dataset);

                // Chart
                mChart.setBorderSpacing(Tools.fromDpToPx(15))
                        .setAxisBorderValues(Math.round(Float.parseFloat(mYearLow)), Math.round(Float.parseFloat(mYearHigh)))
                        .setYLabels(AxisRenderer.LabelPosition.OUTSIDE)
                        .setLabelsColor(Color.parseColor("#6a84c3"))
                        .setXAxis(false)
                        .setYAxis(false)
                        ///*.setValueThreshold(mValues[0], mValues[mValues.length-1], new Paint())*/
                        .setGrid(ChartView.GridType.FULL, new Paint());


                final int finalPosition = position;
                Runnable chartAction = new Runnable() {
                    @Override
                    public void run() {
                        mTip.prepare(mChart.getEntriesArea(0).get(finalPosition), mValues[finalPosition]);
                        mChart.removeAllViews();
                        mChart.showTooltip(mTip, true);
                    }
                };

                Animation anim = new Animation().setEasing(new BounceEase()).setEndAction(chartAction);
                mChart.show(anim);
            }
            else {
                Utils.displayToast(mContext, getString(R.string.network_toast));
            }

            progressDialog.dismiss();
        }
    }

    private class dateComparator implements Comparator<DetailStockPojo> {

        @Override
        public int compare(DetailStockPojo a, DetailStockPojo b) {
            return a.getDate().compareToIgnoreCase(b.getDate());
        }
    }

    private class maxComparator implements Comparator<DetailStockPojo> {

        @Override
        public int compare(DetailStockPojo a, DetailStockPojo b) {
            return a.getClose().compareToIgnoreCase(b.getClose());
        }
    }

}
