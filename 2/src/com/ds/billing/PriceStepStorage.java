package com.ds.billing;

import java.util.ArrayList;
import java.util.TreeSet;

class PriceStepStorage {
    private static PriceStepStorage instance;

    static synchronized PriceStepStorage getInstance() {
        if (instance == null) {
            instance = new PriceStepStorage();
        }

        return instance;
    }

    private final TreeSet<PriceStep> storage = new TreeSet<PriceStep>();

    private PriceStepStorage() {
        insert(new PriceStep(0, 100, 3, 7));
        insert(new PriceStep(100, 200, 5, 6.5));
        insert(new PriceStep(200, 500, 7, 6));
        insert(new PriceStep(500, 1000, 10, 5.5));
        insert(new PriceStep(1000, 0, 15, 5));
    }

    synchronized PriceSteps getPriceSteps() {
        return new PriceSteps(new ArrayList<PriceStep>(storage));
    }

    synchronized boolean insert(PriceStep s) {
        if (storage.contains(s)) {
            return false;
        }

        storage.add(s);
        return true;
    }

    synchronized boolean delete(double startPrice, double endPrice) {
        for (PriceStep p : storage) {
            if (p.equalRange(startPrice, endPrice)) {
                storage.remove(p);
                return true;
            }
        }

        return false;
    }
}
