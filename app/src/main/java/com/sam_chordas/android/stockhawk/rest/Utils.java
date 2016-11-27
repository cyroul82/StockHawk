package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static final String ACTION_DATA_UPDATED = "com.sam_chordas.android.stockhawk.ACTION_DATA_UPDATED";

    public static ArrayList quoteJsonToContentVals(Context context, String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray resultsArray;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    if(jsonObject.getString("Bid").equals("null") || jsonObject.getString("Name").equals("null")){
                        batchOperations = null;
                    }
                    else {
                        batchOperations.add(buildBatchOperation(jsonObject));
                    }
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);

                            if(jsonObject.getString("Bid").equals("null") || jsonObject.getString("Name").equals("null")){
                                batchOperations = null;
                            }
                            else {
                                if (batchOperations != null) {
                                    batchOperations.add(buildBatchOperation(jsonObject));
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    private static String truncateBidPrice(String bidPrice) {
        if(bidPrice.equals("null")){
            bidPrice = String.format(Locale.getDefault(), "%.2f", Float.parseFloat("0"));
        }
        else {
            bidPrice = String.format(Locale.getDefault(), "%.2f", Float.parseFloat(bidPrice));
        }
        return bidPrice;
    }

    private static String truncateChange(String change, boolean isPercentChange) {
        if(change.equals("null")){
            change = "";
        }
        else {
            String weight = change.substring(0, 1);
            String ampersand = "";
            if (isPercentChange) {
                ampersand = change.substring(change.length() - 1, change.length());
                change = change.substring(0, change.length() - 1);
            }
            change = change.substring(1, change.length());
            double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
            change = String.format(Locale.getDefault(), "%.2f", round);
            StringBuffer changeBuffer = new StringBuffer(change);
            changeBuffer.insert(0, weight);
            changeBuffer.append(ampersand);
            change = changeBuffer.toString();
        }
        return change;
    }

    private static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.NAME, jsonObject.getString("Name"));
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol").toUpperCase());
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.DAY_LOW, jsonObject.getString("DaysLow"));
            builder.withValue(QuoteColumns.DAY_HIGH, jsonObject.getString("DaysHigh"));
            builder.withValue(QuoteColumns.YEAR_LOW, jsonObject.getString("YearLow"));
            builder.withValue(QuoteColumns.YEAR_HIGH, jsonObject.getString("YearHigh"));
            builder.withValue(QuoteColumns.CURRENCY, jsonObject.getString("Currency"));
            builder.withValue(QuoteColumns.PREVIOUS_CLOSE, jsonObject.getString("PreviousClose"));
            builder.withValue(QuoteColumns.OPEN, jsonObject.getString("Open"));
            builder.withValue(QuoteColumns.LAST_TRADE_DATE, jsonObject.getString("LastTradeDate"));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    private static DetailStockPojo buildDetailStock(JSONObject jsonObject) {
        DetailStockPojo detailStockPojo = new DetailStockPojo();
        try {
            detailStockPojo.setSymbol(jsonObject.getString("Symbol"));
            detailStockPojo.setDate(jsonObject.getString("Date"));
            detailStockPojo.setOpen(jsonObject.getString("Open"));
            detailStockPojo.setHigh(jsonObject.getString("High"));
            detailStockPojo.setLow(jsonObject.getString("Low"));
            detailStockPojo.setClose(jsonObject.getString("Close"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return detailStockPojo;
    }

    public static ArrayList<DetailStockPojo> quoteJsonToDetailStockVals(String JSON) {
        ArrayList<DetailStockPojo> details = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray resultsArray;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    details.add(buildDetailStock(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            details.add(buildDetailStock(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return details;
    }

    public static void displayToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static  void updateWidgets(Context context) {
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}
