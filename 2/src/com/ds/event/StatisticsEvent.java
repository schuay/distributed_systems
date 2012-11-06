package com.ds.event;

public class StatisticsEvent extends Event {

    private static final long serialVersionUID = 1L;

    public static final String USER_SESSIONTIME_MIN = "USER_SESSIONTIME_MIN";
    public static final String USER_SESSIONTIME_MAX = "USER_";
    public static final String USER_SESSIONTIME_AVG = "USER_";
    public static final String BID_PRICE_MAX = "USER_";
    public static final String BID_COUNT_PER_MINUTE = "USER_";
    public static final String AUCTION_TIME_AVG = "USER_";
    public static final String AUCTION_SUCCESS_RATIO = "USER_";

    private final double value;

    public StatisticsEvent(String type, double value) {
        super(type);
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
