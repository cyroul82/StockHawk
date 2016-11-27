package com.sam_chordas.android.stockhawk.rest;

/**
 * Created by griffin on 12/11/16.
 */
public class WidgetItem {

    private String symbol;
    private String bidPrice;
    private String change;
    private int is_up;

    public WidgetItem() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(String bidPrice) {
        this.bidPrice = bidPrice;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public int getIs_up() {
        return is_up;
    }

    public void setIs_up(int is_up) {
        this.is_up = is_up;
    }
}
