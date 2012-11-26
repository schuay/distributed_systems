package com.ds.billing;

import java.io.Serializable;

public class PriceStep implements Serializable, Comparable<PriceStep> {

    private static final long serialVersionUID = 1L;

    private final double startPrice;
    private final double endPrice;
    private final double fixedPrice;
    private final double variablePricePercent;

    PriceStep(double startPrice, double endPrice, double fixedPrice,
            double variablePricePercent) throws IllegalArgumentException {
        if (startPrice < 0 || endPrice < 0 || fixedPrice < 0
                || variablePricePercent < 0) {
            throw new IllegalArgumentException("All values must be non-negative.");
                }

        if (endPrice == 0) {
            endPrice = Double.POSITIVE_INFINITY;
        }

        if (startPrice > endPrice) {
            throw new IllegalArgumentException("Start Price has to be <= endPrice.");
        }

        this.startPrice = startPrice;
        this.endPrice = endPrice;
        this.fixedPrice = fixedPrice;
        this.variablePricePercent = variablePricePercent;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getEndPrice() {
        return endPrice;
    }

    public double getFixedPrice() {
        return fixedPrice;
    }

    public double getVariablePricePercent() {
        return variablePricePercent;
    }

    public boolean equalRange(double startPrice, double endPrice) {
        if (endPrice == 0) {
            endPrice = Double.POSITIVE_INFINITY;
        }

        return this.startPrice == startPrice
            && this.endPrice == endPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof PriceStep)) {
            return false;
        }

        PriceStep p = (PriceStep) o;

        return compareTo(p) == 0;
    }

    @Override
    public int compareTo(PriceStep o) {
        if (o.startPrice >= endPrice) {
            return -1;
        }
        if (startPrice >= o.endPrice) {
            return 1;
        }
        return 0;
    }
}
