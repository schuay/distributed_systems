package com.ds.util;

import java.io.IOException;

public class LoadTestProperties extends SimpleProperties {

    protected LoadTestProperties() throws IOException {
        super("loadtest.properties");
    }

    public String getClients() {
        return getProperty("clients");
    }

    public String getAuctionsPerMin() {
        return getProperty("auctionsPerMin");
    }

    public String getAuctionDuration() {
        return getProperty("auctionDuration");
    }

    public String getUpdateIntervalSec() {
        return getProperty("updateIntervalSec");
    }

    public String getBidsPerMin() {
        return getProperty("bidsPerMin");
    }
}
