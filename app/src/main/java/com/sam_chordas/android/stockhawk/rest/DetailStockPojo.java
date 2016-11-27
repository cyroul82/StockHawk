package com.sam_chordas.android.stockhawk.rest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by griffin on 30/10/16.
 */

public class DetailStockPojo implements Parcelable {

    private String symbol;
    private String date;
    private String high;
    private String low;
    private String open;
    private String close;

    public DetailStockPojo() {
    }

    public DetailStockPojo(String close, String symbol, String date, String high, String low, String open) {
        this.close = close;
        this.symbol = symbol;
        this.date = date;
        this.high = high;
        this.low = low;
        this.open = open;
    }

    protected DetailStockPojo(Parcel in) {
        symbol = in.readString();
        date = in.readString();
        high = in.readString();
        low = in.readString();
        open = in.readString();
        close = in.readString();
    }

    public static final Creator<DetailStockPojo> CREATOR = new Creator<DetailStockPojo>() {
        @Override
        public DetailStockPojo createFromParcel(Parcel in) {
            return new DetailStockPojo(in);
        }

        @Override
        public DetailStockPojo[] newArray(int size) {
            return new DetailStockPojo[size];
        }
    };

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(symbol);
        dest.writeString(date);
        dest.writeString(high);
        dest.writeString(low);
        dest.writeString(open);
        dest.writeString(close);
    }
}
