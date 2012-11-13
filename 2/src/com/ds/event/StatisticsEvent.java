package com.ds.event;

public class StatisticsEvent extends Event {

    private static final long serialVersionUID = 1L;

    public static final String USER_SESSIONTIME_MIN = "USER_SESSIONTIME_MIN";
    public static final String USER_SESSIONTIME_MAX = "USER_SESSIONTIME_MAX";
    public static final String USER_SESSIONTIME_AVG = "USER_SESSIONTIME_AVG";
    public static final String BID_PRICE_MAX = "BID_PRICE_MAX";
    public static final String BID_COUNT_PER_MINUTE = "BID_COUNT_PER_MINUTE";
    public static final String AUCTION_TIME_AVG = "AUCTION_TIME_AVG";
    public static final String AUCTION_SUCCESS_RATIO = "AUCTION_SUCCESS_RATIO";

    private final double value;

    public StatisticsEvent(String type, double value) {
        super(type);
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
