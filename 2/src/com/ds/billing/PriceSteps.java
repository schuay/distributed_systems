package com.ds.billing;

import java.io.Serializable;
import java.util.List;

public class PriceSteps implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<PriceStep> priceSteps;

    PriceSteps(List<PriceStep> priceSteps) {
        this.priceSteps = priceSteps;
    }

    public List<PriceStep> getPriceSteps() {
        return priceSteps;
    }
}
