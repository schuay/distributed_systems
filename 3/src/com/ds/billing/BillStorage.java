package com.ds.billing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

class BillStorage {
    private static BillStorage instance;

    static synchronized BillStorage getInstance() {
        if (instance == null) {
            instance = new BillStorage();
        }

        return instance;
    }

    private final HashMap<String, List<AuctionBill> > storage =
        new HashMap<String, List<AuctionBill >>();

    private BillStorage() { }

    synchronized Bill getBill(String user) {
        return new Bill(Collections.unmodifiableList(
                    new ArrayList<AuctionBill>(getEntry(user))));
    }

    synchronized void insert(String user, AuctionBill ab) {
        getEntry(user).add(ab);
    }

    private synchronized List<AuctionBill> getEntry(String user) {
        List<AuctionBill> l = storage.get(user);
        if (l == null) {
            l = new LinkedList<AuctionBill>();
            storage.put(user, l);
        }

        return l;
    }
}
