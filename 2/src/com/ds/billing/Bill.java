package com.ds.billing;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<AuctionBill> bills;

    Bill(List<AuctionBill> bills) {
        this.bills = bills;
    }
    public List<AuctionBill> getAuctionBills() {
        return bills;
    }
}
