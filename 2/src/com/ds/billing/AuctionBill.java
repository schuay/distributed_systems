package com.ds.billing;

import java.io.Serializable;

public class AuctionBill implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long auctionID;
    private final double strikePrice;
    private final double feeFixed;
    private final double feeVariable;

    AuctionBill(long auctionID, double strikePrice, PriceStep p) {
        this.auctionID = auctionID;
        this.strikePrice = strikePrice;
        this.feeFixed = p.getFixedPrice();
        this.feeVariable = p.getVariablePricePercent() * strikePrice / 100;
    }

    public long getAuctionID() {
        return auctionID;
    }

    public double getStrikePrice() {
        return strikePrice;
    }

    public double getFeeFixed() {
        return feeFixed;
    }

    public double getFeeVariable() {
        return feeVariable;
    }

    public double getFeeTotal() {
        return feeFixed + feeVariable;
    }
}
