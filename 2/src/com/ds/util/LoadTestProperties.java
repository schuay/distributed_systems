package com.ds.util;

import java.io.IOException;

public class LoadTestProperties extends SimpleProperties {

    public LoadTestProperties() throws IOException {
        super("loadtest.properties");
    }

    public int getClients() {
        return Integer.parseInt(getProperty("clients"));
    }

    public long getAuctionsPerMin() {
        return Long.parseLong(getProperty("auctionsPerMin"));
    }

    public long getAuctionDuration() {
        return Long.parseLong(getProperty("auctionDuration"));
    }

    public long getUpdateIntervalSec() {
        return Long.parseLong(getProperty("updateIntervalSec"));
    }

    public long getBidsPerMin() {
        return Long.parseLong(getProperty("bidsPerMin"));
    }
}
